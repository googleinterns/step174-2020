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

import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.EntityAnnotation;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.protobuf.ByteString;
import com.google.sps.images.data.AnnotatedImage;
import java.io.IOException;
import java.lang.IllegalArgumentException;
import java.util.ArrayList;
import java.util.List;

/**
 * {@inheritDoc}
 *
 * VisionImagesManager is an ImagesManager implemented using the Google Vision API.
 * VisionImagesManager Manages the gathering and packaging of Vision API image analytics.
 */
public final class VisionImagesManager implements ImagesManager {
  private final ImageAnnotatorClient imageAnnotatorClient;

  /**
   * Creates a vision manager object using the image annotator client from Vision API.
   */
  public VisionImagesManager() throws IOException {
    ImageAnnotatorClient imageAnnotatorClient = ImageAnnotatorClient.create();
    this.imageAnnotatorClient = imageAnnotatorClient;
  }

  /**
   * Creates a vision manager object using a passed-in image annotator client.
   *
   * @param imageAnnotatorClient the client to use to analyze the image
   */
  public VisionImagesManager(ImageAnnotatorClient imageAnnotatorClient) {
    this.imageAnnotatorClient = imageAnnotatorClient;
  }

  @Override
  public List<AnnotatedImage> createAnnotatedImagesFromImagesAsByteArrays(List<byte[]> imagesAsByteArrays)
    throws IOException {
    List<AnnotateImageRequest> requests = new ArrayList<AnnotateImageRequest>();

    // add the features we want (labels & landmarks to list)
    List<Feature> features = new ArrayList<Feature>();

    Feature labelFeature = Feature.newBuilder().setType(Feature.Type.LABEL_DETECTION).build();
    Feature landmarkFeature = Feature.newBuilder().setType(Feature.Type.LANDMARK_DETECTION).build();

    features.add(labelFeature);
    features.add(landmarkFeature);

    // iterate through the images to build the request
    int size = imagesAsByteArrays.size();

    for (int i = 0; i < size; i++){
      Image image = Image.newBuilder().setContent(ByteString.copyFrom(imagesAsByteArrays.get(i))).build();
      AnnotateImageRequest request =
        AnnotateImageRequest.newBuilder().addAllFeatures(features).setImage(image).build();
      requests.add(request);
    }

    // TODO: parallelize calls to detectLabelsFromImageBytes() since it requires a network call.

    // The invocation of batchAnnotateImages() makes a network call.
    BatchAnnotateImagesResponse batchResponse = imageAnnotatorClient.batchAnnotateImages(requests);
    List<AnnotateImageResponse> responses = batchResponse.getResponsesList();

    // TODO: parallelize calls to detectLabelsFromImageBytes() since it requires a network call.
    if (!(responses.size() == size)) {
      throw new IllegalArgumentException(
          "The number of responses is not equal to the number of requests.");
    }

    List<AnnotatedImage> annotatedImages = new ArrayList<AnnotatedImage>();

    // add an annotated image to list for every image received as byte array
    for (int i = 0; i < responses.size(); i++) {
      AnnotateImageResponse response = responses.get(i);

      if (response.hasError()) {
        throw new IOException(response.getError().getMessage());
      }

      // For full list of available annotations, see http://g.co/cloud/vision/docs
      List<EntityAnnotation> labels = response.getLabelAnnotationsList();
      List<EntityAnnotation> locations = response.getLandmarkAnnotationsList();

      annotatedImages.add(new AnnotatedImage(imagesAsByteArrays.get(i), labels, locations));
    }

    return annotatedImages;
  }
}
