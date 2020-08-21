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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.gson.Gson;
import com.google.sps.data.PostBatch;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that manages the comments page */
@WebServlet("/data")
public class DataServlet extends HttpServlet {
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Read the query string to get comment limit
    int commentLimit = Integer.parseInt(request.getParameter("num-comments"));

    // Query to find all comment entities sorted from newest to oldest
    Query query = new Query("Comment").addSort("timestamp", SortDirection.ASCENDING);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    // adding all comment entities text to a PostBatch instance
    final PostBatch comments = new PostBatch(new ArrayList<String>());
    for (Entity entity : results.asIterable(FetchOptions.Builder.withLimit(commentLimit))) {
      String commentText = (String) entity.getProperty("comment-text");
      comments.addPost(commentText);
    }

    response.setContentType("application/json;");
    Gson gson = new Gson();
    String jsonComments = gson.toJson(comments.getPostArray());
    response.getWriter().println("{\"comments\":" + jsonComments + "}");
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Get the input from the form.
    final String comment = request.getParameter("comment");
    final long timestamp = System.currentTimeMillis();

    // Add the input to datastore
    Entity commentEntity = new Entity("Comment");
    commentEntity.setProperty("comment-text", comment);
    commentEntity.setProperty("timestamp", timestamp);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(commentEntity);

    // Redirect back to the HTML page.
    response.sendRedirect("/comments/dataPage.html");
  }
}
