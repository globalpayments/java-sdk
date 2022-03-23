package com.global.api.entities.enums;

public enum TransactionCode implements IStringConstant {

	BalanceInquiry("01"),
	Purchase("03"),
	PurchaseCashBack("04"),
	PurchaseReturn("05"),
	PreAuthorizationFunds("06"),
	PreAuthCompletion("07"),
	PreAuthCancelation("08"),
	VoucherSale("09"),
	VoucherReturn("10"),
	PurchaseReversal("13"),
	PurchaseCashBackReversal("14"),
	PurchaseReturnReversal("15"),
	Withdrawal("16"),
	WithdrawalReversal("17"),
	Load("22"),
	LoadReversal("32");

	String value;
	TransactionCode(String value) { this.value = value; }
    public String getValue() { return this.value; }
    public byte[] getBytes() { return this.value.getBytes(); }
}
