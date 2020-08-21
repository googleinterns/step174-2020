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
 * Object for providing container URLs to StoryManager.
 */
public class StoryManagerURLProvider {
  /** serviceUrls - URLs for each story generation container */
  private String[] serviceURLs = {"https://backstory-text-gen-1-pdaqhmzgva-uc.a.run.app",
      "https://backstory-text-gen-2-pdaqhmzgva-uc.a.run.app",
      "https://backstory-text-gen-3-pdaqhmzgva-uc.a.run.app",
      "https://backstory-text-gen-4-pdaqhmzgva-uc.a.run.app",
      "https://backstory-text-gen-5-pdaqhmzgva-uc.a.run.app"};

  /** Index of URL to provide. */
  private int selectedURLIndex;

  /**
   * Initializes index for URL cycling.
   */
  public StoryManagerURLProvider() {
    selectedURLIndex = 0;
  }

  /**
   * Cycles to next serviceUrl to a backup url.
   * @return False if called on last URL.
   */
  synchronized public boolean cycleURL() {
    boolean isLast = true;
    if (selectedURLIndex == serviceURLs.length - 1) {
      isLast = false;
    }
    if (selectedURLIndex < serviceURLs.length) {
      selectedURLIndex++;
    } else {
      selectedURLIndex = 0;
    }
    return isLast;
  }

  /**
   * Returns the current URL as cycled.
   * @return The current url.
   */
  synchronized public String getCurrentURL() {
    return serviceURLs[selectedURLIndex];
  }
}
