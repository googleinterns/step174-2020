import com.google.appengine.api.users.UserService;
package com.google.sps.servlets.data;

/**
 * Factory pattern for object/mock creation.
 */
public interface UserServiceFactorySps { public UserService newInstance(); }
