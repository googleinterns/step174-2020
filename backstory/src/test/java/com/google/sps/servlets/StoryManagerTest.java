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

package com.google.sps.servlets;

import static org.mockito.Mockito.*;

import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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
 * Test the Prompt Creation Suite
 */
@RunWith(MockitoJUnitRunner.class)
public final class StoryManagerTest {
  private static final String PREFIX_SAMPLE = "HELLO_WORLD";
  private static final int SIZE_SAMPLE = 100;
  private static final Double TEMPERATURE_SAMPLE = 1.0;

  private StoryManager storyManager;
  @Before
  public void setUp() {
    storyManager = new StoryManagerImpl(PREFIX_SAMPLE, SIZE_SAMPLE, TEMPERATURE_SAMPLE);
  }

  @Test
  public void invalidPrefix() {
    // Verifies that "" replaces invalid prefix input.
    storyManager = new StoryManagerImpl(null, SIZE_SAMPLE, TEMPERATURE_SAMPLE);

    String expected = "";
    String actual = storyManager.getPrefix();

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void lengthTooLow() {
    // Verifies that 100 replaces invalid low size input.
    storyManager = new StoryManagerImpl(PREFIX_SAMPLE, 0, TEMPERATURE_SAMPLE);

    int expected = 100;
    int actual = storyManager.getMaxLength();

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void lengthTooHigh() {
    // Verifies that 1000 replaces invalid low size input.
    storyManager = new StoryManagerImpl(PREFIX_SAMPLE, 10000, TEMPERATURE_SAMPLE);

    int expected = 1000;
    int actual = storyManager.getMaxLength();

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void invalidTemperature() {
    // Verifies that 1 replaces invalid temperature input
    storyManager = new StoryManagerImpl(PREFIX_SAMPLE, SIZE_SAMPLE, 1000.0);

    Double expected = 1.0;
    Double actual = storyManager.getTemperature();

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void correctRequestConfiguration() throws IOException {
    /* Ensure the method configures a proper POST request and receives
     * proper output translation.
     */

    // Declare StoryManager with standard inputs.
    storyManager = new StoryManagerImpl(PREFIX_SAMPLE, SIZE_SAMPLE, TEMPERATURE_SAMPLE);

    // Configure fake Request Factory to allow for mock request injection.
    StoryManagerRequestFactoryFakeImpl factoryFake = new StoryManagerRequestFactoryFakeImpl();
    HttpRequest mockRequest = mock(HttpRequest.class);
    factoryFake.setRequest(mockRequest);
    storyManager.setRequestFactory(factoryFake);

    // Configure mockHeaders to ensure correct type setting of request headers.
    HttpHeaders mockHeaders = mock(HttpHeaders.class);
    when(mockRequest.getHeaders()).thenReturn(mockHeaders);

    // Define expected input JSON for post request.
    String expectedRequestString = "{\"length\": " + SIZE_SAMPLE
        + ",\"truncate\": \"<|endoftext|>\", \"prefix\": \"" + PREFIX_SAMPLE
        + "\", \"temperature\": " + TEMPERATURE_SAMPLE + "}";

    // Stub the mockResponse to return specified text output.
    String sampleJSONOutput = "{\"text\": \"foo\"}";
    HttpResponse mockResponse = mock(HttpResponse.class);
    try {
      when(mockRequest.execute()).thenReturn(mockResponse);
      when(mockResponse.parseAsString()).thenReturn(sampleJSONOutput);
    } catch (IOException mockHttpException) {
      throw new RuntimeException("Error with mock Http", mockHttpException);
    }

    // Define expected output text for generateText()
    String expectedOutput = "foo";

    // Obtain actual output text, input body, and verify request configurations.
    String actualOutput = storyManager.generateText();
    String actualRequestString = factoryFake.getRequestBody();
    verify(mockHeaders).setContentType("application/json");
    verify(mockRequest).setConnectTimeout(0);
    verify(mockRequest).setReadTimeout(0);

    // Evaluate input body and output text accuracy.
    Assert.assertEquals(expectedRequestString, actualRequestString);
    Assert.assertEquals(expectedOutput, actualOutput);
  }
}