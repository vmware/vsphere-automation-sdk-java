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
package vmware.samples.vcenter.vm.hardware.boot;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.Option;

import com.vmware.vcenter.vm.hardware.Boot;
import com.vmware.vcenter.vm.hardware.BootTypes;

import vmware.samples.common.SamplesAbstractBase;
import vmware.samples.vcenter.helpers.VmHelper;

/**
 * Description: Demonstrates how to configure the settings used when booting a 
 * virtual machine.
 *
 * Author: VMware, Inc.
 * Sample Prerequisites: The sample needs an existing VM.
 */
public class BootConfiguration extends SamplesAbstractBase {
    private String vmName;
    private String vmId;
    private BootTypes.Info originalBootInfo;
    private Boot bootService;

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
                .desc("The name of the vm for which to configure boot options")
                .build();
        List<Option> optionList = new ArrayList<Option>();
        optionList.add(vmNameOption);
        super.parseArgs(optionList, args);
        this.vmName = (String) parsedOptions.get("vmname");
    }

    protected void setup() throws Exception {
        this.bootService =
                vapiAuthHelper.getStubFactory().createStub(Boot.class,
                    this.sessionStubConfig);

        System.out.println("\n\n#### Setup: Get the virtual machine id");
        this.vmId = VmHelper.getVM(vapiAuthHelper.getStubFactory(),
            sessionStubConfig,
            vmName);
        System.out.println("Using VM: " + vmName + " (vmId="
            + this.vmId + " ) for boot configuration sample.");

    }

    protected void run() throws Exception {
        // Print the current boot configuration
        System.out.println("\n\n#### Print the original Boot Info");
        BootTypes.Info bootInfo = this.bootService.get(this.vmId);
        System.out.println(bootInfo);

        // Save the current boot info to verify that we have cleaned up properly
        this.originalBootInfo = bootInfo;

        System.out.println(
            "\n\n#### Example: Update firmware to EFI for boot configuration.");
        BootTypes.UpdateSpec bootUpdateSpec = new BootTypes.UpdateSpec.Builder()
            .setType(BootTypes.Type.EFI)
            .build();
        this.bootService.update(this.vmId, bootUpdateSpec);
        System.out.println(bootUpdateSpec);
        bootInfo = this.bootService.get(this.vmId);
        System.out.println(bootInfo);

        System.out.println(
            "\n\n#### Example: Update boot firmware to tell it to enter setup"
            + " mode on next boot.");
        bootUpdateSpec = new BootTypes.UpdateSpec.Builder()
            .setEnterSetupMode(true)
            .build();
        this.bootService.update(this.vmId, bootUpdateSpec);
        System.out.println(bootUpdateSpec);
        bootInfo = this.bootService.get(this.vmId);
        System.out.println(bootInfo);

        System.out.println(
            "\n\n#### Example: Update firmware to introduce a delay in boot "
            + "process and automatically reboot after a failure to boot, "
            + "retry delay = 30000 ms.");
        bootUpdateSpec = new BootTypes.UpdateSpec.Builder()
            .setDelay(10000l)
            .setRetry(true)
            .setRetryDelay(30000l)
            .build();
        this.bootService.update(this.vmId, bootUpdateSpec);
        bootInfo = this.bootService.get(this.vmId);
        System.out.println(bootInfo);
    }

    protected void cleanup() throws Exception {
        System.out.println("\n\n#### Cleanup: Revert the boot configuration.");
        BootTypes.UpdateSpec bootUpdateSpec = new BootTypes.UpdateSpec.Builder()
            .setDelay(this.originalBootInfo.getDelay())
            .setEfiLegacyBoot(this.originalBootInfo.getEfiLegacyBoot())
            .setEnterSetupMode(this.originalBootInfo.getEnterSetupMode())
            .setNetworkProtocol(this.originalBootInfo.getNetworkProtocol())
            .setRetry(this.originalBootInfo.getRetry())
            .setRetryDelay(this.originalBootInfo.getRetryDelay())
            .setType(this.originalBootInfo.getType())
            .build();
        this.bootService.update(this.vmId, bootUpdateSpec);
        System.out.println(bootUpdateSpec);
        BootTypes.Info bootInfo = this.bootService.get(this.vmId);
        System.out.println(bootInfo);
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
        new BootConfiguration().execute(args);
    }
}
