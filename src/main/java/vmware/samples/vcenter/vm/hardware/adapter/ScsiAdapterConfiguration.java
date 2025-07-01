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

import com.vmware.vcenter.vm.hardware.adapter.Scsi;
import com.vmware.vcenter.vm.hardware.adapter.ScsiTypes;

import vmware.samples.common.SamplesAbstractBase;
import vmware.samples.vcenter.helpers.VmHelper;

/**
 * Description: Demonstrates how to configure virtual SCSI adapters of a 
 * virtual machine.
 *
 * Author: VMware, Inc.
 * Sample Prerequisites: The sample needs an existing VM.
 *
 */
public class ScsiAdapterConfiguration extends SamplesAbstractBase {
    private String vmName;
    private String vmId;
    private List<String> createdScsiAdapters = new ArrayList<String>();
    private Scsi scsiService;

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
                .desc("The name of the vm for which to configure virtual SCSI"
                        + " adapter")
                .build();
        List<Option> optionList = new ArrayList<Option>();
        optionList.add(vmNameOption);
        super.parseArgs(optionList, args);
        this.vmName = (String) parsedOptions.get("vmname");
    }

    protected void setup() throws Exception {
        this.scsiService =
                vapiAuthHelper.getStubFactory().createStub(Scsi.class,
                    this.sessionStubConfig);

        System.out.println("\n\n#### Setup: Get the virtual machine id");
        this.vmId = VmHelper.getVM(vapiAuthHelper.getStubFactory(),
            sessionStubConfig,
            vmName);
        System.out.println("Using VM: " + vmName + " (vmId="
            + this.vmId + " ) for SCSI adapter configuration sample.");
    }

    protected void run() throws Exception {
        System.out.println("\n\n#### Example: List of all SCSI adapters"
                + " on the VM.");
        List<ScsiTypes.Summary> scsiSummaries = this.scsiService.list(
            this.vmId);
        System.out.println(scsiSummaries);

        System.out.println("\n\n#### Display information about each adapter.");
        for (ScsiTypes.Summary scsiSummary : scsiSummaries) {
            ScsiTypes.Info scsiInfo = this.scsiService.get(this.vmId,
                scsiSummary.getAdapter());
            System.out.println(scsiInfo);
        }

        System.out.println("\n\n#### Example: Create SATA adapter with"
                           + " defaults.");
        ScsiTypes.CreateSpec scsiCreateSpec = new ScsiTypes.CreateSpec();
        String scsiId = this.scsiService.create(this.vmId, scsiCreateSpec);
        System.out.println(scsiCreateSpec);
        ScsiTypes.Info scsiInfo = this.scsiService.get(this.vmId, scsiId);
        System.out.println("VM ID=" + this.vmId);
        System.out.println("SCSI Adapter ID=" + scsiId);
        System.out.println(scsiInfo);
        this.createdScsiAdapters.add(scsiId);

        System.out.println("\n\n#### Create SCSI adapter with specific bus and"
                           + " sharing=true");
        scsiCreateSpec = new ScsiTypes.CreateSpec.Builder()
                .setBus(2l)
                .setSharing(ScsiTypes.Sharing.VIRTUAL)
                .build();
        scsiId = this.scsiService.create(this.vmId, scsiCreateSpec);
        System.out.println(scsiCreateSpec);
        scsiInfo = this.scsiService.get(this.vmId, scsiId);
        System.out.println("VM ID=" + this.vmId);
        System.out.println("SCSI Adapter ID=" + scsiId);
        System.out.println(scsiInfo);
        this.createdScsiAdapters.add(scsiId);

        System.out.println("\n\n#### Update SCSI adapter by setting"
                + " sharing=false");
        ScsiTypes.UpdateSpec scsiUpdateSpec = new ScsiTypes.UpdateSpec.Builder()
            .setSharing(ScsiTypes.Sharing.NONE).build();
        this.scsiService.update(this.vmId, scsiId, scsiUpdateSpec);
        System.out.println(scsiUpdateSpec);
        scsiInfo = this.scsiService.get(this.vmId, scsiId);
        System.out.println("VM ID=" + this.vmId);
        System.out.println("SCSI Adapter ID=" + scsiId);
        System.out.println(scsiInfo);

        // List all SCSI adapters for a VM
        System.out.println("\n\n####List of all SCSI adapters on the VM");
        scsiSummaries = this.scsiService.list(this.vmId);
        System.out.println(scsiSummaries);
    }

    protected void cleanup() throws Exception {
        System.out.println("\n\n#### Cleanup: Deleting all the adapters that "
                + "were created");
        for(String scsiId : createdScsiAdapters) {
            this.scsiService.delete(this.vmId, scsiId);
        }
        System.out.println("\n\n#### List of all SCSI adapters on the VM");
        List<ScsiTypes.Summary>scsiSummaries = this.scsiService.list(this.vmId);
        System.out.println(scsiSummaries);
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
        new ScsiAdapterConfiguration().execute(args);
    }
}
