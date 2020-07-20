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

/*
 * A class to find a time that satisfies a meeting query and ensures
 * all mandatory attendees can attend and the most possible
 * optional attendees can attend.
 */
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
    
    // Holds whether the mandatory attendees can attend & the number of optional attendees 
    // that can attend. If minuteStatus[number] is equal to MANDATORY_UNAVAILABLE, then mandatory
    // attendees can't attend at that number. If it's equal to 0, then all attendees can attend.
    // If it's equal to a negative int, then the abs value of that negative number is the number 
    // of optional attendees who can't attend. The +1 is to account for last minute in day
    int[] minuteStatus = new int[MINUTES_IN_DAY + 1];

    // keep track of min status reached 
    int minStatus = 0;

    for(Event event: events) {
      int status = 0;
      int mandatoryOverlap = amountOfOverlap(event.getAttendees(), attendees);

      if(mandatoryOverlap > 0) {
        status = MANDATORY_UNAVAILABLE;
      } 
      else {
        int optionalOverlap = amountOfOverlap(event.getAttendees(), optionalAttendees);
        
        if(optionalOverlap > 0) {
          status = optionalOverlap;
        }
      }
       
      // 0 is default so does not need to be set
      if(status != 0) {
        TimeRange range = event.getWhen();

        for(int i = range.start(); i < range.end(); i++) {
          // don't change it if it's already set to being unavailable
          if(minuteStatus[i] != MANDATORY_UNAVAILABLE) {
            if(status == MANDATORY_UNAVAILABLE) {
              minuteStatus[i] = MANDATORY_UNAVAILABLE;
            }
            else {
              // if it's available but some attendees can't make it
              // then just subtract the ones who can't make it from 
              // the status (so the number of optional attendees who can't make
              // it will compound)
              minuteStatus[i] -=  status;

              // if a new minimum status is found, update the min status champion
              if (minuteStatus[i] < minStatus) {
                minStatus = minuteStatus[i];
              }
            }
          }
        }
      }
    }

    for(int status = 0; status >= minStatus; status--) {
      // an array list of available times 
      ArrayList<TimeRange> availableTimes = (ArrayList<TimeRange>) findTimesForStatus(request, minuteStatus, status);

      // as soon as the times are found return it, bc status only declines after this point
      if(availableTimes.size() > 0) return availableTimes;
    }

    // if there are no available times, return an empty array list
    return new ArrayList<TimeRange>();
  }
  
  /**
   * A private helper method to determine the amount of overlap between
   * two String Collections
   *
   * @return the number of elements the two Collections have in common
   */
  private int amountOfOverlap(Collection<String> left, Collection<String> right) {
    int overlap = 0;

    for(String leftElement: left)
      for(String rightElement: right)
        if(leftElement.equals(rightElement))
          overlap++;

    return overlap;
  }

  /** 
   * A private helper method that locates the times that work for the current status (which 
   * indicates how many optional attendees can make it). It will return whatever time ranges
   * are of a long enough duration.
   * 
   * @return the time ranges that allow this number of optional attendees and are long enough
   */
  private Collection<TimeRange> findTimesForStatus(MeetingRequest request, int[] minuteStatus, int status) {
    ArrayList<TimeRange> availableTimes = new ArrayList<TimeRange>();
    int start = 0;
    boolean wasLastMinuteAvailable = minuteStatus[0] != MANDATORY_UNAVAILABLE && minuteStatus[0] >= status;
  
    for(int i = 0; i < minuteStatus.length; i++) {
    
      // checks if the current minute is available by checking that mandatory attendees are available
      // and the status is same or better than current status
      boolean currentMinuteAvailable = minuteStatus[i] != MANDATORY_UNAVAILABLE && minuteStatus[i] >= status;

      if(wasLastMinuteAvailable) {
        // If the previous minute was available, but the current minute is unavailable or if it's
        // the end of the day, this is the end of an available time range. If the time range is longer 
        // than the required duration, it's recorded as an available time range.
        if(! currentMinuteAvailable || i == minuteStatus.length - 1) {
          int end = i;
          int duration = end - start;

          if(duration >= request.getDuration()) {
            availableTimes.add(TimeRange.fromStartEnd(start, end - 1, true)); // add time range (inclusive of start & end) 
          }
        
          wasLastMinuteAvailable = false;
        }      
      }
      else {
        // If the last minute was unavailable, but this minute is available, then this is the beginning
        // of a new available time range, so start will be set to this minute and wasLastMinuteAvailable to true.
        if(currentMinuteAvailable) {
          start = i;
          wasLastMinuteAvailable = true;
        }
      }
    }

    return availableTimes;
  }
}
