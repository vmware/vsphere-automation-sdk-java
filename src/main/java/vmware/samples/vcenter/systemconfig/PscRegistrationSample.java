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
package vmware.samples.vcenter.systemconfig;

import java.util.Collections;
import org.apache.commons.cli.Option;
import com.vmware.vcenter.system_config.PscRegistration;
import vmware.samples.common.SamplesAbstractBase;

/**
 * Description:API sample which displays PSC Registration Information
 *
 * Author: VMware, Inc. Sample Prerequisites: vCenter 6.7+
 */
public class PscRegistrationSample extends SamplesAbstractBase {
    private PscRegistration pscRegistration;

    protected void setup() throws Exception {
        this.pscRegistration = vapiAuthHelper.getStubFactory().createStub(
            PscRegistration.class,
            sessionStubConfig);
    }

    protected void run() throws Exception {
        System.out.println("Getting Platform Services Controller info for vCenter");
        System.out.println("Fully Qualified Domain Name of the Platform Services Controller node : "
                           + pscRegistration.get().getAddress());
        System.out.println("HTTPS port address : " + pscRegistration.get()
            .getHttpsPort());
        System.out.println("SSO Domain Name : " + pscRegistration.get()
            .getSsoDomain());
    }

    protected void cleanup() throws Exception {
        // This sample is only a get call so clean up is not required.
    }

    public static void main(String[] args) throws Exception {
        /*
         * Execute the sample using the command line arguments or parameters
         * from the configuration file.
         * This executes the following steps:
         * 1. Parse the arguments required by the sample
         * 2. Login to the server
         * 3. Setup any resources required by the sample run
         * 4. Run the sample
         * 5. Cleanup any data created by the sample run, if cleanup=true
         * 6. Logout of the server
         */
        new PscRegistrationSample().execute(args);
    }

    @Override
    protected void parseArgs(String[] args) {
        // This sample is only a get call so no specific arguments to parse.
        super.parseArgs(Collections.<Option> emptyList(), args);

    }
}
