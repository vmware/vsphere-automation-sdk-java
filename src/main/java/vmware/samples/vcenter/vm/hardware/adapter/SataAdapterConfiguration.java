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
package vmware.samples.vcenter.vm.hardware.adapter;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.Option;

import com.vmware.vcenter.vm.hardware.adapter.Sata;
import com.vmware.vcenter.vm.hardware.adapter.SataTypes;

import vmware.samples.common.SamplesAbstractBase;
import vmware.samples.vcenter.helpers.VmHelper;

/**
 * Description: Demonstrates how to configure virtual SATA adapters of a
 * virtual machine.
 *
 * Author: VMware, Inc.
 * Sample Prerequisites: The sample needs an existing VM.
 *
 */
public class SataAdapterConfiguration extends SamplesAbstractBase {
    private String vmName;
    private String vmId;
    private List<String> createdSataAdapters = new ArrayList<String>();
    private Sata sataService;

    /**
     * Define the options specific to this sample and configure the sample using
     * command-line arguments or a config file
     *
     * @param args command line arguments passed to the sample
     */
    protected void parseArgs(String[] args) {
        Option vmNameOption = Option.builder()
                .required(true)
                .hasArg()
                .argName("VM NAME")
                .longOpt("vmname")
                .desc("The name of the vm for which to configure virtual SATA"
                        + " adapters.")
                .build();
        List<Option> optionList = new ArrayList<Option>();
        optionList.add(vmNameOption);
        super.parseArgs(optionList, args);
        this.vmName = (String) parsedOptions.get("vmname");
    }

    protected void setup() throws Exception {
        this.sataService =
                vapiAuthHelper.getStubFactory().createStub(Sata.class,
                    this.sessionStubConfig);

        System.out.println("\n\n#### Setup: Get the virtual machine id");
        this.vmId = VmHelper.getVM(vapiAuthHelper.getStubFactory(),
            sessionStubConfig,
            vmName);
        System.out.println("Using VM: " + vmName + " (vmId="
            + this.vmId + " ) for SATA adapter configuration sample.");
    }

    protected void run() throws Exception {
        System.out.println("\n\n#### Example: List of all SATA adapters"
                + " on the VM.");
        List<SataTypes.Summary> sataSummaries = this.sataService.list(
            this.vmId);
        System.out.println(sataSummaries);

        System.out.println("\n\n#### Display information about each"
                + " adapter.");
        for (SataTypes.Summary sataSummary : sataSummaries) {
            SataTypes.Info sataInfo = this.sataService.get(this.vmId,
                sataSummary.getAdapter());
            System.out.println(sataInfo);
        }

        System.out.println("\n\n#### Example: Create SATA adapter with"
                + " defaults.");
        SataTypes.CreateSpec sataCreateSpec = new SataTypes.CreateSpec();
        String sataId = this.sataService.create(this.vmId, sataCreateSpec);
        System.out.println(sataCreateSpec);
        SataTypes.Info sataInfo = this.sataService.get(this.vmId, sataId);
        System.out.println("VM ID=" + this.vmId);
        System.out.println("SATA Adapter ID=" + sataId);
        System.out.println(sataInfo);
        this.createdSataAdapters.add(sataId);

        System.out.println("\n\n#### Create SATA adapter with a specific bus");
        sataCreateSpec = new SataTypes.CreateSpec.Builder().setBus(2l).build();
        System.out.println(sataCreateSpec);
        sataId = this.sataService.create(this.vmId, sataCreateSpec);
        sataInfo = this.sataService.get(this.vmId, sataId);
        System.out.println("VM ID=" + this.vmId);
        System.out.println("SATA Adapter ID=" + sataId);
        System.out.println(sataInfo);
        this.createdSataAdapters.add(sataId);

        // List all SATA adapters for a VM
        System.out.println("\n\n#### List of all SATA adapters on the VM.");
        sataSummaries = this.sataService.list(
            this.vmId);
        System.out.println(sataSummaries);
    }

    protected void cleanup() throws Exception {
        System.out.println("\n\n#### Cleanup: Deleting all the adapters"
                + " that were created.");
        for(String sataId : createdSataAdapters) {
            this.sataService.delete(this.vmId, sataId);
        }
        List<SataTypes.Summary>sataSummaries = this.sataService.list(this.vmId);
        System.out.println("\n\n#### List of all SATA adapters on the VM.");
        System.out.println(sataSummaries);
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
        new SataAdapterConfiguration().execute(args);
    }
}
