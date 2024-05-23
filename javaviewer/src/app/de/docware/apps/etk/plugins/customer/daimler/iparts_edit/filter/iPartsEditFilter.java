/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.filter;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.filter.EtkFilterItems;
import de.docware.apps.etk.base.project.filter.EtkFilterTyp;
import de.docware.apps.etk.base.project.filter.FilterMode;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.VirtualMaterialType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualassembly.helper.iPartsSaaBkConstPartsListHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsVirtualNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.code.DaimlerCodes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsPartlistTextHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsUserSettingsHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.textkinds.iPartsDialogPartlistTextkind;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.textkinds.iPartsSaaPartlistTextkind;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.iPartsEditValidationHelper;
import de.docware.apps.etk.plugins.utils.GridFilterReturnType;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.gui.misc.logger.LogChannels;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.session.Session;
import de.docware.util.StrUtils;
import de.docware.util.sql.TableAndFieldName;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Methodik abgeschaut von DraegerMtFilter
 */
public class iPartsEditFilter {

    private static final String SESSION_ATTRIB_IPARTS_EDIT_FILTER = "iPartsEditFilter";


    public static iPartsEditFilter get() {
        iPartsEditFilter filter = (iPartsEditFilter)Session.get().getAttribute(SESSION_ATTRIB_IPARTS_EDIT_FILTER);
        if (filter == null) {
            filter = new iPartsEditFilter();
            filter.load();
            Session.get().setAttribute(SESSION_ATTRIB_IPARTS_EDIT_FILTER, filter);
        }
        return filter;
    }

    /**
     * Initialisierungen
     */
    public void load() {

    }


    public GridFilterReturnType checkGridFilter(EtkFilterTyp filterTyp, String tableName, String fieldName, DBDataObjectAttributes attributes, String language) {
        if ((fieldName != null) && (filterTyp != null) && filterTyp.isActive()) {
            DBDataObjectAttribute attribute = attributes.getField(fieldName, false);
            if (attribute != null) {
                String codeString = attribute.getAsString();
                // Leere Code behandeln
                if (DaimlerCodes.isEmptyCodeString(codeString)) {
                    List<String> filterValues = filterTyp.getFilterValues();
                    //zuerst Suche in gesamten filterValues nach leerem Code
                    for (String filterValue : filterValues) {
                        if (DaimlerCodes.isEmptyCodeString(prepareFilterValue(filterValue))) {
                            return GridFilterReturnType.FILTERED_TRUE;
                        }
                    }
                }

                if (!containsCode(codeString, tableName, filterTyp)) {
                    return GridFilterReturnType.FILTERED_FALSE;
                }
                return GridFilterReturnType.FILTERED_TRUE;
            }
        }
        return GridFilterReturnType.NOT_FILTERED;
    }

    private String prepareFilterValue(String filterValue) {
        if (StrUtils.stringContainsWildcards(filterValue)) {
            filterValue = StrUtils.replaceSubstring(filterValue, "*", "");
            filterValue = StrUtils.replaceSubstring(filterValue, "?", "");
        }
        return filterValue;
    }

    /**
     * überprüft den Filter für einen codeString (FeldTyp Memo)
     * Suchalgorithmus: freie Suche in codeString
     * kann eingegrenzt werden auf Suche in codes
     *
     * @param codeString
     * @param tableName
     * @param filterTyp
     * @return
     */
    private boolean containsCode(String codeString, String tableName, EtkFilterTyp filterTyp) {
        EtkFilterItems items = filterTyp.getFilterItems().getItemsByTable(tableName);

        if (!items.isEmpty()) {
            List<String> filterValues = filterTyp.getFilterValues();
            //zuerst Suche im gesamten codeString
            for (String filterValue : filterValues) {
                // DAIMLER-15742: Auswerten der Filter
                if (!iPartsEditValidationHelper.isAllPartialConjunctionsIncludedForColumnFilter(prepareFilterValue(filterValue), codeString)) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean checkFilter(EtkDataObject etkDataObject, FilterMode filterMode) {
        if (etkDataObject instanceof EtkDataPartListEntry) {
            EtkDataPartListEntry dataObjectWithPart = (EtkDataPartListEntry)etkDataObject;
            // Check, ob es sich um einen Text handelt
            boolean isPartListText = VirtualMaterialType.isPartListTextEntry(dataObjectWithPart);
            EtkDataAssembly parentAssembly = dataObjectWithPart.getOwnerAssembly();

            String partListType = parentAssembly.getEbeneName();
            boolean isEds = partListType.equals(iPartsConst.PARTS_LIST_TYPE_EDS_SAA);
            if (isEds || partListType.equals(iPartsConst.PARTS_LIST_TYPE_CTT_SAA)) {
                // Check ob, das Teil in einem EDS Baumuster liegt, das nicht angezeigt werden soll (z.B. aus der Suche raus)
                if (isEds && !checkEdsModelConstFilter(parentAssembly.getAsId())) {
                    return false;
                }
                // Check ob, das Teil in einem CTT Baumuster liegt, das nicht angezeigt werden soll (z.B. aus der Suche raus)
                if (!isEds && !checkCTTModelConstFilter(parentAssembly.getAsId())) {
                    return false;
                }

                String level = dataObjectWithPart.getFieldValue(iPartsDataVirtualFieldsDefinition.EDS_LEVEL);
                if (level.isEmpty()) { // tritt während der Suche auf
                    return true;
                }
                if (isPartListText && isVirtualPartlist(parentAssembly)) {
                    String textkindValue = dataObjectWithPart.getFieldValue(iPartsDataVirtualFieldsDefinition.EDS_PARTLIST_TEXTKIND);
                    iPartsSaaPartlistTextkind textkind = iPartsSaaPartlistTextkind.getFromTextkindShort(textkindValue);
                    if (textkind != iPartsSaaPartlistTextkind.NOT_A_TEXT) {
                        if (isEds && !iPartsPartlistTextHelper.isTextForSelectedEDSBCSTextkind(parentAssembly.getEtkProject(), textkind)) {
                            return false;
                        }
                        if (!isEds && !iPartsPartlistTextHelper.isTextForSelectedCTTTextkind(parentAssembly.getEtkProject(), textkind)) {
                            return false;
                        }
                    }
                }

                int maxLevel = isEds ? iPartsUserSettingsHelper.getEdsSaaStructureLevel(parentAssembly.getEtkProject())
                                     : iPartsUserSettingsHelper.getCTTSaaStructureLevel(parentAssembly.getEtkProject());
                if (!isStructureLevelValid(maxLevel, level)) {
                    return false;
                }

                // Ist die Serien Sicht aktiv, werden Positionen mit POS >= 999000 (ET-Positionen) und alle darunter
                // liegenden Positionen ausgefiltert
                AssemblyId assemblyId = dataObjectWithPart.getOwnerAssemblyId();
                // Check, ob an der Stückliste die Checkbox gesetzt ist
                boolean checkBoxSet = isEds ? iPartsUserSettingsHelper.isEDSSeriesViewActiveForAssembly(dataObjectWithPart.getEtkProject(), assemblyId)
                                            : iPartsUserSettingsHelper.isCTTSeriesViewActiveForAssembly(dataObjectWithPart.getEtkProject(), assemblyId);
                if (checkBoxSet) {
                    // Erst prüfen, ob schon eine Position mit POS >= 999000 schon ausgefiltert wurde. Falls ja, wurde
                    // ihre Strukturstufe gespeichert
                    int levelOfCurrentEntry = StrUtils.strToIntDef(level, -1);
                    int leveOfRemovedPosition = isEds ? iPartsUserSettingsHelper.getEDSSeriesViewActiveEntryLevel(dataObjectWithPart.getEtkProject())
                                                      : iPartsUserSettingsHelper.getCTTSeriesViewActiveEntryLevel(dataObjectWithPart.getEtkProject());
                    if (leveOfRemovedPosition > 0) {
                        if (levelOfCurrentEntry > leveOfRemovedPosition) {
                            // Strukturstufe von ET-Position existiert und die Strukturstufe der aktuellen Position ist größer,
                            // d.h. die aktuelle Position liegt unter der ET-Position -> ausfiltern
                            return false;
                        } else {
                            // Strukturstufe von ET-Position existiert und die Strukturstufe der aktuellen Position ist
                            // gleich oder kleiner der ET-Position, d.h. die aktuelle Position liegt auf gleicher Ebene mit der
                            // ET-Position oder darunter -> die Strukturstufe der ET-Position muss aus dem Speicher entfernt werden
                            if (isEds) {
                                iPartsUserSettingsHelper.setEDSSeriesViewActiveEntryLevel(dataObjectWithPart.getEtkProject(), 0);
                            } else {
                                iPartsUserSettingsHelper.setCTTSeriesViewActiveEntryLevel(dataObjectWithPart.getEtkProject(), 0);
                            }
                        }
                    }

                    // EDS Positionswert bestimmen
                    String posValue = dataObjectWithPart.getFieldValue(iPartsDataVirtualFieldsDefinition.EDS_PARTPOS);
                    long posValueLong = StrUtils.strToIntDef(posValue, -1);
                    // Check, ob der Positionswert größer gleich der Grenze für ET-Positionen ist. Falls ja, die
                    // aktuelle Position ausblenden (ist schließlich eine ET-Position) und ihre Strukturstufe für die
                    // nachfolgenden Positionen zwischenspeichern
                    if (posValueLong >= iPartsSaaBkConstPartsListHelper.ET_POS_AVAILABLE_MIN_POS) {
                        if (isEds) {
                            iPartsUserSettingsHelper.setEDSSeriesViewActiveEntryLevel(dataObjectWithPart.getEtkProject(), levelOfCurrentEntry);
                        } else {
                            iPartsUserSettingsHelper.setCTTSeriesViewActiveEntryLevel(dataObjectWithPart.getEtkProject(), levelOfCurrentEntry);
                        }
                        return false;
                    }
                }

            } else if (partListType.equals(iPartsConst.PARTS_LIST_TYPE_MBS_CON_GROUP)) {
                // Check ob, das Teil in einem MBS Baumuster liegt, das nicht angezeigt werden soll (z.B. aus der Suche raus)
                if (!checkMBSModelConstFilter(parentAssembly.getAsId())) {
                    return false;
                }

                // Strukturstufe prüfen
                String level = dataObjectWithPart.getFieldValue(iPartsDataVirtualFieldsDefinition.MBS_LEVEL);
                if (level.isEmpty()) { // tritt während der Suche auf
                    return true;
                }
                int maxLevel = iPartsUserSettingsHelper.getMBSStructureLevel(parentAssembly.getEtkProject());
                if (!isStructureLevelValid(maxLevel, level)) {
                    return false;
                }
            } else if (partListType.equals(iPartsConst.PARTS_LIST_TYPE_STRUCTURE_MODEL)) {
                if (parentAssembly instanceof iPartsDataAssembly) {
                    iPartsDataAssembly dataAssembly = (iPartsDataAssembly)parentAssembly;
                    if (dataAssembly.isEdsConstructionAssembly()) { // EDS Baumuster ausfiltern
                        if (!checkEdsModelConstFilter(dataObjectWithPart.getDestinationAssemblyId())) {
                            return false;
                        }
                    } else if (dataAssembly.isMBSConstructionAssembly()) { // MBS Baumuster ausfiltern
                        if (!checkMBSModelConstFilter(dataObjectWithPart.getDestinationAssemblyId())) {
                            return false;
                        }
                    } else if (dataAssembly.isCTTConstructionAssembly()) { // CTT Baumuster ausfiltern
                        if (!checkCTTModelConstFilter(dataObjectWithPart.getDestinationAssemblyId())) {
                            return false;
                        }
                    }
                }
            } else if (partListType.equals(iPartsConst.PARTS_LIST_TYPE_DIALOG_SM)) {
                if (etkDataObject instanceof iPartsDataPartListEntry) {
                    iPartsDataPartListEntry partListEntry = (iPartsDataPartListEntry)etkDataObject;

                    // Keine Filterung durchführen, wenn der Filter-Aufruf aus den Suchergebnissen heraus stattfindet
                    if (filterMode == FilterMode.SEARCH_RESULT) {
                        return true;
                    }

                    // Stücklistentexte dürfen nicht ausgefiltert werden
                    if (!isPartListText) {
                        String posFilterValue = iPartsUserSettingsHelper.getConstPartListFilterValue(partListEntry.getEtkProject());
                        if (!posFilterValue.isEmpty()) {
                            String posValue = partListEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_POSE);
                            boolean result = posFilterValue.compareTo(posValue) > 0;
                            if (result) {
                                return false;
                            }
                        }
                        boolean showNonASRelEntries = iPartsUserSettingsHelper.isShowNonASRelEntries(partListEntry.getEtkProject());
                        if (!showNonASRelEntries) {
                            // Für AS nicht relevante Einträge sollen nicht angezeigt werden
                            if (!partListEntry.getFieldValueAsBoolean(iPartsDataVirtualFieldsDefinition.DD_CALCULATED_AS_RELEVANT)) {
                                return false;
                            }
                        }
                    }

                    boolean showOnlyLastApprovedEntries = iPartsUserSettingsHelper.isShowOnlyLastApprovedEntries(partListEntry.getEtkProject());
                    if (showOnlyLastApprovedEntries) {
                        // Nur die letzten freigegeben Einträge (SDATB = leer) sollen angezeigt werden
                        if (!partListEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_SDATB).isEmpty()) {
                            return false;
                        }
                    }
                }
                // Stücklistentexte können vom Benutzer bestimmt werden. Alle nicht ausgewählten Textarten werden hier
                // ausgefiltert. Stücklistentexte existieren nur für Stücklisteneinträge ohne Materialnummer.
                // Die Texte können nur angezeigt werden, wenn das dataObject vollständig geladen werden konnte.
                // Dies ist z.B. nicht der Fall, wenn man aus der Suche (auch Materialverwendung) kommt, denn dort ist
                // kLfdnr = null.  Um diesen Fall abfangen zu können, wird hier geprüft, ob die attributes geladen wurden
                if (isPartListText && isVirtualPartlist(parentAssembly)) {
                    String textkindValue = dataObjectWithPart.getFieldValue(iPartsDataVirtualFieldsDefinition.DD_PARTLIST_TEXTKIND);
                    iPartsDialogPartlistTextkind textkind = iPartsDialogPartlistTextkind.getFromTextkindShort(textkindValue);
                    if (textkind != iPartsDialogPartlistTextkind.NOT_A_TEXT) {
                        if (!iPartsPartlistTextHelper.isTextForSelectedDIALOGTextkind(parentAssembly.getEtkProject(), textkind)) {
                            return false;
                        }
                    }
                }
            }

        }
        return true;
    }

    /**
     * Prüft, ob die übergebene Strukturstufe kleiner als das Maximum ist
     *
     * @param maxLevel
     * @param level
     * @return
     */
    private boolean isStructureLevelValid(int maxLevel, String level) {
        int iLevel = StrUtils.strToIntDef(level, -1);
        if (iLevel == -1) {
            Logger.log(LogChannels.APPLICATION, LogType.ERROR, "iPartsEditFilter.checkFilter(): Level '" + level + "' not numeric -> filter not applied");
            return true;
        }
        return iLevel <= maxLevel;
    }

    private boolean isVirtualPartlist(EtkDataAssembly parentAssembly) {
        if (parentAssembly instanceof iPartsDataAssembly) {
            iPartsDataAssembly assembly = (iPartsDataAssembly)parentAssembly;
            return assembly.getAsId().isVirtual();
        }
        return false;
    }


    /**
     * wie checkFilter(), jedoch nur speziell für Editor
     *
     * @param etkDataObject
     * @return
     */
    public boolean checkEditFilter(EtkDataObject etkDataObject) {
        if (etkDataObject instanceof EtkDataPartListEntry) {
            EtkDataPartListEntry dataObjectWithPart = (EtkDataPartListEntry)etkDataObject;
            EtkDataAssembly parentAssembly = dataObjectWithPart.getOwnerAssembly();

            if ((parentAssembly instanceof iPartsDataAssembly) && ((iPartsDataAssembly)parentAssembly).isPartListEditable()) {
                if (!dataObjectWithPart.getFieldValue(iPartsDataVirtualFieldsDefinition.RETAIL_MODIFIED_STATE).equals(iPartsConst.ENUM_MODIFIED_STATE_NEW)) {
                    return false;
                }
            }

        }
        return true;
    }

    /**
     * Überprüft, ob in der übergebenen {@link AssemblyId} ein EDS Konstruktionsbaumuster existiert, das in der
     * Konstruktionsstückliste ausgefiltert wurde
     *
     * @param assemblyId
     * @return
     */
    private boolean checkEdsModelConstFilter(AssemblyId assemblyId) {
        return checkModelConstFilter(assemblyId, iPartsPlugin.SESSION_KEY_SELECT_EDS_CONST_MODEL);
    }

    /**
     * Überprüft, ob in der übergebenen {@link AssemblyId} ein MBS Konstruktionsbaumuster existiert, das in der
     * Konstruktionsstückliste ausgefiltert wurde
     *
     * @param assemblyId
     * @return
     */
    private boolean checkMBSModelConstFilter(AssemblyId assemblyId) {
        return checkModelConstFilter(assemblyId, iPartsPlugin.SESSION_KEY_SELECT_MBS_CONST_MODEL);
    }

    /**
     * Überprüft, ob in der übergebenen {@link AssemblyId} ein CTT Konstruktionsbaumuster existiert, das in der
     * Konstruktionsstückliste ausgefiltert wurde
     *
     * @param assemblyId
     * @return
     */
    private boolean checkCTTModelConstFilter(AssemblyId assemblyId) {
        return checkModelConstFilter(assemblyId, iPartsPlugin.SESSION_KEY_SELECT_CTT_CONST_MODEL);
    }

    /**
     * Überprüft, ob in der übergebenen {@link AssemblyId} ein Konstruktionsbaumuster existiert, das in der
     * Konstruktionsstückliste ausgefiltert wurde
     *
     * @param assemblyId
     * @return
     */
    private boolean checkModelConstFilter(AssemblyId assemblyId, String sessionKey) {
        String modelNumber = iPartsVirtualNode.getModelNumberFromAssemblyId(assemblyId);
        if (StrUtils.isValid(modelNumber)) {
            Map<String, Set<String>> filterValuesMap = (Map<String, Set<String>>)Session.get().getAttribute(sessionKey);
            if (filterValuesMap != null) {
                for (Set<String> valueSet : filterValuesMap.values()) {
                    if (valueSet.contains(modelNumber)) {
                        return true;
                    }
                }
            }
            return false;
        }
        return true;
    }

    public Set<String> getActiveFilterFields(Set<String> neededTables) {
        if (neededTables.contains(EtkDbConst.TABLE_KATALOG)) {
            Set<String> neededFilterFields = new HashSet<>();
            neededFilterFields.add(TableAndFieldName.make(iPartsConst.TABLE_KATALOG, iPartsConst.FIELD_K_VIRTUAL_MAT_TYPE));
            return neededFilterFields;
        }

        return null;
    }

}
