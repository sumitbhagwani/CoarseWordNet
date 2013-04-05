package ch.usi.inf.sape.hac.experiment;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import krsystem.StaticValues;
import weka.core.Attribute;
import weka.core.Instance;

public class MyDissimilarityMeasureSQL implements DissimilarityMeasure{

	Connection connection = null;
	
	public MyDissimilarityMeasureSQL()
	{
		try {
		    System.out.println("Loading driver...");
		    Class.forName("com.mysql.jdbc.Driver");
		    System.out.println("Driver loaded!");
		} catch (ClassNotFoundException e) {
		    throw new RuntimeException("Cannot find the driver in the classpath!", e);
		}
		
		String ip = StaticValues.cseLabIP;
		String url = "jdbc:mysql://"+ip+":3306/synsetSimilarity";
		String username = StaticValues.sqlUsername;
		String password = StaticValues.sqlPassword;		
		try {
		    System.out.println("Connecting database...");
		    connection = DriverManager.getConnection(url, username, password);
		    System.out.println("Database connected!");
		}
		catch (SQLException e) {
			e.printStackTrace();
		    throw new RuntimeException("Cannot connect the database!", e);
		} catch(Exception ex)
		{
			ex.printStackTrace();
			System.exit(-1);
		}
	}
	
	@Override
	public double computeDissimilarity(Experiment experiment, int observation0,
			int observation1) {
		Instance arg0 = experiment.getObservation(observation0);
		Instance arg1 = experiment.getObservation(observation1);
		Attribute offset = arg0.dataset().attribute("offset");
		Attribute pos = arg0.dataset().attribute("pos");
		
		String offsetString0 = arg0.stringValue(offset);
		long offset0 = Long.parseLong(offsetString0);
		String offsetString1 = arg1.stringValue(offset);
		long offset1 = Long.parseLong(offsetString1);
				
		String pos0  = arg0.stringValue(pos);
		String pos1  = arg1.stringValue(pos);
		if(!pos0.equalsIgnoreCase(pos1))
		{
			System.out.println("Error : POS Mismatch while finding distance");
		}
					
		try{
		// Get a statement from the connection
	      Statement stmt = connection.createStatement() ;
	      // Execute the query
	      String smaller, larger;
	      if(offsetString0.compareToIgnoreCase(offsetString1) < 0)
	      {
	    	  smaller = offsetString0;
	    	  larger  = offsetString1;
	      }
	      else
	      {
	    	  smaller = offsetString1;
	    	  larger  = offsetString0;
	      }
	      ResultSet rs = stmt.executeQuery( "SELECT * FROM synsetSimilarityNoun WHERE smallerSynsetOffset='"+smaller+"' AND largerSynsetOffset='"+larger+"'" ) ;
	      if(rs != null)
	      {
		      // Loop through the result set
		      while( rs.next() )
		      {	 
		    	 double score = Double.parseDouble(rs.getString(3));
		         System.out.println("Score : "+ rs.getString(1) + " "+ rs.getString(2) + " "+score) ;
		         return -1*score;
		      }
	      }
		}
		catch (SQLException e) {
			e.printStackTrace();
		    throw new RuntimeException("Cannot connect the database!", e);
		}
		return 0;		
	}


}
