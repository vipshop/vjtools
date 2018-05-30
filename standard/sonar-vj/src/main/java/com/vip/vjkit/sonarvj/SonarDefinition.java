
package com.vip.vjkit.sonarvj;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Iterables;
import com.google.common.io.Resources;
import com.google.gson.Gson;
import org.apache.commons.lang.StringUtils;
import org.sonar.api.rule.RuleStatus;
import org.sonar.api.rules.RuleType;
import org.sonar.api.server.debt.DebtRemediationFunction;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.server.rule.RulesDefinitionAnnotationLoader;
import org.sonar.api.utils.AnnotationUtils;
import org.sonar.check.Cardinality;
import org.sonar.squidbridge.annotations.RuleTemplate;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

/**
 * Declare rule metadata in server repository of rules.
 * That allows to list the rules in the page "Rules".
 */
public class SonarDefinition implements RulesDefinition {

    // don't change that because the path is hard coded in CheckVerifier
    private static final String RESOURCE_BASE_PATH = "/com/vip/java/rules";

    public static final String REPOSITORY_KEY = "v-squid";

    private final Gson gson = new Gson();

    @Override
    public void define(Context context) {
        NewRepository repository = context
                .createRepository(REPOSITORY_KEY, "java")
                .setName("VIPAnalyzer ");

        List<Class> checks = SonarRulesList.getChecks();
        new RulesDefinitionAnnotationLoader().load(repository, Iterables.toArray(checks, Class.class));

        for (Class ruleClass : checks) {
            newRule(ruleClass, repository);
        }
        repository.done();
    }

    @VisibleForTesting
    protected void newRule(Class<?> ruleClass, NewRepository repository) {

        org.sonar.check.Rule ruleAnnotation = AnnotationUtils.getAnnotation(ruleClass, org.sonar.check.Rule.class);
        if (ruleAnnotation == null) {
            throw new IllegalArgumentException("No Rule annotation was found on " + ruleClass);
        }
        String ruleKey = ruleAnnotation.key();
        if (StringUtils.isEmpty(ruleKey)) {
            throw new IllegalArgumentException("No key is defined in Rule annotation of " + ruleClass);
        }
        NewRule rule = repository.rule(ruleKey);
        if (rule == null) {
            throw new IllegalStateException("No rule was created for " + ruleClass + " in " + repository.key());
        }
        ruleMetadata(ruleClass, rule);

        rule.setTemplate(AnnotationUtils.getAnnotation(ruleClass, RuleTemplate.class) != null);
        if (ruleAnnotation.cardinality() == Cardinality.MULTIPLE) {
            throw new IllegalArgumentException("Cardinality is not supported, use the RuleTemplate annotation instead for " + ruleClass);
        }
    }

    private String ruleMetadata(Class<?> ruleClass, NewRule rule) {
        String metadataKey = rule.key();
        org.sonar.java.RspecKey rspecKeyAnnotation = AnnotationUtils.getAnnotation(ruleClass, org.sonar.java.RspecKey.class);
        if (rspecKeyAnnotation != null) {
            metadataKey = rspecKeyAnnotation.value();
            rule.setInternalKey(metadataKey);
        }
        addHtmlDescription(rule, metadataKey);
        addMetadata(rule, metadataKey);
        return metadataKey;
    }

    private void addMetadata(NewRule rule, String metadataKey) {
        URL resource = SonarDefinition.class.getResource(RESOURCE_BASE_PATH + "/" + metadataKey + "_java.json");
        if (resource != null) {
            RuleMetatada metatada = gson.fromJson(readResource(resource), RuleMetatada.class);
            rule.setSeverity(metatada.defaultSeverity.toUpperCase(Locale.US));
            rule.setName(metatada.title);
            rule.addTags(metatada.tags);
            rule.setType(RuleType.valueOf(metatada.type));
            rule.setStatus(RuleStatus.valueOf(metatada.status.toUpperCase(Locale.US)));
            if (metatada.remediation != null) {
                rule.setDebtRemediationFunction(metatada.remediation.remediationFunction(rule.debtRemediationFunctions()));
                rule.setGapDescription(metatada.remediation.linearDesc);
            }
        }
    }

    private static void addHtmlDescription(NewRule rule, String metadataKey) {
        URL resource = SonarDefinition.class.getResource(RESOURCE_BASE_PATH + "/" + metadataKey + "_java.html");
        if (resource != null) {
            rule.setHtmlDescription(readResource(resource));
        }
    }

    private static String readResource(URL resource) {
        try {
            return Resources.toString(resource, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read: " + resource, e);
        }
    }

    private static class RuleMetatada {
        String title;
        String status;
        @Nullable
        Remediation remediation;

        String type;
        String[] tags;
        String defaultSeverity;
    }

    private static class Remediation {
        String func;
        String constantCost;
        String linearDesc;
        String linearOffset;
        String linearFactor;

        public DebtRemediationFunction remediationFunction(DebtRemediationFunctions drf) {
            if (func.startsWith("Constant")) {
                return drf.constantPerIssue(constantCost.replace("mn", "min"));
            }
            if ("Linear".equals(func)) {
                return drf.linear(linearFactor.replace("mn", "min"));
            }
            return drf.linearWithOffset(linearFactor.replace("mn", "min"), linearOffset.replace("mn", "min"));
        }
    }

}
