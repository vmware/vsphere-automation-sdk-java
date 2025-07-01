/*
 * *******************************************************
 * Copyright VMware, Inc. 2020.  All Rights Reserved.
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
import java.util.List;
import java.util.Set;

import com.vmware.vapi.bindings.StubConfiguration;
import com.vmware.vapi.bindings.StubFactory;
import com.vmware.vcenter.Host;
import com.vmware.vcenter.HostTypes;

public class HostHelper {

    /**
     * Returns the identifier of a Host
     *
     * Note: The method assumes that there is only one Host, cluster and datacenter
     * with the mentioned names.
     *
     * @param stubFactory Stub factory for the api endpoint
     * @param sessionStubConfig stub configuration for the current session
     * @param datacenterName name of the datacenter
     * @param clusterName name of the cluster
     * @param hostName name of the host
     * @return identifier of a Host
     */
    public static String getHost(
        StubFactory stubFactory, StubConfiguration sessionStubConfig,
        String datacenterName, String clusterName, String hostName) {
        Host hostService = stubFactory.createStub(Host.class,
            sessionStubConfig);
        HostTypes.FilterSpec.Builder hostFilterBuilder = new HostTypes.FilterSpec.Builder();

        List<HostTypes.Summary> hostSummaries = null;
        if (null != hostName) {
            Set<String> hosts = Collections.singleton(hostName);
            hostFilterBuilder.setNames(hosts);
        }
        if(null != clusterName) {
            Set<String> clusters =  Collections.singleton(
                    ClusterHelper.getCluster(stubFactory, sessionStubConfig, datacenterName, clusterName));
            hostFilterBuilder.setClusters(clusters);
            hostSummaries =
                    hostService.list(hostFilterBuilder.build());
            assert hostSummaries.size() > 0 : "Host " + hostName
                                            + "not found in cluster: "
                                            + clusterName;
        }else {
            Set<String> datacenters = Collections.singleton(
                    DatacenterHelper.getDatacenter(stubFactory,
                            sessionStubConfig, datacenterName));
            hostFilterBuilder.setDatacenters(datacenters);
            hostSummaries =
                    hostService.list(hostFilterBuilder.build());
            assert hostSummaries.size() > 0 : "Host " + hostName
                                                 + "not found under datacenter: "
                                                 + datacenterName;
        }
        return hostSummaries.get(0).getHost();
    }
}
