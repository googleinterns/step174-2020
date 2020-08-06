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
import org.junit.Assert.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Tests for VisionManagerImpl.
 *
 * Testing strategy for each operation of VisionManagerImpl:
 * public VisionManagerImpl(byte[] rawImageData);
 *  Partition on rawImageData: empty, null, non-empty image data.
 * 
 * public String getLabelsAsJson();
 *  Unnecesary to test as it is a wrapper for a Gson call.
 *
 * public List<String> getLabelDescriptions();
 *  Partition on this: labelAnnotations is empty, or non-empty list of EntityAnnotations.
 */
public final class VisionManagerTest {
  @Test
  public void testCorrectRequestInput(){
    ImageAnnotatorClient mockImageAnnotatorClient = mock(ImageAnnotatorClient.class);
    ImagesManager manager = VisionManager(mockImageAnnotatorClient);

    ArgumentCaptor<Collection<AnnotateImagesRequest>> argument = ArgumentCaptor.forClass(AnnotateImagesRequest.class);
    verify(mockImageAnnotatorClient.batchAnnotateImages(argument.capture()));
    Collection<AnnotateImagesRequest> requests = argument.getValue();
    Assert.assertEqual(requests.size, 1);

  }
}