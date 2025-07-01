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
package vmware.samples.appliance.health;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.Option;

import com.vmware.appliance.Health;
import com.vmware.appliance.Notification;
import com.vmware.vapi.std.LocalizableMessage;

import vmware.samples.common.SamplesAbstractBase;

/**
 * Description: Demonstrates getting Health messages for various health items
 *
 * Author: VMware, Inc. Sample Prerequisites: vCenter 6.7+
 */
public class HealthMessages extends SamplesAbstractBase {
    private Health healthService;
    private String item;

    protected void setup() throws Exception {
        this.healthService = vapiAuthHelper.getStubFactory().createStub(
            Health.class,
            sessionStubConfig);
    }

    protected void run() throws Exception {
        List<Notification> messages = healthService.messages(this.item);
        if (messages.isEmpty()) {
            System.out.println("No health alarms for : " + this.item);
        } else {
            System.out.println("Health Alarms");
            System.out.println("-------------\n");
            for (Notification message : messages) {
                System.out.println("------------------------------------------"
                                   + "-------------");
                System.out.println("Alert time : " + message.getTime()
                    .getTime()
                    .toString());
                System.out.println("Alert message Id: " + message.getId());
                LocalizableMessage msg = message.getMessage();
                System.out.println("Alert message : " + msg
                    .getDefaultMessage());
                LocalizableMessage resolution = message.getResolution();
                System.out.println("Resolution : " + resolution
                    .getDefaultMessage());
                System.out.println("-------------------------------------------"
                                   + "-------------");
            }
        }
    }

    protected void cleanup() throws Exception {
        // No cleanup required
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
        new HealthMessages().execute(args);
    }

    protected void parseArgs(String[] args) {
        Option healthMsgOption = Option.builder()
            .longOpt("item")
            .desc("REQUIRED: Specify the name of health item"
                  + " to view the messages.")
            .argName("ITEM")
            .required(true)
            .hasArg()
            .build();
        List<Option> optionList = Arrays.asList(healthMsgOption);
        super.parseArgs(optionList, args);
        this.item = (String) parsedOptions.get("item");
    }
}
