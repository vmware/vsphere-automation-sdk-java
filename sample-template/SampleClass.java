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
package vmware.samples.feature;

import java.util.Collections;
import java.util.List;

import org.apache.commons.cli.Option;
import com.vmware.vcenter.VM;
import vmware.samples.common.SamplesAbstractBase;

/**
 * Description: <TODO: Sample Description>
 *
 * Author: VMware, Inc.
 * Sample Prerequisites: <TODO>
 */
public class SampleClass extends SamplesAbstractBase {
    private VM vmService;

    /**
     * Define the options specific to this sample and configure the sample using
     * command-line arguments or a config file
     *
     * @param args command line arguments passed to the sample
     */
    protected void parseArgs(String[] args) {
    	List<Option> optionList = Collections.<Option>emptyList();
        super.parseArgs(optionList, args);
    }

    protected void setup() throws Exception {
        this.vmService =
                vapiAuthHelper.getStubFactory()
                    .createStub(VM.class, sessionStubConfig);
    }

    protected void run() throws Exception {
        // TODO: Add your sample code here
    }
    protected void cleanup() throws Exception {
    	// TODO: Add cleanup here
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
        new SampleClass().execute(args);
    }
}
