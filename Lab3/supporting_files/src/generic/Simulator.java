package generic;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import processor.Clock;
import processor.Processor;

public class Simulator {
		
	static Processor processor;
	static boolean simulationComplete;
	
	public static void setupSimulation(String assemblyProgramFile, Processor p)
	{
		Simulator.processor = p;
		loadProgram(assemblyProgramFile);
		
		simulationComplete = false;
	}
	
	static void loadProgram(String assemblyProgramFile)
	{
		/*
		 * TODO
		 * 1. load the program into memory according to the program layout described
		 *    in the ISA specification
		 * 2. set PC to the address of the first instruction in the main
		 * 3. set the following registers:
		 *     x0 = 0
		 *     x1 = 65535
		 *     x2 = 65535
		 */
		FileInputStream programFile = null;

		try{
			programFile = new FileInputStream(assemblyProgramFile);
			int c;
			int index = -1;
			byte[] buffer = new byte[4];
			
			while((c=programFile.read(buffer))!=-1){
				int dataInt = ByteBuffer.wrap(buffer).getInt();
				if(index==-1){
					processor.getRegisterFile().setProgramCounter(dataInt);
				}else{
					processor.getMainMemory().setWord(index, dataInt);
				}
				index++;
				
			}
			processor.getRegisterFile().setValue(0, 0);
			processor.getRegisterFile().setValue(1, 65535);
			processor.getRegisterFile().setValue(2, 65535);
			System.out.println(processor.getRegisterFile().getProgramCounter());
			processor.printState(0, 50);
			System.out.println("Read completed");
			
			programFile.close();

		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public static void simulate()
	{
		Statistics.setNumberOfCycles(0);
		Statistics.setNumberOfInstructions(0);
		while(simulationComplete == false)
		{
			processor.getIFUnit().performIF();
			Clock.incrementClock();
			processor.getOFUnit().performOF();
			Clock.incrementClock();
			processor.getEXUnit().performEX();
			Clock.incrementClock();
			processor.getMAUnit().performMA();
			Clock.incrementClock();
			processor.getRWUnit().performRW();
			Clock.incrementClock();
			Statistics.setNumberOfCycles(Statistics.getNumberOfCycles()+1);
			Statistics.setNumberOfInstructions(Statistics.getNumberOfInstructions()+1);

		}
	}
	
	public static void setSimulationComplete(boolean value)
	{
		simulationComplete = value;
	}
}
