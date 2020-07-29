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

public final class PromptManager {

  private List<String> keywords;
  
  /**
   * Initialize labels parameter
   * 
   * @param labels A list of Strings containing keywords for prompts.
   */
  public PromptManager(List<String> keywords){
    this.keywords = keywords;
  }

  /**
   * Generates prompt using labels.
   * 
   * @return A String containing the output prompt.
   */
  public String generatePrompt() {
    String prompt = "";
    if(keywords.size() == 0){
        return "";
    }


    for(int incrementer = 0; incrementer < keywords.size() - 1; incrementer++){
        prompt = prompt + keywords.get(incrementer) + " ";
    }
    prompt+=keywords.get(keywords.size()-1);
    prompt += ".";

    return prompt;
  }


  /**
   * An executable demonstration of prompt generation.
   */
  public static void main(String[] args) {
    Scanner input = new Scanner(System.in);

    List<String> inputKeywords = new ArrayList<String>();
    System.out.println("Please enter the first label");
    String inputKeyword = input.nextLine();
    inputKeywords.add(inputKeyword);

    System.out.println("Please enter the second label");
    inputKeyword = input.nextLine();
    inputKeywords.add(inputKeyword);

    System.out.println("Please enter the third label");
    inputKeyword = input.nextLine();
    inputKeywords.add(inputKeyword);

    PromptManager testPromptGeneration = new PromptManager(inputKeywords);
    System.out.println(testPromptGeneration.generatePrompt());
  }
}
