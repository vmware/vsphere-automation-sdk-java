/*
 * *******************************************************
 * Copyright VMware, Inc. 2016.  All Rights Reserved.
 * SPDX-License-Identifier: MIT
 * *******************************************************
 *
 * DISCLAIMER. THIS PROGRAM IS PROVIDED TO YOU "AS IS" WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, WHETHER ORAL OR WRITTEN,
 * EXPRESS OR IMPLIED. THE AUTHOR SPECIFICALLY DISCLAIMS ANY IMPLIED
 * WARRANTIES OR CONDITIONS OF MERCHANTABILITY, SATISFACTORY QUALITY,
 * NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE.
 */
package vmware.samples.vcenter.helpers;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.vmware.vapi.bindings.StubConfiguration;
import com.vmware.vapi.bindings.StubFactory;
import com.vmware.vcenter.Network;
import com.vmware.vcenter.NetworkTypes;

/**
 * Helper class with methods to get identifiers of standard or
 * a distributed network.
 */
public class NetworkHelper {
    /**
     * Returns the identifier of a standard network.
     *
     * Note: The method assumes that there is only one standard portgroup
     * and datacenter with the mentioned names.
     *
     * @param stubFactory stub factory of the API endpoint
     * @param sessionStubConfig stub configuration for the current session
     * @param datacenterName name of the datacenter on which the network exists
     * @param stdPortgroupName name of the standard portgroup
     * @return identifier of a standard network.
     */
    public static String getStandardNetworkBacking(
        StubFactory stubFactory, StubConfiguration sessionStubConfig,
        String datacenterName, String stdPortgroupName) {

        Network networkService = stubFactory.createStub(Network.class,
            sessionStubConfig);

        // Get the datacenter id
        Set<String> datacenters = Collections.singleton(DatacenterHelper
            .getDatacenter(stubFactory, sessionStubConfig, datacenterName));

        // Get the network id
        Set<String> networkNames = Collections.singleton(stdPortgroupName);
        Set<NetworkTypes.Type> networkTypes = new HashSet<>(Collections
            .singletonList(NetworkTypes.Type.STANDARD_PORTGROUP));
        NetworkTypes.FilterSpec networkFilterSpec =
                new NetworkTypes.FilterSpec.Builder().setDatacenters(
                    datacenters)
                    .setNames(networkNames)
                    .setTypes(networkTypes)
                    .build();
        List<NetworkTypes.Summary> networkSummaries = networkService.list(
            networkFilterSpec);
        assert networkSummaries.size() > 0 : "Standard Portgroup with name "
                                             + stdPortgroupName
                                             + " not found in datacenter "
                                             + datacenterName;

        return networkSummaries.get(0).getNetwork();
    }

    /**
     * Returns the identifier of a distributed network
     *
     * Note: The method assumes that there is only one distributed portgroup
     * and datacenter with the mentioned names.
     *
     * @param stubFactory stub factory of the API endpoint
     * @param sessionStubConfig stub configuration for the session
     * @param datacenterName name of the datacenter on which the distributed
     * network exists
     * @param vdPortgroupName name of the distributed portgroup
     * @return identifier of the distributed network
     */
    public static String getDistributedNetworkBacking(
        StubFactory stubFactory, StubConfiguration sessionStubConfig,
        String datacenterName, String vdPortgroupName) {

        Network networkService = stubFactory.createStub(Network.class,
            sessionStubConfig);

        // Get the datacenter id
        Set<String> datacenters = Collections.singleton(DatacenterHelper
            .getDatacenter(stubFactory, sessionStubConfig, datacenterName));

        // Get the network id
        Set<String> networkNames = Collections.singleton(vdPortgroupName);
        Set<NetworkTypes.Type> networkTypes = new HashSet<>(Collections
            .singletonList(NetworkTypes.Type.DISTRIBUTED_PORTGROUP));
        NetworkTypes.FilterSpec networkFilterSpec =
                new NetworkTypes.FilterSpec.Builder().setDatacenters(
                    datacenters)
                    .setNames(networkNames)
                    .setTypes(networkTypes)
                    .build();
        List<NetworkTypes.Summary> networkSummaries = networkService.list(
            networkFilterSpec);
        assert networkSummaries.size() > 0 : "Distributed Portgroup with name "
                                             + vdPortgroupName
                                             + " not found in datacenter "
                                             + datacenterName;

        return networkSummaries.get(0).getNetwork();
    }
    
    /**
     * Returns the identifier of a Opaque network
     *
     * Note: The method assumes that there is only one Opaque portgroup
     * with the mentioned name.
     *
     * @param stubFactory stub factory of the API endpoint
     * @param sessionStubConfig stub configuration for the session
     * network exists
     * @param opaquePortgroup name of the opaque portgroup
     * @return identifier of the opaque network
     */
    public static String getOpaqueNetworkBacking(
        StubFactory stubFactory, StubConfiguration sessionStubConfig,
        String opaquePortgroup) {

        Network networkService = stubFactory.createStub(Network.class,
            sessionStubConfig);

        // Get the network id
        Set<String> networkNames = Collections.singleton(opaquePortgroup);
        Set<NetworkTypes.Type> networkTypes = new HashSet<>(Collections
            .singletonList(NetworkTypes.Type.OPAQUE_NETWORK));
        NetworkTypes.FilterSpec networkFilterSpec =
                new NetworkTypes.FilterSpec.Builder()
                    .setNames(networkNames)
                    .setTypes(networkTypes)
                    .build();
        List<NetworkTypes.Summary> networkSummaries = networkService.list(
            networkFilterSpec);
        assert networkSummaries.size() > 0 : "Opaque Portgroup with name "
                                             + opaquePortgroup
                                             + " not found ";

        return networkSummaries.get(0).getNetwork();
    }
}
