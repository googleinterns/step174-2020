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
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.TimeZone;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet to post and retrieve messages sent
 * through the "Contact Me" form
 */
@WebServlet("/message")
public class MessageServlet extends HttpServlet {
  /**
   * A private class to represent a message
   * sent from "Contact Me"
   */
  private class Message {
    /** Name of the contacter */
    private String name;
    /** Email of the contacter */
    private String email;
    /** Subject of the contact message */
    private String subject;
    /** Text/main description of the contact message */
    private String body;
    /** Date & time message submitted */
    private Date timestamp;
    /** Key of the Entity*/
    Key key;

    public Message(String name, String email, String subject, String body, Date timestamp) {
      this.name = name;
      this.email = email;
      this.subject = subject;
      this.body = body;
      this.timestamp = timestamp;
    }

    public Message(Entity entity) {
      name = (String) entity.getProperty("name");
      email = (String) entity.getProperty("email");
      subject = (String) entity.getProperty("subject");
      body = (String) entity.getProperty("body");
      timestamp = (Date) entity.getProperty("timestamp");
      key = entity.getKey();
    }

    public Entity getEntity() {
      Entity entity = new Entity("Message");
      entity.setProperty("name", name);
      entity.setProperty("email", email);
      entity.setProperty("subject", subject);
      entity.setProperty("body", body);
      entity.setProperty("timestamp", timestamp);

      return entity;
    }

    /**
     * Returns a Message formatted as an HTML div.
     * Should only be called after constructor using Entity (requires key variable).
     */
    public String htmlFormat() {
      // gets time in Eastern Time
      SimpleDateFormat format = new SimpleDateFormat("h:mm a M/dd/yy");
      format.setTimeZone(
          TimeZone.getTimeZone("America/New_York")); // sets time zone to eastern time

      return "<div class='message-div'><p class='date'>" + format.format(timestamp) + "</p>"
          + "<p><b>" + name + " (<a href='mailto:" + email + "'>" + email + "</a>): " + subject
          + "</b> <br><br>" + body + "</p><a href='javascript:deleteMessage(" + key.getId()
          + ")'><i class='fa fa-trash'></i></a></div>";
    }
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Retrieve from Datastore all entities of type "Message", sorted by descending time
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Query query = new Query("Message").addSort("timestamp", SortDirection.DESCENDING);
    PreparedQuery results = datastore.prepare(query);

    StringBuilder messageDivs = new StringBuilder();

    for (Entity entity : results.asIterable()) {
      // Create a Message from the Entity
      Message message = new Message(entity);
      messageDivs.append(message.htmlFormat());
    }

    response.setContentType("application/html;");
    response.getWriter().println(messageDivs);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Retrieve info from form
    String fname = getParameter(request, "fname", "");
    String lname = getParameter(request, "lname", "");
    String email = getParameter(request, "email", "");
    String subject = getParameter(request, "subject", "");
    String body = getParameter(request, "body", "");

    // Get a calendar to store date/time in the message
    Calendar cal = Calendar.getInstance();

    // create a Message with info from form & date submitted
    Message message = new Message(fname + " " + lname, email, subject, body, cal.getTime());

    // add this Message to the database
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(message.getEntity());

    response.sendRedirect("/contact.html");
  }

  /**
   * @return the request parameter, or the default value if the parameter
   *         was not specified by the client
   */
  private static String getParameter(HttpServletRequest request, String name, String defaultValue) {
    String value = request.getParameter(name);
    if (value == null) {
      return defaultValue;
    }
    return value;
  }
}
