package com.test.rpc.jobmaster;

import com.test.rpc.inter.FencedRpcEndpoint;
import com.test.rpc.inter.RpcService;
import org.apache.flink.api.common.JobID;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.queryablestate.KvStateID;
import org.apache.flink.runtime.checkpoint.CheckpointMetrics;
import org.apache.flink.runtime.checkpoint.TaskStateSnapshot;
import org.apache.flink.runtime.clusterframework.types.AllocationID;
import org.apache.flink.runtime.clusterframework.types.ResourceID;
import org.apache.flink.runtime.execution.ExecutionState;
import org.apache.flink.runtime.executiongraph.ArchivedExecutionGraph;
import org.apache.flink.runtime.executiongraph.ExecutionAttemptID;
import org.apache.flink.runtime.io.network.partition.ResultPartitionID;
import org.apache.flink.runtime.jobgraph.IntermediateDataSetID;
import org.apache.flink.runtime.jobgraph.JobStatus;
import org.apache.flink.runtime.jobgraph.JobVertexID;
import org.apache.flink.runtime.jobmaster.JobMasterGateway;
import org.apache.flink.runtime.jobmaster.JobMasterId;
import org.apache.flink.runtime.jobmaster.RescalingBehaviour;
import org.apache.flink.runtime.jobmaster.SerializedInputSplit;
import org.apache.flink.runtime.messages.Acknowledge;
import org.apache.flink.runtime.messages.checkpoint.DeclineCheckpoint;
import org.apache.flink.runtime.messages.webmonitor.JobDetails;
import org.apache.flink.runtime.query.KvStateLocation;
import org.apache.flink.runtime.registration.RegistrationResponse;
import org.apache.flink.runtime.resourcemanager.ResourceManagerId;
import org.apache.flink.runtime.rest.handler.legacy.backpressure.OperatorBackPressureStatsResponse;
import org.apache.flink.runtime.state.KeyGroupRange;
import org.apache.flink.runtime.taskexecutor.AccumulatorReport;
import org.apache.flink.runtime.taskexecutor.slot.SlotOffer;
import org.apache.flink.runtime.taskmanager.TaskExecutionState;
import org.apache.flink.runtime.taskmanager.TaskManagerLocation;

import javax.annotation.Nullable;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class JobMasterGatewayImp extends FencedRpcEndpoint<JobMasterId> implements JobMasterGateway {
    public JobMasterGatewayImp(RpcService rpcService, String endpointId) {
        super(rpcService, endpointId);
    }

    public JobMasterGatewayImp(RpcService rpcService) {
        super(rpcService);
    }

    @Override
    public CompletableFuture<Void> postStop() {
        return null;
    }

    public CompletableFuture<Acknowledge> cancel(Time time) {
        return null;
    }

    public CompletableFuture<Acknowledge> stop(Time time) {
        return null;
    }

    public CompletableFuture<Acknowledge> rescaleJob(int i, RescalingBehaviour rescalingBehaviour, Time time) {
        return null;
    }

    public CompletableFuture<Acknowledge> rescaleOperators(Collection<JobVertexID> collection, int i, RescalingBehaviour rescalingBehaviour, Time time) {
        return null;
    }

    public CompletableFuture<Acknowledge> updateTaskExecutionState(TaskExecutionState taskExecutionState) {
        return null;
    }

    public CompletableFuture<SerializedInputSplit> requestNextInputSplit(JobVertexID jobVertexID, ExecutionAttemptID executionAttemptID) {
        return null;
    }

    public CompletableFuture<ExecutionState> requestPartitionState(IntermediateDataSetID intermediateDataSetID, ResultPartitionID resultPartitionID) {
        return null;
    }

    public CompletableFuture<Acknowledge> scheduleOrUpdateConsumers(ResultPartitionID resultPartitionID, Time time) {
        return null;
    }

    public CompletableFuture<Acknowledge> disconnectTaskManager(ResourceID resourceID, Exception e) {
        return null;
    }

    public void disconnectResourceManager(ResourceManagerId resourceManagerId, Exception e) {

    }

    public CompletableFuture<Collection<SlotOffer>> offerSlots(ResourceID resourceID, Collection<SlotOffer> collection, Time time) {
        return null;
    }

    public void failSlot(ResourceID resourceID, AllocationID allocationID, Exception e) {

    }

    public CompletableFuture<RegistrationResponse> registerTaskManager(String s, TaskManagerLocation taskManagerLocation, Time time) {
        return null;
    }

    public void heartbeatFromTaskManager(ResourceID resourceID, AccumulatorReport accumulatorReport) {

    }

    public void heartbeatFromResourceManager(ResourceID resourceID) {

    }

    public CompletableFuture<JobDetails> requestJobDetails(Time time) {
        return null;
    }

    public CompletableFuture<JobStatus> requestJobStatus(Time time) {
        return null;
    }

    public CompletableFuture<ArchivedExecutionGraph> requestJob(Time time) {
        return null;
    }

    public CompletableFuture<String> triggerSavepoint(@Nullable String s, boolean b, Time time) {
        return null;
    }

    public CompletableFuture<OperatorBackPressureStatsResponse> requestOperatorBackPressureStats(JobVertexID jobVertexID) {
        return null;
    }

    public void notifyAllocationFailure(AllocationID allocationID, Exception e) {

    }

    public CompletableFuture<Object> updateGlobalAggregate(String s, Object o, byte[] bytes) {
        return null;
    }

    public void acknowledgeCheckpoint(JobID jobID, ExecutionAttemptID executionAttemptID, long l, CheckpointMetrics checkpointMetrics, TaskStateSnapshot taskStateSnapshot) {

    }

    public void declineCheckpoint(DeclineCheckpoint declineCheckpoint) {

    }

    public CompletableFuture<KvStateLocation> requestKvStateLocation(JobID jobID, String s) {
        return null;
    }

    public CompletableFuture<Acknowledge> notifyKvStateRegistered(JobID jobID, JobVertexID jobVertexID, KeyGroupRange keyGroupRange, String s, KvStateID kvStateID, InetSocketAddress inetSocketAddress) {
        return null;
    }

    public CompletableFuture<Acknowledge> notifyKvStateUnregistered(JobID jobID, JobVertexID jobVertexID, KeyGroupRange keyGroupRange, String s) {
        return null;
    }

    public JobMasterId getFencingToken() {
        return null;
    }

    public String getAddress() {
        System.out.println("address");
        return "address";
    }

    public String getHostname() {
        return null;
    }
}
