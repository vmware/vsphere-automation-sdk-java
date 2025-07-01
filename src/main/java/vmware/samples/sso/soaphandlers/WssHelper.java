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
package vmware.samples.sso.soaphandlers;

import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.oasis_open.docs.wss._2004._01.oasis_200401_wss_wssecurity_secext_1_0.ObjectFactory;
import org.oasis_open.docs.wss._2004._01.oasis_200401_wss_wssecurity_secext_1_0.SecurityHeaderType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class WssHelper {

    static final ObjectFactory wsseObjFactory = new ObjectFactory();

    /**
     * Retrieves the specified property of the Node, and returns its String
     * value
     *
     * @param node
     * @param propertyName
     * @return
     */
    public static String getNodeProperty(Node node, String propertyName) {
        return node.getAttributes().getNamedItem(propertyName).getNodeValue();
    }

    /**
     * Finds the Security element from the header. If not found then creates one
     * and returns the same
     *
     * @param header
     * @return
     */
    public static Node getSecurityElement(SOAPHeader header) {
        NodeList targetElement = header.getElementsByTagNameNS(Constants.WSS_NS,
            Constants.SECURITY_ELEMENT_NAME);
        if (targetElement == null || targetElement.getLength() == 0) {
            JAXBElement<SecurityHeaderType> value = wsseObjFactory
                .createSecurity(wsseObjFactory.createSecurityHeaderType());
            Node headerNode = marshallJaxbElement(value).getDocumentElement();
            return header.appendChild(header.getOwnerDocument().importNode(
                headerNode, true));
        } else if (targetElement.getLength() > 1) {
            throw new RuntimeException(Constants.ERR_INSERTING_SECURITY_HEADER);
        }
        return targetElement.item(0);
    }

    /**
     * Returns the header. If not present then adds one and return the same
     *
     * @param smc
     * @return
     * @throws SOAPException
     */
    public static SOAPHeader getSOAPHeader(SOAPMessageContext smc)
        throws SOAPException {
        return smc.getMessage().getSOAPPart().getEnvelope().getHeader() == null
                ? smc.getMessage().getSOAPPart().getEnvelope().addHeader()
                : smc.getMessage().getSOAPPart().getEnvelope().getHeader();
    }

    /**
     * Performs an elementary test to check if the Node possibly represents a
     * Holder-Of-Key SAML token.
     *
     * @param token
     * @return
     */
    public static boolean isHoKToken(Node token) {
        if (isSamlToken(token)) {
            NodeList elements = ((Element) token).getElementsByTagNameNS(
                Constants.URN_OASIS_NAMES_TC_SAML_2_0_ASSERTION,
                Constants.SUBJECT_CONFIRMATION);
            if (elements.getLength() != 1) {
                throw new IllegalArgumentException(
                    Constants.ERR_NOT_A_SAML_TOKEN);
            }
            Node value = elements.item(0).getAttributes().getNamedItem(
                Constants.METHOD);
            return Constants.URN_OASIS_NAMES_TC_SAML_2_0_CM_HOLDER_OF_KEY
                .equalsIgnoreCase(value.getNodeValue());
        }
        throw new RuntimeException("The Node does not represnt a SAML token");
    }

    /**
     * Returns true if the {@link SOAPMessageContext} is part of the request
     *
     * @param smc
     * @return
     */
    public static boolean isOutgoingMessage(SOAPMessageContext smc) {
        Boolean outboundProperty = (Boolean) smc.get(
            MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        return outboundProperty.booleanValue();
    }

    /**
     * Performs an elementary test to check if the Node possibly represents a
     * SAML token.
     *
     * @param token
     * @return
     */
    public static boolean isSamlToken(Node token) {
        boolean isValid = false;
        isValid = (Constants.URN_OASIS_NAMES_TC_SAML_2_0_ASSERTION
            .equalsIgnoreCase(token.getNamespaceURI())) && ("assertion"
                .equalsIgnoreCase(token.getLocalName()));
        return isValid;
    }

    /**
     * Marshall a jaxbElement into a Document
     *
     * @param jaxbElement
     * @return Document
     */
    public static final <T> Document marshallJaxbElement(
            JAXBElement<T> jaxbElement) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        Document result = null;
        try {
            JAXBContext jaxbContext = JAXBContext
                    .newInstance(Constants.WS_1_3_TRUST_JAXB_PACKAGE + ":"
                            + Constants.WSSE_JAXB_PACKAGE + ":"
                            + Constants.WSSU_JAXB_PACKAGE);
            result = dbf.newDocumentBuilder().newDocument();
            jaxbContext.createMarshaller().marshal(jaxbElement, result);
        } catch (JAXBException jaxbException) {
            jaxbException.printStackTrace();
            throw new RuntimeException(Constants.MARSHALL_EXCEPTION_ERR_MSG,
                    jaxbException);
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
            throw new RuntimeException(Constants.MARSHALL_EXCEPTION_ERR_MSG,
                    pce);
        }

        return result;
    }

    /**
     * Prints the SOAP Message on the console
     *
     * @param smc
     */
    public static void printMessage(SOAPMessageContext smc) {
        try {
            System.out.println("*********Message Start********");
            System.out.println("This is a " + (((Boolean) smc.get(
                MessageContext.MESSAGE_OUTBOUND_PROPERTY)).booleanValue()
                        ? "Outbound request" : "Inbound response"));
            // Print out the outbound SOAP message to System.out
            smc.getMessage().writeTo(System.out);
            System.out.println("*********Message End**********");
        } catch (SOAPException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * Prints some basic information about the SAML token on the console
     *
     * @param token
     */
    public static void printToken(Element token) {
        if (isSamlToken(token)) {
            System.out.println("Token details:");
            System.out.println("\tAssertionId = "
                + WssHelper.getNodeProperty(token, "ID"));
            System.out.println("\tToken type = " + (isHoKToken(token)
                    ? "Holder-Of-Key" : "Bearer"));
            System.out.println("\tIssued On = "
                    + WssHelper.getNodeProperty(token, "IssueInstant"));
        } else {
            System.out.println("Invalid token");
        }
    }
}
