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

import com.google.appengine.api.blobstore.BlobstoreService;

/**
 * Wrapper class for BlobstoreService which is used to access
 * any and all constant fields.
 */
public class BlobstoreServiceConstantFields {
  // This is the blobstore service connected to this wrapper class.
  private BlobstoreService blobstoreService;

  /**
   * Creates a  wrapper connected to a Blobstore Service.
   *
   * @param blobstoreService the blobstore service connected to this wrapper.
   */
  public BlobstoreServiceConstantFields(BlobstoreService blobstoreService) {
    this.blobstoreService = blobstoreService;
  }

  /**
   * Gets the max blob fetch size for this blobstore service.
   *
   * @return the max blob fetch size for the blobstore service, which is the max number
   * of bytes which we can fetch from any blob at once using this blobstore service.
   */
  public int getMaxBlobFetchSize() {
    return blobstoreService.MAX_BLOB_FETCH_SIZE;
  }
}
