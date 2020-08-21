// Copyright 2019 Google LLC
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

package com.google.sps.data;

/**
 * Represents a photo taken at a specific lat lng point.
 * This holds the file name of the image taken and the
 * title of that lat lng point.
 */
public class PhotoLocation {
  /** the name of the location where photo was taken */
  private String title;
  /** the latitude of the location where photo taken */
  private double lat;
  /** the longitude of the location where photo taken */
  private double lng;
  /** the name of the image file */
  private String img;

  public PhotoLocation(String title, double lat, double lng, String img) {
    this.title = title;
    this.lat = lat;
    this.lng = lng;
    this.img = img;
  }
}
