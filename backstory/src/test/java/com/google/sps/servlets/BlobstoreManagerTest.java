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
  // All necessary mocks and objects for the test calls themselves
  private BlobstoreService mockBlobstoreService;
  private BlobstoreServiceConstantFields mockBlobstoreServiceConstantFields;
  private BlobInfoFactory mockBlobInfoFactory;
  private HttpServletRequest mockRequest;
  private final String formInputElementName = "image-upload";
  private final String mockBlobKeyString = "mockBlobKeyString";

  /**
   * Tests the getUploadedFileBlobKeyString method of BlobstoreManager
   */
  @Test
  public void testGetUploadedFileBlobKeyString() throws IOException {
    mockRequest = mock(HttpServletRequest.class);
    mockBlobstoreService = mock(BlobstoreService.class);
    mockBlobstoreServiceConstantFields = mock(BlobstoreServiceConstantFields.class);
    mockBlobInfoFactory = mock(BlobInfoFactory.class);

    BlobInfo mockBlobInfo = mock(BlobInfo.class);
    Map<String, List<BlobKey>> mockInputToBlobKey = new HashMap<>();
    List<BlobKey> mockBlobKeyStrings = new ArrayList<>();
    mockBlobKeyStrings.add(new BlobKey(mockBlobKeyString));
    mockInputToBlobKey.put(formInputElementName, mockBlobKeyStrings);

    when(mockBlobstoreService.getUploads(any(HttpServletRequest.class)))
        .thenReturn(mockInputToBlobKey);

    // BlobInfo's size attribute represents the size (in bytes) of the blob uploaded. If it is 0,
    // then an empty form must have been submitted.
    Long uploadedFileSize = new Long(1);
    when(mockBlobInfo.getSize()).thenReturn(uploadedFileSize);
    when(mockBlobInfoFactory.loadBlobInfo(any(BlobKey.class)))
        .thenReturn(mockBlobInfo);

    BlobstoreManager blobstoreManager = new BlobstoreManager(mockBlobstoreService, mockBlobstoreServiceConstantFields, mockBlobInfoFactory);
    Assert.assertEquals(mockBlobKeyString, blobstoreManager.getUploadedFileBlobKeyString(mockRequest, formInputElementName));
  }

  /**
   * Tests the getBlobBytes method of BlobstoreManager
   */
  @Test
  public void testGetBlobBytes() throws IOException {
    mockRequest = mock(HttpServletRequest.class);
    mockBlobstoreService = mock(BlobstoreService.class);
    mockBlobstoreServiceConstantFields = mock(BlobstoreServiceConstantFields.class);
    mockBlobInfoFactory = mock(BlobInfoFactory.class);

    BlobInfo mockBlobInfo = mock(BlobInfo.class);
    Map<String, List<BlobKey>> mockInputToBlobKey = new HashMap<>();
    List<BlobKey> mockBlobKeyStrings = new ArrayList<>();
    mockBlobKeyStrings.add(new BlobKey(mockBlobKeyString));
    mockInputToBlobKey.put(formInputElementName, mockBlobKeyStrings);

    when(mockBlobstoreService.getUploads(any(HttpServletRequest.class)))
        .thenReturn(mockInputToBlobKey);

    // BlobInfo's size attribute represents the size (in bytes) of the blob uploaded. If it is 0,
    // then an empty form must have been submitted. We will assume our image consists of 5 bytes.
    Long uploadedFileSize = new Long(5);
    when(mockBlobInfo.getSize()).thenReturn(uploadedFileSize);
    when(mockBlobInfoFactory.loadBlobInfo(any(BlobKey.class)))
        .thenReturn(mockBlobInfo);

    // This represents the image uploaded, as an array of 5 bytes.
    byte[] mockImageByteArray = new byte[uploadedFileSize.intValue()];
    // The max blob fetch size represents how many bytes of data a call to BlobstoreService's fetchData
    // method can return at most (in bytes). We will assume this is set to 10 bytes.
    int maxBlobFetchSize = 10;
    when(mockBlobstoreServiceConstantFields.getMaxBlobFetchSize())
        .thenReturn(maxBlobFetchSize);
    when(mockBlobstoreService.fetchData(
      any(BlobKey.class), anyLong(), anyLong()
    )).thenReturn(mockImageByteArray);

    BlobstoreManager blobstoreManager = new BlobstoreManager(mockBlobstoreService, mockBlobstoreServiceConstantFields, mockBlobInfoFactory);
    Assert.assertEquals(uploadedFileSize.intValue(), blobstoreManager.getBlobBytes(mockRequest, formInputElementName).length);
  }
}
