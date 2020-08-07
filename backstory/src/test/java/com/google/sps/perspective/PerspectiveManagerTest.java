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
import com.google.sps.perspective.PerspectiveStoryAnalysisManager;
import com.google.sps.perspective.data.APINotAvailableException;
import com.google.sps.perspective.data.NoAppropriateStoryException;
import com.google.sps.perspective.data.StoryDecision;
import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/** Quality tests for PerspectiveStoryAnalysisManager */
@RunWith(JUnit4.class)
public final class PerspectiveStoryAnalysisManagerTest {

  /** a PerspectiveAPI object to be used as the input api throughout the tests */
  private static PerspectiveAPI api;
  /** the array of attribute types that we want scores back for */
  private static AttributeType[] desiredTypes;
  /** a Map to be used as the desired scores for PerspectiveAPI to return */
  private static Map<AttributeType, Float> desiredScores;


  @Before 
  public void setUp() {
    // get requested attribute types from PerspectiveStoryAnalysisManager class
    desiredTypes = PerspectiveStoryAnalysisManager.getRequestedAttributes();

    // create empty or default instances to be used in classes
    desiredScores = new HashMap<AttributeType, Float>();
    api = createMockAPI(desiredScores); // will set up an API that only returns -1
  }

  /**
   * Creates a PerspectiveStoryAnalysisManager object with the no-args constructor
   * to check it constructs a non-null object.
   */
  @Test 
  public void checkNoArgsConstructor() {
    try {
      PerspectiveStoryAnalysisManager manager = new PerspectiveStoryAnalysisManager();
      Assert.assertNotNull(manager);
    }
    catch(APINotAvailableException exception) {
      Assert.fail(exception.toString()); // should fail if this exception is thrown
    }
  }

  /**
   * Check that requested attributes gotten are the ones we want.
   */
  @Test
  public void checkRequestedAttributesGetter() {
    Assert.assertEquals(desiredTypes, PerspectiveStoryAnalysisManager.getRequestedAttributes());
  }

  /**
   * Call generateDecision() with an appropriate story 
   * (toxicity manually set below 70%) to ensure that it will
   * give back the correct StoryDecision object
   */
  @Test 
  public void checkAppropriateStory() {
    final float NOT_TOO_TOXIC = .69f;
    desiredScores.put(AttributeType.TOXICITY, NOT_TOO_TOXIC);
    api = createMockAPI(desiredScores);

    PerspectiveStoryAnalysisManager manager = new PerspectiveStoryAnalysisManager(api);
    
    try {
      StoryDecision actual = manager.generateDecision("foo");
      StoryDecision expected = new StoryDecision("foo");

      Assert.assertEquals(expected, actual);
    } catch (NoAppropriateStoryException exception) {
      Assert.fail(exception.toString());
    }
  }

  /**
   * Call generateDecision() with an inappropriate story 
   * (toxicity manually set above 70%) to ensure that it will throw
   * a NoAppropriateStoryException
   */
  @Test 
  public void checkInappropriateStory() {
    final float TOO_TOXIC = .71f;
    desiredScores.put(AttributeType.TOXICITY, TOO_TOXIC);
    api = createMockAPI(desiredScores);

    PerspectiveStoryAnalysisManager manager = new PerspectiveStoryAnalysisManager(api);
    try {
      manager.generateDecision("foo");
    } catch (NoAppropriateStoryException exception) {
      return;
    }

    Assert.fail("NoAppropriateStoryException should have been thrown");
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
    AttributeScore mockAttributeScore = mock(AttributeScore.class);
    Score mockScore = mock(Score.class);

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

        float fakeScore = -1;
        AttributeType type = (AttributeType) args[0];

        if (desiredScores.containsKey(type)) {
          fakeScore = desiredScores.get(type);
        }

        when(mockAttributeScore.getSummaryScore()).thenReturn(mockScore);
        when(mockScore.getValue()).thenReturn(fakeScore);

        return mockAttributeScore;
      }
    });

    return mockAPI;
  }
}
