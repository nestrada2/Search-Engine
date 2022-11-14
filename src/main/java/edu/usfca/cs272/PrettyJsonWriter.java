package edu.usfca.cs272;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Outputs several simple data structures in "pretty" JSON format where newlines
 * are used to separate elements and nested elements are indented using spaces.
 *
 * Warning: This class is not thread-safe. If multiple threads access this class
 * concurrently, access must be synchronized externally.
 *
 * @author Nino Estrada
 * @author CS 272 Software Development (University of San Francisco)
 * @version Fall 2022
 * 
 */
public class PrettyJsonWriter
{
	/**
	 * Indents the writer by the specified number of times. Does nothing if the
	 * indentation level is 0 or less.
	 *
	 * @param writer the writer to use
	 * @param indent the number of times to indent
	 * @throws IOException if an IO error occurs
	 */
	public static void writeIndent(Writer writer, int indent) throws IOException
	{
		while (indent-- > 0) {
			writer.write("  ");
		}
	}

	/**
	 * Indents and then writes the String element.
	 *
	 * @param element the element to write
	 * @param writer the writer to use
	 * @param indent the number of times to indent
	 * @throws IOException if an IO error occurs
	 */
	public static void writeIndent(String element, Writer writer, int indent) throws IOException
	{
		writeIndent(writer, indent);
		writer.write(element);
	}

	/**
	 * Indents and then writes the text element surrounded by {@code " "}
	 * quotation marks.
	 *
	 * @param element the element to write
	 * @param writer the writer to use
	 * @param indent the number of times to indent
	 * @throws IOException if an IO error occurs
	 */
	public static void writeQuote(String element, Writer writer, int indent) throws IOException
	{
		writeIndent(writer, indent);
		writer.write('"');
		writer.write(element);
		writer.write('"');
	}

	/**
	 * Writes the elements as a pretty JSON array.
	 *
	 * @param elements the elements to write
	 * @param writer the writer to use
	 * @param indent the initial indent level; the first bracket is not indented,
	 *   inner elements are indented by one, and the last bracket is indented at
	 *   the initial indentation level
	 * @throws IOException if an IO error occurs
	 *
	 * @see Writer#write(String)
	 * @see #writeIndent(Writer, int)
	 * @see #writeIndent(String, Writer, int)
	 */
	public static void writeArray(Collection<? extends Number> elements,
								  Writer writer, int indent) throws IOException
	{
		writer.write("[\n");
		int element_size = elements.size();
		int index = 0;

		if (!elements.isEmpty())
		{
			for (Number number: elements)
			{
				writeIndent(number.toString(), writer, indent + 1);

				// Don't add comma to last element
				if (index != element_size - 1)
				{
					writer.write(",\n");
				}

				index += 1;
			}

			writer.write("\n");
			writeIndent("]", writer, indent);
		}
		else
		{
			writeIndent("]", writer, indent);
		}
	}

	/**
	 * Writes the elements as a pretty JSON array to file.
	 *
	 * @param elements the elements to write
	 * @param path the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 * @see Files#newBufferedReader(Path, java.nio.charset.Charset)
	 * @see StandardCharsets#UTF_8
	 * @see #writeArray(Collection, Writer, int)
	 */
	public static void writeArray(Collection<? extends Number> elements,
								  Path path) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			writeArray(elements, writer, 0);
		}
	}

	/**
	 * Returns the elements as a pretty JSON array.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 *
	 * @see StringWriter
	 * @see #writeArray(Collection, Writer, int)
	 */
	public static String writeArray(Collection<? extends Number> elements) {
		try {
			StringWriter writer = new StringWriter();
			writeArray(elements, writer, 0);
			return writer.toString();
		}
		catch (IOException e) {
			return null;
		}
	}

	/**
	 * Writes the elements as a pretty JSON object.
	 *
	 * @param elements the elements to write
	 * @param writer the writer to use
	 * @param indent the initial indent level; the first bracket is not indented,
	 *   inner elements are indented by one, and the last bracket is indented at
	 *   the initial indentation level
	 * @throws IOException if an IO error occurs
	 *
	 * @see Writer#write(String)
	 * @see #writeIndent(Writer, int)
	 * @see #writeIndent(String, Writer, int)
	 */
	public static void writeObject(Map<String, ? extends Number> elements,
								   Writer writer, int indent) throws IOException {

		int index = 0;
		int length = elements.size();
		writer.flush();

		if (elements.isEmpty())
		{
			writer.write("{\n");
			writeIndent("}", writer, indent);
		}
		else
		{
			// @TODO is this `if` statement necessary? If indent is 0, then it'll write the same thing in either block
			if (indent == 0)
			{
				writeIndent("{", writer, indent);
				writer.write("\n");
			}
			else
			{
				writeIndent("{", writer,  0);
				writer.write("\n");
			}

			for (Map.Entry<String, ? extends Number> entry: elements.entrySet())
			{
				// @TODO can you combine these 2 `if` statements? The condition is the same
				if (indent == 0)
				{
					writeIndent(writer, 1);
				}

				if (indent == 0)
				{
					writeQuote(entry.getKey(), writer, indent);
				}
				else
				{
					writeQuote(entry.getKey(), writer, indent + 1);

				}

				writer.write(": ");
				writeIndent(entry.getValue().toString(), writer, 0);

				// Add a Comma If not the Last Element
				if (index != length - 1)
				{
					writer.write(",\n");
				}

				index += 1;
			}

			writer.write("\n");

			writeIndent("}", writer, indent);
		}
	}

	/**
	 * Writes the elements as a pretty JSON object to file.
	 *
	 * @param elements the elements to write
	 * @param path the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 * @see Files#newBufferedReader(Path, java.nio.charset.Charset)
	 * @see StandardCharsets#UTF_8
	 * @see #writeObject(Map, Writer, int)
	 */
	public static void writeObject(Map<String, ? extends Number> elements,
								   Path path) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			writeObject(elements, writer, 0);
		}
	}

	/**
	 * Returns the elements as a pretty JSON object.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 *
	 * @see StringWriter
	 * @see #writeObject(Map, Writer, int)
	 */
	public static String writeObject(Map<String, ? extends Number> elements) {
		try {
			StringWriter writer = new StringWriter();
			writeObject(elements, writer, 0);
			return writer.toString();
		}
		catch (IOException e) {
			return null;
		}
	}

	/**
	 * Writes the elements as a pretty JSON object with nested arrays. The generic
	 * notation used allows this method to be used for any type of map with any
	 * type of nested collection of number objects.
	 *
	 * @param inverted_index the elements to write
	 * @param writer the writer to use
	 * @param indent the initial indent level; the first bracket is not indented,
	 *   inner elements are indented by one, and the last bracket is indented at
	 *   the initial indentation level
	 * @throws IOException if an IO error occurs
	 *
	 * @see Writer#write(String)
	 * @see #writeIndent(Writer, int)
	 * @see #writeIndent(String, Writer, int)
	 * @see #writeArray(Collection)
	 */
	// @TODO: you should rename this to something like `writeNestedNestedArrays` or something,
	// since you have 2 layers of Maps. Also, you should be using writeNestedArrays in this method, instead of
	// manually writing every layer in this 1 method.
	public static void writeNestedArrays(
			// @TODO Please use camelCase, not snake case, since that's what you're using everywhere else
			// @TODO: just pass in a Map and List, not a HashMap and ArrayList, we want this to work for any kind of Map or List
			HashMap<String, HashMap<String, ArrayList<Integer>>> inverted_index,
			Writer writer, int indent) throws IOException {

		writer.flush();
		int length = inverted_index.size();
		int index = 0;

		if (inverted_index.isEmpty())
		{
			writer.write("{\n");
			writeIndent("}", writer, indent);
		}
		else
		{
			writeIndent("{\n", writer, 0);

			// Turn Key Set to a List
			List<String> sorted_keys = new ArrayList<>(inverted_index.keySet().stream().toList());

			Collections.sort(sorted_keys);

			// Loop Through the Words
			for (String key: sorted_keys)
			{
				// Inner Map
				// @TODO You should be using writeNestedArrays here instead of looping again.
				// If you need the keys to be sorted, consider using a TreeMap instead of a HashMap
				Map<String, ? extends Collection<? extends Number>> values = inverted_index.get(key);
				// @TODO you can just pass in values.keySet() I think
				List<String> sorted_values = new ArrayList<>(values.keySet().stream().toList());
				int inner_map_length = sorted_values.size();
				int inner_idx = 0;

				Collections.sort(sorted_values);
				writeQuote(key, writer, indent + 1);
				writer.write(": ");
				writeIndent("{\n", writer, 0);
				writeIndent(writer, indent + 1);
				
				// Loop Through the Inner Map: Document & Position(s)
				for (String value: sorted_values)
				{
					writeQuote(value, writer, indent + 1);
					writer.write(": ");
					Collection<? extends Number> numbers = values.get(value);
		
					writeArray(numbers, writer, indent + 2);
					
					// Add a Comma Except for the Last Element
					if (inner_idx != inner_map_length - 1)
					{
						writer.write(",\n");
						writeIndent(writer, 1);
					}
					
					inner_idx += 1;
				}
				
				// Add a Comma Except for the Last Element
				if (index != length - 1)
				{
					writer.write("\n");
					writeIndent("},\n", writer, 1);
				}
				else
				{
					writer.write("\n");
					writeIndent("}", writer, 1);
				}
				
				index += 1;
			}

			writer.write("\n");
			writeIndent("}", writer, indent);
			writer.write("\n");
		}
	}
	
	/**
	 * Entry of Each User's Query Search Data
	 *
	 */
	// @TODO this should be in a separate class (file)
		// @TODO: Comparable works with Generics. Instead of Object, this should be Entry so that you can just compare Entry to Entry
	public static class Entry implements Comparable<Object>
	{
		/**
		 * The Calculated Score for the Entry
		 */
		double score;
		/**
		 * The Number of Times the Entry is in the Document
		 */
		int count;
		/**
		 * The Filename
		 */
		String document;
		
		/**
		 * @param score is the query words score
		 * @param count is the query words count
		 * @param document is the query's filename
		 */
		public Entry (double score, int count, String document)
		{
			this.score = score;
			this.count = count;
			this.document = document;
		}

		@Override
		// @TODO this should be Entry input parameter
		public int compareTo(Object o) 
		{
			// Cast Object to Entry
			// @TODO remove casting! this is unsafe! If you pass in a non-Entry, this all breaks at runtime
			Entry e = (Entry) o;
			
			if (score > e.getScore())
			{
				return -1;
			}
			else if (score < e.getScore())
			{
				return 1;
			}
			else if (count > e.getCount())
			{
				return -1;
			}
			else if (count < e.getCount())
			{
				return 1;
			}
		
			
			return document.compareTo(e.getDocument());
		}
		
		/**
		 * @return the count
		 */
		public int getCount() 
		{
			return count;
		}

		/**
		 * @return the score
		 */
		public double getScore() 
		{
			return score;
		}

		/**
		 * @return the document
		 */
		public String getDocument() 
		{
			return document;
		}

		// @TODO: toMap()
		// Create the {
		//	"count": ,
		//	"score": ,
		//
		// } that can be passed into PrettyJsonWriter
	}
	
	/**
	 * Writes the queries as a pretty JSON object with nested arrays.
	 * 
	 * @param query_calculation is a TreeMap that stores a mapping of queries to documents and its counts
	 * @param word_count is a mapping of words and their total count based on a specific document
	 * @param writer the writer to use
	 * @param indent the initial indent level; the first bracket is not indented,
	 *   inner elements are indented by one, and the last bracket is indented at
	 *   the initial indentation level
	 * @throws IOException if an IO error occurs
	 */
	// @TODO
	public static void writeNestedArrays(
			TreeMap<String, TreeMap<String, Integer>> query_calculation, Map<String, Integer> word_count,
			Writer writer, int indent) throws IOException {

		writer.flush();
		int length = query_calculation.size();
		int index = 0;

		if (query_calculation.isEmpty())
		{
			writer.write("{\n");
			writeIndent("}", writer, indent);
		}
		else
		{
			writeIndent("{\n", writer, 0);

			// Loop Through all the Queries
			for (String query: query_calculation.keySet())
			{
				// Format Query
				writeQuote(query, writer, indent + 1);
				writer.write(": ");
				writeIndent("[\n", writer, 0);
				
				// @TODO: overall there's much too much search index-specific calculations going on here.
				// PrettyJSONWriter should *only* be writing JSON, not computing anything related to the search index
				// I'd suggest refactoring this so that you do the calculations somewhere else,
				// create a datastructure you want print and then use PrettyJSONWriter to just print it

				TreeMap<String, Integer> docs = query_calculation.get(query);
				
				int inner_map_length = docs.size();
				int inner_idx = 0;
				
				
				TreeSet<Entry> scores = new TreeSet<Entry>();
				
				// Loop Through all the Documents of that Specific Queries
				for (String document : docs.keySet())
				{
					// Count of Current Queries
					int cur_count = query_calculation.get(query).get(document);
					double score = (double) cur_count / word_count.get(document);
					String formatted_score = String.format("%.8f", score);
					
					Entry cur_queries = new Entry(score, cur_count, document);
					scores.add(cur_queries);
				}
				
				for (Entry score : scores)
				{
					// Count
					writeIndent("{\n", writer, indent + 2);
					writeQuote("count", writer, indent + 3);
					writer.write(": ");
					writer.write(score.getCount() + ",\n");
					
					// Score
					writeQuote("score", writer, indent + 3);
					writer.write(": ");
					writer.write(String.format("%.8f", score.getScore()) + ",\n");
					
					// Where
					writeQuote("where", writer, indent + 3);
					writer.write(": ");
					writeQuote(score.getDocument(), writer, 0);
					writer.write("\n");
					
					
					writeIndent("}", writer, indent + 2);
					
					// Add a Comma Except for the Last Element
					if (inner_idx != inner_map_length - 1)
					{
						writeIndent(",\n", writer, 0);
					}
					else
					{
						writeIndent("\n", writer, 0);
					}
					
					inner_idx += 1;
				}
				
				
				// Add a Comma Except for the Last Element
				if (index != length - 1)
				{
					writeIndent("],\n", writer, 1);
				}
				else
				{
					writeIndent("]", writer, 1);
				}
				
				index += 1;
			}

			writer.write("\n");
			writeIndent("}", writer, indent);
			writer.write("\n");
		}
	}

	/**
	 * Writes the elements as a pretty JSON object with nested arrays to file.
	 *
	 * @param elements the elements to write
	 * @param path the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 * @see Files#newBufferedReader(Path, java.nio.charset.Charset)
	 * @see StandardCharsets#UTF_8
	 * @see #writeNestedArrays(Map, Writer, int)
	 */
//	public static void writeNestedArrays(
//			Map<String, ? extends Collection<? extends Number>> elements, Path path)
//			throws IOException {
//		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
//			writeNestedArrays(elements, writer, 0);
//		}
//	}

	/**
	 * Returns the elements as a pretty JSON object with nested arrays.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 *
	 * @see StringWriter
	 * @see #writeNestedArrays(Map, Writer, int)
	 */
//	public static String writeNestedArrays(
//			Map<String, ? extends Collection<? extends Number>> elements) {
//		try {
//			StringWriter writer = new StringWriter();
//			writeNestedArrays(elements, writer, 0);
//			return writer.toString();
//		}
//		catch (IOException e) {
//			return null;
//		}
//	}

	/**
	 * Writes the elements as a pretty JSON array with nested objects. The generic
	 * notation used allows this method to be used for any type of collection with
	 * any type of nested map of String keys to number objects.
	 *
	 * @param elements the elements to write
	 * @param writer the writer to use
	 * @param indent the initial indent level; the first bracket is not indented,
	 *   inner elements are indented by one, and the last bracket is indented at
	 *   the initial indentation level
	 * @throws IOException if an IO error occurs
	 *
	 * @see Writer#write(String)
	 * @see #writeIndent(Writer, int)
	 * @see #writeIndent(String, Writer, int)
	 * @see #writeObject(Map)
	 */
	public static void writeNestedObjects(
			Collection<? extends Map<String, ? extends Number>> elements,
			Writer writer, int indent) throws IOException {
		System.out.println("Elements: " + elements);


		int index = 0;
		int length = elements.size();
		System.out.println("Elements Length: " + length);

		if (elements.isEmpty())
		{
			writeIndent("[", writer, indent);
			writer.write("\n");
			writeIndent("]",writer, indent);
		}
		else
		{
			writeIndent(writer, indent);

			writeIndent("[", writer, indent);
			writer.write("\n");
			writeIndent(writer,1);

			for (Map<String, ? extends Number> map: elements)
			{
				String check = writeObject(map);
				writeObject(map, writer, indent + 1);
				System.out.println("This is the object: " + check);

				// Add a Comma Except for the Last Element
				if (index != length - 1)
				{
					writer.write(",\n");
					writeIndent(writer, 1);
				}

				index += 1;
			}

			writer.write("\n");
			writeIndent("]",writer, indent);
		}
	}

	/**
	 * Writes the elements as a pretty JSON array with nested objects to file.
	 *
	 * @param elements the elements to write
	 * @param path the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 * @see Files#newBufferedReader(Path, java.nio.charset.Charset)
	 * @see StandardCharsets#UTF_8
	 * @see #writeNestedObjects(Collection)
	 */
	public static void writeNestedObjects(
			Collection<? extends Map<String, ? extends Number>> elements, Path path)
			throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			writeNestedObjects(elements, writer, 0);
		}
	}

	/**
	 * Returns the elements as a pretty JSON array with nested objects.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 *
	 * @see StringWriter
	 * @see #writeNestedObjects(Collection)
	 */
	public static String writeNestedObjects(
			Collection<? extends Map<String, ? extends Number>> elements) {
		try {
			StringWriter writer = new StringWriter();
			writeNestedObjects(elements, writer, 0);
			return writer.toString();
		}
		catch (IOException e) {
			return null;
		}
	}
}
