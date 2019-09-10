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
import com.vmware.vmc.draas.SiteRecoverySrmNodes;
import com.vmware.vmc.draas.model.ProvisionSrmConfig;
import vmware.samples.vmc.draas.helpers.DraasTaskHelper;
import vmware.samples.common.authentication.VmcAuthenticationHelper;
import vmware.samples.common.VmcSamplesAbstractBase;

 /**
 * Sample code to deploy and remove additional VMC Site Recovery Node
 * @author VMware Inc.
 *
 **/

public class DeployAdditonalSiteRecoveryNode extends VmcSamplesAbstractBase {

	private String orgId;
	private String sddcId;
	private ApiClient apiClient;
	private SiteRecoverySrmNodes siteRecoverySrmNodes;
	private String SrmNodeId;

	public static final int TASK_POLLING_DELAY_IN_MILLISECONDS = 500;

	/**
	 * A SRM extension key suffix must be fewer than 13 characters and
	can include alphanumeric characters, hyphen, or period,
	but cannot start or end with a sequence of hyphen, or period characters.
	The KEY SUFFIX is used to identify the node under deployment and removal.
	This suffix is appended to 'com.vmware.vcDr-
	**/
	private static final String SITE_RECOVERY_EXTENSION_KEY_SUFFIX = "com.VcDr-Test";

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
		siteRecoverySrmNodes = apiClient.createStub(SiteRecoverySrmNodes.class);
	}

	@Override
	protected void run() throws Exception {
		ProvisionSrmConfig provisionSrmConfig = new ProvisionSrmConfig();
		provisionSrmConfig.setSrmExtensionKeySuffix(SITE_RECOVERY_EXTENSION_KEY_SUFFIX);
		com.vmware.vmc.draas.model.Task additonalSrmDeployment = 
		        siteRecoverySrmNodes.post(orgId, sddcId, provisionSrmConfig);
		System.out.printf("Deployment of additional SRM Node "
		        + "is in progress with Task ID %s\n", additonalSrmDeployment.getId());
		SrmNodeId = additonalSrmDeployment.getResourceId();
		System.out.println("Deployed SRM NodeId is"+SrmNodeId);
		boolean taskCompleted =
		        DraasTaskHelper.pollTask(apiClient, orgId, additonalSrmDeployment.getId(),
						TASK_POLLING_DELAY_IN_MILLISECONDS);
		System.out.println("Deployment Status "+taskCompleted);
	}

	@Override
	protected void cleanup() throws Exception {
	    com.vmware.vmc.draas.model.Task deleteAdditionalSrm = siteRecoverySrmNodes.delete(orgId, sddcId, SrmNodeId);
	    System.out.printf("Removal of additional SRM Node is in progress with Task ID %s\n",
	            deleteAdditionalSrm.getId() );
		boolean cleanupStatus =
		        DraasTaskHelper.pollTask(apiClient, orgId, deleteAdditionalSrm.getId(),
						TASK_POLLING_DELAY_IN_MILLISECONDS);
		System.out.println("Cleanup Status "+cleanupStatus);
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
		new DeployAdditonalSiteRecoveryNode().execute(args);

	}

}
