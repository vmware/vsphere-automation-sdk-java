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
package vmware.samples.vcenter.identity;

import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.vmware.vcenter.identity.Providers;
import com.vmware.vcenter.identity.ProvidersTypes.Summary;

import vmware.samples.common.authentication.VapiAuthenticationHelper;

/*
 * Sample code to list the identity providers federated with the
 * vCenter Server. The Identity providers list API doesn't require a
 * an authenticated session. This will enable users to identify
 * the federated IDPs and its various end points.
 * (discovery end point, auth end point, token end point etc) 
 */
public class ListIdentityProviders  {
    private Providers providersService;
    private VapiAuthenticationHelper vAuthHelper = new VapiAuthenticationHelper();
    protected Map<String, Object> parsedOptions;
    protected String server;
    protected boolean skipServerVerification = true;
    protected void parseArgs(String args[]) {  
        Options options = new Options();
        Option serverOption = Option.builder()
                .required(true)
                .hasArg()
                .argName("SERVER")
                .longOpt("server")
                .desc("hostname of vCenter Server")
                .build();
        options.addOption(serverOption);
        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(options, args);
            this.server = cmd.getOptionValue("server");
            } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    protected void setup() throws Exception {
        this.vAuthHelper.createStubFactory(this.server, skipServerVerification);
        this.providersService = 
                this.vAuthHelper.getStubFactory().createStub(Providers.class);
    }

    protected void run(String args[]) throws Exception {
        parseArgs(args);      
        setup();
        List<Summary> identityProviders= providersService.list();
        if(identityProviders.isEmpty())
        {
            System.out.println("Identity Providers not Configured with this vCenter");
        }
        for (Summary summary : identityProviders) {
            if (summary.getIsDefault())
              System.out.println("Default Identity Provider Details \n"+summary);  
            else
                System.out.println("Non Default Identity Provider \n"+summary);
        }

    }
    
    public static void main(String[] args) throws Exception {
        ListIdentityProviders list = new ListIdentityProviders();
        list.run(args);        
    }
}
