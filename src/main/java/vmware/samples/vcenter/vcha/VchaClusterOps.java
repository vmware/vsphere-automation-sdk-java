/*
 * *******************************************************
 * Copyright VMware, Inc. 2017.  All Rights Reserved.
 * SPDX-License-Identifier: MIT
 * *******************************************************
 *
 * DISCLAIMER. THIS PROGRAM IS PROVIDED TO YOU "AS IS" WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, WHETHER ORAL OR WRITTEN,
 * EXPRESS OR IMPLIED. THE AUTHOR SPECIFICALLY DISCLAIMS ANY IMPLIED
 * WARRANTIES OR CONDITIONS OF MERCHANTABILITY, SATISFACTORY QUALITY,
 * NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE.
 */
package vmware.samples.vcenter.vcha;

import com.vmware.vcenter.vcha.*;
import org.apache.commons.cli.Option;
import vmware.samples.common.SamplesAbstractBase;
import vmware.samples.vcenter.vcha.helpers.SpecHelper;
import vmware.samples.vcenter.vcha.helpers.TaskHelper;

import java.util.Arrays;
import java.util.List;

import static vmware.samples.vcenter.vcha.helpers.ArgumentsHelper.*;

/**
 * Description: Demonstrates vCenter HA Cluster Deploy, Undeploy operations for a given vCenter server
 * with automatic cluster configuration and IPv4 network configuration
 * Step 1: Deploy a vCenter HA cluster from the given configuration
 * Step 2: List the Cluster Info
 * Step 3: Undeploy the vCenter HA cluster
 *
 * Author: VMware, Inc.
 * Sample Prerequisites: The sample needs a vCenter server configured to be the active
 * node in the vCenter HA cluster and IPv4 network configuration for cluster networking
 *
 */
public class VchaClusterOps extends SamplesAbstractBase {
    private String vcSpecActiveLocationHostname;
    private String vcSpecActiveLocationUsername;
    private String vcSpecActiveLocationPassword;
    private String vcSpecActiveLocationSSLThumbprint;
    private String activeHaIpDefaultGateway;
    private List<String> activeHaIpDnsServers;
    private String activeHaIpIpv4Address;
    private String activeHaIpIpv4SubnetMask;
    private Long activeHaIpIpv4Prefix;
    private String activeHaNetwork;
    private NetworkType activeHaNetworkType;
    private String passiveFailoverIpDefaultGateway;
    private List<String> passiveFailoverIpDnsServers;
    private String passiveFailoverIpIpv4Address;
    private String passiveFailoverIpIpv4SubnetMask;
    private Long passiveFailoverIpIpv4Prefix;
    private String passiveHaIpDefaultGateway;
    private List<String> passiveHaIpDnsServers;
    private String passiveHaIpIpv4Address;
    private String passiveHaIpIpv4SubnetMask;
    private Long passiveHaIpIpv4Prefix;
    private String passivePlacementName;
    private String passivePlacementFolder;
    private String passivePlacementHaNetwork;
    private NetworkType passivePlacementHaNetworkType;
    private String passivePlacementHost;
    private String passivePlacementResourcePool;
    private String passivePlacementStorageDatastore;
    private String passivePlacementManagementNetwork;
    private NetworkType passivePlacementManagementNetworkType;
    private String witnessHaIpDefaultGateway;
    private List<String> witnessHaIpDnsServers;
    private String witnessHaIpIpv4Address;
    private String witnessHaIpIpv4SubnetMask;
    private Long witnessHaIpIpv4Prefix;
    private String witnessPlacementName;
    private String witnessPlacementFolder;
    private String witnessPlacementHaNetwork;
    private NetworkType witnessPlacementHaNetworkType;
    private String witnessPlacementHost;
    private String witnessPlacementResourcePool;
    private String witnessPlacementStorageDatastore;
    private String witnessPlacementManagementNetwork;
    private NetworkType witnessPlacementManagementNetworkType;

    private CredentialsSpec mgmtVcCredentialsSpec;
    private Cluster clusterService;

    /**
     * Define the options specific to this sample and configure the sample using
     * command-line arguments or a config file
     *
     * @param args command line arguments passed to the sample
     */
    protected void parseArgs(String[] args) {
        // Management vCenter Connection Spec Options
        Option vcSpecActiveLocationHostnameOption = Option.builder()
                .longOpt(VC_SPEC_ACTIVE_LOCATION_HOSTNAME)
                .desc("OPTIONAL: hostname of the Management vCenter Server. " +
                        "Leave blank if it's a self-managed VC")
                .argName("MGMT VC HOST")
                .required(false)
                .hasArg()
                .build();
        Option vcSpecActiveLocationUsernameOption = Option.builder()
                .longOpt(VC_SPEC_ACTIVE_LOCATION_USERNAME)
                .desc("OPTIONAL: username to login to the Management vCenter Server. " +
                        "Leave blank if it's a self-managed VC")
                .argName("MGMT VC USERNAME")
                .required(false)
                .hasArg()
                .build();
        Option vcSpecActiveLocationPasswordOption = Option.builder()
                .longOpt(VC_SPEC_ACTIVE_LOCATION_PASSWORD)
                .desc("OPTIONAL: password to login to the Management vCenter Server. " +
                        "Leave blank if it's a self-managed VC")
                .argName("MGMT VC PASSWORD")
                .required(false)
                .hasArg()
                .build();
        Option vcSpecActiveLocationSSLThumbprintOption = Option.builder()
                .longOpt(VC_SPEC_ACTIVE_LOCATION_SSL_THUMBPRINT)
                .desc("SSL Thumbprint of Management vCenter Server. " +
                        "Leave blank if it's a self-managed VC")
                .argName("MGMT VC SSL THUMBPRINT")
                .required(true)
                .hasArg()
                .build();
        // Active HA Network Spec Options
        Option activeHaIpDefaultGatewayOption = Option.builder()
                .longOpt(ACTIVE_HA_IP_DEFAULT_GATEWAY)
                .desc("OPTIONAL: IP address of the gateway for this interface")
                .argName("ACTIVE HA IP DEFAULT GATEWAY")
                .required(false)
                .hasArg()
                .build();
        Option activeHaIpDnsServersOption = Option.builder()
                .longOpt(ACTIVE_HA_IP_DNS_SERVERS)
                .desc("List of IP addresses of the DNS servers to configure the interface")
                .argName("ACTIVE HA IP DNS SERVERS")
                .required(true)
                .hasArg()
                .build();
        Option activeHaIpIpv4AddressOption = Option.builder()
                .longOpt(ACTIVE_HA_IP_IPV4_ADDRESS)
                .desc("IP address to be used to configure the interface")
                .argName("ACTIVE HA IP IPV4 ADDRESS")
                .required(true)
                .hasArg()
                .build();
        Option activeHaIpIpv4SubnetMaskOption = Option.builder()
                .longOpt(ACTIVE_HA_IP_IPV4_SUBNET_MASK)
                .desc("Subnet mask of the interface")
                .argName("ACTIVE HA IP IPV4 SUBNET MASK")
                .required(true)
                .hasArg()
                .build();
        Option activeHaIpIpv4PrefixOption = Option.builder()
                .longOpt(ACTIVE_HA_IP_IPV4_PREFIX)
                .desc("OPTIONAL: CIDR Prefix for the interface")
                .argName("ACTIVE HA IP IPV4 PREFIX")
                .required(false)
                .hasArg()
                .build();
        Option activeHaNetworkOption = Option.builder()
                .longOpt(ACTIVE_HA_NETWORK)
                .desc("OPTIONAL: The identifier of the network object to be used for the HA network. " +
                        "Leave blank if Active node is already configured with HA network")
                .argName("ACTIVE HA NETWORK")
                .required(false)
                .hasArg()
                .build();
        Option activeHaNetworkTypeOption = Option.builder()
                .longOpt(ACTIVE_HA_NETWORK_TYPE)
                .desc("OPTIONAL: The type of the network object to be used by the HA network. " +
                        "Leave blank if Active node is already configured with HA network")
                .argName("ACTIVE HA NETWORK TYPE")
                .required(false)
                .hasArg()
                .build();
        // Passive Failover Network Spec Options
        Option passiveFailoverIpDefaultGatewayOption = Option.builder()
                .longOpt(PASSIVE_FAILOVER_IP_DEFAULT_GATEWAY)
                .desc("OPTIONAL: IP address of the gateway for this interface")
                .argName("PASSIVE FAILOVER IP DEFAULT GATEWAY")
                .required(false)
                .hasArg()
                .build();
        Option passiveFailoverIpDnsServersOption = Option.builder()
                .longOpt(PASSIVE_FAILOVER_IP_DNS_SERVERS)
                .desc("OPTIONAL: List of IP addresses of the DNS servers to configure the interface")
                .argName("PASSIVE FAILOVER IP DNS SERVERS")
                .required(false)
                .hasArg()
                .build();
        Option passiveFailoverIpIpv4AddressOption = Option.builder()
                .longOpt(PASSIVE_FAILOVER_IP_IPV4_ADDRESS)
                .desc("OPTIONAL: IP address to be used to configure the interface")
                .argName("PASSIVE FAILOVER IP IPV4 ADDRESS")
                .required(false)
                .hasArg()
                .build();
        Option passiveFailoverIpIpv4SubnetMaskOption = Option.builder()
                .longOpt(PASSIVE_FAILOVER_IP_IPV4_SUBNET_MASK)
                .desc("OPTIONAL: Subnet mask of the interface")
                .argName("PASSIVE FAILOVER IP IPV4 SUBNET MASK")
                .required(false)
                .hasArg()
                .build();
        Option passiveFailoverIpIpv4PrefixOption = Option.builder()
                .longOpt(PASSIVE_FAILOVER_IP_IPV4_PREFIX)
                .desc("OPTIONAL: CIDR Prefix for the interface")
                .argName("PASSIVE FAILOVER IP IPV4 PREFIX")
                .required(false)
                .hasArg()
                .build();
        // Passive HA Network Spec Options
        Option passiveHaIpDefaultGatewayOption = Option.builder()
                .longOpt(PASSIVE_HA_IP_DEFAULT_GATEWAY)
                .desc("OPTIONAL: IP address of the gateway for this interface")
                .argName("PASSIVE HA IP DEFAULT GATEWAY")
                .required(false)
                .hasArg()
                .build();
        Option passiveHaIpDnsServersOption = Option.builder()
                .longOpt(PASSIVE_HA_IP_DNS_SERVERS)
                .desc("List of IP addresses of the DNS servers to configure the interface")
                .argName("PASSIVE HA IP DNS SERVERS")
                .required(true)
                .hasArg()
                .build();
        Option passiveHaIpIpv4AddressOption = Option.builder()
                .longOpt(PASSIVE_HA_IP_IPV4_ADDRESS)
                .desc("IP address to be used to configure the interface")
                .argName("PASSIVE HA IP IPV4 ADDRESS")
                .required(true)
                .hasArg()
                .build();
        Option passiveHaIpIpv4SubnetMaskOption = Option.builder()
                .longOpt(PASSIVE_HA_IP_IPV4_SUBNET_MASK)
                .desc("Subnet mask of the interface")
                .argName("PASSIVE HA IP IPV4 SUBNET MASK")
                .required(true)
                .hasArg()
                .build();
        Option passiveHaIpIpv4PrefixOption = Option.builder()
                .longOpt(PASSIVE_HA_IP_IPV4_PREFIX)
                .desc("OPTIONAL: CIDR Prefix for the interface")
                .argName("PASSIVE HA IP IPV4 PREFIX")
                .required(false)
                .hasArg()
                .build();
        // Passive Placement Spec Options
        Option passivePlacementNameOption = Option.builder()
                .longOpt(PASSIVE_PLACEMENT_NAME)
                .desc("The name of the vCenter HA node to be used for the VM name")
                .argName("PASSIVE PLACEMENT NAME")
                .required(true)
                .hasArg()
                .build();
        Option passivePlacementFolderOption = Option.builder()
                .longOpt(PASSIVE_PLACEMENT_FOLDER)
                .desc("The identifier of the folder to deploy the vCenter HA node to")
                .argName("PASSIVE PLACEMENT FOLDER")
                .required(true)
                .hasArg()
                .build();
        Option passivePlacementHaNetworkOption = Option.builder()
                .longOpt(PASSIVE_PLACEMENT_HA_NETWORK)
                .desc("OPTIONAL: The identifier of the network object to be used for the HA network")
                .argName("PASSIVE PLACEMENT HA NETWORK")
                .required(false)
                .hasArg()
                .build();
        Option passivePlacementHaNetworkTypeOption = Option.builder()
                .longOpt(PASSIVE_PLACEMENT_HA_NETWORK_TYPE)
                .desc("OPTIONAL: The type of the network object to be used by the HA network")
                .argName("PASSIVE PLACEMENT HA NETWORK TYPE")
                .required(false)
                .hasArg()
                .build();
        Option passivePlacementHostOption = Option.builder()
                .longOpt(PASSIVE_PLACEMENT_HOST)
                .desc("The identifier of the host to deploy the vCenter HA node to")
                .argName("PASSIVE PLACEMENT HOST")
                .required(true)
                .hasArg()
                .build();
        Option passivePlacementResourcePoolOption = Option.builder()
                .longOpt(PASSIVE_PLACEMENT_RESOURCE_POOL)
                .desc("OPTIONAL: The identifier of the resource pool to deploy the vCenter HA node to")
                .argName("PASSIVE PLACEMENT RESOURCE POOL")
                .required(false)
                .hasArg()
                .build();
        Option passivePlacementStorageDatastoreOption = Option.builder()
                .longOpt(PASSIVE_PLACEMENT_STORAGE_DATASTORE)
                .desc("The identifier of the datastore to put all the virtual disks on")
                .argName("PASSIVE PLACEMENT STORAGE DATASTORE")
                .required(true)
                .hasArg()
                .build();
        Option passivePlacementManagementNetworkOption = Option.builder()
                .longOpt(PASSIVE_PLACEMENT_MANAGEMENT_NETWORK)
                .desc("The identifier of the network object to be used for the Management network")
                .argName("PASSIVE PLACEMENT MANAGEMENT NETWORK")
                .required(true)
                .hasArg()
                .build();
        Option passivePlacementManagementNetworkTypeOption = Option.builder()
                .longOpt(PASSIVE_PLACEMENT_MANAGEMENT_NETWORK_TYPE)
                .desc("The type of the network object to be used by the Management network")
                .argName("PASSIVE PLACEMENT MANAGEMENT NETWORK TYPE")
                .required(true)
                .hasArg()
                .build();
        // Witness HA Network Spec Options
        Option witnessHaIpDefaultGatewayOption = Option.builder()
                .longOpt(WITNESS_HA_IP_DEFAULT_GATEWAY)
                .desc("OPTIONAL: IP address of the gateway for this interface")
                .argName("WITNESS HA IP DEFAULT GATEWAY")
                .required(false)
                .hasArg()
                .build();
        Option witnessHaIpDnsServersOption = Option.builder()
                .longOpt(WITNESS_HA_IP_DNS_SERVERS)
                .desc("List of IP addresses of the DNS servers to configure the interface")
                .argName("WITNESS HA IP DNS SERVERS")
                .required(true)
                .hasArg()
                .build();
        Option witnessHaIpIpv4AddressOption = Option.builder()
                .longOpt(WITNESS_HA_IP_IPV4_ADDRESS)
                .desc("IP address to be used to configure the interface")
                .argName("WITNESS HA IP IPV4 ADDRESS")
                .required(true)
                .hasArg()
                .build();
        Option witnessHaIpIpv4SubnetMaskOption = Option.builder()
                .longOpt(WITNESS_HA_IP_IPV4_SUBNET_MASK)
                .desc("Subnet mask of the interface")
                .argName("WITNESS HA IP IPV4 SUBNET MASK")
                .required(true)
                .hasArg()
                .build();
        Option witnessHaIpIpv4PrefixOption = Option.builder()
                .longOpt(WITNESS_HA_IP_IPV4_PREFIX)
                .desc("OPTIONAL: CIDR Prefix for the interface")
                .argName("WITNESS HA IP IPV4 PREFIX")
                .required(false)
                .hasArg()
                .build();
        // Witness Placement Spec Options
        Option witnessPlacementNameOption = Option.builder()
                .longOpt(WITNESS_PLACEMENT_NAME)
                .desc("The name of the vCenter HA node to be used for the VM name")
                .argName("WITNESS PLACEMENT NAME")
                .required(true)
                .hasArg()
                .build();
        Option witnessPlacementFolderOption = Option.builder()
                .longOpt(WITNESS_PLACEMENT_FOLDER)
                .desc("The identifier of the folder to deploy the vCenter HA node to")
                .argName("WITNESS PLACEMENT FOLDER")
                .required(true)
                .hasArg()
                .build();
        Option witnessPlacementHaNetworkOption = Option.builder()
                .longOpt(WITNESS_PLACEMENT_HA_NETWORK)
                .desc("OPTIONAL: The identifier of the network object to be used for the HA network")
                .argName("WITNESS PLACEMENT HA NETWORK")
                .required(false)
                .hasArg()
                .build();
        Option witnessPlacementHaNetworkTypeOption = Option.builder()
                .longOpt(WITNESS_PLACEMENT_HA_NETWORK_TYPE)
                .desc("OPTIONAL: The type of the network object to be used by the HA network")
                .argName("WITNESS PLACEMENT HA NETWORK TYPE")
                .required(false)
                .hasArg()
                .build();
        Option witnessPlacementHostOption = Option.builder()
                .longOpt(WITNESS_PLACEMENT_HOST)
                .desc("The identifier of the host to deploy the vCenter HA node to")
                .argName("WITNESS PLACEMENT HOST")
                .required(true)
                .hasArg()
                .build();
        Option witnessPlacementResourcePoolOption = Option.builder()
                .longOpt(WITNESS_PLACEMENT_RESOURCE_POOL)
                .desc("OPTIONAL: The identifier of the resource pool to deploy the vCenter HA node to")
                .argName("WITNESS PLACEMENT RESOURCE POOL")
                .required(false)
                .hasArg()
                .build();
        Option witnessPlacementStorageDatastoreOption = Option.builder()
                .longOpt(WITNESS_PLACEMENT_STORAGE_DATASTORE)
                .desc("The identifier of the datastore to put all the virtual disks on")
                .argName("WITNESS PLACEMENT STORAGE DATASTORE")
                .required(true)
                .hasArg()
                .build();
        Option witnessPlacementManagementNetworkOption = Option.builder()
                .longOpt(WITNESS_PLACEMENT_MANAGEMENT_NETWORK)
                .desc("The identifier of the network object to be used for the Management network")
                .argName("WITNESS PLACEMENT MANAGEMENT NETWORK")
                .required(true)
                .hasArg()
                .build();
        Option witnessPlacementManagementNetworkTypeOption = Option.builder()
                .longOpt(WITNESS_PLACEMENT_MANAGEMENT_NETWORK_TYPE)
                .desc("The type of the network object to be used by the Management network")
                .argName("WITNESS PLACEMENT MANAGEMENT NETWORK TYPE")
                .required(true)
                .hasArg()
                .build();
        List<Option> optionList = Arrays.asList(vcSpecActiveLocationHostnameOption,
                vcSpecActiveLocationUsernameOption,
                vcSpecActiveLocationPasswordOption,
                vcSpecActiveLocationSSLThumbprintOption,
                activeHaIpDefaultGatewayOption,
                activeHaIpDnsServersOption,
                activeHaIpIpv4AddressOption,
                activeHaIpIpv4SubnetMaskOption,
                activeHaIpIpv4PrefixOption,
                activeHaNetworkTypeOption,
                activeHaNetworkOption,
                passiveFailoverIpDefaultGatewayOption,
                passiveFailoverIpDnsServersOption,
                passiveFailoverIpIpv4AddressOption,
                passiveFailoverIpIpv4SubnetMaskOption,
                passiveFailoverIpIpv4PrefixOption,
                passiveHaIpDefaultGatewayOption,
                passiveHaIpDnsServersOption,
                passiveHaIpIpv4AddressOption,
                passiveHaIpIpv4SubnetMaskOption,
                passiveHaIpIpv4PrefixOption,
                passivePlacementNameOption,
                passivePlacementFolderOption,
                passivePlacementHaNetworkOption,
                passivePlacementHaNetworkTypeOption,
                passivePlacementHostOption,
                passivePlacementResourcePoolOption,
                passivePlacementStorageDatastoreOption,
                passivePlacementManagementNetworkOption,
                passivePlacementManagementNetworkTypeOption,
                witnessHaIpDefaultGatewayOption,
                witnessHaIpDnsServersOption,
                witnessHaIpIpv4AddressOption,
                witnessHaIpIpv4SubnetMaskOption,
                witnessHaIpIpv4PrefixOption,
                witnessPlacementNameOption,
                witnessPlacementFolderOption,
                witnessPlacementHaNetworkOption,
                witnessPlacementHaNetworkTypeOption,
                witnessPlacementHostOption,
                witnessPlacementResourcePoolOption,
                witnessPlacementStorageDatastoreOption,
                witnessPlacementManagementNetworkOption,
                witnessPlacementManagementNetworkTypeOption
        );
        super.parseArgs(optionList, args);
        this.vcSpecActiveLocationHostname = getStringArg(parsedOptions, VC_SPEC_ACTIVE_LOCATION_HOSTNAME);
        if(this.vcSpecActiveLocationHostname == null)
            this.vcSpecActiveLocationHostname = this.getServer();
        this.vcSpecActiveLocationUsername = getStringArg(parsedOptions, VC_SPEC_ACTIVE_LOCATION_USERNAME);
        if(this.vcSpecActiveLocationUsername == null)
            this.vcSpecActiveLocationUsername = this.getUsername();
        this.vcSpecActiveLocationPassword = getStringArg(parsedOptions, VC_SPEC_ACTIVE_LOCATION_PASSWORD);
        if(this.vcSpecActiveLocationPassword == null)
            this.vcSpecActiveLocationPassword = this.getPassword();
        this.vcSpecActiveLocationSSLThumbprint = getStringArg(parsedOptions, VC_SPEC_ACTIVE_LOCATION_SSL_THUMBPRINT);
        this.activeHaIpDefaultGateway = getStringArg(parsedOptions, ACTIVE_HA_IP_DEFAULT_GATEWAY);
        this.activeHaIpDnsServers = getStringListArg(parsedOptions, ACTIVE_HA_IP_DNS_SERVERS);
        this.activeHaIpIpv4Address = getStringArg(parsedOptions, ACTIVE_HA_IP_IPV4_ADDRESS);
        this.activeHaIpIpv4SubnetMask = getStringArg(parsedOptions, ACTIVE_HA_IP_IPV4_SUBNET_MASK);
        this.activeHaIpIpv4Prefix = getLongArg(parsedOptions, ACTIVE_HA_IP_IPV4_PREFIX);
        this.activeHaNetwork = getStringArg(parsedOptions, ACTIVE_HA_NETWORK);
        this.activeHaNetworkType = getNetworkTypeArg(parsedOptions, ACTIVE_HA_NETWORK_TYPE);
        this.passiveFailoverIpDefaultGateway = getStringArg(parsedOptions, PASSIVE_FAILOVER_IP_DEFAULT_GATEWAY);
        this.passiveFailoverIpDnsServers = getStringListArg(parsedOptions, PASSIVE_FAILOVER_IP_DNS_SERVERS);
        this.passiveFailoverIpIpv4Address = getStringArg(parsedOptions, PASSIVE_FAILOVER_IP_IPV4_ADDRESS);
        this.passiveFailoverIpIpv4SubnetMask = getStringArg(parsedOptions, PASSIVE_FAILOVER_IP_IPV4_SUBNET_MASK);
        this.passiveFailoverIpIpv4Prefix = getLongArg(parsedOptions, PASSIVE_FAILOVER_IP_IPV4_PREFIX);
        this.passiveHaIpDefaultGateway = getStringArg(parsedOptions, PASSIVE_HA_IP_DEFAULT_GATEWAY);
        this.passiveHaIpDnsServers = getStringListArg(parsedOptions, PASSIVE_HA_IP_DNS_SERVERS);
        this.passiveHaIpIpv4Address = getStringArg(parsedOptions, PASSIVE_HA_IP_IPV4_ADDRESS);
        this.passiveHaIpIpv4SubnetMask = getStringArg(parsedOptions, PASSIVE_HA_IP_IPV4_SUBNET_MASK);
        this.passiveHaIpIpv4Prefix = getLongArg(parsedOptions, PASSIVE_HA_IP_IPV4_PREFIX);
        this.passivePlacementName = getStringArg(parsedOptions, PASSIVE_PLACEMENT_NAME);
        this.passivePlacementFolder = getStringArg(parsedOptions, PASSIVE_PLACEMENT_FOLDER);
        this.passivePlacementHaNetwork = getStringArg(parsedOptions, PASSIVE_PLACEMENT_HA_NETWORK);
        this.passivePlacementHaNetworkType = getNetworkTypeArg(parsedOptions, PASSIVE_PLACEMENT_HA_NETWORK_TYPE);
        this.passivePlacementHost = getStringArg(parsedOptions, PASSIVE_PLACEMENT_HOST);
        this.passivePlacementResourcePool = getStringArg(parsedOptions, PASSIVE_PLACEMENT_RESOURCE_POOL);
        this.passivePlacementStorageDatastore = getStringArg(parsedOptions, PASSIVE_PLACEMENT_STORAGE_DATASTORE);
        this.passivePlacementManagementNetwork = getStringArg(parsedOptions, PASSIVE_PLACEMENT_MANAGEMENT_NETWORK);
        this.passivePlacementManagementNetworkType = getNetworkTypeArg(parsedOptions,
                                                                       PASSIVE_PLACEMENT_MANAGEMENT_NETWORK_TYPE);
        this.witnessHaIpDefaultGateway = getStringArg(parsedOptions, WITNESS_HA_IP_DEFAULT_GATEWAY);
        this.witnessHaIpDnsServers = getStringListArg(parsedOptions, WITNESS_HA_IP_DNS_SERVERS);
        this.witnessHaIpIpv4Address = getStringArg(parsedOptions, WITNESS_HA_IP_IPV4_ADDRESS);
        this.witnessHaIpIpv4SubnetMask = getStringArg(parsedOptions, WITNESS_HA_IP_IPV4_SUBNET_MASK);
        this.witnessHaIpIpv4Prefix = getLongArg(parsedOptions, WITNESS_HA_IP_IPV4_PREFIX);
        this.witnessPlacementName = getStringArg(parsedOptions, WITNESS_PLACEMENT_NAME);
        this.witnessPlacementFolder = getStringArg(parsedOptions, WITNESS_PLACEMENT_FOLDER);
        this.witnessPlacementHaNetwork = getStringArg(parsedOptions, WITNESS_PLACEMENT_HA_NETWORK);
        this.witnessPlacementHaNetworkType = getNetworkTypeArg(parsedOptions, WITNESS_PLACEMENT_HA_NETWORK_TYPE);
        this.witnessPlacementHost = getStringArg(parsedOptions, WITNESS_PLACEMENT_HOST);
        this.witnessPlacementResourcePool = getStringArg(parsedOptions, WITNESS_PLACEMENT_RESOURCE_POOL);
        this.witnessPlacementStorageDatastore = getStringArg(parsedOptions, WITNESS_PLACEMENT_STORAGE_DATASTORE);
        this.witnessPlacementManagementNetwork = getStringArg(parsedOptions, WITNESS_PLACEMENT_MANAGEMENT_NETWORK);
        this.witnessPlacementManagementNetworkType = getNetworkTypeArg(parsedOptions,
                                                                       WITNESS_PLACEMENT_MANAGEMENT_NETWORK_TYPE);
    }

    protected void setup() throws Exception {
        this.clusterService =
                this.vapiAuthHelper.getStubFactory().createStub(Cluster.class, this.sessionStubConfig);
        this.mgmtVcCredentialsSpec = SpecHelper.createCredentialsSpec(this.vcSpecActiveLocationHostname,
                                                                      this.vcSpecActiveLocationUsername,
                                                                      this.vcSpecActiveLocationPassword,
                                                                      this.vcSpecActiveLocationSSLThumbprint);
    }

    protected void run() throws Exception {
        // Deploy vCenter HA Cluster and List Cluster Info

        // Spec for active node HA network interface
        IpSpec activeHaIpSpec = SpecHelper.createIpSpec(this.activeHaIpIpv4Address,
                                                        this.activeHaIpIpv4SubnetMask,
                                                        this.activeHaIpIpv4Prefix,
                                                        this.activeHaIpDefaultGateway,
                                                        this.activeHaIpDnsServers);

        // Spec for active Node
        ClusterTypes.ActiveSpec activeSpec = SpecHelper.createActiveSpec(activeHaIpSpec,
                                                                         this.activeHaNetwork,
                                                                         this.activeHaNetworkType);

        // Spec for passive node HA network interface
        IpSpec passiveHaIpSpec = SpecHelper.createIpSpec(this.passiveHaIpIpv4Address,
                                                         this.passiveHaIpIpv4SubnetMask,
                                                         this.passiveHaIpIpv4Prefix,
                                                         this.passiveHaIpDefaultGateway,
                                                         this.passiveHaIpDnsServers);
        // Spec for passive node failover network interface
        IpSpec passiveFailoverIpSpec = SpecHelper.createIpSpec(this.passiveFailoverIpIpv4Address,
                                                               this.passiveFailoverIpIpv4SubnetMask,
                                                               this.passiveFailoverIpIpv4Prefix,
                                                               this.passiveFailoverIpDefaultGateway,
                                                               this.passiveFailoverIpDnsServers);
        // Spec for passive node placement
        PlacementSpec passivePlacementSpec = SpecHelper.createPlacementSpec(this.passivePlacementName,
                                                                            this.passivePlacementFolder,
                                                                            this.passivePlacementHost,
                                                                            this.passivePlacementHaNetwork,
                                                                            this.passivePlacementHaNetworkType,
                                                                            this.passivePlacementStorageDatastore,
                                                                            this.passivePlacementResourcePool,
                                                                            this.passivePlacementManagementNetwork,
                                                                            this.passivePlacementManagementNetworkType);
        // Spec for passive node
        ClusterTypes.PassiveSpec passiveSpec = SpecHelper.createPassiveSpec(passiveHaIpSpec,
                                                                            passiveFailoverIpSpec,
                                                                            passivePlacementSpec);

        // Spec for witness node HA network interface
        IpSpec witnessHaIpSpec = SpecHelper.createIpSpec(this.witnessHaIpIpv4Address,
                                                         this.witnessHaIpIpv4SubnetMask,
                                                         this.witnessHaIpIpv4Prefix,
                                                         this.witnessHaIpDefaultGateway,
                                                         this.witnessHaIpDnsServers);
        // Spec for witness node placement
        PlacementSpec witnessPlacementSpec = SpecHelper.createPlacementSpec(this.witnessPlacementName,
                                                                            this.witnessPlacementFolder,
                                                                            this.witnessPlacementHost,
                                                                            this.witnessPlacementHaNetwork,
                                                                            this.witnessPlacementHaNetworkType,
                                                                            this.witnessPlacementStorageDatastore,
                                                                            this.witnessPlacementResourcePool,
                                                                            this.witnessPlacementManagementNetwork,
                                                                            this.witnessPlacementManagementNetworkType);
        // Spec for witness node
        ClusterTypes.WitnessSpec witnessSpec = SpecHelper.createWitnessSpec(witnessHaIpSpec, witnessPlacementSpec);

        // Spec for vCenter HA cluster deployment
        ClusterTypes.DeploySpec deploySpec = SpecHelper.createDeploySpec(ClusterTypes.Type.AUTO,
                                                                         activeSpec, passiveSpec, witnessSpec,
                                                                         this.mgmtVcCredentialsSpec);

        System.out.println("--------------------------------------------------------------------");
        System.out.println("DEPLOY vCenter HA CLUSTER");
        System.out.println("--------------------------------------------------------------------");
        try {
            String deployTaskID = this.clusterService.deploy_Task(deploySpec);
            if(TaskHelper.waitForTask(this.vimAuthHelper, deployTaskID)) {
                // Wait for cluster to be healthy
                TaskHelper.sleep(TaskHelper.TASK_SLEEP);
            }
        } catch(Exception e) {
            System.out.println("Unable to deploy vCenter HA Cluster");
            System.out.println("Stack trace: ");
            e.printStackTrace();
        }
        System.out.println("--------------------------------------------------------------------");
        System.out.println("CLUSTER INFO");
        System.out.println("--------------------------------------------------------------------");
        ClusterTypes.Info clusterInfo = this.clusterService.get(this.mgmtVcCredentialsSpec, false);
        System.out.println(clusterInfo.toString());
    }

    protected void cleanup() throws Exception {
        // Undeploy Cluster and Delete VMs
        ClusterTypes.UndeploySpec undeploySpec = SpecHelper.createUndeploySpec(this.mgmtVcCredentialsSpec, true);
        System.out.println("--------------------------------------------------------------------");
        System.out.println("UNDEPLOY vCenter HA CLUSTER");
        System.out.println("--------------------------------------------------------------------");
        String undeployTaskID = this.clusterService.undeploy_Task(undeploySpec);
        TaskHelper.waitForTask(this.vimAuthHelper, undeployTaskID);
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
        new VchaClusterOps().execute(args);
    }
}