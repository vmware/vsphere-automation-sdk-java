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
import com.vmware.vmc.model.NatRules;
import com.vmware.vmc.model.Nsxnatrule;
import com.vmware.vmc.orgs.sddcs.networks.Edges;
import com.vmware.vmc.orgs.sddcs.networks.edges.nat.Config;
import com.vmware.vmc.orgs.sddcs.networks.edges.nat.config.Rules;

import vmware.samples.common.VmcSamplesAbstractBase;
import vmware.samples.common.authentication.VmcAuthenticationHelper;

/**
 * Description: Demonstrates NAT Rule CRUD operations.
 *
 * Author: VMware, Inc.
 * Sample Prerequisites:
 *   - An organization associated with the calling user.
 *   - A SDDC in the organization
 */
public class NatRuleCrud extends VmcSamplesAbstractBase {
    private static final String DEFAULT_NAT_RULE_DESCRIPTION = "Sample NAT RULE";

    private String orgId, sddcId, publicIp, internalIp, ruleDesc, edgeId;
    private Edges edges;
    private Rules rules;
    private Config natRuleConfig;
    private long ruleId;

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
        Option publicIpOption = Option.builder()
                .longOpt("public_ip")
                .desc("Specify the public IP range for the NAT Rule")
                .argName("PUBLIC IP RANGE")
                .required(true)
                .hasArg()
                .build();
        Option internalIpOption = Option.builder()
                .longOpt("internal_ip")
                .desc("Specify the NAT Rule subnet. e.g. 192.168.200.1/24")
                .argName("NAT RULE SUBNET")
                .required(true)
                .hasArg()
                .build();
        Option ruleDescriptionOption = Option.builder()
                .longOpt("rule_description")
                .desc("OPTIONAL: Specify the description of the NAT Rule")
                .argName("RULE DESCRIPTION")
                .required(false)
                .hasArg()
                .build();
        List<Option> optionList = Arrays.asList(orgOption, sddcOption, publicIpOption, internalIpOption,
                ruleDescriptionOption);

        super.parseArgs(optionList, args);
        this.orgId = (String) parsedOptions.get("org_id");
        this.sddcId = (String) parsedOptions.get("sddc_id");
        this.publicIp = (String) parsedOptions.get("public_ip");
        this.internalIp = (String) parsedOptions.get("internal_ip");
        this.ruleDesc = (String) parsedOptions.get("rule_description");
        if(this.ruleDesc == null) {
          this.ruleDesc = DEFAULT_NAT_RULE_DESCRIPTION;
        }
    }

    @Override
    protected void setup() throws Exception {
        this.vmcAuthHelper = new VmcAuthenticationHelper();
        ApiClient vmcClient =
                this.vmcAuthHelper.newVmcClient(this.vmcServer,
                        this.cspServer, this.refreshToken);
        this.edges = vmcClient.createStub(Edges.class);
        this.rules = vmcClient.createStub(Rules.class);
        this.natRuleConfig = vmcClient.createStub(Config.class);
    }

    @Override
    protected void run() throws Exception {
        createNatRule();
        getNatRule();
        updateNatRule();
    }

    private void createNatRule() {
        System.out.println("\n#### Example: Add a NAT Rule to the Compute Gateway");
        Nsxnatrule nsxNatRule =
                new Nsxnatrule.Builder()
                .setVnic("0")
                .setRuleType("user")
                .setAction("dnat")
                .setProtocol("tcp")
                .setDescription(this.ruleDesc)
                .setOriginalAddress(this.publicIp)
                .setOriginalPort("443")
                .setTranslatedAddress(this.internalIp)
                .setTranslatedPort("443")
                .setEnabled(true)
                .build();
        List<EdgeSummary> edges =
                this.edges.get(this.orgId, this.sddcId, "gatewayServices", null, null, null, null, null, null, null)
                  .getEdgePage().getData();
        this.edgeId = edges.get(1).getId();
        rules.add(this.orgId, this.sddcId, this.edgeId,
                new NatRules.Builder()
                .setNatRulesDtos(Collections.singletonList(nsxNatRule))
                .build());
        System.out.printf("\nNew NAT Rule with description \"%s\" added to the Compute Gateway",
                nsxNatRule.getDescription());
    }

    private void getNatRule() {
        System.out.println("\n#### Example: Display all the NAT Rules");
        List<Nsxnatrule> natRules =
                this.natRuleConfig.get(this.orgId, this.sddcId, this.edgeId).getRules().getNatRulesDtos();
        for(Nsxnatrule rule : natRules) {
            System.out.printf("\nDescription: %s\tOriginal Address=%s\t"
                    + "OriginalPor=%s\tTranslated Address=%s\tTranslated Port=%s",
                    rule.getDescription(), rule.getOriginalAddress(), rule.getOriginalPort(),
                    rule.getTranslatedAddress(), rule.getTranslatedPort());
        }

        System.out.println("\n\n#### Example: Find the NAT Rule that was created");
        for(Nsxnatrule rule : natRules) {
            if(rule.getDescription().equals(this.ruleDesc)) {
                System.out.printf("\nFound NAT Rule with description %s", rule.getDescription());
                this.ruleId = rule.getRuleId();
                System.out.printf("\nNAT Rule Specs for rule \"%s\"\n", rule.getDescription());
                System.out.println(rule);
                break;
            }
        }
    }

    private void updateNatRule() {
        System.out.println("\n#### Example: Update the NAT Rule");
        Nsxnatrule updatedNatRule =
                new Nsxnatrule.Builder()
                .setVnic("0")
                .setRuleType("user")
                .setAction("dnat")
                .setProtocol("tcp")
                .setDescription("Updated Description")
                .setOriginalAddress(this.publicIp)
                .setOriginalPort("any")
                .setTranslatedAddress(this.internalIp)
                .setTranslatedPort("443")
                .setEnabled(true)
                .build();
        this.rules.update(this.orgId, this.sddcId, this.edgeId, this.ruleId, updatedNatRule);
        System.out.printf("\nNAT Rule \"ruleId=%s\" updated\n", this.ruleId);
    }

    private void deleteNatRule() {
        this.rules.delete(this.orgId, this.sddcId, this.edgeId, this.ruleId);
        System.out.printf("\nNAT Rule \"ruleId=%s\" deleted", this.ruleId);
    }

    @Override
    protected void cleanup() throws Exception {
        if(this.ruleId != 0) {
            System.out.println("\n#### Example: Delete the NAT Rule");
            deleteNatRule();
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
        new NatRuleCrud().execute(args);
    }
}
