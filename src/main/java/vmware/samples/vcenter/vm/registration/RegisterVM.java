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
package vmware.samples.vcenter.vm.registration;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.Option;

import com.vmware.vcenter.VM;
import com.vmware.vcenter.VMTypes;
import com.vmware.vcenter.VMTypes.RegisterSpec;
import com.vmware.vcenter.VMTypes.RegisterPlacementSpec;

import vmware.samples.common.SamplesAbstractBase;

/**
 * Description: Demonstrates how to register a VM with destination
 * VM folder, datastore with path, host, cluster, and resource pool.
 *
 * Author: VMware, Inc.
 * Sample Prerequisites:
 * The sample needs the following resources:
 * - vm folder
 * - datastore ID and relative path, or complete datastore path
 * - At least one of host, resource pool, or cluster
 */
public class RegisterVM extends SamplesAbstractBase {
    private String datastore;
    private String path;
    private String datastorePath;
    private String vmFolder;
    private String vmName;
    private String cluster;
    private String resourcePool;
    private String host;
    private VM vmService;
    private String resultVMId;

    /**
     * Define the options specific to this sample and configure the sample using
     * command-line arguments or a config file
     *
     * @param args command line arguments passed to the sample
     */
    protected void parseArgs(String[] args) {
        Option datastoreOption = Option.builder()
            .longOpt("datastore")
            .desc("OPTIONAL: The datastore ID where the VM is located.\n" +
                  "If set, path must also be set. If unset datastorePath must" +
                  " be set.")
            .argName("DATASTORE")
            .required(false)
            .hasArg()
            .build();
        Option pathOption = Option.builder()
            .longOpt("path")
            .desc("OPTIONAL: The path on the datastore where the VM is " +
                  "located.\n" +
                  "If set, datastore must also be set. If unset " +
                  "datastorePath must be set.")
            .argName("PATH")
            .required(false)
            .hasArg()
            .build();
        Option datastorePathOption = Option.builder()
            .longOpt("datastorePath")
            .desc("OPTIONAL: The datastore and path where the VM is " +
                  "located.\n" +
                  "If set, datastore and path must be unset.")
            .argName("DATASTOREPATH")
            .required(false)
            .hasArg()
            .build();
          Option vmNameOption = Option.builder()
            .longOpt("vmname")
            .desc("OPTIONAL: The name of the vm to be created.")
            .argName("VMNAME")
            .required(false)
            .hasArg()
            .build();
        Option vmFolderOption = Option.builder()
            .longOpt("vmfolder")
            .desc("The ID of the vm folder in which to register the vm to.")
            .argName("VM FOLDER")
            .required(true)
            .hasArg()
            .build();
        Option clusterOption = Option.builder()
            .longOpt("cluster")
            .desc("OPTIONAL: The ID of the cluster in which to register the " +
                  "vm to. One of cluster, resourcePool or host must be set.")
            .argName("CLUSTER")
            .required(false)
            .hasArg()
            .build();
        Option resourcePoolOption = Option.builder()
            .longOpt("resourcePool")
            .desc("OPTIONAL: The ID of the resource pool in which to " +
                  "register the vm to. One of cluster, resourcePool or host " +
                  "must be set.")
            .argName("RESOURCE POOL")
            .required(false)
            .hasArg()
            .build();
        Option hostOption = Option.builder()
            .longOpt("host")
            .desc("OPTIONAL: The ID of the host in which to register the " +
                  "vm to. One of cluster, resourcePool or host must be set.")
            .argName("HOST")
            .required(false)
            .hasArg()
            .build();
        List<Option> optionList = Arrays.asList(datastoreOption,
            pathOption,
            datastorePathOption,
            vmNameOption,
            vmFolderOption,
            clusterOption,
            resourcePoolOption,
            hostOption);

        super.parseArgs(optionList, args);
        this.datastore = (String) parsedOptions.get("datastore");
        this.path = (String) parsedOptions.get("path");
        this.datastorePath = (String) parsedOptions.get("datastorePath");
        this.vmFolder = (String) parsedOptions.get("vmfolder");
        this.vmName = (String) parsedOptions.get("vmname");
        this.cluster = (String) parsedOptions.get("cluster");
        this.resourcePool = (String) parsedOptions.get("resourcePool");
        this.host = (String) parsedOptions.get("host");
    }

    protected void setup() throws Exception {
        this.vmService = vapiAuthHelper.getStubFactory().createStub(VM.class,
            sessionStubConfig);
    }

    protected void run() throws Exception {
        // Basic parameter validation.
        if ((this.datastore != null && this.path != null &&
             this.datastorePath != null) ||
            (this.datastore == null && this.path == null &&
             this.datastorePath == null)) {
            throw new Exception("Either datastore with path or datastorePath " +
                                "must be set but not both");
        }
        if (this.host == null && this.cluster == null &&
            this.resourcePool == null) {
            throw new Exception("At least one of host, cluster or " +
                                "resourcePool must be set");
        }

        // Get a placement spec
        RegisterPlacementSpec vmRegisterPlacementSpec =
            new RegisterPlacementSpec();
        vmRegisterPlacementSpec.setFolder(this.vmFolder);
        if (this.cluster != null) {
            vmRegisterPlacementSpec.setCluster(this.cluster);
        }
        if (this.resourcePool != null) {
            vmRegisterPlacementSpec.setResourcePool(this.resourcePool);
        }
        if (this.host != null) {
            vmRegisterPlacementSpec.setHost(this.host);
        }
        // Perform a VM register.
        registerVM(vmRegisterPlacementSpec);
    }

    /*
     * Settings of register operation.
     */
    private void registerVM(
        RegisterPlacementSpec vmRegisterPlacementSpec) {
        RegisterSpec.Builder specBuilder =
            new RegisterSpec.Builder().setPlacement(
                vmRegisterPlacementSpec);

       if (this.datastorePath != null) {
            specBuilder.setDatastorePath(this.datastorePath);
        } else {
            specBuilder.setDatastore(this.datastore);
            specBuilder.setPath(this.path);
        }

        RegisterSpec vmRegisterSpec = specBuilder.build();
        System.out.println("\n\n#### Example: Register VM with spec:\n"
                           + vmRegisterSpec);
        this.resultVMId = vmService.register(vmRegisterSpec);
        VMTypes.Info vmInfo = vmService.get(this.resultVMId);
        System.out.println("\nVM Info:\n" + vmInfo);
    }

    protected void cleanup() throws Exception {
        if (this.resultVMId != null) {
            System.out.println("\n\n#### Unregistering the resulting VM");
            this.vmService.unregister(this.resultVMId);
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
        new RegisterVM().execute(args);
    }
}
