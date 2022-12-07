package edu.usfca.cs272;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
	 * 
	 */
	boolean is_http = false;
	/**
	 * 
	 */
	int max_crawl;
	/**
	 * 
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
	 * @param max_crawl 
	 */
	public MTInvertedIndex(int threads, int max_crawl)
	{
		multithreading = new WorkQueue(threads);
		lock = new ReadWriteLock();
		this.max_crawl = max_crawl;
		total_crawl = 0;
	}
	
	/**
	 * @param max_crawl
	 */
	public void setMaxCrawl(int max_crawl)
	{
		this.max_crawl = max_crawl;
	}
	
	/**
	 * Adds the word to the inverted index
	 * 
	 * @param word is a key that will be added to the inverted index
	 */
	public void add(String word)
	{
		inverted_index.putIfAbsent(word, new TreeMap<String, ArrayList<Integer>>()); 
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
		
		// Add Current "word" as a Key to the inverted index
		add(word);
		
		// Counts Words in a Specific Document
		word_count.increment(document);
		
		// Grabs the HashMap which is the Value from the inverted index: Document, Positions
		Map<String, ArrayList<Integer>> values = inverted_index.get(word);

		// Store the Document as a Key to the HashMap "values" which is the Values of the inverted index
		values.putIfAbsent(document, new ArrayList<Integer>());

		// Get the Position of the Current word in the Document and Add it to the HashMap as a Value
		values.get(document).add(position);
		
		// Release the Write Lock
		lock.write().unlock();
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
	 * @param seed_is the starting url for web crawling
	 * @throws IOException if there is an IO error
	 */
	public void addHtml(URL seed) throws IOException
	{
		// Adds a Work (or Task) Request to the Queue
		multithreading.execute(new HtmlTask(seed));
		
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
				
				// Build the Inverted Index
				add(list, document);	
			} 
			catch (IOException e) 
			{
				System.out.println("Could not read the path \"" + current_path.toString() +"\"");
			}
		}
	}
	
	/**
	 * @author nino
	 *
	 */
	public class HtmlTask implements Runnable 
	{
		/**
		 * Current Webpage
		 */
		public URL current_url;

		/**
		 * Instantiates the current url
		 * 
		 * @param current_url is the current webpage
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
			
//			System.out.println(current_url);

			var results = HtmlFetcher.fetch(current_url, 3);

			if (results == null)
			{
				return;
			}
			
//			// Acquire the Write Lock
//			lock.write().lock();
//			total_crawl += 1;
//			// Release the Write Lock
//			lock.write().unlock();
			
			String content;
			content = HtmlCleaner.stripBlockElements(results);
			
			// Getting the Links inside the Html Code
			List<URL> links = LinkFinder.listUrls(current_url, content);
			
//			// Acquire the Write Lock
//			lock.write().lock();
			
			// Loop through the Links and Give each Link a Working Thread
			for (URL link : links)
			{
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
						
						multithreading.execute(new HtmlTask(link));
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
			
			// Release the Write Lock
//			lock.write().unlock();
			
			content = HtmlCleaner.stripTags(content);
			content = HtmlCleaner.stripEntities(content);
			
			// Cleans and Stems Each Word in English of the Contents of the Current URL
			list = WordCleaner.listStems(content);
			
			// The URL
			String url = current_url.toString();
			
			// Build the Inverted Index
			add(list, url);
		}
	}
}

