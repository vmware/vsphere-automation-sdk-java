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
package vmware.samples.appliance.timezone;

import java.util.Arrays;
import java.util.List;
import org.apache.commons.cli.Option;
import com.vmware.appliance.system.time.Timezone;
import vmware.samples.common.SamplesAbstractBase;

/**
 * Description: Demonstrates setting and getting TimeZone.Accepted values are
 * valid Timezone values for appliance
 *
 * Author: VMware, Inc. Sample Prerequisites: vCenter 6.7+
 */
public class TimeZoneSample extends SamplesAbstractBase {
    private Timezone timeZone;
    private String timeZoneName;

    protected void parseArgs(String[] args) {

        // Fetch command line argument for TimeZone
        Option timeZoneOption = Option.builder()
            .required(true)
            .hasArg()
            .argName("TIME_ZONE")
            .longOpt("timezone")
            .desc("TimeZone to set on appliance")
            .build();
        List<Option> optionList = Arrays.asList(timeZoneOption);
        super.parseArgs(optionList, args);
        timeZoneName = (String) parsedOptions.get("timezone");
    }

    protected void setup() throws Exception {
        this.timeZone = vapiAuthHelper.getStubFactory().createStub(
            Timezone.class,
            sessionStubConfig);
    }

    protected void run() throws Exception {
        System.out.println("Setting the appliance timeZone as : "
                           + timeZoneName);
        timeZone.set(timeZoneName);
        System.out.println("Timezone is set as : " + timeZone.get());
    }

    protected void cleanup() throws Exception {
        // No clean up required.
    }

    public static void main(String[] args) throws Exception {
        /*
         * Execute the sample using the command line arguments or parameters
         * from the configuration file.
         * This executes the following steps:
         * 1. Pars the arguments required by the sample
         * 2. Login to the server
         * 3. Setup any resources required by the sample run
         * 4. Run the sample
         * 5. Cleanup any data created by the sample run, if cleanup=true
         * 6. Logout of the server
         */
        new TimeZoneSample().execute(args);
    }
}
