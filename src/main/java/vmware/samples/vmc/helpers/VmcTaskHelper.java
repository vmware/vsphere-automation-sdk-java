/*
 * *******************************************************
 * Copyright VMware, Inc. 2017, 2018.  All Rights Reserved.
 * SPDX-License-Identifier: MIT
 * *******************************************************
 *
 * DISCLAIMER. THIS PROGRAM IS PROVIDED TO YOU "AS IS" WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, WHETHER ORAL OR WRITTEN,
 * EXPRESS OR IMPLIED. THE AUTHOR SPECIFICALLY DISCLAIMS ANY IMPLIED
 * WARRANTIES OR CONDITIONS OF MERCHANTABILITY, SATISFACTORY QUALITY,
 * NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE.
 */
package vmware.samples.vmc.helpers;

import java.util.Date;

import com.vmware.vapi.client.ApiClient;
import com.vmware.vmc.model.Task;
import com.vmware.vmc.orgs.Tasks;

/**
 * Utility class for task operations.
 * @author VMware Inc.
 *
 */
public class VmcTaskHelper {
    /**
     * Poll a task, every "x" milliseconds, to find out its status. Return if the task is canceled, failed
     * or finished.
     *
     * @param apiClient API client for initializing stubs.
     * @param orgId organization identifier
     * @param taskId task identifier
     *
     * @return true, if task is finished, false otherwise.
     * @throws InterruptedException
     */
    public static boolean pollTask(ApiClient apiClient, String orgId, String taskId, int x)
            throws InterruptedException {
        Tasks tasksStub = apiClient.createStub(Tasks.class);
        System.out.println();
        do {
            Thread.sleep(x);
            Task task = tasksStub.get(orgId, taskId);
            String taskStatus = new Date() + ": Task Status = " + task.getStatus();
            String taskPhaseInProgress =
                (task.getPhaseInProgress() != null && !task.getPhaseInProgress().isEmpty()) ?
                		(", Task Phase = " + task.getPhaseInProgress()) : "";
            System.out.println(taskStatus + taskPhaseInProgress);
            switch(task.getStatus()) {
                case Task.STATUS_FINISHED :
                    return true;
                case Task.STATUS_CANCELED :
                	// Execute next case.
                case Task.STATUS_FAILED :
                	// Execute next case.
                default:
                	return false;
            }
        } while (true);
    }
}
