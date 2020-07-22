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
import com.google.sps.data.PerspectiveText;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that filters text using the Perspective API. */
@WebServlet("/perspective-filter")
public final class PerspectiveFilterServlet extends HttpServlet {

  /** the API key for the Perspective API */
  private final String API_KEY = "AIzaSyBGanMblCA8ZRtZj757eppSbVH0V9vCxgI";

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    PerspectiveAPI perspectiveAPI = PerspectiveAPI.create(API_KEY);
    String text = getParameter(request, "text", "");

    PerspectiveText textAnalysis = new PerspectiveText(perspectiveAPI, text);

    response.setContentType("application/html;");
    response.getWriter().println("<p>" + textAnalysis.getToxicity() +"% Toxicity</p>");
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
