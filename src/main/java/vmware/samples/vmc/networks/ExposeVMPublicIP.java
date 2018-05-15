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
import com.vmware.vmc.model.SddcAllocatePublicIpSpec;
import com.vmware.vmc.model.SddcPublicIp;
import com.vmware.vmc.model.Task;
import com.vmware.vmc.orgs.sddcs.Publicips;

import vmware.samples.common.VmcSamplesAbstractBase;
import vmware.samples.common.authentication.VmcAuthenticationHelper;
import vmware.samples.vmc.helpers.EdgeType;
import vmware.samples.vmc.helpers.FirewallConfigHelper;
import vmware.samples.vmc.helpers.NatConfigHelper;
import vmware.samples.vmc.helpers.VmcTaskHelper;

/**
 * Description: Demonstrates how to expose a SDDC VM to the public internet
 *
 * Demo steps required to expose a VM to public internet
 * 1. Request a public IP address
 * 2. Add a Firewall Rule on Compute Gateway to access the VM
 * 3. Create a NAT rule to forward traffic from public IP to private IP
 *
 * Sample Prerequisites:
 * A VM deployed inside the SDDC with a private IP address
 *
 * Author: VMware, Inc.
 */
public class ExposeVMPublicIP extends VmcSamplesAbstractBase {
    private ApiClient vmcClient;
    private Publicips publicIpsStub;
    private FirewallConfigHelper firewallConfigHelper;
    private NatConfigHelper natConfigHelper;
    private String orgId, sddcId, internalIp, ipId, publicIp;
    private static final int TASK_POLLING_DELAY_IN_MILLISECONDS = 500;
    private static final String PUBLIC_IP_NOTES = "Sample-Public-IP-java";
    private static final String DEFAULT_NAT_RULE_DESCRIPTION = "Sample NAT Rule - Java";
    private static final String DEFAULT_FIREWALL_RULE_NAME = "Sample Firewall Rule - Java";

    /**
     * Define the options specific to this sample and configure the sample using
     * command-line arguments or a config file
     *
     * @param args command line arguments passed to the sample
     */
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
        Option internalIpOption = Option.builder()
                .longOpt("internal_ip")
                .desc("Private IP of the VM")
                .argName("SDDC ID")
                .required(true)
                .hasArg()
                .build();
        List<Option> optionList = Arrays.asList(orgOption, sddcOption, internalIpOption);

        super.parseArgs(optionList, args);
        this.orgId = (String) parsedOptions.get("org_id");
        this.sddcId = (String) parsedOptions.get("sddc_id");
        this.internalIp = (String) parsedOptions.get("internal_ip");
    }

    protected void setup() throws Exception {
        this.vmcAuthHelper = new VmcAuthenticationHelper();
        this.vmcClient = this.vmcAuthHelper.newVmcClient(this.vmcServer,
        		this.cspServer, this.refreshToken);
        this.publicIpsStub = vmcClient.createStub(Publicips.class);
        this.firewallConfigHelper = new FirewallConfigHelper(this.vmcClient,
        		this.orgId, this.sddcId);
        this.natConfigHelper = new NatConfigHelper(this.vmcClient,
        		this.orgId, this.sddcId);
    }

    protected void run() throws Exception {
        System.out.printf("\n\n#### Step 1: Request a new Public IP Address");
        SddcAllocatePublicIpSpec sddcAllocateSpec =
                new SddcAllocatePublicIpSpec.Builder(1)
                .setNames(Collections.singletonList(PUBLIC_IP_NOTES))
                .build();

        Task publicIpCreateTask = publicIpsStub.create(this.orgId, this.sddcId, sddcAllocateSpec);
        String taskId = publicIpCreateTask.getId();
        System.out.printf("\nPolling the REQUEST PUBLIC IP task (taskId = %s) :", taskId);
        boolean taskCompleted = VmcTaskHelper.pollTask(vmcClient, orgId, taskId,
        		TASK_POLLING_DELAY_IN_MILLISECONDS);
        if(!taskCompleted) {
            System.out.println("REQUEST PUBLIC IP task was either canceled or it failed. Exiting.");
            System.exit(1);
        }

        List<SddcPublicIp> publicipList = publicIpsStub.list(this.orgId, this.sddcId);
        boolean found = false;
        for(SddcPublicIp ip : publicipList) {
            if(ip.getName().equals(PUBLIC_IP_NOTES)) {
                this.ipId = ip.getAllocationId();
                this.publicIp = ip.getPublicIp();
                found = true;
                System.out.printf("Successfully requested a new Public IP Address: %s", this.publicIp);
                break;
            }
        }
        if(!found) {
            throw new Exception("Can't find public IP with notes " + PUBLIC_IP_NOTES);
        }

        System.out.printf("\n\n#### Step 2: Create a new Firewall Rule"
        		+ " which allows any to any traffic");
        this.firewallConfigHelper.createAnyToAnyFirewallRule(DEFAULT_FIREWALL_RULE_NAME,
        		EdgeType.COMPUTE_GATEWAY);

        System.out.printf("\n\n#### Step 3: Create a new NAT Rule to translate "
        		+ "the public IP address to an internal IP address");
        this.natConfigHelper.createNatRule(DEFAULT_NAT_RULE_DESCRIPTION, EdgeType.COMPUTE_GATEWAY,
                this.publicIp, this.internalIp);
    }

    protected void cleanup() throws Exception {
        System.out.printf("\n\n#### Cleanup: Delete Firewall Rule");
        this.firewallConfigHelper.deleteFirewallRule(DEFAULT_FIREWALL_RULE_NAME,
        		EdgeType.COMPUTE_GATEWAY);

        System.out.printf("\n\n#### Cleanup: Delete NAT rule");
        this.natConfigHelper.deleteNatRule(DEFAULT_NAT_RULE_DESCRIPTION, EdgeType.COMPUTE_GATEWAY);

        System.out.printf("\n\n#### Cleanup: Release Public IP");
        this.publicIpsStub.delete(this.orgId, this.sddcId, this.ipId);
        System.out.printf("\nPublic IP Address \"%s\" has been released", this.publicIp);
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
        new ExposeVMPublicIP().execute(args);
    }
}