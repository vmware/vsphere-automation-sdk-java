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
package vmware.samples.appliance.networking.dns;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.Option;

import com.vmware.appliance.networking.dns.Domains;

import vmware.samples.common.SamplesAbstractBase;

/**
 * Description: Demonstrates getting and setting DNS domains
 *
 * Author: VMware, Inc. Sample Prerequisites: vCenter 6.7+
 */
public class DnsDomainWorkflow extends SamplesAbstractBase {
    private Domains domainService;
    private String[] dnsDomains;
    private List<String> initialDomains;

    protected void setup() throws Exception {
        this.domainService = vapiAuthHelper.getStubFactory().createStub(
            Domains.class,
            sessionStubConfig);
    }

    protected void run() throws Exception {
        initialDomains = domainService.list();
        System.out.println("Existing DNS domains : " + initialDomains + "\n");

        // Adding new DNS domain
        System.out.println("Adding domain " + dnsDomains[0] + " to the "
                           + "DNS domains...");
        domainService.add(dnsDomains[0]);
        System.out.println("New list of DNS domains : " + domainService.list()
                           + "\n");

        // Setting new DNS domains
        System.out.println("Setting " + Arrays.asList(this.dnsDomains)
                           + " as DNS domains...");
        domainService.set(Arrays.asList(dnsDomains));
        System.out.println("New DNS domains list : " + domainService.list());
    }

    protected void cleanup() throws Exception {
        System.out.println("\nCleaning up DNS domain settings...");
        domainService.set(initialDomains);
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
        new DnsDomainWorkflow().execute(args);
    }

    protected void parseArgs(String[] args) {
        Option dnsDomainsOption = Option.builder()
            .longOpt("dnsDomains")
            .desc("REQUIRED: Specify the DNS domain names as comma "
                  + "separated value")
            .argName("DNS_DOMAIN_NAMES")
            .required(true)
            .hasArgs()
            .build();
        List<Option> optionList = Arrays.asList(dnsDomainsOption);
        super.parseArgs(optionList, args);
        String serverNames = (String) parsedOptions.get("dnsDomains");
        this.dnsDomains = serverNames.split(",");
    }
}