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


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import teamPursuit.TeamPursuit;
import teamPursuit.WomensTeamPursuit;

public class EA implements Runnable{
	
	// create a new team with the default settings
	public static TeamPursuit teamPursuit = new WomensTeamPursuit(); 
	
	private ArrayList<Individual> population = new ArrayList<Individual>();
	private int iteration = 0;
	
	public EA() {
		
	}

	
	public static void main(String[] args) {
		EA ea = new EA();
		ea.run();
	}

	public void run() {
		initialisePopulation();	
		System.out.println("finished init pop");
		iteration = 0;
		boolean rank = false;
		//used for rank selection swap, if the population stagnates for a number of generations, switch to rank
		double bestFit = getBest(population).getFitness();
		int count = 0;
		while(iteration < Parameters.maxIterations){
			iteration++;
			Individual parent1, parent2;
			if (rank){
				parent1 = rankSelection();
				parent2 = rankSelection();
			}else{
				parent1 = tournamentSelection();
				parent2 = rouletteSelection();
			}

			//ArrayList<Individual> parents = stochasticSelection();
			//Individual parent1 = parents.get(0);
			//Individual parent2 = parents.get(1);
			Individual child = uniformCrossover(parent1, parent2);
			//Individual child = twoCrossover(parent1, parent2);
			child = mutate(child);
			child.evaluate(teamPursuit);
			replace(child);
			printStats();
			if(getBest(population).getFitness() == bestFit && !rank){
				count++;
			}else{
				bestFit = getBest(population).getFitness();
				count =0;
			}

			if(count >200){
				System.out.println("switching selection");
				count = 0;
				rank = !rank;
			}

		}						
		Individual best = getBest(population);
		best.print();
		
	}

	private void printStats() {		
		System.out.println("" + iteration + "\t" + getBest(population) + "\t" + getWorst(population));		
	}


	private void replace(Individual child) {
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
				int index = Parameters.rnd.nextInt(child.pacingStrategy.length);
				int index2 = Parameters.rnd.nextInt(child.pacingStrategy.length);
				int p = child.pacingStrategy[index];
				child.pacingStrategy[index] = child.pacingStrategy[index2];
				child.pacingStrategy[index2] = p;
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
		if(crossoverPointOneP < crossoverPointTwoP){
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
	private Individual tournamentSelection() {
		ArrayList<Individual> candidates = new ArrayList<Individual>();
		for(int i = 0; i < Parameters.tournamentSize; i++){
			candidates.add(population.get(Parameters.rnd.nextInt(population.size())));
		}
		return getBest(candidates).copy();
	}


	private Individual rouletteSelection(){
		double fitnessSum = 0;
		for(int i =0; i<population.size();i++){
			fitnessSum += population.get(i).getFitness();
		}
		//random pointer in our fitness sum (the specific number points to the individual that is at that location in the roulette)
		int randNumber = Parameters.rnd.nextInt((int)fitnessSum);
		int currentSum = 0;
		int index = 0;
		//spin the wheel until we meet the pointer set
		while(currentSum < randNumber && index < population.size()){
			currentSum += population.get(index).getFitness();
			index++;
		}
		if(index != 0)
			index --;
		return population.get(index).copy();
	}
	private Individual rankSelection(){
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
		for(Individual individual : population){
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
		while(population.size() < Parameters.popSize){
			Individual individual = new Individual();
			individual.initialise();			
			individual.evaluate(teamPursuit);
			population.add(individual);
							
		}		
	}	
}
