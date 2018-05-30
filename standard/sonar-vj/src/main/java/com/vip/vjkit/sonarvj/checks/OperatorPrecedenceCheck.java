/*
 * SonarQube Java
 * Copyright (C) 2012-2017 SonarSource SA
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

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Table;
import org.apache.commons.lang.BooleanUtils;
import org.sonar.check.Rule;
import org.sonar.plugins.java.api.JavaFileScanner;
import org.sonar.plugins.java.api.JavaFileScannerContext;
import org.sonar.plugins.java.api.tree.*;

import java.util.*;

/**
 * 三目运算符中的表达式可以不要括号
 * 
 * https://github.com/SonarSource/sonar-java/blob/master/java-checks/src/main/java/org/sonar/java/checks/OperatorPrecedenceCheck.java
 * 
 * 0d54578  Jan 8, 2018
 */
@Rule(key = "S864")
public class OperatorPrecedenceCheck extends BaseTreeVisitor implements JavaFileScanner {

    private static final Table<Tree.Kind, Tree.Kind, Boolean> OPERATORS_RELATION_TABLE;

    private static final Set<Tree.Kind> ARITHMETIC_OPERATORS = EnumSet.of(
            Tree.Kind.MINUS,
            Tree.Kind.REMAINDER,
            Tree.Kind.MULTIPLY,
            Tree.Kind.PLUS
    );

    private static final Set<Tree.Kind> EQUALITY_RELATIONAL_OPERATORS = EnumSet.of(
            Tree.Kind.EQUAL_TO,
            Tree.Kind.GREATER_THAN,
            Tree.Kind.GREATER_THAN_OR_EQUAL_TO,
            Tree.Kind.LESS_THAN,
            Tree.Kind.LESS_THAN_OR_EQUAL_TO,
            Tree.Kind.NOT_EQUAL_TO
    );

    private static final Set<Tree.Kind> SHIFT_OPERATORS = EnumSet.of(
            Tree.Kind.LEFT_SHIFT,
            Tree.Kind.RIGHT_SHIFT,
            Tree.Kind.UNSIGNED_RIGHT_SHIFT
    );

    private static final Tree.Kind[] CONDITIONAL_EXCLUSIONS = new Tree.Kind[]{
            Tree.Kind.METHOD_INVOCATION, Tree.Kind.IDENTIFIER, Tree.Kind.MEMBER_SELECT,
            Tree.Kind.PARENTHESIZED_EXPRESSION, Tree.Kind.TYPE_CAST, Tree.Kind.NEW_CLASS,
            Tree.Kind.ARRAY_ACCESS_EXPRESSION, Tree.Kind.NEW_ARRAY
    };

    static {
        OPERATORS_RELATION_TABLE = HashBasedTable.create();
        put(ARITHMETIC_OPERATORS, Iterables.concat(SHIFT_OPERATORS, EnumSet.of(Tree.Kind.AND, Tree.Kind.XOR, Tree.Kind.OR)));
        put(SHIFT_OPERATORS, Iterables.concat(ARITHMETIC_OPERATORS, EnumSet.of(Tree.Kind.AND, Tree.Kind.XOR, Tree.Kind.OR)));
        put(EnumSet.of(Tree.Kind.AND), Iterables.concat(ARITHMETIC_OPERATORS, SHIFT_OPERATORS, EnumSet.of(Tree.Kind.XOR, Tree.Kind.OR)));
        put(EnumSet.of(Tree.Kind.XOR), Iterables.concat(ARITHMETIC_OPERATORS, SHIFT_OPERATORS, EnumSet.of(Tree.Kind.AND, Tree.Kind.OR)));
        put(EnumSet.of(Tree.Kind.OR), Iterables.concat(ARITHMETIC_OPERATORS, SHIFT_OPERATORS, EnumSet.of(Tree.Kind.AND, Tree.Kind.XOR)));
        put(EnumSet.of(Tree.Kind.CONDITIONAL_AND), EnumSet.of(Tree.Kind.CONDITIONAL_OR));
        put(EnumSet.of(Tree.Kind.CONDITIONAL_OR), EnumSet.of(Tree.Kind.CONDITIONAL_AND));
    }

    private JavaFileScannerContext context;
    private Deque<Tree.Kind> stack = new LinkedList<>();
    private Set<Integer> reportedLines = new HashSet<>();

    private static void put(Iterable<Tree.Kind> firstSet, Iterable<Tree.Kind> secondSet) {
        for (Tree.Kind first : firstSet) {
            for (Tree.Kind second : secondSet) {
                OPERATORS_RELATION_TABLE.put(first, second, true);
            }
        }
    }

    @Override
    public void scanFile(JavaFileScannerContext context) {
        this.context = context;
        reportedLines.clear();
        scan(context.getTree());
        reportedLines.clear();
    }

    @Override
    public void visitAnnotation(AnnotationTree tree) {
        stack.push(null);
        for (ExpressionTree argument : tree.arguments()) {
            if (argument.is(Tree.Kind.ASSIGNMENT)) {
                scan(((AssignmentExpressionTree) argument).expression());
            } else {
                scan(argument);
            }
        }
        stack.pop();
    }

    @Override
    public void visitArrayAccessExpression(ArrayAccessExpressionTree tree) {
        scan(tree.expression());
        stack.push(null);
        scan(tree.dimension());
        stack.pop();
    }

    @Override
    public void visitBinaryExpression(BinaryExpressionTree tree) {
        Tree.Kind peek = stack.peek();
        Tree.Kind kind = tree.kind();
        if (requiresParenthesis(peek, kind)) {
            raiseIssue(tree.operatorToken().line(), tree);
        }
        stack.push(kind);
        super.visitBinaryExpression(tree);
        stack.pop();
    }

    private static boolean requiresParenthesis(Tree.Kind kind1, Tree.Kind kind2) {
        return BooleanUtils.isTrue(OPERATORS_RELATION_TABLE.get(kind1, kind2));
    }

    @Override
    public void visitIfStatement(IfStatementTree tree) {
        super.visitIfStatement(tree);
        ExpressionTree condition = tree.condition();
        if (condition.is(Tree.Kind.ASSIGNMENT) && EQUALITY_RELATIONAL_OPERATORS.contains(((AssignmentExpressionTree) condition).expression().kind())) {
            raiseIssue(((AssignmentExpressionTree) condition).operatorToken().line(), tree);
        }
    }

    @Override
    public void visitMethodInvocation(MethodInvocationTree tree) {
        scan(tree.methodSelect());
        scan(tree.typeArguments());
        for (ExpressionTree argument : tree.arguments()) {
            stack.push(null);
            scan(argument);
            stack.pop();
        }
    }

    @Override
    public void visitNewArray(NewArrayTree tree) {
        stack.push(null);
        super.visitNewArray(tree);
        stack.pop();
    }

    @Override
    public void visitNewClass(NewClassTree tree) {
        stack.push(null);
        super.visitNewClass(tree);
        stack.pop();
    }

    @Override
    public void visitParenthesized(ParenthesizedTree tree) {
        stack.push(null);
        super.visitParenthesized(tree);
        stack.pop();
    }

    @Override
    public void visitConditionalExpression(ConditionalExpressionTree tree) {
        checkConditionalOperand(tree.trueExpression());
        checkConditionalOperand(tree.falseExpression());
        super.visitConditionalExpression(tree);
    }

    private void checkConditionalOperand(ExpressionTree tree) {
        if (!(tree.is(CONDITIONAL_EXCLUSIONS) || tree instanceof LiteralTree || tree instanceof UnaryExpressionTree)) {
            //VJ ADD 三目运算符中的表达式可以不要括号
            if (tree.parent() != null && tree.parent().kind().equals(Tree.Kind.CONDITIONAL_EXPRESSION)) {
                return;
            }
            //VJ END
            raiseIssue(tree.firstToken().line(), tree);
        }
    }

    private void raiseIssue(int line, Tree tree) {
        if (reportedLines.add(line)) {
            context.reportIssue(this, tree, "Add parentheses to make the operator precedence explicit.");
        }
    }

}
