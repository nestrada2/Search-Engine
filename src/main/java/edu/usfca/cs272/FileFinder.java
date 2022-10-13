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
    /* TODO JavaDoc Make sure to add a description in all of your
     * Javadoc comments. Javadoc is usually all other developers use to
     * figure out how to use your code, so you don't want that part to
     * be blank!
     */

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
	 * @param path current path
	 * @return boolean true if is a directory
	 * @throws Exception if error
	 */
	// Check if the Path is a Directory
	public static boolean isDirectory(Path path) throws Exception
	{
		// File Attributes - Variables/Methods
		BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
		
		// If the File is A Directory
		if (attrs.isDirectory())
		{
			return true;
		}
		
		return false;
	}
	
	/**
	 * @param path current file
	 * @return true if the files are text files
	 * @throws IOException if error occurs
	 */
	// Check if the File is a Text File
	public static boolean isTextFile(Path path) throws IOException
	{
		// If the Text Ends with "text" or "txt" it's a Text File 

        // TODO call `path.toString().toLowerCase()` once
        // TODO return ( endsWith("text") || ...endsWith("txt") )

		if (path.toString().toLowerCase().endsWith("text") || path.toString().toLowerCase().endsWith("txt"))
		{
			return true;
		}
		
		return false;
	}
}

// References (XXX: remember, we're using Java SDK 17 in class -- feel
// free to delete after)
// https://docs.oracle.com/javase/8/docs/api/java/nio/file/attribute/BasicFileAttributes.html
// https://docs.oracle.com/javase/7/docs/api/java/nio/file/DirectoryStream.html
