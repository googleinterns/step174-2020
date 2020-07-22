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

/* eslint-disable no-unused-vars */

/* JS Functions for Comments Page
 * Features: comment display, comment deletion
 */

// COMMENT DISPLAY

/** Gets comments from data tag and updates "Comments" page with it */
async function getComments() {
  // initiate maxComments & displayLang and set defaults
  let maxComments = document.getElementById('max-comments').value;
  if (!maxComments) maxComments = 50;

  let displayLang = document.getElementById('display-lang').value;
  if (!displayLang) displayLang = 'en';

  const data = await fetch(
      '/data?max-comments=' + maxComments + '&display-lang=' + displayLang);
  const comments = await data.text();
  document.getElementById('comments-display').innerHTML = comments;
}

// COMMENT DELETION

/** Deletes comments from page and removes them */
async function deleteComments() {
  const request = new Request('/delete-data', {method: 'post'});
  await fetch(request);
  await getComments();
}
