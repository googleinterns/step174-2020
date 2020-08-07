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

package com.google.sps;

import static org.mockito.Mockito.*;

import au.com.origma.perspectiveapi.v1alpha1.PerspectiveAPI;
import au.com.origma.perspectiveapi.v1alpha1.models.AnalyzeCommentRequest;
import au.com.origma.perspectiveapi.v1alpha1.models.AnalyzeCommentResponse;
import au.com.origma.perspectiveapi.v1alpha1.models.AttributeScore;
import au.com.origma.perspectiveapi.v1alpha1.models.AttributeType;
import au.com.origma.perspectiveapi.v1alpha1.models.Score;
import com.google.sps.perspective.PerspectiveAPIClient;
import com.google.sps.perspective.PerspectiveValues;
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

  /** an instance of the PerspectiveAPIClient to use throughout our tests */
  private static PerspectiveAPIClient client;
  /** a mock PerspectiveAPI object to be used as the input api throughout the tests */
  private static PerspectiveAPI mockAPI;
  /** the array of attribute types that we want scores back for */
  private static final AttributeType[] desiredTypes = { AttributeType.PROFANITY, AttributeType.TOXICITY, AttributeType.UNSUBSTANTIAL };;


  @Before 
  public void setUp() {
    // will set up an API that only returns -1
    mockAPI = createMockAPI(new HashMap<AttributeType, Float>()); 

    client = new PerspectiveAPIClient(mockAPI);
  }

  /**
   * Construct a PerspectiveAPIClient with a null PerspectiveAPI to ensure 
   * that an IllegalArgumentException will be thrown.
   */
  @Test (expected = IllegalArgumentException.class)
  public void nullAPIInput() {
    client = new PerspectiveAPIClient(null); 
  }

  /**
   * Call analyze() with a null Attribute[] to ensure 
   * that an IllegalArgumentException will be thrown.
   */
  @Test (expected = IllegalArgumentException.class)
  public void nullAttributeArrayInput() {
    client.analyze(null, "foo");
  }

  /**
   * Call analyze() with a null text input to ensure 
   * that an IllegalArgumentException will be thrown.
   */
  @Test (expected = IllegalArgumentException.class)
  public void nullTextInput() {
    client.analyze(desiredTypes, null);
  }

  /**
   * Call analyze() with an empty text input to ensure 
   * that an IllegalArgumentException will be thrown.
   */
  @Test (expected = IllegalArgumentException.class)
  public void emptyTextInput() {
    client.analyze(desiredTypes, "");
  }

  /**
   * Check that analyze() returns a PerspectiveValues object
   * with a map of the correct size.
   */
  @Test
  public void checkSizeOfMap() {
    PerspectiveValues values = client.analyze(desiredTypes, "foo");

    Assert.assertEquals(desiredTypes.length, values.getAttributeTypesToScores().size());
  }

  /**
   * Check that analyze() returns a PerspectiveValues
   * object with the same text as passed in.
   */
  @Test
  public void checkTextSame() {
    PerspectiveValues values = client.analyze(desiredTypes, "foo");

    Assert.assertEquals("foo", values.getText());
  }

  /**
   * Check that analyze() returns a PerspectiveValues
   * object with the right scores.
   */
  @Test
  public void checkRightScores() {
    // a Map to be used as the desired scores for the mock API to return 
    Map desiredScores = new HashMap<AttributeType, Float>();

    // add each of the desiredTypes and a random float between 0 & 1 as their score 
    for (AttributeType type: desiredTypes) {
      Random rand = new Random();
     
      desiredScores.put(type, rand.nextFloat());
    }

    mockAPI = createMockAPI(desiredScores);
    client = new PerspectiveAPIClient(mockAPI);
    
    // get the PerspectiveValues object and specifically the output analyses (scores from PerspectiveAPI)
    PerspectiveValues values = client.analyze(desiredTypes, "foo");
    Map<AttributeType, Float> outputScores = values.getAttributeTypesToScores();

    // check all the right scores were returned from analyze()
    for (AttributeType type: desiredTypes) {
      Assert.assertEquals((float) desiredScores.get(type), outputScores.get(type), 0);
    }
  }

  /** 
   * Returns a mock version of the PerspectiveAPI which will return the scores
   * passed in through desiredScores or which returns -1 when there is no desired score
   * (i.e. the map does not contain that attribute type as a key).
   *
   * @param desiredScores a map of the scores you want the mockAPI to return 
   *    (if you don't set any, the mock version of this API will always return -1)
   * @return the mocked version of the PerspectiveAPI
   */
  private static PerspectiveAPI createMockAPI(Map<AttributeType, Float> desiredScores) {
    // instantiate all the necessary mock objects
    PerspectiveAPI mockAPI = mock(PerspectiveAPI.class);
    AnalyzeCommentResponse mockResponse = mock(AnalyzeCommentResponse.class);

    // when analyze is called for our mockAPI, return the mockResponse
    when(mockAPI.analyze(any(AnalyzeCommentRequest.class))).thenReturn(mockResponse);

    // when getAttributeScore is called we need to be able to grab the type it is looking for
    // then, check if this AttributeType is in the map and if it is, grab its value, and
    // set up the next few calls so that this value will be returned. if it isn't in the map,
    // set up the next few calls so -1 will be returned (-1 signifies no particular value was desired).
    when(mockResponse.getAttributeScore(any(AttributeType.class))).thenAnswer(new Answer() {
      public Object answer(InvocationOnMock invocation) {
        Object[] args = invocation.getArguments();
        Object mock = invocation.getMock();

        AttributeType type = (AttributeType) args[0];

        float scoreValue = -1;
        if (desiredScores.containsKey(type)) {
          scoreValue = desiredScores.get(type);
        }

        // create a mocked score with the desired scoreValue and null ScoreType
        Score mockScore = new Score(scoreValue, null);
        // create a mocked score with mockScore as summary score and null list of spanscores
        AttributeScore mockAttributeScore = new AttributeScore(mockScore, null);

        return mockAttributeScore;
      }
    });

    return mockAPI;
  }
}
