package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
	 * Instantiates the work queue and lock object
	 * 
	 * @param threads is the number of worker threads
	 */
	public MTInvertedIndex(int threads)
	{
		multithreading = new WorkQueue(threads);
		lock = new ReadWriteLock();
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
	 * Inner Task Checks if a Number is Prime
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
}

