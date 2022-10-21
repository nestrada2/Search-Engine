package edu.usfca.cs272;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

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
	 * // Stores the Mapping from Words to the Documents and Positions
	 */
	HashMap<String, HashMap<String, ArrayList<Integer>>> inverted_index;
	
	/**
	 * Instantiates the Inverted Index
	 */
	public InvertedIndex()
	{
		inverted_index = new HashMap<String, HashMap<String, ArrayList<Integer>>>();
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

			// Add Current "word" as a Key to the "inverted_index"
			inverted_index.putIfAbsent(word, new HashMap<String, ArrayList<Integer>>()); 

			// Grabs the HashMap which is the Value from the "inverted_index": Document, Positions
			HashMap<String, ArrayList<Integer>> values = inverted_index.get(word);

			// Store the Document as a Key to the HashMap "values" which is the Values of the "inverted_index"
			values.putIfAbsent(document, new ArrayList<Integer>());

			// Get the Position of the Current "word" in the Document and Add it to the HashMap as a Value: Add 1 because the Index "i" begins at 0
			values.get(document).add(i + 1);
		}
	}
	
	/**
	 * Prints the inverted index in JSON format
	 * 
	 * @param writer is the class file for file output string that we want to write to the file
	 * @throws IOException if an IO error occurs
	 */
	public void printJson(Writer writer) throws IOException
	{
		PrettyJsonWriter.writeNestedArrays(inverted_index, writer, 0);
	}
}
