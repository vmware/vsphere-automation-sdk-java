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
package vmware.samples.vmc.orgs;

import java.util.Collections;
import java.util.List;

import org.apache.commons.cli.Option;

import com.vmware.vapi.client.ApiClient;
import com.vmware.vmc.Orgs;
import com.vmware.vmc.model.Organization;

import vmware.samples.common.VmcSamplesAbstractBase;
import vmware.samples.common.authentication.VmcAuthenticationHelper;

/**
 * Description: Demonstrates how to get organization details using token
 * generated from client_credentials flow.
 *
 * Author: VMware, Inc.
 * Sample Prerequisites: VMC Server
 */
public class GetOrganizationOperation extends VmcSamplesAbstractBase {
    private Orgs orgsStub;

    /**
     * Define the options specific to this sample and configure the sample using
     * command-line arguments or a config file
     *
     * @param args command line arguments passed to the sample
     */
    @Override
    protected void parseArgs(String[] args) {
        List<Option> optionList = Collections.<Option>emptyList();
        super.parseArgs(optionList, args);
    }

    @Override
    @SuppressWarnings(value = { "deprecation" })
    protected void setup() throws Exception {
        this.vmcAuthHelper = new VmcAuthenticationHelper();
        ApiClient apiClient = this.vmcAuthHelper.newVmcClient(
                this.vmcServer,
                this.cspServer,
                this.clientId,
                this.clientSecret.toCharArray());

        orgsStub = apiClient.createStub(Orgs.class);
    }

    @Override
    protected void run() throws Exception {
        Organization orgDetails = orgsStub.get(this.orgId);
        System.out.printf("\nPrinting details for ORG (ORG ID=%s, ORG NAME=%s)\n",
                this.orgId, orgDetails.getDisplayName());
        System.out.println(orgDetails);
    }

    @Override
    protected void cleanup() throws Exception {
        // No cleanup required
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
        new GetOrganizationOperation().execute(args);
    }
}
