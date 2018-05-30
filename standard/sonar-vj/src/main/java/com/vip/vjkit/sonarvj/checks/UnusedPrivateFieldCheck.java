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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sonar.check.Rule;
import org.sonar.java.model.ExpressionUtils;
import org.sonar.java.model.ModifiersUtils;
import org.sonar.plugins.java.api.IssuableSubscriptionVisitor;
import org.sonar.plugins.java.api.JavaFileScannerContext;
import org.sonar.plugins.java.api.semantic.Symbol;
import org.sonar.plugins.java.api.tree.AssignmentExpressionTree;
import org.sonar.plugins.java.api.tree.ClassTree;
import org.sonar.plugins.java.api.tree.ExpressionStatementTree;
import org.sonar.plugins.java.api.tree.ExpressionTree;
import org.sonar.plugins.java.api.tree.IdentifierTree;
import org.sonar.plugins.java.api.tree.ImportTree;
import org.sonar.plugins.java.api.tree.MemberSelectExpressionTree;
import org.sonar.plugins.java.api.tree.MethodInvocationTree;
import org.sonar.plugins.java.api.tree.MethodReferenceTree;
import org.sonar.plugins.java.api.tree.MethodTree;
import org.sonar.plugins.java.api.tree.Modifier;
import org.sonar.plugins.java.api.tree.Tree;
import org.sonar.plugins.java.api.tree.Tree.Kind;
import org.sonar.plugins.java.api.tree.VariableTree;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;

/**
 * 不检查lombok自动生成getter/setter的类
 * 
 * https://github.com/SonarSource/sonar-java/blob/master/java-checks/src/main/java/org/sonar/java/checks/unused/UnusedPrivateFieldCheck.java
 * 0d54578 Jan 8, 2018
 */
@Rule(key = "S1068")
public class UnusedPrivateFieldCheck extends IssuableSubscriptionVisitor {

	private static final Tree.Kind[] ASSIGNMENT_KINDS = { Tree.Kind.ASSIGNMENT, Tree.Kind.MULTIPLY_ASSIGNMENT,
			Tree.Kind.DIVIDE_ASSIGNMENT, Tree.Kind.REMAINDER_ASSIGNMENT, Tree.Kind.PLUS_ASSIGNMENT,
			Tree.Kind.MINUS_ASSIGNMENT, Tree.Kind.LEFT_SHIFT_ASSIGNMENT, Tree.Kind.RIGHT_SHIFT_ASSIGNMENT,
			Tree.Kind.UNSIGNED_RIGHT_SHIFT_ASSIGNMENT, Tree.Kind.AND_ASSIGNMENT, Tree.Kind.XOR_ASSIGNMENT,
			Tree.Kind.OR_ASSIGNMENT };

	private List<ClassTree> classes = Lists.newArrayList();
	private ListMultimap<Symbol, IdentifierTree> assignments = ArrayListMultimap.create();
	private Set<String> unknownIdentifiers = new HashSet<>();
	private boolean hasNativeMethod = false;
	private boolean lombokClass = false;

	@Override
	public List<Kind> nodesToVisit() {
		return ImmutableList.of(Tree.Kind.IMPORT, Tree.Kind.CLASS, Tree.Kind.METHOD, Tree.Kind.EXPRESSION_STATEMENT,
				Tree.Kind.IDENTIFIER);
	}

	@Override
	public void scanFile(JavaFileScannerContext context) {
		super.scanFile(context);
		if (!hasNativeMethod && !lombokClass) {
			classes.forEach(this::checkClassFields);
		}
		classes.clear();
		assignments.clear();
		unknownIdentifiers.clear();
		hasNativeMethod = false;
		lombokClass = false;
	}

	@Override
	public void visitNode(Tree tree) {
		if (!hasSemantic()) {
			return;
		}
		switch (tree.kind()) {
		case METHOD:
			checkIfNativeMethod((MethodTree) tree);
			break;
		case CLASS:
			classes.add((ClassTree) tree);
			break;
		case IMPORT:// VJ
			checkIfLombokClass((ImportTree) tree);
			break;
		case EXPRESSION_STATEMENT:
			collectAssignment(((ExpressionStatementTree) tree).expression());
			break;
		case IDENTIFIER:
			collectUnknownIdentifier((IdentifierTree) tree);
			break;
		default:
			throw new IllegalStateException("Unexpected subscribed tree.");
		}
	}

	// VJ
	private void checkIfLombokClass(ImportTree tree) {
		String importStr = fullQualifiedName(tree.qualifiedIdentifier());
		if (importStr.contains("lombok")) {
			lombokClass = true;
		}
	}

	// from WildcardImportsShouldNotBeUsedCheck
	private static String fullQualifiedName(Tree tree) {
		if (tree.is(Tree.Kind.IDENTIFIER)) {
			return ((IdentifierTree) tree).name();
		} else if (tree.is(Tree.Kind.MEMBER_SELECT)) {
			MemberSelectExpressionTree m = (MemberSelectExpressionTree) tree;
			return fullQualifiedName(m.expression()) + "." + m.identifier().name();
		}
		throw new UnsupportedOperationException(String.format("Kind/Class '%s' not supported", tree.getClass()));
	}

	private void collectUnknownIdentifier(IdentifierTree identifier) {
		if (identifier.symbol().isUnknown() && !isMethodIdentifier(identifier)) {
			unknownIdentifiers.add(identifier.name());
		}
	}

	private static boolean isMethodIdentifier(IdentifierTree identifier) {
		Tree parent = identifier.parent();
		while (parent != null && !parent.is(Tree.Kind.METHOD_INVOCATION, Tree.Kind.METHOD_REFERENCE)) {
			parent = parent.parent();
		}
		if (parent == null) {
			return false;
		}
		if (parent.is(Tree.Kind.METHOD_INVOCATION)) {
			return identifier.equals(methodName((MethodInvocationTree) parent));
		} else {
			return identifier.equals(((MethodReferenceTree) parent).method());
		}
	}

	//VJ: copy from MethodsHelper
	public static IdentifierTree methodName(MethodInvocationTree mit) {
	    IdentifierTree id;
	    if (mit.methodSelect().is(Tree.Kind.IDENTIFIER)) {
	      id = (IdentifierTree) mit.methodSelect();
	    } else {
	      id = ((MemberSelectExpressionTree) mit.methodSelect()).identifier();
	    }
	    return id;
	  }
	
	private void checkIfNativeMethod(MethodTree method) {
		if (ModifiersUtils.hasModifier(method.modifiers(), Modifier.NATIVE)) {
			hasNativeMethod = true;
		}
	}

	private void checkClassFields(ClassTree classTree) {
		classTree.members().stream().filter(member -> member.is(Tree.Kind.VARIABLE)).map(VariableTree.class::cast)
				.forEach(this::checkIfUnused);
	}

	public void checkIfUnused(VariableTree tree) {
		if (hasNoAnnotation(tree)) {
			Symbol symbol = tree.symbol();
			String name = symbol.name();
			if (symbol.isPrivate() && onlyUsedInVariableAssignment(symbol) && !"serialVersionUID".equals(name)
					&& !unknownIdentifiers.contains(name)) {
				reportIssue(tree.simpleName(), "Remove this unused \"" + name + "\" private field.");
			}
		}
	}

	private boolean onlyUsedInVariableAssignment(Symbol symbol) {
		return symbol.usages().size() == assignments.get(symbol).size();
	}

	private static boolean hasNoAnnotation(VariableTree tree) {
		return tree.modifiers().annotations().isEmpty();
	}

	private void collectAssignment(ExpressionTree expressionTree) {
		if (expressionTree.is(ASSIGNMENT_KINDS)) {
			addAssignment(((AssignmentExpressionTree) expressionTree).variable());
		}
	}

	private void addAssignment(ExpressionTree tree) {
		ExpressionTree variable = ExpressionUtils.skipParentheses(tree);
		if (variable.is(Tree.Kind.IDENTIFIER)) {
			addAssignment((IdentifierTree) variable);
		} else if (variable.is(Tree.Kind.MEMBER_SELECT)) {
			addAssignment(((MemberSelectExpressionTree) variable).identifier());
		}
	}

	private void addAssignment(IdentifierTree identifier) {
		Symbol reference = identifier.symbol();
		if (!reference.isUnknown()) {
			assignments.put(reference, identifier);
		}
	}

}
