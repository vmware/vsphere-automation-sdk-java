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
package vmware.samples.sso.embeddedpsc;

import java.security.KeyStore;
import java.util.Collections;

import org.apache.commons.cli.Option;

import com.vmware.vapi.bindings.StubConfiguration;
import com.vmware.vapi.saml.SamlToken;
import com.vmware.vcenter.Datacenter;
import com.vmware.vcenter.DatacenterTypes;

import vmware.samples.common.SamplesAbstractBase;
import vmware.samples.common.authentication.VapiAuthenticationHelper;
import vmware.samples.sso.SsoHelper;

/**
 * Description: Demonstrates how to create a SSO connection using a SAML Bearer token when
 * we have a vCenter server and embedded Platform Services Controller.
 * 
 * Author: VMware, Inc.
 */
public class EmbeddedPscSsoWorkflow extends SamplesAbstractBase {
    private Datacenter datacenterService;
    public static final String SSO_PATH = "/sts/STSService";

    /**
     * Define the options specific to this sample and configure the sample using
     * command-line arguments or a config file
     *
     * @param args command line arguments passed to the sample
     */
    @Override
    public void parseArgs(String[] args) {
        // Parse the command line options or config file
        super.parseArgs(Collections.<Option>emptyList(), args);
    }

    @Override
    protected void setup() throws Exception {
        // No setup required for the sample

    }

    @Override
    public void run() throws Exception {

        System.out.println("\n\n#### Example: Login to vCenter server with "
                           + "embedded Platform Services Controller");

        this.vapiAuthHelper = new VapiAuthenticationHelper();

        /*
         * Since the platform services controller is embedded, the sso server
         * is the same as the vcenter server.
         */
        String ssoUrl = "https://" + this.server + SSO_PATH;

        KeyStore trustStore = setupSslTrustForServer();

        System.out.println("\nStep 1: Connect to the Single Sign-On URL and "
                           + "retrieve the SAML bearer token.");
        SamlToken samlBearerToken = SsoHelper.getSamlBearerToken(ssoUrl,
            username,
            password);

        System.out.println("\nStep 2. Login to vAPI services using the "
                           + "SAML bearer token.");
        StubConfiguration sessionStubConfig =
                this.vapiAuthHelper.loginBySamlBearerToken(this.server,
                    samlBearerToken, trustStore);

        System.out.println("\nStep 3: Perform certain tasks using the vAPI "
                           + "services.");
        this.datacenterService = this.vapiAuthHelper.getStubFactory()
            .createStub(Datacenter.class, sessionStubConfig);
        System.out.println("\nList of datacenters on the vcenter server:\n"
                           + this.datacenterService.list(
                               new DatacenterTypes.FilterSpec()));
        vapiAuthHelper.logout();
    }

    @Override
    protected void cleanup() throws Exception {
        // No cleanup required for the sample
    }

    public static void main(String[] args) throws Exception {
        EmbeddedPscSsoWorkflow embeddedPscSsoWorkflow = null;
        embeddedPscSsoWorkflow = new EmbeddedPscSsoWorkflow();

        // Parse the command line arguments or the configuration file
        embeddedPscSsoWorkflow.parseArgs(args);

        // Execute the sample
        embeddedPscSsoWorkflow.run();
    }
}