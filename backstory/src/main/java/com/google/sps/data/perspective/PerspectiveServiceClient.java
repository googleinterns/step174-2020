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

/**
 * A service client to access the Perspective API 
 */
public class PerspectiveServiceClient {
  public static PerspectiveAnalysis analyze(PerspectiveAPI perspective, AttributeType[] types, String text) {
    AnalyzeCommentRequest.Builder builder = new AnalyzeCommentRequest.Builder().comment(
        new Entry.Builder().type(ContentType.PLAIN_TEXT).text(text).build());

    // add all the types we want to this builder
    for (AttributeType type : types) {
      builder.addRequestedAttribute(type, null);
    }

    AnalyzeCommentRequest request = builder.build();
    AnalyzeCommentResponse response = perspective.analyze(request);
    
    AttributeAnalysis[] analyses = new AttributeAnalysis[types.length];

    for (int i = 0; i < types.length; i++) {
      analyses[i] = new AttributeAnalysis(fetchScore(response, types[i]), types[i]);
    }

    // Convert response to PerspectiveAnalysis.
    PerspectiveAnalysis analysis = new PerspectiveAnalysis(text, analyses);

    return analysis;
  }

  /**
   * private helper method to fetch the score for a given response & type
   *
   * @return the score for the response for a given type
   */
  private static float fetchScore(AnalyzeCommentResponse response, AttributeType type) {
    AttributeScore attributeScore = response.getAttributeScore(type);
    Score score = attributeScore.getSummaryScore();
    return score.getValue();
  }
}
