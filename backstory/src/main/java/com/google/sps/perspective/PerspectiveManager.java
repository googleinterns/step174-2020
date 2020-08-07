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

import au.com.origma.perspectiveapi.v1alpha1.PerspectiveAPI;
import au.com.origma.perspectiveapi.v1alpha1.models.AttributeType;
import com.google.sps.perspective.data.APINotAvailableException;
import com.google.sps.perspective.data.ContentDecisions;
import com.google.sps.perspective.data.NoAppropriateStoryException;
import com.google.sps.perspective.data.PerspectiveAPIClient;
import com.google.sps.perspective.data.PerspectiveAPIFactory;
import com.google.sps.perspective.data.PerspectiveAPIFactoryImpl;
import com.google.sps.perspective.data.PerspectiveValues;
import com.google.sps.perspective.data.StoryDecision;
import java.lang.reflect.InvocationTargetException;

/**
 * An implementation of StoryAnalysisManager using PerspectiveAPI for analysis.
 */
public class PerspectiveManager implements StoryAnalysisManager {
  /** an array of all the types we want analysis on */
  private static final AttributeType[] REQUESTED_ATTRIBUTES = {
      AttributeType.ATTACK_ON_AUTHOR,
      AttributeType.ATTACK_ON_COMMENTER,
      AttributeType.FLIRTATION,
      AttributeType.IDENTITY_ATTACK,
      AttributeType.INCOHERENT,
      AttributeType.INSULT,
      AttributeType.LIKELY_TO_REJECT,
      AttributeType.OBSCENE,
      AttributeType.PROFANITY,
      AttributeType.SEVERE_TOXICITY,
      AttributeType.SEXUALLY_EXPLICIT,
      AttributeType.SPAM,
      AttributeType.THREAT,
      AttributeType.TOXICITY,
      AttributeType.UNSUBSTANTIAL,
  };

  /** The perspective API used by this instance. */
  private final PerspectiveAPI perspectiveAPI;

  /**
   * Constructs an object which implements the StoryAnalysisManager
   * class using the Google Perspective API.
   *
   * @throws APINotAvailableException when it can't create an instance of the PerspectiveAPI
   *    (this most likely occurs if the "PerspectiveAPIKey.java" file is not present)
   */
  public PerspectiveManager() throws APINotAvailableException {
    try {
      PerspectiveAPIFactory factory = new PerspectiveAPIFactoryImpl();
      perspectiveAPI = factory.newInstance();
    } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException
        | InvocationTargetException exception) {
      throw new APINotAvailableException("Perspective API is not available: " + exception);
    }
  }

  /**
   * Constructs an object with a specified instance of the PerspectiveAPI.
   * Constructor is to be used for testing.
   *
   * @param perspectiveAPI the instance of the PerspectiveAPI to use
   *     to analyze stories with.
   */
  public PerspectiveManager(PerspectiveAPI perspectiveAPI) {
    this.perspectiveAPI = perspectiveAPI;
  }

  /**
   * Analyzes the passed-in story using the perspective API and returns the decision
   * as a StoryDecision object.
   *
   * @param story The story to be analyzed
   * @return An object describing the recommendation resulting from the analysis.
   * @throws NoAppropriateStoryException if story is not considered appropriate
   */
  public StoryDecision generateDecision(String story) throws NoAppropriateStoryException {
    PerspectiveAPIClient apiClient = new PerspectiveAPIClient(perspectiveAPI);
    PerspectiveValues storyValues = apiClient.analyze(REQUESTED_ATTRIBUTES, story);
    boolean isStoryAppropriate = ContentDecisions.makeDecision(storyValues);

    // if content decisions returns that it's appropriate
    // then return a StoryDecision object with this story
    if (isStoryAppropriate) {
      return new StoryDecision(story);
    }

    // otherwise throw the NoAppropriateStoryException
    throw new NoAppropriateStoryException("The story passed in was not appropriate.");
  }

  /**
   * Gets an array of the AttributeTypes that will be analyzed (mainly for testing).
   *
   * @return An array of the AttributeTypes we're requesting Perspective to analyze
   */
  public static AttributeType[] getRequestedAttributes() {
    return REQUESTED_ATTRIBUTES;
  }
}
