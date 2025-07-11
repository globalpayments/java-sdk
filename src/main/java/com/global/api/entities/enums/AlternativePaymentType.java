package com.global.api.entities.enums;

public enum AlternativePaymentType implements IStringConstant{
	
	ASTROPAY_DIRECT("astropaydirect"),
	AURA("aura"),
	BALOTO_CASH("baloto"),
	BANAMEX("banamex"),
	BANCA_AV_VILLAS("bancaavvillas"),
	BANCA_CAJA_SOCIAL("bancacajasocial"),
	BANCO_GNB_SUDAMERIS("bancagnbsudameris"),
	BANCO_CONSORCIO("bancoconsorcio"),
	BANCO_COOPERATIVO_COOPCENTRAL("bancocooperativocoopcentral"),
	BANCO_CORPBANCA("bancocorpbanca"),
	BANCO_DE_BOGOTA("bancodebogota"),
	BANCO_DE_CHILE_EDWARDS_CITI("bancodechile"),
	BANCO_DE_CHILE_CASH("bancodechilecash"),
	BANCO_DE_OCCIDENTE("bancodeoccidente"),
	BANCO_DE_OCCIDENTE_CASH("bancodeoccidentecash"),
	BANCO_DO_BRASIL("bancodobrasil"),
	BANCO_FALABELLA_Chile("bancofalabellachile"),
	BANCO_FALABELLA_Columbia("bancofalabellacolumbia"),
	BANCO_INTERNATIONAL("bancointernational"),
	BANCO_PICHINCHA("bancopichincha"),
	BANCO_POPULAR("bancopopular"),
	BANCO_PROCREDIT("bancoprocredit"),
	BANCO_RIPLEY("bancoripley"),
	BANCO_SANTANDER("bancosantander"),
	BANCO_SANTANDER_BANEFE("bancosantanderbanefe"),
	BANCO_SECURITY("bancosecurity"),
	BANCOBICE("bancobice"),
	BANCOESTADO("bancoestado"),
	BANCOLOMBIA("bancolombia"),
	BANCOMER("bancomer"),
	BANCONTACT_MR_CASH("bancontact"),
	BANCOOMEVA("bancoomeva"),
	BANK_ISLAM("bankislam"),
	BANK_TRANSFER("banktransfer"),
	BBVA_Chile("bbvachile"),
	BBVA_Columbia("bbvacolumbia"),
	BCI_TBANC("bcitbanc"),
	BITPAY("bitpay"),
	BOLETO_BANCARIO("boletobancario_"),
	BRADESCO("bradesco"),
	CABAL("cabal_"),
	CARTAO_MERCADOLIVRE("cartaomercadolivre"),
	CARULLA("carulla"),
	CENCOSUD("cencosud"),
	CHINA_UNION_PAY("unionpay"),
	CIMB_CLICKS("cimbclicks"),
	CITIBANK("citibank"),
	CMR("cmr"),
	COLPATRIA("colpatria"),
	COOPEUCH("coopeuch"),
	CORPBANCA("corpbanca"),
	DANSKE_BANK("danskebank"),
	DAVIVIENDA("davivienda"),
	DRAGONPAY("dragonpay"),
	EASYPAY("easypay"),
	EFECTY("efecty"),
	ELO("elo"),
	EMPRESA_DE_ENERGIA_DEL_QUINDIO("empresadeenergia"),
	ENETS("enets"),
	ENTERCASH("entercash"),
	E_PAY_PETRONAS("epaypetronas"),
	EPS("EPS"),
	ESTONIAN_ONLINE_BANK_TRANSFER("estonianbanks"),
	FINLAND_ONLINE_BANK_TRANSFER("finlandonlinebt"),
	GIROPAY("giropay"),
	HANDELSBANKEN("handelsbanken"),
	HELM_BANK("helm"),
	HIPERCARD("hipercard"),
	HONG_LEONG_BANK("hongleongbank"),
	IDEAL("ideal"),
	INDONESIA_ATM("indonesiaatm"),
	INSTANT_TRANSFER("instanttransfer"),
	INTERNATIONAL_PAY_OUT("intpayout"),
	ITAU_BRAZIL("itaubrazil"),
	ITAU_CHILE("itauchile"),
	O("latvianbt"),
	LINK("link"),
	LITHUANIAN_ONLINE_BANK_TRANSFER("lituanianbt"),
	MAGNA("magna"),
	MAXIMA("maxima"),
	MAYBANK2U("maybank2u"),
	MULTIBANCO("multibanco"),
	MYBANK("mybank"),
	MYCLEAR_FPX("myclearfpx"),
	NARANJA("naranja"),
	NARVESEN_LIETUVOS_SPAUDA("narvesen"),
	NATIVA("nativa"),
	NORDEA("nordea"),
	OSUUSPANKKI("osuuspankki"),
	OXXO("oxxo"),
	PAGO_FACIL("pagofacil"),
	PAYPAL("paypal"),
	PAYPOST_LIETUVOS_PASTAS("paypost"),
	PAYSAFECARD("paysafecard"),
	PAYSBUY_CASH("paysbuy"),
	PAYSERA("paysera"),
	PAYU("payu"),
	PERLAS("perlas"),
	POLI("poli"),
	POLISH_PAYOUT("polishpayout"),
	POP_PANKKI("poppankki"),
	POSTFINANCE("postfinance"),
	PRESTO("presto"),
	PROVINCIA_NET("provincianet"),
	PRZELEWY24("p24"),
	PSE("pse"),
	QIWI("qiwi"),
	QIWI_PAYOUT("qiwipayout"),
	RAPI_PAGO("rapipago"),
	REDPAGOS("redpagos"),
	RHB_BANK("rhbbank"),
	SAASTOPANKKI("sasstopankki"),
	SAFETYPAY("safetypay"),
	SANTANDER_BRAZIL("santanderbr"),
	SANTANDER_MEXICO("santandermx"),
	SANTANDER_RIO("santanderrio"),
	SCOTIABANK("scotiabank"),
	SEPA_DIRECTDEBIT_MERCHANT_MANDATE_MODEL_C("sepamm"),
	SEPA_DIRECTDEBIT_PPPRO_MANDATE_MODEL_A("sepapm"),
	SEPA_PAYOUT("sepapayout"),
	SERVIPAG("servipag"),
	SINGPOST("singpost"),
	SKRILL("skrill"),
	SOFORTUBERWEISUNG("sofort"),
	S_PANKKI("spankki"),
	SURTIMAX("surtimax"),
	TARJETA_SHOPPING("tarjeta"),
	TELEINGRESO("teleingreso"),
	TESTPAY("testpay"),
	TRUSTLY("trustly"),
	TRUSTPAY("trustpay"),
	WEBMONEY("webmoney"),
	WEBPAY("webpay"),
	WECHAT_PAY("wechatpay"),
	ZIMPLER("zimpler"),
	UK_DIRECT_DEBIT("ukdirectdebit"),
	PAYBYBANKAPP("paybybankapp"),
	ALIPAY("alipay"),
	BLIK("blik");
	
    String value;
    AlternativePaymentType(String value) { this.value = value; }
    public byte[] getBytes() { return value.getBytes(); }
    public String getValue() { return value; }

	public static AlternativePaymentType fromValue(String value) {
		for (AlternativePaymentType apt : AlternativePaymentType.values()) {
			if (apt.getValue().equals(value)) {
				return apt;
			}
		}
		return null;
	}
}
