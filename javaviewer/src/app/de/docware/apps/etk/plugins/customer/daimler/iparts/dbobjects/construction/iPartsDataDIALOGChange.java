/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.mechanic.ids.PartId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.replace.iPartsReplacePartId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogId;
import de.docware.framework.modules.db.DBActionOrigin;

import java.util.EnumSet;

/**
 * Repräsentiert eine DIALOG Datenänderung in Tabelle DA_DIALOG_CHANGES
 * Die Tabelle enthält eine 1:N Relation von Änderung zu Orten, wo sich die Änderung auswirkt. Orte sind entweder der
 * BCTE-Schlüssel oder ein Material (bezogen auf eine Baureihe).
 * Beispiele:
 * - Werkseinsatzdaten:           1:1 Zuordnung zu BCTE-Schlüssel
 * - Werkseinsatzdaten zu Farben: 1:1 Zuordnung zu Material mit Baureihe
 * - Rückmeldedaten:              1:N Zuordnung zu BCTE-Schlüssel
 * Da ein Primary Key über alle Fremdschlüsselfelder zu lang werden würde, bilden wir über die Ortsfelder (Baureihe, BCTE, Material)
 * einen Hash und nehmen diesen in den PK auf. Der Hash hat gegenüber einer laufenden Nummer auch den Vorteil dass keine
 * doppelten Datensätze entstehen können. Die Beschaffung einer laufenden Nummer wäre wegen "BufferedSave" auch nicht trivial.
 */
public class iPartsDataDIALOGChange extends EtkDataObject implements iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DDC_DO_TYPE, FIELD_DDC_DO_ID, FIELD_DDC_HASH };

    public enum ChangeType {
        FACTORY_DATA(iPartsFactoryDataId.TYPE, iPartsFactoryDataId.DESCRIPTION),
        COLORTABLE_FACTORY_DATA(iPartsColorTableFactoryId.TYPE, iPartsColorTableFactoryId.DESCRIPTION),
        RESPONSE_DATA(iPartsResponseDataId.TYPE, iPartsResponseDataId.DESCRIPTION),
        REPLACEMENT_AS(iPartsReplacePartId.TYPE, iPartsReplacePartId.DESCRIPTION),
        PARTLISTENTRY_ETKZ(iPartsDialogId.TYPE, "!!ETK-Änderung der Teileposition"),  // Änderung am ET-KZ Feld der Teileposition
        MAT_ETKZ(PartId.TYPE, "!!ET-KZ-Änderung am Material"),  // Änderung am ET-KZ Feld des Materialstamms
        UNKNOWN("", "!!Unbekannt");
        // weitere Typen werden folgen

        public static EnumSet<ChangeType> UNHANDLED_CHANGE_TYPES = EnumSet.of(RESPONSE_DATA);

        private String dbKey;
        private String displayKey;

        ChangeType(String dbKey, String displayKey) {
            this.dbKey = dbKey;
            this.displayKey = displayKey;
        }

        public static ChangeType getChangeType(String dbKey) {
            for (ChangeType changeType : ChangeType.values()) {
                if (changeType.dbKey.equals(dbKey)) {
                    return changeType;
                }
            }
            return UNKNOWN;
        }

        public static boolean isValid(ChangeType changeType) {
            if ((changeType != null) && (changeType != ChangeType.UNKNOWN)) {
                return true;
            }
            return false;
        }

        public static boolean isHandledForDelete(ChangeType changeType) {
            if (isValid(changeType)) {
                return !UNHANDLED_CHANGE_TYPES.contains(changeType);
            }
            return false;
        }

        public String getDbKey() {
            return dbKey;
        }

        public String getDisplayKey() {
            return displayKey;
        }
    }

    private iPartsDataDIALOGChange(EtkProject project) {
        super(KEYS);
        tableName = TABLE_DA_DIALOG_CHANGES;
        if (project != null) {
            init(project);
        }
    }

    public iPartsDataDIALOGChange(EtkProject project, iPartsDialogChangesId id) {
        this(project);
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsDialogChangesId createId(String... idValues) {
        return new iPartsDialogChangesId(idValues[0], idValues[1], idValues[2]);
    }

    @Override
    public iPartsDialogChangesId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }

        return (iPartsDialogChangesId)id;
    }
}
