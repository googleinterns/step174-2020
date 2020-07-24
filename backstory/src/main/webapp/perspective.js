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

/** displays the Perspective scores for the given text  */
async function displayScores() {
  console.log('working!');

  const text = document.getElementById('text-for-analysis').value;
  
  if(text === '' || text === null) {
    alert('You need to enter a value');
    return;
  }

  const display = document.getElementById('attributes');
  console.log('got display');

  const data = await fetch('/perspective?text=' + text);
  console.log('got data');

  const json = await data.text();
  console.log(json);

  const obj = JSON.parse(json);
  console.log('converted JSON');

  console.log(formatAttributeArray(obj.analyses));
  display.innerHTML = formatAttributeArray(obj.analyses);
  console.log('success');
}

/** 
 * @return {String} HTML formatting for attributes array
 */
function formatAttributeArray(attributes) {
  let html = '<table id="attribute-table">' +
      '<tr><th>Attribute Type</th><th class="score">Score</th></tr>';

  for(let i = 0; i < attributes.length; i++) {
    html += 
    `<tr>` +
    `<td>${formatType(attributes[i].type)}</td>` +
    `<td class="score" id="score-header">${(attributes[i].score * 100).toFixed(3)}%</td>` +
    `</tr>`;
  }

  html += "</table>";

  return html;
}

/*
 * Converts a String from this format ("ATTACK_ON_AUTHOR") 
 * to this format ("Attack On Author") by replacing underscores with spaces
 *
 * @return {String} a formatted type String
 */
function formatType(type) {
  let words = type.split('_');
  let format = '';

  for(let i = 0; i < words.length; i++) {
    let word = words[i];

    word = word.substring(0, 1) + word.substring(1).toLowerCase();

    format += word + ' ';
  }
  
  return format.substring(0, format.length - 1); // remove extra space
}