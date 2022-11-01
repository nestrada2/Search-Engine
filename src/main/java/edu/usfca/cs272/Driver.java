package edu.usfca.cs272;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Class responsible for running this project based on the provided command-line
 * arguments. See the README for details.
 *
 * @author Nino Estrada
 * @author CS 272 Software Development (University of San Francisco)
 * @version Fall 2022
 */
public class Driver 
{
	/**
	 * Initializes the classes necessary based on the provided command-line
	 * arguments. This includes (but is not limited to) how to build or search an
	 * inverted index.
	 *
	 * @param args flag/value pairs used to start this program
	 */
	public static void main(String[] args) 
	{		
		/**
		 * Homework 1: Argument Parser - Pass in the Command Line Arguments to Parse
		 */
		ArgumentParser parse = new ArgumentParser(args);
		
		/**
		 * Inverted Index Builder: Stores the Mapping from Words to the Documents and Positions
		 */
		InvertedIndex inverted_index = new InvertedIndex();
		
		/**
		 * Word Counter: Stores the Mapping of a Documents and its Number of Words
		 */
		WordCount word_count = new WordCount();
		
		/**
		 * Query Reader: Reads the Queries to be used for Search, Stems Each Word, and Adds Each Stem Word to a List
		 */
		QueryReader query_reader = new QueryReader();
		
		/* -------------------- Parsing -------------------- */
		
		// Parses the Arguments to Flags/Values
		parse.parse(args);
		
		// Value of the Specified Flag
		Path path = parse.getPath("-text");
		
		// Created an ArrayList to Store all the Paths
		 List<Path> path_list = new ArrayList<>();

		// If the Path is Empty, Output an Empty String
		if (parse.getString("-text") == null) 
		{
			System.out.println("The given path is empty.");
		}
		else
		{
			try 
			{
				// Loop through all the Current Directories/Files and Adds all the Files into the ArrayList
				path_list = FileFinder.listSourceFiles(path, 0);
			}
			catch (NoSuchFileException e)
			{
				System.out.println("The input was not a file.");
			}
			catch (IOException e)
			{
				System.out.println("Could not read the attributes of the given path.");
			}
		}
		
		/* -------------------- Inverted Index Formatting -------------------- */
	
		try
		{
			inverted_index.add(path_list);
		}
		catch (IOException e)
		{
			System.out.println("Could not read file.");
		}
		
		/* -------------------- JSON Formatting -------------------- */

		// Default Output Path
		String json_file = "index.json";
		String counts_file = "counts.json";
		String results_file = "results.json";
		
		// Default Search Operation for Query
		boolean is_partial = true;
		
		// Checks if Flag is Exact Search Operation
		if (parse.hasFlag("-exact"))
		{
			is_partial = false;
		}
		
		/* -------------------- Index: JSON Formatting -------------------- */
	
		// Checks if the Inverted Index Should be Output to a JSON File  
		if (parse.hasFlag("-index")) 
		{
			
			if (parse.hasValue("-index"))
			{
				// Gets the Value of the Specified File/Directory
				json_file = parse.getString("-index");
			}
			
			// Write to the JSON File
			try (PrintWriter writer = new PrintWriter(json_file))
			{
				// Formatting the Writer
				inverted_index.printJson(writer);
				
			} 
			catch (IOException e) 
			{
				System.out.println("Could not read file.");
			}
		}
		
		/* -------------------- Counts: JSON Formatting -------------------- */
		
		if (parse.hasFlag("-counts"))
		{
			if (parse.hasValue("-counts"))
			{
				// Gets the Value of the Specified File/Directory
				counts_file = parse.getString("-counts");
			}
			
			// Write to the JSON File
			try (PrintWriter count_writer = new PrintWriter(counts_file))
			{
				// Formatting the Writer
				inverted_index.printWordCountJson(count_writer);
			}
			catch (IOException e) 
			{
				System.out.println("Could not read counts file.");
			}	
		}
		
		/* -------------------- Query: JSON Formatting -------------------- */
		
		if (parse.hasFlag("-query"))
		{
			if (parse.hasValue("-query"))
			{
				// Gets the Name of the File Containing the User's Queries
				String query_file = parse.getString("-query");
				
				// Convert a String to a Path
				Path query_path = Paths.get(query_file);
				
				// Used to Store all the Query Words in a List
				List<TreeSet<String>> queries;
				
				try
				{
					queries = query_reader.clean(query_path, inverted_index);
					query_reader.search(inverted_index, queries, is_partial);
				}
				catch (IOException e)
				{
					System.out.println("Could not read query file.");
				}
			}
		}
		
		/* -------------------- Results: JSON Formatting -------------------- */
		
		if (parse.hasFlag("-results"))
		{
			if (parse.hasValue("-results"))
			{
				// Gets the Value of the Specified File/Directory
				results_file = parse.getString("-results");
			}
				
			// Convert a String to a Path
			Path result_path = Paths.get(results_file);
			
			// Write to the JSON File
			try (PrintWriter results_writer = new PrintWriter(results_file))
			{
				// Formatting the Writer
				query_reader.printJson(inverted_index, inverted_index.getWordCount(), results_writer);
				
				// Results
				System.out.println(results_file);
			}
			catch (IOException e) 
			{
				System.out.println("Could not read results file.");
			}	
		}
	}
}

