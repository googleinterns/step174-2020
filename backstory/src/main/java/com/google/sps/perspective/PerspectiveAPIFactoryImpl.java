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
  
  /** the API key to use to create PerspectiveAPI instances in this factory */
  private String apiKey;

  /**
   * Constructs a factory by finding and setting the api key for this factory.
   * 
   * @throws ClassNotFoundException if it cannot found "PerspectiveAPIKey.java"
   * @throws NoSuchMethodException if PerspectiveAPIKey doesn't have a getKey() method
   * @throws IllegalAccessException if class called from doesn't have proper access to getKey()
   * @throws InvocationTargetException if getKey() itself throws an exception
   */
  public PerspectiveAPIFactoryImpl() throws ClassNotFoundException, NoSuchMethodException,
      IllegalAccessException, InvocationTargetException {
    // fetch the PerspectiveAPIKey class if it's there
    Class<?> keyClass = Class.forName("com.google.sps.data.perspective.PerspectiveAPIKey");
    
    // create a method getKey that takes no parameters 
    // (which is what null param of getMethod signifies)
    Method getKey = keyClass.getMethod("getKey", null);

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
