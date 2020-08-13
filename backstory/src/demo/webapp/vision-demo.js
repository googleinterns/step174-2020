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
 * features: fetch Blobstore URL, retrieve/format images and labels, 
 * display correct file upload
 */

 /* exported fetchBlobstoreUrl getAnalyzedImages updateFileName */

// FETCH BLOBSTORE URL

/**
 * Fetches the URL for uploading to Blobstore and adds it to the image upload form.
 */
function fetchBlobstoreUrl() {
  fetch('/blobstore-upload-url')
      .then((response) => {
        return response.text();
      })
      .then((imageUploadUrl) => {
        const imageUploadForm = document.getElementById('image-upload-form');
        imageUploadForm.action = imageUploadUrl;
      });
}

// RETRIEVE/FORMAT IMAGES & LABELS

/** Adds the analyzed images to the image-list unordered list element */
function getAnalyzedImages() {
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

          imageListElement.appendChild(
              createRow(imageUrl, labels));
        }
      });
}

/** 
 * Formats the image url and labels array into HTML
 *
 * @param {string} imageUrl - the url of the analyzed image
 * @param {object} labels - an array of labels & their other properties
 * @return {object} - an Element containing the analyzed image with its labels and scores 
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

  let index = 0;
  
  // add headers
  const header = table.insertRow(index);
  const labelHeader = document.createElement('th');
  labelHeader.innerText = 'Label';
  labelHeader.classList.add('labels-table-cell');
  const scoreHeader = document.createElement('th');
  scoreHeader.innerText = 'Score';
  scoreHeader.classList.add('labels-table-cell');

  header.appendChild(labelHeader);
  header.appendChild(scoreHeader);

  index++;

  for (let i = 0; i < labels.length; i++) {
    const label = labels[i];

    const row = table.insertRow(index);

    const labelData = row.insertCell(0);
    labelData.innerText = label.description;
    labelData.classList.add('labels-table-cell');

    const scoreData = row.insertCell(1);
    scoreData.innerText = `${(label.score * 100).toFixed(2)}%`;
    scoreData.classList.add('labels-table-cell');

    index++;
  }
  
  return table;
}

// DISPLAY CORRECT FILE NAME

/**
 * Updates the text of the file upload label to match the uploaded file
 * or {number of files uploaded} files selected, if multiple files.
 */
function updateFileName() {
  const fileInput = document.getElementById('image-upload');
  const label = document.getElementById('upload-visual');

  if (fileInput.files) {
    if (fileInput.files.length > 1)  {
      label.innerText = `${fileInput.files.length} files selected`;
    }
    else {
      label.innerText = fileInput.files.item(0).name;
    }
  }
}
