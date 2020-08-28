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
 * Test the Prompt Creation Suite
 */
@RunWith(MockitoJUnitRunner.class)
public final class PromptManagerTest {
  /** keywords to use for testing */
  private final String NOUN_DOG = "dog";
  private final String NOUN_CAT = "cat";
  private final String NOUN_TREE = "tree";
  private final String NOUN_BIRD = "bird";
  private final String GERUND_RUNNING = "running";
  private final String GERUND_WALKING = "walking";
  private final String[] SAMPLE_GENERATED_ADJECTIVES = {"happy", "large"};

  /** Strings with locations for testing */
  private final String LOCATION = "Oxford, England";
  private final String LOCATION_WITH_THE = "The Radcliffe Camera";

  private NLServiceClient mockClassifier;
  private DatamuseRequestClient mockFetcher;

  /** the keywords to input */
  private List<String> keywords;
  /** the locations to input */
  private List<String> locations;

  @Before
  public void setUp() {
    keywords = new ArrayList<String>();
    locations = new ArrayList<String>();
  }

  /**
   * Checks an exception is thrown if keywords is null
   */
  @Test (expected = IllegalArgumentException.class)
  public void nullKeywordsInput() throws IOException, APINotAvailableException {
    PromptManager promptManager = new PromptManager(null, locations);
  }

  /**
   * Checks an exception is thrown if locations is null
   */
  @Test (expected = IllegalArgumentException.class)
  public void nullLocationsInput() throws IOException, APINotAvailableException {
    PromptManager promptManager = new PromptManager(keywords, null);
  }

  /**
   * Verifies that an input location is correctly formatted.
   *
   * Expected Template: Once upon a time near Oxford, England, a hectic, unrecognizable
   * scene took place.
   */
  @Test
  public void checkLocation() throws IOException, APINotAvailableException {
    locations.add(LOCATION);
    PromptManager promptManager = new PromptManager(keywords, locations);

    // Expected template should be the default empty template.
    String expected = "Once upon a time near Oxford, England, a hectic, unrecognizable scene took place.";
    String actual = promptManager.generatePrompt();

    Assert.assertEquals(expected, actual);
  }

  /**
   * Verifies that an input with "The" is correctly formatted.
   *
   * Expected Template: Once upon a time at the Radcliffe Camera, a hectic, unrecognizable
   * scene took place.
   */
  @Test
  public void checkLocationWithThe() throws IOException, APINotAvailableException {
    locations.add(LOCATION_WITH_THE);
    PromptManager promptManager = new PromptManager(keywords, locations);

    // Expected template should be the default empty template.
    String expected = "Once upon a time at the Radcliffe Camera, a hectic, unrecognizable scene took place.";
    String actual = promptManager.generatePrompt();

    Assert.assertEquals(expected, actual);
  }

  /**
   * Verifies that if multiple locations given, prompt will choose first one.
   *
   * Expected Template: Once upon a time near Oxford, England, a hectic, 
   *    unrecognizable scene took place.
   */
  @Test
  public void checkMultipleLocations() throws IOException, APINotAvailableException {
    locations.add(LOCATION);
    locations.add(LOCATION_WITH_THE);
    PromptManager promptManager = new PromptManager(keywords, locations);

    // Expected template should be the default empty template.
    String expected = "Once upon a time near Oxford, England, a hectic, unrecognizable scene took place.";
    String actual = promptManager.generatePrompt();

    Assert.assertEquals(expected, actual);
  }
  
  /**
   * Verifies a non-random prompt generation for an input list satisfying
   * the full template case(>=3 nouns, >=1 gerund).
   * Output prompt should use the first template and the first 3 nounds/1 gerund.
   *
   * Expected template: Once upon a time, there was a <adjective> <adjective>
   * <noun> <gerund> alongside a <adjective> <noun>. A <adjective> <noun> was
   * also present, quite an interesting scene.
   *
   */
  @Test
  public void descriptiveMethodNonrandom() throws IOException, APINotAvailableException {
    try {
      // Prepare input list
      keywords.add(NOUN_DOG);
      keywords.add(NOUN_CAT);
      keywords.add(NOUN_TREE);
      keywords.add(NOUN_BIRD);
      keywords.add(GERUND_RUNNING);
      keywords.add(GERUND_WALKING);

      PromptManager promptManager = new PromptManager(keywords, locations);

      // Mock word processing APIs
      PromptManagerAPIsClient mockedAPIsClient = mock(PromptManagerAPIsClient.class);

      // Inject mock and disable randomness
      promptManager.setAPIsClient(mockedAPIsClient);
      promptManager.isTemplateRandomized(false);

      // Prepare canned classification of nouns and gerunds
      Map<WordType, List<String>> classifiedInput = new HashMap<WordType, List<String>>();
      List<String> gerunds = new ArrayList<String>();
      gerunds.add(GERUND_RUNNING);
      keywords.remove(GERUND_RUNNING);
      classifiedInput.put(WordType.GERUND, gerunds);
      classifiedInput.put(WordType.NOUN, keywords);

      // Stub API calls with canned classification and adjectives
      when(mockedAPIsClient.groupByWordType(anyList())).thenReturn(classifiedInput);
      when(mockedAPIsClient.fetchRelatedAdjectives(anyString(), anyInt(), anyBoolean()))
          .thenReturn(SAMPLE_GENERATED_ADJECTIVES);

      // Expected nonrandom template prompt uses first template.
      String expected = "Once upon a time, there was a happy large dog running "
          + "alongside a happy cat. A happy tree was also present, quite an interesting scene.";

      String actual = promptManager.generatePrompt();
      Assert.assertEquals(expected, actual);
    } catch (Exception exception) {
      throw exception;
    }
  }

  
  /**
   * Verifies a non-random prompt generation for an input list satisfying
   * the limited template case(2 nouns). Output prompt should use
   * the first template and all given nouns.
   *
   * Expected Template: Once upon a time, (Listed nouns) were all really quite
   * interesting.
   */
  @Test
  public void listMethodTwoNounsNonrandom() throws IOException, APINotAvailableException {
    try {
      // Prepare input list
      keywords.add(NOUN_DOG);
      keywords.add(NOUN_CAT);

      PromptManager promptManager = new PromptManager(keywords, locations);

      // Disable randomness
      promptManager.isTemplateRandomized(false);

      // Expected nonrandom template prompt uses first template.
      String expected =
          "Once upon a time, a dog as well as a cat were all really quite interesting.";
      String actual = promptManager.generatePrompt();

      Assert.assertEquals(expected, actual);
    } catch (Exception exception) {
      throw exception;
    }
  }

  
  /**
   * Verifies a non-random prompt generation for an input list satisfying
   * a limited template case(1 noun). Output prompt should use
   * the first template and sole given nouns.
   *
   * Expected Template: Once upon a time, a (noun) was present.
   */
  @Test
  public void listMethodOneNounNonrandom() throws IOException, APINotAvailableException {
    try {
      // Prepare input list
      keywords.add(NOUN_DOG);

      PromptManager promptManager = new PromptManager(keywords, locations);

      // Disable randomness
      promptManager.isTemplateRandomized(false);

      // Expected nonrandom template prompt uses first template.
      String expected = "Once upon a time, a dog was present.";
      String actual = promptManager.generatePrompt();

      Assert.assertEquals(expected, actual);
    } catch (Exception exception) {
      throw exception;
    }
  }

  
  /**
   * Verifies the full input method of prompt generation using
   * a randomly selected template. Ensure output contains
   * all keywords as well as "Once upon a time"
   */
  @Test
  public void descriptiveMethodRandomContains() throws IOException, APINotAvailableException {
    try {
      // Prepare input list
      keywords.add(NOUN_DOG);
      keywords.add(NOUN_CAT);
      keywords.add(NOUN_TREE);
      keywords.add(NOUN_BIRD);
      keywords.add(GERUND_RUNNING);
      keywords.add(GERUND_WALKING);

      PromptManager promptManager = new PromptManager(keywords, locations);

      // Inject mock APIs into word tools.
      PromptManagerAPIsClient mockedAPIsClient = mock(PromptManagerAPIsClient.class);

      promptManager.setAPIsClient(mockedAPIsClient);

      // Prepare canned output for classification.
      Map<WordType, List<String>> classifiedInput = new HashMap<WordType, List<String>>();
      List<String> gerunds = new ArrayList<String>();
      gerunds.add(GERUND_RUNNING);
      gerunds.add(GERUND_WALKING);
      keywords.remove(GERUND_RUNNING);
      keywords.remove(GERUND_WALKING);

      classifiedInput.put(WordType.GERUND, gerunds);
      classifiedInput.put(WordType.NOUN, keywords);

      // Stub API calls to output canned classification and adjectives
      when(mockedAPIsClient.groupByWordType(anyList())).thenReturn(classifiedInput);
      when(mockedAPIsClient.fetchRelatedAdjectives(anyString(), anyInt(), anyBoolean()))
          .thenReturn(SAMPLE_GENERATED_ADJECTIVES);

      String outputPrompt = promptManager.generatePrompt();

      // Prepare list for expected contained elements.
      List<String> expectedList = new ArrayList<String>();
      expectedList.add("Once upon a time, ");
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
    } catch (Exception exception) {
      throw exception;
    }
  }

  
  /**
   * Verifies the case of an empty list input and empty location input. The output prompt
   * should use a pre-designated empty case template.
   *
   * Expected Template: Once upon a time, a hectic, unrecognizable
   * scene took place.
   */
  @Test
  public void noKeywordsOrLocationGiven() throws IOException, APINotAvailableException {
    try {
      PromptManager promptManager = new PromptManager(keywords, locations);

      // Expected template should be the default empty template.
      String expected = "Once upon a time, a hectic, unrecognizable scene took place.";
      String actual = promptManager.generatePrompt();

      Assert.assertEquals(expected, actual);
    } catch (Exception exception) {
      throw exception;
    }
  }
}
