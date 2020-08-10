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

import au.com.origma.perspectiveapi.v1alpha1.PerspectiveAPI;
import au.com.origma.perspectiveapi.v1alpha1.models.AnalyzeCommentRequest;
import au.com.origma.perspectiveapi.v1alpha1.models.AnalyzeCommentResponse;
import au.com.origma.perspectiveapi.v1alpha1.models.AttributeScore;
import au.com.origma.perspectiveapi.v1alpha1.models.AttributeType;
import au.com.origma.perspectiveapi.v1alpha1.models.Score;
import com.google.sps.perspective.PerspectiveStoryAnalysisManager;
import com.google.sps.perspective.data.APINotAvailableException;
import com.google.sps.perspective.data.MockPerspectiveAPIFactory;
import com.google.sps.perspective.data.NoAppropriateStoryException;
import com.google.sps.perspective.data.StoryDecision;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.Assert;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.mockito.stubbing.ValidableAnswer;


/** Quality tests for PerspectiveStoryAnalysisManager */
@RunWith(JUnit4.class)
public final class PerspectiveStoryAnalysisManagerTest {

  /** a String of default text to use in tests */
  private static final String DEFAULT_TEXT = "foo";

  /** a factory to produce instances of mock Perspective APIs for the tests */
  private static MockPerspectiveAPIFactory factory;


  @Before 
  public void setUp() {
    // create a default instance of PerspectiveAPIFactory to use in any test 
    // which doesn't need specified values (will just return the default value)
    Map<AttributeType, Float> noDesiredScores = new HashMap<AttributeType, Float>();
    factory = new MockPerspectiveAPIFactory(noDesiredScores);
  }

  /**
   * Checks that the correct story is passed to the PerspectiveAPI 
   * by checking the AnalyzeCommentRequest.
   */
  @Test 
  public void checkStoryPassed() {
    PerspectiveAPI mockAPI = factory.newInstance();
    String story = DEFAULT_TEXT;

    try { 
      PerspectiveStoryAnalysisManager manager = new PerspectiveStoryAnalysisManager(mockAPI);
      manager.generateDecision(story);
    } catch (NoAppropriateStoryException exception) {/* nothing needs to happen here */}

    ArgumentCaptor<AnalyzeCommentRequest> requestCaptor = ArgumentCaptor.forClass(AnalyzeCommentRequest.class);
    verify(mockAPI).analyze(requestCaptor.capture()); 

    AnalyzeCommentRequest request = requestCaptor.getValue();
    
    // check that story passed to PerspectiveAPI was same 
    if (request != null) { 
      Assert.assertEquals(story, request.getComment().getText());
    } else {
      Assert.fail("analyze() was not even called for the mock API.");
    }
  }

  /**
   * Checks that the correct attributes are requested of PerspectiveAPI
   * by checking the AnalyzeCommentRequest.
   */
  @Test 
  public void checkAttributesRequested() {
    PerspectiveAPI mockAPI = factory.newInstance();

    try { 
      PerspectiveStoryAnalysisManager manager = new PerspectiveStoryAnalysisManager(mockAPI);
      manager.generateDecision(DEFAULT_TEXT);
    } catch (NoAppropriateStoryException exception) {/* nothing needs to happen here */}

    ArgumentCaptor<AnalyzeCommentRequest> requestCaptor = ArgumentCaptor.forClass(AnalyzeCommentRequest.class);
    verify(mockAPI).analyze(requestCaptor.capture()); 

    AnalyzeCommentRequest request = requestCaptor.getValue();

    // check that the request PerspectiveAPI gets has the correct attribute types to analyze
    if (request != null) {
      Map<AttributeType, Object> expectedAttributes = new HashMap<AttributeType, Object>();
      
      for (AttributeType type: PerspectiveStoryAnalysisManager.REQUESTED_ATTRIBUTES) {
        expectedAttributes.put(type, null);
      }

      Assert.assertEquals(expectedAttributes.keySet(), request.getRequestedAttributes().keySet());
    } else {
      Assert.fail("analyze() was not even called for the mock API.");
    }
  }

  /**
   * Call generateDecision() with an appropriate story 
   * (toxicity manually set below 70%) to ensure that it will
   * give back the correct StoryDecision object
   */
  @Test
  public void checkAppropriateStory() {
    final float NOT_TOO_TOXIC = .69f;

    Map<AttributeType, Float> desiredScores = new HashMap<AttributeType, Float>();

    desiredScores.put(AttributeType.TOXICITY, NOT_TOO_TOXIC);
    MockPerspectiveAPIFactory factory = new MockPerspectiveAPIFactory(desiredScores);

    PerspectiveStoryAnalysisManager manager = new PerspectiveStoryAnalysisManager(factory.newInstance());
    
    try {
      StoryDecision actual = manager.generateDecision(DEFAULT_TEXT);
      StoryDecision expected = new StoryDecision(DEFAULT_TEXT);

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

    Map<AttributeType, Float> desiredScores = new HashMap<AttributeType, Float>();
    desiredScores.put(AttributeType.TOXICITY, TOO_TOXIC);
    
    MockPerspectiveAPIFactory factory = new MockPerspectiveAPIFactory(desiredScores);

    PerspectiveStoryAnalysisManager manager = new PerspectiveStoryAnalysisManager(factory.newInstance());

    try {
      manager.generateDecision(DEFAULT_TEXT);
    } catch (NoAppropriateStoryException exception) {
      return;
    }

    Assert.fail("NoAppropriateStoryException should have been thrown");
  }
}
