package edu.usfca.cs272;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Utility class for reading, stemming, and listing the query file
 * 
 * @author Nino Estrada
 * @author CS 272 Software Development (University of San Francisco)
 * @version Fall 2022
 *
 */
public class QueryReader 
{
	/**
	 * Stores a Mapping of a Clean, Stemmed Query to its Documents and Match Count for each Document
	 */
	private static TreeMap<String, TreeMap<String, Integer>> query_calculation;
	
	/**
	 * Instantiates the TreeMaps
	 */
	public QueryReader()
	{	
		query_calculation = new TreeMap<>();
		
		// Want Highest Scores to Appear First - TreeMap by Default is in Ascending Order
//		score = new TreeMap<>(Collections.reverseOrder());
	}
	
	/**
	 * Reads and steams the query file to a list of a TreeSet 
	 * 
	 * @param query_file is the text file of queries used to search
	 * @param inverted_index 
	 * @return a list of queries (words) store as a TreeSet which are stemmed and sorted
	 * @throws IOException if the query file could not be read 
	 * 
	 */
	public List<TreeSet<String>> clean(Path query_file, InvertedIndex inverted_index) throws IOException
	{
		// Used to Store Each Query (Word) in a Sorted List
		ArrayList<TreeSet<String>> list_of_queries = new ArrayList<TreeSet<String>>();
		
		// Read the User's Queries
		try(BufferedReader reader = Files.newBufferedReader(query_file))
		{
			String line;
			
			// Keep Looping through the User's Queries
			while ((line = reader.readLine()) != null)
			{
				// Used to Store Each Query (Word) in a List
				ArrayList<String> clean_line = new ArrayList<>();
				
				// Stems the Word in English
				clean_line = WordCleaner.listStems(line);
				
				// If the User's Query are Empty
				if (clean_line.isEmpty())
				{
					continue;
				}
				
				// Adding a TreeSet of Words of this Query to an ArrayList 
				list_of_queries.add(new TreeSet<String>(clean_line));
			}
			
			return list_of_queries;
		}
	}
	
	/**
	 * Builds the queries TreeMap mapping queries to its document and search count
	 * 
	 * @param inverted_index is the inverted index
	 * @param list_of_queries is a list of TreeSet of queries
	 * @param is_partial is checking if to calculate for exact or partial search
	 */
	public void search(InvertedIndex inverted_index, List<TreeSet<String>> list_of_queries, boolean is_partial) 
	{
		// Clear TreeMap
//		query_calculation = new TreeMap<>();
		
		// Looping through the List of Queries 
		for (TreeSet<String> query : list_of_queries)
		{
			// Character Class Trying to Catch , [ and ]
			String regex = "[,\\[\\]]";
			
			// TreeSet when toString() adds [,] so remove it
			String complete_query = query.toString().replaceAll(regex, "");
			
			// Skip this Query if it already been seen
			if (query_calculation.containsKey(complete_query)) 
			{
				continue;
			}
			
			// Put Query in the TreeMap no Matter What
			query_calculation.putIfAbsent(complete_query, new TreeMap<String, Integer>());
			
			// Document to Count either Partially or Exact for this Query
			TreeMap<String, Integer> values = query_calculation.get(complete_query);
			
			// Looping through the Words of One Query
			for (String query_word : query)
			{
				// Loop through the Inverted Index's Words
				for (String stem_word : inverted_index.getSortedKeys())
				{
					// Substring (Query Word) is Present in the Current String within the ArrayList of Query Words
					String regex_two = "(?m)^" + query_word + ".*";
					
					// Query Word is in the Inverted Index either Partially or Exact
					if (!is_partial && stem_word.equals(query_word) || is_partial && stem_word.matches(regex_two))
					{
						// All the Documents for that Specific Stem Word that contains that Query Word: Inner Map of Inverted Index
						Map<String, ArrayList<Integer>> docs = inverted_index.get(stem_word);
						
						// Document has No Words
//						if (docs == null)
//						{
//							continue;
//						}
						
						// Loop through the Stem Word's Documents
						for (String document : docs.keySet())
						{
							// Initialized the Count to be Zero
							values.putIfAbsent(document, 0);
							
							// Increment the Count Based on the Size of the Inverted Index's Position's ArrayList Length
							values.put(document, values.get(document) + docs.get(document).size());
						}	
					}
				}
			}
		}
	}
	
	
	/**
	 * Provides the exact count of a particular query word
	 * 
	 * @param query is the current queries we want to get its count
	 * @param document is the filename we are searching the queries count in that file
	 * @return the exact count of a specific query
	 */
//	public int getQueryCount(String query, String document)
//	{
//		return query_calculation.get(query).get(document);
//	}
	
	/**
	 * Prints the count in JSON format
	 * 
	 * @param inverted_index is the class used to retrieve which the word count of a specific document
	 * @param word_count is a mapping of words and their total count based on a specific document
	 * @param writer is the class file for file output string that we want to write to the file
	 * @throws IOException if it could not read the results file
	 */
	public void printJson(InvertedIndex inverted_index, Map<String, Integer> word_count, Writer writer) throws IOException
	{
		PrettyJsonWriter.writeNestedArrays(query_calculation, word_count, writer, 0);
	}
}
