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

package com.google.sps.data.perspective;

import au.com.origma.perspectiveapi.v1alpha1.PerspectiveAPI;
import au.com.origma.perspectiveapi.v1alpha1.models.AnalyzeCommentRequest;
import au.com.origma.perspectiveapi.v1alpha1.models.AnalyzeCommentRequest.Builder;
import au.com.origma.perspectiveapi.v1alpha1.models.AnalyzeCommentResponse;
import au.com.origma.perspectiveapi.v1alpha1.models.AttributeScore;
import au.com.origma.perspectiveapi.v1alpha1.models.AttributeType;
import au.com.origma.perspectiveapi.v1alpha1.models.ContentType;
import au.com.origma.perspectiveapi.v1alpha1.models.Entry;
import au.com.origma.perspectiveapi.v1alpha1.models.RequestedAttribute;
import au.com.origma.perspectiveapi.v1alpha1.models.Score;

/**
 * A service client to access the Perspective API 
 */
public class PerspectiveServiceClient {
  
  /** 
   * Generates an instance of the PerspectiveAPI using a Java file PerspectiveAPIKey 
   * that may not be present (due to security concerns)
   *
   * @return a funcioning instance of PerspectiveAPI
   * @throws ClassNotFoundException if it cannot found "PerspectiveAPIKey.java"
   * @throws NoSuchMethodException if PerspectiveAPIKey doesn't have a getKey() method
   * @throws IllegalAccessException if class called from doesn't have proper access to getKey()
   * @throws InvocationTargetException if getKey() itself throws an exception
   */
  public PerspectiveAPI generateAPI() throws ClassNotFoundException, NoSuchMethodException,
      IllegalAccessException, InvocationTargetException  {
    
    // creates a string apiKey and lets it be null 
    String apiKey = null;
    
    // fetch the PerspectiveAPIKey class if it's there
    Class<?> keyClass = Class.forName("com.google.sps.data.perspective.PerspectiveAPIKey");
    
    // create a method getKey that takes no parameters 
    // (which is what null param of getMethod signifies)
    Method getKey = keyClass.getMethod("getKey", null);

    // invoke this static method (first null means it's static 
    // & second null means it does not need arguments) & stores result
    apiKey = (String) getKey.invoke(null, null);

    return PerspectiveAPI.create(apiKey);
  }

  /**
   * Return a PerspectiveAnalysis object with all requested attribute types for a specified piece of text
   * using a specified instance of the PerspectiveAPI
   *
   * @param perspective an instance of the PerspectiveAPI to use to analyze the text for the requested attributeTypes
   * @param attributeTypes the requested attribute types to analyze the text for
   * @param text the text to be analyzed by Perspective API
   * @return an object containing all of the scores from the PerspectiveAPI
   */
  public static PerspectiveAnalysis analyze(PerspectiveAPI perspective, AttributeType[] attributeTypes, String text) {
    AnalyzeCommentRequest.Builder builder = new AnalyzeCommentRequest.Builder().comment(
        new Entry.Builder().type(ContentType.PLAIN_TEXT).text(text).build());

    // add all the attribute types we want to this builder
    for (AttributeType attributeType : attributeTypes) {
      builder.addRequestedAttribute(attributeType, null);
    }

    AnalyzeCommentRequest request = builder.build();
    AnalyzeCommentResponse response = perspective.analyze(request);
    
    AttributeAnalysis[] analyses = new AttributeAnalysis[attributeTypes.length];

    for (int i = 0; i < attributeTypes.length; i++) {
      AttributeType attributeType = attributeTypes[i];

      analyses[i] = new AttributeAnalysis(extractScore(response, attributeType), attributeType);
    }

    // Convert response to PerspectiveAnalysis.
    PerspectiveAnalysis analysis = new PerspectiveAnalysis(text, analyses);

    return analysis;
  }

  /**
   * private helper method to extract the score for a given response & type
   *
   * @return the score for the response for a given type
   */
  private static float extractScore(AnalyzeCommentResponse response, AttributeType type) {
    AttributeScore attributeScore = response.getAttributeScore(type);
    Score score = attributeScore.getSummaryScore();
    return score.getValue();
  }
}
