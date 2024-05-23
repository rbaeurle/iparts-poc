/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.db.EtkDbsHelper;
import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.importer.base.model.fixedlength.*;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.base.project.events.ApplicationEvents;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPart;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.drawing.EtkDataImage;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.base.project.mechanic.ids.PartId;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.DCAggregateTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataSa;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataSaa;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsSaId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsSaaId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsModuleId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsProductModelsId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsProductSAsId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsReplacement;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu.KgTuId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.config.iPartsMADDateTimeHandler;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.helper.images.PictureReference;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.dialog.DIALOGImportHelper;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBBase;
import de.docware.framework.modules.db.DBDataObject;
import de.docware.framework.modules.db.DBDataObjectList;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.ArrayUtil;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.date.DateUtils;
import de.docware.util.file.DWFile;
import de.docware.util.file.DWFileCoding;
import de.docware.util.sort.SortBetweenHelper;
import de.docware.util.sql.TableAndFieldName;

import java.util.*;


/**
 * Eldas Tal40A Importer
 * Die TAL40A Dateien sind fixed Length und enthalten die Stücklistendaten der MAD.
 * Diese Datei ist die zentrale Datei der Migration
 */
public class MadTal4XABaseImporter extends AbstractDataImporter implements iPartsConst, EtkDbConst {

    public enum Tal4XAType {TAL40A, TAL46A}

    final static int TAL4XA_RECORD_LEN = 2001;

    // Baumuster-Grunddaten
    final static public String SATZART_7 = "SATZART_7";

    // Baumuster-Übersicht Texte
    final static public String SATZART_2X = "SATZART_2X";

    // Baumuster-Übersicht Aggregate-BM
    final static public String SATZART_2A = "SATZART_2A";

    // Baumuster-Übersicht Fahrgestell-BM
    final static public String SATZART_2F = "SATZART_2F";

    // Baumuster-Konstruktionsgruppe (KG)
    final static public String SATZART_8 = "SATZART_8";

    // Header SA-Katalge
    final static public String SATZART_6 = "SATZART_6";

    // SA-Kataloge Beschreibung Strichausführungen
    final static public String SATZART_C = "SATZART_C";

    // Baumuster-TU-Steuerposition
    final static public String SATZART_9BS = "SATZART_9BS";

    // SAA TU-Steuerposition
    final static public String SATZART_DBS = "SATZART_DBS";

    // Baumuster-Teile
    final static public String SATZART_9 = "SATZART_9";

    // SAA-Teile
    final static public String SATZART_D = "SATZART_D";

    // Fußnoten Texte kurz
    final static public String SATZART_AX = "SATZART_AX";

    // SAA Fußnoten Texte kurz
    final static public String SATZART_EX = "SATZART_EX";

    // Fußnoten Texte lang
    final static public String SATZART_AY = "SATZART_AY";

    // SAA Fußnoten Texte lang
    final static public String SATZART_EY = "SATZART_EY";

    // Fußnoten Nummern
    final static public String SATZART_A9 = "SATZART_A9";

    // SAA Fußnoten Nummern
    final static public String SATZART_E9 = "SATZART_E9";

    // Fußnoten Einsatzdaten (nur Dialog)
    final static public String SATZART_AE = "SATZART_AE";

    // SAA Fußnoten Einsatzdaten (nur Dialog)
    final static public String SATZART_EE = "SATZART_EE";

    // Fußnoten Farbdaten (nur Dialog)
    final static public String SATZART_AF = "SATZART_AF";

    // SAA Fußnoten Farbdaten (nur Dialog)
    final static public String SATZART_EF = "SATZART_EF";

    // Fußnoten Bedienungsanleitungen (nur Dialog)
    final static public String SATZART_AB = "SATZART_AB";

    // SAA Fußnoten Bedienungsanleitungen (nur Dialog)
    final static public String SATZART_EB = "SATZART_EB";

    // Fußnoten Lochbilder (nur Dialog)
    final static public String SATZART_AL = "SATZART_AL";

    // SAA Fußnoten Lochbilder (nur Dialog)
    final static public String SATZART_EL = "SATZART_EL";


    final static public String TAL40A_KATALOG = "TAL40A_KATALOG";
    final static public String TAL46A_SA_RUMPF = "TAL46A_SA_RUMPF";
    final static public String TAL46A_SA_INTERVALL = "TAL46A_SA_INTERVALL";


    final static public String TAL40A_KATALOG_TYP = "TAL40A_KATALOG_TYP";
    final static public String TAL40A_TYP = "TAL40A_TYP";
    final static public String TAL40A_BAUMUSTER = "TAL40A_BAUMUSTER";
    final static public String TAL4XA_SORTIMENTSKLASSEN = "TAL4XA_SORTIMENTSKLASSEN";
    final static public String TAL4XA_BEREICHSLAENDERCODE = "TAL4XA_BEREICHSLAENDERCODE";
    final static public String TAL40A_BAUMUSTERART = "TAL40A_BAUMUSTERART";
    final static public String TAL40A_POS_NR = "TAL40A_POS_NR";
    final static public String TAL40A_ZEILE = "TAL40A_ZEILE";
    final static public String TAL40A_FOLGE = "TAL40A_FOLGE";
    final static public String TAL40A_TEXT = "TAL40A_TEXT";
    final static public String TAL40A_VERKAUFSBEZEICHNUNG = "TAL40A_VERKAUFSBEZEICHNUNG";
    final static public String TAL40A_FGST_NR = "TAL40A_FGST_NR";
    final static public String TAL40A_ET_KATALOGE = "TAL40A_ET_KATALOGE";
    final static public String TAL40A_MOTOR = "TAL40A_MOTOR";
    final static public String TAL40A_GETRIEBE_MECHANISCH = "TAL40A_GETRIEBE_MECHANISCH";
    final static public String TAL40A_GETRIEBE_AUTOMATIK = "TAL40A_GETRIEBE_AUTOMATIK";
    final static public String TAL40A_VERTEILER_GETRIEBE = "TAL40A_VERTEILER_GETRIEBE";
    final static public String TAL40A_VORDERACHSE = "TAL40A_VORDERACHSE";
    final static public String TAL40A_HINTERACHSE = "TAL40A_HINTERACHSE";
    final static public String TAL40A_LENKUNG = "TAL40A_LENKUNG";
    final static public String TAL40A_PRITSCHE = "TAL40A_PRITSCHE";
    final static public String TAL40A_AUFBAU = "TAL40A_AUFBAU";
    final static public String TAL40A_BRENNSTOFFZELLE = "TAL40A_BRENNSTOFFZELLE";
    final static public String TAL40A_HOCHVOLTBATTERIE = "TAL40A_HOCHVOLTBATTERIE";
    final static public String TAL40A_ELEKTROMOTOR = "TAL40A_ELEKTROMOTOR";
    final static public String TAL40A_ABGASNACHBEHANDLUNG = "TAL40A_ABGASNACHBEHANDLUNG";
    final static public String TAL40A_KG = "TAL40A_KG";
    final static public String TAL40A_SA_SNR = "TAL40A_SA_SNR";
    final static public String TAL40A_BK_SNR = "TAL40A_BK_SNR";
    final static public String TAL40A_SA_VERWENDUNG = "TAL40A_SA_VERWENDUNG";
    final static public String TAL40A_POS_ADR_DIALOG = "TAL40A_POS_ADR_DIALOG";

    final static public String TAL4XA_BENENNUNG = "TAL4XA_BENENNUNG";
    final static public String TAL4XA_LFDNR = "TAL4XA_LFDNR";
    final static public String TAL4XA_BT_POS = "TAL4XA_BT_POS";
    final static public String TAL4XA_TU = "TAL4XA_TU";
    final static public String TAL4XA_TU_BENENNUNG = "TAL4XA_TU_BENENNUNG";
    final static public String TAL4XA_BILD_TAFEL_IDENT = "TAL4XA_BILD_TAFEL_IDENT";
    final static public String TAL4XA_ERSETZT = "TAL4XA_ERSETZT";
    final static public String TAL4XA_TEILENUMMER = "TAL4XA_TEILENUMMER";
    final static public String TAL4XA_ENTFALL_TEILENUMMER = "TAL4XA_ENTFALL_TEILENUMMER";
    final static public String TAL4XA_BENENNUNG_DE = "TAL4XA_BENENNUNG_DE";
    final static public String TAL4XA_ADRESSE_ERGAENZUNGSTEXTE = "TAL4XA_ADRESSE_ERGAENZUNGSTEXTE";
    final static public String TAL4XA_SPRACHNEUTRALER_TEXT = "TAL4XA_SPRACHNEUTRALER_TEXT";
    final static public String TAL4XA_EINRUECKZAHL = "TAL4XA_EINRUECKZAHL";
    final static public String TAL4XA_TUV_KZ = "TAL4XA_TUV_KZ";
    final static public String TAL4XA_WW_KZ = "TAL4XA_WW_KZ";
    final static public String TAL4XA_A_N_KZ = "TAL4XA_A_N_KZ";
    final static public String TAL4XA_LENKUNG_GETRIEBE = "TAL4XA_LENKUNG_GETRIEBE";
    final static public String TAL4XA_FN_HINWEISE = "TAL4XA_FN_HINWEISE";
    final static public String TAL4XA_MENGE_JE_BAUMUSTER = "TAL4XA_MENGE_JE_BAUMUSTER";
    final static public String TAL4XA_ERGAENZUNGSTEXT_DE = "TAL4XA_ERGAENZUNGSTEXT_DE";
    final static public String TAL4XA_ERGAENZUNGSTEXT_FREMD = "TAL4XA_ERGAENZUNGSTEXT_FREMD";
    final static public String TAL4XA_REP_TNR_1 = "TAL4XA_REP_TNR_1";
    final static public String TAL4XA_REP_TNR_N = "TAL4XA_REP_TNR_N";
    final static public String TAL4XA_WW_TNR = "TAL4XA_WW_TNR";
    final static public String TAL4XA_CODE_B = "TAL4XA_CODE_B";
    final static public String TAL4XA_TERMID = "TAL4XA_TERMID";
    final static public String TAL4XA_ADRESSE_ERGAENZUNGSTEXTE_TEILESTAMM = "TAL4XA_ADRESSE_ERGAENZUNGSTEXTE_TEILESTAMM";
    final static public String TAL4XA_SPRACHNEUTRALER_TEXT_TEILESTAMM = "TAL4XA_SPRACHNEUTRALER_TEXT_TEILESTAMM";
    final static public String TAL4XA_KZ_SICHERHEITSRELEVANT = "TAL4XA_KZ_SICHERHEITSRELEVANT";
    final static public String TAL4XA_KZ_DIEBSTAHLRELEVANT = "TAL4XA_KZ_DIENSTAHLRELEVANT";
    final static public String TAL4XA_KZ_CHINARELEVANT = "TAL4XA_KZ_CHINARELEVANT";
    final static public String TAL4XA_KZ_VEDOCRELEVANT = "TAL4XA_KZ_VEDOCRELEVANT";
    final static public String TAL4XA_KZ_NN_RELEVANT_1 = "TAL4XA_KZ_NN_RELEVANT_1";
    final static public String TAL4XA_KZ_NN_RELEVANT_2 = "TAL4XA_KZ_NN_RELEVANT_2";
    final static public String TAL4XA_KZ_NN_RELEVANT_3 = "TAL4XA_KZ_NN_RELEVANT_3";
    final static public String TAL4XA_KZ_NN_RELEVANT_4 = "TAL4XA_KZ_NN_RELEVANT_4";
    final static public String TAL4XA_VPD_IDENT = "TAL4XA_VPD_IDENT";
    final static public String TAL4XA_NR = "TAL4XA_NR";
    final static public String TAL4XA_FN_FOLGE = "TAL4XA_FN_FOLGE";
    final static public String TAL4XA_FN_SPRACHE = "TAL4XA_FN_SPRACHE";
    final static public String TAL4XA_FN_TABELLE = "TAL4XA_FN_TABELLE";
    final static public String TAL4XA_ART = "TAL4XA_ART";
    final static public String TAL4XA_TEXTE = "TAL4XA_TEXTE";
    final static public String TAL4XA_FGST_END_NR = "TAL4XA_FGST_END_NR";
    final static public String TAL4XA_BK_VERWENDUNG = "TAL4XA_BK_VERWENDUNG";
    final static public String TAL4XA_FAHRGESTELL = "TAL4XA_FAHRGESTELL";
    final static public String TAL4XA_AB_IDENT = "TAL4XA_AB_IDENT";
    final static public String TAL4XA_BIS_IDENT = "TAL4XA_BIS_IDENT";
    final static public String TAL4XA_HAUPT_IDENT = "TAL4XA_HAUPT_IDENT";
    final static public String TAL4XA_EINSATZ_TERMIN = "TAL4XA_EINSATZ_TERMIN";
    final static public String TAL4XA_STEUERCODE = "TAL4XA_STEUERCODE";
    final static public String TAL4XA_GUELTIGE_IDENT = "TAL4XA_GUELTIGE_IDENT";
    final static public String TAL4XA_UNGUELTIGE_IDENT = "TAL4XA_UNGUELTIGE_IDENT";
    final static public String TAL4XA_IDENT = "TAL4XA_IDENT";
    final static public String TAL4XA_PEM = "TAL4XA_PEM";
    final static public String TAL4XA_FARBTABELLE = "TAL4XA_FARBTABELLE";
    final static public String TAL4XA_FARBBENENNUNG = "TAL4XA_FARBBENENNUNG";
    final static public String TAL4XA_ES2 = "TAL4XA_ES2";
    final static public String TAL4XA_VARIANTENTABELLE = "TAL4XA_VARIANTENTABELLE";
    final static public String TAL4XA_VARIANTENBENENNUNG = "TAL4XA_VARIANTENBENENNUNG";
    final static public String TAL4XA_VARIANTE = "TAL4XA_VARIANTE";

    final static public String TAL46A_CODE_1 = "TAL46A_CODE_1";
    final static public String TAL46A_CODE_2 = "TAL46A_CODE_2";
    final static public String TAL46A_SA_STRICH = "TAL46A_SA_STRICH";
    final static public String TAL46A_SA_TYP = "TAL46A_SA_TYP";
    final static public String TAL46A_FN_HINWEISE = "TAL46A_FN_HINWEISE";
    final static public String TAL46A_VERBINDUNGS_SA = "TAL46A_VERBINDUNGS_SA";
    final static public String TAL46A_UNTERBAUMUSTER = "TAL46A_UNTERBAUMUSTER";

    public static final String EMPTY_TEXT_ADDRESS = "00000000";

    private static final String PICTURE_DATE_DUMMY = "DUMMY";

    // Der Import ist gerade in diesem Katalog
    private iPartsDataProduct currentProduct;
    private Set<String> nonVisibleProductModels; // Set mit allen Produkt-zu-Baumuster-Beziehungen, die nicht sichtbar sind
    // Zusatztext zum aktuellen Produkt-Baumuster
    private AddTextForProductModel currentAddTextForProductModel;
    private List<iPartsDataAssembly> currentAssembliesForKgInProduct;
    private String currentKgInProduct;

    // Map für alle Baumuster mit den Verknüpfungen zur DA_PRODUCT_MODELS Tabelle
    private Set<iPartsModelId> modelsForProductModelCheck;

    // Merker, dass wir im Lack und Leder Katalog sind
    private boolean currentProductIsSpecialCatalog;
    private boolean currentProductIsGlobalModel;

    // Diese Baumuster bzw. Typen sind in diesem Produkt für die Mengenmatrix enthalten
    private List<iPartsModelId> currentModelsOrTypesForQuantityInProduct;

    // Map von Typkennzahl auf gültige Baumuster für diese Typkennzahl in diesem Globalbaumuster-Produkt
    private Map<String, Set<iPartsModelId>> currentModelsForTypeInGlobalModelProduct;

    // Diese SAs sind in den entsprechenden KGs in diesem Baumuster-Katalog enthalten
    private Set<iPartsProductSAsId> currentSAsForKGInProduct;

    // Diese SAAs sind in diesem SA-Katalog enthalten
    private List<String> currentSAAsInProduct;  // todo Set wäre wahrscheinlich das richtige Interface

    // Der Import ist gerade in diesem Modul (KG/TU Knoten, SA-Modul)
    private iPartsDataAssembly currentAssembly;

    // Der Import ist gerade in dieser SA
    private String currentSANumber;

    // Werte für diesen Teilestamm wurden schon übernommen
    private Set<PartId> partsDone = new HashSet<>();

    private Map<String, EtkMultiSprache> cacheTermIdsToTexts = new HashMap<>();

    // Diese Kataloge ignorieren, es sind keine ELDAS-Kataloge
    private Set<String> ignoreProduct = new HashSet<>();

    // ImportRec vom letzten echten Stücklisteneintrag mit Teil, der keine Y-Teileposition ist
    private Map<String, String> lastCompletePartListEntryImportRec;

    // Letzter echter Stücklisteneintrag (bzw. Liste aufgrund unterschiedlicher Mengen pro Baumuster) mit Teil, der keine
    // Y-Teileposition ist
    private List<EtkDataPartListEntry> lastCompletePartListEntry = new ArrayList<>();

    private int currentCombinedTextSeqNo;
    private Set<iPartsFootNoteId> currentFootNotesForPartListEntry = new HashSet<>();

    // Liste aller SAA-Fußnoten-Referenzen, die nach dem Löschen der SA gespeichert werden müssen
    private List<iPartsDataFootNoteSaaRef> currentFootNoteSaaRefList = new DwList<>();

    // Liste mit allen Bildreferenzen, die bei AS-PLM angefragt werden
    private Map<AssemblyId, Set<iPartsPicReferenceId>> picReferenceIdsForAssembly = new HashMap<>();

    // Set mit allen Teilenummern im aktuellen Modul
    private Set<String> currentPartNumbersInAssembly = new HashSet<>();

    // Map mit Teilenummer auf Wahlweise-Teilenummern im aktuellen Modul
    private Map<String, Set<String>> currentWWPartNumbersToWWPartNumbersInAssembly = new LinkedHashMap<>();

    // Map mit allen Ersetzungen
    private Map<PartListEntryId, iPartsReplacement> replacementsForAssembly = new HashMap<>();

    // Map mit Teilenummer auf Stücklisteneinträge im aktuellen Modul
    private Map<String, List<EtkDataPartListEntry>> currentWWPartNumbersToPLEntriesInAssembly = new LinkedHashMap<>();

    private int currentRecordNo;

    private Tal4XAType talType;
    private DictImportTextIdHelper importTextIDHelper = new DictImportTextIdHelper(getProject());

    // Zuordnungstabelle von Sortimentsklasse ==> After Sales Produktklasse
    private Map<String, String> ac2pcMappingList;

    // Wenn ich zu Debugzwecken die Batchstatements abgeschalten habe, dann ist es wahrscheinlich, dass ich auch die BufferedSafe abschalten möchte
    private boolean doBufferSave = DBBase.BATCH_STATEMENTS_ENABLED;
    // Importiere Baumuster Zusatztexte (Langtext) - solange die Langtext-Verarbeitung nicht implementiert ist -> keine Langtexte importieren

    // Liste zum Merken der Modul-Werte DA_MODULE.DM_VARIANTS_VISIBLE um sie später wieder setzen zu können.
    // In der DB vorhandene Werte dürfen über den Import nicht überschrieben werden.
    Map<iPartsModuleId, Boolean> moduleVariantsVisibleMap = new HashMap<>();
    private iPartsMigrationFootnotesHandler footnotesHandler;
    // Weitere Variablen für den Import der Fußnotendaten
    private String currentFootNoteNumber;
    private boolean currentFootNoteIsValid = true;
    private boolean currentFootNoteIsColorTable; // Marker, ob die aktuelle Fußnote Teil einer Farb-Tabellenfußnote ist
    private boolean currentFootNoteIsTable;
    private iPartsFootNoteId currentTableFootNoteId;
    private ColortTablefootnote currentColorTablefootnote; // Die aktuelle Farb-Tabellenfßnote, die Zeile für Zeile aufgebaut wird
    // Sollte es mehr als eine Farb-Fußnote geben, dann werden hier alle Farbfußnoten gehalten
    private Map<String, List<ColortTablefootnote>> allCurrentColorTablefootnotes = new TreeMap<>();
    private Map<String, iPartsFootNoteContentId> responseDataTextInTableFootNoteToPseudoFNContentId;

    /**
     * Constructor Fixedlen-Import
     *
     * @param project
     */
    public MadTal4XABaseImporter(EtkProject project, String title, Tal4XAType talType) {
        super(project, title, new FilesImporterFileListType("MAD-Rohdaten", "!!MAD-Rohdaten", true, false, false,
                                                            new String[]{ MimeTypes.EXTENSION_GZ, MimeTypes.EXTENSION_ALL_FILES }));
        setIsImportWithOnlyNewDataObjects(true);
        this.talType = talType;
        ac2pcMappingList = new HashMap<>();
        modelsForProductModelCheck = new HashSet<>();
    }

    private void resetLastCompletePartListEntry() {
        lastCompletePartListEntryImportRec = null;
        lastCompletePartListEntry.clear();
    }

    @Override
    protected void setMustCheckData(AbstractKeyValueRecordReader importer) {

    }

    @Override
    protected boolean importTableIsValid(AbstractKeyValueRecordReader importer) {
        //Überprüfung der Lexika
        DictTxtKindIdByMADId dictTxtKindIdByMADId = DictTxtKindIdByMADId.getInstance(getProject());
        if (!dictTxtKindIdByMADId.checkDictionariesExistsWithErrorLogMessageForMAD(getMessageLog(), getLogLanguage(),
                                                                                   DictTextKindTypes.NEUTRAL_TEXT)) {
            return false;
        }
        return true;
    }

    @Override
    protected boolean isDialogRecordValid(AbstractKeyValueRecordReader importer, Map<String, String> importRec, List<String> errors) {
        return true;
    }

    /**
     * Eigentlich hier unnötig da der Importer nicht wiederverwendet wird. Könnte also auch in Konstruktor.
     */
    @Override
    protected void preImportTask() {
        footnotesHandler = new iPartsMigrationFootnotesHandler(getProject(), this,
                                                               iPartsDataFootNote.FOOTNOTE_PREFIX_ELDAS,
                                                               iPartsImportDataOrigin.ELDAS);
        currentProduct = null;
        currentAssembliesForKgInProduct = null;
        currentKgInProduct = null;
        currentProductIsSpecialCatalog = false;
        currentProductIsGlobalModel = false;
        currentModelsOrTypesForQuantityInProduct = null;
        currentModelsForTypeInGlobalModelProduct = null;
        currentSAsForKGInProduct = null;
        currentSAAsInProduct = null;
        currentAssembly = null;
        ignoreProduct.clear();
        resetLastCompletePartListEntry();
        currentFootNotesForPartListEntry.clear();
        currentFootNoteSaaRefList.clear();
        picReferenceIdsForAssembly.clear();
        currentPartNumbersInAssembly.clear();
        currentWWPartNumbersToWWPartNumbersInAssembly.clear();
        currentWWPartNumbersToPLEntriesInAssembly.clear();
        currentRecordNo = 0;
        progressMessageType = ProgressMessageType.READING;
        moduleVariantsVisibleMap.clear();
        currentFootNoteNumber = null;
        currentFootNoteIsValid = true;
        currentFootNoteIsTable = false;
        currentTableFootNoteId = null;
        footnotesHandler.clear();
        allCurrentColorTablefootnotes = new HashMap<>();
        responseDataTextInTableFootNoteToPseudoFNContentId = new HashMap<>();
        nonVisibleProductModels = new HashSet<>();
        setBufferedSave(doBufferSave);
    }


    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        boolean handled = false;
        currentRecordNo = recordNo;
        if (!importRec.isEmpty()) {
            // Aus dem Key des ersten Feldes den Alias (Datensatzart) bestimmen. Der Alias und das Feld ist mit '.' getrennt

            String key = importRec.keySet().iterator().next();
            String alias = TableAndFieldName.getFirstPart(key);

            if (alias != null) {
                String katalogOrSa;

                if (talType == Tal4XAType.TAL40A) {
                    katalogOrSa = importRec.get(alias + "." + TAL40A_KATALOG);
                    if (katalogOrSa == null) {
                        cancelImport(translateForLog("!!Katalog-Feld nicht gefunden"));
                        return;
                    }
                } else {
                    try {
                        katalogOrSa = importRec.get(alias + "." + TAL46A_SA_RUMPF);
                        if (katalogOrSa == null) {
                            cancelImport(translateForLog("!!SA-Rumpf-Feld nicht gefunden"));
                            return;
                        }
                        iPartsNumberHelper numberHelper = new iPartsNumberHelper();
                        katalogOrSa = numberHelper.unformatSaForDB(katalogOrSa);
                    } catch (RuntimeException e) {
                        getMessageLog().fireMessage(e.getMessage(), MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                        reduceRecordCount();
                        return;
                    }
                }

                if (talType == Tal4XAType.TAL40A) {
                    if ((currentProduct != null) && !katalogOrSa.equals(currentProduct.getAsId().getProductNumber())) {
                        // Es beginnt ein neuer Katalog -> der bisherige ist damit durch -> abspeichern und Product neu initialisieren
                        saveCurrentProduct();
                    }

                    // Dieser Katalog ist nicht aus Eldas -> Alle diese Datensätze überspringen
                    if (ignoreProduct.contains(katalogOrSa)) {
                        reduceRecordCount();
                        return;
                    }

                    if (currentProduct == null) {
                        // Falls das Produkt schon da ist -> erstmal löschen
                        currentProduct = new iPartsDataProduct(getProject(), new iPartsProductId(katalogOrSa));
                        boolean productVisible = false;
                        boolean productIsEpcRelevant = false;
                        boolean secondPartsEnabled = false;
                        boolean showLooseSas = false;
                        boolean useProductionAggregates = false;
                        String comment = "";
                        List<String> brands = new DwList<>();
                        List<String> validCountries = new DwList<>();
                        List<String> invalidCountries = new DwList<>();
                        List<String> originalASproductClasses = new DwList<>();
                        List<String> disabledFilters = new DwList<>();

                        EtkMultiSprache apsRemark = null;
                        String apsCodes = "";
                        String apsFromIdents = "";
                        String apsToIdents = "";

                        if (katalogOrSa.equals("598")) {
                            currentProductIsSpecialCatalog = true;
                        } else {
                            currentProductIsSpecialCatalog = false;
                        }

                        iPartsDocumentationType documentationType = iPartsModuleTypes.EDSRetail.getDefaultDocumentationType(); // default
                        if (currentProductIsSpecialCatalog) {
                            documentationType = iPartsModuleTypes.WorkshopMaterial.getDefaultDocumentationType();
                        }

                        // Folgende vorhandene Werte nicht überschreiben, bzw. den vorhandenen merken und wieder setzen.
                        EtkMultiSprache title = null;
                        boolean identClassOldSystematic = false;
                        boolean ttzFlag = false;
                        boolean scoringWithModelCodes = false;

                        if (currentProduct.existsInDB()) {
                            // Vorhandene Produktbezeichnung merken falls diese nicht leer ist
                            title = currentProduct.getFieldValueAsMultiLanguage(FIELD_DP_TITLE);
                            if (title.allStringsAreEmpty()) {
                                title = null;
                            }

                            // Den Wert für "Ident in der alten Systematik" übernehmen
                            identClassOldSystematic = currentProduct.getFieldValueAsBoolean(FIELD_DP_IDENT_CLASS_OLD);

                            // Werte die aus der Applikationsliste oder Edit schon vorhanden sein könnten zwischenspeichern
                            productVisible = currentProduct.getFieldValueAsBoolean(FIELD_DP_PRODUCT_VISIBLE);
                            productIsEpcRelevant = currentProduct.getFieldValueAsBoolean(FIELD_DP_EPC_RELEVANT);
                            comment = currentProduct.getFieldValue(FIELD_DP_COMMENT);
                            brands = currentProduct.getFieldValueAsSetOfEnum(FIELD_DP_BRAND);
                            validCountries = currentProduct.getFieldValueAsSetOfEnum(FIELD_DP_VALID_COUNTRIES);
                            invalidCountries = currentProduct.getFieldValueAsSetOfEnum(FIELD_DP_INVALID_COUNTRIES);
                            // Original AS Produktklassen merken, damit das darin enthaltene Powersystems nicht überschrieben wird
                            originalASproductClasses = currentProduct.getFieldValueAsSetOfEnum(FIELD_DP_ASPRODUCT_CLASSES);

                            // Werte die aus dem Auto-Product-Select Import (TAL47S) kommen merken
                            apsRemark = currentProduct.getFieldValueAsMultiLanguage(FIELD_DP_APS_REMARK);
                            apsCodes = currentProduct.getFieldValue(FIELD_DP_APS_CODE);
                            apsFromIdents = currentProduct.getFieldValue(FIELD_DP_APS_FROM_IDENTS);
                            apsToIdents = currentProduct.getFieldValue(FIELD_DP_APS_TO_IDENTS);

                            // Den Wert für (Farb-) Varianten anzeigen (ja/nein) aus den bestehenden Daten ermitteln und merken,
                            // um ihn später bei finishCurrentAssembly() wieder setzen zu können.
                            for (iPartsDataProductModules productModule : currentProduct.getProductModulesList()) {
                                iPartsModuleId moduleId = new iPartsModuleId(productModule.getAsId().getModuleNumber());
                                iPartsDataModule dataModule = new iPartsDataModule(getProject(), moduleId);
                                if (dataModule.existsInDB()) {
                                    moduleVariantsVisibleMap.put(moduleId, dataModule.isVariantsVisible());
                                }
                            }

                            // TTZ-Flag übernehmen
                            ttzFlag = currentProduct.getFieldValueAsBoolean(FIELD_DP_TTZ_FILTER);
                            // Kenner, ob bm-bildene Code beim Erweiterten-Code-Filter berücksichtigt werden sollen.
                            // Spielt im Moment für ELDAS keine Rolle, da DIALOG spezifisch. Bleibt trotzdem erhalten,
                            // falls der Kenner in Zukunft doch noch Verwendung findet
                            scoringWithModelCodes = currentProduct.getFieldValueAsBoolean(FIELD_DP_SCORING_WITH_MCODES);
                            // Auch die abschaltbaren Filtereinstellungen vom bereits vorhandenen Produkt übernehmen.
                            disabledFilters = currentProduct.getFieldValueAsSetOfEnum(FIELD_DP_DISABLED_FILTERS);

                            if (currentProduct.getDocumentationType() != iPartsDocumentationType.UNKNOWN) {
                                documentationType = currentProduct.getDocumentationType();
                            }
                            showLooseSas = currentProduct.getFieldValueAsBoolean(FIELD_DP_SHOW_SAS);
                            useProductionAggregates = currentProduct.getFieldValueAsBoolean(FIELD_DP_CAB_FALLBACK);

                            // Sichtbarkeit Baumuster am Produkt
                            nonVisibleProductModels.clear();
                            for (iPartsDataProductModels productModel : currentProduct.getProductModelsList()) {
                                if (!productModel.getFieldValueAsBoolean(FIELD_DPM_MODEL_VISIBLE)) {
                                    nonVisibleProductModels.add(productModel.getAsId().getModelNumber());
                                }
                            }

                            String message = translateForLog("!!Lösche vorhandenen Katalog %1.", currentProduct.getAsId().getProductNumber());
                            getMessageLog().fireMessage(message, MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);

                            if (!currentProduct.deleteFromDBWithModules(true, getMessageLog())) {
                                currentProduct = null;
                                cancelImport();
                                return;
                            }
                        }

                        currentProduct = new iPartsDataProduct(getProject(), new iPartsProductId(katalogOrSa));

                        currentModelsOrTypesForQuantityInProduct = new ArrayList<>();
                        currentAssembliesForKgInProduct = new ArrayList<>();
                        currentSAsForKGInProduct = new HashSet<>();
                        currentProduct.initAttributesWithDefaultValues(DBActionOrigin.FROM_EDIT);
                        currentProduct.setFieldValue(FIELD_DP_COMMENT, comment, DBActionOrigin.FROM_EDIT);
                        currentProduct.setFieldValueAsSetOfEnum(FIELD_DP_ASPRODUCT_CLASSES, originalASproductClasses, DBActionOrigin.FROM_EDIT);
                        currentProduct.setFieldValueAsBoolean(FIELD_DP_PRODUCT_VISIBLE, productVisible, DBActionOrigin.FROM_EDIT);
                        currentProduct.setFieldValueAsBoolean(FIELD_DP_EPC_RELEVANT, productIsEpcRelevant, DBActionOrigin.FROM_EDIT);
                        currentProduct.setFieldValueAsSetOfEnum(FIELD_DP_VALID_COUNTRIES, validCountries, DBActionOrigin.FROM_EDIT);
                        currentProduct.setFieldValueAsSetOfEnum(FIELD_DP_INVALID_COUNTRIES, invalidCountries, DBActionOrigin.FROM_EDIT);
                        currentProduct.setDocuMethod(documentationType.getDBValue());
                        currentProduct.setFieldValueAsBoolean(FIELD_DP_MIGRATION, true, DBActionOrigin.FROM_EDIT);
                        currentProduct.setFieldValue(FIELD_DP_SOURCE, iPartsImportDataOrigin.ELDAS.getOrigin(), DBActionOrigin.FROM_EDIT);
                        currentProduct.setDatasetDate(getDatasetDate());
                        currentProduct.setMigrationDate(getMigrationDate());
                        currentProduct.refreshModificationTimeStamp(); // DAIMLER-4841: Änderungszeitstempel am Produkt setzen

                        // brands nur vom vorhandenen Produkt übernehmen wenn mindestens eine brand dort gesetzt war, sonst gilt default = "MB"
                        if (!brands.isEmpty()) {
                            currentProduct.setFieldValueAsSetOfEnum(FIELD_DP_BRAND, brands, DBActionOrigin.FROM_EDIT);
                        }

                        // APS Werte vom vorhandenen Produkt setzen
                        if (apsRemark != null) {
                            currentProduct.setFieldValueAsMultiLanguage(FIELD_DP_APS_REMARK, apsRemark, DBActionOrigin.FROM_EDIT);
                        }
                        currentProduct.setFieldValue(FIELD_DP_APS_CODE, apsCodes, DBActionOrigin.FROM_EDIT);
                        currentProduct.setFieldValue(FIELD_DP_APS_FROM_IDENTS, apsFromIdents, DBActionOrigin.FROM_EDIT);
                        currentProduct.setFieldValue(FIELD_DP_APS_TO_IDENTS, apsToIdents, DBActionOrigin.FROM_EDIT);

                        // Die typischen Dinge für MAD-Daten schon vorbelegen
                        currentProduct.setFieldValue(FIELD_DP_STRUCTURING_TYPE, PRODUCT_STRUCTURING_TYPE.KG_TU.toString(), DBActionOrigin.FROM_EDIT);

                        currentProduct.setFieldValueAsBoolean(FIELD_DP_IS_SPECIAL_CAT, currentProductIsSpecialCatalog, DBActionOrigin.FROM_EDIT);

                        // Eine Bezeichnung haben wir nicht
                        if (title == null) {
                            title = new EtkMultiSprache();
                            if (currentProductIsSpecialCatalog) {
                                title.setText(Language.DE, "Lacke und Betriebsstoffe");
                            } else {
                                title.setText(Language.DE, katalogOrSa);
                                title.fillAllLanguages(getProject().getConfig().getDatabaseLanguages(), Language.DE);
                            }
                        }
                        currentProduct.setFieldValueAsMultiLanguage(FIELD_DP_TITLE, title, DBActionOrigin.FROM_EDIT);

                        // Werte aus ggf. vorhandenem Produkt zuweisen
                        currentProduct.setFieldValueAsBoolean(FIELD_DP_IDENT_CLASS_OLD, identClassOldSystematic, DBActionOrigin.FROM_EDIT);
                        currentProduct.setFieldValueAsBoolean(FIELD_DP_TTZ_FILTER, ttzFlag, DBActionOrigin.FROM_EDIT);
                        currentProduct.setFieldValueAsBoolean(FIELD_DP_SCORING_WITH_MCODES, scoringWithModelCodes, DBActionOrigin.FROM_EDIT);
                        currentProduct.setFieldValueAsBoolean(FIELD_DP_SHOW_SAS, showLooseSas, DBActionOrigin.FROM_EDIT);
                        currentProduct.setFieldValueAsBoolean(FIELD_DP_CAB_FALLBACK, useProductionAggregates, DBActionOrigin.FROM_DB);
                        currentProduct.setFieldValueAsSetOfEnum(FIELD_DP_DISABLED_FILTERS, disabledFilters, DBActionOrigin.FROM_EDIT);
                    }

                    if (alias.equals(SATZART_7)) {
                        if (ignoreProductType(importRec)) {
                            skipCurrentProduct();
                            reduceRecordCount();
                            return;
                        }
                    }

                    // Allgemeine Infos
                    if (alias.equals(SATZART_7)) {
                        importSatzart7(importRec);
                        String message = translateForLog("!!Bilde Katalog %1.", currentProduct.getAsId().getProductNumber());
                        getMessageLog().fireMessage(message, MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                        handled = true;
                    }

                    if (!currentProductIsSpecialCatalog) {
                        // Baumuster, die in diesem Katalog sind (2F=Fahrzeugbaumuster, 2A=Aggregatebaumuster)
                        if (!handled && (alias.equals(SATZART_2F) || alias.equals(SATZART_2A))) {
                            importSatzart2FAnd2A(importRec, alias);
                            handled = true;
                        }

                        // Baumusterzusatztexte
                        if (!handled && alias.equals(SATZART_2X)) {
                            importSatzart2X(importRec, alias);
                            handled = true;
                        }
                    }
                }


                // Speziell Daten TAL46A
                if (talType == Tal4XAType.TAL46A) {
                    // Kopfdatensatz der SAA
                    if (!handled && alias.equals(SATZART_6)) {
                        importSatzart6(importRec);
                        handled = true;
                    }

                    // Allgemeine Infos
                    if (!handled && alias.equals(SATZART_C)) {
                        importSatzartC(importRec);
                        handled = true;
                    }
                }


                // TU-Eintrag (Zeichungen der TU und Steuerung, dass hier die neue Baugruppe losgeht)
                if (!handled && (alias.equals(SATZART_9BS) || alias.equals(SATZART_DBS))) {
                    importSatzart9BSorDBS(alias, importRec);
                    handled = true;
                }

                // Stücklistenpositionen
                if (!handled && (alias.equals(SATZART_9) || alias.equals(SATZART_D))) {
                    importSatzart9orD(alias, importRec);
                    handled = true;
                }

                // Spezielle Fußnoten-Nummern (z.B. SA-Verwendungsnachweis)
                if (!handled && alias.equals(SATZART_A9)) {
                    handled = importSatzartA9(importRec);
                }

                // Fußnoten
                if (!handled && (alias.equals(SATZART_AX) || alias.equals(SATZART_AY) || alias.equals(SATZART_A9)
                                 || alias.equals(SATZART_EX) || alias.equals(SATZART_EY) || alias.equals(SATZART_E9))) {
                    handled = importFootNotes(alias, importRec);
                }

                // Baumuster-Konstruktionsgruppe (KG)
                if (!handled && alias.equals(SATZART_8)) {
                    handled = importSatzart8(importRec);
                }
            }
        }
        if (!handled) {
            reduceRecordCount();
        }
    }

    /**
     * Fußnoten (kurze Texte, lange Texte, Nummern, Farbtabellenfußnoten)
     * <p>
     * Zu den Einsatzfußnoten:
     * Es gibt Einsatzfußnoten aus
     * - Gatterfußnoten
     * - Fußnoten der Satzart A bzw. E / Textart 9 (Nummern)
     * Bei Gatterfußnoten sind Zeilen mit Idents als Rückmeldedaten zu übernehmen, andere Zeilen werden zur Fußnote an Werkseinsatzdaten.
     * Eine Einsatzfußnote kann also zu Idents werden, zu Fußnote an Werkseinsatzdaten, oder zu beidem.
     *
     * @param importRec
     */
    private boolean importFootNotes(String alias, Map<String, String> importRec) {
        if ((((talType == Tal4XAType.TAL40A) && (currentProduct != null)) || ((talType == Tal4XAType.TAL46A) && (currentAssembly != null)))) {
            String footNoteNumber = importRec.get(alias + "." + TAL4XA_NR).trim();

            // Falls es sich um eine Standardfußnote handelt, diese nicht erneut importieren
            if (footnotesHandler.isStandardFootNote(footNoteNumber)) {
                currentFootNoteNumber = footNoteNumber;
                currentFootNoteIsValid = false;
                return true;
            }

            if (!Utils.objectEquals(currentFootNoteNumber, footNoteNumber)) { // neue Fußnotennummer
                currentFootNoteNumber = footNoteNumber;
                currentFootNoteIsValid = true;
            } else if (!currentFootNoteIsValid) {
                // Falls der erste Satz nicht entsprechend aufgebaut ist, müssen der aktuelle Satz und alle weiteren Sätze zur
                // gleichen Fussnotennummer überlesen werden, wobei Tabellenfußnoten trotzdem sauber beendet werden müssen
                if (currentFootNoteIsTable) {
                    String tableFootNote = importRec.get(alias + "." + TAL4XA_FN_TABELLE);
                    if (tableFootNote.equals("Z")) {
                        currentFootNoteIsTable = false;
                    }
                }
                return true;
            }

            boolean isFootNoteForProduct = alias.equals(SATZART_AX) || alias.equals(SATZART_AY) || alias.equals(SATZART_A9);
            iPartsFootNoteId footNoteIdLine;
            iPartsFootNoteId footNoteIdReal = currentTableFootNoteId;
            if (isFootNoteForProduct) {
                footNoteIdLine = footnotesHandler.getFootNoteIdForProduct(importRec.get(alias + "." + TAL40A_KG), footNoteNumber, currentProduct.getAsId().getProductNumber());
            } else {
                footNoteIdLine = getFootNoteIdForSA(footNoteNumber);
            }

            // Marker, ob es eine Farb-Tabellenfußnote ist (Fußnotennummern >= 900, 3-stellig und erstes Zeichen ist 9 bis Z);
            boolean isColorTablefootnote = footnotesHandler.isColorFootnote(footNoteNumber);

            if (!currentFootNoteIsTable) {
                footNoteIdReal = footNoteIdLine;
            } else {
                // In den Tabellenfußnoten kann die FußnotenId innerhalb der Fußnote wechseln, wenn es sich um Farb-Tabellenfußnoten handelt
                // Prüfe deshalb hier, ob sich diese ID innerhalb der Fußnote ändert und es keine Farb-Tabellenfußnote ist.
                // Falls ja, dann ist die Fußnote nicht gültig
                if (!footNoteIdReal.equals(footNoteIdLine) && !isColorTablefootnote) {
                    // Wechsel der Fußnotennummer ist nur in Farbfußnoten erlaubt. Hier ist so ein Beispiel, welches eigentlich nicht vorkommen darf
                    getMessageLog().fireMessage(translateForLog("!!Tabellenfußnote \"%1\" wechselt in Record %2 die Fußnotennummer (\"%3\"). Das ist nur bei Farbfußnoten erlaubt.",
                                                                footNoteIdReal.getFootNoteId(), Long.toString(currentRecordNo),
                                                                footNoteNumber), MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);

                    currentFootNoteIsValid = false;
                    footnotesHandler.addToCurrentInvalidFootnoteIds(footNoteIdLine);
                    return true;
                }
            }

            boolean isShortTextFootNote = alias.equals(SATZART_AX) || alias.equals(SATZART_EX);
            boolean isLongTextFootNote = alias.equals(SATZART_AY) || alias.equals(SATZART_EY);
            boolean isNumberFootNote = alias.equals(SATZART_A9) || alias.equals(SATZART_E9);

            // Kurze und lange Texte für BM/SA
            if (isShortTextFootNote || isLongTextFootNote || isNumberFootNote) { // todo Bedingung jetzt eigentlich obsolet da alle drei Fälle
                boolean ignoreLine = false;
                String textDE = "";
                // deutschen Text bei kurzer Fußnote bestimmen
                if (isShortTextFootNote) {
                    List<String> texts = getAsArray(importRec.get(alias + "." + TAL4XA_TEXTE), true);
                    if (!texts.isEmpty()) {
                        textDE = StrUtils.trimRight(texts.get(0));
                    }
                }

                // deutschen Text bei langer Fußnote bestimmen
                if (isLongTextFootNote) {
                    // Sprache
                    Language lang = getLangFromFootNoteInteger(importRec.get(alias + "." + TAL4XA_FN_SPRACHE));

                    // nur die deutschen Texte einlesen, die Überetzungen kommen aus dem Wörterbuch
                    if (lang == Language.DE) {
                        List<String> memoLines = getAsArray(importRec.get(alias + "." + TAL4XA_TEXTE), true);

                        // Leere Zeilen hinten entfernen
                        for (int i = memoLines.size() - 1; i >= 0; i--) {
                            if (memoLines.get(i).trim().isEmpty()) {
                                memoLines.remove(i);
                            }
                        }

                        // Memotext zusammenbauen
                        textDE = "";
                        for (String s : memoLines) {
                            if (!textDE.isEmpty()) {
                                textDE += "\n";
                            }
                            textDE += StrUtils.trimRight(s);
                        }
                    } else {
                        // Alle anderen Sprachen kommen aus dem Wörterbuch, können hier ignoriert werden -> der Datensatz wird einfach übersprungen
                        ignoreLine = true;
                    }
                }

                if (isNumberFootNote) {
                    footnotesHandler.addResponseFootNoteContentToMap(footNoteIdReal, new iPartsFootNoteContentId(footNoteIdReal.getFootNoteId(),
                                                                                                                 importRec.get(alias + "." + TAL4XA_FN_FOLGE)));
                    textDE = StrUtils.replaceSubstring(importRec.get(alias + "." + TAL4XA_FGST_END_NR).trim(), "\n", "");
                    textDE = DictMultilineText.getInstance().convertFootNoteForImport(textDE, true);
                }

                if (!ignoreLine) {
                    footnotesHandler.handleCrossRefFootnotes(footNoteIdReal, getProductNoFromCurrentProduct(), importRec.get(alias + "." + TAL40A_KG), currentSANumber, textDE, isFootNoteForProduct);
                    iPartsFootNoteContentId footNoteContentId = new iPartsFootNoteContentId(footNoteIdReal.getFootNoteId(),
                                                                                            importRec.get(alias + "." + TAL4XA_FN_FOLGE));
                    // Spezialitäten für die Tabellenfußnoten
                    String tableFootNote = importRec.get(alias + "." + TAL4XA_FN_TABELLE);
                    boolean isTableFootNoteStart = tableFootNote.equals("S");
                    boolean isResponseDataFootNote = false;
                    iPartsFootNoteContentId pseudoFNContentId = null;
                    // Unterscheidung, Einsatzdatenfußnote aus Tabellenfußnote oder eigene Einsatzfußnote
                    if (isTableFootNoteStart) {
                        pseudoFNContentId = responseDataTextInTableFootNoteToPseudoFNContentId.get(textDE);
                        if (pseudoFNContentId == null) {
                            // Einsatzfußnoten erhalten ein eigenes Flag "TF" (TableFootNote)
                            iPartsFootNoteId pseudoFNId = footnotesHandler.getFNIdForResponseDataInColorTable(footNoteContentId.getFootNoteId(),
                                                                                                              responseDataTextInTableFootNoteToPseudoFNContentId.size());
                            if (pseudoFNId != null) {
                                pseudoFNContentId = new iPartsFootNoteContentId(pseudoFNId.getFootNoteId(), footNoteContentId.getFootNoteLineNo());
                                isResponseDataFootNote = footnotesHandler.isFootNoteLineHandledAsResponseData(textDE, pseudoFNContentId);
                                if (isResponseDataFootNote) {
                                    responseDataTextInTableFootNoteToPseudoFNContentId.put(textDE, pseudoFNContentId);
                                    footnotesHandler.addResponseDataFootNoteFromTableFootNote(footNoteContentId.getFootNoteId(), pseudoFNContentId.getFootNoteId());
                                }
                            }
                        } else {
                            footnotesHandler.addResponseDataFootNoteFromTableFootNote(footNoteContentId.getFootNoteId(), pseudoFNContentId.getFootNoteId());
                        }
                    } else {
                        isResponseDataFootNote = footnotesHandler.isFootNoteLineHandledAsResponseData(textDE, footNoteContentId);
                    }

                    // Zeile ggf. als Rückmeldedaten behandeln
                    if (isResponseDataFootNote) {
                        iPartsFootNoteCatalogueRefId catRefId;
                        if (pseudoFNContentId != null) {
                            // Referenz auf gültige ID, falls Einsatzfußnote aus Tabellenfußnote
                            catRefId = new iPartsFootNoteCatalogueRefId(pseudoFNContentId.getFootNoteId(), "",
                                                                        pseudoFNContentId.getFootNoteLineNo(), currentFootNoteNumber);
                        } else {
                            catRefId = new iPartsFootNoteCatalogueRefId(footNoteContentId.getFootNoteId(), "",
                                                                        footNoteContentId.getFootNoteLineNo(), currentFootNoteNumber);
                        }
                        // todo DAIMLER-6646: hier Text von #-Fußnote umformatieren
                        footnotesHandler.addToFootnoteResponseDataForCrossRef(catRefId, textDE);
                        // Werkseinsatzdaten-Fußnoten können am Anfang einer Farbtabellenfußnote vorkommen. Falls das
                        // der Fall ist, dann muss die Tabellenfußnote weiter verarbeitet werden.
                        if (!isTableFootNoteStart) {
                            return true;
                        }
                    }


                    boolean currentFootNoteWasTable = currentFootNoteIsTable;
                    boolean currentFootNoteWasColorTable = currentFootNoteIsColorTable;
                    if (currentFootNoteIsTable || currentFootNoteIsColorTable) {
                        // Eigentlich endet eine Tabellenfußnote mit "Z", manchmal aber auch gar nicht oder falsch mit "S"
                        // Bei S wird jetzt einfach davon ausgegangen, dass eine neue Tabelle anfangen soll
                        // Wenn die Tabellenfußnote gar nicht endet, dann beginnt in der Regel eine neue TU
                        // Deahalb wird diese Variable auch im finishAssembly zurückgesetzt
                        if (tableFootNote.equals("Z")) {
                            if (currentFootNoteIsColorTable) {
                                // Wenn die aktuelle Farb-Tabellenfußnote zu ende ist, dann leg sie in die Map mit allen
                                // aktuellen Farb-Tabellenfußnoten ab. Sortiert wird in der Map nach der kleinsten
                                // Fußnotennummer in einer Farb-Tabellenfußnote
                                String firstFootnoteNumberinTable = currentColorTablefootnote.getFirstFootnotenumberInTable();
                                List<ColortTablefootnote> colorTablefootnotesWithSameStartNumber = allCurrentColorTablefootnotes.get(firstFootnoteNumberinTable);
                                if (colorTablefootnotesWithSameStartNumber == null) {
                                    colorTablefootnotesWithSameStartNumber = new ArrayList<>();
                                    allCurrentColorTablefootnotes.put(firstFootnoteNumberinTable, colorTablefootnotesWithSameStartNumber);
                                }
                                colorTablefootnotesWithSameStartNumber.add(currentColorTablefootnote);
                            }
                            currentFootNoteIsTable = false;
                            currentFootNoteIsColorTable = false;
                        }
                    }

                    // Es startet eine Tabellenfußnote. Die FußnotenID wird sich gemerkt und mit dieser weitergearbeitet
                    if (isTableFootNoteStart) {
                        if (isColorTablefootnote) {
                            currentFootNoteIsColorTable = true;
                            currentFootNoteWasColorTable = true;
                            // Es startet eine neue Farb-Tabellenfußnote
                            currentColorTablefootnote = new ColortTablefootnote(footnotesHandler);
                        } else {
                            if (currentFootNoteIsTable) {
                                getMessageLog().fireMessage(translateForLog("!!Tabellenfußnote \"%1\" hat kein gültiges Ende in Record %2",
                                                                            currentTableFootNoteId.getFootNoteId(), Long.toString(currentRecordNo)),
                                                            MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
                            }
                            currentFootNoteIsTable = true;
                            currentFootNoteWasTable = true;
                            currentTableFootNoteId = footNoteIdReal;
                        }
                    }

                    EtkMultiSprache multiLang = null;
                    if (isShortTextFootNote || isLongTextFootNote || isNumberFootNote) {
                        // Nach deutschem Text in den Fußnoten bzw. sprachneutralen Texten suchen
                        multiLang = new EtkMultiSprache();
                        if (isShortTextFootNote) {
                            textDE = DictMultilineText.getInstance().convertFootNoteForImport(textDE, currentFootNoteWasTable || currentFootNoteWasColorTable);
                        }
                        // Hinweis: Wenn eine Gatterfussnote ohne Ident gefunden wurde, dann wird der komplette Textinhalt
                        // samt Gatter übernommen, z.B. "#BF 11.01.04"
                        multiLang.setText(Language.DE, textDE);
                        boolean dictSuccessful;
                        if (isShortTextFootNote || isLongTextFootNote) {
                            dictSuccessful = importTextIDHelper.handleFootNoteTextWithCache(multiLang, "", DictHelper.getMADForeignSource(),
                                                                                            TableAndFieldName.make(TABLE_DA_FN_CONTENT, FIELD_DFNC_TEXT));
                        } else { // isNumberFootNote -> sprachneutraler Text
                            dictSuccessful = importTextIDHelper.handleNeutralTextWithCache(multiLang, TableAndFieldName.make(TABLE_DA_FN_CONTENT, FIELD_DFNC_TEXT));
                        }

                        if (!iPartsMigrationHelper.checkImportTextID(this, importTextIDHelper, dictSuccessful, currentRecordNo)) {
                            return true;
                        }
                    }

                    // Übernehme die ID Farb-Tabellenfußnote nur, wenn sie gerade anfängt.
                    if (currentFootNoteWasColorTable && footNoteContentId.getFootNoteLineNo().equals(iPartsMigrationFootnotesHandler.COLOR_TABLEFOOTNOTE_START)) {
                        currentColorTablefootnote.addSingleFootnoteId(footNoteIdLine);
                    }

                    /**
                     * Wir speichern den Fußnoten-Hauptdatensatz in einer Map anstatt direkt in der DB. Dies deshalb weil sich für A9/E9 Einsatzfußnoten i.A. erst aus
                     * einer Folgezeile ergibt dass es sich um eine A9/E9 Einsatzfußnote handelt, die zu einer Fußnote an Werkseinsatzdaten wird.
                     * Damit müssten wir hier die Fußnote anlegen und später nochmal lesen und verändern müssen um das "FD"
                     * Kennzeichen für Werkseinsatzdaten zu setzen. Dies bedeutete einen Bruch beim "Buffered Save".
                     */
                    footnotesHandler.addToCurrentFootnoteMapIfNotExists(footNoteIdReal, footNoteNumber);

                    // Fußnoteninhalt erzeugen und speichern
                    iPartsDataFootNoteContent dataFootNoteContent = new iPartsDataFootNoteContent(getProject(), footNoteIdReal,
                                                                                                  importRec.get(alias + "." + TAL4XA_FN_FOLGE));
                    saveFootNoteContent(dataFootNoteContent, isShortTextFootNote, isLongTextFootNote, isNumberFootNote, multiLang, textDE);
                }
            }

            return true;
        }

        return false;
    }

    private void saveFootNoteContent(iPartsDataFootNoteContent dataFootNoteContent, boolean isShortTextFootNote,
                                     boolean isLongTextFootNote, boolean isNumberFootNote,
                                     EtkMultiSprache multiLang, String textDE) {
        dataFootNoteContent.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
        if (isShortTextFootNote || isLongTextFootNote) {
            dataFootNoteContent.setFieldValueAsMultiLanguage(FIELD_DFNC_TEXT, multiLang, DBActionOrigin.FROM_EDIT);
        } else if (isNumberFootNote) {
            dataFootNoteContent.setFieldValue(FIELD_DFNC_TEXT_NEUTRAL, textDE, DBActionOrigin.FROM_EDIT);
        }
        saveToDB(dataFootNoteContent);
    }


    private void skipCurrentProduct() {
        // Dieses Produkt soll nicht importiert werden, z.B. weil es ein Dialogprodukt ist
        // Nehme alle bis dahin gemachten Änderungen zurück und Starte die Transaktion neu
        // Möglich ist das, weil nach jedem Produkt commitet wird.
        getProject().getDbLayer().rollback();
        getProject().getDbLayer().startTransaction();

        String message = translateForLog("!!Katalog %1 ist kein ELDAS-Katalog und wird übersprungen", currentProduct.getAsId().getProductNumber());

        getMessageLog().fireMessage(message, MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);

        ignoreProduct.add(currentProduct.getAsId().getProductNumber());
        currentProduct = null;
        currentAssembliesForKgInProduct = null;
        currentKgInProduct = null;
        currentModelsOrTypesForQuantityInProduct = null;
        currentModelsForTypeInGlobalModelProduct = null;
        currentSAsForKGInProduct = null;
        resetLastCompletePartListEntry();
    }


    /**
     * In Statzart 7 steht, ob der Katalog aus ELDAS kommt. Teste hier das entsprechende Feld
     *
     * @param importRec
     */
    private boolean ignoreProductType(Map<String, String> importRec) {
        String alias = SATZART_7;

        // Ermitteln, in welchem Knoten das Produkt eingehängt werden soll
        String productType = importRec.get(alias + "." + TAL40A_KATALOG_TYP);

        if (productType.trim().isEmpty()) {
            // Leerer Producttyp == ELDAS
            return false;
        } else {
            // "DIA" == Dialog "MCC" = Smart
            // Außer ELDAS soll stand jetzt nichts importiert werden
            return true;
        }
    }


    /**
     * Stammdaten des Produktes (Kataloges) importieren
     *
     * @param importRec
     */
    private void importSatzart7(Map<String, String> importRec) {
        String alias = SATZART_7;

        // Ermitteln, in welchem Knoten das Produkt eingehängt werden soll
        String baumusterArt = importRec.get(alias + "." + TAL40A_BAUMUSTERART);

        List<String> sortimentsKlassen = getAsArray(importRec.get(alias + "." + TAL4XA_SORTIMENTSKLASSEN), false);
        // Sortimentsklassen zum Produkt hinzufügen
        if (!sortimentsKlassen.isEmpty()) {
            currentProduct.setFieldValueAsSetOfEnum(FIELD_DP_ASSORTMENT_CLASSES, sortimentsKlassen, DBActionOrigin.FROM_EDIT);

            // Sortimentsklassen in After Sales Produktklassen umwandeln
            // Über die Liste der Sortimentsklassen iterieren und für jedes Element eine AS-Produktklasse anhängen, falls noch nicht vorhanden.
            DIALOGImportHelper helper = new DIALOGImportHelper(getProject(), null, "");
            List<String> afterSalesProductClasses = new ArrayList<>();
            for (String assortmentClass : sortimentsKlassen) {
                // Die gecachten Werte:
                // Erst versuchen den übersetzten Wert aus der Mapping-Tabelle zu holen und nur wenn er nicht darin enthalten ist ...
                String mappedValue = ac2pcMappingList.get(assortmentClass);
                if (mappedValue == null) {
                    // ... ihn aus der Datenbank lesen ...
                    mappedValue = helper.convertAssortmentClassToReferencingASProductClass(assortmentClass);
                    // ... und für die nächste Sortimentsklassenzuordnung zwischenpuffern.
                    ac2pcMappingList.put(assortmentClass, mappedValue);
                }
                // Zu mehreren Sortimentsklassen gibt es die gleiche AS Produktklasse (also [n:1])
                // Sicherstellen, dass die gefundene AS Produktklasse nur einmal in der Zielliste vorhanden ist.
                if (!afterSalesProductClasses.contains(mappedValue)) {
                    afterSalesProductClasses.add(mappedValue);
                }
            }

            // überprüfen ob bisher in den AS Produktklassen Powersystems enhalten war (darf nicht überschrieben werden,
            // weil es nur vom Applikationslisten Importer geschrieben wird)
            List<String> originalASproductClasses = currentProduct.getFieldValueAsSetOfEnum(FIELD_DP_ASPRODUCT_CLASSES);
            if (originalASproductClasses.contains(AS_PRODUCT_CLASS_POWERSYSTEMS)) {
                afterSalesProductClasses.add(AS_PRODUCT_CLASS_POWERSYSTEMS);
            }

            // Die After Sales Produktklassen speichern.
            if (!afterSalesProductClasses.isEmpty()) {
                currentProduct.setFieldValueAsSetOfEnum(FIELD_DP_ASPRODUCT_CLASSES, afterSalesProductClasses, DBActionOrigin.FROM_EDIT);
            }
        }

        // Baumuster in diesem Katalog bestimmen. Die Baumuster in diesem Feld sind aber nur für die Mengenangabe relevant.
        // Die echte Baumusterliste ist in Satzart 2


        // Liste der Baumuster: leere Werte am Ende interessieren nicht, aber falls zwischen drin ein leeres Baumuster ist,
        // muss das für den Mengenindex berücksichtigt werden
        List<String> models = getAsArray(StrUtils.trimRight(importRec.get(alias + "." + TAL40A_BAUMUSTER)), true);

        String modelType = importRec.get(alias + "." + TAL40A_TYP);
        modelType = modelType.trim();

        // Handelt es sich um einen Globalbaumuster-Katalog oder einen Echt-Baumusterkatalog
        currentProductIsGlobalModel = currentProduct.getDocumentationType() == iPartsDocumentationType.BCS_PLUS_GLOBAL_BM;

        // Falls kein Globalbaumuster:
        // - Falls model nur 3-stellig, dann ist modelType + model die Baumusternummer
        // - Falls model 6-stellig, dann ist das die Baumusternummer
        //
        // Falls Globalbaumuster:
        // - Nur 3-stellige Typkennzahl speichern und später über currentModelsForTypeInGlobalModelProduct alle gültigen
        //   Baumuster für diese Typkennzahl an den Stücklisteneintrag hängen
        // - Bei nur einer Typkennzahl currentModelsOrTypesForQuantityInProduct überhaupt nicht befüllen, weil dann alle
        //   Stücklisteneinträge immer für alle Baumuster des Globalbaumusters gültig sind
        if (!currentProductIsGlobalModel || (models.size() > 1)) {
            for (String model : models) {
                model = iPartsNumberHelper.getPlainModelNumber(model);

                if (!model.isEmpty()) {
                    if (currentProductIsGlobalModel) {
                        model = model.substring(0, 3); // bei Globalbaumuster sind die ersten drei Stellen die Typkennzahl
                    } else if (model.length() <= 3) {
                        model = modelType + model;
                    }

                    if (baumusterArt.equals(AGGREGATE_TYPE_CAR)) {
                        model = iPartsConst.MODEL_NUMBER_PREFIX_CAR + model;
                    } else {
                        model = iPartsConst.MODEL_NUMBER_PREFIX_AGGREGATE + model;
                    }
                    currentModelsOrTypesForQuantityInProduct.add(new iPartsModelId(model));
                } else {
                    // Zwischendrin können auch leere Baumuster stehen. Diese müssen berücksichtigt werden, da sonst der Baumusterindex nicht mehr stimmt.
                    // DAIMLER-5091
                    currentModelsOrTypesForQuantityInProduct.add(new iPartsModelId(""));
                }
            }
        }

        // Die Baumusterart als Aggregatetyp in das Produkt eintragen
        if (!currentProductIsSpecialCatalog) {
            currentProduct.setFieldValue(FIELD_DP_AGGREGATE_TYPE, baumusterArt, DBActionOrigin.FROM_EDIT);
        }

    }

    /**
     * Konstruktionsgruppe
     *
     * @param importRec
     * @return
     */
    private boolean importSatzart8(Map<String, String> importRec) {
        // Das letzte Modul noch abspeichern (saveCurrentFootNotesForPartListEntries() würde ansonsten interne Datenstrukturen
        // wie z.B. moduleHasColorTablefootnotes leeren)
        finishCurrentAssembly();

        iPartsMigrationHelper.saveCurrentAssembliesForKgInProduct(this, currentAssembliesForKgInProduct, currentKgInProduct); // alle Module für die aktuelle KG speichern
        footnotesHandler.saveCurrentFootNotesForPartListEntries(allCurrentColorTablefootnotes, true); // alle Fußnoten für die aktuelle KG speichern
        resetLastCompletePartListEntry(); // neue KG -> letzter echter Stücklisteneintrag mit Teil wird ungültig
        currentKgInProduct = importRec.get(SATZART_8 + "." + TAL40A_KG);
        responseDataTextInTableFootNoteToPseudoFNContentId = new HashMap<>();
        return true;
    }

    /**
     * Teilepositionen importieren
     */
    private void importSatzart9orD(String alias, Map<String, String> importRec) {
        String partNumber = importRec.get(alias + "." + TAL4XA_TEILENUMMER);

        // DAIMLER-3564, Teilepositionen mit NUR-Minusteil ignorieren!
        String omittedPart = importRec.get(alias + "." + TAL4XA_ENTFALL_TEILENUMMER);
        omittedPart = omittedPart.trim();
        if (iPartsMigrationHelper.isOnlyOmmitedPartData(this, partNumber, omittedPart, currentRecordNo)) {
            lastCompletePartListEntryImportRec = null; // nur zur Sicherheit für etwaige Y-Teilepositionen
            return;
        }

        // V-Positionen sind reine Textpositionen in der Stückliste
        // Vergleich auf partNumber enthält automatisch das Feld "Kennbuchstabe" mit Wert "V" an Position 23 (= Position
        // der Teilenummer) und Länge 1
        boolean isVTextPos = iPartsMigrationHelper.isVTextPartListEntry(partNumber);

        // Vergleich auf partNumber enthält automatisch das Feld "Kennbuchstabe" mit Wert "Y" an Position 23 (= Position
        // der Teilenummer) und Länge 1
        boolean isYPartPos = !isVTextPos && iPartsMigrationHelper.isYPartListEntry(partNumber);

        boolean isYTextPos = false; // Y-Textposition (Hinweistext)

        String steeringAndGearboxValue = importRec.get(alias + "." + TAL4XA_LENKUNG_GETRIEBE);
        List<String> quantityValues = getAsArray(importRec.get(alias + "." + TAL4XA_MENGE_JE_BAUMUSTER), true);

        String shelfLife = iPartsMigrationHelper.getShelfLife(currentProductIsSpecialCatalog, quantityValues);

        if (!isYPartPos || (lastCompletePartListEntryImportRec == null)) { // isYPartPos && (lastCompletePartListEntryImportRec == null) -> Y-Textposition
            // Y-Textposition erkennen
            if (isYPartPos && (lastCompletePartListEntryImportRec == null)) {
                isYTextPos = true;
                isYPartPos = false;
            }

            lastCompletePartListEntryImportRec = importRec;
            lastCompletePartListEntry.clear();
            currentCombinedTextSeqNo = 1;
            currentFootNotesForPartListEntry.clear();
        } else {
            // Flag für V-Textposition aus letztem vollständigen Stücklisteneintrag bestimmen
            String lastCompletePartListEntryPartNumber = lastCompletePartListEntryImportRec.get(alias + "." + TAL4XA_TEILENUMMER);
            isVTextPos = StrUtils.countCharacters(lastCompletePartListEntryPartNumber.toUpperCase().trim(), 'V') == lastCompletePartListEntryPartNumber.trim().length();

            // Flag für Y-Textposition aus letztem vollständigen Stücklisteneintrag bestimmen
            isYTextPos = iPartsMigrationHelper.isYPartListEntry(lastCompletePartListEntryPartNumber);

            boolean isTextPos = isVTextPos || isYTextPos; // Textposition = V- oder Y-Textposition

            // Teilenummer auf leer setzen, damit sie vom lastCompletePartListEntryImportRec weiter unten übernommen wird
            importRec.put(alias + "." + TAL4XA_TEILENUMMER, "");

            // Überprüfen, ob es sich um eine eigenständige Teileposition für die Y-Position handelt oder um weitere Attribute
            // zur vorhergehenden letzten vollständigen Teileposition
            boolean isYForNewPartPos = false;

            boolean isSteeringAndGearboxDifferent = false;
            if (!isVTextPos) { // Lenkung, Getriebe und Menge bei V-Textposition nicht importieren (bei Y-Textpositionen Lenkung und Getriebe wohl schon)
                // Ist Lenkung/Getriebe unterschiedlich?
                String steeringAndGearboxValueLastRec = lastCompletePartListEntryImportRec.get(alias + "." + TAL4XA_LENKUNG_GETRIEBE);
                isSteeringAndGearboxDifferent = iPartsMigrationHelper.isDifferentSteeringAndGearValue(steeringAndGearboxValue, steeringAndGearboxValueLastRec);
                if (isSteeringAndGearboxDifferent) {
                    isYForNewPartPos = true;
                }

                if (!isYTextPos) { // Menge bei Y-Textposition nicht importieren
                    // Sind echte Mengenangaben vorhanden?
                    boolean isYWithQuantity = iPartsMigrationHelper.hasRealQuantityValues(quantityValues);
                    if (isYWithQuantity) {
                        isYForNewPartPos = true;
                    }
                    // Falls der Y-Datensatz keine echten Mengenangaben hat, das gesamte Feld auf leer setzen, damit es vom lastCompletePartListEntryImportRec
                    // weiter unten übernommen wird
                    if (!isYWithQuantity) {
                        importRec.put(alias + "." + TAL4XA_MENGE_JE_BAUMUSTER, "");
                    }
                }
            }

            if (isYForNewPartPos) { // eigenständige Teileposition für die Y-Position
                // DAIMLER-7891 Code sollen nicht veerbt werden -> Alter Wert speichern um, überschriebenes rückgängig zu machen
                String oldValueCode = importRec.get(alias + "." + TAL4XA_CODE_B);

                // Fast alle Attribute vom lastCompletePartListEntryImportRec übernehmen, die im aktuellen importRec leer sind
                // (sofern es sich nicht um ein verändertes Lenkung/Getriebe-Attribut handelt, welches an der Y-Teileposition
                // absichtlich leer gesetzt wurde und bei lastCompletePartListEntryImportRec nicht leer ist)
                iPartsMigrationHelper.copyValuesFromPreviousDataset(importRec, lastCompletePartListEntryImportRec, isSteeringAndGearboxDifferent, (alias + "." + TAL4XA_LENKUNG_GETRIEBE));

                // Code wieder auf den vorherigen Wert setzen
                if (oldValueCode != null) {
                    importRec.put(alias + "." + TAL4XA_CODE_B, oldValueCode);
                }

                // partNumber, steeringAndGearboxValue und quantityValues aktualisieren, da diese von lastCompletePartListEntryImportRec
                // übernommen worden sein könnten
                partNumber = importRec.get(alias + "." + TAL4XA_TEILENUMMER);
                steeringAndGearboxValue = importRec.get(alias + "." + TAL4XA_LENKUNG_GETRIEBE);
                quantityValues = getAsArray(importRec.get(alias + "." + TAL4XA_MENGE_JE_BAUMUSTER), true);
            } else {
                partNumber = StrUtils.replaceSubstring(lastCompletePartListEntryPartNumber, " ", "");

                boolean hasSupportedYImportData = iPartsMigrationHelper.handleUnsupportedFields(this, isTextPos, currentRecordNo,
                                                                                                importRec.get(alias + "." + TAL4XA_EINRUECKZAHL),
                                                                                                importRec.get(alias + "." + TAL4XA_ERSETZT),
                                                                                                importRec.get(alias + "." + TAL4XA_REP_TNR_1),
                                                                                                importRec.get(alias + "." + TAL4XA_REP_TNR_N),
                                                                                                lastCompletePartListEntryImportRec.get(alias + "." + TAL4XA_EINRUECKZAHL),
                                                                                                lastCompletePartListEntryImportRec.get(alias + "." + TAL4XA_TEILENUMMER));
                String code = "";
                if ((!isTextPos) && (talType == Tal4XAType.TAL40A)) {
                    code = importRec.get(alias + "." + TAL4XA_CODE_B);
                }

                if (!isTextPos) { // SAA-Gültigkeiten nicht für Textpositionen erweitern
                    // SAA-Gültigkeiten in lastCompletePartListEntry erweitern
                    if (talType == Tal4XAType.TAL40A) {
                        iPartsMigrationHelper.handleSaaValidity(lastCompletePartListEntry, getSatzart9SaaBkValidity(alias, importRec));
                    }
                }

                // Ergänzungstext in lastCompletePartListEntry erweitern
                EtkMultiSprache additionalText = getAdditionalTextForSatzart9orD(alias, importRec);
                currentCombinedTextSeqNo = iPartsMigrationHelper.handleAddOrNeutralTextForYPartPosition(getProject(), this, additionalText, currentCombinedTextSeqNo, lastCompletePartListEntry);

                // Sprachneutralen Text in lastCompletePartListEntry erweitern
                EtkMultiSprache neutralText = iPartsMigrationHelper.handleNeutralText(this, importTextIDHelper, importRec.get(alias + "." + TAL4XA_SPRACHNEUTRALER_TEXT), currentRecordNo);
                currentCombinedTextSeqNo = iPartsMigrationHelper.handleAddOrNeutralTextForYPartPosition(getProject(), this, neutralText, currentCombinedTextSeqNo, lastCompletePartListEntry);


                // Fußnoten in lastCompletePartListEntry erweitern
                Set<iPartsFootNoteId> footNoteIds = getFootNotesForSatzart9orD(alias, importRec, isVTextPos);
                iPartsMigrationHelper.handleFootNotesForYPartPosition(footNoteIds, currentFootNotesForPartListEntry, lastCompletePartListEntry, footnotesHandler);

                // Den Code an den bereits vorhandenen Code anhängen
                iPartsMigrationHelper.handleCodeValueForYPartPosition(code, lastCompletePartListEntry);

                if (!isVTextPos) { // Folgende Attribute nicht bei V-Textpositionen importieren (bei Y-Textpositionen wohl schon)
                    // Wahlweise-Teile
                    String wwFlag = lastCompletePartListEntryImportRec.get(alias + "." + TAL4XA_WW_KZ);
                    List<String> wwPartNumbersFormatted = getAsArray(importRec.get(alias + "." + TAL4XA_WW_TNR), false);
                    iPartsMigrationHelper.handleWWSets(partNumber, wwFlag, wwPartNumbersFormatted, currentWWPartNumbersToWWPartNumbersInAssembly);


                    // Ersetzungen und Mitlieferteile in Y-Teileposition
                    boolean hasReplacement = lastCompletePartListEntryImportRec.get(alias + "." + TAL4XA_ERSETZT).equals("R");
                    if (hasReplacement) {
                        String replacePart = importRec.get(alias + "." + TAL4XA_REP_TNR_1).replace(" ", "");
                        List<String> includeParts = getAsArray(importRec.get(alias + "." + TAL4XA_REP_TNR_N), false, false);
                        iPartsMigrationHelper.handleReplacePartAndIncludePartsForYPartPosition(this, lastCompletePartListEntry, replacementsForAssembly, replacePart, includeParts, currentRecordNo);
                    }
                }

                if (!hasSupportedYImportData) {
                    reduceRecordCount();
                }

                return; // Import von dieser Y-Teileposition ist damit abgeschlossen
            }
        }

        if (currentAssembly == null) {
            // Das kann eigentlich nur bei einer komplett verdaddelten Importdatei passieren
            String errorMessage = translateForLog("!!Satzart 9 (Teile) darf erst nach einem TU-Steuersatz kommen");
            Logger.getLogger().throwRuntimeException(errorMessage);
        }

        // Einrückzahl und Code
        String hierarchyValue = importRec.get(alias + "." + TAL4XA_EINRUECKZAHL);
        String codeValue = "";
        if (talType == Tal4XAType.TAL40A) { // Code gibt es nur bei TAL40A
            codeValue = importRec.get(alias + "." + TAL4XA_CODE_B);
        }

        boolean isTextPos = isVTextPos || isYTextPos; // Textposition = V- oder Y-Textposition

        // Ergänzungstext
        int localCurrentCombinedTextSeqNo = 1;
        int currentAdditionalTextSeqNo = localCurrentCombinedTextSeqNo;
        EtkMultiSprache additionalText = getAdditionalTextForSatzart9orD(alias, importRec);
        if (additionalText != null) {
            localCurrentCombinedTextSeqNo++;
        }

        // Sprachneutraler Text
        int currentNeutralTextSeqNo = localCurrentCombinedTextSeqNo;
        EtkMultiSprache neutralText = iPartsMigrationHelper.handleNeutralText(this, importTextIDHelper, importRec.get(alias + "." + TAL4XA_SPRACHNEUTRALER_TEXT), currentRecordNo);
        if (neutralText != null) {
            localCurrentCombinedTextSeqNo++;
        }

        // Nur bei einer echten Teileposition die Sequenznummer für die kombinierten Texte auf den lokalen Wert setzen,
        // da es bei Y-Positionen für eigenständige Stücklisteneinträge keine Ergänzungen von kombinierten Texten geben
        // kann und ansonsten die Sequenznummer für den letzten vollständigen Stücklisteneintrag fälschlicherweise wieder
        // auf 1 zurückgesetzt werden würde
        if (!isYPartPos) {
            currentCombinedTextSeqNo = localCurrentCombinedTextSeqNo;
        }

        // Fußnoten
        Set<iPartsFootNoteId> footNoteIds = getFootNotesForSatzart9orD(alias, importRec, isVTextPos);
        if (footNoteIds != null) {
            currentFootNotesForPartListEntry.addAll(footNoteIds);
        }

        /**
         * In TU-Stückliste eintragen
         */

        DBDataObjectList<EtkDataPartListEntry> destPartList = currentAssembly.getPartListUnfiltered(null, false, false);

        if (!isTextPos) { // Material nicht bei Textpositionen importieren
            partNumber = StrUtils.replaceSubstring(partNumber, " ", "");
            importPartsDataFromSatzart9orD(alias, importRec, shelfLife);
            currentPartNumbersInAssembly.add(partNumber);
        } else {
            partNumber = ""; // Teilenummer bei Textpositionen auf leer setzen für virtuelles Material
        }

        // Wahlweise-Teile
        boolean isWW = false;
        if (!isVTextPos) {
            String wwFlag = importRec.get(alias + "." + TAL4XA_WW_KZ);
            List<String> wwPartNumbersFormatted = getAsArray(importRec.get(alias + "." + TAL4XA_WW_TNR), false);
            isWW = iPartsMigrationHelper.handleWWSets(partNumber, wwFlag, wwPartNumbersFormatted, currentWWPartNumbersToWWPartNumbersInAssembly);
        }

        iPartsMigrationHelper.QuantityForModelOrSAA quantityForModelOrSAA;
        if (!isTextPos) {
            // Ermitteln der Menge bei Baumuster oder SAA-Gültigkeit
            if ((talType == Tal4XAType.TAL40A)) {
                List<String> models = new ArrayList<>();
                for (iPartsModelId modelId : currentModelsOrTypesForQuantityInProduct) {
                    models.add(modelId.getModelNumber());
                }
                if (currentProductIsGlobalModel && (currentModelsForTypeInGlobalModelProduct == null)) {
                    currentModelsForTypeInGlobalModelProduct = iPartsMigrationHelper.loadAllModelsForTypeInGlobaldModelProduct(currentProduct.getProductModelsList());
                }
                quantityForModelOrSAA = iPartsMigrationHelper.handleQuantityForModel(this, models, quantityValues,
                                                                                     currentModelsForTypeInGlobalModelProduct,
                                                                                     currentProduct.getAsId().getProductNumber(),
                                                                                     currentProductIsGlobalModel);
            } else if ((talType == Tal4XAType.TAL46A)) {
                quantityForModelOrSAA = new iPartsMigrationHelper.QuantityForModelOrSAA();
                // Ermitteln der Menge bei SAA-Ausführungen
                int intervall = Integer.parseInt(importRec.get(alias + "." + TAL46A_SA_INTERVALL));

                String saRumpf;
                try {
                    iPartsNumberHelper numberHelper = new iPartsNumberHelper();
                    saRumpf = numberHelper.unformatSaForDB(importRec.get(alias + "." + TAL46A_SA_RUMPF));
                } catch (RuntimeException e) {
                    getMessageLog().fireMessage(e.getMessage(), MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
                    reduceRecordCount();
                    return;
                }

                // Intervall 1 = SAA 0 - 9, Intervall 2 10-19 hier muss je nach Intervall die in diesem Intervall gemeinten SAAs-Strichausführungen ermittelt werden
                for (int i = 0; i < 10; i++) {
                    int strichAusfuehrung = (intervall - 1) * 10;
                    strichAusfuehrung += i + 1;

                    String strichAusfuehrungString = Integer.toString(strichAusfuehrung);
                    strichAusfuehrungString = StrUtils.leftFill(strichAusfuehrungString, 2, '0');
                    strichAusfuehrungString = saRumpf + strichAusfuehrungString;

                    if (currentSAAsInProduct.contains(strichAusfuehrungString)) {
                        // Für jede Strichausführung muss die Menge ermittelt werden
                        // Das Array quantityValues ist nicht unbedingt so lange, wie die Anzahl der Baumuster
                        // weil leer Werte hinten evtl. abgeschnitten sein können

                        String quantity = "";
                        if (i < quantityValues.size()) {
                            quantity = quantityValues.get(i);
                        }

                        quantity = formatQuantityValue(quantity);

                        quantityForModelOrSAA.add(quantity, strichAusfuehrungString);
                    }
                }
            } else {
                quantityForModelOrSAA = new iPartsMigrationHelper.QuantityForModelOrSAA();
            }
        } else {
            // Dummy-Eintrag für eine leere Menge bei Textpositionen
            quantityForModelOrSAA = new iPartsMigrationHelper.QuantityForModelOrSAA();
            quantityForModelOrSAA.add("", "");
        }

        // Ermitteln der SAA/BK-Gültigkeit bei den Stücklisteneinträgen, nur TAL40
        Set<String> saaBkValidity;
        if (!isTextPos && (talType == Tal4XAType.TAL40A)) { // SAA-Gültigkeiten nicht bei Textpositionen importieren
            saaBkValidity = getSatzart9SaaBkValidity(alias, importRec);
        } else {
            saaBkValidity = new LinkedHashSet<>();
        }

        // Für jede verschiedene Menge wird ein Datensatz geschrieben
        for (String currentQuantity : quantityForModelOrSAA.getQuantities()) {
            if (!isTextPos && currentQuantity.isEmpty()) { // bei Textpositionen gibt es nur genau eine Menge und die ist leer
                // Keine Quantity -> diese Baumuster sind nicht verbaut
                continue;
            }

            // Startwert für die laufende Nummer und Sequenznummer auf Basis der höchsten existierenden laufenden
            // Nummer bzw. Sequenznummer bestimmen
            // Die Sequenznumber kann hier als String aufgefasst werden, weil bei diesem Import immer alles neu gemacht wird
            int destLfdNr = 0;
            String destSeqNr = SortBetweenHelper.getFirstSortValue();
            for (EtkDataPartListEntry partListEntry : destPartList) {
                destLfdNr = Math.max(destLfdNr, Integer.valueOf(partListEntry.getAsId().getKLfdnr()));

                // Ermittele den nächsten Sequenzewert, das Ergebnis ist der höchste Sequenznummer innerhalb der gesamten Stückliste. Das ist dann der Wert für das Einfügen hinten an der Stückliste
                String nextSeqNr = SortBetweenHelper.getNextSortValue(partListEntry.getFieldValue(EtkDbConst.FIELD_K_SEQNR));

                if (SortBetweenHelper.isGreater(nextSeqNr, destSeqNr)) {
                    destSeqNr = nextSeqNr;
                }
            }
            destLfdNr++;

            PartListEntryId destPartListEntryId = new PartListEntryId(currentAssembly.getAsId().getKVari(), currentAssembly.getAsId().getKVer(),
                                                                      EtkDbsHelper.formatLfdNr(destLfdNr));

            EtkDataPartListEntry destPartListEntry = EtkDataObjectFactory.createDataPartListEntry(getProject(), destPartListEntryId);
            destPartListEntry.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);

            // Sequenznummer setzen
            destPartListEntry.setFieldValue(EtkDbConst.FIELD_K_SEQNR, destSeqNr, DBActionOrigin.FROM_EDIT);

            // K_SOURCE_GUID setzen
            destPartListEntry.setFieldValue(iPartsConst.FIELD_K_SOURCE_GUID, getEldasSourceGUID(destPartListEntryId.getKVari(),
                                                                                                destPartListEntryId.getKLfdnr()),
                                            DBActionOrigin.FROM_EDIT);

            destPartListEntry.setFieldValue(FIELD_K_MENGE, currentQuantity, DBActionOrigin.FROM_EDIT);

            if (!isTextPos && (talType == Tal4XAType.TAL40A)) { // Gültige Baumuster nicht bei Textpositionen importieren
                iPartsMigrationHelper.setModelValidity(destPartListEntry, quantityForModelOrSAA, currentQuantity, currentProductIsSpecialCatalog, currentProduct.getProductModelsList().size());
            }

            if ((talType == Tal4XAType.TAL40A) && currentProductIsSpecialCatalog) {
                // Bei TAL40 Lacke und Betriebsstoffe Katalogen die Sortimentsklassengültigkeit prüfen
                // Leeres Feld bedeutet alle Sortimentsklassen sind gültig. Laut DAIMLER-4265 sollen dann auch alle
                // Sortimentsklassen eingetragen werden (auch bei reinen TextPos(V-Positionen))
                iPartsMigrationHelper.setPClassValidity(destPartListEntry);
            }

            if (!isTextPos && (talType == Tal4XAType.TAL46A)) { // Mengen pro SAA nicht bei Textpositionen importieren
                // Bei den SAA-Katalogen ist hier für jede Strichausführung die Menge gespeichert
                // Damit ist dieser Stücklisteneintrag bei dieser Menge nur gültig, bei den bestimmten SAA-Nummern
                saaBkValidity.clear();
                saaBkValidity.addAll(quantityForModelOrSAA.getNumbers(currentQuantity));
            }

            iPartsMigrationHelper.setBestFlag(destPartListEntry, isTextPos, currentQuantity);

            // Positionsnummer (führende Nullen sollen entfernt werden)
            String posNr = StrUtils.removeLeadingCharsFromString(importRec.get(alias + "." + TAL4XA_BT_POS), '0');
            iPartsMigrationHelper.handleFieldRelatedToTextValue(destPartListEntry, saaBkValidity, partNumber, posNr, isTextPos, isVTextPos);

            if (!isTextPos && (talType == Tal4XAType.TAL46A)) {// SAA-Intervalle nicht bei Textpositionen importieren
                // Bei den SAA-Katalogen kann es wegen den Intervallen sein, dass die gleichen Positionen an mehreren Stellen kommen und nur wegen der Beschränkung auf
                // 10 Strichausführungen pro Intervall doppelt sind. Versuche diese Daten hier wieder zu verdichten
                if (iPartsMigrationHelper.handleSimiliarSaPartlistEntry(currentAssembly, destPartListEntry)) {
                    continue;
                }
            }


            if (!isVTextPos) { // Folgende Attribute nicht bei V-Textpositionen importieren (bei Y-Textpositionen wohl schon)
                iPartsMigrationHelper.assignSteeringAndGearboxType(this, steeringAndGearboxValue, destPartListEntry,
                                                                   partNumber,
                                                                   (talType == Tal4XAType.TAL40A));

                // Einrückzahl und Code zuweisen
                iPartsMigrationHelper.assignHierarchyValue(this, hierarchyValue, destPartListEntry, currentRecordNo);
                if (talType == Tal4XAType.TAL40A) {
                    iPartsMigrationHelper.assignCodeValue(codeValue, destPartListEntry);
                }

                // Falls es sich um ein Wahlweise-Teil handelt, diesen Stücklisteneintrag entsprechend merken
                iPartsMigrationHelper.addPartListEntryIfWW(destPartListEntry, partNumber, currentWWPartNumbersToPLEntriesInAssembly, isWW);

                // Ersetungen und Mitlieferteile
                if (importRec.get(alias + "." + TAL4XA_ERSETZT).equals("R")) {
                    String replacePart = importRec.get(alias + "." + TAL4XA_REP_TNR_1).replace(" ", "");
                    List<String> includeParts = getAsArray(importRec.get(alias + "." + TAL4XA_REP_TNR_N), false, false);
                    iPartsMigrationHelper.handleReplacementAndIncludeParts(this, destPartListEntry, partNumber,
                                                                           replacePart, includeParts,
                                                                           replacementsForAssembly, currentRecordNo);
                }
            }

            destPartList.add(destPartListEntry, DBActionOrigin.FROM_EDIT);

            iPartsMigrationHelper.finishSinglePartListEntry(this, getProject(), destPartListEntry, additionalText,
                                                            currentAdditionalTextSeqNo, neutralText, currentNeutralTextSeqNo,
                                                            isYPartPos, lastCompletePartListEntry, footNoteIds, footnotesHandler);

        }
    }

    private String getProductNoFromCurrentProduct() {
        if (currentProduct != null) {
            return currentProduct.getAsId().getProductNumber();
        }
        return "";
    }

    private Set<iPartsFootNoteId> getFootNotesForSatzart9orD(String alias, Map<String, String> importRec, boolean isVTextPos) {
        Set<iPartsFootNoteId> footNoteIds = null;
        if (!isVTextPos) { // Fußnoten nicht bei V-Textpositionen importieren (bei Y-Textpositionen wohl schon)
            List<String> footNoteNumbers = getAsArray(importRec.get(alias + "." + TAL4XA_FN_HINWEISE), false);
            String tuvValue = importRec.get(alias + "." + TAL4XA_TUV_KZ);
            if (talType == Tal4XAType.TAL40A) {

                footNoteIds = footnotesHandler.handleFootnotesForModel(footNoteNumbers, tuvValue,
                                                                       getProductNoFromCurrentProduct(),
                                                                       currentKgInProduct, true);
            } else {
                footNoteIds = footnotesHandler.handleFootnotesForSA(footNoteNumbers, tuvValue, currentSANumber, true);
            }
        }
        return footNoteIds;
    }

    private EtkMultiSprache getAdditionalTextForSatzart9orD(String alias, Map<String, String> importRec) {
        return getMultiLangFromDict(alias, importRec, TAL4XA_ADRESSE_ERGAENZUNGSTEXTE, TAL4XA_ERGAENZUNGSTEXT_DE,
                                    TAL4XA_ERGAENZUNGSTEXT_FREMD, null, DictTextKindTypes.ADD_TEXT,
                                    TABLE_DA_COMB_TEXT, FIELD_DCT_DICT_TEXT, false);
    }


    private String formatQuantityValue(String quantityValue) {
        return iPartsMigrationHelper.formatQuantityValue(quantityValue);
    }


    private Set<String> getSatzart9SaaBkValidity(String alias, Map<String, String> importRec) {
        if (talType == Tal4XAType.TAL40A) {
            String type = importRec.get(alias + "." + TAL4XA_ART);
            List<String> values = null;
            if (type.equals("SA")) {
                values = getAsArray(importRec.get(alias + "." + TAL40A_SA_SNR), true);
            } else if (type.equals("BK")) {
                values = getAsArray(importRec.get(alias + "." + TAL40A_BK_SNR), false);
            }
            return iPartsMigrationHelper.handleSasOrBkValidityForModel(this, type, values);
        }
        return new LinkedHashSet<>();
    }


    /**
     * Teilestammdaten von Satzart 9 importieren
     *
     * @param importRec
     * @param shelfLife kann null sein
     */
    private void importPartsDataFromSatzart9orD(String alias, Map<String, String> importRec, String shelfLife) {
        String partNumber = importRec.get(alias + "." + TAL4XA_TEILENUMMER);
        partNumber = StrUtils.replaceSubstring(partNumber, " ", "");

        PartId partId = new PartId(partNumber, "");

        if (!partsDone.contains(partId)) {
            partsDone.add(partId);

            EtkDataPart part = EtkDataObjectFactory.createDataPart(getProject(), partId);

            // Nur anlegen, falls das Teil noch nicht da ist.
            if (!part.existsInDB()) {
                part = EtkDataObjectFactory.createDataPart(getProject(), partId);
                part.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);

                // Verarbeite mögliche ES1 und/oder ES2 Schlüssel an der Teilenummer
                DIALOGImportHelper.handleESKeysInDataPart(getProject(), part, getMessageLog(), getLogLanguage());

                // Benennung aus Datensatzart9
                EtkMultiSprache description = getMultiLangFromDict(alias, importRec, TAL4XA_TERMID, TAL4XA_BENENNUNG_DE,
                                                                   TAL4XA_BENENNUNG, DictTextKindRSKTypes.MAT_AFTER_SALES,
                                                                   null, TABLE_MAT, FIELD_M_TEXTNR, true);

                if (description != null) {
                    part.setFieldValueAsMultiLanguage(FIELD_M_TEXTNR, description, DBActionOrigin.FROM_EDIT);
                }


            }
            part.setFieldValue(FIELD_M_BESTNR, part.getAsId().getMatNr(), DBActionOrigin.FROM_EDIT);

            if (shelfLife != null) {
                part.setFieldValue(FIELD_M_SHELF_LIFE, shelfLife, DBActionOrigin.FROM_EDIT);
            }
            // Datenquelle setzen
            part.addSetOfEnumValueToFieldValue(FIELD_M_SOURCE, iPartsImportDataOrigin.MAD.getOrigin(), DBActionOrigin.FROM_EDIT);

            saveToDB(part);
        }
    }

    private EtkMultiSprache getMultiLangFromDict(String alias, Map<String, String> importRec, String termIdRecField, String textGermanRecField,
                                                 String textForeignLanguagesRecField, DictTextKindRSKTypes rskType, DictTextKindTypes textKindType,
                                                 String importTable, String importFieldName, boolean removeLeadingZerosFromTermId) {
        String tableAndFieldName = TableAndFieldName.make(importTable, importFieldName);
        EtkMultiSprache multiLang;

        String termId = importRec.get(alias + "." + termIdRecField);
        if (termId.equals(EMPTY_TEXT_ADDRESS)) {
            termId = "";
        }

        String textId = "";
        if (!termId.isEmpty()) { // textId über Lexikon
            // Text zunächst im lokalen Cache suchen
            String cacheTermId;
            if (removeLeadingZerosFromTermId) {
                termId = iPartsTermIdHandler.removeLeadingZerosFromTermId(termId);
            }
            if (rskType != null) {
                cacheTermId = rskType.getRSKId() + "@" + termId;
            } else {
                cacheTermId = textKindType.getMadId() + "@" + termId;
            }
            multiLang = cacheTermIdsToTexts.get(cacheTermId);
            if (multiLang != null) {
                return multiLang;
            }

            // Kein Eintrag im Cache gefunden -> neues EtkMultiSprache erzeugen
            if (rskType != null) {
                // iParts TextId über RSK-ID bestimmen
                textId = DictHelper.getRSKTextId(rskType, termId);
            } else {
                // iParts TextId über ELDAS-ID bestimmen
                textId = importTextIDHelper.getDictTextIdForEldasId(textKindType, termId);
                if (textId == null) {
                    getMessageLog().fireMessage(translateForLog("!!Keine Lexikon Text-ID zur ELDAS TextNr \"%1\" im Record %2 gefunden",
                                                                termId, String.valueOf(currentRecordNo)),
                                                MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
                    return null;
                }
            }

            multiLang = getProject().getDbLayer().getLanguagesTextsByTextId(textId);
            if (multiLang == null) {
                // Diese TermId ist noch nicht in der Datenbank -> lege die an
                multiLang = new EtkMultiSprache();
                multiLang.setTextId(textId);
            }

            cacheTermIdsToTexts.put(cacheTermId, multiLang);
        } else {
            multiLang = new EtkMultiSprache();
        }

        // Bei einem Datensatz mit Ersetzungsflag stehen hier die Ersetzungsteile und nicht die Ergänzungstexte
        if (!importRec.get(alias + "." + TAL4XA_ERSETZT).equals("R")) {
            // Teste, ob für die Sprache schon ein Text existiert, nur falls noch kein Text existiert den aus MAD übernehmen
            if (!multiLang.spracheExists(Language.DE)) {
                multiLang.setText(Language.DE, importRec.get(alias + "." + textGermanRecField));
            }

            List<String> foreignLanguages = getAsArray(importRec.get(alias + "." + textForeignLanguagesRecField), true);

            // Falls keine Texte vorhanden sind und die Liste leer ist -> Auffüllen mit leeren Strings
            StrUtils.rightFillStringListWithString(foreignLanguages, "", 5);

            if (!multiLang.spracheExists(Language.EN)) {
                multiLang.setText(Language.EN, foreignLanguages.get(0));
            }
            if (!multiLang.spracheExists(Language.FR)) {
                multiLang.setText(Language.FR, foreignLanguages.get(1));
            }
            if (!multiLang.spracheExists(Language.ES)) {
                multiLang.setText(Language.ES, foreignLanguages.get(2));
            }
            if (!multiLang.spracheExists(Language.PT)) {
                multiLang.setText(Language.PT, foreignLanguages.get(3));
            }
            if (!multiLang.spracheExists(Language.IT)) {
                multiLang.setText(Language.IT, foreignLanguages.get(4));
            }
        }

        if (((textId == null) || textId.isEmpty()) && multiLang.allStringsAreEmpty()) { // überhaupt kein Text vorhanden (weder TextId noch direkter Text im Datensatz)
            return null;
        }

        boolean dictSuccessful;
        if (rskType != null) {
            dictSuccessful = importTextIDHelper.handleDictTextId(rskType, multiLang, textId, DictHelper.getMADForeignSource(), tableAndFieldName);
        } else {
            boolean dictTextCreationAllowed = (textKindType == DictTextKindTypes.ADD_TEXT) || (textKindType == DictTextKindTypes.NEUTRAL_TEXT);
            dictSuccessful = importTextIDHelper.handleDictTextId(textKindType, multiLang, textId, DictHelper.getMADForeignSource(),
                                                                 dictTextCreationAllowed, tableAndFieldName);
        }

        if (!iPartsMigrationHelper.checkImportTextID(this, importTextIDHelper, dictSuccessful, currentRecordNo)) {
            return null;
        }

        return multiLang;
    }

    /**
     * TU-Positionen importieren
     *
     * @param importRec
     */
    private void importSatzart9BSorDBS(String alias, Map<String, String> importRec) {
        //erstmal das letzte Modul abspeichern
        finishCurrentAssembly();

        resetLastCompletePartListEntry();

        EtkMultiSprache moduleName = new EtkMultiSprache();

        List<String> descriptions = getAsArray(importRec.get(alias + "." + TAL4XA_TU_BENENNUNG), true);

        // Falls keine Texte vorhanden sind und die Liste leer ist -> Auffüllen mit leeren Strings
        StrUtils.rightFillStringListWithString(descriptions, "", 6);

        moduleName.setText(Language.DE, descriptions.get(0));
        moduleName.setText(Language.EN, descriptions.get(1));
        moduleName.setText(Language.FR, descriptions.get(2));
        moduleName.setText(Language.ES, descriptions.get(3));
        moduleName.setText(Language.PT, descriptions.get(4));
        moduleName.setText(Language.IT, descriptions.get(5));

        if (talType == Tal4XAType.TAL40A) {
            // Sind wir in einem gültigen Katalog?
            if (currentProduct != null) {
                // Es beginnt ein neue TU-Knoten -> neues Modul anlegen
                KgTuId kgTuId = new KgTuId(importRec.get(alias + "." + TAL40A_KG),
                                           importRec.get(alias + "." + TAL4XA_TU));
                AssemblyId newAssemblyId = new AssemblyId(EditModuleHelper.buildKgTuModuleNumber(currentProduct.getAsId(),
                                                                                                 kgTuId, getProject()), "");

                // Dokumentationstyp als Produkt bestimmen
                iPartsDocumentationType documentationType = currentProduct.getDocumentationType();

                // Modultyp aus Dokumentationstyp bestimmen ausser bei Spezialkatalogen
                iPartsModuleTypes moduleType;
                if (currentProductIsSpecialCatalog) {
                    moduleType = iPartsModuleTypes.WorkshopMaterial;
                } else {
                    moduleType = documentationType.getModuleType(false);
                }

                // Fallback auf Modultyp EDSRetail und den zugehörigen Dokumentationstyp
                if (moduleType == iPartsModuleTypes.UNKNOWN) {
                    moduleType = iPartsModuleTypes.EDSRetail;
                }
                if (documentationType == iPartsDocumentationType.UNKNOWN) {
                    documentationType = moduleType.getDefaultDocumentationType();
                }
                currentAssembly = EditModuleHelper.createAndSaveModuleWithKgTuAssignment(newAssemblyId,
                                                                                         moduleType,
                                                                                         moduleName,
                                                                                         currentProduct.getAsId(),
                                                                                         kgTuId,
                                                                                         getProject(),
                                                                                         documentationType,
                                                                                         false, null);

            }
        } else { // SA
            try {
                iPartsNumberHelper numberHelper = new iPartsNumberHelper();
                currentSANumber = numberHelper.unformatSaForDB(importRec.get(alias + "." + TAL46A_SA_RUMPF));
            } catch (RuntimeException e) {
                getMessageLog().fireMessage(e.getMessage(), MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
                reduceRecordCount();
                return;
            }
            AssemblyId newAssemblyId = new AssemblyId(iPartsConst.SA_MODULE_PREFIX + currentSANumber, "");

            // Im SA Modus muss das Assembly erst gelöscht werden
            boolean oldModuleHidden = false;
            EtkDataAssembly assembly = EtkDataObjectFactory.createDataAssembly(getProject(), newAssemblyId);
            if (assembly.existsInDB()) {
                if (assembly instanceof iPartsDataAssembly) {
                    iPartsDataAssembly iPartsAssembly = (iPartsDataAssembly)assembly;
                    oldModuleHidden = iPartsAssembly.getModuleMetaData().getFieldValueAsBoolean(FIELD_DM_MODULE_HIDDEN);
                    moduleVariantsVisibleMap.put(new iPartsModuleId(newAssemblyId.getKVari()), iPartsAssembly.getModuleMetaData().isVariantsVisible());
                    iPartsAssembly.delete_iPartsAssembly(true);
                }
            }


            String message = translateForLog("!!Lege %1 an.", newAssemblyId.getKVari());
            getMessageLog().fireMessage(message, MessageLogType.tmlMessage, MessageLogOption.WRITE_ONLY_IN_LOG_FILE, MessageLogOption.TIME_STAMP);
            iPartsSAModulesId saModulesId = new iPartsSAModulesId(currentSANumber);
            currentAssembly = EditModuleHelper.createAndSaveModuleWithSAAssignment(newAssemblyId,
                                                                                   iPartsModuleTypes.SA_TU,
                                                                                   moduleName,
                                                                                   null,
                                                                                   saModulesId,
                                                                                   null,
                                                                                   getProject(),
                                                                                   iPartsModuleTypes.SA_TU.getDefaultDocumentationType(),
                                                                                   iPartsImportDataOrigin.ELDAS,
                                                                                   false,
                                                                                   DCAggregateTypes.UNKNOWN,
                                                                                   true, null);

            // Jetzt erst die SAA-Fußnoten-Referenzen speichern, weil sonst oben beim Löschen vom SA-Modul auch die neuen
            // SAA-Fußnoten-Referenzen gelöscht werden würden
            for (iPartsDataFootNoteSaaRef dataFootNoteSaaRef : currentFootNoteSaaRefList) {
                saveToDB(dataFootNoteSaaRef);
            }
            currentFootNoteSaaRefList.clear();

            // Soll das das SA-Modul ausgeblendet werden?
            if (oldModuleHidden || iPartsMigrationHelper.isSAHidden(saModulesId, getProject())) {
                currentAssembly.getModuleMetaData().setFieldValueAsBoolean(FIELD_DM_MODULE_HIDDEN, true, DBActionOrigin.FROM_EDIT);
            }
        }

        if (!currentProductIsSpecialCatalog) {
            // Bildtafel-IDs hinzufügen
            List<String> imagesIds = getAsArray(importRec.get(alias + "." + TAL4XA_BILD_TAFEL_IDENT), false);
            for (String imageId : imagesIds) {
                String convertedImageId = StrUtils.replaceSubstring(imageId, PICTURE_DATE_DUMMY, "").trim();
                if (!convertedImageId.isEmpty()) {
                    PictureReference pictureReference = getPictureReference(imageId, convertedImageId);
                    if (pictureReference.isValid()) {
                        if (!pictureReference.isImageExists()) {
                            // Falls das Bild zur DASTI Referenz noch nicht existiert -> Erstelle Verknüpfung zum noch nicht existierenden Bild (via Bildnummer)
                            EtkDataImage image = currentAssembly.addImage(pictureReference.getPictureNumber(), DBActionOrigin.FROM_EDIT);
                            Set<iPartsPicReferenceId> picReferenceIds = picReferenceIdsForAssembly.get(currentAssembly.getAsId());
                            if (picReferenceIds == null) {
                                picReferenceIds = new LinkedHashSet<>();
                                picReferenceIdsForAssembly.put(currentAssembly.getAsId(), picReferenceIds);
                            }

                            picReferenceIds.add(new iPartsPicReferenceId(pictureReference.getPictureNumber(),
                                                                         pictureReference.getPictureDate()));
                            if (!StrUtils.isEmpty(pictureReference.getPictureDate())) {
                                image.setFieldValue(FIELD_I_IMAGEDATE, pictureReference.getPictureDate(),
                                                    DBActionOrigin.FROM_EDIT);
                            }
                        } else {
                            // Falls das Bild zur DASTI Referenz schon existiert -> Erstelle Verknüpfung zum
                            // existierenden Bild (via VarItemId und VarItemRevId)
                            currentAssembly.addImage(pictureReference.getVarItemId(), pictureReference.getVarItemRevId(),
                                                     true, DBActionOrigin.FROM_EDIT);
                        }
                    }
                    if (pictureReference.hasErrorsOrWarnings()) {
                        getMessageLog().fireMessage(translateForLog("!!Record %1: \"%2\". Bildreferenz \"%3\"",
                                                                    String.valueOf(currentRecordNo),
                                                                    translateForLog(pictureReference.getErrorOrWarningText()),
                                                                    imageId), MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
                    }
                    if (!convertedImageId.equals(imageId.trim())) {
                        getMessageLog().fireMessage((translateForLog("!!Record %1 enthält eine ungültige Bildnummer. " +
                                                                     "Automatische Korrektur von %2 zu %3.",
                                                                     String.valueOf(currentRecordNo), imageId,
                                                                     pictureReference.getPictureNumber() + pictureReference.getPictureDate())),
                                                    MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP,
                                                    MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
                    }
                } else {
                    getMessageLog().fireMessage(translateForLog("!!Record %1 mit ungültiger Bildnummer \"%2\" übersprungen",
                                                                String.valueOf(currentRecordNo), imageId),
                                                MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
                }
            }
        }
    }

    /**
     * Liefert abhängig von der Bildnummer und der bearbeiteten Bildnummer das passende {@link PictureReference} Objekt
     * zurück.
     *
     * @param imageId
     * @param convertedImageId
     * @return
     */
    public PictureReference getPictureReference(String imageId, String convertedImageId) {
        if (StrUtils.stringContains(imageId, PICTURE_DATE_DUMMY)) {
            String dateForID = getDateForDummyId(imageId);
            return new PictureReference(getProject(), convertedImageId, dateForID);
        } else {
            return new PictureReference(getProject(), convertedImageId);
        }
    }

    /**
     * Liefert abhängig von der DASTi Bildnummer das dazugehörige Datum.
     * <p>
     * Wenn die DASTi Bildnummer einen "DUMMY" Platzhalter hat, soll mit dem Datum der Importdatei gesucht werden. Wenn
     * dieses ebenfalls fehlen sollte, dann wird mit dem aktuellen Datum gesucht.
     *
     * @param imageId
     * @return
     */
    private String getDateForDummyId(String imageId) {
        if (StrUtils.stringContains(imageId, PICTURE_DATE_DUMMY)) {
            Date dateForId = getDatasetDate();
            if (dateForId != null) {
                return DateUtils.toyyyyMMdd_Date(dateForId);
            } else {
                return DateUtils.toyyyyMMdd_currentDate();
            }
        }
        return "";
    }

    /**
     * Baumuster Zusatztexte zusammenführen
     *
     * @param importRec
     * @param alias
     */
    private void importSatzart2X(Map<String, String> importRec, String alias) {
        Language[] languages = { Language.DE, Language.EN, Language.FR, Language.ES, Language.PT, Language.IT };
        if (alias.equals(SATZART_2X)) {
            String product = importRec.get(alias + "." + TAL40A_KATALOG);
            String posNr = importRec.get(alias + "." + TAL40A_POS_NR);
            String lineNumber = importRec.get(alias + "." + TAL40A_ZEILE);
            // Check, ob Werte valide sind
            if (currentAddTextForProductModel.checkValues(posNr, lineNumber)) {
                if (currentAddTextForProductModel.isValidProduct(product)) {
                    List<String> languageTexts = getAsArray(importRec.get(alias + "." + TAL40A_TEXT), true);
                    // Falls keine Texte vorhanden sind und die Liste leer ist -> Auffüllen mit leeren Strings
                    StrUtils.rightFillStringListWithString(languageTexts, "", 6);
                    for (int i = 0; i < languages.length; i++) {
                        String text = StrUtils.pad(languageTexts.get(i), 80);
                        EtkMultiSprache multiLang = currentAddTextForProductModel.getCurrentText();
                        // Das aktuelle MultiLang Objekt füllen bzw. erweitern
                        if (!multiLang.spracheExists(languages[i])) {
                            multiLang.setText(languages[i], text);
                        } else {
                            String currentText = multiLang.getText(languages[i].getCode());
                            multiLang.setText(languages[i], currentText + text);
                        }
                    }
                } else {
                    getMessageLog().fireMessage(translateForLog("!!Record %1 fehlerhaft. Zusatztext Baumuster: Produkt stimmt nicht überein. Aktuell: \"%2\" Neu: \"%3\"",
                                                                String.valueOf(currentRecordNo),
                                                                currentAddTextForProductModel.getCurrentModelProduct().getAsId().getProductNumber(), product),
                                                MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
                }
            } else {
                getMessageLog().fireMessage(translateForLog("!!Record %1 fehlerhaft. Zusatztext Baumuster: Position und Zeile ungültig. Position: \"%2\" Neu: \"%3\"Zeile: \"%4\" Neu: \"%5\"",
                                                            String.valueOf(currentRecordNo),
                                                            currentAddTextForProductModel.getPosNumber(), posNr, currentAddTextForProductModel.getLineNumber(), lineNumber),
                                            MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
            }
        }
    }


    /**
     * Die im Katalog enthaltenen Baumuster importieren
     *
     * @param importRec
     * @param alias
     */
    private void importSatzart2FAnd2A(Map<String, String> importRec, String alias) {
        // Sind wir in einem gültigen Katalog
        if (currentProduct != null) {

            String model = importRec.get(alias + "." + TAL40A_BAUMUSTER);

            model = iPartsNumberHelper.getPlainModelNumber(model);

            if (alias.equals(SATZART_2A)) {
                model = iPartsConst.MODEL_NUMBER_PREFIX_AGGREGATE + model;
            } else if (alias.equals(SATZART_2F)) {
                model = iPartsConst.MODEL_NUMBER_PREFIX_CAR + model;
            } else {
                Logger.log(iPartsImportPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, "Invalid Satzart alias for \"importSatzart2FAnd2A\" method. "
                                                                                + "Valid values: " + SATZART_2F + " and " + SATZART_2A + ". Found: " + alias);
                reduceRecordCount();
                return;
            }

            // Model anlegen, falls es noch nicht existiert
            iPartsModelId modelId = new iPartsModelId(model);

            iPartsDataModel dataModel = new iPartsDataModel(getProject(), modelId);

            if (!dataModel.existsInDB()) {
                dataModel.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
                EtkMultiSprache description = new EtkMultiSprache();
                description.setText(Language.DE, importRec.get(alias + "." + TAL40A_VERKAUFSBEZEICHNUNG));

                dataModel.setFieldValueAsMultiLanguage(FIELD_DM_SALES_TITLE, description, DBActionOrigin.FROM_EDIT);
                dataModel.setFieldValue(FIELD_DM_SOURCE, iPartsImportDataOrigin.ELDAS.getOrigin(), DBActionOrigin.FROM_EDIT); // Datenquelle setzen (ELDAS)
                dataModel.setFieldValueAsBoolean(FIELD_DM_MODEL_VISIBLE, true, DBActionOrigin.FROM_EDIT);
                dataModel.saveToDB(false, DBDataObject.PrimaryKeyExistsInDB.FALSE);
            } else if (!dataModel.getFieldValueAsBoolean(FIELD_DM_MODEL_VISIBLE)) {
                dataModel.setFieldValueAsBoolean(FIELD_DM_MODEL_VISIBLE, true, DBActionOrigin.FROM_EDIT);
                dataModel.saveToDB(false, DBDataObject.PrimaryKeyExistsInDB.TRUE);
            }
            modelsForProductModelCheck.add(modelId);

            handleAdditionalTextForModel();
            // Dieses Baumuster dem Produkt zuordnen
            iPartsDataProductModels productModel = new iPartsDataProductModels(getProject(), new iPartsProductModelsId(getProductNoFromCurrentProduct(), model));
            currentAddTextForProductModel = new AddTextForProductModel(productModel);

            DBDataObjectList<iPartsDataProductModels> productModelsList = currentProduct.getProductModelsList();
            iPartsProductModelsId productModelsId = productModel.getAsId();
            if (!productModelsList.containsId(productModelsId)) {
                productModel.initAttributesWithDefaultValues(DBActionOrigin.FROM_EDIT);
                // Nicht sichtbare Produkt-Model Beziehungen, die vor dem Import bereits existiert haben, müssen hier wieder gesetzt werden.
                if (nonVisibleProductModels.contains(productModelsId.getModelNumber())) {
                    productModel.setFieldValueAsBoolean(FIELD_DPM_MODEL_VISIBLE, false, DBActionOrigin.FROM_EDIT);
                }
                productModelsList.add(productModel, DBActionOrigin.FROM_EDIT);
            }
        }
    }

    /**
     * Konvertiert den aktuellen Zusatztext zu einem Produkt-Baumuster und fügt ihn dem Baumuster hinzu
     *
     * @return
     */
    private void handleAdditionalTextForModel() {
        if ((currentAddTextForProductModel == null) || !currentAddTextForProductModel.isValid()) {
            return;
        }
        EtkMultiSprache multiLang = currentAddTextForProductModel.getCurrentText();
        for (Map.Entry<String, String> entry : multiLang.getLanguagesAndTextsModifiable().entrySet()) {
            entry.setValue(DictMultilineText.getInstance().convertDictTextLong(DictTextKindTypes.ELDAS_MODEL_ADDTEXT, entry.getValue()));
        }
        boolean successful = importTextIDHelper.handleDictTextId(DictTextKindTypes.ELDAS_MODEL_ADDTEXT, multiLang, "", DictHelper.getMADForeignSource(),
                                                                 true, TableAndFieldName.make(TABLE_DA_PRODUCT_MODELS, FIELD_DPM_TEXTNR));
        if (!iPartsMigrationHelper.checkImportTextID(this, importTextIDHelper, successful, currentRecordNo)) {
            return;
        }
        currentAddTextForProductModel.getCurrentModelProduct().setFieldValueAsMultiLanguage(FIELD_DPM_TEXTNR, multiLang, DBActionOrigin.FROM_EDIT);

        currentAddTextForProductModel = null;
    }


    /**
     * Enthaltene Strichausführungen des SAA-Kataloges importieren
     *
     * @param importRec
     */
    private void importSatzartC(Map<String, String> importRec) {
        resetLastCompletePartListEntry();

        String alias = SATZART_C;

        String saRumpf = importRec.get(alias + "." + TAL46A_SA_RUMPF);
        String saStrich = importRec.get(alias + "." + TAL46A_SA_STRICH); // die 2 Ziffern f.d. Strichausf.
        iPartsNumberHelper numberHelper = new iPartsNumberHelper();
        try {
            saStrich = numberHelper.unformatSaaForDB(saRumpf + saStrich);
        } catch (RuntimeException e) {
            getMessageLog().fireMessage(e.getMessage(), MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
            reduceRecordCount();
            return;
        }

        if (!currentSAAsInProduct.contains(saStrich)) {
            currentSAAsInProduct.add(saStrich);
        }

        String saType = importRec.get(alias + "." + TAL46A_SA_TYP);
        // Nur importieren, wenn der Typ "000" ist
        if (saType.equals("000")) {
            // Verbindungs-SAs setzen
            String connectedSas = "";
            List<String> sas = getAsArray(importRec.get(alias + "." + TAL46A_VERBINDUNGS_SA), false);
            if (!sas.isEmpty()) {
                connectedSas = StrUtils.stringListToString(sas, EDS_CONNECTED_SAS_DELIMITER);
            }
            iPartsSaaId saaId = new iPartsSaaId(saStrich);
            iPartsDataSaa dataSaa = new iPartsDataSaa(getProject(), saaId);
            if (!dataSaa.loadFromDB(saaId)) {
                dataSaa.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
                dataSaa.setFieldValue(FIELD_DS_SOURCE, iPartsImportDataOrigin.ELDAS.getOrigin(), DBActionOrigin.FROM_EDIT);
            }
            dataSaa.setFieldValue(FIELD_DS_CONNECTED_SAS, connectedSas, DBActionOrigin.FROM_EDIT);
            saveToDB(dataSaa);
            // Fussnoten mit SAA verknüpfen
            String formattedSaRumpf = numberHelper.unformatSaForDB(saRumpf);
            List<String> footnoteIds = getAsArray(importRec.get(alias + "." + TAL46A_FN_HINWEISE), false);
            for (int i = 0; i < footnoteIds.size(); i++) {
                iPartsFootNoteId eldasFootNoteId = footnotesHandler.getFootNoteIdForSA(footnoteIds.get(i), formattedSaRumpf);
                iPartsFootNoteSaaRefId saaRefId = new iPartsFootNoteSaaRefId(saStrich, eldasFootNoteId.getFootNoteId());
                iPartsDataFootNoteSaaRef footNoteSaaRef = new iPartsDataFootNoteSaaRef(getProject(), saaRefId);

                // Die SAA-Fußnoten-Referenzen sind immer neu, weil sie beim Löschen der SA vorher zunächst alle gelöscht werden
                footNoteSaaRef.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);

                if (footnoteIds.size() > 1) {
                    footNoteSaaRef.setFieldValue(FIELD_DFNS_FN_SEQNO, EtkDbsHelper.formatLfdNr(i + 1), DBActionOrigin.FROM_EDIT);
                }
                currentFootNoteSaaRefList.add(footNoteSaaRef);
            }
        }
    }

    private iPartsFootNoteId getFootNoteIdForSA(String footnoteNumber) {
        return footnotesHandler.getFootNoteIdForSA(footnoteNumber, currentSANumber);
    }

    /**
     * Kopfdaten der SA
     *
     * @param importRec
     */
    private void importSatzart6(Map<String, String> importRec) {
        //erstmal das letzte SA-Modul abspeichern
        finishCurrentAssembly();

        // Evtl. vorhandenen Buffer jetzt speichern, damit dies im Commit berücksichtigt wird
        saveBufferListToDB(true);

        // Nach jeder SA wird die Transaktion commited, weil sonst der Transaktionbuffer zu groß wird
        getProject().getDbLayer().commit();
        getProject().getDbLayer().startTransaction();

        String alias = SATZART_6;

        // Hier beginnt eine neue SA
        // Deshalb hier die Liste der SAAs neu erstellen

        currentSAAsInProduct = new ArrayList<>();
        resetLastCompletePartListEntry();

        String saRumpf;
        try {
            iPartsNumberHelper numberHelper = new iPartsNumberHelper();
            saRumpf = numberHelper.unformatSaForDB(importRec.get(alias + "." + TAL46A_SA_RUMPF));
        } catch (RuntimeException e) {
            getMessageLog().fireMessage(e.getMessage(), MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
            reduceRecordCount();
            return;
        }

        String message = translateForLog("!!Bilde %1.", saRumpf);
        getMessageLog().fireMessage(message, MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);


        /**
         * Codes an SA speichern
         * Die Codes sollen ver-ODER-t gespeichert werden.
         * Beispiele in TAL46A150905
         */

        String codeStr = "";
        List<String> codes = getAsArray(importRec.get(alias + "." + TAL46A_CODE_1), false);
        codes.addAll(getAsArray(importRec.get(alias + "." + TAL46A_CODE_2), false));
        if (!codes.isEmpty()) {
            codeStr = StrUtils.stringListToString(codes, "/");
        }

        // Code für die SA muss immer abgespeichert werden (auch wenn er leer ist -> dann wird ein vorhandener Code gelöscht)
        iPartsSaId saaId = new iPartsSaId(saRumpf);
        iPartsDataSa dataSa = new iPartsDataSa(getProject(), saaId);
        if (!dataSa.loadFromDB(saaId)) {
            dataSa.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
            dataSa.setFieldValue(FIELD_DS_SOURCE, iPartsImportDataOrigin.ELDAS.getOrigin(), DBActionOrigin.FROM_EDIT);
        }
        dataSa.setFieldValue(FIELD_DS_CODES, codeStr, DBActionOrigin.FROM_EDIT);
        saveToDB(dataSa);
    }

    /**
     * Nur Fußnoten Nummern 999 und künftig noch 998.
     * Die anderen Nummern werden von importFootNote() behandelt.
     *
     * @param importRec
     */
    private boolean importSatzartA9(Map<String, String> importRec) {
        String alias = SATZART_A9;

        String footNoteNumber = importRec.get(alias + "." + TAL4XA_NR);
        if ((talType == Tal4XAType.TAL40A) && (currentProduct != null) && footNoteNumber.equals("999")) { // SA-Verwendungsnachweis für Produkt
            String kg = importRec.get(alias + "." + TAL40A_KG);
            List<String> saReferences = getAsArray(importRec.get(alias + "." + TAL40A_SA_VERWENDUNG), false);
            if (!saReferences.isEmpty()) {
                for (String saNumber : saReferences) {
                    try {
                        iPartsNumberHelper numberHelper = new iPartsNumberHelper();
                        saNumber = numberHelper.unformatSaForDB(parseSaNumberValidity(saNumber));
                    } catch (RuntimeException e) {
                        getMessageLog().fireMessage(e.getMessage(), MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
                        continue;
                    }

                    // SA-Verwendungsnachweis für Produkt über iPartsDataProductSAs speichern
                    iPartsProductSAsId productSAsId = new iPartsProductSAsId(getProductNoFromCurrentProduct(),
                                                                             saNumber, kg);

                    // Aus Performancegründen verwenden wir hier nicht currentProduct.getProductSAsList().containsId()
                    // sondern ein separates HashSet currentSAsForKGInProduct, um die Eindeutigkeit sicherzustellen
                    if (!currentSAsForKGInProduct.contains(productSAsId)) {
                        currentSAsForKGInProduct.add(productSAsId);
                        iPartsDataProductSAs dataProductSAs = new iPartsDataProductSAs(getProject(), productSAsId);
                        dataProductSAs.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
                        dataProductSAs.setFieldValue(iPartsConst.FIELD_DPS_SOURCE, iPartsImportDataOrigin.ELDAS.getOrigin(), DBActionOrigin.FROM_EDIT);
                        currentProduct.getProductSAsList().add(dataProductSAs, DBActionOrigin.FROM_EDIT);
                    }
                }
            }
            return true;
        }

        return false;
    }


    private String getEldasSourceGUID(String kVari, String kLfdnr) {
        return EditConstructionToRetailHelper.createNonDIALOGSourceGUID(new PartListEntryId(kVari, "", kLfdnr));
    }


    private Language getLangFromFootNoteInteger(String value) {
        value = value.trim();
        if (value.equals("0")) {
            return Language.DE;
        }
        if (value.equals("1")) {
            return Language.EN;
        }
        if (value.equals("2")) {
            return Language.FR;
        }
        if (value.equals("3")) {
            return Language.ES;
        }
        if (value.equals("4")) {
            return Language.PT;
        }
        if (value.equals("5")) {
            return Language.IT;
        }
        return Language.DE;
    }


    /**
     * Einzelne SA-Gültigkeit parsen
     *
     * @param saNumber
     * @return
     */
    static String parseSaNumberValidity(String saNumber) {
        // , bzw. . hinten entfernen, SA-Nummer danach trimmen und ggf. auf 6 Stellen mit Leerzeichen auffülen wg. 5-stelliger SAs
        char lastChar = saNumber.charAt(saNumber.length() - 1);
        if ((lastChar == ',') || (lastChar == '.')) { // letztes Zeichen ',' oder '.'?
            saNumber = saNumber.substring(0, saNumber.length() - 1); // letztes Zeichen abschneiden
        }
        return StrUtils.prefixStringWithCharsUpToLength(saNumber.trim(), ' ', 6);
    }

    private List<String> getAsArray(String value, boolean returnBlanks) {
        return getAsArray(value, returnBlanks, !returnBlanks);
    }

    private List<String> getAsArray(String value, boolean returnBlanks, boolean trim) {
        if (value == null) {
            return new DwList<>();
        } else {
            return StrUtils.toStringList(value, "\n", returnBlanks, trim);
        }
    }


    @Override
    public void postImportTask() {
        if (!isCancelled()) {
            // Der bis dahin gemerkte Katalog bzw. Modul muss noch gespeichert werden
            if (talType == Tal4XAType.TAL40A) {
                if (currentProduct != null) {
                    saveCurrentProduct(); // Ruft implizit auch ein commit() und startTransaction() am Ende auf
                }
            } else {
                finishCurrentAssembly();

                try {
                    // Bisherige Transaktion explizit committen (und danach eine neue Transaktion starten, damit der generische
                    // Import-Wrapper am Ende keine Probleme wegen fehlender Transaktion hat), damit alle Daten vom Modul in
                    // der DB abgespeichert sind bevor das Synchronisieren der Bildreferenzen gestartet wird
                    getProject().getDbLayer().commit();
                    getProject().getDbLayer().startTransaction();
                } catch (Exception e) {
                    Logger.logExceptionWithoutThrowing(iPartsImportPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, e);
                    cancelImport(e.getMessage());
                }
            }
            if (!isCancelled() && !picReferenceIdsForAssembly.isEmpty()) {
                getMessageLog().fireMessage(MQPicScheduler.getInstance().createStartRetrievingImagesLogMessage(picReferenceIdsForAssembly.size(), this),
                                            MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                Set<iPartsPicReferenceId> allPicReferences = new LinkedHashSet<>();
                for (Set<iPartsPicReferenceId> picReferencesPerAssembly : picReferenceIdsForAssembly.values()) {
                    allPicReferences.addAll(picReferencesPerAssembly);
                }
                MQPicScheduler.getInstance().startRetrievingImages(allPicReferences, getProject(), getMessageLog());
            }
        }
        ac2pcMappingList.clear();
        moduleVariantsVisibleMap.clear();

        super.postImportTask();

        // Alle aufgesammelten Baumuster muss mit der DA_PRODUCT_MODELS Tabelle synchronisiert werden
        if (!modelsForProductModelCheck.isEmpty()) {
            setBufferedSave(true);
            int oldSkippedRecords = skippedRecords;
            for (iPartsModelId modelId : modelsForProductModelCheck) {
                // Zu einem Baumuster alle PRODUCT_MODEL Datensätze holen
                iPartsDataProductModelsList productModelsList = iPartsDataProductModelsList.loadDataProductModelsList(getProject(), modelId);
                iPartsDataModel dataModel = new iPartsDataModel(getProject(), modelId);
                if (!dataModel.existsInDB()) {
                    dataModel.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
                }
                iPartsProductModelHelper.syncProductModelsWithModel(dataModel, productModelsList);
                saveToDB(dataModel);
                for (iPartsDataProductModels productModel : productModelsList) {
                    saveToDB(productModel);
                }
            }
            modelsForProductModelCheck.clear();
            skippedRecords = oldSkippedRecords;
            responseDataTextInTableFootNoteToPseudoFNContentId = null;
            nonVisibleProductModels = null;
            super.postImportTask();
        }
    }

    @Override
    protected void clearCaches() {
        super.clearCaches();
        // ADD_TEXT, NEUTRAL_TEXT und FOOTNOTE sind auf jeden Fall im WarmUp enthalten -> ein WarmUp am Ende
        ApplicationEvents.fireEventInAllProjectsAndAllClusters(new iPartsClearTextCacheEvent(DictTextKindTypes.ADD_TEXT, false));
        ApplicationEvents.fireEventInAllProjectsAndAllClusters(new iPartsClearTextCacheEvent(DictTextKindTypes.FOOTNOTE, false));
        ApplicationEvents.fireEventInAllProjectsAndAllClusters(new iPartsClearTextCacheEvent(DictTextKindTypes.NEUTRAL_TEXT, false));
        ApplicationEvents.fireEventInAllProjectsAndAllClusters(new iPartsClearTextCacheEvent(DictTextKindTypes.ELDAS_MODEL_ADDTEXT, true));
    }

    @Override
    public boolean finishImport() {
        importTextIDHelper.clearCache();
        return super.finishImport(false); // Keine Caches löschen bei diesem Importer
    }

    private void saveCurrentProduct() {
        if ((currentAddTextForProductModel != null) && currentAddTextForProductModel.isValid()) {
            handleAdditionalTextForModel();
        }
        finishCurrentAssembly();
        iPartsMigrationHelper.saveCurrentAssembliesForKgInProduct(this, currentAssembliesForKgInProduct, currentKgInProduct); // alle Module für die aktuelle KG speichern
        footnotesHandler.saveCurrentFootNotesForPartListEntries(allCurrentColorTablefootnotes, true); // alle Fußnoten für die aktuelle KG speichern
        saveBufferListToDB(true); // evtl. vorhandenen Buffer jetzt speichern, damit dies im Commit weiter unten berücksichtigt wird
        currentProduct.saveToDB(false, DBDataObject.PrimaryKeyExistsInDB.FALSE);
        String productNumber = getProductNoFromCurrentProduct();
        currentProduct = null;
        currentAssembliesForKgInProduct = null;
        currentKgInProduct = null;
        currentModelsOrTypesForQuantityInProduct = null;
        currentModelsForTypeInGlobalModelProduct = null;
        currentSAsForKGInProduct = null;
        resetLastCompletePartListEntry();

        // Nach jedem Product wird die Transaktion Commited weil sonst der Transaktionbuffer zu groß wird
        try {
            getProject().getDbLayer().commit();
            getProject().getDbLayer().startTransaction();
        } catch (Exception e) {
            Logger.logExceptionWithoutThrowing(iPartsImportPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, e);
            cancelImport(e.getMessage());
            return;
        }

        String message = translateForLog("!!Katalog %1 wurde importiert", productNumber);

        getMessageLog().fireMessage(message, MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);

    }


    private void finishCurrentAssembly() {
        currentFootNoteNumber = null;
        currentFootNoteIsValid = true;

        if (currentFootNoteIsTable) {
            getMessageLog().fireMessage(translateForLog("!!Tabellenfußnote \"%1\" hat kein gültiges Ende in Record %2",
                                                        currentTableFootNoteId.getFootNoteId(), Long.toString(currentRecordNo)),
                                        MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
        }

        currentFootNoteIsTable = false;
        currentFootNoteIsColorTable = false;
        currentTableFootNoteId = null;
        if (currentAssembly != null) {
            iPartsMigrationHelper.handleModuleMetaData(currentAssembly, moduleVariantsVisibleMap);
            // Maps mit Wahlweise-Teileinformationen auswerten
            iPartsMigrationHelper.handleWWSetsForAssembly(this, currentAssembly,
                                                          currentWWPartNumbersToWWPartNumbersInAssembly,
                                                          currentPartNumbersInAssembly, currentWWPartNumbersToPLEntriesInAssembly);

            // Nachfolger und Mitlieferteile für Ersetzungen suchen und in DB abspeichern
            iPartsMigrationHelper.handleReplacementAndIncludePartsForAssembly(getProject(), this, currentAssembly, replacementsForAssembly);
            if (talType == Tal4XAType.TAL40A) {
                iPartsMigrationHelper.storeFinishedAssemblyForModel(this, currentAssembly, currentAssembliesForKgInProduct, picReferenceIdsForAssembly);
            } else {
                iPartsMigrationHelper.storeFinishedAssemblyForSa(this, currentAssembly, picReferenceIdsForAssembly,
                                                                 footnotesHandler, allCurrentColorTablefootnotes, true);
            }
            currentAssembly = null;
        }

        currentPartNumbersInAssembly.clear();
        currentWWPartNumbersToWWPartNumbersInAssembly.clear();
        currentWWPartNumbersToPLEntriesInAssembly.clear();
    }


    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        return false;
    }

    @Override
    protected boolean importFile(FilesImporterFileListType importFileType, DWFile importFile) {
        setDatasetDate(iPartsMADDateTimeHandler.extractTalXDateFromFilename(importFile.getPath(), this));
        return importMasterData(prepareImporterFixedLength(importFile, talType));
    }

    /**
     * Vorbereitung des FixedLengthRecordReaders für die Tal40 Datei. Die Funktion ist statisch, damit sie auch vom MadDevelTal40AToXlsConverter
     * verwendet werden kann
     *
     * @param xmlImportFile
     * @param talType
     * @return
     */
    public static AbstractKeyValueRecordReader prepareImporterFixedLength(DWFile xmlImportFile, Tal4XAType talType) {

        List<FixedLenRecordType> recordTypes = new ArrayList<>();

        // Ab hier nur die speziellen Datensatzarten in der TAL40
        if (talType == Tal4XAType.TAL40A) {
            recordTypes.add(new FixedLenRecordType(SATZART_7,
                                                   new FixedLenRecordTypeIdentifier[]{
                                                           new FixedLenRecordTypeIdentifier(9, 9, "7")
                                                   },
                                                   new FixedLenFieldDescription[]{
                                                           new FixedLenFieldDescription(1, 3, TAL40A_KATALOG),
                                                           new FixedLenFieldDescription(4, 8, TAL40A_KATALOG_TYP),
                                                           new FixedLenFieldDescription(17, 19, TAL40A_TYP),
                                                           new FixedLenFieldDescriptionArray(20, 151, 6, TAL40A_BAUMUSTER),
                                                           new FixedLenFieldDescriptionArray(152, 166, 1, TAL4XA_SORTIMENTSKLASSEN),
                                                           new FixedLenFieldDescription(167, 167, TAL4XA_BEREICHSLAENDERCODE),
                                                           new FixedLenFieldDescription(168, 169, TAL40A_BAUMUSTERART)
                                                   }

            ));

            recordTypes.add(new FixedLenRecordType(SATZART_2X,
                                                   new FixedLenRecordTypeIdentifier[]{
                                                           new FixedLenRecordTypeIdentifier(9, 9, "2"),
                                                           new FixedLenRecordTypeIdentifier(14, 14, "X")
                                                   },
                                                   new FixedLenFieldDescription[]{
                                                           new FixedLenFieldDescription(1, 3, TAL40A_KATALOG),
                                                           new FixedLenFieldDescription(10, 11, TAL40A_POS_NR),
                                                           new FixedLenFieldDescription(12, 13, TAL40A_ZEILE),
                                                           new FixedLenFieldDescription(15, 15, TAL40A_FOLGE),
                                                           // Ab hier X-Datensatz
                                                           new FixedLenFieldDescriptionArray(17, 496, 80, TAL40A_TEXT)
                                                   }
            ));

            recordTypes.add(new FixedLenRecordType(SATZART_2A,
                                                   new FixedLenRecordTypeIdentifier[]{
                                                           new FixedLenRecordTypeIdentifier(9, 9, "2"),
                                                           new FixedLenRecordTypeIdentifier(14, 14, "A")
                                                   },
                                                   new FixedLenFieldDescription[]{
                                                           new FixedLenFieldDescription(1, 3, TAL40A_KATALOG),
                                                           new FixedLenFieldDescription(10, 11, TAL40A_POS_NR),
                                                           new FixedLenFieldDescription(14, 14, TAL40A_ZEILE),
                                                           new FixedLenFieldDescription(15, 15, TAL40A_FOLGE),
                                                           // Ab hier A-Datensatz
                                                           new FixedLenFieldDescription(17, 27, TAL40A_BAUMUSTER),
                                                           new FixedLenFieldDescription(28, 75, TAL40A_VERKAUFSBEZEICHNUNG),
                                                           new FixedLenFieldDescriptionArray(76, 395, 16, TAL40A_FGST_NR),
                                                           new FixedLenFieldDescriptionArray(396, 795, 20, TAL40A_ET_KATALOGE)
                                                   }
            ));

            recordTypes.add(new FixedLenRecordType(SATZART_2F,
                                                   new FixedLenRecordTypeIdentifier[]{
                                                           new FixedLenRecordTypeIdentifier(9, 9, "2"),
                                                           new FixedLenRecordTypeIdentifier(14, 14, "F")
                                                   },
                                                   new FixedLenFieldDescription[]{
                                                           new FixedLenFieldDescription(1, 3, TAL40A_KATALOG),
                                                           new FixedLenFieldDescription(10, 11, TAL40A_POS_NR),
                                                           new FixedLenFieldDescription(14, 14, TAL40A_ZEILE),
                                                           new FixedLenFieldDescription(15, 15, TAL40A_FOLGE),
                                                           // Ab hier F-Datensatz
                                                           new FixedLenFieldDescription(17, 27, TAL40A_BAUMUSTER),
                                                           new FixedLenFieldDescription(28, 75, TAL40A_VERKAUFSBEZEICHNUNG),
                                                           new FixedLenFieldDescriptionArray(76, 153, 13, TAL40A_MOTOR),
                                                           new FixedLenFieldDescriptionArray(154, 223, 7, TAL40A_GETRIEBE_MECHANISCH),
                                                           new FixedLenFieldDescriptionArray(224, 293, 7, TAL40A_GETRIEBE_AUTOMATIK),
                                                           new FixedLenFieldDescriptionArray(294, 363, 7, TAL40A_VERTEILER_GETRIEBE),
                                                           new FixedLenFieldDescriptionArray(364, 433, 7, TAL40A_VORDERACHSE),
                                                           new FixedLenFieldDescriptionArray(434, 503, 7, TAL40A_HINTERACHSE),
                                                           new FixedLenFieldDescriptionArray(504, 573, 7, TAL40A_LENKUNG),
                                                           new FixedLenFieldDescriptionArray(574, 643, 7, TAL40A_PRITSCHE),
                                                           new FixedLenFieldDescriptionArray(644, 803, 16, TAL40A_AUFBAU),
                                                           new FixedLenFieldDescriptionArray(804, 873, 7, TAL40A_BRENNSTOFFZELLE),
                                                           new FixedLenFieldDescriptionArray(874, 943, 7, TAL40A_HOCHVOLTBATTERIE),
                                                           new FixedLenFieldDescriptionArray(944, 1013, 7, TAL40A_ELEKTROMOTOR),
                                                           new FixedLenFieldDescriptionArray(1014, 1083, 7, TAL40A_ABGASNACHBEHANDLUNG)
                                                   }
            ));


            recordTypes.add(new FixedLenRecordType(SATZART_8,
                                                   new FixedLenRecordTypeIdentifier[]{
                                                           new FixedLenRecordTypeIdentifier(9, 9, "8")
                                                   },
                                                   new FixedLenFieldDescription[]{
                                                           new FixedLenFieldDescription(1, 3, TAL40A_KATALOG),
                                                           new FixedLenFieldDescription(7, 8, TAL40A_KG),
                                                           new FixedLenFieldDescriptionArray(20, 259, 40, TAL4XA_BENENNUNG)
                                                   }

            ));


        }


        // Jetzt die speziellen Datensatzarten der TAL46
        if (talType == Tal4XAType.TAL46A) {

            recordTypes.add(new FixedLenRecordType(SATZART_6,
                                                   new FixedLenRecordTypeIdentifier[]{
                                                           new FixedLenRecordTypeIdentifier(9, 9, "6")
                                                   },
                                                   new FixedLenFieldDescription[]{
                                                           new FixedLenFieldDescription(1, 6, TAL46A_SA_RUMPF),
                                                           new FixedLenFieldDescriptionArray(20, 859, 140, TAL4XA_BENENNUNG),
                                                           new FixedLenFieldDescriptionArray(860, 958, 3, TAL46A_CODE_1),
                                                           new FixedLenFieldDescriptionArray(959, 1057, 3, TAL46A_CODE_2),
                                                           new FixedLenFieldDescription(1058, 1072, TAL4XA_SORTIMENTSKLASSEN),
                                                           new FixedLenFieldDescription(1073, 1073, TAL4XA_BEREICHSLAENDERCODE),
                                                           }

            ));


            recordTypes.add(new FixedLenRecordType(SATZART_C,
                                                   new FixedLenRecordTypeIdentifier[]{
                                                           new FixedLenRecordTypeIdentifier(9, 9, "C")
                                                   },
                                                   new FixedLenFieldDescription[]{
                                                           new FixedLenFieldDescription(1, 6, TAL46A_SA_RUMPF),
                                                           new FixedLenFieldDescription(7, 8, TAL46A_SA_STRICH),
                                                           new FixedLenFieldDescription(10, 12, TAL46A_SA_TYP),
                                                           new FixedLenFieldDescriptionArray(17, 736, 120, TAL4XA_BENENNUNG),
                                                           new FixedLenFieldDescriptionArray(737, 751, 3, TAL46A_FN_HINWEISE),
                                                           new FixedLenFieldDescriptionArray(752, 871, 6, TAL46A_VERBINDUNGS_SA),
                                                           new FixedLenFieldDescriptionArray(17, 376, 3, TAL46A_UNTERBAUMUSTER)
                                                   }

            ));


        }


        // Und nun die, die in beiden Importfiles praktisch identisch sind
        if ((talType == Tal4XAType.TAL46A) || (talType == Tal4XAType.TAL40A)) {

            //Satzart 9BS und DBS -> Headerinforamtion der TU
            recordTypes.add(getTuHeaderDescription(talType));


            //Satzart 9 und D ohne BS -> Teileinformation der TU
            recordTypes.add(getTUPartsDescription(talType));


            //Satzart AX und EX kurze Fußnoten
            recordTypes.add(getFootNotesShort(talType));

            //Satzart AY und EY lange Fußnoten
            recordTypes.add(getFootNotesLong(talType));

            //Satzart A9 und E9 Nummern Fußnoten
            recordTypes.add(getFootNotesNumber(talType));

            //Satzart AE und EE Einsatzdaten Fußnoten
            recordTypes.add(getFootNotesEinsatz(talType));

            //Satzart AF und EF Farben Fußnoten
            recordTypes.add(getFootNotesColor(talType));

            //Satzart AB und EB Bedienungsanleitungen Fußnoten
            recordTypes.add(getFootNotesManuals(talType));

            //Satzart AL und EL Lochbilder Fußnoten
            recordTypes.add(getFootNotesLochBilder(talType));
        }

        // Daimler definiert seine Recordlänge als echte Recordlänge, also Nutzdaten + Zeilenende; da Daimler immer Unix-Zeileende hat
        // gilt also: Nutzdatenlänge = Daimler-Record-Länge - 1
        //return new KeyValueRecordFixedLengthGZFileReader(xmlImportFile, "", ArrayUtil.toArray(recordTypes), TAL4XA_RECORD_LEN - 1, DWFileCoding.CP_1252);
        return new KeyValueRecordFixedLengthFixGZFileReader(xmlImportFile, "", ArrayUtil.toArray(recordTypes), TAL4XA_RECORD_LEN - 1, DWFileCoding.CP_1252);
    }


    /**
     * Fixedlen Beschreibung der TU-Header
     *
     * @param talType
     * @return
     */
    private static FixedLenRecordType getTuHeaderDescription(Tal4XAType talType) {
        FixedLenRecordTypeIdentifier[] typeIndetifier;
        String satzArt;


        // Satzart 9BS und DBS. Ist der Header der TU mit Bezeichnung und Bildnummern
        List<FixedLenFieldDescription> fieldDescription = new ArrayList<>();
        if (talType == Tal4XAType.TAL40A) {
            // Satzartkenner bei TAL40 + die Headerfelder
            satzArt = SATZART_9BS;
            typeIndetifier = new FixedLenRecordTypeIdentifier[]{
                    new FixedLenRecordTypeIdentifier(9, 9, "9"),
                    new FixedLenRecordTypeIdentifier(17, 21, " BS") };

            fieldDescription.addAll(Arrays.asList(new FixedLenFieldDescription(1, 3, TAL40A_KATALOG),
                                                  new FixedLenFieldDescription(7, 8, TAL40A_KG)));

        } else {
            // Satzartkenner bei TAL46 + die Headerfelder
            satzArt = SATZART_DBS;
            typeIndetifier = new FixedLenRecordTypeIdentifier[]{
                    new FixedLenRecordTypeIdentifier(9, 9, "D"),
                    new FixedLenRecordTypeIdentifier(17, 21, " BS") };

            fieldDescription.addAll(Arrays.asList(new FixedLenFieldDescription(1, 6, TAL46A_SA_RUMPF),
                                                  new FixedLenFieldDescription(7, 8, TAL46A_SA_INTERVALL)));

        }

        // Diese Felder sind in Tal40 + 46 identisch
        fieldDescription.addAll(Arrays.asList(new FixedLenFieldDescription(10, 14, TAL4XA_LFDNR),
                                              new FixedLenFieldDescription(17, 21, TAL4XA_BT_POS),
                                              // Ab hier Speziell TU
                                              new FixedLenFieldDescription(22, 24, TAL4XA_TU),
                                              new FixedLenFieldDescriptionArray(25, 504, 80, TAL4XA_TU_BENENNUNG),
                                              new FixedLenFieldDescriptionArray(505, 1584, 18, TAL4XA_BILD_TAFEL_IDENT)));


        return new FixedLenRecordType(satzArt,
                                      typeIndetifier,
                                      ArrayUtil.toArray(fieldDescription));
    }


    /**
     * Fixedlen Beschreibung der TU-Teile
     *
     * @param talType
     * @return
     */
    private static FixedLenRecordType getTUPartsDescription(Tal4XAType talType) {
        FixedLenRecordTypeIdentifier[] typeIndetifier;
        String satzArt;


        // Satzart 9 und D, die nicht BS sind. Hier sind die Teile der TU enthalten
        // Die Satzart 9 mit BS wird vorher raugefiltert, ist kein BS drin, dann hier der Default
        List<FixedLenFieldDescription> fieldDescription = new ArrayList<>();
        if (talType == Tal4XAType.TAL40A) {
            // Satzartkenner bei TAL40 + die Headerfelder
            satzArt = SATZART_9;
            typeIndetifier = new FixedLenRecordTypeIdentifier[]{
                    new FixedLenRecordTypeIdentifier(9, 9, "9") };

            fieldDescription.addAll(Arrays.asList(new FixedLenFieldDescription(1, 3, TAL40A_KATALOG),
                                                  new FixedLenFieldDescription(7, 8, TAL40A_KG),
                                                  new FixedLenFieldDescription(1408, 1410, TAL4XA_ART),
                                                  new FixedLenFieldDescriptionArray(1411, 1540, 13, TAL40A_BK_SNR),
                                                  new FixedLenFieldDescriptionArray(1411, 1530, 24, TAL40A_SA_SNR),
                                                  new FixedLenFieldDescriptionArray(1398, 1407, 10, TAL40A_POS_ADR_DIALOG),
                                                  new FixedLenFieldDescription(1541, 1600, TAL4XA_CODE_B)));

        } else {
            // Satzartkenner bei TAL46 + die Headerfelder
            satzArt = SATZART_D;
            typeIndetifier = new FixedLenRecordTypeIdentifier[]{
                    new FixedLenRecordTypeIdentifier(9, 9, "D") };

            fieldDescription.addAll(Arrays.asList(new FixedLenFieldDescription(1, 6, TAL46A_SA_RUMPF),
                                                  new FixedLenFieldDescription(7, 8, TAL46A_SA_INTERVALL)));

        }

        // Diese Felder sind in Tal40 + 46 identisch
        fieldDescription.addAll(Arrays.asList(new FixedLenFieldDescription(10, 14, TAL4XA_LFDNR),
                                              new FixedLenFieldDescription(17, 21, TAL4XA_BT_POS),
                                              // Ab hier speziell Teile
                                              new FixedLenFieldDescription(22, 22, TAL4XA_ERSETZT),
                                              new FixedLenFieldDescription(23, 41, TAL4XA_TEILENUMMER),
                                              new FixedLenFieldDescription(42, 60, TAL4XA_ENTFALL_TEILENUMMER),
                                              new FixedLenFieldDescription(61, 75, TAL4XA_BENENNUNG_DE),
                                              new FixedLenFieldDescriptionArray(76, 200, 25, TAL4XA_BENENNUNG),
                                              new FixedLenFieldDescription(201, 208, TAL4XA_ADRESSE_ERGAENZUNGSTEXTE),
                                              new FixedLenFieldDescription(209, 248, TAL4XA_SPRACHNEUTRALER_TEXT),
                                              new FixedLenFieldDescription(249, 249, TAL4XA_EINRUECKZAHL),
                                              new FixedLenFieldDescription(250, 250, TAL4XA_TUV_KZ),
                                              new FixedLenFieldDescription(251, 251, TAL4XA_WW_KZ),
                                              new FixedLenFieldDescription(252, 252, TAL4XA_A_N_KZ),
                                              new FixedLenFieldDescription(253, 256, TAL4XA_LENKUNG_GETRIEBE),
                                              new FixedLenFieldDescriptionArray(257, 271, 3, TAL4XA_FN_HINWEISE),
                                              new FixedLenFieldDescriptionArray(272, 337, 3, TAL4XA_MENGE_JE_BAUMUSTER),
                                              new FixedLenFieldDescription(338, 446, TAL4XA_ERGAENZUNGSTEXT_DE),
                                              new FixedLenFieldDescriptionArray(447, 941, 99, TAL4XA_ERGAENZUNGSTEXT_FREMD),
                                              new FixedLenFieldDescription(338, 356, TAL4XA_REP_TNR_1),
                                              new FixedLenFieldDescriptionArray(357, 941, 22, TAL4XA_REP_TNR_N),
                                              new FixedLenFieldDescriptionArray(942, 1397, 19, TAL4XA_WW_TNR),
                                              new FixedLenFieldDescription(1601, 1610, TAL4XA_TERMID),
                                              new FixedLenFieldDescription(1611, 1618, TAL4XA_ADRESSE_ERGAENZUNGSTEXTE_TEILESTAMM),
                                              new FixedLenFieldDescription(1619, 1658, TAL4XA_SPRACHNEUTRALER_TEXT_TEILESTAMM),
                                              new FixedLenFieldDescription(1659, 1659, TAL4XA_KZ_SICHERHEITSRELEVANT),
                                              new FixedLenFieldDescription(1660, 1660, TAL4XA_KZ_DIEBSTAHLRELEVANT),
                                              new FixedLenFieldDescription(1661, 1661, TAL4XA_KZ_CHINARELEVANT),
                                              new FixedLenFieldDescription(1662, 1662, TAL4XA_KZ_VEDOCRELEVANT),
                                              new FixedLenFieldDescription(1663, 1663, TAL4XA_KZ_NN_RELEVANT_1),
                                              new FixedLenFieldDescription(1664, 1664, TAL4XA_KZ_NN_RELEVANT_2),
                                              new FixedLenFieldDescription(1665, 1665, TAL4XA_KZ_NN_RELEVANT_3),
                                              new FixedLenFieldDescription(1666, 1666, TAL4XA_KZ_NN_RELEVANT_4),
                                              new FixedLenFieldDescription(1667, 1686, TAL4XA_VPD_IDENT)));


        return new FixedLenRecordType(satzArt,
                                      typeIndetifier,
                                      ArrayUtil.toArray(fieldDescription));
    }


    /**
     * Fixedlen Beschreibung der kurzen Fußnoten
     *
     * @param talType
     * @return
     */
    private static FixedLenRecordType getFootNotesShort(Tal4XAType talType) {
        FixedLenRecordTypeIdentifier[] typeIndetifier;
        String satzArt;


        // Satzart 9BS und DBS. Ist der Header der TU mit Bezeichnung und Bildnummern
        List<FixedLenFieldDescription> fieldDescription = new ArrayList<>();
        if (talType == Tal4XAType.TAL40A) {
            // Satzartkenner bei TAL40 + die Headerfelder
            satzArt = SATZART_AX;
            typeIndetifier = new FixedLenRecordTypeIdentifier[]{
                    new FixedLenRecordTypeIdentifier(9, 9, "A"),
                    new FixedLenRecordTypeIdentifier(18, 18, "X") };

            fieldDescription.addAll(Arrays.asList(new FixedLenFieldDescription(1, 3, TAL40A_KATALOG),
                                                  new FixedLenFieldDescription(7, 8, TAL40A_KG)));

        } else {
            // Satzartkenner bei TAL46 + die Headerfelder
            satzArt = SATZART_EX;
            typeIndetifier = new FixedLenRecordTypeIdentifier[]{
                    new FixedLenRecordTypeIdentifier(9, 9, "E"),
                    new FixedLenRecordTypeIdentifier(18, 18, "X") };

            fieldDescription.addAll(Arrays.asList(new FixedLenFieldDescription(1, 6, TAL46A_SA_RUMPF),
                                                  new FixedLenFieldDescription(7, 8, TAL46A_SA_INTERVALL)));

        }

        // Diese Felder sind in Tal40 + 46 identisch
        fieldDescription.addAll(Arrays.asList(new FixedLenFieldDescription(10, 12, TAL4XA_NR),
                                              new FixedLenFieldDescription(13, 15, TAL4XA_FN_FOLGE),
                                              new FixedLenFieldDescription(16, 16, TAL4XA_FN_SPRACHE),
                                              new FixedLenFieldDescription(17, 17, TAL4XA_FN_TABELLE),
                                              new FixedLenFieldDescription(18, 18, TAL4XA_ART),
                                              // Ab hier speziell Typ X
                                              new FixedLenFieldDescriptionArray(19, 1158, 190, TAL4XA_TEXTE)));


        return new FixedLenRecordType(satzArt,
                                      typeIndetifier,
                                      ArrayUtil.toArray(fieldDescription));
    }


    /**
     * Fixedlen Beschreibung der langen Fußnoten
     *
     * @param talType
     * @return
     */
    private static FixedLenRecordType getFootNotesLong(Tal4XAType talType) {
        FixedLenRecordTypeIdentifier[] typeIndetifier;
        String satzArt;


        // Satzart 9BS und DBS. Ist der Header der TU mit Bezeichnung und Bildnummern
        List<FixedLenFieldDescription> fieldDescription = new ArrayList<>();
        if (talType == Tal4XAType.TAL40A) {
            // Satzartkenner bei TAL40 + die Headerfelder
            satzArt = SATZART_AY;
            typeIndetifier = new FixedLenRecordTypeIdentifier[]{
                    new FixedLenRecordTypeIdentifier(9, 9, "A"),
                    new FixedLenRecordTypeIdentifier(18, 18, "Y") };

            fieldDescription.addAll(Arrays.asList(new FixedLenFieldDescription(1, 3, TAL40A_KATALOG),
                                                  new FixedLenFieldDescription(7, 8, TAL40A_KG)));

        } else {
            // Satzartkenner bei TAL46 + die Headerfelder
            satzArt = SATZART_EY;
            typeIndetifier = new FixedLenRecordTypeIdentifier[]{
                    new FixedLenRecordTypeIdentifier(9, 9, "E"),
                    new FixedLenRecordTypeIdentifier(18, 18, "Y") };

            fieldDescription.addAll(Arrays.asList(new FixedLenFieldDescription(1, 6, TAL46A_SA_RUMPF),
                                                  new FixedLenFieldDescription(7, 8, TAL46A_SA_INTERVALL)));

        }

        // Diese Felder sind in Tal40 + 46 identisch
        fieldDescription.addAll(Arrays.asList(new FixedLenFieldDescription(10, 12, TAL4XA_NR),
                                              new FixedLenFieldDescription(13, 15, TAL4XA_FN_FOLGE),
                                              new FixedLenFieldDescription(16, 16, TAL4XA_FN_SPRACHE),
                                              new FixedLenFieldDescription(17, 17, TAL4XA_FN_TABELLE),
                                              new FixedLenFieldDescription(18, 18, TAL4XA_ART),
                                              // Ab hier speziell Typ X
                                              new FixedLenFieldDescriptionArray(19, 1152, 126, TAL4XA_TEXTE)));

        return new FixedLenRecordType(satzArt,
                                      typeIndetifier,
                                      ArrayUtil.toArray(fieldDescription));
    }


    /**
     * Fixedlen Beschreibung der Nummern Fußnoten
     *
     * @param talType
     * @return
     */
    private static FixedLenRecordType getFootNotesNumber(Tal4XAType talType) {
        FixedLenRecordTypeIdentifier[] typeIndetifier;
        String satzArt;


        List<FixedLenFieldDescription> fieldDescription = new ArrayList<>();
        if (talType == Tal4XAType.TAL40A) {
            // Satzartkenner bei TAL40 + die Headerfelder
            satzArt = SATZART_A9;
            typeIndetifier = new FixedLenRecordTypeIdentifier[]{
                    new FixedLenRecordTypeIdentifier(9, 9, "A"),
                    new FixedLenRecordTypeIdentifier(18, 18, "9") };

            fieldDescription.addAll(Arrays.asList(new FixedLenFieldDescription(1, 3, TAL40A_KATALOG),
                                                  new FixedLenFieldDescription(7, 8, TAL40A_KG),
                                                  // Ab hier speziell Typ 9
                                                  new FixedLenFieldDescriptionArray(19, 828, 9, TAL40A_SA_VERWENDUNG)));

        } else {
            // Satzartkenner bei TAL46 + die Headerfelder
            satzArt = SATZART_E9;
            typeIndetifier = new FixedLenRecordTypeIdentifier[]{
                    new FixedLenRecordTypeIdentifier(9, 9, "E"),
                    new FixedLenRecordTypeIdentifier(18, 18, "9") };

            fieldDescription.addAll(Arrays.asList(new FixedLenFieldDescription(1, 6, TAL46A_SA_RUMPF),
                                                  new FixedLenFieldDescription(7, 8, TAL46A_SA_INTERVALL)));

        }

        // Diese Felder sind in Tal40 + 46 identisch
        fieldDescription.addAll(Arrays.asList(new FixedLenFieldDescription(10, 12, TAL4XA_NR),
                                              new FixedLenFieldDescription(13, 15, TAL4XA_FN_FOLGE),
                                              new FixedLenFieldDescription(16, 16, TAL4XA_FN_SPRACHE),
                                              new FixedLenFieldDescription(17, 17, TAL4XA_FN_TABELLE),
                                              new FixedLenFieldDescription(18, 18, TAL4XA_ART),
                                              // Ab hier speziell Typ 9
                                              new FixedLenFieldDescriptionArray(19, 828, 9, TAL4XA_FGST_END_NR),
                                              new FixedLenFieldDescriptionArray(19, 1318, 13, TAL4XA_BK_VERWENDUNG)));

        return new FixedLenRecordType(satzArt,
                                      typeIndetifier,
                                      ArrayUtil.toArray(fieldDescription));
    }


    /**
     * Fixedlen Beschreibung der Einsatzdaten Fußnoten
     *
     * @param talType
     * @return
     */
    private static FixedLenRecordType getFootNotesEinsatz(Tal4XAType talType) {
        FixedLenRecordTypeIdentifier[] typeIndetifier;
        String satzArt;


        List<FixedLenFieldDescription> fieldDescription = new ArrayList<>();
        if (talType == Tal4XAType.TAL40A) {
            // Satzartkenner bei TAL40 + die Headerfelder
            satzArt = SATZART_AE;
            typeIndetifier = new FixedLenRecordTypeIdentifier[]{
                    new FixedLenRecordTypeIdentifier(9, 9, "A"),
                    new FixedLenRecordTypeIdentifier(18, 18, "E") };

            fieldDescription.addAll(Arrays.asList(new FixedLenFieldDescription(1, 3, TAL40A_KATALOG),
                                                  new FixedLenFieldDescription(7, 8, TAL40A_KG)));

        } else {
            // Satzartkenner bei TAL46 + die Headerfelder
            satzArt = SATZART_EE;
            typeIndetifier = new FixedLenRecordTypeIdentifier[]{
                    new FixedLenRecordTypeIdentifier(9, 9, "E"),
                    new FixedLenRecordTypeIdentifier(18, 18, "E") };

            fieldDescription.addAll(Arrays.asList(new FixedLenFieldDescription(1, 6, TAL46A_SA_RUMPF),
                                                  new FixedLenFieldDescription(7, 8, TAL46A_SA_INTERVALL)));

        }

        // Diese Felder sind in Tal40 + 46 identisch
        fieldDescription.addAll(Arrays.asList(new FixedLenFieldDescription(10, 12, TAL4XA_NR),
                                              new FixedLenFieldDescription(13, 15, TAL4XA_FN_FOLGE),
                                              new FixedLenFieldDescription(16, 16, TAL4XA_FN_SPRACHE),
                                              new FixedLenFieldDescription(17, 17, TAL4XA_FN_TABELLE),
                                              new FixedLenFieldDescription(18, 18, TAL4XA_ART),
                                              // Ab hier speziell Typ E
                                              new FixedLenFieldDescription(19, 19, TAL4XA_FAHRGESTELL),
                                              new FixedLenFieldDescription(20, 20, TAL4XA_AB_IDENT),
                                              new FixedLenFieldDescription(21, 21, TAL4XA_BIS_IDENT),
                                              new FixedLenFieldDescription(22, 29, TAL4XA_HAUPT_IDENT),
                                              new FixedLenFieldDescription(30, 37, TAL4XA_EINSATZ_TERMIN),
                                              new FixedLenFieldDescription(38, 42, TAL4XA_STEUERCODE),
                                              new FixedLenFieldDescription(43, 43, TAL4XA_GUELTIGE_IDENT),
                                              new FixedLenFieldDescription(44, 44, TAL4XA_UNGUELTIGE_IDENT),
                                              new FixedLenFieldDescriptionArray(45, 1564, 8, TAL4XA_IDENT),
                                              new FixedLenFieldDescription(1565, 1574, TAL4XA_PEM)));

        return new FixedLenRecordType(satzArt,
                                      typeIndetifier,
                                      ArrayUtil.toArray(fieldDescription));
    }


    /**
     * Fixedlen Beschreibung der Farb Fußnoten
     *
     * @param talType
     * @return
     */
    private static FixedLenRecordType getFootNotesColor(Tal4XAType talType) {
        FixedLenRecordTypeIdentifier[] typeIndetifier;
        String satzArt;


        List<FixedLenFieldDescription> fieldDescription = new ArrayList<>();
        if (talType == Tal4XAType.TAL40A) {
            // Satzartkenner bei TAL40 + die Headerfelder
            satzArt = SATZART_AF;
            typeIndetifier = new FixedLenRecordTypeIdentifier[]{
                    new FixedLenRecordTypeIdentifier(9, 9, "A"),
                    new FixedLenRecordTypeIdentifier(18, 18, "F") };

            fieldDescription.addAll(Arrays.asList(new FixedLenFieldDescription(1, 3, TAL40A_KATALOG),
                                                  new FixedLenFieldDescription(7, 8, TAL40A_KG)));

        } else {
            // Satzartkenner bei TAL46 + die Headerfelder
            satzArt = SATZART_EF;
            typeIndetifier = new FixedLenRecordTypeIdentifier[]{
                    new FixedLenRecordTypeIdentifier(9, 9, "E"),
                    new FixedLenRecordTypeIdentifier(18, 18, "F") };

            fieldDescription.addAll(Arrays.asList(new FixedLenFieldDescription(1, 6, TAL46A_SA_RUMPF),
                                                  new FixedLenFieldDescription(7, 8, TAL46A_SA_INTERVALL)));

        }

        // Diese Felder sind in Tal40 + 46 identisch
        fieldDescription.addAll(Arrays.asList(new FixedLenFieldDescription(10, 12, TAL4XA_NR),
                                              new FixedLenFieldDescription(13, 15, TAL4XA_FN_FOLGE),
                                              new FixedLenFieldDescription(16, 16, TAL4XA_FN_SPRACHE),
                                              new FixedLenFieldDescription(17, 17, TAL4XA_FN_TABELLE),
                                              new FixedLenFieldDescription(18, 18, TAL4XA_ART),
                                              // Ab hier speziell Typ F
                                              new FixedLenFieldDescription(19, 46, TAL4XA_FARBTABELLE),
                                              new FixedLenFieldDescriptionArray(47, 346, 50, TAL4XA_FARBBENENNUNG),
                                              new FixedLenFieldDescription(347, 351, TAL4XA_ES2),
                                              new FixedLenFieldDescription(352, 951, TAL4XA_CODE_B),
                                              new FixedLenFieldDescriptionArray(952, 1011, 3, TAL4XA_FN_HINWEISE)));

        return new FixedLenRecordType(satzArt,
                                      typeIndetifier,
                                      ArrayUtil.toArray(fieldDescription));
    }


    /**
     * Fixedlen Beschreibung der Bedienungsanleitungen Fußnoten
     *
     * @param talType
     * @return
     */
    private static FixedLenRecordType getFootNotesManuals(Tal4XAType talType) {
        FixedLenRecordTypeIdentifier[] typeIndetifier;
        String satzArt;


        List<FixedLenFieldDescription> fieldDescription = new ArrayList<>();
        if (talType == Tal4XAType.TAL40A) {
            // Satzartkenner bei TAL40 + die Headerfelder
            satzArt = SATZART_AB;
            typeIndetifier = new FixedLenRecordTypeIdentifier[]{
                    new FixedLenRecordTypeIdentifier(9, 9, "A"),
                    new FixedLenRecordTypeIdentifier(18, 18, "B") };

            fieldDescription.addAll(Arrays.asList(new FixedLenFieldDescription(1, 3, TAL40A_KATALOG),
                                                  new FixedLenFieldDescription(7, 8, TAL40A_KG)));

        } else {
            // Satzartkenner bei TAL46 + die Headerfelder
            satzArt = SATZART_EB;
            typeIndetifier = new FixedLenRecordTypeIdentifier[]{
                    new FixedLenRecordTypeIdentifier(9, 9, "E"),
                    new FixedLenRecordTypeIdentifier(18, 18, "B") };

            fieldDescription.addAll(Arrays.asList(new FixedLenFieldDescription(1, 6, TAL46A_SA_RUMPF),
                                                  new FixedLenFieldDescription(7, 8, TAL46A_SA_INTERVALL)));

        }

        // Diese Felder sind in Tal40 + 46 identisch
        fieldDescription.addAll(Arrays.asList(new FixedLenFieldDescription(10, 12, TAL4XA_NR),
                                              new FixedLenFieldDescription(13, 15, TAL4XA_FN_FOLGE),
                                              new FixedLenFieldDescription(16, 16, TAL4XA_FN_SPRACHE),
                                              new FixedLenFieldDescription(17, 17, TAL4XA_FN_TABELLE),
                                              new FixedLenFieldDescription(18, 18, TAL4XA_ART),
                                              // Ab hier speziell Typ F
                                              new FixedLenFieldDescription(19, 46, TAL4XA_VARIANTENTABELLE),
                                              new FixedLenFieldDescriptionArray(47, 346, 50, TAL4XA_VARIANTENBENENNUNG),
                                              new FixedLenFieldDescription(347, 351, TAL4XA_VARIANTE),
                                              new FixedLenFieldDescription(352, 951, TAL4XA_CODE_B),
                                              new FixedLenFieldDescriptionArray(952, 1011, 3, TAL4XA_FN_HINWEISE)));

        return new FixedLenRecordType(satzArt,
                                      typeIndetifier,
                                      ArrayUtil.toArray(fieldDescription));
    }


    /**
     * Fixedlen Beschreibung der Lochbilder Fußnoten
     *
     * @param talType
     * @return
     */
    private static FixedLenRecordType getFootNotesLochBilder(Tal4XAType talType) {
        FixedLenRecordTypeIdentifier[] typeIndetifier;
        String satzArt;


        List<FixedLenFieldDescription> fieldDescription = new ArrayList<>();
        if (talType == Tal4XAType.TAL40A) {
            // Satzartkenner bei TAL40 + die Headerfelder
            satzArt = SATZART_AL;
            typeIndetifier = new FixedLenRecordTypeIdentifier[]{
                    new FixedLenRecordTypeIdentifier(9, 9, "A"),
                    new FixedLenRecordTypeIdentifier(18, 18, "L") };

            fieldDescription.addAll(Arrays.asList(new FixedLenFieldDescription(1, 3, TAL40A_KATALOG),
                                                  new FixedLenFieldDescription(7, 8, TAL40A_KG)));

        } else {
            // Satzartkenner bei TAL46 + die Headerfelder
            satzArt = SATZART_EL;
            typeIndetifier = new FixedLenRecordTypeIdentifier[]{
                    new FixedLenRecordTypeIdentifier(9, 9, "E"),
                    new FixedLenRecordTypeIdentifier(18, 18, "L") };

            fieldDescription.addAll(Arrays.asList(new FixedLenFieldDescription(1, 6, TAL46A_SA_RUMPF),
                                                  new FixedLenFieldDescription(7, 8, TAL46A_SA_INTERVALL)));

        }

        // Diese Felder sind in Tal40 + 46 identisch
        fieldDescription.addAll(Arrays.asList(new FixedLenFieldDescription(10, 12, TAL4XA_NR),
                                              new FixedLenFieldDescription(13, 15, TAL4XA_FN_FOLGE),
                                              new FixedLenFieldDescription(16, 16, TAL4XA_FN_SPRACHE),
                                              new FixedLenFieldDescription(17, 17, TAL4XA_FN_TABELLE),
                                              new FixedLenFieldDescription(18, 18, TAL4XA_ART),
                                              // Ab hier speziell Typ F
                                              new FixedLenFieldDescription(19, 46, TAL4XA_VARIANTENTABELLE),
                                              new FixedLenFieldDescription(347, 351, TAL4XA_VARIANTE),
                                              new FixedLenFieldDescription(352, 951, TAL4XA_CODE_B),
                                              new FixedLenFieldDescriptionArray(952, 1011, 3, TAL4XA_FN_HINWEISE)));

        return new FixedLenRecordType(satzArt,
                                      typeIndetifier,
                                      ArrayUtil.toArray(fieldDescription));
    }

    /**
     * Hält den Zusatztext zu einer Baumuster - Produkt Beziehung
     */
    private class AddTextForProductModel {

        private iPartsDataProductModels currentModelProduct;
        private EtkMultiSprache currentText;
        private String posNumber;
        private String lineNumber;

        public AddTextForProductModel(iPartsDataProductModels currentModelProduct) {
            this.currentModelProduct = currentModelProduct;
        }

        public void setCurrentText(EtkMultiSprache currentText) {
            this.currentText = currentText;
        }

        public void setPosNumber(String posNumber) {
            this.posNumber = posNumber;
        }

        public void setLineNumber(String lineNumber) {
            this.lineNumber = lineNumber;
        }

        public EtkMultiSprache getCurrentText() {
            return currentText;
        }

        public String getPosNumber() {
            return posNumber;
        }

        public String getLineNumber() {
            return lineNumber;
        }

        public iPartsDataProductModels getCurrentModelProduct() {
            return currentModelProduct;
        }

        public boolean isValid() {
            return (currentModelProduct != null) && (currentText != null) && (posNumber != null) && (lineNumber != null);
        }

        /**
         * Überprüft, ob das übergebene Produkt identisch zu dem aktuellen ist
         *
         * @param product
         * @return
         */
        public boolean isValidProduct(String product) {
            return currentModelProduct.getAsId().getProductNumber().equals(product);
        }

        /**
         * Überprüft, ob die Positionsnummer und die Zeilenummer valide sind.
         * Nur valide, wenn:
         * - jeder Textbaustein eines Zusatztextes die gleiche Positionsnummer hat
         * - die nachfolgende Zeilennummer 1 größer ist, als die aktuelle
         *
         * @param posNr
         * @param lineNumber
         * @return
         */
        public boolean checkValues(String posNr, String lineNumber) {
            if (getPosNumber() == null) {
                setPosNumber(posNr);
            } else if (!getPosNumber().equals(posNr)) {
                return false;
            }
            if (getLineNumber() == null) {
                setLineNumber(lineNumber);
            } else {
                int lineNumberTemp = Integer.parseInt(this.lineNumber);
                int newLineNumber = Integer.parseInt(lineNumber);
                if (lineNumberTemp != (newLineNumber - 1)) {
                    return false;
                } else {
                    setLineNumber(lineNumber);
                }
            }
            if (getCurrentText() == null) {
                currentText = new EtkMultiSprache();
            }
            return true;
        }
    }

    /**
     * Repräsentiert eine Farb-Tabellenfußnote
     */
    public static class ColortTablefootnote {

        private List<iPartsFootNoteId> singleFootnotes;
        private Set<iPartsFootNoteId> allContainingIds;
        private iPartsMigrationFootnotesHandler footnotesHandler;

        public ColortTablefootnote(iPartsMigrationFootnotesHandler footnotesHandler) {
            singleFootnotes = new ArrayList<>();
            allContainingIds = new HashSet<>();
            this.footnotesHandler = footnotesHandler;
        }

        public void addSingleFootnoteId(iPartsFootNoteId singleFootnoteId) {
            singleFootnotes.add(singleFootnoteId);
            allContainingIds.add(singleFootnoteId);
        }

        public boolean containsFootnoteId(iPartsFootNoteId footNoteId) {
            return allContainingIds.contains(footNoteId);
        }

        /**
         * Befüllt die übergebene {@link iPartsDataFootNoteCatalogueRef}-Liste mit allen Fußnoten, die Bestandteil der
         * kompletten Farb-Tabellenfußnote sind. Durch die vorgegebene Reihenfolge erzeuge wir somit die komplette
         * Farb-Tabellenfußnote innerhalb der übergebenen Liste.
         *
         * @param catalogueRefList
         * @param mainRefId
         * @param seqNo
         */
        public int fillCatalogueReferencesList(List<iPartsDataFootNoteCatalogueRef> catalogueRefList,
                                               iPartsFootNoteCatalogueRefId mainRefId, int seqNo) {
            int tempSeqNo = seqNo;
            PartListEntryId partListEntryId = mainRefId.getPartListEntryId();
            for (int i = 0; i < singleFootnotes.size(); i++) {
                iPartsFootNoteId footNoteId = singleFootnotes.get(i);
                iPartsFootNoteCatalogueRefId catalogueRefId = new iPartsFootNoteCatalogueRefId(partListEntryId, footNoteId.getFootNoteId());
                boolean isMarked = catalogueRefId.equals(mainRefId);
                catalogueRefList.add(footnotesHandler.makeFootnoteRefDataObject(catalogueRefId, isMarked, getAllFootNotesAsString(), tempSeqNo));
                tempSeqNo++;
            }
            return tempSeqNo;
        }

        /**
         * Erzeugt einen kommaseparierten String mit allen Original-Fußnotennummern der Farb-Tabellenfußnote
         *
         * @return
         */
        public String getAllFootNotesAsString() {
            List<String> originalFootnotes = new ArrayList<>();
            for (iPartsFootNoteId footnoteId : singleFootnotes) {
                if (footnotesHandler.isColorFootnote(footnoteId)) {
                    originalFootnotes.add(footnotesHandler.getOriginalFootnoteNumberFromId(footnoteId));
                }
            }
            if (!originalFootnotes.isEmpty()) {
                return StrUtils.stringListToString(originalFootnotes, ",");
            }
            return "";
        }

        public String getFirstFootnotenumberInTable() {
            if (singleFootnotes.isEmpty()) {
                return "";
            }
            return footnotesHandler.getOriginalFootnoteNumberFromId(singleFootnotes.get(0));
        }
    }

}