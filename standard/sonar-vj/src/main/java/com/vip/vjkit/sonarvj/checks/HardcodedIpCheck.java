package com.vip.vjkit.sonarvj.checks;

import com.google.common.base.Splitter;

import org.sonar.check.Rule;
import org.sonar.java.model.LiteralUtils;
import org.sonar.plugins.java.api.JavaFileScanner;
import org.sonar.plugins.java.api.JavaFileScannerContext;
import org.sonar.plugins.java.api.tree.BaseTreeVisitor;
import org.sonar.plugins.java.api.tree.LiteralTree;
import org.sonar.plugins.java.api.tree.Tree;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * 忽略127.0.0.1
 * 
 * https://github.com/SonarSource/sonar-java/blob/master/java-checks/src/main/java/org/sonar/java/checks/HardcodedIpCheck.java
 * 
 * 0d54578  Jan 8, 2018
 */
@Rule(key = "S1313")
public class HardcodedIpCheck extends BaseTreeVisitor implements JavaFileScanner {

	private static final Matcher IP = Pattern
			.compile("([^\\d.]*\\/)?(?<ip>(?:\\d{1,3}\\.){3}\\d{1,3}(?!\\d|\\.))(\\/.*)?").matcher("");

	private JavaFileScannerContext context;

	@Override
	public void scanFile(final JavaFileScannerContext context) {
		this.context = context;
		scan(context.getTree());
	}

	@Override
	public void visitLiteral(LiteralTree tree) {
		if (tree.is(Tree.Kind.STRING_LITERAL)) {
			String value = LiteralUtils.trimQuotes(tree.value());
			IP.reset(value);
			if (IP.matches()) {
				String ip = IP.group("ip");

				// VJ:ADD 忽略127.0.0.1
				if ("127.0.0.1".equals(ip)) {
					return;
				}
				// VJ:END
				if (areAllBelow256(Splitter.on('.').split(ip))) {
					context.reportIssue(this, tree, "Make this IP \"" + ip + "\" address configurable.");
				}
			}
		}
	}

	private static boolean areAllBelow256(Iterable<String> numbersAsStrings) {
		for (String numberAsString : numbersAsStrings) {
			if (Integer.valueOf(numberAsString) > 255) {
				return false;
			}
		}
		return true;
	}

}
