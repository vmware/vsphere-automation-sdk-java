/*
 * *******************************************************
 * Copyright VMware, Inc. 2016, 2020.  All Rights Reserved.
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

import com.vmware.vapi.bindings.StubConfiguration;
import com.vmware.vapi.bindings.StubFactory;
import com.vmware.vcenter.VMTypes;

public class PlacementHelper {

    /**
     * Returns a VM placement spec for a cluster. Ensures that the
     * cluster, resource pool, vm folder and datastore are all in the same
     * datacenter which is specified.
     *
     * Note: The method assumes that there is only one of each resource type
     * (i.e. datacenter, resource pool, cluster, folder, datastore) with the
     * mentioned names.
     *
     * @param stubFactory Stub factory for the api endpoint
     * @param sessionStubConfig stub configuration for the current session
     * @param datacenterName name of the datacenter for the placement spec
     * @param resourcePoolName name of the resource pool for the placement spec
     * @param clusterName name of the cluster for the placement spec
     * @param vmFolderName name of the vm folder for the placement spec
     * @param datastoreName name of the datastore for the placement spec
     * @return a VM placement spec for the specified cluster
     */
    public static VMTypes.PlacementSpec getPlacementSpecForCluster(
        StubFactory stubFactory, StubConfiguration sessionStubConfig,
        String datacenterName, String clusterName,
        String vmFolderName, String datastoreName) {

        String clusterId =
                ClusterHelper.getCluster(stubFactory,
                    sessionStubConfig,
                    datacenterName,
                    clusterName);
        System.out.println("Selecting cluster " + clusterName + "(id="
                           + clusterId + ")");

        String vmFolderId =
                FolderHelper.getFolder(stubFactory,
                    sessionStubConfig,
                    datacenterName,
                    vmFolderName);
        System.out.println("Selecting folder " + vmFolderName + "id=("
                           + vmFolderId + ")");

        String datastoreId =
                DatastoreHelper.getDatastore(stubFactory,
                    sessionStubConfig,
                    datacenterName,
                    datastoreName);
        System.out.println("Selecting datastore " + datastoreName + "(id="
                           + datastoreId + ")");

        /*
         *  Create the vm placement spec with the datastore, resource pool,
         *  cluster and vm folder
         */
        VMTypes.PlacementSpec vmPlacementSpec = new VMTypes.PlacementSpec();
        vmPlacementSpec.setDatastore(datastoreId);
        vmPlacementSpec.setCluster(clusterId);
        vmPlacementSpec.setFolder(vmFolderId);

        return vmPlacementSpec;
    }

    /**
     * Returns a VM placement spec for a Host. Ensures that the
     * vm folder, host, cluster and datastore are all in the same
     * datacenter which is specified.
     *
     * Note: The method assumes that there is only one of each resource type
     * (i.e. datacenter, cluster, host, folder and datastore) with the
     * mentioned names.
     *
     * @param stubFactory Stub factory for the api endpoint
     * @param sessionStubConfig stub configuration for the current session
     * @param hostName Name/Ip Address of the host.
     * @param clusterName  name of the cluster for the placement spec
     * @param datacenterName name of the datacenter for the placement spec
     * @param vmFolderName name of the vm folder for the placement spec
     * @param datastoreName name of the datastore for the placement spec
     * @return a VM placement spec for specified Datacenter
     */
    public static VMTypes.PlacementSpec getVMPlacementSpec(
        StubFactory stubFactory, StubConfiguration sessionStubConfig, String hostName,
        String clusterName, String datacenterName, String vmFolderName, String datastoreName) {
        /*
         *  Create the vm placement spec with the datastore, host
         *  and vm folder
         */
        VMTypes.PlacementSpec vmPlacementSpec = new VMTypes.PlacementSpec();
        String vmFolderId =
                FolderHelper.getFolder(stubFactory,
                    sessionStubConfig,
                    datacenterName,
                    vmFolderName);
        System.out.println("Selecting folder " + vmFolderName + "id=("
                           + vmFolderId + ")");

        String datastoreId =
                DatastoreHelper.getDatastore(stubFactory,
                    sessionStubConfig,
                    datacenterName,
                    datastoreName);
        System.out.println("Selecting datastore " + datastoreName + "(id="
                           + datastoreId + ")");

        if(null != clusterName) {
           String clusterId =
               ClusterHelper.getCluster(stubFactory,
                    sessionStubConfig,
                    datacenterName,
                    clusterName);
           System.out.println("Selecting cluster " + clusterName + "(id="
                           + clusterId + ")");
           vmPlacementSpec.setCluster(clusterId);
        }

        String hostID =
                HostHelper.getHost(stubFactory,
                    sessionStubConfig,
                    datacenterName,
                    clusterName,
                    hostName);
        System.out.println("Selecting Host (id="
                           + hostID + ")");
        vmPlacementSpec.setHost(hostID);
        vmPlacementSpec.setDatastore(datastoreId);
        vmPlacementSpec.setFolder(vmFolderId);

        return vmPlacementSpec;
    }
}
