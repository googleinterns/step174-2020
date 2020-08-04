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
  private enum Overlap {
    OVERLAP_WITH_SAME_START,
    OVERLAP_WITH_SAME_END,
    OVERLAPS_END,
    OVERLAPS_START,
    OVERLAP_CONTAINS,
    NO_OVERLAP
  }

  /**
   * Returns a collection of TimeRange objects to indicate available meeting times
   * for indicated people and duration given through a request object. Determines
   * whether or not to include optional attendees.
   */
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    // Perform queries with and without optional attendees.
    Collection<TimeRange> queryWithoutOptionalAttendees = query(events, request, false);
    Collection<TimeRange> queryIncludingOptionalAttendees = query(events, request, true);

    // Return optional attendee checks if valid.
    if ((queryIncludingOptionalAttendees.size() != 0) || (request.getAttendees().size() == 0)) {
      return queryIncludingOptionalAttendees;
    } else {
      return queryWithoutOptionalAttendees;
    }
  }

  /**
   * Returns a collection of TimeRange objects to indicate available meeting times
   * for indicated people and duration given through a request object, given
   * whether or not to include optional attendees.
   */
  public Collection<TimeRange> query(
      Collection<Event> events, MeetingRequest request, boolean includesOptionalAttendees) {
    /* Create an output collection with a full day to cut(shorten or split)
     * into the available ranges.
     */
    TimeRange fullDay = TimeRange.WHOLE_DAY;
    Collection<TimeRange> availableRanges = new HashSet<>();
    availableRanges.add(fullDay);

    // Obtain data from the request.
    Collection<String> attendees = new HashSet<>();
    attendees.addAll(request.getAttendees());
    
    // Include Optional Attendees if indicated.
    if (includesOptionalAttendees) {
      attendees.addAll(request.getOptionalAttendees());
    }

    long requestDuration = request.getDuration();
    Collection<TimeRange> busyRanges = new HashSet<>();

    

    // Detect relevant busy time ranges based on attendees.
    for (Event event : events) {
      if (!Collections.disjoint(attendees, event.getAttendees())) {
        busyRanges.add(event.getWhen());
      }
    }

    /* Iteratively cut availableRange's times based on
     * overlap with busy events and store results in availableRanges.
     */
    for (TimeRange busyRange : busyRanges) {
      // For a single busy event, check all time ranges for overlap and cut
      removeOverlapsWithTimeRange(busyRange, availableRanges);
    }

    // Eliminate invalid time intervals by duration.
    for (TimeRange timeRange : availableRanges) {
      if (timeRange.duration() < requestDuration) {
        availableRanges.remove(timeRange);
      }
    }

    // Convert to list and sort.
    List sortedAvailableRanges = new ArrayList(availableRanges);
    Collections.sort(sortedAvailableRanges, TimeRange.ORDER_BY_START);
    return sortedAvailableRanges;
  }

  /**
   * Mutates timeRanges by removing the time range defined in possiblyOverlappingTimeRange from any
   * overlapping TimeRange objects it contains. Removal is carried our through either:
   *  1. trimming the TimeRange into a shorter one,
   *  2. splitting the TimeRange and trimming the resulting TimeRanges.
   */
  private void removeOverlapsWithTimeRange(
      TimeRange possiblyOverlappingTimeRange, Collection timeRanges) {
    // Make a snapshot of TimeRanges for each busy check
    Collection<TimeRange> rangesSnapshot = new HashSet<>();
    rangesSnapshot.addAll(timeRanges);

    for (TimeRange currentTimeRange : rangesSnapshot) {
      // Check for overlap and adjust currentTimeRange
      if ((currentTimeRange.overlaps(possiblyOverlappingTimeRange))
          || (possiblyOverlappingTimeRange.overlaps(currentTimeRange))) {
        timeRanges.remove(currentTimeRange);

        // Get TimeRange(s) to replace currentTimeRange
        Collection<TimeRange> replacementRanges =
            getReplacementForOverlappedTimeRange(possiblyOverlappingTimeRange, currentTimeRange);
        timeRanges.addAll(replacementRanges);
      }
    }
  }

  /**
   * Returns a collection of TimeRanges to replace an overlap with overlappingTimeRange. The
   * replacement omits the overlap through either:
   *  1. trimming the TimeRange into a shorter one,
   *  2. splitting the TimeRange and trimming the resulting TimeRanges.
   * If events do not overlap, the returned collection will be empty.
   */
  private Collection<TimeRange> getReplacementForOverlappedTimeRange(
      TimeRange overlappingTimeRange, TimeRange overlappedTimeRange) {
    Collection<TimeRange> replacementRanges = new HashSet<>();
    // Obtain timeRange and overlappingTimeRange start/end data..
    int overlappedTimeRangeStartMinute = overlappedTimeRange.start();
    int overlappedTimeRangeEndMinute = overlappedTimeRange.end();

    int overlappingStartMinute = overlappingTimeRange.start();
    int overlappingEndMinute = overlappingTimeRange.end();

    Overlap overlapType = classifyOverlap(overlappingTimeRange, overlappedTimeRange);

    // Case 1&4: Trim front of overlappedTimeRange.
    if ((overlapType == Overlap.OVERLAP_WITH_SAME_START) || (overlapType == Overlap.OVERLAPS_START)) {
      TimeRange replaceTime = TimeRange.fromStartDuration(
          overlappingEndMinute, overlappedTimeRangeEndMinute - overlappingEndMinute);
      replacementRanges.add(replaceTime);
    }

    // Case 2&3: Trim end of overlappedTimeRange.
    else if ((overlapType == Overlap.OVERLAP_WITH_SAME_END) || (overlapType == Overlap.OVERLAPS_END)) {
      TimeRange replaceTime = TimeRange.fromStartDuration(
          overlappedTimeRangeStartMinute, overlappingStartMinute - overlappedTimeRangeStartMinute);
      replacementRanges.add(replaceTime);
    }

    // Case 5: Chop overlappingTimeRange out of overlappedTimeRange.
    else if (overlapType == Overlap.OVERLAP_CONTAINS) {
      TimeRange replaceTimeA = TimeRange.fromStartDuration(
          overlappedTimeRangeStartMinute, overlappingStartMinute - overlappedTimeRangeStartMinute);
      TimeRange replaceTimeB = TimeRange.fromStartDuration(
          overlappingEndMinute, overlappedTimeRangeEndMinute - overlappingEndMinute);
      replacementRanges.add(replaceTimeA);
      replacementRanges.add(replaceTimeB);
    }
    return replacementRanges;
  }

  /**
   * Returns integer representing overlap case type:
   *  1. OVERLAP_WITH_SAME_START - TimeRanges start at same time and overlappingTimeRange
   *     ends sooner.
   *  2. OVERLAP_WITH_SAME_END - overlappingTimeRange starts during timeRange and
   *     both end together.
   *  3. OVERLAPS_END - overlappingTimeRange starts during timeRange and ends after.
   *  4. OVERLAPS_START - overlappingTimeRange starts before timeRange and ends
   *     during.
   *  5. OVERLAP_CONTAINS - overlappingTimeRange is completely overlapped by
   *     timeRange.
   *  0. Does not overlap
   */
  private Overlap classifyOverlap(TimeRange overlappingTimeRange, TimeRange timeRange) {
    // Obtain timeRange and overlappingTimeRange start/end data..
    int timeRangeStartMinute = timeRange.start();
    int timeRangeEndMinute = timeRange.end();

    int overlappingStartMinute = overlappingTimeRange.start();
    int overlappingEndMinute = overlappingTimeRange.end();

    // Define Overlap Cases
    if ((timeRangeStartMinute == overlappingStartMinute)
        && (timeRangeEndMinute > overlappingEndMinute)) {
      return Overlap.OVERLAP_WITH_SAME_START;
    } else if ((timeRangeEndMinute == overlappingEndMinute)
        && (timeRangeStartMinute < overlappingStartMinute)) {
      return Overlap.OVERLAP_WITH_SAME_END;
    } else if ((timeRangeStartMinute < overlappingStartMinute)
        && (timeRangeEndMinute < overlappingEndMinute)) {
      return Overlap.OVERLAPS_END;
    } else if ((timeRangeStartMinute > overlappingStartMinute)
        && (timeRangeEndMinute > overlappingEndMinute)) {
      return Overlap.OVERLAPS_START;
    } else if ((timeRangeStartMinute < overlappingStartMinute)
        && (timeRangeEndMinute > overlappingEndMinute)) {
      return Overlap.OVERLAP_CONTAINS;
    } else {
      return Overlap.NO_OVERLAP;
    }
  }
}
