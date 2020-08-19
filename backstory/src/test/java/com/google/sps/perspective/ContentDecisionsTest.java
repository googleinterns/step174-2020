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

import static org.mockito.Mockito.*;

import au.com.origma.perspectiveapi.v1alpha1.models.AttributeType;
import com.google.sps.perspective.data.ContentDecisions;
import com.google.sps.perspective.data.PerspectiveValues;
import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/** Quality tests for ContentDecisions */
@RunWith(JUnit4.class)
public final class ContentDecisionsTest {

  /** the types used as criteria for the content decisions */
  private static final ImmutableList<AttributeType> CRITERIA = ImmutableList.of(AttributeType.TOXICITY,
      AttributeType.SEXUALLY_EXPLICIT, AttributeType.PROFANITY, AttributeType.IDENTITY_ATTACK,
      AttributeType.OBSCENE);

  /** default string to use for text */
  private static final String DEFAULT_TEXT = "foo";

  /** a PerspectiveValue object to be used as input for ContentDecisions class */
  private static PerspectiveValues input;
  /** a Map to be used as the attributeTypesToScores field of input */
  private static Map<AttributeType, Float> inputScores;

  @Before 
  public void setUp() {
    inputScores = new HashMap<AttributeType, Float>();
    
    // set all criteria to 0 at the moment (so default is it's appropriate)
    for (AttributeType type: CRITERIA) {
      inputScores.put(type, 0f);
    }
  }

  /**
   * Call makeDecision with a PerspectiveValues with a null Map
   * to ensure that an IllegalArgumentException will be thrown.
   */
  @Test (expected = IllegalArgumentException.class)
  public void nullMapInput() {
    inputScores = null;
    input = new PerspectiveValues(DEFAULT_TEXT, inputScores);
    ContentDecisions.makeDecision(input); // should be the line causing the error
  }

  /**
   * Call makeDecision with a PerspectiveValues with a Map without a toxicity score 
   * to ensure that an IllegalArgumentException will be thrown.
   */
  @Test (expected = IllegalArgumentException.class)
  public void noToxicityScore() {
    inputScores.remove(AttributeType.TOXICITY);

    input = new PerspectiveValues(DEFAULT_TEXT, inputScores); 
    ContentDecisions.makeDecision(input); // should be the line causing the error
  }

  /**
   * Call makeDecision with a PerspectiveValues with a Map without a sexually explicit score 
   * to ensure that an IllegalArgumentException will be thrown.
   */
  @Test (expected = IllegalArgumentException.class)
  public void noSexuallyExplicitScore() {
    inputScores.remove(AttributeType.SEXUALLY_EXPLICIT);

    input = new PerspectiveValues(DEFAULT_TEXT, inputScores); 
    ContentDecisions.makeDecision(input); // should be the line causing the error
  }

  /**
   * Call makeDecision with a PerspectiveValues with a Map without a profanity score 
   * to ensure that an IllegalArgumentException will be thrown.
   */
  @Test (expected = IllegalArgumentException.class)
  public void noProfanityScore() {
    inputScores.remove(AttributeType.PROFANITY);

    input = new PerspectiveValues(DEFAULT_TEXT, inputScores); 
    ContentDecisions.makeDecision(input); // should be the line causing the error
  }

  /**
   * Call makeDecision with a PerspectiveValues with a Map without an identity attack score 
   * to ensure that an IllegalArgumentException will be thrown.
   */
  @Test (expected = IllegalArgumentException.class)
  public void noIdentityAttackScore() {
    inputScores.remove(AttributeType.IDENTITY_ATTACK);

    input = new PerspectiveValues(DEFAULT_TEXT, inputScores); 
    ContentDecisions.makeDecision(input); // should be the line causing the error
  }

  /**
   * Call makeDecision with a PerspectiveValues with a Map without an obscenity score 
   * to ensure that an IllegalArgumentException will be thrown.
   */
  @Test (expected = IllegalArgumentException.class)
  public void noObscenityScore() {
    inputScores.remove(AttributeType.OBSCENE);

    input = new PerspectiveValues(DEFAULT_TEXT, inputScores); 
    ContentDecisions.makeDecision(input); // should be the line causing the error
  }

  /**
   * Check that ContentDecisions makeDecision returns that PerspectiveValues
   * with low toxicities (below 70% threshold) is appropriate (thus true as return).
   */
  @Test
  public void checkDecisionForLowToxicity() {
    // check with a pretty low toxicity (very below 70%)
    final float VERY_LOW_TOXICITY = ContentDecisions.TOXICITY_THRESHOLD - .1f;

    inputScores.put(AttributeType.TOXICITY, VERY_LOW_TOXICITY);
    input = new PerspectiveValues(DEFAULT_TEXT, inputScores);

    Assert.assertTrue(ContentDecisions.makeDecision(input));

    // check with a toxicity just below 70%
    final float LOW_TOXICITY = ContentDecisions.TOXICITY_THRESHOLD - .01f;
    inputScores.put(AttributeType.TOXICITY, LOW_TOXICITY);
    input = new PerspectiveValues(DEFAULT_TEXT, inputScores);

    Assert.assertTrue(ContentDecisions.makeDecision(input));
  }

  /**
   * Check that ContentDecisions makeDecision() returns that PerspectiveValues
   * with threshold toxicity (exactly 70%) is inappropiate (thus false as return).
   */
  @Test
  public void checkDecisionForThresholdToxicity() {
    // check with the threshold toxicity
    final float THRESHOLD_TOXICITY = ContentDecisions.TOXICITY_THRESHOLD;

    inputScores.put(AttributeType.TOXICITY, THRESHOLD_TOXICITY);
    input = new PerspectiveValues(DEFAULT_TEXT, inputScores);

    Assert.assertEquals(false, ContentDecisions.makeDecision(input));
  }

  /**
   * Check that ContentDecisions returns that PerspectiveValues
   * with high toxicity (above 70% threshold) is inappropiate (thus false as return).
   */
  @Test
  public void checkDecisionForHighToxicity() {
    // check with high toxicity (but just above threshold)
    final float HIGH_TOXICITY = ContentDecisions.TOXICITY_THRESHOLD + .01f;

    inputScores.put(AttributeType.TOXICITY, HIGH_TOXICITY);
    input = new PerspectiveValues(DEFAULT_TEXT, inputScores);

    Assert.assertEquals(false, ContentDecisions.makeDecision(input));

    // check with very high toxicity (well above threshold)
    final float VERY_HIGH_TOXICITY = ContentDecisions.TOXICITY_THRESHOLD + .1f;

    inputScores.put(AttributeType.TOXICITY, VERY_HIGH_TOXICITY);
    input = new PerspectiveValues(DEFAULT_TEXT, inputScores);

    Assert.assertEquals(false, ContentDecisions.makeDecision(input));
  }

  /**
   * Check that ContentDecisions makeDecision returns that PerspectiveValues
   * with low scores for sexually explicit material (below 60% threshold) 
   * is appropriate (thus true as return).
   */
  @Test
  public void checkDecisionForLowSexuallyExplicit() {
    // check with a pretty low sexually explicit score (far below 60%)
    final float VERY_LOW_SEXUALLY_EXPLICIT = ContentDecisions.SEXUALLY_EXPLICIT_THRESHOLD - .1f;

    inputScores.put(AttributeType.SEXUALLY_EXPLICIT, VERY_LOW_SEXUALLY_EXPLICIT);
    input = new PerspectiveValues(DEFAULT_TEXT, inputScores);

    Assert.assertTrue(ContentDecisions.makeDecision(input));

    // check with a sexually explicit score just below 60%
    final float LOW_SEXUALLY_EXPLICIT = ContentDecisions.SEXUALLY_EXPLICIT_THRESHOLD - .01f;
    inputScores.put(AttributeType.SEXUALLY_EXPLICIT, LOW_SEXUALLY_EXPLICIT);
    input = new PerspectiveValues(DEFAULT_TEXT, inputScores);

    Assert.assertTrue(ContentDecisions.makeDecision(input));
  }

  /**
   * Check that ContentDecisions makeDecision() returns that PerspectiveValues
   * with threshold sexually explicit material (exactly 60%) is inappropiate 
   * (thus false as return).
   */
  @Test
  public void checkDecisionForThresholdSexuallyExplicit() {
    // check with the threshold sexually explicit score
    final float THRESHOLD_SEXUALLY_EXPLICIT = ContentDecisions.SEXUALLY_EXPLICIT_THRESHOLD;

    inputScores.put(AttributeType.SEXUALLY_EXPLICIT, THRESHOLD_SEXUALLY_EXPLICIT);
    input = new PerspectiveValues(DEFAULT_TEXT, inputScores);

    Assert.assertEquals(false, ContentDecisions.makeDecision(input));
  }

  /**
   * Check that ContentDecisions returns that PerspectiveValues
   * with high sexually explicit material (above 60% threshold) 
   * is inappropiate (thus false as return).
   */
  @Test
  public void checkDecisionForHighSexuallyExplicit() {
    // check with high sexually explicit score (but just above threshold)
    final float HIGH_SEXUALLY_EXPLICIT = ContentDecisions.SEXUALLY_EXPLICIT_THRESHOLD + .01f;

    inputScores.put(AttributeType.SEXUALLY_EXPLICIT, HIGH_SEXUALLY_EXPLICIT);
    input = new PerspectiveValues(DEFAULT_TEXT, inputScores);

    Assert.assertEquals(false, ContentDecisions.makeDecision(input));

    // check with very high sexually explicit score (well above threshold)
    final float VERY_HIGH_SEXUALLY_EXPLICIT = ContentDecisions.SEXUALLY_EXPLICIT_THRESHOLD + .1f;

    inputScores.put(AttributeType.SEXUALLY_EXPLICIT, VERY_HIGH_SEXUALLY_EXPLICIT);
    input = new PerspectiveValues(DEFAULT_TEXT, inputScores);

    Assert.assertEquals(false, ContentDecisions.makeDecision(input));
  }

  /**
   * Check that ContentDecisions makeDecision returns that PerspectiveValues
   * with low scores for profane material (below 80% threshold) 
   * is appropriate (thus true as return).
   */
  @Test
  public void checkDecisionForLowProfanity() {
    // check with a pretty low profanity score (far below 80%)
    final float VERY_LOW_PROFANITY = ContentDecisions.PROFANITY_THRESHOLD - .1f;

    inputScores.put(AttributeType.PROFANITY, VERY_LOW_PROFANITY);
    input = new PerspectiveValues(DEFAULT_TEXT, inputScores);

    Assert.assertTrue(ContentDecisions.makeDecision(input));

    // check with a sexually explicit score just below 80%
    final float LOW_PROFANITY = ContentDecisions.PROFANITY_THRESHOLD - .01f;

    inputScores.put(AttributeType.PROFANITY, LOW_PROFANITY);
    input = new PerspectiveValues(DEFAULT_TEXT, inputScores);

    Assert.assertTrue(ContentDecisions.makeDecision(input));
  }

  /**
   * Check that ContentDecisions makeDecision() returns that PerspectiveValues
   * with threshold profane material (exactly 80%) is inappropiate 
   * (thus false as return).
   */
  @Test
  public void checkDecisionForThresholdProfanity() {
    // check with the threshold sexually explicit score
    final float THRESHOLD_PROFANITY = ContentDecisions.PROFANITY_THRESHOLD;

    inputScores.put(AttributeType.PROFANITY, THRESHOLD_PROFANITY);
    input = new PerspectiveValues(DEFAULT_TEXT, inputScores);

    Assert.assertEquals(false, ContentDecisions.makeDecision(input));
  }

  /**
   * Check that ContentDecisions returns that PerspectiveValues
   * with highly profane material (above 80% threshold) 
   * is inappropiate (thus false as return).
   */
  @Test
  public void checkDecisionForHighProfanity() {
    // check with high profanity score (but just above threshold)
    final float HIGH_PROFANITY = ContentDecisions.PROFANITY_THRESHOLD + .01f;

    inputScores.put(AttributeType.PROFANITY, HIGH_PROFANITY);
    input = new PerspectiveValues(DEFAULT_TEXT, inputScores);

    Assert.assertEquals(false, ContentDecisions.makeDecision(input));

    // check with very high profanity score (well above threshold)
    final float VERY_HIGH_PROFANITY = ContentDecisions.PROFANITY_THRESHOLD + .1f;

    inputScores.put(AttributeType.PROFANITY, VERY_HIGH_PROFANITY);
    input = new PerspectiveValues(DEFAULT_TEXT, inputScores);

    Assert.assertEquals(false, ContentDecisions.makeDecision(input));
  }

  /**
   * Check that ContentDecisions makeDecision returns that PerspectiveValues
   * with low scores for offensive material (below 80% threshold) 
   * is appropriate (thus true as return).
   */
  @Test
  public void checkDecisionForLowOffensivity() {
    // check with a pretty low identity attack score (far below 80%)
    final float VERY_LOW_OFFENSIVITY = ContentDecisions.OFFENSIVE_THRESHOLD - .1f;

    inputScores.put(AttributeType.IDENTITY_ATTACK, VERY_LOW_OFFENSIVITY);
    input = new PerspectiveValues(DEFAULT_TEXT, inputScores);

    Assert.assertTrue(ContentDecisions.makeDecision(input));

    // check with a identity attack score just below 80%
    final float LOW_OFFENSIVITY = ContentDecisions.OFFENSIVE_THRESHOLD - .01f;

    inputScores.put(AttributeType.IDENTITY_ATTACK, LOW_OFFENSIVITY);
    input = new PerspectiveValues(DEFAULT_TEXT, inputScores);

    Assert.assertTrue(ContentDecisions.makeDecision(input));
  }

  /**
   * Check that ContentDecisions makeDecision() returns that PerspectiveValues
   * with threshold offensive material (exactly 80%) is inappropiate 
   * (thus false as return).
   */
  @Test
  public void checkDecisionForThresholdOffensivity() {
    // check with the threshold identity attack score
    final float THRESHOLD_OFFENSIVITY = ContentDecisions.OFFENSIVE_THRESHOLD;

    inputScores.put(AttributeType.IDENTITY_ATTACK, THRESHOLD_OFFENSIVITY);
    input = new PerspectiveValues(DEFAULT_TEXT, inputScores);

    Assert.assertEquals(false, ContentDecisions.makeDecision(input));
  }

  /**
   * Check that ContentDecisions returns that PerspectiveValues
   * with highly offensive material (above 80% threshold) 
   * is inappropiate (thus false as return).
   */
  @Test
  public void checkDecisionForHighOffensivity() {
    // check with high identity attack score (but just above threshold)
    final float HIGH_OFFENSIVITY = ContentDecisions.OFFENSIVE_THRESHOLD + .01f;

    inputScores.put(AttributeType.IDENTITY_ATTACK, HIGH_OFFENSIVITY);
    input = new PerspectiveValues(DEFAULT_TEXT, inputScores);

    Assert.assertEquals(false, ContentDecisions.makeDecision(input));

    // check with very high identity attack score (well above threshold)
    final float VERY_HIGH_OFFENSIVITY = ContentDecisions.OFFENSIVE_THRESHOLD + .1f;

    inputScores.put(AttributeType.IDENTITY_ATTACK, VERY_HIGH_OFFENSIVITY);
    input = new PerspectiveValues(DEFAULT_TEXT, inputScores);

    Assert.assertEquals(false, ContentDecisions.makeDecision(input));
  }

  /**
   * Check that ContentDecisions makeDecision returns that PerspectiveValues
   * with low scores for obscene material (below 80% threshold) 
   * is appropriate (thus true as return).
   */
  @Test
  public void checkDecisionForLowObscenity() {
    // check with a pretty low obscenity score (far below 80%)
    final float VERY_LOW_OBSCENITY = ContentDecisions.OBSCENITY_THRESHOLD - .1f;

    inputScores.put(AttributeType.OBSCENE, VERY_LOW_OBSCENITY);
    input = new PerspectiveValues(DEFAULT_TEXT, inputScores);

    Assert.assertTrue(ContentDecisions.makeDecision(input));

    // check with an obscenity score just below 80%
    final float LOW_OBSCENITY =  ContentDecisions.OBSCENITY_THRESHOLD - .01f;

    inputScores.put(AttributeType.OBSCENE, LOW_OBSCENITY);
    input = new PerspectiveValues(DEFAULT_TEXT, inputScores);

    Assert.assertTrue(ContentDecisions.makeDecision(input));
  }

  /**
   * Check that ContentDecisions makeDecision() returns that PerspectiveValues
   * with threshold obscene material (exactly 80%) is inappropiate 
   * (thus false as return).
   */
  @Test
  public void checkDecisionForThresholdObscenity() {
    // check with the threshold obscene score
    final float THRESHOLD_OBSCENITY = ContentDecisions.OBSCENITY_THRESHOLD;

    inputScores.put(AttributeType.OBSCENE, THRESHOLD_OBSCENITY);
    input = new PerspectiveValues(DEFAULT_TEXT, inputScores);

    Assert.assertEquals(false, ContentDecisions.makeDecision(input));
  }

  /**
   * Check that ContentDecisions returns that PerspectiveValues
   * with highly obscene material (above 80% threshold) 
   * is inappropiate (thus false as return).
   */
  @Test
  public void checkDecisionForHighObscenity() {
    // check with high obscenity score (but just above threshold)
    final float HIGH_OBSCENITY = ContentDecisions.OBSCENITY_THRESHOLD + .01f;

    inputScores.put(AttributeType.OBSCENE, HIGH_OBSCENITY);
    input = new PerspectiveValues(DEFAULT_TEXT, inputScores);

    Assert.assertEquals(false, ContentDecisions.makeDecision(input));

    // check with very high identity attack score (well above threshold)
    final float VERY_HIGH_OBSCENITY = ContentDecisions.OBSCENITY_THRESHOLD + .1f;

    inputScores.put(AttributeType.OBSCENE, VERY_HIGH_OBSCENITY);
    input = new PerspectiveValues(DEFAULT_TEXT, inputScores);

    Assert.assertEquals(false, ContentDecisions.makeDecision(input));
  }
}
