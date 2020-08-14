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
import com.google.protobuf.ByteString;
import com.google.sps.images.ImagesManager;
import com.google.sps.images.VisionImagesManager;
import com.google.sps.images.data.AnnotatedImage;
import com.google.sps.perspective.PerspectiveStoryAnalysisManager;
import com.google.sps.perspective.StoryAnalysisManager;
import com.google.sps.perspective.data.APINotAvailableException;
import com.google.sps.perspective.data.NoAppropriateStoryException;
import com.google.sps.perspective.data.StoryDecision;
import com.google.sps.story.PromptManager;
import com.google.sps.story.StoryManager;
import com.google.sps.story.StoryManagerImpl;
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
 * Backend servlet which manages the analysis of images, creation of backstories, and uploading
 * the analyzed images with the backstory to permanent storage.
 */
@WebServlet("/analyze-image")
public class AnalyzeImageServlet extends HttpServlet {
  private ImagesManager imagesManager;
  private Boolean useMockStoryManager;
  private StoryManager storyManager;
  private StoryAnalysisManager storyAnalysisManager;
  private DatastoreService datastore;
  private Boolean useMockDatastoreService;
  private BlobstoreService blobstoreService;
  private Boolean useMockBlobstoreService;
  private Entity mockAnalyzedImageEntity;

  public AnalyzeImageServlet() throws IOException, APINotAvailableException {
    imagesManager = new VisionImagesManager();
    useMockStoryManager = false;
    storyAnalysisManager = new PerspectiveStoryAnalysisManager();
    useMockDatastoreService = false;
    datastore = DatastoreServiceFactory.getDatastoreService();
    useMockBlobstoreService = false;
    blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
  }

  // Injection methods for testing.

  /**
   * Sets an optional images manager.
   * @param imagesManager an ImagesManager object, real or mock.
   */
  public void setToUseMockImagesManager(ImagesManager imagesManager) {
    this.imagesManager = imagesManager;
  }

  /**
   * Sets an optional storyAnalysisManager.
   * @param storyAnalysisManager an StoryAnalysisManager object, real or mock.
   */
  public void setToUseMockStoryAnalysisManager(StoryAnalysisManager storyAnalysisManager) {
    this.storyAnalysisManager = storyAnalysisManager;
  }

  /**
   * Sets optional Datastore services and entities.
   * @param datastore a datastore service object, real or mock.
   * @param mockAnalyzedImageEntity an entity object, real or mock.
   */
  public void setToUseMockDatastoreService(
      DatastoreService datastore, Entity mockAnalyzedImageEntity) {
    useMockDatastoreService = true;
    this.datastore = datastore;
    this.mockAnalyzedImageEntity = mockAnalyzedImageEntity;
  }

  /**
   * Sets optional Blobstore services.
   * @param blobstoreService a Blobstore service object, real or mock.
   */
  public void setToUseMockBlobstoreService(BlobstoreService blobstoreService) {
    useMockBlobstoreService = true;
    this.blobstoreService = blobstoreService;
  }

  /**
   * Sets an optional StoryManager.
   * @param storyManager a StoryManager object, real or mock.
   */
  public void setToUseMockStoryManager(StoryManager storyManager) {
    useMockStoryManager = true;
    this.storyManager = storyManager;
  }

  /**
   * Helper method to set a real story manager object, if a mock is not being used.
   * @param prompt the small prompt used to generate a larger story.
   * @param storyLength number of words to be in the backstory.
   * @param temperature 0-1 paramater which determines the freedom of expression given to
   * the text generation model.
   */
  private void createStoryManager(String prompt, int storyLength, double temperature) {
    if (!useMockStoryManager) {
      storyManager = new StoryManagerImpl(prompt, storyLength, temperature);
    }
  }

  /**
   * Method which returns the max fetch size for blobstore, or returns a trivial answer of 1 when
   * using a mock blobstore service.
   * @return a trivial 1 or the BlobstoreService.MAX_BLOB_FETCH_SIZE, as required.
   */
  private int getBlobstoreServiceMaxFetch() {
    return useMockBlobstoreService ? 1 : BlobstoreService.MAX_BLOB_FETCH_SIZE;
  }

  /**
   * Method to return a real or mock Entity object to put into datastore, as required.
   * @param entityName the type of the entity to be used, if a real entity is required.
   * @return a real or mock Entity object as required.
   */
  private Entity createEntity(String entityName) {
    if (useMockDatastoreService) {
      return mockAnalyzedImageEntity;
    } else {
      Entity analyzedImageEntity = new Entity(entityName);
      return analyzedImageEntity;
    }
  }

  /**
   * {@inheritDoc}
   *
   * Expecting a post request from Blobstore containing the data fields from the image-upload
   * form. After having gone through Blobstore, the request will include the image uploaded. The
   * form in the HTML will connect to the Blobstore URL, which encodes the image and then
   * redirects the request to this Url.
   *
   * The image is analyzed with the ImagesAnalysisManager, the result of which is fed into the
   * PromptManager to create a prompt which is then used to generate the raw Backstory throug the
   * StoryManager. The raw Backstory then is checked by the StoryAnalysisManager for toxicity and,
   * if it passes, is sent to permanent storage, along with the uploaded image's blob key.
   */
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Get the input from the form: the image uploaded.
    // Get the image as a string representation of its blob key.
    final String blobKeyString = getUploadedFileBlobKeyString(request, "image-upload");
    // Get the raw byte array representing the image.
    final byte[] bytes = getBlobBytes(request, "image-upload");

    // Validate than  an image was uploaded
    if (bytes == null || blobKeyString == null) {
      // Redirect back to the HTML page.
      response.sendError(400, "Please upload a valid image.");

    } else {
      // Gets the full label information from the image byte array by calling Images Manager.
      List<byte[]> imagesAsByteArrays = new ArrayList<>();
      imagesAsByteArrays.add(bytes);
      List<AnnotatedImage> annotatedImages =
          imagesManager.createAnnotatedImagesFromImagesAsByteArrays(imagesAsByteArrays);
      // There wil only be one image uploaded at a time for the demo
      AnnotatedImage annotatedImage = annotatedImages.get(0);
      List<String> descriptions = annotatedImage.getLabelDescriptions();

      PromptManager promptManager = new PromptManager(descriptions);
      // The delimiter for the MVP will be tentatively be "and"
      String prompt = promptManager.generatePrompt(" and ");

      // Tentative story parameterizations for the MVP
      createStoryManager(prompt, 200, .7);

      String rawBackstory = "";
      Boolean generateTextFailed = true;
      while (generateTextFailed) {
        try {
          rawBackstory = storyManager.generateText();
          generateTextFailed = false;
        } catch (RuntimeException exception) {
          System.err.println(exception);
          generateTextFailed = true;
        }
      }

      // Filtration Check
      Text backstory = new Text("");
      try {
        StoryDecision storyDecision = storyAnalysisManager.generateDecision(rawBackstory);
        backstory = new Text(storyDecision.getStory());
      } catch (NoAppropriateStoryException exception) {
        response.sendError(400, "Please upload a valid image.");
      }

      // Get metadata about the backstory
      final long timestamp = System.currentTimeMillis();

      // Add the input to datastore
      Entity analyzedImageEntity = createEntity("analyzed-image");
      analyzedImageEntity.setProperty("blobKeyString", blobKeyString);
      analyzedImageEntity.setProperty("backstory", backstory);
      analyzedImageEntity.setProperty("timestamp", timestamp);

      datastore.put(analyzedImageEntity);

      // Redirect back to the HTML page.
      response.sendRedirect("/index.html");
    }
  }

  /**
   * Given a request and the form input element name get the image uploaded in the form as
   * a Blob Key in String form. This key is used to serve the picture back to the front-end.
   * @param request the HTTP request sent from the front-end form.
   * @param formInputElementName the name of the input element in the front-end form.
   * @return the image uploaded in the front-end input form, as a Blob Key in String form.
   */
  private String getUploadedFileBlobKeyString(
      HttpServletRequest request, String formInputElementName) {
    Map<String, List<BlobKey>> blobs = blobstoreService.getUploads(request);
    List<BlobKey> blobKeys = blobs.get(formInputElementName);

    // User submitted form without selecting a file, so we can't get a URL. (dev server)
    if (blobKeys == null || blobKeys.isEmpty()) {
      return null;
    }

    // Our form only contains a single file input, so get the first index.
    BlobKey blobKey = blobKeys.get(0);

    // User submitted form without selecting a file, so we can't get a URL. (live server)
    if (!useMockBlobstoreService) {
      BlobInfo blobInfo = new BlobInfoFactory().loadBlobInfo(blobKey);
      if (blobInfo.getSize() == 0) {
        blobstoreService.delete(blobKey);
        return null;
      }
    }

    String blobKeyString = blobKey.getKeyString();
    return blobKeyString;
  }

  /**
   * Given a request and the form input element name get the image uploaded in the form as
   * a byte array.
   * @param request the HTTP request sent from the front-end form.
   * @param formInputElementName the name of the input element in the front-end form.
   * @return the image uploaded in the front-end input form, as a byte array.
   */
  private byte[] getBlobBytes(HttpServletRequest request, String formInputElementName)
      throws IOException {
    Map<String, List<BlobKey>> blobs = blobstoreService.getUploads(request);
    List<BlobKey> blobKeys = blobs.get(formInputElementName);

    // User submitted form without selecting a file, so we can't get a URL. (dev server)
    if (blobKeys == null || blobKeys.isEmpty()) {
      return null;
    }

    // Our form only contains a single file input, so get the first index.
    BlobKey blobKey = blobKeys.get(0);

    // User submitted form without selecting a file, so we can't get a URL. (live server)
    if (!useMockBlobstoreService) {
      BlobInfo blobInfo = new BlobInfoFactory().loadBlobInfo(blobKey);
      if (blobInfo.getSize() == 0) {
        blobstoreService.delete(blobKey);
        return null;
      }
    }

    ByteArrayOutputStream outputBytes = new ByteArrayOutputStream();

    int fetchSize = getBlobstoreServiceMaxFetch();
    long currentByteIndex = 0;
    boolean continueReading = true;
    while (continueReading) {
      // end index is inclusive, so we have to subtract 1 to get fetchSize bytes
      byte[] bytesFromImage =
          blobstoreService.fetchData(blobKey, currentByteIndex, currentByteIndex + fetchSize - 1);
      outputBytes.write(bytesFromImage);

      // if we read fewer bytes than we requested, then we reached the end
      if (bytesFromImage.length < fetchSize) {
        continueReading = false;
      }

      currentByteIndex += fetchSize;
    }

    return outputBytes.toByteArray();
  }
}
