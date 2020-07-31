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

package com.google.sps.data.perspective;

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
 * A class to hold all of the attribute scores from Perspective for a specified
 * piece of text.
 */
public class PerspectiveAnalysis {

  /** an array of all the types we want analysis on */
  public static final AttributeType[] ANALYSIS_TYPES = {AttributeType.ATTACK_ON_AUTHOR,
      AttributeType.ATTACK_ON_COMMENTER, AttributeType.FLIRTATION, AttributeType.IDENTITY_ATTACK,
      AttributeType.INCOHERENT, AttributeType.INSULT, AttributeType.LIKELY_TO_REJECT,
      AttributeType.OBSCENE, AttributeType.PROFANITY, AttributeType.SEVERE_TOXICITY,
      AttributeType.SEXUALLY_EXPLICIT, AttributeType.SPAM, AttributeType.THREAT,
      AttributeType.UNSUBSTANTIAL};

  /** the text that generated these scores */
  private final String text;

  /** the scores & their types from the analysis of the text */
  private final AttributeAnalysis[] analyses;

  /** 
   * Constructs a PerspectiveAnalysis object from text & analyses parameters
   */ 
  public PerspectiveAnalysis(String text, AttributeAnalysis[] analyses) {
    this.text = text;
    this.analyses = analyses;
  }

  /**
   * @return text that was analyzed
   */
  public String getText() {
    return text;
  }

  /**
   * @return analysis scores of text
   */
  public AttributeAnalysis[] getAnalyses() {
    return analyses;
  }
}
