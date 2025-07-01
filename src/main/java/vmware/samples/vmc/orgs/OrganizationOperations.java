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
 * Description: Demonstrates how to list all organizations the calling user
 * is authorized on.
 *
 * Author: VMware, Inc.
 * Sample Prerequisites: VMC Server
 */
public class OrganizationOperations extends VmcSamplesAbstractBase {
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
        ApiClient apiClient =
                this.vmcAuthHelper.newVmcClient(this.vmcServer,
                        this.cspServer, this.refreshToken);

        orgsStub = apiClient.createStub(Orgs.class);
    }

    @Override
    protected void run() throws Exception {
        List<Organization> currentUserOrgs = orgsStub.list();
        for (Organization org : currentUserOrgs) {
            System.out.printf("\nPrinting details for ORG (ORG ID=%s, ORG NAME=%s)\n",
                org.getId(), org.getDisplayName());
            System.out.println(currentUserOrgs);
        }
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
        new OrganizationOperations().execute(args);
    }
}
