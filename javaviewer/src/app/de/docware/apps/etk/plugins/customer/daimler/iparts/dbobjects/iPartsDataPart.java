/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.mechanic.EtkDataPart;
import de.docware.apps.etk.base.project.mechanic.ids.PartId;
import de.docware.apps.etk.base.project.mechanic.usage.MechanicUsagePosition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualassembly.iPartsVirtualAssemblyCTT;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualassembly.iPartsVirtualAssemblyDialogBase;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualassembly.iPartsVirtualAssemblyEdsBase;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualassembly.iPartsVirtualAssemblyMBS;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsPartId;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.CanceledException;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.misc.id.IdWithType;

import java.util.List;

/**
 * Erweiterung von {@link EtkDataPart} um iParts-spezifische Methoden und Daten.
 */
public class iPartsDataPart extends EtkDataPart implements EtkDbConst {

    public iPartsDataPart() {
        super();
    }

    public iPartsDataPart(EtkProject project, PartId partId) {
        super(project, partId);
    }

    @Override
    public void setId(IdWithType id, DBActionOrigin origin) {
        if ((id == null) || (id instanceof iPartsPartId)) {
            super.setId(id, origin);
        } else {
            super.setId(new iPartsPartId(id), origin);
        }
    }

    @Override
    public iPartsPartId createId(String... idValues) {
        return new iPartsPartId(idValues[0], idValues[1]);
    }

    @Override
    public iPartsPartId getAsId() {
        return (iPartsPartId)super.getAsId();
    }

    @Override
    public void initAttributesWithEmptyValues(DBActionOrigin origin) {
        super.initAttributesWithEmptyValues(origin);
        attributes.addField(iPartsDataVirtualFieldsDefinition.DA_MAPPED_MATNR, getAsId().getMatNr(), true, DBActionOrigin.FROM_DB);
    }

    @Override
    protected DBDataObjectAttributes internalLoad(IdWithType id, String[] resultFields) {
        if (!(id instanceof iPartsPartId)) {
            throw new RuntimeException("iPartsDataPart.internalLoad(): ID must be an instance of iPartsPartId");
        }

        iPartsPartId matId = (iPartsPartId)id;
        if (!matId.isVirtual()) {
            DBDataObjectAttributes loadedAttributes = super.internalLoad(id, resultFields);
            if (loadedAttributes != null) {
                loadedAttributes.addField(iPartsDataVirtualFieldsDefinition.DA_MAPPED_MATNR, getAsId().getMatNr(), true, DBActionOrigin.FROM_DB);
            }
            return loadedAttributes;
        } else {
            DBDataObjectAttributes result = new DBDataObjectAttributes();

            // Aktuell sind bei der virtuellen Navigation nur diese Felder im Kopfdatensatz gesetzt
            result.addField(FIELD_M_MATNR, matId.getMatNr(), DBActionOrigin.FROM_DB);
            result.addField(FIELD_M_VER, matId.getMVer(), DBActionOrigin.FROM_DB);
            result.addField(FIELD_M_BESTNR, iPartsDataVirtualFieldsHelper.getOrderNumberFromVirtualKey(matId.getMatNr(),
                                                                                                       getEtkProject()),
                            DBActionOrigin.FROM_DB);

            // Den Rest einfach Leer
            for (String fieldName : resultFields) {
                if (!result.fieldExists(fieldName)) {
                    result.addField(fieldName, "", DBActionOrigin.FROM_DB);
                }
            }

            // Texte des Materials
            EtkMultiSprache texts = iPartsDataVirtualFieldsHelper.getTextsFromVirtualKey(matId.getMatNr(), getEtkProject());
            if (texts != null) {
                DBDataObjectAttribute textAttribute = result.getField(FIELD_M_TEXTNR);
                if (textAttribute != null) {
                    textAttribute.setValueAsMultiLanguage(texts, DBActionOrigin.FROM_DB);
                }
            }

            return result;
        }
    }

    @Override
    public DBDataObjectAttribute getAttribute(String attributeName, boolean exceptionIfNotExists) {
        DBDataObjectAttribute attribute = super.getAttribute(attributeName, exceptionIfNotExists);
        if ((attribute == null) && (getAttributes() == null) && attributeName.equals(iPartsDataVirtualFieldsDefinition.DA_MAPPED_MATNR)) {
            // Falls kein Materialstamm existiert, muss die gemappte Teilenummer trotzdem die Original-Teilenummer zurückgeben
            // Die Variable attributes muss aber null bleiben, um weiterhin erkennen zu können, dass der Datensatz nicht existiert
            attribute = new DBDataObjectAttribute(iPartsDataVirtualFieldsDefinition.DA_MAPPED_MATNR, -1, true);
            attribute.setValueAsString(getAsId().getMatNr(), DBActionOrigin.FROM_DB);
        }
        return attribute;
    }

    @Override
    public List<MechanicUsagePosition> getMechanicUsage(boolean withFilter, boolean cancelable) throws CanceledException {
        // Bei leerer Teilenummer keine Verwendungen suchen. Texte haben z.B. leere Teilenummern
        if (getAsId().isEmpty()) {
            return new DwList<>();
        }

        List<MechanicUsagePosition> result = super.getMechanicUsage(withFilter, cancelable, false);

        // virtuelles Material
        iPartsVirtualAssemblyDialogBase.getMechanicUsageForDialogPartLists(getAsId(), withFilter, getEtkProject(), result);
        iPartsVirtualAssemblyEdsBase.getMechanicUsageForEdsMaterial(getAsId(), withFilter, getEtkProject(), result);
        iPartsVirtualAssemblyMBS.getMechanicUsageForMBSMaterial(getAsId(), withFilter, getEtkProject(), result);
        iPartsVirtualAssemblyCTT.getMechanicUsageForCTTMaterial(getAsId(), withFilter, getEtkProject(), result);
        return result;
    }

    @Override
    public void setFieldValue(String attributeName, String value, DBActionOrigin origin) {
        if (attributeName.equals(FIELD_M_BESTNR)) {
            // die beiden EqualPart Felder nur Setzen, wenn es sich um ein reales Part, das neu angelegt wurde, handelt.
            if (!getAsId().isVirtual() && isNew() && attributeExists(iPartsConst.FIELD_M_MATNR_MBAG)) {
                // zur Sicherheit die Equal Part Felder noch auf leer überprüfen
                if (StrUtils.isEmpty(getFieldValue(iPartsConst.FIELD_M_MATNR_MBAG))) {
                    super.setFieldValue(iPartsConst.FIELD_M_MATNR_MBAG, value, origin);
                }
                if (StrUtils.isEmpty(getFieldValue(iPartsConst.FIELD_M_MATNR_DTAG))) {
                    super.setFieldValue(iPartsConst.FIELD_M_MATNR_DTAG, value, origin);
                }
            }
        }
        super.setFieldValue(attributeName, value, origin);
    }

    @Override
    public boolean loadVirtualField(String attributeName) {
        if (attributeName.equals(iPartsDataVirtualFieldsDefinition.DA_MAPPED_MATNR)) {
            attributes.addField(attributeName, getAsId().getMatNr(), true, DBActionOrigin.FROM_DB);
            return false;
        } else if (attributeName.equals(iPartsDataVirtualFieldsDefinition.DA_HAS_MAPPED_MATNR)) {
            calculateAndSetEqualPartType();
            return false;
        }
        return false;
    }

    public void setMappedMatNr(String mappedMatNr) {
        if (!existsInDB()) {
            initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
        }
        attributes.addField(iPartsDataVirtualFieldsDefinition.DA_MAPPED_MATNR, mappedMatNr, true, DBActionOrigin.FROM_DB);
    }

    public void calculateAndSetEqualPartType() {
        if (!existsInDB()) {
            initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
        }
        attributes.addField(iPartsDataVirtualFieldsDefinition.DA_HAS_MAPPED_MATNR, getEqualPartType().getDbValue(), true,
                            DBActionOrigin.FROM_DB);
    }

    public boolean isNormalPart() {
        return getAsId().getMatNr().equals(getFieldValue(iPartsDataVirtualFieldsDefinition.DA_MAPPED_MATNR));
    }

    public boolean isMBAGpart() {
        if (!isNormalPart()) {
            return getFieldValue(iPartsDataVirtualFieldsDefinition.DA_MAPPED_MATNR).equals(getFieldValue(iPartsConst.FIELD_M_MATNR_MBAG));
        }
        return false;
    }

    public boolean isDTAGpart() {
        if (!isNormalPart()) {
            return getFieldValue(iPartsDataVirtualFieldsDefinition.DA_MAPPED_MATNR).equals(getFieldValue(iPartsConst.FIELD_M_MATNR_DTAG));
        }
        return false;
    }

    public iPartsEqualPartType getEqualPartType() {
        String matNo = getAsId().getMatNr();
        if (StrUtils.isValid(matNo)) {
            String mbagPart = getFieldValue(iPartsConst.FIELD_M_MATNR_MBAG);
            String dtagPart = getFieldValue(iPartsConst.FIELD_M_MATNR_DTAG);
            if (!mbagPart.equals(matNo) && !mbagPart.isEmpty()) {
                if (!dtagPart.equals(matNo)) {
                    return iPartsEqualPartType.BOTH;
                } else {
                    return iPartsEqualPartType.MB;
                }
            } else {
                if (!dtagPart.equals(matNo) && !dtagPart.isEmpty()) {
                    return iPartsEqualPartType.DT;
                }
            }
        }
        return iPartsEqualPartType.NONE;
    }
}
