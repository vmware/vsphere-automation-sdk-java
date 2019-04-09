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
package vmware.samples.sso.externalpsc;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.Option;

import com.vmware.cis.tagging.Tag;
import com.vmware.vapi.bindings.StubConfiguration;
import com.vmware.vapi.protocol.HttpConfiguration;
import com.vmware.vapi.saml.SamlToken;

import vmware.samples.common.SamplesAbstractBase;
import vmware.samples.common.authentication.VapiAuthenticationHelper;
import vmware.samples.sso.LookupServiceHelper;
import vmware.samples.sso.SsoHelper;

/**
 * Description: Demonstrates how to create a SSO connection using a SAML Bearer
 * token when we have a vcenter server and external Platform Services 
 * Controller.
 * 
 * Author: VMware, Inc.
 */
public class ExternalPscSsoWorkflow extends SamplesAbstractBase {
    private String lookupServiceUrl;
    private Tag taggingService;

    /**
     * Define the options specific to this sample and configure the sample using
     * command-line arguments or a config file
     *
     * @param args command line arguments passed to the sample
     */
    @Override
    public void parseArgs(String[] args) {
        // Parse the command line options or config file
        Option lookupServiceUrlOption = Option.builder()
            .longOpt("lookupserviceurl")
            .desc("url of the lookup service")
            .argName("LOOKUP SERVICE URL")
            .required(true)
            .hasArg()
            .build();
        List<Option> optionList = Arrays.asList(lookupServiceUrlOption);
        super.parseArgs(optionList, args);
        this.lookupServiceUrl = (String) parsedOptions.get("lookupserviceurl");
    }

    @Override
    protected void setup() throws Exception {
        // No setup required for the sample
    }

    @Override
    public void run() throws Exception {
        System.out.println("\n\n#### Example: Login to vCenter server with "
                           + "external Platform Services Controller.");
        
        HttpConfiguration httpConfig = this.buildHttpConfiguration();
        
        this.vapiAuthHelper = new VapiAuthenticationHelper();

        System.out.println("\nStep 1: Connect to the lookup service on the "
                           + "Platform Services Controller node.");
        LookupServiceHelper lookupServiceHelper = new LookupServiceHelper(
            this.lookupServiceUrl);

        System.out.println("\nStep 2: Discover the Single Sign-On service URL"
                           + " from lookup service.");
        String ssoUrl = lookupServiceHelper.findSsoUrl();

        System.out.println("\nStep 3: Connect to the Single Sign-On URL and "
                           + "retrieve the SAML bearer token.");
        SamlToken samlBearerToken = SsoHelper.getSamlBearerToken(ssoUrl,
            username,
            password);

        System.out.println("\nStep 4. Login to vAPI services using the "
                + "SAML bearer token.");

        StubConfiguration sessionStubConfig =
                this.vapiAuthHelper.loginBySamlBearerToken(this.server,
                    samlBearerToken, httpConfig);

        System.out.println("\nStep 5: Perform certain tasks using the vAPI "
                           + "services.");
        this.taggingService = this.vapiAuthHelper.getStubFactory()
            .createStub(Tag.class, sessionStubConfig);
        System.out.println("\nListing all tags on the vcenter server..");
        List<String> tagList = this.taggingService.list();
        if(tagList.isEmpty()) {
            System.out.println("\nNo tags found !");
        } else {
            System.out.println("\nTag Name\tTag Description");
            for(String tagId : tagList) {
                System.out.println(this.taggingService.get(tagId).getName()
                        + "\t" + this.taggingService.get(tagId).getDescription());
            }
        }
        vapiAuthHelper.logout();
    }

    @Override
    protected void cleanup() throws Exception {
        // No cleanup required
    }

    public static void main(String[] args) throws Exception {
        ExternalPscSsoWorkflow externalPscSsoWorkflow = null;
        externalPscSsoWorkflow = new ExternalPscSsoWorkflow();

        // Parse the command line arguments or the configuration file
        externalPscSsoWorkflow.parseArgs(args);

        // Execute the sample
        externalPscSsoWorkflow.run();
    }
}