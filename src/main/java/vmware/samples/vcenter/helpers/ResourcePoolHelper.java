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
import com.vmware.vcenter.ResourcePool;
import com.vmware.vcenter.ResourcePoolTypes;

public class ResourcePoolHelper {
    /**
     * Returns the identifier of a resource pool
     *
     * Note: The method assumes that there is only one resource pool
     * with the mentioned name.
     *
     * @param stubFactory Stub factory for the api endpoint
     * @param sessionStubConfig stub configuration for the current session
     * @param resourcePoolName name of the resource pool
     * @return identifier of a resource pool
     */
    public static String getResourcePool(
            StubFactory stubFactory, StubConfiguration sessionStubConfig, String resourcePoolName) {
        return getResourcePool(stubFactory, sessionStubConfig, null, resourcePoolName);
    }
    /**
     * Returns the identifier of a resource pool
     *
     * Note: The method assumes that there is only one resource pool
     * and datacenter with the mentioned names.
     *
     * @param stubFactory Stub factory for the api endpoint
     * @param sessionStubConfig stub configuration for the current session
     * @param datacenterName name of the datacenter
     * @param resourcePoolName name of the resource pool
     * @return identifier of a resource pool
     */
    public static String getResourcePool(
        StubFactory stubFactory, StubConfiguration sessionStubConfig,
        String datacenterName, String resourcePoolName) {
        // Get the resource pool
        ResourcePool resourcePoolService = stubFactory.createStub(
            ResourcePool.class, sessionStubConfig);
        Set<String> resourcePools = Collections.singleton(resourcePoolName);
        ResourcePoolTypes.FilterSpec rpFilterSpec = null;
        List<ResourcePoolTypes.Summary> rpSummaries = null;
        if(null == datacenterName) {
        	rpFilterSpec = new ResourcePoolTypes.FilterSpec.Builder().setNames(
                    resourcePools).build();
        	rpSummaries = resourcePoolService.list(rpFilterSpec);        	
                assert rpSummaries.size() > 0 : "Resource Pool " + resourcePoolName
                                                + "not found";
        }else {
        	// Get the datacenter
            Set<String> datacenters = Collections.singleton(DatacenterHelper
                .getDatacenter(stubFactory, sessionStubConfig, datacenterName));            
            rpFilterSpec = new ResourcePoolTypes.FilterSpec.Builder().setNames(
                        resourcePools).setDatacenters(datacenters).build();
            rpSummaries = resourcePoolService.list(rpFilterSpec);
            assert rpSummaries.size() > 0 : "Resource Pool " + resourcePoolName
                                            + "not found in datacenter: "
                                            + datacenterName;
        }        
        String resourcepoolId = rpSummaries.get(0).getResourcePool();        
        return resourcepoolId;
    }
}
