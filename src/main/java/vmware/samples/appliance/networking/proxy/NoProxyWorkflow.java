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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.Option;

import com.vmware.appliance.networking.NoProxy;
import vmware.samples.common.SamplesAbstractBase;

/**
 * Description: Demonstrates setting and getting servers that has to be
 * excluded from Proxy
 *
 * Author: VMware, Inc. Sample Prerequisites: vCenter 6.7+
 */
public class NoProxyWorkflow extends SamplesAbstractBase {
    private NoProxy noProxyService;
    private List<String> noProxyServers;
    private List<String> initialServerList;

    protected void setup() throws Exception {
        this.noProxyService = vapiAuthHelper.getStubFactory().createStub(
            NoProxy.class,
            sessionStubConfig);
    }

    protected void run() throws Exception {
        System.out.println("Existing list of servers that should not be "
                           + "accessed via the proxy server...");
        initialServerList = noProxyService.get();
        System.out.println(initialServerList);

        System.out.println("Excluding " + noProxyServers + " from proxy...");
        noProxyService.set(noProxyServers);

        System.out.println("New list of servers that should not be accessed "
                           + "via the proxy server");
        System.out.println(noProxyService.get());
    }

    protected void cleanup() throws Exception {
        System.out.println("\nCleaning up no proxy configuration...");
        if (initialServerList.isEmpty()) {
            noProxyService.set(new ArrayList<String>());
        } else {
            noProxyService.set(initialServerList);
        }
        System.out.println(noProxyService.get());
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
        new NoProxyWorkflow().execute(args);
    }

    protected void parseArgs(String[] args) {
        Option noProxyOption = Option.builder()
            .longOpt("noProxyServers")
            .desc("REQUIRED: Specify the servers to be excluded from proxy "
                  + "(Hostname/IP).")
            .argName("NO_PROXY_SERVERS")
            .required(true)
            .hasArgs()
            .build();
        List<Option> optionList = Arrays.asList(noProxyOption);
        super.parseArgs(optionList, args);
        String noProxy = (String) parsedOptions.get("noProxyServers");
        this.noProxyServers = Arrays.asList(noProxy.split(","));
    }
}
