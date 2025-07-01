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
package vmware.samples.appliance.networking.interfaces;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.Option;

import com.vmware.appliance.networking.interfaces.Ipv4;
import com.vmware.appliance.networking.interfaces.Ipv4Types.Config;
import com.vmware.appliance.networking.interfaces.Ipv4Types.Info;
import com.vmware.appliance.networking.interfaces.Ipv4Types.Mode;

import vmware.samples.appliance.helpers.NetworkingHelper;
import vmware.samples.common.SamplesAbstractBase;

/**
 * Description:
 * 1. Demonstrates getting IPv4 information for specific nic
 * 2. Demonstrates setting DHCP/STATIC IPv4 for specific nic
 *
 * Author: VMware, Inc. Sample Prerequisites: vCenter 6.7+
 */
public class IPv4Workflow extends SamplesAbstractBase {
    private Ipv4 ipv4Service;
    private String nic;
    private Mode mode;
    private String address;
    private String defaultGateway;
    private long prefix;
    private Mode initialMode;
    private String initialAddress;
    private long initialPrefix;
    private String initialDefaultGateway;

    protected void setup() throws Exception {
        this.ipv4Service = vapiAuthHelper.getStubFactory().createStub(
            Ipv4.class,
            sessionStubConfig);
        Info ipv4Info = ipv4Service.get(nic);
        initialMode = ipv4Info.getMode();
        if (initialMode == Mode.STATIC) {
            initialAddress = ipv4Info.getAddress();
            initialDefaultGateway = ipv4Info.getDefaultGateway();
            initialPrefix = ipv4Info.getPrefix();
        }
    }

    protected void run() throws Exception {
        Config cfg = new Config();
        // Set IP address
        cfg.setMode(this.mode);
        if (mode.equals(Mode.STATIC)) {
            cfg.setAddress(address);
            cfg.setDefaultGateway(defaultGateway);
            cfg.setPrefix(prefix);
        }
        System.out.println("Setting IPv4 address for nic : " + nic
                           + " with mode : " + mode);
        ipv4Service.set(nic, cfg);

        // Get IP address
        System.out.println();
        System.out.println("IPv4 Address information for nic : " + nic);
        System.out.println("----------------------------------------");
        Info ipv4Info = ipv4Service.get(nic);
        NetworkingHelper.printIPv4Info(ipv4Info);
    }

    protected void cleanup() throws Exception {
        System.out.println("\nCleaning up IPv4 Configuration...");
        Config cfg = new Config();
        cfg.setMode(initialMode);
        if (initialMode == Mode.STATIC) {
            cfg.setAddress(initialAddress);
            cfg.setPrefix(initialPrefix);
            cfg.setDefaultGateway(initialDefaultGateway);
        }
        ipv4Service.set(nic, cfg);
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
        new IPv4Workflow().execute(args);
    }

    protected void parseArgs(String[] args) {
        Option nicOption = Option.builder()
            .longOpt("nic")
            .desc("REQUIRED: Specify the interface name.")
            .argName("INTERFACE_NAME")
            .required(true)
            .hasArgs()
            .build();
        Option modeOption = Option.builder()
            .longOpt("mode")
            .desc(
                "OPTIONAL: Specify the IPv4 mode (DHCP, STATIC, UNCONFIGURED)")
            .argName("IPv4_MODE")
            .required(false)
            .hasArgs()
            .build();
        Option addressOption = Option.builder()
            .longOpt("address")
            .desc("OPTIONAL: Specify the IP address to set if mode is Static")
            .argName("IPv4_ADDRESS")
            .required(false)
            .hasArgs()
            .build();
        Option prefixOption = Option.builder()
            .longOpt("prefix")
            .desc("OPTIONAL: Specify the prefix to set if mode is Static")
            .argName("IPv4_PREFIX")
            .required(false)
            .hasArgs()
            .build();
        Option gatewayOption = Option.builder()
            .longOpt("defaultGateway")
            .desc("OPTIONAL: Specify the Default gateway to set if mode is "
                  + "Static")
            .argName("IPv4_DEFAULT_GATEWAY")
            .required(false)
            .hasArgs()
            .build();
        List<Option> optionList = Arrays.asList(nicOption,
            modeOption,
            addressOption,
            prefixOption,
            gatewayOption);
        super.parseArgs(optionList, args);
        this.nic = (String) parsedOptions.get("nic");
        if (parsedOptions.get("mode") != null) {
            String ipMode = (String) parsedOptions.get("mode");
            this.mode = Mode.valueOf(ipMode.toUpperCase());
        } else {
            System.out.println("Value for option mode is not provided, hence "
                               + "defaulting to DHCP");
            this.mode = Mode.DHCP;
        }
        if (this.mode.equals(Mode.STATIC)) {
            if (parsedOptions.get("address") == null || parsedOptions.get(
                "defaultGateway") == null || parsedOptions.get(
                    "prefix") == null) {
                System.out.println("Please provide values for options server, "
                                   + "defaultGateway and prefix when mode is "
                                   + "STATIC");
                System.exit(0);
            }
            this.address = (String) parsedOptions.get("address");
            this.defaultGateway = (String) parsedOptions.get("defaultGateway");
            this.prefix = Long.parseLong((String) parsedOptions.get("prefix"));
        }
    }
}
