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
import java.util.List;

/**
 * Interface for the Image Analytics Manager.
 *
 * VisionManager Manages the gathering and packaging of Vision API image analytics.
 * A VisionManager object represents an image annotated with analytics from Vision API.
 *
 * The image is represented as the byte data in the rawImageData byte array, and the annotated
 * analytics are represented as labelAnnotations, a list of Vision EntityAnnotation objects, which
 * each represent individual labels.
 */

public interface VisionManager {
  /**
   * Returns the full image label annotations as a Json-formatted string of text.
   *
   * @return all image labels from Vision API's getLabelAnnotationsList method, in Json.
   */
  public String getLabelsAsJson();

  /**
   * Returns the image label description fields for all labels.
   *
   * @return list of "description" field for all labels returned by Vision API's
   *     getLabelAnnotationsList method.
   */
  public List<String> getLabelDescriptions();
}