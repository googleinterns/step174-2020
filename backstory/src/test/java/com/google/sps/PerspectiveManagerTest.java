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
import com.google.sps.data.perspective.PerspectiveManager;
import com.google.sps.data.perspective.PerspectiveManagerImpl;
import java.util.HashMap;
import java.util.List;
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

/** Quality tests for PerspectiveManager */
@RunWith(JUnit4.class)
public final class PerspectiveManagerTest {

  @Test (expected = IllegalArgumentException.class)
  public void nullTextInput() {
    // Call the PerspectiveManager constructor with null input for text
    // Should throw an IllegalArgumentException

    Map<AttributeType, Float> input = new HashMap<AttributeType, Float>();
    PerspectiveAPI api = setUpMockAPI(input);
    
    PerspectiveManager manager = new PerspectiveManagerImpl(api, null);
  }

  @Test (expected = IllegalArgumentException.class)
  public void emptyTextInput() {
    // Call the PerspectiveManager constructor with an empty String for text
    // Should throw an IllegalArgumentException

    Map<AttributeType, Float> input = new HashMap<AttributeType, Float>();
    PerspectiveAPI api = setUpMockAPI(input);
    
    PerspectiveManager manager = new PerspectiveManagerImpl(api, "");
  }

  @Test
  public void standardCase() {
    // Check that when PerspectiveManager created with no desired values,
    // you get all the correct outputs: a map of -1 values, the correct text ("foo")
    // & the correct decision (true, which means it is appropriate).
    
    Map<AttributeType, Float> input = new HashMap<AttributeType, Float>();
    PerspectiveAPI api = setUpMockAPI(input);

    PerspectiveManager manager = new PerspectiveManagerImpl(api, "foo");
    
    Assert.assertEquals("foo", manager.getText());

    Map<AttributeType, Float> output = manager.getAnalyses();
    for(AttributeType type: output.keySet()) {
      Assert.assertEquals(-1f, output.get(type), 0);
    }

    // -1 of toxicity is < .7 (the current rule) so should be considered appropriate
    Assert.assertEquals(true, manager.getDecision());
  }

  @Test 
  public void checkSizeOfMap() {
    // Check that PerspectiveManager analyzes and returns all 15 of our desired attributes
    Map<AttributeType, Float> input = new HashMap<AttributeType, Float>();
    PerspectiveAPI api = setUpMockAPI(input);

    PerspectiveManager manager = new PerspectiveManagerImpl(api, "foo");
    Map<AttributeType, Float> output = manager.getAnalyses();

    Assert.assertEquals(15, output.size());
  }

  @Test
  public void checkDecisionBasedOnToxicity() {
    // Check that PerspectiveManager makes the right decision when given 
    // inputs with different levels of toxicity). getDecision() should return
    // true if toxicity < .7 and false if toxicity >= .7
    
    Map<AttributeType, Float> input = new HashMap<AttributeType, Float>();
    PerspectiveAPI api;

    // check that if below .7, it returns true
    final float LOW_TOXICITY = .69f;
    input.put(AttributeType.TOXICITY, LOW_TOXICITY);
    api = setUpMockAPI(input);

    PerspectiveManager lowToxicity = new PerspectiveManagerImpl(api, "foo");
    
    Assert.assertEquals(LOW_TOXICITY, lowToxicity.getAnalyses().get(AttributeType.TOXICITY), 0);
    Assert.assertEquals(true, lowToxicity.getDecision());

    // check that if equal to .7, it returns false
    final float EQUAL_TOXICITY = .7f;
    input.put(AttributeType.TOXICITY, EQUAL_TOXICITY);
    api = setUpMockAPI(input);

    PerspectiveManager equalToxicity = new PerspectiveManagerImpl(api, "foo");
    
    Assert.assertEquals(EQUAL_TOXICITY, equalToxicity.getAnalyses().get(AttributeType.TOXICITY), 0);
    Assert.assertEquals(false, equalToxicity.getDecision());

    // check that if above .7, it returns false
    final float HIGH_TOXICITY = .71f;
    input.put(AttributeType.TOXICITY, HIGH_TOXICITY);
    api = setUpMockAPI(input);

    PerspectiveManager highToxicity = new PerspectiveManagerImpl(api, "foo");
    
    Assert.assertEquals(HIGH_TOXICITY, highToxicity.getAnalyses().get(AttributeType.TOXICITY), 0);
    Assert.assertEquals(false, highToxicity.getDecision());
  }

  /** 
   * Returns a mock version of the PerspectiveAPI returns the scores that you set in desiredScores
   * or which returns -1 when AttributeType scores are set.
   *
   * @param desiredScores a map of the scores you want the mockAPI to return 
   *    (if you don't set any, the mock version of this API will always return -1)
   * @return the mocked version of the PerspectiveAPI
   */
  private static PerspectiveAPI setUpMockAPI(Map<AttributeType, Float> desiredScores) {
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