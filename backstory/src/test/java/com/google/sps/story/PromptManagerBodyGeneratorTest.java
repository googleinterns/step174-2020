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

package com.google.sps.story;
import static org.mockito.Mockito.*;

import com.google.sps.APINotAvailableException;
import com.google.sps.story.data.*;
import java.io.IOException;
import java.lang.Exception;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Test PromptManagerBodyGenerator
 */
@RunWith(MockitoJUnitRunner.class)
public final class PromptManagerBodyGeneratorTest {
  /** keywords to use for testing */
  private final String NOUN_DOG = "dog";
  private final String NOUN_CAT = "cat";
  private final String NOUN_TREE = "tree";
  private final String NOUN_BIRD = "bird";
  private final String GERUND_RUNNING = "running";
  private final String GERUND_WALKING = "walking";
  private final String[] SAMPLE_GENERATED_ADJECTIVES = {"happy", "large"};

  private List<String> inputList;
  private List<String> expectedList;
  @Before
  public void setUp() {
    inputList = new ArrayList<String>();
    expectedList = new ArrayList<String>();
  }

  @Test
  /**
   * Verifies a non-random prompt generation for an input list satisfying
   * the full template case(>=3 nouns, >=1 gerund).
   * Output prompt should use the first template and the first 3 nounds/1 gerund.
   *
   *
   */
  public void descriptiveMethodNonRandom() throws IOException, APINotAvailableException {
    // Prepare input list
    inputList.add(NOUN_DOG);
    inputList.add(NOUN_CAT);
    inputList.add(NOUN_TREE);
    inputList.add(NOUN_BIRD);
    inputList.add(GERUND_RUNNING);
    inputList.add(GERUND_WALKING);

    // Initialize Non-Random body generator.
    PromptManagerBodyGenerator PromptManagerBodyGenerator =
        new PromptManagerBodyGenerator(inputList, false);

    // Mock word processing APIs
    PromptManagerAPIsClient mockedAPIsClient = mock(PromptManagerAPIsClient.class);

    // Inject mock and disable randomness
    PromptManagerBodyGenerator.setAPIsClient(mockedAPIsClient);

    // Prepare canned classification of nouns and gerunds
    Map<WordType, List<String>> classifiedInput = new HashMap<WordType, List<String>>();
    List<String> gerunds = new ArrayList<String>();
    gerunds.add(GERUND_RUNNING);
    inputList.remove(GERUND_RUNNING);
    classifiedInput.put(WordType.GERUND, gerunds);
    classifiedInput.put(WordType.NOUN, inputList);

    // Stub API calls with canned classification and adjectives
    when(mockedAPIsClient.groupByWordType(anyList())).thenReturn(classifiedInput);
    when(mockedAPIsClient.fetchRelatedAdjectives(anyString(), anyInt(), anyBoolean()))
        .thenReturn(SAMPLE_GENERATED_ADJECTIVES);
    String outputPrompt = PromptManagerBodyGenerator.generateBody();

    // Expected to use first available nouns and gerund.
    expectedList.add(NOUN_DOG);
    expectedList.add(NOUN_CAT);
    expectedList.add(NOUN_TREE);
    expectedList.add(GERUND_RUNNING);

    boolean containsCheck = true;

    // Verify contained elements
    for (String expected : expectedList) {
      if (!outputPrompt.contains(expected)) {
        containsCheck = false;
      }
    }

    Assert.assertTrue(containsCheck);
  }

  @Test
  /**
   * Verifies a non-random prompt generation for an input list satisfying
   * the limited template case(2 nouns). Output prompt should use
   * the first template and all given nouns.
   *
   */
  public void listMethodTwoNounsNonRandom() throws IOException, APINotAvailableException {
    // Prepare input list
    inputList.add(NOUN_DOG);
    inputList.add(NOUN_CAT);

    // Initialize Non-Random body generator.
    PromptManagerBodyGenerator PromptManagerBodyGenerator =
        new PromptManagerBodyGenerator(inputList, false);

    String outputPrompt = PromptManagerBodyGenerator.generateBody();

    // Expected to use first available nouns and gerund.
    expectedList.add(NOUN_DOG);
    expectedList.add(NOUN_CAT);

    boolean containsCheck = true;

    // Verify contained elements
    for (String expected : expectedList) {
      if (!outputPrompt.contains(expected)) {
        containsCheck = false;
      }
    }

    Assert.assertTrue(containsCheck);
  }

  @Test
  /**
   * Verifies a non-random prompt generation for an input list satisfying
   * a limited template case(1 noun). Output prompt should use
   * the first template and sole given nouns.
   *
   */
  public void listMethodOneNounNonRandom() throws IOException, APINotAvailableException {
    // Prepare input list
    inputList.add(NOUN_DOG);

    // Initialize Non-Random body generator.
    PromptManagerBodyGenerator PromptManagerBodyGenerator =
        new PromptManagerBodyGenerator(inputList, false);

    String outputPrompt = PromptManagerBodyGenerator.generateBody();

    // Prepare list for expected contained elements.
    expectedList.add(NOUN_DOG);

    boolean containsCheck = true;

    // Verify contained elements
    for (String expected : expectedList) {
      if (!outputPrompt.contains(expected)) {
        containsCheck = false;
      }
    }

    Assert.assertTrue(containsCheck);
  }

  @Test
  /**
   * Verifies the full input method of prompt generation using
   * a randomly selected template. Ensure output contains
   * all keywords as well as "Once upon a time"
   */
  public void descriptiveMethodRandomContains() throws IOException, APINotAvailableException {
    // Prepare input list
    inputList.add(NOUN_DOG);
    inputList.add(NOUN_CAT);
    inputList.add(NOUN_TREE);
    inputList.add(NOUN_BIRD);
    inputList.add(GERUND_RUNNING);
    inputList.add(GERUND_WALKING);

    // Initialize Random body generator.
    PromptManagerBodyGenerator PromptManagerBodyGenerator =
        new PromptManagerBodyGenerator(inputList, true);

    // Inject mock APIs into word tools.
    PromptManagerAPIsClient mockedAPIsClient = mock(PromptManagerAPIsClient.class);

    PromptManagerBodyGenerator.setAPIsClient(mockedAPIsClient);

    // Prepare canned output for classification.
    Map<WordType, List<String>> classifiedInput = new HashMap<WordType, List<String>>();
    List<String> gerunds = new ArrayList<String>();
    gerunds.add(GERUND_RUNNING);
    gerunds.add(GERUND_WALKING);
    inputList.remove(GERUND_RUNNING);
    inputList.remove(GERUND_WALKING);

    classifiedInput.put(WordType.GERUND, gerunds);
    classifiedInput.put(WordType.NOUN, inputList);

    // Stub API calls to output canned classification and adjectives
    when(mockedAPIsClient.groupByWordType(anyList())).thenReturn(classifiedInput);
    when(mockedAPIsClient.fetchRelatedAdjectives(anyString(), anyInt(), anyBoolean()))
        .thenReturn(SAMPLE_GENERATED_ADJECTIVES);

    String outputPrompt = PromptManagerBodyGenerator.generateBody();

    // Prepare list for expected contained elements.
    expectedList.add(NOUN_DOG);
    expectedList.add(NOUN_CAT);
    expectedList.add(NOUN_TREE);
    expectedList.add(GERUND_RUNNING);

    boolean containsCheck = true;

    // Verify contained elements
    for (String expected : expectedList) {
      if (!outputPrompt.contains(expected)) {
        containsCheck = false;
      }
    }

    Assert.assertTrue(containsCheck);
  }

  @Test
  /**
   * Verifies the case of an empty list input. The output prompt
   * should use a pre-designated empty case template.
   *
   */
  public void noKeywordsGiven() throws IOException, APINotAvailableException {
    // Initialize Non-Random generator.
    PromptManagerBodyGenerator PromptManagerBodyGenerator =
        new PromptManagerBodyGenerator(inputList, false);

    // Expected template should be the default empty case template.
    String actual = PromptManagerBodyGenerator.generateBody();

    // Ensure real string returned.
    Assert.assertNotNull(actual);
  }
}
