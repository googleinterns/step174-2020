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

package com.google.sps.perspective;

import au.com.origma.perspectiveapi.v1alpha1.PerspectiveAPI;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Uses local Java file containing API key to create 
 * new instances of PerspectiveAPI class.
 */
public class PerspectiveAPIFactoryImpl implements PerspectiveAPIFactory {

  /** The name of the file with the API key (including its package) */
  private static final String API_KEY_FILE = "com.google.sps.data.perspective.PerspectiveAPIKey";
  
  /* The name of the method within the API_KEY_FILE to get the key */
  private static final String GET_KEY_METHOD_NAME = "getKey";

  /** the API key to use to create PerspectiveAPI instances in this factory */
  private String apiKey;

  /**
   * Constructs a factory by finding and setting the api key for this factory.
   * 
   * @throws ClassNotFoundException if it cannot find the file with the api key (PerspectiveAPIKey)
   * @throws NoSuchMethodException if said file doesn't have the method to get the key (getKey)
   * @throws IllegalAccessException if class called from doesn't have proper access to the method to get the key (getKey)
   * @throws InvocationTargetException if the method to get the key (getKey()) itself throws an exception
   */
  public PerspectiveAPIFactoryImpl() throws ClassNotFoundException, NoSuchMethodException,
      IllegalAccessException, InvocationTargetException {
    // fetch the PerspectiveAPIKey class if it's there
    Class<?> keyClass = Class.forName(API_KEY_FILE);
    
    // create a method getKey that takes no parameters 
    // (which is what null param of getMethod signifies)
    Method getKey = keyClass.getMethod(GET_KEY_METHOD_NAME, null);

    // invoke static method getKey() (first null means it's static 
    // & second null means it does not need arguments) & stores result in apiKey
    apiKey = (String) getKey.invoke(null, null); 
  }

  /**
   * Returns an instance of the PerspectiveAPI using API key.
   *
   * @return a functioning instance of PerspectiveAPI.
   */
  public PerspectiveAPI newInstance() {
    return PerspectiveAPI.create(apiKey);
  }
}
