/*
 * SonarQube Java
 * Copyright (C) 2012-2018 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package com.vip.vjkit.sonarvj.checks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.sonar.check.Rule;
import org.sonar.java.model.ModifiersUtils;
import org.sonar.plugins.java.api.IssuableSubscriptionVisitor;
import org.sonar.plugins.java.api.JavaFileScannerContext;
import org.sonar.plugins.java.api.semantic.Symbol;
import org.sonar.plugins.java.api.semantic.Type;
import org.sonar.plugins.java.api.tree.BaseTreeVisitor;
import org.sonar.plugins.java.api.tree.ExpressionTree;
import org.sonar.plugins.java.api.tree.IdentifierTree;
import org.sonar.plugins.java.api.tree.MemberSelectExpressionTree;
import org.sonar.plugins.java.api.tree.MethodInvocationTree;
import org.sonar.plugins.java.api.tree.MethodTree;
import org.sonar.plugins.java.api.tree.Modifier;
import org.sonar.plugins.java.api.tree.Tree;
import org.sonar.plugins.java.api.tree.VariableTree;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

/**
 * 只判读private方法里的无用参数
 * 
 * https://github.com/SonarSource/sonar-java/blob/master/java-checks/src/main/java/org/sonar/java/checks/unused/UnusedMethodParameterCheck.java
 * 
 * 2c86dbb Jan 12, 2018
 */
@Rule(key = "S1172")
public class UnusedMethodParameterCheck extends IssuableSubscriptionVisitor {
	private static final String AUTHORIZED_ANNOTATION = "javax.enterprise.event.Observes";
	private static final String STRUTS_ACTION_SUPERCLASS = "org.apache.struts.action.Action";
	private static final Collection<String> EXCLUDED_STRUTS_ACTION_PARAMETER_TYPES = ImmutableList.of(
			"org.apache.struts.action.ActionMapping", "org.apache.struts.action.ActionForm",
			"javax.servlet.http.HttpServletRequest", "javax.servlet.http.HttpServletResponse");

	@Override
	public List<Tree.Kind> nodesToVisit() {
		return ImmutableList.of(Tree.Kind.METHOD, Tree.Kind.CONSTRUCTOR);
	}

	@Override
	public void visitNode(Tree tree) {
		if (!hasSemantic()) {
			return;
		}
		MethodTree methodTree = (MethodTree) tree;
		if (methodTree.block() == null || !isIncluded(methodTree)) {
			return;
		}
		List<IdentifierTree> unused = Lists.newArrayList();
		for (VariableTree var : methodTree.parameters()) {
			Symbol symbol = var.symbol();
			if (symbol.usages().isEmpty() && !symbol.metadata().isAnnotatedWith(AUTHORIZED_ANNOTATION)
					&& !isStrutsActionParameter(var)) {
				unused.add(var.simpleName());
			}
		}
		Set<String> unresolvedIdentifierNames = unresolvedIdentifierNames(methodTree.block());
		// kill the noise regarding unresolved identifiers, and remove the one with matching names from the list of
		// unused
		unused = unused.stream().filter(id -> !unresolvedIdentifierNames.contains(id.name()))
				.collect(Collectors.toList());
		if (!unused.isEmpty()) {
			reportUnusedParameters(unused);
		}
	}

	private void reportUnusedParameters(List<IdentifierTree> unused) {
		List<JavaFileScannerContext.Location> locations = new ArrayList<>();
		for (IdentifierTree identifier : unused) {
			locations.add(new JavaFileScannerContext.Location(
					"Remove this unused method parameter " + identifier.name() + "\".", identifier));
		}
		IdentifierTree firstUnused = unused.get(0);
		String msg;
		if (unused.size() > 1) {
			msg = "Remove these unused method parameters.";
		} else {
			msg = "Remove this unused method parameter \"" + firstUnused.name() + "\".";
		}
		reportIssue(firstUnused, msg, locations, null);
	}

	// VJ: 改为只判读private方法里的无用参数，替代原isExclude
	// 大量删除原来exclude里的复杂判断
	private static boolean isIncluded(MethodTree tree) {
		return isPrivateMethod(tree);
	}

	private static boolean isStrutsActionParameter(VariableTree variableTree) {
		Type superClass = variableTree.symbol().enclosingClass().superClass();
		return superClass != null && superClass.isSubtypeOf(STRUTS_ACTION_SUPERCLASS)
				&& EXCLUDED_STRUTS_ACTION_PARAMETER_TYPES.contains(variableTree.symbol().type().fullyQualifiedName());
	}

	private static boolean isPrivateMethod(MethodTree methodTree) {
		return ModifiersUtils.hasModifier(methodTree.modifiers(), Modifier.PRIVATE);
	}

	private static Set<String> unresolvedIdentifierNames(Tree tree) {
		UnresolvedIdentifierVisitor visitor = new UnresolvedIdentifierVisitor();
		tree.accept(visitor);
		return visitor.unresolvedIdentifierNames;
	}

	private static class UnresolvedIdentifierVisitor extends BaseTreeVisitor {

		private Set<String> unresolvedIdentifierNames = new HashSet<>();

		@Override
		public void visitMemberSelectExpression(MemberSelectExpressionTree tree) {
			// skip annotations and identifier, a method parameter will only be used in expression side (before the dot)
			scan(tree.expression());
		}

		@Override
		public void visitMethodInvocation(MethodInvocationTree tree) {
			ExpressionTree methodSelect = tree.methodSelect();
			if (!methodSelect.is(Tree.Kind.IDENTIFIER)) {
				// not interested in simple method invocations, we are targeting usage of method parameters
				scan(methodSelect);
			}
			scan(tree.typeArguments());
			scan(tree.arguments());
		}

		@Override
		public void visitIdentifier(IdentifierTree tree) {
			if (tree.symbol().isUnknown()) {
				unresolvedIdentifierNames.add(tree.name());
			}
			super.visitIdentifier(tree);
		}
	}
}
