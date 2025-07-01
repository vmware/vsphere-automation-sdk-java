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

import com.vmware.appliance.networking.Interfaces;
import com.vmware.appliance.networking.InterfacesTypes.InterfaceInfo;

import vmware.samples.appliance.helpers.NetworkingHelper;
import vmware.samples.common.SamplesAbstractBase;

/**
 * Description:
 * 1. Demonstrates getting interface information for all interfaces
 * 2. Demonstrates getting interface information for specific interface
 *
 * Author: VMware, Inc. Sample Prerequisites: vCenter 6.7+
 */
public class InterfacesWorkflow extends SamplesAbstractBase {
    private Interfaces interfacesService;
    private String nic;

    protected void setup() throws Exception {
        this.interfacesService = vapiAuthHelper.getStubFactory().createStub(
            Interfaces.class,
            sessionStubConfig);
    }

    protected void run() throws Exception {
        // List all interfaces information
        System.out.println("Getting all interfaces info...");
        List<InterfaceInfo> interfaceInfo = interfacesService.list();
        for (InterfaceInfo intInfo : interfaceInfo) {
            System.out.println("Interface name : " + intInfo.getName());
            System.out.println("MAC : " + intInfo.getMac());
            System.out.println("Status : " + intInfo.getStatus());
            System.out.println();
            System.out.println("IPv4 Configuration:");
            System.out.println("-------------------");
            NetworkingHelper.printIPv4Info(intInfo.getIpv4());
            System.out.println();
            System.out.println("IPv6 Configuration:");
            System.out.println("-------------------");
            NetworkingHelper.printIPv6Info(intInfo.getIpv6());
        }

        // Get info of specified interface
        System.out.println("Getting information for Nic : " + nic);
        InterfaceInfo intInfo = interfacesService.get(nic);
        System.out.println("Interface name : " + intInfo.getName());
        System.out.println("MAC : " + intInfo.getMac());
        System.out.println("Status : " + intInfo.getStatus());
        System.out.println();
        System.out.println("IPv4 Configuration:");
        System.out.println("-------------------");
        NetworkingHelper.printIPv4Info(intInfo.getIpv4());
        System.out.println();
        System.out.println("IPv6 Configuration:");
        System.out.println("-------------------");
        NetworkingHelper.printIPv6Info(intInfo.getIpv6());
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
        new InterfacesWorkflow().execute(args);
    }

    protected void parseArgs(String[] args) {
        Option nicOption = Option.builder()
            .longOpt("nic")
            .desc("OPTIONAL: Specify the interface name.")
            .argName("INTERFACE_NAME")
            .required(false)
            .hasArgs()
            .build();
        List<Option> optionList = Arrays.asList(nicOption);
        super.parseArgs(optionList, args);
        if (parsedOptions.get("nic") != null) {
            this.nic = (String) parsedOptions.get("nic");
        } else {
            System.out.println("Value for nic option is not specified, hence "
                               + "defaulting to nic0");
            this.nic = "nic0";
        }
    }
}
