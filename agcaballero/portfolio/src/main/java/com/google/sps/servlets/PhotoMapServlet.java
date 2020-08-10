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

import com.google.gson.Gson;
import com.google.sps.data.PhotoLocation;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Scanner;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Returns photo location data as a JSON array,
 */
@WebServlet("/photo-map-data")
public class PhotoMapServlet extends HttpServlet {
  private Collection<PhotoLocation> locations;

  @Override
  public void init() {
    locations = new ArrayList<>();

    // create a CSV reader from photo-marker-data.csv file
    InputStream stream = getServletContext().getResourceAsStream("/WEB-INF/photo-marker-data.csv");
    Reader streamReader = new InputStreamReader(stream);
    CSVReader reader = new CSVReaderBuilder(streamReader)
                           // Skip the first lines, with headers
                           .withSkipLines(1)
                           .build();

    locations = new ArrayList<>();

    for (String[] line : reader) {
      // retrieve field data of a PhotoLocation
      String title = line[0].replace("\"", "");
      double lat = Double.parseDouble(line[1]);
      double lng = Double.parseDouble(line[2]);
      String image = line[3];

      locations.add(new PhotoLocation(title, lat, lng, image));
    }

    try {
      reader.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Sets response to an array of the JSON representation of
   * a PhotoLocation which should resemble:
   * {title: "Title", lat: 0, lng: 0, img: "title.jpg"}
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json");
    Gson gson = new Gson();
    String json = gson.toJson(locations);
    response.getWriter().println(json);
  }
}
