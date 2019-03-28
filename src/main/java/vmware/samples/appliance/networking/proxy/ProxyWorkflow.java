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
package vmware.samples.appliance.networking.proxy;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.Option;

import com.vmware.appliance.networking.Proxy;
import com.vmware.appliance.networking.ProxyTypes.Config;
import com.vmware.appliance.networking.ProxyTypes.Protocol;
import com.vmware.appliance.networking.ProxyTypes.TestResult;

import vmware.samples.appliance.helpers.NetworkingHelper;
import vmware.samples.common.SamplesAbstractBase;

/**
 * Description: Demonstrates setting of proxyServer details, getting
 * proxy server details, test the proxy server whether we're able to
 * connect to test host through proxy and deleting the proxy server
 * details for a specific protocol
 *
 * Author: VMware, Inc. Sample Prerequisites: vCenter 6.7+
 */
public class ProxyWorkflow extends SamplesAbstractBase {
    private Proxy proxyService;
    private String protocol;
    private String proxyServer;
    private long port;
    private boolean enabled;
    private String proxyUsername;
    private String proxyPassword;
    private String testHost;

    protected void setup() throws Exception {
        this.proxyService = vapiAuthHelper.getStubFactory().createStub(
            Proxy.class,
            sessionStubConfig);
    }

    protected void run() throws Exception {
        Map<Protocol, Config> proxyList = proxyService.list();
        NetworkingHelper.printProxyDetails(proxyList);

        // Set Proxy details
        Config cfg = new Config();
        cfg.setEnabled(enabled);
        cfg.setServer(proxyServer);
        cfg.setPort(port);
        if (proxyUsername != null) {
            cfg.setUsername(proxyUsername);
            cfg.setPassword(proxyPassword.toCharArray());
        }
        System.out.println("Setting proxy server configuration...");
        proxyService.set(protocol, cfg);

        // Get Protocol detail
        NetworkingHelper.printProxyDetail(protocol, proxyService.get(protocol));

        if (testHost != null) {
            System.out.println("Testing host : '" + testHost
                               + "' is reachable through proxy server : '" + cfg
                                   .getServer() + "'...");
            TestResult result = proxyService.test(testHost, protocol, cfg);
            System.out.println("Server status : " + result.getStatus());
            System.out.println("Result Message : " + result.getMessage()
                .getDefaultMessage());
            System.out.println();
        }

    }

    protected void cleanup() throws Exception {
        // Delete the proxy configuration
        System.out.println("Deleting proxy configuration for protocol : "
                           + protocol);
        proxyService.delete(protocol);
        NetworkingHelper.printProxyDetail(protocol, proxyService.get(protocol));
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
        new ProxyWorkflow().execute(args);
    }

    protected void parseArgs(String[] args) {
        Option protocolOption = Option.builder()
            .longOpt("protocol")
            .desc("REQUIRED: Specify the protocol (HTTP/HTTPS/FTP) for which "
                  + "proxy has to be set.")
            .argName("PROXY_PROTOCOL")
            .required(true)
            .hasArgs()
            .build();
        Option proxyEnabledOption = Option.builder()
            .longOpt("enabled")
            .desc(
                "REQUIRED: Specify whether the proxy has to be enabled or not")
            .argName("PROXY_ENABLED")
            .required(true)
            .hasArgs()
            .build();
        Option proxyServerOption = Option.builder()
            .longOpt("proxyServer")
            .desc("OPTIONAL: Specify the proxy server name (Hostname/IP).")
            .argName("PROXY_SERVER")
            .required(false)
            .hasArgs()
            .build();
        Option proxyPortOption = Option.builder()
            .longOpt("proxyPort")
            .desc("OPTIONAL: Specify the proxy port number.")
            .argName("PROXY_PORT")
            .required(false)
            .hasArgs()
            .build();
        Option proxyUsernameOption = Option.builder()
            .longOpt("proxyUsername")
            .desc("OPTIONAL: Specify the proxy username, if authentication"
                  + " is required to connect to the proxy.")
            .argName("PROXY_USERNAME")
            .required(false)
            .hasArgs()
            .build();
        Option proxyPasswordOption = Option.builder()
            .longOpt("proxyPassword")
            .desc("OPTIONAL: Specify the proxy password.")
            .argName("PROXY_PASSWORD")
            .required(false)
            .hasArgs()
            .build();
        Option testHostOption = Option.builder()
            .longOpt("testHost")
            .desc("OPTIONAL: Specify the test host to connect to to test the "
                  + "proxy.")
            .argName("TEST_HOST")
            .required(false)
            .hasArgs()
            .build();
        List<Option> optionList = Arrays.asList(protocolOption,
            proxyEnabledOption,
            proxyServerOption,
            proxyPortOption,
            proxyUsernameOption,
            proxyPasswordOption,
            testHostOption);
        super.parseArgs(optionList, args);
        this.protocol = (String) parsedOptions.get("protocol");
        this.enabled = Boolean.parseBoolean((String) parsedOptions.get(
            "enabled"));
        if (this.enabled) {
            if (parsedOptions.get("proxyServer") == null) {
                System.out.println(
                    "Please provide a value for proxyServer option");
                System.exit(0);
            }
            if (parsedOptions.get("proxyPort") == null) {
                System.out.println(
                    "Please provide a value for proxyPort option");
                System.exit(0);
            }
            this.proxyServer = (String) parsedOptions.get("proxyServer");
            this.port = Long.parseLong((String) parsedOptions.get("proxyPort"));
            if (parsedOptions.get("testHost") != null) {
                this.testHost = (String) parsedOptions.get("testHost");
            } else {
                System.out.println("Value for testHost is not provided, "
                                   + "defaulting to www.google.com");
                this.testHost = "www.google.com";
            }
            if (parsedOptions.get("proxyUsername") != null) {
                this.proxyUsername = (String) parsedOptions.get(
                    "proxyUsername");
                if (parsedOptions.get("proxyPassword") == null) {
                    System.out.println(
                        "Please provide a value for proxyPassword option");
                    System.exit(0);
                } else {
                    this.proxyPassword = (String) parsedOptions.get(
                        "proxyPassword");
                }
            }
        }
    }
}
