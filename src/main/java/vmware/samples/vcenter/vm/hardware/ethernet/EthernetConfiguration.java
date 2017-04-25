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
package vmware.samples.vcenter.vm.hardware.ethernet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.Option;

import com.vmware.vcenter.vm.Power;
import com.vmware.vcenter.vm.PowerTypes;
import com.vmware.vcenter.vm.hardware.Ethernet;
import com.vmware.vcenter.vm.hardware.EthernetTypes;
import com.vmware.vcenter.vm.hardware.EthernetTypes.MacAddressType;

import vmware.samples.common.SamplesAbstractBase;
import vmware.samples.vcenter.helpers.NetworkHelper;
import vmware.samples.vcenter.helpers.VmHelper;

/**
 * Description: Demonstrates how to configure virtual ethernet adapters of a 
 * virtual machine.
 *
 * Author: VMware, Inc.
 * Sample Prerequisites: The sample needs an existing VM.
 *
 */
public class EthernetConfiguration extends SamplesAbstractBase {
    private String vmName;
    private String datacenterName;
    private String stdPortgroupName;
    private String distPortgroupName;
    private String vmId;
    private List<String> createdNics = new ArrayList<String>();
    private Power powerService;
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
            .desc("The name of the vm for which to configure the virtual "
                  + "ethernet adapters.")
            .required(true)
            .hasArg()
            .argName("VM NAME")
            .build();
        Option datacenterOption = Option.builder()
            .longOpt("datacenter")
            .desc("The name of the datacenter containing the vCenter networks")
            .required(true)
            .hasArg()
            .argName("DATACENTER")
            .build();
        Option stdPortgroupOption = Option.builder()
            .longOpt("standardportgroup")
            .desc("The name of the standard portgroup")
            .required(true)
            .hasArg()
            .argName("STANDARD PORTGROUP")
            .build();
        Option distPortgroupOption = Option.builder()
            .longOpt("distributedportgroup")
            .desc("The name of the distributed portgroup")
            .required(true)
            .hasArg()
            .argName("DISTRIBUTED PORTGROUP")
            .build();

        List<Option> optionList = Arrays.asList(vmNameOption,
            datacenterOption,
            stdPortgroupOption,
            distPortgroupOption);
        super.parseArgs(optionList, args);
        this.vmName = (String) parsedOptions.get("vmname");
        this.datacenterName = (String) parsedOptions.get("datacenter");
        this.stdPortgroupName = (String) parsedOptions.get("standardportgroup");
        this.distPortgroupName = (String) parsedOptions.get(
            "distributedportgroup");
    }

    protected void setup() throws Exception {
        this.powerService = vapiAuthHelper.getStubFactory().createStub(
            Power.class, this.sessionStubConfig);
        this.ethernetService = vapiAuthHelper.getStubFactory().createStub(
            Ethernet.class, this.sessionStubConfig);

        System.out.println("\n\n#### Setup: Get the virtual machine id");
        this.vmId = VmHelper.getVM(vapiAuthHelper.getStubFactory(),
            sessionStubConfig,
            vmName);
        System.out.println("Using VM: " + vmName + " (vmId="
                + this.vmId + " ) for ethernet adapter configuration sample.");
    }

    protected void run() throws Exception {
        // List all ethernet adapters of the virtual machine
        List<EthernetTypes.Summary> nicSummaries = this.ethernetService.list(
            this.vmId);
        System.out.println("\n\n#### List of all Ethernet NICS on the VM:\n"
                           + nicSummaries);

        // Get info for each ethernet on the VM
        System.out.println("\n\n####Print info for each Ethernet NIC on the"
                                   + " vm.");
        for (EthernetTypes.Summary ethSummary : nicSummaries) {
            EthernetTypes.Info ethInfo = this.ethernetService.get(vmId,
                ethSummary.getNic());
            System.out.println(ethInfo);
        }

        System.out.println("\n\n#### Example: Create Ethernet NIC using "
                + "STANDARD_PORTGROUP with default settings.");
        String stdNetworkId = NetworkHelper.getStandardNetworkBacking(
            this.vapiAuthHelper.getStubFactory(), sessionStubConfig,
            this.datacenterName, this.stdPortgroupName);
        EthernetTypes.CreateSpec nicCreateSpec =
                new EthernetTypes.CreateSpec.Builder().setBacking(
                    new EthernetTypes.BackingSpec.Builder(
                        EthernetTypes.BackingType.STANDARD_PORTGROUP)
                            .setNetwork(stdNetworkId).build()).build();
        String nicId = this.ethernetService.create(this.vmId, nicCreateSpec);
        this.createdNics.add(nicId);
        System.out.println(nicCreateSpec);
        EthernetTypes.Info nicInfo = this.ethernetService.get(this.vmId, nicId);
        System.out.println("VM ID=" + this.vmId);
        System.out.println("Ethernet NIC ID=" + nicId);
        System.out.println(nicInfo);

        System.out.println("\n\n#### Example: Create Ethernet NIC using "
                + "DISTRIBUTED_PORTGROUP with default settings.");
        String distNetworkId = NetworkHelper.getDistributedNetworkBacking(
            this.vapiAuthHelper.getStubFactory(), sessionStubConfig,
            this.datacenterName, this.distPortgroupName);
        nicCreateSpec = new EthernetTypes.CreateSpec.Builder().setBacking(
            new EthernetTypes.BackingSpec.Builder(
                EthernetTypes.BackingType.DISTRIBUTED_PORTGROUP).setNetwork(
                    distNetworkId).build()).build();
        nicId = this.ethernetService.create(this.vmId, nicCreateSpec);
        this.createdNics.add(nicId);
        System.out.println(nicCreateSpec);
        nicInfo = this.ethernetService.get(this.vmId, nicId);
        System.out.println("VM ID=" + this.vmId);
        System.out.println("Ethernet NIC ID=" + nicId);
        System.out.println(nicInfo);

        System.out.println("\n\n#### Example: Create Ethernet NIC using"
                + " standard portgroup and specifying start_connected=true,"
                + " allow_guest_control=true, mac_type, mac_address,"
                + " wake_on_lan=enabled.");
        nicCreateSpec =
                new EthernetTypes.CreateSpec.Builder().setBacking(
                    new EthernetTypes.BackingSpec.Builder(
                        EthernetTypes.BackingType.STANDARD_PORTGROUP)
                            .setNetwork(stdNetworkId).build())
                .setStartConnected(true)
                .setAllowGuestControl(true)
                .setMacType(MacAddressType.MANUAL)
                .setMacAddress("01:23:45:67:89:10").build();
        nicId = this.ethernetService.create(this.vmId, nicCreateSpec);
        this.createdNics.add(nicId);
        System.out.println(nicCreateSpec);
        nicInfo = this.ethernetService.get(this.vmId, nicId);
        System.out.println("VM ID=" + this.vmId);
        System.out.println("Ethernet NIC ID=" + nicId);
        System.out.println(nicInfo);

        System.out.println("\n\n#### Example: Create Ethernet NIC using"
                + " distributed portgroup and specifying start_connected=true,"
                + " allow_guest_control=true, mac_type, mac_address,"
                + " wake_on_lan=enabled.");
        nicCreateSpec =
                new EthernetTypes.CreateSpec.Builder().setBacking(
                    new EthernetTypes.BackingSpec.Builder(
                        EthernetTypes.BackingType.STANDARD_PORTGROUP)
                            .setNetwork(stdNetworkId).build())
                .setStartConnected(true)
                .setAllowGuestControl(true)
                .setMacType(MacAddressType.MANUAL)
                .setMacAddress("24:68:10:12:14:16").build();
        nicId = this.ethernetService.create(this.vmId, nicCreateSpec);
        this.createdNics.add(nicId);
        System.out.println(nicCreateSpec);
        nicInfo = this.ethernetService.get(this.vmId, nicId);
        System.out.println("VM ID=" + this.vmId);
        System.out.println("Ethernet NIC ID=" + nicId);
        System.out.println(nicInfo);

        String lastNicId = nicId;

        System.out.println(
            "\n\n#### Example: Update Ethernet NIC with different"
            + " backing.");
        EthernetTypes.UpdateSpec nicUpdateSpec =
                new EthernetTypes.UpdateSpec.Builder().setBacking(
                    new EthernetTypes.BackingSpec.Builder(
                        EthernetTypes.BackingType.STANDARD_PORTGROUP)
                            .setNetwork(stdNetworkId).build()).build();
        this.ethernetService.update(this.vmId, lastNicId, nicUpdateSpec);
        System.out.println(nicUpdateSpec);
        nicInfo = this.ethernetService.get(this.vmId, lastNicId);
        System.out.println("VM ID=" + this.vmId);
        System.out.println("Ethernet NIC ID=" + lastNicId);
        System.out.println(nicInfo);

        System.out.println("\n\n#### Example: Update the Ethernet NIC,"
                + " wake_on_lan = false, mac_type=GENERATED,"
                + " startConnected = false, allowGuestControl = false.");
        nicUpdateSpec = new EthernetTypes.UpdateSpec.Builder()
            .setAllowGuestControl(false)
            .setStartConnected(false)
            .setWakeOnLanEnabled(false)
            .build();
        this.ethernetService.update(this.vmId, lastNicId, nicUpdateSpec);
        System.out.println(nicUpdateSpec);
        nicInfo = this.ethernetService.get(this.vmId, lastNicId);
        System.out.println("VM ID=" + this.vmId);
        System.out.println("Ethernet NIC ID=" + lastNicId);
        System.out.println(nicInfo);


        System.out.println("\n\n#### Powering on VM to run connect/disconnect"
                + " example.");
        this.powerService.start(this.vmId);
        nicInfo = this.ethernetService.get(this.vmId, lastNicId);
        System.out.println("VM ID=" + this.vmId);
        System.out.println("Ethernet NIC ID=" + lastNicId);
        System.out.println(nicInfo);

        System.out.println("\n\n#### Example: Connect Ethernet NIC after"
                + " powering on VM.");
        this.ethernetService.connect(this.vmId, lastNicId);
        nicInfo = this.ethernetService.get(this.vmId, lastNicId);
        System.out.println("VM ID=" + this.vmId);
        System.out.println("Ethernet NIC ID=" + lastNicId);
        System.out.println(nicInfo);

        System.out.println("\n\n#### Example: Disconnect Ethernet NIC after"
                + " powering on VM.");
        this.ethernetService.disconnect(this.vmId, lastNicId);
        nicInfo = this.ethernetService.get(this.vmId, lastNicId);
        System.out.println("VM ID=" + this.vmId);
        System.out.println("Ethernet NIC ID=" + lastNicId);
        System.out.println(nicInfo);

        // Power off the VM
        System.out.println("\n\n#### Powering off the VM after"
                + " connect/disconnect example.");
        this.powerService.stop(this.vmId);
        nicInfo = this.ethernetService.get(this.vmId, lastNicId);
        System.out.println("VM ID=" + this.vmId);
        System.out.println("Ethernet NIC ID=" + lastNicId);
        System.out.println(nicInfo);
    }

    protected void cleanup() throws Exception {
        if (this.powerService.get(this.vmId).getState().equals(
            PowerTypes.State.POWERED_ON)) {
            System.out.println("Power off the vm");
            this.powerService.stop(this.vmId);
        }

        // List all ethernet adapters of the virtual machine
        List<EthernetTypes.Summary> nicSummaries = this.ethernetService.list(
            this.vmId);
        System.out.println("\n\n#### List of all Ethernet NICS on the VM:\n"
                           + nicSummaries);

        System.out.println("\n\n#### Cleanup: Delete all the created Ethernet"
                + " NICs.");
        for (String nicId : createdNics) {
            this.ethernetService.delete(this.vmId, nicId);
        }
        nicSummaries = this.ethernetService.list(this.vmId);
        System.out.println("\n\n#### List of all Ethernet NICS on the VM:\n"
                           + nicSummaries);
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
        new EthernetConfiguration().execute(args);
    }
}