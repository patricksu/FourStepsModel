import java.util.Vector;
import java.io.*;
import java.lang.*;
//This class is for trip synthesizer. It stores the trips in a seed.
public class Seed 
{
	
	private int TAZID;
	private int numOfSeeds;
	private Vector<Trip> seedsData=new Vector<Trip>();
	public void addTripToSeedsData(Trip valueTrip)
	{
		seedsData.add(valueTrip);
	}
	public Vector<Trip> getSeedsData()
	{
		return this.seedsData;
	}
	public void setTAZID(int TAZID)
	{
		this.TAZID=TAZID;
	}
	public int getTAZID()
	{
		return this.TAZID;
	}
	public void setNumOfSeeds(int numOfSeeds)
	{
		this.numOfSeeds=numOfSeeds;
	}
	public void numOfSeedsPlus()
	{
		this.numOfSeeds=this.numOfSeeds+1;
	}
	public int getNumOfSeeds()
	{
		return this.numOfSeeds;
	}
}
