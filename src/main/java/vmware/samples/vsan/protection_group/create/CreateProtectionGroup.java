/*
 * *******************************************************
 * Copyright (c) 2024 Broadcom. All Rights Reserved.
 * The term "Broadcom" refers to Broadcom Inc.
 * and/or its subsidiaries.
 * SPDX-License-Identifier: MIT
 * *******************************************************
 */
package vmware.samples.vsan.protection_group.create;

import com.vmware.snapservice.clusters.ProtectionGroups;
import com.vmware.snapservice.clusters.ProtectionGroupsTypes;
import com.vmware.snapservice.ProtectionGroupSpec;
import com.vmware.snapservice.RetentionPeriod;
import com.vmware.snapservice.SnapshotSchedule;
import com.vmware.snapservice.SnapshotPolicy;
import com.vmware.snapservice.TargetEntities;
import com.vmware.snapservice.Tasks;
import com.vmware.snapservice.TimeUnit;
import com.vmware.snapservice.tasks.Info;
import com.vmware.snapservice.tasks.Status;
import org.apache.commons.cli.Option;
import vmware.samples.common.SnapserviceSamplesAbstractBase;
import vmware.samples.vcenter.helpers.ClusterHelper;
import vmware.samples.vcenter.helpers.VmHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * Description: Demonstrates creating protection group
 * <p>
 * Author: Broadcom, Inc.
 * Sample Prerequisites: vCenter 8.0.3+
 */
public class CreateProtectionGroup extends SnapserviceSamplesAbstractBase {
    private ProtectionGroups pgService;
    private Tasks taskService;
    private String clusterName;
    private String clusterId;
    private String pgName;
    private String vmNames;
    private String vmFormats;
    private long schedule;
    private String scheduleUnit;
    private long retention;
    private String retentionUnit;
    private Boolean lock;
    private String pgId;

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
                .desc("Specify the name of the protection group.")
                .argName("PG NAME")
                .required(true)
                .hasArg()
                .build();

        Option vmOption = Option.builder()
                .longOpt("vm-names")
                .desc("OPTIONAL: Specify the name of the VMs to be protected."
                        + " separate by common.")
                .argName("VM NAMES")
                .required(false)
                .hasArg()
                .build();

        Option vmFormatOption = Option.builder()
                .longOpt("vm-formats")
                .desc("OPTIONAL: Specify the name format of the VMs to be protected."
                        + " separate by common.")
                .argName("VM FORMATS")
                .required(false)
                .hasArg()
                .build();

        Option scheduleOption = Option.builder()
                .longOpt("schedule")
                .desc("Specify the schedule of the protection group.")
                .argName("SCHEDULE")
                .required(true)
                .hasArg()
                .build();

        Option scheduleUnitOption = Option.builder()
                .longOpt("schedule-unit")
                .desc("Specify the schedule unit of the protection group.")
                .argName("SCHEDULE UNIT")
                .required(true)
                .hasArg()
                .build();

        Option retentionOption = Option.builder()
                .longOpt("retention")
                .desc("Specify the retention of the protection group.")
                .argName("RETENTION")
                .required(true)
                .hasArg()
                .build();

        Option retentionUnitOption = Option.builder()
                .longOpt("retention-unit")
                .desc("Specify the retention unit of the protection group.")
                .argName("RETENTION UNIT")
                .required(true)
                .hasArg()
                .build();

        Option lockOption = Option.builder()
                .longOpt("lock")
                .desc("OPTIONAL: Whether the group is mutable.")
                .required(false)
                .type(Boolean.class)
                .build();

        List<Option> optionList = Arrays.asList(clusterOption, pgOption, vmOption,
                vmFormatOption, scheduleOption, scheduleUnitOption, retentionOption,
                retentionUnitOption, lockOption);

        super.parseArgs(optionList, args);
        this.clusterName = (String) parsedOptions.get("cluster");
        this.pgName = (String) parsedOptions.get("pg-name");
        this.vmNames = (String) parsedOptions.get("vm-names");
        this.vmFormats = (String) parsedOptions.get("vm-formats");
        this.schedule = Long.parseLong((String) parsedOptions.get("schedule"));
        this.scheduleUnit = (String) parsedOptions.get("schedule-unit");
        this.retention = Long.parseLong((String) parsedOptions.get("retention"));
        this.retentionUnit = (String) parsedOptions.get("retention-unit");
        this.lock = parsedOptions.get("lock") != null;
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
        this.clusterId = ClusterHelper.getCluster(
                this.vcAuthHelper.getStubFactory(), vcStubConfig,
                this.clusterName);

        // Get vm identifier
        List<String> vmNameList = new ArrayList<>();
        List<String> vmIdList = new ArrayList<>();

        if (null != this.vmNames && !this.vmNames.isEmpty()) {
            vmNameList = Arrays.asList(this.vmNames.split(","));
        }

        for (String vmName : vmNameList) {
            vmIdList.add(VmHelper.getVM(
                    this.vcAuthHelper.getStubFactory(), vcStubConfig, vmName));
        }

        // Get vm formats
        List<String> vmFormatList = new ArrayList<>();
        if (null != this.vmFormats && !this.vmFormats.isEmpty()) {
            vmFormatList = Arrays.asList(this.vmFormats.split(","));
        }

        // Build protection group target entities
        TargetEntities.Builder targetBldr = new TargetEntities.Builder();
        targetBldr.setVms(new HashSet<>(vmIdList));
        targetBldr.setVmNamePatterns(vmFormatList);

        // Build protection group snapshot policy
        SnapshotSchedule.Builder scheduleBldr = new SnapshotSchedule.Builder(
                TimeUnit.valueOf(scheduleUnit), schedule);
        RetentionPeriod.Builder retentionBldr = new RetentionPeriod.Builder(
                TimeUnit.valueOf(retentionUnit), retention);
        SnapshotPolicy.Builder policyBldr = new SnapshotPolicy.Builder("policy1",
                scheduleBldr.build(), retentionBldr.build());

        // Build protection group Spec
        ProtectionGroupSpec.Builder specBldr = new ProtectionGroupSpec.Builder(pgName,
                targetBldr.build());
        specBldr.setSnapshotPolicies(Arrays.asList(policyBldr.build()));
        specBldr.setLocked(lock);

        System.out.println("\n###Creating protection group with spec:\n");
        System.out.println(specBldr.build());
        String taskId = pgService.create_Task(clusterId, specBldr.build());

        // Wait for task to complete.
        while (true) {
            Info info = this.taskService.get(taskId);

            if (info.getStatus() == Status.SUCCEEDED) {
                System.out.println("\n###Creation task " + taskId + " succeeds.");
                break;
            } else if (info.getStatus() == Status.FAILED) {
                System.out.println("\n###Creation task " + taskId + " fails.\nError:\n");
                System.out.println(info.getResult().toString());
                break;
            } else {
                System.out.println("\n###Creation task " + taskId + "progress: " + info.getProgress());
            }

            Thread.sleep(5000);
        }

        ProtectionGroupsTypes.ListResult pgListResult = this.pgService.list(clusterId, null);
        for (ProtectionGroupsTypes.ListItem pgListItem : pgListResult.getItems()) {
            if (pgName.equals(pgListItem.getInfo().getName())) {
                this.pgId = pgListItem.getPg();
                break;
            }
        }
    }

    protected void cleanup() throws Exception {
        if (!this.pgId.isEmpty() && !lock) {
            System.out.println("\n\n#### Deleting the created protection group");
            ProtectionGroupsTypes.DeleteSpec.Builder bldr =
                    new ProtectionGroupsTypes.DeleteSpec.Builder().setForce(Boolean.TRUE);
            this.pgService.delete_Task(this.clusterId, this.pgId, bldr.build());
        } else if (!this.pgId.isEmpty() && lock) {
            System.out.println("\n\n#### Created protection group is mutable, can't be cleaned.");
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
        new CreateProtectionGroup().execute(args);
    }
}