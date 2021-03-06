package com.test.rpc.akka.flink8;

import akka.actor.ActorRef;
import akka.actor.Status;
import akka.actor.UntypedActor;
import akka.pattern.Patterns;
import org.apache.flink.runtime.concurrent.FutureUtils;
import org.apache.flink.runtime.rpc.MainThreadValidatorUtil;
import org.apache.flink.runtime.rpc.RpcEndpoint;
import org.apache.flink.runtime.rpc.RpcGateway;
import org.apache.flink.runtime.rpc.akka.ControlMessages;
import org.apache.flink.runtime.rpc.akka.exceptions.AkkaHandshakeException;
import org.apache.flink.runtime.rpc.akka.exceptions.AkkaRpcException;
import org.apache.flink.runtime.rpc.akka.exceptions.AkkaRpcInvalidStateException;
import org.apache.flink.runtime.rpc.akka.exceptions.AkkaUnknownMessageException;
import org.apache.flink.runtime.rpc.exceptions.RpcConnectionException;
import org.apache.flink.runtime.rpc.messages.*;
import org.apache.flink.types.Either;
import org.apache.flink.util.ExceptionUtils;
import org.apache.flink.util.Preconditions;
import org.apache.flink.util.SerializedValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.duration.FiniteDuration;
import scala.concurrent.impl.Promise;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.apache.flink.util.Preconditions.checkArgument;
import static org.apache.flink.util.Preconditions.checkNotNull;

public class AkkaRpcActor<T extends RpcEndpoint & RpcGateway> extends UntypedActor {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    /** the endpoint to invoke the methods on. */
    protected final T rpcEndpoint;

    /** the helper that tracks whether calls come from the main thread. */
    private final MainThreadValidatorUtil mainThreadValidator;

    private final CompletableFuture<Boolean> terminationFuture;

    private final int version;

    private final long maximumFramesize;

    private final AtomicBoolean rpcEndpointStopped;

    private volatile AkkaRpcActor.RpcEndpointTerminationResult rpcEndpointTerminationResult;

    @Nonnull
    private AkkaRpcActor.State state;

    AkkaRpcActor(
            final T rpcEndpoint,
            final CompletableFuture<Boolean> terminationFuture,
            final int version,
            final long maximumFramesize) {

        checkArgument(maximumFramesize > 0, "Maximum framesize must be positive.");
        this.rpcEndpoint = checkNotNull(rpcEndpoint, "rpc endpoint");
        this.mainThreadValidator = new MainThreadValidatorUtil(rpcEndpoint);
        this.terminationFuture = checkNotNull(terminationFuture);
        this.version = version;
        this.maximumFramesize = maximumFramesize;
        this.rpcEndpointStopped = new AtomicBoolean(false);
        this.rpcEndpointTerminationResult = AkkaRpcActor.RpcEndpointTerminationResult.failure(
                new AkkaRpcException(
                        String.format("RpcEndpoint %s has not been properly stopped.", rpcEndpoint.getEndpointId())));
        this.state = AkkaRpcActor.StoppedState.INSTANCE;
    }

    @Override
    public void postStop() throws Exception {
        super.postStop();

        if (rpcEndpointTerminationResult.isSuccess()) {
            terminationFuture.complete(null);
        } else {
            terminationFuture.completeExceptionally(rpcEndpointTerminationResult.getFailureCause());
        }

        state = state.finishTermination();
    }

    @Override
    public void onReceive(final Object message) {
        if (message instanceof RemoteHandshakeMessage) {
            handleHandshakeMessage((RemoteHandshakeMessage) message);
        } else if (message instanceof ControlMessages) {
            handleControlMessage(((ControlMessages) message));
        } else if (state.isRunning()) {
            mainThreadValidator.enterMainThread();

            try {
                handleRpcMessage(message);
            } finally {
                mainThreadValidator.exitMainThread();
            }
        } else {
            log.info("The rpc endpoint {} has not been started yet. Discarding message {} until processing is started.",
                    rpcEndpoint.getClass().getName(),
                    message.getClass().getName());

            sendErrorIfSender(new AkkaRpcException(
                    String.format("Discard message, because the rpc endpoint %s has not been started yet.", rpcEndpoint.getAddress())));
        }
    }

    private void handleControlMessage(ControlMessages controlMessage) {
        switch (controlMessage) {
            case START:
                state = state.start(this);
                break;
            case STOP:
                state = state.stop();
                break;
            case TERMINATE:
                state.terminate(this);
                break;
            default:
                handleUnknownControlMessage(controlMessage);
        }
    }

    private void handleUnknownControlMessage(ControlMessages controlMessage) {
        final String message = String.format("Received unknown control message %s. Dropping this message!", controlMessage);
        log.warn(message);
        sendErrorIfSender(new AkkaUnknownMessageException(message));
    }

    protected void handleRpcMessage(Object message) {
        if (message instanceof RunAsync) {
            handleRunAsync((RunAsync) message);
        } else if (message instanceof CallAsync) {
            handleCallAsync((CallAsync) message);
        } else if (message instanceof RpcInvocation) {
            handleRpcInvocation((RpcInvocation) message);
        } else {
            log.warn(
                    "Received message of unknown type {} with value {}. Dropping this message!",
                    message.getClass().getName(),
                    message);

            sendErrorIfSender(new AkkaUnknownMessageException("Received unknown message " + message +
                    " of type " + message.getClass().getSimpleName() + '.'));
        }
    }

    private void handleHandshakeMessage(RemoteHandshakeMessage handshakeMessage) {
        if (!isCompatibleVersion(handshakeMessage.getVersion())) {
            sendErrorIfSender(new AkkaHandshakeException(
                    String.format(
                            "Version mismatch between source (%s) and target (%s) rpc component. Please verify that all components have the same version.",
                            handshakeMessage.getVersion(),
                            getVersion())));
        } else if (!isGatewaySupported(handshakeMessage.getRpcGateway())) {
            sendErrorIfSender(new AkkaHandshakeException(
                    String.format(
                            "The rpc endpoint does not support the gateway %s.",
                            handshakeMessage.getRpcGateway().getSimpleName())));
        } else {
            getSender().tell(new Status.Success(HandshakeSuccessMessage.INSTANCE), getSelf());
        }
    }

    private boolean isGatewaySupported(Class<?> rpcGateway) {
        return rpcGateway.isAssignableFrom(rpcEndpoint.getClass());
    }

    private boolean isCompatibleVersion(int sourceVersion) {
        return sourceVersion == getVersion();
    }

    private int getVersion() {
        return version;
    }

    /**
     * Handle rpc invocations by looking up the rpc method on the rpc endpoint and calling this
     * method with the provided method arguments. If the method has a return value, it is returned
     * to the sender of the call.
     *
     * @param rpcInvocation Rpc invocation message
     */
    private void handleRpcInvocation(RpcInvocation rpcInvocation) {
        Method rpcMethod = null;

        try {
            String methodName = rpcInvocation.getMethodName();
            Class<?>[] parameterTypes = rpcInvocation.getParameterTypes();

            rpcMethod = lookupRpcMethod(methodName, parameterTypes);
        } catch (ClassNotFoundException e) {
            log.error("Could not load method arguments.", e);

            RpcConnectionException rpcException = new RpcConnectionException("Could not load method arguments.", e);
            getSender().tell(new Status.Failure(rpcException), getSelf());
        } catch (IOException e) {
            log.error("Could not deserialize rpc invocation message.", e);

            RpcConnectionException rpcException = new RpcConnectionException("Could not deserialize rpc invocation message.", e);
            getSender().tell(new Status.Failure(rpcException), getSelf());
        } catch (final NoSuchMethodException e) {
            log.error("Could not find rpc method for rpc invocation.", e);

            RpcConnectionException rpcException = new RpcConnectionException("Could not find rpc method for rpc invocation.", e);
            getSender().tell(new Status.Failure(rpcException), getSelf());
        }

        if (rpcMethod != null) {
            try {
                // this supports declaration of anonymous classes
                rpcMethod.setAccessible(true);

                if (rpcMethod.getReturnType().equals(Void.TYPE)) {
                    // No return value to send back
                    rpcMethod.invoke(rpcEndpoint, rpcInvocation.getArgs());
                }
                else {
                    final Object result;
                    try {
                        result = rpcMethod.invoke(rpcEndpoint, rpcInvocation.getArgs());
                    }
                    catch (InvocationTargetException e) {
                        log.trace("Reporting back error thrown in remote procedure {}", rpcMethod, e);

                        // tell the sender about the failure
                        getSender().tell(new Status.Failure(e.getTargetException()), getSelf());
                        return;
                    }

                    final String methodName = rpcMethod.getName();

                    if (result instanceof CompletableFuture) {
                        final CompletableFuture<?> responseFuture = (CompletableFuture<?>) result;
                        sendAsyncResponse(responseFuture, methodName);
                    } else {
                        sendSyncResponse(result, methodName);
                    }
                }
            } catch (Throwable e) {
                log.error("Error while executing remote procedure call {}.", rpcMethod, e);
                // tell the sender about the failure
                getSender().tell(new Status.Failure(e), getSelf());
            }
        }
    }

    private void sendSyncResponse(Object response, String methodName) {
        if (isRemoteSender(getSender())) {
            Either<SerializedValue<?>, AkkaRpcException> serializedResult = serializeRemoteResultAndVerifySize(response, methodName);

            if (serializedResult.isLeft()) {
                getSender().tell(new Status.Success(serializedResult.left()), getSelf());
            } else {
                getSender().tell(new Status.Failure(serializedResult.right()), getSelf());
            }
        } else {
            getSender().tell(new Status.Success(response), getSelf());
        }
    }

    private void sendAsyncResponse(CompletableFuture<?> asyncResponse, String methodName) {
        final ActorRef sender = getSender();
        Promise.DefaultPromise<Object> promise = new Promise.DefaultPromise<>();

        asyncResponse.whenComplete(
                (value, throwable) -> {
                    if (throwable != null) {
                        promise.failure(throwable);
                    } else {
                        if (isRemoteSender(sender)) {
                            Either<SerializedValue<?>, AkkaRpcException> serializedResult = serializeRemoteResultAndVerifySize(value, methodName);

                            if (serializedResult.isLeft()) {
                                promise.success(serializedResult.left());
                            } else {
                                promise.failure(serializedResult.right());
                            }
                        } else {
                            promise.success(value);
                        }
                    }
                });

        Patterns.pipe(promise.future(), getContext().dispatcher()).to(sender);
    }

    private boolean isRemoteSender(ActorRef sender) {
        return !sender.path().address().hasLocalScope();
    }

    private Either<SerializedValue<?>, AkkaRpcException> serializeRemoteResultAndVerifySize(Object result, String methodName) {
        try {
            SerializedValue<?> serializedResult = new SerializedValue<>(result);

            long resultSize = serializedResult.getByteArray().length;
            if (resultSize > maximumFramesize) {
                return Either.Right(new AkkaRpcException(
                        "The method " + methodName + "'s result size " + resultSize
                                + " exceeds the maximum size " + maximumFramesize + " ."));
            } else {
                return Either.Left(serializedResult);
            }
        } catch (IOException e) {
            return Either.Right(new AkkaRpcException(
                    "Failed to serialize the result for RPC call : " + methodName + '.', e));
        }
    }

    /**
     * Handle asynchronous {@link Callable}. This method simply executes the given {@link Callable}
     * in the context of the actor thread.
     *
     * @param callAsync Call async message
     */
    private void handleCallAsync(CallAsync callAsync) {
        try {
            Object result = callAsync.getCallable().call();

            getSender().tell(new Status.Success(result), getSelf());
        } catch (Throwable e) {
            getSender().tell(new Status.Failure(e), getSelf());
        }
    }

    /**
     * Handle asynchronous {@link Runnable}. This method simply executes the given {@link Runnable}
     * in the context of the actor thread.
     *
     * @param runAsync Run async message
     */
    private void handleRunAsync(RunAsync runAsync) {
        final long timeToRun = runAsync.getTimeNanos();
        final long delayNanos;

        if (timeToRun == 0 || (delayNanos = timeToRun - System.nanoTime()) <= 0) {
            // run immediately
            try {
                runAsync.getRunnable().run();
            } catch (Throwable t) {
                log.error("Caught exception while executing runnable in main thread.", t);
                ExceptionUtils.rethrowIfFatalErrorOrOOM(t);
            }
        }
        else {
            // schedule for later. send a new message after the delay, which will then be immediately executed
            FiniteDuration delay = new FiniteDuration(delayNanos, TimeUnit.NANOSECONDS);
            RunAsync message = new RunAsync(runAsync.getRunnable(), timeToRun);

            final Object envelopedSelfMessage = envelopeSelfMessage(message);

            getContext().system().scheduler().scheduleOnce(delay, getSelf(), envelopedSelfMessage,
                    getContext().dispatcher(), ActorRef.noSender());
        }
    }

    /**
     * Look up the rpc method on the given {@link RpcEndpoint} instance.
     *
     * @param methodName Name of the method
     * @param parameterTypes Parameter types of the method
     * @return Method of the rpc endpoint
     * @throws NoSuchMethodException Thrown if the method with the given name and parameter types
     * 									cannot be found at the rpc endpoint
     */
    private Method lookupRpcMethod(final String methodName, final Class<?>[] parameterTypes) throws NoSuchMethodException {
        return rpcEndpoint.getClass().getMethod(methodName, parameterTypes);
    }

    /**
     * Send throwable to sender if the sender is specified.
     *
     * @param throwable to send to the sender
     */
    protected void sendErrorIfSender(Throwable throwable) {
        if (!getSender().equals(ActorRef.noSender())) {
            getSender().tell(new Status.Failure(throwable), getSelf());
        }
    }

    /**
     * Hook to envelope self messages.
     *
     * @param message to envelope
     * @return enveloped message
     */
    protected Object envelopeSelfMessage(Object message) {
        return message;
    }

    /**
     * Stop the actor immediately.
     */
    private void stop(AkkaRpcActor.RpcEndpointTerminationResult rpcEndpointTerminationResult) {
        if (rpcEndpointStopped.compareAndSet(false, true)) {
            this.rpcEndpointTerminationResult = rpcEndpointTerminationResult;
            getContext().stop(getSelf());
        }
    }

    // ---------------------------------------------------------------------------
    // Internal state machine
    // ---------------------------------------------------------------------------

    interface State {
        default AkkaRpcActor.State start(AkkaRpcActor<?> akkaRpcActor) {
            throw new AkkaRpcInvalidStateException(invalidStateTransitionMessage(AkkaRpcActor.StartedState.INSTANCE));
        }

        default AkkaRpcActor.State stop() {
            throw new AkkaRpcInvalidStateException(invalidStateTransitionMessage(AkkaRpcActor.StoppedState.INSTANCE));
        }

        default AkkaRpcActor.State terminate(AkkaRpcActor<?> akkaRpcActor) {
            throw new AkkaRpcInvalidStateException(invalidStateTransitionMessage(AkkaRpcActor.TerminatingState.INSTANCE));
        }

        default AkkaRpcActor.State finishTermination() {
            return AkkaRpcActor.TerminatedState.INSTANCE;
        }

        default boolean isRunning() {
            return false;
        }

        default String invalidStateTransitionMessage(AkkaRpcActor.State targetState) {
            return String.format("AkkaRpcActor is currently in state %s and cannot go into state %s.", this, targetState);
        }
    }

    @SuppressWarnings("Singleton")
    enum StartedState implements AkkaRpcActor.State {
        INSTANCE;

        @Override
        public AkkaRpcActor.State start(AkkaRpcActor<?> akkaRpcActor) {
            return INSTANCE;
        }

        @Override
        public AkkaRpcActor.State stop() {
            return AkkaRpcActor.StoppedState.INSTANCE;
        }

        @Override
        public AkkaRpcActor.State terminate(AkkaRpcActor<?> akkaRpcActor) {
            akkaRpcActor.mainThreadValidator.enterMainThread();

            CompletableFuture<Void> terminationFuture;
            try {
                terminationFuture = akkaRpcActor.rpcEndpoint.onStop();
            } catch (Throwable t) {
                terminationFuture = FutureUtils.completedExceptionally(
                        new AkkaRpcException(
                                String.format("Failure while stopping RpcEndpoint %s.", akkaRpcActor.rpcEndpoint.getEndpointId()),
                                t));
            } finally {
                akkaRpcActor.mainThreadValidator.exitMainThread();
            }

            // IMPORTANT: This only works if we don't use a restarting supervisor strategy. Otherwise
            // we would complete the future and let the actor system restart the actor with a completed
            // future.
            // Complete the termination future so that others know that we've stopped.

            terminationFuture.whenComplete((ignored, throwable) -> akkaRpcActor.stop(AkkaRpcActor.RpcEndpointTerminationResult.of(throwable)));

            return AkkaRpcActor.TerminatingState.INSTANCE;
        }

        @Override
        public boolean isRunning() {
            return true;
        }
    }

    @SuppressWarnings("Singleton")
    enum StoppedState implements AkkaRpcActor.State {
        INSTANCE;

        @Override
        public AkkaRpcActor.State start(AkkaRpcActor<?> akkaRpcActor) {
            akkaRpcActor.mainThreadValidator.enterMainThread();

            try {
                akkaRpcActor.rpcEndpoint.onStart();
            } catch (Throwable throwable) {
                akkaRpcActor.stop(
                        AkkaRpcActor.RpcEndpointTerminationResult.failure(
                                new AkkaRpcException(
                                        String.format("Could not start RpcEndpoint %s.", akkaRpcActor.rpcEndpoint.getEndpointId()),
                                        throwable)));
            } finally {
                akkaRpcActor.mainThreadValidator.exitMainThread();
            }

            return AkkaRpcActor.StartedState.INSTANCE;
        }

        @Override
        public AkkaRpcActor.State stop() {
            return INSTANCE;
        }

        @Override
        public AkkaRpcActor.State terminate(AkkaRpcActor<?> akkaRpcActor) {
            akkaRpcActor.stop(AkkaRpcActor.RpcEndpointTerminationResult.success());

            return AkkaRpcActor.TerminatingState.INSTANCE;
        }
    }

    @SuppressWarnings("Singleton")
    enum TerminatingState implements AkkaRpcActor.State {
        INSTANCE;

        @Override
        public boolean isRunning() {
            return true;
        }
    }

    enum TerminatedState implements AkkaRpcActor.State {
        INSTANCE
    }

    private static final class RpcEndpointTerminationResult {

        private static final AkkaRpcActor.RpcEndpointTerminationResult SUCCESS = new AkkaRpcActor.RpcEndpointTerminationResult(null);

        @Nullable
        private final Throwable failureCause;

        private RpcEndpointTerminationResult(@Nullable Throwable failureCause) {
            this.failureCause = failureCause;
        }

        public boolean isSuccess() {
            return failureCause == null;
        }

        public Throwable getFailureCause() {
            Preconditions.checkState(failureCause != null);
            return failureCause;
        }

        private static AkkaRpcActor.RpcEndpointTerminationResult success() {
            return SUCCESS;
        }

        private static AkkaRpcActor.RpcEndpointTerminationResult failure(Throwable failureCause) {
            return new AkkaRpcActor.RpcEndpointTerminationResult(failureCause);
        }

        private static AkkaRpcActor.RpcEndpointTerminationResult of(@Nullable Throwable failureCause) {
            if (failureCause == null) {
                return success();
            } else {
                return failure(failureCause);
            }
        }
    }
}
