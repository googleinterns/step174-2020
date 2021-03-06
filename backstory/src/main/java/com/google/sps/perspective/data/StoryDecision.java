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

package com.google.sps.perspective.data;

/** The result of the analysis of text(s) for inappropriate content. */
public class StoryDecision {
  /** a story deemed appropriate by analysis engine */
  private final String story;

  /**
   * Constructs a StoryDecision object from a String story.
   *
   * @param story A story deemed appropriate by the analysis engine.
   * @throws IllegalArgumentException if story is null
   */
  public StoryDecision(String story) throws IllegalArgumentException {
    if (story == null) {
      throw new IllegalArgumentException("Story should never be null");
    }

    this.story = story;
  }

  /**
   * Returns the appropriate story.
   *
   * @return A story deemed appropriate by the analysis engine.
   */
  public String getStory() {
    return story;
  }

  @Override
  public boolean equals(Object other) {
    StoryDecision otherStoryDecision = (StoryDecision) other;
    return story.equals(otherStoryDecision.getStory());
  }

  @Override
  public int hashCode() {
    // overriding hash code b/c need to when you override equals
    // just setting it to the hash code of the only field
    return story.hashCode();
  }
}
