/*
 * *******************************************************
 * Copyright VMware, Inc. 2017.  All Rights Reserved.
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

import com.vmware.vapi.client.ApiClient;
import com.vmware.vapi.vmc.client.VmcClients;

/**
 * Helper class which provides methods for creating a new <code>ApiClient</code>
 * for VMC APIs
 */
public class VmcAuthenticationHelper {
    public static final String CSP_AUTHORIZATION_URL = "/csp/gateway/am/api/auth/api-tokens/authorize";
    public static final String CSP_CLIENT_CREDS_AUTH_URL = "/csp/gateway/am/api/auth/authorize";

    /**
     * Instantiates an ApiClient using a refresh token which can be used
     * for creating stubs.
     *
     * @param vmcServer hostname/ip address of the vmc server
     * @param cspServer hostname/ipaddress of the csp server
     * @param refreshToken refresh token of the user
     * @return
     */
    public ApiClient newVmcClient(String vmcServer, String cspServer,
            String refreshToken) {
        String cspUrl = "https://" + cspServer + CSP_AUTHORIZATION_URL;
        String vmcUrl = "https://" + vmcServer;
        return VmcClients.custom()
                .setBaseUrl(vmcUrl)
                .setAuthorizationUrl(cspUrl)
                .setRefreshToken(refreshToken.toCharArray())
                .build();
    }

    /**
 *      * Instantiates an ApiClient using a client id and client secret which can be used
 *           * for creating stubs.
 *                *
 *                     * @param vmcServer hostname/ip address of the vmc server
 *                          * @param cspServer hostname/ipaddress of the csp server
 *                               * @param clientId client id of the app
 *                                    * @param clientSecret client secret of the app
 *                                         * @return
 *                                              */
    public ApiClient newVmcClient(String vmcServer, String cspServer,
            String clientId, char[] clientSecret) {
        String cspUrl = "https://" + cspServer + CSP_CLIENT_CREDS_AUTH_URL;
        String vmcUrl = "https://" + vmcServer;
        return VmcClients.custom()
                .setBaseUrl(vmcUrl)
                .setAuthorizationUrl(cspUrl)
                .setClientId(clientId)
                .setClientSecret(clientSecret)
                .build();
    }

    /**
 *      * Instantiates an ApiClient using a client id and client secret which can be used
 *           * for creating stubs.
 *                *
 *                     * @param vmcServer hostname/ip address of the vmc server
 *                          * @param cspServer hostname/ipaddress of the csp server
 *                               * @param clientId client id of the app
 *                                    * @param clientSecret client secret of the app
 *                                         * @param orgId orgId for which access token needs to be generated. Uses default org if missing.
 *                                              * @return
 *                                                   */
    public ApiClient newVmcClient(String vmcServer, String cspServer,
            String clientId, char[] clientSecret, String orgId) {
        String cspUrl = "https://" + cspServer + CSP_CLIENT_CREDS_AUTH_URL;
        String vmcUrl = "https://" + vmcServer;
        return VmcClients.custom()
                .setBaseUrl(vmcUrl)
                .setAuthorizationUrl(cspUrl)
                .setClientId(clientId)
                .setClientSecret(clientSecret)
                .setOrgId(orgId)
                .build();
    }

    /**
     * Instantiates an NSX ApiClient using a refresh token which can be used
     * for creating stubs.
     *
     * @param vmcServer hostname/ip address of the vmc server
     * @param cspServer hostname/ipaddress of the csp server
     * @param refreshToken refresh token of the user
     * @param orgId organization ID
     * @param sddcId SDDC ID
     * @return An ApiClient that can be used to submit API requests
     */
    public ApiClient newNsxClient(String vmcServer, String cspServer,
            String refreshToken, String orgId, String sddcId) {
        String cspUrl = "https://" + cspServer + CSP_AUTHORIZATION_URL;
        String vmcUrl = "https://" + vmcServer + "/vmc/api/orgs/" + orgId + "/sddcs/" + sddcId + "/networks";
        return VmcClients.custom()
                .setBaseUrl(vmcUrl)
                .setAuthorizationUrl(cspUrl)
                .setRefreshToken(refreshToken.toCharArray())
                .build();
    }
}
