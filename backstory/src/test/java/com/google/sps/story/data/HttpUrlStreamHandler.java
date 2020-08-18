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

package com.google.sps.story.data;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

/**
 * A URLStreamHandler class that lets us control the URLConnection
 * present for a given URL in order to test methods that
 * get responses from URLs. This class is for the purpose
 * of assisting in testing DatamuseRequestClient.
 * The code for this class was taken from this tutorial: 
 * https://claritysoftware.co.uk/mocking-javas-url-with-mockito/.
 * However, I wrote the Javadocs for the class.
 */
public class HttpUrlStreamHandler extends URLStreamHandler {
 
   /** the URLConnections to hold for this class */
  private Map<URL, URLConnection> connections = new HashMap();
 
  @Override
  protected URLConnection openConnection(URL url) throws IOException {
    return connections.get(url);
  }
 
  /** 
   * Clean out existing connections .
   */
  public void resetConnections() {
    connections = new HashMap();
  }
 
  /**
   * Add a URLConnection for a given url.
   *
   * @param url the url to add a connection for
   * @param urlConnection the connection to add for that url
   */
  public HttpUrlStreamHandler addConnection(URL url, URLConnection urlConnection) {
    connections.put(url, urlConnection);
    return this;
  }
}
