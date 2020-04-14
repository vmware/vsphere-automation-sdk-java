/*
 * *******************************************************
 * Copyright VMware, Inc. 2019.  All Rights Reserved.
 * SPDX-License-Identifier: MIT
 * *******************************************************
 *
 * DISCLAIMER. THIS PROGRAM IS PROVIDED TO YOU "AS IS" WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, WHETHER ORAL OR WRITTEN,
 * EXPRESS OR IMPLIED. THE AUTHOR SPECIFICALLY DISCLAIMS ANY IMPLIED
 * WARRANTIES OR CONDITIONS OF MERCHANTABILITY, SATISFACTORY QUALITY,
 * NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE.
 */
package vmware.samples.vmc.draas;

import java.util.Arrays;
import java.util.List;
import org.apache.commons.cli.Option;

import com.vmware.vapi.client.ApiClient;
import com.vmware.vmc.draas.SiteRecovery;
import com.vmware.vmc.draas.model.Task;

import vmware.samples.common.VmcSamplesAbstractBase;
import vmware.samples.common.authentication.VmcAuthenticationHelper;
import vmware.samples.vmc.draas.helpers.DraasTaskHelper;


/**
 * Sample to Activate and Deactivate DRaaS plugin in VMC deployed SDDCs
 * Once SiteRecovery Add-On is activated, the SiteRecovery Component Details are queried for the current status
 * @author VMware Inc.
 **/
public class ActivationOperations extends VmcSamplesAbstractBase {

	private String orgId;
	private String sddcId;
	private ApiClient apiClient;
	private SiteRecovery siteRecoveryStub;
	public static final int TASK_POLLING_DELAY_IN_MILLISECONDS = 500;

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
		List<Option> optionList = Arrays.asList(orgOption, sddcOption);

		super.parseArgs(optionList, args);
		this.orgId = (String) parsedOptions.get("org_id");
		this.sddcId = (String) parsedOptions.get("sddc_id");

	}

	@Override
	protected void setup() throws Exception {
		this.vmcAuthHelper = new VmcAuthenticationHelper();
		this.apiClient =
				this.vmcAuthHelper.newVmcClient(this.vmcServer,
						this.cspServer, this.refreshToken);
		siteRecoveryStub = apiClient.createStub(SiteRecovery.class);
	}

	@Override
	protected void run() throws Exception {
	    Task siteRecoveryActivationTask =
				siteRecoveryStub.post(orgId, sddcId, null);
		System.out.printf("Site Recovery Addon Task Activation ID  %s", siteRecoveryActivationTask.getId(),
				"\t", siteRecoveryActivationTask.getStatus());
        boolean taskCompleted = DraasTaskHelper.pollTask(apiClient, orgId, siteRecoveryActivationTask.getId(),
                TASK_POLLING_DELAY_IN_MILLISECONDS);
        if(!taskCompleted) {
            System.out.println(" Deploy DraaS task was either canceled or it failed. Exiting.");
            System.exit(1);
        }
	}

	@Override
	protected void cleanup() throws Exception {
	    Task siteRecoveryDeActivationTask =
				siteRecoveryStub.delete(orgId, sddcId, false, false);
		String taskId =
				siteRecoveryDeActivationTask.getId();
		System.out.printf("Site Recovery Add-On Task De-Activation ID  %s", taskId);
        boolean taskCompleted = DraasTaskHelper.pollTask(apiClient, orgId, siteRecoveryDeActivationTask.getId(),
                TASK_POLLING_DELAY_IN_MILLISECONDS);
        if(!taskCompleted) {
            System.out.println(" Deploy DraaS task was either canceled or it failed. Exiting.");
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
		new ActivationOperations().execute(args);
	}


}
