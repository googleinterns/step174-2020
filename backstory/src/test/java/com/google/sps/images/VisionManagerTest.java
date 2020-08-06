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
import org.mockito.ArgumentCaptor;
import com.google.sps.images.AnnotatedImageTest;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.sps.images.VisionManager;
import com.google.sps.images.data.AnnotatedImage;
import org.junit.Assert;
import org.junit.Test;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;

/**
 * Tests for VisionManager.
 */
public final class VisionManagerTest {

  /**
   * Checkts that the request given to Vision API is valid
   */
  @Test
  public void testCorrectRequestInput() throws IOException {
    List<byte[]> rawImageDataList = new ArrayList<>();
    byte[] rawImageData = AnnotatedImageTest.getBytesFromImageReference(
        "src/test/java/com/google/sps/images/data/dogRunningOnBeach.jpg", "jpg");
    rawImageDataList.add(rawImageData);

    // Create the necessary mocks
    ImageAnnotatorClient mockImageAnnotatorClient = mock(ImageAnnotatorClient.class);
    BatchAnnotateImagesResponse mockBatchResponse = mock(BatchAnnotateImagesResponse.class);
    List<AnnotateImageResponse> mockResponses = mock(List.class);
    AnnotateImageResponse mockResponse = mock(AnnotateImageResponse.class);
    List<EntityAnnotation> mockLabelAnnotations = mock(List.class);

    when(mockResponse.hasError()).thenReturn(false);
    when(mockResponse.getLabelAnnotationsList()).thenReturn(mockLabelAnnotations);
    when(mockResponses.get(0)).thenReturn(mockResponse);
    when(mockBatchResponse.getResponsesList()).thenReturn(mockResponses);
    when(mockImageAnnotatorClient.batchAnnotateImages(any(List.class))).thenReturn(mockBatchResponse);

    // Create the ImagesManager with the mock image annotator client
    ImagesManager manager = new VisionManager(mockImageAnnotatorClient);

    // Use the ImageAnnotatorClient to check the response given  to it
    manager.createAnnotatedImagesFromImagesAsByteArrays(rawImageDataList);

    // Check that the request is as expected
    ArgumentCaptor<List<AnnotateImageRequest>> argument = ArgumentCaptor.forClass(List.class);
    verify(mockImageAnnotatorClient).batchAnnotateImages(argument.capture());
    List<AnnotateImageRequest> requests = argument.getValue();
    Assert.assertEquals(requests.size(), 1);
  }
}