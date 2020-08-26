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
  /** Word fetching/processing client */
  private PromptManagerAPIsClient wordAPIsClient;
  /** Randomness flag. */
  private boolean isTemplateRandomized = true;

  /**
   * Initialize keywords and randomness parameters.
   *
   * @param keywords A list of Strings containing keywords for prompts.
   * @throws IllegalArgumentException If input list is null.
   */
  public PromptManager(List<String> keywords) throws IllegalArgumentException {
    // Check for null input.
    if (keywords == null) {
      throw new IllegalArgumentException("Input list cannot be null.");
    }
    this.keywords = keywords;
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
    String prompt = "Once upon a time, ";

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
}
