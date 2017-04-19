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
import com.vmware.vcenter.Datacenter;
import com.vmware.vcenter.DatacenterTypes;

public class DatacenterHelper {

    /**
     * Returns the identifier of a datacenter
     *
     * Note: The method assumes only one datacenter with the
     * mentioned name.
     * @param stubFactory Stub factory for the api endpoint
     * @param sessionStubConfig stub configuration for the current session
     * @param datacenterName name of the datacenter for the placement spec
     * @return identifier of a datacenter
     */
    public static String getDatacenter(
        StubFactory stubFactory, StubConfiguration sessionStubConfig,
        String datacenterName) {

        Datacenter datacenterService = stubFactory.createStub(Datacenter.class,
            sessionStubConfig);

        Set<String> datacenterNames = Collections.singleton(datacenterName);
        DatacenterTypes.FilterSpec dcFilterSpec =
                new DatacenterTypes.FilterSpec.Builder().setNames(
                    datacenterNames).build();
        List<DatacenterTypes.Summary> dcSummaries = datacenterService.list(
            dcFilterSpec);

        assert dcSummaries.size() > 0 : "Datacenter with name " + datacenterName
                                        + " not found.";

        return dcSummaries.get(0).getDatacenter();
    }
}
