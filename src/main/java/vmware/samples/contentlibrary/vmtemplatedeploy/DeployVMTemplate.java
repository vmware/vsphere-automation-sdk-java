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
package vmware.samples.contentlibrary.vmtemplatedeploy;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.cli.Option;
import org.apache.commons.lang.StringUtils;

import com.vmware.content.library.ItemTypes;
import com.vmware.vcenter.VM;
import com.vmware.vcenter.vm_template.LibraryItems;
import com.vmware.vcenter.vm_template.LibraryItemsTypes;

import vmware.samples.common.SamplesAbstractBase;
import vmware.samples.contentlibrary.client.ClsApiClient;
import vmware.samples.vcenter.helpers.DatastoreHelper;
import vmware.samples.vcenter.helpers.FolderHelper;
import vmware.samples.vcenter.helpers.ResourcePoolHelper;

/**
 * Demonstrates how to deploy a virtual machine from a library item containing a virtual machine template.
 *    Prerequisites:
 *        - A library item containing a virtual machine template
 *        - A datacenter
 *        - A VM folder
 *        - A resource pool
 *        - A datastore
 */
public class DeployVMTemplate extends SamplesAbstractBase {

	private String vmFolderName;
	private String vmName;
	private String datastoreName;
	private String datacenterName;
	private String resourcePoolName;
	private String libItemName;
	private String vmId;
	private ClsApiClient client;
	private VM vmService;
	private LibraryItems vmLibraryItemService;

	/**
	 * Define the options specific to this sample and configure the sample using
	 * command-line arguments or a config file
	 *
	 * @param args command line arguments passed to the sample
	 */
	protected void parseArgs(String[] args) {
		// Parse the command line options or use config file
		Option vmFolderOption = Option.builder()
				.longOpt("vmfolder")
				.desc("The name of the vm folder in which to create the vm.")
				.argName("VM FOLDER")
				.required(true)
				.hasArg()
				.build();
		
		Option vmNameOption = Option.builder()
				.longOpt("vmname")
				.desc("OPTIONAL: The name of the vm to be created.")
				.argName("VMNAME")
				.required(false)
				.hasArg()
				.build();
		
		Option datastoreOption = Option.builder()
				.longOpt("datastore")
				.desc("The name of the datastore in which to create the vm")
				.required(true)
				.argName("DATASTORE")
				.hasArg()
				.build();
		
		Option datacenterOption = Option.builder()
				.longOpt("datacenter")
				.desc("The name of the datacenter in which to create the vm.")
				.argName("DATACENTER")
				.required(true)
				.hasArg()
				.build();
		
		Option resourcePoolOption = Option.builder()
				.longOpt("resourceool")
				.desc("The name of the resource pool in the datacenter in which to place the deployed VM.")
				.argName("RESOURCEPOOL")
				.required(true)
				.hasArg()
				.build();
		
		Option libItemNameOption = Option.builder()
				.longOpt("libitemname")
				.desc("The name of the vm template in a content library")
				.required(true)
				.hasArg()
				.argName("VMTEMPLATENAME")
				.build();

		List<Option> optionList = Arrays.asList(
				vmFolderOption, 
				vmNameOption, 
				datastoreOption, 
				datacenterOption,
				resourcePoolOption, 
				libItemNameOption);

		super.parseArgs(optionList, args);

		this.vmFolderName = (String) parsedOptions.get("vmfolder");
		this.vmName = (String) parsedOptions.get("vmname");
		this.datastoreName = (String) parsedOptions.get("datastore");
		this.datacenterName = (String) parsedOptions.get("datacenter");
		this.resourcePoolName = (String) parsedOptions.get("resourceool");
		this.libItemName = (String) parsedOptions.get("libitemname");
	}

	protected void setup() throws Exception {
		this.client = new ClsApiClient(this.vapiAuthHelper.getStubFactory(), sessionStubConfig);
		this.vmService = vapiAuthHelper.getStubFactory().createStub(VM.class, sessionStubConfig);
		this.vmLibraryItemService = vapiAuthHelper.getStubFactory().createStub(LibraryItems.class, sessionStubConfig);

		// Generate a default VM name if it is not provided
		if (StringUtils.isBlank(this.vmName)) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-kkmmss");
			this.vmName = "VM-" + sdf.format(new Date());
		}
	}

	protected void run() throws Exception {
		// find template id by name
		String templateId = findTemplateId();

		// Find folder id by name
		String folderId = findFolderId();

		// Find resource Pool Id by name
		String resourcePoolId = findResourcePoolId();

		// Find datastore Id by name
		String datastoreId = findDatastoreId();

		// Specify the place in the inventory on which to deploy the VM such as an ESXi
		// host,resource pool, and VM folder
		// If getHost() and getResourcePool() are both specified, getResourcePool() must
		// belong to getHost().
		// If getHost() and getCluster() are both specified, getHost() must be a member
		// of getCluster().
		// This property may be null if getResourcePool() or getCluster() is specified.
		LibraryItemsTypes.DeployPlacementSpec placementSpec = new LibraryItemsTypes.DeployPlacementSpec
				.Builder()
				.setResourcePool(resourcePoolId) // Resource pool into which the deployed virtual machine should be placed.
				.setFolder(folderId) // Required. Virtual machine folder into which the deployed virtual machine should be placed.
				.build();

		// Specify the place in the inventory on which to deploy the virtual machine
		// such as an ESXi host, resource pool, and VM folder.
		LibraryItems.DeploySpecVmHomeStorage vmHomeStorageSpec = new LibraryItemsTypes.DeploySpecVmHomeStorage
				.Builder()
				.setDatastore(datastoreId)
				.build();
		
		LibraryItems.DeploySpecDiskStorage diskStorageSpec = new LibraryItemsTypes.DeploySpecDiskStorage
				.Builder()
				.setDatastore(datastoreId)
				.build();

		// (Optional) Specify the guest operating system and hardware customization
		// specifications that you want to
		// apply to the VM during the deployment process and include them in the
		// deployment specification.
		// You can use the GuestCustomizationSpec and HardwareCustomizationSpec classes

		// deployment specification
		LibraryItems.DeploySpec spec = new LibraryItems.DeploySpec
				.Builder(this.vmName)
				.setPlacement(placementSpec)
				.setVmHomeStorage(vmHomeStorageSpec)
				.setDiskStorage(diskStorageSpec)
				.build();

		// Deploy a virtual machine from the VM template item
		System.out.println("\nDeploying a virtual machine from VM template item...");
		this.vmId = this.vmLibraryItemService.deploy(templateId, spec);

		assert this.vmId != null;
		
		System.out.println("\nVm " + this.vmName + " created with id: " + this.vmId);

	}

	private String findDatastoreId() {
		String datastoreId = DatastoreHelper.getDatastore(
				this.vapiAuthHelper.getStubFactory(), 
				sessionStubConfig,
				this.datastoreName);

		assert !datastoreId.isEmpty() : "Unable to find datastore : " + this.datastoreName;

		System.out.println("\nDatastore Id found: " + datastoreId);

		return datastoreId;
	}

	private String findResourcePoolId() {
		String resourcePoolId = ResourcePoolHelper.getResourcePool(
				this.vapiAuthHelper.getStubFactory(),
				sessionStubConfig, 
				this.resourcePoolName);

		assert !resourcePoolId.isEmpty() : "Unable to find resource Pool : " + this.resourcePoolName;

		System.out.println("\nResource Pool Id found: " + resourcePoolId);

		return resourcePoolId;
	}

	private String findFolderId() {
		String folderId = FolderHelper.getFolder(
				this.vapiAuthHelper.getStubFactory(), 
				sessionStubConfig,
				this.datacenterName, 
				this.vmFolderName);

		assert !folderId.isEmpty() : "Unable to find folder: " + this.vmFolderName;

		System.out.println("\nFolder id found: " + folderId);

		return folderId;
	}

	private String findTemplateId() {

		ItemTypes.FindSpec findSpec = new ItemTypes.FindSpec();
		findSpec.setName(this.libItemName);
		List<String> itemIds = this.client.itemService().find(findSpec);

		assert !itemIds.isEmpty() : "Unable to find template with name: " + this.libItemName;

		String templateId = itemIds.get(0);

		System.out.println("\nTemplate id found: " + templateId);

		return templateId;
	}

	protected void cleanup() throws Exception {

		// Delete the VM
		System.out.println("\n\n#### Deleting the VM");
		if (this.vmId != null) {
			this.vmService.delete(this.vmId);
		}
	}

	public static void main(String[] args) throws Exception {
		/*
		 * Execute the sample using the command line arguments or parameters from the configuration file. 
		 * This executes the following steps: 
		 * 1. Parse the arguments required by the sample 
		 * 2. Login to the server 
		 * 3. Setup any resources required by the sample run 
		 * 4. Run the sample 
		 * 5. Cleanup any data created by the sample run, if cleanup=true 
		 * 6. Logout of the server
		 */
		new DeployVMTemplate().execute(args);
	}
}
