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

package com.google.sps.servlets;

import com.google.sps.servlets.data.BlobstoreManager;
import com.google.sps.servlets.data.BlobstoreServiceConstantFields;
import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobInfoFactory;
import com.google.appengine.api.blobstore.BlobstoreService;
import org.junit.Assert;
import org.junit.Test;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import static org.mockito.Mockito.*;
import com.google.appengine.api.blobstore.BlobKey;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Tests for the blobstore service wrapper, BlobstoreManager
 */
@RunWith(MockitoJUnitRunner.class)
public final class BlobstoreManagerTest {
  /**
   * Tests the getUploadedFileBlobKeyString method of BlobstoreManager
   */
  @Test
  public void testGetUploadedFileBlobKeyString() {
    // Creates the required mocks for an HTTP request/response.
    HttpServletRequest mockRequest = mock(HttpServletRequest.class);

    BlobstoreService mockBlobstoreService = mock(BlobstoreService.class);
    BlobstoreServiceConstantFields mockBlobstoreServiceConstantFields = mock(BlobstoreServiceConstantFields.class);
    BlobInfoFactory mockBlobInfoFactory = mock(BlobInfoFactory.class);
    BlobInfo mockBlobInfo = mock(BlobInfo.class);

    String formInputElementName = "image-upload";
    String mockBlobkey = "mockBlobkey";
    Map<String, List<BlobKey>> mockInputToBlobkey = new HashMap<>();
    List<BlobKey> mockBlobKeys = new ArrayList<>();
    mockBlobKeys.add(new BlobKey(mockBlobkey));
    mockInputToBlobkey.put(formInputElementName, mockBlobKeys);

    when(mockBlobstoreService.getUploads(any(HttpServletRequest.class)))
        .thenReturn(mockInputToBlobkey);

    // Mock one image getting uploaded
    when(mockBlobInfo.getSize()).thenReturn(new Long(1));
    when(mockBlobInfoFactory.loadBlobInfo(any(BlobKey.class)))
        .thenReturn(mockBlobInfo);

    BlobstoreManager blobstoreManager = new BlobstoreManager(mockBlobstoreService, mockBlobstoreServiceConstantFields, mockBlobInfoFactory);
    Assert.assertEquals(mockBlobkey, blobstoreManager.getUploadedFileBlobKeyString(mockRequest, formInputElementName));
  }

  /**
   * Tests the getBlobBytes method of BlobstoreManager
   */
  @Test
  public void testGetBlobBytes() throws IOException {
    // Creates the required mocks for an HTTP request/response.
    HttpServletRequest mockRequest = mock(HttpServletRequest.class);

    BlobstoreService mockBlobstoreService = mock(BlobstoreService.class);
    BlobstoreServiceConstantFields mockBlobstoreServiceConstantFields = mock(BlobstoreServiceConstantFields.class);
    BlobInfoFactory mockBlobInfoFactory = mock(BlobInfoFactory.class);
    BlobInfo mockBlobInfo = mock(BlobInfo.class);

    byte[] mockImageByteArray = new byte[0];
    // Mock a max blob fetch size of 1
    when(mockBlobstoreServiceConstantFields.getMaxBlobFetchSize())
        .thenReturn(1);
    when(mockBlobstoreService.fetchData(
      any(BlobKey.class), anyLong(), anyLong()
    )).thenReturn(mockImageByteArray);

    String formInputElementName = "image-upload";
    String mockBlobkey = "mockBlobkey";
    Map<String, List<BlobKey>> mockInputToBlobkey = new HashMap<>();
    List<BlobKey> mockBlobKeys = new ArrayList<>();
    mockBlobKeys.add(new BlobKey(mockBlobkey));
    mockInputToBlobkey.put(formInputElementName, mockBlobKeys);

    when(mockBlobstoreService.getUploads(any(HttpServletRequest.class)))
        .thenReturn(mockInputToBlobkey);

    // Mock at least one image getting uploaded
    when(mockBlobInfo.getSize()).thenReturn(new Long(1));
    when(mockBlobInfoFactory.loadBlobInfo(any(BlobKey.class)))
        .thenReturn(mockBlobInfo);

    BlobstoreManager blobstoreManager = new BlobstoreManager(mockBlobstoreService, mockBlobstoreServiceConstantFields, mockBlobInfoFactory);
    Assert.assertEquals(0, blobstoreManager.getBlobBytes(mockRequest, formInputElementName).length);
  }
}
