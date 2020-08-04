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
<<<<<<< HEAD:backstory/src/test/java/com/google/sps/managers/VisionManagerImplTest.java
import com.google.sps.managers.VisionManagerImpl;
=======
import com.google.sps.vision.VisionManagerImpl;
import org.junit.Before;
>>>>>>> d7865c2... addresses comments:backstory/src/test/java/com/google/sps/vision/VisionManagerImplTest.java

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

<<<<<<< HEAD:backstory/src/test/java/com/google/sps/managers/VisionManagerImplTest.java
  private static final byte[] nullRawImageData = null;
  private static final byte[] emptyRawImageData = new byte[0];
  private static final byte[] rawImageData = getBytesFromImageReference(
      "src/test/java/com/google/sps/managers/data/dogRunningOnBeach.jpg", "jpg");
  private static final List<EntityAnnotation> emptyLabelAnnotations = new ArrayList<>();
=======
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

    try {
      labelAnnotations = detectLabelsFromImageBytes(rawImageData);
    } catch(IOException exception) {
      System.err.println("detectLabelsFromImageBytes failed");
    }
  }
>>>>>>> d7865c2... addresses comments:backstory/src/test/java/com/google/sps/vision/VisionManagerImplTest.java

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

  /** Detects labels in the image specified by the image byte data by calling the Vision API. */
  private static List<EntityAnnotation> detectLabelsFromImageBytes(byte[] bytes) throws IOException {
    List<AnnotateImageRequest> requests = new ArrayList<>();
    List<EntityAnnotation> labels;

    Image img = Image.newBuilder().setContent(ByteString.copyFrom(bytes)).build();
    Feature feat = Feature.newBuilder().setType(Feature.Type.LABEL_DETECTION).build();
    AnnotateImageRequest request =
        AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();
    requests.add(request);

    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of the requests the
    // client will be automatically closed, as it is called within the try.
    try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
      BatchAnnotateImagesResponse response = client.batchAnnotateImages(requests);
      List<AnnotateImageResponse> responses = response.getResponsesList();

      // There is only one image in the batch response (only supports uploading one image at a time
      // right now)
      AnnotateImageResponse res = responses.get(0);

      if (res.hasError()) {
        System.out.format("Error: %s%n", res.getError().getMessage());
        return new ArrayList<EntityAnnotation>();
      }

      // For full list of available annotations, see http://g.co/cloud/vision/docs
      labels = res.getLabelAnnotationsList();
    }

    return labels;
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
   * Tests paritions on getLabelsAsJson();
   *  labelAnnotations is empty.
  */
  @Test
  public void getLabelsAsJsonEmptyLabelAnnotations() {
    VisionManagerImpl vmActual = new VisionManagerImpl(rawImageData, emptyLabelAnnotations);
    String actual = vmActual.getLabelsAsJson();
    String expected = "[]";
    assertEquals(expected, actual);
  }

  /**
   * Tests paritions on getLabelsAsJson();
   *  labelAnnotations is non-empty list of EntityAnnotations..
  */
  @Test
  public void getLabelsAsJsonNonEmptyLabelAnnotations() {
    VisionManagerImpl vmActual = new VisionManagerImpl(rawImageData, labelAnnotations);
    String actual = vmActual.getLabelsAsJson();
    assertFalse(actual.equals(""));
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

    assertFalse(actual.isEmpty());
  }
}
