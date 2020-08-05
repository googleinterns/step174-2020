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

import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.EntityAnnotation;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.protobuf.ByteString;
import java.io.IOException;
import java.lang.IllegalArgumentException;
import java.util.ArrayList;
import java.util.List;

public final class VisionManagerImpl implements VisionManager {
  private final byte[] rawImageData;
  private final List<EntityAnnotation> labelAnnotations;

  /**
   * VisionManager Manages the gathering and packaging of Vision API image analytics.
   * A VisionManager object represents an image annotated with analytics from Vision API.
   *
   * The image is represented as the byte data in the rawImageData byte array, and the annotated
   * analytics are represented as labelAnnotations, a list of Vision EntityAnnotation objects, which
   * each represent individual labels.
   *
   * Abstraction Function(rawImageData, labelAnnotations) =
   * An image represented by the bytes in rawImageData, with a list of labels annotated
   * to the image, represented by the EntityAnnotation(s) in labelAnnotations.
   *
   * Representation Invariant:
   * rawImageData will always represent the same image, it is immutable. It must be non-null. It
   * must be non-empty. Once set, labelAnnotations will never change, it is immutable. It must be
   * non-null.
   *
   * Safety from Representation Exposure:
   * All representation fields are immutable.
   */

  /**
   * Asserts that the representation invariants are met
   * The representation invariants are the conditions which must always
   * be true about the class representation fields.
   */
  private void checkRepresentationInvariantMet() {
    assert rawImageData != null : "raw image data field is null";
    assert labelAnnotations != null : "label annotations data field is null";
    assert rawImageData.length != 0 : "raw image data field is empty";
  }

  /**
   * Instantiates the VisionManager object parameterized with a byte array of raw image data.
   *
   * @param rawImageData The raw image data for the image being represented in this VisionManager
   *     object. Must be non-empty and non-null.
   */
  public VisionManagerImpl(byte[] rawImageData) throws IllegalArgumentException, IOException {
    this.rawImageData = rawImageData;

    if (rawImageData != null && rawImageData.length != 0) {
      this.labelAnnotations = detectLabelsFromImageBytes(rawImageData);
    } else {
      throw new IllegalArgumentException("Raw image data must be non-null and non-empty");
    }

    checkRepresentationInvariantMet();
  }

  /**
   * Instantiates the VisionManager object parameterized with a byte array of raw image data and
   * preset labels.
   *
   * @param rawImageData The raw image data for the image being represented in this VisionManager
   *     object. Must be non-empty and non-null.
   * @param labelAnnotations the preset labels to annotate the image with. Must be non-null.
   */
  public VisionManagerImpl(byte[] rawImageData, List<EntityAnnotation> labelAnnotations)
      throws IllegalArgumentException {
    if (rawImageData == null || labelAnnotations == null || rawImageData.length == 0) {
      throw new IllegalArgumentException(
          "Raw image data must be non-null and non-empty, label annotation data must be non-null");
    }

    this.rawImageData = rawImageData;
    this.labelAnnotations = labelAnnotations;

    checkRepresentationInvariantMet();
  }

  /**
   * Returns the full image label annotations as a Json-formatted string of text.
   *
   * @return all image labels from Vision API's getLabelAnnotationsList method, in Json.
   */
  public String getLabelsAsJson() {
    Gson gson = new Gson();
    return gson.toJson(labelAnnotations);
  }

  /**
   * Returns only the "description" field, for all image label annotations.
   *
   * @return list of "description" fields for all labels returned by Vision API's
   *     getLabelAnnotationsList method.
   */
  public List<String> getLabelDescriptions() {
    List<String> descriptions = new ArrayList<>();

    for (EntityAnnotation labelAnnotation : labelAnnotations) {
      descriptions.add(labelAnnotation.getDescription());
    }

    return descriptions;
  }

  /** Return the bytes representing the image */
  public byte[] getRawImageData() {
    return rawImageData;
  }

  /** Return the labels annotated to the image */
  public List<EntityAnnotation> getLabelAnnotations() {
    return labelAnnotations;
  }

  /**
   * Creates and returns label annotations for the image represented in bytes, using the Vision API.
   *
   * @param bytes The raw image byte data for the image from which labels will be annotated.
   * @return The list of label annotations related to the image, with each individual label being
   *     represented as an EntityAnnotation object.
   */
  private List<EntityAnnotation> detectLabelsFromImageBytes(byte[] bytes) throws IOException {
    List<AnnotateImageRequest> requests = new ArrayList<>();
    List<EntityAnnotation> labels;

    Image img = Image.newBuilder().setContent(ByteString.copyFrom(bytes)).build();
    Feature feat = Feature.newBuilder().setType(Feature.Type.LABEL_DETECTION).build();
    AnnotateImageRequest request =
        AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();
    requests.add(request);

    // Initialize the client that will be used to send requests. This client only needs to be
    // created once, and can be reused for multiple requests. After completing all of the requests
    // the client will be automatically closed, because it is called within the try block.
    try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
      BatchAnnotateImagesResponse response = client.batchAnnotateImages(requests);
      List<AnnotateImageResponse> responses = response.getResponsesList();

      // There is only one image in the batch response (VisionManager only represents one image).
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
}
