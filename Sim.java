package virus_Bacteria_Simulation;

/* 
 * Bacteria / Lambda Phage Simulation
 * Aaron Huttner & Parker Mills
 * Final Project, April 2007
 * Russell Schwartz
 * Computational Methods for Biological Modeling and Simulation
 * 
 * Acronyms Used:
 * lp - Lambda Phage
 * ba - Bacterium
 * t - time
 */


/* Imports */
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Vector;
import java.util.Random;

import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;

/**
 * Main simulation class
 * 
 * @author Parker, Huttner
 *
 */
public class Sim{
	/* Final Variables (Edit for different simulations) */
	static final int lp_max = 1000; //Max number of lambda phage
	static final int ba_max = 100; //Max number of bacteria
	static int lp_start = 100; //Starting nbumber of lambda phage
	static int ba_start = 200; //Starting number of bateria
	static final boolean DEBUG = true; //Boolean flag for debugging printouts
	static int step_max = 50000000; //Max value allowed for step_sum
	
	/* Class Variables */
	static double t; //Current time in simulation
	static int i,j; //Loop Variables
	static Random randy = new Random(); // Random variable
	static Vector<Event> events = new Vector<Event>(); //List of events
	static Vector<Lambda_phage> lps = new Vector<Lambda_phage>();
	static Vector<Bacterium> bas = new Vector<Bacterium>();
	static String exportTo;//location to export data
	
	/* Running Sums */
	static double bas_sum_sr; //sum of resistance levels for bacteria surface receptors
	static double bas_sum_enz;//sum of resistance levels for bacteria enzymes
	static double lps_sum_sr; //sum of resistance levels for bacteria surface receptors
	static double lps_sum_enz;//sum of resistance levels for bacteria enzymes
	static int bas_infected = 0;//sum of bacteria infected;
	static int step_sum = 0; //Total number of events so far
	
	/* Graphing Variables */
	private static XYSeries lps_pop = new XYSeries("Lambda_Phage");
	private static XYSeries bas_pop = new XYSeries("Bacteria");
	private static XYSeries bas_avg_sr_data = new XYSeries("bas_avg_sr");
	private static XYSeries bas_avg_enz_data = new XYSeries("bas_avg_enz");
	private static XYSeries lps_avg_sr_data = new XYSeries("lps_avg_sr");
	private static XYSeries lps_avg_enz_data = new XYSeries("lps_avg_enz");
	private static XYSeries bas_infected_data = new XYSeries("bas_infected");
	
	
	/* Debuging Variables */
	private static double test_count = 0;
	
	/**
	 * Main
	 * @param args
	 */
	public static void main(String[] args) {
		
		/* get user input */
		getInfo();
		
		/* Create starting number of bacteria and virus */
		for(i=0; i<lp_start; i++) lps.add(new Lambda_phage());
		for(i=0; i<ba_start; i++) bas.add(new Bacterium());
		
		/* Run simulation */
		while(true){
			
			/* Remove invalid events */
			for(i=0; i<events.size(); i++) if(events.get(i).delete) events.remove(i);
			
			/* If system still valid */
			if(events.size()!=0 && bas.size()!=0 && lps.size()!=0){
				
				/* Initialize Event Traversal */
				double min_time = Double.MAX_VALUE; //The time until first event
				Event first_event = new Event(); 
				
				/*  Traverse list of events */
				for(i=0; i<events.size(); i++){
					double time = -Math.log(randy.nextDouble())/events.get(i).lambda; //Calculate time for each valid event
					if(time < min_time){ //Find the event that happens first
						min_time = time;
						first_event = events.get(i);
					}
				}
				
				/* Store number of steps that have occurred */
				step_sum += events.size();
				
				/* Graph Relevant Variables */
				addParameters();
				
				t += min_time; //Update the time
				first_event.org.execute(first_event.type); //Call event!
				
				if(DEBUG) System.out.println(String.valueOf(t) + " " + first_event.type.toString() + " ");
				
				if(step_sum >= step_max ){break;}
				
				int count = 0;
				for(Bacterium ba : bas){
					if(ba.infected){
						count++;
					}
				}
				if(bas_infected != count){
					System.err.println(first_event.type.toString());
					System.out.println("bas_infected : count ==> " + bas_infected + " : " + count);
					System.exit(1);
				}
			
				
			}
			else{
				System.out.println("Critical population extinct");
				System.exit(1);
			}
			
		}
		

		
		
		/* graph populations */
		XYSeries[] test = {lps_pop,bas_pop};
		graph(test, "Population", "Time", "Populaiton Size"); // call graph method to plot visual representation of data
	//	printData(test);
		
		/* graph average resistance */
		XYSeries[] resistance = {bas_avg_sr_data, bas_avg_enz_data,lps_avg_sr_data, lps_avg_enz_data};
		graph(resistance, "Average Resistance", "Time", "Resistance Probability");
	//	printData(resistance);
		
		/* graph bas_infected_data */
		XYSeries[] infected = {bas_infected_data};
		graph(infected,"Percent Bacteria Infected", "Time", "Percent Infected");
	//	printData(infected);
		
	}
	
	/**
	 * Adds parameters to graph
	 */
	public static void addParameters(){
		
		lps_pop.add(new XYDataItem(t,lps.size()));
		bas_pop.add(new XYDataItem(t,bas.size()));
		bas_avg_sr_data.add(new XYDataItem(t,bas_sum_sr / bas.size()));
		bas_avg_enz_data.add(new XYDataItem(t,bas_sum_enz / bas.size()));
		bas_infected_data.add(new XYDataItem(t, (bas_infected / (double)bas.size()) * 100d));
		lps_avg_sr_data.add(new XYDataItem(t,lps_sum_sr / lps.size()));
		lps_avg_enz_data.add(new XYDataItem(t, lps_sum_enz / lps.size()));
		
	}
	
	/**
	 * Get user input as defind in a text file
	 * 	lp_start \n
	 * 	ba_start \n
	 * 	step_max \n
	 */
	public static void getInfo(){
		BufferedReader br;
		File input;
		
		try{
			/* get file name */
			br = new BufferedReader(new InputStreamReader(System.in));
			System.out.print("Enter file name with parameters (lp_start,ba_start,step_max): ");
			input = new File(br.readLine());
			br.close();
			
			/* get file parameters */
			br = new BufferedReader(new FileReader(input));
			lp_start = Integer.valueOf(br.readLine());
			ba_start = Integer.valueOf(br.readLine());
			step_max = Integer.valueOf(br.readLine());
			exportTo = br.readLine();
			br.close();

			
		}
		catch(Exception e){
			
		}
		
	}
	
	/**
	 * Graphs data using freeChart
	 * @param series
	 * @param Title
	 * @param x_axis
	 * @param y_axis
	 */
	public static void graph(XYSeries[] series, String Title, String x_axis, String y_axis){
		new Graph(series, Title, x_axis, y_axis);
		
	}
	
	/**
	 * Prints data to a text file with "seriesName" as the file name
	 * @param series
	 * @param seriesName
	 */
	public static void printData(XYSeries[] series){
		for(XYSeries nextSeries : series){
		File newFile = new File(exportTo + nextSeries.getKey() +".txt");
		
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(newFile));
			Iterator dataIter = nextSeries.getItems().iterator();
			while(dataIter.hasNext()){
				XYDataItem dataItem = (XYDataItem) dataIter.next();
				bw.write(dataItem.getX() + "\t" + dataItem.getY() + "\n");
			}
			
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
		
	}
	
}