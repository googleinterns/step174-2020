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

import au.com.origma.perspectiveapi.v1alpha1.models.AttributeType;

/**
 * Handles analysis of stories for content and provides a recommendation on
 * the "best" appropriate story (if multiple).
 */
public interface PerspectiveManager {
  /** an array of all the types we want analysis on */
  public static final AttributeType[] REQUESTED_ATTRIBUTES = {
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
      AttributeType.UNSUBSTANTIAL
    };

  /**
   * Analyzes the passed-in story and returns PerspectiveDecision
   * containing decision based on analysis.
   *
   * @param story The story to be analyzed
   * @return An object describing the recommendation resulting from the analysis.
   */
  public PerspectiveDecision getDecision(String story);
}
