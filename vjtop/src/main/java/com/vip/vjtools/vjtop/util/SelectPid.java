package com.vip.vjtools.vjtop.util;

import java.io.BufferedReader;
import java.io.Console;
import java.io.InputStreamReader;

/**
 * Created by traburiss on 2019/12/11.
 * describe:
 */
public class SelectPid {

	public static Integer getPidFromJpsList(){

		Integer pid = null;
		Process process = null;
		BufferedReader reader = null;
		try {

			Console console = System.console();
			process = Runtime.getRuntime().exec("jps -l");
			reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String defPidStr = "";
			String line;
			System.out.println("please input a pid from list:");
			System.out.println("PID\tNAME");
			while ((line = reader.readLine()) != null) {
				//不显示jps和自己
				if (!line.contains("sun.tools.jps.Jps") && !line.contains("com.vip.vjtools.vjtop.VJTop")){
					String[] pm = line.split(" ",2);
					if (defPidStr.isEmpty()){
						defPidStr = pm[0];
					}
					System.out.println(pm[0] + "\t" + pm[1]);
				}
			}
			String pidString = console.readLine("\nplease input pid(default is " + defPidStr + "):");
			if (pidString == null || pidString.isEmpty()){
				pidString = defPidStr;
			}
			pid = Integer.valueOf(pidString);
		}catch (Exception ignored){
		}finally {

			try {
				if (reader != null) {
					reader.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				if (process != null) {
					process.destroy();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return pid;
	}
}
