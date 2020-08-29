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
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Test PromptManager
 */
@RunWith(MockitoJUnitRunner.class)
public final class PromptManagerTest {
  /** keywords to use for testing */
  private final String NOUN_DOG = "dog";
  private final String NOUN_CAT = "cat";
  private final String NOUN_TREE = "tree";


 

  /** Strings with locations for testing */
  private final String LOCATION = "Oxford, England";
  private final String LOCATION_WITH_THE = "The Radcliffe Camera";

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
  public void nullLocationsInput()throws IOException, APINotAvailableException {
    PromptManager promptManager = new PromptManager(keywords, null);
  }

  /**
   * Verifies the case of an empty list input and empty location input. The output prompt
   * should use a standard "Once upon a time," starting.
   *
   */
  @Test
  public void checkStartWithoutKeywordsOrLocation() throws IOException, APINotAvailableException {
    PromptManager promptManager = new PromptManager(keywords, locations);
    String expectedStart = "Once upon a time,";
    String actualStart = promptManager.generatePrompt().substring(0,expectedStart.length());

    Assert.assertEquals(actualOutput,expectedStart);

  }

  /**
   * Verifies that an input location is correctly formatted.
   *
   */
  @Test
  public void checkStartLocation() throws IOException, APINotAvailableException {
    locations.add(LOCATION);
    PromptManager promptManager = new PromptManager(keywords, locations);

    // Network calls should not be made without keywords.
    String expectedStart = "Once upon a time near Oxford, England,";
    String actualStart = promptManager.generatePrompt().substring(0,expectedStart.length());

    Assert.assertEquals(actualOutput,expectedStart);
  }

  /**
   * Verifies that an input with "The" is correctly formatted.
   *
   */
  @Test
  public void checkStartLocationWithThe() throws IOException, APINotAvailableException {
    locations.add(LOCATION_WITH_THE);
    PromptManager promptManager = new PromptManager(keywords, locations);

    // Network calls should not be made without keywords.
    String expectedStart = "Once upon a time at the Radcliffe Camera,";
    String actualStart = promptManager.generatePrompt().substring(0,expectedStart.length());

    Assert.assertEquals(actualOutput,expectedStart);
  }

  /**
   * Verifies that if multiple locations given, prompt will choose first one.
   *
   */
  @Test
  public void checkStartMultipleLocations() throws IOException, APINotAvailableException {
    locations.add(LOCATION);
    locations.add(LOCATION_WITH_THE);
    PromptManager promptManager = new PromptManager(keywords, locations);

    // Network calls should not be made without keywords.
    String expectedStart = "Once upon a time at the Radcliffe Camera,";
    String actualStart = promptManager.generatePrompt().substring(0,expectedStart.length());

    Assert.assertEquals(actualOutput,expectedStart);
  }

  /**
   * Verifies a prompt is returned with "Once upon a time" as its start.
   *
   */
  public void listMethodTwoNounsNonRandom() throws IOException, APINotAvailableException {
    // Prepare input list
    keywords.add(NOUN_DOG);
    keywords.add(NOUN_CAT);

    PromptManager promptManager = new PromptManager(keywords, locations);

    // Inject mock PromptManagerBodyGenerator
    PromptManagerBodyGenerator mockGenerator = mock(PromptManagerBodyGenerator.class);
    promptManager.setPromptManagerBodyGenerator(mockGenerator);

    // Stub generator calls to output canned prompt body.
    when(mockGenerator.generateBody()).thenReturn("the dog and the cat ran around.");

    // Ensure "Once upon a time," is concatenated to canned body.
    String expected = "Once upon a time, the dog and the cat ran away.";
    String actual = promptManager.generatePrompt();

    verify(mockGenerator).generateBody();
    Assert.assertEquals(expected, actual);
  }
}
