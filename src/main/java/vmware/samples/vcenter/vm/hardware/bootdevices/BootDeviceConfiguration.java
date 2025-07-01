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
package vmware.samples.vcenter.vm.hardware.bootdevices;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.cli.Option;

import com.vmware.vcenter.VM;
import com.vmware.vcenter.VMTypes;
import com.vmware.vcenter.vm.hardware.Disk;
import com.vmware.vcenter.vm.hardware.DiskTypes;
import com.vmware.vcenter.vm.hardware.Ethernet;
import com.vmware.vcenter.vm.hardware.EthernetTypes;
import com.vmware.vcenter.vm.hardware.boot.Device;
import com.vmware.vcenter.vm.hardware.boot.DeviceTypes;

import vmware.samples.common.SamplesAbstractBase;
import vmware.samples.vcenter.helpers.VmHelper;

/**
 * Description: Demonstrates how to modify the boot devices used by a virtual 
 * machine, and in what order they are tried.
 *
 * Author: VMware, Inc.
 * Sample Prerequisites:
 * The sample needs an existing VM with the following minimum number of devices:
 * - 1 Ethernet adapter
 * - 1 CD-ROM
 * - 1 Floppy drive
 * - 3 Disks
 */
public class BootDeviceConfiguration extends SamplesAbstractBase {
    private String vmName;
    private String vmId;
    private List<DeviceTypes.Entry> originalBootDeviceEntries;
    private Device bootDeviceService;
    private Disk diskService;
    private Ethernet ethernetService;

    /**
     * Define the options specific to this sample and configure the sample using
     * command-line arguments or a config file
     *
     * @param args command line arguments passed to the sample
     */
    protected void parseArgs(String[] args) {
        Option vmNameOption = Option.builder()
                .longOpt("vmname")
                .desc("The name of the vm for which to configure boot device "
                        + "order.")
                .required(true)
                .hasArg()
                .argName("VM NAME")
                .build();
        List<Option> optionList = Collections.singletonList(vmNameOption);
        super.parseArgs(optionList, args);
        this.vmName = (String) parsedOptions.get("vmname");
    }

    protected void setup() throws Exception {
        this.diskService =
                vapiAuthHelper.getStubFactory().createStub(Disk.class,
                    this.sessionStubConfig);
        this.ethernetService =
                vapiAuthHelper.getStubFactory().createStub(Ethernet.class,
                    this.sessionStubConfig);
        this.bootDeviceService =
                vapiAuthHelper.getStubFactory().createStub(Device.class,
                    this.sessionStubConfig);

        System.out.println("\n\n#### Setup: Get the virtual machine id");
        this.vmId = VmHelper.getVM(vapiAuthHelper.getStubFactory(),
            sessionStubConfig,
            vmName);
        System.out.println("\nUsing VM: " + vmName + " (vmId="
            + this.vmId + " ) for boot device configuration sample.");


        System.out.println("\nValidate whether the vm has required "
                + "minimum number of devices");
        VM vmService =
                vapiAuthHelper.getStubFactory().createStub(VM.class,
                    this.sessionStubConfig);
        VMTypes.Info vmInfo = vmService.get(this.vmId);
        if(vmInfo.getCdroms().size() < 1 || vmInfo.getFloppies().size() < 1
                || vmInfo.getDisks().size() < 3
                || vmInfo.getNics().size() < 1) {
            throw new Exception("\nSelected VM does not have the required "
                    + "minimum number of devices: i.e. 1 Ethernet adapter, "
                    + "1 CD-ROM, 1 Floppy drive, 3 disks");
        }
    }

    protected void run() throws Exception {
        System.out.println("\n\n#### Example: Print the current boot device"
                           + " configuration");
        List<DeviceTypes.Entry> bootDeviceEntries =
                this.bootDeviceService.get(this.vmId);
        System.out.println(bootDeviceEntries);

        // Save the current boot info to verify that we have cleaned up properly
        this.originalBootDeviceEntries = bootDeviceEntries;

        System.out.println("\n\n#### Example: Set boot order to be Floppy,"
                           + " Disk1, Disk2, Disk3, Ethernet NIC, CD-ROM");

        // Get device identifiers for disks
        List<DiskTypes.Summary> diskSummaries =
                this.diskService.list(this.vmId);
        System.out.println("\nList of disks attached to vm: \n"
                           + diskSummaries);
        List<String> diskIds = new ArrayList<String>();
        for(DiskTypes.Summary diskSummary : diskSummaries) {
            diskIds.add(diskSummary.getDisk());
        }

        // Get device identifiers for ethernet nics
        List<EthernetTypes.Summary> ethernetSummaries =
                this.ethernetService.list(this.vmId);
        System.out.println("\nList of ethernet nics attached to vm: \n"
                           + ethernetSummaries);
        List<String> ethernetIds = new ArrayList<String>();
        for(EthernetTypes.Summary ethernetSummary : ethernetSummaries) {
            ethernetIds.add(ethernetSummary.getNic());
        }
        List<DeviceTypes.Entry> devices = Arrays.asList(
            new DeviceTypes.Entry.Builder(DeviceTypes.Type.FLOPPY).build(),
            new DeviceTypes.Entry.Builder(DeviceTypes.Type.DISK)
                .setDisks(diskIds)
                .build(),
            new DeviceTypes.Entry.Builder(DeviceTypes.Type.ETHERNET)
                .setNic(ethernetIds.get(0))
                .build(),
            new DeviceTypes.Entry.Builder(DeviceTypes.Type.CDROM).build());
        bootDeviceService.set(this.vmId, devices);
        bootDeviceEntries = this.bootDeviceService.get(this.vmId);
        System.out.println("\nNew boot device configuration");
        System.out.println(bootDeviceEntries);
    }

    protected void cleanup() throws Exception {
        System.out.println("\n\n#### Cleanup: Revert boot device"
                           + " configuration");
        this.bootDeviceService.set(this.vmId, this.originalBootDeviceEntries);
        List<DeviceTypes.Entry> bootDeviceEntries = this.bootDeviceService.get(
            this.vmId);
        System.out.println(bootDeviceEntries);
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
        new BootDeviceConfiguration().execute(args);
    }
}
