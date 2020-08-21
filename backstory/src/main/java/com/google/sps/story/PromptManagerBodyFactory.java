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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

/**
 * Constructs body of prompt given input keywords.
 */
public class PromptManagerBodyFactory {
  /** Keywords for prompt generation, word processing client, and randomness flag */
  private List<String> keywords;
  private PromptManagerWordTools wordTools;
  boolean chooseRandomly;

  /** Random number generator */
  private Random templateChooser;

  /** Data structures to hold classified words */
  private List<String> gerunds;
  private List<String> nouns;
  private Map<WordType, List<String>> typeMap;

  /** Minimum number of nouns for descriptive method. */
  private final int MINIMUM_NOUNS = 3;

  /** Final output string. */
  private String outputBody;

  /** Templates for introductory sentence completion */
  private final String[] INTRO_TEMPLATES = {
      "a <adj> <noun> as well as a <adj> <noun> decided to come together.",
      "a <adj> <adj> <noun> and a <adj> <adj> <noun> appeared all at once. "};

  /** Templates for introductory sentence completion with gerunds included */
  private final String[] INTRO_TEMPLATES_WITH_GERUNDS = {
      "there was a <adj> <adj> <noun> <gerund> alongside a <adj> <noun>.",
      "a <adj> <noun> as well as a <gerund> <adj> <noun> were together."};

  /** Templates for second sentence. */
  private final String[] SECOND_TEMPLATES = {
      "A <adj> <noun> was also present, quite an interesting scene.",
      "One must not forget the <adj> <noun>, which simply cannot be ignored.",
  };

  /** Templates for list method sentence endings. */
  private final String[] SIMPLE_ENDINGS = {"were all really quite interesting.",
      "all came together in one place.", "were all together at once."};

  /** Default ending for empty string return. */
  private final String EMPTY_ENDING = "a hectic, unrecognizable scene took place.";

  /**
   * Initializes fields and takes in word processing client if supplied.
   *
   * @param keywords Input words for generation.
   * @param chooseRandomly Whether or not to randomly select a template.
   * @param wordTools Client for word processing API calls.
   */
  public PromptManagerBodyFactory(
      List<String> keywords, boolean chooseRandomly, PromptManagerWordTools wordTools) {
    this(keywords, chooseRandomly);
    this.wordTools = wordTools;
  }

  /**
   * Initializes fields.
   *
   * @param keywords Input words for generation.
   * @param chooseRandomly Whether or not to randomly select a template.
   */
  public PromptManagerBodyFactory(List<String> keywords, boolean chooseRandomly) {
    this.keywords = keywords;
    this.chooseRandomly = chooseRandomly;
    templateChooser = new Random();
    outputBody = "";
  }

  /**
   * Generates prompt body using given labels. If randomness
   * is disabled, the first of each applicable template type
   * will be used. Templates are filled using first available
   * of each WordType.
   *
   * @param delimiter String for delimiter between appended strings.
   * @return A String containing the output prompt.
   */
  public String newInstance() {
    try {
      // Instantiate wordTools within try-catch if not supplied.
      if (wordTools == null) {
        wordTools = new PromptManagerWordTools();
      }

      // Classify given word inputs.
      typeMap = wordTools.groupByWordType(keywords);
      nouns = typeMap.get(WordType.NOUN);

      // Consolidate all nouns into one list.
      if (typeMap.get(WordType.MULTIWORD_NOUN) != null) {
        nouns.addAll(typeMap.get(WordType.MULTIWORD_NOUN));
      }

      // Isolate gerunds
      gerunds = typeMap.get(WordType.GERUND);

      // Determine method based on given noun count.
      if (nouns.size() >= MINIMUM_NOUNS) {
        return makeDescriptiveTemplate();
      } else {
        return makeListTemplate();
      }
    } catch (Exception e) {
      // In case of network exception, use list method.
      return makeListTemplate();
    }
  }

  /**
   * Generates descriptive prompt body using adjective generation.
   *
   * @return Completed prompt body.
   */
  private String makeDescriptiveTemplate() {
    try {
      // If gerunds are absent use noun-only templates.
      if (gerunds == null) {
        outputBody = getTemplate(INTRO_TEMPLATES);
      } else {
        outputBody = getTemplate(INTRO_TEMPLATES_WITH_GERUNDS);
      }
      outputBody += " " + getTemplate(SECOND_TEMPLATES);

      while (true) {
        // Replace gerund tag with first available from list.
        if (outputBody.contains("<gerund>")) {
          String gerund = gerunds.remove(0);

          outputBody = outputBody.replaceFirst("<gerund>", gerund);

          // Replace double-adjective noun case with first available noun and fetched adjectives.
        } else if (outputBody.contains("<adj> <adj> <noun>")) {
          String doubleAdjectiveNoun = nouns.remove(0);
          String[] relatedAdjectives = wordTools.getRelatedAdjectives(doubleAdjectiveNoun, 2);

          doubleAdjectiveNoun =
              relatedAdjectives[0] + " " + relatedAdjectives[1] + " " + doubleAdjectiveNoun;
          outputBody = outputBody.replaceFirst("<adj> <adj> <noun>", doubleAdjectiveNoun);

          // Replace single-adjective noun case with first available noun and fetched adjective.
        } else if (outputBody.contains("<adj> <noun>")) {
          String singleAdjectiveNoun = nouns.remove(0);
          String[] relatedAdjectives = wordTools.getRelatedAdjectives(singleAdjectiveNoun, 1);

          singleAdjectiveNoun = relatedAdjectives[0] + " " + singleAdjectiveNoun;
          outputBody = outputBody.replaceFirst("<adj> <noun>", singleAdjectiveNoun);

          // Leave once tags are all replaced
        } else {
          break;
        }
      }

      return outputBody;
    } catch (Exception e) {
      // In case of network exception, use list method.
      return makeListTemplate();
    }
  }

  /**
   * Generates listing prompt body using given keywords.
   *
   * @return Completed prompt body.
   */
  private String makeListTemplate() {
    // Use specific template if empty list is given.
    if (keywords.size() == 0) {
      outputBody = EMPTY_ENDING;
      // List single keyword if given.
    } else if (keywords.size() == 1) {
      outputBody = "a " + keywords.get(0) + " was present.";
      // List two keywords if given.
    } else if (keywords.size() == 2) {
      outputBody += "a " + keywords.get(0) + " as well as a " + keywords.get(1) + " ";
      outputBody += getTemplate(SIMPLE_ENDINGS);

    } else {
      // Iterate through given keywords if over size 2.
      for (int index = 0; index < keywords.size() - 1; index++) {
        outputBody += keywords.get(index) + ", ";
      }
      outputBody += "as well as a " + keywords.get(keywords.size() - 1) + " ";

      // Get ending template.
      outputBody += getTemplate(SIMPLE_ENDINGS);
    }
    return outputBody;
  }

  /**
   * Obtains either the first available or a randomly selected template from the list.
   *
   * @return Template obtained.
   */
  private String getTemplate(String[] templateList) {
    if (chooseRandomly) {
      return templateList[templateChooser.nextInt(templateList.length)];
    } else {
      return templateList[0];
    }
  }
}
