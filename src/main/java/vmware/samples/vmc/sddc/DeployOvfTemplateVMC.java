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
package vmware.samples.vmc.sddc;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.apache.commons.cli.Option;
import org.apache.commons.lang.StringUtils;

import com.vmware.content.library.ItemTypes.FindSpec;
import com.vmware.vcenter.VM;
import com.vmware.vcenter.ovf.LibraryItemTypes.DeploymentResult;
import com.vmware.vcenter.ovf.LibraryItemTypes.DeploymentTarget;
import com.vmware.vcenter.ovf.LibraryItemTypes.OvfSummary;
import com.vmware.vcenter.ovf.LibraryItemTypes.ResourcePoolDeploymentSpec;
import com.vmware.vcenter.vm.hardware.Ethernet;
import com.vmware.vcenter.vm.hardware.EthernetTypes;
import com.vmware.vcenter.vm.hardware.EthernetTypes.BackingType;

import vmware.samples.common.SamplesAbstractBase;
import vmware.samples.contentlibrary.client.ClsApiClient;
import vmware.samples.vcenter.helpers.FolderHelper;
import vmware.samples.vcenter.helpers.NetworkHelper;
import vmware.samples.vcenter.helpers.ResourcePoolHelper;

/**
 * Demonstrates the workflow to deploy an OVF library item to a resource pool in VMware Cloud on AWS.
 * Note: the sample needs an existing library item with an OVF template
 * and an existing resource pool with resources for deploying the VM.
 *
 * Author: VMware, Inc.
 * Sample Prerequisites: The sample needs an existing OVF
 * library item and a resources for creating the VM.
 * 
 *
 */
public class DeployOvfTemplateVMC extends SamplesAbstractBase {

    private String libItemName;
    private String vmName;
    private String  resourcePoolName, opaqueNetwrokName;
	private ClsApiClient contentLibClient;
	private String resourcePoolID;
	private String vmId;
	private Ethernet ethernetService;
	private VM vmService;
	private String folderName;
	private String folderID;

    /**
     * Define the options specific to this sample and configure the sample using
     * command-line arguments or a config file
     *
     * @param args command line arguments passed to the sample
     */
    protected void parseArgs(String[] args) {
        //Parse the command line options or use config file        
        Option libItemNameOption = Option.builder()
                .longOpt("libitemname")
                .desc("REQUIRED: The name of the library item to"
                      + "deploy. The library item "
                      + "should contain an OVF package")
                .required(true)
                .hasArg()
                .argName("CONTENT LIBRARY ITEM")
                .build();

        Option resourcePoolOption = Option.builder()
        		.longOpt("resourcepoolname")
                .desc("REQUIRED: The name of the resource pool to be used.")
                .required(true)
                .hasArg()
                .argName("RESOURCE POOL")
                .build();

        Option opaqueNetworkName = Option.builder()
        		.longOpt("opaquenetworkname")
                .required(true)
                .desc("REQUIRED: The name of the opaque network to be added to the deployed vm")
                .hasArg()
                .argName("OPEQUE NETWORK NAME")
                .build();

        Option vmNameOption = Option.builder()
                .longOpt("vmname")
                .desc("OPTIONAL: The name of the VM to be created in "
                	  + "the cluster. Defaults to a generated VM name "
                	  + "based on the current date if not specified")
                .required(false)
                .hasArg()
                .argName("VM NAME")
                .build();       

        Option folderNameOption = Option.builder()
        		.longOpt("foldername")
                .required(false)
                .desc("OPTIONAL:  The name of the folder to be used. Defaults to 'Workloads'")
                .hasArg()
                .argName("FOLDER NAME")
                .build();     

        List<Option> optionList = Arrays.asList(libItemNameOption,
        		vmNameOption, resourcePoolOption, opaqueNetworkName,
        		folderNameOption);
        
        super.parseArgs(optionList, args);       
        
        this.resourcePoolName = (String) parsedOptions.get("resourcepoolname");
        this.opaqueNetwrokName =  (String) parsedOptions.get("opaquenetworkname");
        this.libItemName = (String) parsedOptions.get("libitemname");
        this.vmName =  (String) parsedOptions.get("vmname");
        this.folderName = (String) parsedOptions.get("foldername");
    }

    protected void setup() throws Exception {
        this.contentLibClient = new ClsApiClient(
        		vapiAuthHelper.getStubFactory(), sessionStubConfig);
        this.resourcePoolID = ResourcePoolHelper.getResourcePool(
        		vapiAuthHelper.getStubFactory(),
        		sessionStubConfig, resourcePoolName);
        System.out.println("this.resourcePool: "+this.resourcePoolID);
        //Generate a default VM name if it is not provided
        if (StringUtils.isBlank(this.vmName)) {
            this.vmName = "deployed-vm-opaque-Nw-"+ UUID.randomUUID();
        }
        this.ethernetService = vapiAuthHelper.getStubFactory().createStub(
                Ethernet.class, this.sessionStubConfig);
        if(clearData)
        	this.vmService = vapiAuthHelper.getStubFactory().createStub(VM.class,
                    sessionStubConfig);
        if(null == this.folderName)
        	this.folderName = "Workloads";
        this.folderID = FolderHelper.getFolder(vapiAuthHelper.getStubFactory(),
        		sessionStubConfig, this.folderName);
        
    }

    protected void run() throws Exception {
        FindSpec findSpec = new FindSpec();
        findSpec.setName(this.libItemName);
        List<String> itemIds = this.contentLibClient.itemService().find(findSpec);
        assert !itemIds.isEmpty() : "Unable to find a library item with name: "
                                    + this.libItemName;
        String libItemId = itemIds.get(0);
        System.out.println("Library item ID : " + libItemId);

        // Deploy a VM from the library item on the given cluster
        System.out.println("Deploying Vm : " + this.vmName);
        deployVMFromOvfItem(libItemId);
        assert this.vmId != null;
        System.out.println("Vm created : " + this.vmId);
        //Add an opaque network portgroup to the deployed VM
        addOpaqueNetworkPortGroup();
    }

    protected void cleanup() throws Exception {
    	if(null != this.vmId) {
    		System.out.println("\n\n#### Deleting the Deployed VM");
    		this.vmService.delete(this.vmId);
    		System.out.println("\n\n#### Deleted the Deployed VM :" + this.vmName);
    	}
    }

    /**
     * Deploying a VM from the Content Library into a cluster.
     *
     * @param libItemId identifier of the OVF library item to deploy
     * @return 
     */
    private void deployVMFromOvfItem(String libItemId) {
        // Creating the deployment.
        DeploymentTarget deploymentTarget = new DeploymentTarget();
        //Setting the target resource pool.
        deploymentTarget.setResourcePoolId(this.resourcePoolID);
        //Setting the target Folder.
        deploymentTarget.setFolderId(this.folderID);
        // Creating and setting the resource pool deployment spec.
        ResourcePoolDeploymentSpec deploymentSpec = 
        		new ResourcePoolDeploymentSpec();
        deploymentSpec.setName(this.vmName);
        deploymentSpec.setAcceptAllEULA(true);
        // Retrieve the library items OVF information and use it for populating
        // deployment spec.
        OvfSummary ovfSummary = this.contentLibClient.ovfLibraryItemService()
            .filter(libItemId, deploymentTarget);
        // Setting the annotation retrieved from the OVF summary.
        deploymentSpec.setAnnotation(ovfSummary.getAnnotation());
        // Calling the deploy and getting the deployment result.
        DeploymentResult deploymentResult = 
        		this.contentLibClient.ovfLibraryItemService()
        		.deploy(UUID.randomUUID().toString(),
                libItemId,
                deploymentTarget,
                deploymentSpec);
        if (deploymentResult.getSucceeded())
            this.vmId =  deploymentResult.getResourceId().getId();            
        else
            throw new RuntimeException(deploymentResult.getError().toString());
    }
    
    /**
     * Adds Opaque Network backing to the newly deployed VM.
     *     
     * @return 
     */
    private void addOpaqueNetworkPortGroup()
    {
        if (null != this.opaqueNetwrokName){        	
            String opaqueNetworkBacking = NetworkHelper.getOpaqueNetworkBacking(
                    vapiAuthHelper.getStubFactory(),
                    sessionStubConfig,
                    this.opaqueNetwrokName);
            //Create a nic with Opaque network backing
            EthernetTypes.BackingSpec nicBackingSpec =
                    new EthernetTypes.BackingSpec.Builder(
                        BackingType.OPAQUE_NETWORK).setNetwork(
                        		opaqueNetworkBacking).build();
            EthernetTypes.CreateSpec nicCreateSpec =
                    new EthernetTypes.CreateSpec.Builder().setStartConnected(true)
                        .setBacking(nicBackingSpec)
                        .build();
            String nicId = this.ethernetService.create(this.vmId, nicCreateSpec);
            System.out.println(nicCreateSpec);
            EthernetTypes.Info nicInfo = this.ethernetService.get(this.vmId, nicId);
            System.out.println("Ethernet NIC ID=" + nicId);
            System.out.println(nicInfo);
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
        new DeployOvfTemplateVMC().execute(args);
    }
}
