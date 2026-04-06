package processor.pipeline;

import generic.Element;
import generic.Event;
import generic.MemoryReadEvent;
import generic.MemoryResponseEvent;
import generic.MemoryWriteEvent;
import generic.Simulator;
import processor.Clock;
import processor.Processor;

public class MemoryAccess implements Element {
	Processor containingProcessor;
	EX_MA_LatchType EX_MA_Latch;
	MA_RW_LatchType MA_RW_Latch;

	// Store opcode and rd while waiting for memory response
	generic.Instruction.OperationType pendingOpcode;
	int pendingRd;

	public MemoryAccess(Processor containingProcessor, EX_MA_LatchType eX_MA_Latch, MA_RW_LatchType mA_RW_Latch) {
		this.containingProcessor = containingProcessor;
		this.EX_MA_Latch = eX_MA_Latch;
		this.MA_RW_Latch = mA_RW_Latch;
	}

	public void performMA() {
		// If waiting for memory, stall
		if (MA_RW_Latch.isMA_busy()) {
			return;
		}

		if (EX_MA_Latch.isMA_enable()) {
			generic.Instruction.OperationType opcode = EX_MA_Latch.getOpcode();
			int aluResult = EX_MA_Latch.getAluResult();
			int rd = EX_MA_Latch.getRd();
			int result = aluResult;

			if (opcode == generic.Instruction.OperationType.load) {
				// Post memory read event — fires at currentTime + mainMemoryLatency
				pendingOpcode = opcode;
				pendingRd = rd;
				Simulator.getEventQueue().addEvent(
						new MemoryReadEvent(
								Clock.getCurrentTime(),
								this,
								containingProcessor.getL1dCache(),
								aluResult));
				EX_MA_Latch.setMA_enable(false);
				MA_RW_Latch.setMA_busy(true);
				return;

			} else if (opcode == generic.Instruction.OperationType.store) {
				// Post memory write event — value to store from EX stage
				int storeValue = EX_MA_Latch.getRs2Val();
				pendingOpcode = opcode;
				pendingRd = rd;
				Simulator.getEventQueue().addEvent(
						new MemoryWriteEvent(
								Clock.getCurrentTime(),
								this,
								containingProcessor.getL1dCache(),
								aluResult,
								storeValue));
				EX_MA_Latch.setMA_enable(false);
				MA_RW_Latch.setMA_busy(true);
				return;

			} else {
				// Non-memory operation: write result directly to MA_RW latch
				MA_RW_Latch.setOpcode(opcode);
				MA_RW_Latch.setResult(result);
				MA_RW_Latch.setRd(rd);
				EX_MA_Latch.setMA_enable(false);
				MA_RW_Latch.setRW_enable(true);
			}
		}
	}

	/**
	 * Called when memory responds after a load (MemoryResponse event),
	 * or when a write event fires at MainMemory.handleEvent().
	 * After MemoryWrite, MainMemory does NOT send a response — we handle
	 * store completion via the MemoryWriteEvent itself being handled by
	 * the memory (no response needed). Instead we use a custom approach:
	 * for stores, after scheduling the write event, we still need to
	 * populate MA_RW_Latch so RegisterWrite can update statistics.
	 *
	 * For load: memory sends MemoryResponseEvent back here.
	 * For store: we receive nothing from memory — instead we post a self-event
	 * to know when the write is done.
	 */
	@Override
	public void handleEvent(Event e) {
		if (e.getEventType() == Event.EventType.MemoryResponse) {
			// Load completed — fill MA_RW latch
			MemoryResponseEvent event = (MemoryResponseEvent) e;
			MA_RW_Latch.setOpcode(pendingOpcode);
			MA_RW_Latch.setResult(event.getValue());
			MA_RW_Latch.setRd(pendingRd);
			MA_RW_Latch.setMA_busy(false);
			MA_RW_Latch.setRW_enable(true);
		}
		// MemoryWrite events are handled by MainMemory; we handle completion via
		// MemoryWriteDoneEvent below
		// Since we cannot get a "done" signal for write without another event type,
		// We repurpose: for store, MainMemory's handleEvent fires the write, and we
		// schedule a separate MemoryResponseEvent back here with the aluResult as
		// "value".
		// This is handled in our modified scheduleStoreCompletion approach — see below.
	}

	/**
	 * Called by a "store completion" event (we reuse MemoryResponseEvent with
	 * value=0
	 * and a special flag — actually we'll use a different approach in handleEvent).
	 * 
	 * Since MemoryWriteEvent goes to MainMemory and we need a signal back to MA,
	 * we need MainMemory to send a MemoryResponseEvent back to us.
	 * We modify the store scheduling to pass "this" as requesting element to
	 * MainMemory
	 * so it sends us a MemoryResponseEvent. The value will be 0 (ignored for
	 * store).
	 */
	public void handleStoreComplete(Event e) {
		MA_RW_Latch.setOpcode(pendingOpcode);
		MA_RW_Latch.setResult(0); // Store doesn't pass a result to RW
		MA_RW_Latch.setRd(pendingRd);
		MA_RW_Latch.setMA_busy(false);
		MA_RW_Latch.setRW_enable(true);
	}
}
