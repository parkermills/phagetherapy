package virus_Bacteria_Simulation;


/**
 * General inteface for handling event execution for organisms
 * @author Aaron Huttner
 *
 */
public interface Organism {
	
	enum EVENT_TYPE {DEATH, REPRODUCE, CONJUGATION, DENATURE, INFECT, SWITCHPHASE, SECRETE};

	
	/**
	 * execute event
	 * @param type - EVENT_TYPE
	 */
	public void execute(EVENT_TYPE type);
	
	
}
