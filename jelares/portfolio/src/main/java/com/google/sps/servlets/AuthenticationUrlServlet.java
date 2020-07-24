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

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import com.google.sps.servletData.AuthenticationInformation;
import com.google.sps.servletData.AuthenticationAction;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Checks whether the user is logged in or not, and writes the appropriate AuthenticationInformation object to the response, with:
 * AuthenticationAction.LOGIN if the user is not logged in
 * AuthenticationAction.LOGOUT if the user is logged in
 */
@WebServlet("/authentication-url")
public class AuthenticationUrlServlet extends HttpServlet {
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("text/html");
    PrintWriter out = response.getWriter();
    UserService userService = UserServiceFactory.getUserService();

    AuthenticationAction authenticationAction;
    String authenticationUrl;
    // If user is not logged in, give a login link in the navbar
    if (!userService.isUserLoggedIn()) {
      authenticationAction = AuthenticationAction.LOGIN;
      authenticationUrl = userService.createLoginURL("/index.html");

    } else {
      authenticationAction = AuthenticationAction.LOGOUT;
      authenticationUrl = userService.createLogoutURL("/index.html");
    }

    AuthenticationInformation logInfo = new AuthenticationInformation(authenticationAction, authenticationUrl);
    Gson gson = new Gson();
    out.println(gson.toJson(logInfo));
  }
}
