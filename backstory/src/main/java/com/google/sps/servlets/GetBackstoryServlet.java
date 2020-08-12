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
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.sps.servlets.data.Backstory;

/**
 *
 */
@WebServlet("/backstory")
public class GetBackstoryServlet extends HttpServlet {
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Query to find all analyzed image entities.
    Query query = new Query("analyzed-image").addSort("timestamp", SortDirection.DESCENDING);
    // Will limit the Query (which is sorted from newest to oldest) to only return the first result,
    // Thus displaying the most recent story uploaded.
    int onlyShowMostRecentStoryUploaded = 1;

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    List<Backstory> backstories = new ArrayList<>();
    for (Entity entity : results.asIterable(FetchOptions.Builder.withLimit(onlyShowMostRecentStoryUploaded))) {
      Backstory backstory = new Backstory(
          (String) ((Text) entity.getProperty("backstory")).getValue());
      backstories.add(backstory);
    }

    response.setContentType("application/json;");
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    String backstoriesJsonArray = gson.toJson(backstories);
    System.out.println(backstoriesJsonArray);
    response.getWriter().println(backstoriesJsonArray);
  }
}
