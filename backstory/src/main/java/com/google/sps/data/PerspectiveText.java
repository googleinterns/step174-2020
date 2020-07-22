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
import au.com.origma.perspectiveapi.v1alpha1.models.Score;

public class PerspectiveText {

  private final String text;
  private final float toxicity;

  public PerspectiveText(PerspectiveAPI perspective, String text) {
    this.text = text;
    
    AnalyzeCommentResponse response = perspective.analyze(text);

    response.getAttributeScore(AttributeType.TOXICITY);
    AttributeScore attributeScore = response.getAttributeScore(AttributeType.TOXICITY);
    Score score = attributeScore.getSummaryScore();
    this.toxicity = score.getValue();
  }

  public String getText() {
    return text;
  }

  public float getToxicity() {
    return toxicity;
  }
}
