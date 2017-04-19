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
package vmware.samples.vcenter.vm.hardware.cpu;

import java.util.Collections;
import java.util.List;

import org.apache.commons.cli.Option;

import com.vmware.vcenter.vm.hardware.Cpu;
import com.vmware.vcenter.vm.hardware.CpuTypes;

import vmware.samples.common.SamplesAbstractBase;
import vmware.samples.vcenter.helpers.VmHelper;

/**
 * Description: Demonstrates how to configure a CPU for a VM.
 *
 * Author: VMware, Inc.
 * Sample Prerequisites: The sample needs an existing VM.
 *
 */
public class CpuConfiguration extends SamplesAbstractBase {
    private String vmName;
    private String vmId;
    CpuTypes.Info originalCpuInfo;
    private Cpu cpuService;

    /**
     * Define the options specific to this sample and configure the sample using
     * command-line arguments or a config file
     *
     * @param args command line arguments passed to the sample
     */
    protected void parseArgs(String[] args) {
        Option vmNameOption = Option.builder()
            .longOpt("vmname")
            .desc("The name of the vm for which to configure cpu settings.")
            .required(true)
            .hasArg()
            .argName("VM NAME")
            .build();
        List<Option> optionList = Collections.singletonList(vmNameOption);
        super.parseArgs(optionList, args);
        this.vmName = (String) parsedOptions.get("vmname");
    }

    protected void setup() throws Exception {
        this.cpuService = vapiAuthHelper.getStubFactory().createStub(Cpu.class,
            this.sessionStubConfig);

        System.out.println("\n\n#### Setup: Get the virtual machine id");
        this.vmId = VmHelper.getVM(vapiAuthHelper.getStubFactory(),
            sessionStubConfig,
            vmName);
        System.out.println("Using VM: " + vmName + " (vmId="
            + this.vmId + " ) for CPU configuration sample.");
    }

    protected void run() throws Exception {
        // Get the current cpu info
        System.out.println("\n\n#### Example: Print original cpu info");
        CpuTypes.Info cpuInfo = cpuService.get(this.vmId);
        System.out.println(cpuInfo);

        // Save the current cpu info to verify that we have cleaned up properly
        this.originalCpuInfo = cpuInfo;

        System.out.println("\n\n#### Example: Update count field of CPU"
                           + " configuration");
        CpuTypes.UpdateSpec cpuUpdateSpec = new CpuTypes.UpdateSpec.Builder()
            .setCount(2l).build();
        cpuService.update(this.vmId, cpuUpdateSpec);
        System.out.println(cpuUpdateSpec);
        cpuInfo = cpuService.get(this.vmId);
        System.out.println("VM ID=" + this.vmId);
        System.out.println(cpuInfo);

        System.out.println("\n\n#### Example: Update cpu fields,"
                           + " number of cores per socket=2, enable hot add");
        cpuUpdateSpec = new CpuTypes.UpdateSpec.Builder().setCoresPerSocket(2l)
            .setHotAddEnabled(true)
            .build();
        cpuService.update(this.vmId, cpuUpdateSpec);
        System.out.println(cpuUpdateSpec);
        cpuInfo = cpuService.get(this.vmId);
        System.out.println("VM ID=" + this.vmId);
        System.out.println(cpuInfo);
    }

    protected void cleanup() throws Exception {
        System.out.println("\n\n#### Cleanup: Revert cpu configuration");
        CpuTypes.UpdateSpec cpuUpdateSpec = new CpuTypes.UpdateSpec.Builder()
                .setCoresPerSocket(this.originalCpuInfo.getCoresPerSocket())
                .setCount(this.originalCpuInfo.getCount())
                .setHotAddEnabled(this.originalCpuInfo.getHotAddEnabled())
                .setHotRemoveEnabled(this.originalCpuInfo.getHotRemoveEnabled())
                .build();
        cpuService.update(this.vmId,  cpuUpdateSpec);
        System.out.println(cpuUpdateSpec);
        CpuTypes.Info cpuInfo = cpuService.get(this.vmId);
        System.out.println("VMID=" + this.vmId);
        System.out.println(cpuInfo);
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
        new CpuConfiguration().execute(args);
    }
}