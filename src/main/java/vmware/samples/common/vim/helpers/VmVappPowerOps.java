/*
 * *******************************************************
 * Copyright VMware, Inc. 2013, 2016.  All Rights Reserved.
 * SPDX-License-Identifier: MIT
 * *******************************************************
 *
 * DISCLAIMER. THIS PROGRAM IS PROVIDED TO YOU "AS IS" WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, WHETHER ORAL OR WRITTEN,
 * EXPRESS OR IMPLIED. THE AUTHOR SPECIFICALLY DISCLAIMS ANY IMPLIED
 * WARRANTIES OR CONDITIONS OF MERCHANTABILITY, SATISFACTORY QUALITY,
 * NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE.
 */
package vmware.samples.common.vim.helpers;

import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.ServiceContent;
import com.vmware.vim25.VimPortType;

public class VmVappPowerOps {
    private final VimPortType vimPort;
    @SuppressWarnings("unused")
    private final ServiceContent serviceContent;
    private final WaitForValues waitForValues;

    public VmVappPowerOps(VimPortType vimPort, ServiceContent serviceContent) {
        this.vimPort = vimPort;
        this.serviceContent = serviceContent;
        this.waitForValues = new WaitForValues(vimPort, serviceContent);
    }

    /**
     * Powers on VM and wait for power on operation to complete
     *
     * @param vmName name of the vm (for logging)
     * @param vmMor vm MoRef
     */
    public boolean powerOnVM(String vmName, ManagedObjectReference vmMor) {
        System.out.println("Powering on virtual machine : " + vmName + "["
                           + vmMor.getValue() + "]");
        try {
            ManagedObjectReference taskmor = vimPort.powerOnVMTask(vmMor, null);
            if (waitForValues.getTaskResultAfterDone(taskmor)) {
                System.out.println(vmName + "[" + vmMor.getValue()
                                   + "] powered on successfully");
                return true;
            } else {
                System.out.println("Unable to poweron vm : " + vmName + "["
                                   + vmMor.getValue() + "]");
                return false;
            }
        } catch (Exception e) {
            System.out.println("Unable to poweron vm : " + vmName + "[" + vmMor
                .getValue() + "]");
            System.out.println("Reason :" + e.getLocalizedMessage());
            return false;
        }
    }

    /**
     * Powers off VM and waits for power off operation to complete
     *
     * @param vmName name of the vm (for logging)
     * @param vmMor vm MoRef
     */
    public boolean powerOffVM(String vmName, ManagedObjectReference vmMor) {
        System.out.println("Powering off virtual machine : " + vmName + "["
                           + vmMor.getValue() + "]");
        try {
            ManagedObjectReference taskmor = vimPort.powerOffVMTask(vmMor);
            if (waitForValues.getTaskResultAfterDone(taskmor)) {
                System.out.println(vmName + "[" + vmMor.getValue()
                                   + "] powered off successfully");
                return true;
            } else {
                System.out.println("Unable to poweroff vm : " + vmName + "["
                                   + vmMor.getValue() + "]");
                return false;
            }
        } catch (Exception e) {
            System.out.println("Unable to poweroff vm : " + vmName + "[" + vmMor
                .getValue() + "]");
            System.out.println("Reason :" + e.getLocalizedMessage());
            return false;
        }
    }

    /**
     * Powers on vApp and waits for the the power on operation to complete
     *
     * @param vAppName name of the vApp (for logging)
     * @param vAppMor vApp MoRef
     */
    public boolean powerOnVApp(
        String vAppName, ManagedObjectReference vAppMor) {
        System.out.println("Powering on Virtual App : " + vAppName + "["
                           + vAppMor.getValue() + "]");
        try {
            ManagedObjectReference taskmor = vimPort.powerOnVAppTask(vAppMor);
            if (waitForValues.getTaskResultAfterDone(taskmor)) {
                System.out.println(vAppName + "[" + vAppMor.getValue()
                                   + "] powered on successfully");
                return true;
            } else {
                System.out.println("Unable to poweron vApp : " + vAppName + "["
                                   + vAppMor.getValue() + "]");
                return false;
            }
        } catch (Exception e) {
            System.out.println("Unable to poweron vApp : " + vAppName + "["
                               + vAppMor.getValue() + "]");
            System.out.println("Reason :" + e.getLocalizedMessage());
            return false;
        }
    }

    /**
     * Powers off vApp and waits for the power off operation to complete
     *
     * @param vAppName name of the vApp (for logging)
     * @param vAppMor vApp MoRef
     */
    public boolean powerOffVApp(
        String vAppName, ManagedObjectReference vAppMor) {
        System.out.println("Powering off Virtual App : " + vAppName + "["
                           + vAppMor.getValue() + "]");
        try {
            ManagedObjectReference taskmor = vimPort.powerOffVAppTask(vAppMor,
                true);
            if (waitForValues.getTaskResultAfterDone(taskmor)) {
                System.out.println(vAppName + "[" + vAppMor.getValue()
                                   + "] powered off successfully");
                return true;
            } else {
                System.out.println("Unable to poweroff vApp : " + vAppName + "["
                                   + vAppMor.getValue() + "]");
                return false;
            }
        } catch (Exception e) {
            System.out.println("Unable to poweroff vApp : " + vAppName + "["
                               + vAppMor.getValue() + "]");
            System.out.println("Reason :" + e.getLocalizedMessage());
            return false;
        }
    }

}
