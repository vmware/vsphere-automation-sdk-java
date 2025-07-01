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
package vmware.samples.vcenter.vm.hardware.cdrom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.Option;

import com.vmware.vcenter.vm.Power;
import com.vmware.vcenter.vm.PowerTypes;
import com.vmware.vcenter.vm.hardware.Cdrom;
import com.vmware.vcenter.vm.hardware.CdromTypes;
import com.vmware.vcenter.vm.hardware.CdromTypes.HostBusAdapterType;
import com.vmware.vcenter.vm.hardware.IdeAddressSpec;
import com.vmware.vcenter.vm.hardware.SataAddressSpec;
import com.vmware.vcenter.vm.hardware.adapter.Sata;
import com.vmware.vcenter.vm.hardware.adapter.SataTypes;

import vmware.samples.common.SamplesAbstractBase;
import vmware.samples.vcenter.helpers.VmHelper;

/**
 * Description: Demonstrates how to configure a CD-ROM device for a VM.
 *
 * Author: VMware, Inc.
 * Sample Prerequisites: The sample needs an existing VM
 *
 */
public class CdromConfiguration extends SamplesAbstractBase {
    private String vmName;
    private String vmId;
    private String sataId;
    private String isoDatastorePath;
    private Cdrom cdromService;
    private Power powerService;
    private Sata sataService;
    private List<String> createdCdroms = new ArrayList<String>();

    /**
     * Define the options specific to this sample and configure the sample using
     * command-line arguments or a config file
     *
     * @param args command line arguments passed to the sample
     */
    protected void parseArgs(String[] args) {
        Option vmNameOption = Option.builder()
            .longOpt("vmname")
            .desc("The name of the vm for which to configure virtual CD-ROM "
                  + "device.")
            .required(true)
            .hasArg()
            .argName("VM NAME")
            .build();
        Option isoDatastorePathOption = Option.builder()
            .longOpt("isodatastorepath")
            .desc("The path to the iso file on the datastore")
            .required(true)
            .hasArg()
            .argName("ISO DATASTORE PATH")
            .build();

        List<Option> optionList = Arrays.asList(vmNameOption,
            isoDatastorePathOption);
        super.parseArgs(optionList, args);
        this.vmName = (String) parsedOptions.get("vmname");
        this.isoDatastorePath = (String) parsedOptions.get("isodatastorepath");
    }

    protected void setup() throws Exception {
        this.cdromService = vapiAuthHelper.getStubFactory().createStub(
            Cdrom.class, this.sessionStubConfig);
        this.powerService = vapiAuthHelper.getStubFactory().createStub(
            Power.class, this.sessionStubConfig);
        this.sataService = vapiAuthHelper.getStubFactory().createStub(
            Sata.class, this.sessionStubConfig);

        System.out.println("\n\n#### Setup: Get the virtual machine id");
        this.vmId = VmHelper.getVM(vapiAuthHelper.getStubFactory(),
            sessionStubConfig,
            vmName);
        System.out.println("Using VM: " + vmName + " (vmId="
                + this.vmId + " ) for the CD-ROM configuration sample.");

        System.out.println("\n\n#### Setup: Create SATA controller");
        SataTypes.CreateSpec sataCreateSpec = new SataTypes.CreateSpec();
        this.sataId = sataService.create(this.vmId, sataCreateSpec);
        System.out.println(sataCreateSpec);
    }

    protected void run() throws Exception {
        System.out.println("\n\n### Example: List all CD-ROMs");
        listAllCdroms();

        System.out.println("\n\n### Example: Create CD-ROM with ISO_FILE"
                + " backing");
        createCdrom(CdromTypes.BackingType.ISO_FILE);

        System.out.println("\n\n### Example: Create CD-ROM with CLIENT_DEVICE"
                           + " backing");
        createCdrom(CdromTypes.BackingType.CLIENT_DEVICE);

        System.out.println("\n\n### Example: Create SATA CD-ROM with"
                           + " CLIENT_DEVICE backing");
        createCdromForAdapterType(CdromTypes.HostBusAdapterType.SATA,
            CdromTypes.BackingType.CLIENT_DEVICE);

        System.out.println("\n\n### Example: Create SATA CD-ROM on specific bus"
                           + " with CLIENT_DEVICE backing");
        createSataCdromAtSpecificLocation(
            CdromTypes.BackingType.CLIENT_DEVICE, 0l, null);

        System.out.println("\n\n### Example: Create SATA CD-ROM on specific bus"
                           + " and unit number with CLIENT_DEVICE backing");
        createSataCdromAtSpecificLocation(
            CdromTypes.BackingType.CLIENT_DEVICE, 0l, 10l);

        System.out.println("\n\n### Example: Create IDE CD-ROM with"
                           + " CLIENT_DEVICE backing");
        createCdromForAdapterType(CdromTypes.HostBusAdapterType.IDE,
            CdromTypes.BackingType.CLIENT_DEVICE);

        System.out.println("\n\n### Example: Create IDE CD-ROM as a slave"
                           + " device with HOST_DEVICE backing");
        createIdeCdromAsSpecificDevice(
            CdromTypes.BackingType.HOST_DEVICE, false);

        // Change the last CD-ROM that was created
        String lastCdromId = createdCdroms.get(createdCdroms.size() - 1);

        System.out.println("\n\n#### Example: Update backing from "
                           + "CLIENT_DEVICE to ISO_FILE for the last created"
                           + " CD-ROM");
        CdromTypes.UpdateSpec cdromUpdateSpec =
                new CdromTypes.UpdateSpec.Builder().setBacking(
                    new CdromTypes.BackingSpec.Builder(
                        CdromTypes.BackingType.ISO_FILE).setIsoFile(
                            isoDatastorePath).build()).build();
        this.cdromService.update(this.vmId, lastCdromId, cdromUpdateSpec);
        System.out.println(cdromUpdateSpec);
        CdromTypes.Info cdromInfo = cdromService.get(this.vmId, lastCdromId);
        System.out.println("VM ID=" + this.vmId);
        System.out.println("CD-ROM ID=" + lastCdromId);
        System.out.println(cdromInfo);

        System.out.println("\n\n#### Example: Update startConnected and"
                           + " allowGuestControl to false for the last created"
                           + " cdrom");
        cdromUpdateSpec = new CdromTypes.UpdateSpec.Builder()
            .setAllowGuestControl(false).setStartConnected(false).build();
        this.cdromService.update(this.vmId, lastCdromId, cdromUpdateSpec);
        cdromInfo = this.cdromService.get(this.vmId, lastCdromId);
        System.out.println("VM ID=" + this.vmId);
        System.out.println("CD-ROM ID=" + lastCdromId);
        System.out.println(cdromInfo);

        System.out.println("\n\n#### Power on VM to run connect/disconnect"
                           + " sample");
        this.powerService.start(this.vmId);

        cdromInfo = this.cdromService.get(this.vmId, lastCdromId);
        System.out.println("VM ID=" + this.vmId);
        System.out.println("CD-ROM ID=" + lastCdromId);
        System.out.println(cdromInfo);

        System.out.println("\n\n#### Example: Connect CD-ROM after powering"
                           + " on VM");
        this.cdromService.connect(this.vmId, lastCdromId);
        cdromInfo = this.cdromService.get(this.vmId, lastCdromId);
        System.out.println("VM ID=" + this.vmId);
        System.out.println("CD-ROM ID=" + lastCdromId);
        System.out.println(cdromInfo);

        System.out.println("\n\n#### Example: Disconnect cdrom while VM is"
                           + " powered on");
        this.cdromService.disconnect(this.vmId, lastCdromId);
        cdromInfo = this.cdromService.get(this.vmId, lastCdromId);
        System.out.println("VM ID=" + this.vmId);
        System.out.println("CD-ROM ID=" + lastCdromId);
        System.out.println(cdromInfo);

        System.out.println("\n\n#### Power off the VM after the"
                           + " connect/disconnect");
        this.powerService.stop(this.vmId);
        cdromInfo = this.cdromService.get(this.vmId, lastCdromId);
        System.out.println("VM ID=" + this.vmId);
        System.out.println("CD-ROM ID=" + lastCdromId);
        System.out.println(cdromInfo);

        System.out.println("\n\n#### List all the CD-ROMs");
        listAllCdroms();
    }

    /**
     * Displays CD-ROM on info for each CD-ROM on the vm
     */
    private void listAllCdroms() {
        List<CdromTypes.Summary> cdromSummaries = this.cdromService.list(
            this.vmId);
        System.out.println(cdromSummaries);

        for (CdromTypes.Summary cdromSummary : cdromSummaries) {
            String cdromId = cdromSummary.getCdrom();
            CdromTypes.Info cdromInfo = this.cdromService.get(this.vmId,
                cdromId);
            System.out.println(cdromInfo);
        }
    }

    /**
     * Creates a CD-ROM device with the specified backing type
     *
     * @param backingType backing type for the CD-ROM device
     */
    private void createCdrom(CdromTypes.BackingType backingType) {
        CdromTypes.CreateSpec cdromCreateSpec =
                new CdromTypes.CreateSpec.Builder().setBacking(
                    new CdromTypes.BackingSpec.Builder(backingType).build())
                    .build();
        if (backingType.equals(CdromTypes.BackingType.ISO_FILE)) {
            cdromCreateSpec.getBacking().setIsoFile(this.isoDatastorePath);
        }

        String cdromId = this.cdromService.create(this.vmId, cdromCreateSpec);
        System.out.println(cdromCreateSpec);
        this.createdCdroms.add(cdromId);
        CdromTypes.Info cdromInfo = this.cdromService.get(this.vmId, cdromId);
        System.out.println("VM ID=" + this.vmId);
        System.out.println("CD-ROM ID=" + cdromId);
        System.out.println(cdromInfo);
    }

    /**
     * Creates a CD-ROM device for the specified host bus adapter type and
     * backing type.
     *
     * @param hostBusAdapterType host bus adapter type for the CD-ROM
     * @param backingType backing type for the CD-ROM
     */
    private void createCdromForAdapterType(
        CdromTypes.HostBusAdapterType hostBusAdapterType,
        CdromTypes.BackingType backingType) {

        CdromTypes.CreateSpec cdromCreateSpec =
                new CdromTypes.CreateSpec.Builder().setBacking(
                    new CdromTypes.BackingSpec.Builder(backingType).build())
                    .setType(hostBusAdapterType)
                    .build();
        String cdromId = this.cdromService.create(this.vmId, cdromCreateSpec);
        System.out.println(cdromCreateSpec);
        this.createdCdroms.add(cdromId);
        CdromTypes.Info cdromInfo = this.cdromService.get(this.vmId, cdromId);
        System.out.println("VM ID=" + this.vmId);
        System.out.println("CD-ROM ID=" + cdromId);
        System.out.println(cdromInfo);
    }

    /**
     * Creates an IDE CD-ROM as either a master or a slave device with the
     * specified backing type.
     *
     * @param backingType backing type for CD-ROM
     * @param isMaster true, if the CD-ROM should be created as a master
     *        device, false otherwise
     */
    private void createIdeCdromAsSpecificDevice(
        CdromTypes.BackingType backingType, boolean isMaster) {

        CdromTypes.CreateSpec cdromCreateSpec;
        String cdromId = null;

        cdromCreateSpec = new CdromTypes.CreateSpec.Builder().setBacking(
            new CdromTypes.BackingSpec.Builder(backingType).build())
            .setType(HostBusAdapterType.IDE)
            .setIde(new IdeAddressSpec.Builder().setMaster(isMaster)
                .build())
            .build();
        cdromId = this.cdromService.create(this.vmId, cdromCreateSpec);
        System.out.println(cdromCreateSpec);
        CdromTypes.Info cdromInfo = this.cdromService.get(this.vmId, cdromId);
        System.out.println("VM ID=" + this.vmId);
        System.out.println("CD-ROM ID=" + cdromId);
        System.out.println(cdromInfo);
        this.createdCdroms.add(cdromId);
    }

    /**
     * Creates a SATA CD-ROM on a specific bus/unit number with the specified
     * backing type.
     *
     * @param backingType backing type for the CD-ROM
     * @param bus bus number
     * @param unit unit number
     */
    private void createSataCdromAtSpecificLocation(
        CdromTypes.BackingType backingType, Long bus, Long unit) {

        CdromTypes.CreateSpec cdromCreateSpec;
        String cdromId = null;
        if (unit==null) {
            cdromCreateSpec = new CdromTypes.CreateSpec.Builder().setBacking(
                new CdromTypes.BackingSpec.Builder(backingType).build())
                .setType(HostBusAdapterType.SATA)
                .setSata(new SataAddressSpec.Builder(0l).build())
                .build();
            cdromId = this.cdromService.create(this.vmId, cdromCreateSpec);
            System.out.println(cdromCreateSpec);
            CdromTypes.Info cdromInfo = this.cdromService.get(this.vmId,
                cdromId);
            System.out.println("VM ID=" + this.vmId);
            System.out.println("CD-ROM ID=" + cdromId);
            System.out.println(cdromInfo);
        } else {
            cdromCreateSpec = new CdromTypes.CreateSpec.Builder().setBacking(
                new CdromTypes.BackingSpec.Builder(backingType).build())
                .setType(HostBusAdapterType.SATA)
                .setSata(new SataAddressSpec.Builder(0l).setUnit(10l).build())
                .build();
            cdromId = this.cdromService.create(this.vmId, cdromCreateSpec);
            System.out.println(cdromCreateSpec);
            CdromTypes.Info cdromInfo = this.cdromService.get(this.vmId,
                cdromId);
            System.out.println("VM ID=" + this.vmId);
            System.out.println("CD-ROM ID=" + cdromId);
            System.out.println(cdromInfo);
        }
        this.createdCdroms.add(cdromId);
    }
    protected void cleanup() throws Exception {
        PowerTypes.Info powerInfo = this.powerService.get(this.vmId);
        if (powerInfo.getState().equals(PowerTypes.State.POWERED_ON)) {
            System.out.println("\n\n#### Cleanup: Powering off the VM");
            this.powerService.stop(this.vmId);
        }

        if (this.sataId != null) {
            System.out.println("\n\n#### Cleanup: Deleting the SATA "
                               + "controller");
            this.sataService.delete(this.vmId, this.sataId);
        }

        System.out.println("\n\n#### Cleanup: Deleting all the created"
                           + " CD-ROMs");
        for (String cdromId : createdCdroms) {
            this.cdromService.delete(this.vmId, cdromId);
        }
        listAllCdroms();
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
        new CdromConfiguration().execute(args);
    }
}