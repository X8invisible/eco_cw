package ea;

/***
 * This is an example of an EA used to solve the problem
 *  A chromosome consists of two arrays - the pacing strategy and the transition strategy
 * This algorithm is only provided as an example of how to use the code and is very simple - it ONLY evolves the transition strategy and simply sticks with the default
 * pacing strategy
 * The default settings in the parameters file make the EA work like a hillclimber:
 * 	the population size is set to 1, and there is no crossover, just mutation
 * The pacing strategy array is never altered in this version- mutation and crossover are only
 * applied to the transition strategy array
 * It uses a simple (and not very helpful) fitness function - if a strategy results in an
 * incomplete race, the fitness is set to 1000, regardless of how much of the race is completed
 * If the race is completed, the fitness is equal to the time taken
 * The idea is to minimise the fitness value
 */


import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

import teamPursuit.TeamPursuit;
import teamPursuit.WomensTeamPursuit;

public class EA implements Runnable{

	// create a new team with the default settings
	public static TeamPursuit teamPursuit = new WomensTeamPursuit();

	private ArrayList<Individual> population = new ArrayList<Individual>();
	private ArrayList<Individual> population2;
	private int iteration = 0;
	private int selection = 0;

	public EA() {

	}


	public static void main(String[] args) {
		EA ea = new EA();
		double sum;
		Individual best = new Individual();
		double bestFitness = 9999;
		//loops through different selection methods
		for(int j = 1; j<3;j++){
			ea.selection = j;
			sum = 0;
			//run test 30 times
			for(int i = 0; i<30; i++) {
				System.out.println(i);
				ea.run();
				Individual t = ea.getBest(ea.population);
				sum += t.getFitness();
				if(t.getFitness() < bestFitness){
					best = t;
					bestFitness = best.getFitness();
				}
				System.out.println(bestFitness);
			}
			ea.writeStats(best.write(),j);
			ea.writeStats("average: "+ 2500, j);
		}
		//ea.run();

	}

	public void run() {

		initialisePopulation();
		System.out.println("finished init pop");

		singleIsland(0,selection);

		//writeStats(best.write(),selection);

		//tournament
//		singleIsland(0,0);
//		//rank
//		singleIsland(1,0);
//		//roulette
//		singleIsland(0,0);
//		//stochastic
//		singleIsland(3,0);
//		twoIsland();
		Individual best = getBest(population);
		best.print();
		//writeStats(best.write(),25000);
	}

	private void twoIsland(){
		population2 = new ArrayList<>(population); //second island population, starts identical
		initialisePopulation();
		iteration = 0;
		double bestFit = getBest(population).getFitness();
		while(iteration < Parameters.maxIterations){
			iteration++;
			Individual i1Parent1, i1Parent2, i2Parent1, i2Parent2;
//			i1Parent1 = rankSelection();
//			i1Parent2 = rankSelection();
			i1Parent1 = rouletteSelection(population);
			i1Parent2 = rouletteSelection(population);

			i2Parent1 = tournamentSelection(population2);
			i2Parent2 = tournamentSelection(population2);

//			ArrayList<Individual> parents = stochasticSelection();
//			i1Parent1 = parents.get(0);
//			i1Parent2 = parents.get(1);

			//ArrayList<Individual> parents = stochasticSelection();
			//Individual parent1 = parents.get(0);
			//Individual parent2 = parents.get(1);
			Individual child = crossover(i1Parent1, i1Parent2);
			Individual child2 = uniformCrossover(i2Parent1, i2Parent2);
			//Individual child = crossover(parent1, parent2);
			child = mutate(child);
			child2 = mutate(child2);
			child.evaluate(teamPursuit);
			child2.evaluate(teamPursuit);
			replace(child,population);
			replace(child2, population2);
			System.out.println("Generation: "+ iteration);
			printStats("Island1 Rou: ", population);
			printStats("Island2 Tou: ", population2);
			if(iteration % 40 ==0){
				swapIslandPopulation();
			}
		}
	}

	private void swapIslandPopulation(){
		for(int i= 0; i<5;i++){
			Individual p1 = tournamentSelection(population);
			Individual p2 = tournamentSelection(population2);
			int idx1 = Parameters.rnd.nextInt(population.size());
			int idx2 = Parameters.rnd.nextInt(population2.size());
			population.set(idx1, p2);
			population2.set(idx2, p1);
		}
		System.out.println("swapped");
	}

	//s for selection mode, c for crossover mode
	private void singleIsland(int s, int c){
		iteration = 0;
		boolean rank = false; //used for rank selection swap, if the population stagnates for a number of generations, switch to rank
		double bestFit = getBest(population).getFitness();
		int count = 0;
		int selection = s;
		while(iteration < Parameters.maxIterations){
			iteration++;
			Individual parent1, parent2;
			switch (selection){
				case 1:
					parent1 = rankSelection(population);
					parent2 = rankSelection(population);
					break;
				case 2:
					parent1 = rouletteSelection(population);
					parent2 = rouletteSelection(population);
					break;
				case 3:
					ArrayList<Individual> parents = stochasticSelection();
					parent1 = parents.get(0);
					parent2 = parents.get(1);
					break;
				default:
					parent1 = tournamentSelection(population);
					parent2 = tournamentSelection(population);
					break;
			}
			Individual child;
			switch(c){
				case 1:
					child = crossover(parent1, parent2);
					break;
				case 2:
					child = twoCrossover(parent1,parent2);
					break;
				default:
					child = uniformCrossover(parent1, parent2);
					break;
			}
			child = mutate(child);
			child.evaluate(teamPursuit);
			replace(child, population);
			printStats(iteration+" "+s+" "+c+" ", population);

//			if(getBest(population).getFitness() == bestFit){
//				count++;
//			}else{
//				bestFit = getBest(population).getFitness();
//				count =0;
//			}
//
//			if(count > 5000)
//				break;
//
//			if(count >1000 && !rank){
//				System.out.println("switching selection");
//				selection++;
//				count = 0;
//				rank = !rank;
//
//			}
		}
	}
	private void printStats(String isl, ArrayList<Individual> population) {
		System.out.println(isl + "\t" + getBest(population) + "\t" + getWorst(population));
	}
	private void writeStats(String best, int i){
		try {
			PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(i+"CrossTest.txt", true)));
			String s = Parameters.maxIterations + " " + best;
			writer.println(s);
			writer.println();
			writer.close();
		}catch (IOException e){
			e.printStackTrace();
		}
	}

	private void replace(Individual child, ArrayList<Individual> population) {
		Individual worst = getWorst(population);
		if(child.getFitness() < worst.getFitness()){
			int idx = population.indexOf(worst);
			population.set(idx, child);
		}
	}


	private Individual mutate(Individual child) {
		if(Parameters.rnd.nextDouble() > Parameters.mutationProbability){
			return child;
		}

		// choose how many elements to alter
		int mutationRate = 1 + Parameters.rnd.nextInt(Parameters.mutationRateMax);

		// mutate the transition strategy

		//mutate the transition strategy by flipping boolean value
		for(int i = 0; i < mutationRate; i++){
			int index = Parameters.rnd.nextInt(child.transitionStrategy.length);
			child.transitionStrategy[index] = !child.transitionStrategy[index];
		}

		//mutate the pacing by swapping two values in the chromosome
		for(int i = 0; i< mutationRate;i++){
			//int r = Parameters.rnd.nextInt(20)-10;
			int index = Parameters.rnd.nextInt(child.pacingStrategy.length);
			child.pacingStrategy[index] = Parameters.rnd.nextInt(550-200) +200;;
		}



		return child;
	}


	private Individual mutateTwo(Individual child) {
		if(Parameters.rnd.nextDouble() > Parameters.mutationProbability){
			return child;
		}

		// choose how many elements to alter
		int mutationRate = 1 + Parameters.rnd.nextInt(Parameters.mutationRateMax);

		// mutate the transition strategy

		//mutate the transition strategy by flipping boolean value
		for(int i = 0; i < mutationRate; i++){
			int index = Parameters.rnd.nextInt(child.transitionStrategy.length);
			child.transitionStrategy[index] = !child.transitionStrategy[index];
		}

		//mutate the pacing by adding/subtracting 5 from a random pacing
		for(int i = 0; i< mutationRate;i++){
			int r = Parameters.rnd.nextInt(10)-5;
			int index = Parameters.rnd.nextInt(child.pacingStrategy.length);
			child.pacingStrategy[index] +=r;
		}



		return child;
	}

	private Individual crossover(Individual parent1, Individual parent2) {
		if(Parameters.rnd.nextDouble() > Parameters.crossoverProbability){
			return parent1;
		}
		Individual child = new Individual();

		int crossoverPointT = Parameters.rnd.nextInt(parent1.transitionStrategy.length);
		int crossoverPointP = Parameters.rnd.nextInt(parent1.pacingStrategy.length);

		for(int i = 0; i < crossoverPointP; i++){
			child.pacingStrategy[i] = parent1.pacingStrategy[i];
		}
		for(int i = crossoverPointP; i < parent2.pacingStrategy.length; i++){
			child.pacingStrategy[i] = parent2.pacingStrategy[i];
		}

		for(int i = 0; i < crossoverPointT; i++){
			child.transitionStrategy[i] = parent1.transitionStrategy[i];
		}
		for(int i = crossoverPointT; i < parent2.transitionStrategy.length; i++){
			child.transitionStrategy[i] = parent2.transitionStrategy[i];
		}
		return child;
	}

	private Individual uniformCrossover(Individual parent1, Individual parent2) {
		if(Parameters.rnd.nextDouble() > Parameters.crossoverProbability){
			return parent1;
		}
		Individual child = new Individual();

		for(int i =0; i< parent1.pacingStrategy.length; i++){
			if(Parameters.rnd.nextDouble() < 0.5)
				child.pacingStrategy[i] = parent1.pacingStrategy[i];
			else
				child.pacingStrategy[i] = parent2.pacingStrategy[i];
		}
		for(int i =0; i< parent1.transitionStrategy.length; i++){
			if(Parameters.rnd.nextDouble() < 0.5)
				child.transitionStrategy[i] = parent1.transitionStrategy[i];
			else
				child.transitionStrategy[i] = parent2.transitionStrategy[i];
		}
		return child;
	}

	private Individual twoCrossover(Individual parent1, Individual parent2) {
		if(Parameters.rnd.nextDouble() > Parameters.crossoverProbability){
			return parent1;
		}
		Individual child = new Individual();
		int crossoverPointOneT = Parameters.rnd.nextInt(parent1.transitionStrategy.length);
		int crossoverPointTwoT = Parameters.rnd.nextInt(parent1.transitionStrategy.length);
		if(crossoverPointOneT < crossoverPointTwoT){
			int t = crossoverPointOneT;
			crossoverPointOneT = crossoverPointTwoT;
			crossoverPointTwoT = t;
		}

		int crossoverPointOneP = Parameters.rnd.nextInt(parent1.pacingStrategy.length);
		int crossoverPointTwoP = Parameters.rnd.nextInt(parent1.pacingStrategy.length);
		if(crossoverPointOneP > crossoverPointTwoP){
			int t = crossoverPointOneP;
			crossoverPointOneP = crossoverPointTwoP;
			crossoverPointTwoP = t;
		}
		for(int i = 0; i < crossoverPointOneP; i++){
			child.pacingStrategy[i] = parent1.pacingStrategy[i];
		}
		for(int i = crossoverPointOneP; i < crossoverPointTwoP; i++){
			child.pacingStrategy[i] = parent2.pacingStrategy[i];
		}
		for(int i = crossoverPointTwoP; i < parent1.pacingStrategy.length; i++){
			child.pacingStrategy[i] = parent1.pacingStrategy[i];
		}


		for(int i = 0; i < crossoverPointOneT; i++){
			child.transitionStrategy[i] = parent1.transitionStrategy[i];
		}
		for(int i = crossoverPointOneT; i < crossoverPointTwoT; i++){
			child.transitionStrategy[i] = parent2.transitionStrategy[i];
		}
		for(int i = crossoverPointTwoT; i < parent1.transitionStrategy.length; i++){
			child.transitionStrategy[i] = parent1.transitionStrategy[i];
		}
		return child;
	}

	/**
	 * Returns a COPY of the individual selected using tournament selection
	 * @return
	 */
	private Individual tournamentSelection(ArrayList<Individual> population) {
		ArrayList<Individual> candidates = new ArrayList<Individual>();
		for(int i = 0; i < Parameters.tournamentSize; i++){
			candidates.add(population.get(Parameters.rnd.nextInt(population.size())));
		}
		return getBest(candidates).copy();
	}


	private Individual rouletteSelection(ArrayList<Individual> population){
		double fitnessSum = 0;
		for(int i =0; i<population.size();i++){
			fitnessSum += (1/population.get(i).getFitness())+1;
		}
		//random pointer in our fitness sum (the specific number points to the individual that is at that location in the roulette)
		int randNumber = Parameters.rnd.nextInt((int)fitnessSum);
		double currentSum = 0;
		int index = 0;
		//spin the wheel until we meet the pointer set
		while(currentSum < randNumber && index < population.size()){
			currentSum += (1/population.get(index).getFitness())+1;
			index++;
		}
		if(index != 0)
			index --;
		return population.get(index).copy();
	}
	private Individual rankSelection(ArrayList<Individual> population){
		ArrayList<Individual> sortedPop = new ArrayList<Individual>(population);
		//sorts population by fitness
		Collections.sort(sortedPop, new Comparator<Individual>() {
			@Override
			public int compare(Individual o1, Individual o2) {
				if(o1.getFitness() > o2.getFitness()) {
					return -1;
				} else if(o1.getFitness() < o2.getFitness()) {
					return 1;
				}
				return 0;
			}
		});
		//initialise with biggest fitness rank (rank 1 is best fitness, rank 20 is lowest)
		int fitnessSum = 1;
		int currentRank =1;
		//calculate the sum of the ranks
		for(int i = sortedPop.size()-2; i > 0; i--){
			if(sortedPop.get(i).getFitness() != sortedPop.get(i+1).getFitness()){
				currentRank++;
			}
			fitnessSum+=currentRank;
		}
		//random pointer in our fitness sum (the specific number points to the individual that is at that location in the roulette)
		int randNumber = Parameters.rnd.nextInt(fitnessSum);
		int currentSum = 0;
		int index = 0;
		//spin the wheel until we meet the pointer set
		while(currentSum < randNumber && index < sortedPop.size()){
			currentSum += sortedPop.get(index).getFitness();
			index++;
		}
		if(index != 0)
			index --;
		return sortedPop.get(index).copy();
	}
	//selecting two parents using Stochastic Universal Sampling
	private ArrayList<Individual> stochasticSelection(){
		double fitnessSum = 0;
		ArrayList<Individual> selection = new ArrayList<Individual>();
		for(int i =0; i<population.size();i++){
			fitnessSum += population.get(i).getFitness();
		}
		int randNumber = Parameters.rnd.nextInt((int)fitnessSum);
		int currentSum = 0;
		int index = 0;
		int indexTwo = 0;
		while(currentSum < randNumber && index < population.size()){
			currentSum += population.get(index).getFitness();
			index++;
		}
		if(index != 0)
			index--;
		if(index > population.size()/2) {
			indexTwo = (population.size() + index) - population.size();
		}else{
			indexTwo = index + population.size()/2 -1;
		}
		selection.add(population.get(index).copy());
		selection.add(population.get(indexTwo).copy());
		return selection;
	}

	private Individual getBest(ArrayList<Individual> aPopulation) {
		double bestFitness = Double.MAX_VALUE;
		Individual best = null;
		for(Individual individual : aPopulation){
			if(individual.getFitness() < bestFitness || best == null){
				best = individual;
				bestFitness = best.getFitness();
			}
		}
		return best;
	}

	private Individual getWorst(ArrayList<Individual> aPopulation) {
		double worstFitness = 0;
		Individual worst = null;
		for(Individual individual : aPopulation){
			if(individual.getFitness() > worstFitness || worst == null){
				worst = individual;
				worstFitness = worst.getFitness();
			}
		}
		return worst;
	}

	private void printPopulation() {
		for(Individual individual : population){
			System.out.println(individual);
		}
	}

	private void initialisePopulation() {
		population.clear();
		while(population.size() < Parameters.popSize){
			Individual individual = new Individual();
			individual.initialise();
			individual.evaluate(teamPursuit);
			population.add(individual);

		}
	}
}