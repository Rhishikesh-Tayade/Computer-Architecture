package generic;

/**
 * Abstract base class for all events in the discrete event simulator.
 * An event is a tuple: <eventTime, eventType, requestingElement,
 * processingElement, payload>
 */
public abstract class Event {

    public enum EventType {
        MemoryRead,
        MemoryWrite,
        MemoryResponse,
        ExecutionComplete
    }

    long eventTime;
    EventType eventType;
    Element requestingElement;
    Element processingElement;

    public Event(long eventTime, EventType eventType, Element requestingElement, Element processingElement) {
        this.eventTime = eventTime;
        this.eventType = eventType;
        this.requestingElement = requestingElement;
        this.processingElement = processingElement;
    }

    public long getEventTime() {
        return eventTime;
    }

    public void setEventTime(long eventTime) {
        this.eventTime = eventTime;
    }

    public EventType getEventType() {
        return eventType;
    }

    public Element getRequestingElement() {
        return requestingElement;
    }

    public Element getProcessingElement() {
        return processingElement;
    }
}
