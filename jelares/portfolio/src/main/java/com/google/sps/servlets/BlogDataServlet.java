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

import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobInfoFactory;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.ServingUrlOptions;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import com.google.sps.servletData.BlogPost;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet for managing blog data */
@WebServlet("/blog-data")
public class BlogDataServlet extends HttpServlet {
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Read the query string to get post limit
    int postLimit = Integer.parseInt(request.getParameter("num-posts"));

    // Query to find all post entities sorted from newest to oldest
    Query query = new Query("blog-post").addSort("timestamp", SortDirection.ASCENDING);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    List<BlogPost> blogPosts = new ArrayList<>();
    for (Entity entity : results.asIterable(FetchOptions.Builder.withLimit(postLimit))) {
      String imageUrl = (String) entity.getProperty("blog-post-image");
      String title = (String) entity.getProperty("blog-post-title");
      String content = (String) entity.getProperty("blog-post-content");

      blogPosts.add(new BlogPost(imageUrl, title, content));
    }

    response.setContentType("application/json;");
    String jsonBlogPosts = blogToJson(blogPosts);
    response.getWriter().println(jsonBlogPosts);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Expecting a post request from Blobstore containing the data fields from the blog-post form.
    // After having gone through Blobstore, the request will include a blog post title, content, and image.
    // The form in the HTML will connect to the Blobstore URL, which processes the image and then redirects the
    // request to this Url.

    // Get authentication object
    UserService userService = UserServiceFactory.getUserService();
    if (!userService.isUserLoggedIn()) {
      String loginUrl = userService.createLoginURL("/index.html#blog-box");
      response.sendRedirect(loginUrl);
    } else {
      // Get the input from the form.
      final String blogPostTitle = request.getParameter("blog-post-title");
      final String blogPostContent = request.getParameter("blog-post-content");
      final long timestamp = System.currentTimeMillis();

      // Get the URL of the image that the user uploaded to Blobstore.
      final String blogPostImageUrl = getUploadedFileUrl(request, "blog-post-image");

      // Add the input to datastore
      Entity blogPostEntity = new Entity("blog-post");
      blogPostEntity.setProperty("blog-post-title", blogPostTitle);
      blogPostEntity.setProperty("blog-post-content", blogPostContent);
      blogPostEntity.setProperty("blog-post-image", blogPostImageUrl);
      blogPostEntity.setProperty("timestamp", timestamp);

      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      datastore.put(blogPostEntity);

      // Redirect back to the HTML page.
      response.sendRedirect("/index.html#blog-box");
    }
  }

  /** Returns a URL that points to the uploaded file, or null if the user didn't upload a file. */
  private String getUploadedFileUrl(HttpServletRequest request, String formInputElementName) {
    BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
    Map<String, List<BlobKey>> blobs = blobstoreService.getUploads(request);
    List<BlobKey> blobKeys = blobs.get(formInputElementName);

    // User submitted form without selecting a file, so we can't get a URL. (dev server)
    if (blobKeys == null || blobKeys.isEmpty()) {
      return null;
    }

    // Our form only contains a single file input, so get the first index.
    BlobKey blobKey = blobKeys.get(0);

    // User submitted form without selecting a file, so we can't get a URL. (live server)
    BlobInfo blobInfo = new BlobInfoFactory().loadBlobInfo(blobKey);
    if (blobInfo.getSize() == 0) {
      blobstoreService.delete(blobKey);
      return null;
    }

    // Use ImagesService to get a URL that points to the uploaded file.
    ImagesService imagesService = ImagesServiceFactory.getImagesService();
    ServingUrlOptions options = ServingUrlOptions.Builder.withBlobKey(blobKey);

    // To support running in Google Cloud Shell with AppEngine's devserver, we must use the relative
    // path to the image, rather than the path returned by imagesService which contains a host.
    try {
      URL url = new URL(imagesService.getServingUrl(options));
      return url.getPath();
    } catch (MalformedURLException e) {
      return imagesService.getServingUrl(options);
    }
  }

  /** Turns blog posts to json */
  private String blogToJson(List<BlogPost> blogPosts) {
    Gson gson = new Gson();
    Map<String, List<BlogPost>> map = new HashMap<>();
    map.put("blog-posts", blogPosts);

    return gson.toJson(map);
  }
}
