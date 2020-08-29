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

import com.google.sps.APINotAvailableException;
import com.google.sps.story.data.*;
import java.io.IOException;
import java.lang.Exception;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Test PromptManager
 */
@RunWith(MockitoJUnitRunner.class)
public final class PromptManagerTest {
  private final String NOUN_DOG = "dog";
  private final String NOUN_CAT = "cat";
  private final String NOUN_TREE = "tree";

  @Test(expected = IllegalArgumentException.class)
  /**
   * Verifies an IllegalArgumentException is thrown with null input.
   *
   */
  public void nullListInput() throws IllegalArgumentException {
    PromptManager promptManager = new PromptManager(null);
  }

  @Test
  /**
   * Verifies a prompt is returned with "Once upon a time," at the beginning.
   *
   */
  public void listMethodTwoNounsNonRandom() throws IOException, APINotAvailableException {
    // Prepare input list
    List<String> inputList = new ArrayList<String>();
    inputList.add(NOUN_DOG);
    inputList.add(NOUN_CAT);

    PromptManager promptManager = new PromptManager(inputList);

    // Inject mock PromptManagerBodyGenerator
    PromptManagerBodyGenerator mockGenerator = mock(PromptManagerBodyGenerator.class);
    promptManager.setPromptManagerBodyGenerator(mockGenerator);

    // Stub generator calls to output canned prompt body.
    when(mockGenerator.generateBody()).thenReturn("the dog and the cat ran away.");

    // Ensure "Once upon a time," is concatenated to canned body.
    String expected = "Once upon a time, the dog and the cat ran away.";
    String actual = promptManager.generatePrompt();

    verify(mockGenerator).generateBody();
    Assert.assertEquals(expected, actual);
  }
}
