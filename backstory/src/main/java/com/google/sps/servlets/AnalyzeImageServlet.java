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
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.protobuf.ByteString;
import com.google.sps.images.ImagesManager;
import com.google.sps.images.VisionImagesManager;
import com.google.sps.images.data.AnnotatedImage;
import com.google.sps.perspective.PerspectiveStoryAnalysisManager;
import com.google.sps.perspective.StoryAnalysisManager;
import com.google.sps.perspective.data.APINotAvailableException;
import com.google.sps.perspective.data.NoAppropriateStoryException;
import com.google.sps.perspective.data.StoryDecision;
import com.google.sps.servlets.data.BackstoryDatastoreServiceFactory;
import com.google.sps.servlets.data.BackstoryUserServiceFactory;
import com.google.sps.servlets.data.BlobstoreManager;
import com.google.sps.servlets.data.BlobstoreManagerFactory;
import com.google.sps.servlets.data.EntityFactory;
import com.google.sps.servlets.data.ImagesManagerFactory;
import com.google.sps.servlets.data.StoryAnalysisManagerFactory;
import com.google.sps.servlets.data.StoryManagerFactory;
import com.google.sps.story.PromptManager;
import com.google.sps.story.StoryManager;
import com.google.sps.story.StoryManagerImpl;
import com.google.sps.story.data.StoryEndingTools;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Backend servlet which manages the analysis of images, creation of stories, filtrations of stories, and uploading
 * the analyzed images along with its story to permanent storage.
 */
@WebServlet("/analyze-image")
public class AnalyzeImageServlet extends HttpServlet {
  /** Creates the UserService instance, which includes authentication functionality. */
  private BackstoryUserServiceFactory backstoryUserServiceFactory;
  /** Creates the BlobstoreManager instance, which manages Backstory's BLOB (binary large object) upload functionality. */
  private BlobstoreManagerFactory blobstoreManagerFactory;
  /** Creates the DatastoreService instance, which includes permanent storage functionality. */
  private BackstoryDatastoreServiceFactory backstoryDatastoreServiceFactory;
  /** Creates the ImagesManager instance, which manages Backstory's images and their analytics. */
  private ImagesManagerFactory imagesManagerFactory;
  /** Creates the StoryManager instance, which manges Backstory's story generation network call. */
  private StoryManagerFactory storyManagerFactory;
  /** Creates the StoryAnalysisManager, which manages Backstory's story analysis and filtration. */
  private StoryAnalysisManagerFactory storyAnalysisManagerFactory;
  /** Creates the Entity instance which will be uploaded to permanent storage; analogous to a row in a table. */
  private EntityFactory entityFactory;
  /** 
   * Delimiter which will be placed between the labels for prompt creation. 
   *  ex: "<label1> and <label2> and <label3>" 
   */
  private final String DELIMITER = " and ";
  /** World length parameter for the story to be generated */
  private final int STORY_WORD_LENGTH = 200;
  /** Temperature parameter for the story to be generated; indicates the coherence of the story */
  private final double TEMPERATURE = .7;
  /** Determines the nubmer of times GPT2 will be called to attempt to generate text */
  private final int MAX_GENERATION_ATTEMPS = 3;

  /**
   * Constructor which sets the manager factories to return their online implementations
   * (such that each manager is connected to the network).
   *
   * @return an instance of the backstory backend, capable of handling a request containing an image for Backstory creation.
   * @throws IOException if an error occurs when reading the uploaded BLOB.
   * @throws APINotAvailableException if an error occurs when connecting to the story analysis API.
   */
  public AnalyzeImageServlet() throws IOException, APINotAvailableException {
    backstoryUserServiceFactory = () -> {
      return UserServiceFactory.getUserService();
    };
    backstoryDatastoreServiceFactory = () -> {
      return DatastoreServiceFactory.getDatastoreService();
    };
    blobstoreManagerFactory = () -> {
      return new BlobstoreManager();
    };
    imagesManagerFactory = () -> {
      return new VisionImagesManager();
    };
    storyManagerFactory = (String prompt, int storyLength, double temperature) -> {
      return new StoryManagerImpl(prompt, storyLength, temperature);
    };
    storyAnalysisManagerFactory = () -> {
      return new PerspectiveStoryAnalysisManager();
    };
    entityFactory = (String entityName) -> {
      return new Entity(entityName);
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
   * Sets the BlobstoreManagerFactory.
   *
   * @param blobstoreManagerFactory a BlobstoreManagerFactory object set to return a new
   *     BlobstoreManager.
   */
  public void setBlobstoreManagerFactory(BlobstoreManagerFactory blobstoreManagerFactory) {
    this.blobstoreManagerFactory = blobstoreManagerFactory;
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
   * Sets the ImagesManagerFactory.
   *
   * @param imagesManagerFactory an ImagesManagerFactory object set to return a new ImagesManager.
   */
  public void setImagesManagerFactory(ImagesManagerFactory imagesManagerFactory) {
    this.imagesManagerFactory = imagesManagerFactory;
  }

  /**
   * Sets the StoryManagerFactory.
   *
   * @param storyManagerFactory a StoryManagerFactory object set to return a new StoryManager.
   */
  public void setStoryManagerFactory(StoryManagerFactory storyManagerFactory) {
    this.storyManagerFactory = storyManagerFactory;
  }

  /**
   * Sets the StoryAnalysisManagerFactory.
   *
   * @param storyAnalysisManagerFactory a StoryAnalysisManagerFactory object set to return a new
   *     StoryAnalysisManager.
   */
  public void setStoryAnalysisManagerFactory(
      StoryAnalysisManagerFactory storyAnalysisManagerFactory) {
    this.storyAnalysisManagerFactory = storyAnalysisManagerFactory;
  }

  /**
   * Sets the EntityFactory.
   *
   * @param entityFactory an EntityFactory object set to return a new Entity.
   */
  public void setEntityFactory(EntityFactory entityFactory) {
    this.entityFactory = entityFactory;
  }

  /**
   * {@inheritDoc}
   *
   * Expecting a post request from Blobstore containing the data fields from the image-upload
   * form. The form in the HTML will connect to the Blobstore URL, which encodes the image and then
   * redirects the request to this Url. After having gone through Blobstore, the request will
   * include the image uploaded, available as a blob.
   *
   * If the current user is logged out, they will automatically be logged in before they upload the
   * image. The image is analyzed with the ImagesAnalysisManager, the result of which is fed into
   * the PromptManager to create a prompt which is then used to generate the raw Backstory through
   * the StoryManager. The raw Backstory then is checked by the StoryAnalysisManager for toxicity
   * and, if it passes, is sent to permanent storage, along with the uploaded image's blob key.
   */
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Check to see if the user is currently logged in
    UserService userService = backstoryUserServiceFactory.newInstance();
    if (!userService.isUserLoggedIn()) {
      String urlToRedirectToAfterUserLogsIn = "/analyze-image";
      String loginUrl = userService.createLoginURL(urlToRedirectToAfterUserLogsIn);
      response.sendRedirect(loginUrl);
      return;
    }

    // Get user identification to store alongside their backstory and image
    String userEmail = userService.getCurrentUser().getEmail();

    // The blobKeyString of the image will be used to serve the image back to the front-end.
    BlobstoreManager blobstoreManager = blobstoreManagerFactory.newInstance();
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
    List<byte[]> imagesAsByteArrays = Arrays.asList(bytes);
    List<AnnotatedImage> annotatedImages =
        imagesManager.createAnnotatedImagesFromImagesAsByteArrays(imagesAsByteArrays);
    // Validate that annotatedImages only includes one image since, currently, Backstory 
    //only supports single image uploads.
    if (annotatedImages.size() != 1) {
      // Redirect back to the HTML page.
      response.sendError(400, "Please upload exactly one image.");
      return;
    }
    AnnotatedImage annotatedImage = annotatedImages.get(0);
    List<String> descriptions = annotatedImage.getLabelDescriptions();

    // From the image annotations (the analytics) the prompt can be created.
    PromptManager promptManager = new PromptManager(descriptions);
    String prompt = promptManager.generatePrompt(DELIMITER);

    StoryManager storyManager = storyManagerFactory.newInstance(prompt, STORY_WORD_LENGTH, TEMPERATURE);
    // The loop is necessary because of a memory leak in the GPT2 container which causes generation to fail.
    // TODO: Fix the memory leak within the GPT2 container itself.
    String rawBackstory = "";
    int textGenerationAttemps = 0;
    while (textGenerationAttemps < MAX_GENERATION_ATTEMPS) {
      try {
        rawBackstory = storyManager.generateText();
      } catch (RuntimeException exception) {
        System.err.println(exception);
      }
      textGenerationAttemps++;
    }
    if (rawBackstory == "") {
      response.sendError(400,
          "Sorry! There was an error in your backstory generation. Please try again!");
      return;
    }

    String backstory = "";
    try {
      StoryAnalysisManager storyAnalysisManager = storyAnalysisManagerFactory.newInstance();
      StoryDecision storyDecision = storyAnalysisManager.generateDecision(rawBackstory);
      backstory = storyDecision.getStory();
    } catch (NoAppropriateStoryException | APINotAvailableException exception) {
      response.sendError(400,
          "Sorry! No appropriate Backstory was found for your image. Please try again with another image.");
    }

    // Adds an ending to a story which passes the filtration check.
    Text finalBackstory = new Text(StoryEndingTools.endStory(backstory));

    // Get metadata about the backstory
    final long timestamp = System.currentTimeMillis();

    // Add the input to datastore
    Entity analyzedImageEntity = entityFactory.newInstance("analyzed-image");
    analyzedImageEntity.setProperty("userEmail", userEmail);
    analyzedImageEntity.setProperty("blobKeyString", blobKeyString);
    analyzedImageEntity.setProperty("backstory", finalBackstory);
    analyzedImageEntity.setProperty("timestamp", timestamp);

    DatastoreService datastoreService = backstoryDatastoreServiceFactory.newInstance();
    datastoreService.put(analyzedImageEntity);

    // Redirect back to the HTML page.
    response.sendRedirect("/index.html");
  }
}
