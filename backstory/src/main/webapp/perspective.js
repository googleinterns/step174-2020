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
  const array = JSON.parse(json);
  console.log(array);
  const objOfAnalysis = array[0];
  console.log(objOfAnalysis)
  const decision = array[1];
  console.log(decision);
  
  // properly format and display HTML
  display.innerHTML = formatAttributeMap(objOfAnalysis.analyses, decision);
}

/**
 * @return {String} HTML formatting for attributes map
 */
function formatAttributeMap(attributes, decision) {
  let html = '';

  if(decision)
    html += '<p id="approval-status">Approved</p>';
  else
    html += '<p id="approval-status">Not approved</p>';

  html += '<table id="attribute-table">' +
      '<tr><th>Attribute Type</th><th class="score">Score</th></tr>';

  for (let [key, value] of Object.entries(attributes)) {
    html += `<tr>` +
        `<td>${formatType(key)}</td>` +
        `<td class="score" id="score-header">
          ${(value * 100).toFixed(3)}%</td>` +
        `</tr>`;
  }

  html += '</table>';

  return html;
}

/*
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
