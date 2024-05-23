/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.mechanic.EtkDataPart;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataES1;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsNumberHelper;
import de.docware.framework.modules.db.DBDataObjectList;
import de.docware.util.StrUtils;

/**
 * AlternativePart Data Transfer Object für die iParts Webservices
 * Wird von {@link iPartsWSPartInfo} verwendet um Alternativen (z.B. Reman Teile) zu kennzeichnen
 */
public class iPartsWSAlternativePart extends iPartsWSPartBase {

    private String type;

    /**
     * Leerer Konstruktor (notwendig für die die automatische Erzeugung aus einem JSON-String)
     */
    public iPartsWSAlternativePart() {
    }

    public iPartsWSAlternativePart(EtkProject project, EtkDataPart alternativePart, boolean withExtendedDescriptions) {
        super();
        // als partNo und partNoFormatted soll nur die reine Grundsachnummer ausgegeben werden
        String matNr = alternativePart.getFieldValue(iPartsConst.FIELD_M_BASE_MATNR);

        // Benennung vom Alternativteil ermitteln
        String name = alternativePart.getFieldValue(EtkDbConst.FIELD_M_TEXTNR, project.getDBLanguage(), true);
        setPartBaseValues(matNr, name, alternativePart.getFieldValueAsBoolean(iPartsConst.FIELD_M_SECURITYSIGN_REPAIR));

        if (iPartsNumberHelper.isPseudoPart(matNr)) {
            this.setPartNoFormatted("");
        } else {
            this.setPartNoFormatted(iPartsNumberHelper.formatPartNo(project, matNr, project.getDBLanguage()));
        }

        this.setNameRef(getNameRefFromPart(alternativePart, withExtendedDescriptions));

        // PartContext gibts hier nicht
        this.setPartContext(null);

        // Alternativteile können ab DAIMLER-9557 auch gefüllte ES2 haben
        String es2Key = alternativePart.getFieldValue(iPartsConst.FIELD_M_AS_ES_2);
        if (StrUtils.isValid(es2Key)) {
            this.setEs2Key(es2Key);
        }

        // ES 1 aus eigenem Objekt übernehmen
        DBDataObjectList dataObjectList = alternativePart.getChildren(iPartsConst.TABLE_DA_ES1);
        // Es kann immer nur ein iPartsDataES1 Kind geben
        if ((dataObjectList != null) && !dataObjectList.isEmpty()) {
            iPartsDataES1 dataES1 = (iPartsDataES1)dataObjectList.get(0);
            this.setEs1Key(dataES1.getAsId().getES1Code());

            // Type als ENUM und nicht als Text übernehmen
            this.type = dataES1.getES1Type();
        }
    }

    // Getter and Setter
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
