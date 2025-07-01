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

import com.vmware.vcenter.hvc.management.Administrators;
import org.apache.commons.cli.Option;
import vmware.samples.common.SamplesAbstractBase;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Description: Demonstrates Add, Get, Remove operations for a given
 * Identity Source group to the CloudAdminGroup.
 * Step 1: Add the given group to CloudAdminGroup.
 * Step 2: Get all the groups in CloudAdminGroup.
 * Step 3: Remove the given group from CloudAdminGroup.
 *
 * Author: VMware, Inc.
 * Sample Prerequisites:
 * The sample needs an Identity source added to the vCenter apart from the
 * default ones already added during vCenter deployment.
 * The Identity source should contain one SSO group.
 * The user invoking the API should have the HLM.Manage privilege.
 */
public class AdministratorClient extends SamplesAbstractBase {
    private String _groupName;
    private Administrators _administratorProvider;

    /**
     * Define the options specific to this sample and configure the sample using
     * command-line arguments or a config file
     *
     * @param args command line arguments passed to the sample
     */
    protected void parseArgs(String[] args) {
        Option groupNameOption = Option.builder()
              .longOpt("groupname")
              .desc("Group name to be added to CloudAdminGroup group. "
                    + "eg - admingroup@coke.com")
              .argName("GROUP NAME")
              .required(true)
              .hasArg()
              .build();
        List<Option> optionList = Arrays.asList(groupNameOption);
        super.parseArgs(optionList, args);
        this._groupName = (String) parsedOptions.get("groupname");
    }

    protected void setup() throws Exception {
        this._administratorProvider =
              vapiAuthHelper.getStubFactory().createStub(Administrators.class,
                    sessionStubConfig);
    }

    protected void run() throws Exception {
        //Add the group to CloudAdminGroup
        _administratorProvider.add(this._groupName);
        System.out.println("Group added successful.");

        //Get all the groups which belong to CloudAdminGroup
        System.out.println("Getting all the groups under CloudAdminGroup.");
        Set<String> groups =_administratorProvider.get();
        for(String groupName : groups) {
            System.out.println("Group: " + groupName);
        }
    }

    protected void cleanup() throws Exception {
        //Remove the group from CloudAdminGroup
        if(null != this._groupName) {
            _administratorProvider.remove(_groupName);
            System.out.println("Group removed successful.");
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
        new AdministratorClient().execute(args);
    }
}
