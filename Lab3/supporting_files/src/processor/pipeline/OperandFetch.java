package processor.pipeline;

import generic.Instruction;
import generic.Instruction.OperationType;
import generic.Operand.OperandType;
import generic.Operand;
import processor.Processor;

public class OperandFetch {
	Processor containingProcessor;
	IF_OF_LatchType IF_OF_Latch;
	OF_EX_LatchType OF_EX_Latch;
	OperationType[] operations = OperationType.values();
	
	public OperandFetch(Processor containingProcessor, IF_OF_LatchType iF_OF_Latch, OF_EX_LatchType oF_EX_Latch)
	{
		this.containingProcessor = containingProcessor;
		this.IF_OF_Latch = iF_OF_Latch;
		this.OF_EX_Latch = oF_EX_Latch;
	}
	
	public void performOF()
	{
		if(IF_OF_Latch.isOF_enable())
		{
			//TODO

			int inst = IF_OF_Latch.getInstruction();
			int opcode = (inst >> 27) & (0b11111);
			OperationType operation = operations[opcode];
			Operand imm = new Operand();
			Operand rd = new Operand();
			Operand rs2 = new Operand();
			Operand rs1 = new Operand();
			Instruction instruction = new Instruction();
			imm.setOperandType(OperandType.Immediate);
			rs2.setOperandType(OperandType.Register);
			rs1.setOperandType(OperandType.Register);
			instruction.setOperationType(operation);
			rd.setOperandType(OperandType.Register);
			
			int immediate;
			System.out.println("=======\nOF Stage\nOperationType="+operation.toString());
			switch (operation) {
				case add: case sub: case mul: case div: case and: case or: case xor: case slt: case sll: case srl: case sra:
					rs1.setValue((inst>>22) & (0b11111));
					rs2.setValue((inst>>17) & (0b11111));
					rd.setValue((inst>>12) & (0b11111));
					instruction.setSourceOperand1(rs1);
					instruction.setSourceOperand2(rs2);
					instruction.setDestinationOperand(rd);
					System.out.println("rs1="+rs1.getValue()+"\nrs2="+rs2.getValue()+"\nrd="+rd.getValue());
					break;
				case addi: case subi: case muli: case divi: case andi: case ori: case xori: case slti: case slli: case srli: case srai:
					case load: case store:
					rs1.setValue((inst>>22) & (0b11111));
					rd.setValue((inst>>17) & (0b11111));
					immediate = inst & ((1<<17)-1);
					immediate = (immediate << 15)>>15;
					imm.setValue(immediate);
					instruction.setSourceOperand1(rs1);
					instruction.setSourceOperand2(imm);
					instruction.setDestinationOperand(rd);
					System.out.println("rs1="+instruction.getSourceOperand1().getValue()+"\nimm(rs2)="+instruction.getSourceOperand2().getValue()+"\nrd="+instruction.getDestinationOperand().getValue());
					break;
				case beq: case bne: case blt: case bgt:
	
					rs1.setValue((inst>>22) & (0b11111));
					
					rd.setValue((inst>>17) & (0b11111));
					
					immediate = inst & ((1<<17)-1);
					immediate = (immediate << 15)>>15;
					imm.setValue(immediate);
					instruction.setSourceOperand1(rs1);
					instruction.setSourceOperand2(rd);
					instruction.setDestinationOperand(imm);
					System.out.println("rs1="+instruction.getSourceOperand1().getValue()+"\nrs2="+instruction.getSourceOperand2().getValue()+"\nrd(imm)="+instruction.getDestinationOperand().getValue());
					break;
				case jmp:					
					immediate = inst & ((1<<22)-1);
					immediate = (immediate << 10) >> 10;
					imm.setValue(immediate);
					System.out.println("Instruction: "+inst+"\nImm: "+imm.getValue());
					instruction.setDestinationOperand(imm);
					break;
				default:
					break;
			}
	
			System.out.println("========");
			OF_EX_Latch.setInstruction(instruction);
			IF_OF_Latch.setOF_enable(false);
			OF_EX_Latch.setEX_enable(true);
		}
	}

}
