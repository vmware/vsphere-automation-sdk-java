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
package vmware.samples.vcenter.vm.create.exhaustivevm;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.cli.Option;

import com.vmware.vcenter.VM;
import com.vmware.vcenter.VMTypes;
import com.vmware.vcenter.vm.GuestOS;
import com.vmware.vcenter.vm.HardwareTypes;
import com.vmware.vcenter.vm.hardware.CdromTypes;
import com.vmware.vcenter.vm.hardware.CpuTypes;
import com.vmware.vcenter.vm.hardware.DiskTypes;
import com.vmware.vcenter.vm.hardware.DiskTypes.HostBusAdapterType;
import com.vmware.vcenter.vm.hardware.EthernetTypes;
import com.vmware.vcenter.vm.hardware.FloppyTypes;
import com.vmware.vcenter.vm.hardware.MemoryTypes;
import com.vmware.vcenter.vm.hardware.ParallelTypes;
import com.vmware.vcenter.vm.hardware.ScsiAddressSpec;
import com.vmware.vcenter.vm.hardware.SerialTypes;
import com.vmware.vcenter.vm.hardware.boot.DeviceTypes;

import vmware.samples.common.SamplesAbstractBase;
import vmware.samples.vcenter.helpers.NetworkHelper;
import vmware.samples.vcenter.helpers.PlacementHelper;

/**
 * Description: Demonstrates how to create a exhaustive VM with the below 
 * configuration:
 * 3 disks, 2 nics, 2 vcpu, 2 GB, memory, boot=BIOS, 1 cdrom, 1 serial port,
 * 1 parallel port, 1 floppy, boot_device=[CDROM, DISK, ETHERNET])
 *
 * Author: VMware, Inc.
 * Sample Prerequisites:
 * The sample needs a datacenter and the following resources:
 * - vm folder
 * - resource pool
 * - datastore
 * - cluster
 * - A standard switch network
 * - A distributed switch network
 * - An iso file on the datastore mentioned above
 */
public class CreateExhaustiveVM extends SamplesAbstractBase {
    private String vmFolderName;
    private String vmName;
    private String datastoreName;
    private String datacenterName;
    private String clusterName;
    private String standardPortgroupName;
    private String distributedPortgroupName;
    private String isoDatastorePath;
    private VM vmService;
    private static final String EXHAUSTIVE_VM_NAME = "Sample-Exhaustive-VM";
    private static final String SERIAL_PORT_NETWORK_SERVICE_LOCATION =
            "tcp://localhost:16000";
    private static final long GB = 1024 * 1024 * 1024;
    private GuestOS vmGuestOS = GuestOS.WINDOWS_9_64;
    private HardwareTypes.Version HARDWARE_VERSION =
            HardwareTypes.Version.VMX_11;
    private String exhaustiveVMId;

    /**
     * Define the options specific to this sample and configure the sample using
     * command-line arguments or a config file
     *
     * @param args command line arguments passed to the sample
     */
    protected void parseArgs(String[] args) {
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
        Option datacenterOption = Option.builder()
            .longOpt("datacenter")
            .desc("The name of the datacenter in which to create the vm.")
            .argName("DATACENTER")
            .required(true)
            .hasArg()
            .build();
        Option clusterOption = Option.builder()
            .longOpt("cluster")
            .desc("The name of the cluster in which to create the vm.")
            .argName("CLUSTER")
            .required(true)
            .hasArg()
            .build();
        Option stdPortgroupOption = Option.builder()
            .longOpt("standardportgroup")
            .desc("The name of the standard portgroup")
            .argName("STANDARD PORTGROUP")
            .required(true)
            .hasArg()
            .build();
        Option distributedPortgroupOption = Option.builder()
            .longOpt("distributedportgroup")
            .desc("The name of the distributed portgroup")
            .argName("DISTRIBUTED PORTGROUP")
            .required(true)
            .hasArg()
            .build();
        Option isoDatastorePathOption = Option.builder()
            .longOpt("isodatastorepath")
            .desc("The path to the iso file on the datastore")
            .argName("ISO DATASTORE PATH")
            .required(true)
            .hasArg()
            .build();

        List<Option> optionList = Arrays.asList(vmNameOption,
            vmFolderOption,
            datastoreOption,
            datacenterOption,
            clusterOption,
            stdPortgroupOption,
            distributedPortgroupOption,
            isoDatastorePathOption);

        super.parseArgs(optionList, args);
        this.vmName = (String)parsedOptions.get("vmname");
        this.vmFolderName = (String) parsedOptions.get("vmfolder");
        this.datastoreName = (String) parsedOptions.get("datastore");
        this.datacenterName = (String) parsedOptions.get("datacenter");
        this.clusterName = (String) parsedOptions.get("cluster");
        this.standardPortgroupName =
                (String) parsedOptions.get("standardportgroup");
        this.distributedPortgroupName =
                (String) parsedOptions.get("distributedportgroup");
        this.isoDatastorePath = (String) parsedOptions.get("isodatastorepath");
    }

    protected void setup() throws Exception {
        this.vmService = vapiAuthHelper.getStubFactory().createStub(VM.class,
            sessionStubConfig);
    }

    protected void run() throws Exception {
        // Get a placement spec
        VMTypes.PlacementSpec vmPlacementSpec = PlacementHelper
            .getPlacementSpecForCluster(this.vapiAuthHelper.getStubFactory(),
                this.sessionStubConfig,
                this.datacenterName,
                this.clusterName,
                this.vmFolderName,
                this.datastoreName);

        // Get a standard network backing
        String standardNetworkBacking = NetworkHelper.getStandardNetworkBacking(
            this.vapiAuthHelper.getStubFactory(),
            this.sessionStubConfig,
            this.datacenterName,
            this.standardPortgroupName);

        // Get a distributed network backing
        String distributedNetworkBacking = NetworkHelper
            .getDistributedNetworkBacking(this.vapiAuthHelper.getStubFactory(),
                this.sessionStubConfig,
                this.datacenterName,
                this.distributedPortgroupName);

        // Create the VM
        createExhaustiveVM(vmPlacementSpec,
            standardNetworkBacking,
            distributedNetworkBacking);
    }

    /*
     * Create an exhaustive VM with the following configuration:
     * - Hardware Version = VMX_11 (for 6.0)
     * - CPU (count = 2, coresPerSocket = 2, hotAddEnabled = false,
     *   hotRemoveEnabled = false)
     * - Memory (size_mib = 2 GB, hotAddEnabled = false)
     * - 3 Disks and specify each of the HBAs and the unit numbers
     *   (capacity=40 GB, name=<some value>, spaceEfficient=true)
     * - Specify 2 ethernet adapters, one using a Standard Portgroup backing
     *   and the other using a DISTRIBUTED_PORTGROUP networking backing.
     *        # nic1: Specify Ethernet (macType=MANUAL, macAddress=<some value>)
     *        # nic2: Specify Ethernet (macType=GENERATED)
     * - 1 CDROM (type=ISO_FILE, file="small.iso", startConnected=true)
     * - 1 Serial Port (type=NETWORK_SERVER, file="tcp://localhost/16000",
     *   startConnected=true)
     * - 1 Parallel Port  (type=HOST_DEVICE, startConnected=false)
     * - 1 Floppy Drive (type=CLIENT_DEVICE)
     * - Boot, type=BIOS
     * - BootDevice order: CDROM, DISK, ETHERNET
     */
    private void createExhaustiveVM(VMTypes.PlacementSpec vmPlacementSpec,
        String standardNetworkBacking, String distributedNetworkBacking) {
        // CPU UpdateSpec
        CpuTypes.UpdateSpec cpuUpdateSpec =
                new CpuTypes.UpdateSpec.Builder().setCoresPerSocket(1l)
                    .setHotAddEnabled(false)
                    .setHotRemoveEnabled(false)
                    .build();

        // Memory UpdateSpec
        MemoryTypes.UpdateSpec memoryUpdateSpec =
                new MemoryTypes.UpdateSpec.Builder().setSizeMiB(2 * 1024l)
                    .setHotAddEnabled(false)
                    .build();

        // Disk CreateSpec
        DiskTypes.CreateSpec diskCreateSpec1 =
                new DiskTypes.CreateSpec.Builder()
                    .setType(HostBusAdapterType.SCSI)
                    .setScsi(new ScsiAddressSpec.Builder(0).setUnit(0l).build())
                    .setNewVmdk(new DiskTypes.VmdkCreateSpec.Builder()
                        .setName("boot")
                        .setCapacity(40 * GB)
                        .build())
                    .build();

        DiskTypes.CreateSpec diskCreateSpec2 =
                new DiskTypes.CreateSpec.Builder()
                    .setNewVmdk(new DiskTypes.VmdkCreateSpec.Builder()
                        .setName("data1")
                        .setCapacity(10 * GB)
                        .build())
                    .build();

        DiskTypes.CreateSpec diskCreateSpec3 =
                new DiskTypes.CreateSpec.Builder()
                    .setNewVmdk(new DiskTypes.VmdkCreateSpec.Builder()
                        .setName("data2")
                        .setCapacity(10 * GB)
                        .build())
                    .build();

        // Ethernet CreateSpec
        EthernetTypes.CreateSpec manualEthernetSpec =
                new EthernetTypes.CreateSpec.Builder().setStartConnected(true)
                    .setMacType(EthernetTypes.MacAddressType.MANUAL)
                    .setMacAddress("11:23:58:13:21:34")
                    .setBacking(new EthernetTypes.BackingSpec.Builder(
                        EthernetTypes.BackingType.STANDARD_PORTGROUP)
                            .setNetwork(standardNetworkBacking).build())
                    .build();


        EthernetTypes.CreateSpec generatedEthernetSpec =
                new EthernetTypes.CreateSpec.Builder().setStartConnected(true)
                    .setMacType(EthernetTypes.MacAddressType.GENERATED)
                    .setBacking(new EthernetTypes.BackingSpec.Builder(
                        EthernetTypes.BackingType.DISTRIBUTED_PORTGROUP)
                            .setNetwork(distributedNetworkBacking).build())
                    .build();

        // Cdrom CreateSpec
        CdromTypes.CreateSpec cdromCreateSpec =
                new CdromTypes.CreateSpec.Builder()
                    .setBacking(new CdromTypes.BackingSpec.Builder(
                        CdromTypes.BackingType.ISO_FILE)
                            .setIsoFile(this.isoDatastorePath).build())
                    .build();

        // Serial Port CreateSpec
        SerialTypes.CreateSpec serialCreateSpec =
                new SerialTypes.CreateSpec.Builder().setStartConnected(false)
                    .setBacking(new SerialTypes.BackingSpec.Builder(
                        SerialTypes.BackingType.NETWORK_SERVER)
                            .setNetworkLocation(URI
                                .create(SERIAL_PORT_NETWORK_SERVICE_LOCATION))
                            .build())
                    .build();

        // Parallel port CreateSpec
        ParallelTypes.CreateSpec parallelCreateSpec =
                new ParallelTypes.CreateSpec.Builder().setStartConnected(false)
                    .setBacking(new ParallelTypes.BackingSpec.Builder(
                        ParallelTypes.BackingType.HOST_DEVICE).build())
                    .build();

        // Floppy CreateSpec
        FloppyTypes.CreateSpec floppyCreateSpec =
                new FloppyTypes.CreateSpec.Builder()
                    .setBacking(new FloppyTypes.BackingSpec.Builder(
                        FloppyTypes.BackingType.CLIENT_DEVICE).build())
                    .build();

        // Specify the boot order
        List<DeviceTypes.EntryCreateSpec> bootDevices = Arrays.asList(
            new DeviceTypes.EntryCreateSpec.Builder(DeviceTypes.Type.CDROM)
                .build(),
            new DeviceTypes.EntryCreateSpec.Builder(DeviceTypes.Type.DISK)
                .build(),
            new DeviceTypes.EntryCreateSpec.Builder(DeviceTypes.Type.ETHERNET)
                .build());
        String vmName = (null == this.vmName || this.vmName.isEmpty())?
        				EXHAUSTIVE_VM_NAME :this.vmName;
        // Create a VM with above configuration
        VMTypes.CreateSpec vmCreateSpec =
                new VMTypes.CreateSpec.Builder(vmGuestOS)
                    .setBootDevices(bootDevices)
                    .setCdroms(Collections.singletonList(cdromCreateSpec))
                    .setCpu(cpuUpdateSpec)
                    .setDisks(Arrays.asList(diskCreateSpec1,
                        diskCreateSpec2,
                        diskCreateSpec3))
                    .setFloppies(Collections.singletonList(floppyCreateSpec))
                    .setHardwareVersion(HARDWARE_VERSION)
                    .setMemory(memoryUpdateSpec)
                    .setName(vmName)
                    .setNics(Arrays.asList(manualEthernetSpec,
                        generatedEthernetSpec))
                    .setParallelPorts(Collections.singletonList(
                            parallelCreateSpec))
                    .setPlacement(vmPlacementSpec)
                    .setSerialPorts(Collections.singletonList(serialCreateSpec))
                    .build();
        System.out.println("\n\n#### Example: Creating exhaustive VM with spec:"
                           + "\n" + vmCreateSpec);
        this.exhaustiveVMId = vmService.create(vmCreateSpec);
        System.out.println("\nCreated exhaustive VM : " + vmName
                           + " with id: " + this.exhaustiveVMId);
        VMTypes.Info vmInfo = vmService.get(this.exhaustiveVMId);
        System.out.println("\nExhaustive VM Info:\n" + vmInfo);
    }

    protected void cleanup() throws Exception {
        System.out.println("\n\n#### Deleting the Exhaustive VM");
        if(this.exhaustiveVMId != null) {
            this.vmService.delete(this.exhaustiveVMId);
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
        new CreateExhaustiveVM().execute(args);
    }
}
