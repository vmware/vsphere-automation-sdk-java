/*
 * *******************************************************
 * Copyright VMware, Inc. 2023.  All Rights Reserved.
 * SPDX-License-Identifier: MIT
 * *******************************************************
 *
 * DISCLAIMER. THIS PROGRAM IS PROVIDED TO YOU "AS IS" WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, WHETHER ORAL OR WRITTEN,
 * EXPRESS OR IMPLIED. THE AUTHOR SPECIFICALLY DISCLAIMS ANY IMPLIED
 * WARRANTIES OR CONDITIONS OF MERCHANTABILITY, SATISFACTORY QUALITY,
 * NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE.
 */
package vmware.samples.vcenter.transcoder;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.HttpCookie;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.MessageContext;

import org.apache.commons.cli.Option;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.vmware.vapi.bindings.DynamicStructure;
import com.vmware.vapi.bindings.DynamicStructureImpl;
import com.vmware.vapi.data.DataValue;
import com.vmware.vapi.data.StructValue;
import com.vmware.vapi.protocol.common.json.JsonConverter;
import com.vmware.vim25.VirtualMachineConfigSpec;
import com.vmware.vim25.VirtualMachineFileInfo;

import vmware.samples.common.SamplesAbstractBase;
import vmware.samples.common.TrustAllTrustManager;

/**
 * Demonstrates conversion of data objects from their XML
 * representation, present in SOAP based runtimes, to their JSON
 * representation, present in JSON based runtimes, and vice versa.
 * Conversion is done via the transcoder API, introduced in version
 * `8.0.2.0`.
 *
 * The current sample utilizes a `vim.vm.ConfigSpec` managed object,
 * present in bindings of both runtimes.
 *
 *
 * Author: VMware, Inc.
 * Sample Prerequisites: 
 *  - vCenter 8.0U2+
 *  - JDK8+
 */
public class TranscodeVmConfig extends SamplesAbstractBase {
    
    private String version;

    private String vimCookie;

    private CloseableHttpClient transcoderClient;
    
    // JSON based runtime serializer/deserializer
    private JsonConverter jsonConverter = new JsonConverter();

    private void acquireVimCookie() {
        TreeMap<String, List<String>> headers = (TreeMap<String, List<String>>)
                ((BindingProvider) this.vimAuthHelper.getVimPort()).getResponseContext()
                        .get(MessageContext.HTTP_RESPONSE_HEADERS);
        
        vimCookie = HttpCookie.parse(headers.get("set-cookie").get(0)).get(0).getValue();
    }
    
    protected void login() throws Exception {
        super.login();
        acquireVimCookie();
    }
    
    protected void parseArgs(String[] args) {
    	List<Option> optionList = Collections.<Option>emptyList();
        super.parseArgs(optionList, args);
    }

    // Not recommended for production use
    protected void setupNoSsl() throws Exception {
        // Add permissive TrustManager
        javax.net.ssl.TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[1];
        javax.net.ssl.TrustManager tm = new TrustAllTrustManager();
        trustAllCerts[0] = tm;
        // Create the SSL context
        SSLContext sc = javax.net.ssl.SSLContext.getInstance("SSL");

        // Create the session context
        javax.net.ssl.SSLSessionContext sslsc = sc.getServerSessionContext();

        // Initialize the contexts; the session context takes the trust manager.
        sslsc.setSessionTimeout(0);
        sc.init(null, trustAllCerts, null);

        HttpsURLConnection.getDefaultHostnameVerifier();

        transcoderClient = HttpClients.custom()
                // Set via super class when `skipServerVerification` is enabled
                .setSSLHostnameVerifier(HttpsURLConnection.getDefaultHostnameVerifier())
                .setSSLContext(sc).build();
    }
    
    /**
     * Setup the transcoder API client. 
     * 
     * If "skip-server-verification" is specified, then the server certificate
     * verification is skipped. An all-trusting {@code HttpClient} is configured.
     * If "skip-server-verification" is NOT specified, the default {@code HttpClient}
     * provided by {@code HttpClients.createDefault()} is utilized.
     */
    protected void setup() throws Exception {
        if(this.skipServerVerification) {
            setupNoSsl();
        } else {
            transcoderClient = HttpClients.createDefault();
        }
    }
    
    /**
     * Invokes the System::Hello API, responsible for negotiating common
     * parameters for API communication. The implementation selects mutually
     * supported version from the choices passed in the request body.
     */
    private void negotiateVersion(List<String> clientDesiredVersions)
            throws Exception {
        final HttpPost req = new HttpPost("https://" + this.server
                                          + "/api/vcenter/system?action=hello");
        
        req.addHeader("Content-Type", "application/json");
        
        // Alternatively add org.json dependency or equivalent library
        StringBuilder jsonBodyBuilder = new StringBuilder();
        jsonBodyBuilder.append("{\"api_releases\": [");
        for(int i=0; i<clientDesiredVersions.size(); i++) {
            if (i!=0) 
                jsonBodyBuilder.append(",");
            jsonBodyBuilder.append(String.format("\"%s\"", clientDesiredVersions.get(i)));
        }
        jsonBodyBuilder.append("]}");
        
        req.setEntity(new StringEntity(jsonBodyBuilder.toString(), StandardCharsets.UTF_8));
        try (CloseableHttpResponse response = transcoderClient.execute(req)) {
            String output = EntityUtils.toString(response.getEntity());
            // Custom error prone version extraction
            // Utilize org.json or equivalent for JSON handling
            version = output.split(":")[1].replaceAll("\"|}", "");
            System.out.println(version);
        }
        
    }

    /**
     * Transcodes and validates the integrity of a JSON or XML serialized data
     * object.
     * 
     * Transcoding is available from JSON or XML to JSON or XML for both cases.
     * 
     * Transcoding to different encoding types is useful when utilizing the same
     * data objects in a program involving SOAP and JSON based stacks/bindings.
     */
    private String transcode(String data, boolean toJson)
            throws Exception {
        final HttpPost req = new HttpPost("https://" + this.server
                                          + "/sdk/vim25/" + version
                                          + "/transcoder");
        req.addHeader("Content-Type", toJson ? "application/xml" : "application/json");
        req.addHeader("Accept", toJson ? "application/json" : "application/xml");
        req.addHeader("vmware-api-session-id", vimCookie);
        req.setEntity(new StringEntity(data, StandardCharsets.UTF_8));

        try (CloseableHttpResponse response = transcoderClient.execute(req)) {
            String output = EntityUtils.toString(response.getEntity());
            return output;
        }
    }
    
    /**
     * 
     * Creates a `vim.vm.ConfigSpec` data object with arbitrarily populated
     * fields.
     */
    private static VirtualMachineConfigSpec createVmConfigSpec() {
        VirtualMachineConfigSpec vmConfigSpec = new VirtualMachineConfigSpec();
        vmConfigSpec.setName("test-vm");
        vmConfigSpec.setMemoryMB(4L);
        vmConfigSpec.setGuestId("guest");
        vmConfigSpec.setAnnotation("Sample");
        vmConfigSpec.setNumCPUs(1);
        VirtualMachineFileInfo files = new VirtualMachineFileInfo();
        files.setVmPathName("[datastore1]");
        vmConfigSpec.setFiles(files);

        return vmConfigSpec;
    }
    
    private static String serializeToXml(Object managedObj) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(managedObj.getClass());
        Marshaller marshaller = context.createMarshaller();

        // To format XML
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

        //If we DO NOT have JAXB annotated class
        JAXBElement<Object> jaxbElement = new JAXBElement<>(new QName("urn:vim25", "obj"), Object.class, managedObj);

        StringWriter writter = new StringWriter();

        marshaller.marshal(jaxbElement, writter);

        return writter.toString();
    }
    
    private static <T> T deserializeFromXml(String xml, Class<T> clazz) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(clazz);
        Source source = new StreamSource(new ByteArrayInputStream(xml.getBytes()));
        JAXBElement<T> root = context.createUnmarshaller().unmarshal(source, clazz);
        return root.getValue();
    }
    
    private DynamicStructure convertSoapObjToDynamicStruct(Object soapObj) throws Exception {
        // Serialize SOAP object to XML
        String xml = serializeToXml(soapObj);

        // Transcode XML to JSON
        String json = transcode(xml, true);
        System.out.println(json);

        // Deserialize JSON to a StructValue
        byte[] buf = json.getBytes(StandardCharsets.UTF_8);
        InputStream input = new ByteArrayInputStream(buf);
        DataValue dataValue = jsonConverter.toDataValue(input);
        // Convert StructValue to DynamicStructure
        DynamicStructure struct = new DynamicStructureImpl((StructValue) dataValue);
        System.out.println(struct.getClass().getName());
        
        return struct;
    }
    
    /**
     * Converts a DynamicStructure to its equivalent SOAP type {@code T}.
     * 
     * Passing an invalid type results in XML conversion errors.
     */
    private <T> void  convertDynamicStructToSoapObj(DynamicStructure struct, Class<T> clazz) throws Exception {
        // Serialize DynamicStructure into JSON
        String json = jsonConverter.fromDataValue(struct._getDataValue());
        
        // Transcode JSON to XML
        String xml = transcode(json, false);
        System.out.println(xml);
        
        T soapObj = deserializeFromXml(xml, clazz);
        System.out.println(soapObj.getClass().getName());
    }

    protected void run() throws Exception {
        // Negotiating API release is necessary to use in APIs
        // utilizing inheritance based polymorphism - such as transcoder API.
        // Desired version is '8.0.2.0'
        negotiateVersion(Arrays.asList("8.0.2.0"));
        
        // Create SOAP vim.vm.ConfigSpec obj
        VirtualMachineConfigSpec vmConfigSpec = createVmConfigSpec();
        
        DynamicStructure struct = convertSoapObjToDynamicStruct(vmConfigSpec);
        // Demonstrate conversion in the other direction
        convertDynamicStructToSoapObj(struct, VirtualMachineConfigSpec.class);
    }
    
    protected void cleanup() throws Exception {
        transcoderClient.close();
    }
    
    public static void main(String[] args) throws Exception {
        /*
         * Execute the sample using the command line arguments or parameters
         * from the configuration file. This executes the following steps:
         * 1. Parse the arguments required by the sample
         * 2. Login to the server
         * 3. Setup any resources required by the sample run
         * 4. Run the sample
         * 5. Cleanup any data created by the sample run, if cleanup=true
         * 6. Logout of the server
         */
        new TranscodeVmConfig().execute(args);
    }
}
