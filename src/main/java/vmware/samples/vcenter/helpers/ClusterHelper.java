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
import java.util.List;
import java.util.Set;

import com.vmware.vapi.bindings.StubConfiguration;
import com.vmware.vapi.bindings.StubFactory;
import com.vmware.vcenter.Cluster;
import com.vmware.vcenter.ClusterTypes;

public class ClusterHelper {

    /**
     * Returns the identifier of a cluster
     *
     * Note: The method assumes that there is only one cluster and datacenter
     * with the mentioned names.
     *
     * @param stubFactory Stub factory for the api endpoint
     * @param sessionStubConfig stub configuration for the current session
     * @param datacenterName name of the datacenter
     * @param clusterName name of the cluster
     * @return identifier of a cluster
     */
    public static String getCluster(
        StubFactory stubFactory, StubConfiguration sessionStubConfig,
        String datacenterName, String clusterName) {
        Cluster clusterService = stubFactory.createStub(Cluster.class,
            sessionStubConfig);
        Set<String> clusters = Collections.singleton(clusterName);
        ClusterTypes.FilterSpec.Builder clusterFilterBuilder = 
                new ClusterTypes.FilterSpec.Builder().setNames(clusters);
        if (null != datacenterName) {
            // Get the datacenter
            Set<String> datacenters = Collections.singleton(
                    DatacenterHelper.getDatacenter(stubFactory,
                            sessionStubConfig, datacenterName));
            clusterFilterBuilder.setDatacenters(datacenters);
        }
        List<ClusterTypes.Summary> clusterSummaries =
                clusterService.list(clusterFilterBuilder.build());
        assert clusterSummaries.size() > 0 : "Cluster " + clusterName
                                             + "not found in datacenter: "
                                             + datacenterName;
        return clusterSummaries.get(0).getCluster();
    }

    public static String getCluster(StubFactory stubFactory, StubConfiguration
            sessionStubConfig, String clusterName) {
        return getCluster(stubFactory, sessionStubConfig, null, clusterName);
    }
}
