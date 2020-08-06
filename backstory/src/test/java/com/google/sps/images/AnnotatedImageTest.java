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

/**
 * Tests for AnnotatedImage.
 * Testing strategy for each operation of AnnotatedImage:
 *
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
public final class AnnotatedImageTest {
  private static final byte[] nullRawImageData = null;
  private static final byte[] emptyRawImageData = new byte[0];
  private static final List<EntityAnnotation> emptyLabelAnnotations = new ArrayList<>();

  /**
   * Create a mock entity annotation with a description field set.
   *
   * @param description the description to give the mock entity annotation
   * @return the mock entity annotation
   */
  private static EntityAnnotation makeMockEntityAnnotationWithDescription(String description) {
    EntityAnnotation mockEntityAnnotation = mock(EntityAnnotation.class);
    when(mockEntityAnnotation.getDescription()).thenReturn(description);
    return mockEntityAnnotation;
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
    AnnotatedImage annotatedImageExpectingException = new AnnotatedImage(nullRawImageData, emptyLabelAnnotations);
  }

  /**
   * public AnnotatedImage(byte[] rawImageData, List<EntityAnnotation> labelAnnotations);
   *  rawImageData is empty.
   *  labelAnnotations is non-empty.
   *  Expects IllegalArgumentException to be thrown.
  */
  @Test(expected = IllegalArgumentException.class)
  public void constructorEmptyImageData() throws IllegalArgumentException {
    List<EntityAnnotation> labelAnnotations = new ArrayList<>();
    labelAnnotations.add(makeMockEntityAnnotationWithDescription("Description")); 

    AnnotatedImage annotatedImageExpectingException = new AnnotatedImage(emptyRawImageData, labelAnnotations);
  }

  /**
   * public AnnotatedImage(byte[] rawImageData, List<EntityAnnotation> labelAnnotations);
   *  rawImageData is non-empty image data.
   *  labelAnnotations is non-empty.
  */
  @Test
  public void constructorRawImageData() throws IllegalArgumentException, IOException {
    List<EntityAnnotation> labelAnnotations = new ArrayList<>();
    labelAnnotations.add(makeMockEntityAnnotationWithDescription("Description"));
    byte[] rawImageData;
    try {
      rawImageData = getBytesFromImageReference(
        "src/test/java/com/google/sps/images/data/dogRunningOnBeach.jpg", "jpg");
    } catch (IOException exception) {
      throw exception;
    }

    AnnotatedImage actualAnnotatedImage = new AnnotatedImage(rawImageData, labelAnnotations);
    assertTrue(Arrays.equals(rawImageData, actualAnnotatedImage.getRawImageData()));
    assertEquals(labelAnnotations, actualAnnotatedImage.getLabelAnnotations());
  }

  /**
   * public List<String> getLabelDescriptions();
   *  labelAnnotations is empty.
  */
  @Test
  public void getLabelDescriptionsEmptyLabelAnnotations() throws IllegalArgumentException, IOException {
    byte[] rawImageData;
    try {
      rawImageData = getBytesFromImageReference(
        "src/test/java/com/google/sps/images/data/dogRunningOnBeach.jpg", "jpg");
    } catch (IOException exception) {
      throw exception;
    }
        
    AnnotatedImage annotatedImageActual = new AnnotatedImage(rawImageData, emptyLabelAnnotations);
    assertEquals(new ArrayList<>(), annotatedImageActual.getLabelDescriptions());
  }

  /**
   * public List<String> getLabelDescriptions();
   *  labelAnnotations is a non-empty list of EntityAnnotations.
  */
  @Test
  public void getLabelDescriptionsNonEmptyLabelAnnotations() throws IllegalArgumentException, IOException {
    List<EntityAnnotation> labelAnnotations = new ArrayList<>();
    labelAnnotations.add(makeMockEntityAnnotationWithDescription("DescriptionOne"));
    labelAnnotations.add(makeMockEntityAnnotationWithDescription("DescriptionTwo")); 
    byte[] rawImageData;
    try {
      rawImageData = getBytesFromImageReference(
        "src/test/java/com/google/sps/images/data/dogRunningOnBeach.jpg", "jpg");
    } catch (IOException exception) {
      throw exception;
    }

    AnnotatedImage annotatedImageActual = new AnnotatedImage(rawImageData, labelAnnotations);
    List<String> actualLabelDescriptions = annotatedImageActual.getLabelDescriptions();
    List<String> expectedLabelDescriptions = new ArrayList<>();
    expectedLabelDescriptions.add("DescriptionOne");
    expectedLabelDescriptions.add("DescriptionTwo");

    assertEquals(expectedLabelDescriptions, actualLabelDescriptions);
  }
}
