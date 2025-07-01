# VMware vSphere Automation SDK for Java
## Table of Contents
- [Abstract](#abstract)
- [Supported vCenter Releases](#supported-onprem-vcenter-releases)
- [Table of Contents](#table-of-contents)
- [Quick Start Guide](#quick-start-guide)
  - [Setting up maven](#setting-up-maven)
  - [Setting up a vSphere Test Environment](#setting-up-a-vsphere-test-environment)
  - [Building the Samples](#building-the-samples)
  - [Running the Samples](#running-the-samples)
  - [Connect to VMware Cloud on AWS](#connect-to-vmware-cloud-on-aws)
  - [Importing the samples to eclipse](#importing-the-samples-to-eclipse)
- [API Documentation](#api-documentation)
- [Support](#support)
- [Submitting samples](#submitting-samples)
  - [Required Information](#required-information)
  - [Suggested Information](#suggested-information)
  - [Contribution Process](#contribution-process)
  - [Code Style](#code-style)
- [Resource Maintenance](#resource-maintenance)
  - [Maintenance Ownership](#maintenance-ownership)
  - [Filing Issues](#filing-issues)
  - [Resolving Issues](#resolving-issues)
  - [VMware Sample Exchange](#vmware-sample-exchange)
- [Repository Administrator Resources](#repository-administrator-resources)
  - [Board Members](#board-members)
  - [Approval of Additions](#approval-of-additions)
- [Announcements](#announcements)
  - [Compatibility](#compatibility)
  - [Dependency updates](#dependency-updates)
- [VMware Resources](#vmware-resources)

## Abstract
This document describes the vSphere Automation Java SDK samples that use the vSphere Automation
java client library. Additionally, some of the samples demonstrate the combined use of the
vSphere Automation and vSphere Web Service APIs. The samples have been developed to work with
JDK 1.8+

## Supported OnPrem vCenter Releases:

vCenter 7.0, 7.0U1, 7.0U2, 7.0U3, 8.0, 8.0U1, 8.0U2 and 8.0U3

Please refer to the notes in each sample for detailed compatibility information. 

## Supported NSX-T Releases
NSX-T 2.2, 2.3, 3.0, 4.0.0.0, 4.0.1

## Latest VMware Cloud on AWS Release:
VMC M24 (1.24) ([Release Notes](https://docs.vmware.com/en/VMware-Cloud-on-AWS/0/rn/vmc-on-aws-relnotes.html))

## Quick Start Guide
This document will walk you through getting up and running with the Java SDK Samples. Prior to running the samples you will need to setup a vCenter test environment and install maven, the following steps will take you through this process.
Before you can run the SDK samples we'll need to walk you through the following steps:

1. Setting up maven
2. Setting up a vSphere test environment

### Setting up maven
The SDK requires maven to build the samples. 
1. Download the latest maven from <https://maven.apache.org/download.cgi> and extract it to your machine.
2. Install JDK 8 and set JAVA_HOME to the directory where JDK is installed.
   ```` bash
   export JAVA_HOME=<jdk-install-dir>
   ````
3. Update PATH environment variable to include the maven and jdk "bin" directories.
   ```` bash
   export PATH=<maven-bin-dir>:$JAVA_HOME/bin:$PATH
   ````

### Setting up a vSphere Test Environment
**NOTE:** The samples are intended to be run against a freshly installed **non-Production** vSphere setup as the scripts may make changes to the test environment and in some cases can destroy items when needed.

To run the samples a vSphere test environment is required with the following minimum configuration
* 1 vCenter Server
* 2 ESX hosts
* 1 NFS Datastore with at least 3GB of free capacity

Apart from the above, each individual sample may require additional setup. Please refer to the sample parameters for more information on that.

### Building the Samples
In the root directory of your folder after cloning the repository, run the below maven commands -
```` bash
mvn initialize

mvn clean install
````

### Running the Samples
When running the samples, parameters can be provided either on the command line, in a configuration file (using the --config-file parameter), or a combination of both. The parameter values specified on the command line will override those specified in the configuration file. When using a configuration file, each required parameter for the sample must be specified either in the configuration file or as a command line parameter. Each parameter specified in the configuration file should be in the "key=value" format. For example:

`vmname=TestVM`

`cluster=Cluster1`

**Note:** Please specify the fully qualified “hostname” of the server for running the samples to avoid hostname verification errors.

Use a command like the following to display usage information for a particular sample.
```` bash
$java -ea -cp target/vsphere-samples-8.0.3.0.jar vmware.samples.vcenter.vm.list.ListVMs

java -cp target/vsphere-samples-8.0.3.0.jar vmware.samples.vcenter.vm.list.ListVMs [--config-file <CONFIGURATION FILE>]
       --server <SERVER> --username <USERNAME> --password <PASSWORD> --cluster <CLUSTER> [--truststorepath <ABSOLUTE PATH OF JAVA TRUSTSTORE FILE>]
       [--truststorepassword <JAVA TRUSTSTORE PASSWORD>] [--cleardata] [--skip-server-verification]

Sample Options:
    --config-file <CONFIGURATION FILE>                         OPTIONAL: Absolute path to  the configuration file containing the sample options.
                                                               NOTE: Parameters can be specified either in the configuration file or on the command
                                                               line. Command line parameters will override values specified in the configuration file.
    --server <SERVER>                                          hostname of vCenter Server
    --username <USERNAME>                                      username to login to the vCenter Server
    --password <PASSWORD>                                      password to login to the vCenter Server
    --truststorepath <ABSOLUTE PATH OF JAVA TRUSTSTORE FILE>   Specify the absolute path to the file containing the trusted server certificates. This
                                                               option can be skipped if the parameter skip-server-verification is specified.
    --truststorepassword <JAVA TRUSTSTORE PASSWORD>            Specify the password for the java truststore. This option can be skipped if the
                                                               parameter skip-server-verification is specified.
    --cleardata                                                OPTIONAL: Specify this option to undo all persistent results of running the sample.
    --skip-server-verification                                 OPTIONAL: Specify this option if you do not want to perform SSL certificate
                                                               verification.
                                                               NOTE: Circumventing SSL trust in this manner is unsafe and should not be used with
                                                               production code. This is ONLY FOR THE PURPOSE OF DEVELOPMENT ENVIRONMENT.
````

Use a command like the following to run a sample using only command line parameters:
```` bash
$java -ea -cp target/vsphere-samples-8.0.3.0.jar vmware.samples.vcenter.vm.list.ListVMs --server servername --username administrator@vsphere.local --password password --skip-server-verification
````

Use a command like the following to run a sample using only a configuration file:
```` bash
$java -ea -cp target/vsphere-samples-8.0.3.0.jar vmware.samples.vcenter.vm.list.ListVMs --config-file sample.properties
````

Use the following command to run the sample using a combination of configuration file and command line parameters:
```` bash
$java -ea -cp target/vsphere-samples-8.0.3.0.jar vmware.samples.vcenter.vm.list.ListVMs --config-file sample.properties --server servername
````

### Connect to VMware Cloud on AWS

#### using csp api_token (also called refresh_token)
```java
import com.vmware.vmc.Orgs;
import com.vmware.vmc.model.Organization;
import com.vmware.vapi.vmc.client.VmcClients;

ApiClient apiClient = VmcClients.createDefault("<refresh_token>");
Orgs orgsStub = apiClient.createStub(Orgs.class);
Organization orgDetails = orgsStub.get("<org_id>");
System.out.println(orgDetails);
```

Output in console:

```shell
Organization (com.vmware.vmc.model.organization) => {
    updated = 8/18/22 20:41:34 +0000,
    userId = xxxx-xxxx-xxxx,
    updatedByUserId = xxxx-xxxx-xxxx,
    created = 10/3/17 22:19:31 +0000,
    version = 4,
    updatedByUserName = xxxx-xxxx-xxxx,
    userName = xxxx-xxxx-xxxx,
    id = xxxx-xxxx-xxxx,
    orgType = INTERNAL_NON_CORE,
    displayName = xxxx/xxxx/xxxx,
    name = xxxx,
    projectState = CREATED,
    properties = OrgProperties (com.vmware.vmc.model.org_properties) => {
        values = {...}
    }
    [dynamic fields]: {
        org_seller_info = <unset>
    }
}
```

#### using client_credentials (Server to Server OAuth app's id and secret)
```java
import com.vmware.vmc.Orgs;
import com.vmware.vmc.model.Organization;
import com.vmware.vapi.vmc.client.VmcClients;

ApiClient apiClient = VmcClients.createDefault("<client_id>", "<client_secret>", "<org_id>");
orgsStub = apiClient.createStub(Orgs.class);
Organization orgDetails = orgsStub.get("<org_id>");
System.out.println(orgDetails);
```

Output in console:

```shell
Organization (com.vmware.vmc.model.organization) => {
    updated = 8/18/22 20:41:34 +0000,
    userId = xxxx-xxxx-xxxx,
    updatedByUserId = xxxx-xxxx-xxxx,
    created = 10/3/17 22:19:31 +0000,
    version = 4,
    updatedByUserName = xxxx-xxxx-xxxx,
    userName = xxxx-xxxx-xxxx,
    id = xxxx-xxxx-xxxx,
    orgType = INTERNAL_NON_CORE,
    displayName = xxxx/xxxx/xxxx,
    name = xxxx,
    projectState = CREATED,
    properties = OrgProperties (com.vmware.vmc.model.org_properties) => {
        values = {...}
    }
    [dynamic fields]: {
        org_seller_info = <unset>
    }
}
```

### Importing the samples to eclipse
To generate the eclipse project files for the samples run the below command
```` bash
mvn eclipse:clean eclipse:eclipse
````

Once generated, follow below steps to import the project to eclipse:
1. Go to File -> Import.
2. Select Existing Projects into Workspace.
3. Select the root directory as the directory where the samples are located.
4. Click Finish

### Adding a new sample
Once the eclipse project is imported, follow below steps to add a new sample using the sample template:
1. Right click on the project and select New -> Package. Specify a package name in the wizard and click Finish.
2. Right click on the newly created package and click Import -> General -> File System. In the dialog box, click "Browse" and select the "sample-template" folder in the root directory.
3. Select the "SampleClass" in the dialog box and click finish.

This will import a basic sample class to your package, which you can then customize according to your needs.

## API Documentation

### vSphere API Documentation
* [VMware vSphere REST API Reference documentation](https://developer.broadcom.com/xapis/vsphere-automation-api/latest/).

* [VMware vSphere JAVA APIs 8.0 U3 (latest version)](https://vmware.github.io/vsphere-automation-sdk-java/vsphere/8.0.3.0/vcenter-bindings/index.html).

* Previous releases: VMware vSphere JAVA APIs [8.0 U2](https://vmware.github.io/vsphere-automation-sdk-java/vsphere/8.0.2.0/vcenter-bindings/index.html),
[8.0 U1](https://vmware.github.io/vsphere-automation-sdk-java/vsphere/8.0.1.0/vcenter-bindings/index.html),
[8.0 GA](https://vmware.github.io/vsphere-automation-sdk-java/vsphere/8.0.0.1/vsphereautomation-client-sdk/index.html),
[8.0.0.0](https://vmware.github.io/vsphere-automation-sdk-java/vsphere/8.0.0.0/vsphereautomation-client-sdk/index.html),
[7.0 U3](https://vmware.github.io/vsphere-automation-sdk-java/vsphere/7.0.3.0/vsphereautomation-client-sdk/index.html),
[7.0 U2](https://vmware.github.io/vsphere-automation-sdk-java/vsphere/7.0.2.0/vsphereautomation-client-sdk/index.html),
[7.0 U1](https://vmware.github.io/vsphere-automation-sdk-java/vsphere/7.0.1.0/vsphereautomation-client-sdk/index.html),
[7.0 GA](https://vmware.github.io/vsphere-automation-sdk-java/vsphere/7.0.0.1/vsphereautomation-client-sdk/index.html)

### VMware Cloud on AWS API Documentation

* [VMware Cloud on AWS REST APIs](https://developer.broadcom.com/xapis/vmware-cloud-on-aws-api-reference/latest/).

* [VMware Cloud on AWS JAVA APIs](https://vmware.github.io/vsphere-automation-sdk-java/vmc/index.html).

* [VMware Cloud on AWS Disaster Recovery as a Service (DRaaS) JAVA APIs](https://vmware.github.io/vsphere-automation-sdk-java/vmc-draas/index.html).

### NSX API Documentation
* [VMware NSX-T Data Center REST API](https://developer.vmware.com/apis/1248)

* [VMware NSX-T Manager APIs (for on-prem customers)](https://vmware.github.io/vsphere-automation-sdk-java/nsx/nsx/index.html).

* [VMware NSX-T Policy APIs (for on-prem customers)](https://vmware.github.io/vsphere-automation-sdk-java/nsx/nsx-policy/index.html).

* [VMware NSX-T Policy APIs (for VMC customers)](https://vmware.github.io/vsphere-automation-sdk-java/nsx/nsx-vmc-policy/index.html).

* [VMware NSX-T VMC AWS Integration APIs (for VMC customers)](https://vmware.github.io/vsphere-automation-sdk-java/nsx/nsx-vmc-aws-integration/index.html).

* [VMware NSX-T VMC Common APIs (for VMC customers)](https://vmware.github.io/vsphere-automation-sdk-java/nsx/nsx-vmc-sdk-common/index.html).

## Support

Support details can be referenced under the **SDK and API Support for Commercial and Enterprise Organizations** section at [Broadcom Developer Portal](https://developer.broadcom.com/support).

For community support, please open a [Github issue](https://github.com/vmware/vsphere-automation-sdk-java/issues) or start a [Discussion](https://github.com/vmware/vsphere-automation-sdk-java/discussions).

## Submitting samples

### Developer Certificate of Origin

Before you start working with this project, please read our [Developer Certificate of Origin](https://cla.vmware.com/dco). All contributions to this repository must be signed as described on that page. Your signature certifies that you wrote the patch or have the right to pass it on as an open-source patch.

### Required Information
The following information must be included in the README.md for the sample.
* Author Name
  * This can include full name, email address or other identifiable piece of information that would allow interested parties to contact author with questions.
* Date
  * Date the sample was originally written
* Minimal/High Level Description
  * What does the sample do ?
* Any KNOWN limitations or dependencies

### Versioning

vSphere Automation Java SDK - X.Y.Z.U.P

vCenter-related: X.Y - (Major release), Z - (Update release), U - (Patch)

vSphere Automation Java SDK - related: P - (vSphere Automation Java SDK Maintenance patches)

### Suggested Information
The following information should be included when possible. Inclusion of information provides valuable information to consumers of the resource.
* vSphere version against which the sample was developed/tested
* SDK version against which the sample was developed/tested
* Java version against which the sample was developed/tested

### Contribution Process

* Follow the [GitHub process](https://help.github.com/articles/fork-a-repo)
  * Please use one branch per sample or change-set
  * Please use one commit and pull request per sample
  * Please post the sample output along with the pull request
  * If you include a license with your sample, use the project license

### Code Style

Please conform to oracle java coding standards.
    http://www.oracle.com/technetwork/articles/javase/codeconvtoc-136057.html

## Resource Maintenance
### Maintenance Ownership
Ownership of any and all submitted samples are maintained by the submitter.
### Filing Issues
Any bugs or other issues should be filed within GitHub by way of the repository’s Issue Tracker.
### Resolving Issues
Any community member can resolve issues within the repository, however only the board member can approve the update. Once approved, assuming the resolution involves a pull request, only a board member will be able to merge and close the request.

### VMware Sample Exchange
It is highly recommended to add any and all submitted samples to the VMware Sample Exchange:  <https://code.vmware.com/samples>

Sample Exchange can be allowed to access your GitHub resources, by way of a linking process, where they can be indexed and searched by the community. There are VMware social media accounts which will advertise resources posted to the site and there's no additional accounts needed, as the VMware Sample Exchange uses MyVMware credentials.     

## Repository Administrator Resources
### Board Members

Board members are volunteers from the SDK community and VMware staff members, board members are not held responsible for any issues which may occur from running of samples from this repository.

Members:
* Ankit Agrawal (VMware)
* Martin Cvetanov (VMware)
* Shweta Purohit (VMware)
* Jobin George (VMware)
* Kunal Singh (VMware)

### Approval of Additions
Items added to the repository, including items from the Board members, require 2 votes from the board members before being added to the repository. The approving members will have ideally downloaded and tested the item. When two “Approved for Merge” comments are added from board members, the pull can then be committed to the repository.

## Announcements
### Compatibility
vSphere Management SDK 8.0U1+ libraries can not be used in vSphere Automation SDK. This is due to difference in jaxws library version used to build these SDKs.
vSphere Management SDK 8.0U1+ uses jaxws 3.x while vSphere Automation SDK still uses jaxws 2.x version. Currently we are shipping vim25.jar and ssoclient.jar from Management SDK 8.0 in Automation SDK to maintain compatibility.
In next major release, 9.0, the compatibility issue will be addressed by using same version of jaxws libraries to build both the SDKs

### Dependency updates
To align the dependencies on jaxws libraries and address above mentioned compatibility issue, SDK 9.x release of vSphere Automation SDK will use jaxws 3.x version.
This change requires us to switch to jakarta imports instead of javax. This is a breaking change and users may need a similar change in their environment to use our vSphere Automation SDK 9.x

## VMware Resources
* [VMware Developers Site](https://developer.broadcom.com/)
* [vSphere Automation SDK Overview](https://code.vmware.com/web/sdk/7.0/vsphere-automation-java)
* [VMware Code](https://code.vmware.com/home)
* [VMware Developer Community](https://community.broadcom.com/home)
* VMware vSphere [Java API Reference documentation](https://vmware.github.io/vsphere-automation-sdk-java/vsphere/8.0.3.0/vcenter-bindings/index.html).
* [VMware Java forum](https://code.vmware.com/forums/7508/vsphere-automation-sdk-for-java)
