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

package com.google.sps.data.perspective;

/**
 * An implementation of PerspectiveManager using PerspectiveAPI for analysis.
 */
public interface PerspectiveManagerImpl {
  /** The story managed by this instance. */
  private final String story;

  /**
   * @param story The story managed by the constructed instance.
   */
  public PerspectiveManagerImpl(String story) {
    this.story = story;
  }

  /**
   * Analyzes the stories managed by this instance.
   * @return An object describing the recommendation resulting from the analysis.
   */
  PerspectiveDecision analyze() {
    // TODO(agcaballero): Replace this fake behavior with a real API call, etc.
    return new PerspectiveDecision(story);
  }
}
