package com.global.api.terminals.ingenico.pat;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.global.api.entities.exceptions.ApiException;
import com.global.api.terminals.ingenico.variables.INGENICO_GLOBALS;
import com.global.api.terminals.ingenico.variables.PATPrivateDataCode;
import com.global.api.terminals.ingenico.variables.PATRequestType;
import com.global.api.terminals.ingenico.variables.TLVFormat;
import com.global.api.utils.TypeLengthValue;

public class PATRequest {
	private TypeLengthValue _tlv;
	private PATRequestType _requestType;
	private TransactionOutcome _transactionOutcome;

	private String _rawData;
	private String _waiterId;
	private String _tableNumber;
	private String _terminalId;
	private String _terminalCurrency;
	private String _xmlData;

	public PATRequest(byte[] buffer) throws ApiException {
		parseRequest(buffer);
	}

	public String getWaiterId() {
		return _waiterId;
	}

	public String getTableId() {
		return _tableNumber;
	}

	public String getTerminalId() {
		return _terminalId;
	}

	public String getTerminalCurrency() {
		return _terminalCurrency;
	}

	public TransactionOutcome getTransactionOutcome() {
		return _transactionOutcome;
	}

	public PATRequestType getRequestType() {
		return _requestType;
	}

	public String getXMLData() {
		return _xmlData;
	}

	private void parseRequest(byte[] buffer) throws ApiException {
		try {
			if (buffer != null) {
				_rawData = new String(buffer, StandardCharsets.UTF_8);

				// XML Format
				if (_rawData.contains(new INGENICO_GLOBALS().XML_TAG)) {
					_rawData = new String(_rawData.getBytes(), StandardCharsets.ISO_8859_1);

					if (!_rawData.endsWith(">")) {
						char[] xmlContentArr = _rawData.toCharArray();

						for (int i = _rawData.length() - 1; i <= _rawData.length(); i--) {
							if (xmlContentArr[i] == '>') {
								_xmlData = _rawData.substring(0, (i + 1));
								break;
							}
						}
					} else {
						_xmlData = _rawData;
					}

					DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
					DocumentBuilder builder = factory.newDocumentBuilder();
					Document doc = builder.parse(new InputSource(new StringReader(_xmlData)));

					String rootTag = doc.getDocumentElement().getNodeName();

					if (rootTag.equals(new INGENICO_GLOBALS().ADDITIONAL_MSG_ROOT)) {
						_requestType = PATRequestType.ADDITIONAL_MESSAGE;
					} else if (rootTag.equals(new INGENICO_GLOBALS().TRANSFER_DATA_REQUEST)) {
						_requestType = PATRequestType.TRANSFER_DATA;
					} else if (rootTag.equals(new INGENICO_GLOBALS().TRANSACTION_XML)) {
						NodeList nList = doc.getElementsByTagName("RECEIPT");
						Node node = nList.item(0);

						if (node.getNodeType() == Node.ELEMENT_NODE) {
							Element element = (Element) node;
							String sType = element.getAttribute("STYPE");

							if (sType.equals("SPLITSALE REPORT")) {
								_requestType = PATRequestType.SPLITSALE_REPORT;
							} else if (sType.equals("CUSTOMER")) {
								_requestType = PATRequestType.TICKET;
							} else {
								_requestType = PATRequestType.EOD_REPORT;
							}
						} else {
							throw new ApiException("First child node is not an element");
						}
					} else {
						throw new ApiException("The root tag of the xml cannot recognize");
					}
				} else {
					// Workaround for split sale but not final logic
					if (_rawData.toLowerCase().contains("split_sale")) {
						_requestType = PATRequestType.SPLITSALE_REPORT;
						_xmlData = _rawData;
					}
					
					// Message Frame 2 Format
					else if (buffer.length >= 80) {
						_requestType = PATRequestType.TRANSACTION_OUTCOME;
						_transactionOutcome = new TransactionOutcome(buffer);
					} else {
						// Message Frame 1 Format
						Integer type = Integer.parseInt(_rawData.substring(11, 12));
						_requestType = PATRequestType.getEnumName(type);

						String privData = _rawData.substring(16);
						if (privData.length() < 55) {
							switch (_requestType) {
							case TABLE_LOCK:
							case TABLE_UNLOCK:
								_tableNumber = privData;
								break;
							default:
								break;
							}
						} else {
							_tlv = new TypeLengthValue(privData.getBytes());

							_waiterId = (String) _tlv.getValue((byte) PATPrivateDataCode.WaiterId.getValue(),
									String.class, null);
							_tableNumber = (String) _tlv.getValue((byte) PATPrivateDataCode.TableId.getValue(),
									String.class, TLVFormat.PayAtTable);
							_terminalId = (String) _tlv.getValue((byte) PATPrivateDataCode.TerminalId.getValue(),
									String.class, null);
							_terminalCurrency = (String) _tlv.getValue(
									(byte) PATPrivateDataCode.TerminalCurrency.getValue(), String.class, null);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
