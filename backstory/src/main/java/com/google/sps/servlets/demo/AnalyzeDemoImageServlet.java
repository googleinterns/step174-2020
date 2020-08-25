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
import com.google.sps.servlets.data.AnalyzedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobInfoFactory;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.ServingUrlOptions;
import com.google.protobuf.ByteString;
import com.google.sps.images.data.AnnotatedImage;
import com.google.sps.images.ImagesManager;
import com.google.sps.images.VisionImagesManager;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.sps.servlets.data.BlobstoreManager;

/**
 * Prototype Servlet for analyzing uploaded images, and serving back the analysis.
 */
@WebServlet("/analyze-demo-image")
public class VisionServlet extends HttpServlet {
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Check to see if the user is currently logged in
    UserService userService = backstoryUserServiceFactory.newInstance();
    if (!userService.isUserLoggedIn()) {
      String urlToRedirectToAfterUserLogsIn = "/analyze-demo-image";
      String loginUrl = userService.createLoginURL(urlToRedirectToAfterUserLogsIn);
      response.sendRedirect(loginUrl);
      return;
    }
    // Get user identification to store alongside their backstory and image
    String userEmail = userService.getCurrentUser().getEmail();

    // The blobKeyString of the image will be used to serve the image back to the front-end.
    BlobstoreManager blobstoreManager = new BlobstoreManager();
    final String blobKeyString =
        blobstoreManager.getUploadedFileBlobKeyString(request, "image-upload");
    // The raw byte array representing the image will be used for image analytics.
    final byte[] bytes = blobstoreManager.getBlobBytes(request, "image-upload");

    // Validate that an image was actually uploaded.
    if (bytes == null || blobKeyString == null) {
      // Redirect back to the HTML page.
      response.sendError(400, "Please upload a valid image.");
      return;
    }

    // Generate a list of AnnotatedImages, with each annotatedImage consisting of an image with
    // labels.
    ImagesManager imagesManager = imagesManagerFactory.newInstance();
    List<byte[]> imagesAsByteArrays = new ArrayList<>();
    imagesAsByteArrays.add(bytes);
    List<AnnotatedImage> annotatedImages =
        imagesManager.createAnnotatedImagesFromImagesAsByteArrays(imagesAsByteArrays);
    // Currently, Backstory only supports single image uploads.
    // which is why we only get the first annotatedImage element here from annotatedImages.
    AnnotatedImage annotatedImage = annotatedImages.get(0);
    List<String> descriptions = annotatedImage.getLabelDescriptions();

    // Get metadata about the backstory
    final long timestamp = System.currentTimeMillis();

    // Add the input to datastore
    Entity analyzedImageEntity = entityFactory.newInstance("analyzed-demo-image");
    analyzedImageEntity.setProperty("userEmail", userEmail);
    analyzedImageEntity.setProperty("blobKeyString", blobKeyString);
    analyzedImageEntity.setProperty("descriptions", descriptions);
    analyzedImageEntity.setProperty("timestamp", timestamp);

    DatastoreService datastoreService = backstoryDatastoreServiceFactory.newInstance();
    datastoreService.put(analyzedImageEntity);

    // Redirect back to the HTML page.
    response.sendRedirect("/vision-demo.html");
    }
  }
}
