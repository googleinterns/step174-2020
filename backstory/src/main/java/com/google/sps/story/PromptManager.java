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
  /** Prompt Body Generator */
  private PromptManagerBodyGenerator promptManagerBodyGenerator;

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
   * Sets use of randomness in prompt template selection.
   * If false, the first template for each input configuration is chosen.
   *
   * @param boolean Whether or not to randomly choose output templates.
   */
  public void setTemplateRandomized(boolean isTemplateRandomized) {
    this.isTemplateRandomized = isTemplateRandomized;
  }

  /**
   * Sets PromptManagerBodyInstance for generation of prompt body.
   *
   * @param PromptManagerBodyGenerator Generation object for body of output prompt.
   */
  public void setPromptManagerBodyGenerator(PromptManagerBodyGenerator promptManagerBodyGenerator) {
    this.promptManagerBodyGenerator = promptManagerBodyGenerator;
  }

  /**
   * Generates prompt using keywords given. "Once upon a time, " is appended.
   *
   * @return A String containing the output prompt.
   */
  public String generatePrompt() {
    // Prepare story-like prefix.
    String prompt = "Once upon a time, ";

    if (promptManagerBodyGenerator == null) {
      promptManagerBodyGenerator = new PromptManagerBodyGenerator(keywords, isTemplateRandomized);
    }

    // Append generated prompt body.
    prompt += promptManagerBodyGenerator.generateBody();
    return prompt;
  }
}
