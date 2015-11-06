package virus_Bacteria_Simulation;

// Import the Swing classes
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

// Import the JFreeChart classes
import org.jfree.chart.*;
import org.jfree.chart.plot.*;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.XYSeries;

/**
 * Graph Class to create XY line chart for two data sets
 * @author Parker, Huttner
 *
 */
public class Graph extends JPanel{
	
	private DefaultXYDataset xyDataset = new DefaultXYDataset();// Holds the data
  	private JFreeChart XYChart;// Create chart
  	private ChartPanel XYPanel;// Create panel


 /**
  * Default constructor - builds graph
  * @param series
  * @param Title
  * @param x_axis
  * @param y_axis
  */
  public Graph(XYSeries[] series, String Title, String x_axis, String y_axis){	  
	
	  
	  for(int i = 0; i < series.length; i++){
		  xyDataset.addSeries(series[i].getKey(), series[i].toArray());
	  }
	  
	  /* Create  the chart */
	  XYChart = ChartFactory.createScatterPlot(
				Title, x_axis, 
				y_axis, xyDataset, PlotOrientation.VERTICAL, true, false, false);
	
	  /* Create this panel */
	  this.setLayout( new GridLayout( 1, 1 ) );
	  this.XYPanel = new ChartPanel(XYChart);
	  this.add(XYPanel);
	  
	  createFrame();
	  
  }
  
  
  /**
   * creates a frame to display the chart
   *
   */
  public void createFrame() {
	  
	JFrame frame = new JFrame( "XY Line Chart" );
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
	frame.getContentPane().add( this, BorderLayout.CENTER );
	frame.setSize( 640, 480 );
	frame.setVisible(true);
	frame.setResizable(true);

	for(WindowListener l : frame.getWindowListeners()){
		frame.addWindowListener(l);
	}
	
		    
  }
  
  /**
   * monitors window closing event
   * @param evt, window event
   */
  public void WindowClosing(WindowEvent evt){
	  System.exit(0);
  }

  
} 