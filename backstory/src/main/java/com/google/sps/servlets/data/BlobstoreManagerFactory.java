import com.google.sps.servlets.data.BlobstoreManager;
package com.google.sps.servlets.data;

/**
 * Factory pattern for object/mock creation.
 */
public interface BlobstoreManagerFactory { public BlobstoreManager newInstance(); }
