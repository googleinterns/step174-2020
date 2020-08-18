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
  private BlobstoreService blobstoreService;

  /**
   * Creates a  wrapper connected to a Blobstore Service or mock.
   */
  public BlobstoreServiceConstantFields(BlobstoreService blobstoreService) {
    this.blobstoreService = blobstoreService;
  }

  /** Returns the max blob fetch size for the blobstore service */
  public int getMaxBlobFetchSize() {
    return blobstoreService.MAX_BLOB_FETCH_SIZE;
  }
}
