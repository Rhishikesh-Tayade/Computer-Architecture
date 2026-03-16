package processor.pipeline;

import configuration.Configuration;
import generic.Element;
import generic.Event;
import generic.MemoryReadEvent;
import generic.MemoryResponseEvent;
import generic.Simulator;
import processor.Clock;
import processor.Processor;

public class InstructionFetch implements Element {

	Processor containingProcessor;
	IF_EnableLatchType IF_EnableLatch;
	IF_OF_LatchType IF_OF_Latch;
	EX_IF_LatchType EX_IF_Latch;

	public InstructionFetch(Processor containingProcessor, IF_EnableLatchType iF_EnableLatch,
			IF_OF_LatchType iF_OF_Latch, EX_IF_LatchType eX_IF_Latch) {
		this.containingProcessor = containingProcessor;
		this.IF_EnableLatch = iF_EnableLatch;
		this.IF_OF_Latch = iF_OF_Latch;
		this.EX_IF_Latch = eX_IF_Latch;
	}

	public void performIF() {
		if (IF_EnableLatch.isIF_enable()) {
			// If already waiting for memory response, do nothing (stall)
			if (IF_EnableLatch.isIF_busy()) {
				return;
			}
			// Post a MemoryReadEvent to the main memory with latency
			int currentPC = containingProcessor.getRegisterFile().getProgramCounter();
			Simulator.getEventQueue().addEvent(
					new MemoryReadEvent(
							Clock.getCurrentTime() + Configuration.mainMemoryLatency,
							this,
							containingProcessor.getMainMemory(),
							currentPC));
			// Increment PC now (speculative — will be overridden if branch taken)
			containingProcessor.getRegisterFile().setProgramCounter(currentPC + 1);
			IF_EnableLatch.setIF_busy(true);
		}
	}

	/**
	 * Called when a branch is taken to discard the current fetch.
	 */
	public void invalidatePendingFetch() {
		IF_EnableLatch.setIF_busy(false);
		Simulator.getEventQueue().deleteEvents(this);
	}

	/**
	 * Called by EventQueue when memory responds with the fetched instruction.
	 */
	@Override
	public void handleEvent(Event e) {
		if (e.getEventType() == Event.EventType.MemoryResponse) {
			// If OF latch is still occupied (previous instruction not yet consumed),
			// re-schedule
			if (IF_OF_Latch.isOF_enable()) {
				e.setEventTime(Clock.getCurrentTime() + 1);
				Simulator.getEventQueue().addEvent(e);
				return;
			}
			MemoryResponseEvent event = (MemoryResponseEvent) e;
			IF_OF_Latch.setInstruction(event.getValue());
			IF_OF_Latch.setOF_enable(true);
			IF_EnableLatch.setIF_busy(false);
		}
	}
}
