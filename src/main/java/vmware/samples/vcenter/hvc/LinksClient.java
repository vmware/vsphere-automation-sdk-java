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
package vmware.samples.vcenter.hvc;

import org.apache.commons.cli.Option;
import vmware.samples.common.SamplesAbstractBase;
import com.vmware.vcenter.hvc.Links;
import com.vmware.vcenter.hvc.LinksTypes;

import java.util.Arrays;
import java.util.List;

/**
 * Description: Demonstrates link Create, List, Delete operations with a
 * foreign platform service controller (PSC) on a different SSO domain.
 * Step 1: Create a link with a foreign domain.
 * Step 2: List all the linked domains.
 * Step 3: Delete the existing link with the foreign domain.
 *
 * Author: VMware, Inc.
 * Sample Prerequisites:
 * The sample needs a second vCenter on a different SSO domain.
 * The user invoking the API should have the HLM.Manage privilege.
 */
public class LinksClient extends SamplesAbstractBase {
    private String _foreignHostname;
    private String _foreignUsername;
    private String _foreignPassword;
    private String _foreignDomain;
    private String _foreignPort;
    private Links _linkProvider;
    private String linkId;

    /**
     * Define the options specific to this sample and configure the sample using
     * command-line arguments or a config file
     *
     * @param args command line arguments passed to the sample
     */
    protected void parseArgs(String[] args) {
        Option foreignHostOption = Option.builder()
              .longOpt("foreignhost")
              .desc("Foreign PSC hostname to link to.")
              .argName("FOREIGN PSC HOSTNAME")
              .required(true)
              .hasArg()
              .build();
        Option foreignUsernameOption = Option.builder()
              .longOpt("foreignusername")
              .desc("Administrator username for the foreign domain. "
                    + "Eg - Administrator")
              .argName("FOREIGN USERNAME")
              .required(true)
              .hasArg()
              .build();
        Option foreignPasswordOption = Option.builder()
              .longOpt("foreignpassword")
              .desc("Administrator password for the foreign domain.")
              .required(true)
              .argName("FOREIGN PASSWORD")
              .hasArg()
              .build();
        Option foreignDomainOption = Option.builder()
              .longOpt("foreigndomain")
              .desc("SSO Domain name for the foreign PSC. Eg - vsphere.local")
              .argName("FOREIGN DOMAIN")
              .required(true)
              .hasArg()
              .build();
        Option foreignPortOption = Option.builder()
              .longOpt("foreignport")
              .desc("OPTIONAL: Foreign HTTPS Port")
              .argName("FOREIGN PORT")
              .required(false)
              .hasArg()
              .build();
        List<Option> optionList = Arrays.asList(foreignHostOption,
              foreignUsernameOption,
              foreignPasswordOption,
              foreignDomainOption,
              foreignPortOption);
        super.parseArgs(optionList, args);
        this._foreignHostname = (String) parsedOptions.get("foreignhost");
        this._foreignUsername = (String) parsedOptions.get("foreignusername");
        this._foreignPassword = (String) parsedOptions.get("foreignpassword");
        this._foreignDomain = (String) parsedOptions.get("foreigndomain");
        this._foreignPort = (String) parsedOptions.get("foreignport");
    }

    protected void setup() throws Exception {
        this._linkProvider =
              vapiAuthHelper.getStubFactory().createStub(Links.class,
                    sessionStubConfig);
    }

    protected void run() throws Exception {
        //Prepare the spec for creating a link
        LinksTypes.CreateSpec spec = new LinksTypes.CreateSpec();
        //Set the PSC hostname of the foreign SSO domain being linked
        spec.setPscHostname(_foreignHostname);
        spec.setUsername(_foreignUsername);
        spec.setPassword(_foreignPassword.toCharArray());
        spec.setDomainName(_foreignDomain);
        if (_foreignPort != null) {
            spec.setPort(_foreignPort);
        }
        //Invoke the create operation and get link id back on a successful link
        this.linkId = _linkProvider.create(spec);
        System.out.println("Link successful. Link ID - " + this.linkId);

        //Invoke the list operation to print out all the linked domains
        System.out.println("Getting all the links.");
        List<LinksTypes.Summary> linkedDomains = _linkProvider.list();
        for (LinksTypes.Summary summary : linkedDomains) {
            System.out.println("Link ID: " + summary.getLink() +
                  ", Linked Domain: " + summary.getDisplayName());
        }
    }

    protected void cleanup() throws Exception {
        //Invoke the delete operation and pass the existing link id to remove.
        if(null != this.linkId) {
            _linkProvider.delete(linkId);
            System.out.println("Link removed successful.");
        }
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
        new LinksClient().execute(args);
    }
}