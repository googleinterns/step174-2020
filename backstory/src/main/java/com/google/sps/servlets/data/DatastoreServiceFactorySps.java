import com.google.appengine.api.datastore.DatastoreService;
package com.google.sps.servlets.data;

/**
 * Factory pattern for object/mock creation.
 */
public interface DatastoreServiceFactorySps { public DatastoreService newInstance(); }
