package generic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import processor.Clock;

/**
 * EventQueue - a sorted list of pending events ordered by event time.
 * Events are fired (processed) when the clock reaches their scheduled time.
 */
public class EventQueue {

    ArrayList<Event> eventQueue;

    public EventQueue() {
        eventQueue = new ArrayList<Event>();
    }

    /**
     * Add an event to the queue, maintaining sorted order by event time.
     */
    public void addEvent(Event event) {
        eventQueue.add(event);
        Collections.sort(eventQueue, new Comparator<Event>() {
            public int compare(Event a, Event b) {
                return Long.compare(a.getEventTime(), b.getEventTime());
            }
        });
    }

    /**
     * Process all events whose event time equals the current clock cycle.
     * Handling an event may generate more events.
     */
    public void processEvents() {
        long currentTime = Clock.getCurrentTime();
        // Use index-based iteration since handleEvent may add new events
        int i = 0;
        while (i < eventQueue.size()) {
            Event e = eventQueue.get(i);
            if (e.getEventTime() == currentTime) {
                eventQueue.remove(i);
                e.getProcessingElement().handleEvent(e);
                // Do NOT increment i: list shifted left, check same index again
            } else if (e.getEventTime() > currentTime) {
                break; // Queue is sorted; no more events for this cycle
            } else {
                i++;
            }
        }
    }

    /**
     * Deletes all events in the queue that were requested by a specific element.
     * Used for flushing the pipeline or invalidating pending fetches on a branch.
     */
    public void deleteEvents(Element requestingElement) {
        for (int i = 0; i < eventQueue.size(); i++) {
            if (eventQueue.get(i).getRequestingElement() == requestingElement) {
                eventQueue.remove(i);
                i--; // Adjust index after removal
            }
        }
    }
}
