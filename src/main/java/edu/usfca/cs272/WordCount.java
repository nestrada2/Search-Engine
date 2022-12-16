package edu.usfca.cs272;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * Utility class for counting the number of words of a specific file
 * 
 * @author Nino Estrada
 * @author CS 272 Software Development (University of San Francisco)
 * @version Fall 2022
 *
 */
public class WordCount 
{
	/**
	 * Stores the Mapping from Documents and Number of Words 
	 */
	private TreeMap<String, Integer> word_count;
	
	/**
	 * Instantiates the Word Count
	 */
	public WordCount()
	{
		word_count = new TreeMap<>();
	}
	
	/**
	 * Increments the word count of a specific document
	 * 
	 * @param document is the current key to increment it's word count value
	 */
	public void increment(String document)
	{
		
		// Already Seen a Word in this Document
		if (word_count.containsKey(document))
		{
			int current_count = word_count.get(document);
			word_count.put(document, current_count + 1);
		}
		else
		{
			// Looking/Opening a Document for the First Time
			word_count.put(document, 1);
		}
	}
	
	/**
	 * @return a view only copy of the word count TreeMap
	 */
	public Map<String, Integer> getWordCount()
	{
		return Collections.unmodifiableMap(word_count);
	}
	
	/**
	 * Prints the word count in JSON format
	 * 
	 * @param writer is the class file for file output string that we want to write to the file
	 * @throws IOException if it could not read the file
	 */
	public void printJson(Writer writer) throws IOException
	{
		PrettyJsonWriter.writeObject(word_count, writer, 0);
	}
}
