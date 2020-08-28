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
 * JS for Updating File Name of a File UPload Label
 */

export {updateFileName};

/**
 * Updates the text of a file upload label to match the uploaded file
 * or {number of files uploaded} files selected, if multiple files in
 * the file upload element.
 *
 * @param fileUpload - the file upload that contains the file (or files) we're
 *     supposed
 *      be updating the labels to reflect
 * @param fileUploadLabel - the label to update with the file name
 */
function updateFileName(fileUpload, fileUploadLabel) {
  const fileInput = document.getElementById(fileUpload);
  const label = document.getElementById(fileUploadLabel);

  const files = fileInput.files;

  if (files) {
    const length = files.length;
    if (length > 1) {
      label.innerText = `${length} files selected`;
    } else {
      label.innerText = files.item(0).name;
    }
  }
}