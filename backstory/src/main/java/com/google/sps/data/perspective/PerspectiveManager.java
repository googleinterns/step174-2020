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

import au.com.origma.perspectiveapi.v1alpha1.models.AttributeType;
import java.util.Map;

/**
 * An interface for Perspective API integration with project
 */
public interface PerspectiveManager {

  /**
   * Returns text that was analyzed by Perspective in this class
   *
   * @return text that was analyzed
   */
  String getText();

  /**
   * Returns the Map of all AttributeTypes and the corresponding scores.
   *
   * @return analysis scores of text
   */
  Map<AttributeType, Float> getAnalyses();

  /**
   * Returns the decision on the appropriateness of the text
   *
   * @return true, if content considered appropriate; false, otherwise
   */
  boolean getDecision();
}