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
package vmware.samples.appliance.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.Option;

import com.vmware.appliance.Services;
import com.vmware.appliance.ServicesTypes.Info;

import vmware.samples.common.SamplesAbstractBase;

/**
 * Description: Demonstrates services api workflow
 * 1.List all services
 * 2.Stop a running service
 * 3.Get details of stopped service
 * 4.Start the service stopped in step 2
 * 5.Get details of service
 * 6.Restart the service
 * 7.Get details of service
 * Author: VMware, Inc. Sample Prerequisites: vCenter 6.7+
 */
public class ServicesWorkflow extends SamplesAbstractBase {
    private Services applianceServicesApiStub;
    private String serviceName;
    private Map<String, Info> svcList;
    private Boolean listServices;

    protected void setup() throws Exception {
        this.applianceServicesApiStub = vapiAuthHelper.getStubFactory()
            .createStub(Services.class, sessionStubConfig);
    }

    protected void formattedOutputDisplay(Info info, String serviceName) {
        System.out.println("Service: " + serviceName);
        System.out.println("Description: " + info.getDescription());
        System.out.println("State: " + info.getState());
        System.out.println("----------");
    }

    protected void run() throws Exception {

        if(this.listServices)
        {
            //List all appliance services using services api
            System.out.println("#### Example: List Appliance Services:");
            //get the list of services
            svcList = applianceServicesApiStub.list();
            //fomatting the list api output
            for (Map.Entry<String, Info> svc : svcList.entrySet()) {
                formattedOutputDisplay(svc.getValue(), svc.getKey());
            }
        }
        if(null != this.serviceName)
        {
            // Stop a service using services api
            System.out.println("#### Example:  Stopping service " + serviceName
                               + "\n");
            applianceServicesApiStub.stop(serviceName);
            // Get details of the service stopped in previous step using services
            // api
            formattedOutputDisplay(applianceServicesApiStub.get(serviceName),
                serviceName);
            // Start a stopped service using services api
            System.out.println("#### Example: Starting service " + serviceName
                               + "\n");
            applianceServicesApiStub.start(serviceName);

            // Get details of the service started in previous step using services
            // api
            formattedOutputDisplay(applianceServicesApiStub.get(serviceName),
                serviceName);
            // Restart a service using services api
            System.out.println("#### Example: Restarting service " + serviceName
                               + "\n");
            applianceServicesApiStub.restart(serviceName);

            // Get details of the service restarted in previous step using services
            // api
            System.out.println("#### Example: Getting service details for "
                               + serviceName + "\n");
            formattedOutputDisplay(applianceServicesApiStub.get(serviceName),
                serviceName);
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
        new ServicesWorkflow().execute(args);
    }

    protected void parseArgs(String[] args) {
        Option serviceName = Option.builder()
            .longOpt("svc_name")
            .desc("OPTIONAL: Specify servicename for all operations")
            .argName("SERVICE_NAME")
            .required(false)
            .hasArg(true)
            .build();
        Option listServices = Option.builder()
            .longOpt("list")
            .desc("OPTIONAL: Lists all the Operations")
            .argName("LIST_OPERATION")
            .required(false)
            .hasArg(false)
            .build();

        List<Option> optionList = new ArrayList<Option>();
        optionList.addAll(Arrays.asList( listServices, serviceName));
        super.parseArgs(optionList, args);
        this.serviceName = (String) parsedOptions.get("svc_name");
        Object lstSVC = parsedOptions.get("list");
        this.listServices = ( null == lstSVC )? false:true;
        if(null == this.serviceName && !this.listServices) {
            System.out.println("\n ERROR: Either of svc_name or list option must be provided");
            System.exit(0);
        }
    }
}