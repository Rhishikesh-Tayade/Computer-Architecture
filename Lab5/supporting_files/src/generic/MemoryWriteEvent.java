package generic;

/**
 * Event that requests a memory write to the main memory.
 * Posted by MemoryAccess unit for store instructions.
 */
public class MemoryWriteEvent extends Event {

    int addressToWriteTo;
    int valueToWrite;

    public MemoryWriteEvent(long eventTime, Element requestingElement, Element processingElement,
            int addressToWriteTo, int valueToWrite) {
        super(eventTime, EventType.MemoryWrite, requestingElement, processingElement);
        this.addressToWriteTo = addressToWriteTo;
        this.valueToWrite = valueToWrite;
    }

    public int getAddressToWriteTo() {
        return addressToWriteTo;
    }

    public int getValueToWrite() {
        return valueToWrite;
    }
}
