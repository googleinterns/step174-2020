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

package com.google.sps.images;

import static org.mockito.Mockito.*;

import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.EntityAnnotation;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.sps.images.AnnotatedImageTest;
import com.google.sps.images.VisionImagesManager;
import com.google.sps.images.data.AnnotatedImage;
import com.google.rpc.Status;
import com.google.rpc.Status.Builder;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

/**
 * Tests for VisionImagesManager.
 */
public final class VisionImagesManagerTest {
  /**
   * Check that RuntimeException thrown if
   * different number of responses than requests.
   */
  @Test (expected = RuntimeException.class)
  public void checkWrongNumberOfResponses() throws IOException {
    List<byte[]> rawImageDataList = new ArrayList<byte[]>();
    byte[] rawImageData = AnnotatedImageTest.getBytesFromImageReference(
        "src/test/java/com/google/sps/images/data/dogRunningOnBeach.jpg", "jpg");
    rawImageDataList.add(rawImageData);

    // create an image annotator client that will not return any responses for the request
    List<AnnotatedImage> noResponses = new ArrayList<AnnotatedImage>();

    ImageAnnotatorClient mockImageAnnotatorClient = mockImageAnnotatorClient(noResponses);

    // Create the ImagesManager with the mock image annotator client
    ImagesManager manager = new VisionImagesManager(mockImageAnnotatorClient);

    // Use the ImageAnnotatorClient to check the response given  to it
    manager.createAnnotatedImagesFromImagesAsByteArrays(rawImageDataList);
  }

  /**
   * Check that IOException thrown if
   * a responses has an error
   */
  @Test (expected = IOException.class)
  public void checkInvalidResponseData() throws IOException {
    List<byte[]> rawImageDataList = new ArrayList<byte[]>();
    byte[] rawImageData = AnnotatedImageTest.getBytesFromImageReference(
        "src/test/java/com/google/sps/images/data/dogRunningOnBeach.jpg", "jpg");
    rawImageDataList.add(rawImageData);

    // the null signals to the mock constructor that this response
    // should be set to having an error
    List<AnnotatedImage> nullResponse = new ArrayList<AnnotatedImage>();
    nullResponse.add(null);

    ImageAnnotatorClient mockImageAnnotatorClient = mockImageAnnotatorClient(nullResponse);

    // Create the ImagesManager with the mock image annotator client
    ImagesManager manager = new VisionImagesManager(mockImageAnnotatorClient);

    // Use the ImageAnnotatorClient to check the response given  to it
    manager.createAnnotatedImagesFromImagesAsByteArrays(rawImageDataList);
  }

  /**
   * Checks that the request given to Vision API is valid
   */
  @Test
  public void testCorrectRequestInput() throws IOException {
    List<byte[]> rawImageDataList = new ArrayList<byte[]>();
    byte[] rawImageData = AnnotatedImageTest.getBytesFromImageReference(
        "src/test/java/com/google/sps/images/data/dogRunningOnBeach.jpg", "jpg");
    rawImageDataList.add(rawImageData);

    List<AnnotatedImage> expectedResponses = new ArrayList<AnnotatedImage>();
    expectedResponses.add(new AnnotatedImage(rawImageData, new ArrayList<EntityAnnotation>(), new ArrayList<EntityAnnotation>()));

    ImageAnnotatorClient mockImageAnnotatorClient = mockImageAnnotatorClient(expectedResponses);

    // Create the ImagesManager with the mock image annotator client
    ImagesManager manager = new VisionImagesManager(mockImageAnnotatorClient);

    // Use the ImageAnnotatorClient to check the response given  to it
    manager.createAnnotatedImagesFromImagesAsByteArrays(rawImageDataList);

    // Check that the request is as expected
    ArgumentCaptor<List<AnnotateImageRequest>> argument = ArgumentCaptor.forClass(List.class);
    verify(mockImageAnnotatorClient).batchAnnotateImages(argument.capture());
    List<AnnotateImageRequest> requests = argument.getValue();
    Assert.assertEquals(rawImageDataList.size(), requests.size());

    // Check that features we want are requested
    List<Feature> expectedFeatures = new ArrayList<Feature>();

    Feature labelFeature = Feature.newBuilder().setType(Feature.Type.LABEL_DETECTION).build();
    Feature landmarkFeature = Feature.newBuilder().setType(Feature.Type.LANDMARK_DETECTION).build();

    expectedFeatures.add(labelFeature);
    expectedFeatures.add(landmarkFeature);
    
    for (AnnotateImageRequest request: requests) {
      Assert.assertEquals(expectedFeatures, request.getFeaturesList());
    }
  }

  /** 
   * Create a mock of the ImageAnnotatorClient that will return responses gotten from the
   * passed-in AnnotatedImage List in place of actual network responses.
   *
   * @param desiredResponses a list of the responses we want as AnnotatedImages

   */
  private ImageAnnotatorClient mockImageAnnotatorClient(List<AnnotatedImage> desiredResponses) {
    // Create the necessary mocks
    ImageAnnotatorClient mockImageAnnotatorClient = mock(ImageAnnotatorClient.class);
    BatchAnnotateImagesResponse mockBatchResponse = mock(BatchAnnotateImagesResponse.class);

    List<AnnotateImageResponse> mockResponses = new ArrayList<AnnotateImageResponse>();

    for (AnnotatedImage response: desiredResponses) {
      AnnotateImageResponse mockResponse = mock(AnnotateImageResponse.class);
      
      if (response != null){
        // if image isn't null, then return false for mock response has error, else return true
        when(mockResponse.hasError()).thenReturn(false);

        when(mockResponse.getLabelAnnotationsList()).thenReturn(response.getLabelAnnotations());
        when(mockResponse.getLandmarkAnnotationsList()).thenReturn(response.getLandmarkAnnotations());
      } else {
        Status mockError = mock(Status.class);
        when(mockResponse.hasError()).thenReturn(true);
        when(mockResponse.getError()).thenReturn(mockError);
        when(mockError.getMessage()).thenReturn("foo");
      }

      mockResponses.add(mockResponse);
    }

    when(mockBatchResponse.getResponsesList()).thenReturn(mockResponses);
    when(mockImageAnnotatorClient.batchAnnotateImages(any(List.class))).thenReturn(mockBatchResponse);
    
    return mockImageAnnotatorClient;
  }
}
