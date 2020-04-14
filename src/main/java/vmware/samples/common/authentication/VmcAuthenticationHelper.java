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
