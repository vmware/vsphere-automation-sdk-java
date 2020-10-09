/*
 * *******************************************************
 * Author: Vikas Shitole
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.cli.Option;
import com.vmware.vcenter.namespace_management.Clusters;
import com.vmware.vcenter.namespaces.AccessTypes;
import com.vmware.vcenter.namespaces.Instances;
import com.vmware.vcenter.namespaces.InstancesTypes;
import com.vmware.vcenter.storage.Policies;
import com.vmware.vcenter.storage.PoliciesTypes;
import com.vmware.vcenter.storage.PoliciesTypes.Summary;

import vmware.samples.common.SamplesAbstractBase;
import vmware.samples.vcenter.helpers.ClusterHelper;


/**
 * Description: Demonstrates the how to create supervisor Namespace on given Supervisor cluster (either NSX-T or VDS based)
 * Author: Vikas Shitole, VMware Inc, https://vThinkBeyondVM.com
 * Understand APIs: https://vthinkbeyondvm.com/script-to-configure-vsphere-supervisor-cluster-using-rest-apis/
 * Sample Prerequisites: The sample needs an existing Supervisor cluster enabled cluster name. It can be either NSX-T based or vSphere networking
 * API doc: https://developer.vmware.com/docs/vsphere-automation/latest/vcenter/api/vcenter/namespaces/instances/post/
 */
public class createNamespace extends SamplesAbstractBase{

    private String clusterName;
    private String clusterId;
	private Policies policyService;
	private String storagePolicyId;
	private Object storagePolicy;
	private Instances namespaceService;
	private String namespaceName;
	private String storageLimit;
	private String domainName;
	private String roleName;
	private String subjectName;
	private String subjectType;
	private Clusters ppClusterService;

    @Override
    protected void parseArgs(String[] args) {
        // Parse the command line options or use config file
        Option clusterNameOption = Option.builder()
            .longOpt("clustername")
            .desc("Name of the cluster we need to get info"
            		+ " about k8scluster")
            .required(true)
            .hasArg()
            .argName("Cluster Name")
            .build();
        Option policyNameOption = Option.builder()
                .longOpt("storagepolicy")
                .desc("REQUIRED: Storage policy name")
                .argName("Storage policy")
                .required(true)
                .hasArg()
                .build();
        Option namespaceNameOption = Option.builder()
                .longOpt("namespacename")
                .desc("REQUIRED: Supervisor namespace Name")
                .argName("Supervisor Namespace")
                .required(true)
                .hasArg()
                .build();
        Option domainNameOption = Option.builder()
                .longOpt("domainname")
                .desc("REQUIRED: Domain name i.e. vsphere.local")
                .argName("Domain name")
                .required(true)
                .hasArg()
                .build();
        Option subjectNameOption = Option.builder()
                .longOpt("subjectname")
                .desc("REQUIRED: Subject name for namespace i.e. Administrator ")
                .argName("Subject name")
                .required(true)
                .hasArg()
                .build();
        Option storageLimitOption = Option.builder()
                .longOpt("storagelimit")
                .desc("REQUIRED: Storage limit in MB 10240 for 10 GB")
                .argName("Storage limit on Namespace")
                .required(true)
                .hasArg()
                .build();
        Option roleNameOption = Option.builder()
                .longOpt("rolename")
                .desc("REQUIRED: Role name either EDIT or VIEW ")
                .argName("Role Name")
                .required(true)
                .hasArg()
                .build();
        Option subjectTypeOption = Option.builder()
                .longOpt("subjecttype")
                .desc("REQUIRED: Subject type i.e. USER or GROUP ")
                .argName("Subject Type")
                .required(true)
                .hasArg()
                .build();
        
        List<Option> optionList = Arrays.asList(clusterNameOption,
        		policyNameOption,
        		namespaceNameOption,
        		domainNameOption,
        		subjectNameOption,
        		storageLimitOption,
        		roleNameOption,
        		subjectTypeOption);
        
        //TODO: Resource quota aspects are not covered, user needs to add those aspects
        
        super.parseArgs(optionList, args);
        this.clusterName =  (String) parsedOptions.get("clustername");
        this.storagePolicy =  (String) parsedOptions.get("storagepolicy");
        this.namespaceName =  (String) parsedOptions.get("namespacename");
        this.domainName =  (String) parsedOptions.get("domainname");
        this.subjectName =  (String) parsedOptions.get("subjectname");
        this.storageLimit =  (String) parsedOptions.get("storagelimit");
        this.roleName =  (String) parsedOptions.get("rolename");
        this.subjectType =  (String) parsedOptions.get("subjecttype");
        
        
    }

    @Override
    protected void setup() throws Exception {
		this.clusterId=ClusterHelper.getCluster(vapiAuthHelper.getStubFactory(), sessionStubConfig, this.clusterName);
		
		this.ppClusterService = this.vapiAuthHelper.getStubFactory().createStub(
                Clusters.class, this.sessionStubConfig); 
		
		com.vmware.vcenter.namespace_management.ClustersTypes.Info info= this.ppClusterService.get(this.clusterId);
		
		boolean k8sStatus=!(info.getKubernetesStatus().toString().equals("READY"));
		boolean clusterStatus=!(info.getConfigStatus().toString().equals("RUNNING"));
		
		if (k8sStatus && clusterStatus) {
			System.out.println("Cluster is NOT in good condition, exiting from creating namespace");
			System.exit(0); 
		}
		
		//Getting Storage policy ID
		this.policyService = this.vapiAuthHelper.getStubFactory().createStub(
                Policies.class, this.sessionStubConfig);
		
		this.namespaceService = this.vapiAuthHelper.getStubFactory().createStub(
				Instances.class, this.sessionStubConfig);
		
		List<PoliciesTypes.Summary> summaries=this.policyService.list(null);
		
		for (Summary summary:summaries) {
			//TODO: Add NULL check conditions.
			if(summary!=null && summary.getName().equals(this.storagePolicy)) {	
				this.storagePolicyId=summary.getPolicy();
			System.out.println("Storage policy UUID::"+summary.getPolicy());
			break;
			}
		}
        
    }

    @Override
    protected void run() throws Exception {
    	
    	InstancesTypes.CreateSpec spec =new InstancesTypes.CreateSpec();
    	spec.setCluster(this.clusterId);
    	spec.setDescription("My first namespace, WOW");
    	spec.setNamespace(this.namespaceName);
    	InstancesTypes.StorageSpec storageSpec=new InstancesTypes.StorageSpec();
    	storageSpec.setLimit(Long.valueOf(this.storageLimit).longValue());
    	storageSpec.setPolicy(this.storagePolicyId);
    	List<InstancesTypes.StorageSpec> storageSpecs = new ArrayList<InstancesTypes.StorageSpec>();
    	storageSpecs.add(storageSpec);
    	spec.setStorageSpecs(storageSpecs);
    	InstancesTypes.Access accessList= new InstancesTypes.Access();
    	accessList.setDomain(this.domainName); 
    	if(this.roleName.equalsIgnoreCase("EDIT")) {
    	accessList.setRole(AccessTypes.Role.EDIT);
    	} else{
    		accessList.setRole(AccessTypes.Role.VIEW);
    	}
    	accessList.setSubject( this.subjectName); //Default is Administrator
    	if(this.subjectType.equalsIgnoreCase("USER")) {
    		accessList.setSubjectType( AccessTypes.SubjectType.USER);
    	} else{
    		accessList.setSubjectType( AccessTypes.SubjectType.GROUP);
    	}
    	
    	List<InstancesTypes.Access> accessLists = new ArrayList<InstancesTypes.Access>();
    	accessLists.add(accessList);
    	spec.setAccessList(accessLists);
    	this.namespaceService.create(spec);
    	System.out.println("Invocation is successful for creating supervisor namespace, check H5C or call GET API to get status");
    	
       
    }

    @Override
    protected void cleanup() throws Exception {
       
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
        new createNamespace().execute(args);
    }

}
