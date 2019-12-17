package com.test.rpc.resourcemanager;

import org.apache.flink.api.common.JobID;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.runtime.blob.TransientBlobKey;
import org.apache.flink.runtime.clusterframework.ApplicationStatus;
import org.apache.flink.runtime.clusterframework.types.AllocationID;
import org.apache.flink.runtime.clusterframework.types.ResourceID;
import org.apache.flink.runtime.clusterframework.types.SlotID;
import org.apache.flink.runtime.instance.HardwareDescription;
import org.apache.flink.runtime.instance.InstanceID;
import org.apache.flink.runtime.jobmaster.JobMasterId;
import org.apache.flink.runtime.messages.Acknowledge;
import org.apache.flink.runtime.registration.RegistrationResponse;
import org.apache.flink.runtime.resourcemanager.ResourceManagerGateway;
import org.apache.flink.runtime.resourcemanager.ResourceManagerId;
import org.apache.flink.runtime.resourcemanager.ResourceOverview;
import org.apache.flink.runtime.resourcemanager.SlotRequest;
import org.apache.flink.runtime.rest.messages.taskmanager.TaskManagerInfo;
import org.apache.flink.runtime.taskexecutor.FileType;
import org.apache.flink.runtime.taskexecutor.SlotReport;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class ResourceManagerGatewayImp implements ResourceManagerGateway {
    @Override
    public CompletableFuture<RegistrationResponse> registerJobManager(JobMasterId jobMasterId, ResourceID resourceID, String s, JobID jobID, Time time) {
        return null;
    }

    @Override
    public CompletableFuture<Acknowledge> requestSlot(JobMasterId jobMasterId, SlotRequest slotRequest, Time time) {
        return null;
    }

    @Override
    public void cancelSlotRequest(AllocationID allocationID) {

    }

    @Override
    public CompletableFuture<RegistrationResponse> registerTaskExecutor(String s, ResourceID resourceID, int i, HardwareDescription hardwareDescription, Time time) {
        return null;
    }

    @Override
    public CompletableFuture<Acknowledge> sendSlotReport(ResourceID resourceID, InstanceID instanceID, SlotReport slotReport, Time time) {
        return null;
    }

    @Override
    public void notifySlotAvailable(InstanceID instanceID, SlotID slotID, AllocationID allocationID) {

    }

    @Override
    public void registerInfoMessageListener(String s) {

    }

    @Override
    public void unRegisterInfoMessageListener(String s) {

    }

    @Override
    public CompletableFuture<Acknowledge> deregisterApplication(ApplicationStatus applicationStatus, @Nullable String s) {
        return null;
    }

    @Override
    public CompletableFuture<Integer> getNumberOfRegisteredTaskManagers() {
        return null;
    }

    @Override
    public void heartbeatFromTaskManager(ResourceID resourceID, SlotReport slotReport) {

    }

    @Override
    public void heartbeatFromJobManager(ResourceID resourceID) {

    }

    @Override
    public void disconnectTaskManager(ResourceID resourceID, Exception e) {

    }

    @Override
    public void disconnectJobManager(JobID jobID, Exception e) {

    }

    @Override
    public CompletableFuture<Collection<TaskManagerInfo>> requestTaskManagerInfo(Time time) {
        return null;
    }

    @Override
    public CompletableFuture<TaskManagerInfo> requestTaskManagerInfo(ResourceID resourceID, Time time) {
        return null;
    }

    @Override
    public CompletableFuture<ResourceOverview> requestResourceOverview(Time time) {
        return null;
    }

    @Override
    public CompletableFuture<Collection<Tuple2<ResourceID, String>>> requestTaskManagerMetricQueryServicePaths(Time time) {
        return null;
    }

    @Override
    public CompletableFuture<TransientBlobKey> requestTaskManagerFileUpload(ResourceID resourceID, FileType fileType, Time time) {
        return null;
    }

    @Override
    public ResourceManagerId getFencingToken() {
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
