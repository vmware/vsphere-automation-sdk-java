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
package vmware.samples.contentlibrary.helpers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.vmware.content.LibraryModel;
import com.vmware.content.library.ItemModel;
import vmware.samples.contentlibrary.client.ClsApiClient;

/**
 * Helper class to perform commonly used operations using Content Library API.
 */
public class ClsApiHelper {

    private final ClsApiClient client;

    /**
     * Constructs an instance of the Content Library API helper.
     *
     * @param client the Content Library API client
     */
    public ClsApiHelper(ClsApiClient client) {
        this.client = client;
    }

    /**
     * Wait for the synchronization of the subscribed library to complete or
     * until the timeout is reached. The subscribed library is fully
     * synchronized when it has the same library items and the same versions as
     * the items in the source published library.
     *
     * @param pubLibraryId the identifier of the published library
     * @param subLibraryId the identifier of the subscribed library
     * @param timeout the maximum time to wait
     * @param unit the time unit of the timeout parameter
     * @return true if the subscribed library is synchronized with the published
     *         library, false otherwise
     * @throws InterruptedException if the current thread was interrupted
     */
    public boolean waitForLibrarySync(String pubLibraryId, String subLibraryId,
                                      long timeout, TimeUnit unit)
                                              throws InterruptedException {
        return new SyncHelper(timeout, unit).waitForLibrarySync(pubLibraryId,
            subLibraryId);
    }

    /**
     * Wait for the synchronization of the subscribed library item to complete
     * or until the timeout is reached. The subscribed item is fully
     * synchronized when it has the same metadata and content version as the
     * source published item.
     *
     * @param subItemId the identifier of the subscribed item
     * @param timeout the maximum time to wait
     * @param unit the time unit of the timeout parameter
     * @return true if the subscribed item is synchronized with the published
     *         item, false otherwise
     * @throws InterruptedException if the current thread was interrupted
     */
    public boolean waitForItemSync(String subItemId, long timeout,
                                   TimeUnit unit) throws InterruptedException {
        return new SyncHelper(timeout, unit).waitForItemSync(subItemId);
    }


    /**
     * Helper class to wait for the subscribed libraries and items to be
     * synchronized completely with their source.
     */
    private class SyncHelper {

        private static final long WAIT_INTERVAL_MS = 1000;

        private final long startTime;
        private final long timeoutNano;

        public SyncHelper(long timeout, TimeUnit unit) {
            this.startTime = System.nanoTime();
            this.timeoutNano = TimeUnit.NANOSECONDS.convert(timeout, unit);
        }

        /*
         * Wait until the subscribed library and its items are synchronized with
         * the published library.
         */
        public boolean waitForLibrarySync(String pubLibraryId,
                                          String subLibraryId)
                                                  throws InterruptedException {

            if (!waitForSameItems(pubLibraryId, subLibraryId)) {
                return false;
            }

            List<String> subItemIds = client.itemService().list(subLibraryId);
            for (String subItemId : subItemIds) {
                if (!waitForItemSync(subItemId)) {
                    return false;
                }
            }

            if (!waitForLibraryLastSyncTime(subLibraryId)) {
                return false;
            }

            return true;
        }

        /*
         * Wait until the subscribed item is synchronized with the published
         * item.
         */
        public boolean waitForItemSync(String subItemId)
                throws InterruptedException {
            boolean isSynced = false;
            String pubItemId =
                    client.itemService().get(subItemId).getSourceId();
            ItemModel pubItem = client.itemService().get(pubItemId);

            while (notTimedOut()) {
                ItemModel subItem = client.itemService().get(subItemId);
                if (isSubscribedItemLatest(pubItem, subItem)) {
                    isSynced = true;
                    break;
                }

                Thread.sleep(WAIT_INTERVAL_MS);
            }
            return isSynced;
        }

        /*
         * Wait until the subscribed library has the same source item IDs as the
         * published library.
         */
        private boolean waitForSameItems(String pubLibraryId,
                                         String subLibraryId)
                                                 throws InterruptedException {
            boolean isSynced = false;
            List<String> pubItemIds = client.itemService().list(pubLibraryId);

            while (notTimedOut()) {
                List<String> subItemIds =
                        client.itemService().list(subLibraryId);
                if (hasSameItems(pubItemIds, subItemIds)) {
                    isSynced = true;
                    break;
                }

                Thread.sleep(WAIT_INTERVAL_MS);
            }
            return isSynced;
        }

        /*
         * Check if the subscribed library contains the same items as the source
         * published library. The item versions are not checked.
         */
        private boolean hasSameItems(List<String> pubItemIds,
                                     List<String> subItemIds) {
            if (pubItemIds.size() != subItemIds.size()) {
                return false;
            }

            List<String> syncedIds = new ArrayList<>(pubItemIds.size());
            for (String subItemId : subItemIds) {
                ItemModel subItem = client.itemService().get(subItemId);
                String sourceId = subItem.getSourceId();
                if (!syncedIds.contains(subItemId)
                    && pubItemIds.contains(sourceId)) {
                    syncedIds.add(subItemId);
                }
            }

            return (pubItemIds.size() == syncedIds.size());
        }

        /*
         * Wait until the subscribed library's last sync time is populated.
         */
        private boolean waitForLibraryLastSyncTime(String subLibraryId)
                throws InterruptedException {
            boolean isSynced = false;

            while (notTimedOut()) {
                LibraryModel library =
                        client.subscribedLibraryService().get(subLibraryId);
                if (library.getLastSyncTime() != null) {
                    isSynced = true;
                    break;
                }

                Thread.sleep(WAIT_INTERVAL_MS);
            }
            return isSynced;
        }

        /*
         * Check if the subscribed item has the same metadata and content
         * version as the source published item.
         */
        private boolean isSubscribedItemLatest(ItemModel pubItem,
                                               ItemModel subItem) {
            String metadataVersion = pubItem.getMetadataVersion();
            String contentVersion = pubItem.getContentVersion();

            return subItem.getMetadataVersion().equals(metadataVersion)
                   && subItem.getContentVersion().equals(contentVersion);
        }

        /*
         * Check if we have not timed out yet.
         */
        private boolean notTimedOut() {
            long elapsedTime = System.nanoTime() - startTime;
            return elapsedTime < timeoutNano;
        }
    }
}
