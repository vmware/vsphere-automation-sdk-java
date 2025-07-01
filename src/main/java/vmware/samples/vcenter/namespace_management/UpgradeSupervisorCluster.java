/*
 * *******************************************************
 *  Copyright VMware, Inc. 2020.  All Rights Reserved.
 *  Author: Vikas Shitole, VMware, https://vthinkbeyondvm.com/
 * SPDX-License-Identifier: MIT
 * *******************************************************
 *
 * DISCLAIMER. THIS PROGRAM IS PROVIDED TO YOU "AS IS" WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, WHETHER ORAL OR WRITTEN,
 * EXPRESS OR IMPLIED. THE AUTHOR SPECIFICALLY DISCLAIMS ANY IMPLIED
 * WARRANTIES OR CONDITIONS OF MERCHANTABILITY, SATISFACTORY QUALITY,
 * NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE.
 */
package vmware.samples.vcenter.namespace_management;

import java.util.Arrays;
import java.util.List;
import org.apache.commons.cli.Option;
import com.vmware.vcenter.namespace_management.Clusters;
import com.vmware.vcenter.namespace_management.software.ClustersTypes;
import com.vmware.vcenter.namespace_management.software.ClustersTypes.UpgradeSpec;
import vmware.samples.common.SamplesAbstractBase;
import vmware.samples.vcenter.helpers.ClusterHelper;

/**
 * Description: Demonstrates how to Update/Upgrade vSphere supervisor cluster (It can be NSXT based or vSphere networking based) 
 * Author: Vikas Shitole, VMware, https://vthinkbeyondvm.com/ Sample Prerequisites: The
 * sample needs an existing Supervisor cluster enabled cluster name
 */
public class UpgradeSupervisorCluster extends SamplesAbstractBase {

	private String clusterName;

	private String clusterId;

	private com.vmware.vcenter.namespace_management.software.Clusters wcpUpdateService;

	private String desiredVersion;

	private List<String> availableVersion;

	private Clusters ppClusterService;

	@Override
	protected void parseArgs(String[] args) {
		// Parse the command line options or use config file

		System.out.println("Parsing the parameters passed for enabling vSphere supervisor cluster");
		Option clusterNameOption = Option.builder().longOpt("clustername")
				.desc("REQUIRED: Name of the cluster we need to upgrade info").required(true).hasArg()
				.argName("Cluster Name").build();

		List<Option> optionList = Arrays.asList(clusterNameOption);

		super.parseArgs(optionList, args);
		this.clusterName = (String) parsedOptions.get("clustername");

	}

	@Override
	protected void setup() throws Exception {

		this.ppClusterService = this.vapiAuthHelper.getStubFactory().createStub(Clusters.class, this.sessionStubConfig);

		// Supervisor cluster update service object
		this.wcpUpdateService = this.vapiAuthHelper.getStubFactory()
				.createStub(com.vmware.vcenter.namespace_management.software.Clusters.class, this.sessionStubConfig);

		System.out.println("Getting cluster identifier as part of setup");
		this.clusterId = ClusterHelper.getCluster(vapiAuthHelper.getStubFactory(), sessionStubConfig, clusterName);

		com.vmware.vcenter.namespace_management.software.ClustersTypes.Info clusterinfo = this.wcpUpdateService
				.get(this.clusterId);
		String status = clusterinfo.getState().toString();
		String expectedStatus = "READY";

		if (status.equalsIgnoreCase(expectedStatus)) {
			System.out.println("Cluster is ready for upgrade as state is::" + status);
		} else {
			System.out.println("Cluster is not ready for upgrade as actual state is::" + status);
			System.exit(0);
		}

		com.vmware.vcenter.namespace_management.ClustersTypes.Info info = this.ppClusterService.get(this.clusterId);

		boolean k8sStatus = !(info.getKubernetesStatus().toString().equals("READY"));
		boolean clusterStatus = !(info.getConfigStatus().toString().equals("RUNNING"));

		if (k8sStatus && clusterStatus) {
			System.out.println("Cluster is NOT in good condition, hence skipping the upgrade");
			System.exit(0);
		}

		// This gives summary for all the clusters
		List<com.vmware.vcenter.namespace_management.software.ClustersTypes.Summary> clusterSummary = this.wcpUpdateService
				.list();
		// Fetching the desired version for upgrade
		for (ClustersTypes.Summary cls : clusterSummary) {
			if (cls.getCluster().equals(clusterId)) {
				this.availableVersion = cls.getAvailableVersions();
				boolean isVersionEmpty = availableVersion.isEmpty();
				if (isVersionEmpty) {
					System.out.println("No updates available, need not update");
					System.exit(0);
				} else {
					// Note that just taking the latest available version for supervisor cluster
					// upgrade, there could be another version as well
					this.desiredVersion = availableVersion.get(0);

				}
				break;
			}
		}

	}

	@Override
	protected void run() throws Exception {

		System.out.println("We are building the Spec for upgrading vSphere supervisor cluster");
		com.vmware.vcenter.namespace_management.software.ClustersTypes.UpgradeSpec spec = new UpgradeSpec();
		spec.setIgnorePrecheckWarnings(true);
		spec.setDesiredVersion(this.desiredVersion);
		this.wcpUpdateService.upgrade(clusterId, spec);
		System.out.println(
				"Invocation is successful for updating vSphere supervisor cluster, check H5C, track the status using GET API");
		// Note: There is another API, which takes multiple clusters for upgrade, so we
		// can upgrade multiple clusters at a time
		// API name:
		// upgradeMultiple(java.util.Map<java.lang.String,ClustersTypes.UpgradeSpec>
		// specs)

	}

	@Override
	protected void cleanup() throws Exception {

	}

	public static void main(String[] args) throws Exception {
		/*
		 * Execute the sample using the command line arguments or parameters from the
		 * configuration file. This executes the following steps: 1. Parse the arguments
		 * required by the sample 2. Login to the server 3. Setup any resources required
		 * by the sample run 4. Run the sample 5. Cleanup any data created by the sample
		 * run, if cleanup=true 6. Logout of the server
		 */
		new UpgradeSupervisorCluster().execute(args);
	}

}
