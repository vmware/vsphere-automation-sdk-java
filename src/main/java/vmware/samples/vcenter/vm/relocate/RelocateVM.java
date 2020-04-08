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
package vmware.samples.vcenter.vm.relocate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.cli.Option;

import com.vmware.vcenter.VM;
import com.vmware.vcenter.VMTypes;
import com.vmware.vcenter.VMTypes.DiskRelocateSpec;

import vmware.samples.common.SamplesAbstractBase;

/**
 * Description: Demonstrates how to relocate a VM with optional destination
 * VM folder, datastore, host, cluster, resource pool, disk datastore.
 *
 * Author: VMware, Inc.
 * Sample Prerequisites:
 * The sample needs a source VM to relocate from.
 */
public class RelocateVM extends SamplesAbstractBase {
    private String vm;
    private String vmFolder;
    private String datastore;
    private String cluster;
    private String resourcePool;
    private String host;
    private String disk;
    private VM vmService;

    /**
     * Define the options specific to this sample and configure the sample using
     * command-line arguments or a config file
     *
     * @param args command line arguments passed to the sample
     */
    protected void parseArgs(String[] args) {
        Option vmOption = Option.builder()
            .longOpt("vm")
            .desc("The VM id of the VM to relocate.")
            .argName("VM")
            .required(true)
            .hasArg()
            .build();
        Option vmFolderOption = Option.builder()
            .longOpt("vmfolder")
            .desc("OPTIONAL: The ID of the vm folder in which to relocate the vm to.")
            .argName("VM FOLDER")
            .required(false)
            .hasArg()
            .build();
        Option datastoreOption = Option.builder()
            .longOpt("datastore")
            .desc("OPTIONAL: The ID of the datastore in which to relocate the vm to")
            .required(false)
            .argName("DATASTORE")
            .hasArg()
            .build();
        Option clusterOption = Option.builder()
            .longOpt("cluster")
            .desc("OPTIONAL: The ID of the cluster in which to relocate the vm to.")
            .argName("CLUSTER")
            .required(false)
            .hasArg()
            .build();
        Option resourcePoolOption = Option.builder()
            .longOpt("resourcepool")
            .desc("OPTIONAL: The ID of the resource pool in which to relocate the vm to.")
            .argName("RESOURCE POOL")
            .required(false)
            .hasArg()
            .build();
        Option hostOption = Option.builder()
            .longOpt("host")
            .desc("OPTIONAL: The ID of the host in which to relocate the vm to.")
            .argName("HOST")
            .required(false)
            .hasArg()
            .build();
        Option diskUpdateOption = Option.builder()
            .longOpt("disk")
            .desc("OPTIONAL: Comma separated disk-ID:datastore-ID map.")
            .argName("DISK")
            .required(false)
            .hasArg()
            .build();

        List<Option> optionList = Arrays.asList(vmOption,
            vmFolderOption,
            datastoreOption,
            clusterOption,
            resourcePoolOption,
            hostOption,
            diskUpdateOption);

        super.parseArgs(optionList, args);
        this.vm = (String) parsedOptions.get("vm");
        this.vmFolder = (String) parsedOptions.get("vmfolder");
        this.datastore = (String) parsedOptions.get("datastore");
        this.cluster = (String) parsedOptions.get("cluster");
        this.resourcePool = (String) parsedOptions.get("resourcepool");
        this.host = (String) parsedOptions.get("host");
        this.disk = (String) parsedOptions.get("disk");
    }

    protected void setup() throws Exception {
        this.vmService = vapiAuthHelper.getStubFactory().createStub(VM.class,
            sessionStubConfig);
    }

    protected void run() throws Exception {
        // Get a placement spec
        VMTypes.RelocatePlacementSpec vmRelocatePlacementSpec =
            new VMTypes.RelocatePlacementSpec();
        if (this.vmFolder != null) {
            vmRelocatePlacementSpec.setFolder(this.vmFolder);
        }
        if (this.cluster != null) {
            vmRelocatePlacementSpec.setCluster(this.cluster);
        }
        if (this.resourcePool != null) {
            vmRelocatePlacementSpec.setResourcePool(this.resourcePool);
        }
        if (this.host != null) {
            vmRelocatePlacementSpec.setHost(this.host);
        }
        if (this.datastore != null) {
            vmRelocatePlacementSpec.setDatastore(this.datastore);
        }
        // Perform a relocate.
        relocateVM(vmRelocatePlacementSpec);
    }

    /*
     * Settings of relocate operation.
     */
    private void relocateVM(
        VMTypes.RelocatePlacementSpec vmRelocatePlacementSpec) {
        VMTypes.RelocateSpec.Builder specBuilder =
            new VMTypes.RelocateSpec.Builder().
            setPlacement(vmRelocatePlacementSpec);

        // Handle mapping of cloning individual disks to other datastores.
        if (this.disk != null) {
            Map<String, DiskRelocateSpec> diskMap =
                new HashMap<String, DiskRelocateSpec>();
            StringTokenizer tokenizer = new StringTokenizer(this.disk, ",");
            while (tokenizer.hasMoreTokens()) {
                String[] keyValue = tokenizer.nextToken().split(":", 2);
                DiskRelocateSpec spec = new DiskRelocateSpec.Builder().
                    setDatastore(keyValue[1]).build();
                diskMap.put(keyValue[0], spec);
            }
            specBuilder.setDisks(diskMap);
        }

        VMTypes.RelocateSpec vmRelocateSpec = specBuilder.build();
        System.out.println("\n\n#### Example: Relocate VM with spec:\n"
                           + vmRelocateSpec);
        vmService.relocate(this.vm, vmRelocateSpec);
    }

    protected void cleanup() throws Exception {
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
        new RelocateVM().execute(args);
    }
}
