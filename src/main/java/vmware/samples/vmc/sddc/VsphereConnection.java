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
package vmware.samples.vmc.sddc;

import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.Option;

import com.vmware.vapi.bindings.StubConfiguration;
import com.vmware.vapi.client.ApiClient;
import com.vmware.vapi.protocol.HttpConfiguration;
import com.vmware.vcenter.VM;
import com.vmware.vcenter.VMTypes;
import com.vmware.vcenter.VMTypes.Summary;
import com.vmware.vmc.model.Sddc;
import com.vmware.vmc.orgs.Sddcs;

import vmware.samples.common.VmcSamplesAbstractBase;
import vmware.samples.common.authentication.VapiAuthenticationHelper;
import vmware.samples.common.authentication.VmcAuthenticationHelper;

/**
 * Description: Demonstrates how to connect to a vSphere in an SDDC
 * using the default cloud admin credentials.
 *
 * Author: VMware, Inc.
 * Sample Prerequisites:
 *      - A SDDC in the org
        - A firewall rule to access the vSphere
 */
public class VsphereConnection extends VmcSamplesAbstractBase {
    private Sddcs sddcsStub;
    private VM vmStub;
    private ApiClient vmcClient;
    private VapiAuthenticationHelper vapiAuthHelper;
    private String orgId, sddcId;

    /**
     * Define the options specific to this sample and configure the sample using
     * command-line arguments or a config file
     *
     * @param args command line arguments passed to the sample
     */
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

    protected void setup() throws Exception {
        this.vmcAuthHelper = new VmcAuthenticationHelper();
        this.vmcClient = this.vmcAuthHelper.newVmcClient(this.vmcServer, this.cspServer, this.refreshToken);
        this.sddcsStub = vmcClient.createStub(Sddcs.class);
        this.vmStub = vmcClient.createStub(VM.class);
    }

    protected void run() throws Exception {
        // Get vCenter hostname, username and password
        Sddc sddc = sddcsStub.get(orgId, sddcId);
        URL vcServerUrl = new URL(sddc.getResourceConfig().getVcUrl());
        String vcServer = vcServerUrl.getHost();
        String vcUsername = sddc.getResourceConfig().getCloudUsername();
        String vcPassword = sddc.getResourceConfig().getCloudPassword();

        // Connect to vSphere client using the initial cloud admin credentials.
        // Please use the new credentials to login after you reset the default one.
        vapiAuthHelper = new VapiAuthenticationHelper();
        StubConfiguration sessionStubConfig =
                vapiAuthHelper.loginByUsernameAndPassword(
                		vcServer, vcUsername, vcPassword, new HttpConfiguration.Builder().getConfig());
        this.vmStub =
                vapiAuthHelper.getStubFactory()
                    .createStub(VM.class, sessionStubConfig);
        List<Summary> vmList = this.vmStub.list(new VMTypes.FilterSpec());
        System.out.println("----------------------------------------");
        System.out.println("List of VMs");
        for (Summary vmSummary : vmList) {
            System.out.println(vmSummary);
        }
        System.out.println("----------------------------------------");
    }

    protected void cleanup() throws Exception {
        // No cleanup required.
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
        new VsphereConnection().execute(args);
    }
}
