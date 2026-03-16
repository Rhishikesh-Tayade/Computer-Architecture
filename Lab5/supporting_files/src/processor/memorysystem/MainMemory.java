package processor.memorysystem;

import generic.Element;
import generic.Event;
import generic.MemoryReadEvent;
import generic.MemoryResponseEvent;
import generic.MemoryWriteEvent;
import generic.Simulator;
import processor.Clock;

public class MainMemory implements Element {
	int[] memory;

	public MainMemory() {
		memory = new int[65536];
	}

	public int getWord(int address) {
		return memory[address];
	}

	public void setWord(int address, int value) {
		memory[address] = value;
	}

	public String getContentsAsString(int startingAddress, int endingAddress) {
		if (startingAddress == endingAddress)
			return "";

		StringBuilder sb = new StringBuilder();
		sb.append("\nMain Memory Contents:\n\n");
		for (int i = startingAddress; i <= endingAddress; i++) {
			sb.append(i + "\t\t: " + memory[i] + "\n");
		}
		sb.append("\n");
		return sb.toString();
	}

	/**
	 * Handle a memory event (read or write).
	 * For MemoryRead: the latency was already modeled by the requester scheduling
	 * this event at currentTime + mainMemoryLatency. We respond immediately by
	 * sending a MemoryResponseEvent at the current clock cycle so it fires this
	 * same cycle.
	 * For MemoryWrite: perform the write directly.
	 */
	@Override
	public void handleEvent(Event e) {
		if (e.getEventType() == Event.EventType.MemoryRead) {
			MemoryReadEvent event = (MemoryReadEvent) e;
			int value = getWord(event.getAddressToReadFrom());
			// Respond to the requesting element immediately (same cycle)
			Simulator.getEventQueue().addEvent(
					new MemoryResponseEvent(
							Clock.getCurrentTime(),
							this,
							event.getRequestingElement(),
							value));
		} else if (e.getEventType() == Event.EventType.MemoryWrite) {
			MemoryWriteEvent event = (MemoryWriteEvent) e;
			setWord(event.getAddressToWriteTo(), event.getValueToWrite());
			// Notify the requesting element (MemoryAccess) that the write is done
			// Use MemoryResponseEvent with value=0 (ignored for store)
			Simulator.getEventQueue().addEvent(
					new MemoryResponseEvent(
							Clock.getCurrentTime(),
							this,
							event.getRequestingElement(),
							0));
		}
	}
}
