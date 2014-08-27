import java.util.ArrayList;
import java.util.List;

public class Driver {

	DAG dag;
	Monitor monitor;
	int maxNodes;
	static int nRound;

	Driver() {
		dag = new DAG();
		monitor = new Monitor();
		nRound = 0;
	}

	void evolve() {
		dag.initializeNetwork(50);
		++nRound;
		monitor.updateGeneralityHistogramGrowth(dag);
		// monitor.printDAG(dag, "DAG after init");
		
		while (nRound <= 110) {
			System.out.println("Birth Phase Running ...");
			dag.runBirthRound();
//			monitor.printDAG(dag, "DAG after birth round " + nRound);
			
			System.out.println("Update Phase Running ...");
			dag.updateFitness();
//			monitor.printDAG(dag, "DAG after fitness round " + nRound);
			
			System.out.println("Competition/Death Phase Running ...");
			dag.runCompetitionDeathRound();
//			monitor.printDAG(dag, "DAG after death round " + nRound);			
			
			System.out.println("End of Round " + nRound + " with " + (dag.nextNodeID - dag.nDeadNodes) + " Nodes.");
			nRound++;
			monitor.updateGeneralityHistogramGrowth(dag);
		}
	}
	
	static void runSimulation() {
		long sTime = System.currentTimeMillis();
		
		Driver driver = new Driver();
		driver.evolve();
		driver.monitor.getMonitors(driver.dag);
			
		System.out.println("\n\nNodes existing: " + (driver.dag.nextNodeID - driver.dag.nDeadNodes) + " Total Nodes Created: " + driver.dag.nextNodeID);
		System.out.print("\nComplete in ");
		
		long eTime = System.currentTimeMillis();
		System.out.println(((eTime - sTime) / 1000) + " seconds!");
	}
	
	public static void main(String[] args) {
		runSimulation();
	}
}
