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

/**
 * Fetches the login URL if the user is not logged in or logout URL if the user
 * is logged in, and adds the URL to the login link on the navbar
*/
function fetchLogUrl() {
  fetch('/authentication-url')
      .then(response => response.json())
      .then((logInformationObject) => {
        const authenticationUrl = logInformationObject.authenticationUrl;
        const authenticationAction = logInformationObject.authenticationAction;

        const navbarLogLink = document.getElementById('navbar-login-link');
        navbarLogLink.href = authenticationUrl;
        navbarLogLink.innerText = authenticationAction;
      });
}
