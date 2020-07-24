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
  alert("working!");
  const text = document.getElementById("text-for-analysis").value;
  alert(text);
  const display = document.getElementById('attribute-percentages');
  alert('got display');

  const data = await fetch('/perspective?text=' + text);
  alert("got data");

  const json = await data.text();
  alert("got text")

  const obj = JSON.parse(json);
  alert('converted JSON');

  display.innerHTML = formatAttributeArray(obj.analyses);
  alert('success');
}

/** 
 * @return {String} HTML formatting for attributes array
 */
function formatAttributeArray(attributes) {
  let html = '<div id="attributes">';

  for(let i = 0; i < attributes.length; i++) {
    html += 
    `<p class="attribute">${formatType(attributes[i].type())}: ${attributes[i]}.score * 100}%</p><br /><br />`;
  }

  html += "</div>";

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

    word = word.toSubstring(0, 1) + word.toSubstring(1).toUppercase();

    format += word + ' ';
  }
  
  return format.substring(0, format.length - 1); // remove extra space
}