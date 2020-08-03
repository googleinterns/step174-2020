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

package com.google.sps.story;

import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.IdTokenCredentials;
import com.google.auth.oauth2.IdTokenProvider;
import java.io.IOException;

/**
 * Object for building POST Requests to a GPT-2 Container.
 */

public class StoryManagerRequestFactoryImpl implements StoryManagerRequestFactory {
  private String requestBody;

  /**
   * Builds a PostRequest given parameters.
   *
   * @return HttpRequest Post Request
   */
  public HttpRequest buildPostRequest(String requestBody)
      throws IllegalArgumentException, IOException {
    try {
      String serviceUrl = "https://backstory-text-gen-pdaqhmzgva-uc.a.run.app";

      // Obtain Credentials
      GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();

      // Validate Credentials
      if (!(credentials instanceof IdTokenProvider)) {
        throw new IllegalArgumentException("Credentials are not an instance of IdTokenProvider.");
      }

      // Generate Authentication Token
      IdTokenCredentials tokenCredential = IdTokenCredentials.newBuilder()
                                               .setIdTokenProvider((IdTokenProvider) credentials)
                                               .setTargetAudience(serviceUrl)
                                               .build();

      // Configure URL
      GenericUrl genericUrl = new GenericUrl(serviceUrl);

      // Form Adapter with Authentication token
      HttpCredentialsAdapter adapter = new HttpCredentialsAdapter(tokenCredential);
      HttpTransport transport = new NetHttpTransport();
      this.requestBody = requestBody;
      return transport.createRequestFactory(adapter).buildPostRequest(
          genericUrl, ByteArrayContent.fromString("application/json", requestBody));
    } catch (IOException serverException) {
      throw new IOException("Error with server", serverException);
    }
  }

  /**
   * Gets the requestBody used to make Post Requests.
   *
   * @return String The post request header request body.
   */
  public String getRequestBody() {
    return requestBody;
  }
}
