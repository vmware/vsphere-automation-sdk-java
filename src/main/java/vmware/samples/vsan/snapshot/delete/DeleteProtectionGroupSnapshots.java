/*
 * *******************************************************
 * Copyright (c) 2024 Broadcom. All Rights Reserved.
 * The term "Broadcom" refers to Broadcom Inc.
 * and/or its subsidiaries.
 * SPDX-License-Identifier: MIT
 * *******************************************************
 */
package vmware.samples.vsan.snapshot.delete;

import com.vmware.snapservice.clusters.protection_groups.SnapshotsTypes;
import com.vmware.snapservice.clusters.ProtectionGroups;
import com.vmware.snapservice.clusters.ProtectionGroupsTypes;
import org.apache.commons.cli.Option;
import com.vmware.snapservice.clusters.protection_groups.Snapshots;
import vmware.samples.common.SnapserviceSamplesAbstractBase;
import vmware.samples.vcenter.helpers.ClusterHelper;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Description: Demonstrates deleting protection group snapshots.
 * <p>
 * Author: Broadcom, Inc.
 * Sample Prerequisites: vCenter 8.0.3+
 */
public class DeleteProtectionGroupSnapshots extends SnapserviceSamplesAbstractBase {
    private ProtectionGroups pgService;
    private Snapshots snapshotService;
    private String clusterName;
    private String pgName;
    private int snapshotRemain;

    /**
     * Define the options specific to this sample and configure the sample using
     * command-line arguments or a config file
     *
     * @param args command line arguments passed to the sample
     */
    protected void parseArgs(String[] args) {
        Option clusterOption = Option.builder()
                .longOpt("cluster")
                .desc("Specify identifier of the cluster to delete the"
                        + " protection group snapshots.")
                .argName("CLUSTER")
                .required(true)
                .hasArg()
                .build();

        Option pgOption = Option.builder()
                .longOpt("pg-name")
                .desc("Specify the name of the protection groups.")
                .argName("PG NAME")
                .required(true)
                .hasArg()
                .build();

        Option remainOption = Option.builder()
                .longOpt("remain")
                .desc("OPTIONAL: How many PG snapshots to leave.")
                .argName("REMAIN")
                .required(false)
                .hasArg()
                .build();

        List<Option> optionList = Arrays.asList(clusterOption, pgOption, remainOption);

        super.parseArgs(optionList, args);
        this.clusterName = (String) parsedOptions.get("cluster");
        this.pgName = (String) parsedOptions.get("pg-name");
        Object remainObject = parsedOptions.get("remain");
        if (remainObject != null) {
            this.snapshotRemain = Integer.parseInt((String) remainObject);
        }
    }

    protected void setup() throws Exception {
        this.pgService =
                ssAuthHelper.getStubFactory()
                        .createStub(ProtectionGroups.class, ssStubConfig);

        this.snapshotService =
                ssAuthHelper.getStubFactory()
                        .createStub(Snapshots.class, ssStubConfig);
    }

    protected void run() throws Exception {
        // Get cluster identifier
        String clusterId = ClusterHelper.getCluster(
                this.vcAuthHelper.getStubFactory(), vcStubConfig,
                this.clusterName);

        // Get protection groups info
        ProtectionGroupsTypes.ListResult pgListResult = this.pgService.list(clusterId, null);

        // Get protection group identifier
        String pgId = "";
        for (ProtectionGroupsTypes.ListItem pgListItem : pgListResult.getItems()) {
            if (pgName.equals(pgListItem.getInfo().getName())) {
                pgId = pgListItem.getPg();
                break;
            }
        }
        if (pgId.isEmpty()) {
            System.out.println("\nProtection groups: \"" + pgName + "\" doesn't exist.");
        }

        // List snapshots of this protection group
        List<SnapshotsTypes.ListItem> snapshots = snapshotService.list(clusterId, pgId, null).getSnapshots();
        System.out.println("\nProtection group \"" + pgName + "\" snapshots number: " + snapshots.size());
        Map<Calendar, String> snapshotExpireMap = new TreeMap<>();
        for (SnapshotsTypes.ListItem snapshot : snapshots) {
            snapshotExpireMap.put(snapshot.getInfo().getExpiresAt(), snapshot.getSnapshot());
        }
        System.out.println(snapshotExpireMap);

        int snapshotToDelete = snapshotExpireMap.size();
        if (this.snapshotRemain >= snapshotExpireMap.size()) {
            System.out.println("\nNo need to delete snapshots.");
            return;
        } else if (this.snapshotRemain > 0) {
            snapshotToDelete = snapshotExpireMap.size() - this.snapshotRemain;
        }
        System.out.println("\nDeleting \"" + snapshotToDelete + "\" snapshots.");

        int snapshotDeleted = 0;
        for (String snapshot : snapshotExpireMap.values()) {
            System.out.println("\n###Deleting protection group snapshot: \"" + snapshot + "\".");
            snapshotService.delete(clusterId, pgId, snapshot);
            snapshotDeleted++;

            if (snapshotDeleted >= snapshotToDelete) {
                break;
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
        new DeleteProtectionGroupSnapshots().execute(args);
    }

}