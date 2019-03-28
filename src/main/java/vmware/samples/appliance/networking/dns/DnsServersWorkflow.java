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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.Option;

import com.vmware.appliance.networking.dns.Servers;
import com.vmware.appliance.networking.dns.ServersTypes.DNSServerConfig;
import com.vmware.appliance.networking.dns.ServersTypes.DNSServerMode;
import com.vmware.appliance.networking.dns.ServersTypes.Message;
import com.vmware.appliance.networking.dns.ServersTypes.TestStatus;
import com.vmware.appliance.networking.dns.ServersTypes.TestStatusInfo;

import vmware.samples.common.SamplesAbstractBase;

/**
 * Description:
 * 1. Demonstrates getting DNS Servers
 * 2. Demonstrates adding a DNS Server
 * 3. Demonstrates setting DNS Servers with Static Mode
 * 4. Demonstrates setting DNS Servers with DHCP Mode
 * 5. Demonstrates testing the DNS Servers
 *
 * Author: VMware, Inc. Sample Prerequisites: vCenter 6.7+
 */
public class DnsServersWorkflow extends SamplesAbstractBase {
    private Servers dnsServerService;
    private DNSServerMode mode;
    private String[] dnsServers;
    private DNSServerMode initialDnsServerMode;
    private List<String> initialDnsServers;

    protected void setup() throws Exception {
        this.dnsServerService = vapiAuthHelper.getStubFactory().createStub(
            Servers.class,
            sessionStubConfig);
    }

    protected void run() throws Exception {
        DNSServerConfig dnsServerConf = dnsServerService.get();
        initialDnsServerMode = dnsServerConf.getMode();
        if (initialDnsServerMode == DNSServerMode.is_static) {
            initialDnsServers = dnsServerConf.getServers();
        }
        System.out.println("DNS Mode : " + initialDnsServerMode);
        System.out.println("Existing DNS Servers : " + initialDnsServers
                           + "\n");

        // Set the list of DNS servers
        if (mode == DNSServerMode.is_static) {
            DNSServerConfig dnsServerConf1 = new DNSServerConfig();
            dnsServerConf1.setMode(mode);
            dnsServerConf1.setServers(Arrays.asList(dnsServers));
            dnsServerService.set(dnsServerConf1);
            dnsServerConf = dnsServerService.get();
            System.out.println("New DNS Mode : " + dnsServerConf.getMode());
            System.out.println("New Modified DNS Servers : " + dnsServerConf
                .getServers() + "\n");

            // Add the first DNS server to the list of DNS servers
            dnsServerConf1 = new DNSServerConfig();
            dnsServerConf1.setMode(DNSServerMode.is_static);
            dnsServerConf1.setServers(new ArrayList<String>());
            dnsServerService.set(dnsServerConf1);
            dnsServerService.add(dnsServers[0]);
            dnsServerConf = dnsServerService.get();
            System.out.println("DNS Mode : " + dnsServerConf.getMode());
            System.out.println("Modified DNS Servers : " + dnsServerConf
                .getServers() + "\n");

            // Test the list of servers
            TestStatusInfo testInfo = dnsServerService.test(Arrays.asList(
                dnsServers));
            TestStatus status = testInfo.getStatus();
            System.out.println("Test status for the provided DNS servers : "
                               + status);
            for (Message testMsg : testInfo.getMessages()) {
                System.out.println("Test Result : " + testMsg.getResult());
                System.out.println("Test Message : " + testMsg.getMessage());
            }
        } else {
            // Change the DNS Server config to DHCP mode
            DNSServerConfig dnsServerConf1 = new DNSServerConfig();
            dnsServerConf1.setMode(mode);
            dnsServerConf1.setServers(new ArrayList<String>());
            dnsServerService.set(dnsServerConf1);
            dnsServerConf = dnsServerService.get();
            System.out.println("DNS Mode : " + dnsServerConf.getMode());
            System.out.println("DHCP DNS Servers : " + dnsServerConf
                .getServers() + "\n");
        }
    }

    protected void cleanup() throws Exception {
        System.out.println("\nCleaning up DNS server configurations...");
        if (initialDnsServerMode == DNSServerMode.dhcp) {
            initialDnsServers = new ArrayList<String>();
        }
        DNSServerConfig dnsServerConf = new DNSServerConfig();
        dnsServerConf.setMode(initialDnsServerMode);
        dnsServerConf.setServers(initialDnsServers);
        dnsServerService.set(dnsServerConf);
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
        new DnsServersWorkflow().execute(args);
    }

    protected void parseArgs(String[] args) {
        Option dnsServerModeOption = Option.builder()
            .longOpt("dnsMode")
            .desc("OPTIONAL: Specify the DNS Mode to be used "
                  + "(DHCP/STATIC).")
            .argName("DNS_MODE")
            .required(true)
            .hasArgs()
            .build();
        Option dnsServersOption = Option.builder()
            .longOpt("dnsServers")
            .desc("OPTIONAL: Specify the DNS server names as comma"
                  + " separated value")
            .argName("DNS_SERVERS")
            .required(true)
            .hasArgs()
            .build();
        List<Option> optionList = Arrays.asList(dnsServerModeOption,
            dnsServersOption);
        super.parseArgs(optionList, args);
        if (parsedOptions.get("dnsMode") == null) {
            System.out.println("Value for dnsMode option is not provided, "
                               + "hence defaulting to DHCP");
            mode = DNSServerMode.dhcp;
        } else {
            String dnsMode = (String) parsedOptions.get("dnsMode");
            if ("dhcp".equalsIgnoreCase(dnsMode)) {
                mode = DNSServerMode.dhcp;
            } else if ("static".equalsIgnoreCase(dnsMode)) {
                mode = DNSServerMode.is_static;
            } else {
                System.out.println("Unsupported DNS mode : " + dnsMode);
                System.exit(0);
            }
        }
        if (mode == DNSServerMode.is_static) {
            if (parsedOptions.get("dnsServers") == null) {
                System.out.println("Value for dnsServers option is not "
                                   + "provided.");
                System.exit(0);
            }
            String serverNames = (String) parsedOptions.get("dnsServers");
            this.dnsServers = serverNames.split(",");
        }
    }
}