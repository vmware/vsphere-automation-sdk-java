# Appliance Management API Samples

This directory contains samples for Appliance Management APIs:

### Appliance Health APIs
Sample                                                                      | Description
----------------------------------------------------------------------------|-------------------------------------------------------------------------
vmware.samples.appliance.health.HealthMessages.java                         | Get the health messages for various appliance health items

### Appliance Networking APIs
Sample                                                                      | Description
----------------------------------------------------------------------------|-------------------------------------------------------------------------
vmware.samples.appliance.networking.NetworkingWorkflow.java                 | Enable/Disable IPv6, Reset and Get the network information
vmware.samples.appliance.networking.dns.DnsDomainWorkflow.java              | Add/Set/List the DNS domains for the appliance
vmware.samples.appliance.networking.dns.DnsServersWorkflow.java             | Add/Set/List the DNS servers for the appliance
vmware.samples.appliance.networking.dns.HostNameWorkflow.java               | Get/Set the hostname for the appliance
vmware.samples.appliance.networking.interfaces.InterfacesWorkflow.java      | List/Get the interfaces information for the appliance
vmware.samples.appliance.networking.interfaces.IPv4Workflow.java            | Set/Get the IPv4 configuration of a specific interface in the appliance
vmware.samples.appliance.networking.interfaces.IPv6Workflow.java            | Set/Get the IPv6 configuration of a specific interface in the appliance
vmware.samples.appliance.networking.proxy.ProxyWorkflow.java                | List/Set/Get the proxy information for the appliance
vmware.samples.appliance.networking.proxy.NoProxyWorkflow.java              | Get/Set the servers with No proxy configuration in the appliance

### Testbed Requirement:
    - 1 vCenter Server