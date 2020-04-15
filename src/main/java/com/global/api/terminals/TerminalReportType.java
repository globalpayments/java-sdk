package com.global.api.terminals;

public enum TerminalReportType
{
	LocalDetailReport;

	public static final int SIZE = java.lang.Integer.SIZE;

	public int getValue()
	{
		return this.ordinal();
	}

	public static TerminalReportType forValue(int value)
	{
		return values()[value];
	}
}