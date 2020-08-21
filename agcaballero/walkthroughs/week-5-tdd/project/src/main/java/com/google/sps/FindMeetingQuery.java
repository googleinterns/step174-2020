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
import java.util.HashMap;
import java.util.Map;

/*
 * A class to find a time that satisfies a meeting query and ensures
 * all mandatory attendees can attend and the most possible
 * optional attendees can attend.
 */
public final class FindMeetingQuery {
   
  /** the number of minutes in a day */
  private static final int MINUTES_IN_DAY = 24 * 60;

  /**
   * A private class that holds the necessary elements of availability:
   * can all mandatory attendees attend and how many optional attendees can't come.
   */
  private class Availability {
    /** holds whether mandatory attendees are available at this time */
    private boolean areMandatoryAttendeesAllAvailable;
    /** holds the number of optional attendees that can't attend */
    private int numberOfOptionalAttendeesUnavailable;

    /**
     * Constructs the default type of Availability which is that everyone can attend 
     * hence areMandatoryAttendeesAllAvailable is true and number of optional attendees
     * that are unavailable is 0.
     */
    public Availability() {
      areMandatoryAttendeesAllAvailable = true;
      numberOfOptionalAttendeesUnavailable = 0;
    }

    /**
     * If any one (or multiple) of the mandatory attendees can't make it, 
     * sets the mandatory availability of this time to false.
     */
    public void mandatoryAttendeeUnavailable() {
      areMandatoryAttendeesAllAvailable = false;
    }

    /**
     * Increments the number of optional attendees that can't come by given number increase.
     */
    public void increaseOptionalAttendeeUnavailability(int increase) {
      numberOfOptionalAttendeesUnavailable += increase;
    }
  }
  
  /**
   * Takes the events of the day and information about a potential meeting 
   * and returns the time ranges in which this meeting could be scheduled
   *
   * @return a collection of TimeRanges in which the meeting could be scheduled
   * @param events the collection of events scheduled for that day
   * @param request the meeting request to be fulfilled (will have duration & attendees)
   */ 
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    // The +1 is to account for last minute in day
    Availability[] minuteAvailability = new Availability[MINUTES_IN_DAY + 1];

    // this method fills the minuteAvailability array with correct availabilities and returns
    // the max number of unavailable optional attendees for any time that day (for the attendees
    // of this meeting)
    int maxUnavailableOptionalAttendeesFound = fillAvailabilityArrayForDay(events, request, minuteAvailability);

    for (int numberOfUnavailableOptionalAttendees = 0; 
        numberOfUnavailableOptionalAttendees <= maxUnavailableOptionalAttendeesFound; 
        numberOfUnavailableOptionalAttendees++) {
      // an array list of available times 
      ArrayList<TimeRange> availableTimes = (ArrayList<TimeRange>) findTimesBasedOnOptionalAttendeeAvailability(request, 
        minuteAvailability, numberOfUnavailableOptionalAttendees);

      // as soon as the times are found return it, bc the number of optional attendees who can't come
      // will only increase after this point
      if (availableTimes.size() > 0) return availableTimes;
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

    // pre-process the collections for the number of times an element occurred
    Map<String, Integer> leftMap = countOccurrences(left);
    Map<String, Integer> rightMap = countOccurrences(right);

    // increase overlap by the number of times a given element occurred
    // in both collections
    for (String key: leftMap.keySet()) {
      if(rightMap.containsKey(key)) {
        overlap += Math.min(leftMap.get(key), rightMap.get(key));
      }
    }

    return overlap;
  }

  /**
   * Takes in a String collection and returns a map with strings from the
   * collection as keys and the number of times that string 
   * appeared in the collection as the value.
   * 
   * @param strings the String collection to count occurrences for
   * @return a map containining the strings from the colleciton and the number of times they occurred
   */
  private Map<String, Integer> countOccurrences(Collection<String> strings) {
    Map<String, Integer> map = new HashMap<String, Integer>();

    for (String string: strings) {
      if(map.containsKey(string)) {
        map.put(string, map.get(string) + 1);
      } else {
        map.put(string, 1);
      }
    }

    return map;
  }

  /**
   * Helper method to fill the availability array for a given meeting on this day
   * where each index in the array represents that minute of the day.
   * 
   * @param events the events to occur on this day
   * @param request the information needed about the current meeting you're looking for availability for
   * @param minuteAvailability the array to fill in its availabilities (based on events) for this day
   * @return the max number of unavailable attendees found (for later purposes)
   */
  private int fillAvailabilityArrayForDay(Collection<Event> events, MeetingRequest request, Availability[] minuteAvailability) {
    // fill the minute availability array with the default (the assumption all attendees can come)
    for (int i = 0; i < minuteAvailability.length; i++) {
      minuteAvailability[i] = new Availability();
    }

    // keep a champion for the maximum number of unavailable optional ttendees found so that 
    // when analyzing the minuteAvailability array, we can determine where to stop. 
    // starts at 0 b/c that's the lowest number of unavailable optional attendees possible
    int maxUnavailableOptionalAttendeesFound = 0;

    Collection<String> attendees = request.getAttendees();
    Collection<String> optionalAttendees = request.getOptionalAttendees();

    // fill the minuteAvailability array with the correct availabilities based on events given
    for (Event event: events) {
      Availability availabilityDuringEvent = new Availability();
      int mandatoryOverlap = amountOfOverlap(event.getAttendees(), attendees);

      if (mandatoryOverlap > 0) {
        availabilityDuringEvent.mandatoryAttendeeUnavailable();
      }

      int optionalOverlap = amountOfOverlap(event.getAttendees(), optionalAttendees);
      availabilityDuringEvent.increaseOptionalAttendeeUnavailability(optionalOverlap);
       
      // if availability during this event isn't just the default, update the array
      if (!availabilityDuringEvent.areMandatoryAttendeesAllAvailable 
          || availabilityDuringEvent.numberOfOptionalAttendeesUnavailable != 0) {
        TimeRange range = event.getWhen();

        for (int i = range.start(); i < range.end(); i++) {
          // update the mandatory attendee availability of this minute
          if (!availabilityDuringEvent.areMandatoryAttendeesAllAvailable) {
            minuteAvailability[i].mandatoryAttendeeUnavailable();
          }
          
          // update the optional availability of this minute
          minuteAvailability[i].increaseOptionalAttendeeUnavailability(availabilityDuringEvent.numberOfOptionalAttendeesUnavailable);
          
          int currentNumberOfUnavailableOptionalAttendees = minuteAvailability[i].numberOfOptionalAttendeesUnavailable;
          
          // if the current number of unavailable attendees is the highest number yet, update the champion
          if (currentNumberOfUnavailableOptionalAttendees > maxUnavailableOptionalAttendeesFound) {
            maxUnavailableOptionalAttendeesFound = currentNumberOfUnavailableOptionalAttendees;
          }
        }
      }
    }

    return maxUnavailableOptionalAttendeesFound;
  }

  /** 
   * A private helper method that locates the times where the number of optional attendees
   * that can't make it is below a cap. It will return whatever time ranges
   * are of a long enough duration.
   * 
   * @return the time ranges that allow this number of optional attendees and are long enough
   */
  private Collection<TimeRange> findTimesBasedOnOptionalAttendeeAvailability(MeetingRequest request, 
    Availability[] minuteAvailability, int maxOptionalAttendeesUnavailable) {

    ArrayList<TimeRange> availableTimes = new ArrayList<TimeRange>();
    int start = 0;
    boolean wasLastMinuteAvailable = minuteAvailability[0].areMandatoryAttendeesAllAvailable 
        && minuteAvailability[0].numberOfOptionalAttendeesUnavailable <= maxOptionalAttendeesUnavailable;
  
    for (int i = 0; i < minuteAvailability.length; i++) {
    
      // checks if the current minute is available by checking that mandatory attendees are available
      // and that the number of optional attendees that can't make it is under the max
      boolean currentMinuteAvailable = minuteAvailability[i].areMandatoryAttendeesAllAvailable 
        && minuteAvailability[i].numberOfOptionalAttendeesUnavailable <= maxOptionalAttendeesUnavailable;

      if (wasLastMinuteAvailable) {
        // If the previous minute was available, but the current minute is unavailable or if it's
        // the end of the day, this is the end of an available time range. If the time range is longer 
        // than the required duration, it's recorded as an available time range.
        if (! currentMinuteAvailable || i == minuteAvailability.length - 1) {
          int end = i;
          int duration = end - start;

          if (duration >= request.getDuration()) {
            availableTimes.add(TimeRange.fromStartEnd(start, end - 1, true)); // add time range (inclusive of start & end) 
          }
        
          wasLastMinuteAvailable = false;
        }      
      } else {
        // If the last minute was unavailable, but this minute is available, then this is the beginning
        // of a new available time range, so start will be set to this minute and wasLastMinuteAvailable to true.
        if (currentMinuteAvailable) {
          start = i;
          wasLastMinuteAvailable = true;
        }
      }
    }

    return availableTimes;
  }
}
