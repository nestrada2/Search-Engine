package edu.usfca.cs272;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Set;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * A simple example of using Jetty and servlets to use an HTML form.
 *
 * @author Nino Estrada
 * @author CS 272 Software Development (University of San Francisco)
 * @version Fall 2022
 */
public class SearchEngineServer 
{
	/**
	 * The logger to use (Jetty is configured via the pom.xml to use Log4j2)
	 */
	public static Logger log = LogManager.getLogger();
	
	/**
	 * Inverted Index Builder: Stores the Mapping from Words to the Documents and Positions
	 */
	public static InvertedIndex inverted_index;
	
	/**
	 * Lock Object Controls Access of the Shared Resources Among the Worker Threads
	 */
	public static ReadWriteLock lock;
	
	/**
	 * The Web Page's Title
	 */
	private static final String TITLE = "Rooster";
	
	/**
	 * Initializes the Server
	 * 
	 * @param inverted_index is the data structure storing the mapping from words to the documents and positions
	 * @param port is the number associated with the IP address to communicate between devices
	 * @throws InterruptedException if the server was interrupted
	 * @throws Exception if the server was unable to start
	 */
	public SearchEngineServer(InvertedIndex inverted_index, int port) throws InterruptedException, Exception
	{
		SearchEngineServer.inverted_index = inverted_index;
		
		Server server = new Server(port);
		lock = new ReadWriteLock();
		
		ServletHandler handler = new ServletHandler();
		
		// Applications with these URLs
		handler.addServletWithMapping(SearchEngineGetServlet.class, "/get_search");
		handler.addServletWithMapping(SearchEnginePostServlet.class, "/post_search");

		// Server Contains the Applications
		server.setHandler(handler);
		
		// Starts Listening
		server.start();
		
		// Automatically Multithreaded because Servlets typically Run on Multithreaded Servers
		log.info("Server: {} with {} threads", server.getState(), server.getThreadPool().getThreads());
		
		// Wait for the Server to Finish
		server.join();
	}	

	/**
	 * Outputs and responds to HTML form while re-displaying form after generating result.
	 */
	public static class SearchEngineGetServlet extends HttpServlet 
	{
		/**
		 * Class version for serialization, in [YEAR][TERM] format (unused)
		 */
		private static final long serialVersionUID = 202240;

		@Override
		protected void doGet(HttpServletRequest request, HttpServletResponse response)
				throws ServletException, IOException {
			log.info(request);

			// String html = fileToString("/../../../../resources/getSearch.html");
			String html = fileToString("getSearch.html");

			processingQueryData(request, response, html);
		}
	}

	/**
	 * Outputs and responds to HTML form without re-displaying form after generating result.
	 */
	public static class SearchEnginePostServlet extends HttpServlet 
	{
		/**
		 * Class version for serialization, in [YEAR][TERM] format (unused)
		 */
		private static final long serialVersionUID = 202240;
		
		@Override
		protected void doGet(HttpServletRequest request, HttpServletResponse response)
				throws ServletException, IOException {
			log.info(request);

			// String html = fileToString("../../../../resources/doGet.html");
			String html = fileToString("resources/doGet.html");

			// Send the Response Object back to the User
			PrintWriter out = response.getWriter();
			out.printf(html, TITLE, Thread.currentThread().getName());

			response.setContentType("text/html");
			response.setStatus(HttpServletResponse.SC_OK);
		}

		@Override
		protected void doPost(HttpServletRequest request, HttpServletResponse response)
				throws ServletException, IOException {
			log.info(request);

			// String html = fileToString("../../../../resources/doPost.html");
			String html = fileToString("resources/doPost.html");

			processingQueryData(request, response, html);
		}
	}
	
	/**
	 * Processes the query data into search results and sends it to HTML.
	 * 
	 * @param request is the user's requested information
	 * @param response is the results of the user's inquiry
	 * @param html is the string results of the response
	 * @throws IOException if the response can't be written
	 */
	public static void processingQueryData(HttpServletRequest request, HttpServletResponse response, String html) throws IOException
	{
		// Getting the Input from the User
		String query = request.getParameter("query");
		log.info("{}", query);
		
		// Search Results as an HTML Contained in a Mutable String
		StringBuilder results_html = new StringBuilder();
		
		// First Time Getting the Form
		if (query == null || query.isBlank()) 
		{
			query = "";
		}
		else 
		{
			// Query Store in a Mutable String
			query = new StringBuilder(query).toString();
			
			// Stems Each Query (Word) in English and Stores it in a TreeSet
			Set<String> clean_line = WordCleaner.uniqueStems(query);
			
			// Not MTQueryReader because it has deal with One Query
			QueryReader query_reader = new QueryReader();
			
			// Store Queries in this Data Structure because 
			ArrayList<Set<String>> queries = new ArrayList<>();
			
			queries.add(clean_line);
			
			// Acquire the Read Lock
			lock.read().lock();
			
			// For any case it crashes make sure you unlock it
			try
			{
				// Builds the Queries TreeMap mapping Queries to its Document and Word Count with Partial Search
				query_reader.search(inverted_index, queries, true);
				
				// Computes and Calculates the Score of Current User's Query
				query_reader.calculateScore(inverted_index.getWordCount());
				
				// Returns the Set of Queries into a String
				String query_key = query_reader.queryKey(clean_line);
				
				// Stores the Current User's Query's Count, Score, and Document
				Set<Entry> query_score = query_reader.getResultsForOneQuery(query_key);
				
				// Message for No Matches for Word
				if (query_score.isEmpty())
				{
					results_html.append("No results were found");
				}
				
				// Loop through the Entries
				for (Entry entry: query_score)
				{
					// HTM: Tag
					results_html.append("<a href = \"");
					
					// Web Link
					results_html.append(entry.getDocument());
					
					// End of Opening HTML Tag
					results_html.append("\">");
					
					// Display to the User
					results_html.append(StringEscapeUtils.escapeHtml4(entry.getDocument()));
					
					// Closing HTML Tag and New Line
					results_html.append("</a><p>");					
				}
			}
			finally
			{
				// Release the Read Lock
				lock.read().unlock();
			}
		}
		
		// Send the Response Object back to the User
		PrintWriter out = response.getWriter();
		out.printf(html, TITLE, results_html, Thread.currentThread().getName());

		response.setContentType("text/html");
		response.setStatus(HttpServletResponse.SC_OK);
	}

	public static String fileToString(String fileName)
	{
		String content = "";
		try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
			StringBuilder stringBuilder = new StringBuilder();
			String line = null;
			String ls = System.getProperty("line.separator");
			while ((line = reader.readLine()) != null) {
				stringBuilder.append(line);
				stringBuilder.append(ls);
			}
			// delete the last new line separator
			stringBuilder.deleteCharAt(stringBuilder.length() - 1);
			reader.close();

			content = stringBuilder.toString();
			return content;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		};

		return content;
	}
}

/*
 * References
 * Reverse Server - https://github.com/usf-cs272-03-fall2022/lectures/blob/main/ServletData/src/main/java/edu/usfca/cs272/ReverseServer.java
 * Message Servlet - https://github.com/usf-cs272-03-fall2022/lectures/blob/main/ServletData/src/main/java/edu/usfca/cs272/MessageServlet.java
 * HttpServlet - https://docs.oracle.com/javaee/7/api/javax/servlet/http/HttpServlet.html
 */ 
