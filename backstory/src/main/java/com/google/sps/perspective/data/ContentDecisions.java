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

package com.google.sps.perspective.data;

import au.com.origma.perspectiveapi.v1alpha1.models.AttributeType;
import java.util.Map;

/**
 * Provides tools to make decision on whether or not the story (or content)
 * is appropriate using analysis from Perspective API.
 */
public class ContentDecisions {
  /**
   * Overrides default constructor to ensure class can't be instantiated.
   */
  private ContentDecisions() {
    throw new AssertionError();
  }

  /**
   * Makes decision on whether or not text in perspective value
   * is considered appropriate based on the analysis scores from Perspective API
   * stored in PerspectiveValues object. Returns this decision as a boolean.
   * Decision is currently based on scores of toxicity, sexual explicitness,
   * profanity, offensivity, and obscenity.
   *
   * @param PerspectiveValues the object containing text to be decided on
   *     & the requested analysis from Perspective API to use in making decision.
   * @return true, if content considered appropriate; false, otherwise
   */
  public static boolean makeDecision(PerspectiveValues values) {
    Map<AttributeType, Float> scores = values.getAttributeTypesToScores();

    return !isToxic(scores) && !isSexuallyExplicit(scores) && !isProfane(scores) 
        && !isOffensive(scores) && !isObscene(scores);
  }

  /**
   * Private helper method to check if content is considered toxic.
   * Threshold for toxicity is a score greater than or equal to 70% as
   * that was the metric used by the demo on the Google Perspective API site.
   *
   * @param attributeTypesToScores a map with attribute types mapped to scores from the Perspective
   *     API
   * @return true, if toxicity score >= 70%; false, if not
   * @throws IllegalArgumentException, if attributeTypesToScores is null or does not contain a
   *     toxicity score
   */
  private static boolean isToxic(Map<AttributeType, Float> attributeTypesToScores)
      throws IllegalArgumentException {
    if (attributeTypesToScores == null) {
      throw new IllegalArgumentException("Map (attributeTypesToScores) cannot be null.");
    } else if (!attributeTypesToScores.containsKey(AttributeType.TOXICITY)) {
      throw new IllegalArgumentException(
          "Map (attributeTypesToScores) does not contain a toxicity score");
    }

    float toxicity = attributeTypesToScores.get(AttributeType.TOXICITY);

    return toxicity >= .7f;
  }

  /**
   * Private helper method to check if content is considered sexually explicit.
   * Threshold for sexually explicit content is a score greater than or equal to 60% 
   * after experimenting with Perspective API  
   *
   * @param attributeTypesToScores a map with attribute types mapped to scores from the Perspective
   *     API
   * @return true, if sexually explicit score >= 60%; false, if not
   * @throws IllegalArgumentException, if attributeTypesToScores is null or does not contain a
   *     sexually explicit score
   */
  private static boolean isSexuallyExplicit(Map<AttributeType, Float> attributeTypesToScores)
      throws IllegalArgumentException {
    if (attributeTypesToScores == null) {
      throw new IllegalArgumentException("Map (attributeTypesToScores) cannot be null.");
    } else if (!attributeTypesToScores.containsKey(AttributeType.SEXUALLY_EXPLICIT)) {
      throw new IllegalArgumentException(
          "Map (attributeTypesToScores) does not contain a sexually explicit score");
    }

    float sexuallyExplicit = attributeTypesToScores.get(AttributeType.SEXUALLY_EXPLICIT);

    return sexuallyExplicit >= .6f;
  }

  /**
   * Private helper method to check if content is considered profane.
   * Threshold for profane content is a score greater than or equal to 80% 
   * after experimenting with Perspective API  
   *
   * @param attributeTypesToScores a map with attribute types mapped to scores from the Perspective
   *     API
   * @return true, if profanity score >= 80%; false, if not
   * @throws IllegalArgumentException, if attributeTypesToScores is null or does not contain a
   *     profanity score
   */
  private static boolean isProfane(Map<AttributeType, Float> attributeTypesToScores)
      throws IllegalArgumentException {
    if (attributeTypesToScores == null) {
      throw new IllegalArgumentException("Map (attributeTypesToScores) cannot be null.");
    } else if (!attributeTypesToScores.containsKey(AttributeType.PROFANITY)) {
      throw new IllegalArgumentException(
          "Map (attributeTypesToScores) does not contain a profanity score");
    }

    float profanity = attributeTypesToScores.get(AttributeType.PROFANITY);

    return profanity >= .8f;
  }

  /**
   * Private helper method to check if content is considered offensive.
   * Threshold for offensive content is an identity attack score greater than or equal to 80% 
   * after experimenting with Perspective API  
   *
   * @param attributeTypesToScores a map with attribute types mapped to scores from the Perspective
   *     API
   * @return true, if identity attack score >= 80%; false, if not
   * @throws IllegalArgumentException, if attributeTypesToScores is null or does not contain a
   *     identity attack score
   */
  private static boolean isOffensive(Map<AttributeType, Float> attributeTypesToScores)
      throws IllegalArgumentException {
    if (attributeTypesToScores == null) {
      throw new IllegalArgumentException("Map (attributeTypesToScores) cannot be null.");
    } else if (!attributeTypesToScores.containsKey(AttributeType.IDENTITY_ATTACK)) {
      throw new IllegalArgumentException(
          "Map (attributeTypesToScores) does not contain an identity attack score");
    }

    float identityAttack = attributeTypesToScores.get(AttributeType.IDENTITY_ATTACK);

    return identityAttack >= .8f;
  }

  /**
   * Private helper method to check if content is considered obscene.
   * Threshold for obscene content is an obscenity score greater than or equal to 80% 
   * after experimenting with Perspective API  
   *
   * @param attributeTypesToScores a map with attribute types mapped to scores from the Perspective
   *     API
   * @return true, if obscenity score >= 80%; false, if not
   * @throws IllegalArgumentException, if attributeTypesToScores is null or does not contain a
   *     obscenity score
   */
  private static boolean isObscene(Map<AttributeType, Float> attributeTypesToScores)
      throws IllegalArgumentException {
    if (attributeTypesToScores == null) {
      throw new IllegalArgumentException("Map (attributeTypesToScores) cannot be null.");
    } else if (!attributeTypesToScores.containsKey(AttributeType.OBSCENE)) {
      throw new IllegalArgumentException(
          "Map (attributeTypesToScores) does not contain an obscenity score");
    }

    float obscenity = attributeTypesToScores.get(AttributeType.OBSCENE);

    return obscenity >= .8f;
  }
}
