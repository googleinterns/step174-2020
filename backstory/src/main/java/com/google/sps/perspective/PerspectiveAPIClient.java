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
import au.com.origma.perspectiveapi.v1alpha1.models.AnalyzeCommentRequest;
import au.com.origma.perspectiveapi.v1alpha1.models.AnalyzeCommentRequest.Builder;
import au.com.origma.perspectiveapi.v1alpha1.models.AnalyzeCommentResponse;
import au.com.origma.perspectiveapi.v1alpha1.models.AttributeScore;
import au.com.origma.perspectiveapi.v1alpha1.models.AttributeType;
import au.com.origma.perspectiveapi.v1alpha1.models.ContentType;
import au.com.origma.perspectiveapi.v1alpha1.models.Entry;
import au.com.origma.perspectiveapi.v1alpha1.models.RequestedAttribute;
import au.com.origma.perspectiveapi.v1alpha1.models.Score;
import java.util.HashMap;
import java.util.Map;

/**
 * A client to analyze text using the Perspective API
 */
public class PerspectiveAPIClient {
  /** This client's PerspectiveAPI instance */
  private final PerspectiveAPI perspective;

  /**
   * Constructs a PerspectiveAPIClient by setting the PerspectiveAPI instance the client
   * should use to analyze text.
   *
   * @param perspective an instance of the PerspectiveAPI to use to analyze the text for the
   *     requested attributeTypes
   * @throws IllegalArgumentException if the PerspectiveAPI instance is null
   */
  public PerspectiveAPIClient(PerspectiveAPI perspective) throws IllegalArgumentException {
    if (perspective == null) {
      throw new IllegalArgumentException("PerspectiveAPI argument cannot be null.");
    }

    this.perspective = perspective;
  }

  /**
   * Return a PerspectiveValues object with analysis of all of the requested types for a specified
   * text.
   *
   * @param attributeTypes the requested attribute types to analyze the text for
   * @param text the text to be analyzed by Perspective API
   * @return an PerspectiveValues object containing all of the scores from the PerspectiveAPI
   * @throws IllegalArgumentException if either argument is null or if text is empty
   */
  public PerspectiveValues analyze(AttributeType[] attributeTypes, String text) {
    if (attributeTypes == null) {
      throw new IllegalArgumentException("The array of attribute types cannot be null.");
    } else if (text == null) {
      throw new IllegalArgumentException("The text to be analyzed cannot be null");
    } else if (text.equals("")) {
      // it can't be empty b/c if it is, when attempting to analyze it with the PerspectiveAPI
      // a NullPointerException will be thrown
      throw new IllegalArgumentException("The text to be analyzed cannot be empty");
    }

    AnalyzeCommentRequest.Builder builder = new AnalyzeCommentRequest.Builder().comment(
        new Entry.Builder().type(ContentType.PLAIN_TEXT).text(text).build());

    // add all the attribute types we want to this builder
    for (AttributeType attributeType : attributeTypes) {
      builder.addRequestedAttribute(attributeType, null);
    }

    AnalyzeCommentRequest request = builder.build();
    AnalyzeCommentResponse response = perspective.analyze(request);

    Map<AttributeType, Float> analyses = new HashMap<AttributeType, Float>();

    // extract the score for each of the requested attribute types
    // and put it in a map which will be used to create a PerspectiveValues object
    for (int i = 0; i < attributeTypes.length; i++) {
      AttributeType attributeType = attributeTypes[i];

      analyses.put(attributeType, extractScore(response, attributeType));
    }

    // Convert response to PerspectiveValues object
    PerspectiveValues analysis = new PerspectiveValues(text, analyses);

    return analysis;
  }

  /**
   * helper method to extract the score for a given response & type
   *
   * @return the score (as a float) extracted from the response for a given type
   */
  private static float extractScore(AnalyzeCommentResponse response, AttributeType type) {
    AttributeScore attributeScore = response.getAttributeScore(type);
    Score score = attributeScore.getSummaryScore();
    return score.getValue();
  }
}
