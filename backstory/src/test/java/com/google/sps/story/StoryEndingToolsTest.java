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
  private final String EMPTY_TEXT = "";
  private final String SENTENCE_FRAGMENT = "This story is not ";
  private final String COMPLETE_SENTENCE = "This story is not incomplete.";
  private final String QUESTION_FRAGMENT = "What is this? I don't ";
  private final String COMPLETE_QUESTION = "I didn't realize. What's happening here?";
  private final String EXCLAMATION_FRAGMENT = "Oh no! This isn't ";
  private final String COMPLETE_EXCLAMATION = "I had no idea. Oh no!";

  // aggregate all of these into one input array
  private final String[] INPUTS = { EMPTY_TEXT, SENTENCE_FRAGMENT, COMPLETE_SENTENCE, 
                                    QUESTION_FRAGMENT, COMPLETE_QUESTION, 
                                    EXCLAMATION_FRAGMENT, COMPLETE_EXCLAMATION };
  // aggregate by other categories
  private final String[] FRAGMENTS = { SENTENCE_FRAGMENT, QUESTION_FRAGMENT, EXCLAMATION_FRAGMENT };
  private final String[] COMPLETE_SENTENCES = { COMPLETE_SENTENCE, COMPLETE_QUESTION, COMPLETE_EXCLAMATION };

  // array of sentence enders
  private final String[] SENTENCE_ENDERS = {".", "?", "!"};

  /**
   * Check that sending in null input to endStory() 
   * causes an IllegalArgumentException.
   */
  @Test (expected = IllegalArgumentException.class)
  public void nullInputToEndStory() {
    StoryEndingTools.endStory(null);
  }

  /**
   * Check that sending in null input to 
   * removeSentenceFragmentAtEnd() to make sure
   * it throws an IllegalArgumentException.
   */
  @Test (expected = IllegalArgumentException.class)
  public void nullInputToRemoveSentenceFragment() {
    StoryEndingTools.removeSentenceFragmentAtEnd(null);
  }

  /**
   * Check that sending in null input to addEnding()
   * to make sure it throws an IllegalArgumentException.
   */
  @Test (expected = IllegalArgumentException.class)
  public void nullInputToAddEnding() {
    StoryEndingTools.addEnding(null);
  }

  /**
   * Check that output from addEnding ends
   * with correct punctuation.
   */
  @Test
  public void addEndingAlwaysEndsProperly() {
    // go through all inputs and check that all end properly 
    // after adding an ending from story ending 
    // (if ending not added, then this will fail)

    for (String input: INPUTS) {
      int lastSentenceEnder = -1;
      String inputWithEnding = StoryEndingTools.addEnding(input);

      for (String ender: SENTENCE_ENDERS) {
        int lastIndex = inputWithEnding.lastIndexOf(ender);

        if (lastIndex > lastSentenceEnder) {
          lastSentenceEnder = lastIndex;
        }
      }

      Assert.assertEquals(inputWithEnding.length() - 1, lastSentenceEnder);
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
   * with empty string as input is the right length.
   */
  @Test
  public void emptyInputForRemoveFragment() {
    Assert.assertEquals(0, StoryEndingTools.removeSentenceFragmentAtEnd(EMPTY_TEXT).length());
  }  

  /**
   * Check that output from removeSentenceFragmentAtEnd
   * with sentence fragments as input is the right length.
   */
  @Test
  public void fragmentInputForRemoveFragment() {
    for (String inputWithFragment: FRAGMENTS) {
      int lastSentenceEnder = -1;

      for (String ender: SENTENCE_ENDERS) {
        int lastIndex = inputWithFragment.lastIndexOf(ender);

        if (lastIndex > lastSentenceEnder) {
          lastSentenceEnder = lastIndex;
        }
      }

      Assert.assertEquals(lastSentenceEnder + 1, StoryEndingTools.removeSentenceFragmentAtEnd(inputWithFragment).length());
    }
  }  

  /**
   * Check that output from removeSentenceFragmentAtEnd
   * with complete sentences as input is the right length.
   */
  @Test
  public void sentenceInputWithRemoveFragment() {
    for (String sentence: COMPLETE_SENTENCES) {
      Assert.assertEquals(sentence.length(), StoryEndingTools.removeSentenceFragmentAtEnd(sentence).length());
    }
  }

  /**
   * Check that output from endStory always ends with correct
   * punctuation.
   */
  @Test
  public void endStoryAlwaysEndsProperly() {
    for (String input: INPUTS) {
      int lastSentenceEnder = -1;
      String inputEnded = StoryEndingTools.endStory(input);

      for (String ender: SENTENCE_ENDERS) {
        int lastIndex = inputEnded.lastIndexOf(ender);

        if (lastIndex > lastSentenceEnder) {
          lastSentenceEnder = lastIndex;
        }
      }

      Assert.assertEquals(inputEnded.length() - 1, lastSentenceEnder);
    }
  }

  /**
   * Check that output from endStory removes sentence fragments if
   * there are any.
   */
  public void ensureFragmentRemoved() {    
    // if it's a fragment, then with the ending removed, it should
    // be shorter than it was before
    for (String inputWithFragment: FRAGMENTS) {
      String output = StoryEndingTools.endStory(inputWithFragment);
      // remove last sentence by removing punctuation then calling remove fragment
      String outputWithoutEnding = StoryEndingTools.removeSentenceFragmentAtEnd(output.substring(0, output.length() - 1));

      Assert.assertTrue(inputWithFragment.length() > outputWithoutEnding.length());    
    }

    // if it's a sentence, then with the ending removed, it should
    // be same length as it was before
    for (String sentence: COMPLETE_SENTENCES) {
      String output = StoryEndingTools.endStory(sentence);
      // remove last sentence by removing punctuation then calling remove fragment
      String outputWithoutEnding = StoryEndingTools.removeSentenceFragmentAtEnd(output.substring(0, output.length() - 1));

      Assert.assertEquals(sentence.length(), outputWithoutEnding.length());
    }
  }
}