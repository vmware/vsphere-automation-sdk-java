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
package vmware.samples.vcenter.vm.hardware.memory;

import java.util.Collections;
import java.util.List;

import org.apache.commons.cli.Option;

import com.vmware.vcenter.vm.hardware.Memory;
import com.vmware.vcenter.vm.hardware.MemoryTypes;

import vmware.samples.common.SamplesAbstractBase;
import vmware.samples.vcenter.helpers.VmHelper;

/**
 * Description: Demonstrates how to configure the memory related settings of a 
 * virtual machine.
 *
 * Author: VMware, Inc.
 * Sample Prerequisites: This sample needs an existing VM.
 */
public class MemoryConfiguration extends SamplesAbstractBase {
    private String vmName;
    private String vmId;
    private MemoryTypes.Info originalMemoryInfo;
    private Memory memoryService;

    /**
     * Define the options specific to this sample and configure the sample using
     * command-line arguments or a config file
     *
     * @param args command line arguments passed to the sample
     */
    protected void parseArgs(String[] args) {
        Option vmNameOption = Option.builder()
            .longOpt("vmname")
            .desc("The name of the vm for which memory needs to be configured.")
            .required(true)
            .hasArg()
            .argName("VM NAME")
            .build();
        List<Option> optionList = Collections.singletonList(vmNameOption);
        super.parseArgs(optionList, args);
        this.vmName = (String) parsedOptions.get("vmname");
    }

    protected void setup() throws Exception {
        this.memoryService =
                vapiAuthHelper.getStubFactory().createStub(Memory.class,
                    this.sessionStubConfig);

        System.out.println("\n\n#### Setup: Get the virtual machine id");
        this.vmId = VmHelper.getVM(vapiAuthHelper.getStubFactory(),
            sessionStubConfig,
            vmName);
        System.out.println("Using VM: " + vmName + " (vmId="
            + this.vmId + " ) for memory configuration sample.");
    }

    protected void run() throws Exception {
        // Get the current memory info
        System.out.println("\n\n#### Print original memory info.");
        MemoryTypes.Info memoryInfo = memoryService.get(this.vmId);
        System.out.println(memoryInfo);

        /*
         *  Save the current memory info to verify that we have cleaned up
         *  properly.
         */
        this.originalMemoryInfo = memoryInfo;

        System.out.println(
            "\n\n#### Example: Update memory size field of memory"
            + " configuration.");
        MemoryTypes.UpdateSpec memoryUpdateSpec =
                new MemoryTypes.UpdateSpec.Builder().setSizeMiB(8 * 1024l)
                    .build();
        memoryService.update(this.vmId, memoryUpdateSpec);
        System.out.println(memoryUpdateSpec);
        memoryInfo = memoryService.get(this.vmId);
        System.out.println(memoryInfo);

        System.out.println(
            "\n\n#### Example: Update hot add enabled field of memory "
            + "configuration.");
        memoryUpdateSpec = new MemoryTypes.UpdateSpec.Builder()
            .setHotAddEnabled(true).build();
        memoryService.update(this.vmId, memoryUpdateSpec);
        System.out.println(memoryUpdateSpec);
        memoryInfo = memoryService.get(this.vmId);
        System.out.println(memoryInfo);
    }

    protected void cleanup() throws Exception {
        System.out.println("\n\n#### Cleanup: Revert memory configuration.");
        MemoryTypes.UpdateSpec memoryUpdateSpec =
                new MemoryTypes.UpdateSpec.Builder().setHotAddEnabled(
                    this.originalMemoryInfo.getHotAddEnabled())
                    .setSizeMiB(this.originalMemoryInfo.getSizeMiB())
                    .build();
        this.memoryService.update(this.vmId, memoryUpdateSpec);
        System.out.println(memoryUpdateSpec);
        MemoryTypes.Info memoryInfo = memoryService.get(this.vmId);
        System.out.println(memoryInfo);
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
        new MemoryConfiguration().execute(args);
    }
}