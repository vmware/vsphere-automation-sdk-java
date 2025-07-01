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
import com.vmware.vcenter.namespace_management.ClustersTypes;
import vmware.samples.common.SamplesAbstractBase;
import vmware.samples.vcenter.helpers.ClusterHelper;

/**
 * Description: Demonstrates how to Disable/Delete vSphere supervisor cluster on
 * given cluster (It can be NSXT based or vSphere networking based) 
 * Author: Vikas Shitole, VMware Inc.
 * Reference: https://vthinkbeyondvm.com/automating-key-vsphere-supervisor-cluster-workflows-using-java-sdk/
 * Sample Prerequisites: The sample needs an existing Supervisor cluster (NSX-T based or vSphere
 * networking based) enabled cluster name
 */
public class DisableSuperVisorCluster extends SamplesAbstractBase {

	private String clusterName;

	private String clusterId;

	private Clusters ppClusterService;

	private String configStatus;

	@Override
	protected void parseArgs(String[] args) {
		// Parse the command line options or use config file

		System.out.println("Parsing the parameters passed for disabling vSphere supervisor cluster");
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
		clusterId = ClusterHelper.getCluster(vapiAuthHelper.getStubFactory(), sessionStubConfig, this.clusterName);
		System.out.println("clusterId::" + this.clusterId);

	}

	@Override
	protected void run() throws Exception {
        // Disable Supervisor cluster: It will disable/remove/delete Supervisor cluster and remove all the vSphere pods, Guest clusters/TKC
		this.ppClusterService.disable(this.clusterId);
		System.out.println("Waiting for 1 min to quick-start the disable Supervisor cluster operation");
		Thread.sleep(60 * 1000);
		//Getting cluster configuration status. It must be in "REMOVING" state.
		ClustersTypes.Info info = this.ppClusterService.get(this.clusterId);
		this.configStatus = info.getConfigStatus().toString();
		System.out.println("Cluster status::"+this.configStatus);
		if ((this.configStatus != null && this.configStatus.equalsIgnoreCase("REMOVING"))) {
			System.out.println(
					"Supervisor Cluster is being disabled, check H5C further, track the status using GET API in loop");

		}

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
		new DisableSuperVisorCluster().execute(args);
	}

}

