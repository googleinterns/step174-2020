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
import com.google.sps.story.StoryManager;
import com.google.sps.story.StoryManagerImpl;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.json.JSONObject;

/** Servlet that returns a generated story. */
@WebServlet("/gpt2")
public final class GPT2Servlet extends HttpServlet {
  public static final int DEFAULT_MAX_STORY_LENGTH = 200;
  public static final Double DEFAULT_TEMPERATURE = 0.7;

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json;");
    Gson gson = new Gson();
    String text = "";
    String json = request.getReader().readLine();

    // get the text from the JSON & handle error if it cannot be converted
    try {
      JSONObject jsonObject = new JSONObject(json);
      text = jsonObject.getString("text");
    } catch (JSONException exception) {
      String errorMessage = "Could not convert text sent to server from JSON.";
      System.out.println(text);

      handleError(response, HttpServletResponse.SC_BAD_REQUEST, errorMessage);
      return;
    }

    // Check that text is valid
    if (text == null || text.equals("")) {
      handleError(response, HttpServletResponse.SC_BAD_REQUEST, "Text input was null or empty");
      return;
    }
    String generatedText;
    try {
      StoryManager storyManager =
          new StoryManagerImpl(text, DEFAULT_MAX_STORY_LENGTH, DEFAULT_TEMPERATURE);
      generatedText = storyManager.generateText();
    } catch (Exception exception) {
      System.out.println(exception);
      // Displays if internal server error.
      handleError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server Error");
      return;
    }

    // Return Generated Text as JSON
    response.getWriter().println(generatedText);
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
  private static void handleError(
      HttpServletResponse response, int errorCode, String errorMessage) {
    Gson gson = new Gson();
    String messageJson = gson.toJson(errorMessage);

    response.setStatus(errorCode);
    try {
      response.getWriter().println(messageJson);
    } catch (IOException exception) {
      exception.printStackTrace();
    }
  }
}
