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
import java.util.List;
import java.util.Scanner;

/**
 * Creates prompt string for text generation using input keyword strings.
 */
public final class PromptManager {
  /** Keywords for generation, word processing client, and randomness flag. */
  private List<String> keywords;
  private PromptManagerWordTools wordTools;
  boolean chooseRandomly = true;

  /**
   * Initialize keywords parameter.
   *
   * @param keywords A list of Strings containing keywords for prompts.
   */
  public PromptManager(List<String> keywords) {
    this.keywords = keywords;
  }

  /**
   * Sets wordTools for word processing API calls.
   *
   * @param wordTools PromptManagerWordTools instance.
   */
  public void setWordTools(PromptManagerWordTools wordTools) {
    this.wordTools = wordTools;
  }

  /**
   * Sets use of randomness in prompt template selection.
   *
   * @param boolean Whether or not to randomly choose output templates..
   */
  public void setRandom(boolean chooseRandomly) {
    this.chooseRandomly = chooseRandomly;
  }

  /**
   * Generates prompt using keywords given.
   *
   * @return A String containing the output prompt.
   */
  public String generatePrompt() {
    // Prepare story-like prefix.
    String prompt = "Once upon a time, ";

    // Check for null input.
    if (keywords == null) {
      throw new IllegalArgumentException("Input list cannot be null.");
    }

    // Initialize bodyFactory to process template construction
    PromptManagerBodyFactory bodyFactory;
    if (wordTools == null) {
      bodyFactory = new PromptManagerBodyFactory(keywords, chooseRandomly);
    } else {
      bodyFactory = new PromptManagerBodyFactory(keywords, chooseRandomly, wordTools);
    }

    // Append generated prompt body.
    prompt += bodyFactory.newInstance();
    return prompt;
  }
}
