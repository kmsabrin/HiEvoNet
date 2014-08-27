import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Monitor {

	int ageHistogram[];
	int cumulativeAgeHistogram[];
	float ageCDF[];
	
	static int deathCountPerGenerality[];
	static int predatorCountPerGenerality[];
	
	int ageStatsPerGenerality[][];
	List<List<Integer>> agesPerGenerality;
	
	int generalityHistogram[];
	int avgProductGeneralityHistogram[];
	
	List<Integer> generalityHistogramGrowth;

	Monitor() {
		deathCountPerGenerality = new int[100];
		predatorCountPerGenerality = new int[100];
		generalityHistogramGrowth = new ArrayList();
	}
	
	public void updateGeneralityHistogramGrowth(DAG dag) {
		int lower = 0, upper = 0;
		
		for (int i = 0; i < dag.nextNodeID; ++i) {
			if (!dag.nodeIDs[i]) {
				continue;
			}

			if (dag.idNodeMap.get(i).generality < 50) ++upper;
			else ++lower;
		}
		
		generalityHistogramGrowth.add(lower);
		generalityHistogramGrowth.add(upper);
	}

	public void getAgeCDF(DAG dag) {

		ageHistogram = new int[Driver.nRound + 1]; // nRound is max age
		cumulativeAgeHistogram = new int[Driver.nRound + 1];
		ageCDF = new float[Driver.nRound + 1];

		for (int i = 0; i < dag.nextNodeID; ++i) {
			if (!dag.nodeIDs[i]) {
				continue;
			}

			ageHistogram[Driver.nRound - dag.idNodeMap.get(i).birthRound]++;
		}

		// cumulativeAgeHistogram[0] = ageHistogram[0];
		for (int i = 1; i <= Driver.nRound; ++i) {
			cumulativeAgeHistogram[i] = cumulativeAgeHistogram[i - 1] + ageHistogram[i];
		}

		for (int i = 1; i <= Driver.nRound; ++i) {
			ageCDF[i] = (float) cumulativeAgeHistogram[i] / (dag.nextNodeID - dag.nDeadNodes);
		}
	}

	public void getAgeStatsPerGenerality(DAG dag) {
		ageStatsPerGenerality = new int[100][3]; 
		
		agesPerGenerality = new ArrayList();
		for (int i = 0; i < 100; ++i) {
			agesPerGenerality.add(new ArrayList());
		}
		
		for (int i = 0; i < dag.nextNodeID; ++i) {
			if (!dag.nodeIDs[i]) {
				continue;
			}
			Node temp = dag.idNodeMap.get(i);
			int age = Driver.nRound - temp.birthRound;
			agesPerGenerality.get(temp.generality).add(age);
		}

		for (int i = 0; i < 100; ++i) {			
			Collections.sort(agesPerGenerality.get(i));
			int size = agesPerGenerality.get(i).size();
			if (agesPerGenerality.get(i).size() < 1) {
				continue;
			}
			ageStatsPerGenerality[i][0] = agesPerGenerality.get(i).get(0); // min
			ageStatsPerGenerality[i][1] = agesPerGenerality.get(i).get(size / 2); // median
			ageStatsPerGenerality[i][2] = agesPerGenerality.get(i).get(size - 1); // max
		}
	}

	public void dumpDAG(DAG dag, int nRound) {
		PrintWriter pw;

		try {
			pw = new PrintWriter(new File("Output/DAG/Round_" + nRound + ".txt"));
			// Format:
			// Total Nodes

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	void getGeneralityHistogram(DAG dag) {
		generalityHistogram = new int[100];

		for (int i = 0; i < dag.nextNodeID; ++i) {
			if (!dag.nodeIDs[i]) continue;
			++generalityHistogram[dag.idNodeMap.get(i).generality];
		}
	}

	void getAvgProductGeneralityHistogram(DAG dag) {
		avgProductGeneralityHistogram = new int[100];
		int nItem[] = new int[100];

		for (int i = 0; i < dag.nextNodeID; ++i) {
			if (!dag.nodeIDs[i]) continue;
			avgProductGeneralityHistogram[dag.idNodeMap.get(i).generality] += dag.idNodeMap.get(i).products.size();
			++nItem[dag.idNodeMap.get(i).generality];
		}

		for (int i = 0; i < 100; ++i) {
			if (nItem[i] > 0) {
				avgProductGeneralityHistogram[i] /= nItem[i];
			}
		}
	}

	public void saveStatistics() {
		PrintWriter pw;

		try {
			pw = new PrintWriter(new File("Output/Age/ageHistogram.txt"));
			for (int i = 1; i < ageHistogram.length; ++i) {
				pw.println(i + "\t" + ageHistogram[i]);
			}
			pw.close();

			pw = new PrintWriter(new File("Output/Age/ageCDF.txt"));
			for (int i = 1; i < ageCDF.length; ++i) {
				pw.println(i + "\t" + ageCDF[i]);
			}
			pw.close();

			pw = new PrintWriter(new File("Output/Age/ageStatsPerGenerality.txt"));
			for (int i = 0; i < ageStatsPerGenerality.length; ++i) {
				pw.println(i + "\t" + ageStatsPerGenerality[i][0] + "\t" + ageStatsPerGenerality[i][1] + "\t" + ageStatsPerGenerality[i][2] );
			}
			pw.close();
			
			pw = new PrintWriter(new File("Output/Death/deathCountPerGenerality.txt"));
			for (int i = 0; i < deathCountPerGenerality.length; ++i) {
				pw.println(i + "\t" + deathCountPerGenerality[i]);
			}
			pw.close();
			
			pw = new PrintWriter(new File("Output/Death/predatorCountPerGenerality.txt"));
			for (int i = 0; i < predatorCountPerGenerality.length; ++i) {
				pw.println(i + "\t" + predatorCountPerGenerality[i]);
			}
			pw.close();
			
			pw = new PrintWriter(new File("Output/Shape/generalityHistogram.txt"));
			for (int i = 0; i < generalityHistogram.length; ++i) {
				pw.println(i + "\t" + generalityHistogram[i]);
			}
			pw.close();
			
			pw = new PrintWriter(new File("Output/Age/avgProductGeneralityHistogram.txt"));
			for (int i = 0; i < avgProductGeneralityHistogram.length; ++i) {
				pw.println(i + "\t" + avgProductGeneralityHistogram[i]);
			}
			pw.close();
			
			pw = new PrintWriter(new File("Output/Shape/generalityHistogramGrowth.txt"));
			for (int i = 0; i < generalityHistogramGrowth.size(); i += 2) {
				pw.println(generalityHistogramGrowth.get(i) + "\t" + generalityHistogramGrowth.get(i + 1));
			}
			pw.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void printDAG(DAG dag, String msg) {
		System.out.println(msg);

		System.out.print("Roots: ");
		for (int i : dag.rootNodes) {
			System.out.print(i + " " + dag.idNodeMap.get(i).fitnessValue + " # ");
		}
		System.out.println();

		dag.sortTopologically();
		System.out.print("Topo sorted nodes: ");
		for (int i : dag.topoSortedNodeIDs) {
			System.out.print(i + " ");
		}
		System.out.println();

		for (int i = 0; i < dag.nextNodeID; ++i) {
			if (dag.nodeIDs[i]) {
				Node temp = dag.idNodeMap.get(i);
				System.out.println("Node: " + i + " Fitness: " + temp.fitnessValue + " Generality: " + temp.generality);
				System.out.print("Products: ");
				for (int j : temp.products) {
					System.out.print(j + " ");
				}
				System.out.println();
			}
		}

		System.out.println("Nodes Killed: " + dag.nDeadNodes);
	}

	public void drawRelationDAG(DAG dag) {
		PrintWriter pw;
		try {
			pw = new PrintWriter(new File("Output/Shape/RelationshipDAG.txt"));

			pw.println("Relationship DAG 2");

			dag.sortTopologically();

			int nodeLevels[] = new int[dag.nextNodeID];
			int levelHistogram[] = new int[dag.nextNodeID];
			int maxLevel = 0;
			int maxLevelLength = 0;

			for (int currentNodeID : dag.topoSortedNodeIDs) {

				int level = 0;
				for (int j : dag.idNodeMap.get(currentNodeID).substrates) {
					if (nodeLevels[j] > level) {
						level = nodeLevels[j];
					}
				}
				++level;
				nodeLevels[currentNodeID] = level;
				levelHistogram[level]++;

				if (levelHistogram[level] > maxLevelLength) {
					maxLevelLength = levelHistogram[level];
				}

				if (level > maxLevel) {
					maxLevel = level;
				}
			}

			for (int i = maxLevel; i > 0; --i) {
				for (int j = 0; j < (maxLevelLength - levelHistogram[i]) / 2; ++j) {
					pw.print(" ");
				}
				for (int j = 0; j < levelHistogram[i]; ++j) {
					pw.print("*");
				}
				pw.println();
			}

			pw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void drawGeneralityDAG(DAG dag) {
		PrintWriter pw;
		try {
			pw = new PrintWriter(new File("Output/Shape/GeneralityDAG.txt"));

			pw.println("Generality DAG");

			int max = -1;
			for (int i = 0; i < 100; ++i) {
				if (generalityHistogram[i] > max) {
					max = generalityHistogram[i];
				}
			}

			for (int i = 0; i < 100; ++i) {
				for (int j = 0; j < (max - generalityHistogram[i]) / 2; ++j) {
					pw.print(" ");
				}

				for (int j = 0; j < generalityHistogram[i]; ++j) {
					pw.print("#");
				}

				// if (generalityHistogram[i] == 0) System.out.print("-");
				pw.println();
			}

			pw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	void getMonitors(DAG dag) {
		getAgeCDF(dag);
		getAgeStatsPerGenerality(dag);
		getAvgProductGeneralityHistogram(dag);
		getGeneralityHistogram(dag);
		drawRelationDAG(dag);
		drawGeneralityDAG(dag);
		saveStatistics();
	}
}
