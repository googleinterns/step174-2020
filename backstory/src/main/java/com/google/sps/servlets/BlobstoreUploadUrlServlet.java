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

import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Writes a Blobstore Upload URL to the response:
 *
 * blobstoreService.createUploadUrl("/analyze-image") creates an intermediate URL (uploadUrl) which
 * encodes the file upload, then forwards the encoded file, as a blob object, in the request to the
 * parameter url
 * ("/analyze-image" in this case). To see the intermediate blobstore URL, try:
 * System.out.println(uploadUrl)
 *
 * From the response sent, to access the explicit blob keys of the encoded file, from the blob
 * object in the request, try: Map<String, List<BlobKey>> blobs =
 * blobstoreService.getUploads(request); List<BlobKey> blobKeys = blobs.get(formInputElementName);
 *
 * Where formInputElementName is the name attribute for the file input in the original HTML form.
 * From blobKeys, you can access the keys for any of the files uploaded. Then with ImagesService or
 * with Javascript, you can build the image from the blobkey, and get the url for it.
 */
@WebServlet("/blobstore-upload-url")
public class BlobstoreUploadUrlServlet extends HttpServlet {
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
    String uploadUrl = blobstoreService.createUploadUrl("/analyze-image");

    response.setContentType("text/html");
    response.getWriter().println(uploadUrl);
  }
}
