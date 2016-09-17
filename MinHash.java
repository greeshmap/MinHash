import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

public class MinHash {
	public HashMap<String, Integer> unionAll = new HashMap<String, Integer>();
	public HashMap<String, int[]> assignNumbersToTerms = new HashMap<String, int[]>();
	int[] minHashSig;
	public String docarray[] ;
	public int[][] minHash = null;
	public int unionSize;
	public int numPermutations;
	public String folderName;
	public int[] minHashCal;
	int hashFunctions[][];
	
	// Constructor of MinHash class  intializes folder name and number of permutations
	public MinHash(String folder, int noOfPermutation)
	{
		this.numPermutations = noOfPermutation;
		this.folderName = folder;
		docarray=this.allDocs();
		this.fetchAllTermsAndCalculateMinHash();
	}
	// Returns names of all the files in the given folder
	
	public String[] allDocs() {
		FilenameFilter filter;
		File folderNames = new File(folderName);
        int i=0;
        filter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				String lowercaseName = name.toLowerCase();
				if (lowercaseName.endsWith(".txt"))
					return true;
				else
					return false;
			}
		};
        File[] files = folderNames.listFiles();
        String[] documents = new String[files.length];
        for (int f=0;i<files.length;i++)
        {
        	documents[f] = files[i].getName();
            i++;
        }
       // System.out.println(documents.length);
        return folderNames.list(filter);	
	}
	
	/* 
	 * Fetching all the terms from all the documents and assigning unique integer value to each term. For each document we are 
	 * storing the integer value of the terms in the document. Then applying Permutation and finding value permutation values 
	 * for terms in all the documents and finally calculating Minhash values for all the permutations on all the documents
	*/
		
	public void fetchAllTermsAndCalculateMinHash()
	{
		int a,b,prime,termGUI;
		Random random = new Random();
		termGUI = unionAll.size();
		minHash = new int[numPermutations][docarray.length];
		int[] minHashSignature;
		for (int j = 0; j < docarray.length; j++)
		{
			HashSet<String> terms = buildsetofterms(folderName,docarray[j]);
			ArrayList<Integer> indexList = new ArrayList<Integer>();
			for (String term : terms) 
			{
				if (unionAll.containsKey(term))
				{
					int indexVal = (int) unionAll.get(term);
					indexList.add(indexVal);

				} 
				else 
				{
					unionAll.put(term, termGUI);
					indexList.add(termGUI);
					termGUI++;
				}
			}
			int[] termMappingArray = new int[indexList.size()];
			for (int i = 0; i < indexList.size(); i++) {
				termMappingArray[i] = indexList.get(i);
			}
			this.assignNumbersToTerms.put(docarray[j], termMappingArray);
		}
//Applying Permutation and finding value permutation values for terms in all the documents	
		prime = Prime.prime(unionAll.size());
		hashFunctions = new int[numPermutations][unionAll.size()];
		for (int i = 0; i < numPermutations; i++) 
		{
			a = random.nextInt(prime)+0;
			b = random.nextInt(prime)+0;
			for (int j = 0; j < unionAll.size(); j++) 
			{
				hashFunctions[i][j] = ((a * j) + b) % prime;
			}
		}
// Calling MinHashSig method to calculate Min hash values for all the permutations on all the documents 		
		for (int j = 0; j < docarray.length; j++) {
			minHashSignature = minHashSig(docarray[j]);
			for (int i = 0; i < numPermutations; i++) {
				minHash[i][j] = minHashSignature[i];
			}
		}
		
	}
// Calculates and returns min hash signature for a given document
	private int[] minHashSig(String fileName) {		
		int[] terms=assignNumbersToTerms.get(fileName);
		int permutationValue;
		minHashSig= new int[numPermutations];
		//initialize the minHashSig values
		for(int i=0;i<numPermutations;i++)
		{
				minHashSig[i] = Integer.MAX_VALUE;
		}
		for (int i = 0; i < numPermutations; i++) 
		{
			for (int j = 0; j < terms.length; j++) 
			{
				permutationValue = hashFunctions[i][terms[j]];
				if (minHashSig[i] > permutationValue)
					minHashSig[i] = permutationValue;
			}
		}
		return minHashSig;
	}
	
	// Creates set of all the terms in all documents by ignoring STOP words and words of length greater than or equal to 3"
	private HashSet<String> buildsetofterms(String filename, String doc)
	{
		String readLine, terms[];
		String FileName=filename+"\\"+doc;
		//System.out.println(FileName);
		HashSet<String> termSet = new HashSet<String>();
        try 
        {
            BufferedReader br = new BufferedReader(new FileReader(FileName));
            while ((readLine = br.readLine()) != null)
            {
                terms = (readLine.replaceAll("[.,;:'\"]"," ").split("\\s+"));
                for (String term : terms)
                {
                    if (term.length()>=3 && term!="the")
                        termSet.add(term.toLowerCase());
                }
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }
        return termSet;
    }
	
	// Returns total number of union of all terms in all the documents
		public double numTerms()
		{
			return unionAll.size();
		}

	// Returns total number of permutations	
		public double numPermutations()
		{
			return this.numPermutations;
		}

	// Returns MinHash Matrix of all the permutations on all the documents 
		public int[][] minHashMatrix()
		{
			return minHash;
		}
		
		//Calculating approximate Jaccord similarity using Minhash matrix
		public double approximateJaccard(String file1, String file2) {
			int Column1=0,Column2=0;
			int count = 0;
			for (int i = 0; i < numPermutations; i++) 
			{
				for (int j = 0;i < docarray.length-1; i++)
				{
					if (docarray[i].equals(file1)) 
						Column1 = i;
					if (docarray[i].equals(file2)) 
						Column2 = i;
				}
			}
			//System.out.println(numPermutations);
			//System.out.println(docarray.length);
			for (int i = 0; i < numPermutations; i++) 
			{				
					if (minHash[i][Column1] == minHash[i][Column2])
						count = count + 1;
			}
			return (double)count / numPermutations;
		}
		//Calculating exact Jaccard similarity 
		public double exactJaccard(String file1, String file2) 
		{
			double jacSimilarity=0.0;
			HashSet<Integer> set1 = new HashSet<Integer>();
			HashSet<Integer> set2 = new HashSet<Integer>();
			for (int i : assignNumbersToTerms.get(file1))
			{
				set1.add((Integer)i);
			}
			for (int j : assignNumbersToTerms.get(file2)) 
			{
				set2.add((Integer)j);
			}
			double unionSize = set1.size() + set2.size();
			set1.retainAll(set2);
			int intersection = set1.size();
			unionSize = unionSize - intersection;
			jacSimilarity = (double) (((double) intersection) / ((double) unionSize));
			//System.out.println(termsInFile1.size() / (unionSize - termsInFile1.size()));
			return jacSimilarity;
		}

}
