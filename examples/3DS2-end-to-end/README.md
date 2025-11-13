<a href="https://github.com/globalpayments" target="_blank">
    <img src="https://developer.globalpay.com/static/media/logo.dab7811d.svg" alt="Global Payments logo" title="Global Payments" align="right" width="225" />
</a>

# Global Payments Java SDK - End to End example.

This sample code is not specific to the Global Payments SDK and is intended as a simple example and
should not be treated as Production-ready code. You'll need to add your own message parsing and
security in line with your application or website

This is a java web application that uses Global Payments Java SDK. 

## General

* What is this example
* Requirements
* How to build this example
* How to deploy this example
* How to use this example
* Test credit card numbers
  
### What is this example

* This is a functional java web application that shows the basics on how to use the Global Payments Java SDK

### Requirements

* Same requirements that you need for Global Payments Java SDK, plus:
* A Java Web Server that listen over https.
* Update the method 'getBaseUrl' in 'ApplicationParameters' class to return your application base url. 
It should not be localhost or 127.0.0.1.

## How to build this example

* cd 3DS2-end-to-end
* mvn clean install
* It produces a WAR file that will be placed here: 3DS2-end-to-end\target
* The WAR name will be: 3DS2-end-to-end.war

## How to deploy this example

* Deploy the WAR file to your java web application server

## How to use this example

* In a web browser, hit the base url you configured before.
For instance: https://example.com:8443/3DS2-end-to-end

## Test credit card numbers

* Challenge: 4012001038488884
* Frictionless: 4263970000005262
* Frictionless: 4012001037461114 (AUTHENTICATION_FAILED)
* Challenge: 4012001038488884 (When presented the challenge click on cancel to simulate challenge failed)
