import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class DAG {
	Map<Integer, Node> idNodeMap;
	boolean nodeIDs[]; // consider this
	
	List<Integer> rootNodes;
	List<Integer> topoSortedNodeIDs;
	
	Random random;
	
 	int nextNodeID;
 	int nDeadNodes;
 	int maxNodes;
 	
 	double generalityFactor;
	double replicationFactor;
	double competitionFactor;
	double mortalityFactor;
	
	boolean visitedNodes[];
	int visitTimes[];
	int vTime;
	int dependants[];
	int nDependants;
	
	DAG() {
		nextNodeID = 0;
		nDeadNodes = 0;
		maxNodes = 10000;
		
		idNodeMap = new HashMap();
		rootNodes = new ArrayList();
		nodeIDs = new boolean[maxNodes];
		Arrays.fill(nodeIDs, true);
		
		random = new Random(1000019);
		
		generalityFactor = 0.2;
		replicationFactor = 0.1;
		competitionFactor = 0.5;
		mortalityFactor = 0.4;
	}
	
//	Initialization
	void initializeNetwork(int nNewNode) {
		int numberOfNewNode = nNewNode;
		
		int minGenerality = 90; // make it 90
		int maxGenerality = 99;
		
		for (int i = 0; i < numberOfNewNode; ++i) {
//			int innateValue = random.nextInt(100);
			int innateValue = 1;
			double fitnessValue = innateValue;
			int generality = random.nextInt(maxGenerality - minGenerality + 1) + minGenerality;
			
			Node node = new Node(maxNodes, innateValue, fitnessValue, generality, Driver.nRound);
			idNodeMap.put(nextNodeID, node);
			rootNodes.add(nextNodeID); // no connection created for init nodes, hence all are roots
			++nextNodeID;
		}
	}
	
//	basic birth
	void runBirthRound() {
		for (int i = 0; i < nextNodeID; ++i) {
//			skip dead nodes
			if (!nodeIDs[i]) continue;
			
			double randomReplication = random.nextDouble();
			if (randomReplication < replicationFactor) {
				addNewNode(i); // replicate this node to initiate a new birth
				++nextNodeID;
			}
		}
		
		updateRootNodes();
	}
	
	void addNewNode(int nodeIDToReplicate) {
		Node nodeToReplicate = idNodeMap.get(nodeIDToReplicate);
		
		int minGenerality = (int)Math.max(0, nodeToReplicate.generality - nodeToReplicate.generality * generalityFactor);
//		int maxGenerality = (int)Math.min(99, nodeToReplicate.generality + nodeToReplicate.generality * generalityFactor);
		int maxGenerality = nodeToReplicate.generality;
		int generality = (random.nextInt(maxGenerality - minGenerality + 1) + minGenerality);

//		int innateValue = random.nextInt(100);
		int innateValue = 1;
		double fitnessValue = innateValue;
		Node newNode = new Node(maxNodes, innateValue, fitnessValue, generality, Driver.nRound);
		idNodeMap.put(nextNodeID, newNode);
		
//		add connections to newborn
		for (int i = 0; i < nextNodeID; ++i) {
//			skip dead nodes
			if (!nodeIDs[i]) continue;
			
			Node temp = idNodeMap.get(i);
			int randomGenerality = random.nextInt(100);
			if (temp.generality > newNode.generality) { // this node can be a substrate of the new born
				if (randomGenerality < temp.generality) { // make substrate					
//					increase shared products for substrates 					
					for (int j: newNode.substrates) {
						Node common = idNodeMap.get(j);
						common.neighbors[i]++;
						temp.neighbors[j]++;
					}
					
					temp.products.add(nextNodeID);
					newNode.substrates.add(i);
				}
			}
			else if (temp.generality < newNode.generality) { // this node can be a product of the new born
				if (randomGenerality < newNode.generality) { // make product
//					increase shared products for substrates 					
					for (int j: temp.substrates) {
						Node common = idNodeMap.get(j);
						common.neighbors[nextNodeID]++;
						newNode.neighbors[j]++;
					}

					temp.substrates.add(nextNodeID);
					newNode.products.add(i);
				}
			}
		}
	}
	
	
	void killNode(int nodeID, Set<Integer> competitors) {
		++nDeadNodes;
		
		Node deadNode = idNodeMap.get(nodeID);
				
		List<Integer> cascadeKills = new ArrayList();
		
//		decrease shared products for substrates
		for (int j: deadNode.substrates) {
			for (int k: deadNode.substrates) {
				if (j < k) {
					Node nodeJ = idNodeMap.get(j);
					Node nodeK = idNodeMap.get(k);
					nodeJ.neighbors[k]--;
					nodeK.neighbors[j]--;
				}
			}
		}
		
//		remove up link from parents 
		for (int substrate: deadNode.substrates) {
			Node temp = idNodeMap.get(substrate);
			temp.products.remove(new Integer(nodeID));
		}
		
//		remove down link towards children
		for (int product: deadNode.products) {
			Node temp = idNodeMap.get(product);
			temp.substrates.remove(new Integer(nodeID));
			
//			if (temp.substrates.size() < 1) {
////				cascade death
//				killNode(product); not this way
//			}
			
			boolean flag = false;
			for (int substrate: temp.substrates) {
				if (competitors.contains(substrate)) {
					flag =  true;
				}
			}
			
			if (flag == false && competitors.size() > 0) {
//				now kill node, because it has lost a functionality
//				no cascading yet
//				killNode(product, new HashSet<Integer>());
				cascadeKills.add(product);
			}
		}
			
//		have to surrender the nodeID, can not reuse it
//		System.out.println("Removing " + nodeID);
		idNodeMap.remove(new Integer(nodeID));
		if (rootNodes.contains(nodeID) == true) {
			rootNodes.remove(new Integer(nodeID));
		}
		deadNode = null;
		nodeIDs[nodeID] = false;
//		System.out.println("Killing Node: " + nodeID);
		
//		induce cascade kills
		for (int i: cascadeKills) {
			killNode(i, new HashSet<Integer>());
		}
	}
	
	void computeCompetitionDeath(int nodeID) {
		Node node = idNodeMap.get(nodeID);
		
		Set<Integer> competitors = new HashSet();
		
//		get max competition
		double maxCompetitorFitnessValue = -1;
		int maxCompetitorNodeID = -1;
		for (int i = 0; i < nextNodeID; ++i) {
			if (!nodeIDs[i] || node.neighbors[i] < 1) {
				continue;
			}
//			check if actually competes i.e. shares a large number of products
			Node temp = idNodeMap.get(i);
			int nSharedProducts = node.neighbors[i]; // intersection
			int nTotalProducts = node.products.size() + temp.products.size() - nSharedProducts; // union
//			System.out.print("Node " + nodeID + " neighbor with node " + neighbor);
//			System.out.println(" with competition ratio " + ((double)nSharedProducts / (double)nTotalProducts));
			if (((double)nSharedProducts / (double)nTotalProducts) > competitionFactor) {
//				System.out.print("Node " + nodeID + " competing with node " + c);
//				System.out.println(" with nShared " + nSharedProducts + " and nTotal " + nTotalProducts);
//				get competitors fitness value
				competitors.add(i);
				
				if (temp.fitnessValue > maxCompetitorFitnessValue) {
					maxCompetitorFitnessValue = temp.fitnessValue;
					maxCompetitorNodeID = i;
				}
			}
		}

//		kill condition
		if (maxCompetitorFitnessValue < 0) {
			return; // no competitor
		}
		double deathRatio = (double)node.fitnessValue / (double)maxCompetitorFitnessValue;
		double deathProbablity;
		if (deathRatio > 0.99) {
			deathProbablity = 0;
		}
		else {
			deathProbablity = Math.exp((-1.0 * mortalityFactor * deathRatio) / (1.0 - deathRatio));
		}
		
		if (random.nextDouble() < deathProbablity) { //	kill node

//			System.out.print("Node " + nodeID + " getting killed by node " + maxCompetitorNodeID);
//			System.out.print(" value ratio "  + node.fitnessValue + " vs " + maxCompetitorFitnessValue);
//			System.out.println(" generality ratio "  + node.generality + " vs " + idNodeMap.get(maxCompetitorNodeID).generality);
		
//			record death stats
			Monitor.deathCountPerGenerality[node.generality]++;

//			record predator stats
			Monitor.predatorCountPerGenerality[idNodeMap.get(maxCompetitorNodeID).generality]++;
			
			killNode(nodeID, competitors);
		}
	}
	
	void updateFitness() {

		visitedNodes = new boolean[nextNodeID];
		
		for (int i = 0; i < nextNodeID; ++i) {
//			skip dead nodes
			if (!nodeIDs[i]) continue;
			
			nDependants = 0;
			Arrays.fill(visitedNodes, false);
			Node node = idNodeMap.get(i);
//			node.fitnessValue = node.innateValue;
			for (int j: node.products) {
				updateFitnessHelper(j);
//				node.fitnessValue += idNodeMap.get(j).fitnessValue;
			}
			
			node.fitnessValue = node.innateValue + nDependants;
		}
	}
	
	int updateFitnessHelper(int nodeID) {
		Node node = idNodeMap.get(nodeID);
		if (visitedNodes[nodeID] == true) {
//			return node.fitnessValue;
			return -1;
		}
		
		++nDependants;
//		node.fitnessValue = node.innateValue;
		for (int i: node.products) {
			updateFitnessHelper(i);
//			node.fitnessValue += idNodeMap.get(i).fitnessValue;
		}
		
		visitedNodes[nodeID] = true;
//		return node.fitnessValue;
		return -1; // not using
	}

	void sortTopologically() {
		topoSortedNodeIDs = new ArrayList();
		visitedNodes = new boolean[nextNodeID];
		for (int i: rootNodes) {
			Node node = idNodeMap.get(i);
			for (int j: node.products) {
				sortTopologicallyHelper(j);
			}
			topoSortedNodeIDs.add(0, i);
		}
	}
	
	void sortTopologicallyHelper(int nodeID) {
		if (visitedNodes[nodeID]) {
			return;
		}
		
		Node node = idNodeMap.get(nodeID);
		for (int i: node.products) {
			sortTopologicallyHelper(i);
		}
		
		visitedNodes[nodeID] = true;
		topoSortedNodeIDs.add(0, nodeID);
	}
	
	void updateRootNodes() {
		rootNodes.clear();
		for (int i = 0; i < nextNodeID; ++i) {
//			skip dead nodes
			if (!nodeIDs[i]) continue;

			Node temp = idNodeMap.get(i);
			if (temp.substrates.size() < 1) {
				rootNodes.add(i);
			}
		}
	}
	
	void runCompetitionDeathRound() {
		System.out.println("Sorting topologically");
		sortTopologically();
		
		System.out.println("Computing competition and death");
		for (int i: topoSortedNodeIDs) {
//			check required if cascading death occurred
			if (!nodeIDs[i]) continue;
//			if (idNodeMap.containsKey(i) == false) { 
//				continue;
//			}
			computeCompetitionDeath(i);
		}
		
		updateRootNodes();
	}
}
