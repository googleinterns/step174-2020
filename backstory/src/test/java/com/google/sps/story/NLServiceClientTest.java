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

import com.google.cloud.language.v1.AnalyzeSyntaxResponse;
import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.LanguageServiceClient;
import com.google.cloud.language.v1.PartOfSpeech;
import com.google.cloud.language.v1.PartOfSpeech.Form;
import com.google.cloud.language.v1.PartOfSpeech.Proper;
import com.google.cloud.language.v1.PartOfSpeech.Tag;
import com.google.cloud.language.v1.Token;
import com.google.sps.story.data.NLServiceClient;
import com.google.sps.story.data.WordType;
import java.io.IOException;
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
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Quality tests for the NLServiceClient
 */
@RunWith(JUnit4.class)
public final class NLServiceClientTest {

  /** array of multiword inputs to use for testing */
  private final String[] MULTIWORD_INPUTS = { "physical exercise", "social group",
        "urban area", "human leg", "tower block", "metropolitan area" };
  /** array of inputs with whitespace in them to use for testing */
  private final String[] WHITESPACE_INPUTS ={ "physical\nexercise", "social\tgroup",
        "urban\rarea", "tower\fblock" };
  /** array of nouns to use for testing */
  private final String[] NOUNS = { "person", "dog", "skyscraper" };
  /** array of proper nouns to use for testing */
  private final String[] PROPER_NOUNS = { "Mary", "Michael", "AnneMarie", "Maddie" };
  /** array of adjectives to use for testing */
  private final String[] ADJECTIVES = { "calm", "fun", "happy", "blue" };
  /** gerunds to use for testing */
  private final String[] GERUNDS = { "baking", "jogging", "running"};  

  /**
   * Check that an exception thrown if you try to create
   * a NLServiceClient with null LanguageServiceClient.
   */
  @Test (expected = IllegalArgumentException.class)
  public void nullClient() {
    NLServiceClient client = new NLServiceClient(null);
  }

  /**
   * Check that document passed in to analyze has
   * the correct text.
   */
  @Test 
  public void checkDocumentText() {
    final String TEXT = "calm";
    PartOfSpeech partOfSpeech = PartOfSpeech.newBuilder().setTag(Tag.ADJ).build();

    Map<String, PartOfSpeech> desiredReturns = new HashMap<String, PartOfSpeech>();
    desiredReturns.put(TEXT, partOfSpeech);
    
    LanguageServiceClient mockClient = createMockLSClient(desiredReturns);

    NLServiceClient client = new NLServiceClient(mockClient);
    client.groupByWordType(Arrays.asList(TEXT));

    // capture the passed-in document
    ArgumentCaptor<Document> documentCaptor = ArgumentCaptor.forClass(Document.class);
    verify(mockClient).analyzeSyntax(documentCaptor.capture());

    Document document = documentCaptor.getValue();

    // check passed-in document has right value
    Assert.assertEquals(TEXT, document.getContent());
  }

  /**
   * Check the number of API calls made. Should be 1 call for every word that is not
   * a multiword noun (which has 0 calls) and not a gerund (which has 2 calls).
   */
  @Test 
  public void checkAPICalls() {
    final String ADJECTIVE = "calm";
    PartOfSpeech adjectivePartOfSpeech = PartOfSpeech.newBuilder().setTag(Tag.ADJ).build();

    final String NOUN = "person";
    PartOfSpeech nounPartOfSpeech = PartOfSpeech.newBuilder().setTag(Tag.NOUN).build();

    Map<String, PartOfSpeech> desiredReturns = new HashMap<String, PartOfSpeech>();
    desiredReturns.put(ADJECTIVE, adjectivePartOfSpeech);
    desiredReturns.put(NOUN, nounPartOfSpeech);
    
    LanguageServiceClient mockClient = createMockLSClient(desiredReturns);

    NLServiceClient client = new NLServiceClient(mockClient);
    client.groupByWordType(Arrays.asList(ADJECTIVE, NOUN));

    // check this method is called 2x (for the two passed-in args - neither of which are gerunds)
    verify(mockClient, times(2)).analyzeSyntax(any(Document.class));
  }

  /**
   * Check the number of API calls made with gerunds, which necessitate extra calls (2 per gerund).
   */
  @Test 
  public void checkAPICallsWithGerund() {
    Map<String, PartOfSpeech> desiredReturns = new HashMap<String, PartOfSpeech>();

    for (String gerund: GERUNDS) {
      addGerund(desiredReturns, gerund);
    }
    
    LanguageServiceClient mockClient = createMockLSClient(desiredReturns);

    NLServiceClient client = new NLServiceClient(mockClient);
    client.groupByWordType(Arrays.asList(GERUNDS));

    // check there are two API calls for each gerund
    verify(mockClient, times(2 * GERUNDS.length)).analyzeSyntax(any(Document.class));
  }

  /**
   * Check that all multiword inputs get grouped into multiword nouns list.
   */
  @Test
  public void checkMultiwordInput() {
    Map<String, PartOfSpeech> desiredReturns = new HashMap<String, PartOfSpeech>();
    
    NLServiceClient client = new NLServiceClient(createMockLSClient(desiredReturns));
    List<String> multiwordInputs = Arrays.asList(MULTIWORD_INPUTS);

    Map<WordType, List<String>> groupedWords = client.groupByWordType(multiwordInputs);

    // all should be grouped as multiword inputs (should only be one key)
    Assert.assertEquals(1, groupedWords.keySet().size());
    Assert.assertEquals(multiwordInputs, groupedWords.get(WordType.MULTIWORD_NOUN));
  }

  /**
   * Check that all non-multiword noun whitespace inputs get grouped into unusable
   */
  @Test
  public void checkWhitespaceInput() {
    Map<String, PartOfSpeech> desiredReturns = new HashMap<String, PartOfSpeech>();
    
    NLServiceClient client = new NLServiceClient(createMockLSClient(desiredReturns));
    List<String> whitespaceInputs = Arrays.asList(WHITESPACE_INPUTS);

    Map<WordType, List<String>> groupedWords = client.groupByWordType(whitespaceInputs);

    // all should be grouped as unusable (one key)
    Assert.assertEquals(1, groupedWords.keySet().size());
    Assert.assertEquals(whitespaceInputs, groupedWords.get(WordType.UNUSABLE));
  }

  /**
   * Check that all nouns get grouped into the list of nouns.
   */
  @Test
  public void checkNouns() {
    Map<String, PartOfSpeech> desiredReturns = new HashMap<String, PartOfSpeech>();
    List<String> inputs = new ArrayList<String>();

    // other inputs
    final String ADJECTIVE = "calm";
    final String VERB = "runs"; // won't be checked as gerund b/c doesn't end in "ing"

    // add all nouns
    for (String noun: NOUNS) {
      desiredReturns.put(noun, buildPartOfSpeech(WordType.NOUN));
      inputs.add(noun);
    }

    desiredReturns.put(ADJECTIVE, buildPartOfSpeech(WordType.ADJECTIVE));
    inputs.add(ADJECTIVE);
    desiredReturns.put(VERB, buildPartOfSpeech(Tag.VERB));
    inputs.add(VERB);

    NLServiceClient client = new NLServiceClient(createMockLSClient(desiredReturns));

    Map<WordType, List<String>> groupedWords = client.groupByWordType(inputs);

    // should be 3 keys (noun, adjective & unusable [the verb])
    Assert.assertEquals(3, groupedWords.keySet().size());
    Assert.assertEquals(Arrays.asList(NOUNS), groupedWords.get(WordType.NOUN));
  }

  /**
   * Check that proper nouns are correctly identified and separated
   * from regular nouns.
   */
  @Test
  public void checkProperNouns() {
    Map<String, PartOfSpeech> desiredReturns = new HashMap<String, PartOfSpeech>();
    List<String> inputs = new ArrayList<String>();

    // add all proper nouns
    for (String properNoun: PROPER_NOUNS) {
      desiredReturns.put(properNoun, buildPartOfSpeech(WordType.PROPER_NOUN));
      inputs.add(properNoun);
    }

    // add all nouns
    for (String noun: NOUNS) {
      desiredReturns.put(noun, buildPartOfSpeech(WordType.NOUN));
      inputs.add(noun);
    }

    NLServiceClient client = new NLServiceClient(createMockLSClient(desiredReturns));

    Map<WordType, List<String>> groupedWords = client.groupByWordType(inputs);

    // should be 2 keys (proper nouns and nouns)
    Assert.assertEquals(2, groupedWords.keySet().size());
    Assert.assertEquals(Arrays.asList(PROPER_NOUNS), groupedWords.get(WordType.PROPER_NOUN));
  }

  /**
   * Check that all adjectives get grouped into the list of adjectives.
   */
  @Test
  public void checkAdjectives() {
    Map<String, PartOfSpeech> desiredReturns = new HashMap<String, PartOfSpeech>();
    List<String> inputs = new ArrayList<String>();

    final String NOUN = "person";
    final String VERB = "runs"; // won't be checked as gerund b/c doesn't end in "ing"

    // add all adjectives
    for (String adjective: ADJECTIVES) {
      desiredReturns.put(adjective, buildPartOfSpeech(WordType.ADJECTIVE));
      inputs.add(adjective);
    }

    desiredReturns.put(NOUN, buildPartOfSpeech(WordType.NOUN));
    inputs.add(NOUN);
    desiredReturns.put(VERB, buildPartOfSpeech(Tag.VERB));
    inputs.add(VERB);

    NLServiceClient client = new NLServiceClient(createMockLSClient(desiredReturns));

    Map<WordType, List<String>> groupedWords = client.groupByWordType(inputs);

    // should be 3 keys (adjective, noun & unusable [the verb])
    Assert.assertEquals(3, groupedWords.keySet().size());
    Assert.assertEquals(Arrays.asList(ADJECTIVES), groupedWords.get(WordType.ADJECTIVE));
  }

  /**
   * Check that gerunds are correctly identified.
   */
  @Test
  public void checkGerunds() {
    Map<String, PartOfSpeech> desiredReturns = new HashMap<String, PartOfSpeech>();
    List<String> inputs = new ArrayList<String>();
    
    final String[] GERUNDS = { "baking", "jogging", "running"};
    // have one that doesn't end in ing & one that won't be classified as a verb
    // when called with 'is' & one that is two words
    final String[] NOT_GERUNDS = { "hello", "swing", "vigorous jogging" };
    
    for (String gerund: GERUNDS) {
      addGerund(desiredReturns, gerund);
      inputs.add(gerund);
    }

    // add "hello" as a gerund to confirms that words
    // that don't end with "ing" aren't classified as gerund
    // even if NL API says they're a verb
    addGerund(desiredReturns, NOT_GERUNDS[0]);
    inputs.add(NOT_GERUNDS[0]);

    // check that words that end with ("ing") like swing
    // that aren't gerunds aren't considered gerunds
    desiredReturns.put(NOT_GERUNDS[1], buildPartOfSpeech(WordType.NOUN));
    inputs.add(NOT_GERUNDS[1]);

    // check that a word that would usually be a gerund (jogging) isn't 
    // when it's passed in as multiple words 
    addGerund(desiredReturns, NOT_GERUNDS[0]);
    inputs.add(NOT_GERUNDS[2]);

    NLServiceClient client = new NLServiceClient(createMockLSClient(desiredReturns));

    Map<WordType, List<String>> groupedWords = client.groupByWordType(inputs);

    // should be 4 keys (multiword noun, noun, verb, unusable) based off input we gave
    Assert.assertEquals(4, groupedWords.keySet().size());
    Assert.assertEquals(Arrays.asList(GERUNDS), groupedWords.get(WordType.GERUND));
  }

  /**
   * Check that sorting works with mixed input.
   */
  @Test
  public void mixedInput() {
    Map<String, PartOfSpeech> desiredReturns = new HashMap<String, PartOfSpeech>();
    List<String> inputs = new ArrayList<String>();
    
    final String[] UNUSABLE = { "runs", "jumps", "typo" }; 

    for (String multiwordInput: MULTIWORD_INPUTS) {
      inputs.add(multiwordInput);
    }

    for (String whitespaceInput: WHITESPACE_INPUTS) {
      inputs.add(whitespaceInput);
    }

    for (String noun: NOUNS) {
      desiredReturns.put(noun, buildPartOfSpeech(WordType.NOUN));
      inputs.add(noun);
    }

    for (String properNoun: PROPER_NOUNS) {
      desiredReturns.put(properNoun, buildPartOfSpeech(WordType.PROPER_NOUN));
      inputs.add(properNoun);
    }
    
    for (String adjective: ADJECTIVES) {
      desiredReturns.put(adjective, buildPartOfSpeech(WordType.ADJECTIVE));
      inputs.add(adjective);
    }

    for (String gerund: GERUNDS) {
      addGerund(desiredReturns, gerund);
      inputs.add(gerund);
    }

    for (String unusableWord: UNUSABLE) {
      inputs.add(unusableWord);
    }

    NLServiceClient client = new NLServiceClient(createMockLSClient(desiredReturns));

    Map<WordType, List<String>> groupedWords = client.groupByWordType(inputs);

    List<String> unusableWords = new ArrayList<String>();
    unusableWords.addAll(Arrays.asList(WHITESPACE_INPUTS));
    unusableWords.addAll(Arrays.asList(UNUSABLE));
    
    // all types of word types present 
    Assert.assertEquals(6, groupedWords.keySet().size());
    Assert.assertEquals(Arrays.asList(MULTIWORD_INPUTS), groupedWords.get(WordType.MULTIWORD_NOUN));
    Assert.assertEquals(Arrays.asList(NOUNS), groupedWords.get(WordType.NOUN));
    Assert.assertEquals(Arrays.asList(PROPER_NOUNS), groupedWords.get(WordType.PROPER_NOUN));
    Assert.assertEquals(Arrays.asList(ADJECTIVES), groupedWords.get(WordType.ADJECTIVE));
    Assert.assertEquals(Arrays.asList(GERUNDS), groupedWords.get(WordType.GERUND));
    Assert.assertEquals(unusableWords, groupedWords.get(WordType.UNUSABLE));
  }

  /** 
   * Add the word as a gerund to the given map.
   * 
   * @param map the map to add the parts of speech for gerund to
   * @param word the word to add as a gerund
   */
  private void addGerund(Map<String, PartOfSpeech> map, String word) {
    PartOfSpeech verb = PartOfSpeech.newBuilder().setTag(Tag.VERB).build();

    map.put("is", verb);
    map.put(word, verb);
  }

  /** 
   * Build a PartOfSpeech object for the given type.
   * 
   * @param type the WordType for which to create a part of speech
   * @return a part of speech object for this type.
   */
  private PartOfSpeech buildPartOfSpeech(WordType type) {
    PartOfSpeech.Builder builder = PartOfSpeech.newBuilder();

    switch(type) {
      case PROPER_NOUN:
        builder.setProper(Proper.PROPER);
      case NOUN: 
        builder.setTag(Tag.NOUN);
        break;
      case ADJECTIVE:
        builder.setTag(Tag.ADJ);
        break;
      default:
        builder.setTag(Tag.UNKNOWN); 
    }

    return builder.build();
  }
  
  private PartOfSpeech buildPartOfSpeech(Tag tag) {
    return PartOfSpeech.newBuilder().setTag(tag).build();
  }

  /**
   * Sets up a mock version of the LanguageServiceClient that will return
   * canned PartOfSpeech values for words passed in as keys in the
   * desiredReturns map.
   *
   * @param desiredReturns the canned values we want the mock client to return
   * @return the mocked version of LanguageServiceClient
   */
  private LanguageServiceClient createMockLSClient(Map<String, PartOfSpeech> desiredReturns) {
    LanguageServiceClient mockClient = mock(LanguageServiceClient.class);

    when(mockClient.analyzeSyntax(any(Document.class))).thenAnswer(new Answer() {
      public Object answer(InvocationOnMock invocation) {
        Object[] args = invocation.getArguments();

        Document document = (Document) args[0];
        String text = document.getContent();
        String[] words = text.split(" ");

        List<Token> tokens = new ArrayList<Token>();

        for (String word: words) {
          // create a default part of speech
          PartOfSpeech partOfSpeech = PartOfSpeech.newBuilder()
              .setTag(Tag.UNKNOWN).build();

          // get the desired part of speech (if there is one)
          if (desiredReturns.containsKey(word)) {
            partOfSpeech = desiredReturns.get(word);
          }

          // add the relevant token with either the default or desired part of speech
          tokens.add(Token.newBuilder().setPartOfSpeech(partOfSpeech).build());
        }

        AnalyzeSyntaxResponse response = AnalyzeSyntaxResponse.newBuilder()
            .addAllTokens(tokens).build();

        return response;
      }
    });

    return mockClient;
  }
}
