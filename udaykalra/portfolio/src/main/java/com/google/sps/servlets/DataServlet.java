// Copyright 2019 Google LLC
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
import com.google.appengine.api.datastore.FetchOptions.Builder;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that returns some comments. */
@WebServlet("/data")
public class DataServlet extends HttpServlet {
  DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

  /**
   * Converts a ServerStats instance into a JSON string using the Gson library. Note: We first added
   * the Gson library dependency to pom.xml.
   */
  private String listToJson(List<String> inputList) {
    Gson gson = new Gson();
    String json = gson.toJson(inputList);
    return json;
  }

  /**
   * Obtains comments json to display upon request.
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("text/html;");
    Query query = new Query("CommentSingle").addSort("timestamp", SortDirection.DESCENDING);
    int commentLimit = getCommentCount(request);
    PreparedQuery results = datastore.prepare(query);

    ArrayList<String> comments = new ArrayList<String>();

    FetchOptions commentLimiter = FetchOptions.Builder.withLimit(commentLimit);

    for (Entity entity : results.asIterable(commentLimiter)) {
      String commentText = (String) entity.getProperty("text");
      comments.add(commentText);
    }

    String outputJson = listToJson(comments);

    response.getWriter().println(outputJson);
  }

  /**
   * Writes comment data to Entity
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Get the input from the form.
    String text = getRequestParameter(request, "text-input", "");
    long timestamp = System.currentTimeMillis();

    Entity taskEntity = new Entity("CommentSingle");
    taskEntity.setProperty("text", text);
    taskEntity.setProperty("timestamp", timestamp);

    datastore.put(taskEntity);

    // Redirect back to the HTML page.
    response.sendRedirect("/index.html");
  }

  /**
   * Gets count for comments to be shown.
   * Returns 1 if the comment limit specified is 0 or negative.
   * Returns null on error.
   */
  private Integer getCommentCount(HttpServletRequest request) {
    // Get the input from the form.
    String commentCountString = request.getParameter("comment-count");

    // Convert the input to an int.
    Integer commentCount;
    try {
      commentCount = Integer.parseInt(commentCountString);

    } catch (NumberFormatException e) {
      System.err.println("Could not convert to int: " + commentCountString);
      return null;
    }
    if (commentCount <= 0) {
      return 1;
    } else {
      return commentCount;
    }
  }

  /**
   * Obtains parameter from Comments typing field.
   */
  private String getRequestParameter(
      HttpServletRequest request, String comment, String defaultValue) {
    String value = request.getParameter(comment);

    if (value == null) {
      return defaultValue;
    }
    return value;
  }
}
