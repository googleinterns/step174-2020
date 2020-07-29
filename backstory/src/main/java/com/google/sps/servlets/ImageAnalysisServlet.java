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
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.ServingUrlOptions;
import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.EntityAnnotation;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.cloud.vision.v1.ImageSource;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.protobuf.ByteString;
import com.google.sps.servletData.AnalyzedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet for managing image analysis with Vision API and blobstore for uploads */
@WebServlet("/image-analysis")
public class ImageAnalysisServlet extends HttpServlet {
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

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Expecting a post request from Blobstore containing the data fields from the image-upload
    // form. After having gone through Blobstore, the request will include the images uploaded. The
    // form in the HTML will connect to the Blobstore URL, which encodes the images and then
    // redirects the request to this Url. In this doPost, the images are analyzed with Vision API,
    // and the analysis along with the image URL will be sent to datastore for permanent storage.

    // Get the input from the form.
    // Get the URL of the image that the user uploaded to Blobstore.
    final String imageUrl = getUploadedFileUrl(request, "image-upload");

    // Get the raw byte array representing the image from Blobstore
    final byte[] bytes = getBlobBytes(request, "image-upload");

    // Gets the full label information from the image byte array by calling Vision API.
    List<EntityAnnotation> labels = detectLabelsBytes(bytes);
    Gson gson = new Gson();
    Text labelsJsonArray = new Text(gson.toJson(labels));

    // Add the input to datastore
    Entity analyzedImageEntity = new Entity("analyzed-image");
    analyzedImageEntity.setProperty("imageUrl", imageUrl);
    analyzedImageEntity.setProperty("labelsJsonArray", labelsJsonArray);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(analyzedImageEntity);

    // Redirect back to the HTML page.
    response.sendRedirect("/visionUploadPrototype/visionDemo.html#image-upload");
  }

  // Detects labels in the image specified by the image byte data by calling the Vision API.
  private List<EntityAnnotation> detectLabelsBytes(byte[] bytes) throws IOException {
    List<AnnotateImageRequest> requests = new ArrayList<>();
    List<EntityAnnotation> labels;

    Image img = Image.newBuilder().setContent(ByteString.copyFrom(bytes)).build();
    Feature feat = Feature.newBuilder().setType(Feature.Type.LABEL_DETECTION).build();
    AnnotateImageRequest request =
        AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();
    requests.add(request);

    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of the requests the
    // client will be automatically closed, as it is called within the try.
    try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
      BatchAnnotateImagesResponse response = client.batchAnnotateImages(requests);
      List<AnnotateImageResponse> responses = response.getResponsesList();

      // There is only one image in the batch response (only supports uploading one image at a time
      // right now)
      AnnotateImageResponse res = responses.get(0);

      if (res.hasError()) {
        System.out.format("Error: %s%n", res.getError().getMessage());
        return new ArrayList<EntityAnnotation>();
      }

      // For full list of available annotations, see http://g.co/cloud/vision/docs
      labels = res.getLabelAnnotationsList();
    }

    return labels;
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

  /* Returns image byte data from BlobKey of image stored with Blobstore. */
  private byte[] getBlobBytes(HttpServletRequest request, String formInputElementName)
      throws IOException {
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

    ByteArrayOutputStream outputBytes = new ByteArrayOutputStream();

    int fetchSize = BlobstoreService.MAX_BLOB_FETCH_SIZE;
    long currentByteIndex = 0;
    boolean continueReading = true;
    while (continueReading) {
      // end index is inclusive, so we have to subtract 1 to get fetchSize bytes
      byte[] b =
          blobstoreService.fetchData(blobKey, currentByteIndex, currentByteIndex + fetchSize - 1);
      outputBytes.write(b);

      // if we read fewer bytes than we requested, then we reached the end
      if (b.length < fetchSize) {
        continueReading = false;
      }

      currentByteIndex += fetchSize;
    }

    return outputBytes.toByteArray();
  }
}
