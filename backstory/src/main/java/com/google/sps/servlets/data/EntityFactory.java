import com.google.appengine.api.datastore.Entity;

/**
 * Factory pattern for object/mock creation.
 */
public interface EntityFactory { public Entity newInstance(String entityName); }
