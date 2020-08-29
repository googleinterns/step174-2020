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
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Test the Prompt Creation Suite
 */
@RunWith(MockitoJUnitRunner.class)
public final class PromptManagerAPIsClientTest {
  private List<String> inputList;
  private List<String> expectedList;

  @Test
  /**
   * Ensure groupByWordType() calls the NLServiceClient method.
   *
   */
  public void groupByWordTypeCallsAPI() throws IOException, APINotAvailableException {
    // Prepare input list
    List<String> inputList = new ArrayList<String>();
    // Prepare output output map
    Map<WordType, List<String>> sampleOutputMap = new HashMap<WordType, List<String>>();

    PromptManagerAPIsClient promptManagerAPIsClient = new PromptManagerAPIsClient();

    // Inject mock API instance
    NLServiceClient mockNLServiceClient = mock(NLServiceClient.class);
    promptManagerAPIsClient.setNLServiceClient(mockNLServiceClient);

    // Stub API Call to return null map
    when(mockNLServiceClient.groupByWordType(anyList())).thenReturn(sampleOutputMap);

    Map<WordType, List<String>> actualOutput = promptManagerAPIsClient.groupByWordType(inputList);

    // Ensure proper API Call
    verify(mockNLServiceClient).groupByWordType(inputList);
    // Ensure canned API output is returned.
    Assert.assertEquals(actualOutput, sampleOutputMap);
  }

  @Test
  /**
   * Ensure fetchRelatedAdjectives() calls the DatamuseRequestClient method.
   *
   */
  public void fetchRelatedAdjectivesCallsAPI() throws IOException, APINotAvailableException {
    // Prepare input string
    String inputWord = "";
    // Prepare input cap
    int inputCap = 1;
    // Prepare canned topic resource
    String sampleTopic = "Sampletopic";

    // Prepare output output array.
    String[] sampleOutput = {"String One", "String two"};

    PromptManagerAPIsClient promptManagerAPIsClient = new PromptManagerAPIsClient();

    // Inject mock API instance
    DatamuseRequestClient mockDatamuseRequestClient = mock(DatamuseRequestClient.class);
    promptManagerAPIsClient.setDatamuseRequestClient(mockDatamuseRequestClient);

    ArgumentCaptor<String> acTopic = ArgumentCaptor.forClass(String.class);
    // Stub API Call to return null array
    when(mockDatamuseRequestClient.fetchRelatedWords(
             anyString(), any(), anyInt(), acTopic.capture()))
        .thenReturn(sampleOutput);

    // Get unshuffled output.
    String[] actualOutput =
        promptManagerAPIsClient.fetchRelatedAdjectives(inputWord, inputCap, false);

    // Ensure proper API Call
    verify(mockDatamuseRequestClient)
        .fetchRelatedWords(
            inputWord, DatamuseRelatedWordType.ADJECTIVE, inputCap, acTopic.getValue());
    // Ensure canned API output is returned.
    Assert.assertEquals(actualOutput, sampleOutput);
  }

  @Test
  /**
   * Ensure fetchRelatedAdjectives() calls the DatamuseRequestClient method, verify containing
   * for shuffled method.
   *
   */
  public void fetchRelatedAdjectivesWithShufflingCallsAPI()
      throws IOException, APINotAvailableException {
    // Prepare input string
    String inputWord = "";
    // Prepare input cap
    int inputCap = 1;
    // Prepare canned topic resource
    String sampleTopic = "Sampletopic";

    // Prepare output output array.
    String[] sampleOutput = {"String One", "String two"};

    PromptManagerAPIsClient promptManagerAPIsClient = new PromptManagerAPIsClient();

    // Inject mock API instance
    DatamuseRequestClient mockDatamuseRequestClient = mock(DatamuseRequestClient.class);
    promptManagerAPIsClient.setDatamuseRequestClient(mockDatamuseRequestClient);

    ArgumentCaptor<String> acTopic = ArgumentCaptor.forClass(String.class);
    // Stub API Call to return null array
    when(mockDatamuseRequestClient.fetchRelatedWords(
             anyString(), any(), anyInt(), acTopic.capture()))
        .thenReturn(sampleOutput);

    // Get shuffled output.
    String[] actualOutput =
        promptManagerAPIsClient.fetchRelatedAdjectives(inputWord, inputCap, true);

    // Ensure proper API Call
    verify(mockDatamuseRequestClient)
        .fetchRelatedWords(
            inputWord, DatamuseRelatedWordType.ADJECTIVE, inputCap, acTopic.getValue());

    List<String> sampleAsList = Arrays.asList(sampleOutput);
    List<String> outputAsList = Arrays.asList(actualOutput);

    // Ensure shuffled output contains all strings.
    boolean containsCheck = true;
    for (String sample : sampleAsList) {
      if (!outputAsList.contains(sample)) {
        containsCheck = false;
      }
    }

    // Ensure canned API output is returned.
    Assert.assertTrue(containsCheck);
  }
}
