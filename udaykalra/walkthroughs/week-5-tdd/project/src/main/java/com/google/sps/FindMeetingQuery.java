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

    /* Create an output collection with a full day to chop(shorten or split) 
     * into the available ranges.
     */
    Collection<TimeRange> chopContainer = new HashSet<>();
    TimeRange fullDay = TimeRange.WHOLE_DAY;
    chopContainer.add(fullDay);

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

    /* 
     * Create an output collection to hold the time ranges.
     * chopContainer will be iterated over, while the "chopping"
     * of ranges will occur in availableRanges.
     */
    Collection<TimeRange> availableRanges = new HashSet<>();
    availableRanges.add(fullDay);

    /* Iteratively chop chopContainer's "stock" times based on 
     * overlap with busy events and store results in availableRanges.
     */
    for (TimeRange busy : busyTimes) {
      for (TimeRange toChop : chopContainer) {
        // Obtain output and busy time range data.
        int stockStart = toChop.start();
        int stockEnd = toChop.end();

        int busyStart = busy.start();
        int busyEnd = busy.end();

        if (toChop.overlaps(busy)) {
          availableRanges.remove(toChop);
          /*
           * Case 1: Busy meeting at start of free interval and ends sooner.
           * Reduce length of free interval.
           */
          if ((stockStart == busyStart) && (stockEnd > busyEnd)) {
            TimeRange replaceTime = TimeRange.fromStartDuration(busyEnd, stockEnd - busyEnd);
            availableRanges.add(replaceTime);
          }
          /*
           * Case 2: Busy meeting starts during free interval and ends at same time.
           * Reduce length of free interval.
           */
          else if ((stockEnd == busyEnd) && (stockStart < busyStart)) {
            TimeRange replaceTime = TimeRange.fromStartDuration(stockStart, busyStart - stockStart);
            availableRanges.add(replaceTime);
          }
          /*
           * Case 3: Busy meeting starts during free interval and ends after.
           * Reduce length of free interval.
           */
          else if ((stockStart < busyStart) && (stockEnd < busyEnd)) {
            TimeRange replaceTime = TimeRange.fromStartDuration(stockStart, busyStart - stockStart);
            availableRanges.add(replaceTime);
          }
          /*
           * Case 4: Busy meeting is completely overlapped by free interval.
           * Chop out of free interval.
           */
          else if ((stockStart < busyStart) && (stockEnd > busyEnd)) {
            TimeRange replaceTimeA =
                TimeRange.fromStartDuration(stockStart, busyStart - stockStart);
            TimeRange replaceTimeB = TimeRange.fromStartDuration(busyEnd, stockEnd - busyEnd);
            availableRanges.add(replaceTimeA);
            availableRanges.add(replaceTimeB);
          }
        }
        if (busy.overlaps(toChop)) {
          availableRanges.remove(toChop);
          /*
           * Case 5: Busy meeting starts before free interval and ends during.
           * Reduce length of free interval.
           */
          if ((stockStart > busyStart) && (stockEnd > busyEnd)) {
            TimeRange replaceTime = TimeRange.fromStartDuration(busyEnd, stockEnd - busyEnd);
            availableRanges.add(replaceTime);
          }
        }
      }

      // Mirror new chopped time intervals onto next stock times.
      chopContainer.clear();
      chopContainer.addAll(availableRanges);
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
}
