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
package vmware.samples.contentlibrary.isomount;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.cli.Option;

import com.vmware.content.library.ItemTypes;
import com.vmware.vapi.bindings.StubConfiguration;
import com.vmware.vcenter.VM;
import com.vmware.vcenter.VMTypes;

import vmware.samples.common.SamplesAbstractBase;
import vmware.samples.common.authentication.VapiAuthenticationHelper;
import vmware.samples.contentlibrary.client.ClsApiClient;

/**
 * Description: Demonstrates the content library ISO item mount and unmount
 * workflow via the mount and unmount APIs from the ISO service.
 *
 * Author: VMware, Inc.
 * Sample Prerequisites: Running this sample requires
 * creation of a VM as well as a library item of type ISO
 */
public class IsoMount extends SamplesAbstractBase {
    public static String ISO_TYPE = "iso";
    private String vmName;
    private String vmId;
    private String itemName;
    private String itemId;

    private ClsApiClient client;

    /**
     * Define the options specific to this sample and configure the sample using
     * command-line arguments or a config file
     *
     * @param args command line arguments passed to the sample
     */
    protected void parseArgs(String[] args) {
        // Parse the command line options or use config file
        Option vmNameOption = Option.builder()
            .longOpt("vmname")
            .desc("Name of the virtual machine")
            .required(true)
            .hasArg()
            .argName("Virtual Machine")
            .build();

        /**
         * For the sample code that creates as iso library item, please
         *
         * @see ContentUpdate
         */
        Option itemNameOption = Option.builder()
            .longOpt("isoitemnametomount")
            .desc("Name of an existing iso library item to mount")
            .required(true)
            .hasArg()
            .argName("Iso Library Item")
            .build();

        List<Option> optionList = Arrays.asList(vmNameOption, itemNameOption);
        super.parseArgs(optionList, args);
        this.vmName = (String) parsedOptions.get("vmname");
        this.itemName = (String) parsedOptions.get("isoitemnametomount");
    }

    protected void setup() throws Exception {
        this.client = new ClsApiClient(vapiAuthHelper.getStubFactory(),
                sessionStubConfig);

        this.itemId = getItemId();
        this.vmId = getVmId(vapiAuthHelper, sessionStubConfig);
        System.out.println("Mounting ISO item " + this.itemName + " ("
                           + this.itemId + ") on VM " + this.vmName + " ("
                           + this.vmId + ")");
    }

    private String getVmId(VapiAuthenticationHelper vapiAuthHelper,
                           StubConfiguration sessionStubConfig) {
        // Look up the VM using the specified vmName
        VM vmService = vapiAuthHelper.getStubFactory().createStub(VM.class,
                sessionStubConfig);
        VMTypes.FilterSpec vmFilterSpec = new VMTypes.FilterSpec.Builder()
                .setNames(new HashSet<>(Arrays.asList(vmName))).build();
        List<VMTypes.Summary> vmList = vmService.list(vmFilterSpec);
        assert vmList.size() > 0 : "VM By Name '" + vmName
                + "' does not exist";
        String vmId = vmList.get(0).getVm();
        return vmId;
    }
    
    
    private String getItemId() {
        // Look up the library item using the specified itemName
        ItemTypes.FindSpec itemFindSpec = new ItemTypes.FindSpec();
        itemFindSpec.setName(itemName);
        itemFindSpec.setType(ISO_TYPE);
        List<String> isoItemIds = client.itemService().find(itemFindSpec);
        assert isoItemIds.size() == 1 : "library item by name " + itemName
                + " and type " + ISO_TYPE + " must exist";
        return isoItemIds.get(0);
    }

    protected void run() throws Exception {
        String deviceId = mount(itemId, vmId);
        unmount(vmId, deviceId);
    }

    protected void cleanup() throws Exception {
        // No clean up necessary as no new items are created
        // Also the mounted device has been unmounted
    }

    private String mount(String itemId, String vmId) {
        // Mount the specified iso item on the given VM
        // Return the id of the mounted device
        String deviceId = client.isoImageService().mount(itemId, vmId);
        System.out.println("Mounted device: " + deviceId);
        return deviceId;
    }

    private void unmount(String vmId, String deviceId) {
        // Unmount the given device from the VM
        client.isoImageService().unmount(vmId, deviceId);
        System.out.println("Unmounted device: " + deviceId);
    }

    public static void main(String[] args) throws Exception {
        /*
         * Execute the sample using the command line arguments or parameters
         * from the configuration file. This executes the following steps:
         * 1. Parse the arguments required by the sample
         * 2. Login to the server
         * 3. Setup any resources required by the sample run
         * 4. Run the sample
         * 5. Cleanup any data created by the sample run, if cleanup=true
         * 6. Logout of the server
         */
        new IsoMount().execute(args);
    }
}
