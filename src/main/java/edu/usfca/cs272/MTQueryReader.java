package edu.usfca.cs272;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * @author Nino Estrada
 * @author CS 272 Software Development (University of San Francisco)
 * @version Fall 2022
 *
 */
public class MTQueryReader extends QueryReader 
{
	
	/**
	 * Worker Threads
	 */
	private WorkQueue multithreading;
	/**
	 * Lock Object Controls Access of the Shared Resources Among the Worker Threads
	 */
	private ReadWriteLock lock;
	
	/**
	 * Instantiates the work queue and lock object
	 * 
	 * @param threads is the number of worker threads
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
	 * @param list_of_queries is a list of set of queries
	 * @param is_partial is checking if to calculate for exact or partial search
	 */
	public void search(InvertedIndex inverted_index, List<Set<String>> list_of_queries, boolean is_partial) 
	{
		// Looping through the List of Queries 
		for (Set<String> query : list_of_queries)
		{
			// Adds a Work (or Task) Request to the Queue
			multithreading.execute(new Task(query, inverted_index, is_partial));
		}
		
		// Wait for Work Queue's to Finish
		multithreading.join();
	}
	
	/**
	 * Inner Task Processes One Query
	 */
	public class Task implements Runnable
	{
		/**
		 * A set of queries
		 */
		Set<String> query;
		
		/**
		 * Inverted Index
		 */
		InvertedIndex inverted_index;
		
		/**
		 * Checks which Search Operation to Perform Exact or Partial
		 */
		boolean is_partial;

		/**
		 * Instantiates the query, inverted index, and is partial
		 * 
		 * @param query is a set of queries
		 * @param inverted_index is the inverted index
		 * @param is_partial checks which search operation to perform exact or partial
		 */
		public Task(Set<String> query, InvertedIndex inverted_index, boolean is_partial)
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
			
			String complete_query = queryKey(query);
		
			// Acquire the Write Lock
			lock.write().lock();
			
			// Skip this Query if it Already Been Seen
			if (query_calculation.containsKey(complete_query)) 
			{
				// Release the Write Lock
				lock.write().unlock();
				return;
			}
			
			// Put Query in the TreeMap no Matter What
			query_calculation.putIfAbsent(complete_query, new TreeMap<String, Integer>());
			
			// Release the Write Lock
			lock.write().unlock();
			
			// Acquire the Read Lock
			lock.read().lock();
			
			// Document to Count either Partially or Exact for this Query
			TreeMap<String, Integer> values = query_calculation.get(complete_query);
			
			// Release the Read Lock
			lock.read().unlock();
			
			// Looping through the Words of One Query
			for (String query_word : query)
			{
				Set<String> matching_keys;
				
				/* -------------------- Exact Search -------------------- */
				
				if (!is_partial)
				{
					// Acquire the Read Lock
					lock.read().lock();
					
					// Query Word is in the Inverted Index
					if (inverted_index.has(query_word))
					{
						// Release the Read Lock
						lock.read().unlock();
						
						// Instantiate the Set
						matching_keys = new HashSet<String>();
						
						// Add the Query Word to the Set of Matching Words
						matching_keys.add(query_word);
					}
					else
					{
						// Release the Read Lock and Continue to Next Iteration
						lock.read().unlock();
						continue;
					}
				}
				else
				{
					/* -------------------- Partial Search -------------------- */
					
					// Acquire the Read Lock
					lock.read().lock();
					
					// Checks if the Query Word is Partially in the Inverted Index
					matching_keys = inverted_index.getByPrefix(query_word);
					
					// Release the Read Lock
					lock.read().unlock();
				}
				
				// Loop through the Inverted Index's Words
				for (String stem_word : matching_keys)
				{
					// Acquire the Read Lock
					lock.read().lock();
					
					// All the Documents for that Specific Stem Word that contains that Query Word: Inner Map of Inverted Index
					Map<String, ArrayList<Integer>> docs = inverted_index.get(stem_word);
					
					// Release the Read Lock
					lock.read().unlock();
					
					// Acquire the Write Lock
					lock.write().lock();
					
					// Loop through the Stem Word's Documents
					for (String document : docs.keySet())
					{
						// Initialized the Count to be Zero
						values.putIfAbsent(document, 0);
						
						// Increment the Count Based on the Size of the Inverted Index's Position's ArrayList Length
						values.put(document, values.get(document) + docs.get(document).size());
					}	
					
					// Release the Write Lock
					lock.write().unlock();
				}
			}
		}
	}
}
