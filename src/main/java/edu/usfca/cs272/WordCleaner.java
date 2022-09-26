package edu.usfca.cs272;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeSet;
import java.util.regex.Pattern;

import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer.ALGORITHM;

/**
 * Utility class for parsing, cleaning, and stemming text and text files into
 * collections of processed words.
 *
 * @author CS 272 Software Development (University of San Francisco)
 * @version Fall 2022
 * Nino Estrada
 * Created on Sept. 7, 2022
 */
public class WordCleaner
{
	/** Regular expression that matches any whitespace. **/
	public static final Pattern SPLIT_REGEX = Pattern.compile("(?U)\\p{Space}+");

	/** Regular expression that matches non-alphabetic characters. **/
	public static final Pattern CLEAN_REGEX = Pattern
			.compile("(?U)[^\\p{Alpha}\\p{Space}]+");

	/**
	 * Cleans the text by removing any non-alphabetic characters (e.g. non-letters
	 * like digits, punctuation, symbols, and diacritical marks like the umlaut)
	 * and converting the remaining characters to lowercase.
	 *
	 * @param text the text to clean
	 * @return cleaned text
	 */
	public static String clean(String text)
	{
		String cleaned = Normalizer.normalize(text, Normalizer.Form.NFD);
		cleaned = CLEAN_REGEX.matcher(cleaned).replaceAll("");
		return cleaned.toLowerCase();
	}

	/**
	 * Splits the supplied text by whitespaces.
	 *
	 * @param text the text to split
	 * @return an array of {@link String} objects
	 */
	public static String[] split(String text)
	{
		return text.isBlank() ? new String[0] : SPLIT_REGEX.split(text.strip());
	}

	/**
	 * Parses the text into an array of clean words.
	 *
	 * @param text the text to clean and split
	 * @return an array of {@link String} objects
	 *
	 * @see #clean(String)
	 * @see #parse(String)
	 */
	public static String[] parse(String text)
	{
		return split(clean(text));
	}

	/**
	 * Parses the line into cleaned and stemmed words and adds them to the
	 * provided collection.
	 *
	 * @param line the line of words to clean, split, and stem
	 * @param stemmer the stemmer to use
	 * @param stems the collection to add stems
	 *
	 * @see #parse(String)
	 * @see Stemmer#stem(CharSequence)
	 * @see Collection#add(Object)
	 */
	public static void addStems(String line, Stemmer stemmer, Collection<String> stems)
	{
		// parse(line) returns a String Array
		String[] parse_words = parse(line);

		// Loop through the "parse_words" Array
		for (int i = 0; i < parse_words.length; i += 1)
		{
			String word = parse_words[i];

			// Convert Word to its Root Form
			String root = (String) stemmer.stem(word);

			// Add to Collection
			stems.add(root);
		}
	}

	/**
	 * Parses the line into a list of cleaned and stemmed words.
	 *
	 * @param line the line of words to clean, split, and stem
	 * @param stemmer the stemmer to use
	 * @return a list of cleaned and stemmed words in parsed order
	 *
	 * @see #parse(String)
	 * @see Stemmer#stem(CharSequence)
	 * @see #addStems(String, Stemmer, Collection)
	 */
	public static ArrayList<String> listStems(String line, Stemmer stemmer)
	{
		ArrayList<String> clean_stem_words = new ArrayList<>();

		// Function Adds Clean and Stem Words to a Collection
		addStems(line, stemmer, clean_stem_words);

		return clean_stem_words;
	}

	/**
	 * Parses the line into a list of cleaned and stemmed words using the default
	 * stemmer for English.
	 *
	 * @param line the line of words to parse and stem
	 * @return a list of cleaned and stemmed words in parsed order
	 *
	 * @see SnowballStemmer#SnowballStemmer(ALGORITHM)
	 * @see ALGORITHM#ENGLISH
	 * @see #listStems(String, Stemmer)
	 */
	public static ArrayList<String> listStems(String line)
	{
		ArrayList<String> clean_stem_en_words;

		// Stemmer Converts Word to its Root Form in English
		SnowballStemmer stemmer = new SnowballStemmer(ALGORITHM.ENGLISH);

		// Add Clean Stem English Words to the Collection
		clean_stem_en_words = listStems(line, stemmer);

		return clean_stem_en_words;
	}

	/**
	 * Reads a file line by line, parses each line into cleaned and stemmed words
	 * using the default stemmer for English.
	 *
	 * @param input the input file to parse and stem
	 * @return a list of stems from file in parsed order
	 * @throws IOException if unable to read or parse file
	 *
	 * @see SnowballStemmer
	 * @see ALGORITHM#ENGLISH
	 * @see StandardCharsets#UTF_8
	 * @see #listStems(String, Stemmer)
	 */
	public static ArrayList<String> listStems(Path input) throws IOException
	{
		ArrayList<String> clean_stem_en_words = new ArrayList<>();

		try (BufferedReader br = Files.newBufferedReader(input, UTF_8))
		{
			String line;

			// Keep Adding "line" to "clean_stem_en_words" ArrayList
			while ((line = br.readLine()) != null)
			{
				// Stemmer Converts Word to its Root Form in English
				SnowballStemmer stemmer = new SnowballStemmer(ALGORITHM.ENGLISH);

				// Add to Collection
				clean_stem_en_words.addAll(listStems(line, stemmer)); // addAll() adds all elements from a collection
			}
		}
		catch (Exception e)
		{
			throw e;

		}

		return clean_stem_en_words;
	}

	/**
	 * Parses the line into a set of unique, sorted, cleaned, and stemmed words.
	 *
	 * @param line the line of words to parse and stem
	 * @param stemmer the stemmer to use
	 * @return a sorted set of unique cleaned and stemmed words
	 *
	 * @see #parse(String)
	 * @see Stemmer#stem(CharSequence)
	 * @see #addStems(String, Stemmer, Collection)
	 */
	public static TreeSet<String> uniqueStems(String line, Stemmer stemmer)
	{
		TreeSet<String> unique_stems = new TreeSet<>();
		String[] parse_words = parse(line);

		// Loop through the "parse_words" Array
		for (int i = 0; i < parse_words.length; i += 1)
		{
			String word = parse_words[i];

			// Add to Collection
			addStems(word, stemmer, unique_stems);
		}

		return unique_stems;
	}

	/**
	 * Parses the line into a set of unique, sorted, cleaned, and stemmed words
	 * using the default stemmer for English.
	 *
	 * @param line the line of words to parse and stem
	 * @return a sorted set of unique cleaned and stemmed words
	 *
	 * @see SnowballStemmer#SnowballStemmer(ALGORITHM)
	 * @see ALGORITHM#ENGLISH
	 * @see #uniqueStems(String, Stemmer)
	 */
	public static TreeSet<String> uniqueStems(String line)
	{
		TreeSet<String> unique_stems;

		// Stemmer Converts Word to its Root Form in English
		SnowballStemmer stemmer = new SnowballStemmer(ALGORITHM.ENGLISH);

		// Parse the Line to Unique, Sorted, Clean, and Stemmed Words using "stemmer" and Store in "unique_stems" TreeSet
		unique_stems = uniqueStems(line, stemmer);

		return unique_stems;
	}

	/**
	 * Reads a file line by line, parses each line into a set of unique, sorted,
	 * cleaned, and stemmed words using the default stemmer for English.
	 *
	 * @param input the input file to parse and stem
	 * @return a sorted set of unique cleaned and stemmed words from file
	 * @throws IOException if unable to read or parse file
	 *
	 * @see SnowballStemmer
	 * @see ALGORITHM#ENGLISH
	 * @see StandardCharsets#UTF_8
	 * @see #uniqueStems(String, Stemmer)
	 */
	public static TreeSet<String> uniqueStems(Path input) throws IOException
	{
		TreeSet<String> unique_stems = new TreeSet<>();

		try (BufferedReader br = Files.newBufferedReader(input, UTF_8))
		{
			String line;

			// Keep Adding Line to "parse" TreeSet
			while ((line = br.readLine()) != null)
			{
				// Add to Collection
				unique_stems.addAll(uniqueStems(line)); // addAll() adds all elements from a collection
			}
		}
		catch (Exception e)
		{
			throw e;

		}

		return unique_stems;
	}

	/**
	 * Reads a file line by line, parses each line into unique, sorted, cleaned,
	 * and stemmed words using the default stemmer for English, and adds the set
	 * of unique sorted stems to a list per line in the file.
	 *
	 * @param input the input file to parse and stem
	 * @return a list where each item is the sets of unique sorted stems parsed
	 *   from a single line of the input file
	 * @throws IOException if unable to read or parse file
	 *
	 * @see SnowballStemmer
	 * @see ALGORITHM#ENGLISH
	 * @see StandardCharsets#UTF_8
	 * @see #uniqueStems(String, Stemmer)
	 */
	public static ArrayList<TreeSet<String>> listUniqueStems(Path input) throws IOException
	{
		ArrayList<TreeSet<String>> list_unique_stems = new ArrayList<>();

		try (BufferedReader br = Files.newBufferedReader(input, UTF_8))
		{
			String line;

			// Keep Adding line to "unique" TreeSet and then add to "check" ArrayList
			while ((line = br.readLine()) != null)
			{
				// Add to a Collection
				TreeSet<String> unique = uniqueStems(line);

				// Add to Collection
				list_unique_stems.add(unique);
			}
		}
		catch (Exception e)
		{
			throw e;
		}

		return list_unique_stems;
	}
	
}

// References
// https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/nio/file/Files.html#newBufferedReader(java.nio.file.Path,java.nio.charset.Charset)
