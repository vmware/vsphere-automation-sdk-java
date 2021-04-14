Author: Masha Orfali (masha.orfali@gmail.com)

Date: April 14, 2021

This directory contains a sample for deploying a virtual machine from a content library item containing a virtual machine template.

The sample was tested against vSphere 7.0.1.

The sample was tested against 7.0.2.0 SDK version.

The sample was tested against Java version 1.8.

### Testbed Requirement:

 - A content library item containing a virtual machine template (not ovf)
 - A datacenter
 - A VM folder
 - A resource pool
 - A datastore

Usage example (command line):

```
java -cp target/vsphere-samples-7.0.2.0.jar vmware.samples.contentlibrary.vmtemplatedeploy.DeployVMTemplate 
--server vcenter.local.server 
--username admin@vsphere.local 
--password myPassword 
--datacenter MyDatacenter 
--vmfolder MyTestFolder 
--vmname MyTestVMName 
--datastore MyDatastore 
--resourceool MyResourcepoolName 
--libitemname my-template-name 
--skip-server-verification
```

Expected output:

```bash
Template id found: b0c32c8d-727d-40d4-9a2b-acd875510376

Folder id found: group-v4255

Resource Pool Id found: resgroup-4021

Datastore Id found: datastore-25

Deploying a virtual machine from VM template item...

Vm MyTestVMName created with id: vm-4242

```