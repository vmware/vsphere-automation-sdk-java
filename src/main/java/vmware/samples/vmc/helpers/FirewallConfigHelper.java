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
package vmware.samples.vmc.helpers;

import java.util.Collections;
import java.util.List;

import com.vmware.vapi.client.ApiClient;
import com.vmware.vmc.model.AddressFWSourceDestination;
import com.vmware.vmc.model.EdgeSummary;
import com.vmware.vmc.model.FirewallRules;
import com.vmware.vmc.model.Nsxfirewallrule;
import com.vmware.vmc.orgs.sddcs.networks.Edges;
import com.vmware.vmc.orgs.sddcs.networks.edges.firewall.Config;
import com.vmware.vmc.orgs.sddcs.networks.edges.firewall.config.Rules;

/**
 * Helper class for firewall configuration operations.
 * @author VMware, Inc
 */
public class FirewallConfigHelper {
    private String orgId, sddcId;
    private ApiClient vmcClient;
    private Rules firewallRulesStub;
    private Config firewallConfigStub;
    private Edges edgesStub;

    public FirewallConfigHelper(ApiClient vmcClient, String orgId, String sddcId) {
        this.vmcClient = vmcClient;
        this.orgId = orgId;
        this.sddcId = sddcId;
        this.firewallConfigStub = this.vmcClient.createStub(Config.class);
        this.firewallRulesStub = this.vmcClient.createStub(Rules.class);
        this.edgesStub = vmcClient.createStub(Edges.class);
    }

    /**
     * Creates a Firewall Rule which allows any to any traffic.
     *
     * @param ruleName name of the Firewall Rule.
     * @param edgeType type of edge on which the rule should be created.
     */
    public void createAnyToAnyFirewallRule(String ruleName, EdgeType edgeType) {
        // Get the edge id for the edge type
        String edgeId = getEdgeId(edgeType);

        // Create the firewall rule configuration
        Nsxfirewallrule firewallRule =
                new Nsxfirewallrule.Builder()
                .setRuleType("user")
                .setName(ruleName)
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
                        .setIpAddress(Collections.singletonList("any"))
                        .build())
                .setLoggingEnabled(false)
                .build();

        System.out.printf("\nFirewall Rule Spec:");
        System.out.printf("%s", firewallRule);

        // Add the Firewall Rule
        firewallRulesStub.add(this.orgId, this.sddcId, edgeId,
                  new FirewallRules.Builder()
                  .setFirewallRules(Collections.singletonList(firewallRule))
                  .build());
        System.out.printf("\nNew Firewall Rule \"%s\" has been added", firewallRule.getName());
    }

    /**
     * Gets the ruleId of the Firewall Rule with the specified name for the specified edge type.
     *
     * @param firewallRuleName Name of the Firewall Rule.
     * @param edgeType Type of edge on which the rule exists
     * @return ruleId of the Firewall Rule
     * @throws Exception if the Firewall Rule with the specified name is not found on the specified edge type.
     */
    public Long getFirewallRule(String firewallRuleName, EdgeType edgeType) throws Exception {
        System.out.printf("\nSearching for the Firewall Rule with name \"%s\" on edge \"%s\"",
                firewallRuleName, edgeType);
        String edgeId = getEdgeId(edgeType);
        FirewallRules rules = this.firewallConfigStub.get(this.orgId, this.sddcId, edgeId).getFirewallRules();
        List<Nsxfirewallrule> nsxFirewallRulesList = rules.getFirewallRules();

        for(Nsxfirewallrule nsxRule : nsxFirewallRulesList) {
            if(nsxRule.getName().equals(firewallRuleName)) {
                System.out.printf("\nFound Firewall Rule with name %s", nsxRule.getName());
                return nsxRule.getRuleId();
            }
        }
        throw new Exception("Could not find Firewall Rule with name " + firewallRuleName);
    }

    /**
     * Deletes the Firewall Rule with the specified name on the specified edge type.
     *
     * @param firewallRuleName Name of the Firewall rule to be deleted.
     * @param edgeType Type of edge on which the rule exists
     * @throws Exception if the Firewall Rule with the specified name is not found on the specified edge type.
     */
    public void deleteFirewallRule(String firewallRuleName, EdgeType edgeType) throws Exception {
        String edgeId = getEdgeId(edgeType);
        Long firewallRuleId = getFirewallRule(firewallRuleName, edgeType);
        this.firewallRulesStub.delete(this.orgId, this.sddcId, edgeId, firewallRuleId);
        System.out.printf("\nFirewall rule \"%s\" has been deleted", firewallRuleName);
    }

    private String getEdgeId(EdgeType edgeType) {
        List<EdgeSummary> edges =
                this.edgesStub.get(this.orgId, this.sddcId, "gatewayServices",
                        null, null, null, null, null, null, null).getEdgePage().getData();
        return edges.get(EdgeType.COMPUTE_GATEWAY.ordinal()).getId();
    }
}