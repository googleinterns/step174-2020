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

/** Fetches the URL for uploading to Blobstore and adds it to the blog form */
function fetchBlobstoreUrl() {
  fetch('/blobstore-upload-url')
      .then((response) => {
        return response.text();
      })
      .then((imageUploadUrl) => {
        const messageForm = document.getElementById('blog-post');
        messageForm.action = imageUploadUrl;
      });
}

/** Adds the blog posts with content, titles, and images, to the blog-posts element */
function getPosts() {
  const numPostsElement = document.getElementById('num-posts');
  const numPosts = numPostsElement.value;

  fetch(`/blog-data?num-posts=${numPosts}`)
      .then(response => response.json())
      .then((posts) => {
        const postListElement = document.getElementById('blog-posts');
        postListElement.innerHTML = '';
        const postObjectArray = Object.values(posts)[0];

        for (let i = 0; i < postObjectArray.length; i++) {
          const content = postObjectArray[i].content;
          const title = postObjectArray[i].title;
          const imageUrl = postObjectArray[i].imageUrl;

          postListElement.appendChild(
              createListElement(content, title, imageUrl));
        }
      });
}

/** @return {Element} <li> A blog post element containing the blog post content, title, and image */
function createListElement(content, title, imageUrl) {
  const listElement = document.createElement('li');
  listElement.className = 'media py-3 bg-dark border border-white';

  const imageElement = document.createElement('img')
  imageElement.className = 'mx-3 img-thumbnail';
  imageElement.src = imageUrl;
  imageElement.width = 100;
  listElement.appendChild(imageElement);

  const mediaBodyElement = document.createElement('div');
  mediaBodyElement.className = 'media-body';

  const mediaBodyTitle = document.createElement('H1');
  mediaBodyTitle.className = 'mt-0';
  const blogTitleText = document.createTextNode(title);
  mediaBodyTitle.appendChild(blogTitleText)
  mediaBodyElement.appendChild(mediaBodyTitle);

  const mediaBodyContent = document.createElement('p');
  const blogContentText = document.createTextNode(content);
  mediaBodyContent.appendChild(blogContentText);
  mediaBodyElement.appendChild(mediaBodyContent);

  listElement.appendChild(mediaBodyElement);

  return listElement;
}
