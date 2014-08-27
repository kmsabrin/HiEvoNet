import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Node {
	int neighbors[];
	List<Integer> products;
	List<Integer> substrates;
	
	int innateValue;
	double fitnessValue;
	int generality;
	int birthRound;
	int deathRound;
	
	Node(int maxNodes, int innateValue, double fitnessValue, int generality, int birthRound) {
		neighbors = new int[maxNodes];
		products = new ArrayList();
		substrates = new ArrayList();
		
		this.innateValue = innateValue;
		this.fitnessValue = fitnessValue;
		this.generality = generality;
		this.birthRound = birthRound;
	}
}
