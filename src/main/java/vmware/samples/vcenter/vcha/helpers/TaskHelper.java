/*
 * *******************************************************
 * Copyright VMware, Inc. 2018.  All Rights Reserved.
 * SPDX-License-Identifier: MIT
 * *******************************************************
 *
 * DISCLAIMER. THIS PROGRAM IS PROVIDED TO YOU "AS IS" WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, WHETHER ORAL OR WRITTEN,
 * EXPRESS OR IMPLIED. THE AUTHOR SPECIFICALLY DISCLAIMS ANY IMPLIED
 * WARRANTIES OR CONDITIONS OF MERCHANTABILITY, SATISFACTORY QUALITY,
 * NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE.
 */

package vmware.samples.vcenter.vcha.helpers;

import com.vmware.vim25.ManagedObjectReference;
import vmware.samples.common.authentication.VimAuthenticationHelper;
import vmware.samples.common.vim.helpers.WaitForValues;

import java.util.concurrent.TimeUnit;

public class TaskHelper {

    public static final String TASK_TYPE_MO_REF = "Task";
    public static final String TASK_ID_SEPERATOR = ":";
    public static final Long TASK_SLEEP = 60L;

    /**
     * Waits for given Task to complete
     *
     * @param vim AuthenticationHelper for VMODL1 APIs
     * @param taskID ID for the performed task
     */
    public static Boolean waitForTask(VimAuthenticationHelper vim, String taskID) {
        String[] taskIDParts= taskID.split(TASK_ID_SEPERATOR);
        String finalTaskID = taskIDParts[0];
        ManagedObjectReference ref = new ManagedObjectReference();
        ref.setType(TASK_TYPE_MO_REF);
        ref.setValue(finalTaskID);
        WaitForValues waitForValues = new WaitForValues(vim.getVimPort(), vim.getServiceContent());
        try {
            if(waitForValues.getTaskResultAfterDone(ref)) {
                System.out.println("Successfully completed task [" + ref.getValue() + "]");
                return true;
            } else {
                System.out.println("Failed to complete task [" + ref.getValue() + "]");
            }
        } catch(Exception e) {
            System.out.println("Unable to execute task [" + ref.getValue() + "]");
            System.out.println("Reason: " + e.getLocalizedMessage());
        }
        return false;
    }

    public static void sleep(Long duration) {
        Long startTime = System.nanoTime();
        Long currTime = System.nanoTime();
        while(currTime - startTime < TimeUnit.NANOSECONDS.convert(duration, TimeUnit.SECONDS)) {
            currTime = System.nanoTime();
        }
    }
}
