package ea;

import java.util.Random;

public class Parameters {

	public static Random rnd = new Random(System.currentTimeMillis());
	
	/**
	 * used as a seed
	 * 
	 */	
	static final boolean [] DEFAULT_WOMENS_TRANSITION_STRATEGY = {true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true};
	public static final int [] DEFAULT_WOMENS_PACING_STRATEGY = {300, 300, 300, 300, 300, 300, 300, 350, 350, 300, 300, 350, 350, 350, 350, 300, 300, 350, 350, 350, 350, 300, 300};
	
	
	public static int popSize = 40;
	public static int tournamentSize = 4;
	
	public static int mutationRateMax = 10;//out of len
	public static double mutationProbability = 0.8;
	public static double crossoverProbability = 1.0;
	
	public static int maxIterations = 1000;
	
	
}
