import com.google.sps.story.StoryManager;

/**
 * Factory pattern for object/mock creation.
 */
public interface StoryManagerFactory {
  public StoryManager newInstance(String prompt, int storyLength, double temperature);
}
