import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.Arrays;
import java.io.*;
import java.util.Random;
import static java.lang.Math.*;

public class ModelRun {		
	private double[][] TAZVs=new double[403][42];
	private double[][] autoTimeMatrix=new double[403][403];
	private double[][] ODDistanceMatrix=new double[403][403];
	private int[] TAZAndID=new int[403];//the inquiry list between TAZ and ID
	private int numOfVs;//the number of socio-demographic variables
	private int numOfSDVs;
	private int[] numberOfAlternativesInEachGroup=new int[5];
	private int totalNumberOfAlternatives=0;
	private double[][] cutPoints=new double[6][3];
	
	private Vector<Trip> scenarioSet=new Vector<Trip>();
	private Vector<Trip> scenarioSetHBW=new Vector<Trip>();
	private Vector<Trip> scenarioSetHBShop=new Vector<Trip>();
	private Vector<Trip> scenarioSetHBSRO1=new Vector<Trip>();
	private Vector<Trip> scenarioSetHBSRO2=new Vector<Trip>();
	private Vector<Trip> scenarioSetNHBW=new Vector<Trip>();
	private Vector<Trip> scenarioSetNHBO=new Vector<Trip>();
	
	private double[][][] ODMatrixHBW=new double[403][403][7];
	private double[][][] ODMatrixHBShop=new double[403][403][7];
	private double[][][] ODMatrixHBSRO1=new double[403][403][7];
	private double[][][] ODMatrixHBSRO2=new double[403][403][7];
	private double[][][] ODMatrixNHBW=new double[403][403][7];
	private double[][][] ODMatrixNHBO=new double[403][403][7];
	private double[][] ODMatrix=new double[403][403];
	private double[][] ODMatrixWalk=new double[403][403];
	private double[][] ODMatrixBike=new double[403][403];
	private double[][] ODMatrixAuto=new double[403][403];
	private double[][] ODMatrixBus=new double[403][403];
	private double[][] ODMatrixRail=new double[403][403];
	private double[][] ODMatrixTaxi=new double[403][403];
	public void initializeNumberOfAlternativesInEachGroup() throws NumberFormatException, IOException
	{
		System.out.println("Please input the number of alternatives in each group");
		BufferedReader reader=new BufferedReader(new InputStreamReader(System.in));
		numberOfAlternativesInEachGroup[1]=Integer.parseInt(reader.readLine());
		numberOfAlternativesInEachGroup[2]=Integer.parseInt(reader.readLine());
		numberOfAlternativesInEachGroup[3]=Integer.parseInt(reader.readLine());
		numberOfAlternativesInEachGroup[4]=Integer.parseInt(reader.readLine());
		totalNumberOfAlternatives=numberOfAlternativesInEachGroup[1]+numberOfAlternativesInEachGroup[2]
		                         +numberOfAlternativesInEachGroup[3]+numberOfAlternativesInEachGroup[4];
	}
	public void initializeNumOfVs() throws NumberFormatException, IOException
	{
		System.out.println("Please input the number of variables:");
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		numOfSDVs=Integer.parseInt(reader.readLine());
		numOfVs=numOfSDVs+totalNumberOfAlternatives+1;//extra space for potential DTAZs and intrazonal distance
	}
	public void readInTAZAndID() throws NumberFormatException, IOException
	{
		StringTokenizer stLine;
		String rLine;
		String itemSep="\t";
		int i=1;
		FileInputStream fileTAZID=new FileInputStream("./TAZID.txt");
		BufferedReader readTAZID=new BufferedReader(new InputStreamReader(fileTAZID));
		i=1;
		while((rLine=readTAZID.readLine())!=null)
		{
			TAZAndID[i]=Integer.parseInt(rLine);
			i=i+1;
		}
	}
	public void readInODDistanceMatrix() throws IOException
	{
		StringTokenizer stLine;
		String rLine;
		String itemSep="\t";
		FileInputStream fileODDistanceMatrix=new FileInputStream("./ODDistanceMatrix.txt");
		BufferedReader readODDistanceMatrix=new BufferedReader(new InputStreamReader(fileODDistanceMatrix));	
		rLine=readODDistanceMatrix.readLine();
		stLine=new StringTokenizer(rLine,itemSep);
		int i;
		for(i=1;i<=402;i++)
		{
			ODDistanceMatrix[0][i]=Integer.parseInt(stLine.nextToken());
		}
		int row=0;
		while((rLine=readODDistanceMatrix.readLine())!=null)
		{
			stLine=new StringTokenizer(rLine,itemSep);
			row=row+1;
			ODDistanceMatrix[row][0]=Integer.parseInt(stLine.nextToken());
			for(i=1;i<=402;i++)
			{
				ODDistanceMatrix[row][i]=Double.parseDouble(stLine.nextToken());
			}			
		}
		readODDistanceMatrix.close();
		fileODDistanceMatrix.close();
		System.out.println("readInODDistanceMatrix is done");
	}
	public void initializeCutPoints()
	{
		cutPoints[1][1]=4.65;
		cutPoints[1][2]=9;
		cutPoints[2][1]=2.28;
		cutPoints[2][2]=4.5;
		cutPoints[3][1]=2.46;
		cutPoints[3][2]=5.08;
		cutPoints[4][1]=2.53;
		cutPoints[4][2]=5.6;
		cutPoints[5][1]=2.55;
		cutPoints[5][2]=5.35;
	}
	public void readInTAZVs() throws NumberFormatException, IOException
	{
		StringTokenizer stLine;
		String rLine;
		String itemSep="\t";
		FileInputStream fileTAZVs=new FileInputStream("./TAZVs.txt");
		BufferedReader readTAZVs=new BufferedReader(new InputStreamReader(fileTAZVs));			
		int i=1;
		while((rLine=readTAZVs.readLine())!=null)
		{
			stLine=new StringTokenizer(rLine,itemSep);
			int j=0;
			while(stLine.hasMoreElements())
			{
				TAZVs[i][j]=Double.parseDouble(stLine.nextToken());
			j=j+1;
			}
			i=i+1;
		}
		System.out.println("readInTAZVs is done");
	}
	public void buildSizeVariable() throws IOException
	{
		int iDTAZID=0;
		int iOTAZID=0;
		int iPurposeID=0;			

		for(iOTAZID=1;iOTAZID<=402;iOTAZID++)
		{
			double [][] sizeVariable=new double[6][5];
			for(iDTAZID=1;iDTAZID<=402;iDTAZID++)
			{	
				for(iPurposeID=1;iPurposeID<6;iPurposeID++)
				{
					if(iOTAZID==iDTAZID)
					{
						if(iPurposeID==2 || iPurposeID==5)
							sizeVariable[iPurposeID][1]=sizeVariable[iPurposeID][1]+TAZVs[iDTAZID][5];
						else
							sizeVariable[iPurposeID][1]=sizeVariable[iPurposeID][1]+TAZVs[iDTAZID][3];
					}
					else if (ODDistanceMatrix[iOTAZID][iDTAZID]>0 && ODDistanceMatrix[iOTAZID][iDTAZID]<cutPoints[iPurposeID][1])
					{
						if(iPurposeID==2 || iPurposeID==5)
							sizeVariable[iPurposeID][2]=sizeVariable[iPurposeID][2]+TAZVs[iDTAZID][5];
						else
							sizeVariable[iPurposeID][2]=sizeVariable[iPurposeID][2]+TAZVs[iDTAZID][3];
					}
					else if (ODDistanceMatrix[iOTAZID][iDTAZID]>cutPoints[iPurposeID][1] && ODDistanceMatrix[iOTAZID][iDTAZID]<cutPoints[iPurposeID][2])
					{
						if(iPurposeID==2 || iPurposeID==5)
							sizeVariable[iPurposeID][3]=sizeVariable[iPurposeID][3]+TAZVs[iDTAZID][5];
						else
							sizeVariable[iPurposeID][3]=sizeVariable[iPurposeID][3]+TAZVs[iDTAZID][3];
					}
					else if (ODDistanceMatrix[iOTAZID][iDTAZID]>cutPoints[iPurposeID][2])
					{
						if(iPurposeID==2 || iPurposeID==5)
							sizeVariable[iPurposeID][4]=sizeVariable[iPurposeID][4]+TAZVs[iDTAZID][5];
						else
							sizeVariable[iPurposeID][4]=sizeVariable[iPurposeID][4]+TAZVs[iDTAZID][3];
					}
				}
			}
			//TAZVs
			//1~20: normal variables. 21: intrazonal distance
			//22~25: size variable TEMP of HBW 
			//26~29: size variable REMP of HBShop
			//30~33: size variable TEMP of HBSRO
			//34~37: size variable TEMP of NHBW
			//38~41: size variable REMP of NHBO
			if(sizeVariable[1][1]==0)
			{
				TAZVs[iOTAZID][22]=0;
			}
			else
			{
				TAZVs[iOTAZID][22]=log(sizeVariable[1][1]);
			}
			if(sizeVariable[1][2]==0)
			{
				TAZVs[iOTAZID][23]=0;
			}
			else
			{
				TAZVs[iOTAZID][23]=log(sizeVariable[1][2]);
			}
			if(sizeVariable[1][3]==0)
			{
				TAZVs[iOTAZID][24]=0;
			}
			else
			{
				TAZVs[iOTAZID][24]=log(sizeVariable[1][3]);
			}
			if(sizeVariable[1][4]==0)
			{
				TAZVs[iOTAZID][25]=0;
			}
			else
			{
				TAZVs[iOTAZID][25]=log(sizeVariable[1][4]);
			}
			
			if(sizeVariable[2][1]==0)
			{
				TAZVs[iOTAZID][26]=0;
			}
			else
			{
				TAZVs[iOTAZID][26]=log(sizeVariable[2][1]);
			}
			if(sizeVariable[2][2]==0)
			{
				TAZVs[iOTAZID][27]=0;
			}
			else
			{
				TAZVs[iOTAZID][27]=log(sizeVariable[2][2]);
			}
			if(sizeVariable[2][3]==0)
			{
				TAZVs[iOTAZID][28]=0;
			}
			else
			{
				TAZVs[iOTAZID][28]=log(sizeVariable[2][3]);
			}
			if(sizeVariable[2][4]==0)
			{
				TAZVs[iOTAZID][29]=0;
			}
			else
			{
				TAZVs[iOTAZID][29]=log(sizeVariable[2][4]);
			}

			if(sizeVariable[3][1]==0)
			{
				TAZVs[iOTAZID][30]=0;
			}
			else
			{
				TAZVs[iOTAZID][30]=log(sizeVariable[3][1]);
			}
			if(sizeVariable[3][2]==0)
			{
				TAZVs[iOTAZID][31]=0;
			}
			else
			{
				TAZVs[iOTAZID][31]=log(sizeVariable[3][2]);
			}
			if(sizeVariable[3][3]==0)
			{
				TAZVs[iOTAZID][32]=0;
			}
			else
			{
				TAZVs[iOTAZID][32]=log(sizeVariable[3][3]);
			}
			if(sizeVariable[3][4]==0)
			{
				TAZVs[iOTAZID][33]=0;
			}
			else
			{
				TAZVs[iOTAZID][33]=log(sizeVariable[3][4]);
			}

			if(sizeVariable[4][1]==0)
			{
				TAZVs[iOTAZID][34]=0;
			}
			else
			{
				TAZVs[iOTAZID][34]=log(sizeVariable[4][1]);
			}
			if(sizeVariable[4][2]==0)
			{
				TAZVs[iOTAZID][35]=0;
			}
			else
			{
				TAZVs[iOTAZID][35]=log(sizeVariable[4][2]);
			}
			if(sizeVariable[4][3]==0)
			{
				TAZVs[iOTAZID][36]=0;
			}
			else
			{
				TAZVs[iOTAZID][36]=log(sizeVariable[4][3]);
			}
			if(sizeVariable[4][4]==0)
			{
				TAZVs[iOTAZID][37]=0;
			}
			else
			{
				TAZVs[iOTAZID][37]=log(sizeVariable[4][4]);
			}

			if(sizeVariable[5][1]==0)
			{
				TAZVs[iOTAZID][38]=0;
			}
			else
			{
				TAZVs[iOTAZID][38]=log(sizeVariable[5][1]);
			}
			if(sizeVariable[5][2]==0)
			{
				TAZVs[iOTAZID][39]=0;
			}
			else
			{
				TAZVs[iOTAZID][39]=log(sizeVariable[5][2]);
			}
			if(sizeVariable[5][3]==0)
			{
				TAZVs[iOTAZID][40]=0;
			}
			else
			{
				TAZVs[iOTAZID][40]=log(sizeVariable[5][3]);
			}
			if(sizeVariable[5][4]==0)
			{
				TAZVs[iOTAZID][41]=0;
			}
			else
			{
				TAZVs[iOTAZID][41]=log(sizeVariable[5][4]);
			}
		}
/*
		FileWriter fileOut=new FileWriter("TAZVs_SizeVs.txt");
		BufferedWriter fileOutBuf=new BufferedWriter(fileOut);
		for(iOTAZID=1;iOTAZID<403;iOTAZID++)
		{
			for(int j=0;j<42;j++)
			{
				fileOutBuf.write(TAZVs[iOTAZID][j]+"\t");
			}
			fileOutBuf.write("\n");
			fileOutBuf.flush();
		}
	    fileOutBuf.close();
	    fileOut.close();	
*/		
		System.out.println("buildSizeVariable is done");
	}
	public void readInAutoTravelTimeMatrix() throws IOException
	{
		StringTokenizer stLine;
		String rLine;
		String itemSep="\t";
		FileInputStream fileAutoTime=new FileInputStream("./AutoTimeMatrix.txt");
		BufferedReader readAutoTimeMatrix=new BufferedReader(new InputStreamReader(fileAutoTime));	
		rLine=readAutoTimeMatrix.readLine();		
		stLine=new StringTokenizer(rLine,itemSep);
		int i;
		for(i=1;i<=402;i++)
		{
			autoTimeMatrix[0][i]=Integer.parseInt(stLine.nextToken());
		}
		int row=0;
		while((rLine=readAutoTimeMatrix.readLine())!=null)
		{
			stLine=new StringTokenizer(rLine,itemSep);
			row=row+1;
			autoTimeMatrix[row][0]=Integer.parseInt(stLine.nextToken());
			for(i=1;i<=402;i++)
			{
				autoTimeMatrix[row][i]=Double.parseDouble(stLine.nextToken());
			}			
		}
		readAutoTimeMatrix.close();
		fileAutoTime.close();
		System.out.println("readInAutoTravelTimeMatrix is done");
	}
	public void readInScenarioSet(String purpose) throws NumberFormatException, IOException
	{
		StringTokenizer stLine;
		String rLine;
		String itemSep="\t";
		String fileName=purpose+"ScenarioOutput.txt";
		FileInputStream fileScenarioSet=new FileInputStream(fileName);
		BufferedReader readScenarioSet=new BufferedReader(new InputStreamReader(fileScenarioSet));	
		Vector<Trip> scenarioSet=new Vector<Trip>();
		//initialize the seed set
		while((rLine=readScenarioSet.readLine())!=null)
		{	
			int TAZID=0;
			int TAZ=0;
			Trip iTrip=new Trip();
			iTrip.initializeVariables(numOfVs);
			stLine=new StringTokenizer(rLine,itemSep);
			TAZ=(int)Double.parseDouble(stLine.nextToken());
			for(int j=1;j<403;j++)
			{
				if (TAZAndID[j]==TAZ)
					{
						TAZID=j;
						break;
					}
			}
			iTrip.addValueToVariables(0,TAZID);
			for(int j=1;j<numOfVs;j++)
			{
				iTrip.addValueToVariables(j, Double.parseDouble(stLine.nextToken()));
			}
			scenarioSet.add(iTrip);
		}
		if (purpose=="HBW")
		{
			scenarioSetHBW=scenarioSet;
		}
		else if (purpose=="HBShop")
		{
			scenarioSetHBShop=scenarioSet;
		}
		else if (purpose=="HBSRO1")
		{
			scenarioSetHBSRO1=scenarioSet;
		}
		else if (purpose=="HBSRO2")
		{
			scenarioSetHBSRO2=scenarioSet;
		}
		else if (purpose=="NHBW")
		{
			scenarioSetNHBW=scenarioSet;
		}
		else if (purpose=="NHBO")
		{
			scenarioSetNHBO=scenarioSet;
		}
		System.out.println("readInScenarioSet "+purpose+" is done");
	}
	public void DCMCModelRun(String purpose)
	{
		int purposeTag=0;
		if (purpose=="HBW")
		{
			scenarioSet=scenarioSetHBW;
			purposeTag=1;
		}
		else if (purpose=="HBShop")
		{
			scenarioSet=scenarioSetHBShop;
			purposeTag=2;
		}
		else if (purpose=="HBSRO1")
		{
			scenarioSet=scenarioSetHBSRO1;
			purposeTag=3;
		}
		else if (purpose=="HBSRO2")
		{
			scenarioSet=scenarioSetHBSRO2;
			purposeTag=4;
		}
		else if (purpose=="NHBW")
		{
			scenarioSet=scenarioSetNHBW;
			purposeTag=5;
		}
		else if (purpose=="NHBO")
		{
			scenarioSet=scenarioSetNHBO;
			purposeTag=6;
		}
		int iTripID=0;
		int iOTAZID=0;
		int iDTAZID=0;
		int OTAZID=0;
		int DTAZID=0;
		int iMode=0;
		double g1MarketShare=0;
		double g2MarketShare=0;
		double g3MarketShare=0;
		double g4MarketShare=0;
		double sumMarketShare=0;
		//Destination choice model firstly
		for(iTripID=0;iTripID<scenarioSet.size();iTripID++)
		{
			double[] utilityDestination=new double[totalNumberOfAlternatives+1];
			double[][] intermediate=new double[totalNumberOfAlternatives+1][7];			
			double[] modeChoicePossibility=new double[7];
			double utilityExponentialSum=0;
			int altID=0;
			Trip iTrip=scenarioSet.get(iTripID);
			OTAZID=(int)iTrip.getValue(0);
			//calculate destination choice
			for(altID=1;altID<=totalNumberOfAlternatives;altID++)
			{					
				iDTAZID=(int)iTrip.getValue(numOfSDVs+altID);
				utilityDestination[altID]=calculateDCUtility(purposeTag,OTAZID,iDTAZID,iTrip);
				utilityExponentialSum=utilityExponentialSum+exp(utilityDestination[altID]);
			}
			for(altID=1;altID<=totalNumberOfAlternatives;altID++)
			{
				iDTAZID=(int)iTrip.getValue(numOfSDVs+altID);
				int n1=numberOfAlternativesInEachGroup[1];
				int n2=numberOfAlternativesInEachGroup[2];
				int n3=numberOfAlternativesInEachGroup[3];
				int n4=numberOfAlternativesInEachGroup[4];
				if(altID<=n1)
				{
					g1MarketShare=g1MarketShare+exp(utilityDestination[altID])/utilityExponentialSum;
				}
				else if(altID<=n1+n2)
				{
					g2MarketShare=g2MarketShare+exp(utilityDestination[altID])/utilityExponentialSum;
				}
				else if(altID<=n1+n2+n3)
				{
					g3MarketShare=g3MarketShare+exp(utilityDestination[altID])/utilityExponentialSum;
				}
				else if(altID<=n1+n2+n3+n4)
				{
					g4MarketShare=g4MarketShare+exp(utilityDestination[altID])/utilityExponentialSum;
				}
				switch (purposeTag)
				{
					case 1:
						ODMatrixHBW[OTAZID][iDTAZID][0]=ODMatrixHBW[OTAZID][iDTAZID][0]+exp(utilityDestination[altID])/utilityExponentialSum;
						break;
					case 2:
						ODMatrixHBShop[OTAZID][iDTAZID][0]=ODMatrixHBShop[OTAZID][iDTAZID][0]+exp(utilityDestination[altID])/utilityExponentialSum;
						break;
					case 3:
						ODMatrixHBSRO1[OTAZID][iDTAZID][0]=ODMatrixHBSRO1[OTAZID][iDTAZID][0]+exp(utilityDestination[altID])/utilityExponentialSum;
						break;
					case 4:
						ODMatrixHBSRO2[OTAZID][iDTAZID][0]=ODMatrixHBSRO2[OTAZID][iDTAZID][0]+exp(utilityDestination[altID])/utilityExponentialSum;
						break;
					case 5: 
						ODMatrixNHBW[OTAZID][iDTAZID][0]=ODMatrixNHBW[OTAZID][iDTAZID][0]+exp(utilityDestination[altID])/utilityExponentialSum;
						break;
					case 6:
						ODMatrixNHBO[OTAZID][iDTAZID][0]=ODMatrixNHBO[OTAZID][iDTAZID][0]+exp(utilityDestination[altID])/utilityExponentialSum;
						break;
					default: System.out.println("Purpose error!");
				}
				intermediate[altID][0]=exp(utilityDestination[altID])/utilityExponentialSum;
			}
			//calculate mode choice
			for(altID=1;altID<=totalNumberOfAlternatives;altID++)
			{
				iDTAZID=(int)iTrip.getValue(numOfSDVs+altID);
				modeChoicePossibility=modeChoice(iTrip, OTAZID, iDTAZID);
				intermediate[altID][1]=intermediate[altID][0]*modeChoicePossibility[1];
				intermediate[altID][2]=intermediate[altID][0]*modeChoicePossibility[2];
				intermediate[altID][3]=intermediate[altID][0]*modeChoicePossibility[3];
				intermediate[altID][4]=intermediate[altID][0]*modeChoicePossibility[4];
				intermediate[altID][5]=intermediate[altID][0]*modeChoicePossibility[5];
				intermediate[altID][6]=intermediate[altID][0]*modeChoicePossibility[6];
				switch (purposeTag)
				{
					case 1:
						ODMatrixHBW[OTAZID][iDTAZID][1]=ODMatrixHBW[OTAZID][iDTAZID][1]+intermediate[altID][1];
						ODMatrixHBW[OTAZID][iDTAZID][2]=ODMatrixHBW[OTAZID][iDTAZID][2]+intermediate[altID][2];
						ODMatrixHBW[OTAZID][iDTAZID][3]=ODMatrixHBW[OTAZID][iDTAZID][3]+intermediate[altID][3];
						ODMatrixHBW[OTAZID][iDTAZID][4]=ODMatrixHBW[OTAZID][iDTAZID][4]+intermediate[altID][4];
						ODMatrixHBW[OTAZID][iDTAZID][5]=ODMatrixHBW[OTAZID][iDTAZID][5]+intermediate[altID][5];
						ODMatrixHBW[OTAZID][iDTAZID][6]=ODMatrixHBW[OTAZID][iDTAZID][6]+intermediate[altID][6];
						break;
					case 2:
						ODMatrixHBShop[OTAZID][iDTAZID][1]=ODMatrixHBShop[OTAZID][iDTAZID][1]+intermediate[altID][1];
						ODMatrixHBShop[OTAZID][iDTAZID][2]=ODMatrixHBShop[OTAZID][iDTAZID][2]+intermediate[altID][2];
						ODMatrixHBShop[OTAZID][iDTAZID][3]=ODMatrixHBShop[OTAZID][iDTAZID][3]+intermediate[altID][3];
						ODMatrixHBShop[OTAZID][iDTAZID][4]=ODMatrixHBShop[OTAZID][iDTAZID][4]+intermediate[altID][4];
						ODMatrixHBShop[OTAZID][iDTAZID][5]=ODMatrixHBShop[OTAZID][iDTAZID][5]+intermediate[altID][5];
						ODMatrixHBShop[OTAZID][iDTAZID][6]=ODMatrixHBShop[OTAZID][iDTAZID][6]+intermediate[altID][6];
						break;
					case 3:
						ODMatrixHBSRO1[OTAZID][iDTAZID][1]=ODMatrixHBSRO1[OTAZID][iDTAZID][1]+intermediate[altID][1];
						ODMatrixHBSRO1[OTAZID][iDTAZID][2]=ODMatrixHBSRO1[OTAZID][iDTAZID][2]+intermediate[altID][2];
						ODMatrixHBSRO1[OTAZID][iDTAZID][3]=ODMatrixHBSRO1[OTAZID][iDTAZID][3]+intermediate[altID][3];
						ODMatrixHBSRO1[OTAZID][iDTAZID][4]=ODMatrixHBSRO1[OTAZID][iDTAZID][4]+intermediate[altID][4];
						ODMatrixHBSRO1[OTAZID][iDTAZID][5]=ODMatrixHBSRO1[OTAZID][iDTAZID][5]+intermediate[altID][5];
						ODMatrixHBSRO1[OTAZID][iDTAZID][6]=ODMatrixHBSRO1[OTAZID][iDTAZID][6]+intermediate[altID][6];
						break;
					case 4:
						ODMatrixHBSRO2[OTAZID][iDTAZID][1]=ODMatrixHBSRO2[OTAZID][iDTAZID][1]+intermediate[altID][1];
						ODMatrixHBSRO2[OTAZID][iDTAZID][2]=ODMatrixHBSRO2[OTAZID][iDTAZID][2]+intermediate[altID][2];
						ODMatrixHBSRO2[OTAZID][iDTAZID][3]=ODMatrixHBSRO2[OTAZID][iDTAZID][3]+intermediate[altID][3];
						ODMatrixHBSRO2[OTAZID][iDTAZID][4]=ODMatrixHBSRO2[OTAZID][iDTAZID][4]+intermediate[altID][4];
						ODMatrixHBSRO2[OTAZID][iDTAZID][5]=ODMatrixHBSRO2[OTAZID][iDTAZID][5]+intermediate[altID][5];
						ODMatrixHBSRO2[OTAZID][iDTAZID][6]=ODMatrixHBSRO2[OTAZID][iDTAZID][6]+intermediate[altID][6];
						break;
					case 5: 
						ODMatrixNHBW[OTAZID][iDTAZID][1]=ODMatrixNHBW[OTAZID][iDTAZID][1]+intermediate[altID][1];
						ODMatrixNHBW[OTAZID][iDTAZID][2]=ODMatrixNHBW[OTAZID][iDTAZID][2]+intermediate[altID][2];
						ODMatrixNHBW[OTAZID][iDTAZID][3]=ODMatrixNHBW[OTAZID][iDTAZID][3]+intermediate[altID][3];
						ODMatrixNHBW[OTAZID][iDTAZID][4]=ODMatrixNHBW[OTAZID][iDTAZID][4]+intermediate[altID][4];
						ODMatrixNHBW[OTAZID][iDTAZID][5]=ODMatrixNHBW[OTAZID][iDTAZID][5]+intermediate[altID][5];
						ODMatrixNHBW[OTAZID][iDTAZID][6]=ODMatrixNHBW[OTAZID][iDTAZID][6]+intermediate[altID][6];
						break;
					case 6:
						ODMatrixNHBO[OTAZID][iDTAZID][1]=ODMatrixNHBO[OTAZID][iDTAZID][1]+intermediate[altID][1];
						ODMatrixNHBO[OTAZID][iDTAZID][2]=ODMatrixNHBO[OTAZID][iDTAZID][2]+intermediate[altID][2];
						ODMatrixNHBO[OTAZID][iDTAZID][3]=ODMatrixNHBO[OTAZID][iDTAZID][3]+intermediate[altID][3];
						ODMatrixNHBO[OTAZID][iDTAZID][4]=ODMatrixNHBO[OTAZID][iDTAZID][4]+intermediate[altID][4];
						ODMatrixNHBO[OTAZID][iDTAZID][5]=ODMatrixNHBO[OTAZID][iDTAZID][5]+intermediate[altID][5];
						ODMatrixNHBO[OTAZID][iDTAZID][6]=ODMatrixNHBO[OTAZID][iDTAZID][6]+intermediate[altID][6];
						break;
					default: System.out.println("Purpose error!");
				}

			}
			int i=1;
			i=2;
		}
		sumMarketShare=g1MarketShare+g2MarketShare+g3MarketShare+g4MarketShare;
		g1MarketShare=g1MarketShare/sumMarketShare;
		g2MarketShare=g2MarketShare/sumMarketShare;
		g3MarketShare=g3MarketShare/sumMarketShare;
		g4MarketShare=g4MarketShare/sumMarketShare;
		scenarioSet.clear();
		System.out.println(purpose+" Modelrun is done");
		System.out.println(purpose+" market share is: "+"g1: "+g1MarketShare+" g2: "+g2MarketShare
				+" g3 "+g3MarketShare+" g4 "+g4MarketShare+"\n");
	}
	private double calculateDCUtility(int purpose,int OTAZID, int iDTAZID, Trip iTrip)
	{
		double utility=0;
		switch (purpose)
		{
		case 1:
		{
			if (OTAZID==iDTAZID)//this alternative belongs to Group 1
			{
				double distance=ODDistanceMatrix[OTAZID][iDTAZID];
				if (distance<1.5)
					utility=-0.01878*distance;
				else if (distance>1.5 && distance<3)
					utility=-0.01878*1.5-0.5348*(distance-1.5);
				else if (distance>3 && distance<5)
					utility=-0.01878*1.5-0.5348*1.5-0.3763*(distance-3);
				else if (distance>5)
					utility=-0.01878*1.5-0.5348*1.5-0.3763*2-0.1444*(distance-5);
				
				utility=utility-0/*constant*/
				-0.4562*iTrip.getValue(3)/*HINCM*/+0.01016*iTrip.getValue(10)/*AGE*/
				+0.6844*iTrip.getValue(12)/*number of jobs*/;	
				
				utility=utility+0.0001784*TAZVs[iDTAZID][3]/*DTEMP*/
				+0.4377*TAZVs[iDTAZID][16]/*DDISS_D*/+0.7908*TAZVs[iDTAZID][14]/*DLD_EPY*/
				+0.1203*TAZVs[iDTAZID][11]/*EP_RATIO*/-0.7006*TAZVs[iDTAZID][19]+0.5891*TAZVs[OTAZID][22]/*log size TEMP*/;                                                                        
			}
			else if(ODDistanceMatrix[OTAZID][iDTAZID]<cutPoints[1][1])//this alternative belongs to Group 2
			{
				double distance=ODDistanceMatrix[OTAZID][iDTAZID];
				if (distance<1.5)
					utility=-0.01878*distance;
				else if (distance>1.5 && distance<3)
					utility=-0.01878*1.5-0.5348*(distance-1.5);
				else if (distance>3 && distance<5)
					utility=-0.01878*1.5-0.5348*1.5-0.3763*(distance-3);
				else if (distance>5)
					utility=-0.01878*1.5-0.5348*1.5-0.3763*2-0.1444*(distance-5);
				
				utility=utility-0.8657/*constant*/
				-0.09466*iTrip.getValue(9)/*female or not*/+0.0006539*iTrip.getValue(15)/*activity duration*/;
				
				utility=utility+0.0001784*TAZVs[iDTAZID][3]/*TEMP*/
				+0.3170*TAZVs[iDTAZID][16]/*Dissi_D*/+0.7908*TAZVs[iDTAZID][14]/*LD_EPY*/
				+0.5223*TAZVs[iDTAZID][11]/*EP_RATIO*/+0.5891*TAZVs[OTAZID][23]/*log size TEMP*/;
			}
			else if (ODDistanceMatrix[OTAZID][iDTAZID]>cutPoints[1][1] && ODDistanceMatrix[OTAZID][iDTAZID]<cutPoints[1][2])//this alternative belongs to Group 3
			{
				double distance=ODDistanceMatrix[OTAZID][iDTAZID];
				if (distance<1.5)
					utility=-0.01878*distance;
				else if (distance>1.5 && distance<3)
					utility=-0.01878*1.5-0.5348*(distance-1.5);
				else if (distance>3 && distance<5)
					utility=-0.01878*1.5-0.5348*1.5-0.3763*(distance-3);
				else if (distance>5)
					utility=-0.01878*1.5-0.5348*1.5-0.3763*2-0.1444*(distance-5);
				
				utility=utility-1.1022/*constant*/
				-0.09466*iTrip.getValue(9)/*female or not*/+0.002128*iTrip.getValue(15)/*activity duration*/;
				
				utility=utility+0.0002071*TAZVs[iDTAZID][3]/*TEMP*/
				/*+0.3170*TAZVs[iDTAZID][16]/*Dissi_D*//*+0.7908*TAZVs[iDTAZID][14]/*LD_EPY*/
				+0.5223*TAZVs[iDTAZID][11]/*EP_RATIO*/+0.5891*TAZVs[OTAZID][24]/*log size TEMP*/;
			}
			else if (ODDistanceMatrix[OTAZID][iDTAZID]>cutPoints[1][2])//this alternative belongs to Group 4
			{
				double distance=ODDistanceMatrix[OTAZID][iDTAZID];
				if (distance<1.5)
					utility=-0.01878*distance;
				else if (distance>1.5 && distance<3)
					utility=-0.01878*1.5-0.5348*(distance-1.5);
				else if (distance>3 && distance<5)
					utility=-0.01878*1.5-0.5348*1.5-0.3763*(distance-3);
				else if (distance>5)
					utility=-0.01878*1.5-0.5348*1.5-0.3763*2-0.1444*(distance-5);
				
				utility=utility-1.6884/*constant*/
				-0.5725*iTrip.getValue(9)/*female or not*/+0.004561*iTrip.getValue(15)/*activity duration*/;
				
				utility=utility+0.0003207*TAZVs[iDTAZID][3]/*TEMP*/
				/*+0.3170*TAZVs[iDTAZID][16]/*Dissi_D*//*+0.7908*TAZVs[iDTAZID][14]/*LD_EPY*/
				+0.5223*TAZVs[iDTAZID][11]/*EP_RATIO*/ +0.5891*TAZVs[OTAZID][25]/*log size TEMP*/;
			}
			break;
		}
		case 2:
		{
			if (OTAZID==iDTAZID)//this alternative belongs to Group 1
			{
				double distance=ODDistanceMatrix[OTAZID][iDTAZID];
				utility=utility-0/*constant*/
				-1.5960*distance/*distance*/-0.03018*iTrip.getValue(15)/*activity duration*/
				+0.4898*iTrip.getValue(9)/*female or not*/;	
				
				utility=utility+0.0001466*TAZVs[iDTAZID][1]/*POP*/
				+0.0000957*TAZVs[iDTAZID][5]/*REMP*/+1.3*TAZVs[iDTAZID][14]/*LD_EPY*/
				+2.54*TAZVs[iDTAZID][20]/*TSTPROP*/+0.5422*TAZVs[OTAZID][26]/*LOGSIZE REMP*/;                                                                      
			}
			else if(ODDistanceMatrix[OTAZID][iDTAZID]<cutPoints[2][1])//this alternative belongs to Group 2
			{
				double distance=ODDistanceMatrix[OTAZID][iDTAZID];
				utility=utility-1.356/*constant*/
				-0.3093*distance/*distance*/-0.005537*iTrip.getValue(15)/*activity duration*/
				+0.3446*iTrip.getValue(9)/*female or not*/;	
				
				utility=utility+0.0001466*TAZVs[iDTAZID][1]/*POP*/
				+0.001385*TAZVs[iDTAZID][5]/*REMP*/+0.7986*TAZVs[iDTAZID][14]/*LD_EPY*/
				+2.54*TAZVs[iDTAZID][20]/*TSTPROP*/+0.5422*TAZVs[OTAZID][27]/*LOGSIZE REMP*/; 
			}
			else if (ODDistanceMatrix[OTAZID][iDTAZID]>cutPoints[2][1] && ODDistanceMatrix[OTAZID][iDTAZID]<cutPoints[2][2])//this alternative belongs to Group 3
			{
				double distance=ODDistanceMatrix[OTAZID][iDTAZID];
				utility=utility+1.717/*constant*/
				-1.018*distance/*distance*/-0.001716*iTrip.getValue(15)/*activity duration*/
				+0.3315*iTrip.getValue(9)/*female or not*/;	
				
				utility=utility+0.0001466*TAZVs[iDTAZID][1]/*POP*/
				+0.001283*TAZVs[iDTAZID][5]/*REMP*//*+0.7986*TAZVs[iDTAZID][14]/*LD_EPY*/
				+2.54*TAZVs[iDTAZID][20]/*TSTPROP*/+0.5422*TAZVs[OTAZID][28]/*LOGSIZE REMP*/; 
			}
			else if (ODDistanceMatrix[OTAZID][iDTAZID]>cutPoints[2][2])//this alternative belongs to Group 4
			{
				double distance=ODDistanceMatrix[OTAZID][iDTAZID];
				utility=utility-0.2199/*constant*/
				-0.3087*distance/*distance*/;	
				
				utility=utility+0.0001466*TAZVs[iDTAZID][1]/*POP*/
				+0.001823*TAZVs[iDTAZID][5]/*REMP*/+0.5422*TAZVs[OTAZID][29]/*LOGSIZE REMP*/; 
			}
			break;
		}
		case 3:
		{
			if (OTAZID==iDTAZID)//this alternative belongs to Group 1
			{
				double distance=ODDistanceMatrix[OTAZID][iDTAZID];
				utility=utility-0/*constant*/
				-0.8141*distance/*distance*/+0.1987*iTrip.getValue(9)/*female or not*/
				-0.3745*iTrip.getValue(11)/*license or not*/;	
				
				utility=utility+0.0003636*TAZVs[iDTAZID][2]/*HH*/
				+0.0001878*TAZVs[iDTAZID][7]/*OEMP*/+0.00000009*TAZVs[iDTAZID][13]/*Retail land use*/
				+0.1629*TAZVs[iDTAZID][20]/*NER_FWYD*/+0.5055*TAZVs[OTAZID][30]/*LOGSIZE TEMP*/;                                                                      
			}
			else if(ODDistanceMatrix[OTAZID][iDTAZID]<cutPoints[3][1])//this alternative belongs to Group 2
			{
				double distance=ODDistanceMatrix[OTAZID][iDTAZID];
				utility=utility+0.5298/*constant*/
				-0.8325*distance/*distance*/+0.001423*iTrip.getValue(15)/*activity duration*/;	
				
				utility=utility+0.0003636*TAZVs[iDTAZID][2]/*HH*/
				+0.0001878*TAZVs[iDTAZID][7]/*OEMP*/+0.0000001215*TAZVs[iDTAZID][13]/*Retail land use*/
				+0.09917*TAZVs[iDTAZID][20]/*NER_FWYD*/+0.5055*TAZVs[OTAZID][31]/*LOGSIZE TEMP*/; 
			}
			else if (ODDistanceMatrix[OTAZID][iDTAZID]>cutPoints[3][1] && ODDistanceMatrix[OTAZID][iDTAZID]<cutPoints[3][2])//this alternative belongs to Group 3
			{
				double distance=ODDistanceMatrix[OTAZID][iDTAZID];
				utility=utility+0.4798/*constant*/
				-0.5358*distance/*distance*/+0.00495*iTrip.getValue(15)/*activity duration*/;	
				
				utility=utility+0.0001854*TAZVs[iDTAZID][2]/*HH*/
				/*+0.0001878*TAZVs[iDTAZID][7]/*OEMP*/+0.0000001542*TAZVs[iDTAZID][13]/*Retail land use*/
				+0.01309*TAZVs[iDTAZID][20]/*NER_FWYD*/+0.5055*TAZVs[OTAZID][32]/*LOGSIZE TEMP*/;
			}
			else if (ODDistanceMatrix[OTAZID][iDTAZID]>cutPoints[3][2])//this alternative belongs to Group 4
			{
				double distance=ODDistanceMatrix[OTAZID][iDTAZID];
				utility=utility-0.8918/*constant*/
				-0.2016*distance/*distance*/+0.007225*iTrip.getValue(15)/*activity duration*/;	
				
				utility=utility+0.0001854*TAZVs[iDTAZID][2]/*HH*/
				/*+0.0001878*TAZVs[iDTAZID][7]/*OEMP*/+0.0000002062*TAZVs[iDTAZID][13]/*Retail land use*/
				+0.01309*TAZVs[iDTAZID][20]/*NER_FWYD*/+0.5055*TAZVs[OTAZID][33]/*LOGSIZE TEMP*/;
			}
			break;
		}
		case 4:
		{
			if (OTAZID==iDTAZID)//this alternative belongs to Group 1
			{
				double distance=ODDistanceMatrix[OTAZID][iDTAZID];
				utility=utility-0/*constant*/
				-0.8141*distance/*distance*/+0.1987*iTrip.getValue(9)/*female or not*/
				-0.3745*iTrip.getValue(11)/*license or not*/;	
				
				utility=utility+0.0003636*TAZVs[iDTAZID][2]/*HH*/
				+0.0001878*TAZVs[iDTAZID][7]/*OEMP*/+0.00000009*TAZVs[iDTAZID][13]/*Retail land use*/
				+0.1629*TAZVs[iDTAZID][20]/*NER_FWYD*/+0.5055*TAZVs[OTAZID][30]/*LOGSIZE TEMP*/;                                                                      
			}
			else if(ODDistanceMatrix[OTAZID][iDTAZID]<cutPoints[3][1])//this alternative belongs to Group 2
			{
				double distance=ODDistanceMatrix[OTAZID][iDTAZID];
				utility=utility+0.5298/*constant*/
				-0.8325*distance/*distance*/+0.001423*iTrip.getValue(15)/*activity duration*/;	
				
				utility=utility+0.0003636*TAZVs[iDTAZID][2]/*HH*/
				+0.0001878*TAZVs[iDTAZID][7]/*OEMP*/+0.0000001215*TAZVs[iDTAZID][13]/*Retail land use*/
				+0.09917*TAZVs[iDTAZID][20]/*NER_FWYD*/+0.5055*TAZVs[OTAZID][31]/*LOGSIZE TEMP*/; 
			}
			else if (ODDistanceMatrix[OTAZID][iDTAZID]>cutPoints[3][1] && ODDistanceMatrix[OTAZID][iDTAZID]<cutPoints[3][2])//this alternative belongs to Group 3
			{
				double distance=ODDistanceMatrix[OTAZID][iDTAZID];
				utility=utility+0.4798/*constant*/
				-0.5358*distance/*distance*/+0.00495*iTrip.getValue(15)/*activity duration*/;	
				
				utility=utility+0.0001854*TAZVs[iDTAZID][2]/*HH*/
				/*+0.0001878*TAZVs[iDTAZID][7]/*OEMP*/+0.0000001542*TAZVs[iDTAZID][13]/*Retail land use*/
				+0.01309*TAZVs[iDTAZID][20]/*NER_FWYD*/+0.5055*TAZVs[OTAZID][32]/*LOGSIZE TEMP*/;
			}
			else if (ODDistanceMatrix[OTAZID][iDTAZID]>cutPoints[3][2])//this alternative belongs to Group 4
			{
				double distance=ODDistanceMatrix[OTAZID][iDTAZID];
				utility=utility-0.8918/*constant*/
				-0.2016*distance/*distance*/+0.007225*iTrip.getValue(15)/*activity duration*/;	
				
				utility=utility+0.0001854*TAZVs[iDTAZID][2]/*HH*/
				/*+0.0001878*TAZVs[iDTAZID][7]/*OEMP*/+0.0000002062*TAZVs[iDTAZID][13]/*Retail land use*/
				+0.01309*TAZVs[iDTAZID][20]/*NER_FWYD*/+0.5055*TAZVs[OTAZID][33]/*LOGSIZE TEMP*/;
			}
			break;
		}
		case 5: 
		{
			if (OTAZID==iDTAZID)//this alternative belongs to Group 1
			{
				double distance=ODDistanceMatrix[OTAZID][iDTAZID];
				utility=utility-0/*constant*/
				-0.4612*distance/*distance*/-0.002773*iTrip.getValue(15)/*activity duration*/
				+0.01717*iTrip.getValue(10)/*AGE*/;	
				
				utility=utility+0.0001094*TAZVs[iDTAZID][3]/*TEMP*/
				+0.5290*TAZVs[iDTAZID][15]/*non work entropy*/+0.3180*TAZVs[iDTAZID][16]/*Dissi_D*/
				+0.5296*TAZVs[OTAZID][34]/*LOGSIZE TEMP*/;                                                                      
			}
			else if(ODDistanceMatrix[OTAZID][iDTAZID]<cutPoints[4][1])//this alternative belongs to Group 2
			{
				double distance=ODDistanceMatrix[OTAZID][iDTAZID];
				utility=utility+0.9933/*constant*/
				-0.6147*distance/*distance*/-0.5587*iTrip.getValue(9)/*Female or not*/;	
				
				utility=utility+0.0002627*TAZVs[iDTAZID][3]/*TEMP*/
				+0.5290*TAZVs[iDTAZID][15]/*non work entropy*/+0.3180*TAZVs[iDTAZID][16]/*Dissi_D*/
				+0.5296*TAZVs[OTAZID][35]/*LOGSIZE TEMP*/;  
			}
			else if (ODDistanceMatrix[OTAZID][iDTAZID]>cutPoints[4][1] && ODDistanceMatrix[OTAZID][iDTAZID]<cutPoints[4][2])//this alternative belongs to Group 3
			{
				double distance=ODDistanceMatrix[OTAZID][iDTAZID];
				utility=utility+1.001/*constant*/
				-0.4151*distance/*distance*/-0.5587*iTrip.getValue(9)/*Female or not*/;	
				
				utility=utility+0.0003014*TAZVs[iDTAZID][3]/*TEMP*/
				/*+0.5290*TAZVs[iDTAZID][15]/*non work entropy*//*+0.3180*TAZVs[iDTAZID][16]/*Dissi_D*/
				+0.5296*TAZVs[OTAZID][36]/*LOGSIZE TEMP*/;  
			}
			else if (ODDistanceMatrix[OTAZID][iDTAZID]>cutPoints[4][2])//this alternative belongs to Group 4
			{
				double distance=ODDistanceMatrix[OTAZID][iDTAZID];
				utility=utility+0.4771/*constant*/
				-0.1832*distance/*distance*/-1.1642*iTrip.getValue(9)/*Female or not*/;	
				
				utility=utility+0.0004221*TAZVs[iDTAZID][3]/*TEMP*/
				/*+0.5290*TAZVs[iDTAZID][15]/*non work entropy*//*+0.3180*TAZVs[iDTAZID][16]/*Dissi_D*/
				+0.5296*TAZVs[OTAZID][37]/*LOGSIZE TEMP*/;  
			}
			break;
		}
		case 6:
		{
			if (OTAZID==iDTAZID)//this alternative belongs to Group 1
			{
				double distance=ODDistanceMatrix[OTAZID][iDTAZID];
				utility=utility-0/*constant*/
				-0.1257*distance/*distance*/;	
				
				utility=utility+0.0000009884*TAZVs[iDTAZID][5]/*REMP*/
				+0.7112*TAZVs[iDTAZID][10]/*Balance*//*+0.2078*TAZVs[iDTAZID][15]/*non work entropy*/
				+0.7886*TAZVs[iDTAZID][21]/*TSTPROP*/+0.001432*TAZVs[iDTAZID][18]/*RD_DED*/
				+0.3674*TAZVs[OTAZID][38]/*LOGSIZE REMP*/;                                                                      
			}
			else if(ODDistanceMatrix[OTAZID][iDTAZID]<cutPoints[5][1])//this alternative belongs to Group 2
			{
				double distance=ODDistanceMatrix[OTAZID][iDTAZID];
				utility=utility+0.2659/*constant*/
				-0.7329*distance/*distance*/+0.5196*iTrip.getValue(11)/*License or not*/
				+0.0009235*iTrip.getValue(15)/*Activity duration*/;	
				
				utility=utility+0.0004325*TAZVs[iDTAZID][5]/*REMP*/
				+0.7112*TAZVs[iDTAZID][10]/*Balance*//*+0.2078*TAZVs[iDTAZID][15]/*non work entropy*/
				+0.7886*TAZVs[iDTAZID][21]/*TSTPROP*/+0.001432*TAZVs[iDTAZID][18]/*RD_DED*/
				+0.3674*TAZVs[OTAZID][39]/*LOGSIZE REMP*/;
			}
			else if (ODDistanceMatrix[OTAZID][iDTAZID]>cutPoints[5][1] && ODDistanceMatrix[OTAZID][iDTAZID]<cutPoints[5][2])//this alternative belongs to Group 3
			{
				double distance=ODDistanceMatrix[OTAZID][iDTAZID];
				utility=utility+0.8267/*constant*/
				-0.5164*distance/*distance*/+0.7052*iTrip.getValue(11)/*License or not*/
				+0.001468*iTrip.getValue(15)/*Activity duration*/;	
				
				utility=utility+0.0005294*TAZVs[iDTAZID][5]/*REMP*/
				/*+0.7112*TAZVs[iDTAZID][10]/*Balance*//*+0.2078*TAZVs[iDTAZID][15]/*non work entropy*/
				+0.001432*TAZVs[iDTAZID][18]/*RD_DED*/+0.3674*TAZVs[OTAZID][40]/*LOGSIZE REMP*/; 
			}
			else if (ODDistanceMatrix[OTAZID][iDTAZID]>cutPoints[5][2])//this alternative belongs to Group 4
			{
				double distance=ODDistanceMatrix[OTAZID][iDTAZID];
				utility=utility-0.7020/*constant*/
				-0.1770*distance/*distance*/+1.2814*iTrip.getValue(11)/*License or not*/
				+0.003494*iTrip.getValue(15)/*Activity duration*/;	
				
				utility=utility+0.0007737*TAZVs[iDTAZID][5]/*REMP*/
				/*+0.7112*TAZVs[iDTAZID][10]/*Balance*//*+0.2078*TAZVs[iDTAZID][15]/*non work entropy*/
				+0.001432*TAZVs[iDTAZID][18]/*RD_DED*/+0.3674*TAZVs[OTAZID][41]/*LOGSIZE REMP*/; 
			}
			break;
		}
		}
		return utility;
	}
	private double[] modeChoice(Trip iTrip,int OTAZID, int DTAZID)
	{
		double IV1=0,IV2=0;
		double branch1Possibility=0,branch2Possibility=0;
		double[] utilityMode=new double[7];
		double[] modeChoice=new double[7];
		if(OTAZID==DTAZID)
		{
		/*utility of walk*/
		utilityMode[1]=5.3780/*constant*/-0.2647*iTrip.getValue(16)/30*60*10/*autoTimeMatrix[OTAZID][DTAZID]*//*walk trip duration*/
		-2.8946*iTrip.getValue(7)/*VEHPPER*/+0.004731*iTrip.getValue(10)/*AGE*/
		+0.00009259*TAZVs[OTAZID][8]/*OPOP_DE*/+0.00001589*TAZVs[OTAZID][9]/*OEMP_DE1*/
		+0.7990*TAZVs[OTAZID][16]/*ODISS_D*/;
		/*utility of bike*/
		utilityMode[2]=0.1162/*constant*/-0.2647*iTrip.getValue(16)/30*60*3/*bike trip duration*/
		-2.8946*iTrip.getValue(7)/*VEHPPER*/+0.01635*iTrip.getValue(8)/*PERINCM*/;
		/*utility of auto*/
		utilityMode[3]=0/*constant*/-0.2647*iTrip.getValue(16)/30*60*1/*auto trip duration*/
		-0.2174*iTrip.getValue(14)/*NTRIPS*/+0.1376*TAZVs[OTAZID][17]/*ORAMP*/
		-0.00005048*TAZVs[OTAZID][8]/*OPOP_DE*/-0.00002922*TAZVs[OTAZID][9]/*OEMP_DE1*/
		-0.2963*TAZVs[OTAZID][16]/*ODISS_D*/;
		/*utility of bus*/
		utilityMode[4]=-2.2941/*constant*/-0.09463*iTrip.getValue(16)/30*60*3/*bus trip duration*/
		-10.3029*iTrip.getValue(7)/*VEHPPER*/-2.0869*TAZVs[OTAZID][12]/*OEMPEPY*/;
		/*utility of rail*/
		utilityMode[5]=-207.4069/*constant*/-0.09463*iTrip.getValue(16)/30*60*2/*bus trip duration*/
		-0.897*iTrip.getValue(7)/*VEHPPER*/+204.5601*TAZVs[OTAZID][20]/*OTSTPROP*/;
		/*utility of taxi*/
		utilityMode[6]=-446.8317/*constant*/-0.4355*iTrip.getValue(16)/30*60*3/*taxi trip duration*/
		-4.6998*iTrip.getValue(7)/*VEHPPER*/+4.5781*iTrip.getValue(10)/*AGE*/+0.003106*iTrip.getValue(15);
		IV1=log(exp(utilityMode[1])+exp(utilityMode[2]));
		IV2=log(exp(utilityMode[3])+exp(utilityMode[4])+exp(utilityMode[5])+exp(utilityMode[6]));
		
		branch1Possibility=exp(0.9401*IV1)/(exp(0.9401*IV1)+exp(1.0299*IV2));
		branch2Possibility=1-branch1Possibility;
		modeChoice[1]=branch1Possibility*exp(utilityMode[1])/exp(IV1);
		modeChoice[2]=branch1Possibility*exp(utilityMode[2])/exp(IV1);
		modeChoice[3]=branch2Possibility*exp(utilityMode[3])/exp(IV2);
		modeChoice[4]=branch2Possibility*exp(utilityMode[4])/exp(IV2);
		modeChoice[5]=branch2Possibility*exp(utilityMode[5])/exp(IV2);
		modeChoice[6]=branch2Possibility*exp(utilityMode[6])/exp(IV2);
		return modeChoice;
		}
		else
		{
		/*utility of walk*/
		utilityMode[1]=6.7784/*constant*/-0.1798*autoTimeMatrix[OTAZID][DTAZID]*10/*walk time*/
		-2.543*iTrip.getValue(7)/*VEHPPER*/-0.1541*iTrip.getValue(9)/*Female or not*/-0.01002*iTrip.getValue(10)/*AGE*/
		+0.0001132*TAZVs[DTAZID][8]/*DPOP_DE*/+0.00001713*TAZVs[DTAZID][9]/*DEMP_DE*/-1.1526*TAZVs[OTAZID][12]/*OEMP_EPY*/;
		/*utility of bike*/
		utilityMode[2]=2.0713/*constant*/-0.1798*autoTimeMatrix[OTAZID][DTAZID]*3/*bike time*/
		-4.2852*iTrip.getValue(7)/*VEHPPER*/-2.4458*iTrip.getValue(9)/*Female or not*/
		+0.0001068*TAZVs[DTAZID][8]/*DPOP_DE*/;
		/*utility of auto*/
		utilityMode[3]=0/*constant*/-0.1372*autoTimeMatrix[OTAZID][DTAZID]*1/*auto time*/-0.01652*iTrip.getValue(10)/*AGE*/
		+0.1641*iTrip.getValue(14)/*NTRIPS*/+0.00002408*TAZVs[DTAZID][8]/*DPOP_DE*/+0.6932*TAZVs[OTAZID][17]/*ORAMP*/
		+0.3771*TAZVs[OTAZID][12]/*OEMP_EPY*/;
		/*utility of bus*/
		utilityMode[4]=0.2186/*constant*/-0.05401*autoTimeMatrix[OTAZID][DTAZID]*3/*bus time*/
		-9.1914*iTrip.getValue(7)/*VEHPPER*/+3.2398*TAZVs[DTAZID][20]/*DTSTPROP*/;
		/*utility of rail*/
		utilityMode[5]=-5.4919/*constant*/-0.05401*autoTimeMatrix[OTAZID][DTAZID]*2/*rail time*/
		-5.1478*iTrip.getValue(7)/*VEHPPER*/+4.7893*TAZVs[DTAZID][20]/*DTSTPROP*/;
		/*utility of taxi*/
		utilityMode[6]=-0.7528*autoTimeMatrix[OTAZID][DTAZID]*3/*taxi time*/
		-6.6072*iTrip.getValue(7)/*VEHPPER*/-0.5663*iTrip.getValue(9)/*Female or not*/
		-0.05472*iTrip.getValue(10)/*AGE*/;
		IV1=log(exp(utilityMode[1])+exp(utilityMode[2]));
		IV2=log(exp(utilityMode[3])+exp(utilityMode[4])+exp(utilityMode[5])+exp(utilityMode[6]));
		branch1Possibility=exp(0.9102*IV1)/(exp(0.9102*IV1)+exp(0.8844*IV2));
		branch2Possibility=1-branch1Possibility;
		modeChoice[1]=branch1Possibility*exp(utilityMode[1])/exp(IV1);
		modeChoice[2]=branch1Possibility*exp(utilityMode[2])/exp(IV1);
		modeChoice[3]=branch2Possibility*exp(utilityMode[3])/exp(IV2);
		modeChoice[4]=branch2Possibility*exp(utilityMode[4])/exp(IV2);
		modeChoice[5]=branch2Possibility*exp(utilityMode[5])/exp(IV2);
		modeChoice[6]=branch2Possibility*exp(utilityMode[6])/exp(IV2);
		}
		return modeChoice;
	}
	public void finalResult() throws IOException
	{
		int OTAZID=0,DTAZID=0,ModeID=0;
		double[] interModeShare=new double[7];
		double[] intraModeShare=new double[7];
		double[] overallModeShare=new double[7];
		for(OTAZID=1;OTAZID<403;OTAZID++)
		{
			for(DTAZID=1;DTAZID<403;DTAZID++)
			{
				ODMatrix[OTAZID][DTAZID]=ODMatrix[OTAZID][DTAZID]+ODMatrixHBW[OTAZID][DTAZID][0];
				ODMatrix[OTAZID][DTAZID]=ODMatrix[OTAZID][DTAZID]+ODMatrixHBShop[OTAZID][DTAZID][0];
				ODMatrix[OTAZID][DTAZID]=ODMatrix[OTAZID][DTAZID]+ODMatrixHBSRO1[OTAZID][DTAZID][0];
				ODMatrix[OTAZID][DTAZID]=ODMatrix[OTAZID][DTAZID]+ODMatrixHBSRO2[OTAZID][DTAZID][0];
				ODMatrix[OTAZID][DTAZID]=ODMatrix[OTAZID][DTAZID]+ODMatrixNHBW[OTAZID][DTAZID][0];
				ODMatrix[OTAZID][DTAZID]=ODMatrix[OTAZID][DTAZID]+ODMatrixNHBO[OTAZID][DTAZID][0];
				
				ODMatrixWalk[OTAZID][DTAZID]=ODMatrixWalk[OTAZID][DTAZID]+ODMatrixHBW[OTAZID][DTAZID][1];
				ODMatrixWalk[OTAZID][DTAZID]=ODMatrixWalk[OTAZID][DTAZID]+ODMatrixHBShop[OTAZID][DTAZID][1];
				ODMatrixWalk[OTAZID][DTAZID]=ODMatrixWalk[OTAZID][DTAZID]+ODMatrixHBSRO1[OTAZID][DTAZID][1];
				ODMatrixWalk[OTAZID][DTAZID]=ODMatrixWalk[OTAZID][DTAZID]+ODMatrixHBSRO2[OTAZID][DTAZID][1];
				ODMatrixWalk[OTAZID][DTAZID]=ODMatrixWalk[OTAZID][DTAZID]+ODMatrixNHBW[OTAZID][DTAZID][1];
				ODMatrixWalk[OTAZID][DTAZID]=ODMatrixWalk[OTAZID][DTAZID]+ODMatrixNHBO[OTAZID][DTAZID][1];
				
				ODMatrixBike[OTAZID][DTAZID]=ODMatrixBike[OTAZID][DTAZID]+ODMatrixHBW[OTAZID][DTAZID][2];
				ODMatrixBike[OTAZID][DTAZID]=ODMatrixBike[OTAZID][DTAZID]+ODMatrixHBShop[OTAZID][DTAZID][2];
				ODMatrixBike[OTAZID][DTAZID]=ODMatrixBike[OTAZID][DTAZID]+ODMatrixHBSRO1[OTAZID][DTAZID][2];
				ODMatrixBike[OTAZID][DTAZID]=ODMatrixBike[OTAZID][DTAZID]+ODMatrixHBSRO2[OTAZID][DTAZID][2];
				ODMatrixBike[OTAZID][DTAZID]=ODMatrixBike[OTAZID][DTAZID]+ODMatrixNHBW[OTAZID][DTAZID][2];
				ODMatrixBike[OTAZID][DTAZID]=ODMatrixBike[OTAZID][DTAZID]+ODMatrixNHBO[OTAZID][DTAZID][2];
				
				ODMatrixAuto[OTAZID][DTAZID]=ODMatrixAuto[OTAZID][DTAZID]+ODMatrixHBW[OTAZID][DTAZID][3];
				ODMatrixAuto[OTAZID][DTAZID]=ODMatrixAuto[OTAZID][DTAZID]+ODMatrixHBShop[OTAZID][DTAZID][3];
				ODMatrixAuto[OTAZID][DTAZID]=ODMatrixAuto[OTAZID][DTAZID]+ODMatrixHBSRO1[OTAZID][DTAZID][3];
				ODMatrixAuto[OTAZID][DTAZID]=ODMatrixAuto[OTAZID][DTAZID]+ODMatrixHBSRO2[OTAZID][DTAZID][3];
				ODMatrixAuto[OTAZID][DTAZID]=ODMatrixAuto[OTAZID][DTAZID]+ODMatrixNHBW[OTAZID][DTAZID][3];
				ODMatrixAuto[OTAZID][DTAZID]=ODMatrixAuto[OTAZID][DTAZID]+ODMatrixNHBO[OTAZID][DTAZID][3];
				
				ODMatrixBus[OTAZID][DTAZID]=ODMatrixBus[OTAZID][DTAZID]+ODMatrixHBW[OTAZID][DTAZID][4];
				ODMatrixBus[OTAZID][DTAZID]=ODMatrixBus[OTAZID][DTAZID]+ODMatrixHBShop[OTAZID][DTAZID][4];
				ODMatrixBus[OTAZID][DTAZID]=ODMatrixBus[OTAZID][DTAZID]+ODMatrixHBSRO1[OTAZID][DTAZID][4];
				ODMatrixBus[OTAZID][DTAZID]=ODMatrixBus[OTAZID][DTAZID]+ODMatrixHBSRO2[OTAZID][DTAZID][4];
				ODMatrixBus[OTAZID][DTAZID]=ODMatrixBus[OTAZID][DTAZID]+ODMatrixNHBW[OTAZID][DTAZID][4];
				ODMatrixBus[OTAZID][DTAZID]=ODMatrixBus[OTAZID][DTAZID]+ODMatrixNHBO[OTAZID][DTAZID][4];
				
				ODMatrixRail[OTAZID][DTAZID]=ODMatrixRail[OTAZID][DTAZID]+ODMatrixHBW[OTAZID][DTAZID][5];
				ODMatrixRail[OTAZID][DTAZID]=ODMatrixRail[OTAZID][DTAZID]+ODMatrixHBShop[OTAZID][DTAZID][5];
				ODMatrixRail[OTAZID][DTAZID]=ODMatrixRail[OTAZID][DTAZID]+ODMatrixHBSRO1[OTAZID][DTAZID][5];
				ODMatrixRail[OTAZID][DTAZID]=ODMatrixRail[OTAZID][DTAZID]+ODMatrixHBSRO2[OTAZID][DTAZID][5];
				ODMatrixRail[OTAZID][DTAZID]=ODMatrixRail[OTAZID][DTAZID]+ODMatrixNHBW[OTAZID][DTAZID][5];
				ODMatrixRail[OTAZID][DTAZID]=ODMatrixRail[OTAZID][DTAZID]+ODMatrixNHBO[OTAZID][DTAZID][5];
				
				ODMatrixTaxi[OTAZID][DTAZID]=ODMatrixTaxi[OTAZID][DTAZID]+ODMatrixHBW[OTAZID][DTAZID][6];
				ODMatrixTaxi[OTAZID][DTAZID]=ODMatrixTaxi[OTAZID][DTAZID]+ODMatrixHBShop[OTAZID][DTAZID][6];
				ODMatrixTaxi[OTAZID][DTAZID]=ODMatrixTaxi[OTAZID][DTAZID]+ODMatrixHBSRO1[OTAZID][DTAZID][6];
				ODMatrixTaxi[OTAZID][DTAZID]=ODMatrixTaxi[OTAZID][DTAZID]+ODMatrixHBSRO2[OTAZID][DTAZID][6];
				ODMatrixTaxi[OTAZID][DTAZID]=ODMatrixTaxi[OTAZID][DTAZID]+ODMatrixNHBW[OTAZID][DTAZID][6];
				ODMatrixTaxi[OTAZID][DTAZID]=ODMatrixTaxi[OTAZID][DTAZID]+ODMatrixNHBO[OTAZID][DTAZID][6];
			}
		}
		for(ModeID=0;ModeID<7;ModeID++)
		{
			FileWriter fileODMatrix;
			if(ModeID==0)
			{
				fileODMatrix=new FileWriter("ODMatrix.txt");
			}
			else if(ModeID==1)
			{
				fileODMatrix=new FileWriter("ODMatrixWalk.txt");
			}
			else if(ModeID==2)
			{
				fileODMatrix=new FileWriter("ODMatrixBike.txt");
			}
			else if(ModeID==3)
			{
				fileODMatrix=new FileWriter("ODMatrixAuto.txt");
			}
			else if(ModeID==4)
			{
				fileODMatrix=new FileWriter("ODMatrixBus.txt");
			}
			else if(ModeID==5)
			{
				fileODMatrix=new FileWriter("ODMatrixRail.txt");
			}
			else
			{
				fileODMatrix=new FileWriter("ODMatrixTaxi.txt");
			}
			BufferedWriter fileWriter=new BufferedWriter(fileODMatrix);	
			fileWriter.write("\t");
			for(DTAZID=1;DTAZID<403;DTAZID++)
			{
				fileWriter.write(TAZAndID[DTAZID]+"\t");
			}
			fileWriter.write("\n");
			for(OTAZID=1;OTAZID<403;OTAZID++)
			{
				fileWriter.write(TAZAndID[OTAZID]+"\t");
				for(DTAZID=1;DTAZID<403;DTAZID++)
				{
					if (ModeID==0)
						fileWriter.write(ODMatrix[OTAZID][DTAZID]+"\t");
					else if (ModeID==1)
						fileWriter.write(ODMatrixWalk[OTAZID][DTAZID]+"\t");
					else if (ModeID==2)
						fileWriter.write(ODMatrixBike[OTAZID][DTAZID]+"\t");
					else if (ModeID==3)
						fileWriter.write(ODMatrixAuto[OTAZID][DTAZID]+"\t");
					else if (ModeID==4)
						fileWriter.write(ODMatrixBus[OTAZID][DTAZID]+"\t");
					else if (ModeID==5)
						fileWriter.write(ODMatrixRail[OTAZID][DTAZID]+"\t");
					else if (ModeID==6)
						fileWriter.write(ODMatrixTaxi[OTAZID][DTAZID]+"\t");
				}
				fileWriter.write("\n");
				fileWriter.flush();
			}				
			fileWriter.close();
			fileODMatrix.close();
		}
		double VMT=0;
		for(OTAZID=1;OTAZID<403;OTAZID++)
		{
			for(DTAZID=1;DTAZID<403;DTAZID++)
			{
				if(OTAZID==DTAZID)
				{
					intraModeShare[1]=intraModeShare[1]+ODMatrixWalk[OTAZID][DTAZID];
					intraModeShare[2]=intraModeShare[2]+ODMatrixBike[OTAZID][DTAZID];
					intraModeShare[3]=intraModeShare[3]+ODMatrixAuto[OTAZID][DTAZID];
					intraModeShare[4]=intraModeShare[4]+ODMatrixBus[OTAZID][DTAZID];
					intraModeShare[5]=intraModeShare[5]+ODMatrixRail[OTAZID][DTAZID];
					intraModeShare[6]=intraModeShare[6]+ODMatrixTaxi[OTAZID][DTAZID];
				}
				else
				{
					interModeShare[1]=interModeShare[1]+ODMatrixWalk[OTAZID][DTAZID];
					interModeShare[2]=interModeShare[2]+ODMatrixBike[OTAZID][DTAZID];
					interModeShare[3]=interModeShare[3]+ODMatrixAuto[OTAZID][DTAZID];
					interModeShare[4]=interModeShare[4]+ODMatrixBus[OTAZID][DTAZID];
					interModeShare[5]=interModeShare[5]+ODMatrixRail[OTAZID][DTAZID];
					interModeShare[6]=interModeShare[6]+ODMatrixTaxi[OTAZID][DTAZID];					
				}
				overallModeShare[1]=overallModeShare[1]+ODMatrixWalk[OTAZID][DTAZID];
				overallModeShare[2]=overallModeShare[2]+ODMatrixBike[OTAZID][DTAZID];
				overallModeShare[3]=overallModeShare[3]+ODMatrixAuto[OTAZID][DTAZID];
				overallModeShare[4]=overallModeShare[4]+ODMatrixBus[OTAZID][DTAZID];
				overallModeShare[5]=overallModeShare[5]+ODMatrixRail[OTAZID][DTAZID];
				overallModeShare[6]=overallModeShare[6]+ODMatrixTaxi[OTAZID][DTAZID];
				VMT=VMT+ODMatrix[OTAZID][DTAZID]*ODDistanceMatrix[OTAZID][DTAZID];
			}
		}
		double temp1=intraModeShare[1]+intraModeShare[2]+intraModeShare[3]+
					 intraModeShare[4]+intraModeShare[5]+intraModeShare[6];
		double temp2=interModeShare[1]+interModeShare[2]+interModeShare[3]+
					 interModeShare[4]+interModeShare[5]+interModeShare[6];
		double temp3=overallModeShare[1]+overallModeShare[2]+overallModeShare[3]+
					 overallModeShare[4]+overallModeShare[5]+overallModeShare[6];
		for(int modeID=1;modeID<7;modeID++)
		{
			intraModeShare[modeID]=intraModeShare[modeID]/temp1;
			interModeShare[modeID]=interModeShare[modeID]/temp2;
			overallModeShare[modeID]=overallModeShare[modeID]/temp3;
		}
		
		System.out.println("Intrazonal mode share is "+intraModeShare[1]+" "+intraModeShare[2]+" "
				+intraModeShare[3]+" "+intraModeShare[4]+" "+intraModeShare[5]+" "+intraModeShare[6]);
		System.out.println("Interzonal mode share is "+interModeShare[1]+" "+interModeShare[2]+" "
				+interModeShare[3]+" "+interModeShare[4]+" "+interModeShare[5]+" "+interModeShare[6]);
		System.out.println("Overall mode share is "+overallModeShare[1]+" "+overallModeShare[2]+" "
				+overallModeShare[3]+" "+overallModeShare[4]+" "+overallModeShare[5]+" "+overallModeShare[6]);
		System.out.println("VMT is: "+ VMT);
		//System.out.println("ODMatrix output is done");
	}
}

