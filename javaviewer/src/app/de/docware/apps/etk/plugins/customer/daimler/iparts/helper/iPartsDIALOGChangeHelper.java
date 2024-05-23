/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsFactoryDataTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.util.StrUtils;
import de.docware.util.misc.id.IdWithType;

import java.util.*;

public class iPartsDIALOGChangeHelper {


    /**
     * Überprüft, ob die übergebenen PEMs der übergebenen Werkseinsatzdaten Änderungen in den zu den PEMs gehörenden
     * Rückmeldedaten haben. Falls ja, wird an das übergeben Werkseinsatzdatenobjekt geschrieben, welche Rückmeldedaten
     * sich geändert haben.
     *
     * @param project
     * @param factoryData
     * @param pemFromValue
     * @param pemToValue
     */
    public static void checkPartListFactoryData(EtkProject project, iPartsDataFactoryData factoryData, String pemFromValue, String pemToValue) {
        // Hole alle PEMs aus allen Änderungsobjekten zum BCTE Schlüssel
        Set<String> pemFromDIALOGChangeData = getPemsFromDIALOGChange(project, factoryData.getAsId().getGuid());
        String pemTextForChange = "";
        // Check, ob Änderungen an Rückmeldedaten zur PEM ab vorhanden sind
        if (pemFromDIALOGChangeData.contains(pemFromValue)) {
            pemTextForChange = TranslationHandler.translate("!!PEM AB");
        }
        // Check, ob Änderungen an Rückmeldedaten zur PEM ab vorhanden sind
        if (pemFromDIALOGChangeData.contains(pemToValue)) {
            if (StrUtils.isValid(pemTextForChange)) {
                pemTextForChange += " " + TranslationHandler.translate("!!und") + " ";
            }
            pemTextForChange += TranslationHandler.translate("!!PEM BIS");
        }
        if (StrUtils.isValid(pemTextForChange)) {
            pemTextForChange += " " + TranslationHandler.translate(iPartsResponseDataId.DESCRIPTION);
        }
        // Setze den Änderungstext
        if (factoryData.getAttributes().fieldExists(iPartsDataVirtualFieldsDefinition.DFD_DIALOG_CHANGE)) {
            factoryData.setFieldValue(iPartsDataVirtualFieldsDefinition.DFD_DIALOG_CHANGE, pemTextForChange, DBActionOrigin.FROM_DB);
        } else {
            factoryData.getAttributes().addField(iPartsDataVirtualFieldsDefinition.DFD_DIALOG_CHANGE, pemTextForChange, true, DBActionOrigin.FROM_DB);
        }
    }

    /**
     * Bestimmt alle PEMs aus allen Rückmeldedaten-Änderungsobjekten aus DA_DIALOG_CHANGES zum übergebenen BCTE Schlüssel
     *
     * @param project
     * @param bcteKey
     * @return
     */
    private static Set<String> getPemsFromDIALOGChange(EtkProject project, String bcteKey) {
        Set<String> result = new HashSet<>();
        if (StrUtils.isValid(bcteKey)) {
            // Lade alle DA_DIALOG_CHANGES Objekte zum BCTE Schlüssel
            iPartsDataDIALOGChangeList dialogChangesForBCTE = iPartsDataDIALOGChangeList.loadForBCTEKey(bcteKey, project);
            if (!dialogChangesForBCTE.isEmpty()) {
                for (iPartsDataDIALOGChange dialogChange : dialogChangesForBCTE) {
                    // Für Änderungen an Rückmeldedaten müssen nur Objekte vom Typ "RESPONSE_DATA" betrachtet werden
                    if (iPartsDataDIALOGChange.ChangeType.getChangeType(dialogChange.getAsId().getDoType()) == iPartsDataDIALOGChange.ChangeType.RESPONSE_DATA) {
                        // Die Objekt-Id ist immer die iPartsResponseDataId. Deshalb kann hier aus dem ID String eine
                        // "echte" iPartsResponseDataId erzeugt werden.
                        IdWithType id = IdWithType.fromDBString(iPartsResponseDataId.TYPE, dialogChange.getAsId().getDoId());
                        // Extrahiere die PEM aus der iPartsResponseDataId
                        if ((id != null) && (id.getIdLength() >= 5)) {
                            String pem = id.getValue(5);
                            if (StrUtils.isValid(pem)) {
                                result.add(pem);
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * Überprüft, ob die übergebenen Teil-zu-Farbtabelle Zuordnungen DIALOG-Änderungen in ihren Werkseinsatzdaten enthalten.
     * Falls ja, wird an die übergebenen Zuordnungen geschrieben, welche Werkseinsatzdaten sich geändert haben (AS und/oder Produktion).
     *
     * Um diese Überprüfungen machen zu können, müssen die übergebenen Änderungsobjekte aus DA_DIALOG_CHANGES vorsortiert
     * werden. Dies wird erreicht indem die {@link iPartsDataDIALOGChangeList} Objekte an die <code>sortFactoryDataDIALOGChanges</code>
     * Methode übergeben werden.
     *
     * Siehe {@link iPartsDIALOGChangeHelper#sortFactoryDataDIALOGChanges(EtkProject project, iPartsDataDIALOGChangeList dialogChangesForBCTEExtern, String partNo)}
     *
     * @param variantTablesDataStructures Teil-zu-Farbtabelle Zuordnungen
     * @param dialogChangesForBCTEExtern  aus DA_DIALOG_CHANGE geladenen Änderungsobjekte
     */
    public static void checkVariantToPartFactoryData(EtkProject project, List<ColorTableHelper.VariantTablesDataStructure> variantTablesDataStructures,
                                                     iPartsDataDIALOGChangeList dialogChangesForBCTEExtern) {
        Map<String, Map<iPartsFactoryDataTypes, Set<IdWithType>>> sortedData = sortFactoryDataDIALOGChanges(project, dialogChangesForBCTEExtern);
        checkVariantToPartFactoryData(variantTablesDataStructures, sortedData);
    }

    /**
     * Überprüft, ob die übergebenen Teil-zu-Farbtabelle Zuordnungen DIALOG-Änderungen in ihren Werkseinsatzdaten enthalten.
     * Falls ja, wird an die übergebenen Zuordnungen geschrieben, welche Werkseinsatzdaten sich geändert haben (AS und/oder Produktion).
     *
     * Um diese Überprüfungen machen zu können, müssen die alle relevanten Änderungsobjekte zur übergebenen Teilenummer
     * aus DA_DIALOG_CHANGES geladen und vorsortiert werden. Dies wird erreicht indem die übergebene Teilenummer <code>partNo</code>
     * an die <code>sortFactoryDataDIALOGChanges</code> Methode übergeben wird.
     *
     * Siehe {@link iPartsDIALOGChangeHelper#sortFactoryDataDIALOGChanges(EtkProject project, iPartsDataDIALOGChangeList dialogChangesForBCTEExtern, String partNo)}
     *
     * @param variantTablesDataStructures Teil-zu-Farbtabelle Zuordnungen
     * @param partNo                      Teilenummer zu der alle Änderungsobjekte aus DA_DIALOG_CHANGES geladen werden
     */
    public static void checkVariantToPartFactoryData(EtkProject project, List<ColorTableHelper.VariantTablesDataStructure> variantTablesDataStructures,
                                                     String partNo) {
        Map<String, Map<iPartsFactoryDataTypes, Set<IdWithType>>> sortedData = sortFactoryDataDIALOGChanges(project, partNo);
        checkVariantToPartFactoryData(variantTablesDataStructures, sortedData);
    }

    /**
     * Überprüft, ob die übergebenen Teil-zu-Farbtabelle Zuordnungen DIALOG-Änderungen in ihren Werkseinsatzdaten enthalten.
     * Falls ja, wird an die übergebenen Zuordnungen geschrieben, welche Werkseinsatzdaten sich geändert haben (AS und/oder Produktion).
     *
     * Um diese Überprüfungen machen zu können, müssen die geladenen Änderungsobjekte aus DA_DIALOG_CHANGES vorsortiert
     * übergeben werden, siehe {@link iPartsDIALOGChangeHelper#sortFactoryDataDIALOGChanges(EtkProject project, iPartsDataDIALOGChangeList dialogChangesForBCTEExtern, String partNo)}
     *
     * @param variantTablesDataStructures
     * @param sortedDialogChangesData
     */
    private static void checkVariantToPartFactoryData(List<ColorTableHelper.VariantTablesDataStructure> variantTablesDataStructures,
                                                      Map<String, Map<iPartsFactoryDataTypes, Set<IdWithType>>> sortedDialogChangesData) {
        if (sortedDialogChangesData != null) {
            for (ColorTableHelper.VariantTablesDataStructure variantTableData : variantTablesDataStructures) {
                iPartsDataColorTableToPart colorTableToPart = variantTableData.colorTableToPart;
                checkSingleVariantToPartObject(sortedDialogChangesData, colorTableToPart, true);
            }
        }
    }

    /**
     * Check, ob eine der übergebenen IDs die gleiche Position und das gleiche Sdata wie die eigentliche Zuordnung besitzt.
     *
     * @param colorTablePartId
     * @param idsForType
     * @return
     */
    private static boolean isValidColorToPartData(iPartsColorTableToPartId colorTablePartId, Set<IdWithType> idsForType) {
        for (IdWithType idForFactoryChange : idsForType) {
            if (idForFactoryChange.getValue(2).equals(colorTablePartId.getPosition()) && idForFactoryChange.getValue(6).equals(colorTablePartId.getSDATA())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check, ob eine der übergebenen IDs die gleiche Position und das gleiche Sdata wie der eigentliche Farbtabelleninhalt besitzt.
     *
     * @param colorTableContent
     * @param idsForType
     * @return
     */
    private static boolean isValidColorContentData(iPartsDataColorTableContent colorTableContent, Set<IdWithType> idsForType) {
        for (IdWithType idForFactoryChange : idsForType) {
            if (idForFactoryChange.getValue(2).equals(colorTableContent.getAsId().getPosition())
                && idForFactoryChange.getValue(6).equals(colorTableContent.getAsId().getSDATA())) {
                return true;
            }
        }
        return false;
    }

    private static String getFactoryDataASText() {
        return TranslationHandler.translate("!!Werkseinsatzdaten AS");
    }

    private static String appendFactoryDataProductionText(String fieldValue) {
        if (StrUtils.isValid(fieldValue)) {
            return fieldValue + " " + TranslationHandler.translate("!!und Produktion");
        } else {
            return TranslationHandler.translate("!!Werkseinsatzdaten Produktion");
        }
    }


    /**
     * Überprüft, ob die übergebenen Farbtabelleninhalte DIALOG-Änderungen in ihren Werkseinsatzdaten enthalten.
     * Falls ja, wird an die übergebenen Farbtabelleninhalte geschrieben, welche Werkseinsatzdaten sich geändert haben (AS oder Produktion.
     *
     * Um diese Überprüfungen machen zu können, müssen die alle relevanten Änderungsobjekte <code>dialogChangesForBCTEExtern</code>
     * zur übergebenen Teilenummer aus DA_DIALOG_CHANGES geladen und vorsortiert werden. Dies wird erreicht indem die
     * übergebene Teilenummer <code>partNo</code> an die <code>sortFactoryDataDIALOGChanges</code> Methode übergeben wird.
     *
     * Siehe {@link iPartsDIALOGChangeHelper#sortFactoryDataDIALOGChanges(EtkProject project, iPartsDataDIALOGChangeList dialogChangesForBCTEExtern, String partNo)}
     *
     * @param variantsDataStructures Farbtabelleninhalte
     * @param partNo                 Teilenummer zu der alle Änderungsobjekte aus DA_DIALOG_CHANGES geladen werden
     */
    public static void checkVariantDataFactoryData(EtkProject project, List<ColorTableHelper.VariantsDataStructure> variantsDataStructures,
                                                   String partNo) {
        Map<String, Map<iPartsFactoryDataTypes, Set<IdWithType>>> sortedData = sortFactoryDataDIALOGChanges(project, partNo);
        checkVariantDataFactoryData(variantsDataStructures, sortedData);

    }

    /**
     * Überprüft, ob die übergebenen Farbtabelleninhalte DIALOG-Änderungen in ihren Werkseinsatzdaten enthalten.
     * Falls ja, wird an die übergebenen Farbtabelleninhalte geschrieben, welche Werkseinsatzdaten sich geändert haben (AS oder Produktion.
     *
     * Um diese Überprüfungen machen zu können, müssen die übergebenen Änderungsobjekte <code>dialogChangesForBCTEExtern</code>
     * aus DA_DIALOG_CHANGES vorsortiert werden. Dies wird erreicht indem die {@link iPartsDataDIALOGChangeList} Objekte
     * an die <code>sortFactoryDataDIALOGChanges</code> Methode übergeben werden.
     *
     * Siehe {@link iPartsDIALOGChangeHelper#sortFactoryDataDIALOGChanges(EtkProject project, iPartsDataDIALOGChangeList dialogChangesForBCTEExtern, String partNo)}
     *
     * @param variantsDataStructures     Farbtabelleninhalte
     * @param dialogChangesForBCTEExtern aus DA_DIALOG_CHANGE geladenen Änderungsobjekte
     */
    public static void checkVariantDataFactoryData(EtkProject project, List<ColorTableHelper.VariantsDataStructure> variantsDataStructures,
                                                   iPartsDataDIALOGChangeList dialogChangesForBCTEExtern) {
        Map<String, Map<iPartsFactoryDataTypes, Set<IdWithType>>> sortedData = sortFactoryDataDIALOGChanges(project, dialogChangesForBCTEExtern);
        checkVariantDataFactoryData(variantsDataStructures, sortedData);

    }

    /**
     * Überprüft, ob die übergebenen Farbtabelleninhalte DIALOG-Änderungen in ihren Werkseinsatzdaten enthalten.
     * Falls ja, wird an die übergebenen Farbtabelleninhalte geschrieben, welche Werkseinsatzdaten sich geändert haben (AS oder Produktion.
     *
     * Um diese Überprüfungen machen zu können, müssen die geladenen Änderungsobjekte <code>dialogChangesForBCTEExtern</code>
     * aus DA_DIALOG_CHANGES vorsortiert übergeben werden, siehe {@link iPartsDIALOGChangeHelper#sortFactoryDataDIALOGChanges(EtkProject project, iPartsDataDIALOGChangeList dialogChangesForBCTEExtern, String partNo)}
     *
     * @param variantsDataStructures  Farbtabelleninhalte
     * @param sortedDialogChangesData aus DA_DIALOG_CHANGE geladenen Änderungsobjekte
     */
    private static void checkVariantDataFactoryData(List<ColorTableHelper.VariantsDataStructure> variantsDataStructures,
                                                    Map<String, Map<iPartsFactoryDataTypes, Set<IdWithType>>> sortedDialogChangesData) {
        if (sortedDialogChangesData != null) {
            for (ColorTableHelper.VariantsDataStructure variantData : variantsDataStructures) {
                iPartsDataColorTableContent colorTableContent = variantData.colorTableContent;
                // Die Überprüfung macht nur Sinn, wenn das Objekt geladen ist
                if (!colorTableContent.existsInDB()) {
                    colorTableContent.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
                }
                // Bestimme die Farbtabellennummer (QFT-Nummer)
                String colorTableId = colorTableContent.getAsId().getColorTableId();
                // Hole alle Werkseinsatzdatenänderungen zur QFT-Nummer
                Map<iPartsFactoryDataTypes, Set<IdWithType>> typesForId = sortedDialogChangesData.get(colorTableId);
                // Falls keine Vorhanden sind, kann auch keine Änderung existieren
                if (typesForId != null) {
                    String fieldValue = "";
                    // Hole alle AS-Werkseinsatzdatenänderungen
                    Set<IdWithType> idsForType = typesForId.get(iPartsFactoryDataTypes.COLORTABLE_CONTENT_AS);
                    if ((idsForType != null) && isValidColorContentData(colorTableContent, idsForType)) {
                        // Sind welche vorahnden und passen die Attribute zum Farbtabelleninhalt, dann setze den AS-Text
                        fieldValue = getFactoryDataASText();
                    }
                    // Hole alle Produktion-Werkseinsatzdatenänderungen
                    idsForType = typesForId.get(iPartsFactoryDataTypes.COLORTABLE_CONTENT);
                    if ((idsForType != null) && isValidColorContentData(colorTableContent, idsForType)) {
                        // Sind welche vorhanden und passen die Attribute zum Farbtabelleninhalt, dann setze den Produktionstext
                        fieldValue = appendFactoryDataProductionText(fieldValue);
                    }
                    // Setz den finalen Text
                    addFieldValue(colorTableContent, fieldValue, iPartsDataVirtualFieldsDefinition.DCTC_DIALOG_CHANGE);
                }
            }
        }
    }

    /**
     * Überprüft, ob die übergebenen Teil-zu-Farbtabelle Zuordnung DIALOG-Änderungen in ihren Werkseinsatzdaten enthält.
     * Falls ja, wird an die übergebene Zuordnung geschrieben, welche Werkseinsatzdaten sich geändert haben (AS und/oder Produktion).
     *
     * Um diese Überprüfungen machen zu können, müssen die geladenen Änderungsobjekte aus DA_DIALOG_CHANGES vorsortiert
     * übergeben werden, siehe {@link iPartsDIALOGChangeHelper#sortFactoryDataDIALOGChanges(EtkProject project, iPartsDataDIALOGChangeList dialogChangesForBCTEExtern, String partNo)}
     *
     * Optional kann übergeben werden, dass geprüft werden soll, ob die darunterliegenden Varianten ebenfalls Änderungen
     * an ihren Werkseinsatzdaten besitzen
     *
     * @param sortedDialogChangesData vorsortierte Änderungsobjekte
     * @param colorTableToPart        Teil-zu-Farbtabelle Zuordnung
     * @param withVariantsCheck       optionaler Check, ob Varinten Änderungen besitzen
     */
    private static void checkSingleVariantToPartObject(Map<String, Map<iPartsFactoryDataTypes, Set<IdWithType>>> sortedDialogChangesData,
                                                       iPartsDataColorTableToPart colorTableToPart, boolean withVariantsCheck) {
        if ((colorTableToPart != null) && (sortedDialogChangesData != null) && !sortedDialogChangesData.isEmpty()) {
            // Die Überprüfung macht nur Sinn, wenn das Objekt geladen ist
            if (!colorTableToPart.existsInDB()) {
                colorTableToPart.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
            }
            iPartsColorTableToPartId colorTablePartId = colorTableToPart.getAsId();
            // Bestimme die Farbtabellennummer (QFT-Nummer)
            String colorTableId = colorTableToPart.getAsId().getColorTableId();
            // Hole alle Werkseinsatzdatenänderungen zur QFT-Nummer
            Map<iPartsFactoryDataTypes, Set<IdWithType>> typesForId = sortedDialogChangesData.get(colorTableId);
            // Falls keine Vorhanden sind, kann auch keine Änderung existieren
            if (typesForId != null) {
                String fieldValue = "";
                // Hole alle AS-Werkseinsatzdatenänderungen
                Set<IdWithType> idsForType = typesForId.get(iPartsFactoryDataTypes.COLORTABLE_PART_AS);
                if ((idsForType != null) && isValidColorToPartData(colorTablePartId, idsForType)) {
                    // Sind welche vorahnden und passen die Attribute zur Zuordnung, dann setze den AS-Text
                    fieldValue += getFactoryDataASText();
                }
                // Hole alle Produktion-Werkseinsatzdatenänderungen
                idsForType = typesForId.get(iPartsFactoryDataTypes.COLORTABLE_PART);
                if ((idsForType != null) && isValidColorToPartData(colorTablePartId, idsForType)) {
                    // Sind welche vorahnden und passen die Attribute zur Zuordnung, dann setze den Produtkionstext
                    fieldValue = appendFactoryDataProductionText(fieldValue);
                }
                // Optionaler Check, ob die Varianten Werkseinsatzdatenänderungen besitzen
                if (withVariantsCheck) {
                    // Kommen Varianten-spezifische Änderungen in den Daten vor
                    if (typesForId.containsKey(iPartsFactoryDataTypes.COLORTABLE_CONTENT_AS) || typesForId.containsKey(iPartsFactoryDataTypes.COLORTABLE_CONTENT)) {
                        //Falls ja, Text setzen
                        if (StrUtils.isValid(fieldValue)) {
                            fieldValue += "; ";
                        }
                        fieldValue += TranslationHandler.translate("!!Varianten");
                    }
                }
                // Setz den finalen Text
                addFieldValue(colorTableToPart, fieldValue, iPartsDataVirtualFieldsDefinition.DCTP_DIALOG_CHANGE);
            }
        }
    }

    /**
     * Lädt dir zur übergebenen Teilenummer gehörenden Änderungsobjekte und sortiert sie nach Farbtabellennummer (QFT-Nummer)
     * und Werkseinatzdatentyp {@link iPartsFactoryDataTypes}.
     *
     * Jedes sortierte Ändrungsobjekt wird in Form einer zusammengebauten ID zurückgeliefert. Die ID ist vom Typ
     * {@link iPartsColorTableFactoryId} und aus dem Inhalt des Feldes DDC_DO_ID generiert. Hierbei handelt es sich um
     * die Original {@link iPartsColorTableFactoryId}, die beim Anlegen der Änderung erzeugt wurde.
     *
     * @param project
     * @param partNo  Teilenummer zu der Änderungsobjekte aus der DB geladen werden
     * @return
     */
    private static Map<String, Map<iPartsFactoryDataTypes, Set<IdWithType>>> sortFactoryDataDIALOGChanges(EtkProject project, String partNo) {
        return sortFactoryDataDIALOGChanges(project, null, partNo);
    }

    /**
     * Sortiert die übergebenen Änderungsobjekte <code>dialogChangesForBCTEExtern</code> nach Farbtabellennummer (QFT-Nummer)
     * und Werkseinatzdatentyp {@link iPartsFactoryDataTypes}.
     *
     * Jedes sortierte Ändrungsobjekt wird in Form einer zusammengebauten ID zurückgeliefert. Die ID ist vom Typ
     * {@link iPartsColorTableFactoryId} und aus dem Inhalt des Feldes DDC_DO_ID generiert. Hierbei handelt es sich um
     * die Original {@link iPartsColorTableFactoryId}, die beim Anlegen der Änderung erzeugt wurde.
     *
     * @param project
     * @param dialogChangesForBCTEExtern
     * @return
     */
    private static Map<String, Map<iPartsFactoryDataTypes, Set<IdWithType>>> sortFactoryDataDIALOGChanges(EtkProject project, iPartsDataDIALOGChangeList dialogChangesForBCTEExtern) {
        return sortFactoryDataDIALOGChanges(project, dialogChangesForBCTEExtern, null);
    }

    /**
     * Sortiert die übergebenen Änderungsobjekte <code>dialogChangesForBCTEExtern</code> nach Farbtabellennummer (QFT-Nummer)
     * und Werkseinatzdatentyp {@link iPartsFactoryDataTypes}.
     *
     * Jedes sortierte Ändrungsobjekt wird in Form einer zusammengebauten ID zurückgeliefert. Die ID ist vom Typ
     * {@link iPartsColorTableFactoryId} und aus dem Inhalt des Feldes DDC_DO_ID generiert. Hierbei handelt es sich um
     * die Original {@link iPartsColorTableFactoryId}, die beim Anlegen der Änderung erzeugt wurde.
     *
     * Wenn keine Änderungsobjekte übergeben wurde aber eine Teilenummer <code>partNo</code> existiert, dann wird anhand
     * der Teilenummer versucht die zugehörigen Änderungsobjekte aus der DB zu laden.
     *
     * @param project
     * @param dialogChangesForBCTEExtern
     * @param partNo                     Fallback, falls keine Änderungsobjekte übergeben werden
     * @return
     */
    private static Map<String, Map<iPartsFactoryDataTypes, Set<IdWithType>>> sortFactoryDataDIALOGChanges(EtkProject project,
                                                                                                          iPartsDataDIALOGChangeList dialogChangesForBCTEExtern,
                                                                                                          String partNo) {
        Map<String, Map<iPartsFactoryDataTypes, Set<IdWithType>>> result = new HashMap<>();
        iPartsDataDIALOGChangeList dialogChangesForBCTE = dialogChangesForBCTEExtern;
        // Wurden keine Änderungsobjekte übergeben, dann versuche zur übergebenen Teilenummer Änderungsobjekte aus der
        // DB zu laden
        if ((dialogChangesForBCTE == null) && StrUtils.isValid(partNo)) {
            dialogChangesForBCTE = iPartsDataDIALOGChangeList.loadForMatNoAndType(project, partNo, iPartsDataDIALOGChange.ChangeType.COLORTABLE_FACTORY_DATA);
        }
        if ((dialogChangesForBCTE != null) && !dialogChangesForBCTE.isEmpty()) {
            for (iPartsDataDIALOGChange dialogChange : dialogChangesForBCTE) {
                // Für Änderungen an Farb-Werkseinsatzdaten müssen nur Objekte vom Typ "COLORTABLE_FACTORY_DATA" betrachtet werden
                if (iPartsDataDIALOGChange.ChangeType.getChangeType(dialogChange.getAsId().getDoType()) == iPartsDataDIALOGChange.ChangeType.COLORTABLE_FACTORY_DATA) {
                    // Die Objekt-Id ist immer die iPartsColorTableFactoryId. Deshalb kann hier aus dem ID String eine
                    // "echte" iPartsColorTableFactoryId erzeugt werden.
                    IdWithType id = IdWithType.fromDBString(iPartsColorTableFactoryId.TYPE, dialogChange.getAsId().getDoId());
                    if ((id != null) && (id.getIdLength() >= 6)) {
                        String tableId = id.getValue(1);
                        String type = id.getValue(5);
                        // Bestimme den Typ der Farb-Werkseinsatzdaten (WX10, VX10, WX9, VX9)
                        iPartsFactoryDataTypes factoryDataType = iPartsFactoryDataTypes.getTypeByDBValue(type);
                        if (factoryDataType != iPartsFactoryDataTypes.UNKNOWN) {
                            Map<iPartsFactoryDataTypes, Set<IdWithType>> typesForTableId = result.get(tableId);
                            // Baue die Zurodnung QFT-Nummer zu Farb-Werkseinsatzdatentypen auf (WX10, VX10, WX9, VX9)
                            if (typesForTableId == null) {
                                typesForTableId = new HashMap<>();
                                result.put(tableId, typesForTableId);
                            }
                            Set<IdWithType> idsForType = typesForTableId.get(factoryDataType);
                            // Baue die Zurodnung Farb-Werkseinsatzdatentypen auf(WX10, VX10, WX9, VX9) zu Werkseinsatzdaten-IDs auf (iPartsColorTableFactoryId)
                            if (idsForType == null) {
                                idsForType = new HashSet<>();
                                typesForTableId.put(factoryDataType, idsForType);
                            }
                            idsForType.add(id);
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * Fügt dem übergebenen {@link EtkDataObject} den übergebenen Wert hinzu
     *
     * @param dataObject
     * @param fieldValue
     * @param virtualFieldName
     */
    private static void addFieldValue(EtkDataObject dataObject, String fieldValue, String virtualFieldName) {
        if (StrUtils.isValid(fieldValue)) {
            if (dataObject.getAttributes().fieldExists(virtualFieldName)) {
                dataObject.setFieldValue(virtualFieldName, fieldValue, DBActionOrigin.FROM_DB);
            } else {
                dataObject.getAttributes().addField(virtualFieldName, fieldValue, true, DBActionOrigin.FROM_DB);
            }
        }
    }

    /**
     * Überprüft, ob die übergebenen Teil-zu-Farbtabelle Zuordnung DIALOG-Änderungen in ihren Werkseinsatzdaten enthält.
     * Falls ja, wird an die übergebene Zuordnung geschrieben, welche Werkseinsatzdaten sich geändert haben (AS und/oder Produktion).
     *
     * Um diese Überprüfungen machen zu können, müssen zur enthaltenen Teilenummer die Änderungsobjekte aus DA_DIALOG_CHANGES
     * geladen und vorsortiert werden, siehe {@link iPartsDIALOGChangeHelper#sortFactoryDataDIALOGChanges(EtkProject project, iPartsDataDIALOGChangeList dialogChangesForBCTEExtern, String partNo)}
     *
     * @param colorTableToPart Teil-zu-Farbtabelle Zuordnung
     */
    public static void checkSingleVariantToPartFactoryData(EtkProject project, iPartsDataColorTableToPart colorTableToPart) {
        if (colorTableToPart != null) {
            String partNo = colorTableToPart.getPartNumber();
            Map<String, Map<iPartsFactoryDataTypes, Set<IdWithType>>> sortedData = sortFactoryDataDIALOGChanges(project, partNo);
            checkSingleVariantToPartObject(sortedData, colorTableToPart, false);
        }
    }
}
