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
package vmware.samples.vcenter.vm.instantclone;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.Option;

import com.vmware.vcenter.VM;
import com.vmware.vcenter.VMTypes;

import vmware.samples.common.SamplesAbstractBase;

/**
 * Description: Demonstrates how to instant clone a VM with optional destination
 * VM folder, datastore, resource pool, and BIOS UUID.
 *
 * Author: VMware, Inc.
 * Sample Prerequisites:
 * The sample needs a source VM and VM name to instant clone as.
 */
public class InstantCloneVM extends SamplesAbstractBase {
    private String sourceVm;
    private String vmFolder;
    private String vmName;
    private String datastore;
    private String resourcePool;
    private String biosUuid;
    private VM vmService;
    private String resultVMId;

    /**
     * Define the options specific to this sample and configure the sample using
     * command-line arguments or a config file
     *
     * @param args command line arguments passed to the sample
     */
    protected void parseArgs(String[] args) {
        Option sourceVmOption = Option.builder()
            .longOpt("sourcevm")
            .desc("The VM id of the VM to clone from.")
            .argName("SOURCEVM")
            .required(true)
            .hasArg()
            .build();
        Option vmNameOption = Option.builder()
            .longOpt("vmname")
            .desc("The name of the vm to be created.")
            .argName("VMNAME")
            .required(true)
            .hasArg()
            .build();
        Option vmFolderOption = Option.builder()
            .longOpt("vmfolder")
            .desc("OPTIONAL: The ID of the vm folder in which to clone the vm to.")
            .argName("VM FOLDER")
            .required(false)
            .hasArg()
            .build();
        Option datastoreOption = Option.builder()
            .longOpt("datastore")
            .desc("OPTIONAL: The ID of the datastore in which to clone the vm to")
            .required(false)
            .argName("DATASTORE")
            .hasArg()
            .build();
        Option resourcePoolOption = Option.builder()
            .longOpt("resourcepool")
            .desc("OPTIONAL: The ID of the resource pool in which to clone the vm to.")
            .argName("RESOURCE POOL")
            .required(false)
            .hasArg()
            .build();
        Option biosUuidOption = Option.builder()
            .longOpt("biosuuid")
            .desc("OPTIONAL: 128-bit SMBIOS UUID of a virtual machine.")
            .argName("BIOS UUID")
            .required(false)
            .hasArg()
            .build();
        List<Option> optionList = Arrays.asList(sourceVmOption,
            vmNameOption,
            vmFolderOption,
            datastoreOption,
            resourcePoolOption,
            biosUuidOption);

        super.parseArgs(optionList, args);
        this.sourceVm = (String) parsedOptions.get("sourcevm");
        this.vmFolder = (String) parsedOptions.get("vmfolder");
        this.vmName = (String) parsedOptions.get("vmname");
        this.datastore = (String) parsedOptions.get("datastore");
        this.resourcePool = (String) parsedOptions.get("resourcepool");
        this.biosUuid = (String) parsedOptions.get("biosuuid");
    }

    protected void setup() throws Exception {
        this.vmService = vapiAuthHelper.getStubFactory().createStub(VM.class,
            sessionStubConfig);
    }

    protected void run() throws Exception {
        // Get a placement spec
        VMTypes.InstantClonePlacementSpec vmInstantClonePlacementSpec =
            new VMTypes.InstantClonePlacementSpec();
        if (this.vmFolder != null) {
            vmInstantClonePlacementSpec.setFolder(this.vmFolder);
        }
        if (this.resourcePool != null) {
            vmInstantClonePlacementSpec.setResourcePool(this.resourcePool);
        }
        if (this.datastore != null) {
            vmInstantClonePlacementSpec.setDatastore(this.datastore);
        }
        // Perform a clone.
        instantCloneVM(vmInstantClonePlacementSpec);
    }

    /*
     * Settings of clone operation.
     */
    private void instantCloneVM(
        VMTypes.InstantClonePlacementSpec vmInstantClonePlacementSpec) {
        VMTypes.InstantCloneSpec.Builder builder =
            new VMTypes.InstantCloneSpec.Builder(
                this.sourceVm, this.vmName).
            setPlacement(vmInstantClonePlacementSpec);
        if (this.biosUuid != null) {
            builder.setBiosUuid(this.biosUuid);
        }
        VMTypes.InstantCloneSpec instantCloneSpec = builder.build();

        System.out.println("\n\n#### Example: Instant Clone VM with spec:\n"
                           + instantCloneSpec);
        this.resultVMId = vmService.instantClone(instantCloneSpec);
        VMTypes.Info vmInfo = vmService.get(this.resultVMId);
        System.out.println("\nVM Info:\n" + vmInfo);
    }

    protected void cleanup() throws Exception {
        if (this.resultVMId != null) {
            System.out.println("\n\n#### Deleting the resulting VM");
            this.vmService.delete(this.resultVMId);
        }
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
        new InstantCloneVM().execute(args);
    }
}
