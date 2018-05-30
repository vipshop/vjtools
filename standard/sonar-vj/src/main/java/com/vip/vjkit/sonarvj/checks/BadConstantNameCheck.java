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

import java.util.List;
import java.util.regex.Pattern;

import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.java.checks.serialization.SerializableContract;
import org.sonar.java.resolve.JavaType;
import org.sonar.plugins.java.api.IssuableSubscriptionVisitor;
import org.sonar.plugins.java.api.JavaFileScannerContext;
import org.sonar.plugins.java.api.semantic.Type;
import org.sonar.plugins.java.api.tree.ClassTree;
import org.sonar.plugins.java.api.tree.Modifier;
import org.sonar.plugins.java.api.tree.ModifierKeywordTree;
import org.sonar.plugins.java.api.tree.Tree;
import org.sonar.plugins.java.api.tree.VariableTree;

import com.google.common.collect.ImmutableList;

/**
 * 不对枚举的命名进行检查
 * 
 * 另顺便修正了isConstantTyp一个BUG,加强了报错信息
 * 
 * https://github.com/SonarSource/sonar-java/blob/master/java-checks/src/main/java/org/sonar/java/checks/naming/BadConstantNameCheck.java
 * 
 * 0d54578 Jan 8, 2018
 */
@Rule(key = "S115")
public class BadConstantNameCheck extends IssuableSubscriptionVisitor {

	private static final String DEFAULT_FORMAT = "^[A-Z][A-Z0-9]*(_[A-Z0-9]+)*$";
	@RuleProperty(key = "format", description = "Regular expression used to check the constant names against.", defaultValue = ""
			+ DEFAULT_FORMAT)
	public String format = DEFAULT_FORMAT;

	private Pattern pattern = null;

	@Override
	public List<Tree.Kind> nodesToVisit() {
		// VJ Replace
		return ImmutableList.of(Tree.Kind.CLASS, Tree.Kind.INTERFACE, Tree.Kind.ANNOTATION_TYPE);
		// return ImmutableList.of(Tree.Kind.CLASS, Tree.Kind.ENUM, Tree.Kind.INTERFACE, Tree.Kind.ANNOTATION_TYPE);
	}

	@Override
	public void scanFile(JavaFileScannerContext context) {
		if (pattern == null) {
			pattern = Pattern.compile(format, Pattern.DOTALL);
		}
		super.scanFile(context);
	}

	@Override
	public void visitNode(Tree tree) {
		ClassTree classTree = (ClassTree) tree;
		for (Tree member : classTree.members()) {
			if (member.is(Tree.Kind.VARIABLE) && hasSemantic()) {
				VariableTree variableTree = (VariableTree) member;
				Type symbolType = variableTree.type().symbolType();
				 if (isConstantType(symbolType) && (classTree.is(Tree.Kind.INTERFACE, Tree.Kind.ANNOTATION_TYPE) || isStaticFinal(variableTree))) {
			          checkName(variableTree);
			     }
			}
			// VJ Remove //
			// else if (member.is(Tree.Kind.ENUM_CONSTANT)) {
			// checkName((VariableTree) member);
			// }
		}
	}

	//VJ: 加上symbolType instanceof JavaType的判断，防止数组转换出错
	private static boolean isConstantType(Type symbolType) {
		return symbolType.isPrimitive() || symbolType.is("java.lang.String") ||  symbolType.is("java.lang.Byte") ||
				symbolType.is("java.lang.Character") ||
				symbolType.is("java.lang.Short") ||
				symbolType.is("java.lang.Integer") ||
				symbolType.is("java.lang.Long") ||
				symbolType.is("java.lang.Float") ||
				symbolType.is("java.lang.Double") ||
				symbolType.is("java.lang.Boolean");
	}

	private void checkName(VariableTree variableTree) {
		String name = variableTree.simpleName().name();
		if (!SerializableContract.SERIAL_VERSION_UID_FIELD.equals(name)
				&& !pattern.matcher(name).matches()) {
			//VJ 报错信息加上变量名
			reportIssue(variableTree.simpleName(),
					"Rename this constant name '" + name + "' to match the regular expression '" + format + "'.");
		}
	}

	private static boolean isStaticFinal(VariableTree variableTree) {
		boolean isStatic = false;
		boolean isFinal = false;
		for (ModifierKeywordTree modifierKeywordTree : variableTree.modifiers().modifiers()) {
			Modifier modifier = modifierKeywordTree.modifier();
			if (modifier == Modifier.STATIC) {
				isStatic = true;
			}
			if (modifier == Modifier.FINAL) {
				isFinal = true;
			}
		}
		return isStatic && isFinal;
	}

}
