package edu.usfca.cs272;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Utility class for building the inverted index
 * 
 * @author Nino Estrada
 * @author CS 272 Software Development (University of San Francisco)
 * @version Fall 2022
 * 
 */
public class InvertedIndex 
{
	/**
	 * Stores the Mapping from Words to the Documents and Positions
	 */
	protected static TreeMap<String, TreeMap<String, ArrayList<Integer>>> inverted_index;
	
	/**
	 * Counts the Number of Words in a Document
	 */
	protected static WordCount word_count;
	
	/**
	 * Instantiates the Inverted Index and Word Count
	 */
	public InvertedIndex()
	{
		inverted_index = new TreeMap<String, TreeMap<String, ArrayList<Integer>>>();
		word_count = new WordCount();
	}
	
	/**
	 * Prints the inverted index in JSON format
	 * 
	 * @param writer is the class file for file output string that we want to write to the file
	 * @throws IOException if the file could not be read
	 */
	public void printJson(Writer writer) throws IOException
	{
		PrettyJsonWriter.writeNestedArrays(inverted_index, writer, 0);
	}
	
	/**
	 * Prints the word count in JSON format
	 * 
	 * @param writer is the class file for file output string that we want to write to the file
	 * @throws IOException if the counts file could not be read
	 */
	public void printWordCountJson(Writer writer) throws IOException
	{
		word_count.printJson(writer);
	}
	
	/**
	 * @return a view only copy of the word count TreeMap
	 */
	public Map<String, Integer> getWordCount()
	{
		return word_count.getWordCount();
	}
	
	/**
	 * Provides a view only copy of the inverted index's keys in alphabetical order
	 * 
	 * @return a list of the inverted index's keys (words)
	 */
	public List<String> getSortedKeys()
	{
		// Turn Key Set to a List
		List<String> sorted_keys = new ArrayList<>(inverted_index.keySet());
		
		Collections.sort(sorted_keys);
		
		return Collections.unmodifiableList(sorted_keys);
	}
	
	/**
	 * Provides a view only copy of the inverted index's keys 
	 * 
	 * @return a set of the inverted index's keys (words)
	 */
	public Set<String> getKeys()
	{
		return Collections.unmodifiableSet(inverted_index.keySet());
	}
	
	
	/**
	 * Provides the size of the inverted index
	 * 
	 * @return the size of the inverted index
	 */
	public int size()
	{
		return inverted_index.size();
	}
	
	/**
	 * Provides the size of the inner map
	 * 
	 * @param word is a key of the inverted index
	 * @return the size of the inner map
	 */
	public int size(String word)
	{
		return inverted_index.get(word).size();
	}
	
	/**
	 * Provides a view only copy of the inverted index of a particular word
	 * 
	 * @param word is the key searching its values
	 * @return the documents and position(s) of the word
	 */
	public Map<String, ArrayList<Integer>> get(String word) 
	{
		// If the Word is in the Inverted Index
		if (inverted_index.containsKey(word))
		{
			return Collections.unmodifiableMap(inverted_index.get(word));
		}
		
		return null;
	}
	
	/**
	 * Provides a view only copy of the inverted index
	 * 
	 * @return the inverted index
	 */
	public Collection<String> view() 
	{
		return Collections.unmodifiableCollection(inverted_index.keySet());
	}
	
	/**
	 * Getting All entries whose keys start with this query word
	 * 
	 * @param query_word is the current query word
	 * @return a set of keys that matches/starts with the passed in query word
	 */
	public Set<String> getByPrefix(String query_word) 
	{
		// Convert a Portion of the Map that Matches/Starts with this Query Word to a Set
        return inverted_index.subMap(query_word, query_word + Character.MAX_VALUE).keySet();
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
	}
	
	/**
	 * Adds to the Inverted Index 
	 * 
	 * @param list is a collection of words from a file
	 * @param document is the name of the current document
	 */
	public void add(ArrayList<String> list, String document) 
	{		
		// Loop through all the Cleaned Words in the Current "list" ArrayList
		for (int i = 0; i < list.size(); i += 1) 
		{
			// Current Word
			String word = list.get(i);

			// Build the inverted index: Add 1 because the Index "i" begins at 0
			add(word, document, i + 1);
		}
	}
	
	/**
	 * Loops through the paths and adds its respected values to the inverted index
	 * 
	 * @param path_list is a list of Paths
	 * @throws IOException if there is an IO error
	 */
	public void add(List<Path> path_list) throws IOException
	{
		// Loop through the List of Paths
		for (Path p : path_list) 
		{
			// Add all the Cleaned and Stemmed English Words of the Current File in a new ArrayList 
			ArrayList<String> list = WordCleaner.listStems(p);
			
			// The Filename
			String document = p.toString();
			
			// Build the Inverted Index
			add(list, document);	
		}
	}
	
	/**
	 * Checks if the word is a key in the inverted index
	 * 
	 * @param word is the key of the inverted index
	 * @return true if the word is in the inverted index
	 */
	public boolean has(String word)
	{
		// Check if the Inverted Index Contains the Word
		return inverted_index.containsKey(word);
	}


	/**
	 * Checks if the word is in the document
	 * 
	 * @param word is the key of the inverted index
	 * @param document is the value of the inverted index
	 * @return true if the word is in the document
	 */
	public boolean has(String word, String document)
	{
		// Check if the word is in the document
		return get(word).containsKey(document);
	}
}

/*
 * References
 * subMap - https://docs.oracle.com/javase/8/docs/api/java/util/TreeMap.html
 * Character.MAX_VALUE - https://docs.oracle.com/javase/7/docs/api/java/lang/Character.html#:~:text=The%20Character%20class%20wraps%20a,letter%2C%20digit%2C%20etc.)
 */
