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

import com.vmware.content.library.StorageBacking;
import com.vmware.vapi.bindings.StubConfiguration;
import com.vmware.vapi.bindings.StubFactory;
import com.vmware.vcenter.Datastore;
import com.vmware.vcenter.DatastoreTypes;

import vmware.samples.common.authentication.VapiAuthenticationHelper;

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
        // Get the datastore
        Datastore datastoreService = stubFactory.createStub(Datastore.class,
            sessionStubConfig);
        Set<String> datastores = Collections.singleton(datastoreName);
        List<DatastoreTypes.Summary> datastoreSummaries = null;
        DatastoreTypes.FilterSpec datastoreFilterSpec = null;
        if(null != datacenterName) {
            // Get the datacenter
            Set<String> datacenters = Collections.singleton(DatacenterHelper
                .getDatacenter(stubFactory, sessionStubConfig, datacenterName));
            datastoreFilterSpec =
                  new DatastoreTypes.FilterSpec.Builder().setNames(datastores)
                      .setDatacenters(datacenters)
                      .build();
            datastoreSummaries = datastoreService.list(
                 datastoreFilterSpec);
            assert datastoreSummaries.size() > 0 : "Datastore " + datastoreName
                                                 + "not found in datacenter : "
                                                 + datacenterName;
        }else {
            datastoreFilterSpec =
               new DatastoreTypes.FilterSpec.Builder().setNames(datastores)
                        .build();
            datastoreSummaries = datastoreService.list(datastoreFilterSpec);
                assert datastoreSummaries.size() > 0 : 
                       "Datastore " + datastoreName+ " not found";
        }
        return datastoreSummaries.get(
                datastoreSummaries.size()-1).getDatastore();

    }

    public static String getDatastore(
            StubFactory stubFactory, StubConfiguration sessionStubConfig,
            String datastoreName) {
        return getDatastore(stubFactory, sessionStubConfig,
                       null, datastoreName);
    }

    /**
     * Creates a datastore storage backing.
     *
     * @return the storage backing
     */
    public static StorageBacking createStorageBacking(
            VapiAuthenticationHelper vapiAuthHelper,
            StubConfiguration sessionStubConfig, String dsName ) {
        String dsId = getDatastore(vapiAuthHelper.getStubFactory(),
               sessionStubConfig, dsName);

        //Build the storage backing with the datastore Id
        StorageBacking storageBacking = new StorageBacking();
        storageBacking.setType(StorageBacking.Type.DATASTORE);
        storageBacking.setDatastoreId(dsId);
        return storageBacking;
    }

}