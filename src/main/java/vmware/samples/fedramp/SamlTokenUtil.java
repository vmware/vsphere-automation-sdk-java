package vmware.samples.fedramp;

import java.io.StringReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import javax.xml.bind.DatatypeConverter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import com.vmware.vapi.saml.DefaultTokenFactory;
import com.vmware.vapi.saml.SamlToken;

public class SamlTokenUtil {

	private static final String CSP_ENDPOINT = "/csp/gateway/am/api/auth/api-tokens/authorize";
	private static final String VCENTER_TOKENEXCHANGE_ENDPOINT = "/rest/vcenter/tokenservice/token-exchange";
	
    /***
     * Method to to get access token by using refresh token from the CSP server
     *
     * @param refreshToken refresh Token of the authorized VMware Cloud User
     * @param cspServer VMware Cloud URL (eg:https://console.cloud.vmware.com)
     * @return
     * @throws Exception
     */
	public static Map<String, String> getAccessTokenByApiRefreshToken(String cspServer, String refreshToken)
			throws Exception {
		// Form the REST URL
		String cspURL = "https://" + cspServer + CSP_ENDPOINT;
		URL url = new URL(cspURL);

		// Form the REST Header
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Content-Type", "application/x-www-form-urlencoded");
		headers.put("Accept", "application/json");
		String body = "refresh_token=" + refreshToken;
		URLEncoder.encode(body, "UTF-8");

		// POST the REST CALL and get the API Response
		Map<Integer, String> apiResponse = RestHandler.httpPost(url, headers, body);
		Iterator<Integer> iter = apiResponse.keySet().iterator();
		Integer key = iter.next();
		String local_response = apiResponse.get(key).replaceFirst("\\{", "").replace("\\}", "");
		StringTokenizer tokenizer = new StringTokenizer(local_response, ",");
		Map<String, String> map = new HashMap<>();
		while (tokenizer.hasMoreElements()) {
			String element = (String) tokenizer.nextElement();
			String[] res = element.split(":");
			String jsonkey = res[0].replaceAll("\"", "");
			String value = res[1].replaceAll("\"", "");
			if ("access_token".equalsIgnoreCase(jsonkey)) {
				map.put(jsonkey, value);
			}

			if ("id_token".equalsIgnoreCase(jsonkey)) {
				map.put(jsonkey, value);
			}
		}
		return map;
	}
	
	/**
	 * Method to to get SAML Bearer token from the vCenter Server by using the access token
	 * retrieved from CSP. 
	 * 
	 * @param accessToken Access Token
	 * @param idToken ID Token
	 * @param vCenterServer vCenter host name or IP address
	 * @return
	 */
	public static Element getSamlTokenByApiAccessToken(String vCenterServer, String accessToken, String idToken)
			throws Exception {
		// Create the REST URL
		String vcHostnameUri = "https://" + vCenterServer + VCENTER_TOKENEXCHANGE_ENDPOINT;
		URL url = new URL(vcHostnameUri);

		// Create the REST Header
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Content-Type", "application/json");
		headers.put("Accept", "application/json");

		headers.put("Authorization", "Bearer " + accessToken);

		// POST the REST CALL and get the API Response
		String payload = "{ \"spec\": { \"subject_token\":\"" + accessToken + "\","
				+ "\"subject_token_type\":\"urn:ietf:params:oauth:token-type:access_token\", \""
				+ "requested_token_type\": \"urn:ietf:params:oauth:token-type:saml2\","
				+ "\"grant_type\": \"urn:ietf:params:oauth:grant-type:token-exchange\","
				+ "\"actor_token_type\":\"urn:ietf:params:oauth:token-type:id_token\","
				+ "\"actor_token\":\"" + idToken + "\"} }";

		URLEncoder.encode(payload, "UTF-8");

		Map<Integer, String> apiResponse = RestHandler.httpPost(url, headers, payload);
		Iterator<Integer> itr = apiResponse.keySet().iterator();
		Integer key = itr.next();
		String local_response = apiResponse.get(key).replaceFirst("\\{", "").replace("\\}", "");
		StringTokenizer tokenizer = new StringTokenizer(local_response, ",");
		while (tokenizer.hasMoreElements()) {
			String element = (String) tokenizer.nextElement();
			String[] res = element.split(":");
			String accessTokenElements = res[1].replaceFirst("\\{", "").replace("\\}", "");
			String replacedAcessToken = accessTokenElements.toString().replaceAll("\"", "");
			if ("access_token".equalsIgnoreCase(replacedAcessToken)) {
				String samlToken = res[2];
				String replacedSamlToken = samlToken.replaceAll("\"", "");
				byte[] decodedBytes = DatatypeConverter.parseBase64Binary(replacedSamlToken);
				String decodedSamlToken = new String(decodedBytes, "UTF-8");
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				factory.setNamespaceAware(true);
				DocumentBuilder builder;
				builder = factory.newDocumentBuilder();
				Document doc = builder.parse(new InputSource(new StringReader(decodedSamlToken)));
				return doc.getDocumentElement();
			}
		}
		
		throw new Exception("Error encountered while retrieving saml bearer token using access token");
	}

	public static SamlToken getSamlBearerToken(String vCenterServer, String cspServer,
			String refreshToken) throws Exception {
		// Get the access token and id token using refresh token
        Map<String, String> tokenMap = getAccessTokenByApiRefreshToken( cspServer, refreshToken);
        String accessToken = tokenMap.get("access_token");
        String idToken = tokenMap.get("id_token");
        
        // Get the SAML Bearer token from the vcenter server endpoint using the access token and id token
        Element bearerTokenElement =
        		getSamlTokenByApiAccessToken(vCenterServer, accessToken, idToken);
        return DefaultTokenFactory.createTokenFromDom(bearerTokenElement);
	}
}
