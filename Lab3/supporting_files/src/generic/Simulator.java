package generic;

import processor.Clock;
import processor.Processor;

public class Simulator {

	static Processor processor;
	static boolean simulationComplete;

	public static void setupSimulation(String assemblyProgramFile, Processor p) {
		Simulator.processor = p;
		loadProgram(assemblyProgramFile);

		simulationComplete = false;
	}

	/**
	 * Loads the binary program from the specified file into the processor's memory.
	 * Initializes registers and program counter.
	 * 
	 * @param assemblyProgramFile Path to the binary object file
	 */
	static void loadProgram(String assemblyProgramFile) {

		try {
			java.io.FileInputStream fis = new java.io.FileInputStream(assemblyProgramFile);

			// Read header (4 bytes) - first instruction address
			int firstCodeAddress = readBigEndianInt(fis);

			// Read data section - starts at address 0
			int address = 0;
			while (address < firstCodeAddress) {
				int dataWord = readBigEndianInt(fis);
				processor.getMainMemory().setWord(address, dataWord);
				address++;
			}

			// Read text section (instructions) - starts at firstCodeAddress
			int instruction;
			while ((instruction = readBigEndianInt(fis)) != -1) {
				processor.getMainMemory().setWord(address, instruction);
				address++;
			}

			fis.close();

			// Set PC to first instruction
			processor.getRegisterFile().setProgramCounter(firstCodeAddress);

			// Initialize registers
			processor.getRegisterFile().setValue(0, 0);
			processor.getRegisterFile().setValue(1, 65535);
			processor.getRegisterFile().setValue(2, 65535);

		} catch (Exception e) {
			System.err.println("Error loading program: " + e.getMessage());
			e.printStackTrace();
		}
	}

	// Helper method to read 32-bit integer in big-endian format
	static int readBigEndianInt(java.io.FileInputStream fis) throws java.io.IOException {
		int b1 = fis.read();
		if (b1 == -1)
			return -1; // EOF
		int b2 = fis.read();
		int b3 = fis.read();
		int b4 = fis.read();
		if (b2 == -1 || b3 == -1 || b4 == -1)
			return -1; // Incomplete read

		return (b1 << 24) | (b2 << 16) | (b3 << 8) | b4;
	}

	public static void simulate() {
		while (simulationComplete == false) {
			processor.getIFUnit().performIF();
			processor.getOFUnit().performOF();
			processor.getEXUnit().performEX();
			processor.getMAUnit().performMA();
			processor.getRWUnit().performRW();
			Clock.incrementClock();
		}

		// Set statistics
		Statistics.numberOfCycles = (int) Clock.getCurrentTime();
	}

	public static void setSimulationComplete(boolean value) {
		simulationComplete = value;
	}
}
