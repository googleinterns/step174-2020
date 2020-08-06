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

import com.google.sps.images.data.AnnotatedImage;
import java.io.IOException;
import java.util.List;

/**
 * Interface for the Image Analytics Manager.
 *
 * ImagesManager Manages the gathering and packaging of image analytics.
 * The manager contains methods to create annotated image objects from raw image data,
 * and performing manipulations and analytics on all the images uploaded.
 */
public interface ImagesManager {
  /**
   * Runs analytics on a list of images represented by their raw byte data and packages the images
   * along with their analytics into annotated image objects.
   *
   * @param imagesAsByteArrays a list of byte arrays such that each byte array within the list
   *     represents an image uploaded to
   * backstory.
   * @return a list of annotated images correspending to the original images in imagesAsByteArrays.
   */
  public List<AnnotatedImage> createAnnotatedImagesFromImagesAsByteArrays(
      List<byte[]> imagesAsByteArrays) throws IOException;
}