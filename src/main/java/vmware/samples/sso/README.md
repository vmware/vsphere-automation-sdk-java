This directory contains samples for SSO and Lookup Service APIs:

The vSphere Automation SDK for Java samples use the vCenter Lookup Service
to obtain the URLs for other vSphere Automation services (SSO, vAPI, VIM, SPBM, etc.).
The SDK contains the Lookup Service WSDL files. The Lookup Service WSDL files are located in lib/ directory.

Sample                                                     | Description
-----------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------
vmware.samples.sso.embeddedpsc.EmbeddedPscSsoWorkflow.java | Demonstrates how to create a SSO connection using a SAML Bearer token when we have a vcenter server and embedded Platform Services Controller
vmware.samples.sso.externalpsc.ExternalPscSsoWorkflow.java | Demonstrates how to create a SSO connection using a SAML Bearer token when we have a vcenter server and external Platform Services Controller

### Testbed Requirement:
    - 1 vCenter Server
    - 1 Platform Services Controller

### Deprecation Notice
Starting vCenter server release 7.0, External Platform Services Controller (PSC) is no longer supported. All PSC services are consolidated into vCenter Server.
https://docs.vmware.com/en/VMware-vSphere/7.0/com.vmware.vsphere.vcenter.configuration.doc/GUID-135F2607-DA51-47A5-BB7A-56AD141113D4.html
In view of the above, related samples (vmware.samples.sso.externalpsc.ExternalPscSsoWorkflow.java) are deprecated and will be removed in next major SDK release.

Consequently, we are deprecating lookupservice client libraries and related files. These will be removed in next major SDK release. Use well known URL path (https://docs.vmware.com/en/VMware-vSphere/8.0/vsphere-apis-sdks-introduction/GUID-B625C8FE-5E15-4918-98C0-69313E5880FB.html) instead of lookupservice.

For SSO, service endpoint is: "https://{domain}/STS/STSService"
https://docs.vmware.com/en/VMware-vSphere/8.0/vsphere-apis-sdks-introduction/GUID-5384662C-CD05-4CAE-894E-972F14A7ECB7.html