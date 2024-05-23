/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import com.owlike.genson.Genson;
import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkEbenenDaten;
import de.docware.apps.etk.base.db.AbstractRevisionChangeSet;
import de.docware.apps.etk.base.db.EtkRevisionsHelper;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.base.GenericEtkDataObjectList;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPart;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntryList;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.base.project.mechanic.ids.PartId;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.AggregateDataCard;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets.iPartsDataReservedPKList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsDataFootNoteCatalogueRef;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsDataFootNoteCatalogueRefList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsFootNoteCatalogueRefId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsFootNoteId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.replace.iPartsDataIncludePart;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.replace.iPartsDataIncludePartList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.replace.iPartsDataReplacePart;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualassembly.AOAffiliationForDIALOGEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualassembly.iPartsVirtualAssemblyDialogBase;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualassembly.iPartsVirtualCalcFieldDocuRel;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualassembly.iPartsVirtualFieldCalcASRel;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsAssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsVirtualNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.code.DaimlerCodes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilter;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.footnotes.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.replacements.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogBCTEPrimaryKey;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogSeries;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.eds.json.SparePartSignAndMarketJSONObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.eds.json.SparePartSignsJSONObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.HierarchicalIDWithType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.helper.iPartsEdsStructureHelper;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.config.db.VirtualFieldDefinition;
import de.docware.framework.modules.config.db.VirtualFieldsUtils;
import de.docware.framework.modules.config.db.datatypes.DatatypeUtils;
import de.docware.framework.modules.db.*;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.resources.FrameworkImage;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.session.Session;
import de.docware.framework.utils.EtkDataArray;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.framework.utils.JSONUtils;
import de.docware.framework.utils.VarParam;
import de.docware.util.ArrayUtil;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.misc.id.IdWithType;
import de.docware.util.sql.SQLStringConvert;
import de.docware.util.sql.TableAndFieldName;

import java.util.*;

/**
 * Erweiterung von {@link EtkDataPartListEntry} um iParts-spezifische Methoden und Daten.
 */
public class iPartsDataPartListEntry extends EtkDataPartListEntry implements iPartsConst {

    private static final iPartsDialogBCTEPrimaryKey EMPTY_BCTE_PRIMARY_KEY = new iPartsDialogBCTEPrimaryKey("", "", "", "", "", "", "", "", "", "");

    private static final String ENUM_CODES_EQUAL = "EQ";
    private static final String ENUM_CODES_NOT_EQUAL = "NEQ";

    private static final String PRIMUS_CODE_22_HAS_SUCCESSOR = "22";
    private static final String PRIMUS_CODE_28_HAS_SUCCESSOR = "28";

    // Virtuelle Felder, die in loadVirtualField() das Nachladen über das Modul erfordern
    private static final Set<String> VIRTUAL_FIELDS_LOADED_BY_ASSEMBLY = new HashSet<>();

    static {
        VIRTUAL_FIELDS_LOADED_BY_ASSEMBLY.add(iPartsDataVirtualFieldsDefinition.DIALOG_DD_FACTORY_ID);
        VIRTUAL_FIELDS_LOADED_BY_ASSEMBLY.add(iPartsDataVirtualFieldsDefinition.DIALOG_DD_FACTORY_FIRST_USE);
        VIRTUAL_FIELDS_LOADED_BY_ASSEMBLY.add(iPartsDataVirtualFieldsDefinition.DIALOG_DD_FACTORY_FIRST_USE_TO);
    }

    /**
     * Flag "Automatisch erzeugt" muss bei Edit-Aktionen zurückgesetzt werden
     *
     * @param partListEntry
     * @return Wurde das Flag zurückgesetzt?
     */
    public static boolean resetAutoCreatedFlag(EtkDataPartListEntry partListEntry) {
        if (partListEntry.getFieldValueAsBoolean(iPartsConst.FIELD_K_AUTO_CREATED)) {
            partListEntry.setFieldValueAsBoolean(iPartsConst.FIELD_K_AUTO_CREATED, false, DBActionOrigin.FROM_EDIT);
            return true;
        }
        return false;
    }

    private String aggregateType;

    private boolean footNotesLoaded;
    private Collection<iPartsFootNote> footNotes;

    private boolean factoryDataForRetailLoaded;
    private boolean factoryDataForRetailWithoutReplacementsCalculated;
    private boolean factoryDataForRetailCalculated;
    private iPartsFactoryData factoryDataForRetailUnfiltered;
    private iPartsFactoryData factoryDataForRetailWithoutReplacements;
    private iPartsFactoryData factoryDataForRetail;
    private boolean factoryDataForConstructionLoaded;
    private iPartsFactoryData factoryDataForConstruction;
    private String currentInternalText;

    private boolean timeSliceDatesCalculatedForModelScoring;
    private long timeSliceDateFromForModelScoring = 0;
    private long timeSliceDateToForModelScoring = Long.MAX_VALUE;

    private boolean timeSliceDatesLoadedUnfiltered;
    private long timeSliceDateFromUnfiltered = 0;
    private long timeSliceDateToUnfiltered = Long.MAX_VALUE;

    // die Farbtabellen enthalten auch ggf. die Werkseinsatzdaten.
    // die Farbtabellen verweisen auf die Farbtabelleninhalte, die ihrerseits wieder ggf. Werkseinsatzdaten enthalten
    private boolean colorTableLoaded;
    private iPartsColorTable colorTableForRetailWithoutFilter;
    private boolean colorTableFiltered;
    private iPartsColorTable colorTableForRetail;
    private boolean colorTableConstructionLoaded;
    private iPartsColorTable colorTableForConstruction;
    private boolean hasColorTablesUnfiltered; // gibt es unabhängig vom Retailfilter Farbtabellen zu diesem Teil?

    private boolean replacementsLoaded;
    private List<iPartsReplacement> predecessors;
    private List<iPartsReplacement> successors;
    private boolean filteredSuccessorsCalculated;
    private List<iPartsReplacement> filteredSuccessors;
    private boolean materialReplacedByPRIMUSSuccessor;

    private Collection<iPartsReplacementConst> predecessorsConst;
    private Collection<iPartsReplacementConst> successorsConst;

    // Materialstämme für Alternativteile (PRIMUS-Teile); null wenn keine vorhanden
    // Ob ein Material Alternativteil ist, ergibt sich aus dem Inhalt von ES1-/ES2-Feld des Materials u. ggf. weiterer Regeln (siehe Zuweisung)
    // zur Definition siehe https://confluence.docware.de/x/e4JbAQ
    private Set<EtkDataPart> alternativeParts;

    private boolean dataDIALOGChangesLoaded;
    private iPartsDataDIALOGChangeList dataDIALOGChangesForBCTE = null; // DIALOG Änderungsreferenzen für BCTE-Schlüssel
    private iPartsDataDIALOGChangeList dataDIALOGChangesForMAT = null; // DIALOG Änderungsreferenzen für Material
    private List<EtkMultiSprache> combinedMultiTextList;  // Liste der kombinierten MultiLang-Texte
    private boolean isColoredPart; // Handelt es sich um ein Farbteil?

    private boolean originalFailLocationCalculated;
    private boolean inheritedFailLocationCalculated;

    private iPartsDataCombTextList dataCombTextList;  // Kann für bessere Performance beim Löschen und Kopieren von Stücklisteneinträgen gesetzt werden

    private Boolean hasDifferentEqualPartNumber;

    private iPartsDialogBCTEPrimaryKey dialogBCTEPrimaryKey;
    private boolean dialogBCTEPrimaryKeyCalculated;
    private List<FrameworkImage> previewImages; // Harte Referenz notwendig, damit die verwendeten FrameworkImages nicht aufgeräumt werden bei Speichermangel

    @Override
    public void assignRecursively(EtkProject project, EtkDataObject source, DBActionOrigin origin) {
        super.assignRecursively(project, source, origin);

        // Diese Daten müssen kopiert werden (assignRecursively() wird z.B. aufgerufen, wenn eine Stückliste aus dem Cache
        // verwendet wird)
        if (source instanceof iPartsDataPartListEntry) {
            iPartsDataPartListEntry sourcePartListEntry = (iPartsDataPartListEntry)source;

            aggregateType = sourcePartListEntry.aggregateType;

            // Fußnoten
            footNotesLoaded = sourcePartListEntry.footNotesLoaded;
            if (sourcePartListEntry.footNotes != null) {
                footNotes = new ArrayList<>(sourcePartListEntry.footNotes);
            }

            // Werkseinsatzdaten AS OHNE Berücksichtigung von Ersetzungen
            factoryDataForRetailLoaded = sourcePartListEntry.factoryDataForRetailLoaded;
            if (sourcePartListEntry.factoryDataForRetailUnfiltered != null) {
                factoryDataForRetailUnfiltered = sourcePartListEntry.factoryDataForRetailUnfiltered.cloneMe();
            }
            clearFactoryDataForRetail();

            // Werkseinsatzdaten Konstruktion (DIALOG Datensätze)
            factoryDataForConstructionLoaded = sourcePartListEntry.factoryDataForConstructionLoaded;
            if (sourcePartListEntry.factoryDataForConstruction != null) {
                factoryDataForConstruction = sourcePartListEntry.factoryDataForConstruction.cloneMe();
            }

            // Zeitscheibe aufgrund aller Werkseinsatzdaten zu gültigen Werken kann nicht mehr geklont werden, da die
            // relevanten Werkseinsatzdaten seit DAIMLER-6018 von der Baumuster-Zeitscheiben-Filterung abhängig sind

            // Farbvarianten für Konstruktion
            colorTableConstructionLoaded = sourcePartListEntry.colorTableConstructionLoaded;
            if (sourcePartListEntry.colorTableForConstruction != null) {
                colorTableForConstruction = sourcePartListEntry.colorTableForConstruction.cloneMe();
            }

            // Farbvarianten
            colorTableLoaded = sourcePartListEntry.colorTableLoaded;
            if (sourcePartListEntry.colorTableForRetailWithoutFilter != null) {
                colorTableForRetailWithoutFilter = sourcePartListEntry.colorTableForRetailWithoutFilter.cloneMe();
            }

            // colorTableForRetail explizit nicht klonen -> wird immer aktiv gefiltert
            colorTableFiltered = false;
            colorTableForRetail = null;

            // Zusatzmaterialien (ES1, ES2)
            if (sourcePartListEntry.alternativeParts != null) {
                alternativeParts = new LinkedHashSet<>();
                for (EtkDataPart alternativePart : sourcePartListEntry.alternativeParts) {
                    alternativeParts.add(alternativePart.cloneMe(project));
                }
            }

            // Enthält die Stücklistenposition ein Farbteil?
            isColoredPart = sourcePartListEntry.isColoredPart;
            hasColorTablesUnfiltered = sourcePartListEntry.hasColorTablesUnfiltered;
            currentInternalText = sourcePartListEntry.currentInternalText;
            // Ersetzungen werden über iPartsDataAssembly.afterClonePartListEntriesInCache() geklont

            // Kombinierte Texte für PartsListWS auch mit klonen
            if (sourcePartListEntry.combinedMultiTextList != null) {
                combinedMultiTextList = new DwList<>(sourcePartListEntry.combinedMultiTextList);
            }
        }
    }

    @Override
    public EtkDataObject[] getObjectsForFilter() {
        EtkDataPart partForFilter = getPart();
        if (partForFilter.existsInDB()) {
            return new EtkDataObject[]{ this, partForFilter };
        } else { // Falls das Material nicht in der DB existiert, dann kann es auch nicht gefiltert werden
            return new EtkDataObject[]{ this };
        }
    }

    public boolean isColorTableLoaded() {
        return colorTableLoaded;
    }

    public boolean isFactoryDataForRetailLoaded() {
        return factoryDataForRetailLoaded;
    }

    public boolean isFootNotesLoaded() {
        return footNotesLoaded;
    }

    public boolean isReplacementsLoaded() {
        return replacementsLoaded;
    }

    /**
     * Setzt alle Flags, ob diverse Retail-Daten und optional auch Konstruktions-Daten wie z.B. Werkseinsatzdaten geladen
     * sind, auf {@code true} für virtuelle Stücklisteneinträge. Dadurch wird verhindert, dass diese Daten unnötig z.B. bei
     * der      m     der Stücklisten-Icons nachgeladen werden, was teilweise sogar die komplette Baugruppe nachladen würde.
     *
     * @param includeConstructionFlags
     */
    public void setDataLoadedFlagsForVirtualPLE(boolean includeConstructionFlags) {
        factoryDataForRetailLoaded = true;
        colorTableLoaded = true;
        replacementsLoaded = true;

        if (includeConstructionFlags) {
            factoryDataForConstructionLoaded = true;
            colorTableConstructionLoaded = true;
        }
    }

    /**
     * Übernimmt alle Ersetzungen (Retail und Konstruktion) vom übergebenen Quell-Stücklisteneintrag, wobei
     * die Vorgänger und Nachfolger in den Quell-Ersetzungen durch die korrespondierenden Stücklisteneinträge der übergebenen
     * {@code destPartListEntriesMap} ersetzt werden.
     *
     * @param source
     * @param destPartListEntriesMap
     */
    public void assignReplacements(iPartsDataPartListEntry source, Map<String, iPartsDataPartListEntry> destPartListEntriesMap) {
        // Retail-Ersetzungen
        if (source.predecessors != null) {
            predecessors = new DwList<>(source.predecessors.size());
            for (iPartsReplacement predecessor : source.predecessors) {
                predecessors.add(predecessor.cloneMe(destPartListEntriesMap));
            }
        } else {
            predecessors = null;
        }
        if (source.successors != null) {
            successors = new DwList<>(source.successors.size());
            for (iPartsReplacement successor : source.successors) {
                successors.add(successor.cloneMe(destPartListEntriesMap));
            }
        } else {
            successors = null;
        }
        replacementsLoaded = true;
        clearFilteredReplacements();

        // Konstruktions-Ersetzungen am Teilestamm
        if (source.predecessorsConst != null) {
            predecessorsConst = new DwList<>(source.predecessorsConst.size());
            for (iPartsReplacementConst predecessorConst : source.predecessorsConst) {
                predecessorsConst.add(predecessorConst.cloneMe(destPartListEntriesMap));
            }
        } else {
            predecessorsConst = null;
        }
        if (source.successorsConst != null) {
            successorsConst = new DwList<>(source.successorsConst.size());
            for (iPartsReplacementConst successorConst : source.successorsConst) {
                successorsConst.add(successorConst.cloneMe(destPartListEntriesMap));
            }
        } else {
            successorsConst = null;
        }
    }

    /**
     * Setzt den übergebenen kombinierten Text am angegebenen Stücklisteneintrag
     *
     * @param partListEntry
     * @param combinedText  Kann auch {@code null} sein und wird dann als leer gesetzt
     * @param project
     */
    public static void setCombinedText(EtkDataPartListEntry partListEntry, String combinedText, String addText, String neutralText, List<EtkMultiSprache> combinedMultiTextList, EtkProject project) {
        if (combinedText == null) {
            combinedText = "";
        }

        // Bei den virtuellen Materialtypen Überschrift und Zwischenüberschrift den kombinierten Text direkt als Materialtext setzen
        if (VirtualMaterialType.isPartListTextEntry(partListEntry)) {
            EtkMultiSprache multiLang = new EtkMultiSprache();
            multiLang.setText(Language.DE, combinedText);
            multiLang.fillAllLanguages(project.getConfig().getDatabaseLanguages(), Language.DE);
            partListEntry.getPart().initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
            partListEntry.getPart().setFieldValueAsMultiLanguage(FIELD_M_TEXTNR, multiLang, DBActionOrigin.FROM_DB);

            // virtuelles Feld RETAIL_COMB_TEXT dafür leer setzen
            partListEntry.getAttributes().addField(iPartsDataVirtualFieldsDefinition.RETAIL_COMB_TEXT, "", true, DBActionOrigin.FROM_DB);
            partListEntry.getAttributes().addField(iPartsDataVirtualFieldsDefinition.RETAIL_ADD_TEXT, "", true, DBActionOrigin.FROM_DB);
            partListEntry.getAttributes().addField(iPartsDataVirtualFieldsDefinition.RETAIL_TEXT_NEUTRAL, "", true, DBActionOrigin.FROM_DB);
        } else { // normaler Stücklisteneintrag -> kombinierten Text im virtuellen Feld RETAIL_COMB_TEXT setzen
            partListEntry.getAttributes().addField(iPartsDataVirtualFieldsDefinition.RETAIL_COMB_TEXT, combinedText, true, DBActionOrigin.FROM_DB);
            if (addText != null) {
                partListEntry.getAttributes().addField(iPartsDataVirtualFieldsDefinition.RETAIL_ADD_TEXT, addText, true, DBActionOrigin.FROM_DB);
            }
            if (neutralText != null) {
                partListEntry.getAttributes().addField(iPartsDataVirtualFieldsDefinition.RETAIL_TEXT_NEUTRAL, neutralText, true, DBActionOrigin.FROM_DB);
            }

            // Virtuelles Feld für "Ergänzungstext Quelle GenVO" muss neu berechnet werden
            partListEntry.getAttributes().deleteField(iPartsDataVirtualFieldsDefinition.RETAIL_COMB_TEXT_SOURCE_GENVO, false,
                                                      DBActionOrigin.FROM_DB);
        }
        if (partListEntry instanceof iPartsDataPartListEntry) {
            // Nur setzen, wenn das WS Plugin und der PartsList-Webservice oder das Export-Plugin aktiv sind
            if ((iPartsPlugin.isWebservicePluginActive() && de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.iPartsWebservicePlugin.isPartsListWSActive()) ||
                iPartsPlugin.isExportPluginActive()) {
                ((iPartsDataPartListEntry)partListEntry).setCombinedMultiTextList(combinedMultiTextList);
            }
        }
    }

    public boolean hasInternalText() {
        return StrUtils.isValid(currentInternalText);
    }

    public void setCurrentInternalText(String currentInternalText) {
        this.currentInternalText = currentInternalText;
        attributes.addField(iPartsDataVirtualFieldsDefinition.DIALOG_DD_INTERNAL_TEXT, StrUtils.getEmptyOrValidString(currentInternalText),
                            true, DBActionOrigin.FROM_DB);
    }

    public String getCurrentInternalText() {
        return currentInternalText;
    }

    /**
     * Zu verwenden wenn keine Baugruppe bekannt (z.B. Suche).
     *
     * @param project
     * @param catalogAttributes
     */
    public iPartsDataPartListEntry(EtkProject project, DBDataObjectAttributes catalogAttributes) {
        super(project, catalogAttributes);
    }

    /**
     * Zu verwenden wenn Baugruppe bekannt (typisch Stückliste).
     *
     * @param project
     * @param ownerAssembly
     * @param catalogAttributes
     */
    public iPartsDataPartListEntry(EtkProject project, EtkDataAssembly ownerAssembly, DBDataObjectAttributes catalogAttributes) {
        super(project, ownerAssembly, catalogAttributes);
    }

    public iPartsDataPartListEntry(EtkProject project, PartListEntryId id) {
        super(project, id);
    }

    @Override
    protected DBDataObjectAttributes internalLoad(IdWithType id, String[] resultFields) {
        if (!(id instanceof PartListEntryId)) {
            throw new RuntimeException("iPartsDataPartListEntry.internalLoad(): ID must be an instance of PartListEntryId");
        }

        PartListEntryId entryId = (PartListEntryId)id;
        if (!new iPartsAssemblyId(entryId.getKVari(), entryId.getKVer()).isVirtual()) {
            if (resultFields != null) {
                // alle virtuellen Felder aus resultFields entfernen, da nicht-virtuelle Baugruppen damit nichts anfangen können
                List<String> nonVirtualFields = new ArrayList<>(resultFields.length);
                for (String field : resultFields) {
                    if (!VirtualFieldsUtils.isVirtualField(field)) {
                        nonVirtualFields.add(field);
                    } else {
                        Logger.log(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, "Virtual field \"" + field
                                                                                  + "\" can't be created since the part list entry \""
                                                                                  + id.toString(", ") + "\" to be loaded is not virtual");
                    }
                }
                resultFields = nonVirtualFields.toArray(new String[nonVirtualFields.size()]);
            }

            return super.internalLoad(id, resultFields);
        } else if (entryId.isValidIdWithkLfdnrNotNull()) {
            // ohne gültige ID (inkl. kLfdnr) kann der Stücklisteneintrag nicht in der ParentAssembly gefunden werden
            // -> ParentAssembly gar nicht erst laden (ungültige IDs werden z.B. bei der Verwendung erzeugt, wo kLfdnr leer ist)

            // Nachladen eines Entrys sollte eigentlich nur selten vorkommen. Beim Laden der Baugruppe werden alle virtuellen
            // Einträge gesetzt und müssen nicht nachgeladen werden.
            // Es kann aber sein, dass Funktionen direkt den Datensatz mit Vari, Ver, LfdNr holen.
            // In diesem Fall muss hier die Parent-Baugruppe komplett geladen und die laufende Nummer rausgesucht werden.
            // Der Grund ist, dass die laufende Nummer auch virtuell ist und es keine Möglichkeit gibt, über die Nummer
            // zum Datensatz zu kommen.
            // Die laufende Nummer ist bei den virtuellen Baugruppen wirklich nur eine laufende Nummer und der Wert kann
            // nur durch komplettes Neuladen ermittelt werden.

            // Parent laden
            EtkDataAssembly parentAssembly = EtkDataObjectFactory.createDataAssembly(getEtkProject(), entryId.getOwnerAssemblyId());

            EtkEbenenDaten tempEbenenFields = null;

            // Die gewünschten Felder als Felder in der Ebene anlegen, damit die auch geladen werden
            Set<String> virtualFields = null;
            if ((resultFields != null) && (resultFields.length > 0)) {
                tempEbenenFields = new EtkEbenenDaten();
                for (String field : resultFields) {
                    EtkDisplayField newField = new EtkDisplayField(TABLE_KATALOG, field, getEtkProject().getConfig());
                    tempEbenenFields.addFeld(newField);
                    String fieldName = newField.getKey().getFieldName();
                    if (VirtualFieldsUtils.isVirtualField(fieldName)) {
                        if (virtualFields == null) {
                            virtualFields = new HashSet<>();
                        }
                        virtualFields.add(fieldName);
                    }
                }
            }

            for (EtkDataPartListEntry entry : parentAssembly.getPartListUnfiltered(tempEbenenFields)) {
                if (entry.getAsId().equals(entryId) && entry.isLoaded()) {
                    // Entry gefunden -> Werte übertragen
                    this.assign(null, entry, DBActionOrigin.FROM_DB);
                    break;
                }
            }

            // Überprüfen, ob alle virtuellen Felder geladen wurden
            if ((virtualFields != null) && (attributes != null)) {
                for (String virtualField : virtualFields) {
                    if (!attributes.fieldExists(virtualField)) {
                        Logger.log(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, "Virtual field \"" + virtualField
                                                                                  + "\" not found after loading the part list entry \""
                                                                                  + id.toString(", ") + "\"");
                    }
                }
            }

            return attributes;
        } else {
            return attributes;
        }
    }

    @Override
    protected boolean tryToLoadFieldIfNeededFromAssembly(String attributeName) {
        // Ausnahme: Attribut EDS/MBS_LEVEL, was im Filter verwendet wird, aber nur die virtuelle EDS/MBS-Stückliste hat
        return !attributeName.equals(iPartsDataVirtualFieldsDefinition.EDS_LEVEL) && !attributeName.equals(iPartsDataVirtualFieldsDefinition.MBS_LEVEL);
    }

    @Override
    public boolean loadVirtualField(String attributeName) {
        if (attributeName.equals(iPartsDataVirtualFieldsDefinition.RETAIL_COMB_TEXT)
            || attributeName.equals(iPartsDataVirtualFieldsDefinition.RETAIL_ADD_TEXT)
            || attributeName.equals(iPartsDataVirtualFieldsDefinition.RETAIL_TEXT_NEUTRAL)) { // kombinierter Text
            String neutralTextForPart = null; // sprachneutraler Text am Material
            EtkMultiSprache addTextMultiLang = getPart().getFieldValueAsMultiLanguage(iPartsConst.FIELD_M_ADDTEXT);
            if (addTextMultiLang != null) {
                neutralTextForPart = addTextMultiLang.getText("DE"); // Sprache egal
            }

            // Wir erzeugen eine Map mit nur einem Eintrag für die aktuelle k_lfdnr damit wir Methode für Bestimmung per Modul verwenden können
            Map<String, String> neutralTextFromPartsForModule = new HashMap<String, String>();
            neutralTextFromPartsForModule.put(getAsId().getKLfdnr(), neutralTextForPart);
            iPartsDataCombTextList combTextsForEntry = iPartsDataCombTextList.loadForPartListEntry(getAsId(), getEtkProject());
            calculateAndSetCombinedText(combTextsForEntry, neutralTextFromPartsForModule, !attributeName.equals(iPartsDataVirtualFieldsDefinition.RETAIL_COMB_TEXT));
            return false;
        } else if (attributeName.equals(iPartsDataVirtualFieldsDefinition.RETAIL_COMB_TEXT_SOURCE_GENVO)) { // Ergänzungstext Quelle GenVO
            iPartsDataCombTextList dataCombTextList = null;
            if (getOwnerAssembly().getEbeneName().equals(iPartsConst.PARTS_LIST_TYPE_DIALOG_RETAIL)) {
                dataCombTextList = iPartsDataCombTextList.loadForPartListEntry(getAsId(), getEtkProject());
            }
            updateCombTextSourceGenVO(dataCombTextList); // Setzt das virtuelle Feld RETAIL_COMB_TEXT_SOURCE_GENVO
            return false;
        } else if (attributeName.equals(iPartsDataVirtualFieldsDefinition.RETAIL_CODES_REDUCED) || attributeName.equals(iPartsDataVirtualFieldsDefinition.RETAIL_CODES_FILTERED)) {
            calculateRetailCodesReducedAndFiltered(iPartsFilter.get());
            return false;
        } else if (attributeName.equals(iPartsDataVirtualFieldsDefinition.RETAIL_CODES_WITH_EVENTS)) {
            String resultCodes = getFieldValue(iPartsDataVirtualFieldsDefinition.RETAIL_CODES_FILTERED); // Basis ist die reduzierte Code-Regel
            iPartsSeriesId seriesId = getSeriesId();
            if (seriesId != null) {
                EtkProject project = getEtkProject();
                iPartsDialogSeries series = iPartsDialogSeries.getInstance(project, seriesId);
                if (series.isEventTriggered()) { // Code-Regeln der Ereignisse hinzufügen bei ereignisgesteuerten Baureihen
                    resultCodes = DaimlerCodes.addEventsCodes(resultCodes, series.getEvent(getFieldValue(iPartsConst.FIELD_K_EVENT_FROM)),
                                                              series.getEvent(getFieldValue(iPartsConst.FIELD_K_EVENT_TO)),
                                                              project);
                }
            }
            attributes.addField(attributeName, resultCodes, true, DBActionOrigin.FROM_DB);
            return false;
        } else if (attributeName.equals(iPartsDataVirtualFieldsDefinition.RETAIL_EVENT_TITLE_FROM) || attributeName.equals(iPartsDataVirtualFieldsDefinition.RETAIL_EVENT_TITLE_TO)) {
            // Event-ID bestimmen
            String eventIdFieldName;
            if (attributeName.equals(iPartsDataVirtualFieldsDefinition.RETAIL_EVENT_TITLE_FROM)) {
                eventIdFieldName = iPartsConst.FIELD_K_EVENT_FROM;
            } else {
                eventIdFieldName = iPartsConst.FIELD_K_EVENT_TO;
            }
            String eventId = getFieldValue(eventIdFieldName);

            String eventTitle = ""; // falls es keine Retail-Stückliste ist, das virtuelle Feld leer befüllen, da das Feld trotzdem angefordert wurde
            if (!eventId.isEmpty() && getOwnerAssembly().isRetailPartList()) {
                // Ereignis-Benennung über die Baureihe bestimmen
                iPartsSeriesId seriesId = getSeriesId();
                if (seriesId != null) {
                    EtkProject etkProject = getEtkProject();
                    iPartsEvent event = iPartsDialogSeries.getInstance(etkProject, seriesId).getEvent(eventId);
                    if (event != null) {
                        eventTitle = event.getTitle().getTextByNearestLanguage(etkProject.getDBLanguage(), etkProject.getDataBaseFallbackLanguages());
                    }
                }
            }
            attributes.addField(attributeName, eventTitle, true, DBActionOrigin.FROM_DB);
            return false;
        } else if (attributeName.equals(iPartsDataVirtualFieldsDefinition.RETAIL_EVENT_CODE_FROM) || attributeName.equals(iPartsDataVirtualFieldsDefinition.RETAIL_EVENT_CODE_TO)) {
            // Event-ID bestimmen
            String eventIdFieldName;
            if (attributeName.equals(iPartsDataVirtualFieldsDefinition.RETAIL_EVENT_CODE_FROM)) {
                eventIdFieldName = iPartsConst.FIELD_K_EVENT_FROM;
            } else {
                eventIdFieldName = iPartsConst.FIELD_K_EVENT_TO;
            }
            String eventId = getFieldValue(eventIdFieldName);

            String eventCode = ""; // falls es keine Retail-Stückliste ist, das virtuelle Feld leer befüllen, da das Feld trotzdem angefordert wurde
            if (!eventId.isEmpty() && getOwnerAssembly().isRetailPartList()) {
                // Ereignis-Benennung über die Baureihe bestimmen
                iPartsSeriesId seriesId = getSeriesId();
                if (seriesId != null) {
                    EtkProject etkProject = getEtkProject();
                    iPartsEvent event = iPartsDialogSeries.getInstance(etkProject, seriesId).getEvent(eventId);
                    if (event != null) {
                        eventCode = event.getCode();
                    }
                }
            }
            attributes.addField(attributeName, eventCode, true, DBActionOrigin.FROM_DB);
            return false;
        } else if (attributeName.equals(iPartsDataVirtualFieldsDefinition.RETAIL_EVAL_PEM_FROM_CALCULATED)
                   || attributeName.equals(iPartsDataVirtualFieldsDefinition.RETAIL_EVAL_PEM_TO_CALCULATED)) {
            updatePEMFlagsFromReplacements();
            return false;
        } else if (attributeName.equals(iPartsDataVirtualFieldsDefinition.K_GENVO_TEXT)) { // GenVO-Text
            String genVoText = null;
            iPartsDataAssembly iPartsOwnerAssembly = getOwnerAssembly();
            if (iPartsOwnerAssembly.getDocumentationType().isPKWDocumentationType() || iPartsOwnerAssembly.isDialogSMConstructionAssembly()) {
                String genVoNumber = getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_GENVO);
                if (StrUtils.isValid(genVoNumber)) {
                    genVoText = iPartsGenVoTextsCache.getInstance(getEtkProject()).getGenVoText(genVoNumber, getEtkProject());
                }
            }
            attributes.addField(attributeName, StrUtils.getEmptyOrValidString(genVoText), true, DBActionOrigin.FROM_DB);
            return false;
        } else if (attributeName.startsWith(VirtualFieldsUtils.VIRTUAL_FIELD_MARKER_PREFIX + iPartsDataVirtualFieldsDefinition.DIALOG
                                            + iPartsDataVirtualFieldsDefinition.VIRTFIELD_SPACER)) {
            iPartsDataAssembly ownerAssembly = getOwnerAssembly();
            String partListType = ownerAssembly.getEbeneName();

            // Virtuelle Felder für die Auswertung von DIALOG-Stücklisten leer hinzufügen, da diese in Hintergrund-Threads
            // berechnet und befüllt werden
            if (attributeName.equals(iPartsDataVirtualFieldsDefinition.DIALOG_DD_OPEN_ENTRIES) || attributeName.equals(iPartsDataVirtualFieldsDefinition.DIALOG_DD_CHANGED_ENTRIES)
                || attributeName.equals(iPartsDataVirtualFieldsDefinition.DIALOG_DD_CALCULATION_DATE)) {
                // Bei DIALOG Baureihen für HM/M/SM sowie HM- und M-Knoten die Stückliste regulär laden und darüber die
                // virtuellen Felder bestimmen
                if ((partListType.equals(iPartsConst.PARTS_LIST_TYPE_SERIES) && ownerAssembly.isHmHSmConstructionAssembly())
                    || partListType.equals(iPartsConst.PARTS_LIST_TYPE_DIALOG_HM) || partListType.equals(iPartsConst.PARTS_LIST_TYPE_DIALOG_M)) {
                    loadFromDB(getAsId(), attributeName);
                    return true;
                } else { // falls es keine passende DIALOG-Stückliste ist, das virtuelle Feld leer befüllen, da das Feld trotzdem angefordert wurde
                    attributes.addField(attributeName, "", true, DBActionOrigin.FROM_DB);
                    return false;
                }
            } else if (attributeName.equals(iPartsDataVirtualFieldsDefinition.DIALOG_DD_GENVO) ||
                       attributeName.equals(iPartsDataVirtualFieldsDefinition.DIALOG_DD_SPLITSIGN)) {
                if (partListType.equals(iPartsConst.PARTS_LIST_TYPE_DIALOG_SM) || ownerAssembly.getDocumentationType().isPKWDocumentationType()) { // Macht nur bei DIALOG-Stücklisten Sinn
                    attributes.addFields(ownerAssembly.getGenInstallLocationAttributes(this), DBActionOrigin.FROM_DB);
                } else { // falls es keine passende DIALOG-Stückliste ist, das virtuelle Feld leer befüllen, da das Feld trotzdem angefordert wurde
                    attributes.addField(attributeName, "", true, DBActionOrigin.FROM_DB);
                }
                return false;
            } else if (attributeName.equals(iPartsDataVirtualFieldsDefinition.DD_AUTHOR_ORDER)) {
                // Feld für Autorenauftrag leer befüllen, wenn es lazy angefordert wird. Normalerweise wird das Feld beim
                // Aufbau der Stückliste befüllt.
                attributes.addField(attributeName, iPartsVirtualAssemblyDialogBase.NO_ACTIVE_AUTHOR_ORDER_NAME, true, DBActionOrigin.FROM_DB);
                return false;
            } else if (attributeName.equals(iPartsDataVirtualFieldsDefinition.DD_AUTHOR_ORDER_AFFILIATION)) {
                // Feld für Zugehörigkeit zu einem AA mit "NONE" befüllen, wenn es lazy angefordert wird. Normalerweise wird das Feld beim
                // Aufbau der Stückliste befüllt.
                attributes.addField(attributeName, AOAffiliationForDIALOGEntry.NONE.getTextValue(), true, DBActionOrigin.FROM_DB);
                return false;
            } else if (attributeName.equals(iPartsDataVirtualFieldsDefinition.DIALOG_DD_INTERNAL_TEXT)) {
                String internalText = "";
                if (hasInternalText()) {
                    internalText = getCurrentInternalText();
                }
                attributes.addField(attributeName, internalText, true, DBActionOrigin.FROM_DB);
                return false;
            } else if (attributeName.equals(iPartsDataVirtualFieldsDefinition.DIALOG_DD_RETAILUSE)) {
                String retailAssigned = ""; // Kein Icon für Nicht-DIALOG-SM-Stücklisten
                if (partListType.equals(iPartsConst.PARTS_LIST_TYPE_DIALOG_SM)) { // Macht nur bei DIALOG-SM-Stücklisten Sinn
                    // Retail-Verwendung prüfen (macht nur bei vorhandener DIALOG-GUID Sinn)
                    String dialogGUID = getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_GUID);
                    if (!dialogGUID.isEmpty()) {
                        DBDataObjectAttributesList retailUsages = EditConstructionToRetailHelper.getRetailSourceGuidAttributeList(iPartsEntrySourceType.DIALOG,
                                                                                                                                  dialogGUID,
                                                                                                                                  null,
                                                                                                                                  new String[]{ FIELD_K_VARI },
                                                                                                                                  getEtkProject());
                        retailAssigned = !retailUsages.isEmpty() ? RETAIL_ASSIGNED : RETAIL_NOT_ASSIGNED;
                    } else {
                        retailAssigned = RETAIL_NOT_ASSIGNED;
                    }
                }
                attributes.addField(attributeName, retailAssigned, true, DBActionOrigin.FROM_DB);
                return false;
            } else if (attributeName.startsWith(VirtualFieldsUtils.VIRTUAL_FIELD_MARKER_PREFIX + iPartsDataVirtualFieldsDefinition.DIALOG_DD_BCTG_PREFIX)) {
                String bctgGenericPartNo = "";
                String bctgSolutuion = "";
                String bctgVariantNo = "";
                iPartsDialogBCTEPrimaryKey primaryKey = iPartsDialogBCTEPrimaryKey.createFromDIALOGPartListEntry(this);
                if (primaryKey != null) {
                    List<iPartsDataGenericPart> validGenericParts =
                            iPartsDataGenericPartList.loadFilterAndSortGenericPartDataForBCTEKeyFromDB(getEtkProject(), primaryKey,
                                                                                                       getSDATA(), getSDATB());
                    if ((validGenericParts != null) && !validGenericParts.isEmpty()) {
                        iPartsDataGenericPart selectedGenericPart = validGenericParts.get(0);
                        bctgGenericPartNo = selectedGenericPart.getFieldValue(FIELD_DGP_GENERIC_PARTNO);
                        bctgSolutuion = selectedGenericPart.getFieldValue(FIELD_DGP_SOLUTION);
                        bctgVariantNo = selectedGenericPart.getFieldValue(FIELD_DGP_VARIANTNO);
                    }
                }

                // Die virtuellen Felder immer (im Zweifelsfall leer) hinzufügen
                attributes.addField(iPartsDataVirtualFieldsDefinition.DIALOG_DD_BCTG_GENERIC_PARTNO, bctgGenericPartNo, true, DBActionOrigin.FROM_DB);
                attributes.addField(iPartsDataVirtualFieldsDefinition.DIALOG_DD_BCTG_SOLUTION, bctgSolutuion, true, DBActionOrigin.FROM_DB);
                attributes.addField(iPartsDataVirtualFieldsDefinition.DIALOG_DD_BCTG_VARIANTNO, bctgVariantNo, true, DBActionOrigin.FROM_DB);
                return false;
            } else if (attributeName.equals(iPartsDataVirtualFieldsDefinition.DIALOG_DD_WITHOUT_USAGE)) {
                // Ohne Verwendung
                boolean withoutUsage = false;
                if (getEtkProject().getEtkDbs().isRevisionChangeSetActive()) {
                    // Wenn ein DIALOG-Stücklisteneintrag keine Retail-Verwendung mehr hat und im aktiven ChangeSet
                    // ein Retail-Stücklisteneintrag mit passender DIALOG-GUID gelöscht wurde, dann ist der
                    // DIALOG-Stücklisteneintrag "ohne Verwendung"
                    if (getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_RETAILUSE).equals(RETAIL_NOT_ASSIGNED)) {
                        iPartsDialogBCTEPrimaryKey primaryKey = iPartsDialogBCTEPrimaryKey.createFromDIALOGPartListEntry(this);
                        if (primaryKey != null) {
                            Set<String> deletedDialogGUIDsInRetail = iPartsVirtualAssemblyDialogBase.getDeletedDialogGUIDsInRetailForActiveChangeSet(primaryKey.getHmMSmId(),
                                                                                                                                                     getEtkProject());
                            if (deletedDialogGUIDsInRetail.contains(primaryKey.createDialogGUID())) {
                                withoutUsage = true;
                            }
                        }
                    }
                }
                attributes.addField(iPartsDataVirtualFieldsDefinition.DIALOG_DD_WITHOUT_USAGE, SQLStringConvert.booleanToPPString(withoutUsage),
                                    true, DBActionOrigin.FROM_DB);
                return false;
            }

            // Diese virtuellen Felder für DIALOG können nur in den DIALOG-Konstruktions-Stücklisten oder DIALOG-Retail-Stücklisten
            // sinnvolle Ergebnisse liefern. Bei den anderen Stücklisten können wir aus Performancegründen das Laden bleiben lassen.
            if (partListType.equals(iPartsConst.PARTS_LIST_TYPE_DIALOG_SM) || partListType.equals(iPartsConst.PARTS_LIST_TYPE_EINPAS_TU_DIALOG)
                || partListType.equals(iPartsConst.PARTS_LIST_TYPE_DIALOG_RETAIL)) {
                String attributeValue = null;
                // Diese virtuellen Felder können aus dem BCTE-Schlüssel berechnet werden (bzw. sind der BCTE-Schlüssel)
                if (attributeName.equals(iPartsDataVirtualFieldsDefinition.DIALOG_DD_GUID)) {
                    attributeValue = iPartsDialogBCTEPrimaryKey.getDialogGUID(this);
                } else if (attributeName.equals(iPartsDataVirtualFieldsDefinition.DIALOG_DD_SERIES_NO) || attributeName.equals(iPartsDataVirtualFieldsDefinition.DIALOG_DD_HM) ||
                           attributeName.equals(iPartsDataVirtualFieldsDefinition.DIALOG_DD_M) || attributeName.equals(iPartsDataVirtualFieldsDefinition.DIALOG_DD_SM) ||
                           attributeName.equals(iPartsDataVirtualFieldsDefinition.DIALOG_DD_POSE) || attributeName.equals(iPartsDataVirtualFieldsDefinition.DIALOG_DD_POSV) ||
                           attributeName.equals(iPartsDataVirtualFieldsDefinition.DIALOG_DD_WW) || attributeName.equals(iPartsDataVirtualFieldsDefinition.DIALOG_DD_ETZ) ||
                           attributeName.equals(iPartsDataVirtualFieldsDefinition.DIALOG_DD_AA) || attributeName.equals(iPartsDataVirtualFieldsDefinition.DIALOG_DD_SDATA)) {
                    String bcteKeyValue = "";
                    iPartsDialogBCTEPrimaryKey primaryKey = iPartsDialogBCTEPrimaryKey.createFromDIALOGPartListEntry(this);
                    if (primaryKey != null) {
                        if (attributeName.equals(iPartsDataVirtualFieldsDefinition.DIALOG_DD_SERIES_NO)) {
                            bcteKeyValue = primaryKey.getHmMSmId().getSeries();
                        } else if (attributeName.equals(iPartsDataVirtualFieldsDefinition.DIALOG_DD_HM)) {
                            bcteKeyValue = primaryKey.getHmMSmId().getHm();
                        } else if (attributeName.equals(iPartsDataVirtualFieldsDefinition.DIALOG_DD_M)) {
                            bcteKeyValue = primaryKey.getHmMSmId().getM();
                        } else if (attributeName.equals(iPartsDataVirtualFieldsDefinition.DIALOG_DD_SM)) {
                            bcteKeyValue = primaryKey.getHmMSmId().getSm();
                        } else if (attributeName.equals(iPartsDataVirtualFieldsDefinition.DIALOG_DD_POSE)) {
                            bcteKeyValue = primaryKey.getPosE();
                        } else if (attributeName.equals(iPartsDataVirtualFieldsDefinition.DIALOG_DD_POSV)) {
                            bcteKeyValue = primaryKey.getPosV();
                        } else if (attributeName.equals(iPartsDataVirtualFieldsDefinition.DIALOG_DD_WW)) {
                            bcteKeyValue = primaryKey.getWW();
                        } else if (attributeName.equals(iPartsDataVirtualFieldsDefinition.DIALOG_DD_ETZ)) {
                            bcteKeyValue = primaryKey.getET();
                        } else if (attributeName.equals(iPartsDataVirtualFieldsDefinition.DIALOG_DD_AA)) {
                            bcteKeyValue = primaryKey.getAA();
                        } else if (attributeName.equals(iPartsDataVirtualFieldsDefinition.DIALOG_DD_SDATA)) {
                            bcteKeyValue = primaryKey.getSData();
                        }
                    }

                    attributeValue = bcteKeyValue;
                } else { // Anderes virtuelles DIALOG-Feld -> Versuch, dieses über das Mapping des virtuellen Feldes zu laden
                    String dialogGUID = iPartsDialogBCTEPrimaryKey.getDialogGUID(this);
                    if (StrUtils.isValid(dialogGUID)) {
                        VirtualFieldDefinition virtualField = iPartsDataVirtualFieldsDefinition.findField(TABLE_KATALOG, attributeName);
                        if (virtualField != null) {
                            String sourceTable = virtualField.getSourceTable();
                            if (Utils.objectEquals(sourceTable, TABLE_DA_DIALOG)) {
                                // Für bessere Performance gleich alle virtuellen Felder aus der Tabelle DA_DIALOG laden,
                                // um mehrere DB-Zugriffe zu vermeiden
                                List<VirtualFieldDefinition> virtualFields = iPartsDataVirtualFieldsDefinition.getMapping(sourceTable,
                                                                                                                          TABLE_KATALOG);
                                Map<String, String> dialogFieldsMap = new HashMap<>();
                                for (VirtualFieldDefinition field : virtualFields) {
                                    dialogFieldsMap.put(field.getSourceFieldName(), field.getVirtualFieldName());
                                }

                                DBDataObjectAttributes dialogAttributes = getEtkProject().getEtkDbs().getAttributes(TABLE_DA_DIALOG,
                                                                                                                    ArrayUtil.toArray(dialogFieldsMap.keySet()),
                                                                                                                    new String[]{ FIELD_DD_GUID },
                                                                                                                    new String[]{ dialogGUID });
                                if (dialogAttributes != null) {
                                    for (Map.Entry<String, DBDataObjectAttribute> dialogAttributeEntry : dialogAttributes.entrySet()) {
                                        String destFieldName = dialogFieldsMap.get(dialogAttributeEntry.getKey());
                                        if (destFieldName != null) {
                                            attributes.addField(destFieldName, dialogAttributeEntry.getValue().getAsString(),
                                                                true, DBActionOrigin.FROM_DB);
                                        }
                                    }
                                } else {
                                    // DIALOG-Datensatz wurde nicht gefunden -> alle DIALOG-Felder leer hinzufügen
                                    for (String destFieldName : dialogFieldsMap.values()) {
                                        attributes.addField(destFieldName, "", true, DBActionOrigin.FROM_DB);
                                    }
                                }

                                return false;
                            }
                        }
                    } else {
                        // Ohne DIALOG-GUID das virtuelle Feld einfach leer lassen, weil es sich nicht um einen gültigen
                        // DIALOG-Datensatz handeln kann
                        attributeValue = "";
                    }
                }

                if (attributeValue != null) {
                    attributes.addField(attributeName, attributeValue, true, DBActionOrigin.FROM_DB);
                    return false;
                }
            }

            // Die virtuellen Felder für DIALOG können nur in den DIALOG-Konstruktions-Stücklisten sinnvolle Ergebnisse liefern.
            // Bei den anderen Stücklisten können wir aus Performancegründen das Laden bleiben lassen.
            if (partListType.equals(iPartsConst.PARTS_LIST_TYPE_DIALOG_SM) || partListType.equals(iPartsConst.PARTS_LIST_TYPE_EINPAS_TU_DIALOG)) {
                // Nachladen der DIALOG-Stückliste ist z.B. aus der Suche heraus notwendig -> auch hier false zurückliefern,
                // weil es kein vermeidbares Performance-Problem darstellt
                if (VIRTUAL_FIELDS_LOADED_BY_ASSEMBLY.contains(attributeName)) {
                    // Bei den Attributen aus VIRTUAL_FIELDS_LOADED_BY_ASSEMBLY muss die ownerAssembly mit dem gewünschten
                    // Attribut neu geladen und der Attributwert von dem Stücklisteneintrag mit der entsprechenden lfdNr
                    // übernommen werden
                    EtkEbenenDaten partsListType = new EtkEbenenDaten();
                    partsListType.addFelder(ownerAssembly.getEbene());
                    for (String virtualFieldName : VIRTUAL_FIELDS_LOADED_BY_ASSEMBLY) {
                        partsListType.addFeld(new EtkDisplayField(TABLE_KATALOG, virtualFieldName, false, false));
                    }
                    DBDataObjectList<EtkDataPartListEntry> partsList = ownerAssembly.getPartListUnfiltered(partsListType,
                                                                                                           false, false);
                    for (EtkDataPartListEntry partListEntry : partsList) {
                        if (partListEntry.getAsId().getKLfdnr().equals(getAsId().getKLfdnr())) {
                            // Gleich alle entsprechenden Attribute aus VIRTUAL_FIELDS_LOADED_BY_ASSEMBLY setzen, um einen
                            // mehrfachen Aufruf von loadVirtualField() zu vermeiden
                            for (String virtualFieldName : VIRTUAL_FIELDS_LOADED_BY_ASSEMBLY) {
                                attributes.addField(virtualFieldName, partListEntry.getFieldValue(virtualFieldName), true,
                                                    DBActionOrigin.FROM_DB);
                            }
                            break;
                        }
                    }
                } else {
                    loadFromDB(getAsId(), attributeName);
                }
            } else { // falls es keine DIALOG-Stückliste ist, das virtuelle Feld leer befüllen, da das Feld trotzdem angefordert wurde
                attributes.addField(attributeName, "", true, DBActionOrigin.FROM_DB);
            }
            return false;
        } else if (attributeName.startsWith(VirtualFieldsUtils.VIRTUAL_FIELD_MARKER_PREFIX + iPartsEdsStructureHelper.getInstance().getVirtualFieldPrefix()
                                            + iPartsDataVirtualFieldsDefinition.VIRTFIELD_SPACER)) {
            // Die virtuellen Felder für OPS_SCOPE können nur in den OPS-SCOPE-Konstruktions-Stücklisten sinnvolle Ergebnisse liefern.
            // Bei den anderen Stücklisten können wir aus Performancegründen das Laden bleiben lassen.
            EtkDataAssembly parentAssembly = EtkDataObjectFactory.createDataAssembly(getEtkProject(), getAsId().getOwnerAssemblyId());
            String partListType = parentAssembly.getEbeneName();
            String fieldValue = null;
            if (iPartsEdsStructureHelper.isEdsStructurePartListType(partListType)) {
                // Die SAA/BK-Nummer ist in der virtuellen AssemblyId der Ziel-Stückliste enthalten
                String saaBKNumber = iPartsVirtualNode.getSaaBKNumberFromAssemblyId(getDestinationAssemblyId());
                if (StrUtils.isValid(saaBKNumber)) {
                    if (iPartsEdsStructureHelper.isSaaBkDescAttributeForLowerStructure(attributeName)) { // SAA/BK-Benennung (AS)
                        if (saaBKNumber.startsWith(SAA_NUMBER_PREFIX) || saaBKNumber.startsWith(BASE_LIST_NUMBER_PREFIX)) { // SAA bzw. Grundstückliste
                            iPartsDataSaa dataSaa = new iPartsDataSaa(getEtkProject(), new iPartsSaaId(saaBKNumber));
                            if (dataSaa.existsInDB()) {
                                fieldValue = dataSaa.getFieldValue(FIELD_DS_DESC, getEtkProject().getDBLanguage(), true);
                            }
                        } else { // Baukasten
                            EtkDataPart dataPart = EtkDataObjectFactory.createDataPart(getEtkProject(), new PartId(saaBKNumber, ""));
                            if (dataPart.existsInDB()) {
                                fieldValue = dataPart.getFieldValue(FIELD_M_TEXTNR, getEtkProject().getDBLanguage(), true);
                            }
                        }
                        if (fieldValue == null) {
                            fieldValue = ""; // SAA bzw. Material vom Baukasten wurde nicht gefunden
                        }
                    } else {
                        EtkDataObject newestData = null;
                        String modelNumber = iPartsVirtualNode.getModelNumberFromAssemblyId(getOwnerAssemblyId());
                        iPartsEdsStructureHelper structureHelper = iPartsEdsStructureHelper.getInstance();
                        if (StrUtils.isValid(modelNumber)) {
                            HierarchicalIDWithType structureId = structureHelper.createStructureIdFromOwnerAssemblyId(getOwnerAssemblyId());
                            if (structureId != null) {
                                EtkDataObjectList<? extends EtkDataObject> dataList
                                        = structureHelper.loadAllStructureEntriesForModelAndSubElement(getEtkProject(),
                                                                                                       modelNumber,
                                                                                                       structureId,
                                                                                                       saaBKNumber);
                                if (Utils.isValid(dataList)) {
                                    // Neuesten Datensatz für Baumuster und OPS-Scope bestimmen
                                    for (EtkDataObject currentData : dataList) {
                                        boolean isNewestData = false;
                                        if (newestData == null) {
                                            isNewestData = true;
                                        } else if (StrUtils.strToIntDef(currentData.getFieldValue(structureHelper.getRevisionFromField()), 0) >
                                                   StrUtils.strToIntDef(newestData.getFieldValue(structureHelper.getRevisionFromField()), 0)) {
                                            isNewestData = true;
                                        }

                                        if (isNewestData) {
                                            newestData = currentData;
                                        }
                                    }
                                }
                            }
                        }

                        // Alle virtuellen Felder für den unteren Strukturknoten mit den Daten aus dem neusten Datensatz befüllen (sofern vorhanden)
                        List<VirtualFieldDefinition> virtualFieldDefinitions = iPartsDataVirtualFieldsDefinition.getMapping(structureHelper.getStructureTableName(),
                                                                                                                            TABLE_KATALOG);
                        for (VirtualFieldDefinition virtualFieldDefinition : virtualFieldDefinitions) {
                            if (virtualFieldDefinition.getVirtualFieldName().startsWith(VirtualFieldsUtils.VIRTUAL_FIELD_MARKER_PREFIX
                                                                                        + structureHelper.getVirtualFieldPrefix()
                                                                                        + iPartsDataVirtualFieldsDefinition.VIRTFIELD_SPACER)) {
                                String virtualFieldValue;
                                if (newestData != null) {
                                    virtualFieldValue = newestData.getFieldValue(virtualFieldDefinition.getSourceFieldName());
                                } else {
                                    virtualFieldValue = "";
                                }
                                attributes.addField(virtualFieldDefinition.getVirtualFieldName(), virtualFieldValue, true, DBActionOrigin.FROM_DB);
                            }
                        }
                    }
                } else {
                    fieldValue = ""; // Es konnte keine SAA/BK-Nummer bestimmt werden
                }
            } else {
                // falls es keine EDS-Stückliste ist, das virtuelle Feld leer befüllen, da das Feld trotzdem angefordert wurde
                fieldValue = "";
            }
            if (fieldValue != null) {
                attributes.addField(attributeName, fieldValue, true, DBActionOrigin.FROM_DB);
            }
            return false;
        } else if (attributeName.startsWith(VirtualFieldsUtils.VIRTUAL_FIELD_MARKER_PREFIX + iPartsDataVirtualFieldsDefinition.EDS
                                            + iPartsDataVirtualFieldsDefinition.VIRTFIELD_SPACER)) {
            // Die virtuellen Felder für EDS können nur in den EDS-Konstruktions-Stücklisten sinnvolle Ergebnisse liefern.
            // Bei den anderen Stücklisten können wir aus Performancegründen das Laden bleiben lassen.
            EtkDataAssembly parentAssembly = EtkDataObjectFactory.createDataAssembly(getEtkProject(), getAsId().getOwnerAssemblyId());
            String partListType = parentAssembly.getEbeneName();
            if (partListType.equals(iPartsConst.PARTS_LIST_TYPE_EDS_SAA) || partListType.equals(iPartsConst.PARTS_LIST_TYPE_CTT_SAA)) {
                if (attributeName.equals(iPartsDataVirtualFieldsDefinition.EDS_MARKET_ETKZ) || attributeName.equals(iPartsDataVirtualFieldsDefinition.EDS_ALL_MARKET_ETKZS)) {
                    String attributeString = "";

                    // Marktspezifische ETKZ hängen am Material
                    EtkDataPart materialOfEntry = getPart();
                    if (materialOfEntry != null) {
                        // M_MARKET_ETKZ wird bei EDS-Stücklisten immer mitgeladen -> falls es nicht vorhanden ist, dann
                        // stimmt etwas mit der Stückliste nicht -> das virtuelle Feld in diesem fall dann auch leer befüllen
                        if (materialOfEntry.attributeExists(FIELD_M_MARKET_ETKZ)) {
                            String marketEtkzJSON = null;
                            try {
                                marketEtkzJSON = materialOfEntry.getFieldValueAsStringFromZippedBlob(FIELD_M_MARKET_ETKZ);
                            } catch (Exception e) {
                                Logger.log(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, "Error while loading zipped JSON for field \""
                                                                                          + FIELD_M_MARKET_ETKZ + "\" of material \""
                                                                                          + materialOfEntry.getAsId().getMatNr() + "\"");
                            }
                            if (StrUtils.isValid(marketEtkzJSON)) {
                                Genson genson = JSONUtils.createGenson(false);
                                SparePartSignsJSONObject sparePartsSignAndMarketObjects;
                                try {
                                    sparePartsSignAndMarketObjects = genson.deserialize(marketEtkzJSON, SparePartSignsJSONObject.class);
                                } catch (Exception e) {
                                    RuntimeException runtimeException = new RuntimeException("JSON of field MAT." + FIELD_M_MARKET_ETKZ
                                                                                             + " is invalid for material \""
                                                                                             + materialOfEntry.getAsId().getMatNr()
                                                                                             + "\"", e);
                                    Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, runtimeException);
                                    sparePartsSignAndMarketObjects = null;
                                }

                                if (sparePartsSignAndMarketObjects != null) {
                                    List<SparePartSignAndMarketJSONObject> sparePartsSignAndObjectsList = sparePartsSignAndMarketObjects.getSparePartSignsAndMarket();
                                    if (attributeName.equals(iPartsDataVirtualFieldsDefinition.EDS_MARKET_ETKZ)) { // Nur den ausgewählten Markt ausgeben
                                        String chosenMarketETKZ = getEtkProject().getUserSettings().getStrValues(iPartsUserSettingsConst.REL_EDS_MARKET_ETKZ);
                                        // Falls es mehr ET-Kenner gibt als das ausgewählte, dann muss ein Stern angefügt werden
                                        if (sparePartsSignAndObjectsList.size() == 1) {
                                            SparePartSignAndMarketJSONObject sparePartsSignAndMarketObject = sparePartsSignAndObjectsList.get(0);
                                            if (Utils.objectEquals(sparePartsSignAndMarketObject.getMarket(), chosenMarketETKZ)) {
                                                attributeString = sparePartsSignAndMarketObject.getSparePartSign();
                                            } else {
                                                attributeString = "*";
                                            }
                                        } else {
                                            for (SparePartSignAndMarketJSONObject sparePartsSignAndMarketObject : sparePartsSignAndObjectsList) {
                                                if (Utils.objectEquals(sparePartsSignAndMarketObject.getMarket(), chosenMarketETKZ)) {
                                                    attributeString = sparePartsSignAndMarketObject.getSparePartSign();
                                                    break;
                                                }
                                            }
                                            attributeString += "*"; // Es gibt ja definitiv mehr als einen ET-Kenner
                                        }
                                    } else { // Alle Märkte ausgeben
                                        String dbLanguage = getEtkProject().getDBLanguage();
                                        StringBuffer allMarketETKZs = new StringBuffer();
                                        for (SparePartSignAndMarketJSONObject sparePartsSignAndMarketObject : sparePartsSignAndObjectsList) {
                                            String market = sparePartsSignAndMarketObject.getMarket();
                                            String sparePartSign = sparePartsSignAndMarketObject.getSparePartSign();
                                            if (StrUtils.isValid(market, sparePartSign)) {
                                                if (allMarketETKZs.length() > 0) {
                                                    allMarketETKZs.append('\n');
                                                }
                                                allMarketETKZs.append(market);
                                                String marketName = getEtkProject().getEnumText(ENUM_KEY_EDS_MARKET_ETKZ,
                                                                                                market, dbLanguage, true);
                                                if (!marketName.isEmpty()) {
                                                    allMarketETKZs.append(" - ");
                                                    allMarketETKZs.append(marketName);
                                                }
                                                allMarketETKZs.append(": ");
                                                allMarketETKZs.append(sparePartSign);
                                            }
                                        }
                                        attributeString = allMarketETKZs.toString();
                                    }
                                }
                            }
                        }
                    }
                    attributes.addField(attributeName, attributeString, true, DBActionOrigin.FROM_DB);
                } else if (attributeName.equals(iPartsDataVirtualFieldsDefinition.EDS_RETAIL_USE)) {
                    // Retail-Verwendung prüfen
                    DBDataObjectAttributesList retailUsages = EditConstructionToRetailHelper.getRetailSourceGuidAttributeList(iPartsEntrySourceType.EDS,
                                                                                                                              getFieldValue(iPartsDataVirtualFieldsDefinition.EDS_SAAGUID),
                                                                                                                              null,
                                                                                                                              new String[]{ FIELD_K_VARI },
                                                                                                                              getEtkProject());
                    attributes.addField(attributeName, !retailUsages.isEmpty() ? RETAIL_ASSIGNED : RETAIL_NOT_ASSIGNED, true,
                                        DBActionOrigin.FROM_DB);
                    return false;
                } else {
                    // Nachladen der EDS-Stückliste ist z.B. aus der Suche heraus notwendig -> auch hier false zurückliefern,
                    // weil es kein vermeidbares Performance-Problem darstellt
                    loadFromDB(getAsId(), attributeName);
                }
            } else { // falls es keine EDS-Stückliste ist, das virtuelle Feld leer befüllen, da das Feld trotzdem angefordert wurde
                attributes.addField(attributeName, "", true, DBActionOrigin.FROM_DB);
            }
            return false;
        } else if (attributeName.startsWith(VirtualFieldsUtils.VIRTUAL_FIELD_MARKER_PREFIX + iPartsDataVirtualFieldsDefinition.MBS
                                            + iPartsDataVirtualFieldsDefinition.VIRTFIELD_SPACER)) {
            // Die virtuellen Felder für MBS können nur in den MBS-Konstruktions-Stücklisten sinnvolle Ergebnisse liefern.
            // Bei den anderen Stücklisten können wir aus Performancegründen das Laden bleiben lassen.
            EtkDataAssembly parentAssembly = EtkDataObjectFactory.createDataAssembly(getEtkProject(), getAsId().getOwnerAssemblyId());
            String partListType = parentAssembly.getEbeneName();
            if (partListType.equals(iPartsConst.PARTS_LIST_TYPE_MBS_LIST_NUMBER) || partListType.equals(iPartsConst.PARTS_LIST_TYPE_MBS_CON_GROUP)) {
                // Nachladen der MBS-Stückliste ist z.B. aus der Suche heraus notwendig -> auch hier false zurückliefern,
                // weil es kein vermeidbares Performance-Problem darstellt
                loadFromDB(getAsId(), attributeName);
            } else { // falls es keine MBS-Stückliste ist, das virtuelle Feld leer befüllen, da das Feld trotzdem angefordert wurde
                attributes.addField(attributeName, "", true, DBActionOrigin.FROM_DB);
            }
            return false;
        } else if (attributeName.startsWith(VirtualFieldsUtils.VIRTUAL_FIELD_MARKER_PREFIX + iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG
                                            + iPartsDataVirtualFieldsDefinition.VIRTFIELD_SPACER)) {

            // Den Dokumentationstyp ermitteln ...
            iPartsDocumentationType documentationType = getOwnerAssembly().getDocumentationType();

            // ... doch nur bei DIALOG-Stücklisten ist die K_SOURCE_GUID vorhanden, die im Folgenden in ihre Einzelteile zerlegt zugewiesen wird.
            if (documentationType.isPKWDocumentationType()) {
                // Um diese GUID geht es
                String sourceGUID = getFieldValue(iPartsConst.FIELD_K_SOURCE_GUID);
                iPartsDialogBCTEPrimaryKey bctePrimaryKey = iPartsDialogBCTEPrimaryKey.createFromDialogGuid(sourceGUID);

                // Wenn der Schlüssel nichts Vernünftiges enthielt, einen definiert leeren verwenden.
                if (bctePrimaryKey == null) {
                    bctePrimaryKey = EMPTY_BCTE_PRIMARY_KEY;
                }

                if (attributeName.equals(iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_SERIES_NO)) {
                    attributes.addField(iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_SERIES_NO, bctePrimaryKey.seriesNo, true, DBActionOrigin.FROM_DB);
                } else if (attributeName.equals(iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_HM)) {
                    attributes.addField(iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_HM, bctePrimaryKey.hm, true, DBActionOrigin.FROM_DB);
                } else if (attributeName.equals(iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_M)) {
                    attributes.addField(iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_M, bctePrimaryKey.m, true, DBActionOrigin.FROM_DB);
                } else if (attributeName.equals(iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_SM)) {
                    attributes.addField(iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_SM, bctePrimaryKey.sm, true, DBActionOrigin.FROM_DB);
                } else if (attributeName.equals(iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_POSE)) {
                    attributes.addField(iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_POSE, bctePrimaryKey.posE, true, DBActionOrigin.FROM_DB);
                } else if (attributeName.equals(iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_POSV)) {
                    attributes.addField(iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_POSV, bctePrimaryKey.posV, true, DBActionOrigin.FROM_DB);
                } else if (attributeName.equals(iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_WW)) {
                    attributes.addField(iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_WW, bctePrimaryKey.ww, true, DBActionOrigin.FROM_DB);
                } else if (attributeName.equals(iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_ETKZ)) {
                    attributes.addField(iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_ETKZ, bctePrimaryKey.et, true, DBActionOrigin.FROM_DB);
                } else if (attributeName.equals(iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_AA)) {
                    attributes.addField(iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_AA, bctePrimaryKey.aa, true, DBActionOrigin.FROM_DB);
                } else if (attributeName.equals(iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_SDATA)) {
                    attributes.addField(iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_SDATA, bctePrimaryKey.sData, true, DBActionOrigin.FROM_DB);
                } else if (attributeName.equals(iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_DD_LINKED_FACTORY_DATA_GUID)) {
                    iPartsDialogId constructionIdOfDestination = new iPartsDialogId(sourceGUID);
                    iPartsDataDialogData constructionDataOfDestination = new iPartsDataDialogData(getEtkProject(), constructionIdOfDestination);
                    String linkedFactoryGuid = "";
                    if (constructionDataOfDestination.existsInDB()) {
                        linkedFactoryGuid = constructionDataOfDestination.getFieldValue(iPartsConst.FIELD_DD_LINKED_FACTORY_DATA_GUID);
                    }
                    attributes.addField(iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_DD_LINKED_FACTORY_DATA_GUID, linkedFactoryGuid, true, DBActionOrigin.FROM_DB);
                }
            } else { // falls es keine DIALOG-Stückliste ist, das virtuelle Feld leer befüllen, da das Feld trotzdem angefordert wurde
                attributes.addField(attributeName, "", true, DBActionOrigin.FROM_DB);
            }
            return false; // kein echtes Nachladen sondern nur Berechnung
        } else if (iPartsDataVirtualFieldsDefinition.loadVirtualFieldForFilterReason(attributeName, attributes)) {
            return false;
        } else if (attributeName.startsWith(VirtualFieldsUtils.VIRTUAL_FIELD_MARKER_PREFIX + iPartsDataVirtualFieldsDefinition.DA_MODEL_EVALUATION)) {
            // Baumusterauswertung -> spezielle virtuelle Felder leer befüllen, da das Feld trotzdem angefordert wurde
            attributes.addField(attributeName, SQLStringConvert.booleanToPPString(false), true, DBActionOrigin.FROM_DB);
            return false;
        } else if (attributeName.startsWith(VirtualFieldsUtils.VIRTUAL_FIELD_MARKER_PREFIX + iPartsDataVirtualFieldsDefinition.DA_FIN_EVALUATION)) {
            // FIN Auswertung -> spezielle virtuelle Felder leer befüllen, da das Feld trotzdem angefordert wurde
            attributes.addField(attributeName, SQLStringConvert.booleanToPPString(false), true, DBActionOrigin.FROM_DB);
            return false;
        } else if (attributeName.equals(iPartsDataVirtualFieldsDefinition.DD_CALCULATED_DOCU_RELEVANT)) {
            if (getOwnerAssembly().isDialogSMConstructionAssembly()) {
                // FIELD DD_CALCULATED_DOCU_RELEVANT berechnen
                iPartsVirtualCalcFieldDocuRel calcField = new iPartsVirtualCalcFieldDocuRel(getEtkProject(), this);
                calcField.calculateAndSetDocuRelevantWithPositionVariants();
            } else { // falls es keine DIALOG-Konstruktions-Stückliste ist, das virtuelle Feld leer befüllen, da das Feld trotzdem angefordert wurde
                attributes.addField(attributeName, "", true, DBActionOrigin.FROM_DB);
            }
            return false;
        } else if (attributeName.equals(iPartsDataVirtualFieldsDefinition.DD_CALCULATED_AS_RELEVANT)) {
            if (getOwnerAssembly().isDialogSMConstructionAssembly()) {
                // FIELD DD_CALCULATED_AS_RELEVANT berechnen
                iPartsVirtualFieldCalcASRel calcField = new iPartsVirtualFieldCalcASRel(getEtkProject(), this);
                calcField.calculateAndSetASRelevant();
            } else { // falls es keine DIALOG-Konstruktions-Stückliste ist, das virtuelle Feld leer befüllen, da das Feld trotzdem angefordert wurde
                attributes.addField(attributeName, "", true, DBActionOrigin.FROM_DB);
            }
            return false;
        } else if (attributeName.equals(iPartsDataVirtualFieldsDefinition.DA_MODULE_DM_DOCUTYPE)) {
            // Dokumentationsmethode bestimmen
            attributes.addField(attributeName, getOwnerAssembly().getModuleMetaData().getDocumentationType().getDBValue(),
                                true, DBActionOrigin.FROM_DB);
            return false;
        } else if (attributeName.equals(iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_CHANGE)) {
            if (getOwnerAssembly().isRetailPartList() || getOwnerAssembly().isDialogSMConstructionAssembly()) {
                // Boolean-Feld für DIALOG Änderung
                loadDIALOGChanges();
                boolean hasChanges = (dataDIALOGChangesForBCTE != null) || (dataDIALOGChangesForMAT != null);
                attributes.addField(iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_CHANGE, SQLStringConvert.booleanToPPString(hasChanges),
                                    true, DBActionOrigin.FROM_DB);
            } else { // falls es keine Retail-Stückliste oder kein SM-Knoten ist, das virtuelle Feld leer befüllen, da das Feld trotzdem angefordert wurde
                attributes.addField(attributeName, "", true, DBActionOrigin.FROM_DB);
            }
            return false;
        } else if (attributeName.equals(iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_CHANGE_REASON)) {
            if (getOwnerAssembly().isRetailPartList() || getOwnerAssembly().isDialogSMConstructionAssembly()) {
                // DIALOG Änderungsgrund
                loadDIALOGChanges();
                String changeReason = iPartsPartlistTextHelper.getDIALOGChangeReason(dataDIALOGChangesForBCTE, dataDIALOGChangesForMAT);
                attributes.addField(iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_CHANGE_REASON, changeReason, true, DBActionOrigin.FROM_DB);
            } else { // falls es keine Retail-Stückliste oder kein SM-Knoten ist, das virtuelle Feld leer befüllen, da das Feld trotzdem angefordert wurde
                attributes.addField(attributeName, "", true, DBActionOrigin.FROM_DB);
            }
            return false;
        } else if (attributeName.equals(iPartsDataVirtualFieldsDefinition.DA_ORIGINAL_FAIL_LOCATION)) {
            if (getOwnerAssembly().isRetailPartList() || getOwnerAssembly().isDialogSMConstructionAssembly()) {
                // Entscheiden, ob K_FAIL_LOCLIST oder DEL_DAMAGE_PART ausgegeben werden soll.
                calculateOriginalFailLocation();
            } else {
                attributes.addField(attributeName, "", true, DBActionOrigin.FROM_DB);
            }
            return false;
        } else if (attributeName.equals(iPartsDataVirtualFieldsDefinition.DA_ORIGINAL_MAT_NR)) {
            // Als Fallback die aktuelle Teilenummer verwenden
            attributes.addField(attributeName, attributes.getFieldValue(FIELD_K_MATNR), true, DBActionOrigin.FROM_DB);
            return false;
        } else if (attributeName.equals(iPartsDataVirtualFieldsDefinition.DA_FAIL_LOCATION)) {
            boolean isRetailPartList = getOwnerAssembly().isRetailPartList();
            if (isRetailPartList || getOwnerAssembly().isDialogSMConstructionAssembly()) {
                // Vererbte Fehlerorte berechnen
                calculateInheritedFailLocation(isRetailPartList, null);
            } else {
                attributes.addField(attributeName, "", true, DBActionOrigin.FROM_DB);
            }
            return false;
        } else if (attributeName.equals(iPartsDataVirtualFieldsDefinition.DD_CALCULATED_BUSINESS_CASE)) {
            if (getOwnerAssembly().isDialogSMConstructionAssembly()) {
                // DIALOG Geschäftsfall, DAIMLER-5899, Der Geschäftsfall in der konstruktiven Stückliste, Feld existiert nicht als Datenbankfeld
                loadDIALOGChanges();

                // "GEÄNDERT" falls es passende Einträge in der Tabelle DA_DIALOG_CHANGES gibt, die noch in keinem ChangeSet
                // bearbeitet werden (also das Feld DDC_CHANGE_SET_GUID leer ist)
                boolean hasChanges = false;
                if (dataDIALOGChangesForBCTE != null) {
                    for (iPartsDataDIALOGChange dataDIALOGChange : dataDIALOGChangesForBCTE) {
                        if (dataDIALOGChange.getFieldValue(iPartsConst.FIELD_DDC_CHANGE_SET_GUID).isEmpty()) {
                            hasChanges = true;
                            break;
                        }
                    }
                }

                if (!hasChanges && (dataDIALOGChangesForMAT != null)) {
                    for (iPartsDataDIALOGChange dataDIALOGChange : dataDIALOGChangesForMAT) {
                        if (dataDIALOGChange.getFieldValue(iPartsConst.FIELD_DDC_CHANGE_SET_GUID).isEmpty()) {
                            hasChanges = true;
                            break;
                        }
                    }
                }

                // "NEU" aus dem virtuellen Feld DD_CALCULATED_DOCU_RELEVANT (SetOfEnum!) ableiten
                String calculatedDocuRelevantString = "";
                List<String> calculatedDocuRelevantSet = getFieldValueAsSetOfEnum(iPartsDataVirtualFieldsDefinition.DD_CALCULATED_DOCU_RELEVANT);
                if (!calculatedDocuRelevantSet.isEmpty()) {
                    // SetOfEnum wird nur für die Filterung benötigt. Im Feld steht immer nur genau ein Wert drin.
                    calculatedDocuRelevantString = calculatedDocuRelevantSet.get(0);
                }
                iPartsDocuRelevant calculatedDocuRelevant = iPartsDocuRelevant.getFromDBValue(calculatedDocuRelevantString);

                // Den Geschäftsfall bestimmen
                iPartsDocuRelevant dbDocuRelevant = iPartsDocuRelevant.getFromDBValue(getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_DOCU_RELEVANT));
                boolean isUsedInAS = getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_RETAILUSE).equals(RETAIL_ASSIGNED);
                String businessCase = iPartsBusinessCase.getCalculatedBusinessCase(hasChanges, dbDocuRelevant, isUsedInAS, calculatedDocuRelevant).getDBValue();
                attributes.addField(iPartsDataVirtualFieldsDefinition.DD_CALCULATED_BUSINESS_CASE, businessCase, true, DBActionOrigin.FROM_DB);
            } else { // falls es keine DIALOG-Konstruktions-Stückliste ist, das virtuelle Feld leer befüllen, da das Feld trotzdem angefordert wurde
                attributes.addField(attributeName, "", true, DBActionOrigin.FROM_DB);
            }
            return false;
        } else if (attributeName.equals(iPartsDataVirtualFieldsDefinition.DA_COLORTABLE_QUALITY_CHECK)
                   || attributeName.equals(iPartsDataVirtualFieldsDefinition.DA_QUALITY_CHECK_ERROR)) {
            // Dieses virtuelle Feld muss aktiv gesetzt werden innerhalb der Qualitätsprüfung
            attributes.addField(attributeName, "", true, DBActionOrigin.FROM_DB);
            return false;
        } else if (attributeName.equals(iPartsDataVirtualFieldsDefinition.RETAIL_CHANGED_CODE)) {
            if (getOwnerAssembly().isRetailPartList()) {
                String codes = getFieldValue(FIELD_K_CODES); // Original Code
                String codesConst = getFieldValue(FIELD_K_CODES_CONST); // Code-Regel aus der Konstruktion, wird bei Übernahme befüllt
                String codesEqualEnumToken;
                if (StrUtils.isEmpty(codes, codesConst)) {
                    codesEqualEnumToken = ENUM_CODES_EQUAL;
                } else {
                    codesEqualEnumToken = codes.equals(codesConst) ? ENUM_CODES_EQUAL : ENUM_CODES_NOT_EQUAL;
                }
                attributes.addField(attributeName, codesEqualEnumToken, true, DBActionOrigin.FROM_DB);
            } else { // falls es keine Retail-Stückliste ist, das virtuelle Feld leer befüllen, da das Feld trotzdem angefordert wurde
                attributes.addField(attributeName, "", true, DBActionOrigin.FROM_DB);
            }
            return false;
        } else if (attributeName.equals(iPartsDataVirtualFieldsDefinition.RETAIL_MODIFIED_STATE)) {
            String value = ENUM_MODIFIED_STATE_UNMODIFIED;
            if (getOwnerAssembly().isRetailPartList()) {
                EtkRevisionsHelper revisionsHelper = getRevisionsHelper();
                if ((revisionsHelper != null) && revisionsHelper.isRevisionChangeSetActive()) {
                    AbstractRevisionChangeSet activeChangeSet = revisionsHelper.getActiveRevisionChangeSetForEdit();
                    if (activeChangeSet == null) { // Kein Edit-ChangeSet aktiv -> es ist ein readOnly-ChangeSet aktiv
                        activeChangeSet = revisionsHelper.getActiveRevisionChangeSets().iterator().next();
                    }
                    if (revisionsHelper.checkIfObjectCreatedInChangeSet(activeChangeSet, getAsId())) {
                        value = ENUM_MODIFIED_STATE_NEW;
                    }
                }
            }
            attributes.addField(attributeName, value, true, DBActionOrigin.FROM_DB);
            return false;
        } else if (attributeName.equals(iPartsDataVirtualFieldsDefinition.K_PRIMUS_CODE_FORWARD)) {
            String forwardCode = null;
            String matNr = getFieldValue(FIELD_K_MATNR);
            if (!matNr.isEmpty()) {
                // Vorwärts-Code via Cache bestimmen
                iPartsPRIMUSReplacementsCache primusReplacementsCache = iPartsPRIMUSReplacementsCache.getInstance(getEtkProject());
                forwardCode = primusReplacementsCache.getForwardCodeForPartNo(matNr);
            }
            attributes.addField(attributeName, (forwardCode == null) ? "" : forwardCode, true, DBActionOrigin.FROM_DB);
            return false;
        } else if (attributeName.equals(iPartsDataVirtualFieldsDefinition.RETAIL_CEMAT_EINPAS)) {
            String value;
            if (getOwnerAssembly().isRetailPartList()) { // Ein Cemat EinPAS Mapping gibt es nur bei Retail-Stücklisten
                value = iPartsDataModuleCematList.buildEinPasVersionCematString(getOwnerAssembly().getCematMapForModule(), this);
            } else {
                value = "";
            }
            attributes.addField(attributeName, value, true, DBActionOrigin.FROM_DB);
            return false;
        } else if (attributeName.equals(iPartsDataVirtualFieldsDefinition.K_MODULE_PREVIEW)) { // Vorschaubilder
            attributes.addField(attributeName, getModulePreviewImagesAsHTML(), true, DBActionOrigin.FROM_DB);
            return false;
        } else { // alle anderen virtuellen Felder wie z.B. STRUCTURE_PICTURE
            loadFromDB(getAsId(), attributeName);
            return true;
        }
    }

    public void calculateAndSetCombinedText(iPartsDataCombTextList combTextsForEntry, Map<String, String> neutralTextFromPartsForModule,
                                            boolean calculateFieldsForAddAndNeutralTexts) {
        Map<String, List<EtkMultiSprache>> multiLangMap = combTextsForEntry.buildSeqNoCombTextsMap();
        List<EtkMultiSprache> multiLangTexts = multiLangMap.get(getAsId().getKLfdnr());
        String combinedText = combTextsForEntry.getCombTexts(neutralTextFromPartsForModule, multiLangMap, getEtkProject()).get(getAsId().getKLfdnr());
        if (calculateFieldsForAddAndNeutralTexts) {
            VarParam<String> addText = new VarParam<>("");
            VarParam<String> neutralText = new VarParam<>("");
            iPartsCombTextHelper.extractAddTextAndNeutralTextWithoutExistingCache(getEtkProject(), getAsId().getKLfdnr(), multiLangTexts, addText, neutralText);
            setCombinedText(this, combinedText, addText.getValue(), neutralText.getValue(), null, getEtkProject());
        } else {
            setCombinedText(this, combinedText, null, null, null, getEtkProject());
        }
    }

    private String getModulePreviewImagesAsHTML() {
        previewImages = null;
        AssemblyId destAssemblyId = getDestinationAssemblyId();
        if (destAssemblyId.isValidId()) {
            EtkDataAssembly destAssembly = EtkDataObjectFactory.createDataAssembly(getEtkProject(), destAssemblyId);
            if (destAssembly instanceof iPartsDataAssembly) {
                List<FrameworkImage> destPreviewImages = ((iPartsDataAssembly)destAssembly).getPreviewImages();
                if (!destPreviewImages.isEmpty()) {
                    StringBuilder previewImageHTML = new StringBuilder();
                    for (FrameworkImage previewImage : destPreviewImages) {
                        previewImageHTML.append(DatatypeUtils.makeImgTag(previewImage, false));
                    }
                    previewImages = destPreviewImages;
                    return DatatypeUtils.addHtmlTags(previewImageHTML.toString(), true);
                }
            }
        }

        return "";
    }

    public void calculateAndSetEqualPartType() {
        EtkDataPart part = getPart();
        if (part instanceof iPartsDataPart) {
            ((iPartsDataPart)part).calculateAndSetEqualPartType();
        }
    }

    /**
     * Setzt die Gleichteile-Teilenummer am Material und anderen relevanten Datenobjekten basierend auf den Einstellungen
     * des übergebenen Filters.
     *
     * @param filter
     * @return
     */
    public boolean setEqualPartNumber(iPartsFilter filter) {
        if (hasDifferentEqualPartNumber != null) { // Gleichteile-Teilenummer nur einmal setzen -> bei neuen Filterwerten wird die Stückliste neu erzeugt
            return hasDifferentEqualPartNumber;
        }

        hasDifferentEqualPartNumber = false;
        EtkDataPart part = getPart();
        if (part.getAsId().isValidId() && !iPartsVirtualNode.isVirtualId(part.getAsId())) {
            String equalPartNumber = filter.getEqualPartNumber(part);
            String matNr = part.getAsId().getMatNr();
            if (!equalPartNumber.equals(matNr)) {
                if (part instanceof iPartsDataPart) {
                    ((iPartsDataPart)part).setMappedMatNr(equalPartNumber);
                    setEqualBasePartNumber(part, matNr, equalPartNumber, filter);
                }

                // In den Alternativteilen die Gleichteile-Teilenummern setzen
                if (alternativeParts != null) {
                    for (EtkDataPart alternativePart : alternativeParts) {
                        if (alternativePart instanceof iPartsDataPart) {
                            ((iPartsDataPart)alternativePart).setMappedMatNr(filter.getEqualPartNumber(alternativePart));
                        }
                        setEqualBasePartNumber(alternativePart, matNr, equalPartNumber, filter);
                    }
                }

                hasDifferentEqualPartNumber = true;
            }

            // Bei Ersetzungen mit reinen Materialnummern ohne Stücklisteneintrag als Nachfolger die Gleichteile-Teilenummern setzen
            // Es müssen alle ungefilterten Nachfolger berücksichtigt werden, da weiter unten damit die successors auch wieder
            // gesetzt werden -> speziell in der Qualitätsprüfung kann dies ansonsten zu Fehlern führen
            Collection<iPartsReplacement> successors = getSuccessors();
            if ((successors != null) && !successors.isEmpty()) {
                List<iPartsReplacement> successorsWithEqualPartNumbers = new ArrayList<>(successors.size());
                for (iPartsReplacement successor : ArrayUtil.toArray(successors)) {
                    // DAIMLER-13405: Anpassung Ersetzungslogik, dass das iParts Gleichteile-Mapping vor der virtuellen
                    // PRIMUS-Ersetzung durchgeführt wird
                    if (successor.source == iPartsReplacement.Source.PRIMUS) {
                        // PRIMUS-Ersetzung für die Gleichteile-Teilenummer suchen
                        EtkDataPartListEntryList partListEntries = new EtkDataPartListEntryList();
                        partListEntries.add(this, DBActionOrigin.FROM_DB);
                        iPartsPRIMUSReplacementsLoader primusReplacementsLoader = new iPartsPRIMUSReplacementsLoader(getEtkProject(),
                                                                                                                     partListEntries);

                        Map<String, List<iPartsReplacement>> primusSuccessorsMap = new HashMap<>();
                        primusReplacementsLoader.addPrimusReplacementsForPartList(primusSuccessorsMap, false);
                        List<iPartsReplacement> primusSuccessors = primusSuccessorsMap.get(getAsId().getKLfdnr());
                        if (primusSuccessors != null) {
                            // PRIMUS-Ersetzung für die Gleichteile-Teilenummer hinzufügen inkl. Gleichteile-Mapping
                            for (iPartsReplacement primusSuccessor : primusSuccessors) {
                                setEqualPartNumberForMatSuccessor(primusSuccessor, filter);
                            }
                            successorsWithEqualPartNumbers.addAll(primusSuccessors);
                        }
                    } else {
                        setEqualPartNumberForMatSuccessor(successor, filter);
                        successorsWithEqualPartNumbers.add(successor);
                    }
                }
                this.successors = successorsWithEqualPartNumbers;
            }
        }

        return hasDifferentEqualPartNumber;
    }

    private void setEqualBasePartNumber(EtkDataPart part, String matNr, String equalPartNumber, iPartsFilter filter) {
        // Falls eine Basis-Materialnummer vorhanden ist, muss diese auch auf die gemappte Teilenummer geändert
        // werden, da ansonsten die Ausgabe in den Webservices und XML falsch wäre
        String baseMatNr = part.getFieldValue(iPartsConst.FIELD_M_BASE_MATNR);
        if (!baseMatNr.isEmpty()) {
            if (baseMatNr.equals(matNr)) { // Teil hat weder ES1, noch ES2 (kommt bei uns trotzdem häufig in der DB vor)
                part.setFieldValue(iPartsConst.FIELD_M_BASE_MATNR, equalPartNumber, DBActionOrigin.FROM_DB);
            } else {
                EtkDataPart basePart = EtkDataObjectFactory.createDataPart(getEtkProject(), baseMatNr, "");
                if (basePart.existsInDB()) {
                    part.setFieldValue(iPartsConst.FIELD_M_BASE_MATNR, filter.getEqualPartNumber(basePart), DBActionOrigin.FROM_DB);
                }
            }
        }
    }

    private void setEqualPartNumberForMatSuccessor(iPartsReplacement successor, iPartsFilter filter) {
        if (successor.successorEntry == null) {
            EtkDataPart successorPart = EtkDataObjectFactory.createDataPart(getEtkProject(), successor.successorPartNumber, "");
            if (successorPart.existsInDB()) {
                successor.successorMappedPartNumber = filter.getEqualPartNumber(successorPart);
            }
        }
    }

    /**
     * Berechnet die virtuellen Felder für die um AS-/Zubehör-Code reduzierte Code-Regel einmal unabhängig und einmal abhängig
     * davon, ob der Filter-Hauptschalter und der Baumuster-Filter im übergebenen Filter aktiv sind oder nicht.
     *
     * @param filter
     */
    public void calculateRetailCodesReducedAndFiltered(iPartsFilter filter) {
        String originalCodes = getFieldValue(iPartsConst.FIELD_K_CODES);
        String reducedCodesFromDB = getFieldValue(iPartsConst.FIELD_K_CODES_REDUCED);
        String reducedCodes = originalCodes;
        String filteredCodes = originalCodes;
        if (!reducedCodesFromDB.isEmpty()) { // Um AS-/Zubehör-Code reduzierte Code-Regel existiert in der DB
            reducedCodes = reducedCodesFromDB;

            // Filter-Hauptschalter und Baumuster-Filter (unabhängig vom Modul) aktiv?
            if ((filter != null) && filter.getSwitchboardState().isMainSwitchActive() && filter.isModelFilterActive(null)) {
                filteredCodes = reducedCodesFromDB;
            }
        }
        attributes.addField(iPartsDataVirtualFieldsDefinition.RETAIL_CODES_REDUCED, reducedCodes, true, DBActionOrigin.FROM_DB);
        attributes.addField(iPartsDataVirtualFieldsDefinition.RETAIL_CODES_FILTERED, filteredCodes, true, DBActionOrigin.FROM_DB);
    }

    public void clearCalculatedFailLocation() {
        originalFailLocationCalculated = false;
        inheritedFailLocationCalculated = false;
        attributes.remove(iPartsDataVirtualFieldsDefinition.DA_ORIGINAL_FAIL_LOCATION);
        attributes.remove(iPartsDataVirtualFieldsDefinition.DA_FAIL_LOCATION);
    }

    /**
     * Berechnung der Orginal-Fehlerorte.
     * Entscheidet, ob die Fehlerorte aus {@code KATALOG.K_FAIL_LOCLIST} oder aus {@code DA_ERROR_LOCATION.DEL_DAMAGE_PART}
     * im virtuellen Feld {@code DA_ORIGINAL_FAIL_LOCATION} verwendet werden.
     */
    public void calculateOriginalFailLocation() {
        if (!originalFailLocationCalculated) {
            // Fehlerorte gibt es nur bei DIALOG, also mit vorhandenem BCTE-Schlüssel
            String failLocation = "";
            iPartsDialogBCTEPrimaryKey bcteKey = getDialogBCTEPrimaryKey();
            if (bcteKey != null) {
                boolean seriesIsRelevantForImport = iPartsDIALOGSeriesValidityCache.getInstance(getEtkProject()).isSeriesValidForDIALOGImport(bcteKey.seriesNo);
                if (seriesIsRelevantForImport) {
                    // Bei versorgungsrelevanten Baureihen den Wert aus DA_ERROR_LOCATION (DEL_DAMAGE_PART) verwenden.
                    iPartsDataErrorLocationLRUCache cache = iPartsDataErrorLocationLRUCache.getInstance(getEtkProject(), new iPartsSeriesId(bcteKey.seriesNo));
                    if (cache != null) {
                        failLocation = cache.getErrorLocationForPartListEntry(bcteKey, getPart().getAsId().getMatNr());
                    }
                }

                // Bei "nicht versorgungsrelevanten" Baureihen und wenn bei "versorgungsrelevanten" Baureihen in DA_ERROR_LOCATION nichts gefunden wurde,
                // wird der Wert FIELD_K_FAIL_LOCLIST aus der Katalog-Tabelle verwendet.
                if (failLocation.isEmpty()) {
                    failLocation = getFieldValue(FIELD_K_FAIL_LOCLIST);
                }
            }

            attributes.addField(iPartsDataVirtualFieldsDefinition.DA_ORIGINAL_FAIL_LOCATION, failLocation, true, DBActionOrigin.FROM_DB);
            originalFailLocationCalculated = true;
        }
    }

    /**
     * Berechnung der vererbten Fehlerorte im virtuellen Feld {@code DA_FAIL_LOCATION}.
     *
     * @param doInheritance                Soll die Vererbung durchgeführt werden? Bei {@code false} wird einfach nur der Wert aus dem
     *                                     virtuellen Feld {@code DA_ORIGINAL_FAIL_LOCATION} verwendet.
     * @param partListEntriesForHotspotMap Optionale Map von Hotspot auf dazugehörige Stücklisteneinträge falls die Vererbung
     *                                     stattfinden soll für bessere Performance
     */
    public void calculateInheritedFailLocation(boolean doInheritance, Map<String, List<EtkDataPartListEntry>> partListEntriesForHotspotMap) {
        if (!inheritedFailLocationCalculated) {
            String failLocation = getFieldValue(iPartsDataVirtualFieldsDefinition.DA_ORIGINAL_FAIL_LOCATION);

            // Vererbung nur dann durchführen, falls es keine eigenen Fehlerorte gibt; Fehlerorte gibt es nur bei DIALOG,
            // also mit vorhandenem BCTE-Schlüssel
            if (doInheritance && failLocation.isEmpty() && (iPartsDialogBCTEPrimaryKey.createFromDIALOGPartListEntry(this) != null)) {
                Set<String> failLocationsSet = new TreeSet<>();
                String hotspot = getFieldValue(FIELD_K_POS);

                List<EtkDataPartListEntry> partListEntriesForHotspot = null;
                if (partListEntriesForHotspotMap != null) {
                    // Stücklisteneinträge für den Hotspot aus der Map nehmen
                    partListEntriesForHotspot = partListEntriesForHotspotMap.get(hotspot);
                }
                if (partListEntriesForHotspot == null) {
                    // Gesamte Stückliste unten im Stream nach Hotspot durchsuchen
                    partListEntriesForHotspot = getOwnerAssembly().getPartListUnfiltered(null).getAsList();
                }

                // Fehlerorte der Stücklisteneinträge mit gleichem Hotspot ermitteln
                PartListEntryId pleId = getAsId();
                partListEntriesForHotspot.stream()
                        .filter(partListEntry -> partListEntry.getFieldValue(FIELD_K_POS).equals(hotspot) && !partListEntry.getAsId().equals(pleId))
                        .forEach(partListEntry -> {
                            String otherFailLocation = partListEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.DA_ORIGINAL_FAIL_LOCATION);
                            if (!otherFailLocation.isEmpty()) {
                                failLocationsSet.addAll(StrUtils.toStringList(otherFailLocation, iPartsDataErrorLocationLRUCache.ERROR_LOCATION_DELIMITER,
                                                                              false, true));
                            }
                        });

                // Wenn es Fehlerorte an anderen Stücklisteneinträgen mit demselben Hotspot gibt, dann diese vererben
                if (!failLocationsSet.isEmpty()) {
                    failLocation = StrUtils.stringListToString(failLocationsSet, iPartsDataErrorLocationLRUCache.ERROR_LOCATION_DELIMITER);
                }
            }

            attributes.addField(iPartsDataVirtualFieldsDefinition.DA_FAIL_LOCATION, failLocation, true, DBActionOrigin.FROM_DB);
            inheritedFailLocationCalculated = true;
        }
    }

    private void loadDIALOGChanges() {
        if (!dataDIALOGChangesLoaded) { // DIALOG Änderungen laden falls notwendig
            boolean forAfterSales = getOwnerAssembly().isRetailPartList();
            String bcteKey;
            // Den BCTE-Schlüssel je nach (AS||DIALOG) aus unterschiedlichen Attributen ermitteln.
            if (forAfterSales) {
                bcteKey = getFieldValue(iPartsConst.FIELD_K_SOURCE_GUID);
            } else {
                bcteKey = getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_GUID);
            }
            Set<String> productFactories = new TreeSet<>();
            // Produkt bestimmen
            if (getOwnerAssembly() != null) {
                iPartsProductId productId = (getOwnerAssembly()).getProductIdFromModuleUsage();
                if (productId != null) {
                    iPartsProduct product = iPartsProduct.getInstance(getEtkProject(), productId);
                    productFactories.addAll(product.getProductFactories(getEtkProject()));
                }
            }
            if (!bcteKey.isEmpty()) {
                dataDIALOGChangesForBCTE = iPartsDataDIALOGChangeList.loadForBCTEKey(bcteKey, getEtkProject());
                if (dataDIALOGChangesForBCTE.isEmpty()) {
                    dataDIALOGChangesForBCTE = null;
                } else {
                    // Einschränken auf diesen Stücklisteneintrag
                    dataDIALOGChangesForBCTE = dataDIALOGChangesForBCTE.filterForPartListEntry(getAsId());
                }
            } else {
                dataDIALOGChangesForBCTE = null;
            }

            String seriesNumber;
            // Die Baureihe je nach (AS||DIALOG) aus unterschiedlichen Attributen ermitteln.
            if (forAfterSales) {
                seriesNumber = getFieldValue(iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_SERIES_NO);
            } else {
                seriesNumber = getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_SERIES_NO);
            }
            if (!seriesNumber.isEmpty()) {
                iPartsSeriesId seriesId = new iPartsSeriesId(seriesNumber);
                dataDIALOGChangesForMAT = iPartsDataDIALOGChangeList.loadForMatNo(getFieldValue(iPartsConst.FIELD_K_MATNR),
                                                                                  seriesId, getEtkProject());
                if (dataDIALOGChangesForMAT.isEmpty()) {
                    dataDIALOGChangesForMAT = null;
                } else {
                    // Einschränken auf diesen Stücklisteneintrag
                    dataDIALOGChangesForMAT = dataDIALOGChangesForMAT.filterForPartListEntry(getAsId());
                    // Die DialogChanges zu den Werksdaten zu Farben zusätzlich auf die relevanten Werke zum Produkt einschränken
                    dataDIALOGChangesForMAT = dataDIALOGChangesForMAT.filterForColorTableFactories(productFactories);
                }
            } else {
                dataDIALOGChangesForMAT = null;
            }

            dataDIALOGChangesLoaded = true;
        }
    }

    public void setDIALOGChangesAttributes(iPartsDataDIALOGChangeList dataDIALOGChangesForBCTE, iPartsDataDIALOGChangeList dataDIALOGChangesForMAT) {
        clearDIALOGChangesAttributes();

        this.dataDIALOGChangesForBCTE = dataDIALOGChangesForBCTE;
        this.dataDIALOGChangesForMAT = dataDIALOGChangesForMAT;
        dataDIALOGChangesLoaded = true;
    }

    public iPartsDataDIALOGChangeList getDIALOGChangesForBCTE() {
        loadDIALOGChanges();
        return dataDIALOGChangesForBCTE;
    }

    public iPartsDataDIALOGChangeList getDIALOGChangesForMAT() {
        loadDIALOGChanges();
        return dataDIALOGChangesForMAT;
    }

    /**
     * Löscht die Caches und virtuellen Felder für die DIALOG-Änderungen, damit diese neu berechnet werden.
     */
    public void clearDIALOGChangesAttributes() {
        // Caches leeren
        dataDIALOGChangesForBCTE = null;
        dataDIALOGChangesForMAT = null;
        dataDIALOGChangesLoaded = false;

        // Virtuelle Felder löschen, damit diese neu berechnet werden
        getAttributes().deleteField(iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_CHANGE, false, DBActionOrigin.FROM_DB);
        getAttributes().deleteField(iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_CHANGE_REASON, false, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsAssemblyId getOwnerAssemblyId() {
        return new iPartsAssemblyId(getAsId().getKVari(), getAsId().getKVer());
    }

    @Override
    public iPartsAssemblyId getDestinationAssemblyId() {
        return new iPartsAssemblyId(getFieldValue(EtkDbConst.FIELD_K_SACH), getFieldValue(EtkDbConst.FIELD_K_SVER));
    }

    public List<iPartsVirtualNode> getVirtualNodesPathForDestinationAssembly() {
        iPartsAssemblyId destinationAssemblyId = getDestinationAssemblyId();
        if (destinationAssemblyId.isVirtual()) {
            return iPartsVirtualNode.parseVirtualIds(destinationAssemblyId);
        } else {
            return null;
        }
    }

    public String getAggregateType() {
        if (aggregateType == null) {
            String aggregateTypeLocal = "";
            iPartsProductId productId = getOwnerAssembly().getProductIdFromModuleUsage();
            if (productId != null) {
                iPartsProduct product = iPartsProduct.getInstance(getEtkProject(), productId);
                aggregateTypeLocal = product.getAggregateType();  // das ist der Aggregatetyp mit ggf. mehreren Stellen z.B. "GA"
            }
            aggregateType = aggregateTypeLocal;
        }
        return aggregateType;
    }

    public void setAggregateType(String aggregateType) {
        this.aggregateType = aggregateType;
    }

    /**
     * Liefert die Liste der Fußnoten zu diesem PartListEntry
     *
     * @return
     */
    public Collection<iPartsFootNote> getFootNotes() {
        if (!footNotesLoaded) {
            Collection<iPartsFootNote> allFootnotes = new LinkedHashSet<>();
            // Set mit bereits hinzugefügten Fußnoten-IDs
            Set<iPartsFootNoteId> alreadyCollectedFootNotes = new HashSet<>();
            // Teilestamm und DIALOG Fußnoten hinzufügen
            iPartsFootNoteHelper.addPartAndDIALOGFootnotes(this, allFootnotes, alreadyCollectedFootNotes, iPartsPartFootnotesCache.getInstance(getEtkProject()),
                                                           iPartsDIALOGFootNotesCache.getInstance(getEtkProject()));
            // Für die Konstruktion sind nur Teilestamm- und DIALOG-Fußnoten relevant, welche direkt aus dem jeweiligen
            // Cache geladen werden.
            if (!getOwnerAssembly().isDialogSMConstructionAssembly()) {
                // Geladene Fußnoten
                iPartsDataFootNoteCatalogueRefList footnotes = iPartsDataFootNoteCatalogueRefList.loadFootNotesForPartListEntryWithJoin(getEtkProject(), getAsId());
                List<iPartsFootNote> localFootNotes = new DwList<>();
                if (!footnotes.isEmpty()) {
                    for (iPartsDataFootNoteCatalogueRef footnoteDBObject : footnotes) { // nur die Fußnoten-IDs in den iPartsFootNote-Objekten setzen und später nachladen
                        String id = footnoteDBObject.getFieldValue(iPartsConst.FIELD_DFNK_FNID);
                        if (!alreadyCollectedFootNotes.contains(new iPartsFootNoteId(id))) {
                            String name = footnoteDBObject.getFieldValue(iPartsConst.FIELD_DFN_NAME);
                            iPartsFootnoteType type = iPartsFootnoteType.getFromDBValue(footnoteDBObject.getFieldValue(iPartsConst.FIELD_DFN_TYPE));
                            boolean isStandardFootnote = footnoteDBObject.getFieldValueAsBoolean(iPartsConst.FIELD_DFN_STANDARD);
                            boolean isMarked = footnoteDBObject.getFieldValueAsBoolean(iPartsConst.FIELD_DFNK_FN_MARKED);
                            iPartsFootNote footnote = new iPartsFootNote(new iPartsFootNoteId(id), name, null, isStandardFootnote, type);
                            footnote.setIsMarked(isMarked);
                            localFootNotes.add(footnote);
                        }
                    }
                }

                if (!localFootNotes.isEmpty()) {
                    allFootnotes.addAll(localFootNotes);
                }

                // virtuelle Fußnote laden
                allFootnotes = iPartsVirtualFootnoteHelper.addVirtualFootnotes(this, allFootnotes, new iPartsPRIMUSReplacementsLoader(getEtkProject(), this));
            }

            if (Utils.isValid(allFootnotes)) {
                footNotes = allFootnotes;
            } else {
                footNotes = null;
            }
            footNotesLoaded = true;
        }
        return footNotes;
    }

    public Collection<iPartsFootNote> getFootNotesForRetail() {
        Collection<iPartsFootNote> allFootnotes = getFootNotes();
        if ((allFootnotes == null) || allFootnotes.isEmpty()) {
            return allFootnotes;
        }

        iPartsDataAssembly ownerAssembly = getOwnerAssembly();
        if (ownerAssembly == null) {
            return allFootnotes;
        }

        // Bei Konstruktions-Stückliste alle vorhandenen Fußnoten zurückliefern
        iPartsModuleTypes moduleType = iPartsModuleTypes.getType(ownerAssembly.getEbeneName());
        if (moduleType != null) {
            if (moduleType.isConstructionRelevant()) {
                return allFootnotes;
            }
        }

        if (ownerAssembly.getModuleMetaData().isShowColorTablefootnotes()) {
            return allFootnotes;
        } else {
            Collection<iPartsFootNote> result = new ArrayList<>();
            for (iPartsFootNote footnote : allFootnotes) {
                if (!footnote.isColorTablefootnote()) {
                    result.add(footnote);
                }
            }
            return result;
        }
    }

    public void setFootNotes(Collection<iPartsFootNote> footNotes) {
        this.footNotes = footNotes;
        footNotesLoaded = true;
    }

    public boolean hasFootNotes() {
        return getFootNotes() != null;
    }

    /**
     * Lädt die Liste der Fußnoten zu diesem PartListEntry neu
     *
     * @return
     */
    public Collection<iPartsFootNote> reloadFootNotes() {
        clearFootnotes();
        return getFootNotes();
    }

    /**
     * Entfernt alle bisher berechneten Fußnoten
     */
    public void clearFootnotes() {
        footNotes = null;
        footNotesLoaded = false;
    }

    /**
     * Liefert das Startdatum der Zeitscheibe für diesen PartListEntry basierend auf allen Werkseinsatzdaten zu
     * gültigen Werken zurück. Bei gesetztem Flag <i>forModelFilter</i> wird das Flag {@link #isPEMFromRelevant()} nicht
     * ausgewertet sondern immer das Startdatum der Zeitscheibe zurückgegeben. Ansonsten wird im Retail bei {@code !isPEMFromRelevant()}
     * unendlich ({@code 0}) zurückgegeben.
     *
     * @param forModelScoring Soll die Zeitscheibe basierend auf den Werkseinsatzdaten für das Baumuster-Scoring berechnet
     *                        werden oder basierend auf den ungefilterten Werkseinsatzdaten vor allen Manipulationen?
     * @return {@code 0} falls "unendlich" (im Sinne von es gibt kein Startdatum, also immer gültig)
     */
    public long getTimeSliceDateFrom(boolean forModelScoring) {
        if (forModelScoring) {
            if (!timeSliceDatesCalculatedForModelScoring) { // Kann eigentlich nicht passieren
                Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR,
                                                   new RuntimeException("Part list entry time slice dates for model scoring have not been calculated yet"));

                // Notfall-Fallback
                timeSliceDateFromForModelScoring = getTimeSliceDateFrom(false);
            }
            return timeSliceDateFromForModelScoring;
        } else {
            if (!timeSliceDatesLoadedUnfiltered) {
                setTimeSliceDates(false, 0, 0);
            }
            return timeSliceDateFromUnfiltered;
        }
    }

    /**
     * Liefert das Enddatum der Zeitscheibe für diesen PartListEntry basierend auf allen Werkseinsatzdaten zu
     * gültigen Werken zurück. Bei gesetztem Flag <i>forModelFilter</i> wird das Flag {@link #isPEMToRelevant()} nicht
     * ausgewertet sondern immer das Enddatum der Zeitscheibe zurückgegeben. Ansonsten wird im Retail bei {@code !isPEMToRelevant()}
     * unendlich ({@code Long#MAX_VALUE}) zurückgegeben.
     *
     * @param forModelScoring Soll die Zeitscheibe basierend auf den Werkseinsatzdaten für das Baumuster-Scoring berechnet
     *                        werden oder basierend auf den ungefilterten Werkseinsatzdaten vor allen Manipulationen?
     * @return {@code Long#MAX_VALUE} falls "unendlich" (im Sinne von es gibt kein Enddatum, also immer gültig)
     */
    public long getTimeSliceDateTo(boolean forModelScoring) {
        if (forModelScoring) {
            if (!timeSliceDatesCalculatedForModelScoring) { // Kann eigentlich nicht passieren
                Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR,
                                                   new RuntimeException("Part list entry time slice dates for model scoring have not been calculated yet"));

                // Notfall-Fallback
                timeSliceDateToForModelScoring = getTimeSliceDateTo(false);
            }
            return timeSliceDateToForModelScoring;
        } else {
            if (!timeSliceDatesLoadedUnfiltered) {
                setTimeSliceDates(false, 0, 0);
            }
            return timeSliceDateToUnfiltered;
        }
    }

    /**
     * Setzt die Zeitscheibe für diesen PartListEntry basierend auf den ungefilterten Werkseinsatzdaten.
     *
     * @param forModelScoring Flag, ob die Zeitscheibe für das Baumuster-Scoring berechnet und gesetzt werden soll
     * @param modelValidFrom  "Baumuster gültig ab" in Kombination mit {@code forModelScoring}
     * @param modelValidTo    "Baumuster gültig bis" in Kombination mit {@code forModelScoring}
     */
    public void setTimeSliceDates(boolean forModelScoring, long modelValidFrom, long modelValidTo) {
        if (modelValidTo == 0) { // Beim Enddatum muss mit Long.MAX_VALUE gerechnet werden für unendlich anstatt mit 0
            modelValidTo = Long.MAX_VALUE;
        }

        iPartsFactoryData factoryData = getFactoryDataForRetailUnfiltered();
        if (hasValidFactoryDataForRetail(factoryData)) {
            // Minimales Startdatum und maximales Enddatum über alle Werkseinsatzdaten (für gültige Werke) bestimmen
            long minStartDate = Long.MAX_VALUE;
            long maxEndDate = Long.MIN_VALUE;
            for (List<iPartsFactoryData.DataForFactory> dataForFactoryList : factoryData.getFactoryDataMap().values()) {
                for (iPartsFactoryData.DataForFactory dataForFactory : dataForFactoryList) {
                    long dateFrom = dataForFactory.dateFrom;
                    long dateTo = dataForFactory.getDateToWithInfinity();
                    if (forModelScoring) {
                        // DAIMLER-6564 Negative Zeitintervalle als gültig auswerten
                        // Siehe auch iPartsFilterHelper.basicCheckTimeSlicePreFilter()
                        if (dateFrom > dateTo) {
                            // "BM-Gültigkeit-Bis >= Termin-Ab der Werkseinsatzdaten" und "BM-Gültigkeit-Ab <= Termin-Bis der Werkseinsatzdaten"
                            if ((modelValidTo >= dateFrom) && (modelValidFrom <= dateTo)) {
                                dateFrom = dateTo;
                            } else { // Werkseinsatzdaten sind ungültig
                                continue;
                            }
                        }

                        // DAIMLER-7002 Korrektur von PEM-Termin-ab/bis auf Baumuster-Gültigkeit ab/bis
                        if (dateFrom < modelValidFrom) {
                            dateFrom = modelValidFrom;
                        }
                        if (dateTo > modelValidTo) {
                            dateTo = modelValidTo;
                        }
                    }

                    if (dateFrom != iPartsFactoryData.INVALID_DATE) {
                        minStartDate = Math.min(minStartDate, dateFrom);
                    }

                    if (dateTo != iPartsFactoryData.INVALID_DATE) {
                        if (dateFrom == dateTo) {
                            // Künstliche Zeitscheibe erzeugen für identische Start- und Endezeiten
                            // bei Datumsangaben ohne Zeitangaben einen kompletten Tag als Dauer verwenden, sonst nur
                            // eine minimale Zeitscheibe von 1 Sekunde
                            // Modulo 1000000 == 0 bedeutet, dass die letzten 6 Stellen 0 sind und demzufolge keine
                            // Zeitangaben existieren
                            if ((dateTo % 1000000) == 0) {
                                dateTo += 235959; // künstliche Zeitschreibe mit 1 Tag
                            } else {
                                dateTo += 1; // minimale künstliche Zeitscheibe mit 1 Sekunde
                            }
                        }
                        maxEndDate = Math.max(maxEndDate, dateTo);
                    }
                }
            }

            if (minStartDate != Long.MAX_VALUE) {
                if (forModelScoring) {
                    timeSliceDateFromForModelScoring = minStartDate;
                } else {
                    timeSliceDateFromUnfiltered = minStartDate;
                }
            }
            if (maxEndDate != Long.MIN_VALUE) {
                if (forModelScoring) {
                    timeSliceDateToForModelScoring = maxEndDate;
                } else {
                    timeSliceDateToUnfiltered = maxEndDate;
                }
            }
        } else {
            if (forModelScoring) {
                timeSliceDateFromForModelScoring = 0;
                timeSliceDateToForModelScoring = Long.MAX_VALUE;
            } else {
                timeSliceDateFromUnfiltered = 0;
                timeSliceDateToUnfiltered = Long.MAX_VALUE;
            }
        }

        if (forModelScoring) {
            timeSliceDatesCalculatedForModelScoring = true;
        } else {
            timeSliceDatesLoadedUnfiltered = true;
        }
    }

    /**
     * Liefert zurück, ob es Werkseinsatzdaten mit gültigen Werken gibt für den Retail zu diesem PartListEntry OHNE Filterung.
     *
     * @return
     */
    public boolean hasValidFactoryDataForRetailUnfiltered() {
        return hasValidFactoryDataForRetail(getFactoryDataForRetailUnfiltered());
    }

    /**
     * Liefert zurück, ob es Werkseinsatzdaten mit gültigen Werken gibt für den Retail zu diesem PartListEntry MIT Filterung
     * aber OHNE Vererbung.
     *
     * @return
     */
    public boolean hasValidFactoryDataForRetailWithoutReplacements() {
        return hasValidFactoryDataForRetail(getFactoryDataForRetailWithoutReplacements());
    }

    /**
     * Liefert zurück, ob es Werkseinsatzdaten mit gültigen Werken gibt für den Retail zu diesem PartListEntry MIT Filterung
     * und MIT Vererbung.
     *
     * @return
     */
    public boolean hasValidFactoryDataForRetail() {
        return hasValidFactoryDataForRetail(getFactoryDataForRetail());
    }

    /**
     * Liefert zurück, ob es Werkseinsatzdaten mit gültigen Werken gibt für den Retail bzgl. der übergebenen Werkseinsatzdaten.
     *
     * @return
     */
    private boolean hasValidFactoryDataForRetail(iPartsFactoryData factoryData) {
        if (factoryData == null) {
            return false;
        }
        if (!factoryData.hasValidFactories()) {
            return false;
        }
        // Im Retail dürfen "nicht relevante" Werke nicht berücksichtigt werden
        boolean isRelevantForFilter = false;
        iPartsFactories factoriesInstance = iPartsFactories.getInstance(getEtkProject());
        for (String factory : factoryData.getFactoryDataMap().keySet()) {
            if (factoriesInstance.isValidForFilter(factory)) {
                isRelevantForFilter = true;
                break;
            }
        }
        return isRelevantForFilter;
    }

    /**
     * Liefert alle Werkseinsatzdaten für den Retail zu diesem PartListEntry OHNE Filterung und OHNE Berücksichtigung von
     * Ersetzungen.
     *
     * @return
     */
    public iPartsFactoryData getFactoryDataForRetailUnfiltered() {
        if (!factoryDataForRetailLoaded) {
            // Nachladen über die Stückliste (langsam, ist aber normalerweise nur bei der Suche notwendig)
            EtkDataPartListEntry partListEntry = getOwnerAssembly().getPartListEntryFromKLfdNrUnfiltered(getAsId().getKLfdnr());
            if (partListEntry instanceof iPartsDataPartListEntry) {
                iPartsFactoryData otherFactoryData = ((iPartsDataPartListEntry)partListEntry).factoryDataForRetailUnfiltered;
                if (otherFactoryData != null) {
                    factoryDataForRetailUnfiltered = otherFactoryData.cloneMe();
                }
            }
            factoryDataForRetailLoaded = true; // eigentlich partListEntry.factoryDataForRetailLoaded, aber mehrmalige Versuche würden ja nichts bringen, wenn es false ist
        }

        // Bei versorgungsrelevanten Baureihen müssen die Ersetzungen wegen den virtuellen "PEM ab/bis auswerten"-Flags
        // geladen sein
        if (!replacementsLoaded && isSeriesRelevantForImport()) {
            loadReplacements(); // Ruft am Ende updatePEMFlagsFromReplacements() auf
        }

        return factoryDataForRetailUnfiltered;
    }

    /**
     * Liefert alle Werkseinsatzdaten für den Retail zu diesem PartListEntry MIT Filterung aber OHNE Berücksichtigung von
     * Ersetzungen.
     *
     * @return
     */
    public iPartsFactoryData getFactoryDataForRetailWithoutReplacements() {
        if (!factoryDataForRetailWithoutReplacementsCalculated) {
            // Werkseinsatzdaten ohne Filterung verwenden, wenn die Werkseinsatzdaten mit Filterung (aber ohne Berücksichtigung
            // von Ersetzungen) nicht explizit durch den Baumuster-Filter gesetzt wurden
            setFactoryDataForRetailWithoutReplacements(getFactoryDataForRetailUnfiltered());
        }
        return factoryDataForRetailWithoutReplacements;
    }

    /**
     * Liefert alle Werkseinsatzdaten für den Retail zu diesem PartListEntry MIT Berücksichtigung von Ersetzungen (diese
     * werden auf Basis der Werkseinsatzdaten für den Retail OHNE Berücksichtigung von Ersetzungen zusammen mit den
     * Ersetzungen dieses Stücklisteneintrags berechnet).
     *
     * @return
     */
    public iPartsFactoryData getFactoryDataForRetail() {
        if (!factoryDataForRetailCalculated) {
            factoryDataForRetail = getFactoryDataChainForReplacements();
            factoryDataForRetailCalculated = true;
        }
        return factoryDataForRetail;
    }

    /**
     * Setzt alle Werkseinsatzdaten für den Retail zu diesem PartListEntry sowohl OHNE als auch MIT Berücksichtigung von Ersetzungen.
     * <br/>Diese Methode sollte normalerweise nur im Zusammenhang mit dem vollständigen Klonen eines Stücklisteneintrags
     * verwendet werden. Ansonsten den Weg über {@link #setFactoryDataForRetailUnfiltered(iPartsFactoryData)} bzw.
     * {@link #setFactoryDataForRetailWithoutReplacements(iPartsFactoryData)} gehen.
     *
     * @param factoryDataForRetailWithoutReplacements
     * @param factoryDataForRetail
     */
    public void setFactoryDataForRetail(iPartsFactoryData factoryDataForRetailWithoutReplacements, iPartsFactoryData factoryDataForRetail) {
        this.factoryDataForRetailWithoutReplacements = factoryDataForRetailWithoutReplacements;
        factoryDataForRetailWithoutReplacementsCalculated = true;
        this.factoryDataForRetail = factoryDataForRetail;
        factoryDataForRetailCalculated = true;
    }

    /**
     * Liefert alle Werkseinsatzdaten für die Konstruktion zu diesem PartListEntry
     *
     * @return
     */
    public iPartsFactoryData getFactoryDataForConstruction() {
        if (!factoryDataForConstructionLoaded) {
            // Nachladen über die Stückliste (langsam, ist aber normalerweise nur bei der Suche notwendig)
            EtkDataPartListEntry partListEntry = getOwnerAssembly().getPartListEntryFromKLfdNrUnfiltered(getAsId().getKLfdnr());
            if (partListEntry instanceof iPartsDataPartListEntry) {
                iPartsFactoryData otherFactoryData = ((iPartsDataPartListEntry)partListEntry).factoryDataForConstruction;
                if (otherFactoryData != null) {
                    factoryDataForConstruction = otherFactoryData.cloneMe();
                }
            }
            factoryDataForConstructionLoaded = true; // eigentlich partListEntry.factoryDataForRetailLoaded, aber mehrmalige Versuche würden ja nichts bringen, wenn es false ist
        }
        return factoryDataForConstruction;
    }

    /**
     * Setzt alle Werkseinsatzdaten für den Retail zu diesem PartListEntry OHNE Filterung und OHNE Berücksichtigung von
     * Ersetzungen.
     *
     * @param factoryDataForRetailUnfiltered
     */
    public void setFactoryDataForRetailUnfiltered(iPartsFactoryData factoryDataForRetailUnfiltered) {
        this.factoryDataForRetailUnfiltered = factoryDataForRetailUnfiltered;
        factoryDataForRetailLoaded = true;
        timeSliceDatesCalculatedForModelScoring = false;
        timeSliceDateFromForModelScoring = 0;
        timeSliceDateToForModelScoring = Long.MAX_VALUE;
        timeSliceDatesLoadedUnfiltered = false;
        timeSliceDateFromUnfiltered = 0;
        timeSliceDateToUnfiltered = Long.MAX_VALUE;

        // Werkseinsatzdaten für den Retail ohne und mit Berücksichtigung von Ersetzungen müssen neu berechnet werden
        clearFactoryDataForRetail();
    }

    /**
     * Setzt alle Werkseinsatzdaten für den Retail zu diesem PartListEntry MIT Filterung aber OHNE Berücksichtigung von
     * Ersetzungen. In diesem Setter findet auch die Verdichtung von Zusatzwerken statt.
     *
     * @param factoryDataForRetailWithoutReplacements
     */
    public void setFactoryDataForRetailWithoutReplacements(iPartsFactoryData factoryDataForRetailWithoutReplacements) {
        // Werkseinsatzdaten für den Retail mit Berücksichtigung von Ersetzungen müssen neu berechnet werden
        clearFactoryDataForRetail();

        // Zusatzwerke verdichten
        if ((factoryDataForRetailWithoutReplacements != null) && factoryDataForRetailWithoutReplacements.hasValidFactories()) {
            // Ungefilterte Werkseinsatzdaten nicht überschreiben
            if (factoryDataForRetailWithoutReplacements == factoryDataForRetailUnfiltered) {
                factoryDataForRetailWithoutReplacements = factoryDataForRetailWithoutReplacements.cloneMe();
            }

            String aggregateType = getAggregateType();
            boolean isAggregateTypeCar = aggregateType.equals(iPartsConst.AGGREGATE_TYPE_CAR);

            // Unterscheidung zwischen Fahrzeug und Aggregaten
            int endNumberLength;
            if (isAggregateTypeCar) {
                endNumberLength = FinId.IDENT_NO_LENGTH;
            } else {
                // Beim Millionenüberlauf spielt auch die Länge der Endnummer eine Rolle. Diese hängt vom Aggregate-Ident ab, den wir hier nicht haben.
                // Wir gehen daher den Umweg über eine virtuelle Aggregatedatenkarte mit Dummy Ident
                endNumberLength = AggregateDataCard.getEndNumberLengthForAggregateType(getEtkProject(), aggregateType);
            }

            iPartsFactoryModel factoryModelCache = iPartsFactoryModel.getInstance(getEtkProject());
            List<List<iPartsFactoryData.DataForFactory>> dataForFactoriesList = new DwList<>(factoryDataForRetailWithoutReplacements.getFactoryDataMap().values());
            for (List<iPartsFactoryData.DataForFactory> addDataForFactories : dataForFactoriesList) {
                for (iPartsFactoryData.DataForFactory addDataForFactory : addDataForFactories) {
                    /**
                     * Die Namen addXXX sind etwas unglücklich weil sich erst weiter unten herausstellt ob es sich um Zusatzwerke
                     * handelt und damit der Prefix "add" gerechtfertigt ist.
                     */

                    // Im Retail-Fall nur die als "freigegeben" gekennzeichneten Werkseinsatzdaten durchlassen.
                    if (addDataForFactory.releaseState != iPartsDataReleaseState.RELEASED) {
                        continue;
                    }

                    String addFactoryNumber = addDataForFactory.factoryDataId.getFactory();
                    String mainFactoryNumber = factoryModelCache.getMainFactoryForAdditionalFactory(addFactoryNumber,
                                                                                                    new iPartsSeriesId(addDataForFactory.seriesNumber));
                    /**
                     * Zusatzwerkverdichtung
                     */
                    if (mainFactoryNumber != null) { // Es handelt sich um ein Zusatzwerk
                        List<iPartsFactoryData.DataForFactory> mainDataForFactories = factoryDataForRetailWithoutReplacements.getDataForFactory(mainFactoryNumber);
                        if ((mainDataForFactories != null) && !mainDataForFactories.isEmpty()) {
                            // Es gibt nach dem Retailfilter immer nur einen Werkseinsatzdaten-Datensatz pro Werk
                            iPartsFactoryData.DataForFactory mainDataForFactory = mainDataForFactories.get(0);

                            // Verdichtung für PEM ab (nur falls es überhaupt eine PEM ab gibt am Zusatzwerk)
                            iPartsModelId mainFactoryModelId = new iPartsModelId(mainDataForFactory.seriesNumber);
                            iPartsModelId addFactoryModelId = new iPartsModelId(addDataForFactory.seriesNumber);
                            if (addDataForFactory.hasPEMFrom()) {
                                int minMainFactoryEndNumberFrom = Integer.MAX_VALUE;
                                List<iPartsFactoryData.Ident> mainFactoryIdentsFrom = mainDataForFactory.getIdentsFrom(endNumberLength);
                                if (mainFactoryIdentsFrom != null) {
                                    for (iPartsFactoryData.Ident mainFactoryIdentFrom : mainFactoryIdentsFrom) {
                                        int mainFactoryEndNumberFrom = factoryModelCache.getSerialNumberWithOverflowForFactoryNumber(mainFactoryNumber,
                                                                                                                                     mainFactoryIdentFrom.factorySign,
                                                                                                                                     mainFactoryModelId,
                                                                                                                                     aggregateType,
                                                                                                                                     mainFactoryIdentFrom.endNumber,
                                                                                                                                     endNumberLength);
                                        if (mainFactoryEndNumberFrom != FinId.INVALID_SERIAL_NUMBER) {
                                            minMainFactoryEndNumberFrom = Math.min(minMainFactoryEndNumberFrom, mainFactoryEndNumberFrom);
                                        }
                                    }
                                }

                                int minAddFactoryEndNumberFrom = Integer.MAX_VALUE;
                                List<iPartsFactoryData.Ident> addFactoryIdentsFrom = addDataForFactory.getIdentsFrom(endNumberLength);
                                if (addFactoryIdentsFrom != null) {
                                    for (iPartsFactoryData.Ident addFactoryIdentFrom : addFactoryIdentsFrom) {
                                        int addFactoryEndNumberFrom = factoryModelCache.getSerialNumberWithOverflowForFactoryNumber(addFactoryNumber,
                                                                                                                                    addFactoryIdentFrom.factorySign,
                                                                                                                                    addFactoryModelId,
                                                                                                                                    aggregateType,
                                                                                                                                    addFactoryIdentFrom.endNumber,
                                                                                                                                    endNumberLength);
                                        if (addFactoryEndNumberFrom != FinId.INVALID_SERIAL_NUMBER) {
                                            minAddFactoryEndNumberFrom = Math.min(minAddFactoryEndNumberFrom, addFactoryEndNumberFrom);
                                        }
                                    }
                                }

                                boolean isAddFactoryPEMFrom = false;
                                if ((minMainFactoryEndNumberFrom == Integer.MAX_VALUE) && (minAddFactoryEndNumberFrom == Integer.MAX_VALUE)) {
                                    // Hauptwerk und Zusatzwerk haben keinen gültigen Ident -> kleinstes PEM Datum ab gewinnt
                                    if (addDataForFactory.dateFrom < mainDataForFactory.dateFrom) { // Zusatzwerk gewinnt
                                        isAddFactoryPEMFrom = true;
                                    }
                                } else { // kleinste Endnummer nach Millionenüberlauf gewinnt
                                    if (minAddFactoryEndNumberFrom < minMainFactoryEndNumberFrom) { // Zusatzwerk gewinnt
                                        isAddFactoryPEMFrom = true;
                                    }
                                }

                                if (isAddFactoryPEMFrom) { // PEM ab vom Hauptwerk durch das Zusatzwerk ersetzen
                                    mainDataForFactory.addFilterInfo(TranslationHandler.translate("!!PEM ab \"%1\" ersetzt durch Zusatzwerk %2",
                                                                                                  mainDataForFactory.pemFrom,
                                                                                                  addFactoryNumber));
                                    mainDataForFactory.pemFrom = addDataForFactory.pemFrom;
                                    mainDataForFactory.dateFrom = addDataForFactory.dateFrom;
                                    mainDataForFactory.stCodeFrom = addDataForFactory.stCodeFrom;
                                    mainDataForFactory.assignIdentsFrom(addDataForFactory);
                                }
                            }

                            // Verdichtung für PEM bis (nur falls es überhaupt eine PEM bis gibt am Zusatzwerk)
                            if (addDataForFactory.hasPEMTo()) {
                                int maxMainFactoryEndNumberTo = Integer.MIN_VALUE;
                                List<iPartsFactoryData.Ident> mainFactoryIdentsTo = mainDataForFactory.getIdentsTo(endNumberLength);
                                if (mainFactoryIdentsTo != null) {
                                    for (iPartsFactoryData.Ident mainFactoryIdentTo : mainFactoryIdentsTo) {
                                        int mainFactoryEndNumberTo = factoryModelCache.getSerialNumberWithOverflowForFactoryNumber(mainFactoryNumber,
                                                                                                                                   mainFactoryIdentTo.factorySign,
                                                                                                                                   mainFactoryModelId,
                                                                                                                                   aggregateType,
                                                                                                                                   mainFactoryIdentTo.endNumber,
                                                                                                                                   endNumberLength);
                                        if (mainFactoryEndNumberTo != FinId.INVALID_SERIAL_NUMBER) {
                                            maxMainFactoryEndNumberTo = Math.max(maxMainFactoryEndNumberTo, mainFactoryEndNumberTo);
                                        }
                                    }
                                }

                                int maxAddFactoryEndNumberTo = Integer.MIN_VALUE;
                                List<iPartsFactoryData.Ident> addFactoryIdentsTo = addDataForFactory.getIdentsTo(endNumberLength);
                                if (addFactoryIdentsTo != null) {
                                    for (iPartsFactoryData.Ident addFactoryIdentTo : addFactoryIdentsTo) {
                                        int addFactoryEndNumberTo = factoryModelCache.getSerialNumberWithOverflowForFactoryNumber(addFactoryNumber,
                                                                                                                                  addFactoryIdentTo.factorySign,
                                                                                                                                  addFactoryModelId,
                                                                                                                                  aggregateType,
                                                                                                                                  addFactoryIdentTo.endNumber,
                                                                                                                                  endNumberLength);
                                        if (addFactoryEndNumberTo != FinId.INVALID_SERIAL_NUMBER) {
                                            maxAddFactoryEndNumberTo = Math.max(maxAddFactoryEndNumberTo, addFactoryEndNumberTo);
                                        }
                                    }
                                }

                                boolean isAddFactoryPEMTo = false;
                                if ((maxMainFactoryEndNumberTo == Integer.MIN_VALUE) && (maxAddFactoryEndNumberTo == Integer.MIN_VALUE)) {
                                    // Hauptwerk und Zusatzwerk haben keinen gültigen Ident -> größtes PEM Datum bis gewinnt
                                    long tempAddDataForFactoryDateTo = addDataForFactory.dateTo;
                                    long tempMainDataForFactoryDateTo = mainDataForFactory.dateTo;
                                    // Für den Check muss ein "0" Wert auf unendlich gestellt werden
                                    if (tempAddDataForFactoryDateTo == 0) {
                                        tempAddDataForFactoryDateTo = Long.MAX_VALUE;
                                    }
                                    if (tempMainDataForFactoryDateTo == 0) {
                                        tempMainDataForFactoryDateTo = Long.MAX_VALUE;
                                    }
                                    if (tempAddDataForFactoryDateTo > tempMainDataForFactoryDateTo) { // Zusatzwerk gewinnt
                                        isAddFactoryPEMTo = true;
                                    }
                                } else { // größte Endnummer nach Millionenüberlauf gewinnt
                                    if (maxAddFactoryEndNumberTo > maxMainFactoryEndNumberTo) { // Zusatzwerk gewinnt
                                        isAddFactoryPEMTo = true;
                                    }
                                }

                                if (isAddFactoryPEMTo) { // PEM bis vom Hauptwerk durch das Zusatzwerk ersetzen
                                    mainDataForFactory.addFilterInfo(TranslationHandler.translate("!!PEM bis \"%1\" ersetzt durch Zusatzwerk %2",
                                                                                                  mainDataForFactory.pemTo,
                                                                                                  addFactoryNumber));
                                    mainDataForFactory.pemTo = addDataForFactory.pemTo;
                                    mainDataForFactory.dateTo = addDataForFactory.dateTo;
                                    mainDataForFactory.stCodeTo = addDataForFactory.stCodeTo;
                                    mainDataForFactory.assignIdentsTo(addDataForFactory);
                                }
                            }
                        } else { // Zusatzwerk unter der Nummer des Hauptwerks abspeichern
                            for (iPartsFactoryData.DataForFactory dataForFactory : addDataForFactories) {
                                dataForFactory.addFilterInfo(TranslationHandler.translate("!!Zusatzwerk %1 geändert in Hauptwerk %2",
                                                                                          addFactoryNumber, mainFactoryNumber));
                            }
                            factoryDataForRetailWithoutReplacements.setDataForFactory(mainFactoryNumber, addDataForFactories);
                        }

                        // Datensatz für Zusatzwerksnummer entfernen (es gibt nach dem Retailfilter immer nur
                        // einen Werkseinsatzdaten-Datensatz pro (Zusatz-)Werk)
                        factoryDataForRetailWithoutReplacements.removeDataForFactory(addFactoryNumber);
                        break; // Eigentlich überflüssig, weil es nur einen Eintrag geben sollte -> trotzdem zur Sicherheit
                    }
                }
            }
        }

        this.factoryDataForRetailWithoutReplacements = factoryDataForRetailWithoutReplacements;
        factoryDataForRetailWithoutReplacementsCalculated = true;
    }

    /**
     * Entfernt die Werkseinsatzdaten für den Retail zu diesem PartListEntry MIT Filterung und Berücksichtigung von Ersetzungen,
     * damit diese bei der ersten Verwendung aufgrund der Werkseinsatzdaten für den Retail OHNE Filterung neu berechnet werden.
     */
    public void clearFactoryDataForRetail() {
        factoryDataForRetailCalculated = false;
        factoryDataForRetailWithoutReplacementsCalculated = false;
        factoryDataForRetailWithoutReplacements = null;
        factoryDataForRetail = null;
    }

    /**
     * Setzt alle Werkseinsatzdaten für die Konstruktion zu diesem PartListEntry
     *
     * @param factoryDataForConstruction
     */
    public void setFactoryDataForConstruction(iPartsFactoryData factoryDataForConstruction) {
        this.factoryDataForConstruction = factoryDataForConstruction;
        factoryDataForConstructionLoaded = true;
    }

    /**
     * Liefert alle Farbvarianten für den Retail zu diesem PartListEntry ohne Filterung für das aktuelle Baumuster bzw.
     * Datenkarte
     *
     * @return
     */
    public iPartsColorTable getColorTableForRetailWithoutFilter() {
        if (!colorTableLoaded) {
            // Nachladen über die Stückliste (langsam, ist aber normalerweise nur bei der Suche notwendig)
            EtkDataPartListEntry partListEntry = getOwnerAssembly().getPartListEntryFromKLfdNrUnfiltered(getAsId().getKLfdnr());
            if (partListEntry instanceof iPartsDataPartListEntry) {
                iPartsColorTable otherColorTable = ((iPartsDataPartListEntry)partListEntry).colorTableForRetailWithoutFilter;
                if (otherColorTable != null) {
                    colorTableForRetailWithoutFilter = otherColorTable.cloneMe();
                }
            }
            colorTableLoaded = true; // eigentlich partListEntry.colorTableLoaded, aber mehrmalige Versuche würden ja nichts bringen, wenn es false ist
        }
        return colorTableForRetailWithoutFilter;
    }

    /**
     * Setzt alle Farbvarianten für den Retail zu diesem PartListEntry ohne Filterung für das aktuelle Baumuster bzw. Datenkarte
     *
     * @param colorTableForRetailWithoutFilter
     */
    public void setColorTableForRetailWithoutFilter(iPartsColorTable colorTableForRetailWithoutFilter) {
        this.colorTableForRetailWithoutFilter = colorTableForRetailWithoutFilter;
        colorTableLoaded = true;
        clearColorTableForRetailFiltered();
    }

    /**
     * Liefert alle Farbvarianten für die Konstruktion zu diesem PartListEntry ohne Werkseinsatzdaten (Werden bei der
     * Konstruktionssicht direkt aus der DB geladen)
     *
     * @return
     */
    public iPartsColorTable getColorTableForConstruction() {
        if (!colorTableConstructionLoaded) {
            EtkDataPartListEntry partListEntry = getOwnerAssembly().getPartListEntryFromKLfdNrUnfiltered(getAsId().getKLfdnr());
            if (partListEntry instanceof iPartsDataPartListEntry) {
                iPartsColorTable otherColorTable = ((iPartsDataPartListEntry)partListEntry).colorTableForConstruction;
                if (otherColorTable != null) {
                    colorTableForConstruction = otherColorTable.cloneMe();
                }
            }
            colorTableConstructionLoaded = true;
        }
        return colorTableForConstruction;
    }

    /**
     * Setzt alle Farbvarianten für die Konstruktion zu diesem PartListEntry (ohne Werkseinsatzdaten)
     *
     * @param colorTableForConstruction
     */
    public void setColorTableForConstruction(iPartsColorTable colorTableForConstruction) {
        this.colorTableForConstruction = colorTableForConstruction;
        colorTableConstructionLoaded = true;
    }

    /**
     * Liefert alle für das aktuelle Baumuster bzw. Datenkarte gefilterten Farbvarianten für den Retail zu diesem PartListEntry
     *
     * @return
     */
    public iPartsColorTable getColorTableForRetail() {
        if (!colorTableFiltered) {
            colorTableForRetail = iPartsFilter.get().getColorTableForRetailFiltered(getColorTableForRetailWithoutFilter(), this);
            colorTableFiltered = true;
        }
        return colorTableForRetail;
    }

    /**
     * Setzt alle für das aktuelle Baumuster bzw. Datenkarte gefilterten Farbvarianten für den Retail zu diesem PartListEntry
     * zurück, so dass diese beim nächsten Aufruf von {@link #getColorTableForRetail()} neu bestimmt werden.
     */
    public void clearColorTableForRetailFiltered() {
        colorTableFiltered = false;
        colorTableForRetail = null;
    }

    /**
     * Lädt alle Vorgänger- und Nachfolger-Ersetzungen zu diesem PartListEntry. Wird im Normalfall nicht aufgerufen,
     * da diese schon beim Laden der Stückliste in {@link iPartsDataAssembly#loadAllReplacementsForPartList(DBDataObjectList)}
     * mitgeladen werden. Wird also nur aufgerufen, wenn der Stücklisteneintrag einzeln erzeugt wurde.
     */
    public void loadReplacements() {
        List<iPartsReplacement> predecessors = new DwList<>();
        List<iPartsReplacement> successors = new DwList<>();
        iPartsReplacementHelper.loadReplacementsForPartListEntry(iPartsDataReleaseState.getReplacementRelevantStatesDBValues(),
                                                                 predecessors, successors, this, getOwnerAssembly().getDocumentationType().isTruckDocumentationType());

        setReplacements(predecessors, successors); // Ruft auch updatePEMFlagsFromReplacements() auf
    }

    /**
     * Gibt an, ob Vorgänger zum aktuellen Teil existieren (ohne Bersücksichtigung vom Filter). Lädt bei Bedarf die Vorgänger-Teile.
     *
     * @return
     */
    public boolean hasPredecessors() {
        return hasPredecessors(false);
    }

    /**
     * Gibt an, ob Vorgänger zum aktuellen Teil existieren. Lädt bei Bedarf die Vorgänger-Teile.
     *
     * @param withIPartsFilter Flag, ob nur solche Vorgänger berücksichtigt werden sollen, die nach der Filterung noch vorhanden sind
     * @return
     */
    public boolean hasPredecessors(boolean withIPartsFilter) {
        return hasPredecessors(withIPartsFilter ? iPartsFilter.get() : null);
    }

    /**
     * Gibt an, ob Vorgänger zum aktuellen Teil existieren. Lädt bei Bedarf die Vorgänger-Teile.
     *
     * @param filter Filter, der für die Filterung der Vorgänger verwendet werden soll
     * @return
     */
    public boolean hasPredecessors(iPartsFilter filter) {
        Collection<iPartsReplacement> predecessorsWithFilter = getPredecessors(filter);
        return ((predecessorsWithFilter != null) && !predecessorsWithFilter.isEmpty());
    }

    /**
     * Gibt an, ob an diesem Teil nur virtuelle Vorgänger aus Vererbung existieren.
     * Ein Vorgänger ist virtuell aus Vererbung, wenn es zu ihm keinen Datensatz in der DB gibt, sondern er beim Laden der
     * Stückliste für einen Vorgängerstand eines Teils mit Vorgänger erzeugt wurde.
     * Diese Funktion gibt keine Auskunft darüber, ob es grundsätzlich Vorgänger gibt. Dazu muss {@link #hasPredecessors()} ()}
     * aufgerufen werden.
     *
     * @param isFilterActive
     * @return <code>false</code> sobald es mindestens einen echten Vorgänger gibt, sonst <code>true</code>
     */
    public boolean isPredecessorVirtualInherited(boolean isFilterActive) {
        Collection<iPartsReplacement> predecessors = getPredecessors(isFilterActive);
        if (predecessors != null) {
            for (iPartsReplacement predecessor : predecessors) {
                if (!predecessor.isVirtualInherited()) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Liefert alle Vorgänger-Teile für diesen PartListEntry (ohne Bersücksichtigung vom Filter)
     *
     * @return
     */
    public Collection<iPartsReplacement> getPredecessors() {
        return getPredecessors(false);
    }

    /**
     * Liefert alle Vorgänger-Teile für diesen PartListEntry
     *
     * @param withIPartsFilter Flag, ob nur solche Vorgänger berücksichtigt werden sollen, die nach der Filterung noch vorhanden sind
     * @return
     */
    public List<iPartsReplacement> getPredecessors(boolean withIPartsFilter) {
        return getPredecessors(withIPartsFilter ? iPartsFilter.get() : null);
    }

    /**
     * Liefert alle Vorgänger-Teile für diesen PartListEntry
     *
     * @param filter Filter, der für die Filterung der Vorgänger verwendet werden soll
     * @return
     */
    public List<iPartsReplacement> getPredecessors(iPartsFilter filter) {
        if (!replacementsLoaded) {
            loadReplacements();
        }
        if ((filter == null) || (predecessors == null) || predecessors.isEmpty()) {
            return predecessors;
        }

        // Filterung
        boolean mainFilterSwitchActive = filter.getSwitchboardState().isMainSwitchActive();
        List<iPartsReplacement> filteredPredecessors = new DwList<>(predecessors.size());

        // Durch filter.checkFilter() können über replaceMaterialByPRIMUSSuccessorIfNeeded() die predecessors verändert werden,
        // was zu einer ConcurrentModificationException führt -> predecessors in ein Array kopieren und darüber iterieren
        for (iPartsReplacement predecessor : ArrayUtil.toArray(predecessors)) {
            // Nicht austauschbare Ersetzungen unterdrücken bei aktivem Filter-Hauptschalter
            if (mainFilterSwitchActive && predecessor.isNotReplaceable()) {
                continue;
            }

            if (filter.checkFilter(predecessor.predecessorEntry)) {
                filteredPredecessors.add(predecessor);
            }
        }
        return filteredPredecessors;
    }

    /**
     * Gibt an, ob Nachfolger zum aktuellen Teil existieren (ohne Bersücksichtigung vom Filter). Lädt bei Bedarf die Nachfolger-Teile.
     *
     * @return
     */
    public boolean hasSuccessors() {
        return hasSuccessors(false);
    }

    /**
     * Gibt an, ob Nachfolger zum aktuellen Teil existieren. Lädt bei Bedarf die Nachfolger-Teile.
     *
     * @param withIPartsFilter Flag, ob nur solche Nachfolger berücksichtigt werden sollen, die nach der Filterung noch vorhanden sind
     * @return
     */
    public boolean hasSuccessors(boolean withIPartsFilter) {
        return hasSuccessors(withIPartsFilter ? iPartsFilter.get() : null);
    }

    /**
     * Gibt an, ob Nachfolger zum aktuellen Teil existieren. Lädt bei Bedarf die Nachfolger-Teile.
     *
     * @param filter Filter, der für die Filterung der Ersetzungen verwendet werden soll
     * @return
     */
    public boolean hasSuccessors(iPartsFilter filter) {
        Collection<iPartsReplacement> successorsWithFilter = getSuccessors(filter);
        return ((successorsWithFilter != null) && !successorsWithFilter.isEmpty());
    }

    /**
     * Gibt an, ob an diesem Teil nur virtuelle Nachfolger aus Vererbung existieren.
     * Ein Nachfolger ist virtuell aus Vererbung, wenn es zu ihm keinen Datensatz in der DB gibt, sondern er beim Laden der
     * Stückliste für einen Nachfolgerstand eines Teils mit Nachfolger erzeugt wurde.
     * Diese Funktion gibt keine Auskunft darüber, ob es grundsätzlich Nachfolger gibt. Dazu muss {@link #hasSuccessors()}
     * aufgerufen werden.
     *
     * @param isFilterActive
     * @return <code>false</code> sobald es mindestens einen echten Nachfolger gibt, sonst <code>true</code>
     */
    public boolean isSuccessorVirtualInherited(boolean isFilterActive) {
        Collection<iPartsReplacement> successors = getSuccessors(isFilterActive);
        if (successors != null) {
            for (iPartsReplacement successor : successors) {
                if (!successor.isVirtualInherited()) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Liefert alle Nachfolger-Teile für diesen PartListEntry (ohne Bersücksichtigung vom Filter)
     *
     * @return
     */
    public Collection<iPartsReplacement> getSuccessors() {
        return getSuccessors(false);
    }

    /**
     * Liefert alle Nachfolger-Teile für diesen PartListEntry
     *
     * @param withIPartsFilter Flag, ob nur solche Nachfolger berücksichtigt werden sollen, die nach der Filterung mit
     *                         dem Standard-Iparts-Filter noch vorhanden sind
     * @return
     */
    public List<iPartsReplacement> getSuccessors(boolean withIPartsFilter) {
        return getSuccessors(withIPartsFilter ? iPartsFilter.get() : null);
    }

    /**
     * Liefert alle Nachfolger-Teile für diesen PartListEntry. Falls gefiltert wird, greifen Ausnahmen für bestimmte RFME Flags und
     * für ELDAS Stücklisten, bei denen die Nachfolger obwohl sie in der Stückliste gefiltert sind, trotzdem ausgegeben werden.
     *
     * @param filter Filter, der für die Filterung der Nachfolger verwendet werden soll
     * @return
     */
    public List<iPartsReplacement> getSuccessors(iPartsFilter filter) {
        if (filteredSuccessorsCalculated) {
            return filteredSuccessors;
        }

        if (!replacementsLoaded) {
            loadReplacements();
        }

        // DAIMLER-4782: Sonderbehandlung für die Nachfolger bei ELDAS: NACHFOLGER sollen angezeigt werden, auch wenn sie normal ausgefilter werden würden!
        if (getOwnerAssembly().getDocumentationType().isTruckDocumentationType()) {
            return successors;
        }

        if ((filter == null) || !Utils.isValid(successors)) {
            // PRIMUS-Nachfolger hinzufügen, falls notwendig
            iPartsPRIMUSReplacementsLoader primusReplacementsLoader = new iPartsPRIMUSReplacementsLoader(getEtkProject(), this);
            List<iPartsReplacement> filteredSuccessorsLocal = primusReplacementsLoader.addPrimusReplacementsForPartListEntry(successors, false);
            if (filter != null) {
                filteredSuccessors = filteredSuccessorsLocal;
                filteredSuccessorsCalculated = true;
            }
            return filteredSuccessorsLocal;
        }

        // Filterung
        boolean mainFilterSwitchActive = filter.getSwitchboardState().isMainSwitchActive();
        List<iPartsReplacement> filteredSuccessorsLocal = new DwList<>(successors.size());

        // Durch filter.checkFilter() können über replaceMaterialByPRIMUSSuccessorIfNeeded() die successors verändert werden,
        // was zu einer ConcurrentModificationException führt -> successors in ein Array kopieren und darüber iterieren
        for (iPartsReplacement successor : ArrayUtil.toArray(successors)) {
            // Nicht austauschbare Ersetzungen unterdrücken bei aktivem Filter-Hauptschalter
            if (mainFilterSwitchActive && successor.isNotReplaceable()) {
                continue;
            }

            if (successor.isVisibleIfSuccessorFiltered() || (successor.successorEntry == null) || filter.checkFilter(successor.successorEntry)) {
                filteredSuccessorsLocal.add(successor);
            }
        }

        // PRIMUS-Nachfolger hinzufügen, falls notwendig
        iPartsPRIMUSReplacementsLoader primusReplacementsLoader = new iPartsPRIMUSReplacementsLoader(getEtkProject(), this);
        filteredSuccessorsLocal = primusReplacementsLoader.addPrimusReplacementsForPartListEntry(filteredSuccessorsLocal, false);
        filteredSuccessors = filteredSuccessorsLocal;
        filteredSuccessorsCalculated = true;

        return filteredSuccessorsLocal;
    }

    public String getSDATA() {
        return getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_SDATA);
    }

    public String getSDATB() {
        return getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_SDATB);
    }

    /**
     * Setzt alle Ersetzungen (Vorgänger- und Nachfolger-Teile) für diesen PartListEntry
     *
     * @param predecessors
     * @param successors
     */
    public void setReplacements(List<iPartsReplacement> predecessors, List<iPartsReplacement> successors) {
        if ((predecessors != null) && predecessors.isEmpty()) {
            predecessors = null;
        }
        if ((successors != null) && successors.isEmpty()) {
            successors = null;
        }
        this.predecessors = predecessors;
        this.successors = successors;
        replacementsLoaded = true;
        clearFilteredReplacements();
        updatePEMFlagsFromReplacements();
    }

    /**
     * Löscht alle Ersetzungen an diesem Stücklisteneintrag
     */
    public void clearReplacements() {
        this.predecessors = null;
        this.successors = null;
        replacementsLoaded = false;
        clearFilteredReplacements();
        materialReplacedByPRIMUSSuccessor = false;

        // Spätere Neuberechnung der virtuellen Felder für die berechneten Flags "Auswertung PEM ab/bis" erzwingen durch
        // Löschen der Attribute
        getAttributes().deleteField(iPartsDataVirtualFieldsDefinition.RETAIL_EVAL_PEM_FROM_CALCULATED, false, DBActionOrigin.FROM_DB);
        getAttributes().deleteField(iPartsDataVirtualFieldsDefinition.RETAIL_EVAL_PEM_TO_CALCULATED, false, DBActionOrigin.FROM_DB);
    }

    /**
     * Löscht alle gefilterten Ersetzungen an diesem Stücklisteneintrag
     */
    public void clearFilteredReplacements() {
        filteredSuccessorsCalculated = false;
        filteredSuccessors = null;
    }

    /**
     * Überprüft, ob es bei gesetztem Flag {@link #FIELD_K_USE_PRIMUS_SUCCESSOR} einen PRIMUS-Nachfolger gibt mit PRIMUS-Vorwärts-Code
     * {@code 22} und ersetzt in diesem Fall das Material durch das Material vom direkten Nachfolger.
     *
     * @param filter
     */
    public void replaceMaterialByPRIMUSSuccessorIfNeeded(iPartsFilter filter) {
        if (!materialReplacedByPRIMUSSuccessor && getFieldValueAsBoolean(FIELD_K_USE_PRIMUS_SUCCESSOR) && hasSuccessors()) {
            iPartsReplacement foundPRIMUSReplacement = null;
            for (iPartsReplacement successor : getSuccessors()) {
                if (successor.isVirtual() && (successor.source == iPartsReplacement.Source.PRIMUS) && (Utils.objectEquals(successor.primusCodeForward,
                                                                                                                          PRIMUS_CODE_22_HAS_SUCCESSOR)
                                                                                                       || Utils.objectEquals(successor.primusCodeForward,
                                                                                                                             PRIMUS_CODE_28_HAS_SUCCESSOR))) {
                    String matNr = getFieldValue(FIELD_K_MATNR);
                    iPartsPRIMUSReplacementCacheObject replacementCacheObject = iPartsPRIMUSReplacementsCache.getInstance(getEtkProject()).getReplacementCacheObjectForMatNr(matNr);
                    if (replacementCacheObject != null) { // Kann eigentlich nicht null sein, aber sicher ist sicher...
                        materialReplacedByPRIMUSSuccessor = true; // Mehrfache Ersetzungen verhindern
                        foundPRIMUSReplacement = successor;
                        String successorMatNr = replacementCacheObject.getSuccessorPartNo();

                        // Die Original-Teilenummer merken
                        getAttributes().addField(iPartsDataVirtualFieldsDefinition.DA_ORIGINAL_MAT_NR, matNr, true, DBActionOrigin.FROM_DB);

                        // Materialnummer vom Nachfolger setzen und das Material durch clear() beim nächsten Zugriff neu laden
                        setFieldValue(FIELD_K_MATNR, successorMatNr, DBActionOrigin.FROM_DB);
                        getPart().clear(DBActionOrigin.FROM_DB);

                        // Gleichteile-Teilenummer setzen
                        EtkDataPart part = getPart(); // Setzt intern die neue Materialnummer am Material
                        String equalPartNumber = filter.getEqualPartNumber(part);
                        if (!equalPartNumber.equals(part.getAsId().getMatNr())) {
                            if (part instanceof iPartsDataPart) {
                                ((iPartsDataPart)part).setMappedMatNr(equalPartNumber);
                            }
                        }
                    }
                    break;
                }
            }

            if (foundPRIMUSReplacement != null) {
                List<iPartsReplacement> successorsForPRIMUS = new ArrayList<>(successors);

                // Gefundene PRIMUS-Ersetzung entfernen
                successorsForPRIMUS.remove(foundPRIMUSReplacement);

                // PRIMUS-Hinweise für die Materialnummer vom direkten PRIMUS-Nachfolger prüfen: virtuelle Ersetzung hinzufügen
                iPartsPRIMUSReplacementsLoader primusReplacementsLoader = new iPartsPRIMUSReplacementsLoader(getEtkProject(), this);
                Map<String, List<iPartsReplacement>> primusSuccessorsMap = new HashMap<>();
                primusReplacementsLoader.addPrimusReplacementsForPartList(primusSuccessorsMap, false);
                List<iPartsReplacement> primusSuccessors = primusSuccessorsMap.get(getAsId().getKLfdnr());
                if (primusSuccessors != null) {
                    // PRIMUS-Nachfolger für die neue Materialnummer hinzufügen; primusPredecessorsMap kann ignoriert werden,
                    // weil bei vorhandener Ersetzungskette removePredecessorAtSuccessor in diesem Fall false wäre und der
                    // Vorgänger (dieser Stücklisteneintrag) am Nachfolger daher gar nicht erst entfernt wird.
                    successorsForPRIMUS.addAll(primusSuccessors);
                }

                // PRIMUS-Hinweise für die Materialnummer vom direkten PRIMUS-Nachfolger prüfen: virtuelle Fußnote hinzufügen
                Collection<iPartsFootNote> extendedFootnotes = iPartsVirtualFootnoteHelper.addVirtualFootnotes(this, getFootNotes(),
                                                                                                               primusReplacementsLoader);
                setFootNotes(extendedFootnotes);

                successors = successorsForPRIMUS;
            }
        }
    }

    public boolean hasSuccessorsConst() {
        return (successorsConst != null);
    }

    public boolean hasPredecessorsConst() {
        return (predecessorsConst != null);
    }

    public Collection<iPartsReplacementConst> getSuccessorsConst() {
        return successorsConst;
    }

    public Collection<iPartsReplacementConst> getPredecessorsConst() {
        return predecessorsConst;
    }

    /**
     * Fügt eine Vorgänger-Ersetzung am Teilestamm aus der Konstruktion für diesen PartListEntry hinzu
     *
     * @param predecessorConst
     */
    public void addPredecessorConst(iPartsReplacementConst predecessorConst) {
        if (predecessorsConst == null) {
            predecessorsConst = new DwList<>();
        }
        if (predecessorConst != null) {
            this.predecessorsConst.add(predecessorConst);
        }
    }

    /**
     * Fügt eine Nachfolger-Ersetzung am Teilestamm aus der Konstruktion für diesen PartListEntry hinzu
     *
     * @param successorConst
     */
    public void addSuccessorConst(iPartsReplacementConst successorConst) {
        if (successorsConst == null) {
            successorsConst = new DwList<>();
        }
        if (successorConst != null) {
            this.successorsConst.add(successorConst);
        }
    }

    /**
     * Berechnet die "PEM ab/bis auswerten"-Flags an diesem Stücklisteneintrag und allen seinen Nachfolgern basierend auf den
     * Ersetzungen neu. Dabei werden potentiell vom Autor gesetzte Flags berücksichtigt, d.h. die Werte werden verodert.
     */
    public void updatePEMFlagsFromReplacements() {
        boolean evalPemFromCalculated = getFieldValueAsBoolean(FIELD_K_EVAL_PEM_FROM);
        boolean evalPemToCalculated = getFieldValueAsBoolean(FIELD_K_EVAL_PEM_TO);

        // Bei versorgungsrelevanten Baureihen müssen die "PEM ab/bis auswerten" anhand der Ersetzungen berechnet werden
        if (isSeriesRelevantForImport()) {
            if (!replacementsLoaded) {
                // Ruft am Ende auch wieder updatePEMFlagsFromReplacements() auf
                loadReplacements();
                return;
            }

            // PEM ab auswerten
            if (!evalPemFromCalculated && (predecessors != null)) {
                for (iPartsReplacement replacement : predecessors) {
                    if (replacement.source == iPartsReplacement.Source.PRIMUS) {
                        continue;
                    }

                    iPartsRFMEA rfmea = new iPartsRFMEA(replacement.rfmeaFlags);
                    iPartsRFMEN rfmen = new iPartsRFMEN(replacement.rfmenFlags);

                    // Muss das Flag "PEM ab auswerten" am Nachfolger (dieser Stücklisteneintrag) gesetzt werden?
                    if (iPartsReplacementHelper.isEvalPEMFrom(rfmea, rfmen)) {
                        evalPemFromCalculated = true;
                        break;
                    }
                }
            }

            // PEM bis auswerten
            if (!evalPemToCalculated && (successors != null)) {
                for (iPartsReplacement replacement : successors) {
                    if (replacement.source == iPartsReplacement.Source.PRIMUS) {
                        continue;
                    }

                    iPartsRFMEA rfmea = new iPartsRFMEA(replacement.rfmeaFlags);

                    // Muss das Flag "PEM bis auswerten" am Vorgänger (dieser Stücklisteneintrag) gesetzt werden?
                    if (rfmea.isEvalPEMToForRealReplacement()) {
                        evalPemToCalculated = true;
                        break;
                    }
                }
            }

        }

        // "PEM ab/bis auswerten"-Flags an den Werkseinsatzdaten anpassen falls notwendig
        if (factoryDataForRetailLoaded && (factoryDataForRetailUnfiltered != null)) {
            boolean setFactoryDataForRetailUnfiltered = false;
            if (factoryDataForRetailUnfiltered.isEvalPemFrom() != evalPemFromCalculated) {
                factoryDataForRetailUnfiltered.setEvalPemFrom(evalPemFromCalculated);
                setFactoryDataForRetailUnfiltered = true;
            }
            if (factoryDataForRetailUnfiltered.isEvalPemTo() != evalPemToCalculated) {
                factoryDataForRetailUnfiltered.setEvalPemTo(evalPemToCalculated);
                setFactoryDataForRetailUnfiltered = true;
            }
            if (setFactoryDataForRetailUnfiltered) {
                setFactoryDataForRetailUnfiltered(factoryDataForRetailUnfiltered);
            }
        }

        if (attributes == null) { // Stücklisteneintrag ist noch gar nicht richtig initialisiert worden -> jetzt nachholen
            if (!existsInDB()) {
                initAttributesWithDefaultValues(DBActionOrigin.FROM_DB);
            }
        }

        attributes.addField(iPartsDataVirtualFieldsDefinition.RETAIL_EVAL_PEM_FROM_CALCULATED, SQLStringConvert.booleanToPPString(evalPemFromCalculated),
                            true, DBActionOrigin.FROM_DB);
        attributes.addField(iPartsDataVirtualFieldsDefinition.RETAIL_EVAL_PEM_TO_CALCULATED, SQLStringConvert.booleanToPPString(evalPemToCalculated),
                            true, DBActionOrigin.FROM_DB);
    }

    /**
     * Ist die PEM AB in Werkseinsatzdaten MIT Ersetzungen dieses Stücklisteneintrags relevant für den Endnummern-Filter?
     *
     * @return
     */
    public boolean isPEMFromRelevant() {
        return (getFactoryDataForRetail() != null) && getFactoryDataForRetail().isEvalPemFrom();
    }

    /**
     * Ist die PEM BIS in Werkseinsatzdaten MIT Ersetzungen dieses Stücklisteneintrags relevant für den Endnummern-Filter?
     *
     * @return
     */
    public boolean isPEMToRelevant() {
        return (getFactoryDataForRetail() != null) && getFactoryDataForRetail().isEvalPemTo();
    }

    /**
     * Gibt an, ob es gültige Werkseinsatzdaten für diesen Stücklisteneintrag gibt und die PEM AB und/oder PEM BIS in den
     * Werkseinsatzdaten dieses Stücklisteneintrags relevant ist für den Endnummern-Filter und somit dieser Stücklisteneintrag
     * beim Endnummern-Filter berücksichtigt werden muss.
     * Zusätzlich müssen im Endnummern-Filter auch Werkseinsatzdaten zu nur ungültigen Werken berücksichtigt werden.
     *
     * @return
     */
    public boolean isValidFactoryDataRelevantForEndNumberFilter() {
        return hasValidFactoryDataForRetail() && (isPEMFromRelevant() || isPEMToRelevant());
    }

    /**
     * Gibt an, ob dieser Stücklisteneintrag Ersetzungen besitzt (Vorgänger oder Nachfolger).
     *
     * @return
     */
    public boolean hasReplacements() {
        return hasPredecessors() || hasSuccessors();
    }

    /**
     * Gibt an, ob dieser Stücklisteneintrag echte Ersetzungen (nicht nur virtuell vererbte) besitzt (Vorgänger oder Nachfolger).
     *
     * @return
     */
    public boolean hasRealReplacements() {
        Collection<iPartsReplacement> predecessors = getPredecessors();
        if (predecessors != null) {
            for (iPartsReplacement predecessor : predecessors) {
                if (!predecessor.isVirtual()) {
                    return true;
                }
            }
        }

        Collection<iPartsReplacement> successors = getSuccessors();
        if (successors != null) {
            for (iPartsReplacement successor : successors) {
                if (!successor.isVirtual()) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Der Owner muss immer ein iPartsDataAssembly sein
     *
     * @return
     */
    @Override
    public iPartsDataAssembly getOwnerAssembly() {
        return (iPartsDataAssembly)super.getOwnerAssembly();
    }

    /**
     * Sollen 2nd-Parts (StarParts) als Alternativteile angezeigt werden?
     *
     * @param modelPrefix Optionaler Baumuster-Präfix (falls leer bzw. {@code null} wird die Baumuster-Präfix-Prüfung übersprungen)
     * @param countryCode Land (falls leer bzw. {@code null} wird {@code true} bei Edit- und {@code false} bei
     *                    Webservice-Instanzen zurückgeliefert)
     * @return
     */
    public boolean isIncludeSecondParts(String modelPrefix, String countryCode) {
        if (StrUtils.isEmpty(countryCode)) {
            Session session = Session.get();
            // Bei den Webservices sollen keine StartParts ausgegeben werden, wenn kein Land angegeben wird
            return (session != null) && session.canHandleGui();
        }

        if (StrUtils.isValid(modelPrefix)) {
            if (!iPartsCountryValidSeriesCache.getInstance(getEtkProject()).isValidModelPrefix(modelPrefix, countryCode)) {
                return false;
            }
        }

        return iPartsCountryInvalidPartsCache.getInstance(getEtkProject()).isValidPart(getPart().getAsId().getMatNr(), countryCode);
    }

    public Set<EtkDataPart> getAlternativePartsFilteredByReplacements(String modelPrefix, String countryCode) {
        return getAlternativePartsFilteredByReplacements(modelPrefix, countryCode, false);
    }

    /**
     * Alternativteile für Stücklisteneintrag gefiltert durch die Ersetzungen bestimmen.
     * Wie {@link #getAlternativeParts(String, String, boolean)}, allerdings gefiltert durch die Ersetzungen, so dass Alternativteile
     * mit {@code ES1 = 80} und identischem ES2 ausgefiltert werden, falls es dafür auch eine Ersetzung gibt.
     *
     * @param modelPrefix Optionaler Baumuster-Präfix (falls leer bzw. {@code null} wird die Baumuster-Präfix-Prüfung für
     *                    die Anzeige von 2nd-Parts (StarParts) als Alternativteile übersprungen)
     * @param countryCode Land (falls leer bzw. {@code null} werden bei Edit-Instanzen auch 2nd-Parts (StarParts) als
     *                    Alternativteile angezeigt. Bei Webservice-Instanzen werden keine 2nd-Parts ausgeliefert.)
     * @param forceReload Sollen die Alternativteile für diesen Stücklisteneintrag neu geladen werden
     *                    (Wird für die Suche benötigt)
     * @return
     */
    public Set<EtkDataPart> getAlternativePartsFilteredByReplacements(String modelPrefix, String countryCode, boolean forceReload) {
        // DAIMLER-15217: Ersetzungen nicht als Alternativteil aufführen
        Set<EtkDataPart> matchingAlternativeParts = getAlternativeParts(modelPrefix, countryCode, forceReload);
        if (Utils.isValid(matchingAlternativeParts)) {
            Collection<iPartsReplacement> successors = getSuccessors();
            if (successors != null) {
                // Pro Alternativteil prüfen, ob es eine Ersetzung mit identischer Materialnummer inkl. ES1 und ES2 gibt
                Set<EtkDataPart> filteredAlternativeParts = null;
                for (EtkDataPart alternativePart : matchingAlternativeParts) {
                    boolean successorFound = false;
                    if (alternativePart.getFieldValue(FIELD_M_AS_ES_1).equals("80")) { // Nur bei ES1=80 auf identische Ersetzung prüfen
                        for (iPartsReplacement successor : successors) {
                            if (successor.successorPartNumber.equals(alternativePart.getAsId().getMatNr())) {
                                successorFound = true;
                                break;
                            }
                        }
                    }
                    if (!successorFound) {
                        if (filteredAlternativeParts == null) {
                            filteredAlternativeParts = new LinkedHashSet<>(matchingAlternativeParts.size());
                        }
                        filteredAlternativeParts.add(alternativePart);
                    }
                }
                matchingAlternativeParts = filteredAlternativeParts;
            }
        }
        return matchingAlternativeParts;
    }

    /**
     * Alternativteile für Stücklisteneintrag bestimmen
     * Wenn der Stücklisteneintrag eine Verbindung zu einem Produkt besitzt, erfolgt eine Filterung.
     * Den Usecase ohne Filterung gibt es praktisch nicht. Ggf. ist er über getAlternativeParts(true) zu ermitteln
     *
     * @param modelPrefix Optionaler Baumuster-Präfix (falls leer bzw. {@code null} wird die Baumuster-Präfix-Prüfung für
     *                    die Anzeige von 2nd-Parts (StarParts) als Alternativteile übersprungen)
     * @param countryCode Land (falls leer bzw. {@code null} werden bei Edit-Instanzen auch 2nd-Parts (StarParts) als
     *                    Alternativteile angezeigt. Bei Webservice-Instanzen werden keine 2nd-Parts ausgeliefert.)
     * @param forceReload Alternativteile für diesen Stücklisteneintrag neu laden (für die Suche)
     * @return
     */
    public Set<EtkDataPart> getAlternativeParts(String modelPrefix, String countryCode, boolean forceReload) {
        if ((alternativeParts != null) || forceReload) {
            // Für bessere Performance isIncludeSecondParts() nur dann aufrufen, wenn es auch wirklich Alternativteile gibt
            return getAlternativeParts(isIncludeSecondParts(modelPrefix, countryCode), forceReload);
        } else {
            return null;
        }
    }

    /**
     * Alternativteile optional gefiltert zurückliefern.
     * Die Filterung kann erfolgen auf 2nd parts Teile (künftig vielleicht auch nach Reman-Teilen).
     * Es erfolgt keine weitere Prüfung wie bei getAlternativeParts();
     * Zur Definition siehe auch Kommentar am Member.
     *
     * @param includeSecondParts bei true wird keine weitere Prüfung gemacht
     * @param forceReload        Alternativteile neu laden (wird für die Suche benötigt)
     * @return
     */
    public Set<EtkDataPart> getAlternativeParts(boolean includeSecondParts, boolean forceReload) {
        if (forceReload && (alternativeParts == null)) {
            loadAlternativeParts();
        }
        if (includeSecondParts) {
            return alternativeParts;
        }
        if (alternativeParts != null) {
            Set<EtkDataPart> filteredAlternativeParts = new LinkedHashSet<>();
            for (EtkDataPart alternativePart : alternativeParts) {
                DBDataObjectList<? extends DBDataObject> children = alternativePart.getChildren(iPartsConst.TABLE_DA_ES1);
                if ((children != null) && !children.isEmpty() && (children.get(0) instanceof iPartsDataES1)) {
                    // SecondParts ausfiltern
                    if (!((iPartsDataES1)children.get(0)).isSecondPart()) {
                        filteredAlternativeParts.add(alternativePart);
                    }
                }
            }
            if (filteredAlternativeParts.isEmpty()) {
                return null;
            }
            return filteredAlternativeParts;
        }
        return null;
    }

    /**
     * Nachladen der Alternativteile für diesen Stücklisteneintrag über die Stückliste.
     * Nur bei der Suche nötig
     */
    private void loadAlternativeParts() {
        EtkDataPartListEntry partListEntry = getOwnerAssembly().getPartListEntryFromKLfdNrUnfiltered(getAsId().getKLfdnr());
        if (partListEntry instanceof iPartsDataPartListEntry) {
            Set<EtkDataPart> otherAlternativeParts = ((iPartsDataPartListEntry)partListEntry).alternativeParts;
            if (otherAlternativeParts != null) {
                alternativeParts = new LinkedHashSet<>(otherAlternativeParts);
            }
        }
    }

    /**
     * Liefert zurück, ob Alternativteile vorhanden sind mit Berücksichtigung von 2nd-Parts (StarParts) auf Basis des
     * übergebenen Baumuster-Präfix und dem übergebenen Ländercode.
     *
     * @param modelPrefix Optionaler Baumuster-Präfix (falls leer bzw. {@code null} wird die Baumuster-Präfix-Prüfung übersprungen)
     * @param countryCode Land (falls leer bzw. {@code null} wird {@code true} bei Edit- und {@code false} bei
     *                    Webservice-Instanzen zurückgeliefert)
     * @return
     */
    public boolean hasAlternativeParts(String modelPrefix, String countryCode) {
        Set<EtkDataPart> alternativeParts = getAlternativeParts(modelPrefix, countryCode, false);
        return (alternativeParts != null) && !alternativeParts.isEmpty();
    }

    public void setAlternativeParts(Set<EtkDataPart> alternativeParts) {
        this.alternativeParts = alternativeParts;
    }

    public List<EtkMultiSprache> getCombinedMultiTextList() {
        return combinedMultiTextList;
    }

    public void setCombinedMultiTextList(List<EtkMultiSprache> combinedMultiTextList) {
        this.combinedMultiTextList = combinedMultiTextList;
    }

    public iPartsDataCombTextList getDataCombTextList() {
        if (dataCombTextList != null) {
            return dataCombTextList;
        } else {
            // Falls dataCombTextList nicht explizit von außen gesetzt ist, dataCombTextList nicht im Cache halten, um folgende
            // Edit-Aktionen nicht zu beeinflussen
            iPartsDataCombTextList tempDataCombTextList = iPartsDataCombTextList.loadForPartListEntryAndAllLanguages(getAsId(), getEtkProject());
            updateCombTextSourceGenVO(tempDataCombTextList);
            return tempDataCombTextList;
        }
    }

    public void setDataCombTextList(iPartsDataCombTextList dataCombTextList) {
        this.dataCombTextList = dataCombTextList;
        updateCombTextSourceGenVO(dataCombTextList);
    }

    public void updateCombTextSourceGenVO(iPartsDataCombTextList dataCombTextList) {
        if (dataCombTextList == null) {
            // Virtuelles Feld für "Ergänzungstext Quelle GenVO" muss neu berechnet werden
            attributes.deleteField(iPartsDataVirtualFieldsDefinition.RETAIL_COMB_TEXT_SOURCE_GENVO, false, DBActionOrigin.FROM_DB);
            return;
        }

        // Ergänzungstext Quelle GenVO?
        boolean sourceGenVO = false;
        if (Utils.isValid(dataCombTextList)) {
            String partListType = getOwnerAssembly().getEbeneName();
            if (partListType.equals(iPartsConst.PARTS_LIST_TYPE_DIALOG_RETAIL)) {
                for (iPartsDataCombText dataCombText : dataCombTextList) {
                    if (dataCombText.getFieldValueAsBoolean(FIELD_DCT_SOURCE_GENVO)) {
                        sourceGenVO = true;
                        break;
                    }
                }
            }
        }
        attributes.addField(iPartsDataVirtualFieldsDefinition.RETAIL_COMB_TEXT_SOURCE_GENVO, SQLStringConvert.booleanToPPString(sourceGenVO),
                            true, DBActionOrigin.FROM_DB);
    }

    public boolean hasConstructionKits() {
        EtkDataArray saas = getFieldValueAsArrayOriginal(iPartsConst.FIELD_K_SA_VALIDITY);
        for (DBDataObjectAttribute attribute : saas.getAttributes()) {
            if (!attribute.getAsString().startsWith("Z")) { // SAAs ignorieren
                return true;
            }
        }
        return false;
    }

    /**
     * Der Eintrag ist Bestandteil eines Spezialkataloges z.B. Lacke und Betriebsstoffe
     *
     * @return
     */
    public boolean isSpecialProductEntry() {
        if (getOwnerAssembly().isSpecialProduct()) {
            return true;
        }

        if (getDestinationAssemblyId() != null) {
            EtkDataAssembly destAssembly = EtkDataObjectFactory.createDataAssembly(getEtkProject(), getDestinationAssemblyId());
            if (destAssembly instanceof iPartsDataAssembly) {
                return ((iPartsDataAssembly)destAssembly).isSpecialProduct();
            }
        }

        return false;
    }

    /**
     * Gültigkeitsart der Werkseinsatzdaten ermitteln
     *
     * @return
     */
    public iPartsFactoryData.ValidityType getFactoryDataValidity() {
        iPartsFactoryData factoryDataForRetail = null;

        // Bei Konstruktions-Stücklisteneinträgen nicht erst unnötig die Retail-Werkseinsatzdaten bestimmen
        if (!factoryDataForConstructionLoaded) {
            factoryDataForRetail = getFactoryDataForRetail();
        }

        if (factoryDataForRetail != null) { // es gibt Werkseinsatzdaten
            // Hier müssen die ungefilterten Werkseinsatzdaten betrachtet werden
            iPartsFactoryData relevantFactoryData = getFactoryDataForRetailUnfiltered();
            if (relevantFactoryData == null) { // Fallback auf die (vererbten) gefilterten Werkseinsatzdaten, wenn es keine ungefilterten gibt
                relevantFactoryData = factoryDataForRetail;
            }
            if (relevantFactoryData.hasValidFactories()) {
                // Werkseinsatzdaten gültig

                if (!isValidFactoryDataRelevantForEndNumberFilter() || !factoryDataForRetail.hasValidFactoryData()) {
                    // nicht Endnummernfilter-relevant
                    return iPartsFactoryData.ValidityType.VALID_NOT_ENDNUMBER_FILTER_RELEVANT;
                } else {
                    // Endnummernfilter-relevant
                    return iPartsFactoryData.ValidityType.VALID;
                }
            } else {
                return iPartsFactoryData.ValidityType.INVALID;
            }
        } else if (getOwnerAssemblyId().isVirtual() && hasValidFactoryDataForConstruction()) { // Konstruktions-Werkseinsatzdaten nur für virtuelle Stücklisten laden
            return iPartsFactoryData.ValidityType.VALID_FOR_CONSTRUCTION;
        } else {
            // es gibt keine Werkseinsatzdaten
            return iPartsFactoryData.ValidityType.NOT_AVAILABLE;
        }
    }

    private boolean hasValidFactoryDataForConstruction() {
        return (getFactoryDataForConstruction() != null) && factoryDataForConstruction.hasValidFactories();
    }

    /**
     * Hat diese Stücklisteneintrag eine Fußnote mit mehreren separaten Zeilen?
     * (gekennzeichnet durch gleiche Fußnoten ID aber mehrere Laufende Nr. DFNC_LINE_NO)
     * Dieses Format kommt aus der Migration der Altdaten und muss bei Bearbeitung konvertiert werden.
     *
     * @param project
     * @return
     */
    public boolean hasMultilineFootNotes(EtkProject project) {
        Collection<iPartsFootNote> footNotes = getFootNotes();
        if (footNotes != null) {
            for (iPartsFootNote footNote : footNotes) {
                // Farb-Tabellenfußnoten sind mehrzeilig, dürfen aber nicht bearbeitet werden
                if (!footNote.isColorTablefootnote()) {
                    List<String> footNoteTexts = footNote.getFootNoteTexts(project);
                    if ((footNoteTexts != null) && (footNoteTexts.size() > 1)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Liefert zurück, ob die Stücklistenposition mit Farbfußnoten verknüpft ist
     *
     * @return
     */
    public boolean hasColorTableFootNotes() {
        Collection<iPartsFootNote> footNotes = getFootNotes();
        if (footNotes != null) {
            for (iPartsFootNote footNote : footNotes) {
                if (footNote.isColorTablefootnote()) {
                    return true;
                }
            }
        }
        return false;

    }

    /**
     * Prüft bei Dokumentationsmethode DIALOG für diesen Stücklisteneintrag, ob es Ersetzungen gibt, und ersetzt gegebenenfalls
     * die Werkseinsatzdaten durch die Daten der Ersetzungen für PEM ab bzw. PEM bis inklusive des Flags "PEM ab/bis auswerten".
     *
     * @return Werkseinsatzdaten für den Retail zu diesem PartListEntry MIT Berücksichtigung von Ersetzungen
     */
    private iPartsFactoryData getFactoryDataChainForReplacements() {
        iPartsFactoryData factoryData = getFactoryDataForRetailWithoutReplacements();

        // Nur bei Dokumentationsmethode DIALOG weitermachen und die Ersetzungen auswerten.
        if (!getOwnerAssembly().getDocumentationType().isPKWDocumentationType()) {
            return factoryData;
        }

        if (factoryData != null) {
            // Unbedingt einen Klon der Werkseinsatzdaten verwenden, da die Original-Werkseinsatzdaten OHNE Ersetzungen
            // nicht verändert werden dürfen, die diese im Stücklisten-Cache liegen und bei neuen Instanzen dieser Stückliste
            // geklont wiederverwendet werden
            factoryData = factoryData.cloneMe();
        }

        // Die Ersetzungskette für diesen Stücklisteneintrag nach der letzten austauschbaren Ersetzung durchsuchen
        Collection<iPartsDataPartListEntry> finalPredecessors = FactoryDataHelper.findFinalReplacements(this, true);
        if (finalPredecessors != null) {
            // Sollte es mehrere austauschbare Ersetzungen geben, dann wird hier pro Werk nach dem jüngsten bzw.
            // ältesten Datum PEM ab/bis gesucht
            iPartsFactoryData relevantPredecessorFactoryData = FactoryDataHelper.findRelevantDataPerFactory(finalPredecessors,
                                                                                                            true, getEtkProject());
            if (relevantPredecessorFactoryData != null) {
                if (factoryData == null) {
                    factoryData = new iPartsFactoryData();
                }

                // Werkseinsatzdaten der zuvor ermittelten Ersetzungen speichern inkl. Setzen des Flags, ob es sich
                // um vererbte Werkseinsatzdaten handelt.
                factoryData.setPemData(relevantPredecessorFactoryData, true);

                // PEM ab verwenden aus den geerbten Werkseinsatzdaten übernehmen
                factoryData.setEvalPemFrom(relevantPredecessorFactoryData.isEvalPemFrom());
            } else if (factoryData != null) {
                factoryData.setPemData(null, true);
                factoryData.setEvalPemFrom(false);
            }
        }

        // Analog für die Nachfolger
        Collection<iPartsDataPartListEntry> finalSuccessors = FactoryDataHelper.findFinalReplacements(this, false);
        if (finalSuccessors != null) {
            iPartsFactoryData relevantSuccessorFactoryData = FactoryDataHelper.findRelevantDataPerFactory(finalSuccessors,
                                                                                                          false, getEtkProject());
            if (relevantSuccessorFactoryData != null) {
                if (factoryData == null) {
                    factoryData = new iPartsFactoryData();
                }
                factoryData.setPemData(relevantSuccessorFactoryData, false);
                factoryData.setEvalPemTo(relevantSuccessorFactoryData.isEvalPemTo());
            } else if (factoryData != null) {
                factoryData.setPemData(null, false);
                factoryData.setEvalPemTo(false);
            }
        }

        // Neue ermittelte Werkseinsatzdaten am Stücklisteneintrag speichern
        return factoryData;
    }

    private void assertActiveChangesetForDelete() {
        if (!isRevisionChangeSetActive()) {
            MessageDialog.showError(TranslationHandler.translate("!!Löschen von referenzierten Daten zum Stücklisteneintrag (%2) ohne " +
                                                                 "Changeset ist nicht möglich.%1" +
                                                                 "Der aktuelle Löschvorgang wurde nicht vollständig durchgeführt.%1" +
                                                                 "Bitte mindestens den Autorenauftrag deaktivieren und wieder aktivieren " +
                                                                 "bzw. idealerweise neu anmelden (neue Session starten), " +
                                                                 "sonst werden die folgenden Bearbeitungen ignoriert.%1" +
                                                                 "Informieren Sie den Support!", "\n", getAsId().toString()));
            throw new RuntimeException("No active changeset for deleting referenced data for " + getAsId());
        }
    }

    /**
     * Sammelt alle zu löschenden referenzierten Daten auf, die bereits beim Laden der Stückliste für diesen
     * Stücklisteneintrag geladen werden. Das sind: Fußnoten und Ersetzungen inkl. Mitlieferteile
     *
     * @param changeSetDataObjectList     Liste, zu der alle gelöschten bzw. geänderten referenzierten {@link EtkDataObject}s
     *                                    hinzugefügt werden müssen
     * @param forceDeleteAll              Flag, ob wirklich alle referenzierten Daten gelöscht werden sollen, weil z.B. das
     *                                    gesamte Modul gelöscht wird (ansonsten werden Ersetzungen zum besten Vorgänger-
     *                                    bzw. Nachfolgerstand verschoben)
     * @param otherDataObjectsToBeDeleted Andere {@link EtkDataPartListEntry}s, die ebenfalls gelöscht werden, was beim
     *                                    Verschieben von Ersetzungen relevant ist.
     */
    public void collectPreloadedReferencedDataForDelete(GenericEtkDataObjectList changeSetDataObjectList, boolean forceDeleteAll,
                                                        List<? extends EtkDataObject> otherDataObjectsToBeDeleted) {
        assertActiveChangesetForDelete();

        // Fußnoten Referenz im Changeset als gelöscht markieren
        Collection<iPartsFootNote> footNotes = getFootNotes();
        if ((footNotes != null) && !footNotes.isEmpty()) {
            iPartsDataFootNoteCatalogueRefList footNoteCatalogueRefList = new iPartsDataFootNoteCatalogueRefList();
            for (iPartsFootNote footNote : footNotes) {
                iPartsFootNoteCatalogueRefId refId = new iPartsFootNoteCatalogueRefId(getAsId(), footNote.getFootNoteId().getFootNoteId());
                iPartsDataFootNoteCatalogueRef footNoteRef = new iPartsDataFootNoteCatalogueRef(getEtkProject(), refId);
                footNoteCatalogueRefList.delete(footNoteRef, true, DBActionOrigin.FROM_EDIT);
            }
            changeSetDataObjectList.addAll(footNoteCatalogueRefList, DBActionOrigin.FROM_EDIT);
            // Fußnoten am Stücklisteneintrag entfernen
            clearFootnotes();
        }

        boolean anyReplacementMoved = false;

        // Ersetzungen im Changeset als gelöscht markieren bzw. verschieben; es müssen alle Ersetzungen aus der DB geladen
        // werden, weil erstens auch nicht freigegebene Ersetzungen berücksichtigt werden müssen und zweitens beim Löschen
        // von mehreren Stücklisteneinträgen die Ersetzungen bereits verschoben sein worden können und daher nicht mehr
        // aktuell wären am Stücklisteneintrag
        List<iPartsReplacement> allPredecessors = new DwList<>();
        List<iPartsReplacement> allSuccessors = new DwList<>();
        iPartsReplacementHelper.loadReplacementsForPartListEntry(null, allPredecessors, allSuccessors, this, false);
        if (!allPredecessors.isEmpty() || !allSuccessors.isEmpty()) {
            // Prüfungen für versorgungsrelevante Baureihen können entfallen, wenn alle referenzierten Daten gelöscht werden sollen
            boolean seriesRelevantForImport = !forceDeleteAll && isSeriesRelevantForImport();

            Set<PartListEntryId> otherPleIDsToBeDeleted = null;
            if (otherDataObjectsToBeDeleted != null) {
                otherPleIDsToBeDeleted = new HashSet<>(otherDataObjectsToBeDeleted.size());
                for (EtkDataObject dataObject : otherDataObjectsToBeDeleted) {
                    if (dataObject instanceof EtkDataPartListEntry) {
                        otherPleIDsToBeDeleted.add(((EtkDataPartListEntry)dataObject).getAsId());
                    }
                }
            }

            iPartsReplacementKEMHelper replacementKEMHelper = null;
            if (seriesRelevantForImport) {
                DBDataObjectList<EtkDataPartListEntry> partListEntries = getOwnerAssembly().getPartListUnfiltered(null);
                replacementKEMHelper = new iPartsReplacementKEMHelper(partListEntries);
            }
            if (!allPredecessors.isEmpty()) {
                // Vererbte Ersetzungen für Nachfolgerstände nur bei versorgungsrelevanten Baureihen aktualisieren
                iPartsDataPartListEntry bestNewSuccessor = null;
                if (seriesRelevantForImport) {
                    // Alle Nachfolgerstände zum aktuellen Stücklisteneintrag ermitteln
                    List<iPartsDataPartListEntry> nextVersionsOfCurrentEntry = replacementKEMHelper.getEntriesForAllKEMs(this, false, true);
                    for (iPartsDataPartListEntry nextVersionOfCurrentEntry : nextVersionsOfCurrentEntry) {
                        // An jedem Nachfolgerstand die Ersetzungen und Retail-Werkseinsatzdaten zurücksetzen zur Neubestimmung
                        nextVersionOfCurrentEntry.clearReplacements();
                        nextVersionOfCurrentEntry.clearFactoryDataForRetail();
                    }

                    bestNewSuccessor = iPartsReplacementKEMHelper.getBestSuccessor(getFieldValue(FIELD_K_DATEFROM), nextVersionsOfCurrentEntry,
                                                                                   otherPleIDsToBeDeleted);
                }

                for (iPartsReplacement predecessor : allPredecessors) {
                    anyReplacementMoved |= deleteOrMoveReplacement(predecessor, bestNewSuccessor, true, changeSetDataObjectList);

                    // Fußnoten wegen einer evtl. vorhandenen virtuellen Fußnote am Vorgänger(stand) entfernen
                    if (predecessor.predecessorEntry instanceof iPartsDataPartListEntry) {
                        ((iPartsDataPartListEntry)predecessor.predecessorEntry).clearFootnotes();
                    }
                }
            }

            if (!allSuccessors.isEmpty()) {
                // Vererbte Ersetzungen für Vorgängerstände nur bei versorgungsrelevanten Baureihen aktualisieren
                iPartsDataPartListEntry bestNewPredecessor = null;
                if (seriesRelevantForImport) {
                    // Alle Vorgängerstände zum aktuellen Stücklisteneintrag ermitteln
                    List<iPartsDataPartListEntry> previousVersionsOfCurrentEntry = replacementKEMHelper.getEntriesForAllKEMs(this, true, true);
                    for (iPartsDataPartListEntry previousVersionOfCurrentEntry : previousVersionsOfCurrentEntry) {
                        // An jedem Vorgängerstand die Ersetzungen und Retail-Werkseinsatzdaten zurücksetzen zur Neubestimmung
                        previousVersionOfCurrentEntry.clearReplacements();
                        previousVersionOfCurrentEntry.clearFactoryDataForRetail();
                    }

                    bestNewPredecessor = iPartsReplacementKEMHelper.getBestPredecessor(getFieldValue(FIELD_K_DATEFROM), previousVersionsOfCurrentEntry,
                                                                                       otherPleIDsToBeDeleted);
                }

                for (iPartsReplacement successor : allSuccessors) {
                    anyReplacementMoved |= deleteOrMoveReplacement(successor, bestNewPredecessor, false, changeSetDataObjectList);
                }
            }

            // Ersetzungen am Stücklisteneintrag löschen, aber das Flag replacementsLoaded auf true setzen, damit die
            // Ersetzungen auch nicht erneut geladen werden durch irgendwelche Zugriffe
            clearReplacements();
            replacementsLoaded = true;

            // Falls mindestens eine Ersetzung verschoben werden musste, dann muss eine evtl. laufende Pseudo-Transaktion
            // beendet und neu gestartet werden, da die verschobene Ersetzung ansonsten bei anderen Stücklisteneinträgen,
            // die ebenfalls gerade gelöscht werden sollen, nicht berücksichtigt werden würde
            if (anyReplacementMoved) {
                EtkRevisionsHelper revisionsHelper = getRevisionsHelper();
                if ((revisionsHelper != null) && revisionsHelper.isInPseudoTransaction()) {
                    stopPseudoTransactionForActiveChangeSet();
                    startPseudoTransactionForActiveChangeSet(true);
                }
            }
        }
    }

    private boolean deleteOrMoveReplacement(iPartsReplacement replacement, iPartsDataPartListEntry bestNewPartListEntry,
                                            boolean isPredecessorReplacement, GenericEtkDataObjectList changeSetDataObjectList) {
        // Bei isPredecessorReplacement==true ist bestNewPartListEntry der neue beste Nachfolger, sonst Vorgänger

        boolean replacementMoved = false;
        if (!replacement.isVirtual()) { // nur echte Ersetzungen löschen bzw. verschieben
            EtkProject etkProject = getEtkProject();
            iPartsDataReplacePart dataReplacePart = replacement.getAsDataReplacePart(etkProject, true);

            // Sollte niemals null sein, da selbst wenn der Vorgänger/Nachfolger vorher gelöscht wurde, dieser an seinem Nachfolger/Vorgänger
            // (also diesem Stücklisteneintrag) die Ersetzungen zurückgesetzt hat und damit keine Ersetzungen ohne vorhandenen
            // Vorgänger/Nachfolger hier entstehen können. Zur Sicherheit trotzdem abfragen.
            if (dataReplacePart != null) {
                if (bestNewPartListEntry == null) { // Kein bester neuer Nachfolger/Vorgänger -> Ersetzung löschen
                    changeSetDataObjectList.delete(dataReplacePart, true, DBActionOrigin.FROM_EDIT);
                    iPartsDataReservedPKList.deleteReservedPrimaryKey(etkProject, dataReplacePart.getAsId());
                } else { // Ersetzung an den neuen besten Nachfolger/Vorgänger verschieben
                    if (!isPredecessorReplacement) {
                        // Falls sich der Primärschlüssel ändert, dann muss eine evtl. vorhandene PK-Reservierung gelöscht
                        // und eine freie Sequenznummer für die Ersetzung am neuen Vorgänger gesucht werden
                        iPartsDataReservedPKList.deleteReservedPrimaryKey(etkProject, dataReplacePart.getAsId());
                        String replacementSeqNo = iPartsReplacementHelper.getNextReplacementSeqNo(etkProject, bestNewPartListEntry.getAsId());
                        dataReplacePart.setFieldValue(FIELD_DRP_SEQNO, replacementSeqNo, DBActionOrigin.FROM_EDIT);
                    }
                    dataReplacePart.setFieldValue(isPredecessorReplacement ? FIELD_DRP_REPLACE_LFDNR : FIELD_DRP_LFDNR,
                                                  bestNewPartListEntry.getAsId().getKLfdnr(), DBActionOrigin.FROM_EDIT);
                    changeSetDataObjectList.add(dataReplacePart, DBActionOrigin.FROM_EDIT);
                    replacementMoved = true;
                }
            }

            // Mitlieferteile zum Vorgänger/Nachfolger im Changeset als gelöscht markieren bzw. an neuen Nachfolger/Vorgänger
            // verschieben
            if (replacement.hasIncludeParts(etkProject)) {
                iPartsDataIncludePartList includeParts = replacement.getIncludePartsAsDataIncludePartList(etkProject, true,
                                                                                                          DBActionOrigin.FROM_DB);
                if (bestNewPartListEntry == null) { // Kein bester neuer Nachfolger/Vorgänger -> Mitlieferteile löschen
                    if (includeParts != null) {
                        includeParts.deleteAll(DBActionOrigin.FROM_EDIT);
                        changeSetDataObjectList.addAll(includeParts, DBActionOrigin.FROM_EDIT);
                    }
                    // Mitlieferteile am Vorgänger/Nachfolger entfernen
                    replacement.clearIncludeParts();
                } else if (includeParts != null) { // Mitlieferteile an den neuen besten Nachfolger/Vorgänger verschieben
                    String replaceKLfdnr = bestNewPartListEntry.getAsId().getKLfdnr();
                    for (iPartsDataIncludePart dataIncludePart : includeParts) {
                        dataIncludePart.setFieldValue(isPredecessorReplacement ? FIELD_DIP_REPLACE_LFDNR : FIELD_DIP_LFDNR,
                                                      replaceKLfdnr, DBActionOrigin.FROM_EDIT);
                        changeSetDataObjectList.add(dataIncludePart, DBActionOrigin.FROM_EDIT);
                    }
                }
            }
        }

        // Am Vorgänger/Nachfolger die Ersetzungen und Retail-Werkseinsatzdaten zurücksetzen zur Neubestimmung
        EtkDataPartListEntry replacementPLE = isPredecessorReplacement ? replacement.predecessorEntry : replacement.successorEntry;
        if (replacementPLE instanceof iPartsDataPartListEntry) {
            iPartsDataPartListEntry iPartsReplacementPLE = (iPartsDataPartListEntry)replacementPLE;
            iPartsReplacementPLE.clearReplacements();
            iPartsReplacementPLE.clearFactoryDataForRetail();
        }

        return replacementMoved;
    }

    /**
     * Löscht alle zu diesem Stücklisteneintrag referenzierten Daten über das aktuell aktive Changeset.
     * Wenn kein Changeset aktiv ist, wird eine Runtime Exception geworfen.
     * Folgende Daten werden entfernt:
     * Fußnoten, Ersetzungen inkl. Mitlieferteile, kombinierte Texte und ELDAS Werkseinsatzdaten
     *
     * @param otherDataObjectsToBeDeleted Andere {@link EtkDataPartListEntry}s, die ebenfalls gelöscht werden, was beim
     *                                    Verschieben von Ersetzungen relevant ist.
     * @return {@link EtkDataObjectList} mit den gelöschten referenzierten Daten; {@code null} falls keine Daten gelöscht
     * werden müssen
     */
    @Override
    public EtkDataObjectList deleteReferencedData(List<? extends EtkDataObject> otherDataObjectsToBeDeleted) {
        assertActiveChangesetForDelete();

        GenericEtkDataObjectList changeSetDataObjectList = new GenericEtkDataObjectList();

        // Zuerst die kombinierten Texte im Changeset als gelöscht markieren, weil dadurch eine Pseudo-Transaktion gestartet wird
        iPartsDataCombTextList combTexts = getDataCombTextList();
        combTexts.deleteAll(DBActionOrigin.FROM_EDIT);
        changeSetDataObjectList.addAll(combTexts, DBActionOrigin.FROM_EDIT);
        // Kombinierten Text am Stücklisteneintrag löschen
        setCombinedText(this, null, null, null, null, getEtkProject());

        // ELDAS Werkseinsatzdaten im Changeset als gelöscht markieren
        // (Rückmeldedaten bleiben wegen Mehrfachverwendung erhalten)
        iPartsDocumentationType documentationType = getOwnerAssembly().getDocumentationType();
        if ((documentationType != null) && documentationType.isTruckDocumentationType()) {
            iPartsDataFactoryDataList factoryDataList = FactoryDataHelper.getFactoryDataList(this, false, true, getEtkProject());
            iPartsDataFactoryDataList factoryDataToDeleteList = new iPartsDataFactoryDataList();
            for (iPartsDataFactoryData factoryData : factoryDataList) {
                // Sicherstellen, dass auch nur ELDAS, EPC oder selbst angelegte iParts Daten gelöscht werden. An
                // dieser Stelle sollten eigentlich keine DIALOG Daten auftauchen.
                if ((factoryData.getSource() == iPartsImportDataOrigin.ELDAS) || (factoryData.getSource() == iPartsImportDataOrigin.EPC)
                    || (factoryData.getSource() == iPartsImportDataOrigin.IPARTS)) {
                    factoryDataToDeleteList.delete(factoryData, true, DBActionOrigin.FROM_EDIT);
                }
            }
            changeSetDataObjectList.addAll(factoryDataToDeleteList, DBActionOrigin.FROM_EDIT);
            // Werkseinsatzdaten am Stücklisteneintrag löschen
            factoryDataForRetailLoaded = false;
            factoryDataForRetailUnfiltered = null;
            clearFactoryDataForRetail();
        }

        collectPreloadedReferencedDataForDelete(changeSetDataObjectList, false, otherDataObjectsToBeDeleted);

        if (!changeSetDataObjectList.isEmptyIncludingDeletedList()) {
            return changeSetDataObjectList;
        } else {
            return null;
        }
    }

    /**
     * Liefert die Baureihen-ID für diesen Stücklisteneintrag basierend auf dem DIALOG-BCTE-Schlüssel bzw. als Fallback
     * der Baureihe vom dazugehörigen Produkt zurück.
     *
     * @return {@code null} falls keine Baureihen-ID bestimmt werden konnte
     */
    public iPartsSeriesId getSeriesId() {
        if (!getOwnerAssembly().isRetailPartList()) {
            return null;
        }

        iPartsSeriesId seriesId = null;
        String seriesNumber = getFieldValue(iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_SERIES_NO);
        if (!seriesNumber.isEmpty()) {
            seriesId = new iPartsSeriesId(seriesNumber);
        } else { // Fallback über die referenzierte Baureihe vom Produkt
            iPartsProductId productId = getOwnerAssembly().getProductIdFromModuleUsage();
            if (productId != null) {
                seriesId = iPartsProduct.getInstance(getEtkProject(), productId).getReferencedSeries();
                if (seriesId == null) {
                    Set<String> modelTypesForProduct = iPartsProduct.getInstance(getEtkProject(), productId).getAllModelTypes(getEtkProject());
                    if (!modelTypesForProduct.isEmpty()) {
                        seriesId = new iPartsSeriesId((String)modelTypesForProduct.toArray()[0]);
                    }
                }
            }
        }

        return seriesId;
    }

    /**
     * Ist die Baureihe (falls vorhanden) versorgungsrelevant für DIALOG-Importe?
     *
     * @return
     */
    public boolean isSeriesRelevantForImport() {
        iPartsSeriesId seriesId = getSeriesId();
        if (seriesId != null) {
            return iPartsDIALOGSeriesValidityCache.getInstance(getEtkProject()).isSeriesValidForDIALOGImport(seriesId.getSeriesNumber());
        } else {
            return false;
        }
    }

    public boolean isColoredPart() {
        return isColoredPart;
    }

    public void setColoredPart(boolean coloredPart) {
        isColoredPart = coloredPart;
    }

    public boolean hasColorTablesUnfiltered() {
        return hasColorTablesUnfiltered;
    }

    public void setHasColorTablesUnfiltered(boolean colortablesExist) {
        this.hasColorTablesUnfiltered = colortablesExist;
    }

    /**
     * Ersetzt den kombinierten Text am {@code target} durch den kombinierten Text von diesem Stücklisteneintrag.
     *
     * @param target
     * @return {@code null} falls es weder an diesem Stücklisteneintrag, noch am {@code target} einen kombinierten Text gibt
     */
    public iPartsDataCombTextList replaceCombTextAtTarget(iPartsDataPartListEntry target) {
        EtkProject project = getEtkProject();

        // Bisherige kombinierte Texte am Ziel laden
        iPartsDataCombTextList targetCombTextList = target.getDataCombTextList();

        // Dann die kombinierten Texte von der Quelle am Ziel setzen und alte Texteinträge löschen
        iPartsDataCombTextList targetNewCombTextList;
        iPartsDataCombTextList sourceCombTextList = getDataCombTextList();
        if (!sourceCombTextList.isEmpty()) {
            Map<iPartsCombTextId, iPartsDataCombText> existingCombTextsMap = new LinkedHashMap<>();
            for (iPartsDataCombText existingCombText : targetCombTextList) {
                existingCombTextsMap.put(existingCombText.getAsId(), existingCombText);
            }

            targetNewCombTextList = new iPartsDataCombTextList();
            for (iPartsDataCombText sourceDataCombText : sourceCombTextList) {
                iPartsCombTextId targetCombTextId = new iPartsCombTextId(target.getAsId(), sourceDataCombText.getAsId().getTextSeqNo());
                iPartsDataCombText targetDataCombText;
                iPartsDataCombText existingCombText = existingCombTextsMap.remove(targetCombTextId);
                if (existingCombText == null) {
                    // Kombinierten Texteintrag kopieren, auf neu setzen und die ID an das Ziel anpassen
                    targetDataCombText = sourceDataCombText.cloneMe(project);
                    targetDataCombText.__internal_setNew(true);
                    targetDataCombText.setId(targetCombTextId, DBActionOrigin.FROM_EDIT);
                    targetDataCombText.updateOldId();
                } else {
                    // Nur die Texte vom kombinierten Texteintrag übernehmen
                    targetDataCombText = existingCombText;
                    targetDataCombText.setFieldValueAsMultiLanguage(iPartsConst.FIELD_DCT_DICT_TEXT,
                                                                    sourceDataCombText.getFieldValueAsMultiLanguage(iPartsConst.FIELD_DCT_DICT_TEXT),
                                                                    DBActionOrigin.FROM_EDIT);
                    targetDataCombText.setFieldValue(iPartsConst.FIELD_DCT_TEXT_NEUTRAL, sourceDataCombText.getFieldValue(iPartsConst.FIELD_DCT_TEXT_NEUTRAL),
                                                     DBActionOrigin.FROM_EDIT);
                }
                targetNewCombTextList.add(targetDataCombText, DBActionOrigin.FROM_EDIT);
            }

            // Übrige (alte) Texteinträge löschen
            for (iPartsDataCombText existingCombText : existingCombTextsMap.values()) {
                targetNewCombTextList.delete(existingCombText, true, DBActionOrigin.FROM_EDIT);
            }
        } else {
            targetNewCombTextList = targetCombTextList;
            if (!targetNewCombTextList.isEmpty()) {
                targetNewCombTextList.deleteAll(DBActionOrigin.FROM_EDIT);
            } else {
                targetNewCombTextList = null;
            }
        }
        return targetNewCombTextList;
    }

    /**
     * Fügt die Array-Werte vom Feld {@code fieldName} dieses Stücklisteneintrags zu den Array-Werten desselben Felds am
     * {@code target} hinzu.
     *
     * @param target
     * @param fieldName
     */
    public void addArrayValuesToTarget(iPartsDataPartListEntry target, String fieldName) {
        EtkDataArray sourceArrayValues = getFieldValueAsArray(fieldName);
        if ((sourceArrayValues != null) && !sourceArrayValues.isEmpty()) {
            EtkDataArray targetArrayValues = target.getFieldValueAsArray(fieldName);
            if (targetArrayValues == null) {
                targetArrayValues = new EtkDataArray();
            }
            Set<String> newTargetArrayValues = new TreeSet<>(targetArrayValues.getArrayAsStringList()); // Array-Werte sortieren
            newTargetArrayValues.addAll(sourceArrayValues.getArrayAsStringList());
            targetArrayValues.clear(false);
            targetArrayValues.add(newTargetArrayValues);

            // Array-ID setzen falls notwendig
            if (StrUtils.isEmpty(targetArrayValues.getArrayId())) {
                String targetArrayId = getEtkProject().getDbLayer().getNewArrayNo(TableAndFieldName.make(iPartsConst.TABLE_KATALOG,
                                                                                                         fieldName),
                                                                                  target.getAsId().toString("|"),
                                                                                  false);
                targetArrayValues.setArrayId(targetArrayId);
                target.setIdForArray(fieldName, targetArrayId, DBActionOrigin.FROM_EDIT);
            }

            target.setFieldValueAsArray(fieldName, targetArrayValues, DBActionOrigin.FROM_EDIT);
        }
    }

    /**
     * Fügt die Fußnoten dieses Stücklisteneintrags zu den Fußnoten am {@code target} hinzu.
     *
     * @param target
     * @return {@code null} falls keine neuen Fußnoten hinzugefügt wurden
     */
    public iPartsDataFootNoteCatalogueRefList addFootNotesToTarget(iPartsDataPartListEntry target) {
        EtkProject project = getEtkProject();

        // Die Fußnoten von der Quelle laden
        iPartsDataFootNoteCatalogueRefList sourceFootNoteList = iPartsDataFootNoteCatalogueRefList.loadFootNotesForPartListEntry(project,
                                                                                                                                 getAsId());
        if (!sourceFootNoteList.isEmpty()) {
            // Bisherige Fußnoten am Ziel laden
            iPartsDataFootNoteCatalogueRefList targetFootNoteList = iPartsDataFootNoteCatalogueRefList.loadFootNotesForPartListEntry(project,
                                                                                                                                     target.getAsId());

            int footNoteSeqNo = 0; // Für die korrekte Sequenznummer der neuen Fußnoten
            Map<String, iPartsDataFootNoteCatalogueRef> existingFootNotesMap = new LinkedHashMap<>();
            for (iPartsDataFootNoteCatalogueRef existingFootNote : targetFootNoteList) {
                existingFootNotesMap.put(existingFootNote.getAsId().getFootNoteId(), existingFootNote);
                footNoteSeqNo = Math.max(footNoteSeqNo, StrUtils.strToIntDef(existingFootNote.getSequenceNumber(), 0));
            }
            footNoteSeqNo++;

            // Die Fußnoten von der Quelle am Ziel hinzufügen
            for (iPartsDataFootNoteCatalogueRef sourceDataFootNote : sourceFootNoteList) {
                String footNoteId = sourceDataFootNote.getAsId().getFootNoteId();
                if (!existingFootNotesMap.containsKey(footNoteId)) {
                    iPartsFootNoteCatalogueRefId targetFootNoteId = new iPartsFootNoteCatalogueRefId(target.getAsId(), footNoteId);
                    iPartsDataFootNoteCatalogueRef targetDataFootNote = sourceDataFootNote.cloneMe(project);
                    targetDataFootNote.__internal_setNew(true);
                    targetDataFootNote.setId(targetFootNoteId, DBActionOrigin.FROM_EDIT);
                    targetDataFootNote.updateOldId();
                    targetDataFootNote.setSequenceNumber(footNoteSeqNo);
                    footNoteSeqNo++;
                    targetFootNoteList.add(targetDataFootNote, DBActionOrigin.FROM_EDIT);
                }
            }

            return targetFootNoteList;
        } else {
            return null;
        }
    }

    @Override
    protected boolean modifyPartListEntryBeforeMarkAsChangedInChangeSet(EtkDataPartListEntry partListEntry) {
        // Flag "Automatisch erzeugt" muss bei Edit-Aktionen zurückgesetzt werden
        return resetAutoCreatedFlag(partListEntry);
    }

    /**
     * Ermittelt aus diesem {@link iPartsDataPartListEntry} den Primärschlüssel des DIALOG-Konstruktionsdatensatzes falls möglich.
     * <p>
     * {@code null} falls der Primärschlüssel nicht ermittelt werden konnte
     */
    public iPartsDialogBCTEPrimaryKey getDialogBCTEPrimaryKey() {
        if (!dialogBCTEPrimaryKeyCalculated) {
            dialogBCTEPrimaryKey = iPartsDialogBCTEPrimaryKey.createFromDialogGuid(iPartsDialogBCTEPrimaryKey.getDialogGUID(this));
            dialogBCTEPrimaryKeyCalculated = true;
        }
        return dialogBCTEPrimaryKey;
    }

    public Set<String> getCountryValidities() {
        String countryValiditiesString = getFieldValue(iPartsConst.FIELD_K_COUNTRY_VALIDITY);
        if (countryValiditiesString.isEmpty()) {
            return new TreeSet<>();
        }
        return new TreeSet<>(StrUtils.toStringList(countryValiditiesString.toUpperCase(), iPartsConst.COUNTRY_SPEC_DB_DELIMITER,
                                                   false, true));
    }

    public Set<String> getSpecValidities() {
        String specValiditiesString = getFieldValue(iPartsConst.FIELD_K_SPEC_VALIDITY);
        if (specValiditiesString.isEmpty()) {
            return new TreeSet<>();
        }
        return new TreeSet<>(StrUtils.toStringList(specValiditiesString, iPartsConst.COUNTRY_SPEC_DB_DELIMITER,
                                                   false, true));
    }

    public boolean isSpecialprotectionETKZ() {
        String etkzValue = getPart().getFieldValue(iPartsConst.FIELD_M_ETKZ);
        return StrUtils.isValid(etkzValue) && etkzValue.equals("S");
    }
}