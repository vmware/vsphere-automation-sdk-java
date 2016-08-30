# Tagging Workflow
This sample demonstrates tagging Create, Read, Update, Delete operations.
* Step 1: Create a Tag category called “Asset”.
* Step 2: Create a Tag called “Server” under the category “Asset”.
* Step 3: Retrieve an existing Cluster using VIM APIs.
* Step 4: Translates the Cluster's MoRef into vAPI UUID.
* Step 5: Assign “Server” tag to the Cluster using the UUID.
*
* Additional steps when clearData flag is set to TRUE:
* Step 6: Detach the tag from the Cluster.
* Step 7: Delete the tag  “Server”.
* Step 8: Delete the tag category “Asset”.

## Prerequisites
The sample needs an existing Cluster

## Meta Information
* Author: VMware Inc.
* Date created: 8/30/2016
* vSphere version tested: 6.0, 6.5
* SDK version tested: 6.0, 6.5
* Java version tested: JDK 8

## Build and run the sample
Refer to the sections [`Building the sample`](https://github.com/vmware/vsphere-automation-sdk-java-samples#building-the-samples) and  [`Running the sample`](https://github.com/vmware/vsphere-automation-sdk-java-samples#running-the-samples) in the root README.md
