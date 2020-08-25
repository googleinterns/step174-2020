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

import static org.mockito.Mockito.*;
import com.google.sps.images.ImagesManager;
import com.google.sps.images.VisionImagesManager;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.sps.images.data.AnnotatedImage;
import com.google.sps.perspective.PerspectiveStoryAnalysisManager;
import com.google.sps.perspective.StoryAnalysisManager;
import com.google.sps.perspective.data.StoryDecision;
import com.google.sps.perspective.data.NoAppropriateStoryException;
import com.google.sps.perspective.data.APINotAvailableException;
import com.google.sps.story.PromptManager;
import com.google.sps.story.StoryManager;
import com.google.sps.story.StoryManagerImpl;
import com.google.sps.servlets.AnalyzeImageServlet;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.sps.servlets.data.BlobstoreManagerFactory;
import com.google.sps.servlets.data.BackstoryDatastoreServiceFactory;
import com.google.sps.servlets.data.EntityFactory;
import com.google.sps.servlets.data.ImagesManagerFactory;
import com.google.sps.servlets.data.StoryManagerFactory;
import com.google.sps.servlets.data.BackstoryUserServiceFactory;
import com.google.sps.servlets.data.StoryAnalysisManagerFactory;
import com.google.sps.servlets.data.BlobstoreManager;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.User;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.ArgumentCaptor;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import com.google.sps.perspective.data.APINotAvailableException;
import com.google.sps.perspective.data.NoAppropriateStoryException;
import com.google.sps.perspective.data.StoryDecision;

/**
 * Tests for the analyze image servlet, which contains all image analysis and backstory generation
 * functionalities.
 */
@RunWith(MockitoJUnitRunner.class)
public final class AnalyzeImageServletTest {

  /**
   * Tests that the correct input is passed from the initial call to the managers, 
   * and that the correct ouput is passed from the managers to the permanent storage service.
   */
  @Test
  public void testDoPostInputAndOuput() throws APINotAvailableException, NoAppropriateStoryException, IOException {
    HttpServletRequest mockRequest = mock(HttpServletRequest.class);
    HttpServletResponse mockResponse = mock(HttpServletResponse.class);

    AnalyzeImageServlet servlet = new AnalyzeImageServlet();
    ImagesManager mockImagesManager = mock(VisionImagesManager.class);
    StoryManager mockStoryManager = mock(StoryManagerImpl.class);
    StoryAnalysisManager mockStoryAnalysisManager = mock(PerspectiveStoryAnalysisManager.class);
    DatastoreService mockDatastoreService = mock(DatastoreService.class);
    Entity mockAnalyzedImageEntity = mock(Entity.class);
    BlobstoreManager mockBlobstoreManager = mock(BlobstoreManager.class);
    UserService mockUserService = mock(UserService.class);

    String userEmail = "user@gmail.com";
    String userAuthenticationDomain = "authentication";
    User testUser = new User(userEmail, userAuthenticationDomain);

    // Creates the required real test objects, and sets the required behavior of the mocks.
    when(mockUserService.isUserLoggedIn()).thenReturn(true);
    when(mockUserService.getCurrentUser()).thenReturn(testUser);

    String blobKeyString = "blobKeyString";
    when(mockBlobstoreManager.getUploadedFileBlobKeyString(any(HttpServletRequest.class), anyString()))
        .thenReturn(blobKeyString);
    int bytesInUploadedImage = 10;
    byte[] uploadedImageBytes = new byte[bytesInUploadedImage];
    when(mockBlobstoreManager.getBlobBytes(any(HttpServletRequest.class), anyString()))
        .thenReturn(uploadedImageBytes);

    String sampleDescription = "sampleDescription";
    List<String> uploadedImageDescriptions = new ArrayList<>();
    uploadedImageDescriptions.add(sampleDescription);
    AnnotatedImage uploadedAnnotatedImage = mock(AnnotatedImage.class);
    when(uploadedAnnotatedImage.getLabelDescriptions()).thenReturn(uploadedImageDescriptions);
    List<AnnotatedImage> uploadedAnnotatedImages = Arrays.asList(uploadedAnnotatedImage);
    when(mockImagesManager.createAnnotatedImagesFromImagesAsByteArrays(
      any(List.class)
    )).thenReturn(uploadedAnnotatedImages);

    String sampleRawBackstory = "sampleRawBackstory";
    when(mockStoryManager.generateText()).thenReturn(sampleRawBackstory);

    StoryDecision sampleStoryDecision = new StoryDecision(sampleRawBackstory);
    when(mockStoryAnalysisManager.generateDecision(anyString())).thenReturn(sampleStoryDecision);

    // Create and set the factories to return the configured mocks.
    BackstoryUserServiceFactory backstoryUserServiceFactory = () -> {
      return mockUserService;
    };
    BackstoryDatastoreServiceFactory backstoryDatastoreServiceFactory = () -> {
      return mockDatastoreService;
    };
    BlobstoreManagerFactory blobstoreManagerFactory = () -> {
      return mockBlobstoreManager;
    };
    ImagesManagerFactory imagesManagerFactory = () -> {
      return mockImagesManager;
    };
    StoryManagerFactory storyManagerFactory = (String prompt, int storyLength, double temperature) -> {
      return mockStoryManager;
    };
    StoryAnalysisManagerFactory storyAnalysisManagerFactory = () -> {
      return mockStoryAnalysisManager;
    };
    EntityFactory entityFactory = (String entityName) -> {
      return mockAnalyzedImageEntity;
    };
    servlet.setBackstoryUserServiceFactory(backstoryUserServiceFactory);
    servlet.setBlobstoreManagerFactory(blobstoreManagerFactory);
    servlet.setBackstoryDatastoreServiceFactory(backstoryDatastoreServiceFactory);
    servlet.setImagesManagerFactory(imagesManagerFactory);
    servlet.setStoryManagerFactory(storyManagerFactory);
    servlet.setStoryAnalysisManagerFactory(storyAnalysisManagerFactory);
    servlet.setEntityFactory(entityFactory);

    // doPost call to initiate testing.
    servlet.doPost(mockRequest, mockResponse);

    // Check that the request is as expected:
    ArgumentCaptor<HttpServletRequest> requestCaptor = ArgumentCaptor.forClass(HttpServletRequest.class);
    ArgumentCaptor<String> formInputNameCaptor = ArgumentCaptor.forClass(String.class);
    verify(mockBlobstoreManager).getUploadedFileBlobKeyString(requestCaptor.capture(), formInputNameCaptor.capture());
    // The formInputName must be "image-upload" because that is the name of the front-end form input element.
    String expectedFormInputName = "image-upload";
    String actualFormInputName = formInputNameCaptor.getValue();
    Assert.assertEquals(expectedFormInputName, actualFormInputName);

    // Verify that the proper properties are set for the entity
    Text sampleBackstory = new Text(sampleRawBackstory);
    verify(mockAnalyzedImageEntity).setProperty("userEmail", userEmail);
    verify(mockAnalyzedImageEntity).setProperty("blobKeyString", blobKeyString);
    verify(mockAnalyzedImageEntity).setProperty(eq("backstory"), any(Text.class));
    verify(mockAnalyzedImageEntity).setProperty(eq("timestamp"), anyLong());

    // Check that the analyzed image entity goes into datastore.
    ArgumentCaptor<Entity> outputArgument = ArgumentCaptor.forClass(Entity.class);
    verify(mockDatastoreService).put(outputArgument.capture());
    Entity actualEntityInDatastore = outputArgument.getValue();
    Assert.assertEquals(mockAnalyzedImageEntity, actualEntityInDatastore);
  }
}
