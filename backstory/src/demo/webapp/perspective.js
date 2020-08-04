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
 * JS for Perspective Page
 * features: display scores
 */

// DISPLAY SCORES

/** displays the Perspective scores for the text in the "text-for-analysis" element  */
/* eslint-disable no-unused-vars */
async function displayScores() {
  const input = document.getElementById('text-for-analysis').value;

  if (input === '' || input === null) {
    alert('You need to enter a value');
    return;
  }

  // get display
  const display = document.getElementById('attributes');

  // grab data and get its text version (it is sent as JSON)
  const data = await fetch('/perspective', {method: 'post', text: input});
  const ok = data.ok; // checks if status of response is an error
  const json = await data.text();

  // parse the JSON into an object
  const jsonObject = JSON.parse(json);

  // properly format and display either the error message or the attribute array
  if (!ok) {
    display.innerHTML = formatErrorMessage(jsonObject);
  } else {
    display.innerHTML = formatAttributeArray(jsonObject);
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
 * Formats an array of attributes into a HTML table.
 *
 * @param {object} attributes - an array of the values returned from the Perspective API
 * @return {string} - HTML table representing attributes array.
 */
function formatAttributeArray(attributes) {
  let html = '<table id="attribute-table">' +
      '<tr><th>Attribute Type</th><th class="score">Score</th></tr>';

  for (let i = 0; i < attributes.length; i++) {
    html += `<tr>` +
        `<td>${uppercaseSnakeCaseToTitleCase(attributes[i].type)}</td>` +
        `<td class="score" id="score-header">
          ${(attributes[i].score * 100).toFixed(3)}%</td>` +
        `</tr>`;
  }

  html += '</table>';

  return html;
}

/**
 * Converts a String from this format ("ATTACK_ON_AUTHOR") [aka all caps snakecase]
 * to this format ("Attack On Author") (title case except all words not just major words capitalized) 
 * by replacing underscores with spaces and changing the capitalization.
 *
 * @param {string} uppercaseSnakeCaseText - the type as an all caps snake case string
 * @return {string} - a formatted type String (in modified title case)
 */
function uppercaseSnakeCaseToTitleCase (uppercaseSnakeCaseText) {
  const words = uppercaseSnakeCaseText.split('_');
  let format = '';

  for (let i = 0; i < words.length; i++) {
    let word = words[i];

    word = word.substring(0, 1) + word.substring(1).toLowerCase();

    format += word + ' ';
  }

  return format.substring(0, format.length - 1);  // remove extra space
}
