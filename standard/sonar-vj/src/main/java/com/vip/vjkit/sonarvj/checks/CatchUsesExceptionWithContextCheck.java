package com.vip.vjkit.sonarvj.checks;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.java.checks.helpers.ExpressionsHelper;
import org.sonar.java.matcher.MethodMatcher;
import org.sonar.java.matcher.TypeCriteria;
import org.sonar.plugins.java.api.JavaFileScanner;
import org.sonar.plugins.java.api.JavaFileScannerContext;
import org.sonar.plugins.java.api.semantic.Symbol;
import org.sonar.plugins.java.api.tree.*;
import org.sonar.plugins.java.api.tree.Tree.Kind;

import java.util.*;

/**
 * catch异常的变量名包含ignore，忽略检查
 */
@Rule(key = "S1166")
public class CatchUsesExceptionWithContextCheck extends BaseTreeVisitor implements JavaFileScanner {

    private static final String EXCLUDED_EXCEPTION_TYPE = "java.lang.InterruptedException, " +
            "java.lang.NumberFormatException, " +
            "java.lang.NoSuchMethodException, " +
            "java.text.ParseException, " +
            "java.net.MalformedURLException, " +
            "java.time.format.DateTimeParseException";

    @RuleProperty(
            key = "exceptions",
            description = "List of exceptions which should not be checked",
            defaultValue = "" + EXCLUDED_EXCEPTION_TYPE)
    public String exceptionsCommaSeparated = EXCLUDED_EXCEPTION_TYPE;

    private JavaFileScannerContext context;
    private Deque<Collection<IdentifierTree>> validUsagesStack;
    private Iterable<String> exceptions;
    private List<String> exceptionIdentifiers;
    private Set<CatchTree> excludedCatchTrees = new HashSet<>();

    @Override
    public void scanFile(JavaFileScannerContext context) {
        this.context = context;
        validUsagesStack = new ArrayDeque<>();
        exceptions = Splitter.on(",").trimResults().split(exceptionsCommaSeparated);
        exceptionIdentifiers = Lists.newArrayList();
        for (String exception : exceptions) {
            exceptionIdentifiers.add(exception.substring(exception.lastIndexOf('.') + 1));
        }
        if (context.getSemanticModel() != null) {
            scan(context.getTree());
        }
        excludedCatchTrees.clear();
    }

    @Override
    public void visitTryStatement(TryStatementTree tree) {
        if (containsEnumValueOf(tree.block())) {
            tree.catches().stream()
                    .filter(c -> c.parameter().symbol().type().is("java.lang.IllegalArgumentException"))
                    .findAny()
                    .ifPresent(excludedCatchTrees::add);
        }
        super.visitTryStatement(tree);
    }

    private static boolean containsEnumValueOf(Tree tree) {
        EnumValueOfVisitor visitor = new EnumValueOfVisitor();
        tree.accept(visitor);
        return visitor.hasEnumValueOf;
    }

    private static class EnumValueOfVisitor extends BaseTreeVisitor {
        private static final MethodMatcher ENUM_VALUE_OF = MethodMatcher.create()
                .typeDefinition(TypeCriteria.subtypeOf("java.lang.Enum"))
                .name("valueOf")
                .withAnyParameters();
        private boolean hasEnumValueOf = false;

        @Override
        public void visitMethodInvocation(MethodInvocationTree tree) {
            if (ENUM_VALUE_OF.matches(tree)) {
                hasEnumValueOf = true;
            }
            super.visitMethodInvocation(tree);
        }

        @Override
        public void visitClass(ClassTree tree) {
            // skip anonymous classes
        }

        @Override
        public void visitLambdaExpression(LambdaExpressionTree lambdaExpressionTree) {
            // skip lambdas
        }
    }

    @Override
    public void visitCatch(CatchTree tree) {
        if (!isExcludedType(tree.parameter().type()) && !excludedCatchTrees.contains(tree)) {
            Symbol exception = tree.parameter().symbol();
            //catch异常的变量名包含ignore，忽略检查
            if (StringUtils.containsIgnoreCase(exception.name(), "ignore")) {
                return;
            }
            validUsagesStack.addFirst(Lists.newArrayList(exception.usages()));
            super.visitCatch(tree);
            Collection<IdentifierTree> usages = validUsagesStack.pop();
            if (usages.isEmpty()) {
                context.reportIssue(this, tree.parameter(), "Either log or rethrow this exception.");
            }
        }
    }

    @Override
    public void visitMemberSelectExpression(MemberSelectExpressionTree tree) {
        IdentifierTree identifier = null;
        ExpressionTree expression = tree.expression();
        if (expression.is(Kind.IDENTIFIER)) {
            identifier = (IdentifierTree) expression;
        } else if (expression.is(Kind.PARENTHESIZED_EXPRESSION) && ((ParenthesizedTree) expression).expression().is(Kind.IDENTIFIER)) {
            identifier = (IdentifierTree) ((ParenthesizedTree) expression).expression();
        }
        if (!validUsagesStack.isEmpty() && identifier != null) {
            Iterator<Collection<IdentifierTree>> iterator = validUsagesStack.iterator();
            while (iterator.hasNext()) {
                iterator.next().remove(identifier);
            }
        }
        super.visitMemberSelectExpression(tree);

    }

    private boolean isExcludedType(Tree tree) {
        return isUnqualifiedExcludedType(tree) ||
                isQualifiedExcludedType(tree);
    }

    private boolean isUnqualifiedExcludedType(Tree tree) {
        return tree.is(Kind.IDENTIFIER) &&
                exceptionIdentifiers.contains(((IdentifierTree) tree).name());
    }

    private boolean isQualifiedExcludedType(Tree tree) {
        if (!tree.is(Kind.MEMBER_SELECT)) {
            return false;
        }
        return Iterables.contains(exceptions, concatenate((MemberSelectExpressionTree) tree));
    }
    
    public static String concatenate(ExpressionTree tree) {
        if(tree == null) {
          return "";
        }
        Deque<String> pieces = new LinkedList<>();
        ExpressionTree expr = tree;
        while (expr.is(Tree.Kind.MEMBER_SELECT)) {
          MemberSelectExpressionTree mse = (MemberSelectExpressionTree) expr;
          pieces.push(mse.identifier().name());
          pieces.push(".");
          expr = mse.expression();
        }
        if (expr.is(Tree.Kind.IDENTIFIER)) {
          IdentifierTree idt = (IdentifierTree) expr;
          pieces.push(idt.name());
        }

        StringBuilder sb = new StringBuilder();
        for (String piece: pieces) {
          sb.append(piece);
        }
        return sb.toString();
      }

}
