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
  // threshold here means that score must be below this threshold
  // in order to be considered appropriate.

  /** the threshold for appropriateness for toxicity score */
  public static final float TOXICITY_THRESHOLD = .7f;
  /** the threshold for appropriateness for sexually explicit score */
  public static final float SEXUALLY_EXPLICIT_THRESHOLD = .6f;
  /** the threshold for appropriateness for profanity score */
  public static final float PROFANITY_THRESHOLD = .8f;
  /** the threshold for appropriateness for offensiveness (identity attack) score */
  public static final float OFFENSIVE_THRESHOLD = .8f;
  /** the threshold for appropriateness for obscenity score */
  public static final float OBSCENITY_THRESHOLD = .8f;

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
   * Validate both that the map isn't null and that the map has a score.
   *
   * @param attributeTypesToScores the map to be validating
   * @param attributeType the type to check it has a score for
   * @throws IllegalArgumentException if the map is null or doesn't have the score
   */
  private static void validateMapHasScore(Map<AttributeType, Float> attributeTypesToScores,
      AttributeType attributeType) throws IllegalArgumentException {
    if (attributeTypesToScores == null) {
      throw new IllegalArgumentException("Map (attributeTypesToScores) cannot be null.");
    } else if (!attributeTypesToScores.containsKey(attributeType)) {
      throw new IllegalArgumentException(
          "Map (attributeTypesToScores) does not contain a score for " + attributeType);
    }
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
    validateMapHasScore(attributeTypesToScores, AttributeType.TOXICITY);

    float toxicity = attributeTypesToScores.get(AttributeType.TOXICITY);
    return toxicity >= TOXICITY_THRESHOLD;
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
    validateMapHasScore(attributeTypesToScores, AttributeType.SEXUALLY_EXPLICIT);

    float sexuallyExplicit = attributeTypesToScores.get(AttributeType.SEXUALLY_EXPLICIT);
    return sexuallyExplicit >= SEXUALLY_EXPLICIT_THRESHOLD;
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
    validateMapHasScore(attributeTypesToScores, AttributeType.PROFANITY);

    float profanity = attributeTypesToScores.get(AttributeType.PROFANITY);
    return profanity >= PROFANITY_THRESHOLD;
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
    validateMapHasScore(attributeTypesToScores, AttributeType.IDENTITY_ATTACK);

    float identityAttack = attributeTypesToScores.get(AttributeType.IDENTITY_ATTACK);
    return identityAttack >= OFFENSIVE_THRESHOLD;
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
    validateMapHasScore(attributeTypesToScores, AttributeType.OBSCENE);

    float obscenity = attributeTypesToScores.get(AttributeType.OBSCENE);
    return obscenity >= OBSCENITY_THRESHOLD;
  }
}
