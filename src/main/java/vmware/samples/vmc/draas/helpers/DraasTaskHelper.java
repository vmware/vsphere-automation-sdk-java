/*
 * *******************************************************
 * Copyright VMware, Inc. 2019.  All Rights Reserved.
 * SPDX-License-Identifier: MIT
 * *******************************************************
 *
 * DISCLAIMER. THIS PROGRAM IS PROVIDED TO YOU "AS IS" WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, WHETHER ORAL OR WRITTEN,
 * EXPRESS OR IMPLIED. THE AUTHOR SPECIFICALLY DISCLAIMS ANY IMPLIED
 * WARRANTIES OR CONDITIONS OF MERCHANTABILITY, SATISFACTORY QUALITY,
 * NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE.
 */
package vmware.samples.vmc.draas.helpers;

import java.util.Date;
import com.vmware.vapi.client.ApiClient;
import com.vmware.vmc.draas.Task;

public class DraasTaskHelper {
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
    public static boolean pollTask(ApiClient apiClient,
            String orgId, String taskId, int waitTime)
            throws InterruptedException {
        Task tasksStub = apiClient.createStub(Task.class);    
        do {
            Thread.sleep(waitTime);
            com.vmware.vmc.draas.model.Task task = tasksStub.get(orgId, taskId);
            String taskStatus = new Date() + ": Task Status = " + task.getStatus() + 
                    " : "+task.getProgressPercent() +"%";          
            System.out.println(taskStatus);
            switch(task.getStatus()) {
                case com.vmware.vmc.draas.model.Task.STATUS_STARTED:
                    continue;
                case com.vmware.vmc.draas.model.Task.STATUS_FINISHED :
                    return true;
                case com.vmware.vmc.draas.model.Task.STATUS_CANCELED :
                    // Execute next case.
                case com.vmware.vmc.draas.model.Task.STATUS_FAILED :
                    // Execute next case.
                default:
                    return false;
            }
        } while (true);
    }


}
