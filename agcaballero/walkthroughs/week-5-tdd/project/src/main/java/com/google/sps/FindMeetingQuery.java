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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public final class FindMeetingQuery {

  /** An enum to represent the different options for availability at every meeting time */
  private enum Availability { 
    ALL_AVAILABLE,
    MANDATORY_AVAILABLE,
    UNAVAILABLE;
  
    boolean isReplaceableBy(Availability availability) {
      switch (this) {
        case ALL_AVAILABLE:
          return true;
        case UNAVAILABLE:
          return false;
        case MANDATORY_AVAILABLE:
          return availability == ALL_AVAILABLE;
        default:
          throw new IllegalStateException("This statement should not be reachable." +
              "This switch should have conditions for all possible values of enum.");
      }
    }
  }
   
  /** the number of minutes in a day */
  private static final int MINUTES_IN_DAY = 24 * 60;
  
  /**
   * Takes the events of the day and information about a potential meeting 
   * and returns the time ranges in which this meeting could be scheduled
   *
   * @return a collection of TimeRanges in which the meeting could be scheduled
   * @param events the collection of events scheduled for that day
   * @param request the meeting request to be fulfilled (will have duration & attendees)
   */ 
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    Collection<String> attendees = request.getAttendees();
    Collection<String> optionalAttendees = request.getOptionalAttendees();

    // minutes of day is + 1 to account for first and last minute of day
    Availability[] minutes = new Availability[MINUTES_IN_DAY + 1];
    // set all spots to default value of all available
    Arrays.fill(minutes, Availability.ALL_AVAILABLE);

    for (Event event: events) {
      Availability availability = Availability.ALL_AVAILABLE;
      
      if (!Collections.disjoint(event.getAttendees(), attendees)) {
        // if there's overlap in mandatory attendees, time is unavailable
        availability = Availability.UNAVAILABLE;
      } else if (!Collections.disjoint(event.getAttendees(), optionalAttendees)) {
        // if there's an overlap in only the optional attendees, time is available for only the mandatory employees
        availability = Availability.MANDATORY_AVAILABLE;
      }
      
      TimeRange range = event.getWhen();

      switch (availability) {
        case ALL_AVAILABLE:
          continue;
        case UNAVAILABLE:
          Arrays.fill(minutes, range.start(), range.end(), Availability.UNAVAILABLE);
          break;
        case MANDATORY_AVAILABLE:
          for (int i = range.start(); i < range.end(); i++) {
            // make sure unavailable times are not overwritten
            if (minutes[i].isReplaceableBy(availability)) {
              minutes[i] = availability;
            }
          }
          break;
      }
    }
    
    // get times where all attendees (including optional) can go
    List<TimeRange> availableTimesWithOptionalAttendees = availableTimeRanges(minutes, EnumSet.of( Availability.ALL_AVAILABLE ), request.getDuration());
    
    // if there's at least one time where all optional attendees can go, 
    // return the times with optional attendees
    if (availableTimesWithOptionalAttendees.size() > 0) {
      return availableTimesWithOptionalAttendees;
    } else { 
      // else return the times where mandatory attendees can go
      return availableTimeRanges(minutes, EnumSet.of ( Availability.ALL_AVAILABLE, Availability.MANDATORY_AVAILABLE ), request.getDuration());
    }
  }
  
  /** 
   * Private helper method that generates a list of available time ranges by taking in
   * in the minuteAvailabilities array and finding the "minutes" that match availabilities
   * in the set minuteAvailabilities.
   *
   * @return {List<TimeRange>} a list of the available time ranges for the given availabilities
   */
  private List<TimeRange> availableTimeRanges (Availability[] minuteAvailabilities, 
      Set<Availability> matchingAvailabilities, long requestDuration) {
    // declare all variables to keep track of time ranges
    int start = 0; 
    ArrayList<TimeRange> times = new ArrayList<TimeRange>();
    boolean thisMinuteAvailable = matchingAvailabilities.contains(minuteAvailabilities[start]);
    // for first minute, this will have same value as thisMinuteAvailable but shouldn't affect result
    boolean wasLastMinuteAvailable = thisMinuteAvailable; 

    // go through minute availabilities array and create time ranges for all
    // spots that match one of the availabilities in the matching availabilities set
    for (int i = 0; i < minuteAvailabilities.length; i++) {
      // gets availability of current minute (considered available if status matches one within set)
      thisMinuteAvailable = matchingAvailabilities.contains(minuteAvailabilities[i]);

      if (wasLastMinuteAvailable) {
        // If the previous minute was available, but the current minute is unavailable or if it's
        // the end of the day, this is the end of an available time range. If the time range is longer 
        // than the required duration, it's recorded as an available time range.
        if (!thisMinuteAvailable  || i == minuteAvailabilities.length - 1) {
          int end = i;
          int duration = end - start;

          if (duration >= requestDuration) {
            times.add(TimeRange.fromStartEnd(start, end - 1, true)); // add time range (inclusive of start & end) 
          }
          
          // avoid an inconsistency b/c in case availableMinutes[availableMinutes.length - 1] is true,
          // you shouldn't set this to false (& that would happen without this if)
          if (!thisMinuteAvailable) {
            wasLastMinuteAvailable = false;
          }
        }
      } else if (thisMinuteAvailable) {
        // If the last minute was unavailable, but this minute is available, then this is the beginning
        // of a new available time range, so start will be set to this minute and wasLastMinuteAvailable to true.

        start = i;
        wasLastMinuteAvailable = true;
      }
    }

    return times;
  }
}
