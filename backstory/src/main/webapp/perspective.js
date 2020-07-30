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
 * features: display Perspective analysis
 */

// DISPLAY PERSPECTIVE ANALYSIS

/** displays the Perspective scores & decision for the given text  */
async function displayPerspectiveAnalysis() {
  const text = document.getElementById('text-for-analysis').value;

  if (text === '' || text === null) {
    alert('You need to enter a value');
    return;
  }

  // grab data and get its text version (it is sent as JSON)
  const data = await fetch('/perspective?text=' + text);
  const json = await data.text();
  
  // parse the JSON into an object
  const jsonResult = JSON.parse(json);

  // get display
  const display = document.getElementById('attributes');

  // properly format and display either the error message or the Perspective analysis
  if (typeof (jsonResult) === 'string') {
    display.innerHTML = formatErrorMessage(jsonResult);
  }

  // gets the object JSON as analysis
  const analysis = jsonResult;
  
  // if display has a child (e.g. if a table has already been appended, remove it
  if (display.firstChild)
    display.firstChild.remove();

  display.appendChild(formatResponse(analysis.analyses, analysis.decision));
}

/**
 * @return {string} HTML formatting for error message
 */
function formatErrorMessage(message) {
  return `<p>${message}</p>`;
}

/**
 * Adds HTML formatting to JSON response to display properly on page
 * Constructs a table to display the attributes & display the
 * decision from the JSON as "Approved" or "Disapproved" above table.
 * Returns an element which is a div containing all this formatting.
 *
 * @param {object} attributes a map of attribute scores and types
 * @param {boolean} decision the decision on whether it's appropriate
 * @return {object} Element that displays attribute scores & decision
 */
function formatResponse(attributes, decision) {
  const container = document.createElement("div");

  const approval = document.createElement('p');
  approval.id = 'approval-status';

  if (decision) {
    approval.innerText = 'Approved';
  } else {
    approval.innerText = 'Not approved';
  }

  // add the approval to the larger container
  container.appendChild(approval);

  const table = document.createElement('table');
  table.id = 'attribute-table';

  let index = 0;

  // create a header & add appropriate titles
  const header = table.insertRow(index);
  const typeHeader = document.createElement('th');
  typeHeader.innerText = 'Attribute Type';
  const scoreHeader = document.createElement('th');
  scoreHeader.innerText = 'Attribute Score';

  header.appendChild(typeHeader);
  header.appendChild(scoreHeader);

  index++;

  // construct the rows of the table using the data from attributes
  for (const [key, value] of Object.entries(attributes)) {
    const row = table.insertRow(index);
    
    // create data element for type and set its text
    const typeData = row.insertCell(0);
    typeData.innerText = formatType(key);

    // create data element for score, set its text, and add a class type for it
    const scoreData = row.insertCell(1);
    scoreData.classList.add('score');
    scoreData.innerText = (value * 100).toFixed(3);

    index++;
  }
  
  container.append(table);

  return container;
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
