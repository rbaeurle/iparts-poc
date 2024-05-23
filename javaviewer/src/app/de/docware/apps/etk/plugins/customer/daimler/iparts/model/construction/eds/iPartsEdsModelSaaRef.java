/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.eds;

import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.helper.EdsSaaId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.modelelementusage.ModelElementUsageId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.ops.OpsId;

/**
 * In einem EDS-Baumuster hängen bestimmte SAAs an bestimmten Stellen der OPS/ModulElementUsage. Diese Klasse ist zur Beschreibung
 * einer Verknüpfung.
 */
public class iPartsEdsModelSaaRef {

    private String upperStructureValue;
    private String lowerStructureValue;
    private OpsId opsId;
    private ModelElementUsageId modelElementUsageId;
    private EdsSaaId edsSaaId;

    public iPartsEdsModelSaaRef(String topValue, String bottomValue, EdsSaaId edsSaaId) {
        this.upperStructureValue = topValue;
        this.lowerStructureValue = bottomValue;
        this.edsSaaId = edsSaaId;
    }

    public OpsId getOpsId() {
        if (opsId == null) {
            opsId = new OpsId(upperStructureValue, lowerStructureValue);
        }
        return opsId;
    }

    public EdsSaaId getEdsSaaId() {
        return edsSaaId;
    }

    public ModelElementUsageId getModelElementUsageId() {
        if (modelElementUsageId == null) {
            modelElementUsageId = new ModelElementUsageId(upperStructureValue, lowerStructureValue);
        }
        return modelElementUsageId;
    }
}
