/*
 * *******************************************************
 * Copyright (c) 2024 Broadcom. All Rights Reserved.
 * The term "Broadcom" refers to Broadcom Inc.
 * and/or its subsidiaries.
 * SPDX-License-Identifier: MIT
 * *******************************************************
 */
package vmware.samples.vsan.protection_group.delete;

import com.vmware.snapservice.ProtectionGroupStatus;
import com.vmware.snapservice.Tasks;
import com.vmware.snapservice.clusters.ProtectionGroups;
import com.vmware.snapservice.clusters.ProtectionGroupsTypes;
import com.vmware.snapservice.tasks.Info;
import com.vmware.snapservice.tasks.Status;
import org.apache.commons.cli.Option;
import vmware.samples.common.SnapserviceSamplesAbstractBase;
import vmware.samples.vcenter.helpers.ClusterHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Description: Demonstrates deleting protection groups.
 * <p>
 * Author: Broadcom, Inc.
 * Sample Prerequisites: vCenter 8.0.3+
 */
public class DeleteProtectionGroups extends SnapserviceSamplesAbstractBase {
    private ProtectionGroups pgService;
    private Tasks taskService;
    private String clusterName;
    private String pgs;
    private Boolean force;

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
                .longOpt("pg-names")
                .desc("OPTIONAL: Specify the name of the protection groups,"
                        + " separate by common.")
                .argName("PG NAMEs")
                .required(true)
                .hasArg()
                .build();

        Option forceOption = Option.builder()
                .longOpt("force")
                .desc("OPTIONAL: Whether delete all related snapshots.")
                .required(false)
                .type(Boolean.class)
                .build();

        List<Option> optionList = Arrays.asList(clusterOption, pgOption, forceOption);

        super.parseArgs(optionList, args);
        this.clusterName = (String) parsedOptions.get("cluster");
        this.pgs = (String) parsedOptions.get("pg-names");
        this.force = parsedOptions.get("force") != null;
    }

    protected void setup() throws Exception {
        this.pgService =
                ssAuthHelper.getStubFactory()
                        .createStub(ProtectionGroups.class, ssStubConfig);

        this.taskService =
                ssAuthHelper.getStubFactory()
                        .createStub(Tasks.class, ssStubConfig);
    }

    protected void run() throws Exception {
        // Get cluster identifier
        String clusterId = ClusterHelper.getCluster(
                this.vcAuthHelper.getStubFactory(), vcStubConfig,
                this.clusterName);

        // Get protection groups info
        ProtectionGroupsTypes.ListResult pgListResult = this.pgService.list(clusterId, null);

        // Get protection groups identifier
        List<String> pgNames = new ArrayList<>();
        if (null != this.pgs && !this.pgs.isEmpty()) {
            pgNames = Arrays.asList(this.pgs.split(","));
        }

        // Skip groups that are mutable or marked as deleted
        List<String> pgList = new ArrayList<>();
        for (ProtectionGroupsTypes.ListItem pgListItem : pgListResult.getItems()) {
            if (pgNames.contains(pgListItem.getInfo().getName())) {
                if (pgListItem.getInfo().getLocked()) {
                    System.out.println("\nProtection group \"" +
                            pgListItem.getInfo().getName() + "\" is mutable, skip the deletion.");
                } else if (pgListItem.getInfo().getStatus() == ProtectionGroupStatus.MARKED_FOR_DELETE) {
                    System.out.println("\nProtection group \"" +
                            pgListItem.getInfo().getName() + "\" is marked as deleted, skip the deletion.");
                } else {
                    pgList.add(pgListItem.getPg());
                    System.out.println("\nProtection group \"" +
                            pgListItem.getInfo().getName() + "\" : " + pgListItem.getPg());
                }
            }
        }

        ProtectionGroupsTypes.DeleteSpec.Builder bldr =
                new ProtectionGroupsTypes.DeleteSpec.Builder().setForce(this.force);

        List<String> taskList = new ArrayList<>();
        for (String pg : pgList) {
            String task = this.pgService.delete_Task(clusterId, pg, bldr.build());
            System.out.println("\n###Deleting protection groups: \"" + pg + "\", task id: " + task);
            taskList.add(task);
        }

        // Wait for tasks to complete.
        int taskCompleted = 0;
        while (true) {
            for (String t : taskList) {
                Info info = this.taskService.get(t);

                if (info.getStatus() == Status.SUCCEEDED) {
                    System.out.println("\n###Task " + t + " succeeds.");
                    taskCompleted++;
                } else if (info.getStatus() == Status.FAILED) {
                    System.out.println("\n###Task " + t + " fails. \nErrors:\n");
                    System.out.println(info.getResult().toString());
                    taskCompleted++;
                } else {
                    System.out.println("\n###Task " + t + "progress: " + info.getProgress());
                }
            }

            if (taskCompleted >= taskList.size()) {
                System.out.println("\n\n###All protection group deletion jobs are completed");
                break;
            }
            Thread.sleep(5000);
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
        new DeleteProtectionGroups().execute(args);
    }

}