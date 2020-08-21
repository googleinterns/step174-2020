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

package com.google.sps.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Class containing a numPosts-large batch of posts */
public final class PostBatch {
  private final List<String> posts;

  /**
   * This Abstract Data Type represents a mutable list of posts.
   *
   * Abstraction Function(posts) =  list of posts such that posts.get(i)
   *                                represents post i where each post is a string of text.
   * Representation Invariant: posts.size() == number of posts
   * Saftey from Rep Exposure: no mutable fields are never returned, new posts only added through
   * mutator methods.
   */

  public PostBatch(List<String> posts) {
    this.posts = new ArrayList<>(posts);
  }

  /** Get the number of posts */
  public int getNumPosts() {
    return posts.size();
  }

  /** Get all posts as a string array */
  public String[] getPostArray() {
    String[] postsArray = new String[posts.size()];

    for (int i = 0; i < posts.size(); i++) {
      postsArray[i] = posts.get(i);
    }

    return postsArray;
  }

  /** Get all posts as an immutable list */
  public List<String> getPostList() {
    return Collections.unmodifiableList(posts);
  }

  /**
   * Get the ith example post
   *
   * @param postIndex the number of the post to return, must be <= posts.size()
   * @return the post in posts with index postIndex
   */
  public String getPost(int postIndex) {
    return posts.get(postIndex);
  }

  /**
   * Add a new post to the list
   *
   * @param post the post to add
   */
  public void addPost(String post) {
    posts.add(post);
  }
}
