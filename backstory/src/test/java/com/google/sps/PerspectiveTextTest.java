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
import com.google.sps.data.PerspectiveText;
import com.google.sps.servlets.PerspectiveFilterServlet;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;

@RunWith(JUnit4.class)
public final class PerspectiveTextTest extends Mockito {
  
  private final String API_KEY = "AIzaSyBGanMblCA8ZRtZj757eppSbVH0V9vCxgI";

  private PerspectiveAPI perspectiveAPI;

  @Before
  public void setUp() {
    perspectiveAPI = PerspectiveAPI.create(API_KEY);
  }

  @Test
  public void testServlet() throws Exception {
    HttpServletRequest request = mock(HttpServletRequest.class);       
    HttpServletResponse response = mock(HttpServletResponse.class);    

    when(request.getParameter("text")).thenReturn("hello");

    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);

    new PerspectiveFilterServlet().doGet(request, response);

    verify(request, atLeast(1)).getParameter("text"); 
    writer.flush(); // it may not have been flushed yet...
    System.out.println(stringWriter.toString());
    Assert.assertTrue(stringWriter.toString().contains("<p>0.034919903% Toxicity</p>"));
}

  @Test
  public void helloTest() {
    // check that the Perspective API is retrieving the right value for a provided nontoxic input

    PerspectiveText textAnalysis = new PerspectiveText(perspectiveAPI, "Hello");
    float toxicity = textAnalysis.getToxicity();

    // found actual value with an independent API call
    Assert.assertEquals(.03491990, toxicity, .0000001);
  }

  @Test
  public void toxicText1() {
    // check that the Perspective API is retrieving the right value for a provided toxic input

    PerspectiveText textAnalysis = new PerspectiveText(perspectiveAPI, "Die");
    float toxicity = textAnalysis.getToxicity();

    // actual value found using notepad provided on Perspective website 
    // (which only provides percentage w/ two decimal points, hence range of error of .0001)
    Assert.assertEquals(.8593, toxicity, .0001);
  }

  @Test
  public void toxicTest2() {
    // check that the Perspective API is retrieving the right value for a provided toxic input
    
    PerspectiveText textAnalysis = new PerspectiveText(perspectiveAPI, "What kind of idiot name is foo?");
    float toxicity = textAnalysis.getToxicity();

    // actual value provided on https://github.com/conversationai/perspectiveapi/blob/master/1-get-started/sample.md
    Assert.assertEquals(0.9208521, toxicity, 0.0000001);
  }
}
