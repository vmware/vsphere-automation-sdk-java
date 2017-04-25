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

package vmware.samples.sso;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.ws.BindingProvider;

import org.w3c.dom.Element;

import com.vmware.vsphereautomation.lookup.LookupServiceContent;
import com.vmware.vsphereautomation.lookup.LookupServiceRegistrationAttribute;
import com.vmware.vsphereautomation.lookup.LookupServiceRegistrationEndpoint;
import com.vmware.vsphereautomation.lookup.LookupServiceRegistrationEndpointType;
import com.vmware.vsphereautomation.lookup.LookupServiceRegistrationFilter;
import com.vmware.vsphereautomation.lookup.LookupServiceRegistrationInfo;
import com.vmware.vsphereautomation.lookup.LookupServiceRegistrationServiceType;
import com.vmware.vsphereautomation.lookup.LsPortType;
import com.vmware.vsphereautomation.lookup.LsService;
import com.vmware.vsphereautomation.lookup.ManagedObjectReference;
import com.vmware.vsphereautomation.lookup.RuntimeFaultFaultMsg;

import vmware.samples.common.SslUtil;

/**
 * Lookup service helper class. Finds nodes and service end point URLs based on
 * certain filters.
 *
 */
public class LookupServiceHelper {
    private final String lookupServiceUrl;
    private final LsPortType lsPort;

    //Managed object reference of ServiceInstance
    private final ManagedObjectReference serviceInstanceRef;
    private final LookupServiceContent lookupServiceContent;

    // Managed object reference of ServiceRegistration
    private final ManagedObjectReference serviceRegistration;

    public String getLookupServiceUrl() {
        return lookupServiceUrl;
    }

    public LsPortType getLsPort() {
        return lsPort;
    }

    public ManagedObjectReference getServiceInstanceRef() {
        return serviceInstanceRef;
    }

    public LookupServiceContent getLookupServiceContent() {
        return lookupServiceContent;
    }

    public ManagedObjectReference getServiceRegistration() {
        return serviceRegistration;
    }

    public LookupServiceHelper(String lookupServiceUrl)
            throws RuntimeFaultFaultMsg {
        this(lookupServiceUrl, null);
    }

    public LookupServiceHelper(String lookupServiceUrl,
                               Element samlBearerTokenElement)
            throws RuntimeFaultFaultMsg {
        try {
            new URL(lookupServiceUrl);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Malformed lookup service url - " + e);
        }

        this.lookupServiceUrl = lookupServiceUrl;

        /*
         * Note: This method uses https for communication but doesn't verify
         * the certificate from the server. Circumventing SSL is unsafe and
         * should not be used with production code. This is ONLY FOR THE PURPOSE
         * OF DEVELOPMENT ENVIRONMENT.
         */
        SslUtil.trustAllHttpsCertificates();

        // configure lookup service
        serviceInstanceRef = new ManagedObjectReference();
        serviceInstanceRef.setType("LookupServiceInstance");
        serviceInstanceRef.setValue("ServiceInstance");

        LsService lookupService = new LsService();
        lsPort = lookupService.getLsPort();
        ((BindingProvider) lsPort).getRequestContext().put(
            BindingProvider.ENDPOINT_ADDRESS_PROPERTY, lookupServiceUrl);

        lookupServiceContent = lsPort.retrieveServiceContent(
            serviceInstanceRef);
        serviceRegistration = lookupServiceContent.getServiceRegistration();
    }

    /**
     * Find the SSO service endpoint URL. Returns the first available SSO URL in
     * a MxN setup.
     *
     * @return {@link String}
     * @throws RuntimeFaultFaultMsg
     */
    public String findSsoServer() throws RuntimeFaultFaultMsg {
        Map<String, String> result = getServiceEndpointUrl("com.vmware.cis",
            "cs.identity",
            "com.vmware.cis.cs.identity.sso",
            "wsTrust",
            null);
        return result.values().toArray(new String[0])[0];
    }

    /**
     * Find the STS SSO URL using lookup service.
     * In a MxN setup where there are more than one platform service controller,
     * this method returns more than one URL
     *
     * @return {@link Map}
     * @throws RuntimeFaultFaultMsg
     */
    public Map<String, String> findSsoUrls() throws RuntimeFaultFaultMsg {
        return getServiceEndpointUrl("com.vmware.cis",
            "cs.identity",
            "com.vmware.cis.cs.identity.sso",
            "wsTrust",
            null);
    }

    /**
     * Find the SSO service endpoint URL. Returns the first available SSO URL in
     * a MxN setup.
     *
     * @return {@link String}
     * @throws RuntimeFaultFaultMsg
     */
    public String findSsoUrl() throws RuntimeFaultFaultMsg {
        Map<String, String> result = getServiceEndpointUrl("com.vmware.cis",
            "cs.identity",
            "com.vmware.cis.cs.identity.sso",
            "wsTrust",
            null);
        return result.values().toArray(new String[0])[0];
    }

    /**
     * Finds all the vAPI service endpoint URLs
     * In a MxN setup where there are more than one management node; this method
     * returns more than one URL
     *
     * @return {@link Map}
     * @throws RuntimeFaultFaultMsg
     */
    public Map<String, String> findVapiUrls() throws RuntimeFaultFaultMsg {
        return getServiceEndpointUrl("com.vmware.cis",
            "cs.vapi",
            "com.vmware.vapi.endpoint",
            "vapi.json.https.public",
            null);
    }

    /**
     * Finds the vapi service endpoint URL of a management node
     *
     * @param nodeId
     * @return {@link String} or, null
     * @throws RuntimeFaultFaultMsg
     */
    public String findVapiUrl(String nodeId) throws RuntimeFaultFaultMsg {
        Map<String, String> result = getServiceEndpointUrl("com.vmware.cis",
            "cs.vapi",
            "com.vmware.vapi.endpoint",
            "vapi.json.https.public",
            nodeId);
        return result.get(nodeId);
    }

    /**
     * Finds all the vim sdk urls
     * In a MxN setup where there are more than one management node; this method
     * returns more than one URL
     *
     * @return {@link Map}
     * @throws RuntimeFaultFaultMsg
     */
    public Map<String, String> findVimUrls() throws RuntimeFaultFaultMsg {
        return getServiceEndpointUrl("com.vmware.cis",
            "vcenterserver",
            "com.vmware.vim",
            "vmomi",
            null);
    }

    /**
     * Finds the vim sdk service endpoint URL of a management node
     *
     * @param nodeId
     * @return {@link String} or, null
     * @throws RuntimeFaultFaultMsg
     */
    public String findVimUrl(String nodeId) throws RuntimeFaultFaultMsg {
        Map<String, String> result = getServiceEndpointUrl("com.vmware.cis",
            "vcenterserver",
            "com.vmware.vim",
            "vmomi",
            nodeId);
        return result.get(nodeId);
    }

    /**
     * Finds all the vim pbm urls
     * In a MxN setup where there are more than one management node; this method
     * returns more than one URL
     *
     * @return {@link Map}
     * @throws RuntimeFaultFaultMsg
     */
    public Map<String, String> findVimPbmUrls() throws RuntimeFaultFaultMsg {
        return getServiceEndpointUrl("com.vmware.vim.sms",
            "sms",
            "com.vmware.vim.pbm",
            "https",
            null);
    }

    /**
     * Finds the vim pbm service endpoint URL of a management node
     *
     * @param nodeId
     * @return {@link String} or, null
     * @throws RuntimeFaultFaultMsg
     */
    public String findVimPbmUrl(String nodeId) throws RuntimeFaultFaultMsg {
        Map<String, String> result = getServiceEndpointUrl("com.vmware.vim.sms",
            "sms",
            "com.vmware.vim.pbm",
            "https",
            nodeId);
        return result.get(nodeId);
    }

    /**
     * Finds all the management nodes
     *
     * @return {@link Map} Management node instance name as key and node's UUID
     *         as value
     * @throws RuntimeFaultFaultMsg
     */
    public Map<String, String> findMgmtNodes() throws RuntimeFaultFaultMsg {
        LookupServiceRegistrationServiceType filterServiceType =
                new LookupServiceRegistrationServiceType();
        filterServiceType.setProduct("com.vmware.cis");
        filterServiceType.setType("vcenterserver");

        LookupServiceRegistrationEndpointType filterEndpointType =
                new LookupServiceRegistrationEndpointType();
        filterEndpointType.setProtocol("vmomi");
        filterEndpointType.setType("com.vmware.vim");

        LookupServiceRegistrationFilter filterCriteria =
                new LookupServiceRegistrationFilter();
        filterCriteria.setServiceType(filterServiceType);
        filterCriteria.setEndpointType(filterEndpointType);

        Map<String, String> retVal = new HashMap<String, String>();
        List<LookupServiceRegistrationInfo> results = lsPort.list(
            serviceRegistration, filterCriteria);
        for (LookupServiceRegistrationInfo service : results) {
            for (LookupServiceRegistrationAttribute serviceAttr : service
                .getServiceAttributes()) {
                if ("com.vmware.vim.vcenter.instanceName".equals(serviceAttr
                    .getKey())) {
                    retVal.put(serviceAttr.getValue(), service.getNodeId());
                }
            }
        }
        return retVal;
    }

    /**
     * Finds the management node's UUID from its instance name
     *
     * @param instanceName
     * @return {@link String} or, Null
     * @throws RuntimeFaultFaultMsg
     */
    public String getMgmtNodeId(String instanceName)
        throws RuntimeFaultFaultMsg {
        Map<String, String> nodes = findMgmtNodes();
        return nodes.get(instanceName);
    }

    /**
     * Finds the management node's instance name from its UUID
     *
     * @param nodeId
     * @return
     * @throws RuntimeFaultFaultMsg
     */
    public String getMgmtNodeInstanceName(String nodeId)
        throws RuntimeFaultFaultMsg {
        Map<String, String> nodes = findMgmtNodes();
        for (String name : nodes.keySet()) {
            if (nodeId.equals(nodes.get(name))) {
                return name;
            }
        }
        return null;
    }

    public String getDefaultMgmtNode()
        throws RuntimeFaultFaultMsg, MultipleManagementNodeException {
        Map<String, String> nodes = findMgmtNodes();
        if (nodes.size() == 1) {
            return nodes.values().toArray(new String[0])[0];
        } else if (nodes.size() > 1) {
            throw new MultipleManagementNodeException(nodes);
        }
        throw new RuntimeException("No Management Node found");
    }

    /**
     * Find a service endpoint's url based on the product type, service type,
     * endpoint type and endpoint protocol.
     *
     * @param productType
     * @param serviceType
     * @param endpointType
     * @param endpointProtocol
     * @param mgmtNodeId optional management node ID
     * @return map of serviceEndPointURL against the NodeId
     * @throws RuntimeFaultFaultMsg
     */
    private Map<String, String> getServiceEndpointUrl(
        String productType, String serviceType, String endpointType,
        String endpointProtocol, String mgmtNodeId)
        throws RuntimeFaultFaultMsg {
        LookupServiceRegistrationServiceType filterServiceType =
                new LookupServiceRegistrationServiceType();
        filterServiceType.setProduct(productType);
        filterServiceType.setType(serviceType);

        LookupServiceRegistrationEndpointType filterEndpointType =
                new LookupServiceRegistrationEndpointType();
        filterEndpointType.setProtocol(endpointProtocol);
        filterEndpointType.setType(endpointType);

        LookupServiceRegistrationFilter filterCriteria =
                new LookupServiceRegistrationFilter();
        filterCriteria.setServiceType(filterServiceType);
        filterCriteria.setEndpointType(filterEndpointType);
        if (mgmtNodeId != null) {
            filterCriteria.setNodeId(mgmtNodeId);
        }
        Map<String, String> retVal = new HashMap<String, String>();
        List<LookupServiceRegistrationInfo> results = lsPort.list(
            serviceRegistration, filterCriteria);
        for (LookupServiceRegistrationInfo lookupServiceRegistrationInfo
               : results) {
            LookupServiceRegistrationEndpoint
                lookupServiceRegistrationEndpoint =
                    lookupServiceRegistrationInfo.getServiceEndpoints().get(0);
            if (lookupServiceRegistrationEndpoint != null) {
                String nodeId = lookupServiceRegistrationInfo.getNodeId();
                String url = lookupServiceRegistrationEndpoint.getUrl();
                retVal.put(nodeId, url);
            }
        }
        return retVal;
    }

    private class MultipleManagementNodeException extends Exception {

        private static final long serialVersionUID = -6179103331243513328L;
        Map<String, String> nodes;

        public MultipleManagementNodeException(Map<String, String> nodes) {
            this.nodes = nodes;
        }

        @Override
        public String getMessage() {
            String separator = System.getProperty( "line.separator" );
            String message = "Multiple Management Node Found on server";
            for (String name : nodes.keySet()) {
                message += String.format(separator +
                    "Node name: %s uuid: %s", name, nodes.get(name));
            }
            return message;
        }
    }
}
