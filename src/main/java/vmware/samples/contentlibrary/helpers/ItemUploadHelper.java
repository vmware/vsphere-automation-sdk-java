/*
 * *******************************************************
 * Copyright VMware, Inc. 2014, 2016  All Rights Reserved.
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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.vmware.content.library.Item;
import com.vmware.content.library.item.UpdateSession;
import com.vmware.content.library.item.UpdateSessionModel;
import com.vmware.content.library.item.updatesession.File;
import com.vmware.content.library.item.updatesession.FileTypes.AddSpec;
import com.vmware.content.library.item.updatesession.FileTypes.Info;
import com.vmware.content.library.item.updatesession.FileTypes.SourceType;
import com.vmware.content.library.item.updatesession.FileTypes.ValidationError;

import vmware.samples.common.HttpClient;

public class ItemUploadHelper {

    /**
     * Perform the upload.
     *
     * @param uploadService
     * @param uploadFileService
     * @param libItemService
     * @param libItemId
     * @param fileLocations
     */
    public static void performUpload(UpdateSession uploadService,
            File uploadFileService, Item libItemService, String libItemId,
            List<String> fileLocations) {

        // get the file names from the local file locations.
        List<String> fileNames = new ArrayList<String>();
        for (String location : fileLocations) {
            java.io.File file = new java.io.File(location);
            fileNames.add(file.getName());
        }

        // create a new upload session for uploading the files
        String sessionId = createUploadSession(uploadService, libItemService,
                libItemId);

        // add the files to the item and PUT the file to the transfer URL
        uploadFiles(uploadFileService, sessionId, fileNames, fileLocations);

        // check if there were any invalid or missing files
        List<ValidationError> invalidFiles =
                uploadFileService.validate(sessionId).getInvalidFiles();
        Set<String> missingFiles = uploadFileService.validate(sessionId)
                .getMissingFiles();
        System.out.println(
                "UploadSession Info : " + uploadService.get(sessionId));
        System.out.println("Invalid Files : " + invalidFiles);
        System.out.println("Missing Files : " + missingFiles);
        if (missingFiles.size() == 0 && invalidFiles.size() == 0) {
            uploadService.complete(sessionId);
            // Delete the update session once the upload is done to free up the
            // session.
            uploadService.delete(sessionId);
        }
        if (invalidFiles.size() != 0) {
            uploadService.fail(sessionId,
                    invalidFiles.get(0).getErrorMessage().toString());
            uploadService.delete(sessionId);
            System.out.println("Invalid files : " + invalidFiles);
            throw new RuntimeException(invalidFiles.toString());
        }
        if (missingFiles.size() != 0) {
            uploadService.cancel(sessionId);
            System.out.println("Following files are missing : " + missingFiles);
            throw new RuntimeException(
                    "Missing the required files : " + missingFiles);
        }

        // verify that the content version number has incremented after the
        // commit.
        System.out.println("The Library Item version after the upload commit : "
                + libItemService.get(libItemId).getContentVersion());
    }

    /**
     * Creating a new upload session.
     *
     * @param uploadService
     * @param libItemService
     * @param libraryItemId
     * @return
     */
    private static String createUploadSession(UpdateSession uploadService,
            Item libItemService, String libraryItemId) {
        // Create a session for upload.
        String currentVersion = libItemService.get(libraryItemId)
                .getContentVersion();
        UpdateSessionModel createSpec = new UpdateSessionModel();
        createSpec.setLibraryItemId(libraryItemId);
        createSpec.setLibraryItemContentVersion(currentVersion);
        String sessionId = uploadService.create(UUID.randomUUID().toString(),
                createSpec);
        return sessionId;
    }

    /**
     * Upload files using upload session.
     *
     * @param uploadFileService
     * @param sessionId
     * @param fileNames
     * @param fileLocations
     * @return
     */
    private static void uploadFiles(File uploadFileService, String sessionId,
            List<String> fileNames, List<String> fileLocations) {
        assert fileNames.size() == fileLocations.size();
        for (int i = 0; i < fileNames.size(); i++) {
            uploadFile(uploadFileService, sessionId, fileNames.get(i),
                    fileLocations.get(i));
        }
    }

    /**
     * Upload a file using upload session
     *
     * @param sessionId
     * @param fileName
     * @param fileLocation
     * @return info of the update session file
     */
    public static Info uploadFile(File uploadFileService, String sessionId,
            String fileName, String fileLocation) {
        HttpClient httpClient = new HttpClient(true);
        // add the file spec to the upload file service
        AddSpec addSpec = new AddSpec();
        addSpec.setName(fileName);
        addSpec.setSourceType(SourceType.PUSH);
        uploadFileService.add(sessionId, addSpec);

        // Do a get on the file, verify the information is the same
        com.vmware.content.library.item.updatesession.FileTypes.Info fileInfo =
                uploadFileService.get(sessionId, fileName);

        // Get the transfer uri.
        URI transferUri = fileInfo.getUploadEndpoint().getUri();

        System.out.println("File Location : " + fileLocation);
        java.io.File file1 = new java.io.File(fileLocation);
        System.out.println("File Name " + file1.getName());
        try {
            String transferUrl = transferUri.toURL().toString();
            System.out.println("Upload/Transfer URL : " + transferUrl);
            httpClient.upload(file1, transferUrl);

        } catch (MalformedURLException e) {
            throw new RuntimeException("Failed to upload due to IOException!",
                    e);
        }

        // Verify that the file has been received
        fileInfo = uploadFileService.get(sessionId, fileName);
        return fileInfo;
    }

    /**
     * Creating a local temp dir with the given prefix.
     *
     * @param prefix
     * @return the File handle for the created directory
     * @throws IOException
     *             when an I/O error occurs
     */
    public static java.io.File createTempDir(String prefix) throws IOException {
        java.io.File temp;
        temp = java.io.File.createTempFile(prefix, "");
        temp.delete();
        temp.mkdir();
        temp.deleteOnExit();
        return temp;
    }

    /**
     * Copies the resource into a temporary file.
     *
     * @param resourceName
     *            the resource name to copy
     * @param dir
     *            the directory where the file will be created
     * @param filename
     *            the name of the file to create
     * @return the absolute path to the file
     * @throws IOException
     *             when an I/O error occurs
     */
    public static String copyResourceToFile(String resourceName,
            java.io.File dir, String filename) throws IOException {
        // Create a temporary file in the directory
        java.io.File tempFile = new java.io.File(dir, filename);
        tempFile.deleteOnExit();

        // Copy the resource to the temporary file
        ClassLoader classLoader = ItemUploadHelper.class.getClassLoader();
        try (InputStream in = classLoader.getResourceAsStream(resourceName);
                OutputStream out = new FileOutputStream(tempFile)) {

            byte[] buffer = new byte[4096];
            int n;
            while ((n = in.read(buffer)) > 0) {
                out.write(buffer, 0, n);
            }
        }

        return tempFile.getAbsolutePath();
    }
}
