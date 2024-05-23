/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.helper.cemat;

import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

/**
 * Repräsentiert die Einzelobjekte für die Liste der "mappings" im JSON für die EinPAS-Knoten aus CEMaT für den Importer
 * <p>
 * <p>    "CEMaT-to-iParts" : [
 * <p>            {
 * <p>                    "iPartsTuId" : "56Z_58_030_00001",
 * <p>                    "iPartsSerialNbr" : "00014",
 * <p>                    "partNumber" : "A0005810117",
 * <p>                    "mappings" : [
 * <p>                            {                            <<===== diese Ebene!
 * <p>                                   "nodeId" : 1103,
 * <p>                                   "node" : "001.35.85.40"
 * <p>                            },
 * <p>                            ...
 * <p>                    ]
 * <p>            },
 * <p>            ...
 */

public class CEMaTTuMapping implements RESTfulTransferObjectInterface {

    private String nodeId;
    private String node;


    public CEMaTTuMapping() {
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
    }

}
