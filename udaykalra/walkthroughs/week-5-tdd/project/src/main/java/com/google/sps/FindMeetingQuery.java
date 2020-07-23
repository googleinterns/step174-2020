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
  public static final int OVERLAP_WITH_SAME_START = 1;
  public static final int OVERLAP_WITH_SAME_END = 2;
  public static final int OVERLAPS_END = 3;
  public static final int OVERLAPS_START = 4;
  public static final int OVERLAP_CONTAINS = 5;

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

    Collection<TimeRange> busyRanges = new HashSet<>();

    attendees.addAll(mandatoryAttendees);

    // Include Optional Attendees if indicated.
    if (withOptional) {
      attendees.addAll(optionalAttendees);
    }

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
    for (TimeRange toEvaluate : availableRanges) {
      if (toEvaluate.duration() < requestDuration) {
        availableRanges.remove(toEvaluate);
      }
    }

    // Convert to list and sort.
    List outList = new ArrayList(availableRanges);
    Collections.sort(outList, TimeRange.ORDER_BY_START);
    return outList;
  }

  /**
   * Mutates timeRanges by removing the time range defined in possiblyOverlappingTimeRange from any
   * overlapping TimeRange objects it contains.
   */
  private void removeOverlapsWithTimeRange(
      TimeRange possiblyOverlappingTimeRange, Collection timeRanges) {

    // Make a snapshot of TimeRanges for each busy check
    Collection<TimeRange> rangesSnapshot = new HashSet<>();
    rangesSnapshot.addAll(timeRanges);

    for (TimeRange currentTimeRange : rangesSnapshot) {

      // Check for overlap and adjust currentTimeRange
      if ((currentTimeRange.overlaps(possiblyOverlappingTimeRange)) || (possiblyOverlappingTimeRange.overlaps(currentTimeRange))) {
        timeRanges.remove(currentTimeRange);
        
        //Get TimeRange(s) to replace currentTimeRange
        Collection<TimeRange> replacementRanges = getReplacementForOverlappedTimeRange(possiblyOverlappingTimeRange, currentTimeRange);
        for(TimeRange replacementRange : replacementRanges){
          timeRanges.add(replacementRange);
        }
      }
    }
  }

  /**
   * Returns a collection of TimeRanges to replace an overlap with overlappingTimeRange. The
   * replacement omits the overlap through either:
   *  1. trimming the TimeRange into a shorter one,
   *  2. splitting the TimeRange and trimming the resulting TimeRanges.
   * If events do not overlap, no changes will occur.
   */
  private Collection<TimeRange> getReplacementForOverlappedTimeRange(
      TimeRange overlappingTimeRange, TimeRange toReplace) {

    Collection<TimeRange> replacementRanges = new HashSet<>();
    // Obtain timeRange and overlappingTimeRange start/end data..
    int toReplaceStartMinute = toReplace.start();
    int toReplaceEndMinute = toReplace.end();

    int overlappingStartMinute = overlappingTimeRange.start();
    int overlappingEndMinute = overlappingTimeRange.end();

    int overlapType = classifyOverlap(overlappingTimeRange, toReplace);

    // Case 1&4: Trim front of toReplace.
    if ((overlapType == OVERLAP_WITH_SAME_START) || (overlapType == OVERLAPS_START)) {
      TimeRange replaceTime = TimeRange.fromStartDuration(
          overlappingEndMinute, toReplaceEndMinute - overlappingEndMinute);
      replacementRanges.add(replaceTime);
    }

    // Case 2&3: Trim end of toReplace.
    else if ((overlapType == OVERLAP_WITH_SAME_END) || (overlapType == OVERLAPS_END)) {
      TimeRange replaceTime = TimeRange.fromStartDuration(
          toReplaceStartMinute, overlappingStartMinute - toReplaceStartMinute);
      replacementRanges.add(replaceTime);
    }
    
    // Case 5: Chop overlappingTimeRange out of toReplace.
    else if (overlapType == OVERLAP_CONTAINS) {
      TimeRange replaceTimeA = TimeRange.fromStartDuration(
          toReplaceStartMinute, overlappingStartMinute - toReplaceStartMinute);
      TimeRange replaceTimeB = TimeRange.fromStartDuration(
          overlappingEndMinute, toReplaceEndMinute - overlappingEndMinute);
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
  private int classifyOverlap(TimeRange overlappingTimeRange, TimeRange timeRange) {
    // Obtain timeRange and overlappingTimeRange start/end data..
    int timeRangeStartMinute = timeRange.start();
    int timeRangeEndMinute = timeRange.end();

    int overlappingStartMinute = overlappingTimeRange.start();
    int overlappingEndMinute = overlappingTimeRange.end();

    // Define Overlap Cases
    boolean overlapWithSameStart = (timeRangeStartMinute == overlappingStartMinute)
        && (timeRangeEndMinute > overlappingEndMinute);

    boolean overlapWithSameEnd = (timeRangeEndMinute == overlappingEndMinute)
        && (timeRangeStartMinute < overlappingStartMinute);

    boolean overlapsEnd = (timeRangeStartMinute < overlappingStartMinute)
        && (timeRangeEndMinute < overlappingEndMinute);

    boolean overlapsStart = (timeRangeStartMinute > overlappingStartMinute)
        && (timeRangeEndMinute > overlappingEndMinute);

    boolean containedInCurrent = (timeRangeStartMinute < overlappingStartMinute)
        && (timeRangeEndMinute > overlappingEndMinute);

    // Return appropriate overlap type
    if (overlapWithSameStart) {
      return OVERLAP_WITH_SAME_START;
    } else if (overlapWithSameEnd) {
      return OVERLAP_WITH_SAME_END;
    } else if (overlapsEnd) {
      return OVERLAPS_END;
    } else if (overlapsStart) {
      return OVERLAPS_START;
    } else if (containedInCurrent) {
      return OVERLAP_CONTAINS;
    } else {
      return 0;
    }
  }
}
