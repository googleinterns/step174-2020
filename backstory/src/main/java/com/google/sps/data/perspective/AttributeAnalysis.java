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

import au.com.origma.perspectiveapi.v1alpha1.models.AttributeType;

/**
 * Container to hold a AttributeType and corresponding score.
 */
public class AttributeAnalysis {
    /** the attribute score given by analysis */
    private final float score;
    /** the type that the analysis score is for */
    private final AttributeType type;

    public AttributeAnalysis(float score, AttributeType type) {
      this.score = score;
      this.type = type;
    }
  }