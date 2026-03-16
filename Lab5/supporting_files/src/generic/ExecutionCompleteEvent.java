package generic;

import generic.Instruction.OperationType;

/**
 * Event fired when a functional unit (ALU, multiplier, divider) completes
 * execution.
 * The EX unit schedules this event at Clock.time + unit_latency.
 */
public class ExecutionCompleteEvent extends Event {

    int aluResult;
    int rd;
    OperationType opcode;
    int rs2Val; // needed for store pass-through / for div remainder

    public ExecutionCompleteEvent(long eventTime, Element requestingElement, Element processingElement,
            int aluResult, int rd, OperationType opcode, int rs2Val) {
        super(eventTime, EventType.ExecutionComplete, requestingElement, processingElement);
        this.aluResult = aluResult;
        this.rd = rd;
        this.opcode = opcode;
        this.rs2Val = rs2Val;
    }

    public int getAluResult() {
        return aluResult;
    }

    public int getRd() {
        return rd;
    }

    public OperationType getOpcode() {
        return opcode;
    }

    public int getRs2Val() {
        return rs2Val;
    }
}
