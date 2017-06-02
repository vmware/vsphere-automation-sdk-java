/*
 * *******************************************************
 * Copyright VMware, Inc. 2016.  All Rights Reserved.
 * SPDX-License-Identifier: MIT
 * *******************************************************
 *
 * DISCLAIMER. THIS PROGRAM IS PROVIDED TO YOU "AS IS" WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, WHETHER ORAL OR WRITTEN,
 * EXPRESS OR IMPLIED. THE AUTHOR SPECIFICALLY DISCLAIMS ANY IMPLIED
 * WARRANTIES OR CONDITIONS OF MERCHANTABILITY, SATISFACTORY QUALITY,
 * NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE.
 */
package vmware.samples.contentlibrary.ovfdeploy;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.commons.cli.Option;
import org.apache.commons.lang.StringUtils;

import com.vmware.content.library.ItemTypes.FindSpec;
import com.vmware.vcenter.ovf.LibraryItemTypes.DeploymentResult;
import com.vmware.vcenter.ovf.LibraryItemTypes.DeploymentTarget;
import com.vmware.vcenter.ovf.LibraryItemTypes.OvfSummary;
import com.vmware.vcenter.ovf.LibraryItemTypes.ResourcePoolDeploymentSpec;
import com.vmware.vim25.DynamicProperty;
import com.vmware.vim25.ManagedObjectReference;

import vmware.samples.common.SamplesAbstractBase;
import vmware.samples.common.vim.helpers.VimUtil;
import vmware.samples.common.vim.helpers.VmVappPowerOps;
import vmware.samples.contentlibrary.client.ClsApiClient;

/**
 * Description: Demonstrates the workflow to deploy an OVF library item to a
 * resource pool.
 *
 * Author: VMware, Inc.
 * Sample Prerequisites: The sample needs an existing OVF
 * library item and an existing cluster with resources for creating the VM."
 *
 */
public class DeployOvfTemplate extends SamplesAbstractBase {

    private String libItemName;
    private String clusterName;
    private String vmName;
    private ClsApiClient client;
    private VmVappPowerOps vmPowerOps;
    private ManagedObjectReference vmMoRef;

    /**
     * Define the options specific to this sample and configure the sample using
     * command-line arguments or a config file
     *
     * @param args command line arguments passed to the sample
     */
    protected void parseArgs(String[] args) {
        // Parse the command line options or use config file
        Option clusNameOption = Option.builder()
            .longOpt("clustername")
            .desc("Name of the Cluster in which VM would be created")
            .required(true)
            .hasArg()
            .argName("CLUSTER")
            .build();

        Option libItemNameOption = Option.builder()
            .longOpt("libitemname")
            .desc("The name of the library item to"
                  + "deploy. The library item "
                  + "should contain an OVF package")
            .required(true)
            .hasArg()
            .argName("CONTENT LIBRARY")
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

        List<Option> optionList = Arrays.asList(clusNameOption, vmNameOption,
            libItemNameOption);
        super.parseArgs(optionList, args);
        this.clusterName = (String) parsedOptions.get("clustername");
        this.libItemName = (String) parsedOptions.get("libitemname");
        this.vmName =  (String) parsedOptions.get("vmname");
    }

    protected void setup() throws Exception {
        this.client = new ClsApiClient(this.vapiAuthHelper.getStubFactory(),
            sessionStubConfig);
        this.vmPowerOps = new VmVappPowerOps(this.vimAuthHelper.getVimPort(),
            this.vimAuthHelper.getServiceContent());

        // Generate a default VM name if it is not provided
        if (StringUtils.isBlank(this.vmName)) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-kkmmss");
            this.vmName = "VM-" + sdf.format(new Date());
        }
    }

    protected void run() throws Exception {
        // Find the MoRef of the VC cluster using VIM APIs
        ManagedObjectReference clusterMoRef =
                VimUtil.getCluster(this.vimAuthHelper.getVimPort(),
                    this.vimAuthHelper.getServiceContent(),
                    this.clusterName);
        assert clusterMoRef != null : "Cluster by name " + this.clusterName
                +" must exist";
        System.out.println("Cluster MoRef : " + clusterMoRef.getType() + " : "
                           + clusterMoRef.getValue());

        // Find the cluster's root resource pool
        List<DynamicProperty> dynamicProps =
                VimUtil.getProperties(this.vimAuthHelper.getVimPort(),
                    this.vimAuthHelper.getServiceContent(),
                    clusterMoRef,
                    clusterMoRef.getType(),
                    Arrays.asList("resourcePool"));
        assert dynamicProps != null && dynamicProps.size() > 0;
        ManagedObjectReference rootResPoolMoRef =
                (ManagedObjectReference) dynamicProps.get(0).getVal();
        System.out.println("Resource pool MoRef : " + rootResPoolMoRef.getType()
                           + " : " + rootResPoolMoRef.getValue());

        // Find the library item by name
        FindSpec findSpec = new FindSpec();
        findSpec.setName(this.libItemName);
        List<String> itemIds = this.client.itemService().find(findSpec);
        assert !itemIds.isEmpty() : "Unable to find a library item with name: "
                                    + this.libItemName;
        String itemId = itemIds.get(0);
        System.out.println("Library item ID : " + itemId);

        // Deploy a VM from the library item on the given cluster
        System.out.println("Deploying Vm : " + this.vmName);
        String vmId =
                deployVMFromOvfItem(rootResPoolMoRef, this.vmName, itemId);
        assert vmId != null;
        System.out.println("Vm created : " + vmId);

        // Power on the VM and wait for the power on operation to complete
        this.vmMoRef = new ManagedObjectReference();
        this.vmMoRef.setType("VirtualMachine");
        this.vmMoRef.setValue(vmId);
        this.vmPowerOps.powerOnVM(this.vmName, this.vmMoRef);
    }

    protected void cleanup() throws Exception {
        if (this.vmMoRef != null) {
            // Power off the VM and wait for the power off operation to complete
            this.vmPowerOps.powerOffVM(this.vmName, this.vmMoRef);

            // Delete the VM
            VimUtil.deleteManagedEntity(this.vimAuthHelper.getVimPort(),
                this.vimAuthHelper.getServiceContent(),
                this.vmMoRef);
        }
    }

    /**
     * Deploying a VM from the Content Library into a cluster.
     *
     * @param rootResPoolMoRef managed object reference of the root resource
     *        pool
     * @param vmName the name of the VM to create
     * @param libItemId identifier of the OVF library item to deploy
     * @return the identifier of the created VM
     */
    private String deployVMFromOvfItem(ManagedObjectReference rootResPoolMoRef,
                                       String vmName, String libItemId) {
        // Creating the deployment.
        DeploymentTarget deploymentTarget = new DeploymentTarget();
        // Setting the target resource pool.
        deploymentTarget.setResourcePoolId(rootResPoolMoRef.getValue());
        // Creating and setting the resource pool deployment spec.
        ResourcePoolDeploymentSpec deploymentSpec =
                new ResourcePoolDeploymentSpec();
        deploymentSpec.setName(this.vmName);
        deploymentSpec.setAcceptAllEULA(true);
        // Retrieve the library items OVF information and use it for populating
        // deployment spec.
        OvfSummary ovfSummary = this.client.ovfLibraryItemService()
            .filter(libItemId, deploymentTarget);
        // Setting the annotation retrieved from the OVF summary.
        deploymentSpec.setAnnotation(ovfSummary.getAnnotation());
        // Calling the deploy and getting the deployment result.
        DeploymentResult deploymentResult = this.client.ovfLibraryItemService()
            .deploy(UUID.randomUUID().toString(),
                libItemId,
                deploymentTarget,
                deploymentSpec);
        if (deploymentResult.getSucceeded()) {
            return deploymentResult.getResourceId().getId();
        } else
            throw new RuntimeException(deploymentResult.getError().toString());
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
        new DeployOvfTemplate().execute(args);
    }
}
