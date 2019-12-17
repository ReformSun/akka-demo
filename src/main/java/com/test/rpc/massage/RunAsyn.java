package com.test.rpc.massage;

public final class RunAsyn {
    /** The runnable to be executed. Transient, so it gets lost upon serialization */
    private final Runnable runnable;

    /** The delay after which the runnable should be called */
    private final long atTimeNanos;

    /**
     * Creates a new {@code RunAsync} message.
     *
     * @param runnable    The Runnable to run.
     * @param atTimeNanos The time (as for System.nanoTime()) when to execute the runnable.
     */
    public RunAsyn(Runnable runnable, long atTimeNanos) {
        this.runnable = runnable;
        this.atTimeNanos = atTimeNanos;
    }

    public Runnable getRunnable() {
        return runnable;
    }

    public long getTimeNanos() {
        return atTimeNanos;
    }
}
