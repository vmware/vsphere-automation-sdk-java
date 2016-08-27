# VMware vSphere Automation SDK for Java
## Table of Contents
* [Abstract](#abstract)
* [Table of Contents](https://github.com/vmware/vsphere-automation-sdk-java-samples#table-of-contents)
* [Getting Started](https://github.com/vmware/vsphere-automation-sdk-java-samples#getting-started)
  * [Downloading the Repository for Local Access](https://github.com/vmware/vsphere-automation-sdk-java-samples#downloading-the-repository-for-local-access)
  * [Prerequisites](https://github.com/vmware/vsphere-automation-sdk-java-samples#prerequisites)
  * [Building the Samples](https://github.com/vmware/vsphere-automation-sdk-java-samples#building-the-samples)
  * [Running the Samples](https://github.com/vmware/vsphere-automation-sdk-java-samples#running-the-samples)
* [Meta Information](https://github.com/vmware/vsphere-automation-sdk-java-samples#meta-information)
  * [Required Information](https://github.com/vmware/vsphere-automation-sdk-java-samples#required-information)
  * [Suggested Information](https://github.com/vmware/vsphere-automation-sdk-java-samples#suggested-information)
* [Resource Maintenance](https://github.com/vmware/vsphere-automation-sdk-java-samples#resource-maintenance)
  * [Maintenance Ownership](https://github.com/vmware/vsphere-automation-sdk-java-samples#maintenance-ownership)
  * [Filing issues](https://github.com/vmware/vsphere-automation-sdk-java-samples#filing-isssues)
  * [Resolving issues](https://github.com/vmware/vsphere-automation-sdk-java-samples#resolving-issues)
* [Additional Resources](https://github.com/vmware/vsphere-automation-sdk-java-samples#additional-resources)
  * [Discussions](https://github.com/vmware/vsphere-automation-sdk-java-samples#discussions)
  * [VMware Sample Exchange](https://github.com/vmware/vsphere-automation-sdk-java-samples#vmware-sample-exchange)
* [LICENSE AGREEMENT](https://github.com/vmware/vsphere-automation-sdk-java-samples#license-agreement)

## Abstract
This document describes how to build and run the samples in this SDK samples repository as well as how to contribute new samples.

## Getting Started
### Downloading the Repository for Local Access
1. Load the GitHub repository page: <https://github.com/vmware/vsphere-automation-sdk-java-samples>
2. Click on the green “Clone or Download” button and then click “Download ZIP”  
3. Once downloaded, extract the zip file to the location of your choosing  
4. At this point, you now have a local copy of the repository

### Prerequisites
#### Required:
The below items need to be installed:
* Maven 3
* JDK 8
* vCenter Server 6.5

### Building the Samples
In the root directory run the below maven commands -

`mvn initialize`

`mvn clean install`

### Running the Samples
Samples can either be run via command line parameters or through a configuration file. If a configuration file is used, the same command line parameters have to be specified as key-value pairs in the configuration file. The configuration file has to be specified as an argument (using --config-file option) while running the sample.

To print usage information about how to run the sample, run the below command

`java -cp target/vsphere-automation-sdk-java-6.5.0-jar-with-dependencies.jar <fully_qualified_sample_name>`

For example:
```` bash
$java -cp target/vsphere-automation-java-samples-6.5.0-jar-with-dependencies.jar vmware.samples.tagging.workflow.TaggingWorkflow

usage: ./<SampleName>.sh --config-file <ABSOLUTE PATH TO THE CONFIGURATION FILE>
OR
usage: ./<SampleName>.sh --server <SERVER> --username <USERNAME> --password <PASSWORD> --cleardata <true | false> --cluster <CLUSTER>

Options to be specified on command line or configuration file:
    --server <SERVER>            hostname of management node
    --username <USERNAME>        username to login to the management node
    --password <PASSWORD>        password to login to the management node
    --cleardata <true | false>   Set to true to clear up all the sample data after the run.
    --cluster <CLUSTER>          The name of the cluster to be tagged
````

### API Documentation and Programming Guide
The API documentation for the samples can be found here : https://developercenter.vmware.com/web/dp/doc/preview?id=1505

The programming guide for vSphere Automation SDK can be found here: http://pubs.vmware.com/vsphere-60/topic/com.vmware.ICbase/PDF/vcs_java_prog_guide_6_0.pdf

## Meta Information
The following information must be included in the README.md for each submitted sample.
* Author Name
  * This can include full name, email address or other identifiable piece of information that would allow interested parties to contact author with questions.
* Date
  * Date the resource was written
* Minimal/High Level Description
  * What does the sample do
* vSphere version against which the sample was developed/tested
* SDK version against which the sample was developed/tested
* Java version against which the sample was developed/tested
* OS platform version against which the sample was tested/developed
* Any KNOWN limitations or dependencies
  * vSphere version, required modules, etc.  

## Resource Maintenance
### Maintenance Ownership
Ownership of any and all submitted resources are maintained by the submitter.
### Filing Issues
Any bugs or other issues should be filed within GitHub by way of the repository’s Issue Tracker.
### Resolving Issues
Any community member can resolve issues within the repository, however only the owner or a board member can approve the update. Once approved, assuming the resolution involves a pull request, only a board member will be able to merge and close the request.

### VMware Sample Exchange
It is highly recommended to add any and all submitted resources to the VMware Sample Exchange: <https://developercenter.vmware.com/samples>

Sample Exchange can be allowed to access your GitHub resources, by way of a linking process, where they can be indexed and searched by the community. There are VMware social media accounts which will advertise resources posted to the site and there's no additional accounts needed, as the VMware Sample Exchange uses MyVMware credentials.     

## LICENSE AGREEMENT
License Agreement: <https://<path to license file>

# Repository Administrator Resources
## Table of Contents
* Board Members
* Approval of Additions

## Board Members

Board members are volunteers from the SDK community and VMware staff members, board members are not held responsible for any issues which may occur from running of scripts inside this repository.

Members:

## Approval of Additions
Items added to the repository, including items from the Board members, require 2 votes from the board members before being added to the repository. The approving members will have ideally downloaded and tested the item. When two “Approved for Merge” comments are added from board members, the pull can then be committed to the repository.
