package com.zhang.distributedlock.util;

public final class ExecuteTimeUtil {

	private static long start = 0L;

	public static void start() {
		start = System.currentTimeMillis();
	}

	public static long end() {
		return System.currentTimeMillis() - start;
	}

	public static String executeTime() {
		long end = end();
		long s = end / 1000;
		long m = 0;
		if (s > 60) {
			// 分
			s = s % 60;
			m = s / 60;
		}
		return m > 0 ? m + "分" + s + "秒" : s + "秒";
	}

}
