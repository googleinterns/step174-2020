import com.google.appengine.api.datastore.Entity;
package com.google.sps.servlets.data;

/**
 * Factory pattern for object/mock creation.
 */
public interface EntityFactory { public Entity newInstance(String entityName); }
