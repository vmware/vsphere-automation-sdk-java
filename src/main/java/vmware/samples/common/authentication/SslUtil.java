/*
 * *******************************************************
 * Copyright VMware, Inc. 2019.  All Rights Reserved.
 * SPDX-License-Identifier: MIT
 * *******************************************************
 *
 * DISCLAIMER. THIS PROGRAM IS PROVIDED TO YOU "AS IS" WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, WHETHER ORAL OR WRITTEN,
 * EXPRESS OR IMPLIED. THE AUTHOR SPECIFICALLY DISCLAIMS ANY IMPLIED
 * WARRANTIES OR CONDITIONS OF MERCHANTABILITY, SATISFACTORY QUALITY,
 * NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE.
 */

package vmware.samples.common.authentication;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Retrieves the SSL certificate chain of an HTTPS server and stores the root
 * certificate into an in-memory trust store.
 * <p>
 * Note: Circumventing SSL trust is unsafe and should not use these in
 * production software.
 * </p>
 */
public class SslUtil {

    public static KeyStore createTrustStoreForServer(String url) {
        URI uri;
        try {
            uri = new URI(url);
        } catch (URISyntaxException ex) {
            throw new IllegalArgumentException(ex);
        }
        String host = uri.getHost();
        int port = uri.getPort();
        if (port == -1) {
            port = 443;
        }
        return createTrustStoreForServer(host, port);
    }

    public static KeyStore createTrustStoreForServer(String host, int port) {
        TrustManager trustAll = new X509TrustManager() {
            @Override
            public void checkServerTrusted(X509Certificate[] chain,
                                           String authType)
                                                   throws CertificateException {
                // accept all
            }

            @Override
            public void checkClientTrusted(X509Certificate[] chain,
                                           String authType)
                                                   throws CertificateException {
                // server-side only; irrelevant for clients
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                // server-side only; irrelevant for clients
                return null;
            }
        };

        try {
            SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(null, new TrustManager[] { trustAll }, null);
            SSLSocket s =
                    (SSLSocket) ctx.getSocketFactory().createSocket(host, port);
            Certificate[] chain = s.getSession().getPeerCertificates();

            // last one is the root certificate
            Certificate rootCert = chain[chain.length - 1];
            KeyStore trustStore =
                    KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);
            trustStore.setCertificateEntry(host, rootCert);

            return trustStore;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Method to trust all the HTTPS certificates. To be used only in the
     * development environment for convenience.
     */
    public static void trustAllHttpsCertificates() {
        try {
            // Create the trust manager.
            TrustManager[] trustAllCerts =
                    new TrustManager[1];
            TrustManager tm = new TrustAllTrustManager();
            trustAllCerts[0] = tm;

            // Create the SSL context
            SSLContext sc =
                    SSLContext.getInstance("SSL");

            // Create the session context
            javax.net.ssl.SSLSessionContext sslsc =
                    sc.getServerSessionContext();
            /*
             * Initialize the contexts; the session context takes the trust
             * manager.
             */
            sslsc.setSessionTimeout(0);
            sc.init(null, trustAllCerts, null);

            /*
             * Use the default socket factory to create the socket for the
             * secure connection
             */
            HttpsURLConnection
                    .setDefaultSSLSocketFactory(sc.getSocketFactory());

            /*
             * Declare a host name verifier that will automatically enable the
             * connection. The host name verifier is invoked during the SSL
             * handshake.
             */
            HostnameVerifier hv = new HostnameVerifier() {
                public boolean verify(String urlHostName, SSLSession session) {
                    return true;
                }
            };

            // Set the default host name verifier to enable the connection.
            HttpsURLConnection.setDefaultHostnameVerifier(hv);

        } catch (KeyManagementException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Load the certificate from file.
     *
     * @param filePath
     * @return {@link X509Certificate}
     * @throws IOException
     * @throws CertificateException
     */
    public static X509Certificate loadCertificate(String filePath)
            throws IOException, CertificateException {
        ByteArrayInputStream bis = new ByteArrayInputStream(Files
                .readAllBytes(Paths.get(filePath)));
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        return (X509Certificate) cf.generateCertificate(bis);
    }

    /**
     * Loads the truststore containing the trusted server certificates.
     *
     * @param filePath path to the truststore file
     * @param password password for the truststore.
     * @return an instance of KeyStore object containing the trusted server
     *         certificates
     * @throws Exception
     */
    public static KeyStore loadTrustStore(String filePath, String password)
            throws Exception {
        KeyStore trustStore = KeyStore.getInstance("JKS");
        InputStream truststoreStream = new FileInputStream(filePath);
        try {
            trustStore.load(truststoreStream, password.toCharArray());
            return trustStore;
        } finally {
            truststoreStream.close();
        }
    }
}
