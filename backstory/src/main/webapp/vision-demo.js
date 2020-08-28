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
 * JS for Vision Demo Page
 * features: fetch Blobstore URL, retrieve/format images and labels for demo,
 * validate vision demo image upload, display correct file upload
 */

import {fetchBlobstoreUrl} from './features/fetch-blobstore-url.js';
import {validateImageUpload} from './features/image-validation.js';
import {updateFileName} from './features/update-file-name.js';

// export methods by making them global
window.fetchBlobstoreUrlForDemo = fetchBlobstoreUrlForDemo;
window.getAnalyzedImagesForDemo = getAnalyzedImagesForDemo;
window.validateVisionImageUpload = validateVisionImageUpload;
window.updateFileNameForDemo = updateFileNameForDemo;

// FETCH BLOBSTORE URL
/**
 * Fetch blobstore url and set it by calling the
 * fetchBlobstoreUrl() method for the Vision Demo
 * image-upload-form element
 */
function fetchBlobstoreUrlForDemo() {
  fetchBlobstoreUrl('image-upload-form');
}

// RETRIEVE/FORMAT IMAGES & LABELS

/**
 * Adds the analyzed images to the image-list unordered list element
 */
function getAnalyzedImagesForDemo() {
  fetch('/analyzed-images')
      .then((response) => response.json())
      .then((analyzedImagesObject) => {
        const imageListElement = document.getElementById('image-list');
        imageListElement.innerHTML = '';

        for (let i = 0; i < analyzedImagesObject.length; i++) {
          const imageUrl = analyzedImagesObject[i].imageUrl;
          const labelsJsonArray = analyzedImagesObject[i].labelsJsonArray;

          // parse the labels json and pass it into the formatting array
          const labelsObj = JSON.parse(labelsJsonArray);
          const labels = labelsObj.labels;

          imageListElement.appendChild(createRow(imageUrl, labels));
        }
      });
}

/**
 * Formats the image url and labels array into HTML
 *
 * @param {string} imageUrl - the url of the analyzed image
 * @param {object} labels - an array of labels & their other properties
 * @return {object} - an Element containing the analyzed image with its labels
 *     and scores
 */
function createRow(imageUrl, labels) {
  const row = document.createElement('div');
  row.classList.add('row');
  row.classList.add('horizontal-flex-container');

  const imageDiv = document.createElement('div');
  imageDiv.classList.add('image-div');

  // create a div to hold the image for this
  const image = document.createElement('img');
  image.src = imageUrl;
  image.classList.add('image');

  // add image to its div, and add this div to container (row)
  imageDiv.appendChild(image);
  row.appendChild(imageDiv);

  // create divs to hold labels
  const labelsDiv = document.createElement('div');
  labelsDiv.classList.add('labels-div');
  const labelsTable = formatLabelsAsTable(labels);
  labelsTable.id = 'labels-table';

  // add labels to row
  labelsDiv.appendChild(labelsTable);
  row.appendChild(labelsDiv);

  return row;
}

/**
 * Format labels as an HTML table.
 *
 * @param {object} labels - an array of labels (as JSON)
 * @return {object} - a table representing the labels array
 */
function formatLabelsAsTable(labels) {
  const table = document.createElement('table');
  table.classList.add('labels-table');

  let rowNumber = 0;

  // add headers
  const header = table.insertRow(rowNumber);
  const labelHeader = document.createElement('th');
  labelHeader.innerText = 'Label';
  labelHeader.classList.add('labels-table-cell');
  const scoreHeader = document.createElement('th');
  scoreHeader.innerText = 'Score';
  scoreHeader.classList.add('labels-table-cell');

  header.appendChild(labelHeader);
  header.appendChild(scoreHeader);


  for (let i = 0; i < labels.length; i++) {
    rowNumber++;

    const label = labels[i];

    const row = table.insertRow(rowNumber);

    const labelData = row.insertCell(0);
    labelData.innerText = label.description;
    labelData.classList.add('labels-table-cell');

    const scoreData = row.insertCell(1);
    scoreData.innerText = `${(label.score * 100).toFixed(2)}%`;
    scoreData.classList.add('labels-table-cell');
  }

  return table;
}

// VALIDATE VISION IMAGE UPLOAD

/**
 * Validate the image upload by calling validateImageUpload
 * on the image-upload element.
 */
function validateVisionImageUpload() {
  return validateImageUpload('image-upload');
}


// DISPLAY CORRECT FILE NAME

/**
 * Updates the text of the file upload label to match
 * the uploaded file for the 'image-upload' file upload
 * and 'upload-visual' label.
 */
function updateFileNameForDemo() {
  updateFileName('image-upload', 'upload-visual');
}
