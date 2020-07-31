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

package com.google.sps.servlets;

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
import java.io.IOException;
import java.util.Scanner;
import org.json.JSONObject;

/**
 * Generates text through interface with Cloud Contained
 * GPT-2 Model.
 */
public final class StoryManagerImpl implements StoryManager {
  /**
   * Prefix, Maximum Length, and Temperature(Volatility) fields
   */
  private String prefix;
  private int maxTextLength;
  private Double temperature;

  private StoryManagerRequestFactory requestFactory;

  /**
   * Instantiate StoryManager. Use "", 100-1000, and 1 as parameter
   * defaults in case of invalid input
   *
   * @param prefix String to serve as generation prompt.
   * @param maxLength Maximum text character length for generation output.
   * @param temperature Double to hold number 0-1 for text generation volatility.
   */
  public StoryManagerImpl(String prefix, int maxLength, Double temperature) {
    this.prefix = prefix;
    this.maxTextLength = maxLength;
    this.temperature = temperature;
    requestFactory = new StoryManagerRequestFactoryImpl();

    if (prefix == null) {
      this.prefix = "";
    }
    if (maxLength < 100) {
      this.maxTextLength = 100;
    }
    if (maxLength > 1000) {
      this.maxTextLength = 1000;
    }
    if (temperature < 0 || temperature > 1) {
      this.temperature = 1.0;
    }
  }

  /**
   * Returns generation prefix.
   *
   * @return String The generation prefix.
   */
  public String getPrefix() {
    return prefix;
  }

  /**
   * Returns maximum length for generation.
   *
   * @return int The maximum length for text generation.
   */
  public int getMaxLength() {
    return maxTextLength;
  }

  /**
   * Returns temperature(volatility of generation).
   *
   * @return Double Numerical quantity representing temperature.
   */
  public Double getTemperature() {
    return temperature;
  }

  /**
   * Makes a post request with a JSON including GPT2 Parameters
   *
   * @returns HttpResponse The reponse from the Generation server.
   */
  private HttpResponse makePostRequestGPT2() throws IOException {
    // Form JSON body using generation parameters
    String requestBody = makeRequestBody(prefix, maxTextLength, temperature);

    // Build Request with Adapter and JSON Input
    HttpRequest request = requestFactory.buildPostRequest(requestBody);
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
  public String generateText() {
    // Obtain response from Server POST Request
    try {
      HttpResponse outputResponse = makePostRequestGPT2();

      // Parse response as JSON
      try {
        JSONObject jsonObject = new JSONObject(outputResponse.parseAsString());
        return jsonObject.getString("text");
      } catch (Exception jsonException) {
        throw new RuntimeException("Failed to convert repsonse into JSON", jsonException);
      }
    } catch (IOException serverException) {
      throw new RuntimeException("Error with server", serverException);
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
  public static String makeRequestBody(String prefix, int maxLength, Double temperature) {
    String requestBody = "{\"length\": " + maxLength
        + ",\"truncate\": \"<|endoftext|>\", \"prefix\": \"" + prefix
        + "\", \"temperature\": " + temperature + "}";
    return requestBody;
  }
}
