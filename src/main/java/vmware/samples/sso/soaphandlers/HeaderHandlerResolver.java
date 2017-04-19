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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.HandlerResolver;
import javax.xml.ws.handler.PortInfo;

/**
 * Reference implementation of the {@link HandlerResolver} interface with an
 * additional method addHandler to add a new {@link SSOHeaderHandler} to the
 * chain.
 */
@SuppressWarnings("rawtypes")
public final class HeaderHandlerResolver implements HandlerResolver {

    private final List<Handler> handlerChain = new ArrayList<Handler>();

    @Override
    public List<Handler> getHandlerChain(PortInfo arg0) {
        return Collections.unmodifiableList(handlerChain);
    }

    /**
     * Adds a specific {@link SSOHeaderHandler} to the handler chain
     *
     * @param ssoHandler
     */
    public void addHandler(SSOHeaderHandler ssoHandler) {
        handlerChain.add(ssoHandler);
    }

    /**
     * Clears the current list of {@link SSOHeaderHandler} in the handler chain
     */
    public void clearHandlerChain() {
        handlerChain.clear();
    }
}
