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

/** Fetches comments and adds them to the DOM. */
function getComments() {
  const numCommentsElement = document.getElementById('num-comments');
  const numComments = numCommentsElement.value;

  /* This is the part about the query string which I mentioned in the sync */
  fetch(`/data?num-comments=${numComments}`)
      .then(response => response.json())
      .then((comments) => {
        const commentListElement = document.getElementById('comment-list');
        commentListElement.innerHTML = '';
        const commentStrings = Object.values(comments)[0];

        for (let i = 0; i < commentStrings.length; i++) {
          commentListElement.appendChild(
              createListElement(`Comment ${i}: ${commentStrings[i]}`));
        }
      });
}

/** Requests to delete all comments */
function deleteComments() {
  /* Delete the comments then return the blank comments */
  fetch('/delete-data', {
    method: 'POST',
  }).then(getComments);
}

/** Creates an <li> element containing text. */
function createListElement(text) {
  const listElement = document.createElement('li');
  listElement.innerText = text;
  return listElement;
}
