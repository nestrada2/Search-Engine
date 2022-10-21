package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;


/**
 * Utility class for listing and searching through all the source files
 * 
 * @author Nino Estrada
 * @author CS 272 Software Development (University of San Francisco)
 * @version Fall 2022
 *
 */
public class FileFinder 
{
	/**
	 * List All the Source Files under the given path
	 * 
	 * @param path a text file or a directory
	 * @param depth how deep within the directory tree
	 * @return a list to store all source files
	 * @throws NoSuchFileException is the file does not exist
	 * @throws IOException if the attributes of the file cannot be read
	 */
	public static List<Path> listSourceFiles(Path path, int depth) throws NoSuchFileException, IOException
	{
		// ArrayList to Store all the Paths
		List<Path> path_list = new ArrayList<>();
		
		
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
				// Check if File is a Text File or Directory
				if (isTextFile(current_path) || isDirectory(current_path))
				{
					// Recursive Call "listSourceFiles", Go Deeper in the Directory, Thus Add 1 to the Depth
					List<Path> sub_list = listSourceFiles(current_path, depth + 1);
					
					// Add the List of the Source Files in the Sub Directories to the List 
					path_list.addAll(sub_list);
				}
			}
		}
		else
		{
			// Add File to the List
			path_list.add(path);
		}
	
		return path_list;
	}
	
	/**
	 * Checks if the path is a directory
	 * 
	 * @param path is the current given path
	 * @return true if the path provided is a directory
	 * @throws IOException if the attributes of the path cannot be read
	 */
	public static boolean isDirectory(Path path) throws IOException
	{
		// File Attributes - Variables/Methods
		BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
		
		// Returns True if the File is a Directory
		return attrs.isDirectory();
	}
	
	/**
	 * Checks if the path is a text file
	 * 
	 * @param path is the current given file
	 * @return true if the path provided is a text file
	 */
	public static boolean isTextFile(Path path)
	{
		// Format File/Document
		String file = path.toString().toLowerCase();
		
		// If the Text Ends with "text" or "txt" it's a Text File 
		return file.endsWith("text") || file.endsWith("txt");
	}
}

/*
 * References
 * https://docs.oracle.com/javase/8/docs/api/java/nio/file/attribute/BasicFileAttributes.html
 * https://docs.oracle.com/javase/7/docs/api/java/nio/file/DirectoryStream.html
 */