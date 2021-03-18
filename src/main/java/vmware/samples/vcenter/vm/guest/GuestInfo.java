/*
 * *******************************************************
 * Copyright VMware, Inc. 2021.  All Rights Reserved.
 * SPDX-License-Identifier: MIT
 * *******************************************************
 *
 * DISCLAIMER. THIS PROGRAM IS PROVIDED TO YOU "AS IS" WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, WHETHER ORAL OR WRITTEN,
 * EXPRESS OR IMPLIED. THE AUTHOR SPECIFICALLY DISCLAIMS ANY IMPLIED
 * WARRANTIES OR CONDITIONS OF MERCHANTABILITY, SATISFACTORY QUALITY,
 * NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE.
 */
package vmware.samples.vcenter.vm.guest;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.Option;

import com.vmware.vcenter.vm.Power;
import com.vmware.vcenter.vm.PowerTypes;
import com.vmware.vcenter.vm.guest.Identity;
import com.vmware.vcenter.vm.guest.IdentityTypes;
import com.vmware.vcenter.vm.guest.LocalFilesystem;
import com.vmware.vcenter.vm.guest.LocalFilesystemTypes;

import vmware.samples.common.SamplesAbstractBase;
import vmware.samples.vcenter.helpers.VmHelper;

/**
Demonstrates the virtual machine guest information.

Sample Prerequisites:
The sample needs an existing VM with VMware Tools.
**/
public class GuestInfo extends SamplesAbstractBase{

    private String vmName;
    private String vmId;
    private Power vmPowerService;
    private Identity identityService;
    private LocalFilesystem filesystemService;
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
        this.vmPowerService = this.vapiAuthHelper.getStubFactory().createStub(
                Power.class, this.sessionStubConfig);
        identityService=this.vapiAuthHelper.getStubFactory().createStub(Identity.class, sessionStubConfig);
        filesystemService=this.vapiAuthHelper.getStubFactory().createStub(LocalFilesystem.class, sessionStubConfig);
    }
    @Override
    protected void run() throws Exception {
        this.vmId = VmHelper.getVM(vapiAuthHelper.getStubFactory(),
                sessionStubConfig,
                vmName);
      if(vmId == null) {
          throw new Exception("Sample requires an existing vm with name "+this.vmName+"Please create the vm first");
      }
      else {
          System.out.println("Using VM "+this.vmName+" "+this.vmId+" for Guest Info Sample");
      }

      System.out.println("# Example: Get current vm power state");
      PowerTypes.Info powerInfo = this.vmPowerService.get(vmId);
      if (!(PowerTypes.State.POWERED_ON.equals(powerInfo.getState())))
      {
          System.out.println("Powering on VM.");
          this.vmPowerService.start(vmId);
      }
      VmHelper.waitForGuestInfoReady(identityService, vmId, 600);
      IdentityTypes.Info info=identityService.get(vmId);
      System.out.println("vm.guest.Identity.get({"+vmName+"})");
      System.out.println("Identity: {"+info.toString()+"}");

      Map<String, LocalFilesystemTypes.Info> filesystemInfo=filesystemService.get(vmId);
      System.out.println("vm.guest.Identity.get({"+vmName+"})");
      System.out.println("LocalFilesystem: {"+filesystemInfo.toString()+"}");
    }

    @Override
    protected void cleanup() throws Exception {

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
        new GuestInfo().execute(args);
    }

}
