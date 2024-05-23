/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsModuleCematId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.einpas.EinPasId;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten aus DA_MODULE_CEMAT.
 * EinPAS-Knoten aus CEMaT
 */
public class iPartsDataModuleCemat extends EtkDataObject implements iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DMC_MODULE_NO, FIELD_DMC_LFDNR, FIELD_DMC_EINPAS_HG, FIELD_DMC_EINPAS_G, FIELD_DMC_EINPAS_TU };

    public iPartsDataModuleCemat(EtkProject project, iPartsModuleCematId id) {
        super(KEYS);
        tableName = TABLE_DA_MODULE_CEMAT;

        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsModuleCematId createId(String... idValues) {
        return new iPartsModuleCematId(idValues[0], idValues[1], idValues[2], idValues[3], idValues[4]);
    }

    @Override
    public iPartsModuleCematId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }
        return (iPartsModuleCematId)id;
    }

    public EinPasId getEinPasId() {
        return getAsId().getEinPasId();
    }

    public AssemblyId getAssemblyId() {
        return getPartListEntryId().getOwnerAssemblyId();
    }

    public PartListEntryId getPartListEntryId() {
        return getAsId().getPartListEntryId();
    }

    public String getPartNo() {
        return getFieldValue(FIELD_DMC_PARTNO);
    }

}
