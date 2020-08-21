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

package com.google.sps.servlets.data;

import com.google.sps.story.StoryManager;

/**
 * Factory pattern for StoryManager.
 */
public interface StoryManagerFactory {
  /**
   * Create a new instance of StoryManager with the specified prompt, story length, and temperature.
   *
   * @param prompt the prompt to be used in generation.
   * @param storyLength the length of the story to be generated.
   * @param temperature the temperature of the story to be generated.
   * @return an instance of StoryManager.
   */
  public StoryManager newInstance(String prompt, int storyLength, double temperature);
}
