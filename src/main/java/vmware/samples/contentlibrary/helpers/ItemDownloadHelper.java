/*
 * *******************************************************
 * Copyright VMware, Inc. 2013, 2016  All Rights Reserved.
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

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

import com.vmware.content.library.Item;
import com.vmware.content.library.item.DownloadSession;
import com.vmware.content.library.item.DownloadSessionModel;
import com.vmware.content.library.item.downloadsession.File;
import com.vmware.content.library.item.downloadsession.FileTypes;
import com.vmware.content.library.item.downloadsession.FileTypes.EndpointType;

import vmware.samples.common.HttpClient;

public class ItemDownloadHelper {

    /**
     * Performing the library item download
     *
     * @param downloadService
     * @param downloadFileService
     * @param libItemService
     * @param libraryItemId
     * @param dir
     * @return
     */
    public static void performDownload(DownloadSession downloadService,
            File downloadFileService, Item libItemService, String libraryItemId,
            java.io.File dir) {
        System.out.println("Download start for Library Item : " + libraryItemId
                + " Name : " + libItemService.get(libraryItemId).getName());
        String downloadSessionId = null;
        try {
            // create download session
            downloadSessionId = createDownloadSession(downloadService,
                    libraryItemId, UUID.randomUUID().toString());
            downloadFiles(downloadService, downloadFileService,
                    downloadSessionId, dir);
            // delete the download session.
        } finally {
            downloadService.delete(downloadSessionId);
        }
    }

    /**
     * Downloading files from library item using the download session.
     *
     * @param downloadService
     * @param downloadFileService
     * @param sessionId
     * @param dir
     * @return
     */
    private static void downloadFiles(DownloadSession downloadService,
            File downloadFileService, String sessionId, java.io.File dir) {
        HttpClient httpClient = new HttpClient(true);
        List<FileTypes.Info> downloadFileInfos = downloadFileService
                .list(sessionId);
        for (FileTypes.Info downloadFileInfo : downloadFileInfos) {
            prepareForDownload(downloadService, downloadFileService, sessionId,
                    downloadFileInfo);
            // Do a get after file is prepared for download.
            downloadFileInfo = downloadFileService.get(sessionId,
                    downloadFileInfo.getName());
            // Download the file
            System.out.println("Download File Info : " + downloadFileInfo);
            try {
                URI downloadUri = downloadFileInfo.getDownloadEndpoint()
                        .getUri();
                String downloadUrl = downloadUri.toURL().toString();
                System.out.println("Download from URL : " + downloadUrl);
                InputStream inputStream = httpClient.downloadFile(downloadUrl);
                String fileName = downloadFileInfo.getName();
                downloadFile(inputStream, dir.getAbsolutePath()
                        + System.getProperty("file.separator") + fileName);
            } catch (MalformedURLException e) {
                System.out
                        .println("Failed to download due to IOException!" + e);
                throw new RuntimeException(
                        "Failed to download due to IOException!", e);
            } catch (IOException e) {
                System.out.println("IO exception during download" + e);
                throw new RuntimeException(
                        "Failed to download due to IOException!", e);
            }
        }
    }

    /**
     * Make sure the file to be dowloaded is ready for download
     *
     * @param downloadService
     * @param downloadFileService
     * @param sessionId
     * @param downloadFileInfo
     */
    private static void prepareForDownload(DownloadSession downloadService,
            File downloadFileService, String sessionId,
            FileTypes.Info downloadFileInfo) {
        System.out
                .println("Download File name : " + downloadFileInfo.getName());
        System.out.println("Download File Prepare Status : "
                + downloadFileInfo.getStatus());
        downloadFileService.prepare(sessionId, downloadFileInfo.getName(),
                EndpointType.HTTPS);
        waitForDownloadFileReady(downloadService, downloadFileService,
                sessionId, downloadFileInfo.getName(),
                com.vmware.content.library.item.downloadsession.File
                .PrepareStatus.PREPARED,
                SESSION_FILE_TIMEOUT);
    }

    private static final long SESSION_FILE_TIMEOUT = 360;

    /**
     * Wait for the download file status to be prepared.
     *
     * @param downloadService
     * @param downloadFileService
     * @param sessionId
     * @param fileName
     * @param timeOut
     * @param expectedStatus
     */
    private static void waitForDownloadFileReady(
            DownloadSession downloadService, File downloadFileService,
            String sessionId, String fileName,
            FileTypes.PrepareStatus expectedStatus, long timeOut) {
        Long endTime = System.currentTimeMillis() + timeOut * 1000;
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        FileTypes.Info fileInfo = downloadFileService.get(sessionId, fileName);
        FileTypes.PrepareStatus currentStatus = fileInfo.getStatus();
        if (currentStatus == expectedStatus) {
            return;
        } else {
            while (endTime > System.currentTimeMillis()) {
                fileInfo = downloadFileService.get(sessionId, fileName);
                currentStatus = fileInfo.getStatus();
                System.out.println("Current Status : " + currentStatus);
                if (currentStatus == expectedStatus) {
                    return;
                } else if (
                        currentStatus ==
                        com.vmware.content.library.item.downloadsession.File
                            .PrepareStatus.ERROR) {
                    System.out.println("DownloadSession Info : "
                            + downloadService.get(sessionId));
                    System.out.println("list on the downloadSessionFile : "
                            + downloadFileService.list(sessionId));
                    throw new RuntimeException(
                            "Error while waiting for download file status to "
                            + "be PREPARED...");
                }
            }
        }
        throw new RuntimeException(
                "Timeout waiting for download file status to be PREPARED,"
                        + "  status : " + currentStatus.toString());
    }

    /**
     * Create a new download session for downloading files from library item.
     *
     * @param downloadService
     * @param libraryItemId
     * @param clientToken
     * @return
     */
    private static String createDownloadSession(DownloadSession downloadService,
            String libraryItemId, String clientToken) {
        DownloadSessionModel downloadSpec = new DownloadSessionModel();
        downloadSpec.setLibraryItemId(libraryItemId);
        String sessionId = downloadService.create(clientToken, downloadSpec);
        return sessionId;
    }

    /**
     * Download a specific file
     *
     * @param inputStream
     * @param fullPath
     * @return
     * @throws IOException
     */
    private static void downloadFile(InputStream inputStream, String fullPath)
            throws IOException {
        Files.copy(inputStream, Paths.get(fullPath),
                StandardCopyOption.REPLACE_EXISTING);
    }
}
