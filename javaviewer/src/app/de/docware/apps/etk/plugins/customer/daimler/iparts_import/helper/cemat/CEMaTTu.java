/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.helper.cemat;

import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

import java.util.List;

/**
 * Repräsentiert die Klasse für ein TU-Objekt im JSON für die EinPAS-Knoten aus CEMaT für den Importer
 * <p>
 * <p>    "CEMaT-to-iParts" : [
 * <p>            {                                            <<===== diese Ebene
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

public class CEMaTTu implements RESTfulTransferObjectInterface {

    private String iPartsTuId;
    private String iPartsSerialNbr;
    private String partNumber;
    private List<CEMaTTuMapping> mappings;


    public CEMaTTu() {
    }

    public String getiPartsTuId() {
        return iPartsTuId;
    }

    public void setiPartsTuId(String iPartsTuId) {
        this.iPartsTuId = iPartsTuId;
    }

    public String getiPartsSerialNbr() {
        return iPartsSerialNbr;
    }

    public void setiPartsSerialNbr(String iPartsSerialNbr) {
        this.iPartsSerialNbr = iPartsSerialNbr;
    }

    public String getPartNumber() {
        return partNumber;
    }

    public void setPartNumber(String partNumber) {
        this.partNumber = partNumber;
    }

    public List<CEMaTTuMapping> getMappings() {
        return mappings;
    }

    public void setMappings(List<CEMaTTuMapping> mappings) {
        this.mappings = mappings;
    }

}
