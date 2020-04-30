package com.global.api.terminals.pax.enums;

public enum TerminalTransactionType {
	MENU (00),
	SALE (01),
	RETURN (02),
	AUTH (03),
	POSTAUTH (04),
	FORCED (05),
	ADJUST (06),
	WITHDRAWAL (07),
//	ACTIVATE (08),
//	ISSUE (9),
	ADD (10),
	CASHOUT (11),
	DEACTIVATE (12),
	REPLACE (13),
	MERGE (14),
	REPORTLOST (15),
	VOID (16),
	VOID_SALE (17),
	VOID_RTRN (18),
	VOID_AUTH (19),
	VOID_POST (20),
	VOID_FRCD (21),
	VOID_WITHDRAW (22),
	BALANCE (23),
	VERIFY (24),
	REACTIVATE (25),
	FORCED_ISSUE (26),
	FORCED_ADD (27),
	UNLOAD (28),
	RENEW (29),
	GET_CONVERT_DETAIL (30),
	CONVERT (31),
	TOKENIZE (32),
	REVERSAL (99);

	private final int type;
	TerminalTransactionType(int type) {this.type = type; }
	public int getTerminalTransactionType() {
		return this.type;
	}
}
