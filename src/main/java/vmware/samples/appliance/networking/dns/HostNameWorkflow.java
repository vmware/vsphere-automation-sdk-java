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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.Option;

import com.vmware.appliance.networking.dns.Hostname;
import com.vmware.appliance.networking.dns.HostnameTypes.Message;
import com.vmware.appliance.networking.dns.HostnameTypes.TestStatus;
import com.vmware.appliance.networking.dns.HostnameTypes.TestStatusInfo;

import vmware.samples.common.SamplesAbstractBase;

/**
 * Description:
 * 1. Demonstrates getting Hostname
 * 2. Demonstrates setting Hostname
 * 3. Demonstrates testing Hostname
 *
 * Author: VMware, Inc. Sample Prerequisites: vCenter 6.7+
 */
public class HostNameWorkflow extends SamplesAbstractBase {
    private Hostname dnsHostnameService;
    private String dnsHostname;

    protected void setup() throws Exception {
        this.dnsHostnameService = vapiAuthHelper.getStubFactory().createStub(
            Hostname.class,
            sessionStubConfig);
    }

    protected void run() throws Exception {
        // Set specified hostname - Set operation not allowed as of now
        // dnsHostnameService.set(dnsHostname);

        // Get hostname
        System.out.println("DNS Hostname : " + dnsHostnameService.get() + "\n");

        // Test hostname
        System.out.println("Testing the new hostname entry...");
        TestStatusInfo testInfo = dnsHostnameService.test(dnsHostname);
        TestStatus testStatus = testInfo.getStatus();
        System.out.println("Hostname Test status : " + testStatus);
        for (Message testMsg : testInfo.getMessages()) {
            System.out.println("Test Result : " + testMsg.getResult());
            System.out.println("Test Message : " + testMsg.getMessage());
        }
    }

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
        new HostNameWorkflow().execute(args);
    }

    protected void parseArgs(String[] args) {
        Option healthMsgOption = Option.builder()
            .longOpt("dnsHostname")
            .desc("OPTIONAL: Specify the hostname to be set for the server")
            .argName("DNS_HOSTNAME")
            .required(false)
            .hasArg()
            .build();
        List<Option> optionList = Arrays.asList(healthMsgOption);
        super.parseArgs(optionList, args);
        if (parsedOptions.get("dnsHostname") == null) {
            System.out.println("Value for dnsHostname is not provided, "
                               + "hence defaulting to provided server name.");
            try {
                this.dnsHostname = InetAddress.getByName((String) parsedOptions
                    .get("server")).getHostName();
            } catch (UnknownHostException ex) {
                System.out.println(
                    "Unable to resolve hostname from provided server value, "
                                   + "hence defaulting to testhost.com");
                this.dnsHostname = "testhost.com";
            }
        }
        else {
            this.dnsHostname = (String) parsedOptions.get("dnsHostname");
        }
    }
}