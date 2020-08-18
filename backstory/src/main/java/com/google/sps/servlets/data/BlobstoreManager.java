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

import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobInfoFactory;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.sps.servlets.data.BlobstoreServiceConstantFields;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * Wrapper class for the blobstore service and all related operations.
 */
public class BlobstoreManager {
  private BlobstoreService blobstoreService;
  private BlobstoreServiceConstantFields blobstoreServiceConstantFields;
  private BlobInfoFactory blobInfoFactory;

  /**
   * Creates a blobstore manager object by calling the blobstore service factory
   */
  public BlobstoreManager() throws IOException {
    BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
    this.blobstoreService = blobstoreService;
    this.blobstoreServiceConstantFields = new BlobstoreServiceConstantFields(blobstoreService);
    this.blobInfoFactory = new BlobInfoFactory();
  }

  /**
   * Injection code for testing.
   * Creates a blobstore manager using a mock blobstoreService.
   */
  public BlobstoreManager(BlobstoreService blobstoreService, BlobstoreServiceConstantFields blobstoreServiceConstantFields, BlobInfoFactory blobInfoFactory) {
    this.blobstoreService = blobstoreService;
    this.blobstoreServiceConstantFields = blobstoreServiceConstantFields;
    this.blobInfoFactory = blobInfoFactory;
  }

  /**
   * Given a request and the form input element name get the image uploaded in the form as
   * a Blob Key in String form. This key is used to serve the picture back to the front-end.
   * @param request the HTTP request sent from the front-end form.
   * @param formInputElementName the name of the input element in the front-end form.
   * @return the image uploaded in the front-end input form, as a Blob Key in String form.
   */
  public String getUploadedFileBlobKeyString(
      HttpServletRequest request, String formInputElementName) {
    Map<String, List<BlobKey>> blobs = blobstoreService.getUploads(request);
    List<BlobKey> blobKeys = blobs.get(formInputElementName);

    // User submitted form without selecting a file, so we can't get a URL. (dev server)
    if (blobKeys == null || blobKeys.isEmpty()) {
      return null;
    }

    // Our form only contains a single file input, so get the first index.
    BlobKey blobKey = blobKeys.get(0);

    // User submitted form without selecting a file, so we can't get a URL. (live server)
    BlobInfo blobInfo = blobInfoFactory.loadBlobInfo(blobKey);
    if (blobInfo.getSize() == 0) {
      blobstoreService.delete(blobKey);
      return null;
    }

    String blobKeyString = blobKey.getKeyString();
    return blobKeyString;
  }

  /**
   * Given a request and the form input element name get the image uploaded in the form as
   * a byte array.
   * @param request the HTTP request sent from the front-end form.
   * @param formInputElementName the name of the input element in the front-end form.
   * @return the image uploaded in the front-end input form, as a byte array.
   */
  public byte[] getBlobBytes(HttpServletRequest request, String formInputElementName)
      throws IOException {
    Map<String, List<BlobKey>> blobs = blobstoreService.getUploads(request);
    List<BlobKey> blobKeys = blobs.get(formInputElementName);

    // User submitted form without selecting a file, so we can't get a URL. (dev server)
    if (blobKeys == null || blobKeys.isEmpty()) {
      return null;
    }

    // Our form only contains a single file input, so get the first index.
    BlobKey blobKey = blobKeys.get(0);

    // User submitted form without selecting a file, so we can't get a URL. (live server)
    BlobInfo blobInfo = blobInfoFactory.loadBlobInfo(blobKey);
    if (blobInfo.getSize() == 0) {
      blobstoreService.delete(blobKey);
      return null;
    }

    ByteArrayOutputStream outputBytes = new ByteArrayOutputStream();

    int fetchSize = blobstoreServiceConstantFields.getMaxBlobFetchSize();
    long currentByteIndex = 0;
    boolean continueReading = true;
    while (continueReading) {
      // end index is inclusive, so we have to subtract 1 to get fetchSize bytes
      byte[] bytesFromImage =
          blobstoreService.fetchData(blobKey, currentByteIndex, currentByteIndex + fetchSize - 1);
      outputBytes.write(bytesFromImage);

      // if we read fewer bytes than we requested, then we reached the end
      if (bytesFromImage.length < fetchSize) {
        continueReading = false;
      }

      currentByteIndex += fetchSize;
    }

    return outputBytes.toByteArray();
  }
}
