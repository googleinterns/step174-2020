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

import com.google.sps.APINotAvailableException;
import com.google.sps.story.data.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Facilitates API calls to Cloud Natural Language
 * and Datamuse through a single object.
 */
public class PromptManagerAPIsClient {
  /** Client object for word classification */
  private NLServiceClient nlServiceClient;
  /** Client object for word fetching */
  private DatamuseRequestClient datamuseRequestClient;

  /**
   * Initialize DataMuseRequestClient object.
   *
   * @throws IOException Exception for NLServiceClient instantiation error.
   */
  public PromptManagerAPIsClient() throws IOException {
    // Instantiate datamuseRequestClient
    datamuseRequestClient = new DatamuseRequestClient();
  }

  /**
   * Sets NLServiceClient instance for word classifying.
   *
   * @param NLServiceClient The NLServiceClient instance to set.
   */
  public void setNLServiceClient(NLServiceClient nlServiceClient) {
    this.nlServiceClient = nlServiceClient;
  }

  /**
   * Sets DatamuseRequestClient instance for word classifying.
   *
   * @param DatamuseRequestClient The DatamuseRequestClient instance to set.
   */
  public void setDatamuseRequestClient(DatamuseRequestClient datamuseRequestClient) {
    this.datamuseRequestClient = datamuseRequestClient;
  }

  /**
   * Classifies an input of strings based on their part of speech.
   *
   * @param words A list of Strings containing keywords for prompts.
   * @return A mapping of WordTypes to given words.
   * @throws IOException Exception for network problem.
   */
  public Map<WordType, List<String>> groupByWordType(List<String> words) throws IOException {
    if (nlServiceClient == null) {
      nlServiceClient = new NLServiceClient();
    }
    Map<WordType, List<String>> groupings = nlServiceClient.groupByWordType(words);
    nlServiceClient.close();

    return groupings;
  }

  /**
   * Fetches related adjectives to a given noun.
   *
   * @param noun The noun to get adjectives for
   * @param cap A count of how many adjectives to return
   * @param isRandom Determines whether or not to shuffle output
   * @return An array of related adjectives.
   * @throws APINotAvailableException Exception for network/availability issues.
   * @throws RuntimeException Exception for API runtime issues.
   * @throws IllegalArgumentException Exception for improper input to API.
   */
  public String[] fetchRelatedAdjectives(String noun, int cap, boolean isRandom)
      throws APINotAvailableException, RuntimeException, IllegalArgumentException {
    try {
      String storytellingTopic = DatamuseRequestClient.getRandomStorytellingTopic();
      String[] relatedAdjectives = datamuseRequestClient.fetchRelatedWords(
          noun, DatamuseRelatedWordType.ADJECTIVE, cap, storytellingTopic);

      if (isRandom) {
        List<String> adjectiveList = Arrays.asList(relatedAdjectives);
        Collections.shuffle(adjectiveList);
        String[] shuffledAdjectives = adjectiveList.toArray(new String[adjectiveList.size()]);
        return shuffledAdjectives;
      } else {
        return relatedAdjectives;
      }

    } catch (Exception apiException) {
      throw apiException;
    }
  }
}
