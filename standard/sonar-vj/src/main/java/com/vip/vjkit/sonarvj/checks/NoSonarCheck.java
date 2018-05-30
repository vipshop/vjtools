/*
 * SonarQube Java Copyright (C) 2012-2018 SonarSource SA mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General
 * Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this program; if not, write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package com.vip.vjkit.sonarvj.checks;

import java.util.Collections;
import java.util.List;

import org.sonar.check.Rule;
import org.sonar.java.RspecKey;
import org.sonar.plugins.java.api.IssuableSubscriptionVisitor;
import org.sonar.plugins.java.api.JavaFileScannerContext;
import org.sonar.plugins.java.api.tree.Tree;

/**
 * 忽略在system.out, e.printStacktrace, catch exception 后设置的NONSAR
 * 
 * 基于TooLongLineCheck完全重写
 */
@Rule(key = "NoSonar")
@RspecKey("S1291")
public class NoSonarCheck extends IssuableSubscriptionVisitor {
	private static final String PATTERN = "NOSONAR";
	private static final String[] IGNORE_PATTERNS = new String[] { "Exception", "Throwable", "System.out", "System.err",
			"printStackTrace" };
	private static final String MESSAGE = "//NOSONAR found: ";

	@Override
	public List<Tree.Kind> nodesToVisit() {
		return Collections.emptyList();
	}

	@Override
	public void scanFile(JavaFileScannerContext context) {
		super.context = context;
		super.scanFile(context);
		visitFile();
	}

	private void visitFile() {
		List<String> lines = context.getFileLines();
		for (int i = 0; i < lines.size(); i++) {
			String line = lines.get(i);
			if (line.contains(PATTERN) && !ignoredLine(line)) {
				addIssue(i + 1, MESSAGE + line);
			}
		}
	}

	private boolean ignoredLine(String line) {
		for (String ignorePattern : IGNORE_PATTERNS) {
			if (line.contains(ignorePattern)) {
				return true;
			}
		}
		return false;
	}
}
