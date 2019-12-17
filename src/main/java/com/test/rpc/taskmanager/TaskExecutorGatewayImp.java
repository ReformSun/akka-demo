package com.test.rpc.taskmanager;

import org.apache.flink.api.common.JobID;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.runtime.blob.TransientBlobKey;
import org.apache.flink.runtime.checkpoint.CheckpointOptions;
import org.apache.flink.runtime.clusterframework.types.AllocationID;
import org.apache.flink.runtime.clusterframework.types.ResourceID;
import org.apache.flink.runtime.clusterframework.types.SlotID;
import org.apache.flink.runtime.deployment.TaskDeploymentDescriptor;
import org.apache.flink.runtime.executiongraph.ExecutionAttemptID;
import org.apache.flink.runtime.executiongraph.PartitionInfo;
import org.apache.flink.runtime.jobmaster.JobMasterId;
import org.apache.flink.runtime.messages.Acknowledge;
import org.apache.flink.runtime.messages.StackTraceSampleResponse;
import org.apache.flink.runtime.resourcemanager.ResourceManagerId;
import org.apache.flink.runtime.rpc.RpcEndpoint;
import org.apache.flink.runtime.rpc.RpcService;
import org.apache.flink.runtime.taskexecutor.FileType;
import org.apache.flink.runtime.taskexecutor.TaskExecutorGateway;
import org.apache.flink.types.SerializableOptional;

import java.util.concurrent.CompletableFuture;

public class TaskExecutorGatewayImp extends RpcEndpoint implements TaskExecutorGateway {

    public TaskExecutorGatewayImp(RpcService rpcService, String endpointId) {
        super(rpcService, endpointId);
    }

    public TaskExecutorGatewayImp(RpcService rpcService) {
        super(rpcService);
    }

    @Override
    public CompletableFuture<Acknowledge> requestSlot(SlotID slotID, JobID jobID, AllocationID allocationID, String s, ResourceManagerId resourceManagerId, Time time) {
        return null;
    }

    @Override
    public CompletableFuture<StackTraceSampleResponse> requestStackTraceSample(ExecutionAttemptID executionAttemptID, int i, int i1, Time time, int i2, Time time1) {
        return null;
    }

    @Override
    public CompletableFuture<Acknowledge> submitTask(TaskDeploymentDescriptor taskDeploymentDescriptor, JobMasterId jobMasterId, Time time) {
        return null;
    }

    @Override
    public CompletableFuture<Acknowledge> updatePartitions(ExecutionAttemptID executionAttemptID, Iterable<PartitionInfo> iterable, Time time) {
        return null;
    }

    @Override
    public void failPartition(ExecutionAttemptID executionAttemptID) {

    }

    @Override
    public CompletableFuture<Acknowledge> triggerCheckpoint(ExecutionAttemptID executionAttemptID, long l, long l1, CheckpointOptions checkpointOptions) {
        return null;
    }

    @Override
    public CompletableFuture<Acknowledge> confirmCheckpoint(ExecutionAttemptID executionAttemptID, long l, long l1) {
        return null;
    }

    @Override
    public CompletableFuture<Acknowledge> stopTask(ExecutionAttemptID executionAttemptID, Time time) {
        return null;
    }

    @Override
    public CompletableFuture<Acknowledge> cancelTask(ExecutionAttemptID executionAttemptID, Time time) {
        return null;
    }

    @Override
    public void heartbeatFromJobManager(ResourceID resourceID) {

    }

    @Override
    public void heartbeatFromResourceManager(ResourceID resourceID) {

    }

    @Override
    public void disconnectJobManager(JobID jobID, Exception e) {

    }

    @Override
    public void disconnectResourceManager(Exception e) {

    }

    @Override
    public CompletableFuture<Acknowledge> freeSlot(AllocationID allocationID, Throwable throwable, Time time) {
        return null;
    }

    @Override
    public CompletableFuture<TransientBlobKey> requestFileUpload(FileType fileType, Time time) {
        return null;
    }

    @Override
    public CompletableFuture<SerializableOptional<String>> requestMetricQueryServiceAddress(Time time) {
        return null;
    }

    @Override
    public String getAddress() {
        return null;
    }

    @Override
    public String getHostname() {
        return null;
    }
}
