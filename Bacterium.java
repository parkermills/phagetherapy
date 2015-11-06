package virus_Bacteria_Simulation;

import java.util.Random;


/**
 * Class for creating bacteria; creates a new (single) bacteria with defined rates.
 * Creates appropriate events for new bacteria and adds them to the event pool
 * @author Parker, Huttner
 *
 */
public class Bacterium implements Organism{
	
	
	
	/* Class Variables */
	static final double rate_death = 0.5; 
	static final double rate_reproduce = 5.0; 
	static final double rate_conjugation = 1.0; 
	static Random rand = new Random();
	static final String DEATH = "DEATH";
	static final String REPRODUCE = "REPRODUCE";
	static final String CONJUGATION = "CONJUGATION";
	
	/* Instance Variables */
	boolean infected; 
	Lambda_phage lp; //infecting phage
	double prob_surface; // probability of surface receptors not allowing binding of virus
	double prob_enzymes; // probability of bacterial enzymes degrading viral DNA
	double mut_inc = 0.2;// amount mutation helps resistance
	double mut_dec = 0.1;// amount mutation hinders resistance
	double[] sr_prob = {.97,.02,.01}; // prob of surface receptor staying the same / increasing efficacy / decreasing efficacy
	double[] enz_prob = {.975,.015,.01}; // prob of enzymes staying the same / increasing efficacy / decreasing efficacy

	
	/**
	 * Default constructor
	 *
	 */
	public Bacterium(){
		
		this.prob_surface = ((double)rand.nextInt(6))/10.0d;
		this.prob_enzymes = ((double)rand.nextInt(6))/10.0d;
		
		Sim.bas_sum_sr += this.prob_surface;
		Sim.bas_sum_enz += this.prob_enzymes;
		
		infected = false;
		lp = null;
		
		addEvents(this);
		
	}
	
	
	/**
	 * Constructor takes in surface receptor and enzyme probabilities
	 * @param prob_surface - probability surface receptor defends against infection
	 * @param prob_enzymes - probability enzymes degreade viral DNA
	 */
	public Bacterium(double prob_surface, double prob_enzymes){
		this.prob_surface = prob_surface;
		this.prob_enzymes = prob_enzymes;
		
		Sim.bas_sum_sr += this.prob_surface;
		Sim.bas_sum_enz += this.prob_enzymes;
		
		infected = false;
		lp = null;
		
		addEvents(this);
	}
	
	/**
	 * Adds all necessary events to Sim event vector
	 * @param bac - this bacteria
	 */
	private void addEvents(Bacterium bac){
		Sim.events.add(new Event(bac,rate_death,EVENT_TYPE.DEATH));
		Sim.events.add(new Event(bac,rate_reproduce,EVENT_TYPE.REPRODUCE));
		Sim.events.add(new Event(bac,rate_conjugation,EVENT_TYPE.CONJUGATION));
	}
	
	/**
	 * Executes events for this bacteria specififed by event type reference
	 */
	public void execute(EVENT_TYPE type){
		
		switch (type){
			case DEATH:
				death();
				break;
			case REPRODUCE:
				reproduce();
				break;
			case CONJUGATION:
				conjugation();
				break;
			default:
				System.err.println("invalid event type");
				break;
		}
	}

	/**
	 * Death event removes bacterium from population and all events associated with it
	 *
	 */
	public boolean death(){
		/* Remove this bacterium from bacteria vector */
		Sim.bas.remove(this);
	
		/* Update Running Sums */
		Sim.bas_sum_sr -= this.prob_surface;
		Sim.bas_sum_enz -= this.prob_enzymes;
		if(this.infected) Sim.bas_infected--;
		
		/* Delete all events related to this bacterium */
		for(Event evt : Sim.events) //Traverse Events
			if(evt.org.equals(this)) evt.delete = true; // Flag event for deletion
		if(infected){
			for(Event evt : Sim.events) if(evt.org.equals(lp)) evt.delete = true; //Traverse Events
		}
		
		return true;
	}
	
	/**
	 * Calculates new resistance probabilites for surface receptors and enzymes to be passed on to progeny
	 * Adds new bacteria to population and updates events accordingly
	 */
	public void reproduce(){
		double new_sr = 0, new_enz = 0; // new surface and enzyme probabiliteis for progeny
		double next_rand = rand.nextDouble(); // random number for mutation
		
		/* Handle surface receptor probabilities */
		if(next_rand >= 0 && next_rand <= sr_prob[0]){new_sr = prob_surface;}
		else if(next_rand > sr_prob[0] && next_rand <= sr_prob[1]){new_sr = prob_surface + mut_inc * (1-prob_surface);}
		else if(next_rand > sr_prob[1] && next_rand < sr_prob[2]){new_sr = prob_surface - mut_dec * (prob_surface);}
		else { new_sr = prob_surface;}
		
		
		/* Handle enzyme probabilities */
		next_rand = rand.nextDouble();
		if(next_rand >= 0 && next_rand <= enz_prob[0]){new_enz = prob_enzymes;}
		else if(next_rand > enz_prob[0] && next_rand <= enz_prob[1]){new_enz = prob_enzymes + mut_inc * (1-prob_enzymes);}
		else if(next_rand > enz_prob[1] && next_rand < enz_prob[2]){new_enz = prob_enzymes - mut_dec * (prob_enzymes);}
		else { new_enz = prob_enzymes;}
		
		
		/* Add bacteria and events to the simulation */
		Bacterium new_bacterium = new Bacterium(new_sr,new_enz);
		if(this.infected){
			new_bacterium.infected = true;
			new_bacterium.lp = new Lambda_phage(this.lp, new_bacterium); // calls infect constructor
		}
		Sim.bas.add(new_bacterium);
		addEvents(new_bacterium);
	}
	
	/**
	 * Conjugation performas bacterial conjugation by selecting a random bacterium from the population
	 * and confering additional resistance to it. Rate increase is based on current prob_resistance
	 *
	 */
	public void conjugation(){
		int bac_index = rand.nextInt(Sim.bas.size()); // bacteria to confer resistance to
		
		/* Confer surface receptor resistance */
		Sim.bas.get(bac_index).prob_surface = Sim.bas.get(bac_index).prob_surface + 
			this.prob_surface * (1 - Sim.bas.get(bac_index).prob_surface);
		
		/* Confer enyzme resistance */
		Sim.bas.get(bac_index).prob_enzymes = Sim.bas.get(bac_index).prob_enzymes + 
			this.prob_enzymes * (1 - Sim.bas.get(bac_index).prob_enzymes);
		
	}


	


	




	


	


	
	
	
	
}