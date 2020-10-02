/*
 * *******************************************************
 *  Copyright VMware, Inc. 2020.  All Rights Reserved.
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.cli.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vmware.vcenter.Network;
import com.vmware.vcenter.NetworkTypes;
import com.vmware.vcenter.namespace_management.Clusters;
import com.vmware.vcenter.namespace_management.ClustersTypes;
import com.vmware.vcenter.namespace_management.DistributedSwitchCompatibility;
import com.vmware.vcenter.namespace_management.DistributedSwitchCompatibilityTypes;
import com.vmware.vcenter.namespace_management.EdgeClusterCompatibility;
import com.vmware.vcenter.namespace_management.EdgeClusterCompatibilityTypes;
import com.vmware.vcenter.namespace_management.Ipv4Cidr;
import com.vmware.vcenter.namespace_management.SizingHint;
import com.vmware.vcenter.storage.Policies;
import com.vmware.vcenter.storage.PoliciesTypes;
import com.vmware.vcenter.storage.PoliciesTypes.Summary;

import vmware.samples.common.SamplesAbstractBase;
import vmware.samples.vcenter.helpers.ClusterHelper;


/**
 * Description: Demonstrates how to enable vSphere supervisor cluster on given cluster
 *
 * Author: Vikas Shitole, VMware, vThinkBeyondVM.com
 * Sample Prerequisites: All below params need to be passed for this sample
 * Reference : https://vthinkbeyondvm.com/script-to-configure-vsphere-supervisor-cluster-using-rest-apis/
 */
public class EnableSupervisorCluster extends SamplesAbstractBase{
	private static Logger logger = LoggerFactory.getLogger(EnableSupervisorCluster.class);

    private String clusterName;
    private Clusters ppClusterService;
    private String clusterId;
    private Policies policyService;
	private EdgeClusterCompatibility nsxEdgeService;
	private DistributedSwitchCompatibility VDSService;
	private Network networkService;
	private String hintSize;
	private String masterNetwork;
	private String floatingIP;
	private String masterDnsServer;
	private String workerDnsServer;
	private String ntpServer;
	private String storagePolicy;
	private String podCidr;
	private String serviceCidr;
	private String ingressCidr;
	private String egressCidr;
	private String dvsSwitchUuid;
	private String networkId;
	private String edgeClusterId;
	private String storagePolicyId;
	//private String floatingIP;

    @Override
    protected void parseArgs(String[] args) {
        // Parse the command line options or use config file
    	
    	System.out.println("Parsing the parameters passed for enabling vSphere supervisor cluster");
        Option clusterNameOption = Option.builder()
            .longOpt("clustername")
            .desc("REQUIRED: Name of the cluster we need to get info"
            		+ " about k8scluster")
            .required(true)
            .hasArg()
            .argName("Cluster Name")
            .build();
        Option hintSizeOption = Option.builder()
                .longOpt("hintsize")
                .desc("REQUIRED: Enum value as either of TINY/SMALL/MEDIUM/LARGE")
                .argName("Cluster size")
                .required(true)
                .hasArg()
                .build();
        Option masterVmNetworkOption = Option.builder()
                .longOpt("masternetwork")
                .desc("REQUIRED: Master VM network port group")
                .argName("Master Network")
                .required(true)
                .hasArg()
                .build();
        Option floatingipOption = Option.builder()
                .longOpt("floatingip")
                .desc("REQUIRED: Floating IP used by HA master cluster ")
                .argName("Master starting IP")
                .required(true)
                .hasArg()
                .build();
        Option masterdnsserverOption = Option.builder()
                .longOpt("masterdnsserver")
                .desc("Master DNS server IP ")
                .argName("DNS Server")
                .required(false)
                .hasArg()
                .build();
        Option workerdnsserverOption = Option.builder()
                .longOpt("workerdnsserver")
                .desc(" Worker DNS server IP ")
                .argName("DNS Server")
                .required(false)
                .hasArg()
                .build();
        Option ntpserverOption = Option.builder()
                .longOpt("ntpserver")
                .desc("NTP server IP ")
                .argName("NTP Server")
                .required(false)
                .hasArg()
                .build();
        Option storagepolicyOption = Option.builder()
                .longOpt("storagepolicy")
                .desc("REQUIRED: Storage Policy for Master, image and ephermal storage ")
                .argName("Storage Policy")
                .required(true)
                .hasArg()
                .build();
        Option podcidrOption = Option.builder()
                .longOpt("podcidr")
                .desc("REQUIRED: POD cidr network i.e. 10.2.3.4/24 (including prefix)")
                .argName("POD CIDR")
                .required(true)
                .hasArg()
                .build();
        Option servicecidrOption = Option.builder()
                .longOpt("servicecidr")
                .desc("REQUIRED: Service cidr  i.e. 10.2.3.4/21  (including prefix)")
                .argName("Service CIDR")
                .required(true)
                .hasArg()
                .build();
        Option ingresscidrOption = Option.builder()
                .longOpt("ingresscidr")
                .desc("REQUIRED: Ingress cidr network (including prefix) ")
                .argName("Ingress CIDR")
                .required(true)
                .hasArg()
                .build();
        Option egresscidrOption = Option.builder()
                .longOpt("egresscidr")
                .desc("REQUIRED: Egress cidr network (including prefix)")
                .argName("Egress CIDR")
                .required(true)
                .hasArg()
                .build();
        
        List<Option> optionList = Arrays.asList(clusterNameOption,
        		hintSizeOption,
        		masterVmNetworkOption,
        		floatingipOption,
        		masterdnsserverOption,
        		workerdnsserverOption,
        		ntpserverOption,
        		storagepolicyOption,
        		podcidrOption,
        		servicecidrOption,
        		ingresscidrOption,
        		egresscidrOption);      
        
       // List<Option> optionList = Collections.singletonList(clusterNameOption);
        super.parseArgs(optionList, args);
        this.clusterName =  (String) parsedOptions.get("clustername");
        this.hintSize =  (String) parsedOptions.get("hintsize");
        this.masterNetwork =  (String) parsedOptions.get("masternetwork");
        this.floatingIP =  (String) parsedOptions.get("floatingip");
        this.masterDnsServer =  (String) parsedOptions.get("masterdnsserver");
        this.workerDnsServer =  (String) parsedOptions.get("workerdnsserver");
        this.ntpServer =  (String) parsedOptions.get("ntpserver");
        this.storagePolicy =  (String) parsedOptions.get("storagepolicy");
        this.podCidr =  (String) parsedOptions.get("podcidr");
        this.serviceCidr =  (String) parsedOptions.get("servicecidr");
        this.ingressCidr =  (String) parsedOptions.get("ingresscidr");
        this.egressCidr =  (String) parsedOptions.get("egresscidr");
        
        
        
    }

    @Override
    protected void setup() throws Exception {
    	
     	//Supervisor cluster service object
    	this.ppClusterService = this.vapiAuthHelper.getStubFactory().createStub(
                Clusters.class, this.sessionStubConfig);
    	
    	System.out.println("Getting cluster identifier as part of setup");
		this.clusterId=ClusterHelper.getCluster(vapiAuthHelper.getStubFactory(), sessionStubConfig, this.clusterName);
		
		// Getting policy ID
		System.out.println("Storage policy name passed was::" + this.storagePolicy);
				this.policyService = this.vapiAuthHelper.getStubFactory().createStub(Policies.class, this.sessionStubConfig);

				List<PoliciesTypes.Summary> summaries = this.policyService.list(null);
				if (summaries != null && !summaries.isEmpty()) {
					for (Summary summary : summaries) {
						// TODO: Add NULL check conditions.
						if (summary.getName().equals(this.storagePolicy)) {
							this.storagePolicyId = summary.getPolicy();
							System.out.println("Storage policy UUID::" + summary.getPolicy());
							break;
						}
					}
					if(this.storagePolicyId == null) {
						System.out.println( "There is no storage policy matching with the one passed in the enable call");
					}
				} else {
					System.out.println( "Storage policies are not returned, please check the setup");
				}
		
		//Getting VDS uuid 
		this.VDSService = this.vapiAuthHelper.getStubFactory().createStub(
				DistributedSwitchCompatibility.class, this.sessionStubConfig);
		
		DistributedSwitchCompatibilityTypes.FilterSpec vdsFilter=new DistributedSwitchCompatibilityTypes.FilterSpec();
		vdsFilter.setCompatible(true);
		List<DistributedSwitchCompatibilityTypes.Summary> vdsSummary=this.VDSService.list(this.clusterId,vdsFilter);
		//Assuming only one DVS compatible in that cluster. Add NULL check
		this.dvsSwitchUuid=vdsSummary.get(0).getDistributedSwitch();
		System.out.println("DVS switch UUID::"+vdsSummary.get(0).getDistributedSwitch());
		
		//Getting Edge cluster id
		this.nsxEdgeService=this.vapiAuthHelper.getStubFactory().createStub(
				EdgeClusterCompatibility.class, this.sessionStubConfig);
		EdgeClusterCompatibilityTypes.FilterSpec edgeFilter=new EdgeClusterCompatibilityTypes.FilterSpec();
		edgeFilter.setCompatible(true);
		List<EdgeClusterCompatibilityTypes.Summary> edgeSummary=this.nsxEdgeService.list(this.clusterId,vdsSummary.get(0).getDistributedSwitch(),edgeFilter);
		//Assuming only one Edge compatible in that cluster. Add NULL check
		this.edgeClusterId=edgeSummary.get(0).getEdgeCluster();
		System.out.println("Edge UUID::"+edgeSummary.get(0).getEdgeCluster());
		
		//Getting master network id
		this.networkService=this.vapiAuthHelper.getStubFactory().createStub(
				Network.class, this.sessionStubConfig);
		NetworkTypes.FilterSpec networkFilter=new NetworkTypes.FilterSpec();
		Set<String> names= new HashSet<String>();
		names.add(this.masterNetwork);
		networkFilter.setNames(names);
		List<NetworkTypes.Summary> networkSummary=this.networkService.list(networkFilter);
		this.networkId=networkSummary.get(0).getNetwork();
		System.out.println("Network id::"+networkSummary.get(0).getNetwork());
		
    	
         
    }

    @Override
    protected void run() throws Exception {
        
    	
    	System.out.println("We are building the Spec for enabling vSphere supervisor cluster");
    	
    	ClustersTypes.EnableSpec spec=new ClustersTypes.EnableSpec();
    	if(this.hintSize.equals("TINY")) {
    	spec.setSizeHint(SizingHint.TINY);
    	}else if (this.hintSize.equals("SMALL")) {
    		spec.setSizeHint(SizingHint.SMALL);
    	}else if (this.hintSize.equals("MEDIUM")) {
    		spec.setSizeHint(SizingHint.MEDIUM);
    	}else if (this.hintSize.equals("LARGE")) {
    		spec.setSizeHint(SizingHint.LARGE);
    	}else {
    		System.out.println("incorrect hintSize, please enter either of TINY, SMALL, MEDIUM, LARGE");
    		//TODO exit here or anyway call will fail as well.
    	}
    	
    	Ipv4Cidr serCidr=new Ipv4Cidr();
    	String[] serviceCidrParts=this.serviceCidr.split("/");
    	serCidr.setAddress(serviceCidrParts[0]);
    	serCidr.setPrefix(Long.parseLong(serviceCidrParts[1]));
    	spec.setServiceCidr(serCidr);
    	
    	spec.setNetworkProvider(ClustersTypes.NetworkProvider.NSXT_CONTAINER_PLUGIN);
    	
    	ClustersTypes.NCPClusterNetworkEnableSpec NCPSpec=new ClustersTypes.NCPClusterNetworkEnableSpec();
    	// VDS identifier/UUID
    	NCPSpec.setClusterDistributedSwitch(this.dvsSwitchUuid); //Identifier 
    	// Edge cluster id
    	NCPSpec.setNsxEdgeCluster(this.edgeClusterId); //Edge cluster id
    	//Ingress CIDR
    	List<Ipv4Cidr> ingressList=new ArrayList<Ipv4Cidr>();
    	Ipv4Cidr ingressCidr=new Ipv4Cidr();
    	String[] ingressCidrParts=this.ingressCidr.split("/");
    	ingressCidr.setAddress(ingressCidrParts[0]);
    	ingressCidr.setPrefix(Long.parseLong(ingressCidrParts[1]));
    	ingressList.add(ingressCidr);
    	NCPSpec.setIngressCidrs(ingressList);
    	//Egress CIDR
    	List<Ipv4Cidr> egressList=new ArrayList<Ipv4Cidr>();
    	Ipv4Cidr egressCidr=new Ipv4Cidr();
    	String[] egressCidrParts=this.egressCidr.split("/");
    	egressCidr.setAddress(egressCidrParts[0]);
    	egressCidr.setPrefix(Long.parseLong(egressCidrParts[1]));
    	egressList.add(egressCidr);
    	NCPSpec.setEgressCidrs(egressList);
    	
    	//POD CIDR
    	List<Ipv4Cidr> podList=new ArrayList<Ipv4Cidr>();
    	Ipv4Cidr podCidr=new Ipv4Cidr();
    	String[] podCidrParts=this.podCidr.split("/");
    	podCidr.setAddress(podCidrParts[0]);
    	podCidr.setPrefix(Long.parseLong(podCidrParts[1]));
    	podList.add(podCidr);
    	NCPSpec.setPodCidrs(podList);
    	
    	spec.setNcpClusterNetworkSpec(NCPSpec);
    	
    	ClustersTypes.NetworkSpec masterNet=new ClustersTypes.NetworkSpec();
    	//MasterVM network identifier
    	masterNet.setNetwork(this.networkId); 
    	
    	masterNet.setMode(ClustersTypes.NetworkSpec.Ipv4Mode.DHCP); //DHCP is only for lab/educational purpose
    	masterNet.setFloatingIP(this.floatingIP);
    	
    	//un-comment below code if you want to enter static IP range. Production enviornment should use it
		/*
		 * ClustersTypes.Ipv4Range range=new ClustersTypes.Ipv4Range();
		 * range.setStartingAddress(this.startingIP); range.setAddressCount(5);
		 * //Hardcoded, please change as needed range.setGateway(this.gatewayIP);
		 * range.setSubnetMask(this.masterSubnetMask); masterNet.setAddressRange(range);
		 */
    	
    	spec.setMasterManagementNetwork(masterNet);
    	
    	//TODO: 
    	List<String> masterDNS=new ArrayList<String>();
    	masterDNS.add(this.masterDnsServer);
    	spec.setMasterDNS(masterDNS);
    	
    	List<String> workerDNS=new ArrayList<String>();
    	workerDNS.add(this.workerDnsServer); //Using same DNS for worker and Master
    	spec.setWorkerDNS(workerDNS);
    	
    	
    	List<String> NTPserver=new ArrayList<String>();
    	NTPserver.add(this.ntpServer);
    	spec.setMasterNTPServers(NTPserver);
    	
    	spec.setMasterStoragePolicy(this.storagePolicyId); //Storage policy identifier
    	spec.setEphemeralStoragePolicy(this.storagePolicyId);//Storage policy identifier
    	spec.setLoginBanner("This is your first Project pacific cluster");
    	//spec.setMasterDNSNames(masterDNSSearch); //re-using above one
    	
    	ClustersTypes.ImageStorageSpec imageSpec=new ClustersTypes.ImageStorageSpec();
    	imageSpec.setStoragePolicy(this.storagePolicyId); //Storage policy identifier 
    	spec.setImageStorage(imageSpec);
    	
    	
    	
    	
    	this.ppClusterService.enable(clusterId, spec);
    	System.out.println("Invocation is successful for enabling vSphere supervisor cluster, check H5C");
    	      
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
        new EnableSupervisorCluster().execute(args);
    }

}
