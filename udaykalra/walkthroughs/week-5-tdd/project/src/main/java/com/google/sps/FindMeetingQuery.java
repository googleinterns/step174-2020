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
    // Create an output collection holding a starting full day block.
    Collection<TimeRange> rangeOutput = new HashSet<>();
    TimeRange fullDay = TimeRange.fromStartDuration(0, 24 * 60);
    rangeOutput.add(fullDay);

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
    for (Event toCheck : events) {
      boolean attendeePresent = false;
      for (String attendee : toCheck.getAttendees()) {
        if (attendees.contains(attendee)) {
          attendeePresent = true;
        }
      }
      if (attendeePresent) {
        busyTimes.add(toCheck.getWhen());
      }
    }

    // Create an external output to hold final chopped intervals output.
    Collection<TimeRange> actualOutput = new HashSet<>();
    actualOutput.add(fullDay);

    // Iteratively compare output intervals to busy times to chop day into final output.
    for (TimeRange busy : busyTimes) {
      for (TimeRange toChop : rangeOutput) {
        // Obtain output and busy time range data.
        int stockStart = toChop.start();
        int stockEnd = toChop.end();

        int busyStart = busy.start();
        int busyEnd = busy.end();

        if (toChop.overlaps(busy)) {
          actualOutput.remove(toChop);
          /*
           * Case 1: Busy meeting at start of free interval and ends sooner.
           * Reduce length of free interval.
           */
          if ((stockStart == busyStart) && (stockEnd > busyEnd)) {
            TimeRange replaceTime = TimeRange.fromStartDuration(busyEnd, stockEnd - busyEnd);
            actualOutput.add(replaceTime);
          }
          /*
           * Case 2: Busy meeting starts during free interval and ends at same time.
           * Reduce length of free interval.
           */
          else if ((stockEnd == busyEnd) && (stockStart < busyStart)) {
            TimeRange replaceTime = TimeRange.fromStartDuration(stockStart, busyStart - stockStart);
            actualOutput.add(replaceTime);
          }
          /*
           * Case 3: Busy meeting starts during free interval and ends after.
           * Reduce length of free interval.
           */
          else if ((stockStart < busyStart) && (stockEnd < busyEnd)) {
            TimeRange replaceTime = TimeRange.fromStartDuration(stockStart, busyStart - stockStart);
            actualOutput.add(replaceTime);
          }
          /*
           * Case 4: Busy meeting is completely overlapped by free interval.
           * Chop out of free interval.
           */
          else if ((stockStart < busyStart) && (stockEnd > busyEnd)) {
            TimeRange replaceTimeA =
                TimeRange.fromStartDuration(stockStart, busyStart - stockStart);
            TimeRange replaceTimeB = TimeRange.fromStartDuration(busyEnd, stockEnd - busyEnd);
            actualOutput.add(replaceTimeA);
            actualOutput.add(replaceTimeB);
          }
        }
        if (busy.overlaps(toChop)) {
          actualOutput.remove(toChop);
          /*
           * Case 5: Busy meeting starts before free interval and ends during.
           * Reduce length of free interval.
           */
          if ((stockStart > busyStart) && (stockEnd > busyEnd)) {
            TimeRange replaceTime = TimeRange.fromStartDuration(busyEnd, stockEnd - busyEnd);
            actualOutput.add(replaceTime);
          }
        }
      }

      // Mirror new chopped time intervals onto next stock times.
      rangeOutput.clear();
      rangeOutput.addAll(actualOutput);
    }

    // Eliminate invalid time intervals by duration.
    for (TimeRange index : actualOutput) {
      if (index.duration() < requestDuration) {
        actualOutput.remove(index);
      }
    }

    // Convert to list and sort.
    List outlist = new ArrayList(actualOutput);
    Collections.sort(outlist, TimeRange.ORDER_BY_START);
    return outlist;
  }
}
