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

import au.com.origma.perspectiveapi.v1alpha1.models.AttributeType;

public class ContentDecision {
  /**
   * Returns if this textAnalysis object is a certain level of toxic.
   * The threshold chosen is above 70% because that was what was used
   * in the demo on the Perspective API website.
   *
   * @return if the PerspectiveAnalysis Object is above 70% toxic
   */
  public static boolean isToxic(PerspectiveAnalysis textAnalysis) {
    float toxicity = textAnalysis.getAnalyses().get(AttributeType.TOXICITY);

    return toxicity >= .7;
  }
}
