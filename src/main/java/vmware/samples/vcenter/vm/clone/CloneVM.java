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
package vmware.samples.vcenter.vm.clone;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.commons.cli.Option;

import com.vmware.vcenter.VM;
import com.vmware.vcenter.VMTypes;
import com.vmware.vcenter.VMTypes.DiskCloneSpec;

import vmware.samples.common.SamplesAbstractBase;

/**
 * Description: Demonstrates how to clone a VM with optional destination
 * VM folder, datastore, host, cluster, resource pool, disk datastore.
 *
 * Author: VMware, Inc.
 * Sample Prerequisites:
 * The sample needs a source VM and VM name to clone as.
 */
public class CloneVM extends SamplesAbstractBase {
    private String sourceVm;
    private String vmFolder;
    private String vmName;
    private String datastore;
    private String cluster;
    private String resourcePool;
    private String host;
    private String diskUpdate;
    private String diskRemove;
    private Boolean powerOn;
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
        Option clusterOption = Option.builder()
            .longOpt("cluster")
            .desc("OPTIONAL: The ID of the cluster in which to clone the vm to.")
            .argName("CLUSTER")
            .required(false)
            .hasArg()
            .build();
        Option resourcePoolOption = Option.builder()
            .longOpt("resourcepool")
            .desc("OPTIONAL: The ID of the resource pool in which to clone the vm to.")
            .argName("RESOURCE POOL")
            .required(false)
            .hasArg()
            .build();
        Option hostOption = Option.builder()
            .longOpt("host")
            .desc("OPTIONAL: The ID of the host in which to clone the vm to.")
            .argName("HOST")
            .required(false)
            .hasArg()
            .build();
        Option diskUpdateOption = Option.builder()
            .longOpt("diskupdate")
            .desc("OPTIONAL: Comma separated disk-ID:datastore-ID map.")
            .argName("DISK UPDATE")
            .required(false)
            .hasArg()
            .build();
        Option diskRemoveOption = Option.builder()
            .longOpt("diskremove")
            .desc("OPTIONAL: Comma separated disk-ID to remove.")
            .argName("DISK REMOVE")
            .required(false)
            .hasArg()
            .build();
        Option powerOnOption = Option.builder()
            .longOpt("poweron")
            .desc("OPTIONAL: Power on the cloned VM after the operation.")
            .argName("POWER-ON")
            .required(false)
            .build();
        List<Option> optionList = Arrays.asList(sourceVmOption,
            vmNameOption,
            vmFolderOption,
            datastoreOption,
            clusterOption,
            resourcePoolOption,
            hostOption,
            diskUpdateOption,
            diskRemoveOption,
            powerOnOption);

        super.parseArgs(optionList, args);
        this.sourceVm = (String) parsedOptions.get("sourcevm");
        this.vmFolder = (String) parsedOptions.get("vmfolder");
        this.vmName = (String) parsedOptions.get("vmname");
        this.datastore = (String) parsedOptions.get("datastore");
        this.cluster = (String) parsedOptions.get("cluster");
        this.resourcePool = (String) parsedOptions.get("resourcepool");
        this.host = (String) parsedOptions.get("host");
        this.diskUpdate = (String) parsedOptions.get("diskupdate");
        this.diskRemove = (String) parsedOptions.get("diskremove");
        this.powerOn = (Boolean) parsedOptions.get("poweron");
    }

    protected void setup() throws Exception {
        this.vmService = vapiAuthHelper.getStubFactory().createStub(VM.class,
            sessionStubConfig);
    }

    protected void run() throws Exception {
        // Get a placement spec
        VMTypes.ClonePlacementSpec vmClonePlacementSpec =
            new VMTypes.ClonePlacementSpec();
        if (this.vmFolder != null) {
            vmClonePlacementSpec.setFolder(this.vmFolder);
        }
        if (this.cluster != null) {
            vmClonePlacementSpec.setCluster(this.cluster);
        }
        if (this.resourcePool != null) {
            vmClonePlacementSpec.setResourcePool(this.resourcePool);
        }
        if (this.host != null) {
            vmClonePlacementSpec.setHost(this.host);
        }
        if (this.datastore != null) {
            vmClonePlacementSpec.setDatastore(this.datastore);
        }
        // Perform a clone.
        cloneVM(vmClonePlacementSpec);
    }

    /*
     * Settings of clone operation.
     */
    private void cloneVM(
        VMTypes.ClonePlacementSpec vmClonePlacementSpec) {
        VMTypes.CloneSpec.Builder specBuilder = new VMTypes.CloneSpec.Builder(
            this.sourceVm, this.vmName).setPlacement(vmClonePlacementSpec).
            setPowerOn(this.powerOn);

        // Handle mapping of cloning individual disks to other datastores.
        if (this.diskUpdate != null) {
            Map<String, DiskCloneSpec> diskUpdateMap =
                new HashMap<String, DiskCloneSpec>();
            StringTokenizer tokenizer = new StringTokenizer(this.diskUpdate, ",");
            while (tokenizer.hasMoreTokens()) {
                String[] keyValue = tokenizer.nextToken().split(":", 2);
                DiskCloneSpec spec = new DiskCloneSpec.Builder().
                    setDatastore(keyValue[1]).build();
                diskUpdateMap.put(keyValue[0], spec);
            }
            specBuilder.setDisksToUpdate(diskUpdateMap);
        }

        // Handle mapping of removing individual disks from cloned VM.
        if (this.diskRemove != null) {
            Set<String> diskRemoveSet = new HashSet<String>();
            StringTokenizer tokenizer = new StringTokenizer(this.diskRemove, ",");
            while (tokenizer.hasMoreTokens()) {
                diskRemoveSet.add(tokenizer.nextToken());
            }
            specBuilder.setDisksToRemove(diskRemoveSet);
        }

        VMTypes.CloneSpec vmCloneSpec = specBuilder.build();
        System.out.println("\n\n#### Example: Clone VM with spec:\n"
                           + vmCloneSpec);
        this.resultVMId = vmService.clone(vmCloneSpec);
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
        new CloneVM().execute(args);
    }
}
