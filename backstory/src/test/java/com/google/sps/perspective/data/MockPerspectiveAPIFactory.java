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

package com.google.sps.perspective.data;

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
import java.util.Map;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/** 
 * An immutable factory that takes in a map of desired scores
 * and can produce instances that return those values.
 */
public class MockPerspectiveAPIFactory implements PerspectiveAPIFactory {
  
  /** the default value that a mockAPI will return for attribute score (not a valid value) */
  private static final int DEFAULT_ATTRIBUTE_SCORE = -1;

  /** a map of the scores the API should return */
  private Map<AttributeType, Float> cannedScores;

  /** the one instance of this API to return */
  private final PerspectiveAPI perspectiveAPI; 

  /** 
   * Constructs a MockPerspective APIFactory object which will return certain scores
   * (those passed in through desiredScores) and DEFAULT_ATTRIBUTE_SCORE if no scores 
   * passed in.
   *
   * @param desiredScores a map of the scores you want the mockAPI to return 
   *    (if you don't set any, the mock version of this API will always return DEFAULT_ATTRIBUTE_SCORE)
   */
  public MockPerspectiveAPIFactory(Map<AttributeType, Float> desiredScores) {
    cannedScores = desiredScores;
    perspectiveAPI = createMockAPI(desiredScores);
  }

  /** 
   * Generates a mock version of the PerspectiveAPI which will return the scores
   * set in the constructor or which returns a default value when there is no desired score
   *
   * @return an instance of a mock Perspective API 
   */
  public PerspectiveAPI newInstance() {
    return perspectiveAPI;
  }

  /** 
   * Returns a mock version of the PerspectiveAPI which will return the scores
   * passed in through desiredScores or which returns DEFAULT_ATTRIBUTE_SCORE when there is no desired score
   * (i.e. the map does not contain that attribute type as a key).
   *
   * @param desiredScores a map of the scores you want the mockAPI to return 
   *    (if you don't set any, the mock version of this API will always return DEFAULT_ATTRIBUTE_SCORE)
   * @return the mocked version of the PerspectiveAPI
   */
  private PerspectiveAPI createMockAPI(Map<AttributeType, Float> desiredScores) {
    // instantiate all the necessary mock objects
    PerspectiveAPI mockAPI = mock(PerspectiveAPI.class);
    AnalyzeCommentResponse mockResponse = mock(AnalyzeCommentResponse.class);

    // when analyze is called for our mockAPI, return the mockResponse
    when(mockAPI.analyze(any(AnalyzeCommentRequest.class))).thenReturn(mockResponse);

    // when getAttributeScore is called we need to be able to grab the type it is looking for
    // then, check if this AttributeType is in the map and if it is, grab its value, and
    // set up the next few calls so that this value will be returned. if it isn't in the map,
    // set up the next few calls so default score will be returned (this signifies no particular 
    // value was desired).
    when(mockResponse.getAttributeScore(any(AttributeType.class))).thenAnswer(new Answer() {
      public Object answer(InvocationOnMock invocation) {
        Object[] args = invocation.getArguments();

        AttributeType type = (AttributeType) args[0];

        float scoreValue = DEFAULT_ATTRIBUTE_SCORE;
        if (desiredScores.containsKey(type)) {
          scoreValue = desiredScores.get(type);
        }

        // create a mocked score with the desired scoreValue and null ScoreType
        Score score = new Score(scoreValue, null);
        // create a mocked score with mockScore as summary score and null list of spanscores
        AttributeScore attributeScore = new AttributeScore(score, null);

        return attributeScore;
      }
    });

    return mockAPI;
  }
}
