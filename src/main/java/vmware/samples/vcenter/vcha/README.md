Author: Varun R Sekar (vrsekar@vmware.com)

Date: September 19, 2018

VCHA Feature Document: https://docs.vmware.com/en/VMware-vSphere/6.7/vsphere-esxi-vcenter-server-671-availability-guide.pdf

This directory contains samples for managing vCenter HA Clusters:

The samples were tested against vSphere 6.7 update 1

### vCenter HA Cluster List Operations
Sample                                                                | Description
----------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------
vmware.samples.vcenter.vcha.VchaClient.java                           | Demonstrates listing active node information, vCenter HA cluster information and vCenter HA cluster mode

### vCenter HA Cluster Deploy/Undeploy Operations
Sample                                                                | Description
----------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------
vmware.samples.vcenter.vcha.VchaClusterOps.java                       | Demonstrates vCenter HA Cluster Deploy, Undeploy Operations for a given vCenter server with automatic cluster configuration and IPv4 network configuration. The sample requires IPv4 network configuration for cluster networking

### Testbed Requirement:
    - 3 ESXi hosts on version 6.0x or later is recommended
    - 1 Management vCenter Server on version 6.0x or later (Optional)
    - 1 vCenter Server Appliance on version 6.7 update 1 or later
    - Seperate network for vCenter HA than the management network (network latency between the vCenter HA cluster nodes must be less than 10ms)
    - vCenter HA requires a single vCenter Server license and a Standard license
