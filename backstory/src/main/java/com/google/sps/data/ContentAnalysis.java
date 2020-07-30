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
import java.util.Map;

/**
 * A class with internal methods that help the PerspectiveManager class to make
 * decisions about the content of the text using scores from the Perspective API.
 */
public class ContentAnalysis {
  /**
   * Returns if the text the analyses are for is a certain level of toxic.
   * The threshold chosen is above 70% because that was what was used
   * in the demo on the Perspective API website.
   * 
   * @param analyses a map which stores the scores from the Perspective API
   * @return true, if the  Object is above (or equal to) 70% toxic; false if not
   */
  public static boolean isToxic(Map<AttributeType, Float> analyses) {
    float toxicity = analyses.get(AttributeType.TOXICITY);

    return toxicity >= .7;
  }
}
