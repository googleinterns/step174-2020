// Copyright 2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// s
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.google.sps.story;

import com.google.sps.story.data.*;
import java.lang.IllegalArgumentException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

/**
 * Creates prompt string for text generation using input keyword strings.
 */
public final class PromptManager {
  /** Keywords for generation */
  private final List<String> keywords;
  /** Locations for generation */
  private final List<String> locations;
  /** Word fetching/processing client */
  private PromptManagerAPIsClient wordAPIsClient;
  /** Randomness flag. */
  private boolean isTemplateRandomized = true;

  /**
   * Initialize keywords and randomness parameters.
   *
   * @param keywords A list of Strings containing keywords for prompts.
   * @param locations A list of Strings containing potential locations for prompts.
   * @throws IllegalArgumentException If input list is null.
   */
  public PromptManager(List<String> keywords, List<String> locations) throws IllegalArgumentException {
    // Check for null input.
    if (keywords == null || locations == null) {
      throw new IllegalArgumentException("Input lists cannot be null.");
    }

    this.keywords = keywords;
    this.locations = locations;
  }

  /**
   * Sets wordTools for word processing API calls.
   *
   * @param wordTools PromptManagerWordTools instance.
   */
  public void setAPIsClient(PromptManagerAPIsClient wordAPIsClient) {
    this.wordAPIsClient = wordAPIsClient;
  }

  /**
   * Sets use of randomness in prompt template selection.
   * If false, the first template for each input configuration is chosen.
   * @param boolean Whether or not to randomly choose output templates.
   */
  public void isTemplateRandomized(boolean isTemplateRandomized) {
    this.isTemplateRandomized = isTemplateRandomized;
  }

  /**
   * Generates prompt using keywords given.
   *
   * @return A String containing the output prompt.
   */
  public String generatePrompt() {
    // Prepare story-like prefix.
    String prompt = "Once upon a time" + getFormattedLocation() + ", ";

    // Initialize bodyFactory to process template construction
    PromptManagerBodyGenerator bodyGenerator;
    if (wordAPIsClient == null) {
      bodyGenerator = new PromptManagerBodyGenerator(keywords, isTemplateRandomized);
    } else {
      bodyGenerator =
          new PromptManagerBodyGenerator(keywords, isTemplateRandomized, wordAPIsClient);
    }

    // Append generated prompt body.
    prompt += bodyGenerator.generateBody();
    return prompt;
  }

  /**
   * Returns the first location from the locations list
   * formatted properly.
   *
   * @return an empty String, if no locations, and a properly formatted
   * location if the list isn't empty
   */
  private String getFormattedLocation() {
    if (locations.size() == 0) {
      return "";
    }

    String location = locations.get(0);
    String prefix = "the ";
    int prefixLength = prefix.length();

    // if the location starts with "the", return " at " + location,
    // (& change the t in The to lowercase if it's uppercase)
    // else return " near " + location
    if (location.length() > prefixLength 
        && location.substring(0, prefixLength).toLowerCase().equals(prefix)) {
      return " at " + prefix + location.substring(prefixLength);
    } else {
      return " near " + location;
    }
  }
}
