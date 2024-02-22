package com.global.api.entities.enums;

public enum TerminalType implements IStringConstant{
	Koppens("01"),
	GilbarcoPassport("02"),
	Ibm("03"),
	VerifonePearl("04"),
	VerifoneRuby("05"),
	ConvenientAutomation("06"),
	NetworkDataDec("07"),
	ConsolidatedApplication("08"),
	DataCard740("09"),
	Verifone("10"),
	DbrSystems("11"),
	DataCard840Or940("10"),
	VerifoneTrinityT650C("11"),
	WayneNucleus("12"),
	Ingenico("13"),
	SysTech("14"),
	Rayethon("15"),
	WaynePlus("16"),
	FboManager("17"),
	TransBillingItn("18"),
	TransBillingSnet("19"),
	TransBillingGte("20"),
	HypercomPetrosMart500("21"),
	HypercomPetrosMart1000("22"),
	RadiantCompuTouchRpos("23"),
	HypercomPetrosMart1500("24"),
	CompuTouchNt("25"),
	GilbarcoC486Dial("26"),
	GilbarcoC2("27"),
	GilbarcoC486Lse("28"),
	GilbarcoC2Lease("29"),
	VerifoneZon("30"),
	SynTech("31"),
	VerifoneOmni3750("32"),
	SynTechSmu2500("33"),
	Nec("34"),
	RadiantLightHouse("35"),
	CastlesSaturn1000S("36"),
	AlliedTdl("37"),
	VerifoneTopaz("39"),
	PaxIm2030300("40"),
	VerifoneVx570("41"),
	VerifoneZonXpe("42"),
	WaynePlus3("43"),
	Ingenico5100("44"),
	VerifoneVx510OrVx570("45"),
	GilbarcoCfnIii("46"),
	GilbarcoCfnIi("47"),
	Retalix("48"),
	VerifoneVx510("50"),
	Hypercom8583("51"),
	Verifone610For8583("52"),
	Verifone670For8583("53"),
	Rsg("54"),
	HypercomT7E("55"),
	CcisTech("56"),
	P97MobilePosFor8583("57"),
	VerifoneIntegDial("58"),
	VerifoneVx610("59"),
	VerifoneVx670("60"),
	HeartlandHost("61"),
	VerifoneVx520("62"),
	VerifoneVx680("63"),
	VerifoneOmni395("64"),
	RadiantRpos("65"),
	AutogasRegalSx260("66"),
	Autogas510("67"),
	AutogasSx270("68"),
	AutogasSx280("69"),
	WayneFusion6000("70"),
	Pinnacle("71"),
	Ncr("72"),
	Petrovend("74"),
	LocSoftware("75"),
	HostToHost("76"),
	Ingenico3500("77"),
	AppliedTech("78"),
	SunTronicsLease("79"),
	SunTronicsDial("80"),
	Schlumberger("81"),
	Fiscal("82"),
	NcrSystem("83"),
	AutogasDial("84"),
	EDH711FOR8583("86"),
	VerifoneCommander("87"),
	VerifoneRuby2Ci("88"),
    VERFIFONEC18("89"),
	VerifoneOmni380("90"),
	IngenicoMove5000("91"),
	VerifoneRubyDial("92"),
	VerifoneSapphire("93"),
	VerifoneOmni490Isdn("94"),
	VerifoneOmni490Dial("95"),
	SchlumbergerPro("98"),
	SDKMobilePayment("S1"),
	SDKDex("S2"),
	SdkBrPos("S3"),
	DexDispenserExperience("S2"),
	SdkBrpos("S3"),
	AnyPinpadsWith7MDOr7POS("N1"),
	DEXWithAnyCRINDsOrDispensers("N2");
	String value;
	TerminalType(String value) {
        this.value = value;
    }
    public String getValue() { return this.value; }
    public byte[] getBytes() { return this.value.getBytes(); }
}
