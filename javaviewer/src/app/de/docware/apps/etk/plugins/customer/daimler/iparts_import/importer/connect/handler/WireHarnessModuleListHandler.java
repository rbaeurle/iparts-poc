/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.connect.handler;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataWireHarness;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataWireHarnessList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsWireHarnessId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsNumberHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.bigdata.AbstractSAXPushHandlerImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.connect.helper.WireHarness;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.util.StrUtils;
import de.docware.util.misc.id.IdWithType;

import java.util.*;

import static de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.connect.handler.WireHarnessContactListHandler.*;
import static de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.connect.handler.WireHarnessPartListHandler.USED_IN_CONTACT_LIST;

public class WireHarnessModuleListHandler extends AbstractWireHarnessHandler {

    private static final String TRIGGER_ELEMENT = "Module";

    private final Map<String, AbstractWireHarnessSubHandler> dataSubHandlers;
    private final Map<String, iPartsDataWireHarnessList> harnessListMap;
    private final Map<String, String> materialStammMap;

    private AbstractWireHarnessSubHandler handler;
    private WireHarnessAccessoryListHandler accessoryListHandler; // Handler für die Zusatzteile
    protected Map<String, String> currentSubRecord;

    public WireHarnessModuleListHandler(EtkProject project, AbstractSAXPushHandlerImporter importer) {
        super(project, TRIGGER_ELEMENT, "ContactList", importer);
        dataSubHandlers = new HashMap<>();
        harnessListMap = new HashMap<>();
        materialStammMap = new HashMap<>();
        handler = null;

        initSubHandlers(importer);
    }

    private void initSubHandlers(AbstractSAXPushHandlerImporter importer) {
        AbstractWireHarnessSubHandler subHandler = new WireHarnessPartListHandler(getProject(), importer);
        dataSubHandlers.put(subHandler.getMainXMLTag(), subHandler);
        subHandler = new WireHarnessContactListHandler(getProject(), importer);
        dataSubHandlers.put(subHandler.getMainXMLTag(), subHandler);
        accessoryListHandler = new WireHarnessAccessoryListHandler(getProject(), importer);
        dataSubHandlers.put(accessoryListHandler.getMainXMLTag(), accessoryListHandler);
    }

    @Override
    protected void handleCurrentRecord() {
        // hier komm ich raus, wenn der EndTag Module kommt
        // Ergebnisse aus den resultMap's der beiden Sub-Handler verarbeiten

        Map<String, String> record = getCurrentRecord();
        if (record != null) {
            Map<IdWithType, DBDataObjectAttributes> partList = getResultListFromHandler(WireHarnessPartListHandler.getTriggerElement());
            Map<IdWithType, DBDataObjectAttributes> contactList = getResultListFromHandler(WireHarnessContactListHandler.getTriggerElement());

            // Zuerst alle vorhandenen Leitungsbaukästen ermitteln.
            Set<String> allWireHarnessesList = getAllWireHarnessesList(partList, contactList);
            // und nacheinander abarbeiten
            for (String wireHarnessNo : allWireHarnessesList) {
                // Dazu erst einmal das maximale Datum pro Leitungsbaukasten aus den beiden Listen ermitteln.
                String maxPartsDate = getMaxDatePerWireHarness(wireHarnessNo, partList);
                String maxContactsDate = getMaxDatePerWireHarness(wireHarnessNo, contactList);
                // Fehlermeldung, wenn die Datumswerte unterschiedlich sind.
                if (maxPartsDate.compareTo(maxContactsDate) != 0) {
                    addWarning("!!Für den Leitungssatzbaukasten \"%1\" sind zwei unterschiedliche maximale Datumswerte vorhanden. PartList: \"%2\", ContactList: \"%3\"",
                               wireHarnessNo, maxPartsDate, maxContactsDate);
                }

                // Die beiden Listen weiter einschränken und nur die Elemente lassen, die mit dem höchsten Datum übereinstimmen.
                reduceElements(maxPartsDate, partList);
                reduceElements(maxContactsDate, contactList);

                // Den Leitungsbaukasten zuerst in der Liste der gespeicherten Leitungsbaukästen suchen ...
                iPartsDataWireHarnessList dbList = harnessListMap.get(wireHarnessNo);
                // ... und nur, wenn er nicht gefunden wurde, aus der Datenbank laden.
                if (dbList == null) {
                    // Jetzt die Liste zum Leitungsbaukasten aus der Datenbank laden, falls vorhanden.
                    // Wenn nicht in der DB, kommt hier eine leere Liste an.
                    dbList = iPartsDataWireHarnessList.loadOneWireHarness(getProject(), wireHarnessNo, null);
                }

                // Wenn in der Datenbank nichts gefunden wurde, ist die dbList leer und als maxDBDate kommt "1800-01-01" zurück.
                String maxDBDate = getMaxDateFromDbList(dbList);
                // Nur speichern, wenn der Leitungsbaukasten aus der Importdatei neuer oder gleich ist, als das was wir schon haben.
                if (((maxDBDate.compareTo(maxPartsDate)) > 0) || ((maxDBDate.compareTo(maxContactsDate)) > 0)) {
                    addWarning("!!Für den Leitungssatzbaukasten \"%1\" ist der gleiche oder ein höherer Datendstand bereits gespeichert.",
                               iPartsNumberHelper.formatPartNo(getProject(), wireHarnessNo));
                } else {
                    // Die beiden Listen in eine speicherbare verwandeln
                    dbList = processWireHarness(wireHarnessNo, partList, contactList);
                }

                // Den aktuellen Stand des Leitungssatzbaukastens merken.
                // Er wird ggf. hier in der Map durch einen neueren ersetzt. Der letzte Stand wird später gespeichert.
                harnessListMap.put(wireHarnessNo, dbList);
            }
        }
        // Handler aufräumen
        clearSubHandlerData(WireHarnessPartListHandler.getTriggerElement());
        clearSubHandlerData(WireHarnessContactListHandler.getTriggerElement());
        clearSubHandlerData(WireHarnessAccessoryListHandler.getTriggerElement());
    }

    private iPartsDataWireHarnessList processWireHarness(String wireHarnessNo, Map<IdWithType, DBDataObjectAttributes> partList, Map<IdWithType, DBDataObjectAttributes> contactList) {

        iPartsDataWireHarnessList list = new iPartsDataWireHarnessList();

        // Die contactList durchgehen und mit den Informationen aus der partList anreichern.
        for (Map.Entry<IdWithType, DBDataObjectAttributes> entry : contactList.entrySet()) {
            iPartsWireHarnessId id = (iPartsWireHarnessId)entry.getKey();
            // Wenn der Leitungssatzbaukasten der gesuchte ist ...
            if (id.getSnr().equals(wireHarnessNo)) {
                DBDataObjectAttributes attributes = entry.getValue();

                String subSnr = attributes.getFieldValue(SUB_SNR);

                // Jetzt das zu speichernde Objekt anlegen und füllen
                iPartsWireHarnessId whId = new iPartsWireHarnessId(attributes.getFieldValue(LEITUNGSSATZ), attributes.getFieldValue(REF),
                                                                   attributes.getFieldValue(STECKER_NO), subSnr,
                                                                   attributes.getFieldValue(SUB_SNR_POS));

                iPartsDataWireHarness item = new iPartsDataWireHarness(getProject(), whId);
                item.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);

                item.setFieldValue(FIELD_DWH_SNR_TYPE, attributes.getFieldValue(SUB_SNR_TYPE), DBActionOrigin.FROM_EDIT);
                item.setFieldValue(FIELD_DWH_CONTACT_DATASET_DATE, attributes.getFieldValue(DATENSTAND), DBActionOrigin.FROM_EDIT);
                item.setFieldValue(FIELD_DWH_PART_DATASET_DATE, getPartDatasetDate(whId, partList), DBActionOrigin.FROM_EDIT);
                item.setFieldValueAsMultiLanguage(FIELD_DWH_CONTACT_ADD_TEXT, attributes.getField(BENENNUNG).getAsMultiLanguageInternal(), DBActionOrigin.FROM_EDIT);

                // Das gefüllte Element in die Ergebnisliste schreiben.
                list.add(item, DBActionOrigin.FROM_EDIT);

                // Zum Leitungssatz die Beschreibung bestimmen.
                String descr = attributes.getFieldValue(BENENNUNG);
                materialStammMap.putIfAbsent(wireHarnessNo, descr);

                // Auch zur unteren Sachnummer die Beschreibung bestimmen, sie kommt IMMER aus der partList!
                descr = getPartDatasetDescription(whId, partList);
                materialStammMap.putIfAbsent(whId.getSubSnr(), descr);

                // Alle unbenutzten Teile der PartList müssen nachher noch gesondert behandelt werden.
                setPartUsed(whId, partList);
            }
        }

        // Jetzt muss für alle noch alle unbenutzten Elemente der PartList ein "Zubehör"-Eintrag erzeugt werden.
        for (Map.Entry<IdWithType, DBDataObjectAttributes> entry : partList.entrySet()) {
            iPartsWireHarnessId id = (iPartsWireHarnessId)entry.getKey();
            // Wenn der Leitungssatzbaukasten der gesuchte ist ...
            if (id.getSnr().equals(wireHarnessNo)) {
                DBDataObjectAttributes attributes = entry.getValue();
                // Wenn während der Verarbeitung NICHT festgestellt wurde, dass dieses Teil aus der partList gebraucht wurde,
                // einen Zubehördatensatz anlegen
                if (attributes.getFieldValue(USED_IN_CONTACT_LIST).isEmpty()) {
                    String partNo = id.getSubSnr();
                    List<WireHarnessAccessoryListHandler.AccessoryListItem> accessoryItemsForPartNo = accessoryListHandler.getAccessoryItemForWireHarnessAndPartNo(partNo, wireHarnessNo);
                    if ((accessoryItemsForPartNo != null) && !accessoryItemsForPartNo.isEmpty()) {
                        accessoryItemsForPartNo.forEach(itemForPart -> {
                            iPartsWireHarnessId idWithRef = new iPartsWireHarnessId(id.getSnr(), itemForPart.getReference(), id.getConnectorNo(), id.getSubSnr(), id.getPos());
                            iPartsDataWireHarness item = addAccessoryEntry(idWithRef, attributes, list);
                            item.setFieldValueAsMultiLanguage(FIELD_DWH_CONTACT_ADD_TEXT, itemForPart.getText(), DBActionOrigin.FROM_EDIT);
                        });

                    } else {
                        addAccessoryEntry(id, attributes, list);
                    }
                    String desc = getPartDatasetDescription(id, partList);
                    materialStammMap.putIfAbsent(id.getSubSnr(), desc);
                }
            }
        }
        return list;
    }

    private iPartsDataWireHarness addAccessoryEntry(iPartsWireHarnessId id, DBDataObjectAttributes attributes, iPartsDataWireHarnessList list) {
        iPartsDataWireHarness item = new iPartsDataWireHarness(getProject(), id);
        item.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
        item.setFieldValue(FIELD_DWH_SNR_TYPE, WireHarness.ACCESSORY.getDbValue(), DBActionOrigin.FROM_EDIT);
        item.setFieldValue(FIELD_DWH_PART_DATASET_DATE, attributes.getFieldValue(DATENSTAND) + "E", DBActionOrigin.FROM_EDIT);
        // Dieses Element in die Ergebnisliste schreiben.
        list.add(item, DBActionOrigin.FROM_EDIT);
        return item;
    }

    private String getPartDatasetDescription(iPartsWireHarnessId whId, Map<IdWithType, DBDataObjectAttributes> partList) {
        DBDataObjectAttributes attributes = getAttributesFromPartList(whId, partList);
        if (attributes != null) {
            return attributes.getFieldValue(BENENNUNG);
        }
        return "";
    }

    /**
     * Ermittelt den Datenstand den die übergebene SubSnr + Pos in der partList hat.
     *
     * @param whId
     * @param partList
     * @return
     */
    private String getPartDatasetDate(iPartsWireHarnessId whId, Map<IdWithType, DBDataObjectAttributes> partList) {
        DBDataObjectAttributes attributes = getAttributesFromPartList(whId, partList);
        if (attributes != null) {
            return attributes.getFieldValue(DATENSTAND);
        }
        return "";
    }

    /**
     * Setzt die Verwendung in der partList. Für alle nicht in der contactList enthaltenen Sachnummern wird am Ende
     * ein Zubehöreintrag erzeugt.
     *
     * @param whId
     * @param partList
     */
    private void setPartUsed(iPartsWireHarnessId whId, Map<IdWithType, DBDataObjectAttributes> partList) {
        DBDataObjectAttributes attributes = getAttributesFromPartList(whId, partList);
        if (attributes != null) {
            DBDataObjectAttribute oneAttribute = attributes.getField(USED_IN_CONTACT_LIST);
            oneAttribute.setValueAsString("IN_USE", DBActionOrigin.FROM_DB);
        }
    }

    private DBDataObjectAttributes getAttributesFromPartList(iPartsWireHarnessId whId, Map<IdWithType, DBDataObjectAttributes> partList) {
        iPartsWireHarnessId partId = getPartHarnessIDFromID(whId);
        return partList.get(partId);
    }

    private iPartsWireHarnessId getPartHarnessIDFromID(iPartsWireHarnessId whId) {
        return new iPartsWireHarnessId(whId.getSnr(), "", "", whId.getSubSnr(), whId.getPos());
    }

    /**
     * Alle Elemente aus der Liste löschen, deren Datum nicht mit dem übergebene Datum übereinstimmt.
     *
     * @param maxDate
     * @param map
     */
    private void reduceElements(String maxDate, Map<IdWithType, DBDataObjectAttributes> map) {
        Iterator iter = map.keySet().iterator();
        while (iter.hasNext()) {
            IdWithType id = (IdWithType)iter.next();
            DBDataObjectAttributes attributes = map.get(id);
            if (!attributes.getFieldValue(DATENSTAND).equals(maxDate)) {
                addMessage("!!Element %1 wird entfernt. (wegen %2 <> %3",
                           id.toStringForLogMessages(), attributes.getFieldValue(DATENSTAND), maxDate);
                iter.remove();
            }
        }
    }

    /**
     * Ermittelt aus der Datenbankliste das höchste Datenstandsdatum.
     *
     * @param dbList
     * @return
     */
    private String getMaxDateFromDbList(iPartsDataWireHarnessList dbList) {
        String maxDate = "18000101";
        String foundPartDate;
        String foundContactDate;
        for (iPartsDataWireHarness wireHarness : dbList) {
            foundPartDate = wireHarness.getFieldValue(FIELD_DWH_PART_DATASET_DATE);
            foundContactDate = wireHarness.getFieldValue(FIELD_DWH_CONTACT_DATASET_DATE);
            if (StrUtils.isValid(foundPartDate) && (foundPartDate.compareTo(maxDate) > 0)) {
                maxDate = foundPartDate;
            }
            if (StrUtils.isValid(foundContactDate) && (foundContactDate.compareTo(maxDate) > 0)) {
                maxDate = foundContactDate;
            }
        }
        return maxDate;
    }

    /**
     * Ermittelt das höchste Datum für den übergebenen Leitungssatzbaukasten aus der Parts- bzw. der Contacts-Liste.
     *
     * @param wireHarnessNo
     * @param list
     * @return
     */
    private String getMaxDatePerWireHarness(String wireHarnessNo, Map<IdWithType, DBDataObjectAttributes> list) {
        String maxDate = "18000101";
        String foundDate;
        // Über die Liste alle Leitungssatzbaukästen ermitteln.
        for (Map.Entry<IdWithType, DBDataObjectAttributes> entry : list.entrySet()) {
            iPartsWireHarnessId id = (iPartsWireHarnessId)entry.getKey();
            // Wenn der Leitungssatzbaukasten der gesuchte ist ...
            if (id.getSnr().equals(wireHarnessNo)) {
                DBDataObjectAttributes attributes = entry.getValue();
                foundDate = attributes.getFieldValue(DATENSTAND);
                if (foundDate.compareTo(maxDate) > 0) {
                    maxDate = foundDate;
                }
            }
        }
        return maxDate;
    }

    /**
     * Ermittelt über die partsList und die contactList alle vorhandenen Leitungsbaukästen.
     *
     * @param partList
     * @param contactList
     * @return
     */
    private Set<String> getAllWireHarnessesList(Map<IdWithType, DBDataObjectAttributes> partList,
                                                Map<IdWithType, DBDataObjectAttributes> contactList) {
        Set<String> allWHs = new HashSet<>();
        getAllWireHarnessesFromList(allWHs, partList);
        getAllWireHarnessesFromList(allWHs, contactList);

        return allWHs;
    }

    /**
     * Addiert alle in der Liste enthaltenen Leitungssatzbaukästen in das (unique) resultSet.
     *
     * @param resultSet
     * @param list
     */
    private void getAllWireHarnessesFromList(Set<String> resultSet, Map<IdWithType, DBDataObjectAttributes> list) {
        for (IdWithType id : list.keySet()) {
            resultSet.add(((iPartsWireHarnessId)id).getSnr());
        }
    }

    /**
     * Holt die Liste der eingelesenen 'PartsList' oder 'ContactLists' von Handler.
     * Sollte sie nicht vorhanden sein, wird wenigstens eine leere Liste angelegt und zurückgegeben.
     *
     * @param triggerElement
     * @return
     */
    private Map<IdWithType, DBDataObjectAttributes> getResultListFromHandler(String triggerElement) {
        Map<IdWithType, DBDataObjectAttributes> resultList = null;
        AbstractWireHarnessSubHandler handler = dataSubHandlers.get(triggerElement);
        if (handler != null) {
            resultList = handler.getResultMap();
        }
        if (resultList == null) {
            resultList = new HashMap<>();
        }
        return resultList;
    }

    /**
     * Definiertes Leeren der Ergebnisliste eines Handlers.
     *
     * @param triggerElement
     */
    private void clearSubHandlerData(String triggerElement) {
        AbstractWireHarnessSubHandler handler = dataSubHandlers.get(triggerElement);
        if (handler != null) {
            handler.clearData();
        }
    }

    public Map<String, iPartsDataWireHarnessList> getHarnessListMap() {
        return harnessListMap;
    }

    public Map<String, String> getMaterialStammMap() {
        return materialStammMap;
    }

    @Override
    protected void onStartElement(String uri, String localName, String qName, Map<String, String> attributes) {
        if (handler == null) {
            AbstractWireHarnessSubHandler subHandler = dataSubHandlers.get(localName);
            if (subHandler != null) {
                handler = subHandler;
                currentSubRecord = new HashMap<>();
                return;
            }
        }
        super.onStartElement(uri, localName, qName, attributes);
    }

    @Override
    protected void onEndElement(String uri, String localName, String qName) {
        if (handler == null) {
            super.onEndElement(uri, localName, qName);
        } else {
            TagData tagData = getCurrentTagData();
            String tagName = tagData.getTagName();
            String content = tagData.getTextValue().toString();
            if (!tagName.equals(handler.getMainXMLTag())) {
                // Schreibe den Wert des aktuellen Tags an der aktuellen Record
                setSubFieldValue(tagName, content);
            } else {
                // Ende des Haupttags erreicht, also ist der Record komplett. Diesen also jetzt verarbeiten
                handler.doHandleCurrentRecord(currentSubRecord);
                handler = null;
            }
        }
    }

    protected void setSubFieldValue(String fieldname, String content) {
        if (currentSubRecord != null) {
            currentSubRecord.put(fieldname, content);
        }
    }

    public void clearMaps() {
        if (harnessListMap != null) {
            harnessListMap.clear();
        }
        if (materialStammMap != null) {
            materialStammMap.clear();
        }
    }
}
