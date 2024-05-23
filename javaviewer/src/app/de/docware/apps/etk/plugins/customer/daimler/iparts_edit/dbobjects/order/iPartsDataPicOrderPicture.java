/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.order;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsColorTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.response.iPartsXMLMediaVariant;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.util.StrUtils;

import java.util.Calendar;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für Tabelle DA_PICORDER_PICTURES.
 */
public class iPartsDataPicOrderPicture extends EtkDataObject implements EtkDbConst, iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DA_POP_ORDER_GUID, FIELD_DA_POP_PIC_ITEMID, FIELD_DA_POP_PIC_ITEMREVID };

    public iPartsDataPicOrderPicture(EtkProject project, iPartsPicOrderPictureId id) {
        super(KEYS);
        tableName = TABLE_DA_PICORDER_PICTURES;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsPicOrderPictureId createId(String... idValues) {
        return new iPartsPicOrderPictureId(idValues[0], idValues[1], idValues[2]);
    }

    @Override
    public iPartsPicOrderPictureId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }

        return (iPartsPicOrderPictureId)id;
    }

    public Calendar getLastModifiedDate() {
        return getFieldValueAsDate(FIELD_DA_POP_LAST_MODIFIED);
    }

    public iPartsColorTypes getVariantType() {
        return iPartsColorTypes.getFromDBValue(getFieldValue(FIELD_DA_POP_VAR_TYPE));
    }

    @Override
    public iPartsDataPicOrderPicture cloneMe(EtkProject project) {
        iPartsDataPicOrderPicture clone = new iPartsDataPicOrderPicture(project, getAsId());
        clone.assignRecursively(project, this, DBActionOrigin.FROM_DB);
        return clone;
    }

    public void setIsUsed(boolean isUsed) {
        setFieldValueAsBoolean(FIELD_DA_POP_USED, isUsed, DBActionOrigin.FROM_EDIT);
    }

    /**
     * Gibt an, ob das Bild für die Korrektur oder einen Änderungsauftrag ausgewählt wurde
     *
     * @return
     */
    public boolean isUsed() {
        return getFieldValueAsBoolean(FIELD_DA_POP_USED);
    }

    /**
     * Erstellt aus dem aktuellen {@link iPartsDataPicOrderPicture} DataObject eine MQ XML Mediavariante ({@link iPartsXMLMediaVariant})
     *
     * @param language
     * @return
     */
    public iPartsXMLMediaVariant getAsMediaVariant(String language) {
        iPartsXMLMediaVariant variant = new iPartsXMLMediaVariant();
        variant.setItemId(getAsId().getPicItemId());
        variant.setItemRevId(getAsId().getPicItemRevId());
        Calendar lastModified = getLastModifiedDate();
        if (lastModified != null) {
            variant.setLastModified(lastModified.getTime());
        }
        variant.setPictureLanguage(language);
        variant.setColorType(getVariantType());
        return variant;
    }

    public boolean isASPLMPicture() {
        return StrUtils.isValid(getAsId().getPicItemId(), getAsId().getPicItemRevId())
               && (StrUtils.countString(getAsId().getPicItemId(), ".", true) == 3)
               && (getLastModifiedDate() != null);
    }
}
