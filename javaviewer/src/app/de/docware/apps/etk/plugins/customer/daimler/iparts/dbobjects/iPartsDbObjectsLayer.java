/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.db.EtkDbs;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.base.EtkDbObjectsLayer;
import de.docware.apps.etk.base.project.mechanic.EtkDataPart;
import de.docware.apps.etk.base.project.mechanic.ids.PartId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsPartId;
import de.docware.apps.etk.viewer.JavaViewerApplication;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.util.StrUtils;

/**
 * Erweiterung von {@link de.docware.apps.etk.base.project.base.EtkDbObjectsLayer} um iParts-spezifische Methoden und DB-Zugriffe.
 */
public class iPartsDbObjectsLayer extends EtkDbObjectsLayer {

    public static boolean TEXTS_WITH_ID_CREATABLE_IN_CHANGE_SETS = false; // Können Texte mit Text-ID in ChangeSets neu erzeugt werden?

    /**
     * Überprüft, ob der mehrsprachige Text für das übergebene {@link DBDataObjectAttribute} in der Datenbank existiert.
     * Bei iParts landen neue Texte mit Text-ID IMMER sofort im Lexikon und damit in der DB (außer das Flag {@link #TEXTS_WITH_ID_CREATABLE_IN_CHANGE_SETS}
     * steht explizit auf {@code true} z.B. für Unittests.
     *
     * @param attribute
     * @param etkDbs
     * @return
     */
    public static boolean isMultiLanguageAttributeExistsInDB(DBDataObjectAttribute attribute, EtkDbs etkDbs) {
        if (TEXTS_WITH_ID_CREATABLE_IN_CHANGE_SETS) {
            return EtkDbObjectsLayer.isMultiLanguageAttributeExistsInDB(attribute, etkDbs);
        } else {
            return StrUtils.isValid(attribute.getMultiLanguageTextId());
        }
    }

    public iPartsDbObjectsLayer(EtkDbs etkDbs) {
        super(etkDbs);
    }

    /**
     * Eigentlich deprecated, sollte über EtkDataPart.getValue() zukünftig gemacht werden
     *
     * @param language   Sprachkürzel ("DE", "EN", ...)
     * @param materialId {@link PartId} vom Material
     * @return
     */
    @Override
    public String getLanguageTextByMatNr(String language, PartId materialId) {
        if (materialId instanceof iPartsPartId) {
            iPartsPartId virtualPartId = (iPartsPartId)materialId;
            if (virtualPartId.isVirtual()) { // Bezeichnung vom virtuellen Material bestimmen
                EtkDataPart part = EtkDataObjectFactory.createDataPart(JavaViewerApplication.getInstance().getProject(),
                                                                       virtualPartId.getMatNr(), virtualPartId.getMVer());
                return part.getFieldValue(FIELD_M_TEXTNR, language, true);
            }
        }

        return super.getLanguageTextByMatNr(language, materialId);
    }

    @Override
    public boolean isMultiLanguageExistsInDB(DBDataObjectAttribute attribute) {
        return isMultiLanguageAttributeExistsInDB(attribute, etkDbs);
    }
}
