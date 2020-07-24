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

package com.google.sps.servletData;

/** 
 * Represents a response to the login/logout link on the home page
 * Link will say LOGIN if the user is logged out, or LOGOUT if the user is logged in 
*/
public final class AuthenticationInformation {
  private final AuthenticationAction authenticationAction;
  private final String authenticationUrl;

  public AuthenticationInformation(AuthenticationAction authenticationAction, String authenticationUrl) {
    this.authenticationAction = authenticationAction;
    this.authenticationUrl = authenticationUrl;
  }

  /** @return the authentication action which the user could take right now, either login or logout */
  public AuthenticationAction getAuthenticationAction() {
    return authenticationAction;
  }

  /** @return the url to access the google authentication API for login/logout  */
  public String getAuthenticationUrl() {
    return authenticationUrl;
  }
}
