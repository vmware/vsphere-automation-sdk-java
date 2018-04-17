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
import com.vmware.vmc.model.AddressFWSourceDestination;
import com.vmware.vmc.model.Application;
import com.vmware.vmc.model.EdgeSummary;
import com.vmware.vmc.model.FirewallRules;
import com.vmware.vmc.model.Nsxfirewallrule;
import com.vmware.vmc.model.Nsxfirewallservice;
import com.vmware.vmc.model.Sddc;
import com.vmware.vmc.orgs.Sddcs;
import com.vmware.vmc.orgs.sddcs.networks.Edges;
import com.vmware.vmc.orgs.sddcs.networks.edges.firewall.Config;
import com.vmware.vmc.orgs.sddcs.networks.edges.firewall.config.Rules;

import vmware.samples.common.VmcSamplesAbstractBase;
import vmware.samples.common.authentication.VmcAuthenticationHelper;

/**
 * Description: Demonstrates firewall rule CRUD operations.
 *
 * Author: VMware, Inc.
 * Sample Prerequisites:
 *   - An organization associated with the calling user.
 *   - A SDDC in the organization
 */
public class FirewallRulesCrud extends VmcSamplesAbstractBase {
    private String orgId, sddcId, edgeId;
    private String ruleName;
    private boolean useComputeGateway;
    private Edges edges;
    private Sddcs sddcs;
    private Rules rules;
    private Config firewallConfig;
    private Nsxfirewallrule firewallRule;
    long ruleId;

    private static final String DEFAULT_FIREWALL_RULE_NAME = "Sample Firewall Rule";

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
        Option ruleNameOption = Option.builder()
                .longOpt("rule_name")
                .desc("OPTIONAL: Name of the new firewall rule")
                .argName("FIREWALL RULE")
                .required(false)
                .hasArg()
                .build();
        Option useComputeGatewayOption = Option.builder()
                .longOpt("use_compute_gateway")
                .desc("OPTIONAL: Use compute gateway. Default is using management gateway")
                .argName("USE COMPUTE GATEWAY")
                .required(false)
                .type(Boolean.class)
                .build();
        List<Option> optionList = Arrays.asList(orgOption, sddcOption, ruleNameOption, useComputeGatewayOption);

        super.parseArgs(optionList, args);
        this.orgId = (String) parsedOptions.get("org_id");
        this.sddcId = (String) parsedOptions.get("sddc_id");
        this.ruleName = (String) parsedOptions.get("rule_name");
        if(this.ruleName == null) {
          this.ruleName = DEFAULT_FIREWALL_RULE_NAME;
        }

        Object useComputeGwObj = parsedOptions.get("use_compute_gateway");
        if (useComputeGwObj != null) {
            this.useComputeGateway = (Boolean) useComputeGwObj;
        } else {
            this.useComputeGateway = false;
        }
    }

    @Override
    protected void setup() throws Exception {
        this.vmcAuthHelper = new VmcAuthenticationHelper();
        ApiClient vmcClient =
                this.vmcAuthHelper.newVmcClient(this.vmcServer,
                        this.cspServer, this.refreshToken);
        this.edges = vmcClient.createStub(Edges.class);
        this.sddcs = vmcClient.createStub(Sddcs.class);
        this.rules = vmcClient.createStub(Rules.class);
        this.firewallConfig = vmcClient.createStub(Config.class);
    }

    @Override
    protected void run() throws Exception {
        createFirewallRule();
        getFirewallRule();
        updateFirewallRule();
    }

    private void createFirewallRule() {
        System.out.println("\n\n### Example: Add a firewall rule\n");
        List<EdgeSummary> edgeList =
                this.edges.get(orgId, sddcId, "gatewayServices", null, null, null, null, null, null, null)
                    .getEdgePage().getData();
        System.out.printf("Management Gateway ID: %s\n", edgeList.get(0).getId());
        System.out.printf("Compute Gateway ID: %s\n", edgeList.get(1).getId());

        this.edgeId = useComputeGateway ? edgeList.get(1).getId() : edgeList.get(0).getId();

        System.out.printf("Using edge \"%s\" to create new firewall rule\n", this.edgeId);
        Sddc sddc = this.sddcs.get(this.orgId, this.sddcId);
        List<String> destIpAddresses =
                Arrays.asList(sddc.getResourceConfig().getVcPublicIp(), sddc.getResourceConfig().getVcManagementIp());
        this.firewallRule =
                new Nsxfirewallrule.Builder()
                .setRuleType("user")
                .setName(this.ruleName)
                .setEnabled(true)
                .setAction("accept")
                .setSource(
                        new AddressFWSourceDestination.Builder()
                        .setExclude(false)
                        .setIpAddress(Collections.singletonList("any"))
                        .build())
                .setDestination(
                        new AddressFWSourceDestination.Builder()
                        .setExclude(false)
                        .setIpAddress(destIpAddresses)
                        .build())
                .setApplication(
                        new Application.Builder()
                        .setApplicationId(Collections.<String>emptyList())
                        .setService(Collections.singletonList(
                                        new Nsxfirewallservice.Builder()
                                        .setSourcePort(Collections.singletonList("443"))
                                        .setProtocol("TCP")
                                        .setIcmpType("")
                                        .build()))
                        .build())
                .build();
        rules.add(this.orgId, this.sddcId, this.edgeId,
                  new FirewallRules.Builder()
                  .setFirewallRules(Collections.singletonList(this.firewallRule))
                  .build());
        System.out.printf("\nNew firewall rule \"%s\" is added", this.ruleName);
    }

    private void getFirewallRule() {
        System.out.println("\n\n#### Example: List all firewall rules");
        FirewallRules rules = firewallConfig.get(this.orgId, this.sddcId, this.edgeId).getFirewallRules();
        List<Nsxfirewallrule> nsxFirewallRulesList = rules.getFirewallRules();
        for(Nsxfirewallrule rule : nsxFirewallRulesList) {
            System.out.printf("\nName: %s, Description: %s", rule.getName(), rule.getDescription());
        }

        System.out.println("\n\n#### Example: Find the firewall rule that was created");
        for(Nsxfirewallrule nsxRule : nsxFirewallRulesList) {
            if(nsxRule.getName().equals(this.ruleName)) {
                System.out.printf("\nFound NSX Firwall rule with name %s", nsxRule.getName());
                this.ruleId = nsxRule.getRuleId();
                System.out.printf("\nFirewall Rule Specs for rule \"%s\"\n", this.ruleName);
                System.out.println(this.rules.get(this.orgId, this.sddcId, this.edgeId, this.ruleId));
                break;
            }
        }
    }

    private void updateFirewallRule() {
        System.out.println("\n\n#### Example: Update firewall rule");
        String updatedDescription = "Updated description";
        String updatedRuleName = "Sample Firewall Rule - Updated";
        String updatedAction = "deny";
        String updatedSourceIp = "127.0.0.1";

        this.firewallRule.setDescription(updatedDescription);
        this.firewallRule.setName(updatedRuleName);
        this.firewallRule.setAction(updatedAction);
        this.firewallRule.getSource().setIpAddress(Collections.singletonList(updatedSourceIp));
        rules.update(this.orgId, this.sddcId, this.edgeId, this.ruleId, this.firewallRule);

        System.out.printf("\nFirewall rule has been updated. Specs of the updated rule is:\n",
            this.firewallRule.getName());
        Nsxfirewallrule updatedRule = this.rules.get(this.orgId, this.sddcId, this.edgeId, this.ruleId);
        System.out.println(updatedRule);
    }

    private void deleteFirewallRule() {
        this.rules.delete(this.orgId, this.sddcId, this.edgeId, this.ruleId);
        System.out.printf("\nFirewall rule \"%s\" deleted", this.ruleName);
    }

    @Override
    protected void cleanup() throws Exception {
        if(this.ruleId !=0) {
            System.out.println("\n#### Example: Delete a firewall rule");
            deleteFirewallRule();
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
        new FirewallRulesCrud().execute(args);
    }
}
