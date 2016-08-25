/*
 * *******************************************************
 * Copyright VMware, Inc. 2016.  All Rights Reserved.
 * *******************************************************
 *
 * DISCLAIMER. THIS PROGRAM IS PROVIDED TO YOU "AS IS" WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, WHETHER ORAL OR WRITTEN,
 * EXPRESS OR IMPLIED. THE AUTHOR SPECIFICALLY DISCLAIMS ANY IMPLIED
 * WARRANTIES OR CONDITIONS OF MERCHANTABILITY, SATISFACTORY QUALITY,
 * NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE.
 */
package vmware.samples.authentication.sso;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.Option;

import com.vmware.vapi.bindings.StubConfiguration;
import com.vmware.vapi.saml.SamlToken;
import com.vmware.vcenter.Datacenter;
import com.vmware.vcenter.DatacenterTypes;

import vmware.samples.authentication.VapiAuthenticationHelper;
import vmware.samples.common.LookupServiceHelper;
import vmware.samples.common.SamplesAbstractBase;

/**
 * Demonstrates how to create a SSO connection using a SAML Bearer token when
 * we have a vcenter server and external platform services controller.
 */
public class ExternalPscSsoWorkflow extends SamplesAbstractBase {
    private String lookupServiceUrl;
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
                           + "external platform services controller.");

        this.vapiAuthHelper = new VapiAuthenticationHelper();

        System.out.println("\nStep 1: Connect to the lookup service on the "
                           + "Platform Services Controller Node.");
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
                    samlBearerToken);

        System.out.println("\nStep 5: Perform certain tasks using the vAPI "
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