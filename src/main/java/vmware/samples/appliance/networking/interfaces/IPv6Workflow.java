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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.cli.Option;

import com.vmware.appliance.networking.interfaces.Ipv6;
import com.vmware.appliance.networking.interfaces.Ipv6Types.Address;
import com.vmware.appliance.networking.interfaces.Ipv6Types.AddressInfo;
import com.vmware.appliance.networking.interfaces.Ipv6Types.Config;
import com.vmware.appliance.networking.interfaces.Ipv6Types.Info;

import vmware.samples.appliance.helpers.NetworkingHelper;
import vmware.samples.common.SamplesAbstractBase;

/**
 * Description:
 * 1. Demonstrates getting IPv6 information for specific nic
 * 2. Demonstrates setting DHCP/STATIC IPv6 for specific nic
 *
 * Author: VMware, Inc. Sample Prerequisites: vCenter 6.7+
 */
public class IPv6Workflow extends SamplesAbstractBase {
    private Ipv6 ipv6Service;
    private String nic;
    private boolean autoconf;
    private boolean dhcp;
    private List<Address> addresses = new ArrayList<Address>();
    private Map<String, String> addressPrefixMap =
            new HashMap<String, String>();
    private String defaultGateway = "";

    // Initial Values
    private boolean initialDhcp;
    private boolean initialAutoconf;
    private List<Address> initialAddresses;
    private String initialDefaultGateway;

    protected void setup() throws Exception {
        this.ipv6Service = vapiAuthHelper.getStubFactory().createStub(
            Ipv6.class,
            sessionStubConfig);
        Info ipv6Info = ipv6Service.get(nic);
        initialDhcp = ipv6Info.getDhcp();
        initialAutoconf = ipv6Info.getAutoconf();
        if (!initialDhcp) {
            List<AddressInfo> addressInfo = ipv6Info.getAddresses();
            for (AddressInfo adrsInfo : addressInfo) {
                Address addr = new Address();
                addr.setAddress(adrsInfo.getAddress());
                addr.setPrefix(adrsInfo.getPrefix());
                initialAddresses.add(addr);
            }
            initialDefaultGateway = ipv6Info.getDefaultGateway();
        } else {
            initialAddresses = new ArrayList<Address>();
            initialDefaultGateway = "";
        }
    }

    protected void run() throws Exception {
        Config cfg = new Config();

        // Set IPv6 address
        System.out.println("Setting " + nic + " IPv6 configuration...");
        cfg.setAutoconf(autoconf);
        cfg.setDhcp(dhcp);
        for (Entry<String, String> addressEntry : addressPrefixMap.entrySet()) {
            Address addr = new Address();
            addr.setAddress(addressEntry.getKey());
            addr.setPrefix(Long.parseLong(addressEntry.getValue()));
            addresses.add(addr);
        }
        cfg.setAddresses(addresses);
        cfg.setDefaultGateway(defaultGateway);

        ipv6Service.set(nic, cfg);
        System.out.println();

        // Get and display IPv6 address
        System.out.println("IPv6 Information for nic : " + nic);
        System.out.println("--------------------------------");
        Info ipv6Info = ipv6Service.get(nic);
        NetworkingHelper.printIPv6Info(ipv6Info);
    }

    protected void cleanup() throws Exception {
        System.out.println("\nCleaning up IPv6 Configuration...");
        Config cfg = new Config();
        cfg.setAutoconf(initialAutoconf);
        cfg.setDhcp(initialDhcp);
        cfg.setAddresses(initialAddresses);
        cfg.setDefaultGateway(initialDefaultGateway);
        ipv6Service.set(nic, cfg);
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
        new IPv6Workflow().execute(args);
    }

    protected void parseArgs(String[] args) {
        Option nicOption = Option.builder()
            .longOpt("nic")
            .desc("REQUIRED: Specify the interface name.")
            .argName("INTERFACE_NAME")
            .required(true)
            .hasArgs()
            .build();
        Option dhcpOption = Option.builder()
            .longOpt("enableDhcp")
            .desc("OPTIONAL: Specify this option to enable DHCP")
            .argName("DHCP")
            .required(false)
            .type(Boolean.class)
            .build();
        Option autoConfOption = Option.builder()
            .longOpt("enableAutoconf")
            .desc("OPTIONAL: Specify this option to enable Autoconf")
            .argName("AUTOCONF")
            .required(false)
            .type(Boolean.class)
            .build();
        Option addressOption = Option.builder()
            .longOpt("addresses")
            .desc("OPTIONAL: Specify the IPv6 address as comma separated value"
                  + " and specify address as address-prefix format ex: <IPv6 "
                  + "address>-<prefix> if dhcp is false")
            .argName("IPv6_ADDRESSES")
            .required(false)
            .hasArgs()
            .build();
        Option gatewayOption = Option.builder()
            .longOpt("defaultGateway")
            .desc("OPTIONAL: Specify the default gateway.")
            .argName("IPv6_DEFAULT_GATEWAY")
            .required(false)
            .hasArgs()
            .build();
        List<Option> optionList = Arrays.asList(nicOption,
            dhcpOption,
            addressOption,
            autoConfOption,
            gatewayOption);
        super.parseArgs(optionList, args);
        this.nic = (String) parsedOptions.get("nic");
        if (parsedOptions.get("enableAutoconf") != null) {
            this.autoconf = (Boolean) parsedOptions.get("enableAutoconf");
        } else {
            System.out.println(
                "Autoconf option is not specified, hence defaulting to false");
            this.autoconf = false;
        }
        if (parsedOptions.get("enableDhcp") != null) {
            this.dhcp = (Boolean) parsedOptions.get("enableDhcp");
        } else {
            this.dhcp = false;
        }
        if (!dhcp) {
            if (parsedOptions.get("addresses") == null || parsedOptions.get(
                "defaultGateway") == null) {
                System.out.println("Values for addresses and defaultGateway "
                                   + "options has to be provided, if dhcp"
                                   + " option is not specified");
                System.exit(0);
            }
            String[] addr = ((String) parsedOptions.get("addresses")).split(
                ",");
            for (String adrs : addr) {
                String[] addressPrefix = adrs.split("-");
                addressPrefixMap.put(addressPrefix[0], addressPrefix[1]);
            }
            this.defaultGateway = (String) parsedOptions.get("defaultGateway");
        }
    }
}
