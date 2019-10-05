// Copyright (c) 2003-present, Jodd Team (http://jodd.org)
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
//
// 1. Redistributions of source code must retain the above copyright notice,
// this list of conditions and the following disclaimer.
//
// 2. Redistributions in binary form must reproduce the above copyright
// notice, this list of conditions and the following disclaimer in the
// documentation and/or other materials provided with the distribution.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
// AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
// ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
// LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
// CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
// SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
// INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
// CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
// POSSIBILITY OF SUCH DAMAGE.

package com.vip.vjtools.vjkit.text;

import java.util.List;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;

/**
 * 
 * 从Jodd移植，匹配以通配符比较字符串（比正则表达式简单），以及Ant Path风格如比较目录Path
 * 
 * https://github.com/oblac/jodd/blob/master/jodd-core/src/main/java/jodd/util/Wildcard.java
 * 
 * Checks whether a string or path matches a given wildcard pattern. Possible patterns allow to match single characters
 * ('?') or any count of characters ('*'). Wildcard characters can be escaped (by an '\'). When matching path, deep tree
 * wildcard also can be used ('**').
 * <p>
 * This method uses recursive matching, as in linux or windows. regexp works the same. This method is very fast,
 * comparing to similar implementations.
 */
public class WildcardMatcher {

	/**
	 * Checks whether a string matches a given wildcard pattern.
	 *
	 * @param string input string
	 * @param pattern pattern to match
	 * @return <code>true</code> if string matches the pattern, otherwise <code>false</code>
	 */
	public static boolean match(CharSequence string, CharSequence pattern) {
		return match(string, pattern, 0, 0);
	}

	/**
	 * Internal matching recursive function.
	 */
	private static boolean match(CharSequence string, CharSequence pattern, final int sNdxConst, final int pNdxConst) {

		int pLen = pattern.length();
		if (pLen == 1) {
			if (pattern.charAt(0) == '*') { // speed-up
				return true;
			}
		}
		int sLen = string.length();
		boolean nextIsNotWildcard = false;

		int sNdx = sNdxConst;
		int pNdx = pNdxConst;
		while (true) {

			// check if end of string and/or pattern occurred
			if ((sNdx >= sLen)) { // end of string still may have pending '*' in pattern
				while ((pNdx < pLen) && (pattern.charAt(pNdx) == '*')) {
					pNdx++;
				}
				return pNdx >= pLen;
			}
			if (pNdx >= pLen) { // end of pattern, but not end of the string
				return false;
			}
			char p = pattern.charAt(pNdx); // pattern char

			// perform logic
			if (!nextIsNotWildcard) {

				if (p == '\\') {
					pNdx++;
					nextIsNotWildcard = true;
					continue;
				}
				if (p == '?') {
					sNdx++;
					pNdx++;
					continue;
				}
				if (p == '*') {
					char pNext = 0; // next pattern char
					if (pNdx + 1 < pLen) {
						pNext = pattern.charAt(pNdx + 1);
					}
					if (pNext == '*') { // double '*' have the same effect as one '*'
						pNdx++;
						continue;
					}
					int i;
					pNdx++;

					// find recursively if there is any substring from the end of the
					// line that matches the rest of the pattern !!!
					for (i = string.length(); i >= sNdx; i--) {
						if (match(string, pattern, i, pNdx)) {
							return true;
						}
					}
					return false;
				}
			} else {
				nextIsNotWildcard = false;
			}

			// check if pattern char and string char are equals
			if (p != string.charAt(sNdx)) {
				return false;
			}

			// everything matches for now, continue
			sNdx++;
			pNdx++;
		}
	}

	// ---------------------------------------------------------------- utilities

	/**
	 * Matches string to at least one pattern. Returns index of matched pattern, or <code>-1</code> otherwise.
	 * @see #match(CharSequence, CharSequence)
	 */
	public static int matchOne(String src, String... patterns) {
		for (int i = 0; i < patterns.length; i++) {
			if (match(src, patterns[i])) {
				return i;
			}
		}
		return -1;
	}

	// ---------------------------------------------------------------- path

	protected static final String PATH_MATCH = "**";
	protected static final Splitter PATH_SPLITTER = Splitter.on(CharMatcher.anyOf("/\\"));

	/**
	 * Matches path to at least one pattern. Returns index of matched pattern or <code>-1</code> otherwise.
	 * @see #matchPath
	 */
	public static int matchPathOne(String platformDependentPath, String... patterns) {
		for (int i = 0; i < patterns.length; i++) {
			if (matchPath(platformDependentPath, patterns[i])) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Matches path against pattern using *, ? and ** wildcards. Both path and the pattern are tokenized on path
	 * separators (both \ and /). '**' represents deep tree wildcard, as in Ant. The separator should match the
	 * corresponding path
	 */
	public static boolean matchPath(String path, String pattern) {
		List<String> pathElements = PATH_SPLITTER.splitToList(path);
		List<String> patternElements = PATH_SPLITTER.splitToList(pattern);
		return matchTokens(pathElements.toArray(new String[0]), patternElements.toArray(new String[0]));
	}

	/**
	 * Match tokenized string and pattern.
	 */
	protected static boolean matchTokens(String[] tokens, String[] patterns) {
		int patNdxStart = 0;
		int patNdxEnd = patterns.length - 1;
		int tokNdxStart = 0;
		int tokNdxEnd = tokens.length - 1;

		while ((patNdxStart <= patNdxEnd) && (tokNdxStart <= tokNdxEnd)) { // find first **
			String patDir = patterns[patNdxStart];
			if (patDir.equals(PATH_MATCH)) {
				break;
			}
			if (!match(tokens[tokNdxStart], patDir)) {
				return false;
			}
			patNdxStart++;
			tokNdxStart++;
		}
		if (tokNdxStart > tokNdxEnd) {
			for (int i = patNdxStart; i <= patNdxEnd; i++) { // string is finished
				if (!patterns[i].equals(PATH_MATCH)) {
					return false;
				}
			}
			return true;
		}
		if (patNdxStart > patNdxEnd) {
			return false; // string is not finished, but pattern is
		}

		while ((patNdxStart <= patNdxEnd) && (tokNdxStart <= tokNdxEnd)) { // to the last **
			String patDir = patterns[patNdxEnd];
			if (patDir.equals(PATH_MATCH)) {
				break;
			}
			if (!match(tokens[tokNdxEnd], patDir)) {
				return false;
			}
			patNdxEnd--;
			tokNdxEnd--;
		}
		if (tokNdxStart > tokNdxEnd) {
			for (int i = patNdxStart; i <= patNdxEnd; i++) { // string is finished
				if (!patterns[i].equals(PATH_MATCH)) {
					return false;
				}
			}
			return true;
		}

		while ((patNdxStart != patNdxEnd) && (tokNdxStart <= tokNdxEnd)) {
			int patIdxTmp = -1;
			for (int i = patNdxStart + 1; i <= patNdxEnd; i++) {
				if (patterns[i].equals(PATH_MATCH)) {
					patIdxTmp = i;
					break;
				}
			}
			if (patIdxTmp == patNdxStart + 1) {
				patNdxStart++; // skip **/** situation
				continue;
			}
			// find the pattern between padIdxStart & padIdxTmp in str between strIdxStart & strIdxEnd
			int patLength = (patIdxTmp - patNdxStart - 1);
			int strLength = (tokNdxEnd - tokNdxStart + 1);
			int ndx = -1;
			strLoop: for (int i = 0; i <= strLength - patLength; i++) {
				for (int j = 0; j < patLength; j++) {
					String subPat = patterns[patNdxStart + j + 1];
					String subStr = tokens[tokNdxStart + i + j];
					if (!match(subStr, subPat)) {
						continue strLoop;
					}
				}

				ndx = tokNdxStart + i;
				break;
				// this is a double-loop, cannot be refactor to break statement directly
			}

			if (ndx == -1) {
				return false;
			}

			patNdxStart = patIdxTmp;
			tokNdxStart = ndx + patLength;
		}

		for (int i = patNdxStart; i <= patNdxEnd; i++) {
			if (!patterns[i].equals(PATH_MATCH)) {
				return false;
			}
		}

		return true;
	}
}
