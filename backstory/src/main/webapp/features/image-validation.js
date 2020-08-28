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
 * JS for Image Validation Feature
 */

/* exported validateImageUpload */

export {validateImageUpload};

/**
 * Validate that the file uploaded to image upload
 * is an accepted image. Alert user to upload a new file if not.
 *
 * @param imageUploadElementID - the id of the image upload element to check
 * @return true, if valid image uploaded; false, if no image uploaded
 *      or if no valid image uploaded
 */
function validateImageUpload(imageUploadElementID) {
  const imageUpload = document.getElementById(imageUploadElementID);
  const files = imageUpload.files;

  if (files.length === 0) {
    alert('No file has been uploaded. Please upload a file.');
    return false;
  }

  if (!validImage(files.item(0))) {
    alert(
        'Only PNGs and JPGs are accepted image upload types. ' +
        'Please upload a PNG or JPG.');
    return false;
  }

  return true;
}

/**
 * Validate that the passed-in file is an
 * accepted image type (jpg or png).
 *
 * @param file - the file to validate
 * @return true, if file exists & is a valid image, false otherwise
 */
function validImage(file) {
  const acceptedImageTypes = ['image/jpeg', 'image/png'];

  return file && acceptedImageTypes.includes(file['type']);
}