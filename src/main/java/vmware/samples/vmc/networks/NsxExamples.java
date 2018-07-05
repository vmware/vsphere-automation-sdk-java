/*
 * *******************************************************
 * Copyright VMware, Inc. 2018.  All Rights Reserved.
 * SPDX-License-Identifier: MIT
 * *******************************************************
 *
 * DISCLAIMER. THIS PROGRAM IS PROVIDED TO YOU "AS IS" WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, WHETHER ORAL OR WRITTEN,
 * EXPRESS OR IMPLIED. THE AUTHOR SPECIFICALLY DISCLAIMS ANY IMPLIED
 * WARRANTIES OR CONDITIONS OF MERCHANTABILITY, SATISFACTORY QUALITY,
 * NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE.
 */
package vmware.samples.vmc.networks;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.Option;

import com.vmware.vapi.client.ApiClient;
import com.vmware.nsx_policy.Infra;

import vmware.samples.common.VmcSamplesAbstractBase;
import vmware.samples.common.authentication.VmcNsxAuthenticationHelper;

/**
 * Description: Demonstrates how to XYZZY
 *
 * Demo steps required to XYZZY
 *
 * Sample Prerequisites:
 *
 *
 * Author: VMware, Inc.
 */
public class NsxExamples extends VmcSamplesAbstractBase {
    private ApiClient vmcNsxClient;
    private String orgId, sddcId;
    private VmcNsxAuthenticationHelper vmcNsxAuthHelper;
    private Infra infraSvc;

    /**
     * Define the options specific to this sample and configure the sample using
     * command-line arguments or a config file
     *
     * @param args command line arguments passed to the sample
     */
    @Override
    protected void parseArgs(String[] args) {
        Option orgOption = Option.builder()
                .longOpt("org_id")
                .desc("Specify the organization id")
                .argName("ORGANIZATION ID")
                .required(true)
                .hasArg()
                .build();
        Option sddcOption = Option.builder()
                .longOpt("sddc_id")
                .desc("Specify the SDDC id")
                .argName("SDDC ID")
                .required(true)
                .hasArg()
                .build();
        List<Option> optionList = Arrays.asList(orgOption, sddcOption);

        super.parseArgs(optionList, args);
        this.orgId = (String) parsedOptions.get("org_id");
        this.sddcId = (String) parsedOptions.get("sddc_id");
    }

    @Override
    protected void setup() throws Exception {
        this.vmcNsxAuthHelper = new VmcNsxAuthenticationHelper();
        this.vmcNsxClient = this.vmcNsxAuthHelper.newVmcClient(this.vmcServer,
        		this.cspServer, this.refreshToken, this.orgId, this.sddcId);
    }

    @Override
    protected void run() throws Exception {
        System.out.printf("\n\n#### Step 1: Read NSX Policy /infra resource");
        this.infraSvc = vmcNsxClient.createStub(Infra.class);
        com.vmware.nsx_policy.model.Infra infra = infraSvc.get("infra");
        System.out.println(infra);
    }

    @Override
    protected void cleanup() throws Exception {
        System.out.printf("\n\n#### Cleanup: XYZZY");
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
        new NsxExamples().execute(args);
    }
}