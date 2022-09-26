package edu.usfca.cs272;

import java.nio.file.Path;
import java.util.HashMap;

/**
 * Parses and stores command-line arguments into simple flag/value pairs.
 *
 * @author CS 272 Software Development (University of San Francisco)
 * @version Fall 2022
 * Nino Estrada
 */
public class ArgumentParser
{
	/**
	 * Stores command-line arguments in flag/value pairs.
	 */
	private final HashMap<String, String> map;

	/**
	 * Initializes this argument map.
	 */
	public ArgumentParser()
	{
		this.map = new HashMap<>();
	}

	/**
	 * Initializes this argument map and then parsers the arguments into
	 * flag/value pairs where possible. Some flags may not have associated values.
	 * If a flag is repeated, its value is overwritten.
	 *
	 * @param args the command line arguments to parse
	 */
	public ArgumentParser(String[] args)
	{
		this();
		parse(args);
	}

	/**
	 * Determines whether the argument is a flag. The argument is considered a
	 * flag if it is a dash "-" character followed by any character that is not a
	 * digit or whitespace. For example, "-hello" and "-@world" are considered
	 * flags, but "-10" and "- hello" are not.
	 *
	 * @param arg the argument to test if its a flag
	 * @return {@code true} if the argument is a flag
	 *
	 * @see String#startsWith(String)
	 * @see String#length()
	 * @see String#codePointAt(int)
	 * @see Character#isDigit(int)
	 * @see Character#isWhitespace(int)
	 */
	public static boolean isFlag(String arg)
	{
		if (arg != null && arg.length() >= 2)
		{
			char first_char = arg.charAt(0);
			char second_char = arg.charAt(1);

			// Check if String is either a # or " "
			boolean is_digit = Character.isDigit(second_char);
			boolean is_whitespace = Character.isWhitespace(second_char);

			// String Starts with "-" and the sec. letter is a # or " " Flag it
			if (first_char == '-' && !is_digit && !is_whitespace)
			{
				return true;
			}
		}

		return false;
	}

	/**
	 * Determines whether the argument is a value. Anything that is not a flag is
	 * considered a value.
	 *
	 * @param arg the argument to test if its a value
	 * @return {@code true} if the argument is a value
	 */
	public static boolean isValue(String arg)
	{
		return !isFlag(arg);
	}

	/**
	 * Parses the arguments into flag/value pairs where possible. Some flags may
	 * not have associated values. If a flag is repeated, its value will be
	 * overwritten.
	 *
	 * @param args the command line arguments to parse
	 */
	public void parse(String[] args)
	{
		for (int i = 0; i < args.length; i += 1)
		{
			if (isFlag(args[i]) && i + 1 < args.length && isValue(args[i + 1]))
			{
				map.put(args[i], args[i + 1]);
				i += 1;
			}
			else
			{
				if (isFlag(args[i]))
				{
					map.put(args[i], null);
				}
			}
		}
	}

	/**
	 * Returns the number of unique flags.
	 *
	 * @return number of unique flags
	 */
	public int numFlags()
	{
		// Hashmap Key's are Unique
		return map.size();
	}

	/**
	 * Determines whether the specified flag exists.
	 *
	 * @param flag the flag check
	 * @return {@code true} if the flag exists
	 */
	public boolean hasFlag(String flag)
	{
		boolean flag_exist = map.containsKey(flag);

		return flag_exist;
	}

	/**
	 * Determines whether the specified flag is mapped to a non-null value.
	 *
	 * @param flag the flag to find
	 * @return {@code true} if the flag is mapped to a non-null value
	 */
	public boolean hasValue(String flag)
	{
		boolean not_null = true;

		// map.get(key) Returns the Value of that Key
		if (map.get(flag) == null)
		{
			not_null = false;
		}

		return not_null;
	}

	/**
	 * Returns the value to which the specified flag is mapped as a {@link String}
	 * or the backup value if there is no mapping.
	 *
	 * @param flag the flag whose associated value is to be returned
	 * @param backup the backup value to return if there is no mapping
	 * @return the value to which the specified flag is mapped, or the backup
	 *   value if there is no mapping
	 */
	public String getString(String flag, String backup)
	{
		// If Key is not Mapped Map it to Backup String
		if (map.get(flag) == null)
		{
			map.put(flag, backup);
		}

		// Return the Key's Value
		return map.get(flag);
	}

	/**
	 * Returns the value to which the specified flag is mapped as a {@link String}
	 * or null if there is no mapping.
	 *
	 * @param flag the flag whose associated value is to be returned
	 * @return the value to which the specified flag is mapped or {@code null} if
	 *   there is no mapping
	 */
	public String getString(String flag)
	{
		// Return Null if the Key is not Mapped
		if (map.get(flag) == null)
		{
			return null;
		}

		// Return the Value of the Specify Key
		return map.get(flag);
	}

	/**
	 * Returns the value the specified flag is mapped as a {@link Path}, or the
	 * backup value if unable to retrieve this mapping (including being unable to
	 * convert the value to a {@link Path} or if no value exists).
	 *
	 * This method should not throw any exceptions!
	 *
	 * @param flag the flag whose associated value will be returned
	 * @param backup the backup value to return if there is no valid mapping
	 * @return the value the specified flag is mapped as a {@link Path}, or the
	 *   backup value if there is no valid mapping
	 *
	 * @see Path#of(String, String...)
	 */
	public Path getPath(String flag, Path backup)
	{
		if (map.get(flag) != null)
		{
			Path path = Path.of(map.get(flag));
			return path;
		}
		else
		{
			return backup;
		}
	}

	/**
	 * Returns the value to which the specified flag is mapped as a {@link Path},
	 * or {@code null} if unable to retrieve this mapping (including being unable
	 * to convert the value to a {@link Path} or no value exists).
	 *
	 * This method should not throw any exceptions!
	 *
	 * @param flag the flag whose associated value is to be returned
	 * @return the value to which the specified flag is mapped, or {@code null} if
	 *   unable to retrieve this mapping
	 *
	 * @see #getPath(String, Path)
	 */
	public Path getPath(String flag)
	{
		if (map.get(flag) != null)
		{
			Path path = Path.of(map.get(flag));
			return path;
		}
		else
		{
			return null;
		}
	}

	/**
	 * Returns the value the specified flag is mapped as an int value, or the
	 * backup value if unable to retrieve this mapping (including being unable to
	 * convert the value to an int or if no value exists).
	 *
	 * @param flag the flag whose associated value will be returned
	 * @param backup the backup value to return if there is no valid mapping
	 * @return the value the specified flag is mapped as an int, or the backup
	 *   value if there is no valid mapping
	 *
	 * @see Integer#parseInt(String)
	 */
	public int getInteger(String flag, int backup)
	{
		try
		{
			if (map.get(flag) == null)
			{
				return backup;
			}

			int value = Integer.parseInt(map.get(flag));

			return value;
		}
		catch (NumberFormatException e)
		{
			System.out.println("String is not a number.");
		}

		return 0;
	}

	/**
	 * Returns the value the specified flag is mapped as an int value, or 0 if
	 * unable to retrieve this mapping (including being unable to convert the
	 * value to an int or if no value exists).
	 *
	 * @param flag the flag whose associated value will be returned
	 * @return the value the specified flag is mapped as an int, or 0 if there is
	 *   no valid mapping
	 *
	 * @see #getInteger(String, int)
	 */
	public int getInteger(String flag)
	{
		if (map.get(flag) == null)
		{
			return 0;
		}

		int value = Integer.parseInt(map.get(flag));

		return value;
	}

	@Override
	public String toString()
	{
		return this.map.toString();
	}

	/**
	 * Demonstrates this class.
	 *
	 * @param args the arguments to test
	 */
	public static void main(String[] args)
	{
		// Feel free to modify or delete this method for debugging
		if (args.length < 1) {
			args = new String[] {
					"-max", "false", "-min", "0", "-min", "-10", "hello", "-@debug", "-f",
					"output.txt", "-verbose" };
		}

		// expected output:
		// {-max=false, -min=-10, -verbose=null, -f=output.txt, -@debug=null}
		ArgumentParser map = new ArgumentParser(args);
		System.out.println(map);
	}
}
