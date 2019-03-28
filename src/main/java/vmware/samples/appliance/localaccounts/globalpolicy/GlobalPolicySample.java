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
package vmware.samples.appliance.localaccounts.globalpolicy;

import java.util.Arrays;
import java.util.List;
import org.apache.commons.cli.Option;
import com.vmware.appliance.local_accounts.Policy;
import com.vmware.appliance.local_accounts.PolicyTypes.Info;
import vmware.samples.common.SamplesAbstractBase;

/**
 * Description: Demonstrates set and get Global policy values.
 *
 * Author: VMware, Inc. Sample Prerequisites: vCenter 6.7+
 */
public class GlobalPolicySample extends SamplesAbstractBase {
    private Policy localAccountsPolicy;
    private Info policyParamInfo;
    private long maxDays, minDays, warnDays;
    final private String MAX_DAYS_TOKEN = "maxDays";
    final private String MIN_DAYS_TOKEN = "minDays";
    final private String WARN_DAYS_TOKEN = "warnDays";

    protected void parseArgs(String[] args) {

        // Fetch command line arguments for maxDays,minDays and warnDays
        Option max_days = Option.builder()
            .required(false)
            .hasArg()
            .argName("MAX_DAYS")
            .longOpt(MAX_DAYS_TOKEN)
            .desc("OPTIONAL:max days to be set to localaccounts globalpolicy")
            .build();

        // min_days value is optional.
        Option min_days = Option.builder()
            .required(false)
            .hasArg()
            .argName("MIN_DAYS")
            .longOpt(MIN_DAYS_TOKEN)
            .desc("OPTIONAL:min days to be set to localaccounts globalpolicy")
            .build();

        // warn_days value is optional
        Option warn_days = Option.builder()
            .required(false)
            .hasArg()
            .argName("WARN_DAYS")
            .longOpt(WARN_DAYS_TOKEN)
            .desc("OPTIONAL:warn days to be set to localaccounts globalpolicy")
            .build();
        List<Option> optionList = Arrays.asList(max_days, min_days, warn_days);
        super.parseArgs(optionList, args);
        if (parsedOptions.containsKey(MAX_DAYS_TOKEN)) {
            if (parsedOptions.get(MAX_DAYS_TOKEN) == null) {
                System.out.println("Provide value for option maxDays:");
                System.exit(0);
            } else {
                maxDays = Long.parseLong((String) parsedOptions.get(MAX_DAYS_TOKEN));
            }
        }
        if (parsedOptions.containsKey(MIN_DAYS_TOKEN)) {
            if (parsedOptions.get(MIN_DAYS_TOKEN) == null) {
                System.out.println("Provide value for option minDays:");
                System.exit(0);
            } else {
                minDays = Long.parseLong((String) parsedOptions.get(MIN_DAYS_TOKEN));
            }
        }
        if (parsedOptions.containsKey(WARN_DAYS_TOKEN)) {
            if (parsedOptions.get(WARN_DAYS_TOKEN) == null) {
                System.out.println("Provide value for option warnDays:");
                System.exit(0);
            } else {
                warnDays = Long.parseLong((String) parsedOptions.get(
                    WARN_DAYS_TOKEN));
            }
        }
    }

    protected void setup() throws Exception {
        this.localAccountsPolicy = vapiAuthHelper.getStubFactory().createStub(
            Policy.class,
            sessionStubConfig);
    }

    protected void run() throws Exception {

        policyParamInfo = new Info();
        policyParamInfo.setMaxDays(maxDays);
        policyParamInfo.setMinDays(minDays);
        policyParamInfo.setWarnDays(warnDays);
        System.out.println(
            "Setting values now as per passed parameters maxDays:" + maxDays
                           + ", minDays:" + minDays + ", warnDays:" + warnDays);
        localAccountsPolicy.set(policyParamInfo);
        System.out.println(
            "Values which are set are displayed below after get call:");
        System.out.println("Maximum number of days between password change:"
                           + localAccountsPolicy.get().getMaxDays());
        System.out.println("Minimum number of days between password change:"
                           + localAccountsPolicy.get().getMinDays());
        System.out.println("Number of days of warning before password expires:"
                           + localAccountsPolicy.get().getWarnDays());

    }

    protected void cleanup() throws Exception {
        // No cleanup required.
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
        new GlobalPolicySample().execute(args);
    }
}