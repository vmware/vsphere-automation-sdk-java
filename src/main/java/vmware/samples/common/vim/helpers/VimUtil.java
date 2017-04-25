/*
 * *******************************************************
 * Copyright VMware, Inc. 2013, 2016.  All Rights Reserved.
 * SPDX-License-Identifier: MIT
 * *******************************************************
 *
 * DISCLAIMER. THIS PROGRAM IS PROVIDED TO YOU "AS IS" WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, WHETHER ORAL OR WRITTEN,
 * EXPRESS OR IMPLIED. THE AUTHOR SPECIFICALLY DISCLAIMS ANY IMPLIED
 * WARRANTIES OR CONDITIONS OF MERCHANTABILITY, SATISFACTORY QUALITY,
 * NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE.
 */

package vmware.samples.common.vim.helpers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.vmware.vim25.DynamicProperty;
import com.vmware.vim25.InvalidPropertyFaultMsg;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.NotFound;
import com.vmware.vim25.NotFoundFaultMsg;
import com.vmware.vim25.ObjectContent;
import com.vmware.vim25.ObjectSpec;
import com.vmware.vim25.PropertyFilterSpec;
import com.vmware.vim25.PropertySpec;
import com.vmware.vim25.RetrieveOptions;
import com.vmware.vim25.RetrieveResult;
import com.vmware.vim25.RuntimeFaultFaultMsg;
import com.vmware.vim25.SelectionSpec;
import com.vmware.vim25.ServiceContent;
import com.vmware.vim25.TraversalSpec;
import com.vmware.vim25.VimPortType;

/**
 * {@link VimUtil} contains utility methods for the samples to access VIM API's.
 *
 */
public class VimUtil {

    /**
     * Get access to the service content
     *
     * @param vimPortType
     * @return {@link ServiceContent}
     * @throws RuntimeFaultFaultMsg
     */
    public static ServiceContent getServiceContent(VimPortType vimPortType)
            throws RuntimeFaultFaultMsg {
        // get the service content
        ManagedObjectReference serviceInstance = new ManagedObjectReference();
        serviceInstance.setType("ServiceInstance");
        serviceInstance.setValue("ServiceInstance");
        return vimPortType.retrieveServiceContent(serviceInstance);
    }

    /**
     * Retrieves the cluster managed object reference for the specified cluster
     * name using the vim port type.
     *
     * @param vimPortType
     *
     * @param serviceContent
     *            {@link ServiceContent}
     * @param clusterName
     *            name of the cluster to be searched for.
     * @return {@link ManagedObjectReference}
     * @throws InvalidPropertyFaultMsg
     * @throws RuntimeFaultFaultMsg
     * @throws NotFoundFaultMsg
     */
    public static ManagedObjectReference getCluster(VimPortType vimPortType,
            ServiceContent serviceContent, String clusterName)
            throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg,
            NotFoundFaultMsg {

        // Spec to allow recursion on the Folder to Folder traversal
        SelectionSpec folderToFolderSelection = new SelectionSpec();
        folderToFolderSelection.setName("folderToFolder");

        SelectionSpec dcToHostFolderSelection = new SelectionSpec();
        dcToHostFolderSelection.setName("dcToHostFolder");

        // spec to traverse from Datacenter to hostFolder
        TraversalSpec dcToHostFolderTraversal = new TraversalSpec();
        dcToHostFolderTraversal.setName("dcToHostFolder");
        dcToHostFolderTraversal.setPath("hostFolder");
        dcToHostFolderTraversal.setType("Datacenter");
        dcToHostFolderTraversal.getSelectSet().addAll(
                Arrays.asList(new SelectionSpec[] { folderToFolderSelection }));
        dcToHostFolderTraversal.setSkip(false);

        // spec to traverse from Folder to a child folder
        TraversalSpec folderToFolderTraversal = new TraversalSpec();
        folderToFolderTraversal.setName("folderToFolder");
        folderToFolderTraversal.setPath("childEntity");
        folderToFolderTraversal.setType("Folder");
        folderToFolderTraversal.getSelectSet()
                .addAll(Arrays.asList(new SelectionSpec[] {
                    folderToFolderSelection, dcToHostFolderSelection }));
        folderToFolderTraversal.setSkip(false);

        PropertySpec propertySpec = new PropertySpec();
        propertySpec.getPathSet()
                .addAll(Arrays.asList(new String[] { "name" }));
        propertySpec.setType("ClusterComputeResource");

        ObjectSpec objectSpec = new ObjectSpec();
        objectSpec.setObj(serviceContent.getRootFolder());
        objectSpec.getSelectSet().addAll(Arrays.asList(new SelectionSpec[] {
            folderToFolderTraversal, dcToHostFolderTraversal }));
        objectSpec.setSkip(false);

        // Create PropertyFilterSpec using the PropertySpec and ObjectPec
        PropertyFilterSpec propertyFilterSpec = new PropertyFilterSpec();
        propertyFilterSpec.getPropSet()
                .addAll(Arrays.asList(new PropertySpec[] { propertySpec }));
        propertyFilterSpec.getObjectSet()
                .addAll(Arrays.asList(new ObjectSpec[] { objectSpec }));

        ManagedObjectReference morPropertyCollector = serviceContent
                .getPropertyCollector();
        List<ObjectContent> objectContents = vimPortType.retrieveProperties(
                morPropertyCollector,
                Arrays.asList(new PropertyFilterSpec[] { propertyFilterSpec }));

        for (ObjectContent objectContent : objectContents) {
            ManagedObjectReference clusterManagedObjectReference = objectContent
                    .getObj();
            List<DynamicProperty> dynamicProperties = objectContent
                    .getPropSet();
            for (DynamicProperty dynamicProperty : dynamicProperties) {
                if (dynamicProperty.getName().equalsIgnoreCase("name")) {
                    if (dynamicProperty.getVal().toString()
                            .equalsIgnoreCase(clusterName)) {
                        return clusterManagedObjectReference;
                    }
                }
            }
        }
        throw new NotFoundFaultMsg("Cluster Not Found - " + clusterName,
                new NotFound());
    }

    /**
     * Retrieves the list of hosts of the given cluster.
     *
     * @param vimPort
     *            vimPort
     * @param serviceContent
     *            serviceContent
     * @param cluster
     *            cluster
     * @return the list of hosts of the clusters
     * @throws InvalidPropertyFaultMsg
     * @throws RuntimeFaultFaultMsg
     */
    public static List<ManagedObjectReference> getHosts(VimPortType vimPort,
            ServiceContent serviceContent, ManagedObjectReference cluster)
            throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
        PropertySpec hostPropSpec = new PropertySpec();
        hostPropSpec.setType("HostSystem");
        hostPropSpec.setAll(false);
        hostPropSpec.getPathSet().addAll(Collections.<String>emptyList());

        TraversalSpec hostTSpec = new TraversalSpec();
        hostTSpec.setType("ComputeResource");
        hostTSpec.setPath("host");
        hostTSpec.setName("hosts");

        final SelectionSpec selectionSpec = new SelectionSpec();
        selectionSpec.setName(hostTSpec.getName());

        hostTSpec.getSelectSet().add(selectionSpec);

        List<ObjectSpec> ospecList = new ArrayList<>();
        ObjectSpec ospec = new ObjectSpec();
        ospec.setObj(cluster);
        ospec.setSkip(true);
        ospec.getSelectSet().addAll(Arrays.asList(hostTSpec));
        ospecList.add(ospec);

        PropertyFilterSpec propertyFilterSpec = new PropertyFilterSpec();
        propertyFilterSpec.getPropSet().addAll(Arrays.asList(hostPropSpec));
        propertyFilterSpec.getObjectSet().addAll(ospecList);

        List<PropertyFilterSpec> listpfs = new ArrayList<>(1);
        listpfs.add(propertyFilterSpec);
        List<ObjectContent> listObjContent = VimUtil
                .retrievePropertiesAllObjects(vimPort,
                        serviceContent.getPropertyCollector(), listpfs);

        List<ManagedObjectReference> hosts = new ArrayList<>();

        if (listObjContent != null) {
            for (ObjectContent oc : listObjContent) {
                hosts.add(oc.getObj());
            }
        }
        return hosts;
    }

    /**
     * Retrieves the vm managed object reference for the specified vm name using
     * the vim port type.
     *
     * @param vimPortType
     * @param serviceContent
     * @param vmname
     * @return
     * @throws NotFoundFaultMsg
     * @throws RuntimeFaultFaultMsg
     * @throws InvalidPropertyFaultMsg
     */
    public static ManagedObjectReference getVM(VimPortType vimPortType,
            ServiceContent serviceContent, String vmname)
            throws NotFoundFaultMsg, InvalidPropertyFaultMsg,
            RuntimeFaultFaultMsg {
        ManagedObjectReference propCollectorRef = serviceContent
                .getPropertyCollector();
        ManagedObjectReference rootFolderRef = serviceContent.getRootFolder();

        ManagedObjectReference retVmRef = null;
        TraversalSpec tSpec = getVMTraversalSpec();

        PropertySpec propertySpec = new PropertySpec();
        propertySpec.setAll(Boolean.FALSE);
        propertySpec.getPathSet().add("name");
        propertySpec.setType("VirtualMachine");

        ObjectSpec objectSpec = new ObjectSpec();
        objectSpec.setObj(rootFolderRef);
        objectSpec.setSkip(Boolean.TRUE);
        objectSpec.getSelectSet().add(tSpec);

        PropertyFilterSpec propertyFilterSpec = new PropertyFilterSpec();
        propertyFilterSpec.getPropSet().add(propertySpec);
        propertyFilterSpec.getObjectSet().add(objectSpec);

        List<PropertyFilterSpec> listpfs = new ArrayList<PropertyFilterSpec>(1);
        listpfs.add(propertyFilterSpec);
        List<ObjectContent> listobjcont = retrievePropertiesAllObjects(
                vimPortType, propCollectorRef, listpfs);

        if (listobjcont != null) {
            for (ObjectContent oc : listobjcont) {
                ManagedObjectReference mr = oc.getObj();
                String vmnm = null;
                List<DynamicProperty> dps = oc.getPropSet();
                if (dps != null) {
                    for (DynamicProperty dp : dps) {
                        vmnm = (String) dp.getVal();
                    }
                }
                if (vmnm != null && vmnm.equals(vmname)) {
                    retVmRef = mr;
                    break;
                }
            }
        }
        if (retVmRef == null) {
            throw new NotFoundFaultMsg("VM Not Found - " + vmname,
                    new NotFound());
        }
        return retVmRef;
    }

    /**
     * Gets the VM TraversalSpec.
     *
     * @return the VM TraversalSpec
     */
    private static TraversalSpec getVMTraversalSpec() {
        TraversalSpec vAppToVM = new TraversalSpec();
        vAppToVM.setName("vAppToVM");
        vAppToVM.setType("VirtualApp");
        vAppToVM.setPath("vm");

        TraversalSpec vAppToVApp = new TraversalSpec();
        vAppToVApp.setName("vAppToVApp");
        vAppToVApp.setType("VirtualApp");
        vAppToVApp.setPath("resourcePool");

        SelectionSpec vAppRecursion = new SelectionSpec();
        vAppRecursion.setName("vAppToVApp");

        SelectionSpec vmInVApp = new SelectionSpec();
        vmInVApp.setName("vAppToVM");

        List<SelectionSpec> vAppToVMSS = new ArrayList<SelectionSpec>();
        vAppToVMSS.add(vAppRecursion);
        vAppToVMSS.add(vmInVApp);
        vAppToVApp.getSelectSet().addAll(vAppToVMSS);

        SelectionSpec sSpec = new SelectionSpec();
        sSpec.setName("VisitFolders");

        TraversalSpec dataCenterToVMFolder = new TraversalSpec();
        dataCenterToVMFolder.setName("DataCenterToVMFolder");
        dataCenterToVMFolder.setType("Datacenter");
        dataCenterToVMFolder.setPath("vmFolder");
        dataCenterToVMFolder.setSkip(false);
        dataCenterToVMFolder.getSelectSet().add(sSpec);

        TraversalSpec traversalSpec = new TraversalSpec();
        traversalSpec.setName("VisitFolders");
        traversalSpec.setType("Folder");
        traversalSpec.setPath("childEntity");
        traversalSpec.setSkip(false);
        List<SelectionSpec> sSpecArr = new ArrayList<SelectionSpec>();
        sSpecArr.add(sSpec);
        sSpecArr.add(dataCenterToVMFolder);
        sSpecArr.add(vAppToVM);
        sSpecArr.add(vAppToVApp);
        traversalSpec.getSelectSet().addAll(sSpecArr);
        return traversalSpec;
    }

    /**
     * Uses the new RetrievePropertiesEx method to emulate the now deprecated
     * RetrieveProperties method.
     *
     * @param listpfs
     * @return list of object content
     * @throws Exception
     */
    public static List<ObjectContent> retrievePropertiesAllObjects(
            VimPortType vimPort, ManagedObjectReference propCollectorRef,
            List<PropertyFilterSpec> listpfs)
            throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
        RetrieveOptions propObjectRetrieveOpts = new RetrieveOptions();
        List<ObjectContent> listobjcontent = new ArrayList<ObjectContent>();

        RetrieveResult rslts = vimPort.retrievePropertiesEx(propCollectorRef,
                listpfs, propObjectRetrieveOpts);
        if (rslts != null && rslts.getObjects() != null
                && !rslts.getObjects().isEmpty()) {
            listobjcontent.addAll(rslts.getObjects());
        }
        String token = null;
        if (rslts != null && rslts.getToken() != null) {
            token = rslts.getToken();
        }
        while (token != null && !token.isEmpty()) {
            rslts = vimPort.continueRetrievePropertiesEx(propCollectorRef,
                    token);
            token = null;
            if (rslts != null) {
                token = rslts.getToken();
                if (rslts.getObjects() != null
                        && !rslts.getObjects().isEmpty()) {
                    listobjcontent.addAll(rslts.getObjects());
                }
            }
        }

        return listobjcontent;
    }

    /**
     * Getting the MOREF of the entity.
     */
    public static ManagedObjectReference getEntityByName(
            VimPortType vimPortType, ServiceContent serviceContent,
            String entityName, String entityType) {
        ManagedObjectReference propCollectorRef = serviceContent
                .getPropertyCollector();
        ManagedObjectReference rootFolderRef = serviceContent.getRootFolder();
        ManagedObjectReference retVal = null;

        try {
            // Create Property Spec
            PropertySpec propertySpec = new PropertySpec();
            propertySpec.setAll(Boolean.FALSE);
            propertySpec.setType(entityType);
            propertySpec.getPathSet().add("name");

            // Now create Object Spec
            ObjectSpec objectSpec = new ObjectSpec();
            objectSpec.setObj(rootFolderRef);
            objectSpec.setSkip(Boolean.TRUE);
            objectSpec.getSelectSet().addAll(Arrays.asList(buildTraversal()));

            // Create PropertyFilterSpec using the PropertySpec and ObjectPec
            // created above.
            PropertyFilterSpec propertyFilterSpec = new PropertyFilterSpec();
            propertyFilterSpec.getPropSet().add(propertySpec);
            propertyFilterSpec.getObjectSet().add(objectSpec);

            List<PropertyFilterSpec> listpfs =
                    new ArrayList<PropertyFilterSpec>(1);
            listpfs.add(propertyFilterSpec);
            List<ObjectContent> listobjcont = retrievePropertiesAllObjects(
                    vimPortType, propCollectorRef, listpfs);
            if (listobjcont != null) {
                for (ObjectContent oc : listobjcont) {
                    if (oc.getPropSet().get(0).getVal().equals(entityName)) {
                        retVal = oc.getObj();
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return retVal;
    }

    /**
     * Get the required properties of the specified object.
     *
     * @param vimPort
     * @param serviceContent
     * @param moRef
     * @param type
     * @param properties
     * @return
     * @throws RuntimeFaultFaultMsg
     * @throws InvalidPropertyFaultMsg
     */
    public static List<DynamicProperty> getProperties(VimPortType vimPort,
            ServiceContent serviceContent, ManagedObjectReference moRef,
            String type, List<String> properties)
            throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
        // Create Property Spec
        PropertySpec propertySpec = new PropertySpec();
        propertySpec.setAll(false);
        propertySpec.setType(type);
        propertySpec.getPathSet().addAll(properties);

        // Now create Object Spec
        ObjectSpec objectSpec = new ObjectSpec();
        objectSpec.setObj(moRef);
        objectSpec.setSkip(false);

        // Create PropertyFilterSpec using the PropertySpec and ObjectPec
        // created above.
        PropertyFilterSpec propertyFilterSpec = new PropertyFilterSpec();
        propertyFilterSpec.getPropSet().add(propertySpec);
        propertyFilterSpec.getObjectSet().add(objectSpec);

        List<PropertyFilterSpec> listpfs = new ArrayList<PropertyFilterSpec>(1);
        listpfs.add(propertyFilterSpec);
        List<ObjectContent> listobjcontent = VimUtil
                .retrievePropertiesAllObjects(vimPort,
                        serviceContent.getPropertyCollector(), listpfs);
        assert listobjcontent != null && listobjcontent.size() > 0;
        ObjectContent contentObj = listobjcontent.get(0);
        List<DynamicProperty> objList = contentObj.getPropSet();
        return objList;
    }

    /**
     * @return An array of SelectionSpec covering Datacenter to DatastoreFolder.
     */
    private static SelectionSpec[] buildTraversal() {

        // For Folder -> Folder recursion
        SelectionSpec sspecvfolders = new SelectionSpec();
        sspecvfolders.setName("VisitFolders");

        TraversalSpec dcToDf = new TraversalSpec();
        dcToDf.setType("Datacenter");
        dcToDf.setSkip(Boolean.FALSE);
        dcToDf.setPath("datastoreFolder");
        dcToDf.setName("dcToDf");
        dcToDf.getSelectSet().add(sspecvfolders);

        // DC -> DS
        TraversalSpec dcToDs = new TraversalSpec();
        dcToDs.setType("Datacenter");
        dcToDs.setPath("datastore");
        dcToDs.setName("dcToDs");
        dcToDs.setSkip(Boolean.FALSE);

        TraversalSpec visitFolders = new TraversalSpec();
        visitFolders.setType("Folder");
        visitFolders.setPath("childEntity");
        visitFolders.setSkip(Boolean.FALSE);
        visitFolders.setName("VisitFolders");

        List<SelectionSpec> sspecarrvf = new ArrayList<SelectionSpec>();
        sspecarrvf.add(dcToDs);
        sspecarrvf.add(dcToDf);
        sspecarrvf.add(sspecvfolders);

        visitFolders.getSelectSet().addAll(sspecarrvf);
        return new SelectionSpec[] { visitFolders };
    }

    /**
     * Deletes a managed object and waits for the delete operation to complete
     * 
     * @param vimPort
     * @param serviceContent
     * @param mor
     */
    public static boolean deleteManagedEntity(VimPortType vimPort,
            ServiceContent serviceContent, ManagedObjectReference mor) {
        WaitForValues waitForValues = new WaitForValues(vimPort,
                serviceContent);
        System.out.println("Deleting : [" + mor.getValue() + "]");
        try {
            ManagedObjectReference taskmor = vimPort.destroyTask(mor);
            if (waitForValues.getTaskResultAfterDone(taskmor)) {
                System.out.println("Successful delete of Managed Entity - ["
                        + mor.getValue() + "]" + " and Entity Type - ["
                        + mor.getType() + "]");
                return true;
            } else {
                System.out
                        .println("Unable to delete : [" + mor.getValue() + "]");
                return false;
            }
        } catch (Exception e) {
            System.out.println("Unable to delete : [" + mor.getValue() + "]");
            System.out.println("Reason :" + e.getLocalizedMessage());
            return false;
        }
    }

    public static boolean createSnapshot(VimPortType vimPort,
            ServiceContent serviceContent, ManagedObjectReference vmMor,
            String snapshotname, String description) {
        WaitForValues waitForValues = new WaitForValues(vimPort,
                serviceContent);
        System.out.println("Taking snapshot : [" + snapshotname + "]");
        try {
            ManagedObjectReference taskMor = vimPort.createSnapshotTask(vmMor,
                    snapshotname, description, false, false);
            if (waitForValues.getTaskResultAfterDone(taskMor)) {
                System.out.println("Snapshot - [" + snapshotname
                        + "] Creation Successful");
                return true;
            } else {
                System.out.println(
                        "Snapshot - [" + snapshotname + "] Creation Failed");
                return false;
            }
        } catch (Exception e) {
            System.out.println(
                    "Snapshot - [" + snapshotname + "] Creation Failed");
            System.out.println("Reason :" + e.getLocalizedMessage());
            return false;
        }
    }

}
