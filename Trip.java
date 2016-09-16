import java.util.Vector;
public class Trip {
	private double[] Variables;
	public void initializeVariables(int numOfVs)
	{
		Variables=new double[numOfVs];
	}
	public void addValueToVariables(int i,double value)
	{
		Variables[i]=value;
	}
	public double getValue(int i)
	{
		return Variables[i];
	}
}
