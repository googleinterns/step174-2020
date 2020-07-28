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

import au.com.origma.perspectiveapi.v1alpha1.models.AnalyzeCommentRequest;
import au.com.origma.perspectiveapi.v1alpha1.models.AnalyzeCommentRequest.Builder;
import au.com.origma.perspectiveapi.v1alpha1.models.AnalyzeCommentResponse;
import au.com.origma.perspectiveapi.v1alpha1.models.AttributeScore;
import au.com.origma.perspectiveapi.v1alpha1.models.AttributeType;
import au.com.origma.perspectiveapi.v1alpha1.models.ContentType;
import au.com.origma.perspectiveapi.v1alpha1.models.Entry;
import au.com.origma.perspectiveapi.v1alpha1.models.RequestedAttribute;
import au.com.origma.perspectiveapi.v1alpha1.models.Score;
import au.com.origma.perspectiveapi.v1alpha1.PerspectiveAPI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class PerspectiveAnalysis {

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

  public PerspectiveAnalysis(PerspectiveAPI perspective, String text) throws NullPointerException {
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

    for (int i = 0; i < types.length; i++) {
      analyses.put(types[i], fetchScore(response, types[i]));
    }
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
  public Map<AttributeType, Float> getAnalyses() {
    return analyses;
  }

  /**
   * private helper method to fetch the score for a given response & type
   *
   * @return the score for the response for a given type
   */
  private float fetchScore(AnalyzeCommentResponse response, AttributeType type) {
    AttributeScore attributeScore = response.getAttributeScore(type);
    Score score = attributeScore.getSummaryScore();
    return score.getValue();
  }
}
