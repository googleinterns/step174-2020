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

/**
 * Prototype Servlet for serving the POST request for the analyze image resource. It uses the Vision
 * API to analyze the images.
 */
@WebServlet("/analyze-image")
public class AnalyzeImageServlet extends HttpServlet {
  /**
   * {@inheritDoc}
   *
   * Expecting a post request from Blobstore containing the data fields from the image-upload
   * form. After having gone through Blobstore, the request will include the image uploaded. The
   * form in the HTML will connect to the Blobstore URL, which encodes the image and then
   * redirects the request to this Url. In this doPost, the image is analyzed with Vision API,
   * and the analysis along with the image URL will be sent to datastore for permanent storage.
   */
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Get the input from the form.
    // Get the URL of the image that the user uploaded to Blobstore.
    final String imageUrl = getUploadedFileUrl(request, "image-upload-form");

    // Get the raw byte array representing the image from Blobstore
    final byte[] bytes = getBlobBytes(request, "image-upload-form");

    if (bytes == null || imageUrl == null) {
      // Redirect back to the HTML page.
      response.sendError(400, "Please upload a valid image.");
      return;
    } 
    
    // Gets the full label information from the image byte array by calling Images Manager.
    ImagesManager demoImagesManager = new VisionImagesManager();
    List<byte[]> imagesAsByteArrays = new ArrayList<byte[]>();
    imagesAsByteArrays.add(bytes);

    List<AnnotatedImage> annotatedImages = demoImagesManager.createAnnotatedImagesFromImagesAsByteArrays(imagesAsByteArrays);
    // There wil only be one image uploaded at a time for the demo
    AnnotatedImage demoAnnotatedImage = annotatedImages.get(0);
    Text labelsJsonArray = new Text(demoAnnotatedImage.getLabelsAsJson());

    // Add the input to datastore
    Entity analyzedImageEntity = new Entity("analyzed-image");
    analyzedImageEntity.setProperty("imageUrl", imageUrl);
    analyzedImageEntity.setProperty("labelsJsonArray", labelsJsonArray);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(analyzedImageEntity);

    // Redirect back to the HTML page.
    response.sendRedirect("vision-demo.html#image-upload");
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

  /** Returns image byte data from BlobKey of image stored with Blobstore. */
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
      byte[] blobstoreData =
          blobstoreService.fetchData(blobKey, currentByteIndex, currentByteIndex + fetchSize - 1);
      outputBytes.write(blobstoreData);

      // if we read fewer bytes than we requested, then we reached the end
      if (blobstoreData.length < fetchSize) {
        continueReading = false;
      }

      currentByteIndex += fetchSize;
    }

    return outputBytes.toByteArray();
  }
}
