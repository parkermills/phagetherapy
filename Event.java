package virus_Bacteria_Simulation;

import virus_Bacteria_Simulation.Organism.EVENT_TYPE;

/**
 * Event class for maintaining event parameters
 * @author Parker, Huttner
 *
 */
public class Event {

	
	/* Instance Variables */
	EVENT_TYPE type;
	double lambda; // The event's exponential distribution lambda variable
	Organism org; // The organism this event belongs to
	boolean delete; // This event's validity flag

	/**
	 * Default constructor - creates an empty event
	 */
	public Event() {
		org = null;
		lambda = 0.0;
		type = null;
		delete = true;
	}

	/**
	 * Constructor for creating new, organism specific, event
	 * 
	 * @param org - organism event is created for (virus/bacteria)
	 * @param new_lambda - events exponential distribution
	 * @param new_type - type of event
	 */
	public Event(Organism org, double new_lambda, EVENT_TYPE new_type) {
		this.org = org;
		lambda = new_lambda;
		this.type = new_type;
		delete = false;
	}
}

