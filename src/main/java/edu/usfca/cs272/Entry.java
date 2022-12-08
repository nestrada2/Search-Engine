package edu.usfca.cs272;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

/**
 * Entry of Each User's Query Search Data
 *
 */
public class Entry implements Comparable<Entry>
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
	public int compareTo(Entry e) 
	{
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
	
	/**
	 * Writes an Entry as a JSON object
	 * 
	 * @return map of string to value for the entry
	 * @throws IOException if an IO error occurs
	 */
	public Map<String, Object> toMap() throws IOException
	{
		TreeMap<String, Object> map = new TreeMap<String, Object>();
		
		map.put("count", getCount());
		map.put("score", getScore());
		map.put("where", getDocument());
		
		return map;
	}
}