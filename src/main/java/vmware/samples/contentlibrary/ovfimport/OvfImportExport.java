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
package vmware.samples.contentlibrary.ovfimport;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.apache.commons.cli.Option;

import com.vmware.content.LibraryTypes.FindSpec;
import com.vmware.content.library.ItemModel;

import vmware.samples.common.SamplesAbstractBase;
import vmware.samples.contentlibrary.client.ClsApiClient;
import vmware.samples.contentlibrary.helpers.ItemDownloadHelper;
import vmware.samples.contentlibrary.helpers.ItemUploadHelper;

/**
 * Description: Demonstrates the workflow to import an OVF package into the 
 * content library.
 *
 * Author: VMware, Inc.
 * Sample Prerequisites: The sample needs an existing content library to place
 * the library item.
 */
public class OvfImportExport extends SamplesAbstractBase {

    private String libName;
    private String libFolderName = "simpleVmTemplate";
    private String libItemName = "descriptor.ovf";
    private String libVMDKName = "disk-0.vmdk";
    private ClsApiClient client;
    private ItemModel libItem;

    /**
     * Define the options specific to this sample and configure the sample using
     * command-line arguments or a config file
     *
     * @param args command line arguments passed to the sample
     */
    protected void parseArgs(String[] args) {
        // Parse the command line options or use config file
        Option libNameOption = Option.builder()
            .longOpt("contentlibraryname")
            .desc("The name of the content library "
                  + "where the library item will be"
                  + " created. Defaults to demo-local-lib")
            .required(true)
            .hasArg()
            .argName("CONTENT LIBRARY")
            .build();

        List<Option> optionList =
                Arrays.asList(libNameOption);
        super.parseArgs(optionList, args);
        this.libName = (String) parsedOptions.get("contentlibraryname");
    }

    /**
     * Setup authentication and other resources needed by the sample
     */
    protected void setup() {
        // Create the Content Library services with authenticated session
        this.client =
                new ClsApiClient(this.vapiAuthHelper.getStubFactory(),
                    sessionStubConfig);
    }

    /**
     * Run the sample
     *
     * @throws IOException
     */
    protected void run() throws IOException {
        // Get the template's OVF and VMDK files
        File tempDir = ItemUploadHelper.createTempDir(libFolderName);
        String ovfFile = ItemUploadHelper.copyResourceToFile(
                libFolderName+"/"+libItemName, tempDir, libItemName);
        String vmdkFile = ItemUploadHelper.copyResourceToFile(
                libFolderName+"/"+libVMDKName, tempDir, libVMDKName);
        System.out.println("OVF Path : " + ovfFile);
        System.out.println("VMDK Path : " + vmdkFile);

        // Find the content library id by name
        FindSpec findSpec = new FindSpec();
        findSpec.setName(this.libName);
        List<String> libraryIds = this.client.libraryService().find(findSpec);
        assert !libraryIds.isEmpty() : "Unable to find a library with name: "
                                       + this.libName;
        String libraryId = libraryIds.get(0);
        System.out.println("Found library : " + libraryId);

        // Build the specification for the library item to be created
        ItemModel createSpec = new ItemModel();
        createSpec.setName(this.libFolderName);
        createSpec.setLibraryId(libraryId);
        createSpec.setType("ovf");

        // Create a new library item in the content library for uploading the
        // files
        String clientToken = UUID.randomUUID().toString();
        String libItemId =
                this.client.itemService().create(clientToken, createSpec);
        this.libItem = this.client.itemService().get(libItemId);
        System.out.println("Library item created : " + this.libItem.getId());

        // Upload the files in the OVF package into the library item
        ItemUploadHelper.performUpload(this.client.updateSession(),
            this.client.updateSessionFileService(),
            this.client.itemService(),
            this.libItem.getId(),
            Arrays.asList(ovfFile, vmdkFile));
        System.out.println("Uploaded files : "
            + this.client.storageService().list(this.libItem.getId()));

        // Download the template files from the library item into a folder
        File downloadDir = ItemUploadHelper.createTempDir(libFolderName);
        ItemDownloadHelper.performDownload(
            this.client.downloadSessionService(),
            this.client.downloadSessionFileService(),
            this.client.itemService(),
            this.libItem.getId(),
            downloadDir);
        System.out.println("Downloaded files to directory : " + downloadDir);
    }

    /**
     * Cleanup any resources created by the sample, logout
     */
    protected void cleanup() {
        if (this.libItem != null) {
            // Delete the library item
            this.client.itemService().delete(this.libItem.getId());
            System.out.println("Deleted library item : "
                + this.libItem.getId());
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
        new OvfImportExport().execute(args);
    }
}
