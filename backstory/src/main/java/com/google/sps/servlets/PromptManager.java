// Copyright 2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//s
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.google.sps.servlets;

import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;

/**
 * Creates prompt string for text generation using input keyword strings.
 */
public final class PromptManager {

  private List<String> keywords;
  
  /**
   * Initialize labels parameter.
   * 
   * @param labels A list of Strings containing keywords for prompts.
   */
  public PromptManager(List<String> keywords){
    this.keywords = keywords;
  }

  /**
   * Generates prompt using labels.
   * 
   * @param delimiter String for delimiter between appended strings.
   * @return A String containing the output prompt.
   */
  public String generatePrompt(String delimiter) {
    String prompt = "";

    //Check if valid.
    if(keywords.size() == 0){
        return "";
    }

    //Append strings.
    for(int incrementer = 0; incrementer < keywords.size() - 1; incrementer++){
        prompt = prompt + keywords.get(incrementer) + delimiter;
    }
    prompt += keywords.get(keywords.size()-1);
    prompt += ".";

    return prompt;
  }

}
