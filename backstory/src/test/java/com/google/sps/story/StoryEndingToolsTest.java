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

import com.google.sps.story.data.StoryEndingTools;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Quality Tests for StoryEndingTools
 */
@RunWith(JUnit4.class)
public final class StoryEndingToolsTest {

  // strings to use in testing story ending tools
  private final static String EMPTY_TEXT = "";
  private final static String SENTENCE_FRAGMENT = "This story is not ";
  private final static String COMPLETE_SENTENCE = "This story is not incomplete.";
  private final static String QUESTION_FRAGMENT = "What is this? I don't ";
  private final static String COMPLETE_QUESTION = "I didn't realize. What's happening here?";
  private final static String EXCLAMATION_FRAGMENT = "Oh no! This isn't ";
  private final static String COMPLETE_EXCLAMATION = "I had no idea. Oh no!";

  // aggregate all of these into one input array
  private final static String[] INPUTS = { EMPTY_TEXT, SENTENCE_FRAGMENT, COMPLETE_SENTENCE, 
                                    QUESTION_FRAGMENT, COMPLETE_QUESTION, 
                                    EXCLAMATION_FRAGMENT, COMPLETE_EXCLAMATION };

  // array of sentence enders
  private final static String[] SENTENCE_ENDERS = StoryEndingTools.SENTENCE_ENDERS;

  /**
   * Check that sending in null input to endStory() 
   * causes an IllegalArgumentException.
   */
  @Test (expected = IllegalArgumentException.class)
  public void nullInputToEndStory() {
    StoryEndingTools.endStory(null);
  }

  /**
   * Check that sending in null input to removeSentenceFragmentAtEnd() 
   * causes an IllegalArgumentException.
   */
  @Test (expected = IllegalArgumentException.class)
  public void nullInputToRemoveSentenceFragment() {
    StoryEndingTools.removeSentenceFragmentAtEnd(null);
  }

  /**
   * Check that sending in null input to addEnding() 
   * causes an IllegalArgumentException.
   */
  @Test (expected = IllegalArgumentException.class)
  public void nullInputToAddEnding() {
    StoryEndingTools.addEnding(null);
  }

  /**
   * Private helper method to check if text ends in punctuation.
   * 
   * @param text the text to check ending for
   * @return true, if the char at the last index is in SENTENCE_ENDERS
   *
   */
  private static boolean endsInSentenceEndingPunctuation(String text) {

    String endCharacter = text.substring(text.length() - 1);

    List<String> sentenceEnders = Arrays.asList(SENTENCE_ENDERS);

    return sentenceEnders.contains(endCharacter);
  }

  /**
   * Check that output from addEnding ends
   * with correct punctuation.
   */
  @Test
  public void addEndingAlwaysEndsProperly() {
    // go through all inputs and check that all have
    // a proper punctuation at end 

    for (String input: INPUTS) {
      Assert.assertTrue(endsInSentenceEndingPunctuation(StoryEndingTools.addEnding(input)));
    }
  }
  
  /**
   * Check that output from addEnding is longer than the input.
   */
  @Test
  public void addEndingMakesInputLonger() {
    for (String input: INPUTS) {
      String inputWithEnding = StoryEndingTools.addEnding(input);

      Assert.assertTrue(inputWithEnding.length() > input.length());
    }
  }

  /**
   * Check that output from removeSentenceFragmentAtEnd
   * with empty string is correct
   */
  @Test
  public void emptyInputForRemoveFragment() {
    Assert.assertEquals("", StoryEndingTools.removeSentenceFragmentAtEnd(EMPTY_TEXT));
  }  

  /**
   * Check that output from removeSentenceFragmentAtEnd
   * with sentence fragments as input is correct.
   */
  @Test
  public void fragmentInputForRemoveFragment() {
    String expectedForSentenceFragment = "";
    String actualForSentenceFragment = StoryEndingTools.removeSentenceFragmentAtEnd(SENTENCE_FRAGMENT);

    Assert.assertEquals(expectedForSentenceFragment, actualForSentenceFragment);

    String expectedForQuestionFragment = "What is this?";
    String actualForQuestionFragment = StoryEndingTools.removeSentenceFragmentAtEnd(QUESTION_FRAGMENT);

    Assert.assertEquals(expectedForQuestionFragment, actualForQuestionFragment);

    String expectedForExclamationFragment = "Oh no!";
    String actualForExclamationFragment = StoryEndingTools.removeSentenceFragmentAtEnd(EXCLAMATION_FRAGMENT);

    Assert.assertEquals(expectedForExclamationFragment, actualForExclamationFragment);
  }  

  /**
   * Check that output from removeSentenceFragmentAtEnd
   * with complete sentences as input is correct.
   * (shouldn't remove anything)
   */
  @Test
  public void sentenceInputWithRemoveFragment() {
    Assert.assertEquals(COMPLETE_SENTENCE, StoryEndingTools.removeSentenceFragmentAtEnd(COMPLETE_SENTENCE));
    Assert.assertEquals(COMPLETE_QUESTION, StoryEndingTools.removeSentenceFragmentAtEnd(COMPLETE_QUESTION));
    Assert.assertEquals(COMPLETE_EXCLAMATION, StoryEndingTools.removeSentenceFragmentAtEnd(COMPLETE_EXCLAMATION));
  }

  /**
   * Check that output from endStory always ends with correct
   * punctuation.
   */
  @Test
  public void endStoryAlwaysEndsProperly() {
    for (String input: INPUTS) {
      Assert.assertTrue(endsInSentenceEndingPunctuation(StoryEndingTools.endStory(input)));
    }
  }

  /**
   * Check that output from endStory removes sentence fragments
   * from the inputs with fragments.
   */
  public void ensureFragmentRemovedForInputWithFragment() {    
    // check this by making sure that the complete sentence
    // part is still present at zero, but the fragment isn't 
    // present at all.
    
    // check for sentence fragment
    String completePartOfSentence = "";
    String fragmentPartOfSentence = "This story is not";
    String outputForSentenceFragment = StoryEndingTools.endStory(SENTENCE_FRAGMENT);
    
    Assert.assertEquals(0, outputForSentenceFragment.indexOf(completePartOfSentence));
    Assert.assertEquals(-1, outputForSentenceFragment.indexOf(fragmentPartOfSentence));

    // check for question fragment
    String completePartOfQuestion = "What is this?";
    String fragmentPartOfQuestion = "I don't";
    String outputForQuestionFragment = StoryEndingTools.endStory(QUESTION_FRAGMENT);
    
    Assert.assertEquals(0, outputForQuestionFragment.indexOf(completePartOfQuestion));
    Assert.assertEquals(-1, outputForQuestionFragment.indexOf(fragmentPartOfQuestion));

    // check for exlamation fragment
    String completePartOfExclamation = "Oh no!";
    String fragmentPartOfExclamation = "This isn't";
    String outputForExclamationFragment = StoryEndingTools.endStory(EXCLAMATION_FRAGMENT);
    
    Assert.assertEquals(0, outputForExclamationFragment.indexOf(completePartOfExclamation));
    Assert.assertEquals(-1, outputForExclamationFragment.indexOf(fragmentPartOfExclamation));
  }

  /**
   * Check that output from endStory still have whole sentences
   * in output for inputs without fragments.
   */
  public void ensureNothingRemovedForInputWithoutFragment() {  
    // check this by checking complete sentences are still at index 0 

    String outputForSentence = StoryEndingTools.endStory(COMPLETE_SENTENCE);
    Assert.assertEquals(0, outputForSentence.indexOf(COMPLETE_SENTENCE));

    String outputForQuestion = StoryEndingTools.endStory(COMPLETE_QUESTION);
    Assert.assertEquals(0, outputForQuestion.indexOf(COMPLETE_QUESTION));

    String outputForExclamation = StoryEndingTools.endStory(COMPLETE_EXCLAMATION);
    Assert.assertEquals(0, outputForExclamation.indexOf(COMPLETE_EXCLAMATION));
  }
}
