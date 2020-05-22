package com.vip.vjtools.vjkit.datamasking.data;

import java.util.List;

/**
 *
 * @author ken
 */
public class TestParent {

	private TestChild child;

	private List<TestChild> children;

	private TestParent other;

	public TestChild getChild() {
		return child;
	}

	public void setChild(TestChild child) {
		this.child = child;
	}

	public List<TestChild> getChildren() {
		return children;
	}

	public void setChildren(List<TestChild> children) {
		this.children = children;
	}

	public TestParent getOther() {
		return other;
	}

	public void setOther(TestParent other) {
		this.other = other;
	}

	@Override
	public String toString() {
		return "TestParent{" + "child=" + child + ", children=" + children + ", other=" + other + '}';
	}
}
