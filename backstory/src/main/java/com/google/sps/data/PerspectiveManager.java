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

package com.google.sps.data;

import au.com.origma.perspectiveapi.v1alpha1.PerspectiveAPI;
import au.com.origma.perspectiveapi.v1alpha1.models.AnalyzeCommentRequest;
import au.com.origma.perspectiveapi.v1alpha1.models.AnalyzeCommentRequest.Builder;
import au.com.origma.perspectiveapi.v1alpha1.models.AnalyzeCommentResponse;
import au.com.origma.perspectiveapi.v1alpha1.models.AttributeScore;
import au.com.origma.perspectiveapi.v1alpha1.models.AttributeType;
import au.com.origma.perspectiveapi.v1alpha1.models.ContentType;
import au.com.origma.perspectiveapi.v1alpha1.models.Entry;
import au.com.origma.perspectiveapi.v1alpha1.models.RequestedAttribute;
import au.com.origma.perspectiveapi.v1alpha1.models.Score;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * An Object that holds a text and all of the desired attribute scores from the Perspective
 * API for that text. The main functionality for this class is for it to use the scores
 * from the Perspective API stored in its final variables to make a decision on the
 * appropriateness of the passed-in text.
 */
public class PerspectiveManager {
  /** an array of all the types we want analysis on */
  private static final AttributeType[] types = {AttributeType.ATTACK_ON_AUTHOR,
      AttributeType.ATTACK_ON_COMMENTER, AttributeType.FLIRTATION, AttributeType.IDENTITY_ATTACK,
      AttributeType.INCOHERENT, AttributeType.INSULT, AttributeType.LIKELY_TO_REJECT,
      AttributeType.OBSCENE, AttributeType.PROFANITY, AttributeType.SEVERE_TOXICITY,
      AttributeType.SEXUALLY_EXPLICIT, AttributeType.SPAM, AttributeType.THREAT,
      AttributeType.TOXICITY, AttributeType.UNSUBSTANTIAL};

  /** the text that generated these scores */
  private final String text;

  /** a map with type-score key-value pairs from the analysis of the text */
  private final Map<AttributeType, Float> analyses;

  /** a boolean that holds the decision on if the text is appropriate or not */
  private final boolean decision;

  /**
   * Constructs a PerspectiveManager object with an instance of the PerspectiveAPI and
   * a text to analyze using Perspective.
   *
   * @param perspective an instance of the Perspective API to analyze text with
   * @param text the text to analyze (should NOT be null or empty)
   * @throws IllegalArgumentException if text is empty or null
   */
  public PerspectiveManager(PerspectiveAPI perspective, String text)
      throws IllegalArgumentException {
    if (text == null || "".equals(text)) {
      throw new IllegalArgumentException(
          "Cannot create a PerspectiveManager with empty or null text");
    }

    this.text = text;

    AnalyzeCommentRequest.Builder builder = new AnalyzeCommentRequest.Builder().comment(
        new Entry.Builder().type(ContentType.PLAIN_TEXT).text(text).build());

    // add all the types we want to this builder
    for (AttributeType type : types) {
      builder.addRequestedAttribute(type, null);
    }

    AnalyzeCommentRequest request = builder.build();
    AnalyzeCommentResponse response = perspective.analyze(request);

    analyses = new HashMap<AttributeType, Float>();

    // put all the analyses for the types desired (those in types array) in the map
    for (int i = 0; i < types.length; i++) {
      analyses.put(types[i], fetchScore(response, types[i]));
    }

    decision = makeDecision();
  }

  /**
   * Returns text that was analyzed by Perspective in this class
   *
   * @return text that was analyzed
   */
  public String getText() {
    return text;
  }

  /**
   * Returns the Map of all AttributeTypes and the corresponding scores.
   *
   * @return analysis scores of text
   */
  public Map<AttributeType, Float> getAnalyses() {
    return analyses;
  }

  /**
   * Returns the decision on the appropriateness of the text
   *
   * @return true, if content considered appropriate; false, otherwise
   */
  public boolean getDecision() {
    return decision;
  }

  /**
   * private helper method to fetch the score for a given response & type
   *
   * @param response the response which holds the analyses
   * @param type the type of attribute to fetch the score for
   * @return the score for the response for a given type
   */
  private float fetchScore(AnalyzeCommentResponse response, AttributeType type) {
    AttributeScore attributeScore = response.getAttributeScore(type);
    Score score = attributeScore.getSummaryScore();
    return score.getValue();
  }

  /**
   * internal method called in constructor to make decision on appropriateness
   *
   * @return true, if it considers text appropriate, false otherwise
   */
  private boolean makeDecision() {
    // currently decision is just based on if text is considered toxic (if it is, it's not
    // appropriate)
    return !ContentAnalysis.isToxic(analyses);
  }
}
