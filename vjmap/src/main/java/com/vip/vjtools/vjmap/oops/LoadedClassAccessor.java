package com.vip.vjtools.vjmap.oops;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import sun.jvm.hotspot.debugger.AddressException;
import sun.jvm.hotspot.memory.SystemDictionary;
import sun.jvm.hotspot.oops.InstanceKlass;
import sun.jvm.hotspot.oops.Klass;
import sun.jvm.hotspot.oops.Oop;
import sun.jvm.hotspot.runtime.VM;

public class LoadedClassAccessor {

	private PrintStream tty = System.out;

	public void pringLoadedClass() {
		tty.println("Finding classes in System Dictionary..");

		try {
			final ArrayList<InstanceKlass> klasses = new ArrayList<>(128);

			SystemDictionary dict = VM.getVM().getSystemDictionary();
			dict.classesDo(new SystemDictionary.ClassVisitor() {
				@Override
				public void visit(Klass k) {
					if (k instanceof InstanceKlass) {
						klasses.add((InstanceKlass) k);
					}
				}
			});

			Collections.sort(klasses, new Comparator<InstanceKlass>() {
				@Override
				public int compare(InstanceKlass x, InstanceKlass y) {
					return x.getName().asString().compareTo(y.getName().asString());
				}
			});

			tty.println("#class             #loader");
			tty.println("-----------------------------------------------");
			for (InstanceKlass k : klasses) {
				tty.printf("%s, %s\n", getClassNameFrom(k), getClassLoaderOopFrom(k));
			}
		} catch (AddressException e) {
			tty.println("Error accessing address 0x" + Long.toHexString(e.getAddress()));
			e.printStackTrace();
		}
	}

	private static String getClassLoaderOopFrom(InstanceKlass klass) {
		Oop loader = klass.getClassLoader();
		return loader != null ? getClassNameFrom((InstanceKlass) loader.getKlass()) + " @ " + loader.getHandle()
				: "<bootstrap>";
	}

	private static String getClassNameFrom(InstanceKlass klass) {
		return klass != null ? klass.getName().asString().replace('/', '.') : null;
	}
}
