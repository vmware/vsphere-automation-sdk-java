/*
 * *******************************************************
 * Copyright VMware, Inc. 2016, 2020.  All Rights Reserved.
 * SPDX-License-Identifier: MIT
 * *******************************************************
 *
 * DISCLAIMER. THIS PROGRAM IS PROVIDED TO YOU "AS IS" WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, WHETHER ORAL OR WRITTEN,
 * EXPRESS OR IMPLIED. THE AUTHOR SPECIFICALLY DISCLAIMS ANY IMPLIED
 * WARRANTIES OR CONDITIONS OF MERCHANTABILITY, SATISFACTORY QUALITY,
 * NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE.
 */
package vmware.samples.vcenter.vm.create.basicvm;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.cli.Option;

import com.vmware.vcenter.VM;
import com.vmware.vcenter.VMTypes;
import com.vmware.vcenter.vm.GuestOS;
import com.vmware.vcenter.vm.hardware.DiskTypes;
import com.vmware.vcenter.vm.hardware.EthernetTypes;
import com.vmware.vcenter.vm.hardware.EthernetTypes.BackingType;
import com.vmware.vcenter.vm.hardware.ScsiAddressSpec;
import com.vmware.vcenter.vm.hardware.boot.DeviceTypes;

import vmware.samples.common.SamplesAbstractBase;
import vmware.samples.vcenter.helpers.NetworkHelper;
import vmware.samples.vcenter.helpers.PlacementHelper;

/**
 * Description: Demonstrates how to create a basic VM with following 
 * configuration: Basic VM (2 disks, 1 nic)
 *
 * Author: VMware, Inc.
 * Sample Prerequisites:
 * The sample needs a datacenter and the following resources:
 * - vm folder
 * - datastore (atleast one host)
 * - cluster (Optional)
 * - A standard switch network
 */
public class CreateBasicVM extends SamplesAbstractBase {
    private String vmFolderName;
    private String vmName;
    private String datastoreName;
    private String datacenterName;
    private String clusterName, hostName;
    private String standardPortgroupName;
    private static final String BASIC_VM_NAME = "Sample-Basic-VM";
    private GuestOS vmGuestOS = GuestOS.WINDOWS_9_64;
    private VM vmService;
    private String basicVMId;

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
            .desc("OPTIONAL: The name of the cluster in which to create the vm."
                + "If not provided, VM is placed under the host provided as input.")
            .argName("CLUSTER")
            .required(false)
            .hasArg()
            .build();
        Option hostOption = Option.builder()
             .longOpt("host")
             .desc("OPTIONAL: The name of the host in which to create the vm."
                    + "If not provided, VM is placed under first host in the datecenter")
             .argName("HOST")
             .required(false)
             .hasArg()
             .build();
        Option stdPortgroupOption = Option.builder()
            .longOpt("standardportgroup")
            .desc("The name of the standard portgroup")
            .argName("STANDARD PORTGROUP")
            .required(true)
            .hasArg()
            .build();

        List<Option> optionList = Arrays.asList(vmNameOption,
            vmFolderOption,
            datastoreOption,
            datacenterOption,
            clusterOption,
            stdPortgroupOption,
            hostOption);

        super.parseArgs(optionList, args);
        this.vmFolderName = (String) parsedOptions.get("vmfolder");
        this.vmName = (String) parsedOptions.get("vmname");
        this.datastoreName = (String) parsedOptions.get("datastore");
        this.datacenterName = (String) parsedOptions.get("datacenter");
        this.clusterName = (String) parsedOptions.get("cluster");
        this.hostName = (String) parsedOptions.get("host");;
        this.standardPortgroupName = (String) parsedOptions.get(
            "standardportgroup");
    }

    protected void setup() throws Exception {
        this.vmService = vapiAuthHelper.getStubFactory().createStub(VM.class,
            sessionStubConfig);
    }

    protected void run() throws Exception {
        VMTypes.PlacementSpec vmPlacementSpec = null;
        // Get a placement spec
         vmPlacementSpec = PlacementHelper
           .getVMPlacementSpec(this.vapiAuthHelper.getStubFactory(),
              this.sessionStubConfig,
              this.hostName,
              this.clusterName,
              this.datacenterName,
              this.vmFolderName,
              this.datastoreName);

        // Get a standard network backing
        String standardNetworkBacking = NetworkHelper.getStandardNetworkBacking(
            this.vapiAuthHelper.getStubFactory(),
            this.sessionStubConfig,
            this.datacenterName,
            this.standardPortgroupName);

        // Create the basic VM
        createBasicVM(vmPlacementSpec, standardNetworkBacking);
    }

    /*
     * Creates a basic VM on a cluster with the following configuration:
     * - Create 2 disks and specify one of them on scsi0:0 since
     * it's the boot disk.
     * - Specify 1 ethernet adapter using a Standard Portgroup backing.
     * - Setup for PXE install by selecting network as first boot device.
     * - Use guest and system provided defaults for most configuration settings.
     */
    private void createBasicVM(
        VMTypes.PlacementSpec vmPlacementSpec, String standardNetworkBacking) {
        // Create the scsi disk as a boot disk
        DiskTypes.CreateSpec bootDiskCreateSpec =
                new DiskTypes.CreateSpec.Builder().setType(
                    DiskTypes.HostBusAdapterType.SCSI)
                    .setScsi(new ScsiAddressSpec.Builder(0l).setUnit(0l)
                        .build())
                    .setNewVmdk(new DiskTypes.VmdkCreateSpec())
                    .build();

        // Create a data disk
        DiskTypes.CreateSpec dataDiskCreateSpec =
                new DiskTypes.CreateSpec.Builder().setNewVmdk(
                    new DiskTypes.VmdkCreateSpec()).build();
        List<DiskTypes.CreateSpec> disks = Arrays.asList(bootDiskCreateSpec,
            dataDiskCreateSpec);

        // Create a nic with standard network backing
        EthernetTypes.BackingSpec nicBackingSpec =
                new EthernetTypes.BackingSpec.Builder(
                    BackingType.STANDARD_PORTGROUP).setNetwork(
                        standardNetworkBacking).build();
        EthernetTypes.CreateSpec nicCreateSpec =
                new EthernetTypes.CreateSpec.Builder().setStartConnected(true)
                    .setBacking(nicBackingSpec)
                    .build();
        List<EthernetTypes.CreateSpec> nics = Collections.singletonList(
            nicCreateSpec);

        // Specify the boot order
        List<DeviceTypes.EntryCreateSpec> bootDevices = Arrays.asList(
            new DeviceTypes.EntryCreateSpec.Builder(DeviceTypes.Type.ETHERNET)
                .build(),
            new DeviceTypes.EntryCreateSpec.Builder(DeviceTypes.Type.DISK)
                .build());
        //Use the VM name provided by the user else use the default VM name
        String vmName = (null == this.vmName || this.vmName.isEmpty())?
        				BASIC_VM_NAME :this.vmName;
        VMTypes.CreateSpec vmCreateSpec = new VMTypes.CreateSpec.Builder(
            this.vmGuestOS).setName(vmName)
                .setBootDevices(bootDevices)
                .setPlacement(vmPlacementSpec)
                .setNics(nics)
                .setDisks(disks)
                .build();
        System.out.println("\n\n#### Example: Creating Basic VM with spec:\n"
                           + vmCreateSpec);
        this.basicVMId = vmService.create(vmCreateSpec);
        VMTypes.Info vmInfo = vmService.get(this.basicVMId);
        System.out.println("\nBasic VM Info:\n" + vmInfo);
    }

    protected void cleanup() throws Exception {
        if (this.basicVMId != null) {
            System.out.println("\n\n#### Deleting the Basic VM");
            this.vmService.delete(this.basicVMId);
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
        new CreateBasicVM().execute(args);
    }
}
