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
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Contains methods for querying availability between attendees.
 */
public final class FindMeetingQuery {
  /**
   * Returns a collection of TimeRange objects to indicate available meeting times
   * for indicated people and duration given through a request object.
   */
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {

    // Perform queries with and without optional attendees.
    Collection<TimeRange> mandatoryCheck = coreQuery(events, request, false);
    Collection<TimeRange> optionalCheck = coreQuery(events, request, true);

    // Return optional attendee checks if valid.
    if ((optionalCheck.size() != 0) || (request.getAttendees().size() == 0)) {
      return optionalCheck;
    } else {
      return mandatoryCheck;
    }
  }
  
  /**
   * Performs interval detection functionality for querying function.
   */
  public Collection<TimeRange> coreQuery(
      Collection<Event> events, MeetingRequest request, boolean withOptional) {
    
    /* Create an output collection with a full day to cut(shorten or split) 
     * into the available ranges.
     */
    TimeRange fullDay = TimeRange.WHOLE_DAY;
    Collection<TimeRange> availableRanges = new HashSet<>();
    availableRanges.add(fullDay);

    // Obtain data from the request.
    Collection<String> attendees = new HashSet<>();
    Collection<String> mandatoryAttendees = request.getAttendees();
    Collection<String> optionalAttendees = request.getOptionalAttendees();
    long requestDuration = request.getDuration();

    Collection<TimeRange> busyTimes = new HashSet<>();

    attendees.addAll(mandatoryAttendees);

    // Include Optional Attendees if indicated.
    if (withOptional) {
      attendees.addAll(optionalAttendees);
    }

    // Detect relevant busy time ranges based on attendees.
    for (Event event : events) {
      boolean attendeePresent = false;
      if (!(Collections.disjoint(attendees, event.getAttendees()))) {
        busyTimes.add(event.getWhen());
      }
    }
    

    /* Iteratively cut availableRange's times based on 
     * overlap with busy events and store results in availableRanges.
     */
    for (TimeRange busy : busyTimes) {
      //Make a snapshot of TimeRanges for each busy check
      Collection<TimeRange> rangesSnapshot = new HashSet<>();
      rangesSnapshot.addAll(availableRanges);
      for (TimeRange toCut : rangesSnapshot) {
        
        //For a single busy event, check all time ranges for overlap and cut
        overlapCut(toCut, busy, availableRanges);
        }
      }
    

    // Eliminate invalid time intervals by duration.
    for (TimeRange index : availableRanges) {
      if (index.duration() < requestDuration) {
        availableRanges.remove(index);
      }
    }

    // Convert to list and sort.
    List outList = new ArrayList(availableRanges);
    Collections.sort(outList, TimeRange.ORDER_BY_START);
    return outList;
  }

  /**
   * Checks whether or not a given time range overlaps with a given busy range and cuts the event
   * shorter in a collection if needed.
   */
  private void overlapCut(TimeRange givenRange, TimeRange busyRange, Collection<TimeRange> whereCut){
      // Obtain output and busy time range data.
        int givenStart = givenRange.start();
        int givenEnd = givenRange.end();

        int busyStart = busyRange.start();
        int busyEnd = busyRange.end();

        if (givenRange.overlaps(busyRange)) {
          whereCut.remove(givenRange);
          /*
           * Case 1: Busy meeting at start of free interval and ends sooner.
           * Reduce length of free interval.
           */
          if ((givenStart == busyStart) && (givenEnd > busyEnd)) {
            TimeRange replaceTime = TimeRange.fromStartDuration(busyEnd, givenEnd - busyEnd);
            whereCut.add(replaceTime);
          }
          /*
           * Case 2: Busy meeting starts during free interval and ends at same time.
           * Reduce length of free interval.
           */
          else if ((givenEnd == busyEnd) && (givenStart < busyStart)) {
            TimeRange replaceTime = TimeRange.fromStartDuration(givenStart, busyStart - givenStart);
            whereCut.add(replaceTime);
          }
          /*
           * Case 3: Busy meeting starts during free interval and ends after.
           * Reduce length of free interval.
           */
          else if ((givenStart < busyStart) && (givenEnd < busyEnd)) {
            TimeRange replaceTime = TimeRange.fromStartDuration(givenStart, busyStart - givenStart);
            whereCut.add(replaceTime);
          }
          /*
           * Case 4: Busy meeting is completely overlapped by free interval.
           * Chop out of free interval.
           */
          else if ((givenStart < busyStart) && (givenEnd > busyEnd)) {
            TimeRange replaceTimeA =
                TimeRange.fromStartDuration(givenStart, busyStart - givenStart);
            TimeRange replaceTimeB = TimeRange.fromStartDuration(busyEnd, givenEnd - busyEnd);
            whereCut.add(replaceTimeA);
            whereCut.add(replaceTimeB);
          }
        }
        if (busyRange.overlaps(givenRange)) {
          whereCut.remove(givenRange);
          /*
           * Case 5: Busy meeting starts before free interval and ends during.
           * Reduce length of free interval.
           */
          if ((givenStart > busyStart) && (givenEnd > busyEnd)) {
            TimeRange replaceTime = TimeRange.fromStartDuration(busyEnd, givenEnd - busyEnd);
            whereCut.add(replaceTime);
          }


  }

}
}
