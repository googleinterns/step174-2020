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

package com.google.sps.images.data;

import com.google.cloud.vision.v1.EntityAnnotation;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.lang.IllegalArgumentException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * AnnotatedImage is an immutable type which represents an image annotated with labels and other
 * related analytics.
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
 * Representation fields are only returned as immutable representations, or (in the case of
 * rawImageData) as a defensive copy.
 */
public final class AnnotatedImage {
  /** the byte representation of the image data */
  private final byte[] rawImageData;
  /** labels describing the picture */
  private final List<EntityAnnotation> labelAnnotations;
  /** the possible locations for this picture */
  private final List<EntityAnnotation> landmarkAnnotations; 

  /**
   * Instantiates the AnnotatedImage object parameterized with a byte array of raw image data and
   * its labels.
   *
   * @param rawImageData The raw image data for the image being represented in this VisionManager
   *     object. Must be non-empty and non-null.
   * @param labelAnnotations the preset labels to annotate the image with. Must be non-null.
   * @param landmarkAnnotations the locations of this photo. Must be non-null.
   */
  public AnnotatedImage(byte[] rawImageData, List<EntityAnnotation> labelAnnotations, 
    List<EntityAnnotation> landmarkAnnotations)
      throws IllegalArgumentException {
    if (rawImageData == null || labelAnnotations == null || landmarkAnnotations == null | rawImageData.length == 0) {
      throw new IllegalArgumentException(
          "Raw image data must be non-null and non-empty, label annotation data must be non-null");
    }

    this.rawImageData = rawImageData;
    this.labelAnnotations = labelAnnotations;
    this.landmarkAnnotations = landmarkAnnotations;
  }

  /**
   * Returns the full image label annotations as a Json-formatted string of text.
   *
   * @return all image labels from Vision API's getLabelAnnotationsList method, in Json.
   * The string returned will be formatted as a Json array of Json objects, where each label is
   * represented by a Json object.
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

  /** 
   * Return the bytes representing the image 
   *
   * @return byte representation of image held in this class
   */
  public byte[] getRawImageData() {
    return Arrays.copyOf(rawImageData, rawImageData.length);
  }

  /** 
   * Return the labels annotated to the image 
   *
   * @return the labels annotations for this image
   */
  public List<EntityAnnotation> getLabelAnnotations() {
    return Collections.unmodifiableList(labelAnnotations);
  }

  /** 
   * Return the landmarks annotated to the image 
   *
   * @return landmarks annotated to this class
   */
  public List<EntityAnnotation> getLandmarkAnnotations() {
    return Collections.unmodifiableList(landmarkAnnotations);
  }
}
