package generic;

/**
 * Event that requests a memory read from the main memory.
 * Posted by InstructionFetch or MemoryAccess units.
 * mainMemory.handleEvent() will respond with a MemoryResponseEvent.
 */
public class MemoryReadEvent extends Event {

    int addressToReadFrom;

    public MemoryReadEvent(long eventTime, Element requestingElement, Element processingElement,
            int addressToReadFrom) {
        super(eventTime, EventType.MemoryRead, requestingElement, processingElement);
        this.addressToReadFrom = addressToReadFrom;
    }

    public int getAddressToReadFrom() {
        return addressToReadFrom;
    }
}
