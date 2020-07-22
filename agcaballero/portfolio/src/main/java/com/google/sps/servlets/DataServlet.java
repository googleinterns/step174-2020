// Copyright 2019 Google LLC
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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.FetchOptions.Builder;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * Servlet that accepts data from the comments form &
 * posts that data to the comments.html page
 */
@WebServlet("/data")
public class DataServlet extends HttpServlet {
  /**
   * Nested private class to represent a comment
   */
  private class Comment {
    /** Stores the date & time comment submitted */
    private final Date timestamp;
    /** Stores the name of the commenter */
    private final String name;
    /** Stores the email of the commenter */
    private final String email;
    /** Stores the text of the comment */
    private String text;
    /** Original language code of comment (an ISO-639-1 Code e.g. "EN" for English) */
    private String language;

    /**
     * Constructs a Comment object from an Entity
     */
    public Comment(Entity entity) {
      timestamp = (Date) entity.getProperty("timestamp");
      name = (String) entity.getProperty("name");
      email = (String) entity.getProperty("email");
      text = (String) entity.getProperty("text");
      language = (String) entity.getProperty("language");
    }

    /**
     * @return comment in format: "name (email): text"
     */
    public String toString() {
      return name + " (" + email + "): " + text;
    }

    /**
     * @return a Comment as a HTML div with proper formatting to be displayed
     * @param String language code for language text should be translated to (an ISO-639-1 Code e.g.
     *     "EN" for English)
     */
    private String htmlFormat(String languageCode) {
      // gets time in local time zone (default: US ET)
      LocalDateTime localTime =
          new LocalDateTime(timestamp.getTime(), DateTimeZone.forID("US/Eastern"));
      DateTimeFormatter formatter = DateTimeFormat.forPattern("h:mm a M/dd/yy");

      return "<div class='comment-div'>"
          + "<p class='date'>" + formatter.print(localTime) + "</p>"
          + "<p><b>" + name + " (<a href='mailto:" + email + "'>" + email + "</a>):</b></p>"
          + "<p class='comment-text' lang=" + languageCode + ">" + getTranslatedText(languageCode)
          + "</p></div>";
    }

    /**
     * Returns translated text of comment, if necessary. Makes a network call to Translation API,
     * so method could be slow.
     * @return the text field translated to the language of a given language code (e.g. "EN" for
     *     English)
     */
    public String getTranslatedText(String languageCode) {
      // declare an instance of translate
      Translate translate = TranslateOptions.getDefaultInstance().getService();

      // if the target language code is NOT same as original language code, translate the text &
      // return it else, just return the text itself (avoids a network call)
      if (!language.toUpperCase().equals(languageCode.toUpperCase())) {
        Translation translation =
            translate.translate(text, Translate.TranslateOption.sourceLanguage(language),
                Translate.TranslateOption.targetLanguage(languageCode));
        return translation.getTranslatedText();
      } else {
        return text;
      }
    }
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Retrieve max number & language code for comments (defaults in comments.js)
    int maxComments = Integer.parseInt(request.getParameter("max-comments"));
    String languageCode = request.getParameter("display-lang");

    // Retrieve from Datastore all entities of type "Comment", sorted by descending time
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Query query = new Query("Comment").addSort("timestamp", SortDirection.DESCENDING);
    PreparedQuery results = datastore.prepare(query);
    StringBuilder commentDivs = new StringBuilder();

    response.setContentType("application/json");

    // Build a String of divs to hold capped # of comments to add to page
    for (Entity entity : results.asIterable(FetchOptions.Builder.withLimit(maxComments))) {
      // Create a Comment from the Entity
      Comment comment = new Comment(entity);

      // append current entity's div to HTML string commentDivs
      commentDivs.append(comment.htmlFormat(languageCode));
    }

    // Respond to request with the commentDivs html
    response.setContentType("application/html;");
    response.setCharacterEncoding("UTF-8"); // ensures special characters display
    response.getWriter().println(commentDivs);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Retrieve input from form & store it in commentEntity
    Entity commentEntity = new Entity("Comment");
    commentEntity.setProperty("name", getRequestParameter(request, "name", ""));
    commentEntity.setProperty("email", getRequestParameter(request, "email", ""));
    commentEntity.setProperty("text", getRequestParameter(request, "comment", ""));
    // default language of source is English
    commentEntity.setProperty("language", getRequestParameter(request, "language", "en"));

    // Store date/time in commentEntity
    Calendar cal = Calendar.getInstance();
    commentEntity.setProperty("timestamp", cal.getTime());

    // Put commentEntity into Datastore
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(commentEntity);

    // Redirect to the comments page
    response.sendRedirect("comments.html");
  }

  /**
   * @return the request parameter, or the default value if the parameter
   *         was not specified by the client
   */
  private String getRequestParameter(HttpServletRequest request, String name, String defaultValue) {
    String value = request.getParameter(name);
    if (value == null) {
      return defaultValue;
    }
    return value;
  }
}
