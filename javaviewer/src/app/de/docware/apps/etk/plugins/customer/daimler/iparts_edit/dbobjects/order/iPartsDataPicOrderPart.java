/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.order;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.base.GenericEtkDataObjectList;
import de.docware.apps.etk.base.project.mechanic.EtkDataPart;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.ids.PartId;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsCombTextId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataCombText;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataCombTextList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.db.DBExtendedDataTypeProvider;
import de.docware.framework.modules.db.etkrecord.EtkRecord;
import de.docware.framework.modules.db.serialization.SerializedDBDataObject;
import de.docware.framework.modules.db.serialization.SerializedDBDataObjectList;
import de.docware.framework.modules.db.serialization.SerializedDbDataObjectAsJSON;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;
import de.docware.util.misc.id.IdWithType;

import java.util.*;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für Tabelle DA_PICORDER_PARTS.
 */
public class iPartsDataPicOrderPart extends EtkDataObject implements EtkDbConst, iPartsConst {

    private static final String[] KEYS = new String[]{ FIELD_DA_PPA_ORDER_GUID, FIELD_DA_PPA_VARI, FIELD_DA_PPA_VER, FIELD_DA_PPA_LFDNR, FIELD_DA_PPA_POS, FIELD_DA_PPA_SACH };
    private static final String PIC_POS_MARKER_DEFAULT_VALUE = "Y";
    private static final String EXTERNAL_ID_DELIMITER = "|";
    public static final String PIC_POS_MARKER_DELETED_VALUE = "D";

    // Kinder für die Serialisierung
    private static final String CHILD_NAME_PART = "child.part";
    private static final String CHILD_NAME_SUPPLEMENTARY_TEXTS = "child.supplementaryTexts";

    private EtkDataPartListEntry relatedPartListEntry; // Die verknüpfte Stücklistenposition im DB-Feld "PPA_PARTLIST_ENTRY_DATA"
    private boolean relatedEntryLoaded; // Info, ob die verknüpfte Stücklistenposition schon geladen wurde
    private DBDataObjectAttributes serializedDBAttributes;
    private Map<String, iPartsDataCombText> combTextMap; // Eingefrorene kombinierte Texte zu Zeitpunkt des Versendens

    public iPartsDataPicOrderPart(EtkProject project, iPartsPicOrderPartId id) {
        super(KEYS);
        tableName = TABLE_DA_PICORDER_PARTS;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsPicOrderPartId createId(String... idValues) {
        return new iPartsPicOrderPartId(idValues[0], idValues[1], idValues[2], idValues[3], idValues[4], idValues[5]);
    }

    @Override
    public iPartsPicOrderPartId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }

        return (iPartsPicOrderPartId)id;
    }

    // Convenience Method:
    // Versuche den Wert erst aus der verknüpften Stücklistenposition zu holen. Falls noch keine in der DB existiert,
    // dann nimm als Fallback die separat gespeicherten Werte
    public String getZGS() {
        if (getStoredRelatedPartListEntry() == null) {
            return getFieldValue(FIELD_DA_PPA_ZGS);
        }
        return getStoredRelatedPartListEntry().getPart().getFieldValue(FIELD_M_IMAGESTATE);
    }

    public String getReleaseDate() {
        if (getStoredRelatedPartListEntry() == null) {
            return getFieldValue(FIELD_DA_PPA_RELDATE);
        }
        return getStoredRelatedPartListEntry().getFieldValue(FIELD_K_DATEFROM);
    }

    public String getPartNumber() {
        if (getStoredRelatedPartListEntry() == null) {
            return getFieldValue(FIELD_DA_PPA_SACH);
        }
        return getStoredRelatedPartListEntry().getFieldValue(FIELD_K_MATNR);
    }

    public String getAssemblySign() {
        if (getStoredRelatedPartListEntry() == null) {
            String partNo = getPartNumber();
            if (StrUtils.isValid(partNo)) {
                String[] fields = new String[]{ FIELD_M_ASSEMBLYSIGN };
                String[] whereFields = new String[]{ FIELD_M_MATNR, FIELD_M_VER };
                String[] whereValues = new String[]{ partNo, "" };
                EtkRecord rec = project.getDB().getRecord(TABLE_MAT, fields, whereFields, whereValues);
                if (rec != null) {
                    return rec.getField(FIELD_M_ASSEMBLYSIGN).getAsString();
                }
            }
            return null;
        }
        return getStoredRelatedPartListEntry().getPart().getFieldValue(FIELD_M_ASSEMBLYSIGN);
    }

    public String getHotSpot() {
        if (getStoredRelatedPartListEntry() == null) {
            return getFieldValue(FIELD_DA_PPA_POS);
        }
        return getStoredRelatedPartListEntry().getFieldValue(FIELD_K_POS);
    }

    public String getPicturePositionMarker() {
        return getFieldValue(FIELD_DA_PPA_PIC_POSITION_MARKER);
    }

    public int getPicPosSeqNo() {
        return getFieldValueAsInteger(FIELD_DA_PPA_SEQ_NO);
    }

    public boolean isSent() {
        return getFieldValueAsBoolean(FIELD_DA_PPA_SENT);
    }

    public String getExternalId() {
        StringBuilder str = new StringBuilder(getAsId().getOrderGuid());
        str.append(EXTERNAL_ID_DELIMITER);
        str.append(getAsId().getKatalogVari());
        str.append(EXTERNAL_ID_DELIMITER);
        str.append(getAsId().getKatalogVer());
        str.append(EXTERNAL_ID_DELIMITER);
        str.append(getAsId().getKatalogLfdNr());
        str.append(EXTERNAL_ID_DELIMITER);
        str.append(getAsId().getHotSpot());
        str.append(EXTERNAL_ID_DELIMITER);
        str.append(getAsId().getPartNumber());
        return str.toString();
    }

    public String getSourceKey() {
        return getFieldValue(FIELD_DA_PPA_SRC_KEY);
    }

    public void clearStoredEntry() {
        relatedEntryLoaded = false;
        relatedPartListEntry = null;
        serializedDBAttributes = null;
        combTextMap = null;
    }

    /**
     * Liefert die kombinierten Texte zum Zeitpunkt des Versendens
     *
     * @return
     */
    public List<iPartsDataCombText> getCombText() {
        getStoredRelatedPartListEntry();
        if (combTextMap != null) {
            return new ArrayList<>(combTextMap.values());
        }
        return null;
    }

    @Override
    public iPartsDataPicOrderPart cloneMe(EtkProject project) {
        iPartsDataPicOrderPart clone = new iPartsDataPicOrderPart(project, getAsId());
        clone.assignRecursively(project, this, DBActionOrigin.FROM_DB);
        return clone;
    }

    @Override
    public void initAttributesWithDefaultValues(DBActionOrigin origin) {
        super.initAttributesWithDefaultValues(origin);
        setFieldValue(iPartsConst.FIELD_DA_PPA_PIC_POSITION_MARKER, PIC_POS_MARKER_DEFAULT_VALUE, DBActionOrigin.FROM_EDIT);
    }

    /**
     * Liefert die gespeicherte Stücklistenposition aus der DB als echtes {@link EtkDataPartListEntry} Objekt
     *
     * @return
     */
    public EtkDataPartListEntry getStoredRelatedPartListEntry() {
        if (!relatedEntryLoaded) {
            // Aktuelle Daten des Stücklisteneintrags als JSON holen
            String partListEntryJSON = getFieldValueAsStringFromZippedBlob(FIELD_DA_PPA_PARTLIST_ENTRY_DATA);
            // Sind überhaupt welche vorhanden? Bei alten Bildaufträgen haben wir keine gespeicherten Stücklisteneinträge...
            if (StrUtils.isValid(partListEntryJSON)) {
                SerializedDbDataObjectAsJSON serializedDbDataObjectAsJSON = new SerializedDbDataObjectAsJSON(true);
                SerializedDBDataObject serializedPartListEntry = serializedDbDataObjectAsJSON.getFromJSON(partListEntryJSON);
                // Check fürs Gewissen: Schauen, ob die ID vom gespeicherten Stücklisteneintrag zur ID der aktuellen
                // Bildposition passt
                if (checkId(serializedPartListEntry)) {
                    // Beim Serialisieren wird das angehängte EtkDataPart und die kombinierten Texte als "composite children"
                    // des Stücklisteneintrag serialisiert. Beide haben ihre eindeutigen Namen (childName).
                    if (serializedPartListEntry.getCompositeChildren() != null) {
                        DBExtendedDataTypeProvider extendedDataTypeProviderForTextIds = EtkDataObject.getExtendedDataTypeProviderForTextIds(getEtkProject());
                        for (SerializedDBDataObjectList<SerializedDBDataObject> compChild : serializedPartListEntry.getCompositeChildren()) {
                            // Check, um welches composite child es sich handelt
                            switch (compChild.getChildName()) {
                                case CHILD_NAME_PART:
                                    if (compChild.getList().size() == 1) {
                                        // Genau ein EtkDataPart -> Baue die Stücklistenposition und das Teil zusammen und verknüpfe die Objekte
                                        EtkDataPartListEntry partListEntry = EtkDataObjectFactory.createDataPartListEntry(getEtkProject(),
                                                                                                                          new PartListEntryId("", "", ""));
                                        partListEntry.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
                                        serializedDBAttributes = serializedPartListEntry.createDBDataObjectAttributes(extendedDataTypeProviderForTextIds);
                                        partListEntry.assignAttributesValues(getEtkProject(), serializedDBAttributes,
                                                                             true, DBActionOrigin.FROM_DB);

                                        SerializedDBDataObject serializedPart = compChild.getList().get(0);
                                        if (serializedPart.getType().equals(PartId.TYPE)) {
                                            EtkDataPart part = EtkDataObjectFactory.createDataPart(getEtkProject(), new PartId("", ""));
                                            part.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
                                            part.assignAttributesValues(getEtkProject(), serializedPart.createDBDataObjectAttributes(extendedDataTypeProviderForTextIds),
                                                                        true, DBActionOrigin.FROM_DB);

                                            partListEntry.setAggregatedDataObject(EtkDataPartListEntry.AGGREGATE_NAME_PART, part);
                                            this.relatedPartListEntry = partListEntry;
                                        } else {
                                            Logger.log(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, "Invalid type as composite child of partListEntry in picOrderPart: "
                                                                                                      + serializedPart.getType());
                                        }
                                    }
                                    break;
                                case CHILD_NAME_SUPPLEMENTARY_TEXTS:
                                    for (SerializedDBDataObject singleText : compChild.getList()) {
                                        if (singleText.getType().equals(iPartsCombTextId.TYPE)) {
                                            iPartsDataCombText combText = new iPartsDataCombText(getEtkProject(), null);
                                            combText.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
                                            combText.assignAttributesValues(getEtkProject(), singleText.createDBDataObjectAttributes(extendedDataTypeProviderForTextIds),
                                                                            true, DBActionOrigin.FROM_DB);
                                            if (combTextMap == null) {
                                                combTextMap = new TreeMap<>();
                                            }
                                            combTextMap.put(combText.getAsId().getTextSeqNo(), combText);
                                        }
                                    }
                                    break;
                            }
                        }

                        if ((relatedPartListEntry != null) && (relatedPartListEntry instanceof iPartsDataPartListEntry)) {
                            // Das virtuelle Feld für den kombinierten Text muss hier befüllt werden, sonst wird der Text mit
                            // den Werten des echten Stücklisteneintrags befüllt
                            iPartsDataCombTextList combTexts = new iPartsDataCombTextList();
                            if (combTextMap != null) {
                                combTexts.addAll(combTextMap.values(), DBActionOrigin.FROM_DB);
                            }
                            String kLfdNr = getAsId().getPartListEntryId().getKLfdnr();
                            Map<String, String> neutralTextFromPartsForModule = new HashMap<>();
                            EtkMultiSprache neutralTextFromPart = relatedPartListEntry.getPart().getFieldValueAsMultiLanguage(FIELD_M_ADDTEXT);
                            if (neutralTextFromPart != null) {
                                neutralTextFromPartsForModule.put(kLfdNr, neutralTextFromPart.getText(getEtkProject().getDBLanguage()));
                            }
                            ((iPartsDataPartListEntry)relatedPartListEntry).calculateAndSetCombinedText(combTexts, neutralTextFromPartsForModule, true);
                        }
                    }
                }
            }
            this.relatedEntryLoaded = true;
        }
        return relatedPartListEntry;
    }

    public DBDataObjectAttributes getSerializedDBAttributes() {
        getStoredRelatedPartListEntry();
        return serializedDBAttributes;
    }

    /**
     * Überprüft, ob die ID des übergebenen {@link SerializedDBDataObject} zur ID der aktuellen Bildposition passt.
     *
     * @param serializedDBDataObject
     * @return
     */
    private boolean checkId(SerializedDBDataObject serializedDBDataObject) {
        IdWithType idFromSerializedObject = serializedDBDataObject.createId();
        PartListEntryId partListEntryId = getAsId().getPartListEntryId();
        return idFromSerializedObject.equals(partListEntryId);
    }

    public boolean hasDeletedPicturePositionMarker() {
        return getPicturePositionMarker().equals(PIC_POS_MARKER_DELETED_VALUE);
    }

    /**
     * Befüllt die aktuelle Bildposition mit den Werten der übergebenen Stücklistenposition
     *
     * @param partListEntry
     */
    public void fillFromRealEntry(EtkDataPartListEntry partListEntry) {
        if (partListEntry != null) {
            // Hier keine Logausgabe wegen dem Nachladen von Feldern
            boolean oldLogLoadFieldIfNeeded = partListEntry.isLogLoadFieldIfNeeded();
            partListEntry.setLogLoadFieldIfNeeded(false);
            try {
                // Erst die "alten" Felder befüllen
                setFieldValue(FIELD_DA_PPA_POS, partListEntry.getFieldValue(FIELD_K_POS), DBActionOrigin.FROM_EDIT); // iParts Position (Hotspot)
                setFieldValue(FIELD_DA_PPA_SACH, partListEntry.getFieldValue(FIELD_K_MATNR), DBActionOrigin.FROM_EDIT); // Sachnummer
                setFieldValue(FIELD_DA_PPA_RELDATE, partListEntry.getFieldValue(FIELD_K_DATEFROM), DBActionOrigin.FROM_EDIT); // ZGS der Sachnummer
                setFieldValue(FIELD_DA_PPA_ZGS, partListEntry.getPart().getFieldValue(FIELD_M_IMAGESTATE), DBActionOrigin.FROM_EDIT); // Freigabedatum der Sachnummer
            } finally {
                partListEntry.setLogLoadFieldIfNeeded(oldLogLoadFieldIfNeeded);
            }

            // Jetzt den Stücklisteneintrag als gezipptes JSON anhängen
            SerializedDbDataObjectAsJSON serializedDbDataObjectAsJSON = new SerializedDbDataObjectAsJSON(true);
            partListEntry.loadAllExtendedDataTypeAttributes();
            SerializedDBDataObject serializedDBDataObject = partListEntry.serialize(false, false, false, false, true, true);

            // Serialisieren der Kinder:
            // Kinder an das Hauptobjekt hängen (EtkDataPart und iPartsDataCombTextList an EtkDataPartlistEntry)
            List<SerializedDBDataObjectList<SerializedDBDataObject>> compChildren = new ArrayList<>();

            // EtkDataPart als Kind anhängen
            EtkDataObjectList partListEntryPartList = new GenericEtkDataObjectList();
            EtkDataPart part = EtkDataObjectFactory.createDataPart(getEtkProject(), partListEntry.getPart().getAsId());
            if (!part.existsInDB()) {
                part.initAttributesWithDefaultValues(DBActionOrigin.FROM_DB);
            }
            part.loadAllExtendedDataTypeAttributes();
            partListEntryPartList.add(part, DBActionOrigin.FROM_DB);
            SerializedDBDataObjectList serializedPartListEntryPartList = partListEntryPartList.serialize(false, false, false,
                                                                                                         true, true);
            serializedPartListEntryPartList.setChildName(CHILD_NAME_PART);
            compChildren.add(serializedPartListEntryPartList);

            // Ergänzungstexte als Kind anlegen
            iPartsDataCombTextList supplementaryTexts = iPartsDataCombTextList.loadForPartListEntryAndAllLanguages(partListEntry.getAsId(),
                                                                                                                   getEtkProject());
            if (!supplementaryTexts.isEmpty()) {
                SerializedDBDataObjectList serializedSupplementaryTexts = supplementaryTexts.serialize(false, false, false,
                                                                                                       true, true);
                serializedSupplementaryTexts.setChildName(CHILD_NAME_SUPPLEMENTARY_TEXTS);
                compChildren.add(serializedSupplementaryTexts);
            }

            serializedDBDataObject.setCompositeChildren(compChildren);

            // JSON Daten setzen
            setFieldValueAsZippedBlobFromString(FIELD_DA_PPA_PARTLIST_ENTRY_DATA, serializedDbDataObjectAsJSON.getAsJSON(serializedDBDataObject),
                                                DBActionOrigin.FROM_EDIT);
            relatedEntryLoaded = false;
        }
    }
}
