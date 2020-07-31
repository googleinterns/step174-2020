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

package com.google.sps.servlets;
import java.io.IOException;

/**
 * Interface for story generation class.
 */

public interface StoryManager {
  /**
   * Returns generation prefix.
   *
   * @return String The generation prefix.
   */
  public String getPrefix();

  /**
   * Returns maximum length for generation.
   *
   * @return int The maximum length for text generation.
   */
  public int getMaxLength();

  /**
   * Returns temperature(volatility of generation).
   *
   * @return Double Numerical quantity representing temperature.
   */
  public Double getTemperature();

  /**
   * Returns generated text output for a given prompt, length, and temperature.
   */
  public String generateText();

  /**
   * Allow public setting of RequestFactory for alternative posting.
   *
   * @param factory StoryManagerRequestFactory to use for HttpRequests.
   */
  public void setRequestFactory(StoryManagerRequestFactory factory);
}