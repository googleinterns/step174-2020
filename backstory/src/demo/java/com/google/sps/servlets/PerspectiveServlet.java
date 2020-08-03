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
import com.google.sps.data.perspective.PerspectiveAnalysis;
import com.google.sps.data.perspective.PerspectiveServiceClient;
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
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // prepare response to return JSON and set up a GSON object
    response.setContentType("application/json;");
    Gson gson = new Gson();
    
    // declare instance of perspective api
    PerspectiveAPI perspectiveAPI;
    
    try {
      perspectiveAPI = PerspectiveServiceClient.generateAPI();
    } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {      
      // if any errors were thrown for looking for api key, send this error message back to JS servlet
      String errorMessage = "Could not retrieve the Perspective API key.";
      
      // set the status of this request to an internal service error
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

      // send the appropriate error message back with the status
      String messageJson = gson.toJson(errorMessage);
      response.getWriter().println(messageJson);
    }

    // get the text and if it's null or empty return a service error & error message
    String text = request.getParameter("text");
    
    if(text === null || text === "") {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

      String errorMessage = "Text input was null or empty";
      String messageJson = gson.toJson(errorMessage);

      response.getWriter().println(messageJson);
      return;
    }

    PerspectiveAnalysis textAnalysis = PerspectiveServiceClient.analyze(perspectiveAPI, PerspectiveAnalysis.ANALYSIS_TYPES, text);

    // write textAnalysis as JSON to response
    String json = gson.toJson(textAnalysis);

    response.getWriter().println(json);
  }
}
