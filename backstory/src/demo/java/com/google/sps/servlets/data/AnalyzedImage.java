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

package com.google.sps.servlets.data;

/** Represents an image analyzed with the Vison API */
public final class AnalyzedImage {
  private final String imageUrl;
  private final String labelsJsonArray;

  public AnalyzedImage(String imageUrl, String labelsJsonArray) {
    this.imageUrl = imageUrl;
    this.labelsJsonArray = labelsJsonArray;
  }

  /** Get the ImageUrl */
  public String getImageUrl() {
    return imageUrl;
  }

  /**
   * Get the array of JSON objects representing a the list of labels returned from Vision API for
   * the labels
   */
  public String getLabelsJsonArray() {
    return labelsJsonArray;
  }
}
