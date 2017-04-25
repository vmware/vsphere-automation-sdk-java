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
package vmware.samples.contentlibrary.client;

import com.vmware.content.Configuration;
import com.vmware.content.Library;
import com.vmware.content.LocalLibrary;
import com.vmware.content.SubscribedLibrary;
import com.vmware.content.Type;
import com.vmware.content.library.Item;
import com.vmware.content.library.SubscribedItem;
import com.vmware.content.library.item.DownloadSession;
import com.vmware.content.library.item.Storage;
import com.vmware.content.library.item.UpdateSession;
import com.vmware.content.library.item.downloadsession.File;
import com.vmware.vapi.bindings.StubConfiguration;
import com.vmware.vapi.bindings.StubFactory;
import com.vmware.vcenter.iso.Image;
import com.vmware.vcenter.ovf.LibraryItem;

/**
 * The {@code ClsApiClient} is used to access the services in Content Library.
 */
public class ClsApiClient {

    private final Library libraryService;
    private final LocalLibrary localLibraryService;
    private final SubscribedLibrary subscribedLibraryService;
    private final Item itemService;
    private final SubscribedItem subscribedItemService;
    private final Storage storageService;
    private final DownloadSession downloadSessionService;
    private final File downloadSessionFileService;
    private final UpdateSession updateSessionService;
    private final com.vmware.content.library.item.updatesession.File
                  updateSessionFileService;
    private final Configuration configurationService;
    private final Type typeService;
    private final LibraryItem ovfLibraryItemService;
    private final Image isoImageService;

    /**
     * Constructs a new Content Library API client using the stub factory.
     *
     * @param stubFactory
     *            the stub factory for the API endpoint
     * @param stubConfig
     *            the stub configuration configured with an authenticated
     *            session
     */
    public ClsApiClient(StubFactory stubFactory, StubConfiguration stubConfig) {
        // Library services
        this.libraryService = stubFactory.createStub(Library.class, stubConfig);
        this.localLibraryService = stubFactory.createStub(LocalLibrary.class,
                stubConfig);
        this.subscribedLibraryService = stubFactory
                .createStub(SubscribedLibrary.class, stubConfig);

        // Library item services
        this.itemService = stubFactory.createStub(Item.class, stubConfig);
        this.subscribedItemService = stubFactory
                .createStub(SubscribedItem.class, stubConfig);
        this.storageService = stubFactory.createStub(Storage.class, stubConfig);
        this.downloadSessionService = stubFactory
                .createStub(DownloadSession.class, stubConfig);
        this.downloadSessionFileService = stubFactory.createStub(File.class,
                stubConfig);
        this.updateSessionService = stubFactory.createStub(UpdateSession.class,
                stubConfig);
        this.updateSessionFileService = stubFactory.createStub(
                com.vmware.content.library.item.updatesession.File.class,
                stubConfig);

        // Configuration service
        this.configurationService = stubFactory.createStub(Configuration.class,
                stubConfig);

        // Content Library extensions
        this.typeService = stubFactory.createStub(Type.class, stubConfig);
        this.ovfLibraryItemService = stubFactory.createStub(LibraryItem.class,
                stubConfig);
        this.isoImageService = stubFactory.createStub(Image.class, stubConfig);
    }

    /**
     * Returns the service which provides support for generic functionality
     * which can be applied equally to all types of libraries.
     *
     * @return the {@code Library} service.
     */
    public Library libraryService() {
        return this.libraryService;
    }

    /**
     * Returns the service for managing local libraries.
     *
     * @return the {@code LocalLibrary} service.
     */
    public LocalLibrary localLibraryService() {
        return this.localLibraryService;
    }

    /**
     * Returns the service for managing subscribed libraries.
     *
     * @return the {@code SubscribedLibrary} service.
     */
    public SubscribedLibrary subscribedLibraryService() {
        return this.subscribedLibraryService;
    }

    /**
     * Returns the service for managing library items.
     *
     * @return the {@code Item} service.
     */
    public Item itemService() {
        return this.itemService;
    }

    /**
     * Returns the service for managing subscribed library items.
     *
     * @return the {@code SubscribedItem} service.
     */
    public SubscribedItem subscribedItemService() {
        return this.subscribedItemService;
    }

    /**
     * Returns the service for retrieving storage information of the files
     * associated with a library item.
     *
     * @return the {@code Storage} service.
     */
    public Storage storageService() {
        return this.storageService;
    }

    /**
     * Returns the service for managing sessions to download content.
     *
     * @return the {@code DownloadSession} service.
     */
    public DownloadSession downloadSessionService() {
        return this.downloadSessionService;
    }

    /**
     * Returns the service for managing files within a download session.
     *
     * @return the {@code com.vmware.content.library.item.downloadsession.File}
     *         service.
     */
    public File downloadSessionFileService() {
        return this.downloadSessionFileService;
    }

    /**
     * Returns the service for managing sessions to update or delete content.
     *
     * @return the {@code UpdateSession} service.
     */
    public UpdateSession updateSession() {
        return updateSessionService;
    }

    /**
     * Returns the service for managing files within an update session.
     *
     * @return the {@code com.vmware.content.library.item.updatesession.File}
     *         service.
     */
    public com.vmware.content.library.item.updatesession.File
           updateSessionFileService() {
        return this.updateSessionFileService;
    }

    /**
     * Returns the service to configure the global settings of the Content
     * Library Service.
     *
     * @return the {@code Configuration} service.
     */
    public Configuration configurationService() {
        return this.configurationService;
    }

    /**
     * Returns the service for listing the library item types supported by the
     * Content Library.
     *
     * @return the {@code Type} service.
     */
    public Type typeService() {
        return this.typeService;
    }

    /**
     * Returns the service for deploying virtual machines from OVF library
     * items.
     *
     * @return the {@code LibraryItem} service.
     */
    public LibraryItem ovfLibraryItemService() {
        return this.ovfLibraryItemService;
    }

    /**
     * Returns the service for mounting images on a virtual machine from ISO
     * library items.
     *
     * @return the {@code Image} service.
     */
    public Image isoImageService() {
        return this.isoImageService;
    }
}
