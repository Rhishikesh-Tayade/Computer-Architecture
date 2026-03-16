package generic;

import java.io.PrintWriter;

public class Statistics {

	public static int numberOfInstructions;
	public static int numberOfCycles;
	public static int numberOfStalls;
	public static int numberOfWrongBranchInstructions;
	public static int numberOfDataHazards;
	public static int numberOfControlHazards;
	public static double throughput; // instructions per cycle

	public static void printStatistics(String statFile) {
		try {
			PrintWriter writer = new PrintWriter(statFile);

			writer.println("Number of instructions executed = " + numberOfInstructions);
			writer.println("Number of cycles taken = " + numberOfCycles);
			writer.println("Number of stalls = " + numberOfStalls);
			writer.println("Number of wrong branch instructions = " + numberOfWrongBranchInstructions);
			writer.println("Number of data hazards = " + numberOfDataHazards);
			writer.println("Number of control hazards = " + numberOfControlHazards);
			writer.println("Throughput (instructions per cycle) = " + String.format("%.6f", throughput));

			writer.close();
		} catch (Exception e) {
			Misc.printErrorAndExit(e.getMessage());
		}
	}

	public void setNumberOfInstructions(int numberOfInstructions) {
		Statistics.numberOfInstructions = numberOfInstructions;
	}

	public void setNumberOfCycles(int numberOfCycles) {
		Statistics.numberOfCycles = numberOfCycles;
	}

	public void setNumberOfStalls(int numberOfStalls) {
		Statistics.numberOfStalls = numberOfStalls;
	}

	public void setNumberOfWrongBranchInstructions(int numberOfWrongBranchInstructions) {
		Statistics.numberOfWrongBranchInstructions = numberOfWrongBranchInstructions;
	}

	public void setNumberOfDataHazards(int numberOfDataHazards) {
		Statistics.numberOfDataHazards = numberOfDataHazards;
	}

	public void setNumberOfControlHazards(int numberOfControlHazards) {
		Statistics.numberOfControlHazards = numberOfControlHazards;
	}
}
