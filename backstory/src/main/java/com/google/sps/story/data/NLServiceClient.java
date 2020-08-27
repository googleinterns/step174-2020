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

package com.google.sps.story.data;

import com.google.cloud.language.v1.AnalyzeSyntaxResponse;
import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.Document.Type;
import com.google.cloud.language.v1.LanguageServiceClient;
import com.google.cloud.language.v1.PartOfSpeech;
import com.google.cloud.language.v1.PartOfSpeech.Form;
import com.google.cloud.language.v1.PartOfSpeech.Proper;
import com.google.cloud.language.v1.PartOfSpeech.Tag;
import com.google.cloud.language.v1.Token;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service client for Cloud Natural Language API
 */
public class NLServiceClient {
  /** holds the language service client instance for this service client */
  private LanguageServiceClient client;

  /**
   * Constructs a default object of client service class.
   *
   * @throws IOException if an instance of LanguageServiceClient could not be created
   */
  public NLServiceClient() throws IOException {
    this(LanguageServiceClient.create());
  }

  /**
   * Constructs a NLServiceClient class with a given LanguageServiceClient
   * (mainly to be used for testing)
   *
   * @param client a given LanguageServiceClient to use to access Natural Language API
   * @throws IllegalArgumentException if client is null
   */
  public NLServiceClient(LanguageServiceClient client) throws IllegalArgumentException {
    if (client == null) {
      throw new IllegalArgumentException("Client cannot be null");
    }

    this.client = client;
  }

  /**
   * Takes a list of words and groups them by word type.
   * Returns a map which has word types as keys and then words
   * in a list linked to their relevant word type (e.g. "Mary" would
   * be grouped into the PROPER_NOUN list and "calm" would be grouped
   * into the adjective list).
   *
   * @param words the words to group by word type
   * @return a map with lists of words grouped by their word type and with their
   *    word type as a key
   */
  public Map<WordType, List<String>> groupByWordType(List<String> words) {
    Map<WordType, List<String>> map = new HashMap<WordType, List<String>>();

    List<String> singleWords = new ArrayList<String>();

    // remove multi-word inputs first and store as multiword nouns
    // also remove any words containing whitespace that's not spaces
    // and store as unusable
    for (String text : words) {
      Pattern pattern = Pattern.compile("\\s");
      Matcher matcher = pattern.matcher(text);

      // check if there was any whitespace in text
      if (matcher.find()) {
        // if the whitespace was a space, add to multiword nouns
        if (text.contains(" ")) {
          List<String> wordList = map.get(WordType.MULTIWORD_NOUN);

          if (wordList == null) {
            wordList = new ArrayList<String>();
            map.put(WordType.MULTIWORD_NOUN, wordList);
          }

          wordList.add(text);
        } else {
          // else add to unusable
          List<String> wordList = map.get(WordType.UNUSABLE);

          if (wordList == null) {
            wordList = new ArrayList<String>();
            map.put(WordType.UNUSABLE, wordList);
          }

          wordList.add(text);
        }
      } else {
        singleWords.add(text);
      }
    }

    // TODO: Parallelize requests to Cloud Natural Language API

    for (String word : singleWords) {
      Document doc = buildDocumentFromText(word);

      AnalyzeSyntaxResponse response = client.analyzeSyntax(doc);

      Token token = response.getTokens(0);
      PartOfSpeech partOfSpeech = token.getPartOfSpeech();

      WordType type = WordType.UNUSABLE;

      switch (partOfSpeech.getTag()) {
        case VERB:
          if (isGerund(word)) {
            type = WordType.GERUND;
          }
          break;
        case NOUN:
          if (isGerund(word)) {
            type = WordType.GERUND;
            break;
          }

          if (partOfSpeech.getProper() == Proper.PROPER) {
            type = WordType.PROPER_NOUN;
          } else {
            type = WordType.NOUN;
          }
          break;

        case ADJ:
          type = WordType.ADJECTIVE;
          break;
      }

      List<String> wordList = map.get(type);

      if (wordList == null) {
        wordList = new ArrayList<String>();
        map.put(type, wordList);
      }

      wordList.add(word);
    }

    return map;
  }

  /**
   * Close out the NLServiceClient by closing out
   * the LanguageServiceClient.
   */
  public void close() {
    client.close();
  }

  /**
   * Build and return a document from the given test.
   *
   * @param text the text to build a document from
   * @return a Document that holds the current text
   */
  private Document buildDocumentFromText(String text) {
    Document doc = Document.newBuilder().setContent(text).setType(Type.PLAIN_TEXT).build();

    return doc;
  }

  /**
   * Checks if a word is a gerund using heuristics (ending in "ing)
   * and then making a call to the NL API if the word passes
   * the heuristic test.
   *
   * @param word the word to check if it's a gerund
   * @return true, if it's a gerund; false, otherwise
   */
  private boolean isGerund(String word) {
    // first, check it's one word by checking for whitespace
    // (if it has whitespace it's not one word)
    Pattern pattern = Pattern.compile("\\s");
    Matcher matcher = pattern.matcher(word);

    if (matcher.find()) {
      return false;
    }

    // second, check if ends in "ing"
    // (check it's long enough then check actual ending)
    String suffix = "ing";
    int suffixLength = suffix.length();

    if (word.length() < suffixLength) {
      return false;
    }

    String ending = word.substring(word.length() - suffixLength);
    if (!ending.equals(suffix)) {
      return false;
    }

    // third, check if when paired with "is", it's identified as a verb
    Document doc = buildDocumentFromText("is " + word);

    AnalyzeSyntaxResponse response = client.analyzeSyntax(doc);

    // get the second token (the potential gerund)
    Token token = response.getTokens(1);

    return token.getPartOfSpeech().getTag() == Tag.VERB;
  }
}
