This directory contains samples for snapservice APIs:
1. Protection Group Create, List and Delete operations:
   Sample                               | Description
   -------------------------------------|---------------------------------------------------
   ListProtectionGroups.java            | Demonstrates listing protection groups
   CreateProtectionGroup .java          | Demonstrates creating protection group
   DeleteProtectionGroups.java          | Demonstrates deleting protection groups

2. Protection Group Snapshot Delete operations:
   Sample                               | Description
   -------------------------------------|---------------------------------------------------
   DeleteProtectionGroupSnapshots.java  | Demonstrates deleting protection group snapshots

### Common property file:
    server=<vc ip>
    snapservice=<snapservice ip>
    username=<username>
    password=<password>

### Running the samples:
	java -cp target/vsphere-samples-8.0.3.0.jar vmware.samples.vsan.protection_group.list.ListProtectionGroups --config-file <property-file> --skip-server-verification --cluster <ClusterName>
	
	java -cp target/vsphere-samples-8.0.3.0.jar vmware.samples.vsan.protection_group.create.CreateProtectionGroup --config-file <property-file> --skip-server-verification --cluster <Cluster Name> --pg-name <PG Name> --vm-formats <format1,format2> --schedule 30 --schedule-unit MINUTE --retention 6 --retention-unit HOUR
	java -cp target/vsphere-samples-8.0.3.0.jar vmware.samples.vsan.protection_group.create.CreateProtectionGroup --config-file <property-file> --skip-server-verification --cluster <Cluster Name> --pg-name <PG Name> --vm-names <vmName1,vmName2> --vm-formats <format1> --schedule 30 --schedule-unit MINUTE --retention 6 --retention-unit HOUR --cleardata
	java -cp target/vsphere-samples-8.0.3.0.jar vmware.samples.vsan.protection_group.create.CreateProtectionGroup --config-file <property-file> --skip-server-verification --cluster <Cluster Name> --pg-name <PG Name> --vm-names <vmName1> --schedule 30 --schedule-unit MINUTE --retention 6 --retention-unit HOUR --lock
	
	java -cp target/vsphere-samples-8.0.3.0.jar vmware.samples.vsan.protection_group.delete.DeleteProtectionGroups --config-file <property-file> --skip-server-verification --cluster <ClusterName> --pg-names <PGName1,PGName2,PGName3>
	java -cp target/vsphere-samples-8.0.3.0.jar vmware.samples.vsan.protection_group.delete.DeleteProtectionGroups --config-file <property-file> --skip-server-verification --cluster <ClusterName> --pg-names <PGName1,PGName2,PGName3>  --force
	
	java -cp target/vsphere-samples-8.0.3.0.jar vmware.samples.vsan.snapshot.delete.DeleteProtectionGroupSnapshots --config-file <property-file> --skip-server-verification --cluster <ClusterName> --pg-name <PGName> --remain <snapshots remain number>

### Testbed Requirement:
    - vCenter Server >= 8.0.3+
    - vSAN ESA disk
    - vSAN Cluster
    - Snapservice = 8.0.3