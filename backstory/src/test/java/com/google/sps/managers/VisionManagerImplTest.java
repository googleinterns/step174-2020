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

package com.google.sps.managers;

import java.io.IOException;
import com.google.cloud.vision.v1.EntityAnnotation;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import java.io.ByteArrayOutputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import com.google.sps.managers.VisionManagerImpl;
import java.lang.IllegalArgumentException;

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
   *  Partition on this: labelAnnotations is empty, or non-empty list of EntityAnnotations.
   *
   * public List<String> getLabelDescriptions();
   *  Partition on this: labelAnnotations is empty, or non-empty list of EntityAnnotations.
   */

  private static final byte[] nullRawImageData = null;
  private static final byte[] emptyRawImageData = new byte[0];
  private static final byte[] rawImageData = getBytesFromImageReference("data/dogRunningOnBeach.jpg", "jpg");
  private static final List<EntityAnnotation> emptyLabelAnnotations = new ArrayList<>();

  private static final EntityAnnotation labelOne = new EntityAnnotation();
  private static final EntityAnnotation labelTwo = new EntityAnnotation();
  labelOne.setDescription("descriptionOne");
  labelOne.setScore(.99)
  labelTwo.setDescription("descriptionTwo");
  labelTwo.setScore(.70)
  private static final List<EntityAnnotation> labelAnnotations = List.of(labelOne, labelTwo);

  /**
   * Returns an image as a byte array from the local reference of the image.
   *
   * @param reference the local reference of the image.
   * @param fileType the file extension of the image.
   * @return the byte array representation of the image.
   */
  private byte[] getBytesFromImageReference(String reference, String fileType) {
    BufferedImage bImage = ImageIO.read(new File(reference));
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ImageIO.write(bImage, fileType, bos);
    return bos.toByteArray();
  }

  /**
   * Tests paritions on public VisionManagerImpl(byte[] rawImageData);
   *  rawImageData is null.
   *  Expects IllegalArgumentException to be thrown.
  */
  @Test
  public void constructorNullImageData() {
    assertThrows(IllegalArgumentException.class,
        () -> {
            VisionManager visionManagerNullImage = new VisionManagerImpl(nullRawImageData);
        }, "expected IllegalArgumentException");
  }

  /**
   * Tests paritions on VisionManagerImpl(byte[] rawImageData);
   *  rawImageData is empty.
   *  Expects IllegalArgumentException to be thrown.
  */
  @Test
  public void constructorEmptyImageData() {
    assertThrows(IllegalArgumentException.class,
        () -> {
            VisionManager visionManagerNullImage = new VisionManagerImpl(emptyRawImageData);
        }, "expected IllegalArgumentException");
  }

  /**
   * Tests paritions on VisionManagerImpl(byte[] rawImageData);
   *  rawImageData is non-empty image data.
  */
  @Test
  public void constructorRawImageData() {
    VisionManager actual = new VisionManagerImpl(rawImageData, labelAnnotations);
    assertEquals(rawImageData, actual.getRawImageData());
    assertEquals(labelAnnotations, actual.getLabelAnnotations())
  }

  /**
   * Tests paritions on getLabelsAsJson();
   *  labelAnnotations is empty.
  */
  @Test
  public void getLabelsAsJsonEmptyLabelAnnotations() {
    VisionManager vmActual = new VisionManagerImpl(rawImageData, emptyLabelAnnotations);
    String actual = vmActual.getLabelsAsJson();
    String expected = "[{}]";
    System.out.println(actual);
    assertEquals(expected, actual);
  }

  /**
   * Tests paritions on getLabelsAsJson();
   *  labelAnnotations is non-empty list of EntityAnnotations..
  */
  @Test
  public void getLabelsAsJsonNonEmptyLabelAnnotations() {
    VisionManager vmActual = new VisionManagerImpl(rawImageData, labelAnnotations);
    String actual = vmActual.getLabelsAsJson();
    String expected = "";
    System.out.println(actual);
    assertEquals(expected, actual);
  }

  /**
   * Tests paritions on getLabelDescriptions();
   *  labelAnnotations is empty.
  */
  @Test
  public void getLabelDescriptionsEmptyLabelAnnotations() {
    VisionManager vmActual = new VisionManagerImpl(rawImageData, emptyLabelAnnotations);
    List<String> actual = vmActual.getLabelDescriptions();
    List<String> expected = new ArrayList<>();
    System.out.println(actual);
    assertEquals(expected, actual);
  }

  /**
   * Tests paritions on getLabelDescriptions();
   *  labelAnnotations is non-empty list of EntityAnnotations..
  */
  @Test
  public void getLabelDescriptionsNonEmptyLabelAnnotations() {
    VisionManager vmActual = new VisionManagerImpl(rawImageData, labelAnnotations);
    String actual = vmActual.getLabelDescriptions();
    List<String> expected = new ArrayList<>();
    expected.add("descriptionOne", "descriptionTwo");
    System.out.println(actual);
    assertEquals(expected, actual);
  }
}
