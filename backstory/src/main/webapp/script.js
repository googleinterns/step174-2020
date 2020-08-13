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

/* JS for Home page 
 * features: get blobstore url, retrieve analyzed images,
 * create loading element, and update file upload label
 */

/* exported fetchBlobstoreUrl getAnalyzedImages createBackstoryLoadingElement */

// FETCH BLOBSTORE URL

/**
 * On load of the application, this function fetches the blobstore Url and sets
 * it as the action of the photo-upload form.
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

// RETRIEVE ANALYZED IMAGES

/**
 * This function interfaces with the back-end to get the user's photo upload
 * along, with the relevant backstory, from permanent storage. No analysis or
 * computation is done from this interface with the backend.
 */
function getAnalyzedImages() {
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

// CREATE LOADING ELEMENT

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

/**
 * Helper function to create and set a loading element to display after the
 * photo-upload form is submitted, while image is being analyzed and the
 * backstory is being created.
 */
function createBackstoryLoadingElement() {
  const backstoryLoadingIcon = document.createElement('div');
  backstoryLoadingIcon.classList.add('backstory-loading');

  const backstoryLoadingParagraphDiv = document.createElement('div');
  const backstoryLoadingParagraph = document.createElement('p');
  const backstoryLoadingText = document.createTextNode(
      'Your backstory is loading! Please be patient.');
  backstoryLoadingParagraph.appendChild(backstoryLoadingText);
  backstoryLoadingParagraphDiv.appendChild(backstoryLoadingParagraph);
  backstoryLoadingParagraphDiv.classList.add('backstory-paragraph');

  const backstoryLoadingElement = document.createElement('div');
  backstoryLoadingElement.classList.add('backstory-element');
  backstoryLoadingElement.appendChild(backstoryLoadingIcon);
  backstoryLoadingElement.appendChild(backstoryLoadingParagraphDiv);

  const storyDisplayElement = document.getElementById('story-display');
  if (storyDisplayElement.childNodes.length === 1) {
    storyDisplayElement.replaceChild(
        backstoryLoadingElement, storyDisplayElement.childNodes[0]);
  } else {
    storyDisplayElement.appendChild(backstoryLoadingElement);
  }
}

// UPDATE FILE UPLOAD LABEL

/**
 * Updates the text of the file upload label to match the uploaded file
 * or {number of files uploaded} files selected, if multiple files.
 */
function updateFileName() {
  const fileInput = document.getElementById('image-upload');
  const label = document.getElementById('upload-label');

  if (fileInput.files) {
    if (fileInput.files.length > 1)  {
      label.innerText = `${fileInput.files.length} files selected`;
    } else {
      label.innerText = fileInput.files.item(0).name;
    }
  }
} 
