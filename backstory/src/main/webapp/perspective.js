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

  // parse the JSON into an array of results
  const jsonResults = JSON.parse(json);
  // gets the object JSON as textAnalysis
  const textAnalysis = jsonResults[0];
  console.log(typeof (textAnalysis));
  // get boolean json as decision
  const decision = jsonResults[1];
  console.log(typeof (decision));

  // properly format and display HTML
  display.innerHTML = formatAttributeMap(textAnalysis.analyses, decision);
}

/**
 * Adds HTML formatting to JSON response to display properly on page
 * Constructs a table to display the attributes & displayed the
 * decision from the JSON as "Approved" or "Disapproved" above table.
 *
 * @param {}
 * @param {}
 * @return {string} HTML formatting for attributes map
 */
function formatAttributeMap(attributes, decision) {
  let html = '<p id="approval-status">';

  if (decision) {
    html += 'Approved';
  } else {
    html += 'Not approved';
  }

  html += '</p>';

  // add table to display the attribute types
  html += '<table id="attribute-table">' +
      '<tr><th>Attribute Type</th><th class="score">Score</th></tr>';

  for (const [key, value] of Object.entries(attributes)) {
    html += `<tr>` +
        `<td>${formatType(key)}</td>` +
        `<td class="score" id="score-header">` +
        `${(value * 100).toFixed(3)}%</td>` +
        `</tr>`;
  }

  html += '</table>';

  return html;
}

/**
 * Converts a String from this format (of an AttributeType enum)
 * ("ATTACK_ON_AUTHOR") to this format ("Attack On Author") by replacing
 * underscores with spaces
 *
 * @param {string} type a String that holds the enum of an AttributeType (e.g.
 *     "TOXICITY" or "ATTACK_ON_COMMENTER")
 * @return {string} a formatted type String (e.g. "Toxicity" or "Attack on
 *     Commenter")
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
