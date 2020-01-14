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

import com.vmware.nsx_policy.Infra;
import com.vmware.nsx_policy.infra.Domains;
import com.vmware.nsx_policy.model.DomainListResult;
import com.vmware.nsx_vmc_app.infra.LinkedVpcs;
import com.vmware.nsx_vmc_app.model.LinkedVpcsListResult;
import com.vmware.vapi.client.ApiClient;

import vmware.samples.common.VmcSamplesAbstractBase;
import vmware.samples.common.authentication.VmcNsxAuthenticationHelper;

/*-
 * This example shows how to authenticate to the VMC (VMware Cloud on AWS)
 * service, using a VMC refresh token to obtain an authentication token that
 * can then be used to authenticate to the NSX-T instance in a Software
 * Defined Data Center (SDDC). It also shows how to list several types
 * of entities.
 */
public class NsxTAuth extends VmcSamplesAbstractBase {

    private String orgId;
    private String sddcId;
    private ApiClient apiClient;
    private Infra infraService;
    private Domains domainsService;
    private LinkedVpcs lvpcService;

    public static String VMC_SERVER = "vmc.vmware.com";
    public static String CSP_SERVER = "console.cloud.vmware.com";

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
        List<Option> optionList =
            Arrays.asList(orgOption, sddcOption);

        super.parseArgs(optionList, args);
        this.orgId = (String) parsedOptions.get("org_id");
        this.sddcId = (String) parsedOptions.get("sddc_id");
    }

    @Override
    protected void setup() throws Exception {
        // Create the API client. This client will automatically obtain
        // and use an authentication token from the VMC CSP service,
        // and will automatically refresh it when it expires.
        this.apiClient = new VmcNsxAuthenticationHelper()
                .newVmcNsxPolicyClient(this.orgId,
                        this.sddcId, this.refreshToken,
                        false, false);
        this.infraService = apiClient.createStub(Infra.class);
        this.domainsService = apiClient.createStub(Domains.class);
        this. lvpcService = apiClient.createStub(LinkedVpcs.class);
    }

    public void listDomains() {
        com.vmware.nsx_policy.model.Infra infra = this.infraService.get(null);
        System.out.println(infra);

        DomainListResult domainsList = this.domainsService.list(null,  null, null, 1000L,  false, null);
        System.out.println(domainsList);

        LinkedVpcsListResult results = this.lvpcService.list();
        System.out.println(results);
    }

    @Override
    protected void run() throws Exception {
        listDomains();
    }

    @Override
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
        new NsxTAuth().execute(args);
    }
}
