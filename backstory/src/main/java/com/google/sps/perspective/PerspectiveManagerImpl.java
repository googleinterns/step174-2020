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

package com.google.sps.perspective;

/**
 * An implementation of PerspectiveManager using PerspectiveAPI for analysis.
 */
public interface PerspectiveManagerImpl implements PerspectiveManager {
    
  /** The perspective API used by this instance. */
  private final PerspectiveAPI perspectiveAPI;

  /**
   * Constructs an object which implements the PerspectiveManager
   * class 
   */
  public PerspectiveManagerImpl() {
    // instantiate the PerspectiveAPI for this instance
  }

  /**
   * Constructs an object with a specified instance of the PerspectiveAPI.
   * Constructor is to be used for testing.
   *
   * @param perspectiveAPI the instance of the PerspectiveAPI to use 
   *     to analyze stories with.
   */
  public PerspectiveManagerImpl(PerspectiveAPI perspectiveAPI) {
    this.perspectiveAPI = perspectiveAPI;
  }

  /**
   * Analyzes the passed-in story using the perspective API and returns the decision
   * as a PerspectiveDeccision object.
   *
   * @param story The story to be analyzed
   * @return An object describing the recommendation resulting from the analysis.
   */
  public PerspectiveDecision getDecision(String story) {
    // TODO: Replace this fake behavior with a real API call, etc.

    // returns default instance of PerspectiveDecision
    return new PerspectiveDecision(story);
  }
}
