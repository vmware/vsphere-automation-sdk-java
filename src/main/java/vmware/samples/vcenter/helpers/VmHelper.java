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
}