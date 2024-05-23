/*
 * Copyright (c) 2020 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.helper;

import de.docware.util.security.PasswordString;

import javax.xml.namespace.QName;
import javax.xml.soap.*;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.util.Set;

public class BomDBDefaultServiceHandler implements SOAPHandler<SOAPMessageContext> {

    protected static final String URI_ADRESSING = "http://www.w3.org/2005/08/addressing";
    protected static final String URI_OASIS_BASE = "http://docs.oasis-open.org/wss/2004/01/";
    protected static final String URI_WSSA = URI_OASIS_BASE + "oasis-200401-wss-wssecurity-secext-1.0.xsd";
    protected static final String URI_TYPE = URI_OASIS_BASE + "oasis-200401-wss-username-token-profile-1.0#PasswordText";
    protected static final String S_ACTOR_NAME = "engbus";
    protected static final String S_MUST_UNDERSTAND_VALUE = "0";

    protected static final String PREFIX_WSA = "wsa";
    protected static final String PREFIX_WSSE = "wsse";
    protected static final String PARAM_NAME_FROM = "From";
    protected static final String PARAM_NAME_ADDRESS = "Address";
    protected static final String PARAM_NAME_APP_TOKEN = "appToken:";
    protected static final String PARAM_NAME_SECURITY = "Security";
    protected static final String PARAM_NAME_S_ACTOR = "S:" + "actor";
    protected static final String PARAM_NAME_S_MUST_UNDERSTAND = "S:" + "mustUnderstand";
    protected static final String PARAM_NAME_USERNAME_TOKEN = "UsernameToken";
    protected static final String PARAM_NAME_USERNAME = "Username";
    protected static final String PARAM_NAME_PASSWORD = "Password";
    protected static final String PARAM_NAME_TYPE = "Type";

    protected String username;
    protected PasswordString password;
    protected String appToken;

    public BomDBDefaultServiceHandler(String username, PasswordString password, String appToken) {
        this.username = username;
        this.password = password;
        this.appToken = appToken;
    }

    @Override
    public boolean handleMessage(SOAPMessageContext context) {
        SOAPMessage msg = context.getMessage();
        Object outboundProperty = context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        if ((outboundProperty instanceof Boolean) && (Boolean)outboundProperty) {
            try {
                SOAPEnvelope envelope = context.getMessage().getSOAPPart().getEnvelope();
                SOAPFactory factory = SOAPFactory.newInstance();

                SOAPElement fromElem = createFromElement(factory);
                SOAPElement securityElem = createSecurityElement(factory);

                addSoapElements(envelope, fromElem, securityElem);

                msg.saveChanges();
            } catch (SOAPException e) {
                return false;
            }
        }
        return true;
    }

    protected void addSoapElements(SOAPEnvelope envelope, SOAPElement... elements) throws SOAPException {
        SOAPHeader header = envelope.getHeader();
        for (SOAPElement element : elements) {
            header.addChildElement(element);
        }
    }

    protected SOAPElement createFromElement(SOAPFactory factory) throws SOAPException {
        String prefixWsa = PREFIX_WSA;
        String uriAddressing = URI_ADRESSING;

        // FROM-Element für Request-XML-Payload
        SOAPElement fromElem = factory.createElement(PARAM_NAME_FROM, prefixWsa, uriAddressing);
        SOAPElement addressElem = factory.createElement(PARAM_NAME_ADDRESS, prefixWsa, uriAddressing);
        addressElem.addTextNode(PARAM_NAME_APP_TOKEN + appToken);
        fromElem.addChildElement(addressElem);
        return fromElem;
    }

    protected SOAPElement createSecurityElement(SOAPFactory factory) throws SOAPException {
        String prefixWsse = PREFIX_WSSE;
        String uriWsse = URI_WSSA;

        // SECURITY-Element für Request-XML-Payload
        SOAPElement securityElem;
        securityElem = factory.createElement(PARAM_NAME_SECURITY, prefixWsse, uriWsse);
        securityElem.addAttribute(QName.valueOf(PARAM_NAME_S_ACTOR), S_ACTOR_NAME);
        securityElem.addAttribute(QName.valueOf(PARAM_NAME_S_MUST_UNDERSTAND), S_MUST_UNDERSTAND_VALUE);
        SOAPElement tokenElem = factory.createElement(PARAM_NAME_USERNAME_TOKEN, prefixWsse, uriWsse);
        SOAPElement userElem = factory.createElement(PARAM_NAME_USERNAME, prefixWsse, uriWsse);
        userElem.addTextNode(username);
        SOAPElement pwdElem = factory.createElement(PARAM_NAME_PASSWORD, prefixWsse, uriWsse);
        pwdElem.addTextNode(password.decrypt());
        pwdElem.addAttribute(QName.valueOf(PARAM_NAME_TYPE), URI_TYPE);
        tokenElem.addChildElement(userElem);
        tokenElem.addChildElement(pwdElem);
        securityElem.addChildElement(tokenElem);
        return securityElem;
    }

    @Override
    public Set<QName> getHeaders() {
        return null;
    }

    @Override
    public boolean handleFault(SOAPMessageContext context) {
        // SOAPFault Exception wird nach oben durchgegeben und muss hier nicht behandelt werden.
        return false;
    }

    @Override
    public void close(MessageContext context) {

    }
}
