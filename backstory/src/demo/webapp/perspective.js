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
 * features: display analysis
 */

// DISPLAY ANALYSIS

/** displays the Perspective scores for the text in the "text-for-analysis" element  */
async function displayAnalysis() {
  /* eslint-disable no-unused-vars */
  const input = document.getElementById('text-for-analysis').value;

  if (input === '' || input === null) {
    alert('You need to enter a value');
    return;
  }

  console.log(input);

  // get display
  const display = document.getElementById('attributes');

  // grab data and get its text version (it is sent as JSON)
  const response = await fetch('/perspective', 
    {
      method: 'post', 
      headers: {'Content-Type': 'application/json'},
      body: `{text: ${input}}`,
    }
  );

  const ok = response.ok; // checks if status of response is an error
  const data = await response.text();
  
  console.log(ok);
  console.log(data);
  display.innerHTML = data;

  // parse the JSON into an object
  // if there was an error, should be a string error message 
  // if there wasn't an error, should be an array such that 
  // [decision (as boolean), scores (as map)]
  const jsonObject = JSON.parse(data);

  console.log(jsonObject);

  // properly format and display either the error message or the results from Perspective
  if (!ok) {
    display.innerHTML = formatErrorMessage(jsonObject);
  } else {
    if (display.firstChild) display.firstChild.remove();

    display.appendChild(formatResponse(jsonObject[0], jsonObject[1].attributeTypesToScores));
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
 * Adds HTML formatting to JSON response to display properly on page
 * Constructs a table to display the attributes & display the
 * decision from the JSON as "Approved" or "Disapproved" above table.
 * Returns an element which is a div containing all this formatting.
 *
 * @param {boolean} - decision the decision on whether it's appropriate
 * @param {object} - attributes a map of attribute scores and types
 * @return {object} - Element that displays attribute scores & decision
 */
function formatResponse(decision, attributes) {
  const container = document.createElement('div');

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
    typeData.innerText = uppercaseSnakeCaseToTitleCase(key);

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
