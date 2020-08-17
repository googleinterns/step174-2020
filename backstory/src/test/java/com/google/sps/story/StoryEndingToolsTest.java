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

package com.google.sps.story;

import com.google.sps.story.data.StoryEndingTools;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Quality Tests for StoryEndingTools
 */
@RunWith(JUnit4.class)
public final class StoryEndingToolsTest {

  // TODO: test that it always ends in a period for the story one

  // TODO: test chopped off method with: 
  // - empty string 
  // - sentence fragment at end
  // - full sentence at the end 

  // test both method with 
  // - empty string 
  // - sentence fragment 
  // - full sentence at end 

  @Test
  public void tryIt() {
    String story = "Once upon a time a girl walked in a forest. She was attacked by a wolf. The wolf was ";
    System.out.println(StoryEndingTools.endStory(story));
  }
}