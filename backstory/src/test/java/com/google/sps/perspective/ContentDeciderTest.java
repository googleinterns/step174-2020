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

package com.google.sps;

import static org.mockito.Mockito.*;

import au.com.origma.perspectiveapi.v1alpha1.models.AttributeType;
import com.google.sps.perspective.ContentDecider;
import com.google.sps.perspective.PerspectiveValues;
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

/** Quality tests for ContentDecider */
@RunWith(JUnit4.class)
public final class ContentDeciderTest {

  /** a PerspectiveValue object to be used as input for ContentDecider class */
  private static PerspectiveValues input;
  /** a Map to be used as the analyses field of input */
  private static Map<AttributeType, Float> inputAnalyses;

  @Before 
  public void setUp() {
    inputAnalyses = new HashMap<AttributeType, Float>();
  }

  /**
   * Call makeDecision with a PerspectiveValues with a null Map
   * to ensure that an IllegalArgumentException will be thrown.
   */
  @Test (expected = IllegalArgumentException.class)
  public void nullMapInput() {
    inputAnalyses = null;
    input = new PerspectiveValues("foo", inputAnalyses);
    ContentDecider.makeDecision(input); // should be the line causing the error
  }

  /**
   * Call makeDecision with a PerspectiveValues with a Map without a toxicity score 
   * to ensure that an IllegalArgumentException will be thrown.
   */
  @Test (expected = IllegalArgumentException.class)
  public void noToxicityInput() {
    input = new PerspectiveValues("foo", inputAnalyses); // no toxicity score has been added yet
    ContentDecider.makeDecision(input); // should be the line causing the error
  }

  /**
   * Check that ContentDecider returns that PerspectiveValues
   * with low toxicities (below 70% threshold) is appropriate (thus true as return).
   */
  @Test
  public void checkDecisionForLowToxicity() {
    // check with a pretty low toxicity (far below 70%)
    final float VERY_LOW_TOXICITY = .25f;

    inputAnalyses.put(AttributeType.TOXICITY, VERY_LOW_TOXICITY);
    input = new PerspectiveValues("foo", inputAnalyses);

    Assert.assertEquals(true, ContentDecider.makeDecision(input));

    // check with a toxicity just below 70%
    final float LOW_TOXICITY = .69f;
    inputAnalyses.put(AttributeType.TOXICITY, LOW_TOXICITY);
    input = new PerspectiveValues("foo", inputAnalyses);

    Assert.assertEquals(true, ContentDecider.makeDecision(input));
  }

  /**
   * Check that ContentDecider returns that PerspectiveValues
   * with threshold toxicity (exactly 70%) is inappropiate (thus false as return).
   */
  @Test
  public void checkDecisionForThresholdToxicity() {
    // check with the threshold toxicity
    final float THRESHOLD_TOXICITY = .7f;

    inputAnalyses.put(AttributeType.TOXICITY, THRESHOLD_TOXICITY);
    input = new PerspectiveValues("foo", inputAnalyses);

    Assert.assertEquals(false, ContentDecider.makeDecision(input));
  }

  /**
   * Check that ContentDecider returns that PerspectiveValues
   * with high toxicity (above 70% threshold) is inappropiate (thus false as return).
   */
  @Test
  public void checkDecisionForHighToxicity() {
    // check with high toxicity (but just above threshold)
    final float HIGH_TOXICITY = .71f;

    inputAnalyses.put(AttributeType.TOXICITY, HIGH_TOXICITY);
    input = new PerspectiveValues("foo", inputAnalyses);

    Assert.assertEquals(false, ContentDecider.makeDecision(input));

    // check with very high toxicity (well above threshold)
    final float VERY_HIGH_TOXICITY = .9f;

    inputAnalyses.put(AttributeType.TOXICITY, VERY_HIGH_TOXICITY);
    input = new PerspectiveValues("foo", inputAnalyses);

    Assert.assertEquals(false, ContentDecider.makeDecision(input));
  }
}
