package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Nino Estrada
 * @author CS 272 Software Development (University of San Francisco)
 * @version Fall 2022
 *
 */
public class MTInvertedIndex extends InvertedIndex 
{
	
	/**
	 * 
	 */
	WorkQueue multithreading;
	/**
	 * 
	 */
	ReadWriteLock lock;
	
	/**
	 * @param threads
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
//		lock.write().lock();
		inverted_index.putIfAbsent(word, new HashMap<String, ArrayList<Integer>>()); 
//		lock.write().unlock();
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
			// adding all the tasks to the list
			multithreading.execute(new Task(p));
		}
		
		// wait for the work queue
		multithreading.join();
	}
	
	/**
	 * Inner Task Checks 1 Number is Prime
	 *
	 */
	public class Task implements Runnable
	{
		/**
		 * 
		 */
		public Path current_path;

		/**
		 * @param current_path
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
				list = WordCleaner.listStems(current_path);
				
				// The Filename
				String document = current_path.toString();
				
				// Build the Inverted Index
				add(list, document);	
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
	}
}

