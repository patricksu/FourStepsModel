import java.io.IOException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.StringTokenizer;
import java.util.Vector;
import java.io.*;

public class main 
{
	public static void main(String args[]) throws IOException
		{
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));			

			System.out.println("Generate the scenario sets? 1 for YES, 0 for NO. Most of the time" +
					"you should select 0 (or NO)");
			//Option 1: generate the trip profiles
			//Option 2: run the framework
			int option=Integer.parseInt(reader.readLine());
			if (option==1)
			{
			DataGeneration DG=new DataGeneration();
			DG.initializeNumberOfAlternativesInEachGroup();
			DG.initializeNumOfVs();
			DG.readInTAZAndID();
			DG.readInODDistanceMatrix();
			DG.readInTAZVs();
			DG.buildSeedsSet();
			DG.readInGeneration();
			DG.initializeCutPoints();
			
			DG.buildScenarioSet("HBW");
			DG.buildScenarioSet("HBShop");
			DG.buildScenarioSet("HBSRO1");
			DG.buildScenarioSet("HBSRO2");
			//Note that HBSRO has so big file that java runs out of memory. Thus it is split into two parts.
			DG.buildScenarioSet("NHBW");
			DG.buildScenarioSet("NHBO");
			}
			else if (option==0)
			//Run destination choice and mode choice model. 
			//The land use variables are loaded when needed. 
			//The travel time is calculated according auto travel time and predefined ratios. 
			{
			ModelRun MR=new ModelRun();
			MR.initializeNumberOfAlternativesInEachGroup();
			MR.initializeNumOfVs();
			
			MR.readInTAZAndID();		
			MR.readInODDistanceMatrix();
			MR.initializeCutPoints();
			MR.readInTAZVs();
			MR.buildSizeVariable();						
			MR.readInAutoTravelTimeMatrix();
			
			MR.readInScenarioSet("HBW");
			MR.DCMCModelRun("HBW");
			MR.readInScenarioSet("HBShop");
			MR.DCMCModelRun("HBShop");
			MR.readInScenarioSet("HBSRO1");
			MR.DCMCModelRun("HBSRO1");
			MR.readInScenarioSet("HBSRO2");
			MR.DCMCModelRun("HBSRO2");
			MR.readInScenarioSet("NHBW");
			MR.DCMCModelRun("NHBW");
			MR.readInScenarioSet("NHBO");
			MR.DCMCModelRun("NHBO");
			MR.finalResult();
	
			System.out.println("All the four steps are done");
			}
			else
			{
				System.out.println("You must be kidding me.");
			}
		}
}
