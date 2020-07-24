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

package com.google.sps;

import au.com.origma.perspectiveapi.v1alpha1.PerspectiveAPI;
import com.google.gson.Gson;
import com.google.sps.data.PerspectiveAnalysis;
import com.google.sps.servlets.PerspectiveServlet;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;

@RunWith(JUnit4.class)
public final class PerspectiveTest extends Mockito {
  
  @Test
  public void testServlet() throws Exception {
    HttpServletRequest request = mock(HttpServletRequest.class);       
    HttpServletResponse response = mock(HttpServletResponse.class);    

    when(request.getParameter("text")).thenReturn("hello");

    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);

    new PerspectiveServlet().doGet(request, response);

    verify(request, atLeast(1)).getParameter("text"); 
    writer.flush(); // it may not have been flushed yet...
    System.out.println(stringWriter.toString());
    // Assert.assertTrue(stringWriter.toString().contains("{\"text\":\"hello\",\"toxicity\":0.034919903}"));
  }
}
