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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.JAXBElement;
import javax.xml.soap.SOAPException;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.oasis_open.docs.wss._2004._01.oasis_200401_wss_wssecurity_utility_1_0.AttributedDateTime;
import org.oasis_open.docs.wss._2004._01.oasis_200401_wss_wssecurity_utility_1_0.ObjectFactory;
import org.oasis_open.docs.wss._2004._01.oasis_200401_wss_wssecurity_utility_1_0.TimestampType;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;

/**
 * Handler class to add the TimeStamp element inside the security header
 */
public class TimeStampHandler extends SSOHeaderHandler {

    public static final String GMT = "GMT";

    /**
     * Creates a datetime formatter needed for populating objects containing XML
     * requests/responses.
     */
    public static DateFormat createDateFormatter() {
        DateFormat dateFormat = new SimpleDateFormat(Constants.XML_DATE_FORMAT);
        // always send UTC/GMT time
        dateFormat.setTimeZone(TimeZone.getTimeZone(TimeStampHandler.GMT));
        return dateFormat;
    }

    /**
     * Creates a timestamp WS-Security element. It is needed for the STS to tell
     * if the request is invalid due to slow delivery
     *
     * @return timestamp element issued with start date = NOW and expiration
     *         date = NOW + REQUEST_VALIDITY_IN_MINUTES
     */
    private JAXBElement<TimestampType> createTimestamp() {
        ObjectFactory wssuObjFactory = new ObjectFactory();

        TimestampType timestamp = wssuObjFactory.createTimestampType();

        final long now = System.currentTimeMillis();
        Date createDate = new Date(now);
        Date expirationDate = new Date(now + TimeUnit.MINUTES.toMillis(
            Constants.REQUEST_VALIDITY_IN_MINUTES));

        DateFormat wssDateFormat = createDateFormatter();
        AttributedDateTime createTime = wssuObjFactory
            .createAttributedDateTime();
        createTime.setValue(wssDateFormat.format(createDate));

        AttributedDateTime expirationTime = wssuObjFactory
            .createAttributedDateTime();
        expirationTime.setValue(wssDateFormat.format(expirationDate));

        timestamp.setCreated(createTime);
        timestamp.setExpires(expirationTime);
        return wssuObjFactory.createTimestamp(timestamp);
    }

    @Override
    public boolean handleMessage(SOAPMessageContext smc) {
        if (WssHelper.isOutgoingMessage(smc)) {
            try {
                Node securityNode = WssHelper.getSecurityElement(WssHelper
                    .getSOAPHeader(smc));
                Node timeStampNode = WssHelper.marshallJaxbElement(
                    createTimestamp()).getDocumentElement();
                securityNode.appendChild(securityNode.getOwnerDocument()
                    .importNode(timeStampNode, true));
            } catch (DOMException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            } catch (SOAPException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
        // Utils.printMessage(smc);

        return true;

    }
}
