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

/**
 * Object for building mock POST Requests for testing purposes..
 */
public class StoryManagerRequestFactoryFakeImpl implements StoryManagerRequestFactory {
  HttpRequest request;
  private String lastRequestBody;

  /**
   * Sets request to output from factory.
   *
   * @param inputRequest HttpRequest to be sent from factory.
   */
  public void setRequest(HttpRequest request) {
    this.request = request;
  }

  /**
   * Builds a PostRequest given parameters.
   *
   * @return HttpRequest Post Request
   */
  public HttpRequest newInstance(String requestBody) {
    this.lastRequestBody = requestBody;
    return request;
  }

  /**
   * Gets the requestBody used to make Post Requests.
   *
   * @return String The post request header request body.
   */
  public String getLastRequestBody() {
    return lastRequestBody;
  }
}
