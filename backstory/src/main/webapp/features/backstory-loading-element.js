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
 * JS for Backstory Loading Element Feature
 */

export { createBackstoryLoadingElement };

/**
 * Helper function to create and set a loading element to display after the
 * photo-upload form is submitted, while image is being analyzed and the
 * backstory is being created. Only execute this function if valid
 * image uploaded.
 * 
 * @param displayName - the name of the element to display the loading graphic in
 */
function createBackstoryLoadingElement(displayName) {
  const backstoryLoadingIcon = document.createElement('div');
  backstoryLoadingIcon.classList.add('backstory-loading');

  const backstoryLoadingParagraphDiv = document.createElement('div');
  const backstoryLoadingParagraph = document.createElement('p');
  const backstoryLoadingText =
      document.createTextNode('Your backstory is loading! Please be patient.');
  backstoryLoadingParagraph.appendChild(backstoryLoadingText);
  backstoryLoadingParagraphDiv.appendChild(backstoryLoadingParagraph);
  backstoryLoadingParagraphDiv.classList.add('backstory-loading-text');

  const backstoryLoadingElement = document.createElement('div');
  backstoryLoadingElement.classList.add('backstory-element');
  backstoryLoadingElement.appendChild(backstoryLoadingIcon);
  backstoryLoadingElement.appendChild(backstoryLoadingParagraphDiv);

  const storyDisplayElement = document.getElementById(displayName);
  if (storyDisplayElement.childNodes.length === 1) {
    storyDisplayElement.replaceChild(
        backstoryLoadingElement, storyDisplayElement.childNodes[0]);
  } else {
    storyDisplayElement.appendChild(backstoryLoadingElement);
  }
}