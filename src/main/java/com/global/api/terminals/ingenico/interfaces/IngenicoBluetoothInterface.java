package com.global.api.terminals.ingenico.interfaces;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import javax.bluetooth.DataElement;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;

import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.terminals.abstractions.IDeviceCommInterface;
import com.global.api.terminals.abstractions.IDeviceMessage;
import com.global.api.terminals.abstractions.ITerminalConfiguration;
import com.global.api.terminals.messaging.IBroadcastMessageInterface;
import com.global.api.terminals.messaging.IMessageSentInterface;
import com.global.api.terminals.messaging.IPayAtTableRequestInterface;
import com.global.api.utils.MessageWriter;

public class IngenicoBluetoothInterface implements IDeviceCommInterface {
	private Object lock = new Object();
	private MessageWriter messageResponse;
	private boolean serviceNotifier;

	private InputStream in;
	private OutputStream out;

	private String deviceKnown;
	private StreamConnection streamConn;
	private LocalDevice localDevice;
	private DiscoveryAgent agent;
	private Object serviceName;
	private String serviceURL;

	private IMessageSentInterface onMessageSent;
	private IBroadcastMessageInterface onBroadcastMessage;

	private DeviceDiscoveryListener listener;

	public IngenicoBluetoothInterface(ITerminalConfiguration settings) throws ConfigurationException {
		listener.devices = new ArrayList<RemoteDevice>();
		connect();
	}

	public void setMessageSentHandler(IMessageSentInterface onMessageSent) {
		this.onMessageSent = onMessageSent;
	}

	public void setBroadcastMessageHandler(IBroadcastMessageInterface onBroadcastMessage) {
		this.onBroadcastMessage = onBroadcastMessage;
	}

	public void connect() throws ConfigurationException {
		try {
			localDevice = LocalDevice.getLocalDevice();
			agent = localDevice.getDiscoveryAgent();
			agent.startInquiry(DiscoveryAgent.GIAC, listener);

			synchronized (lock) {
				wait();
			}

			UUID[] uuidSet = new UUID[1];
			uuidSet[0] = new UUID(0x1105); // OBEX Object Push
			int[] attrIds = new int[] { 0x0100 }; // Service Name

			for (RemoteDevice d : listener.devices) {
				agent.searchServices(attrIds, uuidSet, d, listener);

				synchronized (lock) {
					wait();
				}
			}

			if (serviceNotifier) {
				streamConn = (StreamConnection) Connector.open(serviceURL);
				in = streamConn.openInputStream();
				out = streamConn.openOutputStream();
			}
		} catch (Exception e) {
			throw new ConfigurationException(e.getMessage());
		}
	}

	public void disconnect() {
		try {
			streamConn.close();
			in.close();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public byte[] send(IDeviceMessage message) throws ApiException {
		if (!serviceNotifier) {
			throw new ApiException("Service not connected.");
		}

		sendMessage(serviceURL);
		return messageResponse.toArray();
	}

	private class DeviceDiscoveryListener implements DiscoveryListener {
		public ArrayList<RemoteDevice> devices;

		public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
			try {
				String deviceName = btDevice.getFriendlyName(false);
				String deviceAddress = btDevice.getBluetoothAddress();

				deviceKnown = deviceName == null ? deviceAddress : deviceName;
			} catch (IOException e) {
				deviceKnown = btDevice.getBluetoothAddress();
			}
		}

		public void inquiryCompleted(int discType) {
			synchronized (lock) {
				lock.notify();
			}
		}

		public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
			for (int i = 0; i < servRecord.length; i++) {
				String url = servRecord[i].getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
				if (url == null) {
					continue;
				}

				DataElement servName = servRecord[i].getAttributeValue(0x0100);
				if (servName != null) {
					serviceName = servName.getValue();

					if (serviceName == "OBEX Object Push") {
						serviceURL = url;
						serviceNotifier = true;
					}
				}
			}
		}

		public void serviceSearchCompleted(int transID, int respCode) {
			synchronized (lock) {
				lock.notify();
			}
		}
	}

	private void sendMessage(String serviceURL) throws ApiException {
		try {
			byte[] buffer;
			messageResponse = new MessageWriter();

		} catch (Exception e) {
			throw new ApiException(e.getMessage());
		}
	}

	public void setOnPayAtTableRequestHandler(IPayAtTableRequestInterface onPayAtTable) {
		// TODO Auto-generated method stub
		
	}

//	private void deviceInquiry() {
//		try {
//			localDevice = LocalDevice.getLocalDevice();
//			agent = localDevice.getDiscoveryAgent();
//			agent.startInquiry(DiscoveryAgent.GIAC, listener);
//
//			synchronized (lock) {
//				wait();
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//
//	private void serviceSearch() {
//		try {
//			UUID[] uuidSet = new UUID[1];
//			uuidSet[0] = new UUID(0x1105); // OBEX Object Push
//			int[] attrIds = new int[] { 0x0100 }; // Service Name
//
//			for (RemoteDevice d : listener.devices) {
//				agent.searchServices(attrIds, uuidSet, d, listener);
//
//				synchronized (lock) {
//					wait();
//				}
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
}
