package generic;

import generic.Operand.OperandType;
import java.io.*;
import java.nio.ByteBuffer;


public class Simulator {
		
	static FileInputStream inputcodeStream = null;
	private static String objFilePath = "";
	
	public static void setupSimulation(String assemblyProgramFile, String objectProgramFile)
	{	
		int firstCodeAddress = ParsedProgram.parseDataSection(assemblyProgramFile);
		ParsedProgram.parseCodeSection(assemblyProgramFile, firstCodeAddress);
		objFilePath = objectProgramFile;
		ParsedProgram.printState();
	}
	
	public static void assemble()
	{
		System.out.println("\n===========[ Starting the assembler ]==========\n");
		//TODO your assembler code
		try (FileOutputStream output = new FileOutputStream(objFilePath)) {
			
			// now first get the address of the first instruction
			final int firstInsAdd = ParsedProgram.firstCodeAddress;

			output.write(ByteBuffer.allocate(4).putInt(firstInsAdd).array());

			System.out.println("First line is printed=>");
			System.out.println(firstInsAdd);

			// then put all the variables one after the other
			
			System.out.println("variables being printed =>");

			for(int data_var : ParsedProgram.data){
				output.write(ByteBuffer.allocate(4).putInt(data_var).array());
				System.out.println(data_var);
			}

			//now the hard part putting all the instructions
			for (Instruction ins : ParsedProgram.code){
				int byte_ins = 0;
				int opcode = -1;	
				boolean isFuncSet = false;
				switch (ins.getOperationType()) {
					//R3-Type
					// opcode |  rs1    |  rs2    |  rd     | unused
					// 5bits  |	 5bits  |  5bits  |  5bits  | 12bits
					// arithmetic instruction
					case add: 
					if(!isFuncSet){
						opcode = 0;
						isFuncSet = true;
					}
					case sub:
					if(!isFuncSet){
						opcode = 2;
						isFuncSet = true;
					}
					case mul:
					if(!isFuncSet){
						opcode = 4;
						isFuncSet = true;
					}
					case div:
					if(!isFuncSet){
						opcode = 6;
						isFuncSet = true;
					}
					case and:
					if(!isFuncSet){
						opcode = 8;
						isFuncSet = true;
					}
					case or:
					if(!isFuncSet){
						opcode = 10;
						isFuncSet = true;
					}
					case xor:
					if(!isFuncSet){
						opcode = 12;
						isFuncSet = true;
					}
					case slt:
					if(!isFuncSet){
						opcode = 14;
						isFuncSet = true;
					}
					case sll:
					if(!isFuncSet){
						opcode = 16;
						isFuncSet = true;
					}
					case srl:
					if(!isFuncSet){
						opcode = 18;
						isFuncSet = true;
					}
					case sra:
					if(!isFuncSet){
						opcode = 20;
						isFuncSet = true;
					}
							{
								// setting the opcode
								byte_ins |= (opcode & 0x1F) << 27;
								// setting rs1
								byte_ins |= (ins.getSourceOperand1().getValue() & 0x1F) << 22;
								//setting rs2
								byte_ins |= (ins.getSourceOperand2().getValue() & 0x1F) << 17;
								//setting rd
								byte_ins |= (ins.getDestinationOperand().getValue() & 0x1F) << 12;
							}
							break;
					// R2I-Type
					// opcode |  rs1    |  rd     |  immediate
					// 5bits  |	 5bits  |  5bits  |  17bits
					// arithmetic instruction
					case addi: 
					if(!isFuncSet){
						opcode = 1;
						isFuncSet = true;
					}
					case subi:
					if(!isFuncSet){
						opcode = 3;
						isFuncSet = true;
					}
					case muli:
					if(!isFuncSet){
						opcode = 5;
						isFuncSet = true;
					}
					case divi:
					if(!isFuncSet){
						opcode = 7;
						isFuncSet = true;
					}
					case andi:
					if(!isFuncSet){
						opcode = 9;
						isFuncSet = true;
					}
					case ori:
					if(!isFuncSet){
						opcode = 11;
						isFuncSet = true;
					}
					case xori:
					if(!isFuncSet){
						opcode = 13;
						isFuncSet = true;
					}
					case slti:
					if(!isFuncSet){
						opcode = 15;
						isFuncSet = true;
					}
					case slli:
					if(!isFuncSet){
						opcode = 17;
						isFuncSet = true;
					}
					case srli:
					if(!isFuncSet){
						opcode = 19;
						isFuncSet = true;
					}
					case srai:
					if(!isFuncSet){
						opcode = 21;
						isFuncSet = true;
					}
					// memory instruction 
					case load:
					if(!isFuncSet){
						opcode = 22;
						isFuncSet = true;
					} 
					case store:
					if(!isFuncSet){
						opcode = 23;
						isFuncSet = true;
					}
					// control flow instructions
					case beq:
					if(!isFuncSet){
						opcode = 25;
						isFuncSet = true;
					} 
					case bne:
					if(!isFuncSet){
						opcode = 26;
						isFuncSet = true;
					} 
					case blt:
					if(!isFuncSet){
						opcode = 27;
						isFuncSet = true;
					} 
					case bgt:
					if(!isFuncSet){
						opcode = 28;
						isFuncSet = true;
					}
							{
								if(opcode < 25){
									// setting the opcode
									byte_ins |= (opcode & 0x1F) << 27;
									// setting rs1
									byte_ins |= (ins.getSourceOperand1().getValue() & 0x1F) << 22;
									//setting rd
									byte_ins |= (ins.getDestinationOperand().getValue() & 0x1F) << 17;
									//setting the immediate
									int immValue = 0;
                                    if (ins.getSourceOperand2().getOperandType() == OperandType.Label) {
                                        immValue = ParsedProgram.symtab.get(ins.getSourceOperand2().getLabelValue());
                                    } else {
                                        immValue = ins.getSourceOperand2().getValue();
                                    }

									byte_ins |= (immValue & 0x1FFFF);
								}
								else{
									
									// setting the opcode
									byte_ins |= (opcode & 0x1F) << 27;
									// setting rs1
									byte_ins |= (ins.getSourceOperand1().getValue() & 0x1F) << 22;
									//=========
									//setting rs2
									byte_ins |= (ins.getSourceOperand2().getValue() & 0x1F) << 17;
									//=========
									//setting the label
									int pc = ins.getProgramCounter();
									int addrOfLabel = ParsedProgram.symtab.get(ins.getDestinationOperand().getLabelValue()) - pc;
									int mask = 131071;
									addrOfLabel = (mask & addrOfLabel);
									byte_ins |= (addrOfLabel);
								}

							}
							break;
					// RI-Type
					// opcode |  rs1    |  immediate
					// 5bits  |	 5bits  |  22bits
					// memory instructions
					case jmp:
					if(!isFuncSet){
						opcode = 24;
						isFuncSet = true;
					}
							{
								// setting the opcode
								byte_ins |= (opcode & 0x1F) << 27;
								//setting rd
								byte_ins |= (ins.getDestinationOperand().getValue() & 0x1F) << 17;
								int pc = ins.getProgramCounter();
								int addrOfLabel = ParsedProgram.symtab.get(ins.getDestinationOperand().getLabelValue()) - pc;
								int mask = 4194303;
								addrOfLabel = (mask & addrOfLabel);
								byte_ins |= (addrOfLabel);

							}
							break;
					default:
						opcode = 29;
					
							{
								// setting the opcode
								byte_ins |= (opcode & 0x1F) << 27;

							}
							break;
				}

				String printIns = ins.toString();
				System.out.println(printIns);
					
				output.write(ByteBuffer.allocate(4).putInt(byte_ins).array());
			}
		}			
		
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}