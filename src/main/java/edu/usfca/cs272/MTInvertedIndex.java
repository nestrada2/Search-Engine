package edu.usfca.cs272;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
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
public class MTInvertedIndex extends InvertedIndex 
{
	
	/**
	 * Worker Threads
	 */
	WorkQueue multithreading;
	
	/**
	 * Lock Object Controls Access of the Shared Resources Among the Worker Threads
	 */
	ReadWriteLock lock;
	
	/**
	 * Total Number of URLs to Crawl
	 */
	int max_crawl;
	
	/**
	 * Keep Track of Current URLs Crawled
	 */
	int total_crawl;
	
	/**
	 * Keep Track of URL's Attempted to Fetch
	 */
	List<URL> url_list = new ArrayList<>();
	
	/**
	 * Instantiates the work queue and lock object
	 * 
	 * @param threads is the number of worker threads
	 * @param max_crawl maximum URLs to fetch
	 */
	public MTInvertedIndex(int threads, int max_crawl)
	{
		multithreading = new WorkQueue(threads);
		lock = new ReadWriteLock();
		this.max_crawl = max_crawl;
		total_crawl = 0;
	}
	
	/**
	 * Adds a word, a document, and the position(s) to the inverted index 
	 * 
	 * @param word is the key that will be added to the inverted index
	 * @param document is the value of the inner map
	 * @param position is the index of the given word in the given document
	 */
	public void add(String word, String document, int position)
	{
		// Acquire the Write Lock
		lock.write().lock();
		
		try
		{
			super.add(word, document, position);
		}
		finally
		{
			// Release the Write Lock
			lock.write().unlock();
		}
	}
	
	/**
	 * Loops through the paths and adds its respected values to the inverted index
	 * 
	 * @param path_list is a list of Paths
	 * @throws IOException if there is an IO error
	 */
	@Override
	public void add(List<Path> path_list) throws IOException
	{
		// Loop through the List of Paths
		for (Path p : path_list) 
		{
			// Adds a Work (or Task) Request to the Queue
			multithreading.execute(new Task(p));
		}
		
		// Wait for Work Queue's to Finish
		multithreading.join();
	}
	
	/**
	 * Loops through the URLs and adds its respected values to the inverted index
	 * 
	 * @param seed is the starting URL for web crawling
	 * @throws IOException if there is an IO error
	 */
	public void addHtml(URL seed) throws IOException
	{
		// Adds a Work (or Task) Request to the Queue
		multithreading.execute(new HtmlTask(seed));
		
		// Initialized the Total Crawl: Seed is the 1st URL to Begin Crawling
		total_crawl = 1;
		
		url_list.add(seed);
		
		// Wait for Work Queue's to Finish
		multithreading.join();
	}
	
	/**
	 * Inner Task Processes a File to the Inverted Index
	 *
	 */
	public class Task implements Runnable
	{
		/**
		 * Current Document
		 */
		public Path current_path;

		/**
		 * Instantiates the current path
		 * 
		 * @param current_path is the current document
		 */
		public Task(Path current_path)
		{
			this.current_path = current_path;
		}

		@Override
		public void run()
		{
			// Add all the Cleaned and Stemmed English Words of the Current File in a new ArrayList 
			ArrayList<String> list;
			
			try 
			{
				// Cleans and Stems Each Word in English of the Current File
				list = WordCleaner.listStems(current_path);
				
				// The Filename
				String document = current_path.toString();
				
				// Create a New Inverted Index for Each Thread
				InvertedIndex thread_inverted_index = new InvertedIndex();
				
				// Build's the Thread's Inverted Index for 1 Document
				thread_inverted_index.add(list, document);
				
				// Merges Thread's Inverted Index into Main Inverted Index
				merge(thread_inverted_index);
			} 
			catch (IOException e) 
			{
				System.out.println("Could not read the path \"" + current_path.toString() +"\"");
			}
		}
	}
	
	/**
	 * Merges the values of the thread inverted index into the main inverted index
	 * 
	 * @param thread_inverted_index is the current thread's inverted index
	 */
	public void merge(InvertedIndex thread_inverted_index)
	{
		// Words/Keys of the Thread Inverted Index
		Set<String> thread_keys = thread_inverted_index.getKeys();
		
		// Loop through the Tread's Inverted Index
		for (String word : thread_keys)
		{
			// Acquire the Write Lock
			lock.write().lock();
			try
			{
				// Ensures that Thread's Word is in the Main Inverted Index
				add(word);
			}
			finally
			{
				// Release the Write Lock
				lock.write().unlock();
			}
			
			// Inner Map - Documents and Positions
			Map<String, ArrayList<Integer>> inner_map = thread_inverted_index.get(word);
			
			// Loop through the Documents
			for (String doc : inner_map.keySet())
			{
				// List of the Word's Positions
				ArrayList<Integer> positions = inner_map.get(doc);
				
				// Loop through the Positions
				for (Integer position : positions)
				{
					// Added Current Thread's Position Data to the Main Inverted Index
					add(word, doc, position);
				}
			}
		}	
	}
	
	/**
	 * Worker Thread to process 1 single URL
	 *
	 */
	public class HtmlTask implements Runnable 
	{
		/**
		 * Current Web Page
		 */
		public URL current_url;

		/**
		 * Instantiates the current URL
		 * 
		 * @param current_url is the current Web Page
		 */
		public HtmlTask(URL current_url)
		{
			this.current_url = current_url;
		}

		@Override
		public void run()
		{
			
			// Add all the Cleaned and Stemmed English Words of the Current File in a new ArrayList 
			ArrayList<String> list;

			// Fetching the Actual HTML content from that URL up to 3 Redirects
			var results = HtmlFetcher.fetch(current_url, 3);

			if (results == null)
			{
				return;
			}
			
			// Store the Content of the Current URL
			String content;
			
			// Remove the HTML Block Elements: e.g. <p>, <div>, <span>
			content = HtmlCleaner.stripBlockElements(results);
			
			// Getting the Links inside the HTML Code
			List<URL> links = LinkFinder.listUrls(current_url, content);
			
			// Loop through the Links and Give each Link a Working Thread
			for (URL link : links)
			{
				// Acquire the Read Lock - Need to Lock because these are Shared Variables (total and max crawl)
				lock.read().lock();
				
				if (total_crawl < max_crawl)
				{
					// URL is not in the List, Then Crawl
					if (!url_list.contains(link))
					{
						// Release the Read Lock
						lock.read().unlock();
						
						// Acquire the Write Lock
						lock.write().lock();
						
						// Add Link to the Work Queue to be Processed
						multithreading.execute(new HtmlTask(link));
						
						/// Increment Total URL Crawled
						total_crawl += 1;
						
						// Add to the List
						url_list.add(link);
						
						// Release the Write Lock
						lock.write().unlock();
					}
					else
					{
						// Release the Read Lock
						lock.read().unlock();
					}
					
				}
				else
				{
					// Release the Read Lock
					lock.read().unlock();
					break;
				}
			}
			
			// Remove the HTML Tags: e.g. </a>, <!DOCTYPE>, <center>
			content = HtmlCleaner.stripTags(content);
			
			// Remove the HTML Entities: e.g. &amp, &nbsp, &copy, 
			content = HtmlCleaner.stripEntities(content);
			
			// Cleans and Stems Each Word in English of the Contents of the Current URL
			list = WordCleaner.listStems(content);
			
			// The URL
			String url = current_url.toString();
			
			// Current Thread's Inverted Index
			InvertedIndex thread_inverted_index = new InvertedIndex();
			
			// Builds the Current Thread's Inverted Index for 1 URL
			thread_inverted_index.add(list, url);
			
			// Merges the Content of the Current Thread's Inverted Index into the Main Inverted Index
			merge(thread_inverted_index);
		}
	}
}

