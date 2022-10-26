package edu.usfca.cs272;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

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
	
		// Checks if the "inverted_index" Should be Output to a JSON File  
		if (parse.hasFlag("-index")) 
		{
			
			if (parse.hasValue("-index"))
			{
				// Gets the Value of the Specified File/Directory
				json_file = parse.getString("-index");
			}
		} 
		else 
		{
			// Create an Inverted Index but NOT an Output File
			return;
		}
		
		
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
}

