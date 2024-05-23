/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.endpoints.supply_to_bst_simulation;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.endpoints.iPartsWSAbstractEndpointBST;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.iPartsEditPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.transferobjects.SupplyAuthorOrderToBST;
import de.docware.framework.modules.gui.misc.http.server.HttpServerRequest;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

/**
 * Endpoint für die Simulation vom BST-Webservice für das Versorgen von Autoren-Aufträgen
 */
public class iPartsWSSupplyToBSTSimulationEndpoint extends iPartsWSAbstractEndpointBST<SupplyAuthorOrderToBST> implements iPartsConst {

    public iPartsWSSupplyToBSTSimulationEndpoint(String endpointUri) {
        super(endpointUri);
        setResponseCacheSize(0); // Die Simulation hat keinen JSON-Response-Cache
    }

    @Override
    protected SecureResult isValidRequestSignature(HttpServerRequest request) {
        String headerName = iPartsEditPlugin.getPluginConfig().getConfigValueAsString(iPartsEditPlugin.CONFIG_SUPPLY_AUTHOR_ORDERS_TO_BST_TOKEN_NAME);
        if (!headerName.isEmpty()) {
            Logger.log(iPartsEditPlugin.LOG_CHANNEL_SUPPLY_TO_BST, LogType.DEBUG, "Simulated BST webservice got token: "
                                                                                  + request.getHeader(headerName));
        }

        return new SecureResult(SecureReturnCode.SUCCESS); // Keine Validierung notwendig
    }

    @Override
    protected RESTfulTransferObjectInterface executeWebservice(EtkProject project, final SupplyAuthorOrderToBST requestObject) {
        if (requestObject.getAuthorOrder().getOrderTitle().toLowerCase().contains("error")) {
            throw new RuntimeException("Houston, we have a problem...");
        }

        String jsonString = getGenson().serialize(requestObject);
        Logger.log(iPartsEditPlugin.LOG_CHANNEL_SUPPLY_TO_BST, LogType.DEBUG, "Simulated BST webservice response for author order "
                                                                              + requestObject.getAuthorOrder().getOrderId()
                                                                              + " with request content:\n" + jsonString);

        return null;
    }
}
