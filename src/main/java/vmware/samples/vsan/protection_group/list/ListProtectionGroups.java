/*
 * *******************************************************
 * Copyright (c) 2024 Broadcom. All Rights Reserved.
 * The term "Broadcom" refers to Broadcom Inc.
 * and/or its subsidiaries.
 * SPDX-License-Identifier: MIT
 * *******************************************************
 */
package vmware.samples.vsan.protection_group.list;

import com.vmware.snapservice.clusters.ProtectionGroups;
import com.vmware.snapservice.clusters.ProtectionGroupsTypes;
import org.apache.commons.cli.Option;
import vmware.samples.common.SnapserviceSamplesAbstractBase;
import vmware.samples.vcenter.helpers.ClusterHelper;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Description: Demonstrates getting list of protection groups present
 *              in specific cluster
 *
 * Author: Broadcom, Inc.
 * Sample Prerequisites: vCenter 8.0.3+
 */
public class ListProtectionGroups extends SnapserviceSamplesAbstractBase {
    private ProtectionGroups pgService;
    private String clusterName;
    private String pgName;

    /**
     * Define the options specific to this sample and configure the sample using
     * command-line arguments or a config file
     *
     * @param args command line arguments passed to the sample
     */
    protected void parseArgs(String[] args) {
        Option clusterOption = Option.builder()
                .longOpt("cluster")
                .desc("Specify identifier of the cluster to list the"
                        + " protection groups in it.")
                .argName("CLUSTER")
                .required(true)
                .hasArg()
                .build();

        Option pgOption = Option.builder()
                .longOpt("pg-name")
                .desc("OPTIONAL: Specify the name of the protection group.")
                .argName("PG NAME")
                .required(false)
                .hasArg()
                .build();

        List<Option> optionList = Arrays.asList(clusterOption, pgOption);

        super.parseArgs(optionList, args);
        this.clusterName = (String) parsedOptions.get("cluster");
        this.pgName = (String) parsedOptions.get("pg-name");
    }

    protected void setup() throws Exception {
        this.pgService =
                ssAuthHelper.getStubFactory()
                        .createStub(ProtectionGroups.class, ssStubConfig);
    }

    protected void run() throws Exception {
        // Get cluster identifier
        String clusterId = ClusterHelper.getCluster(
                this.vcAuthHelper.getStubFactory(), vcStubConfig,
                this.clusterName);
        ProtectionGroupsTypes.FilterSpec.Builder bldr = new ProtectionGroupsTypes.FilterSpec.Builder();
        if(null != this.pgName && !this.pgName.isEmpty()){
            bldr.setNames(Collections.singleton(this.pgName));
            //attention: FilterSpe takes no effect now.
        }

        ProtectionGroupsTypes.ListResult pgListResult = this.pgService.list(clusterId, bldr.build());
        System.out.println("----------------------------------------");
        System.out.println("List of protection groups");
        for (ProtectionGroupsTypes.ListItem pgListItem : pgListResult.getItems()) {
            System.out.println("protection group id: " + pgListItem.getPg());
            System.out.println(pgListItem.getInfo().toString());
            System.out.println("----------------------------------------");
        }
        System.out.println("----------------------------------------");
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
        new ListProtectionGroups().execute(args);
    }
}