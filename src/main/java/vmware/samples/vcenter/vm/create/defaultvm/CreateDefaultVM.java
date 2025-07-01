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
package vmware.samples.vcenter.vm.create.defaultvm;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.Option;

import com.vmware.vcenter.VM;
import com.vmware.vcenter.VMTypes;
import com.vmware.vcenter.vm.GuestOS;

import vmware.samples.common.SamplesAbstractBase;
import vmware.samples.vcenter.helpers.PlacementHelper;

/**
 * Description: Demonstrates how to create a VM with system provided defaults
 *
 * Author: VMware, Inc.
 * Sample Prerequisites:
 * The sample needs a datacenter and the following resources:
 * - vm folder
 * - datastore
 * - cluster
 * - A standard switch network
 */
public class CreateDefaultVM extends SamplesAbstractBase {
    private String vmFolderName;
    private String vmName;
    private String datastoreName;
    private String datacenterName;
    private String clusterName;
    private VM vmService;
    private GuestOS vmGuestOS = GuestOS.WINDOWS_9_64;
    private static final String DEFAULT_VM_NAME = "Sample-Default-VM";
    private String defaultVMId;

    /**
     * Define the options specific to this sample and configure the sample using
     * command-line arguments or a config file
     *
     * @param args command line arguments passed to the sample
     */
    protected void parseArgs(String[] args) {
        Option datacenterOption = Option.builder()
            .longOpt("datacenter")
            .desc("The name of the datacenter in which to create the vm.")
            .argName("DATACENTER")
            .required(true)
            .hasArg()
            .build();
        Option vmFolderOption = Option.builder()
            .longOpt("vmfolder")
            .desc("The name of the vm folder in which to create the vm.")
            .argName("VM FOLDER")
            .required(true)
            .hasArg()
            .build();
        Option vmNameOption = Option.builder()
                .longOpt("vmname")
                .desc("OPTIONAL: The name of the vm to be created.")
                .argName("VMNAME")
                .required(false)
                .hasArg()
                .build();
        Option datastoreOption = Option.builder()
            .longOpt("datastore")
            .desc("The name of the datastore in which to create the vm")
            .required(true)
            .argName("DATASTORE")
            .hasArg()
            .build();
        Option clusterOption = Option.builder()
            .longOpt("cluster")
            .desc("The name of the cluster in which to create the vm.")
            .argName("CLUSTER")
            .required(true)
            .hasArg()
            .build();

        List<Option> optionList = Arrays.asList(vmNameOption,
            vmFolderOption,
            datastoreOption,
            datacenterOption,
            clusterOption);

        super.parseArgs(optionList, args);
        this.vmFolderName = (String) parsedOptions.get("vmfolder");
        this.vmName = (String) parsedOptions.get("vmname");
        this.datastoreName = (String) parsedOptions.get("datastore");
        this.datacenterName = (String) parsedOptions.get("datacenter");
        this.clusterName = (String) parsedOptions.get("cluster");
    }

    protected void setup() throws Exception {
        this.vmService = vapiAuthHelper.getStubFactory().createStub(VM.class,
            sessionStubConfig);
    }

    protected void run() throws Exception {
        // Get a placement spec
        VMTypes.PlacementSpec vmPlacementSpec =
                PlacementHelper.getPlacementSpecForCluster(
                    this.vapiAuthHelper.getStubFactory(),
                    this.sessionStubConfig,
                    this.datacenterName,
                    this.clusterName,
                    this.vmFolderName,
                    this.datastoreName);

        // Create the default VM
        createDefaultVM(vmPlacementSpec);
    }

    /*
     * Creates a VM on a cluster with selected Guest OS and name which
     * uses all the system provided defaults.
     */
    private void createDefaultVM(VMTypes.PlacementSpec vmPlacementSpec) {
    	String vmName = (null == this.vmName || this.vmName.isEmpty())?
    					DEFAULT_VM_NAME :this.vmName;
        VMTypes.CreateSpec vmCreateSpec =
                new VMTypes.CreateSpec.Builder(this.vmGuestOS)
                    .setName(vmName)
                    .setPlacement(vmPlacementSpec)
                    .build();
        System.out.println("\n\n#### Example: Creating Default VM with spec:\n"
                           + vmCreateSpec);
        defaultVMId = vmService.create(vmCreateSpec);
        System.out.println("\nCreated default VM : " + DEFAULT_VM_NAME
                           + " with id: " + this.defaultVMId);
        VMTypes.Info vmInfo = this.vmService.get(defaultVMId);
        System.out.println("\nDefault VM Info:\n" + vmInfo);
    }

    protected void cleanup() throws Exception {
        System.out.println("\n\n#### Deleting the Default VM");
        if(this.defaultVMId != null) {
            this.vmService.delete(this.defaultVMId);
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
        new CreateDefaultVM().execute(args);
    }
}
