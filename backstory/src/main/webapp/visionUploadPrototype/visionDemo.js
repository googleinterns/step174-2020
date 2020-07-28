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

/** Fetches the URL for uploading to Blobstore and adds it to the image upload form */
function fetchBlobstoreUrl() {
  fetch('/blobstore-upload-url')
      .then((response) => {
        return response.text();
      })
      .then((imageUploadUrl) => {
        const imageUploadForm = document.getElementById('image-upload');
        imageUploadForm.action = imageUploadUrl;
        // console.log(`Blobstore Upload: ${imageUploadUrl}`);
      });
}

/** Adds the  analyzed images to the image-list unordered list element */
function getAnalyzedImages() {
  fetch("/image-analysis")
      .then(response => response.json())
      .then((analyzedImagesObject) => {
        const imageListElement = document.getElementById('image-list');
        imageListElement.innerHTML = '';

        for (let i = 0; i < analyzedImagesObject.length; i++) {
          const imageUrl = analyzedImagesObject[i].imageUrl;
          const labelsJsonArray = analyzedImagesObject[i].labelsJsonArray;

          imageListElement.appendChild(
              createListElement(imageUrl, labelsJsonArray));
        }
      });
}

/** @return {Element} containing the analyzed image with its labels */
function createListElement(imageUrl, labelsJsonArray) {
  const listElementRow = document.createElement('div');
  listElementRow.classList.add("row");

  const listElement = document.createElement('div');
  listElement.classList.add("col", "flex-column", "d-flex", "justify-content-center");

  const imageElementDiv = document.createElement('div')
  const imageElement = document.createElement('img')
  imageElement.src = imageUrl;
  imageElement.classList.add("img-fluid");
  imageElementDiv.appendChild(imageElement);
  imageElementDiv.classList.add("d-flex", "justify-content-center");
  listElement.appendChild(imageElementDiv);

  const labelsElement = document.createElement('div');
  const labels = document.createTextNode(labelsJsonArray);
  labelsElement.appendChild(labels);
  listElement.appendChild(labelsElement);

  listElementRow.appendChild(listElement)
  return listElementRow;
}
