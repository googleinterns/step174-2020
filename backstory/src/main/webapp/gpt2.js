// Copyright 2020 Google LLC
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
 * JS for GPT2 Page
 * features: display story
 */

import { createBackstoryLoadingElement } from './features/backstory-loading-element.js'

// export displayStory by making it global
window.displayStory = displayStory;

// DISPLAY STORY

/**
 * Sends request to text generation servlet and displays
 * result to front end.
 */
async function displayStory() {
  const input = document.getElementById('prompt').value;

  if (input === '' || input === null) {
    alert('You need to enter a value');
    return;
  }

  const inputObj = {text: input};
  const inputJSON = JSON.stringify(inputObj);

  createBackstoryLoadingElement('gpt2-story-display');

  // grab data and get its text version (it is sent as JSON)
  const response = await fetch('/gpt2', {
    method: 'post',
    headers: {'Content-Type': 'application/json'},
    body: inputJSON,
  });

  const data = await response.text();

  // get display
  const display = document.getElementById('gpt2-story-display');
  display.innerHTML = data;

  // parse the JSON into an object
  const jsonObject = JSON.parse(data);

  const ok = response.ok;  // checks for server error
  // properly format and display either the error message or the results
  if (!ok) {
    display.innerHTML = formatErrorMessage(jsonObject);
  } else {
    if (display.firstChild) display.firstChild.remove();

    display.appendChild(formatResponse(jsonObject));
  }
}

/**
 * Returns HTML formatting (simple paragraph tags) for error message
 *
 * @param {string} message - the error message to be shown
 * @return {string} - HTML formatting for error message
 */
function formatErrorMessage(message) {
  return `<p>${message}</p>`;
}

/**
 * Adds HTML formatting to JSON response to display generated text on page.
 *
 * @param {string} The text to display.
 */
function formatResponse(text) {
  const container = document.createElement('div');

  const generatedText = document.createElement('p');
  generatedText.id = 'outputText';

  generatedText.innerText = text;

  // add the approval to the larger container
  container.appendChild(generatedText);

  return container;
}
