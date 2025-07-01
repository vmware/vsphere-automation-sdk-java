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
package vmware.samples.contentlibrary.contentupdate;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.cli.Option;
import org.apache.commons.lang.StringUtils;

import com.vmware.content.LibraryModel;
import com.vmware.content.LibraryTypes;
import com.vmware.content.library.ItemModel;
import com.vmware.content.library.StorageBacking;
import com.vmware.content.library.item.UpdateSessionModel;
import com.vmware.content.library.item.updatesession.FileTypes;

import vmware.samples.common.SamplesAbstractBase;
import vmware.samples.contentlibrary.client.ClsApiClient;
import vmware.samples.contentlibrary.helpers.ItemUploadHelper;

/**
 * Description: Demonstrates content library item content updates using
 * UpdateSession API.
 *
 * Author: VMware, Inc.
 * 
 * Sample Prerequisites: This sample needs an existing
 * content library to create and update library item.
 */
public class ContentUpdate extends SamplesAbstractBase {

    private static final String OVF_ITEM_TYPE = "ovf";
    private static final String ISO_ITEM_TYPE = "iso";

    private static final String OVF_ITEM_ONE_FOLDER_NAME = "simpleVmTemplate";
    private static final String OVF_ITEM_ONE_OVF_FILE_NAME = "descriptor.ovf";
    private static final String OVF_ITEM_ONE_VMDK_FILE_NAME = "disk-0.vmdk";
    private static final String OVF_ITEM_TWO_FOLDER_NAME = "plainVmTemplate";
    private static final String OVF_ITEM_TWO_OVF_FILE_NAME = "plain-vm.ovf";
    private static final String OVF_ITEM_TWO_VMDK_FILE_NAME = "plain-vm.vmdk";
    private static final String ISO_ITEM_FOLDER_NAME = "isoImages";
    private static final String ISO_ITEM_ONE_ISO_FILE_NAME = "test.iso";
    private static final String ISO_ITEM_TWO_ISO_FILE_NAME = "small.iso";

    private String libName;
    private String ovfItemName = "descriptorovf";
    private String isoItemName = "smalliso";
    private ClsApiClient client;

    private String ovfItemId;
    private String isoItemId;

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
                  + "where the library item will be created.")
            .required(true)
            .hasArg()
            .argName("CONTENT LIBRARY")
            .build();

        List<Option> optionList = Collections.singletonList(libNameOption);
        super.parseArgs(optionList, args);
        this.libName = (String) parsedOptions.get("contentlibraryname");
    }

    protected void setup() throws Exception {
        // Create Content Library client with authenticated session
        this.client =
                new ClsApiClient(this.vapiAuthHelper.getStubFactory(),
                    this.sessionStubConfig);
    }

    protected void run() throws Exception {
        // Find the content library id by name
        assert StringUtils.isNotBlank(this.libName) : "Local library name must "
                                                      + "be provided";
        LibraryTypes.FindSpec findSpec = new LibraryTypes.FindSpec();
        findSpec.setName(this.libName);
        List<String> libraryIds = this.client.libraryService().find(findSpec);
        assert !libraryIds.isEmpty() : "Unable to find a library with name: "
                                       + this.libName;
        String libraryId = libraryIds.get(0);
        System.out.println("Found library : " + libraryId);
        LibraryModel localLibrary = this.client.localLibraryService().
              get(libraryId);
        for (Iterator<StorageBacking> iterator = 
            localLibrary.getStorageBackings().iterator(); iterator
                .hasNext();) {
            StorageBacking sbackingtmp = (StorageBacking) iterator.next();
            System.out.println("The DataStore ID of the"+
                    " Content Librarby::"
                    + sbackingtmp.getDatastoreId());
        }
        // Content update scenario 1:
        // Update OVF library item by creating an update session for the
        // OVF item, removing all existing files in the session, then
        // adding all new files into the same update session, and completing
        // the session to finish the content update.

        // Create an OVF item and upload initial content.
        this.ovfItemId = createOvfItem(libraryId);
        ItemModel ovfItem = this.client.itemService().get(this.ovfItemId);
        String contentVersionBeforeUpdate = ovfItem.getContentVersion();
        System.out.println("OVF Library Item Created : " + this.ovfItemId
                           + ", content version: "
                           + contentVersionBeforeUpdate);

        // Update the OVF item with new OVF template via UpdateSession API.
        UpdateSessionModel updateSessionModel = new UpdateSessionModel();
        updateSessionModel.setLibraryItemId(this.ovfItemId);
        String sessionId = this.client.updateSession()
            .create(null /* clientToken */, updateSessionModel);

        // Delete all existing files
        List<FileTypes.Info> existingFiles =
                this.client.updateSessionFileService().list(sessionId);
        for (FileTypes.Info fileInfo : existingFiles) {
            this.client.updateSessionFileService().remove(sessionId,
                fileInfo.getName());
        }

        // Upload new files and complete the update session.
        Map<String, String> filePathMap =
                getVmTemplateFiles(OVF_ITEM_TWO_FOLDER_NAME,
                    OVF_ITEM_TWO_OVF_FILE_NAME,
                    OVF_ITEM_TWO_VMDK_FILE_NAME);
        ItemUploadHelper.uploadFile(this.client.updateSessionFileService(),
            sessionId,
            OVF_ITEM_TWO_OVF_FILE_NAME,
            filePathMap.get(OVF_ITEM_TWO_OVF_FILE_NAME));
        ItemUploadHelper.uploadFile(this.client.updateSessionFileService(),
            sessionId,
            OVF_ITEM_TWO_VMDK_FILE_NAME,
            filePathMap.get(OVF_ITEM_TWO_VMDK_FILE_NAME));

        this.client.updateSession().complete(sessionId);

        // Verify that the item content version increases by one.
        ovfItem = this.client.itemService().get(this.ovfItemId);
        String contentVersionAfterUpdate = ovfItem.getContentVersion();
        System.out.println("OVF Library Item Updated : " + this.ovfItemId
                           + ", content version: " + contentVersionAfterUpdate);
        assert Integer.parseInt(contentVersionBeforeUpdate) + 1 == Integer
            .parseInt(contentVersionAfterUpdate);

        // Content update scenario 2:
        // Update ISO library item by creating an update session for the
        // item, then adding the new ISO file using the same session file
        // name into the update session, which will update the existing
        // ISO file upon session complete.

        // Create a new ISO item in the content library and upload
        // the initial ISO file.
        this.isoItemId = createIsoItem(libraryId);
        ItemModel isoItem = this.client.itemService().get(this.isoItemId);
        contentVersionBeforeUpdate = isoItem.getContentVersion();
        System.out.println("ISO Library Item Created : " + this.isoItemId
                           + ", content version: "
                           + contentVersionBeforeUpdate);

        // Replace the existing ISO file in the ISO item with a new ISO
        // file with the same session file name via UpdateSession API.
        updateSessionModel = new UpdateSessionModel();
        updateSessionModel.setLibraryItemId(this.isoItemId);
        sessionId = this.client.updateSession().create(null /* clientToken */,
            updateSessionModel);

        String isoFilePath =
                getIsoFile(ISO_ITEM_FOLDER_NAME, ISO_ITEM_TWO_ISO_FILE_NAME);
        ItemUploadHelper.uploadFile(this.client.updateSessionFileService(),
            sessionId,
            ISO_ITEM_ONE_ISO_FILE_NAME,
            isoFilePath);

        this.client.updateSession().complete(sessionId);

        // Verify that item content version increases by one.
        isoItem = this.client.itemService().get(this.isoItemId);
        contentVersionAfterUpdate = isoItem.getContentVersion();
        System.out.println("ISO Library Item Updated : " + this.isoItemId
                           + ", content version: " + contentVersionAfterUpdate);
        assert Integer.parseInt(contentVersionBeforeUpdate) + 1 == Integer
            .parseInt(contentVersionAfterUpdate);
    }

    /**
     * Clean resources created from this sample.
     *
     * @throws Exception when errors like I/O occur
     */
    protected void cleanup() throws Exception {
        if (this.ovfItemId != null) {
            System.out.println("Deleting Library Item : " + this.ovfItemId);
            this.client.itemService().delete(this.ovfItemId);
        }

        if (this.isoItemId != null) {
            System.out.println("Deleting Library Item : " + this.isoItemId);
            this.client.itemService().delete(this.isoItemId);
        }
    }

    /**
     * Create an OVF item with OVF files uploaded.
     *
     * @param libraryId ID of the library
     * @return OVF item ID
     * @throws IOException when an I/O error occurs
     */
    private String createOvfItem(String libraryId) throws IOException {
        ItemModel ovfLibItem =
                createLibraryItem(libraryId, this.ovfItemName, OVF_ITEM_TYPE);
        Map<String, String> filePathMap =
                getVmTemplateFiles(OVF_ITEM_ONE_FOLDER_NAME,
                    OVF_ITEM_ONE_OVF_FILE_NAME,
                    OVF_ITEM_ONE_VMDK_FILE_NAME);
        List<String> fileLocations =
                Arrays.asList(filePathMap.get(OVF_ITEM_ONE_OVF_FILE_NAME),
                    filePathMap.get(OVF_ITEM_ONE_VMDK_FILE_NAME));
        ItemUploadHelper.performUpload(this.client.updateSession(),
            this.client.updateSessionFileService(),
            this.client.itemService(),
            ovfLibItem.getId(),
            fileLocations);
        assert ovfLibItem != null;
        assert this.client.itemService().list(libraryId).size() > 0;
        return ovfLibItem.getId();
    }

    /**
     * Create an ISO item with ISO file uploaded.
     *
     * @param libraryId ID of the library
     * @return ISO item ID
     * @throws IOException when an I/O error occurs
     */
    private String createIsoItem(String libraryId) throws IOException {
        String isoFilePath =
                getIsoFile(ISO_ITEM_FOLDER_NAME, ISO_ITEM_ONE_ISO_FILE_NAME);
        ItemModel isoLibItem =
                createLibraryItem(libraryId, this.isoItemName, ISO_ITEM_TYPE);
        ItemUploadHelper.performUpload(this.client.updateSession(),
            this.client.updateSessionFileService(),
            this.client.itemService(),
            isoLibItem.getId(),
            Arrays.asList(isoFilePath));
        assert isoLibItem != null;
        assert this.client.itemService().list(libraryId).size() > 0;
        return isoLibItem.getId();
    }

    /**
     * Generate and return OVF and VMDK files from class resources with the
     * given OVF and VMDK file names.
     *
     * @param folderName the name of the folder that contains both files
     * @param ovfFileName the name of the OVF file
     * @param diskFileName the name of the VMDK file
     * @return map of OVF file and VMDK file absolute path as below: {
     *         <ovf-file-name>: <ovf-file-path>, <vmdk-file-name>:
     *         <vmdk-file-path>}
     * @throws IOException when an I/O error occurs
     */
    private Map<String, String>
            getVmTemplateFiles(String folderName, String ovfFileName,
                               String diskFileName) throws IOException {
        Map<String, String> filePathMap = new HashMap<>();
        java.io.File tempDir = ItemUploadHelper.createTempDir(folderName);
        String ovfFile = ItemUploadHelper.copyResourceToFile(
            folderName + "/" + ovfFileName, tempDir, ovfFileName);
        filePathMap.put(ovfFileName, ovfFile);
        String vmdkFile = ItemUploadHelper.copyResourceToFile(
            folderName + "/" + diskFileName, tempDir, diskFileName);
        filePathMap.put(diskFileName, vmdkFile);

        System.out.println("OVF Path : " + ovfFile);
        System.out.println("VMDK Path : " + vmdkFile);

        return filePathMap;
    }

    /**
     * Generate and return the ISO file from the class resources with the given
     * ISO file name.
     *
     * @param folderName the name of the folder that contains the ISO file
     * @param isoFileName the name of the ISO file
     * @return the absolute path to the ISO file
     * @throws IOException when an I/O error occurs
     */
    private String getIsoFile(String folderName, String isoFileName)
            throws IOException {
        java.io.File tempDir = ItemUploadHelper.createTempDir(folderName);
        String isoFile = ItemUploadHelper.copyResourceToFile(
            folderName + "/" + isoFileName, tempDir, isoFileName);

        System.out.println("Iso Image Path : " + isoFile);
        return isoFile;
    }

    /**
     * Create a library item in the specified library
     *
     * @param libraryId ID of the library
     * @param libItemName name of the library item
     * @param itemType type of the library item
     * @return {@link ItemModel}
     */
    private ItemModel createLibraryItem(String libraryId, String libItemName,
                                        String itemType) {
        // get the library item spec
        ItemModel libItemSpec = getLibraryItemSpec(libraryId,
            libItemName,
            "item update",
            itemType);
        // create a library item
        String libItemId = this.client.itemService()
            .create(UUID.randomUUID().toString(), libItemSpec);
        return this.client.itemService().get(libItemId);
    }

    /**
     * Construct a library item spec.
     *
     * @param libraryId ID of the library
     * @param name the name of the library item
     * @param description the description of the library item
     * @param type type of the library item
     * @return {@link ItemModel}
     */
    private ItemModel getLibraryItemSpec(String libraryId, String name,
                                         String description, String type) {

        ItemModel libItemSpec = new ItemModel();
        libItemSpec.setName(name);
        libItemSpec.setDescription(description);
        libItemSpec.setLibraryId(libraryId);
        libItemSpec.setType(type);
        return libItemSpec;
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
        new ContentUpdate().execute(args);
    }
}
