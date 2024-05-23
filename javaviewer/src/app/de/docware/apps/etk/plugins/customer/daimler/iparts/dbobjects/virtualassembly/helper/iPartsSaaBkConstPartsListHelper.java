/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualassembly.helper;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.db.EtkDbsHelper;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPart;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntryList;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.base.project.mechanic.ids.PartId;
import de.docware.apps.etk.base.project.mechanic.usage.MechanicUsagePosition;
import de.docware.apps.etk.base.search.model.PartsSearchSqlSelect;
import de.docware.apps.etk.base.search.model.WildCardSettings;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.VirtualMaterialType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsBOMConstKitContentId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsBOMConstKitTextId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataBOMConstKitContentList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.search.iPartsSearchVirtualDataset;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.search.iPartsVirtualMaterialSearchDataset;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualassembly.iPartsVirtualAssemblyCTT;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualassembly.iPartsVirtualAssemblyEdsBase;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualassembly.iPartsVirtualAssemblyHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsAssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsVirtualNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.ConstructionValidationDateHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.textkinds.iPartsDialogPartlistTextkind;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.textkinds.iPartsSaaPartlistTextkind;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.ctt.iPartsCTTModel;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.helper.iPartsSaaPartsListPrimaryKey;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.HierarchicalIDWithType;
import de.docware.framework.modules.config.db.EtkDatabaseField;
import de.docware.framework.modules.config.db.EtkDatabaseTable;
import de.docware.framework.modules.config.db.VirtualFieldDefinition;
import de.docware.framework.modules.db.*;
import de.docware.framework.modules.db.etkrecord.EtkRecord;
import de.docware.framework.modules.gui.session.Session;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.CanceledException;
import de.docware.util.StrUtils;
import de.docware.util.collections.weak.WeakKeysMap;
import de.docware.util.sort.SortStringCache;
import de.docware.util.sql.SQLStringConvert;
import de.docware.util.sql.SQLUtils;
import de.docware.util.sql.TableAndFieldName;
import de.docware.util.sql.terms.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Helfer für das Laden von SAA/BK-Stücklisten in der Konstruktion
 */
public class iPartsSaaBkConstPartsListHelper implements iPartsConst {

    public enum SaaBkConstPartListType {
        EDS,
        CTT
    }

    public static final int ET_POS_AVAILABLE_MIN_POS = 999000;
    private static final char ALIAS_FOR_DB_JOIN = 'P';
    protected static final String[] SAA_PARTS_LIST_SORT_FIELDS = new String[]{ FIELD_DCK_PARTPOS, FIELD_DCK_REVFROM };

    private final EtkProject project;
    private final AssemblyId assemblyId;
    private final int maxLevelFromConfig;
    private final SaaBkConstPartListType saaBkConstPartListType;

    public iPartsSaaBkConstPartsListHelper(EtkProject project, AssemblyId assemblyId, int maxLevelFromConfig, SaaBkConstPartListType saaBkConstPartListType) {
        this.project = project;
        this.assemblyId = assemblyId;
        this.maxLevelFromConfig = maxLevelFromConfig;
        this.saaBkConstPartListType = saaBkConstPartListType;
    }

    public EtkProject getEtkProject() {
        return project;
    }

    public DBDataObjectList<EtkDataPartListEntry> loadVirtualSaaPartsList(String topLevelSaa, String saaOrConstKitNo,
                                                                          boolean subAssembliesOnly,
                                                                          ConstructionValidationDateHelper validationHelper) {
        return loadVirtualSaaPartsList(topLevelSaa, saaOrConstKitNo, subAssembliesOnly, "", validationHelper);
    }

    /**
     * Lädt den Inhalt einer SAA oder eines Teilebaukastens mit allen Unterstrukturen zu einem bestimmten Gültigkeitsdatum.
     *
     * @param topLevelSaa
     * @param saaOrConstKitNo   SAA oder Teilenummer von einem Teilebaukasten als obere Sachnummer
     * @param subAssembliesOnly
     * @return
     */
    public DBDataObjectList<EtkDataPartListEntry> loadVirtualSaaPartsList(String topLevelSaa, String saaOrConstKitNo,
                                                                          boolean subAssembliesOnly, String revision,
                                                                          ConstructionValidationDateHelper validationHelper) {
        //Jetzt die Childs an diese Baugruppe anfügen
        DBDataObjectList<EtkDataPartListEntry> result = new EtkDataPartListEntryList();

        int lfdNumber = 0;
        SaaPartsListConstKitData records = getSaaPartsListForRevision(topLevelSaa, saaOrConstKitNo, subAssembliesOnly, revision, validationHelper);
        if (records != null) {
            createAllPartListEntriesSorted(records, result, lfdNumber);
        }
        return result;
    }

    /**
     * Erzeugt die Stücklisten- und Textpositionen für die Saa Konstruktionsstückliste und setzt die Sequenznummer
     * auf Basis der erzeugten Stücklisten- und Textpositionen
     *
     * @param constPartListData
     * @param result
     * @param lfdNumber
     * @return
     */
    public int createAllPartListEntriesSorted(SaaPartsListConstKitData constPartListData,
                                              DBDataObjectList<EtkDataPartListEntry> result,
                                              int lfdNumber) {
        // Temporäre Liste für alle in dieser Methode erzeugten Stücklistenpositionen
        List<EtkDataPartListEntry> tempPartListEntries = new ArrayList<>();
        // Stücklisten- und Textpositionen erzeugen
        createEntriesWithText(constPartListData, tempPartListEntries);
        int tempSeqNumber = lfdNumber;
        // Da Textpositionen erst nachträglich hinzugefügt werden, darf man die Sequenznummer erst jetzt setzen
        for (EtkDataPartListEntry partListEntry : tempPartListEntries) {
            tempSeqNumber++;
            partListEntry.setFieldValue(FIELD_K_SEQNR, EtkDbsHelper.formatLfdNr(tempSeqNumber), DBActionOrigin.FROM_DB);
            result.add(partListEntry, DBActionOrigin.FROM_DB);
        }
        return tempSeqNumber;
    }

    /**
     * Erzeugt die Stücklisten- und Textpositionen für die Saa Konstruktionsstückliste
     *
     * @param constPartListData
     * @param tempPartListEntries
     */
    private void createEntriesWithText(SaaPartsListConstKitData constPartListData,
                                       List<EtkDataPartListEntry> tempPartListEntries) {
        if (constPartListData != null) {
            List<SaaPartsListRowData> partListRowData = constPartListData.getPartsListRowData();
            Map<iPartsBOMConstKitTextId, DBDataObjectAttributes> textForConstKitNumber = constPartListData.getTextForConstructionKitMap();
            String currentLevel = constPartListData.getLevel();
            if (partListRowData != null) {
                // Durchlaufe alle echten Stücklistenpositionen
                for (SaaPartsListRowData partsListRowDataSingle : partListRowData) {
                    // Baukastennummer und Position der echten Stücklistenposition
                    String position = partsListRowDataSingle.getConstKitAttributes().getFieldValue(FIELD_DCK_PARTPOS);
                    // Alle Texte zum Baukasten
                    if (textForConstKitNumber != null) {
                        // Sind Texte zu, Baukasten vorhanden, wird geprüft, ob einer der Texte VOR die echte Position gesetzt werden soll
                        Iterator<Map.Entry<iPartsBOMConstKitTextId, DBDataObjectAttributes>> iterator = textForConstKitNumber.entrySet().iterator();
                        while (iterator.hasNext()) {
                            Map.Entry<iPartsBOMConstKitTextId, DBDataObjectAttributes> entry = iterator.next();
                            String textPosition = entry.getValue().getFieldValue(FIELD_DCP_PARTPOS);
                            // Check, ob der Text vor die echte Position kommt, z.B. echte Position "000200", Text "000199"
                            if (textPosition.compareTo(position) <= 0) {
                                // Textposition erzeugen und löschen. Ein Text kann pro Positionsnummer nur einmal vorkommen
                                tempPartListEntries.add(createSaaPOSTextEntry(entry.getKey(), entry.getValue(), currentLevel, partsListRowDataSingle));
                                iterator.remove();
                            }
                        }
                    }
                    // Jetzt die echte Stücklistenposition erzeugen (aber noch nicht in die Liste mit allen anderen legen)
                    EtkDataPartListEntry saaPartsListEntry = createSaaPartsListEntry(partsListRowDataSingle, currentLevel);
                    tempPartListEntries.add(saaPartsListEntry);
                    // Bemerkungs- und Wahlweise-Texte setzen
                    addRemarkText(saaPartsListEntry, partsListRowDataSingle, currentLevel, constPartListData, tempPartListEntries);
                    addWWText(saaPartsListEntry, partsListRowDataSingle, currentLevel, constPartListData, tempPartListEntries);
                    createEntriesWithText(partsListRowDataSingle.getSubPartListEntries(), tempPartListEntries);
                }

                if ((textForConstKitNumber != null) && !textForConstKitNumber.isEmpty()) {
                    // Sind Texte zu, Baukasten vorhanden, wird geprüft, ob einer der Texte VOR die echte Position gesetzt werden soll
                    Iterator<Map.Entry<iPartsBOMConstKitTextId, DBDataObjectAttributes>> iterator = textForConstKitNumber.entrySet().iterator();
                    while (iterator.hasNext()) {
                        Map.Entry<iPartsBOMConstKitTextId, DBDataObjectAttributes> entry = iterator.next();
                        // Textposition erzeugen und löschen. Ein Text kann pro Positionsnummer nur einmal vorkommen
                        tempPartListEntries.add(createSaaPOSTextEntry(entry.getKey(), entry.getValue(), currentLevel, null));
                        iterator.remove();
                    }
                }
            }
        }
    }

    /**
     * Fügt den Wahlweise Text hinzu, wenn einer vorhanden ist
     *
     * @param saaPartsListEntry
     * @param partsListRowDataSingle
     * @param currentLevel
     * @param constPartListData
     * @param tempPartListEntries
     */
    private void addWWText(EtkDataPartListEntry saaPartsListEntry, SaaPartsListRowData partsListRowDataSingle,
                           String currentLevel, SaaPartsListConstKitData constPartListData,
                           List<EtkDataPartListEntry> tempPartListEntries) {
        String wwFlag = partsListRowDataSingle.getConstKitAttributes().getFieldValue(FIELD_DCK_WWKB);
        if (StrUtils.isValid(wwFlag)) {
            String wwText = constPartListData.getWWTextForWWFlag(wwFlag);
            if (StrUtils.isValid(wwText)) {

                createSaaPartsListAdditionalTextEntry(currentLevel, partsListRowDataSingle, wwText, wwFlag,
                                                      iPartsSaaPartlistTextkind.WW_TEXT, tempPartListEntries);
                // Text ohne Prefix in den Zusatzinformationen anzeigen
                EtkMultiSprache wwTextObject = new EtkMultiSprache(wwText, getEtkProject().getAvailLanguages());
                saaPartsListEntry.getAttributes().getField(iPartsDataVirtualFieldsDefinition.EDS_PARTLIST_TEXT_WW_TEXT).setValueAsMultiLanguage(wwTextObject, DBActionOrigin.FROM_DB);
            }
        }
    }

    /**
     * Fügt den Bemerkungstext hinzu, wenn einer vorhanden ist
     *
     * @param saaPartsListEntry
     * @param partsListRowDataSingle
     * @param currentLevel
     * @param constPartListData
     * @param tempPartListEntries
     */
    private void addRemarkText(EtkDataPartListEntry saaPartsListEntry, SaaPartsListRowData partsListRowDataSingle,
                               String currentLevel, SaaPartsListConstKitData constPartListData,
                               List<EtkDataPartListEntry> tempPartListEntries) {
        String position = partsListRowDataSingle.getConstKitAttributes().getFieldValue(FIELD_DCK_PARTPOS);
        if (!addRemarkForRemarkKey(position, constPartListData, currentLevel, partsListRowDataSingle, tempPartListEntries, saaPartsListEntry)) {
            String remarkNo = partsListRowDataSingle.getConstKitAttributes().getFieldValue(FIELD_DCK_NOTE_ID);
            addRemarkForRemarkKey(remarkNo, constPartListData, currentLevel, partsListRowDataSingle, tempPartListEntries, saaPartsListEntry);
        }
    }

    /**
     * Fügt die Bemerkung zur aktuellen Stücklistenposition hinzu. Zuerst wird geprüft, ob es einen Text zur Bemerkungs-
     * ziffer gibt (MQ Daten). Falls nicht, wird geprüft, ob es Bemerkungen zur Position gibt (TruckBOM.foundation Daten)
     *
     * @param remarkKeyForText
     * @param constPartListData
     * @param currentLevel
     * @param partsListRowDataSingle
     * @param tempPartListEntries
     * @param saaPartsListEntry
     * @return
     */
    private boolean addRemarkForRemarkKey(String remarkKeyForText, SaaPartsListConstKitData constPartListData, String currentLevel,
                                          SaaPartsListRowData partsListRowDataSingle, List<EtkDataPartListEntry> tempPartListEntries,
                                          EtkDataPartListEntry saaPartsListEntry) {
        if (StrUtils.isValid(remarkKeyForText)) {
            EtkMultiSprache remarkText = constPartListData.getRemarkForRemarkNo(remarkKeyForText);
            if ((remarkText != null) && !remarkText.allStringsAreEmpty()) {
                createSaaPartsListAdditionalTextEntry(currentLevel, partsListRowDataSingle, remarkText,
                                                      remarkKeyForText, iPartsSaaPartlistTextkind.REMARK,
                                                      tempPartListEntries);
                // Text ohne Prefix in den Zusatzinformationen anzeigen
                saaPartsListEntry.getAttributes().getField(iPartsDataVirtualFieldsDefinition.EDS_PARTLIST_TEXT_REMARKS).setValueAsMultiLanguage(remarkText, DBActionOrigin.FROM_DB);
                return true;
            }
        }
        return false;
    }

    public SaaPartsListConstKitData getSaaPartsList(String topLevelSaa, String saaOrConstKitNo, boolean subAssembliesOnly,
                                                    ConstructionValidationDateHelper validationHelper) {
        return getSaaPartsListForRevision(topLevelSaa, saaOrConstKitNo, subAssembliesOnly, "", validationHelper);
    }

    private SaaPartsListConstKitData getSaaPartsListForRevision(String topLevelSaa, String saaOrConstKitNo, boolean subAssembliesOnly,
                                                                String revision, ConstructionValidationDateHelper validationHelper) {
        if (subAssembliesOnly) {
            // es sind nur die Unterbaugruppen gesucht, per Definition gibt es unterhalb einer SAA keine Unterbaugruppen mehr
            // -> ist immer leer
            return null;
        } else {
            return computeSaaPartsListOfOneSubPart(saaOrConstKitNo, topLevelSaa, 1, maxLevelFromConfig,
                                                   revision, validationHelper, saaOrConstKitNo);
        }
    }

    /**
     * Alle Teile einer SAA oder einer unteren Sachnummer (A-Sachnummer) ermitteln. Zurückgeliefert werden alle Elemente
     * dieser Ebene + die unterliegenden Ebenen
     *
     * @param constKitNo
     * @param topLevelSaaNo
     * @param level
     * @return
     */
    private SaaPartsListConstKitData computeSaaPartsListOfOneSubPart(String constKitNo, String topLevelSaaNo, int level,
                                                                     int maxLevel, String revision,
                                                                     ConstructionValidationDateHelper validationHelper,
                                                                     String constKitPath) {
        Map<iPartsBOMConstKitContentId, SaaPartsListRowData> idToRowObject = new HashMap<>();
        Map<iPartsBOMConstKitTextId, DBDataObjectAttributes> textsForConstKit = new LinkedHashMap<>();
        // Alle Teilepositionen zur oberen Sachnummer laden
        // Es wird die Lademethode über die EtkDataObjectList verwendet, weil sie zentral gewartet wird und somit
        // zukünftige Änderungen an z.B. den Fallback-Sprachen automatisch berücksichtigen würde. Sie ist zwar etwas
        // langsamer als die händische Abfrage, wobei die Unterschiede minimal sind. Sollte das Laden via EtkDataObjectList
        // in Zukunft langsamer werden, könnten wir hier wieder auf die händische Abfrage umschalten (siehe alten Commit).
        loadDataForConstKitNoWithDataObjectList(constKitNo, topLevelSaaNo, validationHelper, idToRowObject, textsForConstKit);

        List<SaaPartsListRowData> itemsList = new ArrayList<>(idToRowObject.values());
        sortSaaPartsListResults(itemsList);

        // Jetzt die Items durchgehen und die Subitems ermitteln
        for (SaaPartsListRowData item : itemsList) {
            // Die Schlüsselwerte der aktuellen Sub-Teileposition werden hier in den Pfad aufgenommen
            String subSnr = item.getConstKitAttributes().getField(FIELD_DCK_SUB_SNR).getAsString();
            iPartsSaaPartsListPrimaryKey primaryKey = createPrimaryKeyForConstPath(subSnr, item);
            String nextConstKitPath = constKitPath + K_SOURCE_CONTEXT_DELIMITER + primaryKey.createSaaPartsListGUID();
            // Jetzt den Pfad setzen und dann an die nächste Ebene weitergeben
            item.setConstKitPath(nextConstKitPath);
            // Falls keine Sub-Strukturen oder die maximale Strukturstufe erreicht wurde -> nicht tiefer gehen

            // Sicherstellen, dass es in jedem Fall die benötigte Ergebnisspalte gibt.
            if (item.getConstKitAttributes().getField(iPartsDataVirtualFieldsDefinition.EDS_ET_POS_AVAILABLE, false) == null) {
                item.getConstKitAttributes().addField(iPartsDataVirtualFieldsDefinition.EDS_ET_POS_AVAILABLE, SQLStringConvert.booleanToPPString(false), true,
                                                      DBActionOrigin.FROM_DB);
            }

            if (item.hasSubStruct() && (level <= maxLevel)) {
                // Zu dieser unteren Sachnummer die Sub-Items ermitteln
                item.setSubPartListEntries(computeSaaPartsListOfOneSubPart(subSnr, topLevelSaaNo, level + 1,
                                                                           maxLevel, revision, validationHelper,
                                                                           nextConstKitPath));

                // Für die subList prüfen, ob es dort Positionen >= "999000" gibt (= "enthält ET-Positionen")
                for (SaaPartsListRowData subItem : item.getSubPartListEntries().getPartsListRowData()) {
                    String pos = subItem.getConstKitAttributes().getFieldValue(FIELD_DCK_PARTPOS);
                    if (StrUtils.isValid(pos)) {
                        int iPos = StrUtils.strToIntDef(pos, -1);

                        // Beim ersten Finden einer passenden Position am [subItem] das Flag am [item] setzen
                        if (iPos >= ET_POS_AVAILABLE_MIN_POS) {
                            item.getConstKitAttributes().addField(iPartsDataVirtualFieldsDefinition.EDS_ET_POS_AVAILABLE, SQLStringConvert.booleanToPPString(true),
                                                                  true, DBActionOrigin.FROM_DB);
                            break;
                        }
                    }
                }
            }
        }
        SaaPartsListConstKitData constKitData = new SaaPartsListConstKitData(itemsList, textsForConstKit, Integer.toString(level));
        boolean isConstKit = !itemsList.isEmpty();
        // Handelt es sich um einen Baukasten, dann Texte laden. Außer bei CTT. Hier gibt es keine Texte!
        if (isConstKit && (saaBkConstPartListType != SaaBkConstPartListType.CTT)) {
            loadAdditionalTextsForConstKit(constKitNo, revision, topLevelSaaNo.equals(constKitNo), validationHelper, constKitData);
        }
        return constKitData;
    }

    /**
     * Lädt alle Einträge aus EDS_CONST_KIT zur übergebenen <code>constKitNo</code> (obere Sachnummer) mit Join auf die
     * <code>MAT</code> Tabelle, Join auf die EDS_CONST_PROSP Tabelle und Join auf sich selber um zu bestimmen, ob es
     * weitere Teile in den Unterstrukturen gibt.
     * <p>
     * Zum Laden der Daten wird der <code>searchSortAndFillWithJoin()</code> Mechanismus aus {@link EtkDataObjectList )}
     * verwendet.
     *
     * @param constKitNo
     * @param topLevelSaaNo
     * @param idToRowObject
     * @param textsForConstKit
     */
    private void loadDataForConstKitNoWithDataObjectList(String constKitNo, String topLevelSaaNo,
                                                         ConstructionValidationDateHelper validationHelper, Map<iPartsBOMConstKitContentId, SaaPartsListRowData> idToRowObject,
                                                         Map<iPartsBOMConstKitTextId, DBDataObjectAttributes> textsForConstKit) {
        EtkDisplayFields constKitFields = getEtkProject().getAllDisplayFieldsForTable(TABLE_DA_EDS_CONST_KIT);
        EtkDisplayFields constPropsFields = getEtkProject().getAllDisplayFieldsForTable(TABLE_DA_EDS_CONST_PROPS);
        EtkDisplayFields fields = new EtkDisplayFields(constKitFields);
        fields.addFelder(constPropsFields);

        EtkDisplayFields matFields = getNeededMatFieldsForSaaPartsList(project, assemblyId);
        // Benötigte Felder für den Materialstamm hinzufügen
        fields.addFelder(matFields);

        EtkDataObjectList.FoundAttributesCallback callback = new EtkDataObjectList.FoundAttributesCallback() {
            @Override
            public boolean foundAttributes(DBDataObjectAttributes attributes) {
                // Bestimmen der anzuzeigenden Materialnummer oder Abbruch, wenn es sich nicht um Material handelt.
                if (!iPartsVirtualAssemblyHelper.checkMatNoSaaPartsList(attributes)) {
                    return false;
                }
                // Erstmal die berechneten Felder in die Row
                SaaPartsListRowData objectWithRowData = new SaaPartsListRowData();
                // Die SAA steht auch nicht in den Unterstrukturen, wir bekommen Sie von der Hauptebene, deshalb hier eintragen
                objectWithRowData.setSaaNo(topLevelSaaNo);
                // Die Felder aus der Tabelle [DA_EDS_CONST_KIT]
                iPartsVirtualAssemblyHelper.addAttributesFromDB(constKitFields, attributes, objectWithRowData.getConstKitAttributes());
                // ID des Datensatzes kann nun ermittelt werden
                iPartsBOMConstKitContentId constKitContentId = new iPartsBOMConstKitContentId(
                        objectWithRowData.getConstKitAttributes().getField(FIELD_DCK_SNR).getAsString(),
                        objectWithRowData.getConstKitAttributes().getField(FIELD_DCK_PARTPOS).getAsString(),
                        objectWithRowData.getConstKitAttributes().getField(FIELD_DCK_REVFROM).getAsString());

                // Check, ob für die iPartsBOMConstKitContentId schon ein Objekt existiert. Falls ja, und es gibt einen
                // Text, dann muss der neue Text dem bestehenden Objekt hinzugefügt werden.
                SaaPartsListRowData existingRowObject = idToRowObject.get(constKitContentId);
                if (existingRowObject != null) {
                    objectWithRowData = existingRowObject;
                }

                DBDataObjectAttributes propsAttributes = new DBDataObjectAttributes();
                boolean hasTextData = false;
                // Die Felder aus der Tabelle [DA_EDS_CONST_PROPS] auslesen
                for (EtkDisplayField field : constPropsFields.getFields()) {
                    iPartsVirtualAssemblyHelper.addAttributeFromDB(field, attributes, propsAttributes);
                    String value = propsAttributes.getFieldValue(field.getKey().getFieldName());
                    hasTextData = hasTextData || !value.isEmpty();
                }
                if (hasTextData) {
                    // Wenn ein Text gefunden wurde, dann muss geprüft werden, ob der Text zum übergebenen Datum (validationHelper)
                    // gültig ist.
                    // Bei den Datensätzen zum höchsten Änderungsstand (z.B. in der Konstruktionsstückliste) ist das Datum
                    // immer der aktuelle Zeitpunkt. Bei der Stücklistenanzeige für andere Änderungsstände, wird
                    // das Freigabedatum zum jeweiligen Baukasten verwendet (SAA-> DA_SAA_HISTORY, Teilebaukasten -> DA_BOM_MAT_HISTORY)
                    String releaseFrom = propsAttributes.getField(FIELD_DCP_RELEASE_FROM).getAsString();
                    String releaseTo = propsAttributes.getField(FIELD_DCP_RELEASE_TO).getAsString();
                    if (validationHelper.releaseDateCheck(releaseFrom, releaseTo)) {
                        iPartsBOMConstKitTextId constKitTextId = new iPartsBOMConstKitTextId(propsAttributes.getField(FIELD_DCP_SNR).getAsString(),
                                                                                             propsAttributes.getField(FIELD_DCP_PARTPOS).getAsString(),
                                                                                             propsAttributes.getField(FIELD_DCP_BTX_FLAG).getAsString(),
                                                                                             propsAttributes.getField(FIELD_DCP_REVFROM).getAsString());

                        textsForConstKit.putIfAbsent(constKitTextId, propsAttributes);
                    }
                }

                // Wegen dem Join auf die Unterstrukturen können hier mehrere kommen -> teste, ob diese Id schon da war
                if (existingRowObject != null) {
                    return false;
                }

                // Die Felder aus der Tabelle [MAT] auslesen
                iPartsVirtualAssemblyHelper.addAttributesFromDB(matFields, attributes, objectWithRowData.getMatAttributes());

                // Das letzte Feld ist gefüllt, falls diese Sachnummer noch eine Unterstruktur hat.
                objectWithRowData.setHasSubStruct(!attributes.getFieldValue(ALIAS_FOR_DB_JOIN + "_" + FIELD_DCK_SNR).isEmpty());

                String releaseFrom = objectWithRowData.getConstKitAttributes().getField(FIELD_DCK_RELEASE_FROM).getAsString();
                String releaseTo = objectWithRowData.getConstKitAttributes().getField(FIELD_DCK_RELEASE_TO).getAsString();
                boolean dateValid = validationHelper.releaseDateCheck(releaseFrom, releaseTo);

                if (dateValid) {
                    idToRowObject.put(constKitContentId, objectWithRowData);
                }
                return false;
            }
        };

        iPartsDataBOMConstKitContentList list = new iPartsDataBOMConstKitContentList();
        list.searchSortAndFillWithJoin(getEtkProject(), getEtkProject().getDBLanguage(), fields,
                                       new String[]{ TableAndFieldName.make(TABLE_DA_EDS_CONST_KIT, FIELD_DCK_SNR) },
                                       new String[]{ constKitNo },
                                       false, null, // Sortierung funktioniert mit einem reinen Callback nicht
                                       false, callback,
                                       new EtkDataObjectList.JoinData(TABLE_MAT,
                                                                      new String[]{ FIELD_DCK_SUB_SNR },
                                                                      new String[]{ FIELD_M_MATNR }, true, false),
                                       new EtkDataObjectList.JoinData(TABLE_DA_EDS_CONST_PROPS,
                                                                      new String[]{ FIELD_DCK_SNR },
                                                                      new String[]{ FIELD_DCP_SNR }, true, false),
                                       new EtkDataObjectList.JoinData(TABLE_DA_EDS_CONST_KIT,
                                                                      new String[]{ FIELD_DCK_SUB_SNR },
                                                                      new String[]{ FIELD_DCK_SNR }, true, false, ALIAS_FOR_DB_JOIN));
    }

    private iPartsSaaPartsListPrimaryKey createPrimaryKeyForConstPath(String subSnr, SaaPartsListRowData item) {
        String pos = item.getConstKitAttributes().getField(FIELD_DCK_PARTPOS).getAsString();
        String revFrom = item.getConstKitAttributes().getField(FIELD_DCK_REVFROM).getAsString();
        String kemFrom = item.getConstKitAttributes().getField(FIELD_DCK_KEMFROM).getAsString();
        return new iPartsSaaPartsListPrimaryKey(subSnr, pos, revFrom, kemFrom);
    }

    /**
     * Lädt alle Bemerkungs- und Wahlweisetexte zum Baukasten
     *
     * @param constKitNo
     * @param revision
     * @param isSaa
     * @param constKitData
     */
    private void loadAdditionalTextsForConstKit(String constKitNo, String revision, boolean isSaa,
                                                ConstructionValidationDateHelper validationHelper,
                                                SaaPartsListConstKitData constKitData) {
        // Der gewünschte Änderungstand. Die Daten sollen zu diesem Stand geladen werden
        String currentRevision = StrUtils.removeLeadingCharsFromString(revision, '0');
        // Ist der gewünschte Stand leer, muss via DB der höchste Stand ermittelt werden
        if (StrUtils.isEmpty(currentRevision)) {
            // Für SAA Baukasten via Tabelle DA_SAA_HISTORY
            if (isSaa) {
                currentRevision = getHighestRevisionForConstKit(constKitNo, validationHelper, TABLE_DA_SAA_HISTORY,
                                                                FIELD_DSH_SAA, FIELD_DSH_RELEASE_FROM,
                                                                FIELD_DSH_RELEASE_TO, FIELD_DSH_REV_FROM);
            } else {
                currentRevision = getHighestRevisionForConstKit(constKitNo, validationHelper, TABLE_DA_BOM_MAT_HISTORY,
                                                                FIELD_DBMH_PART_NO, FIELD_DBMH_RELEASE_FROM,
                                                                FIELD_DBMH_RELEASE_TO, FIELD_DBMH_REV_FROM);
            }
        }
        // Ist der Änderungsstand immer noch leer, können keine Texte geladen werden
        if (StrUtils.isValid(currentRevision)) {
            Map<String, EtkMultiSprache> remarks;
            Map<String, String> wwTexts;
            int currentRevisionInt = StrUtils.strToIntDef(currentRevision, 0);
            // Laden der Texte aus den jeweiligen Tabellen
            if (isSaa) {
                remarks = loadRemarkTexts(constKitNo, currentRevisionInt, TABLE_DA_EDS_SAA_REMARKS,
                                          FIELD_DESR_SAA, FIELD_DESR_REV_FROM, FIELD_DESR_REMARK_NO,
                                          FIELD_DESR_TEXT);
                wwTexts = loadTexts(constKitNo, currentRevisionInt, TABLE_DA_EDS_SAA_WW_FLAGS,
                                    FIELD_DESW_SAA, FIELD_DESW_REV_FROM, FIELD_DESW_FLAG,
                                    FIELD_DESW_TEXT);
            } else {
                remarks = loadRemarkTexts(constKitNo, currentRevisionInt, TABLE_DA_EDS_MAT_REMARKS,
                                          FIELD_DEMR_PART_NO, FIELD_DEMR_REV_FROM, FIELD_DEMR_REMARK_NO,
                                          FIELD_DEMR_TEXT);
                wwTexts = loadTexts(constKitNo, currentRevisionInt, TABLE_DA_EDS_MAT_WW_FLAGS,
                                    FIELD_DEMW_PART_NO, FIELD_DEMW_REV_FROM, FIELD_DEMW_FLAG,
                                    FIELD_DEMW_TEXT);
            }
            constKitData.setRemarks(remarks);
            constKitData.setWWTexts(wwTexts);
        }
    }

    /**
     * Fügt der übergebenen Liste {@code selectFields} alle Felder der übergebenen Tabelle hinzu mit Ausnahme der Blob-Felder.
     *
     * @param databaseTable
     * @param selectFields
     */
    protected void addTableFieldsToSelectFields(EtkDatabaseTable databaseTable, Collection<String> selectFields) {
        for (String fieldname : databaseTable.getAllFieldsNoBlob()) {
            selectFields.add(TableAndFieldName.make(databaseTable.getName(), fieldname).toLowerCase());
        }
    }

    private Map<String, String> loadTexts(String constKitNo, int currentRevision, String tableName,
                                          String constKitField, String revFromField, String textIdField, String textField) {
        Map<String, String> result = new HashMap<>();
        Map<String, Integer> textDataIdToRevision = new HashMap<>();
        List<String> selectFields = new ArrayList<>();
        addTableFieldsToSelectFields(getEtkProject().getConfig().getDBDescription().findTable(tableName), selectFields);
        DBSQLQuery query = createTextQuery(tableName, selectFields, constKitField, constKitNo);
        try (DBDataSet dbSet = query.executeQuery()) {
            while (dbSet.next()) {
                EtkRecord rec = dbSet.getRecord(selectFields);
                int revision = StrUtils.strToIntDef(rec.getField(TableAndFieldName.make(tableName, revFromField)).getAsString(), -1);
                // Der Änderungsstand der Texte darf nicht größer als der Änderungsstand des Baukastens sein
                if ((revision == -1) || (revision > currentRevision)) {
                    continue;
                }
                // Textkenner des aktuellen Datensatzes (WW Flag)
                String textDataIdFromDB = rec.getField(TableAndFieldName.make(tableName, textIdField)).getAsString();
                Integer revisionForTextDataId = textDataIdToRevision.get(textDataIdFromDB);
                if ((revisionForTextDataId != null) && (revisionForTextDataId > revision)) {
                    // Revision des DB Textes ist kleiner als die, die wir schon ausgelesen haben -> nächsten DB Datensatz betrachten
                    continue;
                }
                // Text für Kenner ablegen
                String text = rec.getField(TableAndFieldName.make(tableName, textField)).getAsString();
                result.put(textDataIdFromDB, text);
                textDataIdToRevision.put(textDataIdFromDB, revision);
            }
        }
        return result;
    }

    /**
     * Lädt die Bemerkungen zum Baukasten oder zur SAA. Gültig sind nur Texte, deren Änderungsstand kleiner/gleich dem
     * Änderungsstand der Stücklistenposition ist. Um die mehrsprachigen Texte zu bestimmen, wird auf die SPRACHE
     * Tabelle gejoint.
     *
     * @param constKitNo
     * @param currentRevision
     * @param tableName
     * @param constKitField
     * @param revFromField
     * @param remarkIdField
     * @param textField
     * @return
     */
    private Map<String, EtkMultiSprache> loadRemarkTexts(String constKitNo, int currentRevision, String tableName,
                                                         String constKitField, String revFromField, String remarkIdField,
                                                         String textField) {
        Map<String, EtkMultiSprache> result = new HashMap<>();
        Map<String, Integer> remarkIdToRevision = new HashMap<>();
        List<String> selectFields = new ArrayList<>();
        addTableFieldsToSelectFields(getEtkProject().getConfig().getDBDescription().findTable(tableName), selectFields);
        selectFields.add(TableAndFieldName.make(TABLE_SPRACHE, FIELD_S_BENENN));
        selectFields.add(TableAndFieldName.make(TABLE_SPRACHE, FIELD_S_BENENN_LANG));
        selectFields.add(TableAndFieldName.make(TABLE_SPRACHE, FIELD_S_SPRACH));
        DBSQLQuery query = createTextQuery(tableName, selectFields, constKitField, constKitNo);
        // Falls noch keine Konvertierung stattfand, zur Sicherheit alle Zeilen mit leerer Text-Id ausschließen
        Condition textNotEmptyCondition = new Condition(textField.toLowerCase(), Condition.OPERATOR_NOT_EQUALS, "");
        query.and(textNotEmptyCondition);
        // Inner Join auf SPRACHE
        Condition langCondition = new Condition(TableAndFieldName.make(tableName, textField), Condition.OPERATOR_EQUALS, new Fields(TableAndFieldName.make(TABLE_SPRACHE, FIELD_S_TEXTID)));
        query.join(new InnerJoin(TABLE_SPRACHE, langCondition));
        try (DBDataSet dbSet = query.executeQuery()) {
            while (dbSet.next()) {
                EtkRecord rec = dbSet.getRecord(selectFields);
                int revision = StrUtils.strToIntDef(rec.getField(TableAndFieldName.make(tableName, revFromField)).getAsString(), -1);
                // Der Änderungsstand der Texte darf nicht größer als der Änderungsstand des Baukastens sein
                if ((revision == -1) || (revision > currentRevision)) {
                    continue;
                }
                // Textkenner des aktuellen Datensatzes (Bemerkungskennziffer oder Position)
                String remarkIdFromDB = rec.getField(TableAndFieldName.make(tableName, remarkIdField)).getAsString();
                Integer revisionForRemarkId = remarkIdToRevision.get(remarkIdFromDB);
                if ((revisionForRemarkId == null) || (revisionForRemarkId < revision)) {
                    // Revision der DB Bemerkung existiert noch nicht oder ist größer als die, die wir schon ausgelesen haben -> zwischengespeicherte Bemerkung entfernen
                    result.remove(remarkIdFromDB);
                } else if (revisionForRemarkId > revision) {
                    // Revision der DB Bemerkung ist kleiner als die, die wir schon ausgelesen haben -> nächsten DB Datensatz betrachten
                    continue;
                }
                EtkMultiSprache text = result.get(remarkIdFromDB);
                if (text == null) {
                    text = new EtkMultiSprache();
                    result.put(remarkIdFromDB, text);
                    remarkIdToRevision.put(remarkIdFromDB, revision);
                }

                // Text für Kenner ablegen
                String textShort = rec.getField(TableAndFieldName.make(TABLE_SPRACHE, FIELD_S_BENENN)).getAsString();
                String textLong = rec.getField(TableAndFieldName.make(TABLE_SPRACHE, FIELD_S_BENENN_LANG)).getAsString();
                String language = rec.getField(TableAndFieldName.make(TABLE_SPRACHE, FIELD_S_SPRACH)).getAsString();
                text.setText(language, StrUtils.isValid(textLong) ? textLong : textShort);
                result.put(remarkIdFromDB, text);
            }
        }
        return result;
    }

    private DBSQLQuery createTextQuery(String tableName, List<String> selectFields, String constKitField, String constKitNo) {
        DBSQLQuery query = getEtkProject().getDB().getDBForTable(tableName).getNewQuery();
        query.select(new Fields(selectFields)).from(tableName);
        // Alle Texte zum Baukasten laden
        query.where(new Condition(constKitField.toLowerCase(), Condition.OPERATOR_EQUALS, constKitNo));
        return query;
    }

    /**
     * Liefert den höchsten Änderungsstand für einen Baukasten
     *
     * @param constKitNo
     * @param tablename
     * @param constKitField
     * @param releaseFromField
     * @param releaseToField
     * @param revFromField
     * @return
     */
    private String getHighestRevisionForConstKit(String constKitNo, ConstructionValidationDateHelper validationHelper, String tablename,
                                                 String constKitField, String releaseFromField, String releaseToField,
                                                 String revFromField) {
        String compareStringForReleaseFrom = validationHelper.getValidationDateStringForFrom();
        String compareStringForReleaseTo = validationHelper.getValidationDateStringForTo();
        List<String> selectFields = new ArrayList<>();
        addTableFieldsToSelectFields(getEtkProject().getConfig().getDBDescription().findTable(tablename), selectFields);

        DBSQLQuery query = getEtkProject().getDB().getDBForTable(tablename).getNewQuery();
        query.select(new Fields(selectFields)).from(tablename);
        // 1. Bedingung: Freigabetermin Ab der oberen Sachnummer des Baukastens in T43RSAAE/T43RTEIL (constKitNo) <= Freigabetermin,
        // zu dem in T43RBK der Baukasteninhalt zur SAA / zum Teil gelesen wurde (höchster Stand = aktuelles Datum, ansonsten Datum zum Änderungsstand)
        query.where(new Condition(constKitField.toLowerCase(), Condition.OPERATOR_EQUALS, constKitNo))
                .and(new Condition(releaseFromField.toLowerCase(), "<=", compareStringForReleaseFrom));
        List<Condition> orConditions = new ArrayList<>();
        // 2. Bedingung: Freigabetermin, zu dem in T43RBK der Baukasteninhalt zur SAA / zum Teil gelesen wurde < Freigabetermin Bis der oberen Sachnummer des Baukastens in T43RSAAE/T43RTEIL
        orConditions.add(new Condition(releaseToField.toLowerCase(), Condition.OPERATOR_EQUALS, ""));
        orConditions.add(new Condition(releaseToField.toLowerCase(), ">", compareStringForReleaseTo));
        query.and(new ConditionList(orConditions, true));
        // Absteigend Sortieren nach "Änderungsstand ab" um den höchsten Stand ganz oben zu haben
        query.orderByDescending(new String[]{ revFromField.toLowerCase() });
        try (DBDataSet dbSet = query.executeQuery()) {
            if (dbSet.next()) {
                // Den ersten Treffer zurückgeben, da wir hier schon nach Änderungsstand sortiert haben
                EtkRecord rec = dbSet.getRecord(selectFields);
                return rec.getField(TableAndFieldName.make(tablename, revFromField)).getAsString();
            }
        }

        return null;
    }


    /**
     * Erzeugt eine Stücklistenposition für die Saa Konstruktionsstückliste
     *
     * @param saaPartsListRowData
     * @param currentLevel
     * @return
     */
    private EtkDataPartListEntry createSaaPartsListEntry(SaaPartsListRowData saaPartsListRowData, String currentLevel) {

        // Attribute erzeugen und alle Felder mit Leer auffüllen
        DBDataObjectAttributes katAttributes = iPartsVirtualAssemblyHelper.createEmptyAttributesForTable(getEtkProject(), TABLE_KATALOG);

        String matNo = saaPartsListRowData.getConstKitAttributes().getField(FIELD_DCK_SUB_SNR).getAsString();

        katAttributes.addField(FIELD_K_VARI, assemblyId.getKVari(), DBActionOrigin.FROM_DB);
        katAttributes.addField(FIELD_K_VER, assemblyId.getKVer(), DBActionOrigin.FROM_DB);
        katAttributes.addField(FIELD_K_SACH, "", DBActionOrigin.FROM_DB);
        katAttributes.addField(FIELD_K_SVER, "", DBActionOrigin.FROM_DB);
        katAttributes.addField(FIELD_K_MATNR, matNo, DBActionOrigin.FROM_DB);
        katAttributes.addField(FIELD_K_MVER, "", DBActionOrigin.FROM_DB);
        String guid = iPartsSaaPartsListPrimaryKey.buildSaaPartsListGUIDForPartListSeqNo(saaPartsListRowData.getConstKitAttributes(), saaPartsListRowData.getConstKitPath());
        katAttributes.addField(FIELD_K_LFDNR, guid, DBActionOrigin.FROM_DB);

        // virtuelle Felder setzen
        for (VirtualFieldDefinition mapping : iPartsDataVirtualFieldsDefinition.getMapping(TABLE_DA_EDS_CONST_KIT, TABLE_KATALOG)) {
            String fieldName = mapping.getVirtualFieldName();
            String value;

            if (fieldName.equals(iPartsDataVirtualFieldsDefinition.EDS_LEVEL)) {
                value = currentLevel;

            } else if (fieldName.equals(iPartsDataVirtualFieldsDefinition.EDS_SAAKEY)) {
                value = saaPartsListRowData.getSaaNo();
                if (value != null) {
                    // Bei einer CTT Stückliste soll die gemappte Saa zur echten HMO Nummer angezeigt werden. Da das BM
                    // ja schon geladen ist, wird die gemappte Saa Nummer darüber bestimmt
                    if (saaBkConstPartListType == SaaBkConstPartListType.CTT) {
                        String model = iPartsVirtualNode.getModelNumberFromAssemblyId(assemblyId);
                        if (StrUtils.isValid(model)) {
                            iPartsCTTModel cttModel = iPartsCTTModel.getInstance(getEtkProject(), new iPartsModelId(model));
                            String saaForHMO = cttModel.getSaaForHmo(value);
                            if (StrUtils.isValid(saaForHMO)) {
                                value = saaForHMO;
                            }
                        }
                    }
                    value = getEtkProject().getVisObject().asString(TABLE_DA_SAA, FIELD_DS_SAA, value, getEtkProject().getDBLanguage());
                }

            } else if (fieldName.equals(iPartsDataVirtualFieldsDefinition.EDS_RETAIL_USE)) {
                // virtuelles Feld EDS_RETAIL_USE nur hinzufügen wenn benötigt (dann ist retailEdsSaaGUIDs != null)
                value = RETAIL_NOT_ASSIGNED;
            } else {
                value = saaPartsListRowData.getConstKitAttributes().getField(mapping.getSourceFieldName()).getAsString();
            }
            if (value != null) { // Feld hat nur einen Wert, wenn es benötigt wird
                katAttributes.addField(fieldName, value, true, DBActionOrigin.FROM_DB);
            }
        }

        // Berechnetes, virtuelles Feld ohne Mapping übernehmen
        DBDataObjectAttribute etPosAvailableField = saaPartsListRowData.getConstKitAttributes().getField(iPartsDataVirtualFieldsDefinition.EDS_ET_POS_AVAILABLE, false);
        boolean etPosAvailable = (etPosAvailableField != null) && etPosAvailableField.getAsBoolean();
        katAttributes.addField(iPartsDataVirtualFieldsDefinition.EDS_ET_POS_AVAILABLE, SQLStringConvert.booleanToPPString(etPosAvailable), true, DBActionOrigin.FROM_DB);

        addAdditionalVirtualFields(katAttributes);

        EtkDataPartListEntry newPartListEntry = EtkDataObjectFactory.createDataPartListEntry(getEtkProject(), katAttributes);

        EtkDataPart partForPartListEntry = newPartListEntry.getPart();
        partForPartListEntry.setAttributes(saaPartsListRowData.getMatAttributes(), DBActionOrigin.FROM_DB);
        iPartsVirtualAssemblyHelper.addFilterAttributesForTable(getEtkProject(), TABLE_MAT, partForPartListEntry.getAttributes());

        if (newPartListEntry instanceof iPartsDataPartListEntry) {
            ((iPartsDataPartListEntry)newPartListEntry).setDataLoadedFlagsForVirtualPLE(true);
        }
        return newPartListEntry;
    }

    /**
     * Erzeugt Stücklistentexte, die sich auf spezielle Kenner an einer echten Teileposition beziehen (Wahlweise Flag)
     *
     * @param level
     * @param partsListRowDataSingle
     * @param text
     * @param textSign
     * @param textkind
     * @param tempPartListEntries
     * @return
     */
    private void createSaaPartsListAdditionalTextEntry(String level, SaaPartsListRowData partsListRowDataSingle,
                                                       String text, String textSign, iPartsSaaPartlistTextkind textkind,
                                                       List<EtkDataPartListEntry> tempPartListEntries) {
        if (StrUtils.isEmpty(text)) {
            return;
        }
        EtkMultiSprache multiText = new EtkMultiSprache(getTextWithTextkind(textkind, text), getEtkProject().getAvailLanguages());
        tempPartListEntries.add(createPartsListEntryForText(partsListRowDataSingle, textSign, level, "", textkind, multiText));
    }

    /**
     * Erzeugt mehrsprachige Stücklistentexte, die sich auf die Position an einer echten Teileposition beziehen
     *
     * @param level
     * @param partsListRowDataSingle
     * @param text
     * @param textSign
     * @param textkind
     * @param tempPartListEntries
     * @return
     */
    private void createSaaPartsListAdditionalTextEntry(String level, SaaPartsListRowData partsListRowDataSingle,
                                                       EtkMultiSprache text, String textSign, iPartsSaaPartlistTextkind textkind,
                                                       List<EtkDataPartListEntry> tempPartListEntries) {
        if ((text == null) || text.allStringsAreEmpty()) {
            return;
        }
        EtkMultiSprache multiTextWithTextKind = new EtkMultiSprache();
        for (Map.Entry<String, String> entry : text.getLanguagesAndTexts().entrySet()) {
            String lang = entry.getKey();
            String textForLang = entry.getValue();
            multiTextWithTextKind.setText(lang, getTextWithTextkind(textkind, textForLang));

        }
        tempPartListEntries.add(createPartsListEntryForText(partsListRowDataSingle, textSign, level, "", textkind, multiTextWithTextKind));
    }

    private void sortSaaPartsListResults(List<SaaPartsListRowData> result) {
        final SortStringCache cache = new SortStringCache();
        result.sort((o1, o2) -> {
            for (String fieldName : SAA_PARTS_LIST_SORT_FIELDS) {
                String s1 = o1.getConstKitAttributes().getField(fieldName).getAsString();
                String s2 = o2.getConstKitAttributes().getField(fieldName).getAsString();
                s1 = cache.getSortString(s1, true);
                s2 = cache.getSortString(s2, true);
                int result1 = s1.compareTo(s2);
                if (result1 != 0) {
                    return result1;
                }
            }
            return 0;
        });
    }

    /**
     * Erzeugt eine Textposition für die Saa Konstruktionsstückliste samt eigener Positionsangabe.
     *
     * @param constKitTextId
     * @param textData
     * @param saaPartsListRowData
     * @return
     */
    private EtkDataPartListEntry createSaaPOSTextEntry(iPartsBOMConstKitTextId constKitTextId, DBDataObjectAttributes textData,
                                                       String level, SaaPartsListRowData saaPartsListRowData) {
        // Attribute erzeugen und alle Felder mit Leer auffüllen
        String guidFromTextentry = constKitTextId.toString("|");
        EtkMultiSprache text = textData.getField(FIELD_DCP_TEXT).getAsMultiLanguageInternal();
        text.setLanguagesAndTexts(text.getLanguagesAndTextsModifiable().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, value -> getTextWithTextkind(iPartsSaaPartlistTextkind.POS_TEXTS, value.getValue()))));
        return createPartsListEntryForText(saaPartsListRowData, guidFromTextentry, level, textData.getFieldValue(FIELD_DCP_PARTPOS),
                                           iPartsSaaPartlistTextkind.POS_TEXTS, text);
    }

    /**
     * Erzeugt und befüllt ein {@link EtkDataPartListEntry} Objekt für einen Saa Stücklistentext
     *
     * @param partsListRowDataSingle
     * @param textId
     * @param level
     * @param positionValue
     * @param textkind
     * @param text
     * @return
     */
    private EtkDataPartListEntry createPartsListEntryForText(SaaPartsListRowData partsListRowDataSingle, String textId,
                                                             String level, String positionValue,
                                                             iPartsSaaPartlistTextkind textkind, EtkMultiSprache text) {
        // KATALOG Attribute erzeugen und füllen
        DBDataObjectAttributes katAttributes = createKatAttributes(partsListRowDataSingle, textId, level, positionValue, textkind);
        // Das eigentliche Objekt aus den attributen erzeugen
        EtkDataPartListEntry newPartListEntry = EtkDataObjectFactory.createDataPartListEntry(getEtkProject(), katAttributes);
        // Die Textart setzen
        newPartListEntry.setFieldValue(iPartsDataVirtualFieldsDefinition.EDS_PARTLIST_TEXTKIND, textkind.getTxtKindShort(), DBActionOrigin.FROM_DB);

        // Stücklistentexte enthalten nie ET-Positionen
        newPartListEntry.getAttributes().addField(iPartsDataVirtualFieldsDefinition.EDS_ET_POS_AVAILABLE, SQLStringConvert.booleanToPPString(false), true,
                                                  DBActionOrigin.FROM_DB);

        // Den eigentlichen Text setzen
        EtkDataPart partForPartListEntry = newPartListEntry.getPart();
        DBDataObjectAttributes matAttributes = iPartsVirtualAssemblyHelper.createEmptyAttributesForTable(getEtkProject(), TABLE_MAT);
        partForPartListEntry.setAttributes(matAttributes, DBActionOrigin.FROM_DB);
        partForPartListEntry.setFieldValueAsMultiLanguage(FIELD_M_CONST_DESC, text, DBActionOrigin.FROM_DB);

        if (newPartListEntry instanceof iPartsDataPartListEntry) {
            ((iPartsDataPartListEntry)newPartListEntry).setDataLoadedFlagsForVirtualPLE(true);
        }
        return newPartListEntry;
    }

    private String getTextWithTextkind(iPartsSaaPartlistTextkind textkind, String text) {
        return textkind.getTxtKindShort() + ": " + text;
    }

    /**
     * Erzeugt und befüllt die KATALOG Attribute samt allen virtuellen Felder
     *
     * @param saaPartsListRowData
     * @param guidFromTextentry
     * @param level
     * @param positionValue
     * @param textkind
     * @return
     */
    private DBDataObjectAttributes createKatAttributes(SaaPartsListRowData saaPartsListRowData, String guidFromTextentry,
                                                       String level, String positionValue, iPartsSaaPartlistTextkind textkind) {
        // Attribute erzeugen und alle Felder mit Leer auffüllen
        DBDataObjectAttributes katAttributes = iPartsVirtualAssemblyHelper.createEmptyAttributesForTable(getEtkProject(), TABLE_KATALOG);

        katAttributes.addField(FIELD_K_VARI, assemblyId.getKVari(), DBActionOrigin.FROM_DB);
        katAttributes.addField(FIELD_K_VER, assemblyId.getKVer(), DBActionOrigin.FROM_DB);
        katAttributes.addField(FIELD_K_SACH, "", DBActionOrigin.FROM_DB);
        katAttributes.addField(FIELD_K_SVER, "", DBActionOrigin.FROM_DB);
        katAttributes.addField(FIELD_K_MATNR, "", DBActionOrigin.FROM_DB);
        katAttributes.addField(FIELD_K_MVER, "", DBActionOrigin.FROM_DB);
        String guidFromRealEntry = (saaPartsListRowData != null) ? iPartsSaaPartsListPrimaryKey.buildSaaPartsListGUIDForPartListSeqNo(saaPartsListRowData.getConstKitAttributes(), saaPartsListRowData.getConstKitPath()) : "TextWithoutPartlistEntry";
        katAttributes.addField(FIELD_K_LFDNR, guidFromTextentry + "||&&||" + guidFromRealEntry, DBActionOrigin.FROM_DB);
        katAttributes.addField(FIELD_K_VIRTUAL_MAT_TYPE, (textkind == iPartsSaaPartlistTextkind.POS_TEXTS) ? VirtualMaterialType.TEXT_HEADING.getDbValue() : VirtualMaterialType.TEXT_SUB_HEADING.getDbValue(), DBActionOrigin.FROM_DB);

        fillMappedVirtualFields(level, positionValue, katAttributes);
        addAdditionalVirtualFields(katAttributes);
        return katAttributes;
    }

    /**
     * Durchläuft das virtuelles Feld zu Katalog Mapping und befüllt die virtuellen Felder
     *
     * @param level
     * @param positionValue
     * @param katAttributes
     */
    private void fillMappedVirtualFields(String level, String positionValue, DBDataObjectAttributes katAttributes) {
        // virtuelle Felder setzen
        for (VirtualFieldDefinition mapping : iPartsDataVirtualFieldsDefinition.getMapping(TABLE_DA_EDS_CONST_KIT, TABLE_KATALOG)) {
            String fieldName = mapping.getVirtualFieldName();
            String value;

            if (fieldName.equals(iPartsDataVirtualFieldsDefinition.EDS_LEVEL)) {
                value = level;
            } else if (fieldName.equals(iPartsDataVirtualFieldsDefinition.EDS_PARTPOS)) {
                value = positionValue;
            } else if (fieldName.equals(iPartsDataVirtualFieldsDefinition.EDS_RETAIL_USE)) {
                value = RETAIL_NOT_ASSIGNED;
            } else {
                value = "";
            }

            if (value != null) { // Feld hat nur einen Wert, wenn es benötigt wird
                katAttributes.addField(fieldName, value, true, DBActionOrigin.FROM_DB);
            }
        }
    }

    /**
     * Erweitert die <code>katAttributes</code> um virtuelle Felder die in der Stückliste benötigt werden
     *
     * @param katAttributes
     */
    private void addAdditionalVirtualFields(DBDataObjectAttributes katAttributes) {
        if (!katAttributes.fieldExists(iPartsDataVirtualFieldsDefinition.EDS_PARTLIST_TEXT_REMARKS)) {
            DBDataObjectAttribute objectAttribute = new DBDataObjectAttribute(iPartsDataVirtualFieldsDefinition.EDS_PARTLIST_TEXT_REMARKS, DBDataObjectAttribute.TYPE.MULTI_LANGUAGE, true);
            katAttributes.addField(objectAttribute, DBActionOrigin.FROM_DB);
        }
        if (!katAttributes.fieldExists(iPartsDataVirtualFieldsDefinition.EDS_PARTLIST_TEXT_WW_TEXT)) {
            DBDataObjectAttribute objectAttribute = new DBDataObjectAttribute(iPartsDataVirtualFieldsDefinition.EDS_PARTLIST_TEXT_WW_TEXT, DBDataObjectAttribute.TYPE.MULTI_LANGUAGE, true);
            katAttributes.addField(objectAttribute, DBActionOrigin.FROM_DB);
        }
        if (!katAttributes.fieldExists(iPartsDataVirtualFieldsDefinition.EDS_PARTLIST_TEXTKIND)) {
            katAttributes.addField(iPartsDataVirtualFieldsDefinition.EDS_PARTLIST_TEXTKIND, iPartsDialogPartlistTextkind.NOT_A_TEXT.getTxtKindShort(), DBActionOrigin.FROM_DB);
        }
    }

    /**
     * Erzeugt aus der übergebenen <code>parentAssembly</code> ein {@link MechanicUsagePosition} Objekt
     *
     * @param project
     * @param parentAssembly
     * @param resultAssemblyIds
     * @param partId
     * @param filtered
     * @param result
     */
    public static void addAssemblyAsMechanicalUsage(EtkProject project, EtkDataPartListEntry parentAssembly,
                                                    Set<AssemblyId> resultAssemblyIds, PartId partId, boolean filtered,
                                                    List<MechanicUsagePosition> result) {
        if (parentAssembly != null) {
            AssemblyId assemblyId = parentAssembly.getOwnerAssemblyId();
            if (resultAssemblyIds.add(assemblyId)) {
                // Jetzt diese Stückliste laden und testen, ob nach der Filterung über das aktuelle Datum das Teil noch drin ist
                EtkDataAssembly parent = EtkDataObjectFactory.createDataAssembly(project, assemblyId);

                List<EtkDataPartListEntry> entries;
                if (filtered) {
                    entries = parent.getPartList(null);
                } else {
                    entries = parent.getPartListUnfiltered(null).getAsList();
                }


                for (EtkDataPartListEntry entry : entries) {
                    if (entry.getPart().getAsId().equals(partId)) {
                        String quantity = entry.getFieldValue(FIELD_K_MENGE);
                        result.add(MechanicUsagePosition.createAsPartsEntry(parent.getAsId(), partId, entry.getAsId(), quantity));
                    }
                }
            }
        }
    }

    /**
     * Befüllt die übergebenen Listen mit allen Informationen, die für die Suche in einer Struktur mit Saa Stücklisten
     * notwendig sind
     *
     * @param project
     * @param tableName
     * @param andOrSearch
     * @param andOrWhereValues
     * @param selectValues
     * @param searchValues
     * @param selectFields
     */
    public static void fillSearchDataForSaaStructSearch(EtkProject project, String tableName, boolean andOrSearch,
                                                        List<String> andOrWhereValues, List<String> selectValues,
                                                        List<String> searchValues, List<String> selectFields) {
        if (andOrSearch) {
            // andOrSearch, dann gelten die Felder in andOrValues
            searchValues.addAll(andOrWhereValues);
        } else {
            searchValues.addAll(selectValues);
        }

        for (int i = searchValues.size() - 1; i >= 0; i--) {
            if (searchValues.get(i).isEmpty()) {
                searchValues.remove(i);
            }
        }

        for (int i = 0; i < searchValues.size(); i++) {
            // WildCards vorne und hinten
            String s = searchValues.get(i);
            s = SQLUtils.wildcardExpressionToSQLLike(s, true, true, true);
            s = project.getDB().getDBForTable(tableName).sqlToUpperCase(s);
            searchValues.set(i, s);
        }

        EtkDatabaseTable tableWithStructData = project.getConfig().getDBDescription().getTable(tableName);
        for (EtkDatabaseField field : tableWithStructData.getFieldList()) {
            selectFields.add(TableAndFieldName.make(tableName, field.getName()));
        }
    }

    /**
     * Erzeugt das SQL Query für die Suche in Strukturen, die Saa Stücklisten enthalten
     *
     * @param project
     * @param tableName
     * @param subElementField
     * @param selectFields
     * @param saaConstKitTable
     * @param saaConstKitField
     * @param saaConstKitDescField
     * @param modelField
     * @param searchValues
     * @param selectedModelsMap
     * @return
     */
    public static DBSQLQuery createQueryForSaaStructSearch(EtkProject project, String tableName, String subElementField,
                                                           List<String> selectFields, String saaConstKitTable,
                                                           String saaConstKitField, String saaConstKitDescField,
                                                           String modelField, List<String> searchValues,
                                                           Map<String, Set<String>> selectedModelsMap) {
        DBSQLQuery query = project.getDB().getDBForDomain(DBDatabaseDomain.MAIN).getNewQuery();
        query.select(new Fields(selectFields)).from(new Tables(tableName));

        // Join auf die Sprachtabelle
        Condition langCondition = new Condition(TableAndFieldName.make(tableName, subElementField), Condition.OPERATOR_EQUALS,
                                                new Fields(TableAndFieldName.make(saaConstKitTable, saaConstKitField)));
        query.join(new InnerJoin(saaConstKitTable, langCondition));

        // Join auf die Tabelle, die die Bezeichnungen für die gesuchte Saa oder den gesuchten Baukasten hat
        langCondition = new Condition(TableAndFieldName.make(saaConstKitTable, saaConstKitDescField), Condition.OPERATOR_EQUALS,
                                      new Fields(TableAndFieldName.make(TABLE_SPRACHE, FIELD_S_TEXTNR)));
        query.join(new InnerJoin(TABLE_SPRACHE, langCondition));
        AbstractCondition completeCondition
                = iPartsSaaBkConstPartsListHelper.createCompleteWhereConditionForSaaStructSearch(project, searchValues,
                                                                                                 tableName, subElementField,
                                                                                                 saaConstKitTable,
                                                                                                 saaConstKitDescField,
                                                                                                 modelField,
                                                                                                 selectedModelsMap);

        query.where(completeCondition);
        return query;
    }

    /**
     * Erzeugt die where-Bedingungen für SQL Queries für die Suche in Strukturen, die Saa Stücklisten enthalten
     *
     * @param project
     * @param searchValues
     * @param tableName
     * @param subElementField
     * @param saaConstKitTable
     * @param saaConstKitDescField
     * @param modelField
     * @param selectedModelsMap
     * @return
     */
    public static AbstractCondition createCompleteWhereConditionForSaaStructSearch(EtkProject project, List<String> searchValues,
                                                                                   String tableName, String subElementField,
                                                                                   String saaConstKitTable, String saaConstKitDescField,
                                                                                   String modelField, Map<String, Set<String>> selectedModelsMap) {
        CaseMode caseMode = (project.getEtkDbs().getDatabaseType(DBDatabaseDomain.MAIN).isUseUpperCaseMode()) ? CaseMode.UPPERCASE : CaseMode.NOTHING;
        AbstractCondition whereConditionList = null;
        // Die WHERE-Bedingung(en) zusammenbauen. Mehrere Bedingnugnen in der Schleife mit OR verknüpfen.
        for (String searchMask : searchValues) {

            // Suche in der SAA-Nummer verodern mit der Suche in der SAA-Text.
            whereConditionList = andOrCondition(whereConditionList, new Condition(TableAndFieldName.make(tableName, subElementField),
                                                                                  Condition.OPERATOR_LIKE, searchMask, caseMode), true);

            // Den Text passend zum Datenbankfeld abschneiden, falls er zu lang sein sollte.
            // ACHTUNG: Das Abschneiden muss sein, da nichts gefunden wird, wenn der Suchtext länger als das Datenbankfeld ist!
            String truncatedText = project.getEtkDbs().textTruncateToFieldSize(searchMask, EtkDbConst.TABLE_SPRACHE, EtkDbConst.FIELD_S_BENENN);

            // Teil[1] der Where-Bedingung:
            //       WHERE ( S_BENENN like 'xyz' OR S_BENENN_LANG = 'xyzabc' )
            // abhängig von der Datenbankversion. S_BENENN_LANG gibt es erst ab 6.2.
            Condition searchCond1 = new Condition(TableAndFieldName.make(TABLE_SPRACHE, FIELD_S_BENENN), Condition.OPERATOR_LIKE, truncatedText, caseMode);
            if ((project.getConfig().getDataBaseVersion() >= 6.2) && project.getConfig().getDBDescription().isSearchInLongTextsNeeded(searchMask)) {
                Condition searchCond2 = new Condition(TableAndFieldName.make(TABLE_SPRACHE, FIELD_S_BENENN_LANG), Condition.OPERATOR_LIKE, searchMask, caseMode);
                searchCond1.or(searchCond2);
            }
            // Die OR-Suchbedingung klammern.
            AbstractCondition wherePart1 = new ConditionList(searchCond1);

            // Teil[2] der Where-Bedingung:
            //       AND S_FELD = 'lalala' AND S_SPRACH = 'DE'
            AbstractCondition wherePart2 = new Condition(TableAndFieldName.make(TABLE_SPRACHE, FIELD_S_FELD), Condition.OPERATOR_EQUALS,
                                                         TableAndFieldName.make(saaConstKitTable, saaConstKitDescField).toUpperCase())
                    .and(new Condition(TableAndFieldName.make(TABLE_SPRACHE, FIELD_S_SPRACH), Condition.OPERATOR_EQUALS, project.getDBLanguage()));

            // Das komplette WHERE-Statement zur Sicherheit noch einmal klammern:
            AbstractCondition whereComplete = new ConditionList(wherePart1.and(wherePart2));

            // Am Ende der Schleife die erzeugte WHERE-Bedingung an die Liste der Where-Bedingungen anhängen.
            whereConditionList = andOrCondition(whereConditionList, whereComplete, true);
        }
        // Es sollen nur die Baumuster betrachtet werden, die der Benutzer in der Konstruktionssicht ausgewählt hat
        AbstractCondition modelNumberCondition = null;
        boolean makeConditionList = false;
        for (Set<String> selectedModels : selectedModelsMap.values()) {
            for (String modelNumber : selectedModels) {
                if (modelNumberCondition != null) {
                    makeConditionList = true;
                }
                modelNumberCondition = andOrCondition(modelNumberCondition, new Condition(TableAndFieldName.make(tableName, modelField),
                                                                                          Condition.OPERATOR_EQUALS, modelNumber), true);
            }
        }
        // modelNumberCondition kann nur null sein wenn keine Baumuster ausgewählt wurden, was hier nur rein theoretisch
        // passieren kann. Wenn keine BM augewählt wurden wird normalerweise vorher schon abgebrochen. Hier wird zur
        // Sicherheit dann auch rausgesprungen weil sonst ungewollt in allen BM gesucht werden würde
        if (modelNumberCondition == null) {
            return null;
        }
        // mehr als ein Baumuster -> Klammern der OR-Bedingungen
        if (makeConditionList) {
            modelNumberCondition = new ConditionList(modelNumberCondition);
        }
        AbstractCondition completeCondition;
        if (whereConditionList == null) {
            completeCondition = modelNumberCondition;
        } else {
            modelNumberCondition = modelNumberCondition.and(new ConditionList(whereConditionList));
            // Bedingungen für Suchmasken und Baumusternummer in einer ConditionList zusammenfassen
            completeCondition = new ConditionList(modelNumberCondition);
        }
        return completeCondition;
    }

    private static AbstractCondition andOrCondition(AbstractCondition oldCondition, AbstractCondition newCondition, boolean isOr) {
        if (oldCondition == null) {
            return newCondition;
        } else {
            if (isOr) {
                return oldCondition.or(newCondition);
            } else {
                return oldCondition.and(newCondition);
            }
        }
    }

    /**
     * Erzeugt für die übergebenen Baugruppen die Suchtreffer (subAssemblies der Baugruppen)
     *
     * @param project
     * @param assemblyIdList
     * @param result
     * @throws CanceledException
     */
    public static void fillSearchResultsFromSubAssemblyEntries(EtkProject project, Set<AssemblyId> assemblyIdList,
                                                               List<EtkDataPartListEntry> result) throws CanceledException {
        for (AssemblyId assemblyId : assemblyIdList) {
            if (Session.currentSessionThreadAppActionCancelled()) {
                throw new CanceledException(null);
            }
            EtkDataAssembly assembly = EtkDataObjectFactory.createDataAssembly(project, assemblyId);
            List<EtkDataPartListEntry> partListEntries = assembly.getSubAssemblyEntries(false, null);
            result.addAll(partListEntries);
        }
    }

    /**
     * Lese den Inhalt einer SAA oder eines Teilebaukastens mit allen Unterstrukturen zu einem bestimmten Gültigkeitsdatum.
     *
     * @param project
     * @param assemblyId
     * @param saaNo
     * @param saaOrConstKitNo SAA oder Teilenummer von einem Teilebaukasten als obere Sachnummer
     * @param isEDS
     * @return
     */
    public static DBDataObjectList<EtkDataPartListEntry> getSaaOrConstKitEntries(EtkProject project,
                                                                                 iPartsAssemblyId assemblyId, String saaNo,
                                                                                 String saaOrConstKitNo, String revision,
                                                                                 ConstructionValidationDateHelper validationHelper,
                                                                                 int maxLevelFromConfig, boolean isEDS) {

        iPartsSaaBkConstPartsListHelper helper = new iPartsSaaBkConstPartsListHelper(project, assemblyId, maxLevelFromConfig,
                                                                                     isEDS ? SaaBkConstPartListType.EDS
                                                                                           : SaaBkConstPartListType.CTT);
        return helper.loadVirtualSaaPartsList(saaNo, saaOrConstKitNo, false, revision, validationHelper);
    }

    /**
     * Erzeugt einen Saa Knoten für die Saa Ebene in einer Saa Konstruktion
     *
     * @param project
     * @param lfdNumber
     * @param assemblyId
     * @param virtualKeyString
     * @return
     */
    public static EtkDataPartListEntry createSaaPartsListNode(EtkProject project, int lfdNumber, AssemblyId assemblyId, String virtualKeyString) {
        // Attribute erzeugen und alle Felder mit Leer auffüllen
        DBDataObjectAttributes katAttributes = iPartsVirtualAssemblyHelper.createEmptyAttributesForTable(project, TABLE_KATALOG);
        DBDataObjectAttributes matAttributes = iPartsVirtualAssemblyHelper.createEmptyAttributesForTable(project, TABLE_MAT);


        katAttributes.addField(FIELD_K_VARI, assemblyId.getKVari(), DBActionOrigin.FROM_DB);
        katAttributes.addField(FIELD_K_VER, assemblyId.getKVer(), DBActionOrigin.FROM_DB);
        katAttributes.addField(FIELD_K_SACH, virtualKeyString, DBActionOrigin.FROM_DB);
        katAttributes.addField(FIELD_K_VER, "", DBActionOrigin.FROM_DB);
        katAttributes.addField(FIELD_K_MATNR, virtualKeyString, DBActionOrigin.FROM_DB);
        katAttributes.addField(FIELD_K_MVER, "", DBActionOrigin.FROM_DB);
        katAttributes.addField(FIELD_K_LFDNR, EtkDbsHelper.formatLfdNr(lfdNumber), DBActionOrigin.FROM_DB);
        katAttributes.addField(FIELD_K_SEQNR, EtkDbsHelper.formatLfdNr(lfdNumber), DBActionOrigin.FROM_DB);

        EtkDataPartListEntry newPartListEntry = EtkDataObjectFactory.createDataPartListEntry(project, katAttributes);

        EtkDataPart partForPartListEntry = newPartListEntry.getPart();

        matAttributes.addField(FIELD_M_MATNR, virtualKeyString, DBActionOrigin.FROM_DB);
        matAttributes.addField(FIELD_M_VER, "", DBActionOrigin.FROM_DB);
        matAttributes.addField(FIELD_M_BESTNR, iPartsDataVirtualFieldsHelper.getOrderNumberFromVirtualKey(virtualKeyString, project), DBActionOrigin.FROM_DB);

        // Texte des neuen Eintrags
        EtkMultiSprache texts = iPartsDataVirtualFieldsHelper.getTextsFromVirtualKey(virtualKeyString, project);
        if (texts != null) {
            DBDataObjectAttribute textAttribute = matAttributes.getField(FIELD_M_TEXTNR);
            if (textAttribute != null) {
                textAttribute.setValueAsMultiLanguage(texts, DBActionOrigin.FROM_DB);
            }
        }

        partForPartListEntry.setAttributes(matAttributes, DBActionOrigin.FROM_DB);

        if (newPartListEntry instanceof iPartsDataPartListEntry) {
            ((iPartsDataPartListEntry)newPartListEntry).setDataLoadedFlagsForVirtualPLE(true);
        }
        return newPartListEntry;
    }

    public static EtkDisplayFields getNeededMatFieldsForSaaPartsList(EtkProject project, AssemblyId assemblyId) {
        return iPartsVirtualAssemblyHelper.getNeededMatSelectFields(project, assemblyId, FIELD_M_MARKET_ETKZ, FIELD_M_ASSEMBLY, FIELD_M_IMAGE_AVAILABLE);
    }

    /**
     * Sucht nach Treffern in der Saa Konstruktionsstückliste
     *
     * @param optionalRootAssemblyId
     * @param isSearchValuesDisjunction
     * @param selectFields
     * @param selectValues
     * @param whereFields
     * @param whereValues
     * @param andOrSearch
     * @param project
     * @param multiLanguageCache
     * @param wildCardSettings
     * @param isEDS
     * @return
     */
    public static iPartsSearchVirtualDataset searchPartListEntriesInSaaPartLists(final AssemblyId optionalRootAssemblyId,
                                                                                 final boolean isSearchValuesDisjunction,
                                                                                 final EtkDisplayFields selectFields,
                                                                                 final List<String> selectValues,
                                                                                 final EtkDisplayFields whereFields,
                                                                                 final List<String> whereValues,
                                                                                 final boolean andOrSearch, EtkProject project,
                                                                                 WeakKeysMap<String, String> multiLanguageCache,
                                                                                 WildCardSettings wildCardSettings,
                                                                                 boolean isEDS) {

        return new iPartsVirtualMaterialSearchDataset(TABLE_DA_EDS_CONST_KIT, FIELD_DCK_SUB_SNR, optionalRootAssemblyId, isSearchValuesDisjunction,
                                                      selectFields, selectValues, whereFields, whereValues, andOrSearch, project, multiLanguageCache,
                                                      wildCardSettings) {

            private int fieldIndexPartNumber;


            @Override
            protected void addNeededJoins(PartsSearchSqlSelect partsSearchSqlSelect, List<String> doNotJoinList) {

            }

            @Override
            protected void addAdditionalSelectFields(EtkDisplayFields selectFieldsWithoutKatalog, List<String> selectValuesWithoutKatalog) {
                fieldIndexPartNumber = selectFieldsWithoutKatalog.size();
                selectFieldsWithoutKatalog.addFeld(new EtkDisplayField(TABLE_DA_EDS_CONST_KIT, FIELD_DCK_SUB_SNR, false, false));
                selectValuesWithoutKatalog.add("");
            }

            @Override
            protected String getPartNumber(List<String> values) {
                return values.get(fieldIndexPartNumber);
            }

            @Override
            protected HierarchicalIDWithType createParentId(List<String> values) {
                return null;
            }

            @Override
            protected List<EtkDataPartListEntry> searchResultPartListEntries(List<String> values, String partNumber, HierarchicalIDWithType parentId) {
                List<EtkDataPartListEntry> resultPartListEntries = new ArrayList<>();
                List<MechanicUsagePosition> usagePositions = new ArrayList<>();
                // Suche die Verwendung dieses Materials in der Saa-Stückliste. Wir verwenden diese Funktion, weil dort schon das mit der Hirarchie
                // der oberen und unteren Sachnummer brücksichtigt wird.
                // Von der Performance ist das nicht ideal, aber die Rekursion muss irgendwo durchlaufen werden
                if (isEDS) {
                    iPartsVirtualAssemblyEdsBase.getMechanicUsageForEdsMaterial(new PartId(partNumber, ""), true, project, usagePositions);
                } else {
                    iPartsVirtualAssemblyCTT.getMechanicUsageForCTTMaterial(new PartId(partNumber, ""), true, project, usagePositions);
                }

                for (MechanicUsagePosition usagePosition : usagePositions) {
                    createAndAddSearchResultPartListEntry(usagePosition.getParentAssemblyId(), usagePosition.getInternChildPartListEntryId().getKLfdnr(), partNumber, resultPartListEntries);
                }
                return resultPartListEntries;
            }
        };
    }

    /**
     * Liefert alle übergeordneten Teilenummer zur übergebenen Teilenummer in allen Saa Stücklisten
     *
     * @param project
     * @param matNo
     * @param currentLevel
     * @param maxLevel
     * @return
     */
    public static Set<String> getAllMatParentNumbersOfSaaPartsList(EtkProject project, String matNo, int currentLevel, int maxLevel) {

        Set<String> result = new LinkedHashSet<>();
        if (currentLevel > maxLevel) {
            return result;
        }

        //In die Liste erstmal sich selbst eintragen
        result.add(matNo);

        DBDataObjectAttributesList attributesList = project.getDbLayer().getAttributesList(TABLE_DA_EDS_CONST_KIT,
                                                                                           new String[]{ FIELD_DCK_SNR },
                                                                                           new String[]{ FIELD_DCK_SUB_SNR },
                                                                                           new String[]{ matNo });
        for (DBDataObjectAttributes attributes : attributesList) {
            String parentPartNo = attributes.getField(FIELD_DCK_SNR).getAsString();
            if (!result.contains(parentPartNo)) {
                result.add(attributes.getField(FIELD_DCK_SNR).getAsString());

                // Jetzt rekursiv die Ebenen durchgehen
                Set<String> parentResults = getAllMatParentNumbersOfSaaPartsList(project, attributes.getField(FIELD_DCK_SNR).getAsString(),
                                                                                 currentLevel + 1, maxLevel);

                result.addAll(parentResults);
            }
        }

        return result;
    }

}
