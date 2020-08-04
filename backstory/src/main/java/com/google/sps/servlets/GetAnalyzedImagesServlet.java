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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.sps.data.AnalyzedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Prototype Servlet for serving the GET request for the Vision-analyzed images resource.
 */
@WebServlet("/analyzed-images")
public class GetAnalyzedImagesServlet extends HttpServlet {
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Query to find all analyzed image entities.
    Query query = new Query("analyzed-image");

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    List<AnalyzedImage> analyzedImages = new ArrayList<>();
    for (Entity entity : results.asIterable()) {
      String imageUrl = (String) entity.getProperty("imageUrl");
      String labelsJsonArray = (String) ((Text) entity.getProperty("labelsJsonArray")).getValue();

      analyzedImages.add(new AnalyzedImage(imageUrl, labelsJsonArray));
    }

    response.setContentType("application/json;");
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    String analyzedImagesJsonArray = gson.toJson(analyzedImages);
    response.getWriter().println(analyzedImagesJsonArray);
  }
}
