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

/* eslint-disable no-unused-vars */

/**
 * JS for Perspective Page
 * features: display scores
 */

// DISPLAY SCORES

/** displays the Perspective scores for the given text  */
async function displayScores() {
  const text = document.getElementById('text-for-analysis').value;

  if (text === '' || text === null) {
    alert('You need to enter a value');
    return;
  }

  // get display
  const display = document.getElementById('attributes');

  // grab data and get its text version (it is sent as JSON)
  const data = await fetch('/perspective?text=' + text);
  const json = await data.text();

  // parse the JSON into an object
  const jsonResult = JSON.parse(json);

  // properly format and display either the error message or the attribute array
  if(typeof(jsonResult) === "string") {
    display.innerHTML = formatErrorMessage(jsonResult);
  } else {
    display.innerHTML = formatAttributeArray(jsonResult.analyses);
  }
}

/**
 * @return {String} HTML formatting for error message
 */
function formatErrorMessage(message) {
  return `<p>${message}</p>`;
}

/**
 * @return {String} HTML formatting for attributes array
 */
function formatAttributeArray(attributes) {
  let html = '<table id="attribute-table">' +
      '<tr><th>Attribute Type</th><th class="score">Score</th></tr>';

  for (let i = 0; i < attributes.length; i++) {
    html += `<tr>` +
        `<td>${formatType(attributes[i].type)}</td>` +
        `<td class="score" id="score-header">
          ${(attributes[i].score * 100).toFixed(3)}%</td>` +
        `</tr>`;
  }

  html += '</table>';

  return html;
}

/** 
 * Converts a String from this format ("ATTACK_ON_AUTHOR")
 * to this format ("Attack On Author") by replacing underscores with spaces
 *
 * @return {String} a formatted type String
 */
function formatType(type) {
  const words = type.split('_');
  let format = '';

  for (let i = 0; i < words.length; i++) {
    let word = words[i];

    word = word.substring(0, 1) + word.substring(1).toLowerCase();

    format += word + ' ';
  }

  return format.substring(0, format.length - 1);  // remove extra space
}
