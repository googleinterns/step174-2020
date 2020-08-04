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

    String[] cannedOutput = new String[0];

    // write canned output to servlet
    String json = gson.toJson(cannedOutput);

    response.getWriter().println(json);
  }
}
