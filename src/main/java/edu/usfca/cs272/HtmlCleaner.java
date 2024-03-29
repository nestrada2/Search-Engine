package edu.usfca.cs272;

import java.util.regex.Pattern;

import org.apache.commons.text.StringEscapeUtils;

/**
 * Cleans simple, validating HTML 4/5 into plain text. For simplicity, this
 * class cleans already validating HTML, it does not validate the HTML itself.
 * For example, the {@link #stripEntities(String)} method removes HTML entities
 * but does not check that the removed entity was valid.
 *
 * <p>
 * Look at the "See Also" section for useful classes and methods for
 * implementing this class.
 *
 * @see String#replaceAll(String, String)
 * @see Pattern#DOTALL
 * @see Pattern#CASE_INSENSITIVE
 * @see StringEscapeUtils#unescapeHtml4(String)
 *
 * @author Nino Estrada
 * @author CS 272 Software Development (University of San Francisco)
 * @version Fall 2022
 */
public class HtmlCleaner
{
	/**
	 * Replaces all HTML tags with an empty string. For example, the html
	 * {@code A<b>B</b>C} will become {@code ABC}.
	 *
	 * <p>
	 * <em>(View this comment as HTML in the "Javadoc" view in Eclipse.)</em>
	 *
	 * @param html text including HTML tags to remove
	 * @return text without any HTML tags
	 *
	 * @see String#replaceAll(String, String)
	 */
	public static String stripTags(String html)
	{
		// (?s): Java way of putting flags including new line
		// literal: <
		// ^ anything but <> sign
		// literal: >
		String regex = "(?s)\\<[^><]+\\>";
		return html.replaceAll(regex, "");
	}

	/**
	 * Replaces all HTML 4 entities with their Unicode character equivalent or, if
	 * unrecognized, replaces the entity code with an empty string. For example:,
	 * {@code 2010&ndash;2012} will become {@code 2010–2012} and
	 * {@code &gt;&dash;x} will become {@code >x} with the unrecognized
	 * {@code &dash;} entity getting removed. (The {@code &dash;} entity is valid
	 * HTML 5, but not HTML 4 which this code uses.) Should also work for entities
	 * that use decimal syntax like {@code &#8211;} or {@code &#x2013}.
	 *
	 * <p>
	 * <em>(View this comment as HTML in the "Javadoc" view in Eclipse.)</em>
	 *
	 * @see StringEscapeUtils#unescapeHtml4(String)
	 * @see String#replaceAll(String, String)
	 *
	 * @param html text including HTML entities to remove
	 * @return text with all HTML entities converted or removed
	 */
	public static String stripEntities(String html)
	{
		String entity_replace = StringEscapeUtils.unescapeHtml4(html);

		// [] - Character Class - matches any one character that is defined in it
		// + matches multiple - 1 or more times
		String regex = "&[a-zA-Z]+;";
		return entity_replace.replaceAll(regex, "");
	}

	/**
	 * Replaces all HTML comments with an empty string. For example:
	 *
	 * <pre>
	 * A&lt;!-- B --&gt;C
	 * </pre>
	 *
	 * ...and this HTML:
	 *
	 * <pre>
	 * A&lt;!--
	 * B --&gt;C
	 * </pre>
	 *
	 * ...will both become "AC" after stripping comments.
	 *
	 * <p>
	 * <em>(View this comment as HTML in the "Javadoc" view in Eclipse.)</em>
	 *
	 * @param html text including HTML comments to remove
	 * @return text without any HTML comments
	 *
	 * @see String#replaceAll(String, String)
	 */
	public static String stripComments(String html)
	{
		// (?s): Java way of putting flags including new line
		// literal: <!-- -->
		// . can be anything
		// * 0 or more times
		// ? 1 or more times
		// *? means do not be greedy
		String comments = "(?s)\\<!--.*?--\\>";
		return html.replaceAll(comments, "");
	}

	/**
	 * Replaces everything between the element tags and the element tags
	 * themselves with an empty string. For example, consider the html code:
	 *
	 * <pre>
	 * &lt;style type="text/css"&gt;body { font-size: 10pt; }&lt;/style&gt;
	 * </pre>
	 *
	 * If removing the "style" element, all of the above code will be removed, and
	 * replaced with an empty string.
	 *
	 * <p>
	 * <em>(View this comment as HTML in the "Javadoc" view in Eclipse.)</em>
	 *
	 * @param html text including HTML elements to remove
	 * @param name name of the HTML element (like "style" or "script")
	 * @return text without that HTML element
	 *
	 * @see String#formatted(Object...)
	 * @see String#format(String, Object...)
	 * @see String#replaceAll(String, String)
	 */
	public static String stripElement(String html, String name)
	{
		// (?s): Java way of putting flags including new line, case-insensitive
		// literal < , white space 0 or more times, name
		// white space 1 or more times, follow by anything 0 or more times but not be greedy, ? afterwards means optional
		// literal >
		String element = "(?si)\\<\\s*" + name + "(\\s+.*?)?\\>.*?\\</\\s*" + name + "\\s*\\>";
		return html.replaceAll(element, "");
	}

	/**
	 * Removes comments and certain block elements from the provided html. The
	 * block elements removed include: head, style, script, noscript, iframe, and
	 * svg.
	 *
	 * @param html the HTML to strip comments and block elements from
	 * @return text clean of any comments and certain HTML block elements
	 */
	public static String stripBlockElements(String html)
	{
		html = stripComments(html);
		html = stripElement(html, "head");
		html = stripElement(html, "style");
		html = stripElement(html, "script");
		html = stripElement(html, "noscript");
		html = stripElement(html, "iframe");
		html = stripElement(html, "svg");
		return html;
	}

	/**
	 * Removes all HTML tags and certain block elements from the provided text.
	 *
	 * @see #stripBlockElements(String)
	 * @see #stripTags(String)
	 *
	 * @param html the HTML to strip tags and elements from
	 * @return text clean of any HTML tags and certain block elements
	 */
	public static String stripHtml(String html)
	{
		html = stripBlockElements(html);
		html = stripTags(html);
		html = stripEntities(html);
		return html;
	}
}

/*
References
Regex Flags - https://docs.oracle.com/javase/tutorial/essential/regex/pattern.html
Test Regex - https://regex101.com/
Regex Cheat Sheet - https://developer.mozilla.org/en-US/docs/Web/JavaScript/Guide/Regular_Expressions/Cheatsheet
 */

// Note. I received help from tutors in the CS Tutoring Center.
