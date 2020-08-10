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

package com.google.sps.perspective;

import static org.mockito.Mockito.*;

import au.com.origma.perspectiveapi.v1alpha1.models.AttributeType;
import com.google.common.collect.ImmutableList;
import com.google.sps.perspective.data.MockPerspectiveAPIFactory;
import com.google.sps.perspective.data.PerspectiveAPIClient;
import com.google.sps.perspective.data.PerspectiveValues;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/** Quality tests for PerspectiveAPIClient */
@RunWith(JUnit4.class)
public final class PerspectiveAPIClientTest {

  /** a String of default text to use in tests */
  private static final String DEFAULT_TEXT = "foo";
  /** an immutable (b/c we want it to be a constant) list of attribute types that we want scores back for */
  private static final ImmutableList<AttributeType> DESIRED_TYPES = ImmutableList.of(AttributeType.PROFANITY, AttributeType.TOXICITY, AttributeType.UNSUBSTANTIAL);

  /**
   * Construct a PerspectiveAPIClient with a null PerspectiveAPI to ensure 
   * that an IllegalArgumentException will be thrown.
   */
  @Test (expected = IllegalArgumentException.class)
  public void nullAPIInput() {
    PerspectiveAPIClient client = new PerspectiveAPIClient(null); 
  }

  /**
   * Call analyze() with a null Attribute[] to ensure 
   * that an IllegalArgumentException will be thrown.
   */
  @Test (expected = IllegalArgumentException.class)
  public void nullAttributeArrayInput() {
    MockPerspectiveAPIFactory factory = new MockPerspectiveAPIFactory(new HashMap<AttributeType, Float>()); 
    PerspectiveAPIClient client = new PerspectiveAPIClient(factory.newInstance());

    client.analyze(null, DEFAULT_TEXT);
  }

  /**
   * Call analyze() with a null text input to ensure 
   * that an IllegalArgumentException will be thrown.
   */
  @Test (expected = IllegalArgumentException.class)
  public void nullTextInput() {
    MockPerspectiveAPIFactory factory = new MockPerspectiveAPIFactory(new HashMap<AttributeType, Float>());  
    PerspectiveAPIClient client = new PerspectiveAPIClient(factory.newInstance());

    client.analyze(DESIRED_TYPES, null);
  }

  /**
   * Call analyze() with an empty text input to ensure 
   * that an IllegalArgumentException will be thrown.
   */
  @Test (expected = IllegalArgumentException.class)
  public void emptyTextInput() {
    MockPerspectiveAPIFactory factory = new MockPerspectiveAPIFactory(new HashMap<AttributeType, Float>()); 
    PerspectiveAPIClient client = new PerspectiveAPIClient(factory.newInstance());

    client.analyze(DESIRED_TYPES, "");
  }

  /**
   * Check that analyze() returns a PerspectiveValues object
   * with a map of the correct size.
   */
  @Test
  public void checkSizeOfMap() {
    MockPerspectiveAPIFactory factory = new MockPerspectiveAPIFactory(new HashMap<AttributeType, Float>()); 
    PerspectiveAPIClient client = new PerspectiveAPIClient(factory.newInstance());

    PerspectiveValues values = client.analyze(DESIRED_TYPES, DEFAULT_TEXT);

    Assert.assertEquals(DESIRED_TYPES.size(), values.getAttributeTypesToScores().size());
  }

  /**
   * Check that analyze() returns a PerspectiveValues
   * object with the same text as passed in.
   */
  @Test
  public void checkTextSame() {
    MockPerspectiveAPIFactory factory = new MockPerspectiveAPIFactory(new HashMap<AttributeType, Float>());  
    PerspectiveAPIClient client = new PerspectiveAPIClient(factory.newInstance());

    PerspectiveValues values = client.analyze(DESIRED_TYPES, DEFAULT_TEXT);

    Assert.assertEquals(DEFAULT_TEXT, values.getText());
  }

  /**
   * Check that analyze() returns a PerspectiveValues
   * object with the right scores.
   */
  @Test
  public void checkRightScores() {
    // a Map to be used as the desired scores for the mock API to return 
    Map desiredScores = new HashMap<AttributeType, Float>();

    // add each of the DESIRED_TYPES and a random float between 0 & 1 as their score 
    for (AttributeType type: DESIRED_TYPES) {
      Random random = new Random();
     
      desiredScores.put(type, random.nextFloat());
    }

    MockPerspectiveAPIFactory factory = new MockPerspectiveAPIFactory(desiredScores); 
    PerspectiveAPIClient client = new PerspectiveAPIClient(factory.newInstance());
    
    // get the PerspectiveValues object and specifically the output analyses (scores from PerspectiveAPI)
    PerspectiveValues values = client.analyze(DESIRED_TYPES, DEFAULT_TEXT);
    Map<AttributeType, Float> outputScores = values.getAttributeTypesToScores();

    // check all the right scores were returned from analyze()
    for (AttributeType type: DESIRED_TYPES) {
      Assert.assertEquals((float) desiredScores.get(type), outputScores.get(type), 0);
    }
  }

  /**
   * Check that the correct AnalyzeCommentRequest is passed to the Perspective API 
   * by checking all values of AnalyzeCommentRequest that are set by PerspectiveAPIClient.
   */
  @Test
  public void checkRequest() {
    MockPerspectiveAPIFactory factory = new MockPerspectiveAPIFactory(new HashMap<AttributeType, Float>()); 
    PerspectiveAPI mockAPI = factory.newInstance();
    PerspectiveAPIClient client = new PerspectiveAPIClient(mockAPI);

    PerspectiveValues values = client.analyze(DESIRED_TYPES, DEFAULT_TEXT);

    ArgumentCaptor<AnalyzeCommentRequest> requestCaptor = ArgumentCaptor.forClass(AnalyzeCommentRequest.class);
    verify(mockAPI).analyze(requestCaptor.capture()); 

    AnalyzeCommentRequest actual = requestCaptor.getValue();

    // check that the entry to score and requested attributes are as expected
    Assert.assertEquals(DEFAULT_TEXT, actual.getComment().getText()); // check text
    Assert.assertEquals(ContentType.PLAIN_TEXT, actual.getComment().getType()); // check content type

    Map<AttributeType, RequestedAttribute> requestedAttributes = actual.getRequestedAttributes();

    Assert.assertEquals(DESIRED_TYPES.size(), requestedAttributes.size()); // check number of requested types
    
    // check that the types requested match
    for (AttributeType type: DESIRED_TYPES) {
      Assert.assertTrue(requestedAttributes.containsKey(type));
    }
  }
}
