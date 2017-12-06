/*
 * *******************************************************
 * Copyright VMware, Inc. 2017.  All Rights Reserved.
 * SPDX-License-Identifier: MIT
 * *******************************************************
 *
 * DISCLAIMER. THIS PROGRAM IS PROVIDED TO YOU "AS IS" WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, WHETHER ORAL OR WRITTEN,
 * EXPRESS OR IMPLIED. THE AUTHOR SPECIFICALLY DISCLAIMS ANY IMPLIED
 * WARRANTIES OR CONDITIONS OF MERCHANTABILITY, SATISFACTORY QUALITY,
 * NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE.
 */
package vmware.samples.vcenter.vm.power;

import java.util.Collections;
import java.util.List;

import org.apache.commons.cli.Option;

import com.vmware.vcenter.vm.Power;
import com.vmware.vcenter.vm.PowerTypes;

import vmware.samples.common.SamplesAbstractBase;
import vmware.samples.vcenter.helpers.VmHelper;

/**
 * Description: Demonstrates the Power Life Cycle of a VM
 *
 * Author: VMware, Inc.
 * Sample Prerequisites: The sample needs an existing VM
 */
public class PowerLifeCycle extends SamplesAbstractBase{

    private String vmName;
    private Power vmPowerService;
    private String vmId;

    @Override
    protected void parseArgs(String[] args) {
        // Parse the command line options or use config file
        Option vmNameOption = Option.builder()
            .longOpt("vmname")
            .desc("Name of the VM on which the power operations"
            		+ " would be performed")
            .required(true)
            .hasArg()
            .argName("VM NAME")
            .build();
        List<Option> optionList = Collections.singletonList(vmNameOption);
        super.parseArgs(optionList, args);
        this.vmName =  (String) parsedOptions.get("vmname");
    }

    @Override
    protected void setup() throws Exception {
        this.vmId = VmHelper.getVM(vapiAuthHelper.getStubFactory(),
                sessionStubConfig,
                vmName);
        System.out.println("Using VM: " + vmName + " (vmId="
            + this.vmId + " ) for Power Operations sample.");
        this.vmPowerService = this.vapiAuthHelper.getStubFactory().createStub(
                Power.class, this.sessionStubConfig); 
    }

    @Override
    protected void run() throws Exception {
        //Get the vm power state
        System.out.println("# Example: Get current vm power state");
        PowerTypes.Info powerInfo = this.vmPowerService.get(vmId);

        // Power off the vm if it is on
        if (PowerTypes.State.POWERED_ON.equals(powerInfo.getState()))
        {
            System.out.println("# Example: VM is powered on, power it off");
            this.vmPowerService.stop(vmId);
        }

        //Power on the vm
        System.out.println("# Example: Power on the vm");
        this.vmPowerService.start(vmId);
        System.out.println("vm.power->start()");

        //Suspend the vm
        System.out.println("# Example: Suspend the vm");
        this.vmPowerService.suspend(vmId);
        System.out.println("vm.power->suspend()" );

        //Resume the vm
        System.out.println("# Example: Resume the vm");
        this.vmPowerService.start(vmId);
        System.out.println("vm.power->resume()");

        //Reset the vm
        System.out.println("# Example: Reset the vm");
        this.vmPowerService.reset(vmId);
        System.out.println("vm.power->reset()");
    }

    @Override
    protected void cleanup() throws Exception {
        //Power off the vm
        System.out.println("# Cleanup: Power off the vm");
        this.vmPowerService.stop(vmId);
        System.out.println("vm.power->stop()");
        PowerTypes.Info powerInfo = this.vmPowerService.get(vmId);

        //Power off the vm if it is on
        if (PowerTypes.State.POWERED_OFF.equals(powerInfo.getState()))
        {
            System.out.println("VM is powered off" );
        }
        else {
            System.out.println("vm.Power Warning: Could not power off vm" );
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
        new PowerLifeCycle().execute(args);
    }

}
