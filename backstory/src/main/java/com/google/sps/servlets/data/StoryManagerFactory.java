import com.google.sps.story.StoryManager;
package com.google.sps.servlets.data;

/**
 * Factory pattern for object/mock creation.
 */
public interface StoryManagerFactory {
  public StoryManager newInstance(String prompt, int storyLength, double temperature);
}
