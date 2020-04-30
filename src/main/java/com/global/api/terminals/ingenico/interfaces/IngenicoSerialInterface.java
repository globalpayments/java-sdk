package com.global.api.terminals.ingenico.interfaces;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.Character.UnicodeScript;

import java.nio.charset.Charset;
import java.util.*;
import java.util.ResourceBundle.Control;

import com.global.api.entities.enums.ControlCodes;
import com.global.api.entities.enums.PortNames;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.entities.exceptions.MessageException;
import com.global.api.terminals.TerminalUtilities;
import com.global.api.terminals.abstractions.IDeviceCommInterface;
import com.global.api.terminals.abstractions.IDeviceMessage;
import com.global.api.terminals.abstractions.ITerminalConfiguration;
import com.global.api.terminals.ingenico.responses.BroadcastMessage;
import com.global.api.terminals.ingenico.variables.INGENICO_GLOBALS;
import com.global.api.terminals.messaging.IBroadcastMessageInterface;
import com.global.api.terminals.messaging.IMessageSentInterface;
//import com.mgnt.utils.StringUnicodeEncoderDecoder;
import com.global.api.utils.MessageWriter;

import com.global.api.entities.enums.IByteConstant;
import com.global.api.entities.enums.IStringConstant;

import java.util.ArrayList;
import java.util.List;

import com.fazecast.jSerialComm.*;

public class IngenicoSerialInterface implements IDeviceCommInterface {

	public	  IMessageSentInterface OnMessageSent;
	public	  IBroadcastMessageInterface OnBroadcastMessage;

	private ITerminalConfiguration _settings;
	private String buffer = "";
	private String appendReport = "";
	public static List<Byte> messageResponse;

	// Reader Thread
	public static Thread reader;

	// Serial Related Definitions
	public static SerialPort _serialport;
	public static SerialPort _serialportloc;
	public static SerialPort _serialportEnum[];
	
	private static BufferedReader _serialinput;
	private static OutputStream _serialoutput;

	// Data buffers
	private static int rawdata;
	public static char[] rawdataStr = new char[20000];

	public static IDeviceMessage recievedMsg;
	public static String finaStr;

	// Flags
	public static Boolean dataflag = false; // Data or Code Flag
	public static Boolean killthread = false; // kill thread
	public static Boolean resThread = false; // reset thread
	public static Boolean msgRcvd = false; // message Received
	public static Boolean portOpened = false; // if port is opened

	// Counters
	protected static int recCount = 0; // Code received instance counter
	protected static int datCount = 0; // Data received counter
	protected static int broadcstCnt = 0; // Broadcast counter
	protected static int msgrspcnt = 0; // Message Response Char Counter

	public IngenicoSerialInterface(ITerminalConfiguration settings, IBroadcastMessageInterface _onBroadcastMessage) throws ConfigurationException {
		this._settings = settings;
		this.OnBroadcastMessage = _onBroadcastMessage;
		recCount = 0;
		killthread = false;
		msgRcvd = false;
		dataflag = false;
		// Restart Thread
		if((portOpened)&&(resThread == true)){
			reader = new Thread(new serialThread(), "serialThreadnew");
			reader.start();
			// Send message Port Already opened
			resThread = false;
			return;
		}
		connect();
	}

	public void connect() throws ConfigurationException {

		// Get Host Port Number *to be adjusted for Linux
		String OS = System.getProperty("os.name");
		String portname = "";
		if (OS.startsWith("Windows"))
			portname = "COM";
		else
			portname = "ttyS";
		
		portname += _settings.getPort();
		_serialportEnum = _serialport.getCommPorts();
		for (SerialPort portemp : _serialportEnum) {
			if (portemp.getSystemPortName().contains(portname)) {
				_serialportloc = portemp;
				break;
			}
		}
		if (_serialportloc == null) {
			// Send Message to Connect Device
			throw new ConfigurationException("Terminal not connected.");
		}

		if(portOpened){
			// Send message Port Already opened
			throw new ConfigurationException("Terminal already opened.");
		}
			//Open Port
			reader = new Thread(new serialThread(), "serialThread");
			// Open Port
			portOpened = _serialportloc.openPort();
			_serialportloc.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 300, 0);
			_serialportloc.setComPortParameters(_settings.getBaudRate().getValue(), _settings.getDataBits().getValue(),
			_settings.getStopBits().getValue(), _settings.getParity().getValue());
			// Create Streams
			_serialinput = new BufferedReader(new InputStreamReader(_serialportloc.getInputStream()));
			_serialoutput = _serialportloc.getOutputStream();
			
//			Start the thread
			reader.start(); 

	
		return;
	}

	public void disconnect() {
		if (portOpened) {

			_serialportloc.closePort();
			_serialportloc = null;
			portOpened = false;

			
		}else {
			// Serial Port Not Disconnected
		}
		if (!killthread)
			killthread = true;
 		return;
	}
	
	private Boolean validateResponseLRC(String calculate, String actual) {
		Boolean response = false;

		byte[] calculateLRC = TerminalUtilities.calculateLRC(calculate);
		byte[] actualLRC = TerminalUtilities.calculateLRC(actual);

		if (actualLRC[0] == calculateLRC[0])
			response = true;

		return response;
	}
	
	public class serialThread implements Runnable {
			
			
			public serialThread() {
				
			}

			public void stopRun() {
				killthread = true;
			}
			
			public void run() {
				int enquirycnt = 0;
				do {

					// Thread delay
					try {
						if(datCount < 100) {
							Thread.sleep(0100);
						}
						// Checking if serialport is open
						if (!portOpened) {
							stopRun();
							continue;
						}

						// Checking if input is ready
						if (_serialinput.ready()) {
							// read raw data
							rawdata = _serialinput.read();
							if ((byte)rawdata == ControlCodes.STX.getByte()){
								dataflag = true;
							}
							// Check if message is Control Code or Data
							if (dataflag) {

								// double Check is NAK was read
								if ((byte)rawdata == ControlCodes.NAK.getByte()) {
									codeReceived(rawdata);
									continue;
								}

								// Store Data
								rawdataStr[datCount] = (char) rawdata;
								datCount++;

								// Determine End of data here
								if (((byte)rawdata == ControlCodes.ETX.getByte())&&(datCount != 0)){
								
									// convert to string
									finaStr = new String(rawdataStr);
									// Data Checking
									if (finaStr.contains(new INGENICO_GLOBALS().BROADCAST)) {
										datCount = 0;
										recCount = 0;
										BroadcastMessage broadcastMessage = new BroadcastMessage(finaStr.getBytes());
										if (OnBroadcastMessage != null) {
											OnBroadcastMessage.broadcastReceived(broadcastMessage.getCode(), broadcastMessage.getMessage());
										}
									} else if ((finaStr.contains("CREDIT_CARD_RECEIPT")) || (finaStr.contains("LF"))||(finaStr.contains("Split_sale"))) {
										messageResponse = new ArrayList<Byte>(datCount);
										messageResponse.clear();
										
//										appendReport += finaStr.substring(0, datCount);
//										String xmlData = appendReport.substring(1, datCount -3);
//										rawdataStr = xmlData.toCharArray();
										
											msgRcvd = MessageReceived(rawdataStr);									
											if (msgRcvd)
												recCount = 1;
										if (!resThread)
											resThread = true;
										datCount = 0;							
									} else {									
										messageResponse = new ArrayList<Byte>(datCount);
										messageResponse.clear();
										if ((finaStr.contains("xml"))||(finaStr.contains("?"))){
											datCount = 0;
											recCount = 0;
											broadcstCnt++;
										}
										else {
											// LRC Validation
											String rData = finaStr.substring(1, datCount - 3);
											buffer = finaStr.substring(1, datCount - 3);
											Boolean lrcResult = validateResponseLRC(rData, buffer);
											if (lrcResult) {
												msgRcvd = MessageReceived(rawdataStr);
												if (msgRcvd)
													recCount = 1;
											}
											datCount = 0;
										}
									}
									
									codeReceived(rawdata);
									Arrays.fill(rawdataStr,'\0');
									finaStr = " ";
									dataflag = false;
								}
								else {
									if ((byte)rawdata == ControlCodes.ETX.getByte()) {
										dataflag = false;
									}
								}
							} else {
								codeReceived(rawdata);
								
							}

						} else {

							// Data read Retry
							Thread.sleep(0500);
							if (rawdata != 0)
								codeReceived(rawdata);
							else {
								if(msgRcvd) {
									stopRun();
								}
									
							}
								
//							if (enquirycnt == 3){
//								throw new MessageException(
//									"Terminal did not respond in Enquiry for three (3) times. Send aborted.", null);
//								continue;
//							}							
//							byte[] cCodebyte = new byte[] {ControlCodes.ENQ.getByte()};
//							_serialoutput.write(cCodebyte, 0, 1);
//							enquirycnt++;
						}
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} // End of Try and Catch
					catch (IOException e) {
						// Retry zero value read
						e.printStackTrace();
					} catch (MessageException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				} while (!killthread);
			} // End public void run() {

	} // End of serialThread

	@SuppressWarnings("static-access")
	public static void codeReceived(int code) {
		try {
			ControlCodes codeRef = null;
			ControlCodes cCode = null;
			for (ControlCodes d : codeRef.values()) {
				if (d.getByte() == (byte) code) {
					cCode = d;
					break;
				}
			}
			
			if (cCode == null) {
				// Data Received 
				return;
			}

			switch (cCode) {
			case ACK:
			case NAK:
				// Acknowledge Received

				if (recCount < 1) {

					// Send Message
					if (recievedMsg != null) {
					byte[] msg = recievedMsg.getSendBuffer();
					_serialoutput.write(msg, 0, msg.length);
					}


					// continue; need call back here

				} else if (recCount == 1) {

					// Send inquiry
					byte[] cCodebyte = new byte[] {cCode.EOT.getByte()};
					_serialoutput.write(cCodebyte, 0, 1);
					recCount = 0;
				}
				recCount++;
				rawdata = 0;
				break;
			case ENQ:
				// Inquiry Received

				// Send Acknowledge
				byte[] cCodebyte = new byte[] {cCode.ACK.getByte()};
				_serialoutput.write(cCodebyte, 0, 1);

				// Reset read buffer
				rawdata = 0;
				datCount = 0;
				break;
			case EOT:
				// End of Data Transmission Receive

				// First EOT Received
				if (recCount < 1) {

					rawdata = 0;
					dataflag = false;
					broadcstCnt ++;
					// Second EOT Received
				} else if (recCount == 1) {
					recCount = 0;
					rawdata = 0;
					broadcstCnt = 0;
					
					if (msgRcvd) {
						byte[] dataByte = new byte[] { cCode.ACK.getByte() };
						_serialoutput.write(dataByte, 0, 1);
						msgRcvd = false;
						killthread = true;
					}
					// End of Receive -> Kill task
					Thread.sleep(0500);
					return;
				}
				recCount++;
				break;
			default:
				// Data Received Trash recieved
				break;
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	public byte[] send(IDeviceMessage message) {
		recievedMsg = message;
		Thread  local = null;
		if (recievedMsg != null) {
			String sentmsg = new String (recievedMsg.getSendBuffer());
			sentmsg = sentmsg.substring( 1, sentmsg.length() - 2);
			if (OnMessageSent != null)
				OnMessageSent.messageSent(sentmsg);
		}
		
		Set<Thread> threads = Thread.getAllStackTraces().keySet();
		for (Thread t : threads) {
			String name = t.getName();
			int id = (int) t.getId();
 			if (name == "serialThread") {
			    local = t;
			    break;
			}
		}
		byte[] cCodebyte = new byte[] {ControlCodes.ENQ.getByte()};		
		if (local == null) {
			try {
				resThread = true;
				new IngenicoSerialInterface(_settings, OnBroadcastMessage);
				ThreadGroup rootGroup = Thread.currentThread().getThreadGroup();
				Thread[] threads2 = new Thread[rootGroup.activeCount()];	
				Thread.currentThread().getThreadGroup().enumerate(threads2);
				for (Thread t : threads2) {
					String name = t.getName();
					int id = (int) t.getId();
		 			if (name == "serialThreadnew") {
					    local = t;
					    break;
					}
				}
			} catch (ConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		try {
			Thread.sleep(1000);
			_serialoutput.write(cCodebyte, 0, 1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (local != null) {
			// Checking of Thread
			if (local.isAlive()) {
				killthread = false;
				local.run();
			} else {

				// Start reader thread
				local.start();

			}
		}
//			To be updated
//          if (!reader.wait(_settings.getTimeout())) {
//          	throw new MessageException("Terminal did not response within timeout.");
//    		 }
			if (messageResponse == null)
				System.out.print(messageResponse + " is null \n");
			
			byte[] msgRspns = new byte[messageResponse.size()];
			Object[] tempoobj = messageResponse.toArray();
	        for(int i = 0; i < msgrspcnt; i++) {
	        	msgRspns[i] = (Byte)tempoobj[i];
	        }
			return msgRspns;
	}

//
	private boolean MessageReceived(char[] rawdataStr2) {
	  msgrspcnt = 0;
	  if (messageResponse == null)
          return false;
      for (char b : rawdataStr2) {
    	  if (msgrspcnt > datCount) {
  	  		  break;
    	  }
    	  
    	  byte locByte = (byte)b;
    	  if ((locByte == ControlCodes.STX.getByte())||(locByte == ControlCodes.ETX.getByte()))
    		  continue;
    	  messageResponse.add(locByte);
    	  msgrspcnt++;
      }
         return true;
	}

	public void setMessageSentHandler(IMessageSentInterface messageInterface) {
		OnMessageSent = messageInterface;
		return;
	}

	public void Start() {
		throw new UnsupportedOperationException();
	}

	public void Stop() {
		throw new UnsupportedOperationException();
	}

	public int Count() {
		throw new UnsupportedOperationException();
	}

	public void setBroadcastMessageHandler(IBroadcastMessageInterface broadcastInterface) {
		// TODO Auto-generated method stub
		OnBroadcastMessage = broadcastInterface;
		
	}
}
