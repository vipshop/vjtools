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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * 从Jodd移植
 * 
 * https://github.com/oblac/jodd/blob/master/jodd-core/src/main/java/jodd/util/CsvUtil.java
 * 
 * Helps with CSV strings. See: http://en.wikipedia.org/wiki/Comma-separated_values
 */
public class CsvUtil {

	protected static final char FIELD_SEPARATOR = ',';
	protected static final char FIELD_QUOTE = '"';
	protected static final String DOUBLE_QUOTE = "\"\"";
	protected static final String SPECIAL_CHARS = "\r\n";
	protected static final String SPACE = " ";
	protected static final String QUOTE = "\"";

	/**
	 * Parse fields as csv string,
	 */
	public static String toCsvString(Object... elements) {
		StringBuilder line = new StringBuilder();
		int last = elements.length - 1;
		for (int i = 0; i < elements.length; i++) {
			if (elements[i] == null) {
				if (i != last) {
					line.append(FIELD_SEPARATOR);
				}
				continue;
			}
			String field = elements[i].toString();

			// check for special cases
			int ndx = field.indexOf(FIELD_SEPARATOR);
			if (ndx == -1) {
				ndx = field.indexOf(FIELD_QUOTE);
			}
			if (ndx == -1 && (field.startsWith(SPACE) || field.endsWith(SPACE))) {
				ndx = 1;
			}
			if (ndx == -1) {
				ndx = StringUtils.indexOf(field, SPECIAL_CHARS);
			}

			// add field
			if (ndx != -1) {
				line.append(FIELD_QUOTE);
			}
			field = StringUtils.replace(field, QUOTE, DOUBLE_QUOTE);
			line.append(field);
			if (ndx != -1) {
				line.append(FIELD_QUOTE);
			}

			// last
			if (i != last) {
				line.append(FIELD_SEPARATOR);
			}
		}
		return line.toString();
	}

	/**
	 * Converts CSV line to string array.
	 */
	public static String[] fromCsvString(String line) {
		List<String> row = new ArrayList<String>();

		boolean inQuotedField = false;
		int fieldStart = 0;

		final int len = line.length();
		for (int i = 0; i < len; i++) {
			char c = line.charAt(i);
			if (c == FIELD_SEPARATOR) {
				if (!inQuotedField) { // ignore we are quoting
					addField(row, line, fieldStart, i, inQuotedField);
					fieldStart = i + 1;
				}
			} else if (c == FIELD_QUOTE) {
				if (inQuotedField) {
					if (i + 1 == len || line.charAt(i + 1) == FIELD_SEPARATOR) { // we are already quoting - peek to see
						// if this is the end of the field
						addField(row, line, fieldStart, i, inQuotedField);
						fieldStart = i + 2;
						i++; // and skip the comma
						inQuotedField = false;
					}
				} else if (fieldStart == i) {
					inQuotedField = true; // this is a beginning of a quote
					fieldStart++; // move field start
				}
			}
		}
		// add last field - but only if string was not empty
		if (len > 0 && fieldStart <= len) {
			addField(row, line, fieldStart, len, inQuotedField);
		}
		return row.toArray(new String[row.size()]);
	}

	private static void addField(List<String> row, String line, int startIndex, int endIndex, boolean inQuoted) {
		String field = line.substring(startIndex, endIndex);
		if (inQuoted) {
			field = StringUtils.replace(field, DOUBLE_QUOTE, "\"");
		}
		row.add(field);
	}

}
