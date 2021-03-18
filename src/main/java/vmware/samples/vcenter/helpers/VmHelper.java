/*
 * *******************************************************
 * Copyright VMware, Inc. 2016.  All Rights Reserved.
 * SPDX-License-Identifier: MIT
 * *******************************************************
 *
 * DISCLAIMER. THIS PROGRAM IS PROVIDED TO YOU "AS IS" WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, WHETHER ORAL OR WRITTEN,
 * EXPRESS OR IMPLIED. THE AUTHOR SPECIFICALLY DISCLAIMS ANY IMPLIED
 * WARRANTIES OR CONDITIONS OF MERCHANTABILITY, SATISFACTORY QUALITY,
 * NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE.
 */
package vmware.samples.vcenter.helpers;

import java.util.Collections;
import java.util.List;

import com.vmware.vapi.bindings.StubConfiguration;
import com.vmware.vapi.bindings.StubFactory;
import com.vmware.vcenter.VM;
import com.vmware.vcenter.VMTypes;
import com.vmware.vcenter.vm.guest.Identity;
import com.vmware.vcenter.vm.guest.IdentityTypes;
import com.vmware.vapi.std.errors.ServiceUnavailable;

public class VmHelper {
    /**
     * Returns the identifier of a vm
     *
     * Note: The method assumes that there is only one vm with the specified
     * name.
     *
     * @param stubFactory Stub factory for the api endpoint
     * @param sessionStubConfig stub configuration for the current session
     * @param vmName name of the virtual machine
     * @return the identifier of a virtual machine with the specified name
     */
    public static String getVM(
        StubFactory stubFactory, StubConfiguration sessionStubConfig,
        String vmName) {
        VM vmService = stubFactory.createStub(VM.class, sessionStubConfig);

        // Get summary information about the virtual machine
        VMTypes.FilterSpec vmFilterSpec = new VMTypes.FilterSpec.Builder()
            .setNames(Collections.singleton(vmName)).build();
        List<VMTypes.Summary> vmList = vmService.list(vmFilterSpec);
        assert vmList.size() > 0 && vmList.get(0).getName().equals(
            vmName) : "VM with name " + vmName + " not found";

        return vmList.get(0).getVm();
    }

   /**
     * it will wait till guest vm become powered on and information about the same is available.
     * @param identityService : guest vm identity service object
     * @param vmId : id of the virtual machine
     * @param timeout:  waiting time period to guest info
     * @throws Exception
     */
    public static void waitForGuestInfoReady(Identity identityService, String vmId, int timeout) throws Exception {
        System.out.println("Waiting for guest info to be ready.");
        int start=(int) System.currentTimeMillis()/1000;
        timeout = start + timeout;
        IdentityTypes.Info result;
        while(timeout > (int) System.currentTimeMillis()/1000) {
            System.out.println("Waiting for guest info to be ready");
            Thread.sleep(1000);
             try{
                 result=identityService.get(vmId);
                 break;
             }
            catch(ServiceUnavailable e) {
                System.out.println("Got ServiceUnavailable waiting for guest info");
                continue;
            }
             catch(Exception e) {
                 System.out.println("Unexpected exception %s waiting for guest info");
                 throw new Exception(e.getMessage());
             }
        }
        if((int) System.currentTimeMillis()/1000 >= timeout) {
            throw new Exception("Timed out waiting for guest info to be available.Be sure the VM has VMware Tools");
        }
        else {
            int duration=((int) System.currentTimeMillis()/1000)-start;
            System.out.println("Took "+duration+" seconds for guest info to be available");
        }
    }
}
