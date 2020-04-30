package com.global.api.terminals.pax.enums;

public enum TerminalCardType
{
	VISA(01),
	MASTERCARD(02),
	AMEX(03),
	DiSCOVER(04),
	DINER_CLUB(05),
	EN_ROUTE(06),
	JCB(07),
//	REVOLUTION_CARD(08),
//	VISA_FLEET(09),
	MASTERCARD_FLEET(10),
	FLEET_ONE(11),
	FLEET_WIDE(12),
	FUEL_MAN(13),
	GAS_CARD(14),
	VOYAGER(15),
	WRIGHT_EXPRESS(16),
	OTHER(99);

	private final int type;
	TerminalCardType(int type) { this.type = type; }
	public int getTerminalCardType() {
		return this.type;
	}
}
