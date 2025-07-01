/*
 * ******************************************************
 * Copyright VMware, Inc. 2016.  All Rights Reserved.
 * SPDX-License-Identifier: MIT
 * ******************************************************
 *
 * DISCLAIMER. THIS PROGRAM IS PROVIDED TO YOU "AS IS"
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, WHETHER
 * ORAL OR WRITTEN, EXPRESS OR IMPLIED. THE AUTHOR
 * SPECIFICALLY DISCLAIMS ANY IMPLIED WARRANTIES OR
 * CONDITIONS OF MERCHANTABILITY, SATISFACTORY QUALITY,
 * NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE.
 */

package vmware.samples.common;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

/**
 * This class breaks the Java SSL Trust Management services so that Java will
 * trust any and all SSL certificates. You probably don't ever want to use this
 * in a production environment but we are using it here to help you to test your
 * code against systems that may not have a real SSL certificate.
 * <p>
 * <strong>It is not advised to ever use this in production.</strong>
 */
public final class TrustAllTrustManager
        implements javax.net.ssl.TrustManager, javax.net.ssl.X509TrustManager {

    public TrustAllTrustManager() {
    }

    public void register()
            throws NoSuchAlgorithmException, KeyManagementException {
        register(this);
    }

    public static void register(javax.net.ssl.TrustManager tm)
            throws NoSuchAlgorithmException, KeyManagementException {
        javax.net.ssl.TrustManager[] trustAllCerts =
                new javax.net.ssl.TrustManager[1];
        trustAllCerts[0] = tm;
        javax.net.ssl.SSLContext sc =
                javax.net.ssl.SSLContext.getInstance("SSL");
        javax.net.ssl.SSLSessionContext sslsc = sc.getServerSessionContext();
        sslsc.setSessionTimeout(0);
        sc.init(null, trustAllCerts, null);
        javax.net.ssl.HttpsURLConnection
                .setDefaultSSLSocketFactory(sc.getSocketFactory());
    }

    @Override
    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
        return null;
    }

    @Override
    public void
           checkServerTrusted(java.security.cert.X509Certificate[] certs,
                              String authType)
                                throws java.security.cert.CertificateException {
        return;
    }

    @Override
    public void
           checkClientTrusted(java.security.cert.X509Certificate[] certs,
                              String authType)
                                throws java.security.cert.CertificateException {
        return;
    }

}
