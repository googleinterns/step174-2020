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
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.sps.servlets.data.Backstory;
import com.google.sps.servlets.data.BackstoryDatastoreServiceFactory;
import com.google.sps.servlets.data.BackstoryUserServiceFactory;
import com.google.sps.servlets.data.QueryFactory;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet which gets the Backstory resource. Currently, only the most recently uploaded Backstory
 * will be gathered from permanent storage then sent back to the front-end by writing it to the
 * response.
 */
@WebServlet("/backstory")
public class GetBackstoryServlet extends HttpServlet {
  /** Creates the UserService instance, which includes authentication functionality. */
  private BackstoryUserServiceFactory backstoryUserServiceFactory;
  /** Creates the DatastoreService instance, which includes permanent storage functionality. */
  private BackstoryDatastoreServiceFactory backstoryDatastoreServiceFactory;
  /**
   * Creates the Query instance, which performs a network call to datastore to return all
   * entities of a given type/name.
   */
  private QueryFactory queryFactory;

  /**
   * Initializes the servlet with online versions of the userService and datastoreService factories.
   */
  public GetBackstoryServlet() {
    backstoryUserServiceFactory = () -> {
      return UserServiceFactory.getUserService();
    };
    backstoryDatastoreServiceFactory = () -> {
      return DatastoreServiceFactory.getDatastoreService();
    };
    queryFactory = (String queryName) -> {
      return new Query(queryName);
    };
  }

  /**
   * Sets the BackstoryUserServiceFactory.
   *
   * @param backstoryUserServiceFactory a BackstoryUserServiceFactory object set to return a new
   *     UserService.
   */
  public void setBackstoryUserServiceFactory(
      BackstoryUserServiceFactory backstoryUserServiceFactory) {
    this.backstoryUserServiceFactory = backstoryUserServiceFactory;
  }

  /**
   * Sets the BackstoryDatastoreServiceFactory.
   *
   * @param backstoryDatastoreServiceFactory a BackstoryDatastoreServiceFactory object set to return
   *     a new DatastoreService.
   */
  public void setBackstoryDatastoreServiceFactory(
      BackstoryDatastoreServiceFactory backstoryDatastoreServiceFactory) {
    this.backstoryDatastoreServiceFactory = backstoryDatastoreServiceFactory;
  }

  /**
   * Sets the QueryFactory.
   *
   * @param queryFactory a BackstoryDatastoreServiceFactory object set to return
   *     a new DatastoreService.
   */
  public void setQueryFactory(QueryFactory queryFactory) {
    this.queryFactory = queryFactory;
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = backstoryUserServiceFactory.newInstance();
    if (!userService.isUserLoggedIn()) {
      String urlToRedirectToAfterUserLogsIn = "/backstory";
      String loginUrl = userService.createLoginURL(urlToRedirectToAfterUserLogsIn);
      response.sendRedirect(loginUrl);
      return;
    }

    // Get user identification
    String userEmail = userService.getCurrentUser().getEmail();

    // Query to find all analyzed image entities. We will filter to only return the current
    // user's backstories.
    // TODO: figure out what to do in the case that user emails are recycled.
    Filter userBackstoriesFilter =
        new FilterPredicate("userEmail", FilterOperator.EQUAL, userEmail);
    Query query = queryFactory.newInstance("analyzed-image")
                      .setFilter(userBackstoriesFilter)
                      .addSort("timestamp", SortDirection.DESCENDING);
    // Will limit the Query (which is sorted from newest to oldest) to only return the first
    // result, Thus displaying the most recent story uploaded.
    int backstoryFetchLimit = 1;

    DatastoreService datastoreService = backstoryDatastoreServiceFactory.newInstance();
    PreparedQuery results = datastoreService.prepare(query);
    List<Backstory> backstories = new ArrayList<>();
    for (Entity entity : results.asIterable(FetchOptions.Builder.withLimit(backstoryFetchLimit))) {
      Backstory backstory =
          new Backstory((String) ((Text) entity.getProperty("backstory")).getValue());
      backstories.add(backstory);
    }

    response.setContentType("application/json;");
    Gson gson = new Gson();
    String backstoriesJsonArray = gson.toJson(backstories);
    response.getWriter().println(backstoriesJsonArray);
  }
}
