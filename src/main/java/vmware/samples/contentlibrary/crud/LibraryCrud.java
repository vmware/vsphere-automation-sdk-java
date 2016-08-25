/*
 * *******************************************************
 * Copyright VMware, Inc. 2016.  All Rights Reserved.
 * *******************************************************
 *
 * DISCLAIMER. THIS PROGRAM IS PROVIDED TO YOU "AS IS" WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, WHETHER ORAL OR WRITTEN,
 * EXPRESS OR IMPLIED. THE AUTHOR SPECIFICALLY DISCLAIMS ANY IMPLIED
 * WARRANTIES OR CONDITIONS OF MERCHANTABILITY, SATISFACTORY QUALITY,
 * NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE.
 */
package vmware.samples.contentlibrary.crud;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.apache.commons.cli.Option;

import com.vmware.content.LibraryModel;
import com.vmware.content.library.StorageBacking;
import com.vmware.vim25.ManagedObjectReference;

import vmware.samples.common.SamplesAbstractBase;
import vmware.samples.contentlibrary.client.ClsApiClient;
import vmware.samples.vim.helpers.VimUtil;

/**
 * Demonstrates the basic operations of a content library. The sample also
 * demonstrates the interoperability of the VIM and vAPI. Note: The sample needs
 * an existing VC datastore with available storage.
 *
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
        Option libNameOption = Option.builder()
            .required(true)
            .hasArg()
            .argName("CONTENT LIBRARY")
            .longOpt("contentlibraryname")
            .desc("The name of the local library to "
                  + "create. Defaults to demo-local-lib, if not "
                  + "specified.")
            .build();

        Option dsNameOption = Option.builder()
            .required(true)
            .hasArg()
            .argName("DATASTORE")
            .longOpt("datastore")
            .desc("The name of the VC datastore to be used for the local "
                  + "library.")
            .build();

        List<Option> optionList = new ArrayList<Option>();
        optionList.add(libNameOption);
        optionList.add(dsNameOption);
        super.parseArgs(optionList, args);
        this.libName = (String) parsedOptions.get("contentlibraryname");
        this.dsName = (String) parsedOptions.get("datastore");
    }

    protected void setup() throws Exception {
        this.client = new ClsApiClient(this.vapiAuthHelper.getStubFactory(),
            sessionStubConfig);
    }

    protected void run() throws Exception {
        // List of visible content libraries
        List<String> visibleCls = client.localLibraryService().list();
        System.out.println("All libraries : " + visibleCls);

        // Retrieve the MoRef of a VC datastore using VIM APIs
        ManagedObjectReference dsMoref = VimUtil.getEntityByName(
            this.vimAuthHelper.getVimPort(),
            this.vimAuthHelper.getServiceContent(),
            this.dsName,
            "Datastore");
        assert dsMoref != null;
        System.out.println("Datastore MoRef : " + dsMoref.getType() + " : "
                           + dsMoref.getValue());

        // Build the storage backing for the library to be created
        StorageBacking storage = new StorageBacking();
        storage.setType(StorageBacking.Type.DATASTORE);
        storage.setDatastoreId(dsMoref.getValue());

        // Build the specification for the library to be created
        LibraryModel createSpec = new LibraryModel();
        createSpec.setName(this.libName);
        createSpec.setDescription("Local library backed by VC datastore");
        createSpec.setType(LibraryModel.LibraryType.LOCAL);
        createSpec.setStorageBackings(Collections.singletonList(storage));

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
            this.client.localLibraryService().delete(this.localLibrary.getId());
            System.out.println("Deleted library : " + this.localLibrary
                .getId());
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
