/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.endpoints;

import de.docware.apps.etk.base.webservice.endpoints.WSAbstractEndpoint;
import de.docware.apps.etk.base.webservice.transferobjects.WSRequestTransferObjectInterface;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.iPartsEditPlugin;
import de.docware.framework.modules.config.defaultconfig.webservice.WebserviceSettings;
import de.docware.framework.modules.gui.misc.http.server.HttpServerRequest;
import de.docware.util.security.PasswordString;

/**
 * Abstrakte Klasse für iParts BST-Endpoints.
 * <br/>Der Generic {@code REQUEST_CLASS} gibt die Klasse an, die für das Request-Objekt von diesem Endpoint erwartet und automatisch
 * aus einem JSON-String erzeugt wird. In anderen JAVA-Klassen heißen Generics meistens {@code <T>}.
 * Diese Klasse macht also die Annahme, dass der Request-Body genau ein JSON-Objekt enthält, welches zur Klasse REQUEST_CLASS
 * konvertiert werden kann.
 */
public abstract class iPartsWSAbstractEndpointBST<REQUEST_CLASS extends WSRequestTransferObjectInterface> extends WSAbstractEndpoint<REQUEST_CLASS> {

    public iPartsWSAbstractEndpointBST(String endpointUri) {
        super(endpointUri, iPartsEditPlugin.LOG_CHANNEL_WS_DEBUG, iPartsEditPlugin.LOG_CHANNEL_WS_PERFORMANCE);
        logChannelSecure = iPartsEditPlugin.LOG_CHANNEL_WS_TOKEN;
    }

    @Override
    protected WebserviceSettings setConfiguredCacheSettings() {
        // die JavaViewer-WebserviceSettings werden bei iParts nicht ausgelesen. Stattdessen werden sie in in den
        // entsprechenden Plug-Ins definiert und hier ausgelesen. Bei BST wird aber überhaupt nicht gecached.
        return null;
    }

    @Override
    protected SecureResult isValidRequestSignature(HttpServerRequest request) {
        final String headerName = iPartsEditPlugin.getPluginConfig().getConfigValueAsString(iPartsEditPlugin.CONFIG_BST_HEADER_TOKEN_NAME);
        final String authorizationType = iPartsEditPlugin.getPluginConfig().getConfigValueAsString(iPartsEditPlugin.CONFIG_BST_HEADER_TOKEN_TYPE);

        // Passwort für HS256 Verfahren; wenn leer ist das Verfahren nicht zugelassen
        final PasswordString secret = iPartsEditPlugin.getPluginConfig().getConfigValueAsPassword(iPartsEditPlugin.CONFIG_BST_HEADER_TOKEN_PASSWORD);

        // Token validieren
        final int expiryDelay = iPartsEditPlugin.getPluginConfig().getConfigValueAsInteger(iPartsEditPlugin.CONFIG_BST_HEADER_TOKEN_EXPIRES);
        long currentTime = System.currentTimeMillis() / 1000;
        return isValidJWT(request, headerName, authorizationType, secret, currentTime, expiryDelay, iPartsEditPlugin.getKeystoreManagerBST().getKeystores());
    }
}