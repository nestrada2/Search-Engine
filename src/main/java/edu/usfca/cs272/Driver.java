package edu.usfca.cs272;

import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
	 * Homework 1: Argument Parser
	 */
	static ArgumentParser parse = new ArgumentParser();

	/**
	 * Initializes the classes necessary based on the provided command-line
	 * arguments. This includes (but is not limited to) how to build or search an
	 * inverted index.
	 *
	 * @param args flag/value pairs used to start this program
	 */
	public static void main(String[] args) 
	{
		// Stores the Mapping from Words to the Documents and Positions
		HashMap<String, HashMap<String, ArrayList<Integer>>> inverted_index = new HashMap<String, HashMap<String, ArrayList<Integer>>>();

		try 
		{
			// Parses the Arguments to Flags/Values
			parse.parse(args);
			
			// Value of the Specified Flag
			Path path = parse.getPath("-text");
			
			// Created an ArrayList to Store all the Paths
			List<Path> path_list = new ArrayList<>();

			// If the Path is Empty, Output an Empty String
			if (parse.getString("-text") == null) 
			{
				System.out.println("");
				return;
			}
			
			// Loop through all the Current Directories/Files and Adds all the Files into "path_list" ArrayList
			FileFinder.listSourceFiles(path, 0, path_list);
			
			/* -------------------- Inverted Index Formatting -------------------- */
			
			// Loop through all the Paths/Files in the "path_list" ArrayList
			for (Path p: path_list) 
			{
				// Add all the Cleaned and Stemmed English Words in the Current File in a new ArrayList 
				ArrayList<String> list = WordCleaner.listStems(p);

				// Loop through all the Cleaned Words in the Current "list" ArrayList
				for (int i = 0; i < list.size(); i += 1) 
				{
					// Current Word
					String word = list.get(i);

					// Add Current "word" as a Key to the "inverted_index"
					inverted_index.putIfAbsent(word, new HashMap<String, ArrayList<Integer>>()); 

					// Grabs the HashMap which is the Value from the "inverted_index": Document, Positions
					HashMap<String, ArrayList<Integer>> values = inverted_index.get(word);

					// The Filename
					String document = p.toString();

					// Store the Document as a Key to the HashMap "values" which is the Values of the "inverted_index"
					values.putIfAbsent(document, new ArrayList<Integer>());

					// Get the Position of the Current "word" in the Document and Add it to the HashMap as a Value: Add 1 because the Index "i" begins at 0
					values.get(document).add(i + 1);
				}
			}
			
//			ArrayList<String> list = path_list.stream()
//					.map(p -> WordCleaner.listStems(p));
//			
//			ArrayList<String> list = IntStream.range(0, path_list.size())
//					.forEach(i -> WordCleaner.listSteams(i));
			
			
			/* -------------------- JSON Formatting -------------------- */

			String json_file = "";

			// Checks if the "inverted_index" Should be Output to a JSON File  
			if (parse.hasValue("-index")) 
			{
				// Gets the Value of the Specified File/Directory
				json_file = parse.getString("-index");
			} 
			else 
			{
				// Default Output Path
				json_file = "index.json";
			}
			
			// Writer
			PrintWriter writer = new PrintWriter(json_file);

			// An ArrayList of the Keys (Words) in "inverted_index"
			ArrayList<String> sorted_keys = new ArrayList<String>(inverted_index.keySet());

			// Sort the Keys in Alphabetical Order
			Collections.sort(sorted_keys);
			
			// Formatting the Writer
			writer.write("{\n");

			int length = sorted_keys.size();
			int index = 0;

			// Loop through the "sorted_keys" ArrayList
			for (String key : sorted_keys) 
			{
				// Add the Word to the Writer
				PrettyJsonWriter.writeIndent("\"" + key + "\": ", writer, 1);

				// Add the Inner HashMap (Document and Positions) to the Writer
				PrettyJsonWriter.writeNestedArrays(inverted_index.get(key), writer, 1);

				// Don't Add Comma to Last Element
				if (index != length - 1) 
				{
					writer.write(",");
				}

				writer.write("\n");
				index += 1;
			}

			writer.write("}");
			writer.close();

		} 
		catch (Exception e) 
		{
			System.err.println();
		}
	}
}

