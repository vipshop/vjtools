package com.vip.vjkit.sonarvj.checks;

import com.google.common.collect.ImmutableList;
import org.sonar.check.Rule;
import org.sonar.plugins.java.api.IssuableSubscriptionVisitor;
import org.sonar.plugins.java.api.tree.*;

import java.util.List;

/**
 * 1. equals 方法忽略if的检查 2. if(conditon) return true; 忽略在同一行的模式
 * 
 * https://github.com/SonarSource/sonar-java/blob/master/java-checks/src/main/java/org/sonar/java/checks/MissingCurlyBracesCheck.java
 * 
 * 0d54578 Jan 8, 2018
 */
@Rule(key = "S121")
public class MissingCurlyBracesCheck extends IssuableSubscriptionVisitor {

	@Override
	public List<Tree.Kind> nodesToVisit() {
		return ImmutableList.of(Tree.Kind.IF_STATEMENT, Tree.Kind.FOR_EACH_STATEMENT, Tree.Kind.FOR_STATEMENT,
				Tree.Kind.WHILE_STATEMENT, Tree.Kind.DO_STATEMENT);
	}

	@Override
	public void visitNode(Tree tree) {
		switch (tree.kind()) {
		case WHILE_STATEMENT:
			WhileStatementTree whileStatementTree = (WhileStatementTree) tree;
			checkStatement(whileStatementTree.whileKeyword(), whileStatementTree.statement());
			break;
		case DO_STATEMENT:
			DoWhileStatementTree doWhileStatementTree = (DoWhileStatementTree) tree;
			checkStatement(doWhileStatementTree.doKeyword(), doWhileStatementTree.statement());
			break;
		case FOR_STATEMENT:
			ForStatementTree forStatementTree = (ForStatementTree) tree;
			checkStatement(forStatementTree.forKeyword(), forStatementTree.statement());
			break;
		case FOR_EACH_STATEMENT:
			ForEachStatement forEachStatement = (ForEachStatement) tree;
			checkStatement(forEachStatement.forKeyword(), forEachStatement.statement());
			break;
		case IF_STATEMENT:
			checkIfStatement((IfStatementTree) tree);
			break;
		default:
			break;
		}
	}

	private void checkIfStatement(IfStatementTree ifStmt) {
		// equals 方法忽略if的检查, 如果if 与处理函数在同一行忽略。
		if (isInEqualsMethod(ifStmt)) {
			return;
		}

		if (!isSameLine(ifStmt)) {
			checkStatement(ifStmt.ifKeyword(), ifStmt.thenStatement());
		}

		StatementTree elseStmt = ifStmt.elseStatement();
		if (elseStmt != null && !elseStmt.is(Tree.Kind.IF_STATEMENT)) {
			checkStatement(ifStmt.elseKeyword(), elseStmt);
		}
	}

	private boolean isSameLine(IfStatementTree ifStmt) {
		StatementTree thenStmt = ifStmt.thenStatement();
		if (thenStmt.is(Tree.Kind.BLOCK)) {
			return false;
		}

		return ifStmt.firstToken().line() == thenStmt.firstToken().line();
	}

	private boolean isInEqualsMethod(IfStatementTree ifStmt) {
		Tree tree = ifStmt.parent();
		while (tree != null && !(tree instanceof MethodTree)) {
			tree = tree.parent();
		}

		if (tree == null) {
			return false;
		}

		MethodTree methodTree = (MethodTree) tree;
		if (methodTree.simpleName().toString().equals("equals")) {
			return true;
		}
		return false;
	}

	private void checkStatement(SyntaxToken reportToken, StatementTree statement) {
		if (!statement.is(Tree.Kind.BLOCK)) {
			reportIssue(reportToken, "Missing curly brace.");
		}
	}
}
