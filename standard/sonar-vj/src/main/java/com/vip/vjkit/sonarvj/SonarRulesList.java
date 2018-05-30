package com.vip.vjkit.sonarvj;

import com.google.common.collect.ImmutableList;
import com.vip.vjkit.sonarvj.checks.*;
import org.sonar.plugins.java.api.JavaCheck;

import java.util.List;

/**
 * Created by cloud.huang on 18/1/5.
 */
public class SonarRulesList {

    public static List<Class> getChecks() {
        return ImmutableList.<Class>builder().addAll(getJavaChecks()).addAll(getJavaTestChecks()).build();
    }

    public static List<Class<? extends JavaCheck>> getJavaChecks() {
        return ImmutableList.<Class<? extends JavaCheck>>builder()
                .add(BadConstantNameCheck.class)
                .add(OperatorPrecedenceCheck.class)
                .add(UnusedPrivateFieldCheck.class)
                .add(UnusedMethodParameterCheck.class)
                .add(MissingCurlyBracesCheck.class)
                .add(HardcodedIpCheck.class)
                .add(NoSonarCheck.class)
                .add(CatchUsesExceptionWithContextCheck.class)
                .build();
    }

    public static List<Class<? extends JavaCheck>> getJavaTestChecks() {
        return ImmutableList.<Class<? extends JavaCheck>>builder()
                .build();
    }

}
