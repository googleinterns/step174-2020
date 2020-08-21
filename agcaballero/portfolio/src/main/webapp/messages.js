// Copyright 2019 Google LLC
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

/* JS Functions for the Messages Page
 * Features: message display, message deletion
 */

// MESSAGE DISPLAY

/** Gets messages from comm tag and updates Messages page with it */
async function getMessages() {
  const data = await fetch('/message');
  const messages = await data.text();
  document.getElementById('message-display').innerHTML = messages;
}

// MESSAGE DELETION

/** Deletes the message from the display page */
async function deleteMessage(key) {
  // don't delete comment if user doesn't confirm
  if (!confirm('Do you want to delete this message?')) return;

  const request = new Request('/delete-message?key=' + key, {method: 'post'});
  await fetch(request);
  getMessages();
}
