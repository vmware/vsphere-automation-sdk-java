/* **********************************************************
 * Copyright 2018 VMware, Inc.  All rights reserved.
 * **********************************************************/

package vmware.samples.fedramp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * class for the doing httpPost call
 * this class uses native jdk provided libraries for a http REST Call, without using any third party libraries.
 * @author vsphere-sdk Engineering
 */
public class RestHandler {
  /**
   * Method to do httpPost call which uses the following parameteres,this 
   * method acts like template method to do any type of http post call
   *
   * @param url URL for the http REST call
   * @param headers Headers for the http call
   * @param body Body of the http call
   * @return
   * @throws Exception 
   */
  public static Map<Integer, String> httpPost(URL url, Map<String, String> headers, String body) throws Exception {

    Map<Integer, String> responseMap = new HashMap<Integer, String>();
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setDoOutput(true);
    conn.setRequestMethod("POST");
    Iterator<Entry<String, String>> iterator = headers.entrySet().iterator();
    while(iterator.hasNext()) {
      Map.Entry<String, String> pair = (Map.Entry<String, String>)iterator.next();
      conn.setRequestProperty(pair.getKey().toString(), pair.getValue().toString());
      iterator.remove();
    }

    OutputStream os = conn.getOutputStream();
    os.write(body.getBytes("UTF-8"));
    os.flush();

    int responseCode = conn.getResponseCode();
    BufferedReader br = null;
    StringBuilder responseSB = null;
    try {
       if(responseCode==HttpURLConnection.HTTP_OK) {
                          StringBuilder textBuilder = new StringBuilder();
                                if (conn.getErrorStream() != null) {
                                          Reader reader = new BufferedReader(
                  new InputStreamReader(conn.getErrorStream(), Charset.forName(StandardCharsets.UTF_8.name())));
                                         int c = 0;
              while ((c = reader.read()) != -1) {
                textBuilder.append((char) c);
              }
            }
            // Read response
            responseSB = new StringBuilder();
            br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = br.readLine()) != null)
               responseSB.append(line);
            }
       else {
         throw new Exception("Failed : HTTP error code : " + responseCode);
       }
    }
       catch(Exception e){
      System.err.println("Failed : HTTP error code : " + responseCode);
      responseMap.put(responseCode, "Failed : HTTP error code : ");
      throw new Exception("Failed : HTTP error code : " + responseCode);
       }
    finally {
      if(br!=null) {
        br.close();
      }
      if(os!=null) {
        os.close();
      }
    }
    // Close streams
    responseMap.put(responseCode, responseSB.toString());
    return responseMap;

  }

}
