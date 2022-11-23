package edu.usfca.cs272;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

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
	protected static TreeMap<String, TreeMap<String, Integer>> query_calculation;
	
	/**
	 * Instantiates the TreeMap
	 */
	public QueryReader()
	{	
		query_calculation = new TreeMap<>();
	}
	
	/**
	 * Reads and steams the query file to a list of a TreeSet 
	 * 
	 * @param query_file is the text file of queries used to search
	 * @return a list of queries (words) store as a TreeSet which are stemmed and sorted
	 * @throws IOException if the query file could not be read 
	 * 
	 */
	public List<Set<String>> clean(Path query_file) throws IOException
	{
		// Used to Store Each Query (Word) in a Sorted List
		ArrayList<Set<String>> list_of_queries = new ArrayList<Set<String>>();
		
		// Read the User's Queries
		try(BufferedReader reader = Files.newBufferedReader(query_file))
		{
			String line;
			
			// Keep Looping through the User's Queries
			while ((line = reader.readLine()) != null)
			{
				// Stems Each Query (Word) in English
				Set<String> clean_line = WordCleaner.uniqueStems(line);
				
				// If the User's Query are Empty and Continue to Next Iteration
				if (clean_line.isEmpty())
				{
					continue;
				}
				
				// Adding a Set of Words of this Query to an ArrayList 
				list_of_queries.add(clean_line);
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
	public void search(InvertedIndex inverted_index, List<Set<String>> list_of_queries, boolean is_partial) 
	{		
		// Looping through the List of Queries 
		for (Set<String> query : list_of_queries)
		{
			// Character Class Trying to Catch "[,]"
			String treeset_regex = "[,\\[\\]]";
			
			// When using the toString() Method to a TreeSet it Adds "[,]" so Remove It
			String complete_query = query.toString().replaceAll(treeset_regex, "");
			
			// Skip this Query if it Already Been Seen
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
				Set<String> matching_keys;
				
				/* -------------------- Exact Search -------------------- */
				
				if (!is_partial)
				{
					// Query Word is in the Inverted Index
					if (inverted_index.has(query_word))
					{
						// Instantiate the Set
						matching_keys = new HashSet<String>();
						
						// Add the Query Word to the Set of Matching Words
						matching_keys.add(query_word);
					}
					else
					{
						continue;
					}
				}
				else
				{
					/* -------------------- Partial Search -------------------- */
					
					// Checks if the Query Word is Partially in the Inverted Index
					matching_keys = inverted_index.getByPrefix(query_word);
				}
				
				// Loop through the Inverted Index's Words
				for (String stem_word : matching_keys)
				{
					// All the Documents for that Specific Stem Word that contains that Query Word: Inner Map of Inverted Index
					Map<String, ArrayList<Integer>> docs = inverted_index.get(stem_word);
					
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
	
	/**
	 * Prints the exact or partial search results in JSON format
	 * 
	 * @param inverted_index is the class used to retrieve which word count of a specific document
	 * @param word_count is a mapping of words and their total count based on a specific document
	 * @param writer is the class file for file output string that we want to write to the file
	 * @throws IOException if it could not read the results file
	 */
	public void printJson(InvertedIndex inverted_index, Map<String, Integer> word_count, Writer writer) throws IOException
	{
		PrettyJsonWriter.writeNestedArrays(query_calculation, word_count, writer, 0);
	}
}
