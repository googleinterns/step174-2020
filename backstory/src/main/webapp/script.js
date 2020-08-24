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
 * JS for Home page
 * features: validate form (& add loading animation) on submit of form, 
 * get blobstore url for the backstory form, retrieve analyzed images, 
 * and updates to front end when file uploaded.
 */

import { fetchBlobstoreUrl } from './features/fetch-blobstore-url.js';
import { validateImageUpload } from './features/image-validation.js';
import { createBackstoryLoadingElement } from './features/backstory-loading-element.js';
import { updateFileName } from './features/update-file-name.js';

// export methods by making them global
window.checkForm = checkForm;
window.fetchBlobstoreUrlForBackstory = fetchBlobstoreUrlForBackstory;
window.getAnalyzedImagesForBackstory = getAnalyzedImagesForBackstory;
window.uploadFileUpdates = uploadFileUpdates;

// VALIDATE FORM WITH IMAGE UPLOAD

/**
 * Check the form by validating image upload
 * and if it's valid return true and add
 * the loading graphic.
 *
 * @return true, if image is valid, false otherwise
 */
function checkForm() {
  if (!validateImageUpload('image-upload')) {
    return false;
  }

  createBackstoryLoadingElement('story-display');
  return true;
}


// FETCH BLOBSTORE URL

/**
 * Fetch the Blobstore URL by calling the method
 * for the 'photo-upload' form element.
 */
function fetchBlobstoreUrlForBackstory() {
  fetchBlobstoreUrl('photo-upload');
}

// RETRIEVE ANALYZED IMAGES

/**
 * This function interfaces with the back-end to get the user's photo upload
 * along, with the relevant backstory, from permanent storage. No analysis or
 * computation is done from this interface with the backend.
 */
function getAnalyzedImagesForBackstory() {
  fetch('/backstory')
      .then((response) => response.json())
      .then((backstoryObject) => {
        if (backstoryObject.length !== 0) {
          // Only support returning a single backstory at the moment
          const backstory = backstoryObject[0].backstory;

          fetch('/analyzed-images')
              .then((response) => response.blob())
              .then((blob) => {
                const storyDisplayElement =
                    document.getElementById('story-display');
                storyDisplayElement.innerHTML = '';
                const urlCreator = window.URL;
                const imageUrl = urlCreator.createObjectURL(blob);

                if (storyDisplayElement.childNodes.length === 1) {
                  storyDisplayElement.replaceChild(
                      createBackstoryElement(imageUrl, backstory),
                      storyDisplayElement.childNodes[0]);
                } else {
                  storyDisplayElement.appendChild(
                      createBackstoryElement(imageUrl, backstory));
                }
              });
        }
      });
}

/**
 * Helper function to format the image and backstory combination into one
 * element. This element and it's components have classes added to them for
 * front-end purposes - the classes can be found in the project style.css file.
 *
 * @param {string} imageUrl the url created to display the user-uploaded image.
 * @param {string} backstory the backstory text
 * @return {element} the HTML DOM element consisting of the uploaded image and
 *     the backstory text.
 */
function createBackstoryElement(imageUrl, backstory) {
  const imageElement = document.createElement('img');
  imageElement.src = imageUrl;

  const backstoryParagraphDiv = document.createElement('div');
  const backstoryParagraph = document.createElement('p');
  const backstoryText = document.createTextNode(backstory);
  backstoryParagraph.appendChild(backstoryText);
  backstoryParagraphDiv.appendChild(backstoryParagraph);
  backstoryParagraphDiv.classList.add('backstory-paragraph');

  const backstoryElement = document.createElement('div');
  backstoryElement.classList.add('backstory-element');
  backstoryElement.appendChild(imageElement);
  backstoryElement.appendChild(backstoryParagraphDiv);
  return backstoryElement;
}

// UPLOAD FILE CHANGES

/**
 * Updates the text of the file upload when a file is uploaded
 * and enables the submit button (starts as
 * a disabled button in HTML).
 */
function uploadFileUpdates() {
  updateFileNameForImageUpload();
  enableSubmitButton();
}

/**
 * Updates the file name using the imported method
 * for the image-upload and upload-label elements.
 */
function updateFileNameForImageUpload() {
  updateFileName('image-upload', 'upload-label');
}

/**
 * Enables the submit button if there's a valid image upload.
 */
function enableSubmitButton() {
  const submitButton = document.getElementById('submit-button');

  if (validateImageUpload('image-upload')) {
    submitButton.disabled = false;
  }
}
