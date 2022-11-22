package edu.usfca.cs272;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * @author Nino Estrada
 * @author CS 272 Software Development (University of San Francisco)
 * @version Fall 2022
 *
 */
public class MTQueryReader extends QueryReader {
	
	/**
	 * 
	 */
	private WorkQueue multithreading;
	/**
	 * 
	 */
	private ReadWriteLock lock;
	
	/**
	 * @param threads
	 */
	public MTQueryReader(int threads)
	{
		multithreading = new WorkQueue(threads);
		lock = new ReadWriteLock();
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
		
		// Looping through the List of Queries 
		for (TreeSet<String> query : list_of_queries)
		{
			multithreading.execute(new Task(query, inverted_index, is_partial));
		}
		
		// Wait for Work Queue's to Finish
		multithreading.join();
	}
	
	// Inner Task CHeck 1 Number is Prime
		public class Task implements Runnable
		{
			/**
			 * 
			 */
			TreeSet<String> query;
			/**
			 * 
			 */
			InvertedIndex inverted_index;
			/**
			 * 
			 */
			boolean is_partial;

			/**
			 * @param query
			 * @param inverted_index
			 * @param is_partial
			 */
			public Task(TreeSet<String> query, InvertedIndex inverted_index, boolean is_partial)
			{
				this.query = query;
				this.inverted_index = inverted_index;
				this.is_partial = is_partial;
				
			}

			@Override
			public void run()
			{
				// Add all the Cleaned and Stemmed English Words of the Current File in a new ArrayList 
				ArrayList<String> list;
				
				// Character Class Trying to Catch "[,]"
				String treeset_regex = "[,\\[\\]]";
				
				// TreeSet when toString() adds "[,]" so remove it
				String complete_query = query.toString().replaceAll(treeset_regex, "");
				
				
				lock.write().lock();
				
				// Skip this Query if it Already Been Seen
				if (query_calculation.containsKey(complete_query)) 
				{
					lock.write().unlock();
					return;
				}
				
				
				// Put Query in the TreeMap no Matter What
				query_calculation.putIfAbsent(complete_query, new TreeMap<String, Integer>());
				
				lock.write().unlock();
				
				lock.read().lock();
				
				// Document to Count either Partially or Exact for this Query
				TreeMap<String, Integer> values = query_calculation.get(complete_query);
				
				lock.read().unlock();
				
				// Looping through the Words of One Query
				for (String query_word : query)
				{
					Set<String> matching_keys = new HashSet<String>();
					
					// Exact Search
					if (!is_partial)
					{
						lock.read().lock();
						
						// Query Word is in the Inverted Index
						if (inverted_index.has(query_word))
						{
							lock.read().unlock();
							matching_keys.add(query_word);
						}
						else
						{
							lock.read().unlock();
							continue;
						}
					}
					else
					{
						// Partial Search
						
						lock.read().lock();
						
						List<String> sorted_keys = inverted_index.getSortedKeys();
						
						lock.read().unlock();
						
						// Loop through the Inverted Index's Words
						for (String stem_word : inverted_index.getSortedKeys())
						{
							// Query Word is in the Inverted Index Partially
							if (stem_word.startsWith(query_word))
							{
								matching_keys.add(stem_word);
							}
						}
					}
					
					
					// Loop through the Inverted Index's Words
					for (String stem_word : matching_keys)
					{
						
						
						// Query Word is in the Inverted Index either Partially or Exact
//						if (!is_partial && stem_word.equals(query_word) || is_partial && stem_word.matches(substring_regex))
//						{
						lock.read().lock();
						
						// All the Documents for that Specific Stem Word that contains that Query Word: Inner Map of Inverted Index
						Map<String, ArrayList<Integer>> docs = inverted_index.get(stem_word);
						
						lock.read().unlock();
						
						lock.write().lock();
						
						// Loop through the Stem Word's Documents
						for (String document : docs.keySet())
						{
							// Initialized the Count to be Zero
							values.putIfAbsent(document, 0);
							
							// Increment the Count Based on the Size of the Inverted Index's Position's ArrayList Length
							values.put(document, values.get(document) + docs.get(document).size());
						}	
						
						lock.write().unlock();
//						}
					}
				}
			}
		}
}
