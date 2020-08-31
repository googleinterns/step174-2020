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

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
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
import com.google.sps.servlets.data.BackstoryDatastoreServiceFactory;
import com.google.sps.servlets.data.BackstoryUserServiceFactory;
import com.google.sps.servlets.data.BlobstoreManager;
import com.google.sps.servlets.data.BlobstoreManagerFactory;
import com.google.sps.servlets.data.QueryFactory;
import java.io.IOException;
import java.lang.NullPointerException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet which gets the Analyzed Image resource. The image is returned back to
 * the front-end by accessing the blob key from permanent storage, and then
 * using blobstore's serve functionality. Blobstore's serve functionality only
 * supports one image per request.
 */
@WebServlet("/analyzed-images")
public class GetAnalyzedImagesServlet extends HttpServlet {
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
   * Creates the BlobstoreManager instance, which manages Backstory's BLOB (binary large object)
   * upload functionality.
   */
  private BlobstoreManagerFactory blobstoreManagerFactory;

  /**
   * Initializes the servlet with online versions of the userService and datastoreService factories.
   */
  public GetAnalyzedImagesServlet() {
    backstoryUserServiceFactory = () -> {
      return UserServiceFactory.getUserService();
    };
    backstoryDatastoreServiceFactory = () -> {
      return DatastoreServiceFactory.getDatastoreService();
    };
    queryFactory = (String queryName) -> {
      return new Query(queryName);
    };
    blobstoreManagerFactory = () -> {
      return new BlobstoreManager();
    };
  }

  /**
   * Sets the BlobstoreManagerFactory.
   *
   * @param blobstoreManagerFactory a BlobstoreManagerFactory object set to return a new
   *     BlobstoreManager.
   */
  public void setBlobstoreManagerFactory(BlobstoreManagerFactory blobstoreManagerFactory) {
    this.blobstoreManagerFactory = blobstoreManagerFactory;
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
      String urlToRedirectToAfterUserLogsIn = "/analyzed-images";
      String loginUrl = userService.createLoginURL(urlToRedirectToAfterUserLogsIn);
      response.sendRedirect(loginUrl);
      return;
    }

    // Get user identification
    String userEmail = userService.getCurrentUser().getEmail();

    BlobstoreManager blobstoreManager = blobstoreManagerFactory.newInstance();

    // Query to find all analyzed image entities. We will filter to only return the current
    // user's backstories.
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

    BlobKey blobKey = null;
    for (Entity entity : results.asIterable(FetchOptions.Builder.withLimit(backstoryFetchLimit))) {
      blobKey = new BlobKey((String) entity.getProperty("blobKeyString"));
    }

    // Validation to make sure that empty images are not getting uploaded to permanent storage.
    if (blobKey == null) {
      throw new NullPointerException(
          "No image(s) were uploaded, this servlet should not have been called.");
    }

    blobstoreManager.serve(blobKey, response);
  }
}
