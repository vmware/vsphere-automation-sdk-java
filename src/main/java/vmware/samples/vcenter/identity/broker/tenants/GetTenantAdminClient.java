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
package vmware.samples.vcenter.identity.broker.tenants;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.Option;

import com.vmware.vapi.protocol.HttpConfiguration;
import com.vmware.vcenter.identity.broker.tenants.AdminClient;
import com.vmware.vcenter.identity.broker.tenants.TokenInfo;

import vmware.samples.common.authentication.VapiAuthenticationHelper;
import vmware.samples.common.SamplesAbstractBase;

/*
 * This API is called to get a token to access WS1B endpoints for a given tenant.
 * By default the CUSTOMER tenant is created.
 * The token we get can be used to execute any API on the given tenant. 
 * To call this API, we need VcIdentityProviders.Manage Privilege.
 */
public class GetTenantAdminClient extends SamplesAbstractBase {
    protected AdminClient adminClient;
    protected String server;
    protected String username;
    protected String password;

    protected void parseArgs(String args[]) {
        Option serverOption = Option.builder()
                .required(true)
                .hasArg()
                .argName("SERVER")
                .longOpt("server")
                .desc("hostname of vCenter Server")
                .build();
        Option usernameOption = Option.builder()
                .required(true)
                .hasArg()
                .argName("USERNAME")
                .longOpt("username")
                .desc("username to login to the "
                        + "vCenter Server")
                .build();
        Option passwordOption = Option.builder()
                .required(true)
                .hasArg()
                .argName("PASSWORD")
                .longOpt("password")
                .desc("password to login to the "
                        + "vCenter Server")
                .build();
        Option skipServerVerificationOption = Option.builder()
                .required(false)
                .longOpt("skip-server-verification")
                .type(Boolean.class)
                .desc("OPTIONAL: Specify this option if you do not "
                        + "want to perform SSL certificate "
                        + "verification.\nNOTE: Circumventing SSL "
                        + "trust in this manner is unsafe and should "
                        + "not be used with production code. "
                        + "This is ONLY FOR THE PURPOSE OF "
                        + "DEVELOPMENT ENVIRONMENT.")
                .build();
        List<Option> optionList = Arrays.asList(
            serverOption, usernameOption, passwordOption, skipServerVerificationOption);

        super.parseArgs(optionList, args);
        this.server = (String) parsedOptions.get("server");
        this.username = (String) parsedOptions.get("username");
        this.password = (String) parsedOptions.get("password");
    }

    protected void login() throws Exception {
        vapiAuthHelper = new VapiAuthenticationHelper();
        HttpConfiguration httpConfig = buildHttpConfiguration();
        this.sessionStubConfig =
                vapiAuthHelper.loginByUsernameAndPassword(
                    this.server, this.username, this.password, httpConfig);
    }

    protected void logout() throws Exception {
        vapiAuthHelper.logout();
    }

    protected void setup() throws Exception {
        this.adminClient = vapiAuthHelper.getStubFactory().createStub(AdminClient.class, sessionStubConfig);
    }

    protected void run() throws Exception {
        TokenInfo token = this.adminClient.get("CUSTOMER");
        System.out.println("----------------------------------------");
        System.out.println("The token info is: \n" + token.toString());
        System.out.println("----------------------------------------");
    }

    protected void cleanup() throws Exception {
        // No cleanup required.
    }

    public static void main(String[] args) throws Exception {
        /*
         * Execute the sample using the command line arguments or parameters
         * from the configuration file. This executes the following steps:
         * 1. Parse the arguments required by the sample
         * 2. Login to the server
         * 3. Setup any resources required by the sample run
         * 4. Run the sample
         * 5. Cleanup any data created by the sample run, if cleanup=true
         * 6. Logout of the server
         */
        new GetTenantAdminClient().execute(args);
    }
}

