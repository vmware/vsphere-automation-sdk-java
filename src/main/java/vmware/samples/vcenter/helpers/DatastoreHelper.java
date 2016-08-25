/*
 * *******************************************************
 * Copyright VMware, Inc. 2016.  All Rights Reserved.
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
import com.vmware.vcenter.Datastore;
import com.vmware.vcenter.DatastoreTypes;

public class DatastoreHelper {

    /**
     * Returns the identifier of a datastore
     *
     * Note: The method assumes that there is only one datastore and datacenter
     * with the mentioned names.
     *
     * @param stubFactory Stub factory for the api endpoint
     * @param sessionStubConfig stub configuration for the current session
     * @param datacenterName name of the datacenter for the placement spec
     * @param datastoreName name of the datastore for the placement spec
     * @return identifier of a datastore
     */
    public static String getDatastore(
        StubFactory stubFactory, StubConfiguration sessionStubConfig,
        String datacenterName, String datastoreName) {

        // Get the datacenter
        Set<String> datacenters = Collections.singleton(DatacenterHelper
            .getDatacenter(stubFactory, sessionStubConfig, datacenterName));

        // Get the datastore
        Datastore datastoreService = stubFactory.createStub(Datastore.class,
            sessionStubConfig);
        Set<String> datastores = Collections.singleton(datastoreName);
        DatastoreTypes.FilterSpec datastoreFilterSpec =
                new DatastoreTypes.FilterSpec.Builder().setNames(datastores)
                    .setDatacenters(datacenters)
                    .build();
        List<DatastoreTypes.Summary> datastoreSummaries = datastoreService.list(
            datastoreFilterSpec);
        assert datastoreSummaries.size() > 0 : "Datastore " + datastoreName
                                               + "not found in datacenter : "
                                               + datacenterName;
        return datastoreSummaries.get(0).getDatastore();
    }
}
