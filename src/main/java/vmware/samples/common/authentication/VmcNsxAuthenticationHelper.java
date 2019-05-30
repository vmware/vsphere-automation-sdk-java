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

import com.vmware.nsx_vmc.client.VmcNsxClients;
import com.vmware.vapi.client.ApiClient;

/**
 * Helper class which provides methods for creating a new <code>ApiClient</code>
 * for VMC APIs
 */
public class VmcNsxAuthenticationHelper {
    public static final String CSP_AUTHORIZATION_URL = "/csp/gateway/am/api/auth/api-tokens/authorize";

    /**
     * Instantiates an ApiClient using a refresh token which can be used for
     * creating stubs.
     *
     * @param refreshToken
     *            refresh token of the user
     * @param verifyServerCertificate
     *            if true, verify the server's certificate
     * @param verifyServerHostname
     *            if true, verify the server's hostname
     * @return
     */
    public ApiClient newVmcNsxPolicyClient(String organizationId, String sddcId,
            String refreshToken, boolean verifyServerCertificate,
            boolean verifyServerHostname) {
        return VmcNsxClients.custom()
                .setRefreshToken(refreshToken.toCharArray())
                .setOrganizationId(organizationId).setSddcId(sddcId)
                .setVerifyServerCertificate(verifyServerCertificate)
                .setVerifyServerHostname(verifyServerHostname).build();
    }
}
