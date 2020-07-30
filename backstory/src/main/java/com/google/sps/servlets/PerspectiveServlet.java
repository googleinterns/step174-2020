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
import com.google.sps.data.PerspectiveAnalysis;
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

/** Servlet that filters text using the Perspective API. */
@WebServlet("/perspective")
public final class PerspectiveServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // prepare response to return JSON and set up a GSON object
    response.setContentType("application/json;");
    Gson gson = new Gson();
    
    String apiKey = "foo";

    try {
      // fetch the PerspectiveAPIKey class if it's there
      Class<?> keyClass = Class.forName("com.google.sps.data.PerspectiveAPIKey");
      // create a method getKey that takes no parameters
      Method getKey = keyClass.getMethod("getKey", null);
      // invoke this static method (first null means it's static 
      // & second null means it does not need arguments) & stores result
      apiKey = (String) getKey.invoke(null, null);
    } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
      // if any errors were thrown for looking for api key, send this error message back to JS servlet
      String errorMessage = "Could not retrieve the Perspective API.";
      String messageJson = gson.toJson(errorMessage);
      response.getWriter().println(messageJson);
      return;
    }

    PerspectiveAPI perspectiveAPI = PerspectiveAPI.create(apiKey);

    String text = getParameter(request, "text", "");

    PerspectiveAnalysis textAnalysis = new PerspectiveAnalysis(perspectiveAPI, text);

    // write textAnalysis as JSON to response
    String json = gson.toJson(textAnalysis);

    response.getWriter().println(json);
  }

  /**
   * @return the request parameter, or the default value if the parameter
   *         was not specified by the client
   */
  private String getParameter(HttpServletRequest request, String name, String defaultValue) {
    String value = request.getParameter(name);
    if (value == null) {
      return defaultValue;
    }
    return value;
  }
}
