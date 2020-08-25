// Copyright 2019 Google LLC
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

import static org.mockito.Mockito.*;

import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.gson.Gson;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Tests for the StoryManagerImpl class.
 */
@RunWith(MockitoJUnitRunner.class)
public final class StoryManagerTest {
  private static final String PREFIX_SAMPLE = "HELLO_WORLD";
  private static final int SIZE_SAMPLE = 100;
  private static final Double TEMPERATURE_SAMPLE = 1.0;
  private StoryManagerURLProvider URLProvider;
  private StoryManager storyManager;

  @Before
  public void setUp() {
    URLProvider = new StoryManagerURLProvider();
  }

  @Test(expected = IllegalArgumentException.class)
  /**
   * Verifies that "" replaces invalid prefix input.
   */
  public void invalidPrefix() {
    storyManager = new StoryManagerImpl(null, SIZE_SAMPLE, TEMPERATURE_SAMPLE, URLProvider);
  }

  /**
   * Verifies that 100 replaces invalid low size input.
   */
  @Test(expected = IllegalArgumentException.class)
  public void lengthTooLow() {
    storyManager = new StoryManagerImpl(PREFIX_SAMPLE, 0, TEMPERATURE_SAMPLE, URLProvider);
  }

  /**
   * Verifies that 1000 replaces invalid low size input.
   */
  @Test(expected = IllegalArgumentException.class)
  public void lengthTooHigh() {
    storyManager = new StoryManagerImpl(PREFIX_SAMPLE, 10000, TEMPERATURE_SAMPLE, URLProvider);
  }

  /**
   * Verifies that 1 replaces invalid temperature input
   */
  @Test(expected = IllegalArgumentException.class)
  public void invalidTemperature() {
    storyManager = new StoryManagerImpl(PREFIX_SAMPLE, SIZE_SAMPLE, 1000.0, URLProvider);
  }

  /**
   * Ensure the method configures a proper POST request and receives
   * proper output translation.
   */
  @Test
  public void correctRequestConfiguration() throws RuntimeException {
    // Declare StoryManager with standard inputs.
    
    storyManager = new StoryManagerImpl(PREFIX_SAMPLE, SIZE_SAMPLE, TEMPERATURE_SAMPLE, URLProvider);

    // Configure fake Request Factory to allow for mock request injection.
    StoryManagerRequestFactoryFakeImpl factoryFake = new StoryManagerRequestFactoryFakeImpl();
    HttpRequest mockRequest = mock(HttpRequest.class);
    factoryFake.setRequest(mockRequest);
    storyManager.setRequestFactory(factoryFake);

    // Configure mockHeaders to ensure correct type setting of request headers.
    HttpHeaders mockHeaders = mock(HttpHeaders.class);
    when(mockRequest.getHeaders()).thenReturn(mockHeaders);

    // Define expected input JSON for post request.
    Gson gson = new Gson();

    HashMap<String, Object> requestMap = new HashMap<>();

    requestMap.put("length", new Integer(SIZE_SAMPLE));
    requestMap.put("truncate", "<|endoftext|>");
    requestMap.put("prefix", PREFIX_SAMPLE);
    requestMap.put("temperature", TEMPERATURE_SAMPLE);

    String expectedRequestString = gson.toJson(requestMap);

    // Define expected output text for generateText()
    String expectedOutput = "foo";
    HashMap<String, Object> sampleJSONMap = new HashMap<>();

    sampleJSONMap.put("text", expectedOutput);

    String sampleJSONOutput = gson.toJson(sampleJSONMap);

    // Stub the mockResponse to return specified text output.
    HttpResponse mockResponse = mock(HttpResponse.class);
    try {
      when(mockRequest.execute()).thenReturn(mockResponse);
      when(mockResponse.parseAsString()).thenReturn(sampleJSONOutput);
    } catch (IOException mockHttpException) {
      throw new RuntimeException(mockHttpException);
    }

    // Obtain actual output text, input body, and verify request configurations.
    String actualOutput = storyManager.generateText();
    String actualRequestString = factoryFake.getLastRequestBody();
    verify(mockHeaders).setContentType("application/json");
    verify(mockRequest).setConnectTimeout(0);
    verify(mockRequest).setReadTimeout(0);

    // Evaluate input body and output text accuracy.
    Assert.assertEquals(expectedRequestString, actualRequestString);
    Assert.assertEquals(expectedOutput, actualOutput);
  }

  @Test
  /**
   * Ensures Provider does not cycle past bounds.
   */
  public void cyclePastBounds() {
    try {
      String firstURL = URLProvider.getCurrentURL();
      URLProvider.cycleURL();
      
      // Cycle through all URLs in first cycle.
      while (firstURL != URLProvider.getCurrentURL()) {
        URLProvider.cycleURL();
      }
    }
    catch(ArrayIndexOutOfBoundsException indexException){
      Assert.fail("cycleURL went out of bounds.");
    }
  }
}
