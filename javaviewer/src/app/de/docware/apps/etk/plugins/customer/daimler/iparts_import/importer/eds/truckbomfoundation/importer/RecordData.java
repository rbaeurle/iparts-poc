/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.truckbomfoundation.importer;

import de.docware.framework.utils.EtkMultiSprache;

import java.util.Map;
import java.util.TreeMap;

/**
 * Hilfsklasse für einen erzeugten ImportRecord mit zusätzlichen sprachspezifischen Texten
 */
class RecordData {

    private final Map<String, String> record;
    private EtkMultiSprache description;
    private EtkMultiSprache remark;

    public RecordData() {
        this.record = new TreeMap<>();
    }

    public Map<String, String> getRecord() {
        return record;
    }

    public void put(String xmlElement, String value) {
        record.put(xmlElement, value);
    }

    public EtkMultiSprache getDescription() {
        return description;
    }

    public EtkMultiSprache getRemark() {
        return remark;
    }

    public void setDescription(EtkMultiSprache description) {
        if ((description != null) && !description.isEmpty()) {
            this.description = description;
        }
    }

    public void setRemark(EtkMultiSprache remark) {
        if ((remark != null) && !remark.isEmpty()) {
            this.remark = remark;
        }
    }
}
