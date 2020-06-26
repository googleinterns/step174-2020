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

/* Returns the link to a random post on the blog */
function randomPost() {
  // array with all post links
  const posts = ['#post1-lnk', '#post2-lnk', '#post3-lnk', '#post4-lnk'];

  // returns a random link
  return posts[Math.floor(Math.random() * posts.length)];
}

/* Tests randomPost() by verifying it spits out only one of the random post
options & logging the percentage of times it returns each */
function testRandomPost(trials = 10000) {
  const LENGTH = 4;
  const sums = [0, 0, 0, 0];

  for (let i = 0; i < trials; i++) {
    switch (randomPost()) {
      case '#post1-lnk':
        sums[0]++;
        break;
      case '#post2-lnk':
        sums[1]++;
        break;
      case '#post3-lnk':
        sums[2]++;
        break;
      case '#post4-lnk':
        sums[3]++;
        break;
      default:
        console.log('Illegal response found.');
        return;
    }
  }

  /* Logs percentage of times each one occurred; should be ~ 25% */
  for (let i = 0; i < LENGTH; i++) {
    console.log(`${i}: ${sums[i] / trials * 100}%`);
  }
}

testRandomPost(10000);  // runs 10000 trials on randomPost()
