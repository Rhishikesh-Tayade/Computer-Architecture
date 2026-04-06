package generic;

/**
 * Event carrying the response from main memory after a read request.
 * Created by MainMemory.handleEvent() in response to a MemoryReadEvent.
 * The processingElement is the requesting unit (InstructionFetch or
 * MemoryAccess).
 */
public class MemoryResponseEvent extends Event {

    int value;
    int address;

    public MemoryResponseEvent(long eventTime, Element requestingElement, Element processingElement,
            int value, int address) {
        super(eventTime, EventType.MemoryResponse, requestingElement, processingElement);
        this.value = value;
        this.address = address;
    }

    public int getValue() {
        return value;
    }

    public int getAddress() {
        return address;
    }
}
