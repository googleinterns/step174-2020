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

public final class FindMeetingQuery {
   
  /** the number of minutes in a day */
  private static final int MINUTES_IN_DAY = 24 * 60;
  /** 
   * a constant to represent when mandatory attendees unavailable 
   * this number is positive to differentiate it from the negative 
   * values of optional attendees who can't come
   */ 
  private static final int MANDATORY_UNAVAILABLE = 10;
  
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
    
    // has the + 1 to account for last minute in day
    int[] minutes = new int[MINUTES_IN_DAY];

    for(Event event: events) {
      int status = 0;
      int mandatoryOverlap = attendeeOverlap(event.getAttendees(), attendees);

      if(mandatoryOverlap > 0) {
        status = MANDATORY_UNAVAILABLE;
      } 
      else {
        int optionalOverlap = attendeeOverlap(event.getAttendees(), optionalAttendees);
        
        if(optionalOverlap > 0) {
          status = optionalOverlap;
        }
      }
       
      // 0 is default so does not need to be set
      if(status != 0) {
        TimeRange range = event.getWhen();

        for(int i = range.start(); i < range.end(); i++)
          // don't change it if it's already set to being unavailable
          if(minutes[i] != MANDATORY_UNAVAILABLE) {
            if(status == MANDATORY_UNAVAILABLE)
              minutes[i] = MANDATORY_UNAVAILABLE;
            else 
              // if it's available but some attendees can't make it
              // then just subtract the ones who can't make it from 
              // the status (so the number of optional attendees who can't make
              // it will compound)
              minutes[i] -=  status;
          }
      }
    }

    System.out.println(Arrays.toString(minutes));

    int start = 0;
    int statusOfTimes = Integer.MIN_VALUE;
    int lastMaxStatus = Integer.MIN_VALUE;
    int maxStatusFound = Integer.MIN_VALUE;
    ArrayList<TimeRange> times = new ArrayList<TimeRange>();

    // add available times to times array
    for(int i = 0; i < minutes.length; i++) {
      int currentStatus = minutes[i];

      if(currentStatus == MANDATORY_UNAVAILABLE) {
        if(i - 1 > 0 && minutes[i - 1] == maxStatusFound) {
          if(statusOfTimes != maxStatusFound) {
            ArrayList<TimeRange> previousTimes = new ArrayList<TimeRange>(times);
            times = new ArrayList<TimeRange>();
            boolean addable = addMeeting(request, start, i, false, times);

            if(! addable) {
              maxStatusFound = lastMaxStatus;
              times = previousTimes;
            }
            else {
              System.out.println("replaced");
              statusOfTimes = maxStatusFound;
            }
          }
          else 
            addMeeting(request, start, i, false, times);

          start = i;
        }
      }
      else if(i - 1 > 0 &&
        minutes[i - 1] == MANDATORY_UNAVAILABLE)
        start = i;

      if(currentStatus != MANDATORY_UNAVAILABLE
        && currentStatus > maxStatusFound) {
        lastMaxStatus = maxStatusFound;
        maxStatusFound = currentStatus;

        start = i;
      }
      else 
        continue;   
    }
    
    // add meeting for end of day
    if(minutes[MINUTES_IN_DAY - 1] == maxStatusFound) {
      addMeeting(request, start, TimeRange.END_OF_DAY, true, times);
    }

    System.out.println(maxStatusFound);

    return times;
  }
  
  /**
   * A private helper method to determine the amount of overlap between two groups
   * of attendees, represented as String collections
   *
   * @return the number of attendees who overlap between the two meetings
   */
  private int attendeeOverlap(Collection<String> groupA, Collection<String> groupB) {
    int overlap = 0;

    for(String attendeeA: groupA)
      for(String attendeeB: groupB)
        if(attendeeA.equals(attendeeB))
          overlap++;

    return overlap;
  }
   

  /** Private helper method where if a possible time range is long enough,
   *  will add that time to the TimeRange collection
   * 
   * @return true, if the time range is long enough, false, if not
   */
  private boolean addMeeting(MeetingRequest request, int start, int end, boolean inclusive, Collection<TimeRange> times) {
    int duration = end - start + 1;
    boolean longEnough = duration >= request.getDuration();

    if(longEnough)
      times.add(TimeRange.fromStartEnd(start, end, inclusive)); 

    return longEnough;
  }
}
