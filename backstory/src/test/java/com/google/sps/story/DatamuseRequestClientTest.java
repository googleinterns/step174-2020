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

package com.google.sps.story;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import au.com.origma.perspectiveapi.v1alpha1.models.AttributeType;
import com.google.gson.Gson;
import com.google.sps.APINotAvailableException;
import com.google.sps.story.data.DatamuseRequestClient;
import com.google.sps.story.data.DatamuseRequestWordType;
import com.google.sps.story.data.HttpUrlStreamHandler;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/** Quality tests for DatamuseRequestClient */
@RunWith(JUnit4.class)
public final class DatamuseRequestClientTest {

  /** a default url to test with */
  private static final String DEFAULT_URL = "http://sample-website.com/fake?";

  /** the default noun to use for tests */
  private static final String DEFAULT_NOUN = "beach";

  /** the default word type to use for tests */
  private static final DatamuseRequestWordType DEFAULT_WORD_TYPE = DatamuseRequestWordType.ADJECTIVE;

  /** the default max to use for tests */
  private static final int DEFAULT_MAX = 5;

  /** the default topic to use for tests */
  private static final String DEFAULT_TOPIC = "story";

  /** the stream handlet to use for tests in this class (necessary to mock URLConnection) */
  private static HttpUrlStreamHandler httpUrlStreamHandler;
 
  /** 
   * Set up the URLStreamHandlerFactory with the HTTPUrlStreamHandler 
   * in order to mock URLConnections to properly test methods 
   * that call outside URLs. Code comes from this tutorial: 
   * https://claritysoftware.co.uk/mocking-javas-url-with-mockito/.
   */
  @BeforeClass
  public static void setUpURLStreamHandlerFactory() {
    // Allows for mocking URL connections
    URLStreamHandlerFactory urlStreamHandlerFactory = mock(URLStreamHandlerFactory.class);
    URL.setURLStreamHandlerFactory(urlStreamHandlerFactory);
 
    httpUrlStreamHandler = new HttpUrlStreamHandler();
    given(urlStreamHandlerFactory.createURLStreamHandler("http")).willReturn(httpUrlStreamHandler);
  }

  /**
   * Reset the URLConnections of this stream handler before every method,
   * so that URLConnections injected for other tests do not mess 
   * with the current test. Code taken from this tutorial: 
   * https://claritysoftware.co.uk/mocking-javas-url-with-mockito/.
   */
  @Before
  public void reset() {
    httpUrlStreamHandler.resetConnections();
  }

  /**
   * Check that getStorytellingTopic() topic only returns Strings
   * from the storytelling topics array.
   */
  @Test
  public void checkGetStorytellingTopic() {
    Set<String> topics = new HashSet<String>(Arrays.asList(DatamuseRequestClient.STORYTELLING_TOPICS));
     
    // check a significant number of times (at least 2x array length)
    for (int i = 0; i < DatamuseRequestClient.STORYTELLING_TOPICS.length * 2; i++) {
      String topic = DatamuseRequestClient.getStorytellingTopic();
      
      Assert.assertTrue(topics.contains(topic));
    }
  }

  /** 
   * Check that IllegalArgumentException thrown for a multiple
   * word noun.
   */
  @Test
  public void multipleWordNoun() throws Exception {
    final String MULTIWORD_NOUN = "multiword noun";

    DatamuseRequestClient client = new DatamuseRequestClient(DEFAULT_URL);

    try {
      client.fetchRelatedWords(MULTIWORD_NOUN, DEFAULT_WORD_TYPE, DEFAULT_MAX, DEFAULT_TOPIC);
    } catch (IllegalArgumentException exception) {
      return; // test should pass if this exception is thrown
    } 

    Assert.fail("IllegalArgumentException was not thrown.");
  }

  /** 
   * Check that IllegalArgumentException thrown
   * for a noun with whitespace.
   */
  @Test
  public void nounWithWhitespace() throws Exception {
    final String WHITESPACE_NOUN = "noun\n";

    DatamuseRequestClient client = new DatamuseRequestClient(DEFAULT_URL);

    try {
      client.fetchRelatedWords(WHITESPACE_NOUN, DEFAULT_WORD_TYPE, DEFAULT_MAX, DEFAULT_TOPIC);
    } catch (IllegalArgumentException exception) {
      return; // test should pass if this exception is thrown
    } 

    Assert.fail("IllegalArgumentException was not thrown.");
  }

  /** 
   * Check that IllegalArgumentException thrown
   * for a null argument for noun
   */
  @Test
  public void nullNounInput() throws Exception {
    DatamuseRequestClient client = new DatamuseRequestClient(DEFAULT_URL);

    try {
      client.fetchRelatedWords(null, DEFAULT_WORD_TYPE, DEFAULT_MAX, DEFAULT_TOPIC);
    } catch (IllegalArgumentException exception) {
      return; // test should pass if this exception is thrown
    } 

    Assert.fail("IllegalArgumentException was not thrown.");
  }

  /** 
   * Check that APINotAvailableException is thrown 
   * if there is no URLConnection injected (which is equivalent
   * to if it can't access the Datamuse API).
   */
  @Test 
  public void apiNotAvailable() throws Exception {
    DatamuseRequestClient client = new DatamuseRequestClient(DEFAULT_URL);

    try {
      client.fetchRelatedWords(DEFAULT_NOUN, DEFAULT_WORD_TYPE, DEFAULT_MAX, DEFAULT_TOPIC);
    } catch (APINotAvailableException exception) {
      return; // test should pass if this exception is thrown
    }

   Assert.fail("APINotAvailableException was not thrown.");    
  }

  /** 
   * Check that fetchRelatedWords() throws a RuntimeException
   * when the JSON can't be parsed with the correct message signaling
   * it's an issue with the JSON.
   */
  @Test 
  public void jsonCannotBeParsed() throws Exception {
    final String QUERIES = "rel_jjb=" + DEFAULT_NOUN + "&max=" + DEFAULT_MAX 
        + "&topics=" + DEFAULT_TOPIC;
    final String INPUT = "not JSON";

    DatamuseRequestClient client = new DatamuseRequestClient(DEFAULT_URL);

    // inject the url but with a response that's not proper JSON
    injectURLConnectionMock(DEFAULT_URL, QUERIES, INPUT);

    try {
      String[] actualOutput = client.fetchRelatedWords(DEFAULT_NOUN, DEFAULT_WORD_TYPE, DEFAULT_MAX, DEFAULT_TOPIC);
    } catch (RuntimeException exception) {
      final String EXPECTED_MESSAGE = "Could not parse the JSON received back from the Datamuse Query.";
    
      Assert.assertEquals(EXPECTED_MESSAGE, exception.getMessage());
      Assert.assertEquals(JSONException.class, exception.getCause().getClass());
      return; // this is the behavior we wanted to happen
    }

    Assert.fail("No RuntimeException was thrown when parsing incorrect JSON.");
  }   

  /** 
   * Check that fetchRelatedWords() throws a RunTimeException
   * when the JSON array that it gets back from the server does not
   * contain JSON objects.
   */
  @Test 
  public void jsonArrayButNotJsonObjects() throws Exception {
    final String QUERIES = "rel_jjb=" + DEFAULT_NOUN + "&max=" + DEFAULT_MAX 
        + "&topics=" + DEFAULT_TOPIC;
    final String[] INPUT = { "sandy", "long", "private", "white", "small"}; 

    DatamuseRequestClient client = new DatamuseRequestClient(DEFAULT_URL);

    // inject the url but with a response that's an array but of Strings not of JSON objects
    injectURLConnectionMock(DEFAULT_URL, QUERIES, Arrays.toString(INPUT));

    try {
      String[] actualOutput = client.fetchRelatedWords(DEFAULT_NOUN, DEFAULT_WORD_TYPE, DEFAULT_MAX, DEFAULT_TOPIC);
    } catch (RuntimeException exception) {
      final String EXPECTED_MESSAGE = "JSON array received was not of JSON objects.";
    
      Assert.assertEquals(EXPECTED_MESSAGE, exception.getMessage());
      Assert.assertEquals(ClassCastException.class, exception.getCause().getClass());
      return; // this is the behavior we wanted to happen
    }

    Assert.fail("No RuntimeException was thrown when objects were not valid JSON.");
  }

  /** 
   * Check that fetchRelatedWords() throws a RuntimeException
   * when the JSON objects in the JSON array do not have a String
   * value for "word".
   */
  @Test 
  public void noValueForWord() throws Exception {
    final String QUERIES = "rel_jjb=" + DEFAULT_NOUN + "&max=" + DEFAULT_MAX 
        + "&topics=" + DEFAULT_TOPIC;
    
    JSONArray input = new JSONArray();
    input.put(new JSONObject()); // put in a default object without a "word" field

    DatamuseRequestClient client = new DatamuseRequestClient(DEFAULT_URL);

    // inject the url but with a response that doesn't have a "word" field
    injectURLConnectionMock(DEFAULT_URL, QUERIES, input.toString());

    try {
      String[] actualOutput = client.fetchRelatedWords(DEFAULT_NOUN, DEFAULT_WORD_TYPE, DEFAULT_MAX, DEFAULT_TOPIC);
    } catch (RuntimeException exception) {
      final String EXPECTED_MESSAGE = "JSON Object did not have a String value for the key \"word\".";
    
      Assert.assertEquals(EXPECTED_MESSAGE, exception.getMessage());
      Assert.assertEquals(JSONException.class, exception.getCause().getClass());
      return; // this is the behavior we wanted to happen
    }

    Assert.fail("No RuntimeException was thrown when parsing incorrect JSON.");
  }   

  /** 
   * Check that fetchRelatedWords() works for when less
   * words are present than were requested by the method.
   */
  @Test 
  public void tooFewWords() throws Exception {
    final int MAX_WORDS = 10;
    final String QUERIES = "rel_jjb=" + DEFAULT_NOUN + "&max=" + MAX_WORDS 
        + "&topics=" + DEFAULT_TOPIC;
    final String[] EXPECTED_OUTPUT = { "sandy", "long", "private", "white", "small"};

    DatamuseRequestClient client = new DatamuseRequestClient(DEFAULT_URL);
    JSONArray jsonOutput = buildJSONFromStringArray(EXPECTED_OUTPUT);

    injectURLConnectionMock(DEFAULT_URL, QUERIES, jsonOutput.toString());

    String[] actualOutput = client.fetchRelatedWords(DEFAULT_NOUN, DEFAULT_WORD_TYPE, MAX_WORDS, DEFAULT_TOPIC);
    Assert.assertEquals(EXPECTED_OUTPUT, actualOutput);
  }  
  
  /** 
   * Check that fetchRelatedWords() works for the standard case
   * which is five words requested and five words present.
   */
  @Test 
  public void exactNumberOfWords() throws Exception {
    final int MAX_WORDS = 5;
    final String QUERIES = "rel_jjb=" + DEFAULT_NOUN + "&max=" + MAX_WORDS 
        + "&topics=" + DEFAULT_TOPIC;
    final String[] EXPECTED_OUTPUT = { "sandy", "long", "private", "white", "small"};

    DatamuseRequestClient client = new DatamuseRequestClient(DEFAULT_URL);
    JSONArray jsonOutput = buildJSONFromStringArray(EXPECTED_OUTPUT);

    injectURLConnectionMock(DEFAULT_URL, QUERIES, jsonOutput.toString());

    String[] actualOutput = client.fetchRelatedWords(DEFAULT_NOUN, DEFAULT_WORD_TYPE,
        MAX_WORDS, DEFAULT_TOPIC);
    Assert.assertEquals(EXPECTED_OUTPUT, actualOutput);
  }  

  /** 
   * Check that fetchRelatedWords() queries the right url 
   * for an adjective.
   */
  @Test 
  public void checkAdjectiveQuery() throws Exception {
    final String RIGHT_QUERIES = "rel_jjb=" + DEFAULT_NOUN + "&max=" + DEFAULT_MAX 
        + "&topics=" + DEFAULT_TOPIC;
    final String[] EXPECTED_OUTPUT = { "sandy", "long", "private", "white", "small"};

    DatamuseRequestClient client = new DatamuseRequestClient(DEFAULT_URL);
    JSONArray jsonOutput = buildJSONFromStringArray(EXPECTED_OUTPUT);

    injectURLConnectionMock(DEFAULT_URL, RIGHT_QUERIES, jsonOutput.toString());

    String[] actualOutput = client.fetchRelatedWords(DEFAULT_NOUN, DatamuseRequestWordType.ADJECTIVE,
        DEFAULT_MAX, DEFAULT_TOPIC);
    Assert.assertEquals(EXPECTED_OUTPUT, actualOutput);
  }

  /** 
   * Check that fetchRelatedWords() queries the right url 
   * for an adjective.
   */
  @Test 
  public void checkGerundQuery() throws Exception {
    final String RIGHT_QUERIES = "rel_jja=" + DEFAULT_NOUN + "&sp=*ing&max=" + DEFAULT_MAX 
        + "&topics=" + DEFAULT_TOPIC;
    final String[] EXPECTED_OUTPUT = { "sunning", "tanning", "laying", "relaxing", "running"};

    DatamuseRequestClient client = new DatamuseRequestClient(DEFAULT_URL);
    JSONArray jsonOutput = buildJSONFromStringArray(EXPECTED_OUTPUT);

    injectURLConnectionMock(DEFAULT_URL, RIGHT_QUERIES, jsonOutput.toString());

    String[] actualOutput = client.fetchRelatedWords(DEFAULT_NOUN, DatamuseRequestWordType.GERUND,
        DEFAULT_MAX, DEFAULT_TOPIC);
    Assert.assertEquals(EXPECTED_OUTPUT, actualOutput);
  }

  /**
   * Inject a mock of the URLConnection for the given url and queries 
   * so that when a URL is created from the url + queries given,
   * the input stream that will be returned from it will be
   * the passed-in String response. Needed in order to test methods
   * that use a URL. Code adapted from this tutorial: 
   * https://claritysoftware.co.uk/mocking-javas-url-with-mockito/.
   * 
   * @param url the base url to inject canned results for
   * @param queries the quer(ies) to send to that url
   * @param response the response to return from that query
   */
  private void injectURLConnectionMock(String url, String queries, String response) {
    URLConnection urlConnection = mock(URLConnection.class);

    try {
      httpUrlStreamHandler.addConnection(new URL(url + queries), urlConnection);
    } catch (MalformedURLException exception) { 
      exception.printStackTrace();
    };

    InputStream inputStream = new ByteArrayInputStream(
        response.toString().getBytes(Charset.forName("UTF-8")));
    
    try {
      given(urlConnection.getInputStream()).willReturn(inputStream);
    } catch(IOException exception) { 
      exception.printStackTrace(); 
    };
  }

  /**
   * Builds JSON (as a JSONArray) from a String array words by constructing a JSONArray
   * of the same size as words and putting JSONObjects in there with one key-value pair
   * of "word" to each word in words. Supposed to resemble output from Datamuse API 
   * (although only the words output not other information).
   *
   * @param words the array to build the JSON from
   * @return a JSONArray to hold objects which have "word" to each word from words
   */
  private JSONArray buildJSONFromStringArray(String[] words) throws RuntimeException {
    JSONArray jsonArray = new JSONArray();

    for (int i = 0; i < words.length; i++) {
      Map<String, Object> map = new HashMap<String, Object>();
      map.put("word", words[i]);

      try {
        jsonArray.put(i, map);
      } catch(JSONException exception) {
        throw new RuntimeException("Could not create a JSONArray from the String[] given", exception);
      }
    }

    return jsonArray;
  }
}
