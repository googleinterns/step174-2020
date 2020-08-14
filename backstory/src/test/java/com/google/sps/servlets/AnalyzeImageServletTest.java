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
  public void testDoPostInputAndOuput() throws APINotAvailableException, NoAppropriateStoryException {
    try {
      // Creates the required mocks for an HTTP request/response.
      HttpServletRequest requestMock = mock(HttpServletRequest.class);
      HttpServletResponse responseMock = mock(HttpServletResponse.class);

      // Creates the required mocks for the managers and their dependent classes (in the case
      // that real object cannot be used).
      AnalyzeImageServlet servlet = new AnalyzeImageServlet();
      ImagesManager mockImagesManager = mock(VisionImagesManager.class);
      StoryManager mockStoryManager = mock(StoryManagerImpl.class);
      StoryAnalysisManager mockStoryAnalysisManager = mock(PerspectiveStoryAnalysisManager.class);
      DatastoreService mockDatastoreService = mock(DatastoreService.class);
      Entity mockAnalyzedImageEntity = mock(Entity.class);
      BlobstoreService mockBlobstoreService = mock(BlobstoreService.class);

      // Creates the required real test objects, and sets the required behavior of the mocks.
      Map<String, List<BlobKey>> mockInputToBlobkey = new HashMap<>();
      List<BlobKey> mockBlobKeys = new ArrayList<>();
      mockBlobKeys.add(new BlobKey("mockBlobkey"));
      byte[] mockImageByteArray = new byte[0];
      mockInputToBlobkey.put("image-upload", mockBlobKeys);

      when(mockBlobstoreService.getUploads(any(HttpServletRequest.class)))
          .thenReturn(mockInputToBlobkey);
      when(mockBlobstoreService.fetchData(
        any(BlobKey.class), anyLong(), anyLong()
      )).thenReturn(mockImageByteArray);

      List<String> mockDescriptions = new ArrayList<>();
      mockDescriptions.add("mockDescription");
      AnnotatedImage mockAnnotatedImage = mock(AnnotatedImage.class);
      when(mockAnnotatedImage.getLabelDescriptions()).thenReturn(mockDescriptions);

      List<AnnotatedImage> mockAnnotatedImages = new ArrayList<>();
      mockAnnotatedImages.add(mockAnnotatedImage);
      when(mockImagesManager.createAnnotatedImagesFromImagesAsByteArrays(
        any(List.class)
      )).thenReturn(mockAnnotatedImages);

      when(mockStoryManager.generateText()).thenReturn("mockBackstory");

      StoryDecision mockStoryDecision = new StoryDecision("mockBackstory");
      when(mockStoryAnalysisManager.generateDecision(anyString())).thenReturn(mockStoryDecision);

      when(mockDatastoreService.put(any(Entity.class))).thenReturn(mock(Key.class));

      // Calls the servlet injection code to insert the required mocks.
      servlet.setToUseMockImagesManager(mockImagesManager);
      servlet.setToUseMockStoryAnalysisManager(mockStoryAnalysisManager);
      servlet.setToUseMockStoryManager(mockStoryManager);
      servlet.setToUseMockDatastoreService(mockDatastoreService, mockAnalyzedImageEntity);
      servlet.setToUseMockBlobstoreService(mockBlobstoreService);

      // doPost call to initiate testing.
      servlet.doPost(requestMock, responseMock);

      // Check that the request is as expected:
      ArgumentCaptor<List<byte[]>> inputArguments = ArgumentCaptor.forClass(List.class);
      verify(mockImagesManager).createAnnotatedImagesFromImagesAsByteArrays(inputArguments.capture());
      List<byte[]> inputImagesByteArrays = inputArguments.getValue();

      // The list of input images in this example consists of only one image.
      Assert.assertEquals(1, inputImagesByteArrays.size());
      // The one image in the imput images is modeled by an empty byte array.
      Assert.assertTrue(inputImagesByteArrays.get(0).length == 0);

      // Check that the analyzed image gets put into datastore correctly:
      // Here we verify that the entity has the appropriate properties set.
      ArgumentCaptor<String> propertyNameCaptor = ArgumentCaptor.forClass(String.class);
      ArgumentCaptor<Object> propertyValueCaptor = ArgumentCaptor.forClass(Object.class);

      verify(mockAnalyzedImageEntity, times(3)).setProperty(propertyNameCaptor.capture(), propertyValueCaptor.capture());

      List<String> capturedPropertyNames = propertyNameCaptor.getAllValues();
      List<Object> capturedPropertyValues = propertyValueCaptor.getAllValues();
      Assert.assertEquals("blobKeyString", capturedPropertyNames.get(0));
      Assert.assertEquals("backstory", capturedPropertyNames.get(1));
      Assert.assertEquals("timestamp", capturedPropertyNames.get(2));
      Assert.assertEquals(3, capturedPropertyValues.size());


      // Here we verify that an entity is put into datastore.
      ArgumentCaptor<Entity> outputArguments = ArgumentCaptor.forClass(Entity.class);
      verify(mockDatastoreService).put(outputArguments.capture());

    } catch (IOException exception) {
      Assert.fail("Exception not expected.");
    }
  }
}
