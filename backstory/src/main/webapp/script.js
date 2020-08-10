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
 * Fetches the URL for uploading to Blobstore and adds it to the image upload
 * form
 */
function fetchBlobstoreUrl() {
  fetch('/blobstore-upload-url')
      .then((response) => {
        return response.text();
      })
      .then((imageUploadUrl) => {
        const imageUploadForm = document.getElementById('photo-upload');
        imageUploadForm.action = imageUploadUrl;
      });
}

/** Adds the  analyzed images to the image-list unordered list element */
function getAnalyzedImages() {
  fetch('/analyzed-images')
      .then((response) => response.json())
      .then((analyzedImageObject) => {
        const storyDisplayElement = document.getElementById('story-display');
        storyDisplayElement.innerHTML = '';

        if (analyzedImageObject.length === 1) {
          const imageUrl = analyzedImageObject[0].imageUrl;
          const backstory = analyzedImageObject[0].backstory;

          console.log(analyzedImageObject);
          console.log(imageUrl);
          console.log(backstory);

          if (storyDisplayElement.childNodes.length === 1) {
            storyDisplayElement.replaceChild(
              createBackstoryElement(imageUrl, backstory), storyDisplayElement.childNodes[0]);
          } else {
            storyDisplayElement.appendChild(createBackstoryElement(imageUrl, backstory));
          }
        }
      });
}

/** @return {Element} containing the analyzed image with its story */
function createBackstoryElement(imageUrl, backstory) {  
  const imageElement = document.createElement('img');
  imageElement.src = imageUrl;

  const backstoryText = document.createTextNode(backstory);

  const backstoryElement = document.createElement('div');
  backstoryElement.appendChild(imageElement);
  backstoryElement.appendChild(backstoryText);
  return backstoryElement;
}