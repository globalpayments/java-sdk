package com.global.api.gateways.bill_pay;

import com.global.api.entities.billing.Credentials;
import com.global.api.gateways.XmlGateway;
import com.global.api.utils.Element;
import com.global.api.utils.ElementTree;

public abstract class GatewayRequestBase extends XmlGateway {
    protected Credentials credentials;

    /// <summary>
    /// Creates a SOAP envelope with the necessary namespaces
    /// </summary>
    /// <param name="soapAction">The method name that is the target of the invocation</param>
    /// <returns>The Element that represents the envelope node</returns>
    protected Element createSOAPEnvelope(ElementTree et, String soapAction)
    {
        setSOAPAction(soapAction);
        addXMLNS(et);

        return et.element("soapenv:Envelope");
    }

    /// <summary>
    /// Creates and sets the SOAPAction header using the supplied method name
    /// </summary>
    /// <param name="soapAction">The method name that is the target of the invocation</param>
    protected void setSOAPAction(String soapAction)
    {
        this.headers.put("SOAPAction", "https://test.heartlandpaymentservices.net/BillingDataManagement/v3/BillingDataManagementService/IBillingDataManagementService/" + soapAction);
    }

    /// <summary>
    /// Adds the XML Namespaces neccessary to make BillPay SOAP requests
    /// </summary>
    /// <param name="et">The element tree for the SOAP request</param>
    protected void addXMLNS(ElementTree et)
    {
        et.addNamespace("soapenv", "http://schemas.xmlsoap.org/soap/envelope/");
        et.addNamespace("bil", "https://test.heartlandpaymentservices.net/BillingDataManagement/v3/BillingDataManagementService");
        et.addNamespace("bdms", "http://schemas.datacontract.org/2004/07/BDMS.NewModel");
        et.addNamespace("hps", "http://schemas.datacontract.org/2004/07/HPS.BillerDirect.ACHCard.Wrapper");
        et.addNamespace("pos", "http://schemas.datacontract.org/2004/07/POSGateway.Wrapper");
        et.addNamespace("bdm", "https://test.heartlandpaymentservices.net/BillingDataManagement/v3/BDMServiceAdmin");
    }
}
