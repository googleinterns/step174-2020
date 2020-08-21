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

  /** a list of topics related to storytelling for which to filter adjectives/gerunds for */
  public static final String[] STORYTELLING_TOPICS = { "story", "fairytale", "narrative", "anecdote",
    "drama", "fantasy", "adventure", "poem", "grand" };

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
   * Returns a randomly-chosen topic related to storytelling
   * from a preset array.
   *
   * @return a topic related to storytelling to be used in the PCS
   */
  public static String getStorytellingTopic() {
    int randomIndex = (int) (Math.random() * STORYTELLING_TOPICS.length);

    return STORYTELLING_TOPICS[randomIndex];
  }

  /**
   * Fetches an array of words, of type wordType, related to the passed-in noun, 
   * which will have a passed-in cap on its size (array returned might be smaller if database
   * does not have enough related adjectives). Fetches of these the words most related
   * to the passed-in topic. Accomplished by querying the Datamuse database.
   *
   * @param noun the noun to get words related to (must be one word)
   * @param wordType the type of word to fetch (e.g. adjectives or gerunds)
   * @param cap the maximum number of words to retrieve
   * @param topic will sort the words most relevant to given topic (so top ten will
   *    be words related to the noun then the ones most related to the given topic) 
   *    (empty string for topic is same as no topic)
   * @return an array of words of type wordType related to the noun of max size cap
   * @throws IllegalArgumentException if noun is more than one word (has whitespace)
   * @throws APINotAvailableException if Datamuse API cannot be reached
   * @throws RuntimeException if JSON received back from the API cannot be parsed (JSONException)
   *    or does not have objects of type JSON (ClassCastException)
   */
  public String[] fetchRelatedWords(String noun, DatamuseRequestWordType wordType, int cap, String topic)
      throws IllegalArgumentException, APINotAvailableException, RuntimeException {
    
    validateWordArgument(noun, "Noun");

    String query = url;

    switch (wordType) {
      case ADJECTIVE: 
        query += "rel_jjb=" + noun; // get adjectives related to the noun
        break;
      case GERUND: 
        query += "rel_jja=" + noun + "&sp=*ing"; // get nouns related to the noun ending in "ing"
        break;
    }

    // cap number of results (max=cap) & set the topic (topics=topic)
    query += "&max=" + cap + "&topics=" + topic;

    String jsonResponse = queryUrl(query);

    return parseWordArrayFromJson(jsonResponse);
  }
  
  /**
   * Helper method to validate a word argument by checking 
   * it's only one word (properly formatted) which is checked 
   * by ensuring there's no whitespace.
   *
   * @param argument the argument to check for whitespace
   * @param argumentName the name of argument to use in the error message
   * @throws IllegalArgumentException if argument contains whitespace
   */
  private void validateWordArgument(String argument, String argumentName) throws IllegalArgumentException {
    if (argument == null) {
      throw new IllegalArgumentException(argumentName + " cannot be null.");
    }
    // check for whitespace
    Pattern pattern = Pattern.compile("\\s");
    Matcher matcher = pattern.matcher(argument);

    if (matcher.find()) {
      throw new IllegalArgumentException(argumentName + " cannot contain whitespace (must be one word).");
    }
  }

  /**
   * Makes a GET request to given url and retrieve info at that site.
   * If there's nothing at that site, return null. This code was
   * adapted from the DatamuseQuery class in the Datamuse4J repo
   * (https://github.com/sjblair/Datamuse4J/blob/master/src/datamuse/DatamuseQuery.java).
   *
   * @param url the url to query
   * @return the response body of the GET request
   * @throws APINotAvailableException if the method can't retrieve the content at the site
   */
  private String queryUrl(String url) throws APINotAvailableException {
    final String ERROR_MESSAGE =
        "This query could not successfully retrieve content from Datamuse API."
        + System.lineSeparator();

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

  private String[] parseWordArrayFromJson(String jsonResponse) throws RuntimeException {
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
      String[] words = new String[length];

      for (int i = 0; i < length; i++) {
        JSONObject jsonObject = (JSONObject) jsonArray.get(i);
        words[i] = jsonObject.getString("word");
      }

      return words;
    } catch (ClassCastException exception) {
      throw new RuntimeException("JSON array received was not of JSON objects.", exception);
    } catch (JSONException exception) {
      throw new RuntimeException(
          "JSON Object did not have a String value for the key \"word\".", exception);
    }
  }
}
