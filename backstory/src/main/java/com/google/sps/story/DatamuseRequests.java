// Copyright 2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.story.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * A class to make requests of the Datamuse API.
 */
public class DatamuseRequestClient {

  private static final String DATAMUSE_URL = "http://api.datamuse.com/words?";

  /** holds the url to query */
  private final String queryUrl;

  public DatamuseRequestClient() {
    this(queryUrl);
  }

  public DatamuseRequestClient(String queryUrl) {
    this.queryUrl = queryUrl;
  }

  public static String getRelatedAdjective(String noun, int index) throws APINotAvailableException {
    String query = queryUrl + "rel_jjb=" + noun + "+max=10";
    String jsonResponse = querySite(query);

    // parse the json into String array

    // return the one at the given index
    
  }

  /**
   * Query the site at the given url and retrieve the String from that 
   * site. If there's nothing at that site, return null.
   *
   * @param url the url to query
   * @return the content at the site.
   */
  private String querySite(String url) {
    String content = null;

    try {
      URL site = new URL(url);
      URLConnection connection = site.openConnection();
      BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
      
      StringBuilder builder = new StringBuilder();
      
      // build a StringBuilder out of 
      String inputLine;
      while ((inputLine = in.readLine()) != null) {
        builder.append(inputLine);
      }
      
      content = builder.toString();
      in.close();
    } catch (MalformedURLException | IOException exception) {
      throws new APINotAvailableException("This query could not successfully retrieve content from Datamuse API.\n" + exception.toString());
    }
    
    return content;
  }
}
