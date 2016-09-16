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

public class DataGeneration
{
	//test the class Seed by building a HBW seed set
	//the HBW table should be sorted by OTAZ
		private Vector<Seed> seedSetHBW=new Vector<Seed>();
		private Vector<Seed> seedSetHBShop=new Vector<Seed> ();
		private Vector<Seed> seedSetHBSRO=new Vector<Seed> ();
		private Vector<Seed> seedSetNHBW=new Vector<Seed> ();
		private Vector<Seed> seedSetNHBO=new Vector<Seed> ();
		private double[][] TAZVs=new double[403][42];
		private double[][] autoTimeMatrix=new double[403][403];
		private int[] TAZAndID=new int[403];//the inquiry list between TAZ and ID
		private int numOfSDVs;//the number of socio-demographic variables
		private int numOfVs;
		private int[][] Generation=new int[403][6];
		private double[][] cutPoints=new double[6][3];
		private double[][] ODDistanceMatrix=new double[403][403];
		private int[] numberOfAlternativesInEachGroup=new int[5];
		private int totalNumberOfAlternatives=0;
		private Vector<Integer> group1;
		private Vector<Integer> group2;
		private Vector<Integer> group3;
		private Vector<Integer> group4;
		
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
			System.out.println("initialzie cut points is done");
		}
 		public void readInTAZAndID() throws NumberFormatException, IOException
 		{
			String rLine;
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
		public void buildSeedsSet() throws NumberFormatException, IOException
		{	
			//read in the seed table and build the seed sets
			seedSetHBW=readInSeedsTable("./SeedHBW");
			seedSetHBShop=readInSeedsTable("./SeedHBShop");
			seedSetHBSRO=readInSeedsTable("./SeedHBSRO");
			seedSetNHBW=readInSeedsTable("./SeedNHBW");
			seedSetNHBO=readInSeedsTable("./SeedNHBO");		
			System.out.println("buildSeedsSet");
		}
		public void readInGeneration() throws NumberFormatException, IOException
		{
			StringTokenizer stLine;
			String rLine;
			String itemSep="\t";
			FileInputStream fileGeneration=new FileInputStream("./Generation.txt");
			BufferedReader readGeneration=new BufferedReader(new InputStreamReader(fileGeneration));			
			int i=1;
			while((rLine=readGeneration.readLine())!=null)
			{
				stLine=new StringTokenizer(rLine,itemSep);
				int j=0;
				while(stLine.hasMoreElements())
				{
					Generation[i][j]=Integer.parseInt(stLine.nextToken());
					j=j+1;
				}
				i=i+1;
			}
			System.out.println("readInGeneration is done");
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
		private Vector<Seed> readInSeedsTable(String fileName) throws NumberFormatException, IOException
		{
			StringTokenizer stLine;
			String rLine;
			String itemSep="\t";
			int TAZID=0;
			int TAZ=0;
			FileInputStream fileSeed=new FileInputStream(fileName+".txt");
			BufferedReader readSeed=new BufferedReader(new InputStreamReader(fileSeed));	
			Vector<Seed> seedSet=new Vector<Seed>();
			//initialize the seed set
			for(TAZID=1;TAZID<403;TAZID++)
			{
				Seed iSeed=new Seed();
				iSeed.setTAZID(TAZID);	
				iSeed.setNumOfSeeds(0);
				seedSet.add(iSeed);
			}
			while((rLine=readSeed.readLine())!=null)
			{
				stLine=new StringTokenizer(rLine,itemSep);
				TAZ=Integer.parseInt(stLine.nextToken());
				for(int j=1;j<403;j++)
				{
					if (TAZAndID[j]==TAZ)
						{
							TAZID=j;
							break;
						}
				}
				seedSet.get(TAZID-1).numOfSeedsPlus();//Be careful!!!
				Trip iTrip=new Trip();
				iTrip.initializeVariables(numOfSDVs);
				//build a trip record first
				for(int j=1;j<numOfSDVs;j++)
				{
					iTrip.addValueToVariables(j, Double.parseDouble(stLine.nextToken()));
				}
				//copy the trip record into seedSetHBW
				seedSet.get(TAZID-1).addTripToSeedsData(iTrip);
			}
			//Output for verification	
/*	
			FileWriter fileOut=new FileWriter(fileName+"Output.txt");
			BufferedWriter fileOutBuf=new BufferedWriter(fileOut);
			for(i=0;i<seedSet.size();i++)
			{
				int numOfSeeds=seedSet.get(i).getNumOfSeeds();
				for(int j=0;j<numOfSeeds;j++)
				{
					fileOutBuf.write(seedSet.get(i).getTAZ()+"\t");
					for(int k=1;k<=numOfVs;k++)
					{
						fileOutBuf.write(seedSet.get(i).getSeedsData().get(j).getValue(k)+"\t");
					}
					fileOutBuf.write("\n");
					fileOutBuf.flush();
				}
			}
		    fileOutBuf.close();
		    fileOut.close();
*/		    
			System.out.println("readInSeedsTable "+fileName+" is done");
			return seedSet;			
		}
		public void buildScenarioSet(String purpose) throws IOException
		{
			int TAZID=0;
			//TAZ is the actual number of TAZ. TAZID is the sequence number. 
			int production=0;
			Vector<Trip> iSeedData=new Vector<Trip>();
			Vector<Seed> seedSet=new Vector<Seed>();
			Vector<Trip> scenarioSet=new Vector<Trip>();
			int purposeTag=0;
			if (purpose=="HBW")
			{
				seedSet=seedSetHBW;
				purposeTag=1;
			}
			else if (purpose=="HBShop")
			{
				seedSet=seedSetHBShop;
				purposeTag=2;
			}
			else if (purpose=="HBSRO1")
			{
				seedSet=seedSetHBSRO;
				purposeTag=3;
			}
			else if (purpose=="HBSRO2")
			{
				seedSet=seedSetHBSRO;
				purposeTag=4;
			}
			else if (purpose=="NHBW")
			{
				seedSet=seedSetNHBW;
				purposeTag=5;
			}
			else if (purpose=="NHBO")
			{
				seedSet=seedSetNHBO;
				purposeTag=6;
			}
			//scenarioSet seedSet Generation[1:402][purposeTag]
			int startTAZID,endTAZID;
			if (purposeTag==3)
				{
					startTAZID=1;
					endTAZID=201;
				}
			else if (purposeTag==4)
				{
					startTAZID=202;
					endTAZID=402;
				}
			else
			{
				startTAZID=1;
				endTAZID=402;
			}
			for(TAZID=startTAZID;TAZID<=endTAZID;TAZID++)
			{
				if (purposeTag==3 || purposeTag==4)
				{
					production=Generation[TAZID][3];
					initializeGroups(3,TAZID);
				}
				else if (purposeTag==5 || purposeTag==6)
				{
					production=Generation[TAZID][purposeTag-1];
					initializeGroups(purposeTag-1,TAZID);
				}
				else
				{
					production=Generation[TAZID][purposeTag];
					initializeGroups(purposeTag,TAZID);
				}		
				
				iSeedData=seedSet.get(TAZID-1).getSeedsData();//seedSet is filled automatically
				int size=iSeedData.size();
				Random random=new Random();
				if (size>=5)
				{
					for(int aTrip=1;aTrip<=production;aTrip++)
					{
						Trip iTrip=new Trip();
						iTrip.initializeVariables(numOfVs);
						iTrip.addValueToVariables(0, TAZAndID[TAZID]);//OTAZ of each Trip
						for(int v=1;v<numOfSDVs;v++)
						{
							int r=random.nextInt(size);
							iTrip.addValueToVariables(v, iSeedData.get(r).getValue(v));							
						}
						int OTAZ=(int)iTrip.getValue(0);
						int OTAZID=0;
						for(int j=1;j<403;j++)
						{
							if (TAZAndID[j]==OTAZ)
								{
									OTAZID=j;
									break;
								}
						}
						double r1=random.nextDouble();
						iTrip.addValueToVariables(numOfSDVs, r1*TAZVs[OTAZID][21]*2+TAZVs[OTAZID][21]*2);
						buildPotentialDTAZSet(iTrip);
						scenarioSet.add(iTrip);
					}
				}
				else
				{
					for(int aTrip=1;aTrip<=production;aTrip++)
					{
						Trip iTrip=new Trip();
						iTrip.initializeVariables(numOfVs);
						iTrip.addValueToVariables(0, TAZAndID[TAZID]);//OTAZ of each Trip
						for(int v=1;v<numOfSDVs;v++)
						{
							int r1=random.nextInt(403);
							while(r1==0 || seedSet.get(r1-1).getNumOfSeeds()<1)
							{
								r1=random.nextInt(403);								
							}
							iTrip.addValueToVariables(v, seedSet.get(r1-1).getSeedsData().get(0).getValue(v));
						}
						int OTAZ=(int)iTrip.getValue(0);
						int OTAZID=0;
						for(int j=1;j<403;j++)
						{
							if (TAZAndID[j]==OTAZ)
								{
									OTAZID=j;
									break;
								}
						}
						double r1=random.nextDouble();
						iTrip.addValueToVariables(numOfSDVs, r1*TAZVs[OTAZID][21]*2+TAZVs[OTAZID][21]*2);
						buildPotentialDTAZSet(iTrip);
						scenarioSet.add(iTrip);
					}
				}
			}
			FileWriter fileScenarioSetOut=new FileWriter(purpose+"Scenario"+"Output.txt");
			BufferedWriter fileScenarioSetOutBuf=new BufferedWriter(fileScenarioSetOut);
			for(int i=0;i<scenarioSet.size();i++)
			{
				fileScenarioSetOutBuf.write(scenarioSet.get(i).getValue(0)+"\t");
				for(int k=1;k<numOfVs;k++)
				{
					fileScenarioSetOutBuf.write(scenarioSet.get(i).getValue(k)+"\t");
				}
				fileScenarioSetOutBuf.write("\n");
				fileScenarioSetOutBuf.flush();
			}
			fileScenarioSetOutBuf.close();
			fileScenarioSetOut.close();
			scenarioSet.clear();
			System.out.println("buildScenarioSet "+purpose+" is done");
		}
		public void initializeGroups(int purposeTag,int OTAZID)
		{
			int DTAZID;
			group1=new Vector<Integer>();
			group2=new Vector<Integer>();
			group3=new Vector<Integer>();
			group4=new Vector<Integer>();
			for(DTAZID=1;DTAZID<403;DTAZID++)
			{
				if(OTAZID==DTAZID)
				{
					group1.add(DTAZID);
				}
				else if(ODDistanceMatrix[OTAZID][DTAZID]<cutPoints[purposeTag][1])
				{
					group2.add(DTAZID);
				}
				else if(ODDistanceMatrix[OTAZID][DTAZID]>=cutPoints[purposeTag][1] && ODDistanceMatrix[OTAZID][DTAZID]<=cutPoints[purposeTag][2])
				{
					group3.add(DTAZID);
				}
				else if(ODDistanceMatrix[OTAZID][DTAZID]>cutPoints[purposeTag][2])
				{
					group4.add(DTAZID);
				}
				else
					System.out.println("Error!");
			}
		}
		private void buildPotentialDTAZSet(Trip iTrip)
		{
			Random random=new Random();
			//private int[] numberOfAlternativesInEachGroup=new int[5];
			int i=0;
			int n1=numberOfAlternativesInEachGroup[1];
			int n2=numberOfAlternativesInEachGroup[2];
			int n3=numberOfAlternativesInEachGroup[3];
			int n4=numberOfAlternativesInEachGroup[4];
			int sizeGroup1=group1.size();
			int sizeGroup2=group2.size();
			int sizeGroup3=group3.size();
			int sizeGroup4=group4.size();
			for(i=1;i<=n1;i++)
			{
				iTrip.addValueToVariables(numOfSDVs+i, group1.get(random.nextInt(sizeGroup1)));
			}
			for(i=n1+1;i<=n1+n2;i++)
			{
				if(sizeGroup2==0 && sizeGroup3==0)
				{
					iTrip.addValueToVariables(numOfSDVs+i, group4.get(random.nextInt(sizeGroup4)));
				}
				else if(sizeGroup2!=0)
				{
					iTrip.addValueToVariables(numOfSDVs+i, group2.get(random.nextInt(sizeGroup2)));
				}
				else
				{
					iTrip.addValueToVariables(numOfSDVs+i, group3.get(random.nextInt(sizeGroup3)));
				}
			}
			for(i=n1+n2+1;i<=n1+n2+n3;i++)
			{
				if(sizeGroup3==0)
				{
					iTrip.addValueToVariables(numOfSDVs+i, group4.get(random.nextInt(sizeGroup4)));
				}
				else
				{
					iTrip.addValueToVariables(numOfSDVs+i, group3.get(random.nextInt(sizeGroup3)));
				}
			}
			for(i=n1+n2+n3+1;i<=n1+n2+n3+n4;i++)
			{
				iTrip.addValueToVariables(numOfSDVs+i, group4.get(random.nextInt(sizeGroup4)));
			}
		}
}
