package vmware.samples.common.authentication;

/*
 * *******************************************************
 * Copyright VMware, Inc. 2019.  All Rights Reserved.
 * SPDX-License-Identifier: MIT
 * *******************************************************
 *
 * DISCLAIMER. THIS PROGRAM IS PROVIDED TO YOU "AS IS" WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, WHETHER ORAL OR WRITTEN,
 * EXPRESS OR IMPLIED. THE AUTHOR SPECIFICALLY DISCLAIMS ANY IMPLIED
 * WARRANTIES OR CONDITIONS OF MERCHANTABILITY, SATISFACTORY QUALITY,
 * NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE.
 */

/**
IMPORTANT: To be used only in Test Environment NOT for any production.
 Program to setup a simple http webserver for oAuth  Authorization Code Grant Type Workflow.
Web Server would be running in https://<local_ipaddress>:<portnumber>/authcode

We define listeners for two endpoints,
1. /getcode -> The client code hits this endpoint to fetch the 'code' and 'state' variable
                It is a GET request
                Once the response is returned, the variables need to be reassigned with ''
                or None, to avoid inconsistent values

2. /authcode -> This is the redirect endpoint which will be called by the CSP server
                It is a GET request
                code and state are the request params
                e.g., /authcode?code=xxxx&state=xxxxx
In case, you want to change the names of these endpoints in your client,
make sure to change in the below server code as well
**/

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

@SuppressWarnings("restriction")
public class SimpleHttpWebServer  {

    private static int PORT;
    private static String code = "";
    private static String state = "";
    private static HttpServer server;
    private final static String AUTHPATH = "/authcode";
    private final static String GETPATH = "/getcode";

     /**
     * This method send response code and state value back to the client.
     *
     * @param httpExchange {@link HttpExchange}
     * @param response
     */
	protected void writeResponse(HttpExchange httpExchange, String response) throws IOException {
        httpExchange.sendResponseHeaders(200, response.length());
        OutputStream os = httpExchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

     /**
     * This method returns url parameters in a map
     *
     * @param query from the uri
     * @return code and state and its respective values
     */
    protected Map<String, String> queryToMap(String query) {
        Map<String, String> result = new HashMap<String, String>();
        for (String param : query.split("&")) {
            String pair[] = param.split("=");
            if (pair.length > 1) {
                result.put(pair[0], pair[1]);
            } else {
                result.put(pair[0], "");
            }
        }
        return result;
    }

     /**
      * A handler which is invoked to process HTTP exchanges.
      *
      * @params httpExchange encapsulates a HTTP request received
      * and a response generated is code and state
      */
	protected class GetHandler implements HttpHandler {
        public void handle(HttpExchange httpExchange) throws IOException {
            SimpleHttpWebServer  simpleHttpServer = new SimpleHttpWebServer();
            String response = null;
            if(!"".equals(code) && !"".equals(state)) {
                response = "code:" + code + ":state:" + state;
            }
            else {
                response="variables are not set";
            }
            simpleHttpServer.writeResponse(httpExchange, response.toString());
            code="";
            state="";

        }

    }
     /**
      * Fetches the keys-values from the uri and initialize to code and state class variable.
      *
      * @params httpExchange encapsulates a HTTP request received
      * and on successful execution display redirect_uri
      */
    protected class AuthHandler implements HttpHandler {
        SimpleHttpWebServer  httpServer;

        public void handle(HttpExchange httpExchange) throws IOException {
            httpServer = new SimpleHttpWebServer();
            Map<String, String> params = httpServer.queryToMap(httpExchange.getRequestURI().getQuery());
            code = params.get("code");
            state = params.get("state");
            String response = "Found Authorization code and state variables , you can close the window";
            httpServer.writeResponse(httpExchange, response.toString());

        }

    }

    public void startWebServer(int port) {
        PORT=port;
        try {
            server = HttpServer.create(new InetSocketAddress(PORT), 0);
            server.createContext(AUTHPATH, new SimpleHttpWebServer().new AuthHandler());
            server.createContext(GETPATH, new SimpleHttpWebServer(). new GetHandler());
            server.setExecutor(null); // creates a default executor
            server.start();
        } 
        catch (IOException e) {
            System.out.println(e.getMessage());
            System.exit(0);
        }
    }
    
    public void stopServer(int port) {
        server.stop(0);
    }

}

