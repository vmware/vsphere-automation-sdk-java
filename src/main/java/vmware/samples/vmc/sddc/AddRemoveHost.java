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
package vmware.samples.vmc.sddc;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.Option;

import com.vmware.vapi.client.ApiClient;
import com.vmware.vmc.model.EsxConfig;
import com.vmware.vmc.model.Task;
import com.vmware.vmc.orgs.sddcs.Esxs;

import vmware.samples.common.VmcSamplesAbstractBase;
import vmware.samples.common.authentication.VmcAuthenticationHelper;
import vmware.samples.vmc.helpers.VmcTaskHelper;

/**
 * Description: Demonstrates how to add/remove an ESX host in the target cloud.
 *
 * Author: VMware, Inc.
 * Sample Prerequisites: An existing organization and SDDC, VMC Server
 */
public class AddRemoveHost extends VmcSamplesAbstractBase {
    private Esxs esxsStub;
    private ApiClient apiClient;    
    private String orgId, sddcId, numHosts;
    public static final String ADD_HOST_ACTION = "add";
    public static final String REMOVE_HOST_ACTION = "remove";
    public static final int TASK_POLLING_DELAY_IN_MILLISECONDS = 1000;

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
        Option numHostsOption = Option.builder()
                .longOpt("num_hosts")
                .desc("Specify the number of ESX hosts")
                .argName("NUMBER OF ESX HOSTS")
                .required(true)
                .hasArg()
                .build();        
        List<Option> optionList = Arrays.asList(orgOption, sddcOption, numHostsOption);

        super.parseArgs(optionList, args);
        this.orgId = (String) parsedOptions.get("org_id");
        this.sddcId = (String) parsedOptions.get("sddc_id");
        this.numHosts = (String) parsedOptions.get("num_hosts");
    }

    protected void setup() throws Exception {
        this.vmcAuthHelper = new VmcAuthenticationHelper();
        this.apiClient =
                this.vmcAuthHelper.newVmcClient(this.vmcServer,
                        this.cspServer, this.refreshToken);
        
        this.esxsStub = apiClient.createStub(Esxs.class);
    }

    protected void run() throws Exception {
        int num_of_hosts = this.numHosts.isEmpty() ? 1 : Integer.parseInt(numHosts);
        EsxConfig esxConfig = new EsxConfig.Builder(num_of_hosts).build();

        // Add a host
        System.out.printf("Example: Adding a host to SDDC (SDDC ID=%s)", sddcId);
        Task addTask = null;
        addTask = this.esxsStub.create(orgId, sddcId, esxConfig, ADD_HOST_ACTION);
        String taskId = addTask.getId();
        System.out.printf("\nPoll the ADD HOST task (taskId = %s) :", taskId);
        boolean taskCompleted = VmcTaskHelper.pollTask(apiClient, orgId, taskId, TASK_POLLING_DELAY_IN_MILLISECONDS);
        if(!taskCompleted) {
            System.out.println("Add task was either canceled or it failed. Exiting.");
            System.exit(1);
        }
    }
    
    protected void cleanup() throws Exception {
        int num_of_hosts = this.numHosts.isEmpty() ? 1 : Integer.parseInt(numHosts);
        EsxConfig esxConfig = new EsxConfig.Builder(num_of_hosts).build();

        // Remove a host
        System.out.printf("\n\nExample: Removing a host from an SDDC (SDDC ID=%s)", sddcId);
        Task removeTask = this.esxsStub.create(orgId, sddcId, esxConfig, REMOVE_HOST_ACTION);
        String taskId = removeTask.getId();        
        System.out.printf("\nPoll the REMOVE HOST task (taskId = %s) :", taskId);
        VmcTaskHelper.pollTask(apiClient, orgId, taskId, TASK_POLLING_DELAY_IN_MILLISECONDS);
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
        new AddRemoveHost().execute(args);
    }
}
