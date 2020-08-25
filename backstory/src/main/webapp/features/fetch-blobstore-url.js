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

/* 
 * JS for Fetching Blobstore URL
 */

export { fetchBlobstoreUrl };

/**
 * On load of the application, this function fetches the blobstore Url and sets
 * it as the action of the form with the passed-in name.
 *
 * @param formName - the name of the form to set the action for
 */
function fetchBlobstoreUrl(formName) {
  fetch('/blobstore-upload-url')
      .then((response) => {
        return response.text();
      })
      .then((imageUploadUrl) => {
        const imageUploadForm = document.getElementById(formName);
        imageUploadForm.action = imageUploadUrl;
      });
}