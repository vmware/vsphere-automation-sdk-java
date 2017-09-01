/*
 * *******************************************************
 * Copyright VMware, Inc. 2016. All Rights Reserved.
 * SPDX-License-Identifier: MIT
 * *******************************************************
 *
 * DISCLAIMER. THIS PROGRAM IS PROVIDED TO YOU "AS IS" WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, WHETHER ORAL OR WRITTEN,
 * EXPRESS OR IMPLIED. THE AUTHOR SPECIFICALLY DISCLAIMS ANY IMPLIED
 * WARRANTIES OR CONDITIONS OF MERCHANTABILITY, SATISFACTORY QUALITY,
 * NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE.
 */
package vmware.samples.contentlibrary.vmcapture;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.apache.commons.cli.Option;

import com.vmware.content.LibraryModel;
import com.vmware.content.library.ItemTypes.FindSpec;
import com.vmware.content.library.StorageBacking;
import com.vmware.vcenter.ovf.LibraryItemTypes.CreateResult;
import com.vmware.vcenter.ovf.LibraryItemTypes.CreateSpec;
import com.vmware.vcenter.ovf.LibraryItemTypes.CreateTarget;
import com.vmware.vcenter.ovf.LibraryItemTypes.DeployableIdentity;
import com.vmware.vim25.ManagedObjectReference;

import vmware.samples.common.SamplesAbstractBase;
import vmware.samples.common.vim.helpers.VimUtil;
import vmware.samples.contentlibrary.client.ClsApiClient;
import vmware.samples.vcenter.helpers.DatastoreHelper;

/**
 * Description: Demonstrates the workflow to capture vm to content library as
 * vm template.
 *
 * Author: VMware, Inc.
 * Sample Prerequisites: The sample needs an existing VM to capture and a
 * datastore to create the
 * content library.
 *
 */
public class VmTemplateCapture extends SamplesAbstractBase {

    private String contentLibraryName;
    private String dataStoreName;
    private String vmName;

    private ClsApiClient client;
    private String libItemName;
    private String libraryId;

    /**
     * Define the options specific to this sample and configure the sample using
     * command-line arguments or a config file
     *
     * @param args
     *            command line arguments passed to the sample
     */
    protected void parseArgs(String[] args) {
        // Parse the command line options or use config file
        Option dataStoreNameOption =
                Option.builder()
                .longOpt("datastore")
                .desc("The name of the datastore for content library backing "
                        + "(of type vmfs)")
                .required(true)
                .hasArg()
                .argName("DATASTORE")
                .build();

        Option vmNameOption =
                Option.builder()
                .longOpt("vmname")
                .desc("The Name of the vm to be captured")
                .required(true)
                .hasArg()
                .argName("VM NAME")
                .build();

        List<Option> optionList = Arrays.asList(dataStoreNameOption,
                vmNameOption);
        super.parseArgs(optionList, args);
        this.dataStoreName = (String) parsedOptions.get("datastore");
        this.vmName = (String) parsedOptions.get("vmname");
        this.contentLibraryName = "LocalLibraryToCapture";
        this.libItemName = "capturedItem";
    }

    protected void setup() throws Exception {
        this.client = new ClsApiClient(this.vapiAuthHelper.getStubFactory(),
                sessionStubConfig);
    }

    protected void run() throws Exception {        
        // Create content library.
        this.libraryId = createLocalLib();

        // Capture the vm to content library.
        captureVM(this.libraryId);

        // Find the library item by name and verify capture vm created new vm
        // template.
        FindSpec findSpec = new FindSpec();
        findSpec.setName(this.libItemName);
        List<String> itemIds = this.client.itemService().find(findSpec);
        assert !itemIds
                .isEmpty() : "Unable to find captured library item with name: "
                        + this.libItemName;
        String itemId = itemIds.get(0);
        System.out.println(
                "The VM : " + this.vmName + " is captured as library item  : "
                        + itemId + " of type vm-template.");
    }

    /**
     * Capture the VM to the local library provided.
     *
     * @param libId
     *            identifier of the library on which vm will be captured
     */
    private void captureVM(String libraryId) throws Exception {
        String entityType = "VirtualMachine"; // Substitute 'VirtualApp' for
                                              // vApp
        ManagedObjectReference entityId = VimUtil.getVM(
                this.vimAuthHelper.getVimPort(),
                this.vimAuthHelper.getServiceContent(), this.vmName);
        DeployableIdentity deployable = new DeployableIdentity();
        deployable.setType(entityType);
        deployable.setId(entityId.getValue());

        CreateTarget target = new CreateTarget();
        target.setLibraryId(libraryId);
        CreateSpec spec = new CreateSpec();
        spec.setName(this.libItemName);
        spec.setDescription("VM template created from a VM capture");

        // Create OVF library item
        CreateResult result = client.ovfLibraryItemService().create(null,
                deployable, target, spec);
        if (result.getSucceeded()) {
            System.out.println("The vm capture to content library succeeded");
        } else {
            System.out.println("The vm capture to content library failed");
        }
    }

    /**
     * Create Local Library on the input datastore provided.
     *
     *
	 * @return the identifier of the created library
	 */
    private String createLocalLib() {
        // Build the storage backing for the library to be created
        StorageBacking storage = DatastoreHelper.createStorageBacking(
                this.vapiAuthHelper, this.sessionStubConfig,
                this.dataStoreName);
        // Build the specification for the library to be created
        LibraryModel createSpec = new LibraryModel();
        createSpec.setName(this.contentLibraryName);
        createSpec.setDescription("Local library backed by VC datastore");
        createSpec.setType(LibraryModel.LibraryType.LOCAL);
        createSpec.setStorageBackings(Collections.singletonList(storage));

        // Create a local content library backed the VC datastore using vAPIs
        String clientToken = UUID.randomUUID().toString();
        String libraryId = this.client.localLibraryService().create(clientToken,
                createSpec);
        System.out.println("Local library created : " + libraryId);
        return libraryId;
    }

    protected void cleanup() throws Exception {
        if (this.libraryId != null) {
            // Delete the content library
            this.client.localLibraryService().delete(this.libraryId);
            System.out.println("Deleted library : " + this.libraryId);
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
        new VmTemplateCapture().execute(args);
    }
}
