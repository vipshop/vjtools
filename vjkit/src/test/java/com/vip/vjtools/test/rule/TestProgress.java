package com.vip.vjtools.test.rule;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

/**
 * 在Console里打印Case的开始与结束，更容易分清Console里的日志归属于哪个Case.
 * 
 * @author calvin
 */
public class TestProgress extends TestWatcher {

	@Override
	protected void starting(Description description) {
		System.out.println("\n[Test Case starting] " + description.getTestClass().getSimpleName() + "."
				+ description.getMethodName() + "()\n");
	}

	@Override
	protected void finished(Description description) {
		System.out.println("\n[Test Case finished] " + description.getTestClass().getSimpleName() + "."
				+ description.getMethodName() + "()\n");
	}

}
