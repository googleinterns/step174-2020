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
import com.google.protobuf.ByteString;
import com.google.cloud.vision.v1.EntityAnnotation;
import java.util.ArrayList;
import java.util.List;
import java.lang.IllegalArgumentException;
import java.io.IOException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import java.io.ByteArrayOutputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import javax.imageio.ImageIO;
import java.lang.IllegalArgumentException;
import com.google.sps.images.data.AnnotatedImage;
import org.junit.Before;

/**
 * Tests for AnnotatedImage.
 */
public final class AnnotatedImageTest {

  // Testing strategy for each operation of AnnotatedImage:
  /*
   * public AnnotatedImage(byte[] rawImageData, List<EntityAnnotation> labelAnnotations);
   *  Partition on rawImageData: null, empty, non-empty image data.
   *  Partition on labelAnnotations: empty, non-empty image data.
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
  public void setUp() throws IOException {
    nullRawImageData = null;
    emptyRawImageData = new byte[0];
    rawImageData = getBytesFromImageReference(
      "src/test/java/com/google/sps/images/data/dogRunningOnBeach.jpg", "jpg");
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
  private static byte[] getBytesFromImageReference(String reference, String fileType) throws IOException {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();

    try {
      BufferedImage bImage = ImageIO.read(new File(reference));
      ImageIO.write(bImage, fileType, bos);
    } catch (IOException exception){
      throw exception;
    }

    return bos.toByteArray();
  }

  /**
   * public AnnotatedImage(byte[] rawImageData, List<EntityAnnotation> labelAnnotations);
   *  rawImageData is null.
   *  labelAnnotations is empty.
   *  Expects IllegalArgumentException to be thrown.
  */
  @Test(expected = IllegalArgumentException.class)
  public void constructorNullImageData() throws IllegalArgumentException {
    AnnotatedImage annotatedImageActual = new AnnotatedImage(nullRawImageData, emptyLabelAnnotations);
  }

  /**
   * public AnnotatedImage(byte[] rawImageData, List<EntityAnnotation> labelAnnotations);
   *  rawImageData is empty.
   *  labelAnnotations is non-empty.
   *  Expects IllegalArgumentException to be thrown.
  */
  @Test(expected = IllegalArgumentException.class)
  public void constructorEmptyImageData() throws IllegalArgumentException {
    AnnotatedImage annotatedImageActual = new AnnotatedImage(emptyRawImageData, labelAnnotations);
  }

  /**
   * public AnnotatedImage(byte[] rawImageData, List<EntityAnnotation> labelAnnotations);
   *  rawImageData is non-empty image data.
   *  labelAnnotations is non-empty.
  */
  @Test
  public void constructorRawImageData() throws IllegalArgumentException {
    AnnotatedImage actual = new AnnotatedImage(rawImageData, labelAnnotations);
    assertTrue(Arrays.equals(rawImageData, actual.getRawImageData()));
    assertEquals(labelAnnotations, actual.getLabelAnnotations());
  }

  /**
   * Tests paritions on getLabelDescriptions();
   *  labelAnnotations is empty.
  */
  @Test
  public void getLabelDescriptionsEmptyLabelAnnotations() throws IllegalArgumentException {
    AnnotatedImage annotatedImageActual = new AnnotatedImage(rawImageData, emptyLabelAnnotations);
    List<String> actual = annotatedImageActual.getLabelDescriptions();
    List<String> expected = new ArrayList<>();
    assertEquals(expected, actual);
  }

  /**
   * Tests paritions on getLabelDescriptions();
   *  labelAnnotations is a non-empty list of EntityAnnotations.
  */
  @Test
  public void getLabelDescriptionsNonEmptyLabelAnnotations() throws IllegalArgumentException {
    AnnotatedImage annotatedImageActual = new AnnotatedImage(rawImageData, labelAnnotations);
    List<String> actual = annotatedImageActual.getLabelDescriptions();
    List<String> expected = new ArrayList<>();
    expected.add("DescriptionOne");
    expected.add("DescriptionOne");

    assertEquals(expected, actual);
  }
}
