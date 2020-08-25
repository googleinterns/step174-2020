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
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    if (!userService.isUserLoggedIn()) {
      String urlToRedirectToAfterUserLogsIn = "/analyzed-images";
      String loginUrl = userService.createLoginURL(urlToRedirectToAfterUserLogsIn);
      response.sendRedirect(loginUrl);

    } else {
      // Get user identification
      String userEmail = userService.getCurrentUser().getEmail();

      BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();

      // Query to find all analyzed image entities. We will filter to only return the current
      // user's backstories.
      Filter onlyShowUserBackstories =
          new FilterPredicate("userEmail", FilterOperator.EQUAL, userEmail);

      Query query = new Query("analyzed-image")
                        .setFilter(onlyShowUserBackstories)
                        .addSort("timestamp", SortDirection.DESCENDING);
      // Will limit the Query (which is sorted from newest to oldest) to only return the first
      // result, Thus displaying the most recent story uploaded.
      int onlyShowMostRecentStoryUploaded = 1;

      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      PreparedQuery results = datastore.prepare(query);

      BlobKey blobKey = null;
      for (Entity entity :
          results.asIterable(FetchOptions.Builder.withLimit(onlyShowMostRecentStoryUploaded))) {
        blobKey = new BlobKey((String) entity.getProperty("blobKeyString"));
      }

      // Validation to make sure that empty images are not getting uploaded to permanent storage.
      if (blobKey == null) {
        throw new NullPointerException(
            "No image(s) were uploaded, this servlet should not have been called.");
      }

      blobstoreService.serve(blobKey, response);
    }
  }
}
