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
package vmware.samples.appliance.networking;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.Option;

import com.vmware.appliance.Networking;
import com.vmware.appliance.NetworkingTypes.Info;
import com.vmware.appliance.NetworkingTypes.UpdateSpec;

import vmware.samples.appliance.helpers.NetworkingHelper;
import vmware.samples.common.SamplesAbstractBase;

/**
 * Description:
 * 1. Demonstrates getting network information for all interfaces
 * 2. Demonstrates Enabling/Disabling IPv6 on all interfaces
 * 3. Demonstrates resetting and refreshing network
 *
 * Author: VMware, Inc. Sample Prerequisites: vCenter 6.7+
 */
public class NetworkingWorkflow extends SamplesAbstractBase {
    private Networking networkingService;
    private boolean enableIPv6;
    private boolean reset;
    private boolean initialIPv6Config;

    protected void setup() throws Exception {
        this.networkingService = vapiAuthHelper.getStubFactory().createStub(
            Networking.class,
            sessionStubConfig);
    }

    protected void run() throws Exception {
        Info networkInfo = networkingService.get();
        NetworkingHelper.printNetworkInfo(networkInfo);
        System.out.println();

        if (networkInfo.getInterfaces()
            .entrySet()
            .iterator()
            .next()
            .getValue()
            .getIpv6() != null) {
            initialIPv6Config = true;
        }

        UpdateSpec spec = new UpdateSpec();
        spec.setIpv6Enabled(this.enableIPv6);
        if (this.enableIPv6) {
            System.out.println("Enabling IPv6...");
        } else {
            System.out.println("Disabling IPv6...");
        }
        System.out.println();

        networkingService.update(spec);

        // Display the network information
        networkInfo = networkingService.get();
        NetworkingHelper.printNetworkInfo(networkInfo);

        if (this.reset) {
            System.out.println();
            System.out.println("Refreshing network configuration...");
            System.out.println();
            networkingService.reset();
            networkInfo = networkingService.get();
            NetworkingHelper.printNetworkInfo(networkInfo);
        }
    }

    protected void cleanup() throws Exception {
        System.out.println();
        System.out.println("Cleaning up IPv6 configuration...");
        System.out.println();
        UpdateSpec spec = new UpdateSpec();
        spec.setIpv6Enabled(initialIPv6Config);
        networkingService.update(spec);
        Info networkInfo = networkingService.get();
        NetworkingHelper.printNetworkInfo(networkInfo);
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
        new NetworkingWorkflow().execute(args);
    }

    protected void parseArgs(String[] args) {
        Option ipv6Option = Option.builder()
            .longOpt("enableIPv6")
            .desc("REQUIRED: Specify this option if you want to enable IPv6")
            .argName("IPV6_ENABLE")
            .required(false)
            .type(Boolean.class)
            .build();
        Option resetOption = Option.builder()
            .longOpt("reset")
            .desc("REQUIRED: Specify this option if you want to reset and "
                  + "refresh the network")
            .argName("RESET")
            .required(false)
            .type(Boolean.class)
            .build();
        List<Option> optionList = Arrays.asList(ipv6Option, resetOption);
        super.parseArgs(optionList, args);
        Object enableIPv6Option = parsedOptions.get("enableIPv6");
        if (enableIPv6Option != null) {
            this.enableIPv6 = (Boolean) enableIPv6Option;
        } else {
            this.enableIPv6 = false;
        }
        Object resetNetworkOption = parsedOptions.get("reset");
        if (resetNetworkOption != null) {
            this.reset = (Boolean) resetNetworkOption;
        } else {
            this.reset = false;
        }
    }
}
