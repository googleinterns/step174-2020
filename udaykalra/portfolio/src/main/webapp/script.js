// Copyright 2019 Google LLC
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
/* eslint-disable no-undef */

const forValidate = false;

/**
 * Delete comments and re-get Data
 */
async function deleteCommentData() {
  await fetch(new Request('/delete-data', {method: 'post'}));
  await getCommentData;
  window.location.reload();
}

/** Creates an <li> element containing text. */
function createCommentListItem(inputText) {
  const listElement = document.createElement('li');
  listElement.innerText = inputText;
  return listElement;
}

/** Creates a map with marker at UC San Diego */
function createMap() {
  const map = new google.maps.Map(
      document.getElementById('map'),
      {center: {lat: 32.879838, lng: -117.232351}, zoom: 16});

  const schoolMarker = new google.maps.Marker({
    position: {lat: 32.879838, lng: -117.232351},
    map: map,
    title: 'UC San Diego',
  });

  if (forValidate) {
    console.log(schoolMarker);
  }
}

google.charts.load('current', {'packages': ['corechart']});
google.charts.setOnLoadCallback(drawChart);

/** Fetches skill data and uses it to create a chart. */
function drawChart() {
  fetch('/endorse-data')
      .then((response) => response.json())
      .then((skillVotes) => {
        const data = new google.visualization.DataTable();
        data.addColumn('string', 'Skill');
        data.addColumn('number', 'Votes');
        // The skillVotes variable is an array of objects that each have a
        // single key-value pair where the key is a string representing a skill
        // and the value is a number representing the number of endorsements
        // received for that skill.
        Object.keys(skillVotes).forEach((skillVote) => {
          data.addRow([skillVote, skillVotes[skillVote]]);
        });

        const options = {
          'title': 'Endorse a Skill!',
          'width': 600,
          'height': 500,
          'alignment': 'center',
        };

        const chart = new google.visualization.ColumnChart(
            document.getElementById('chart-container'));
        chart.draw(data, options);
      });
}

/**
 * Fetches comments for display.
 */
function getCommentData(commentsLimit = 20) {
  fetch('/data?comment-count=' + commentsLimit)  // sends a request to /data
      .then((response) => response.json())       // parses the response as JSON
      .then((comments) => {  // now we can reference the object
        const commentsElement = document.getElementById('quote-container');
        commentsElement.innerHTML = '';
        for (let index = 0; index < comments.length; index += 1) {
          console.log(comments);
          commentsElement.appendChild(createCommentListItem(comments[index]));
        }
      });
}

/* Function calls to aid validation */
if (forValidate) {
  deleteCommentData();
  createMap();
}
