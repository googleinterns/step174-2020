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

/* eslint-disable no-undef */
/* eslint-disable no-unused-vars */

/* JS Functions for Gallery Page
 * Features: travel map (with Maps integration)
 */

// TRAVEL MAP

/**
 * Creates a Map using Map API with preset markers that
 * show places I've taken photos at, their titles, and what photo
 * was taken there
 */
async function createPhotoLocationMap() {
  const europeLatLng =
      new google.maps.LatLng({lat: 48.499998, lng: 23.3833318});

  const map = new google.maps.Map(
      document.getElementById('map'), {center: europeLatLng, zoom: 4.5});

  // load the data for the photo markers
  const markers = await getPhotoMarkerData();

  // create and display all markers
  markers.forEach((marker) => {
    displayImageInfoMarker(
        map, marker.title, marker.lat, marker.lng, marker.img);
  });
}

/** Fetches the data for the photo markers on the map*/
async function getPhotoMarkerData() {
  const response = await fetch('photo-map-data');
  const photoMarkers = await response.json();

  return photoMarkers;
}

/**
 * Creates a marker that shows a read-only info window with
 * the title of the place and the image taken there when clicked.
 */
function displayImageInfoMarker(map, title, lat, lng, img) {
  // sets icon to a light red (custom color) filled-in downward-facing arrow
  const icon = {
    fillColor: '#e07a5f', /* terra cotta */
    fillOpacity: 1.0,
    path: google.maps.SymbolPath.BACKWARD_CLOSED_ARROW,
    scale: 4,
    strokeColor: '#e07a5f', /* terra cotta */
  };

  const marker = new google.maps.Marker(
      {title: title, position: {lat: lat, lng: lng}, map: map, icon: icon});

  const searchLink = constructSearchLink(title);

  const content = '<div class="header map-title"><h2>' +
      '<a target="_blank" href="' + searchLink + '">' + title +
      '</h2></a></div>' +
      '<div class="map-div flex-container">' +
      '<img class="map-img" src="images/travel/' + img + '" />' +
      '</div>';

  const infoWindow = new google.maps.InfoWindow({content: content});
  marker.addListener('click', () => {
    infoWindow.open(map, marker);
  });
}

/**
 * @return {String} a link from a title that will
 * link to the corresponding Google Search page
 */
function constructSearchLink(title) {
  // if it's null or false, return an empty string
  if (!title || title === '') {
    return '';
  }

  // link (without search terms) for a google search
  const searchLink = 'http://www.google.com/search?q=';

  let searchWord = title;

  // remove section of title after comma (if there is one)
  if (title.indexOf(',') != -1) {
    searchWord = title.substring(0, title.indexOf(','));
  }

  // splits into words then formats the search terms as connected
  // by a plus
  const words = searchWord.split(' ');
  const terms = words.join('+');
  return searchLink + terms;
}

/* Testing */

// test constructSearchLink() with a fake link
console.log(constructSearchLink('Fun Search Term, But Not This Part'));
