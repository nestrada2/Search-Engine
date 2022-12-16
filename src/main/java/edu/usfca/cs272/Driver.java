package edu.usfca.cs272;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
	/* -------------------- Global Constants -------------------- */
	
	/**
	 * Default Index JSON File if the Path Argument is not Provided
	 */
	private static final String DEFAULT_JSON_FILE = "index.json";
	
	/**
	 * Default Counts JSON File if the Path Argument is not Provided
	 */
	private static final String DEFAULT_COUNTS_FILE = "counts.json";
	
	/**
	 *  Default Results JSON File if the Path Argument is not Provided
	 */
	private static final String DEFAULT_RESULTS_FILE = "results.json";
	
	/**
	 * Default Worker Threads if the Number of Threads is not Provided
	 */
	private static final int DEFAULT_THREADS = 5;
	
	/**
	 * Default Max if the Total Number of URLs to Crawl is not Provided
	 */
	private static final int DEFAULT_MAX = 1;
	
	/**
	 * Default Web Sever Port if the Port is not Provided
	 */
	private static final int DEFAULT_PORT = 8080;
	
	/**
	 * Logging Messages for Debugging
	 */
	public static Logger log = LogManager.getLogger();
	
	/**
	 * Initializes the classes necessary based on the provided command-line
	 * arguments. This includes (but is not limited to) how to build or search an
	 * inverted index.
	 *
	 * @param args flag/value pairs used to start this program
	 */
	public static void main(String[] args) 
	{
		/* -------------------- Local Variables -------------------- */
		
		/**
		 * Argument Parser: Pass in the Command Line Arguments to Parse
		 */
		ArgumentParser parse = new ArgumentParser(args);
		
		/**
		 * Inverted Index Builder: Stores the Mapping from Words to the Documents and Positions
		 */
		InvertedIndex inverted_index;
		
		/**
		 * Query Reader: Reads the Queries to be used for Search, Stems Each Word, and Adds Each Stem Word to a List
		 */
		QueryReader query_reader;
			
		/**
		 * Initial Webpage/URL the Inverted Index should Start Web Crawling
		 */
		String seed = null;
		
		/**
		 * Number of Worker Threads to Run
		 */
		int threads = 1;
		
		/**
		 * Total Number of URLs to Crawl
		 */
		int max = DEFAULT_MAX;
		
		/**
		// Default Search Operation for Query
		 * 
		 */
		boolean is_partial = true;
		
		/**
		 * File to Store the Results of the Search Operation for Query
		 */
		String query_file = null;
		
		/**
		 * Web Server Port to Accept Socket Connections
		 */
		int port = 8080;
		
		
		/* -------------------- Parsing -------------------- */
		
		// Parses the Arguments to Flags/Values
		parse.parse(args);
			
		/* -------------------- Multithreading -------------------- */

		if (parse.hasFlag("-threads"))
		{	
			// User did NOT provide Number of Worker Threads
			if (!parse.hasValue("-threads"))
			{
				System.out.println("Did not specifiy number of worker threads, so will default to " + DEFAULT_THREADS + ".");
			}
			
			// Number of Threads
			threads = parse.getInteger("-threads", DEFAULT_THREADS);
			
			// Negative Threads was Inputed, Default Back to 5 Threads
			if (threads < 1)
			{
				System.out.println("Inputted fewer than 1 worker thread, so default back to " + DEFAULT_THREADS + ".");
				
				threads = DEFAULT_THREADS;
			}
		}
		// Server/HTML Flag(s) are Provided but not Threads Flag
		else if (parse.hasFlag("-server") || parse.hasFlag("-html"))
		{
			threads = DEFAULT_THREADS;
			
			// User did NOT provide Number of Worker Threads
			System.out.println("Did not specifiy number of worker threads, so will default to " + DEFAULT_THREADS + ".");
		}
		
		
		/* -------------------- HTML -------------------- */
		
		if (parse.hasFlag("-html"))
		{	
			// User did NOT Provide a Webpage
			if (!parse.hasValue("-html"))
			{
				System.out.println("Did not specifiy a webpage.");
				
				return;
			}
			
			// Webpage
			seed = parse.getString("-html");
		}
		
		/* -------------------- Max -------------------- */
		
		if (parse.hasFlag("-max"))
		{
			// User Did NOT Specify Total Number of URLs to Crawl
			if (!parse.hasValue("-max"))
			{
				System.out.println("Did not specifiy total number of URLs to crawl, so will default to " + DEFAULT_MAX + ".");
			}
						
			// Total Number of URLs to Crawl
			if (parse.hasValue("-max"))
			{
				max = parse.getInteger("-max", DEFAULT_MAX);
			}
		}
		
		// Checks if Program is using Multithreading, and A Non-Null Seed Implies that we are Web Crawling and using the HTML Flag
		if (threads > 1 || seed != null)
		{		
			// Multithreaded Inverted Index
			inverted_index = new MTInvertedIndex(threads, max);
			System.out.println("Number of threads: " + threads);
			
			// Multithreaded Query Reader
			query_reader = new MTQueryReader(threads);
			
		}
		else
		{
			// Single Threaded Inverted Index
			inverted_index = new InvertedIndex();
			
			// Single Threaded Query Reader
			query_reader = new QueryReader();
		}
		
		/* -------------------- Parsing File -------------------- */
		
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
				// Loop through all the Current Directories/Files and Adds all the Files into an ArrayList
				path_list = FileFinder.listSourceFiles(path);
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
		
		if (!path_list.isEmpty())
		{
			try
			{
				inverted_index.add(path_list);
			}
			catch (IOException e)
			{
				System.out.println("Could not read file.");
			}
		}
		
		/* -------------------- Web Crawling -------------------- */
		
		if (seed != null)
		{
			try 
			{
				// Convert Seed String to a URL
				URL seed_url = new URL(seed);
				
				// Starts Web Crawling from Seed URL
				((MTInvertedIndex) inverted_index).addHtml(seed_url);
			} 
			catch (MalformedURLException e) 
			{
				System.out.println("malfunction URL");
			}
			catch (IOException e) 
			{
				System.out.println("An error occurred while processing the webpage.");
			}
		}
		
		/* -------------------- Index: JSON Formatting -------------------- */
	
		// Checks if the Inverted Index Should be Output to a JSON File  
		if (parse.hasFlag("-index")) 
		{
			// User did NOT Provided an Index File
			if (!parse.hasValue("-index"))
			{
				System.out.println("Did NOT specificy an index file, so using " + DEFAULT_JSON_FILE + ".");
			}
			
			// Gets the Value of the Specified File/Directory
			String json_file = parse.getString("-index", DEFAULT_JSON_FILE);
			
			// Write to the JSON File
			try (PrintWriter writer = new PrintWriter(json_file))
			{
				// Formatting the Writer
				inverted_index.printJson(writer);	
			} 
			catch (IOException e) 
			{
				System.out.println("Could NOT write to file \"" + json_file + "\"");
			}
		}
		
		/* -------------------- Counts: JSON Formatting -------------------- */
		
		if (parse.hasFlag("-counts"))
		{
			// User did NOT Provided a Counts File
			if (!parse.hasValue("-counts"))
			{
				System.out.println("Did NOT specificy a counts file, so using " + DEFAULT_COUNTS_FILE + ".");
			}
			
			// Gets the Value of the Specified File/Directory
			String counts_file = parse.getString("-counts", DEFAULT_COUNTS_FILE);
			
			// Write to the JSON File
			try (PrintWriter count_writer = new PrintWriter(counts_file))
			{
				// Formatting the Writer
				inverted_index.printWordCountJson(count_writer);
			}
			catch (IOException e) 
			{
				System.out.println("Could NOT write to counts file \"" + counts_file + "\"");
			}	
		}
		
		/* -------------------- Query Calculation: Exact or Partial Search -------------------- */
		
		// Checks if Flag is Exact Search Operation
		if (parse.hasFlag("-exact"))
		{
			is_partial = false;
		}
		
		if (parse.hasFlag("-query"))
		{			
			// Gets the Name of the File Containing the User's Queries
			query_file = parse.getString("-query");
			
			// User did NOT Provided a Query File
			if (query_file == null)
			{
				System.out.println("Could NOT calculate because did NOT specifiy where are the queries.");
				return;
			}
			
			// Convert a String to a Path
			Path query_path = Paths.get(query_file);
			
			// Used to Store all the Query Words in a List
			List<Set<String>> queries;
			
			try
			{
				// Reads and Steams the Query File to a List of a Set 
				queries = query_reader.clean(query_path);
				
				// Calculates Partial or Exact Search Results 
				query_reader.search(inverted_index, queries, is_partial);
			}
			catch (IOException e)
			{
				System.out.println("Could not read query file \"" + query_file + "\"");
			}
		}
		
		/* -------------------- Results: JSON Formatting -------------------- */
		
		if (parse.hasFlag("-results"))
		{
			// User did NOT Provided a Results File
			if (!parse.hasValue("-results"))
			{
				System.out.println("Did NOT Specificy a results file, so using " + DEFAULT_RESULTS_FILE + ".");
			}
			
			// Gets the Value of the Specified File/Directory
			String results_file = parse.getString("-results", DEFAULT_RESULTS_FILE);
				
			// Write to the JSON File
			try (PrintWriter results_writer = new PrintWriter(results_file))
			{
				// Word Count
				Map<String, Integer> word_count = inverted_index.getWordCount();
				
				// Calculates the Score of Each Entry
				query_reader.calculateScore(word_count);
				
				// Formatting the Writer
				query_reader.printJson(inverted_index, word_count, results_writer);
			}
			catch (IOException e) 
			{
				System.out.println("Could NOT write to results file \"" + results_file + "\"");
			}	
		}
		
		/* -------------------- Server -------------------- */
		
		if (parse.hasFlag("-server"))
		{
			// User did NOT provide a Port Number
			if (!parse.hasValue("-server"))
			{
				System.out.println("Did not specifiy a port, so will default to " + DEFAULT_PORT + ".");
			}
			
			// Port Number
			port = parse.getInteger("-server", DEFAULT_PORT);
			
			try
			{
				// Starting up the Server
				SearchEngineServer server = new SearchEngineServer(inverted_index, port);
			} 
			catch (InterruptedException e) 
			{
				System.out.println("The server got interupted.");
			}
			catch (Exception e) 
			{
				System.out.println("Unable to start server.");
			}
		}
	}
}

