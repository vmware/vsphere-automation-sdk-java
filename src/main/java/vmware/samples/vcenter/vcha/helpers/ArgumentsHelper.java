/*
 * *******************************************************
 * Copyright VMware, Inc. 2018.  All Rights Reserved.
 * SPDX-License-Identifier: MIT
 * *******************************************************
 *
 * DISCLAIMER. THIS PROGRAM IS PROVIDED TO YOU "AS IS" WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, WHETHER ORAL OR WRITTEN,
 * EXPRESS OR IMPLIED. THE AUTHOR SPECIFICALLY DISCLAIMS ANY IMPLIED
 * WARRANTIES OR CONDITIONS OF MERCHANTABILITY, SATISFACTORY QUALITY,
 * NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE.
 */

package vmware.samples.vcenter.vcha.helpers;

import com.vmware.vcenter.vcha.NetworkType;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ArgumentsHelper {
    public static final String VC_SPEC_ACTIVE_LOCATION_HOSTNAME = "vc-spec-active-location-hostname";
    public static final String VC_SPEC_ACTIVE_LOCATION_USERNAME = "vc-spec-active-location-username";
    public static final String VC_SPEC_ACTIVE_LOCATION_PASSWORD = "vc-spec-active-location-password";
    public static final String VC_SPEC_ACTIVE_LOCATION_SSL_THUMBPRINT = "vc-spec-active-location-ssl-thumbprint";
    public static final String ACTIVE_HA_IP_DEFAULT_GATEWAY = "active-ha-ip-default-gateway";
    public static final String ACTIVE_HA_IP_DNS_SERVERS = "active-ha-ip-dns-servers";
    public static final String ACTIVE_HA_IP_IPV4_ADDRESS = "active-ha-ip-ipv4-address";
    public static final String ACTIVE_HA_IP_IPV4_SUBNET_MASK = "active-ha-ip-ipv4-subnet-mask";
    public static final String ACTIVE_HA_IP_IPV4_PREFIX = "active-ha-ip-ipv4-prefix";
    public static final String ACTIVE_HA_NETWORK = "active-ha-network";
    public static final String ACTIVE_HA_NETWORK_TYPE = "active-ha-network-type";
    public static final String PASSIVE_FAILOVER_IP_DEFAULT_GATEWAY = "passive-failover-ip-default-gateway";
    public static final String PASSIVE_FAILOVER_IP_DNS_SERVERS = "passive-failover-ip-dns-servers";
    public static final String PASSIVE_FAILOVER_IP_IPV4_ADDRESS = "passive-failover-ip-ipv4-address";
    public static final String PASSIVE_FAILOVER_IP_IPV4_SUBNET_MASK = "passive-failover-ip-ipv4-subnet-mask";
    public static final String PASSIVE_FAILOVER_IP_IPV4_PREFIX = "passive-failover-ip-ipv4-prefix";
    public static final String PASSIVE_HA_IP_DEFAULT_GATEWAY = "passive-ha-ip-default-gateway";
    public static final String PASSIVE_HA_IP_DNS_SERVERS = "passive-ha-ip-dns-servers";
    public static final String PASSIVE_HA_IP_IPV4_ADDRESS = "passive-ha-ip-ipv4-address";
    public static final String PASSIVE_HA_IP_IPV4_SUBNET_MASK = "passive-ha-ip-ipv4-subnet-mask";
    public static final String PASSIVE_HA_IP_IPV4_PREFIX = "passive-ha-ip-ipv4-prefix";
    public static final String PASSIVE_PLACEMENT_NAME = "passive-placement-name";
    public static final String PASSIVE_PLACEMENT_FOLDER = "passive-placement-folder";
    public static final String PASSIVE_PLACEMENT_HA_NETWORK = "passive-placement-ha-network";
    public static final String PASSIVE_PLACEMENT_HA_NETWORK_TYPE = "passive-placement-ha-network-type";
    public static final String PASSIVE_PLACEMENT_HOST = "passive-placement-host";
    public static final String PASSIVE_PLACEMENT_RESOURCE_POOL = "passive-placement-resource-pool";
    public static final String PASSIVE_PLACEMENT_STORAGE_DATASTORE = "passive-placement-storage-datastore";
    public static final String PASSIVE_PLACEMENT_MANAGEMENT_NETWORK = "passive-placement-management-network";
    public static final String PASSIVE_PLACEMENT_MANAGEMENT_NETWORK_TYPE = "passive-placement-management-network-type";
    public static final String WITNESS_HA_IP_DEFAULT_GATEWAY = "witness-ha-ip-default-gateway";
    public static final String WITNESS_HA_IP_DNS_SERVERS = "witness-ha-ip-dns-servers";
    public static final String WITNESS_HA_IP_IPV4_ADDRESS = "witness-ha-ip-ipv4-address";
    public static final String WITNESS_HA_IP_IPV4_SUBNET_MASK = "witness-ha-ip-ipv4-subnet-mask";
    public static final String WITNESS_HA_IP_IPV4_PREFIX = "witness-ha-ip-ipv4-prefix";
    public static final String WITNESS_PLACEMENT_NAME = "witness-placement-name";
    public static final String WITNESS_PLACEMENT_FOLDER = "witness-placement-folder";
    public static final String WITNESS_PLACEMENT_HA_NETWORK = "witness-placement-ha-network";
    public static final String WITNESS_PLACEMENT_HA_NETWORK_TYPE = "witness-placement-ha-network-type";
    public static final String WITNESS_PLACEMENT_HOST = "witness-placement-host";
    public static final String WITNESS_PLACEMENT_RESOURCE_POOL = "witness-placement-resource-pool";
    public static final String WITNESS_PLACEMENT_STORAGE_DATASTORE = "witness-placement-storage-datastore";
    public static final String WITNESS_PLACEMENT_MANAGEMENT_NETWORK = "witness-placement-management-network";
    public static final String WITNESS_PLACEMENT_MANAGEMENT_NETWORK_TYPE = "witness-placement-management-network-type";

    public static final String STRING_LIST_SEPERATOR = ",";

    public static String getStringArg(Map<String, Object> parsedOptions, String key) {
        if(parsedOptions.containsKey(key))
            return (String) parsedOptions.get(key);
        return null;
    }

    public static Long getLongArg(Map<String, Object> parsedOptions, String key) {
        String stringArg = getStringArg(parsedOptions, key);
        if(!isEmptyOrNull(stringArg))
            return Long.valueOf(stringArg);
        return null;
    }

    public static List<String> getStringListArg(Map<String, Object> parsedOptions, String key) {
        String stringArg = getStringArg(parsedOptions, key);
        if(!isEmptyOrNull(stringArg))
            return Arrays.asList(stringArg.split(STRING_LIST_SEPERATOR));
        return null;
    }

    public static NetworkType getNetworkTypeArg(Map<String, Object> parsedOptions, String key) {
        String stringArg = getStringArg(parsedOptions, key);
        if(!isEmptyOrNull(stringArg))
            return NetworkType.valueOf(stringArg);
        return null;
    }

    public static Boolean isEmptyOrNull(String s) {
        return s == null || s.isEmpty();
    }
}

