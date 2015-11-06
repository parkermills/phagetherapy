package virus_Bacteria_Simulation;

/**
 * Creates new virus and adds appropriate events to event pool.
 * 
 * @author Parker, Huttner
 *
 */
public class Lambda_phage implements Organism {

	
	/* Class Variables */
	static final int progeny = 150; // # progeny for phage lysis
	static final double rate_denature = 0.5;
	static final double rate_infection = 11.0;
	static final double rate_switch = 3.0; // lysogenic to lytic switch rate
	static final double rate_secrete = 7.0; // lysogenic secretion rate
	static final double mutate_prob = 0.06;
	static final double mutate_helps = 0.33;
	static final double mutate_amount = 0.2; // "Amount" mutation helps
	
	/*Instance Variables */
	Bacterium current_ba; // If a prophage, it is inside this bacterium

	double prob_surface;
	double prob_enzymes;


	/**
	 * Default constructor for new viruses
	 */
	public Lambda_phage() {
		
		current_ba = null;
		
		/* generate initial probabilities */
		this.prob_surface = ((double) Sim.randy.nextInt(6)) / 10.0d;
		this.prob_enzymes = ((double) Sim.randy.nextInt(6)) / 10.0d;
		
		/* add events */
		Sim.events.add(new Event(this, rate_denature, EVENT_TYPE.DENATURE)); 
		Sim.events.add(new Event(this, rate_infection,EVENT_TYPE.INFECT)); 

		/* update simulation counters */
		Sim.lps_sum_sr += this.prob_surface;
		Sim.lps_sum_enz += this.prob_enzymes;
	}

	/**
	 * Constructor for lytic or lysogenic virus progeny
	 * @param prob_surface - probability of overcoming bacterial surface receptor defence
	 * @param prob_enzymes - probability of overcomng bacterial enzyme defence
	 */
	public Lambda_phage(double prob_surface, double prob_enzymes) {
		
		current_ba = null;
		
		/* check for mutation probability */
		if (mutate_prob >= Sim.randy.nextDouble()) { 
			double new_prob_surface, new_prob_enzymes;
			
			/* check for helpful mutation probability */
			if (Sim.randy.nextDouble() > mutate_helps) {
				new_prob_surface = prob_surface + Sim.randy.nextDouble()
						* mutate_amount * (1 - prob_surface);
				new_prob_enzymes = prob_enzymes + Sim.randy.nextDouble()
						* mutate_amount * (prob_surface);
			
			/* check for harmful mutation probability */
			} else { // If mutation hurts
				new_prob_surface = prob_surface - Sim.randy.nextDouble()
						* mutate_amount * (1 - prob_enzymes);
				new_prob_enzymes = prob_enzymes - Sim.randy.nextDouble()
						* mutate_amount * (prob_enzymes);
			}
			this.prob_surface = new_prob_surface;
			this.prob_enzymes = new_prob_enzymes;
			
		/* mutation does not occur */
		} else { 
			this.prob_surface = prob_surface;
			this.prob_enzymes = prob_enzymes;
		}

		/* add events */
		Sim.events.add(new Event(this, rate_denature, EVENT_TYPE.DENATURE)); 
		Sim.events.add(new Event(this, rate_infection,EVENT_TYPE.INFECT)); 

		/* update simulation counters */
		Sim.lps_sum_sr += this.prob_surface;
		Sim.lps_sum_enz += this.prob_enzymes;
	}

	/**
	 * Constructor for virus that results from INFECTED bacterium reproducing
	 * 
	 * @param old_lps - original virus
	 * @param new_ba - new bacterium
	 */
	public Lambda_phage(Lambda_phage old_lps, Bacterium new_ba) { 

		current_ba = new_ba;
		
		this.prob_surface = old_lps.prob_surface;
		this.prob_enzymes = old_lps.prob_enzymes;

		/* Create events */
		Sim.events.add(new Event(this, rate_switch, EVENT_TYPE.SWITCHPHASE));
		Sim.events.add(new Event(this, rate_secrete, EVENT_TYPE.SECRETE));

		/* update simulation counters */
		Sim.lps_sum_sr += this.prob_surface;
		Sim.lps_sum_enz += this.prob_enzymes;
		Sim.bas_infected++;
	}

	/**
	 * Excecute - calls appropriate event functions.
	 */
	public void execute(EVENT_TYPE type) {

		switch (type){
			case DENATURE:
				denature();
				break;
			case INFECT:
				infect();
				break;
			case SWITCHPHASE:
				switchPhase();
				break;
			case SECRETE:
				secrete();
				break;
			default:
				System.err.println("invalid event type");
				break;
		}
		
	}

	/**
	 * DENATURE -Invalidate all events for this virus -Remove virus from virus
	 * vector
	 * @return true if virus denatured, false otherwise.
	 */
	public boolean denature() {

		/* check that current virus has not infected a bacterium */
		if (current_ba == null) { 

			/* Remove all events related to this virus */
			for (int i = 0; i < Sim.events.size(); i++) {
				if (Sim.events.elementAt(i).org.equals(this))
					Sim.events.elementAt(i).delete = true;
			}
			
			/* Remove this virus from lambda vector */
			for (int i = 0; i < Sim.lps.size(); i++) {
				if (Sim.lps.elementAt(i).equals(this)) {
					Sim.lps.remove(i);
					Sim.lps_sum_sr -= this.prob_surface;
					Sim.lps_sum_enz -= this.prob_enzymes;
				}
			}
			return true;
		}
		return false; 
	}

	/**
	 * INFECT -Invalidate ALL existing events for this virus, since it can no
	 * longer denature or infect -Create "switch" event for switching to lytic
	 * cycle -Choose random bacteria, and update it. -Update virus object
	 * @return true if virus successfully infected a bacterium, false otherwise.
	 */
	public boolean infect() {
		/* Remove all events related to this virus */
		for (int i = 0; i < Sim.events.size(); i++)
			if (Sim.events.elementAt(i).org.equals(this))
				Sim.events.elementAt(i).delete = true;

		/* Choose a bacteria to infect */
		Bacterium chosen_bacterium = Sim.bas.get(Sim.randy.nextInt(Sim.bas
				.size()));

		/* check probability of successful infection */
		if ((((1 - chosen_bacterium.prob_surface) * this.prob_surface) >= Sim.randy
				.nextDouble())
				&& (((1 - chosen_bacterium.prob_enzymes) * this.prob_enzymes) >= Sim.randy
						.nextDouble()) && !chosen_bacterium.infected) {

			/* Update virus object */
			this.current_ba = chosen_bacterium;

			/* Update bacteria object */
			chosen_bacterium.infected = true;
			chosen_bacterium.lp = this;

			/* Update bas_infected DataSeries in Sim */
			Sim.bas_infected++;

			/* Create "switch" and "secrete" event */
			Sim.events.add(new Event(this, rate_switch, EVENT_TYPE.SWITCHPHASE));
			Sim.events.add(new Event(this, rate_secrete, EVENT_TYPE.SECRETE));

			return true;
			
		} 
		/* check to see if virus has failed to infect */
		else{ 
		
			/* Remove this virus from lambda vector and update counters */
			for (int i = 0; i < Sim.lps.size(); i++) {
				if (Sim.lps.elementAt(i).equals(this)) {
					Sim.lps.remove(i);
					Sim.lps_sum_sr -= this.prob_surface;
					Sim.lps_sum_enz -= this.prob_enzymes;
				}
			}
			return false; 
		}

	}

	/**
	 * SWITCHPHASE -Remove all events related to this virus -Remove this virus from
	 * lambda vector -Remove all events related to this bacterium -Remove this
	 * bacterium from bacteria vector -Create new virus particles
	 *
	 */
	public void switchPhase() {

		/* Remove all events related to this virus */
		for (int i = 0; i < Sim.events.size(); i++)
			if (Sim.events.elementAt(i).org.equals(this))
				Sim.events.elementAt(i).delete = true;

		/* Remove this virus from lambda vector */
		for (int i = 0; i < Sim.lps.size(); i++)
			if (Sim.lps.elementAt(i).equals(this)) {
				Sim.lps.remove(i);
				Sim.lps_sum_sr += this.prob_surface;
				Sim.lps_sum_enz += this.prob_enzymes;
			}

		/* kill infected bacteria */
		this.current_ba.death(); 

		/* Create new virus progeny */
		for (int i = 0; i < progeny; i++)
			Sim.lps.add(new Lambda_phage(prob_surface, prob_enzymes));

	}

	/**
	 * SECRETE - Lysogenic secretion of a new virus particle. -Instantiate a new
	 * virus.
	 */
	public void secrete() {
		/* Instantiate a new virus particle */
		Sim.lps.add(new Lambda_phage(prob_surface, prob_enzymes));
	}

}