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

package com.google.sps.servlets;

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
 * Test the Prompt Creation Suite
 */
@RunWith(JUnit4.class)
public final class PromptManagerTest {
  private static final String FIRST_KEYWORD = "First_Keyword";
  private static final String SECOND_KEYWORD = "Second_Keyword";
  private static final String THIRD_KEYWORD = "Third_Keyword";

  private List<String> inputList;
  @Before
  public void setUp() {
    inputList = new ArrayList<String>();
  }

  @Test
  /** Ensure the output prompt contains all keywords. */
  public void verifyContains() {
    inputList.add(FIRST_KEYWORD);
    inputList.add(SECOND_KEYWORD);
    inputList.add(THIRD_KEYWORD);

    PromptManager testPromptManager = new PromptManager(inputList);
    String outputPrompt = testPromptManager.generatePrompt(" ");

    Assert.assertEquals(outputPrompt, "First_Keyword Second_Keyword Third_Keyword.");
  }

  @Test
  /** Ensure empty prompt return for empty input. */
  public void noKeywordInput() {
    PromptManager testPromptManager = new PromptManager(inputList);
    String expected = "";
    String actual = testPromptManager.generatePrompt(" ");

    Assert.assertEquals(expected, actual);
  }

  @Test
  /** Ensures period at end of output. */
  public void endsInPeriod() {
    inputList.add(FIRST_KEYWORD);
    inputList.add(SECOND_KEYWORD);
    inputList.add(THIRD_KEYWORD);
    PromptManager testPromptManager = new PromptManager(inputList);
    String expected = ".";
    String generated = testPromptManager.generatePrompt(" ");
    String actual = generated.substring(generated.length() - 1);

    Assert.assertEquals(expected, actual);
  }
}