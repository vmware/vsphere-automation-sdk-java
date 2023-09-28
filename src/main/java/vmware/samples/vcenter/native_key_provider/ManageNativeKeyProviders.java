/*
 * *******************************************************
 * Copyright VMware, Inc. 2023.  All Rights Reserved.
 * SPDX-License-Identifier: MIT
 * *******************************************************
 *
 * DISCLAIMER. THIS PROGRAM IS PROVIDED TO YOU "AS IS" WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, WHETHER ORAL OR WRITTEN,
 * EXPRESS OR IMPLIED. THE AUTHOR SPECIFICALLY DISCLAIMS ANY IMPLIED
 * WARRANTIES OR CONDITIONS OF MERCHANTABILITY, SATISFACTORY QUALITY,
 * NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE.
 */
package vmware.samples.vcenter.native_key_provider;

import com.vmware.vapi.std.errors.AlreadyExists;
import com.vmware.vcenter.crypto_manager.kms.Providers;
import com.vmware.vcenter.crypto_manager.kms.ProvidersTypes.CreateSpec;
import com.vmware.vcenter.crypto_manager.kms.ProvidersTypes.ExportResult;
import com.vmware.vcenter.crypto_manager.kms.ProvidersTypes.ExportSpec;
import com.vmware.vcenter.crypto_manager.kms.ProvidersTypes.ImportSpec;
import com.vmware.vcenter.crypto_manager.kms.ProvidersTypes.FilterSpec;
import com.vmware.vcenter.crypto_manager.kms.ProvidersTypes.ImportResult;
import com.vmware.vcenter.crypto_manager.kms.ProvidersTypes.Info;
import com.vmware.vcenter.crypto_manager.kms.ProvidersTypes.Summary;

import java.io.IOException;
import java.net.URI;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLContext;

import org.apache.commons.cli.Option;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;

import vmware.samples.common.SamplesAbstractBase;

/**
 * Demonstration of the basic native key provider functionality through
 * Java APIs. This sample includes create, get, list, delete, export and
 * import operations on native key providers.
 * 
 * Author: VMware, Inc.
 * Sample Prerequisites: vCenter 7.0.2+
 */
public class ManageNativeKeyProviders extends SamplesAbstractBase {
    private static final String PASSKEY = "53cur3Pa55w0rd!";

    private final static String TEST_PROVIDER = "test_nkp";

    private Providers nativeKeyProviders;

    private void listProviders() {
        List<Summary> list = nativeKeyProviders.list(new FilterSpec());

        log("List of native key providers got {0} items", list.size());
        int i = 1;
        for (Summary s : list) {
            log("Provider {1} Summary: {0}", s, i++);
        }
    }

    private void createProvider(String name) {
        CreateSpec providerSpec = new CreateSpec.Builder(name).build();
        try {
            nativeKeyProviders.create(providerSpec);
        } catch (AlreadyExists e) {
            log("Provider {0} already exists. Continue", name);
        }
    }

    private void getProviderDetails(String name) {
        Info info = nativeKeyProviders.get(name);
        log("Native key provider details: {0}", info);
    }

    /**
     * Backup native key provider data. This is a 2 stage process. First a backup
     * is requested. Second step is to download the backup using token and address
     * returned form the first step.
     * <p>
     * The returned backup data can be used with import to restore a native key 
     * provider.
     * @param name name of the native key provider to backup
     * @param pwd password for protection of the back up data
     * @return bytes of the native key provider backup
     */
    private byte[] backupKeyProvider(String name, char[] pwd) {
        // Step 1: request backup
        ExportSpec spec = new ExportSpec.Builder(name)
                .setPassword(pwd)
                .build();
        ExportResult res = nativeKeyProviders.export(spec);
        log("Backup step one: export result is {0}", res);

        // Step 2: download the backup
        URI url = res.getLocation().getUrl();
        char[] token = res.getLocation().getDownloadToken().getToken();
        return downloadBackupData(url, token);
    }

    /**
     * Download backup data from online location.
     * <p>
     * This method used the Java Apache HTTP client to download the back up data.
     * <p>
     * Download is done by making a post request to the url with Authorization 
     * Bearer header carrying the supplied token
     * @param url online location
     * @param token access token
     * @return bytes of the native key provider backup
     */
    private byte[] downloadBackupData(URI url, char[] token) {
        HttpPost request = new HttpPost(url);
        request.addHeader("Authorization", MessageFormat.format("Bearer {0}", new String(token)));

        log("Backup request {0}", request);
        try (CloseableHttpClient client = createHttpClient();
                CloseableHttpResponse resp = client.execute(request)) {
            int statusCode = resp.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                log("Backup failed. HTTP status code {0}", statusCode);
                throw new RuntimeException("Cannot backup");
            }
            HttpEntity body = resp.getEntity();

            byte[] backup = body.getContent().readAllBytes();
            log("Backup received {0} bytes. Backup completed.", backup.length);
            return backup;
        } catch (IOException e) {
            log("IO Exception during backup: {0}", e);
            throw new RuntimeException(e);
        }
    }

    private CloseableHttpClient createHttpClient() {
        if (isSkipServerVerification()) {
            try {
                SSLContext sslCtx = new SSLContextBuilder().loadTrustMaterial(null, TrustAllStrategy.INSTANCE).build();
                return HttpClients
                        .custom()
                        .setSSLContext(sslCtx)
                        .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                        .build();
            } catch (Exception e) {
                log("Cannot create trust all HTTP client {0}", e);
                throw new RuntimeException(e);
            }
        }
        return HttpClients.createDefault();
    }

    private void deleteKeyProvider(String name) {
        nativeKeyProviders.delete(name);
        log("Deleted key provider {0}", name);
    }

    private void restoreKeyProvider(byte[] backup, char[] pwd) {
        ImportSpec spec = new ImportSpec.Builder().setConfig(backup).setPassword(pwd).build();
        ImportResult res = nativeKeyProviders.importProvider(spec);
        log("Restored Native Key Provider {0}", res);
    }

    @Override
    protected void run() throws Exception {
        listProviders();
        createProvider(TEST_PROVIDER);
        getProviderDetails(TEST_PROVIDER);
        byte[] backup = backupKeyProvider(TEST_PROVIDER, PASSKEY.toCharArray());
        getProviderDetails(TEST_PROVIDER);
        deleteKeyProvider(TEST_PROVIDER);
        restoreKeyProvider(backup, PASSKEY.toCharArray());
        deleteKeyProvider(TEST_PROVIDER);
        listProviders();
    }

    @Override
    protected void setup() throws Exception {
        this.nativeKeyProviders = vapiAuthHelper.getStubFactory().createStub(Providers.class,
                sessionStubConfig);
    }

    @Override
    protected void cleanup() throws Exception {
        // No cleanup required
    }

    public static void main(String[] args) throws Exception {
        ManageNativeKeyProviders sample = new ManageNativeKeyProviders();
        sample.execute(args);
    }

    @Override
    protected void parseArgs(String args[]) {
        super.parseArgs(new ArrayList<Option>(), args);
    }

    private void log(String message, Object... args) {
        System.out.println(MessageFormat.format(message, args));
    }
}
