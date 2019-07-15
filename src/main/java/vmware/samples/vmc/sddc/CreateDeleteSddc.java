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
import com.vmware.vmc.model.AwsSddcConfig;
import com.vmware.vmc.model.Task;
import com.vmware.vmc.orgs.Sddcs;
import com.vmware.vmc.model.Sddc;

import vmware.samples.common.VmcSamplesAbstractBase;
import vmware.samples.common.authentication.VmcAuthenticationHelper;
import vmware.samples.vmc.helpers.VmcTaskHelper;

/**
 * Description: Demonstrates how to create/delete an SDDC
 *
 * Author: VMware, Inc.
 * Sample Prerequisites: An existing organization, VMC Server
 */
public class CreateDeleteSddc extends VmcSamplesAbstractBase {
    private Sddcs sddcsStub;    
    private ApiClient apiClient;    
    private String orgId, sddcName, sddcId;
    public static final int TASK_POLLING_DELAY_IN_MILLISECONDS = 500;
    public static final String SDDC_REGION = "US_WEST_2";
    public static final String SDDC_PROVIDER = "AWS";
    public static final int NUM_HOSTS = 4;

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
                .longOpt("sddc_name")
                .desc("Specify the name of the sddc to be created")
                .argName("SDDC NAME")
                .required(true)
                .hasArg()
                .build();    
        List<Option> optionList = Arrays.asList(orgOption, sddcOption);

        super.parseArgs(optionList, args);
        this.orgId = (String) parsedOptions.get("org_id");
        this.sddcName = (String) parsedOptions.get("sddc_name");
    }

    protected void setup() throws Exception {
        this.vmcAuthHelper = new VmcAuthenticationHelper();
        this.apiClient = this.vmcAuthHelper.newVmcClient(this.vmcServer, this.cspServer, this.refreshToken);
        this.sddcsStub = apiClient.createStub(Sddcs.class);
    }

    protected void run() throws Exception {
        System.out.printf("Example: Create a SDDC %s in org %s", this.sddcName, this.orgId);
        
        // Set the provider (for testing only)
        String provider = System.getProperty("VMC_PROVIDER", SDDC_PROVIDER);

        AwsSddcConfig sddcConfig = new AwsSddcConfig();
        sddcConfig.setName(this.sddcName);
        sddcConfig.setRegion(SDDC_REGION);
        sddcConfig.setNumHosts(NUM_HOSTS);
        //TODO: Check What value has to be set 
        // setSddcType
        sddcConfig.setSddcType(provider);
        
        this.sddcsStub.create(this.orgId, sddcConfig, false);
        
        List<Sddc> sddcList = this.sddcsStub.list(this.orgId, false);
        for (Sddc sddc : sddcList) {
            if(sddc.getName().equals(this.sddcName)) {
                this.sddcId = sddc.getId();
                break;
            }
        }
    }
    
    protected void cleanup() throws Exception {        
        System.out.printf("\n\nExample: Delete a SDDC %s in org %s", this.sddcName, this.orgId);
        Task deleteSddcTask = this.sddcsStub.delete(this.orgId, sddcId, false, null, null);
        String taskId = deleteSddcTask.getId();
        System.out.printf("\nPoll the DELETE SDDC task (taskId = %s) :", taskId);
        boolean taskCompleted = VmcTaskHelper.pollTask(apiClient, orgId, taskId, TASK_POLLING_DELAY_IN_MILLISECONDS);
        if(!taskCompleted) {
            System.out.println("DELETE SDDC task was either canceled or it failed. Exiting.");
            System.exit(1);
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
        new CreateDeleteSddc().execute(args);
    }
}
