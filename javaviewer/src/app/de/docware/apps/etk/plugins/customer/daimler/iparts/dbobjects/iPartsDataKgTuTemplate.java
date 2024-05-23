package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.util.misc.id.IdWithType;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für DA_KGTU_TEMPLATE
 *
 * Für verschiedene Kombinationen von Baumusterart und AS Produktklasse sollen verschiedene KG/TU Templates gespeichert werden können.
 *
 * Die Tabelle ist nicht normalisiert. Die Felder KGTU-Benennung und -Bild werden redundant gespeichert.
 * Die normalisierte Form würde zwei Tabellen wie folgt benötigen:
 *
 * 1. KGTU-Stammdaten
 * - KG (PK)
 * - TU (TU)
 * - Benennung
 * - Bild
 *
 * 2. KGTU-Templates
 * - Bamusterart (PK)
 * - AS Produktklasse (PK)
 * - KG (PK)
 * - TU (PK)
 */
public class iPartsDataKgTuTemplate extends EtkDataObject implements iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DA_DKT_AGGREGATE_TYPE, FIELD_DA_DKT_AS_PRODUCT_CLASS, FIELD_DA_DKT_KG, FIELD_DA_DKT_TU };

    public iPartsDataKgTuTemplate(EtkProject project, iPartsKgTuTemplateId id) {
        super(KEYS);
        tableName = TABLE_DA_KGTU_TEMPLATE;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public IdWithType createId(String... idValues) {
        return new iPartsKgTuTemplateId(idValues[0], idValues[1], idValues[2], idValues[3]);
    }

    @Override
    public iPartsKgTuTemplateId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }
        return (iPartsKgTuTemplateId)id;
    }
}
