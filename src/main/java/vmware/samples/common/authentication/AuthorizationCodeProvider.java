package vmware.samples.common.authentication;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

import com.vmware.authorization.IdentityProviderType;
import com.vmware.authorization.id.AuthorizationCodeAndState;

public class AuthorizationCodeProvider {
    private SimpleHttpWebServer httpWebServer;
    private int webServerPort;
    private String webServer;
    private IdentityProviderType identityType;
    private String redirectURI;

    public AuthorizationCodeProvider(IdentityProviderType identityType,
            String redirectURI, String webServerType) {
        this.identityType = identityType;
        this.redirectURI = redirectURI;
        this.webServer = webServerType;
    }

    /**
     * gets the code and state variables from the web server used to access
     * token,id token.
     * 
     * @param uri uri of the web server
     * @return String containing code and state variables.
     */
    private String getAuthCodeAndState(String uri) throws IOException {

        URL url = new URL(uri);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url
                .openConnection();
        httpURLConnection.setRequestMethod("GET");
        httpURLConnection.setConnectTimeout(3000);
        httpURLConnection.connect();
        StringBuilder responseSB = null;
        BufferedReader br = null;
        responseSB = new StringBuilder();
        br = new BufferedReader(
                new InputStreamReader(httpURLConnection.getInputStream()));
        String line;
        while ((line = br.readLine()) != null)
            responseSB.append(line);
        br.close();
        return responseSB.toString();
    }

    public AuthorizationCodeAndState getAuthorizationCodeAndState()
            throws IOException {
        String newRedirectUri = redirectURI.substring(0,
                redirectURI.lastIndexOf("/"));
        String codeStateUri = newRedirectUri + "/getcode";
        String response = getAuthCodeAndState(codeStateUri);
        boolean connect = true;
        while (!(response.contains("code"))) {
            response = getAuthCodeAndState(codeStateUri);
            if (connect) {
                System.out.println("connecting.....");
                connect = false;
            }
        }
        // webserver is going to stop after retrieving code and state
        if (!"remote".equalsIgnoreCase(webServer)) {
            httpWebServer.stopServer(webServerPort);
        }
        String[] responses = response.split(":");
        String code = responses[1];
        String state = responses[3];

        return new AuthorizationCodeAndState(code, state);
    }

    /**
     * pops up the browser with CSP URI which contains client id,client
     * secret,redirect uri.
     * 
     * @param server       vcenter host name.
     * @param clientid     client id of registered web server.
     * @param redirect_uri of the web server.
     */
    public void openbrowser(String server, String clientID) throws Exception {
        String[] strings = redirectURI.split(":");
        // getting the port number from redirecr uri argument to bring up local
        // web server
        webServerPort = Integer.parseInt(strings[2].split("/")[0]);
        if (!"remote".equalsIgnoreCase(webServer)) {
            httpWebServer = new SimpleHttpWebServer();
            httpWebServer.startWebServer(webServerPort);
        }
        String authUri = identityType.getAuthorizationURLString();
        authUri += "client_id=" + clientID + "&redirect_uri="
                + redirectURI.toString();
        String url = authUri.trim();
        // Replacing spaces contained in the url to %20
        if (url.contains(" "))
            url = url.replace(" ", "%20");
        if (Desktop.isDesktopSupported()
                && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            Desktop.getDesktop().browse(new URI(url));
        }
        return;
    }
}
