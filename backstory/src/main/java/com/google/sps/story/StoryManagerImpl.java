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

import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.IdTokenCredentials;
import com.google.auth.oauth2.IdTokenProvider;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;
import org.json.JSONObject;

/**
 * Object which generates text through interface with Cloud Contained
 * GPT-2 Model and indication of associated parameters.
 */
public final class StoryManagerImpl implements StoryManager {
  /** String to indicate basis for text generation */
  private String prefix;
  /** Maximum character length of generation */
  private int maxTextLength;
  /** The volatility of topics and style in generation. */
  private Double temperature;
  /** Provider object of serviceURLs*/
  private StoryManagerURLProvider URLProvider;

  /** requestFactory - Builds and facilitates authenticated post requests. */
  private StoryManagerRequestFactory requestFactory;

  /**
   * Instantiate StoryManager.
   *
   * @param prefix String to serve as generation prompt.
   * @param maxLength Maximum text character length for generation output. 100-1000 characters.
   * @param temperature Double to hold number 0-1 for text generation volatility.
   * @param URLProvider Provides service URLs for generation.
   */
  public StoryManagerImpl(String prefix, int maxLength, Double temperature,
      StoryManagerURLProvider URLProvider) throws RuntimeException, IllegalArgumentException {
    this.prefix = prefix;
    this.maxTextLength = maxLength;
    this.temperature = temperature;
    requestFactory = new StoryManagerRequestFactoryImpl();

    if (prefix == null) {
      throw new IllegalArgumentException("Prefix cannot be null.");
    }
    if (maxLength < 100) {
      throw new IllegalArgumentException("Maximum length must be between 100 and 1000.");
    }
    if (maxLength > 1000) {
      throw new IllegalArgumentException("Maximum length must be between 100 and 1000.");
    }
    if (temperature < 0 || temperature > 1) {
      throw new IllegalArgumentException("Temperature must be between 0 and 1.");
    }
    this.URLProvider = URLProvider;
  }

  /**
   * Makes a post request with a JSON including GPT2 Parameters
   *
   * @return HttpResponse The reponse from the Generation server expected to include
   *          a "text" field with the generated text.
   */
  private HttpResponse requestGeneratedText() throws IOException {
    // Form JSON body using generation parameters
    String requestBody = makeRequestBody(prefix, maxTextLength, temperature);

    // Build Request with Adapter and JSON Input
    HttpRequest request = requestFactory.newInstance(requestBody, URLProvider.getCurrentURL());
    request.getHeaders().setContentType("application/json");

    // Wait until response received
    request.setConnectTimeout(0);
    request.setReadTimeout(0);
    return request.execute();
  }

  /**
   * Returns generated text output using given fields.
   *
   * @return String Generated output text.
   */
  public String generateText() throws RuntimeException {
    // Obtain response from Server POST Request
    HttpResponse outputResponse;
    try {
      outputResponse = requestGeneratedText();
    } catch (IOException serverException) {
      throw new RuntimeException("Error with server", serverException);
    }
    // Parse response as JSON
    try {
      JSONObject jsonObject = new JSONObject(outputResponse.parseAsString());
      return jsonObject.getString("text");
    } catch (Exception jsonException) {
      throw new RuntimeException("Failed to convert repsonse into JSON", jsonException);
    }
  }

  /**
   * Allow public setting of RequestFactory for alternative posting.
   *
   * @param factory StoryManagerRequestFactory to use for HttpRequests.
   */
  public void setRequestFactory(StoryManagerRequestFactory factory) {
    requestFactory = factory;
  }

  /**
   * Forms request body string from GPT-2 parameters.
   *
   * @param prefix String to serve as generation prompt.
   * @param maxLength Maximum text character length for generation output.
   * @param temperature Double to hold number 0-1 for text generation volatility.
   */
  private String makeRequestBody(String prefix, int maxLength, Double temperature) {
    Gson gson = new Gson();

    HashMap<String, Object> requestMap = new HashMap<>();

    requestMap.put("length", new Integer(maxLength));
    requestMap.put("truncate", "<|endoftext|>");
    requestMap.put("prefix", prefix);
    requestMap.put("temperature", temperature);

    String convertedMap = gson.toJson(requestMap);
    return convertedMap;
  }
}
