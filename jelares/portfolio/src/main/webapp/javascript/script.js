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
 * Changes the text of an element to a randomly chosen string
 * Side effect: switches the opacities of elementId + '1' and elementId + '2'
 * Unless both have zero, at which point it initializes elementId + '1' with
 * opacity 1 and elementId + '2' with opacity 0.
 *
 * @param {string[]} textChoices An array of strings from which the element text
 *     is randomly chosen.
 * @param {string} elementId The text is set on either elementId + '1' or
 *     elementId + '2'. The text is added to the
 * element which has zero opacity, then the element is given opacity 1  while
 * the other is given opacity 0. This emulates a fading effect with CSS
 * transitions.
 */
function addRandomText(textChoices, elementId) {
  /* Pick a random text. */
  const textChoice =
      textChoices[Math.floor(Math.random() * textChoices.length)];

  /* Add it to the page. */
  const textContainer1 = document.getElementById(elementId + '1');
  const textContainer2 = document.getElementById(elementId + '2');

  /*
      On page load both containers have 0 opacity and container1 will be chosen
     by default.
  */
  if (textContainer1.style.opacity === '0' &&
      textContainer2.style.opacity === '0') {
    textContainer1.innerText = textChoice;
    textContainer2.style.opacity = 0;
    textContainer1.style.opacity = 1;

    /*
        Case when container 1 currently has text and opacity 1.
    */
  } else if (textContainer1.style.opacity === '1') {
    textContainer2.innerText = textChoice;
    textContainer1.style.opacity = 0;
    textContainer2.style.opacity = 1;

    /*
        Case when container 2 currently has text and opacity 1.
    */
  } else {
    textContainer1.innerText = textChoice;
    textContainer2.style.opacity = 0;
    textContainer1.style.opacity = 1;
  }
}

/*
    Random Greeting
*/
function addRandomGreeting() {
  addRandomText(
      [
        'Hola',
        'Bonjour',
        'Hello',
        'Hej',
        'Hallo',
        'Greetings',
        'Hey',
        'Welcome',
      ],
      'greeting-container');
}

/*
    Random photo subjects
*/
function addRandomPhoto() {
  addRandomText(
      [
        'mountains',
        'nature',
        'the sea',
        'my pets',
        'cities',
        'people',
        'the world',
        'kindness',
        'the crazy',
        'the new',
        'the unbelievable',
      ],
      'photo-subject-container');
}

/*
    Random things which projects are for
*/
function addRandomProject() {
  addRandomText(
      [
        'us',
        'me',
        'you',
        'humanity',
        'friends',
        'family',
        'fun',
        'learning',
      ],
      'project-subject-container');
}

/*
    Random blog readers
*/
function addRandomBlog() {
  addRandomText(
      [
        'my parents',
        'my coworkers',
        'the fans',
        'workers',
        'dreamers',
        'my cat',
        'myself',
      ],
      'blog-subject-container');
}

/**
 * Creates parallax effect in the .parallax-landing elements on the page,
 * according to their attributes direction (either horizontal scrolling or
 * vertical) and rate (how fast the elements will scroll).
 */
function parallaxElements() {
  const targets = document.querySelectorAll('.parallax-landing');

  for (let index = 0; index < targets.length; index++) {
    const targetOffsetMiddle =
        targets[index].offsetTop + Math.floor(targets[index].clientHeight / 2);
    const windowOffsetMiddle =
        window.pageYOffset + Math.floor(window.innerHeight / 2);

    if (windowOffsetMiddle >= targetOffsetMiddle) {
      const pos = (windowOffsetMiddle - targetOffsetMiddle) *
          targets[index].dataset.rate;

      if (targets[index].dataset.direction === 'vertical') {
        targets[index].style.transform = `translate3d(0px, ${pos}px, 0px)`;
      } else {
        targets[index].style.transform = `translate3d(${pos}px, 0px, 0px)`;
      }

    } else {
      targets[index].style.transform = 'translate3d(0px, 0px, 0px)';
    }
  }
}

/**
 * Logic for lightbox gallery
 * Checks for the pictures with the selected class and
 * fades out all the other pictures in the gallery.
 */
$(document).ready(function() {
  let height = $('#gallery').height();
  $('#gallery').height(height);
  let gHeightToWidth = $('#gallery').height() / $('#gallery').width();

  $(function() {
    let selectedClass = '';

    $('.filter').click(function() {
      selectedClass = $(this).attr('data-rel');
      $('#gallery').fadeTo(100, 0.1);
      $('#gallery div')
          .not('.' + selectedClass)
          .fadeOut()
          .removeClass('animation');

      setTimeout(function() {
        $('.' + selectedClass).fadeIn().addClass('animation');
        $('#gallery').fadeTo(300, 1);
      }, 300);
    });
  });

  $(window).resize(function() {
    $('#gallery').height(gHeightToWidth * $('#gallery').width());
  });
});



/**
 * Logic for landing page parallax words effect: scrolling
 * logic
 */
window.addEventListener('scroll', function(e) {
  parallaxElements();
});
