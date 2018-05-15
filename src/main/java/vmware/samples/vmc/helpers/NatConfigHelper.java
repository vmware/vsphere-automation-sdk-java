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
import com.vmware.vmc.model.EdgeSummary;
import com.vmware.vmc.model.NatRules;
import com.vmware.vmc.model.Nsxnatrule;
import com.vmware.vmc.orgs.sddcs.networks.Edges;
import com.vmware.vmc.orgs.sddcs.networks.edges.nat.Config;
import com.vmware.vmc.orgs.sddcs.networks.edges.nat.config.Rules;

/**
 * Helper class for NAT Rules configuration operations.
 * @author VMware, Inc
 */
public class NatConfigHelper {
    private String orgId, sddcId;
    private ApiClient vmcClient;
    private Rules natRulesStub;
    private Config natRulesConfigStub;
    private Edges edgesStub;

    public NatConfigHelper(ApiClient vmcClient, String orgId, String sddcId) {
        this.vmcClient = vmcClient;
        this.orgId = orgId;
        this.sddcId = sddcId;
        this.natRulesConfigStub = this.vmcClient.createStub(Config.class);
        this.natRulesStub = this.vmcClient.createStub(Rules.class);
        this.edgesStub = vmcClient.createStub(Edges.class);
    }

    /**
     * Create a NAT Rule with the specified name on the specified edge type.
     *
     * @param natRuleName Name of the NAT Rule.
     * @param edgeType type of edge (Compute/Management) on which the NAT rule should be created.
     * @param originalAddress original address for NAT Rule.
     * @param translatedAddress translated address for the NAT Rule.
     */
    public void createNatRule(String natRuleName, EdgeType edgeType, String originalAddress, String translatedAddress) {
        // Get the edge id for the edge type
        String edgeId = getEdgeId(edgeType);

        // Add a NAT rule to the edge
        Nsxnatrule nsxNatRule =
                new Nsxnatrule.Builder()
                .setVnic("0")
                .setRuleType("user")
                .setAction("dnat")
                .setProtocol("any")
                .setDescription(natRuleName)
                .setOriginalAddress(originalAddress)
                .setOriginalPort("any")
                .setTranslatedAddress(translatedAddress)
                .setTranslatedPort("any")
                .setEnabled(true)
                .build();
        System.out.printf("\nNAT Rule Spec:");
        System.out.printf("%s", nsxNatRule);
        this.natRulesStub.add(this.orgId, this.sddcId, edgeId,
                     new NatRules.Builder()
                     .setNatRulesDtos(Collections.singletonList(nsxNatRule))
                     .build());
        System.out.printf("\nNew NAT Rule with description \"%s\" added to %s",
                nsxNatRule.getDescription(), edgeType);
    }

    /**
     * Gets the ruleId of the NAT Rule with the specified name for the specified edge type.
     *
     * @param natRuleName Name of the NAT Rule.
     * @param edgeType Type of edge on which the rule exists
     * @return ruleId of the NAT Rule
     * @throws Exception if the NAT Rule with the specified name is not found on the specified edge type.
     */
    public Long getNatRule(String natRuleName, EdgeType edgeType) throws Exception {
        String edgeId = getEdgeId(edgeType);
        List<Nsxnatrule> natRules =
                this.natRulesConfigStub.get(this.orgId, this.sddcId, edgeId)
                .getRules()
                .getNatRulesDtos();
        System.out.printf("\nSearching for the NAT Rule with name \"%s\" on edge \"%s\"",
                natRuleName, edgeType);
        for(Nsxnatrule rule : natRules) {
            if(rule.getDescription().equals(natRuleName)) {
                System.out.printf("\nFound NAT Rule with description %s", rule.getDescription());
                return rule.getRuleId();
            }
        }

        for(Nsxnatrule rule : natRules) {
            if(rule.getDescription().equals(natRuleName)) {
                System.out.printf("\nFound NAT Rule with description %s", rule.getDescription());
                return rule.getRuleId();
            }
        }
        throw new Exception("Could not find Firewall Rule with description " + natRuleName);
    }

    /**
     * Deletes the NAT rule with the specified name on the specified edge type.
     *
     * @param natRuleName Name of the NAT rule to be deleted.
     * @param edgeType Type of edge on which the rule exists
     * @throws Exception if a NAT rule with matching description is not found on the specified edge
     */
    public void deleteNatRule(String natRuleName, EdgeType edgeType) throws Exception {
        String edgeId = getEdgeId(edgeType);
        Long natRuleId = getNatRule(natRuleName, EdgeType.COMPUTE_GATEWAY);
        this.natRulesStub.delete(this.orgId, this.sddcId, edgeId, natRuleId);
        System.out.printf("\nNAT rule \"%s\" has been deleted", natRuleName);
    }

    private String getEdgeId(EdgeType edgeType) {
        List<EdgeSummary> edges =
                this.edgesStub.get(this.orgId, this.sddcId, "gatewayServices", null, null, null, null, null, null, null)
                  .getEdgePage().getData();
        return edges.get(EdgeType.COMPUTE_GATEWAY.ordinal()).getId();
    }
}
