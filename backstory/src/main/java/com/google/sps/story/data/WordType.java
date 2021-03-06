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

package com.google.sps.story.data;

/**
 * Enum to represent types (by part of speech and other
 * syntactical category) of labels returned by Cloud Vision.
 * Unusable means that the word falls into a category of words
 * that the prompt creation suite is not equipped to put into
 * sentences. Almost no words should fall into unusable
 * because the WordType enum contains all word types of labels
 * returned by Cloud Vision.
 */
public enum WordType {
  NOUN,
  PROPER_NOUN,
  MULTIWORD_NOUN,
  GERUND,
  ADJECTIVE,
  UNUSABLE;
}
