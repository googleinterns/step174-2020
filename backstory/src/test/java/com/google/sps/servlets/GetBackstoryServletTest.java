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

package com.google.sps.servlets;

import static org.mockito.Mockito.*;
import com.google.sps.images.ImagesManager;
import com.google.sps.images.VisionImagesManager;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.sps.images.data.AnnotatedImage;
import com.google.sps.perspective.PerspectiveStoryAnalysisManager;
import com.google.sps.perspective.StoryAnalysisManager;
import com.google.sps.perspective.data.StoryDecision;
import com.google.sps.perspective.data.NoAppropriateStoryException;
import com.google.sps.story.PromptManager;
import com.google.sps.story.StoryManager;
import com.google.sps.story.StoryManagerImpl;
import com.google.sps.servlets.AnalyzeImageServlet;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.sps.servlets.data.BlobstoreManagerFactory;
import com.google.sps.servlets.data.BackstoryDatastoreServiceFactory;
import com.google.sps.servlets.data.EntityFactory;
import com.google.sps.servlets.data.QueryFactory;
import com.google.sps.servlets.data.ImagesManagerFactory;
import com.google.sps.servlets.data.StoryManagerFactory;
import com.google.sps.servlets.data.BackstoryUserServiceFactory;
import com.google.sps.servlets.data.StoryAnalysisManagerFactory;
import com.google.sps.servlets.data.BlobstoreManager;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.User;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.ArgumentCaptor;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import com.google.sps.APINotAvailableException;
import com.google.sps.perspective.data.NoAppropriateStoryException;
import com.google.sps.perspective.data.StoryDecision;
import com.google.sps.story.StoryManagerURLProvider;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import com.google.sps.servlets.GetBackstoryServlet;

/**
 * Tests for the GetBackstoryServlet, which manages the GET /backstory request.
 * GET /backstory returns the latest backstory created for the user from
 * permanent storage, then adds it to the response of the GET request.
 */
@RunWith(MockitoJUnitRunner.class)
public final class GetBackstoryServletTest {
  /**
   * Tests that the GetBackstoryServlet GET request succesfully returns only the one latest backstory
   * created for the user, even if multiple backstories have already been created.
   */
  @Test
  public void testDatastoreRetrieval() throws IOException {
    HttpServletRequest mockRequest = mock(HttpServletRequest.class);
    HttpServletResponse mockResponse = mock(HttpServletResponse.class);
    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(mockResponse.getWriter()).thenReturn(writer);
    GetBackstoryServlet servlet = new GetBackstoryServlet();

    DatastoreService mockDatastoreService = mock(DatastoreService.class);
    UserService mockUserService = mock(UserService.class);

    // Constructs the mock user for which the backstory would have theoretically been created.
    String userEmail = "user@gmail.com";
    String userAuthenticationDomain = "authentication";
    User testUser = new User(userEmail, userAuthenticationDomain);
    when(mockUserService.isUserLoggedIn()).thenReturn(true);
    when(mockUserService.getCurrentUser()).thenReturn(testUser);

    // Query to find all analyzed image entities and filter to only return the current
    // user's backstories ordered from newest to oldest.
    Query mockQuery = mock(Query.class);
    Filter userBackstoriesFilter =
            new FilterPredicate("userEmail", FilterOperator.EQUAL, userEmail);
    when(mockQuery.setFilter(userBackstoriesFilter)).thenReturn(mockQuery);
    when(mockQuery.addSort("timestamp", SortDirection.DESCENDING)).thenReturn(mockQuery);

    // Limit the Query (which is sorted from newest to oldest) to only return the first
    // result, thus displaying the most recent story uploaded.
    int backstoryFetchLimit = 1;

    // Create two entities to assure that only the first (most recent) backstory is returned.
    Text firstBackstory = new Text("firstBackstory");
    // The entities must have a property named backstory
    Entity firstEntity = mock(Entity.class);
    when(firstEntity.getProperty("backstory")).thenReturn(firstBackstory);
    Entity secondEntity = mock(Entity.class);
    List<Entity> entityList = Arrays.asList(firstEntity, secondEntity);

    // The datastore prepare function takes in a raw Query object and returns a PreparedQuery
    // which is capable of being iterated through.
    PreparedQuery mockPreparedQuery = mock(PreparedQuery.class);
    when(mockPreparedQuery.asIterable(FetchOptions.Builder.withLimit(backstoryFetchLimit))).thenReturn(entityList.subList(0, backstoryFetchLimit));
    when(mockDatastoreService.prepare(mockQuery)).thenReturn(mockPreparedQuery);
    
    // Create and set the factories to return the configured mocks.
    BackstoryUserServiceFactory backstoryUserServiceFactory = () -> {
      return mockUserService;
    };
    BackstoryDatastoreServiceFactory backstoryDatastoreServiceFactory = () -> {
      return mockDatastoreService;
    };
    QueryFactory queryFactory = (String queryName) -> {
      return mockQuery;
    };
    servlet.setBackstoryUserServiceFactory(backstoryUserServiceFactory);
    servlet.setBackstoryDatastoreServiceFactory(backstoryDatastoreServiceFactory);
    servlet.setQueryFactory(queryFactory);

    // doGet call to initiate testing.
    servlet.doGet(mockRequest, mockResponse);

    // Verify that the appropriate filters were set on the query.
    verify(mockQuery).setFilter(userBackstoriesFilter);
    verify(mockQuery).addSort("timestamp", SortDirection.DESCENDING);

    String actualResponse = stringWriter.toString();
    writer.flush();

    Assert.assertTrue(actualResponse.contains("backstory"));
    Assert.assertTrue(actualResponse.contains("firstBackstory"));
    Assert.assertFalse(actualResponse.contains("secondBackstory"));
  }

  /**
   * Tests that the GetBackstoryServlet GET requests succesfully returns an empty JSON array
   * when no backstories have been generated for the user.
   */
  @Test
  public void testDatastoreRetrievalNoneUploaded() throws IOException {
    HttpServletRequest mockRequest = mock(HttpServletRequest.class);
    HttpServletResponse mockResponse = mock(HttpServletResponse.class);
    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(mockResponse.getWriter()).thenReturn(writer);
    GetBackstoryServlet servlet = new GetBackstoryServlet();

    DatastoreService mockDatastoreService = mock(DatastoreService.class);
    UserService mockUserService = mock(UserService.class);

    // Constructs the mock user for which the backstory would have theoretically been created.
    String userEmail = "user@gmail.com";
    String userAuthenticationDomain = "authentication";
    User testUser = new User(userEmail, userAuthenticationDomain);
    when(mockUserService.isUserLoggedIn()).thenReturn(true);
    when(mockUserService.getCurrentUser()).thenReturn(testUser);

    // Query to find all analyzed image entities and filter to only return the current
    // user's backstories ordered from newest to oldest.
    Query mockQuery = mock(Query.class);
    Filter userBackstoriesFilter =
            new FilterPredicate("userEmail", FilterOperator.EQUAL, userEmail);
    when(mockQuery.setFilter(userBackstoriesFilter)).thenReturn(mockQuery);
    when(mockQuery.addSort("timestamp", SortDirection.DESCENDING)).thenReturn(mockQuery);

    // Limit the Query (which is sorted from newest to oldest) to only return the first
    // result, thus displaying the most recent story uploaded.
    int backstoryFetchLimit = 1;

    // The empty list which must be returned when no backstories have been generated yet for the user.
    List<Entity> entityList = new ArrayList<>();

    // The datastore prepare function takes in a raw Query object and returns a PreparedQuery
    // which is capable of being iterated through.
    PreparedQuery mockPreparedQuery = mock(PreparedQuery.class);
    when(mockPreparedQuery.asIterable(FetchOptions.Builder.withLimit(backstoryFetchLimit))).thenReturn(entityList);
    when(mockDatastoreService.prepare(mockQuery)).thenReturn(mockPreparedQuery);
    
    // Create and set the factories to return the configured mocks.
    BackstoryUserServiceFactory backstoryUserServiceFactory = () -> {
      return mockUserService;
    };
    BackstoryDatastoreServiceFactory backstoryDatastoreServiceFactory = () -> {
      return mockDatastoreService;
    };
    QueryFactory queryFactory = (String queryName) -> {
      return mockQuery;
    };
    servlet.setBackstoryUserServiceFactory(backstoryUserServiceFactory);
    servlet.setBackstoryDatastoreServiceFactory(backstoryDatastoreServiceFactory);
    servlet.setQueryFactory(queryFactory);

    // doGet call to initiate testing.
    servlet.doGet(mockRequest, mockResponse);

    // Verify that the appropriate filters were set on the query.
    verify(mockQuery).setFilter(userBackstoriesFilter);
    verify(mockQuery).addSort("timestamp", SortDirection.DESCENDING);

    String actualResponse = stringWriter.toString();
    writer.flush();

    // The front-end will be expecting an empty JSON array in this scenario
    String expectedResponse = "[]\n";
    Assert.assertEquals(expectedResponse, actualResponse);
  }
}
