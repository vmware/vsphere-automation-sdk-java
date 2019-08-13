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
package vmware.samples.appliance.helpers;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.vmware.appliance.NetworkingTypes.DNSInfo;
import com.vmware.appliance.NetworkingTypes.Info;
import com.vmware.appliance.networking.InterfacesTypes.InterfaceInfo;
import com.vmware.appliance.networking.ProxyTypes.Config;
import com.vmware.appliance.networking.ProxyTypes.Protocol;
import com.vmware.appliance.networking.interfaces.Ipv6Types.AddressInfo;

public class NetworkingHelper {
	/**
	 * Display Networking information
	 *
	 * @param networkInfo Network Information to print
	 */
	public static void printNetworkInfo(Info networkInfo) {
		DNSInfo dns = networkInfo.getDns();
		System.out.println("DNS Mode : " + dns.getMode());
		System.out.println("DNS Hostname : " + dns.getHostname());
		System.out.println("DNS Servers : " + dns.getServers());
		System.out.println();
		Map<String, InterfaceInfo> interfaceInfo = networkInfo.getInterfaces();
		for (Entry<String, InterfaceInfo> interfaceEntry : interfaceInfo.entrySet()) {
			printInterfaceInfo(interfaceEntry);
		}
	}

	/**
	 * Display interface specific information
	 *
	 * @param interfaceEntry Interface information map
	 */
	public static void printInterfaceInfo(Entry<String, InterfaceInfo> interfaceEntry) {
		System.out.println("--------------------------------");
		System.out.println("Interface information for : " + interfaceEntry.getKey());
		System.out.println("--------------------------------");
		System.out.println("Nic Status : " + interfaceEntry.getValue().getStatus());
		System.out.println("Mac : " + interfaceEntry.getValue().getMac());
		com.vmware.appliance.networking.interfaces.Ipv4Types.Info ipv4Info = 
				(interfaceEntry.getValue().getIpv4());
		com.vmware.appliance.networking.interfaces.Ipv6Types.Info ipv6Info = 
				(interfaceEntry.getValue().getIpv6());
		System.out.println();
		System.out.println("IPv4 Information:");
		System.out.println("-----------------");
		if (ipv4Info == null) {
			System.out.println("IPv4 information is not available for : " + interfaceEntry.getKey());
		} else {
			printIPv4Info(ipv4Info);
		}
		System.out.println();
		System.out.println("IPv6 Information:");
		System.out.println("-----------------");
		if (ipv6Info == null) {
			System.out.println("IPv6 information is not available for : " + interfaceEntry.getKey());
		} else {
			printIPv6Info(ipv6Info);
		}
	}

	/**
	 * Display IPv4 Information
	 *
	 * @param ipv4Info IPv4 information
	 */
	public static void printIPv4Info(com.vmware.appliance.networking.interfaces.Ipv4Types.Info ipv4Info) {
		if (ipv4Info == null) {
			System.out.println("IPv4 information is not available ");
		} else {
			System.out.println("Is Configurable : " + ipv4Info.getConfigurable());
			System.out.println("IPv4 mode : " + ipv4Info.getMode());
			System.out.println("IPv4 address : " + ipv4Info.getAddress());
			System.out.println("IPv4 Prefix : " + ipv4Info.getPrefix());
			System.out.println("Default Gateway : " + ipv4Info.getDefaultGateway());
		}
	}

	/**
	 * Display IPv6 Information
	 *
	 * @param ipv6Info IPv6 information to print
	 */
	public static void printIPv6Info(com.vmware.appliance.networking.interfaces.Ipv6Types.Info ipv6Info) {
		if (ipv6Info == null) {
			System.out.println("IPv6 information is not available ");
		} else {
			System.out.println("Is Configurable : " + ipv6Info.getConfigurable());
			System.out.println("Is DHCP : " + ipv6Info.getDhcp());
			System.out.println("Is AutoConf : " + ipv6Info.getAutoconf());
			System.out.println("Default Gateway : " + ipv6Info.getDefaultGateway());
			List<AddressInfo> addressInformation = ipv6Info.getAddresses();
			for (AddressInfo addressInfo : addressInformation) {
				System.out.println("Origin : " + addressInfo.getOrigin());
				System.out.println("Status : " + addressInfo.getStatus());
				System.out.println("Address : " + addressInfo.getAddress());
				System.out.println("Prefix : " + addressInfo.getPrefix());
			}
		}
	}

	/**
	 * Print Proxy Details for various protocols
	 *
	 * @param proxyDetails Proxy configuration and protocol map
	 */
	public static void printProxyDetails(Map<Protocol, Config> proxyDetails) {
		for (Entry<Protocol, Config> proxy : proxyDetails.entrySet()) {
			printProxyDetail(proxy.getKey().name(), proxy.getValue());
		}
	}

	/**
	 * Print Proxy detail for a specific protocol
	 *
	 * @param protocol    Proxy Protocol
	 * @param proxyConfig Proxy Configuration
	 */
	public static void printProxyDetail(String protocol, Config proxyConfig) {
		System.out.println("Proxy details for protocol : " + protocol);
		System.out.println("-----------------------------------");
		System.out.println("Is proxy enabled : " + proxyConfig.getEnabled());
		System.out.println("Proxy Server : " + proxyConfig.getServer());
		System.out.println("Proxy port : " + proxyConfig.getPort());
		String username = proxyConfig.getUsername();
		if (username != null) {
			System.out.println("Proxy Username : " + proxyConfig.getUsername());
			System.out.println("Proxy Password : " + new String(proxyConfig.getPassword()));
		}
		System.out.println();
	}
}
