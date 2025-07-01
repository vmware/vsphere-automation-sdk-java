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

import vmware.samples.common.SamplesAbstractBase;

/**
 * Description: Demonstrates how to unregister a VM by id.
 *
 * Author: VMware, Inc.
 * Sample Prerequisites:
 * The sample needs a VM id.
 */
public class UnregisterVM extends SamplesAbstractBase {
    private VM vmService;
    private String vmId;

    /**
     * Define the options specific to this sample and configure the sample using
     * command-line arguments or a config file
     *
     * @param args command line arguments passed to the sample
     */
    protected void parseArgs(String[] args) {
        Option vmIdOption = Option.builder()
            .longOpt("vmId")
            .desc("The VM ID to unregister.")
            .argName("VMID")
            .required(true)
            .hasArg()
            .build();
        List<Option> optionList = Arrays.asList(vmIdOption);

        super.parseArgs(optionList, args);
        this.vmId = (String) parsedOptions.get("vmId");
    }

    protected void setup() throws Exception {
        this.vmService = vapiAuthHelper.getStubFactory().createStub(VM.class,
            sessionStubConfig);
    }

    protected void run() throws Exception {
        System.out.println("\n\n#### Example: Unregister VM: " + vmId);
        vmService.unregister(vmId);
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
        new UnregisterVM().execute(args);
    }
}
