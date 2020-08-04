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

package com.google.sps.vision;

import static org.mockito.Mockito.*;
import java.io.IOException;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.EntityAnnotation;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.protobuf.ByteString;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import org.junit.Test;
import java.io.ByteArrayOutputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import java.lang.IllegalArgumentException;
import com.google.sps.vision.VisionManagerImpl;
import org.junit.Before;

/**
 * Tests for VisionManagerImpl.
 */
public final class VisionManagerImplTest {

  // Testing strategy for each operation of VisionManagerImpl:
  /*
   * public VisionManagerImpl(byte[] rawImageData);
   *  Partition on rawImageData: empty, null, non-empty image data.
   * 
   * public String getLabelsAsJson();
   *  Unnecesary to test as it is a wrapper for a Gson call.
   *
   * public List<String> getLabelDescriptions();
   *  Partition on this: labelAnnotations is empty, or non-empty list of EntityAnnotations.
   */

  private byte[] nullRawImageData;
  private byte[] emptyRawImageData;
  private byte[] rawImageData;
  private List<EntityAnnotation> emptyLabelAnnotations;
  private List<EntityAnnotation> labelAnnotations;

  @Before
  public void setUp() {
    nullRawImageData = null;
    emptyRawImageData = new byte[0];
    rawImageData = getBytesFromImageReference(
      "src/test/java/com/google/sps/vision/data/dogRunningOnBeach.jpg", "jpg");
    emptyLabelAnnotations = new ArrayList<>();

    labelAnnotations = new ArrayList<>();

    EntityAnnotation entityAnnotationMock = mock(EntityAnnotation.class);
    // mock the behavior of stock service to return the value of various stocks
    when(entityAnnotationMock.getDescription()).thenReturn("DescriptionOne");

    labelAnnotations.add(entityAnnotationMock);
    labelAnnotations.add(entityAnnotationMock);
  }

  /**
   * Returns an image as a byte array from the local reference of the image.
   *
   * @param reference the local reference of the image.
   * @param fileType the file extension of the image.
   * @return the byte array representation of the image.
   */
  private static byte[] getBytesFromImageReference(String reference, String fileType) {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();

    try {
      BufferedImage bImage = ImageIO.read(new File(reference));
      ImageIO.write(bImage, fileType, bos);
    } catch (IOException exception){
      System.err.println("IOException while reading image");
    }

    return bos.toByteArray();
  }

  /**
   * Tests paritions on public VisionManagerImpl(byte[] rawImageData);
   *  rawImageData is null.
   *  Expects IllegalArgumentException to be thrown.
  */
  @Test(expected = IllegalArgumentException.class)
  public void constructorNullImageData() throws IOException {
    VisionManager visionManagerNullImage = new VisionManagerImpl(nullRawImageData);
  }

  /**
   * Tests paritions on VisionManagerImpl(byte[] rawImageData);
   *  rawImageData is empty.
   *  Expects IllegalArgumentException to be thrown.
  */
  @Test(expected = IllegalArgumentException.class)
  public void constructorEmptyImageData() throws IOException {
    VisionManager visionManagerNullImage = new VisionManagerImpl(emptyRawImageData);
  }

  /**
   * Tests paritions on VisionManagerImpl(byte[] rawImageData);
   *  rawImageData is non-empty image data.
  */
  @Test
  public void constructorRawImageData() {
    VisionManagerImpl actual = new VisionManagerImpl(rawImageData, labelAnnotations);
    assertEquals(rawImageData, actual.getRawImageData());
    assertEquals(labelAnnotations, actual.getLabelAnnotations());
  }

  /**
   * Tests paritions on getLabelDescriptions();
   *  labelAnnotations is empty.
  */
  @Test
  public void getLabelDescriptionsEmptyLabelAnnotations() {
    VisionManagerImpl vmActual = new VisionManagerImpl(rawImageData, emptyLabelAnnotations);
    List<String> actual = vmActual.getLabelDescriptions();
    List<String> expected = new ArrayList<>();
    assertEquals(expected, actual);
  }

  /**
   * Tests paritions on getLabelDescriptions();
   *  labelAnnotations is non-empty list of EntityAnnotations..
  */
  @Test
  public void getLabelDescriptionsNonEmptyLabelAnnotations() {
    VisionManagerImpl vmActual = new VisionManagerImpl(rawImageData, labelAnnotations);
    List<String> actual = vmActual.getLabelDescriptions();
    List<String> expected = new ArrayList<>();
    expected.add("DescriptionOne");
    expected.add("DescriptionOne");

    assertEquals(expected, actual);
  }
}
