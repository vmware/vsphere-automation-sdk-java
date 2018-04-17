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
import java.util.Collections;
import java.util.List;

import org.apache.commons.cli.Option;

import com.vmware.vapi.client.ApiClient;
import com.vmware.vmc.model.EdgeSummary;
import com.vmware.vmc.model.SddcNetwork;
import com.vmware.vmc.model.SddcNetworkAddressGroup;
import com.vmware.vmc.model.SddcNetworkAddressGroups;
import com.vmware.vmc.model.SddcNetworkDhcpConfig;
import com.vmware.vmc.model.SddcNetworkDhcpIpPool;
import com.vmware.vmc.orgs.sddcs.networks.Edges;
import com.vmware.vmc.orgs.sddcs.networks.Logical;

import vmware.samples.common.VmcSamplesAbstractBase;
import vmware.samples.common.authentication.VmcAuthenticationHelper;

/**
 * Description: Demonstrates how to create/delete a logical network.
 *
 * Author: VMware, Inc.
 * Sample Prerequisites:
 *   - An organization associated with the calling user.
 *   - A SDDC in the organization
 */
public class LogicalNetworkCrud extends VmcSamplesAbstractBase {
    private String orgId, sddcId, edgeId, networkId, networkName, primaryAddress, prefixLength, dhcpRange;
    private Edges edges;
    private Logical logicalNetwork;

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
        Option networkNameOption = Option.builder()
                .longOpt("network_name")
                .desc("Name of the new logical network")
                .argName("NETWORK NAME")
                .required(true)
                .hasArg()
                .build();
        Option subnetOption = Option.builder()
                .longOpt("subnet")
                .desc("Specify subnet of the logical network e.g. 192.168.52.1/24 ")
                .argName("LOGICAL NETWORK SUBNET")
                .required(true)
                .hasArg()
                .build();
        Option dhcpRangeOption = Option.builder()
                .longOpt("dhcp_range")
                .desc("Specify the DHCP IP range for the logical network e.g.192.168.52.2-192.168.52.10")
                .argName("DHCP RANGE")
                .required(true)
                .hasArg()
                .build();
        List<Option> optionList =
            Arrays.asList(orgOption, sddcOption, networkNameOption, subnetOption, dhcpRangeOption);

        super.parseArgs(optionList, args);
        this.orgId = (String) parsedOptions.get("org_id");
        this.sddcId = (String) parsedOptions.get("sddc_id");
        this.networkName = (String) parsedOptions.get("network_name");
        this.primaryAddress = ((String) parsedOptions.get("subnet")).split("/")[0];
        this.prefixLength = ((String) parsedOptions.get("subnet")).split("/")[1];
        this.dhcpRange = (String) parsedOptions.get("dhcp_range");
    }

    @Override
    protected void setup() throws Exception {
        this.vmcAuthHelper = new VmcAuthenticationHelper();
        ApiClient vmcClient =
                this.vmcAuthHelper.newVmcClient(this.vmcServer,
                        this.cspServer, this.refreshToken);
        this.edges = vmcClient.createStub(Edges.class);
        this.logicalNetwork = vmcClient.createStub(Logical.class);
    }

    @Override
    protected void run() throws Exception {
        createLogicalNetwork();
        getLogicalNetwork();
    }

    private void createLogicalNetwork() {
        System.out.println("\n#### Example: Add a logical network to compute gateway");
        List<EdgeSummary> edgeList =
                this.edges.get(this.orgId, this.sddcId, "gatewayServices", null, null, null, null, null, null, null)
                    .getEdgePage().getData();
        this.edgeId = edgeList.get(1).getId();
        List<SddcNetworkDhcpIpPool> dhcpPool =
                Collections.singletonList(
                        new SddcNetworkDhcpIpPool.Builder()
                        .setIpRange(this.dhcpRange)
                        .build());
        SddcNetwork network =
                new SddcNetwork.Builder(this.networkName, this.edgeId)
                .setCgwName(this.networkName)
                .setDhcpConfigs(new SddcNetworkDhcpConfig.Builder().setIpPools(dhcpPool).build())
                .setSubnets(new SddcNetworkAddressGroups.Builder()
                            .setAddressGroups(Collections.singletonList(
                                    new SddcNetworkAddressGroup.Builder()
                                    .setPrefixLength(this.prefixLength)
                                    .setPrimaryAddress(this.primaryAddress)
                                    .build()))
                            .build())
                .build();
        this.logicalNetwork.create(this.orgId, this.sddcId, network);
        System.out.printf("\nNew logical network %s added", this.networkName);
    }

    private void getLogicalNetwork() {
        System.out.println("\n#### Example: Display the Logical Network Specs");
        List<SddcNetwork> networks =
                this.logicalNetwork.get0(this.orgId, this.sddcId, null, null, null, null).getData();
        for(SddcNetwork network : networks) {
            if(network.getName().equals(this.networkName)) {
                this.networkId = network.getId();
                break;
            }
        }

        SddcNetwork sddcNetwork = this.logicalNetwork.get(this.orgId, this.sddcId, this.networkId);
        System.out.println(sddcNetwork);
    }

    private void deleteLogicalNetwork() {
        this.logicalNetwork.delete(this.orgId, this.sddcId, this.networkId);
    }

    @Override
    protected void cleanup() throws Exception {
        if(this.networkId != null) {
            System.out.println("\n#### Example: Delete the Logical Network");
            deleteLogicalNetwork();
        }
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
        new LogicalNetworkCrud().execute(args);
    }
}
