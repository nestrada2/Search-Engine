package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;


/**
 * File Finder
 *
 */
public class FileFinder 
{
	/**
	 * @param path File
	 * @param depth How Deep is
	 * @param path_list ArrayList to Store all Source Files
	 * @throws Exception if no such file exists
	 */
	// List All the Source Files
	public static void listSourceFiles(Path path, int depth, List<Path> path_list) throws Exception
	{
		try 
		{
			// File Attributes - Variables/Methods
			BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
			
			// If Directory List the Files and Traverse Down Inside each of Those
			if (attrs.isDirectory())
			{
				// Iterate through the Directory - List every File in the Directory
				DirectoryStream<Path> stream = Files.newDirectoryStream(path);
								
				// For Each File Recursively calling ListDirectory
				for (Path current_path: stream)
				{
					
					if (isTextFile(current_path) || isDirectory(current_path))
					{
						// Recursive Call "listSourceFiles", Go Deeper in the Directory, Thus Add 1 to the Depth
						listSourceFiles(current_path, depth + 1, path_list);
					}
				}
			}
			else
			{
				// Add File to the List "path_list"
				path_list.add(path);
			}
		} 
		catch (NoSuchFileException e)
		{
			// This Catches if the File is Gibberish
			return;
		}
		
	}
	
	/**
	 * Checks if the path is a directory
	 * 
	 * @param path current path
	 * @return boolean true if is a directory
	 * @throws Exception if error
	 */
	// Check if the Path is a Directory
	public static boolean isDirectory(Path path) throws Exception
	{
		// File Attributes - Variables/Methods
		BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
		
		// Returns True if the File is a Directory
		return attrs.isDirectory();
	}
	
	/**
	 * Checks if the file is a text file
	 * 
	 * @param path current file
	 * @return true if the files are text files
	 * @throws IOException if error occurs
	 */
	public static boolean isTextFile(Path path) throws IOException
	{
		String file = path.toString().toLowerCase();
		
		// If the Text Ends with "text" or "txt" it's a Text File 
		return file.endsWith("text") || file.endsWith("txt");
	}
}

// References
// https://docs.oracle.com/javase/8/docs/api/java/nio/file/attribute/BasicFileAttributes.html
// https://docs.oracle.com/javase/7/docs/api/java/nio/file/DirectoryStream.html
