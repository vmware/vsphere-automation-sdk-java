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
package vmware.samples.contentlibrary.publishsubscribe;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.commons.cli.Option;

import com.vmware.content.LibraryModel;
import com.vmware.content.LibraryModel.LibraryType;
import com.vmware.content.library.ItemModel;
import com.vmware.content.library.PublishInfo;
import com.vmware.content.library.StorageBacking;
import com.vmware.content.library.SubscriptionInfo;

import vmware.samples.common.SamplesAbstractBase;
import vmware.samples.contentlibrary.client.ClsApiClient;
import vmware.samples.contentlibrary.helpers.ClsApiHelper;
import vmware.samples.contentlibrary.helpers.ItemUploadHelper;
import vmware.samples.vcenter.helpers.DatastoreHelper;

/**
 * Description: Demonstrates the workflow to publish and subscribe content
 * libraries.
 *
 * Author: VMware, Inc.
 * Sample Prerequisites: The sample needs an existing VC datastore with
 * available storage
 *
 */
public class LibraryPublishSubscribe extends SamplesAbstractBase {

    private static final String VCSP_USERNAME = "vcsp";
    private static final char[] DEMO_PASSWORD = "Password!23".toCharArray();
    private static final long SYNC_TIMEOUT_SEC = 60;

    private String dsName;
    private String pubLibName = "demo-publib";
    private String subLibName = "demo-sublib";

    private ClsApiClient client;
    private ClsApiHelper clsHelper;
    private String pubLibId;
    private String subLibId;

    /**
     * Define the options specific to this sample and configure the sample using
     * command-line arguments or a config file
     *
     * @param args command line arguments passed to the sample
     */
    protected void parseArgs(String[] args) {
        Option dsNameOption = Option.builder()
            .longOpt("datastore")
            .desc("The name of the VC datastore to be used for the "
                  + "published and subscribed libraries.")
            .required(true)
            .hasArg()
            .argName("DATASTORE")
            .build();

        List<Option> optionList = Collections.singletonList(dsNameOption);
        super.parseArgs(optionList, args);
        this.dsName = (String) parsedOptions.get("datastore");
    }

    protected void setup() throws Exception {
        // Create the Content Library services with authenticated session
        this.client = new ClsApiClient(this.vapiAuthHelper.getStubFactory(),
            sessionStubConfig);
        this.clsHelper = new ClsApiHelper(this.client);
    }

    protected void run() throws Exception {
        // Build the storage backing for the libraries to be created
        StorageBacking storageBacking = DatastoreHelper.createStorageBacking(
                this.vapiAuthHelper, this.sessionStubConfig, this.dsName );

        // Build the authenticated publish information.
        // The username defaults to "vcsp".
        PublishInfo pubInfo = new PublishInfo();
        pubInfo.setPublished(true);
        pubInfo.setAuthenticationMethod(PublishInfo.AuthenticationMethod.BASIC);
        pubInfo.setPassword(DEMO_PASSWORD);

        // Build the specification for the published library
        LibraryModel pubSpec = new LibraryModel();
        pubSpec.setName(this.pubLibName);
        pubSpec.setType(LibraryType.LOCAL);
        pubSpec.setPublishInfo(pubInfo);
        pubSpec.setStorageBackings(Collections.singletonList(storageBacking));

        // Create the published library and add a library item
        String pubToken = UUID.randomUUID().toString();
        this.pubLibId =
                this.client.localLibraryService().create(pubToken, pubSpec);
        System.out.println("Published library created : " + this.pubLibId);
        LibraryModel pubLib =
                this.client.localLibraryService().get(this.pubLibId);
        System.out.println("Publish URL : "
                + pubLib.getPublishInfo().getPublishUrl());
        createLibraryItem(this.pubLibId, "item 1");

        // Build the subscription information using the publish URL of the
        // published library. The username must be "vcsp".
        SubscriptionInfo subInfo = new SubscriptionInfo();
        subInfo.setAuthenticationMethod(
            SubscriptionInfo.AuthenticationMethod.BASIC);
        subInfo.setUserName(VCSP_USERNAME);
        subInfo.setPassword(DEMO_PASSWORD);
        subInfo.setOnDemand(false);
        subInfo.setAutomaticSyncEnabled(true);
        subInfo.setSubscriptionUrl(pubLib.getPublishInfo().getPublishUrl());

        // Build the specification for the subscribed library
        LibraryModel subSpec = new LibraryModel();
        subSpec.setName(this.subLibName);
        subSpec.setType(LibraryType.SUBSCRIBED);
        subSpec.setSubscriptionInfo(subInfo);
        subSpec.setStorageBackings(Collections.singletonList(storageBacking));

        // Create the subscribed library
        String subToken = UUID.randomUUID().toString();
        this.subLibId = this.client.subscribedLibraryService().create(subToken,
            subSpec);
        LibraryModel subLib =
                this.client.subscribedLibraryService().get(this.subLibId);
        System.out.println("Subscribed library created : " + this.subLibId);

        boolean syncSuccess;
        // Wait for the initial synchronization to finish
        syncSuccess = this.clsHelper.waitForLibrarySync(this.pubLibId,
            this.subLibId,
            SYNC_TIMEOUT_SEC,
            TimeUnit.SECONDS);
        assert syncSuccess : "Timed out while waiting for sync success";
        subLib = this.client.subscribedLibraryService().get(this.subLibId);
        System.out.println("Subscribed library synced : "
                           + subLib.getLastSyncTime().getTime());
        List<String> subItemIds = this.client.itemService().list(this.subLibId);
        assert subItemIds.size() == 1 : "Subscribed library has one item";

        // Add another item to the publish library
        createLibraryItem(pubLib.getId(), "item 2");

        // Manually synchronize the subscribed library to get the latest changes
        // immediately.
        this.client.subscribedLibraryService().sync(this.subLibId);
        syncSuccess =  this.clsHelper.waitForLibrarySync(this.pubLibId,
            this.subLibId,
            SYNC_TIMEOUT_SEC,
            TimeUnit.SECONDS);
        assert syncSuccess : "Timed out while waiting for sync success";
        subLib = this.client.subscribedLibraryService().get(this.subLibId);
        System.out.println("Subscribed library synced : "
                           + subLib.getLastSyncTime().getTime());

        // List the subscribed items
        subItemIds = this.client.itemService().list(this.subLibId);
        assert subItemIds.size() == 2 : "Subscribed library has two items";
        for (String subItemId : subItemIds) {
            ItemModel subItem = this.client.itemService().get(subItemId);
            System.out.println("Subscribed item : " + subItem);
        }

        // Change the subscribed library to be on-demand
        subInfo.setOnDemand(true);
        this.client.subscribedLibraryService().update(this.subLibId, subSpec);

        // Evict the cached content of the first subscribed library item
        String subItemId = subItemIds.get(0);
        this.client.subscribedItemService().evict(subItemId);
        ItemModel subItem = this.client.itemService().get(subItemId);
        System.out.println("Subscribed item evicted : " + subItem);
        assert !subItem.getCached() : "Subscribed item is not cached";

        // Force synchronize the subscribed library item to fetch and cache the
        // content
        this.client.subscribedItemService().sync(subItemId, true);
        syncSuccess = this.clsHelper.waitForItemSync(subItemId,
            SYNC_TIMEOUT_SEC,
            TimeUnit.SECONDS);
        assert syncSuccess : "Timed out while waiting for sync success";
        subItem = this.client.itemService().get(subItemId);
        System.out.println("Subscribed item force synced : " + subItem);
        assert subItem.getCached() : "Subscribed item is cached";
    }

    protected void cleanup() {
        if (this.subLibId != null) {
            // Delete the subscribed content library
            this.client.subscribedLibraryService().delete(this.subLibId);
            System.out.println("Deleted subscribed library : " + this.subLibId);
        }

        if (this.pubLibId != null) {
            // Delete the published content library
            this.client.localLibraryService().delete(this.pubLibId);
            System.out.println("Deleted published library : " + this.pubLibId);
        }
    }

    /**
     * Creates a library item with mock content, for demonstration purposes.
     *
     * @param localLibraryId identifier of the local library where a new item
     *        will be created
     * @param itemName name of the item to create
     * @return identifier of the created item
     * @throws IOException when an I/O error occurs
     */
    private String createLibraryItem(String localLibraryId, String itemName)
            throws IOException {
        // Build the specification for the library item to be created
        ItemModel createSpec = new ItemModel();
        createSpec.setLibraryId(localLibraryId);
        createSpec.setName(itemName);

        // Create the library item
        String clientToken = UUID.randomUUID().toString();
        String libItemId =
                this.client.itemService().create(clientToken, createSpec);

        // Create a temporary file
        Path path = Files.createTempFile(itemName, ".txt");
        path.toFile().deleteOnExit();
        // Write default content to the file
        String content = "Contents of " + itemName;
        Files.write(path, content.getBytes());

        // Upload file to the library item
        ItemUploadHelper.performUpload(this.client.updateSession(),
                this.client.updateSessionFileService(),
                this.client.itemService(),
                libItemId,
                Arrays.asList(path.toString()));

        System.out.println("Library item created : " + libItemId);
        return libItemId;
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
        new LibraryPublishSubscribe().execute(args);
    }
}
