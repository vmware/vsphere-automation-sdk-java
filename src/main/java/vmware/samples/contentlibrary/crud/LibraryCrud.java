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
package vmware.samples.contentlibrary.crud;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.apache.commons.cli.Option;

import com.vmware.content.LibraryModel;
import com.vmware.content.library.StorageBacking;

import vmware.samples.common.SamplesAbstractBase;
import vmware.samples.contentlibrary.client.ClsApiClient;
import vmware.samples.vcenter.helpers.DatastoreHelper;

/**
 * Description: Demonstrates the basic operations of a content library. The
 * sample also demonstrates the interoperability of the VIM and vAPI.
 * 
 * Note: The sample needs an existing VC datastore with available storage.
 *
 * Author: VMware, Inc.
 */
public class LibraryCrud extends SamplesAbstractBase {

    private String dsName;
    private String libName = "demo-local-lib";
    private ClsApiClient client;
    private LibraryModel localLibrary;

    /**
     * Define the options specific to this sample and configure the sample using
     * command-line arguments or a config file
     *
     * @param args command line arguments passed to the sample
     */
    protected void parseArgs(String[] args) {
        // Parse the command line options or use config file
        Option dsNameOption = Option.builder()
            .required(true)
            .hasArg()
            .argName("DATASTORE")
            .longOpt("datastore")
            .desc("The name of the VC datastore to be used for the local "
                  + "library.")
            .build();
        Option libNameOption = Option.builder()
                .longOpt("contentlibraryname")
                .desc("OPTIONAL: The name of the local content library "
                      + "to be created.")
                .required(false)
                .hasArg()
                .argName("CONTENT LIBRARY")
                .build();

        List<Option> optionList = Arrays.asList(dsNameOption, libNameOption);
        super.parseArgs(optionList, args);
        this.dsName = (String) parsedOptions.get("datastore");
        String tmpLibName = (String) parsedOptions.get("contentlibraryname");
        this.libName = (null == tmpLibName || tmpLibName.isEmpty())
        		? this.libName:tmpLibName;
    }

    protected void setup() throws Exception {
        this.client = new ClsApiClient(this.vapiAuthHelper.getStubFactory(),
            sessionStubConfig);
    }

    protected void run() throws Exception {
        // List of visible content libraries
        List<String> visibleCls = client.localLibraryService().list();
        System.out.println("All libraries : " + visibleCls);
        //Build the storage backing for the libraries to be created
        StorageBacking storageBacking = DatastoreHelper.createStorageBacking(
                this.vapiAuthHelper, this.sessionStubConfig, this.dsName );

        // Build the specification for the library to be created
        LibraryModel createSpec = new LibraryModel();
        createSpec.setName(this.libName);
        createSpec.setDescription("Local library backed by VC datastore");
        createSpec.setType(LibraryModel.LibraryType.LOCAL);
        createSpec.setStorageBackings(Collections.
                singletonList(storageBacking));

        // Create a local content library backed the VC datastore using vAPIs
        String clientToken = UUID.randomUUID().toString();
        String libraryId = this.client.localLibraryService()
                                      .create(clientToken, createSpec);
        System.out.println("Local library created : " + libraryId);

        // Retrieve the local content library
        this.localLibrary = this.client.localLibraryService().get(libraryId);
        System.out.println("Retrieved library : " + localLibrary);

        // Update the local content library
        LibraryModel updateSpec = new LibraryModel();
        updateSpec.setDescription("new description");
        this.client.localLibraryService().update(libraryId, updateSpec);
        System.out.println("Updated library description");
    }
    protected void cleanup() throws Exception {
        if (localLibrary != null) {
            // Delete the content library
            this.client.localLibraryService().
            		delete(this.localLibrary.getId());
            System.out.println("Deleted Local Content Library : " 
                    + this.localLibrary.getId());
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
        new LibraryCrud().execute(args);
    }
}