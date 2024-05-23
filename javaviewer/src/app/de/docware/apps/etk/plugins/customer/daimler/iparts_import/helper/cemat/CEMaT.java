/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.helper.cemat;

import com.owlike.genson.annotation.JsonProperty;
import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

import java.util.List;

/**
 * Repräsentiert die äußere Klammer, also den Hauptknoten "CEMaT-to-iParts" im JSON für die EinPAS-Knoten aus CEMaT für den Importer
 * <p>
 * <p>    "CEMaT-to-iParts" : [                                <<===== das hier!
 * <p>            {
 * <p>                    "iPartsTuId" : "56Z_58_030_00001",
 * <p>                    "iPartsSerialNbr" : "00014",
 * <p>                    "partNumber" : "A0005810117",
 * <p>                    "mappings" : [
 * <p>                            {
 * <p>                                   "nodeId" : 1103,
 * <p>                                   "node" : "001.35.85.40"
 * <p>                            },
 * <p>                            ...
 * <p>                    ]
 * <p>            },
 * <p>            ...
 */
public class CEMaT implements RESTfulTransferObjectInterface {

    // "CEMaT-to-iParts" ist kein Name, den als Variable durchginge, er muss über die Property gemappt werden.
    @JsonProperty("CEMaT-to-iParts")
    private List<CEMaTTu> tuList;


    public CEMaT() {
    }

    public List<CEMaTTu> getTuList() {
        return tuList;
    }

    public void setTuList(List<CEMaTTu> tuList) {
        this.tuList = tuList;
    }

}
