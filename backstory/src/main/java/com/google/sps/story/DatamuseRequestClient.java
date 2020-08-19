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

import com.google.sps.APINotAvailableException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A class to make requests of the Datamuse API.
 */
public class DatamuseRequestClient {
  /** the url to access the Datamuse database we're querying */
  private static final String DATAMUSE_URL = "http://api.datamuse.com/words?";

  /** holds the base url to query */
  private final String url;

  /**
   * Constructs a request client with the default Datamuse url.
   */
  public DatamuseRequestClient() {
    this(DATAMUSE_URL);
  }

  /**
   * Constructs a request client that will query
   * the given URL.
   *
   * @param url the url to instantiate this request client with
   */
  public DatamuseRequestClient(String url) {
    this.url = url;
  }

  /**
   * Fetches an array of adjectives related to the passed-in noun, which will have
   * a passed-in cap on its size (array returned might be smaller if database
   * does not have enough related adjectives). Fetches these adjectives
   * by querying the Datamuse database.
   *
   * @param noun the noun to get adjectives related to (must be one word)
   * @param cap the maximum number of adjectives to retrieve
   * @return an array of adjectives related to the noun
   * @throws IllegalArgumentException if noun is more than one word (has whitespace)
   * @throws APINotAvailableException if Datamuse API cannot be reached
   * @throws RuntimeException if JSON received back from the API cannot be parsed (JSONException)
   *    or does not have objects of type JSON (ClassCastException)
   */
  public String[] fetchRelatedAdjectives(String noun, int cap)
      throws IllegalArgumentException, APINotAvailableException, RuntimeException {

    // check for whitespace
    Pattern pattern = Pattern.compile("\\s");
    Matcher matcher = pattern.matcher(noun);

    if (matcher.find()) {
      throw new IllegalArgumentException("Noun cannot contain whitespace (must be one word).");
    }

    // put together a query asking for adjectives related to the noun (rel_jjb=noun)
    // and capping the number of results at 10 (max=10)
    String query = url + "rel_jjb=" + noun + "&max=" + cap;
    String jsonResponse = queryUrl(query);

    // there are two try statements b/c there's a possibility for more than one JSONException to be
    // thrown in this method so specifying what went wrong with that exact JSON exception
    // will be more helpful hence a try block for each JSONException that needs a customized message
    JSONArray jsonArray;

    try {
      jsonArray = new JSONArray(jsonResponse);
    } catch (JSONException exception) {
      throw new RuntimeException(
          "Could not parse the JSON received back from the Datamuse Query.", exception);
    }

    try {
      int length = jsonArray.length();
      String[] adjectives = new String[length];

      for (int i = 0; i < length; i++) {
        JSONObject jsonObject = (JSONObject) jsonArray.get(i);
        adjectives[i] = jsonObject.getString("word");
      }

      return adjectives;
    } catch (ClassCastException exception) {
      throw new RuntimeException("JSON array received was not of JSON objects.", exception);
    } catch (JSONException exception) {
      throw new RuntimeException(
          "JSON Object did not have a String value for the key \"word\".", exception);
    }
  }

  /**
   * Query the given url and retrieve the input stream at the site (likely JSON) from that
   * site. If there's nothing at that site, return null. This code was
   * adapted from the DatamuseQuery class in the Datamuse4J repo
   * (https://github.com/sjblair/Datamuse4J/blob/master/src/datamuse/DatamuseQuery.java).
   *
   * @param url the url to query
   * @return the content at the site.
   * @throws APINotAvailableException if the method can't retrieve the content at the site
   */
  private String queryUrl(String url) throws APINotAvailableException {
    final String ERROR_MESSAGE =
        "This query could not successfully retrieve content from Datamuse API." + System.lineSeparator();

    try {
      URL site = new URL(url);
      URLConnection connection = site.openConnection();

      if (connection == null) {
        throw new APINotAvailableException(ERROR_MESSAGE + "URLConnection was null.");
      }

      BufferedReader in =
          new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));

      StringBuilder content = new StringBuilder();

      // build a StringBuilder from the content present at the site
      String inputLine;
      while ((inputLine = in.readLine()) != null) {
        content.append(inputLine);
      }

      in.close();

      return content.toString();
    } catch (IOException exception) {
      throw new APINotAvailableException(ERROR_MESSAGE + exception.toString());
    }
  }
}
