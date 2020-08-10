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

/** 
 * A type of StoryDecision that includes not just the decision (the
 * appropriate story) but the values used to make that decision.
 */
public class PerspectiveDecision extends StoryDecision {
  /** the values of the story  */
  private final PerspectiveValues values;

  /**
   * Constructs a StoryDecision object from a String story.
   *
   * @param story A story deemed appropriate by the analysis engine.
   * @param values the values used to make the decision
   * @throws IllegalArgumentException if story is null
   */
  public PerspectiveDecision(String story, PerspectiveValues values) throws IllegalArgumentException {
    super(story);

    this.values = values;
  }

  /**
   * Returns the values used to choose the story.
   *
   * @return A PerspectiveValues object used to decide on the story.
   */
  public PerspectiveValues getValues() {
    return values;
  }
}
