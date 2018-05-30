package com.vip.vjkit.sonarvj;

import com.vip.vjkit.sonarvj.checks.*;
import org.sonar.plugins.java.api.CheckRegistrar;
import org.sonar.plugins.java.api.JavaCheck;

import java.util.Arrays;

public class SonarCheckRegistrar implements CheckRegistrar {

	@Override
	public void register(RegistrarContext registrarContext) {
		registrarContext.registerClassesForRepository(SonarDefinition.REPOSITORY_KEY, Arrays.asList(checkClasses()),
				Arrays.asList(testCheckClasses()));
	}

	public static Class<? extends JavaCheck>[] checkClasses() {
		return new Class[] { BadConstantNameCheck.class, OperatorPrecedenceCheck.class,
				UnusedMethodParameterCheck.class, UnusedPrivateFieldCheck.class, MissingCurlyBracesCheck.class,
				HardcodedIpCheck.class, NoSonarCheck.class, CatchUsesExceptionWithContextCheck.class };
	}

	public static Class<? extends JavaCheck>[] testCheckClasses() {
		return new Class[] {};
	}
}