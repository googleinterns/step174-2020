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

import au.com.origma.perspectiveapi.v1alpha1.PerspectiveAPI;
import com.google.gson.Gson;
import com.google.sps.APINotAvailableException;
import com.google.sps.perspective.PerspectiveStoryAnalysisManager;
import com.google.sps.perspective.StoryAnalysisManager;
import com.google.sps.perspective.data.NoAppropriateStoryException;
import com.google.sps.perspective.data.PerspectiveAPIFactory;
import com.google.sps.perspective.data.PerspectiveAPIFactoryImpl;
import com.google.sps.perspective.data.PerspectiveAPIClient;
import com.google.sps.perspective.data.PerspectiveDecision;
import com.google.sps.perspective.data.PerspectiveValues;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.json.JSONObject;

/** Servlet that filters text using the Perspective API. */
@WebServlet("/perspective")
public final class PerspectiveServlet extends HttpServlet {

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // prepare response to return JSON and set up a GSON object
    response.setContentType("application/json;");
    Gson gson = new Gson();

    String json = request.getReader().readLine();
    
    // get the text to be analyzedfrom the JSON & handle error if it cannot be converted
    String text = null;

    try {
      JSONObject jsonObject = new JSONObject(json);
      text = jsonObject.getString("text");
    } catch (JSONException exception) {
      exception.printStackTrace();
      String errorMessage = "Could not convert text sent to server from JSON.";

      handleError(response, HttpServletResponse.SC_BAD_REQUEST, errorMessage);
      return;
    }

    // check that text is valid (not null or empty)
    if (text == null || text.equals("")) {
      handleError(response, HttpServletResponse.SC_BAD_REQUEST, "Text input was null or empty");
      return;
    }
    
    // create a PerspectiveStoryAnalysisManager
    PerspectiveStoryAnalysisManager manager;
    try {
      manager = new PerspectiveStoryAnalysisManager();
    } catch (APINotAvailableException exception) {   
      handleError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, exception.toString());   
      return;
    }

    // generate a PerspectiveDecision with that manager
    PerspectiveDecision perspectiveDecision;
    try {
      perspectiveDecision = manager.generatePerspectiveDecision(text);
    } catch (NoAppropriateStoryException e) {
      perspectiveDecision = null;
    }

    // if perspectiveDecision is null, then it means the decision is not appropriate (should be false)
    Boolean isAppropriateStory = (perspectiveDecision != null);
    PerspectiveValues values = perspectiveDecision.getValues();

    // objects to write back on the respones
    Map<String, Object> responseBody = new HashMap<String, Object>();
    responseBody.put("isAppropriate", isAppropriateStory);
    responseBody.put("attributeTypesToScores", values.getAttributeTypesToScores());

    // pass the decision and values from PerspectiveAPI to demo as JSON
    response.getWriter().println(gson.toJson(responseBody));
  }

  /**
   * Helper method to handle error (e.g. catching an exception 
   * or rejecting bad input) for a certain response by setting the status of 
   * the response to an error code and writing the message to the response.
   * 
   * @param response the response to handle the error for
   * @param errorCode the error code this particular expection should lead to
   * @param errorMessage the message to write to the response
   */
  private static void handleError(HttpServletResponse response, int errorCode, String errorMessage) {
    Gson gson = new Gson();
    String messageJson = gson.toJson(errorMessage);

    response.setStatus(errorCode);
    try {
      response.getWriter().println(messageJson);
    } catch(IOException exception) {
      exception.printStackTrace();
    }
  }
}
