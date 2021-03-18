package vmware.samples.vmc.draas;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.vmware.vmc.draas.model.SrmNode;
import org.apache.commons.cli.Option;
import com.vmware.vapi.client.ApiClient;
import com.vmware.vmc.draas.SiteRecovery;
import com.vmware.vmc.draas.SiteRecoverySrmNodes;
import com.vmware.vmc.draas.model.ProvisionSrmConfig;
import com.vmware.vmc.draas.model.SiteRecoveryNode;
import com.vmware.vmc.draas.model.Task;

import vmware.samples.common.authentication.VmcAuthenticationHelper;
import vmware.samples.common.VmcSamplesAbstractBase;
 /**
 * Sample code to deploy and remove additional SRM Node
 * @author VMware Inc.
 *
 **/

public class DeployAdditonalSRMNode extends VmcSamplesAbstractBase {

	private String orgId;
	private String sddcId;
	private ApiClient apiClient;
	private SiteRecoverySrmNodes siteRecoverySrmNodes;
	private SiteRecovery siteRecoveryStub;
	private String SrmNodeId;

	public static final int TASK_POLLING_DELAY_IN_MILLISECONDS = 10000;

	/**
	 * A SRM extension key suffix must be fewer than 13 characters and
	can include alphanumeric characters, hyphen, or period,
	but cannot start or end with a sequence of hyphen, or period characters.
	The KEY SUFFIX is used to identify the node under deployment and removal.
	**/
	private static final String SRM_EXTENSION_KEY_SUFFIX = "com.VcSrm1";

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
		siteRecoveryStub = apiClient.createStub(SiteRecovery.class);
	}

	@Override
	protected void run() throws Exception {
		ProvisionSrmConfig provisionSrmConfig = new ProvisionSrmConfig();
		provisionSrmConfig.setSrmExtensionKeySuffix(SRM_EXTENSION_KEY_SUFFIX);
		Task additonalSrmDeployment = siteRecoverySrmNodes.post(orgId, sddcId, provisionSrmConfig);
		System.out.printf("Deployment of additional SRM Node "
		        + "is in progress with Task ID %s", additonalSrmDeployment.getId());
		SrmNodeId = additonalSrmDeployment.getResourceId();
		System.out.println("Deployed SRM NodeId is"+SrmNodeId);
		boolean taskCompleted =
				queryNodeDeployment(siteRecoveryStub, orgId, sddcId,
						TASK_POLLING_DELAY_IN_MILLISECONDS);
		System.out.println("\nDeployment Status "+taskCompleted);
	}

	@Override
	protected void cleanup() throws Exception {

		Task deleteAdditionalSrm = siteRecoverySrmNodes.delete(orgId, sddcId, SrmNodeId);
		System.out.printf("Removal of additional SRM Node is in progress %s", deleteAdditionalSrm.getCreated(),
				"%s with Task ID %s"+deleteAdditionalSrm.getId() );
		System.out.println(deleteAdditionalSrm.getStatus());
		boolean cleanupStatus =
				queryNodeDeployment(siteRecoveryStub, orgId, sddcId,
						TASK_POLLING_DELAY_IN_MILLISECONDS);
		System.out.println("Cleanup Status "+cleanupStatus);
	}

	/**
	 * Identify the SRM Node with the SRM Extension name.
	 * Note: As there is no Task or GET API for additional node deployment,
	 * querying the SiteRecovery Node (with the Extension Key) to know the current status
	 **/
	public static boolean queryNodeDeployment(SiteRecovery siteRecoveryStub,
	        String orgId, String sddcId, int waitTime)
			throws InterruptedException {
		boolean status = false;
		//Wait until host name is updated for the new node deployment
		Thread.sleep(waitTime*2);
		int nodeIndex=0;
		while(nodeIndex < siteRecoveryStub.get(orgId, sddcId).getSrmNodes().size()) {
			SrmNode initialNodeState = siteRecoveryStub.get(orgId,
			        sddcId).getSrmNodes().get(nodeIndex);
			if(initialNodeState.getHostname().contains(SRM_EXTENSION_KEY_SUFFIX.
			        replaceAll("\\.", "-").toLowerCase())) {
				while(true) {
					Thread.sleep(waitTime);
					SrmNode currentNodeState = siteRecoveryStub.
					        get(orgId, sddcId).getSrmNodes().get(nodeIndex);
					if("READY".equals(currentNodeState.getState()) ||
					        "DELETING".equals(currentNodeState.getState()))
					{
						System.out.printf("\n Site Recovery (DRaaS) "
						        + "Deployment Status  %s", new Date() +" : "+
								currentNodeState.getState());
						currentNodeState.getId();
						status = true;
						break;

					}
					else if ("CANCELED".equals(currentNodeState.getState()) ||
							"FAILED".equals(currentNodeState.getState())) {
						System.out.printf("\n Site Recovery (DRaaS) "
						        + "Deployment %s", new Date() +" : "+
								currentNodeState.getState());
						status = false;
						break;
					}
					else {
						System.out.printf("\n Deploying/Provisioning "
						        + "in Progress %s", new Date() +" : "+
								currentNodeState.getState());
						status = false;
						continue;
					}
				}
			}
			nodeIndex++;
		}
		return status;
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
		new DeployAdditonalSRMNode().execute(args);

	}

}
