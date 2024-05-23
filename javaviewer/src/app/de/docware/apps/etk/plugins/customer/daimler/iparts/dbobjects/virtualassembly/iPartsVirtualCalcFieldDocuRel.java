/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualassembly;

import de.docware.apps.etk.base.config.db.datatypes.enumtypes.SetOfEnumDataType;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.mechanic.EtkDataPart;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.ids.PartId;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.VirtualMaterialType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.filter.iPartsOmittedParts;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsVirtualNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.code.DaimlerCodes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilterHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsAccAndAsCodeCache;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsDialogEECategory;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsDocuRelevant;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsWireHarnessHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogBCTEPrimaryKey;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogSeries;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsFactoryData;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsFootNote;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsWireHarness;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.HmMSmNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;
import de.docware.framework.modules.config.db.VirtualFieldDefinition;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.session.Session;
import de.docware.framework.utils.VarParam;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.misc.booleanfunctionparser.BooleanFunctionSyntaxException;
import de.docware.util.misc.booleanfunctionparser.model.Conjunction;
import de.docware.util.misc.booleanfunctionparser.model.Disjunction;
import de.docware.util.os.OsUtils;
import de.docware.util.sql.SQLStringConvert;

import java.util.*;
import java.util.stream.Collectors;

import static de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualassembly.iPartsVirtualDocuRelStates.*;

/**
 * Klasse zur Berechnung des Feldes DD_CALCULATED_DOCU_RELEVANT
 * Berechnung kann aus den Dialogdaten ({@link iPartsVirtualAssemblyDialogBase.DialogPartsListResult}) oder
 * den Stücklistendaten {@link EtkDataPartListEntry} erfolgen
 * project wird erst bei späteren Erweiterungen benötigt
 */
public class iPartsVirtualCalcFieldDocuRel extends iPartsDataVirtualFieldsDefinition implements iPartsConst {

    private static final String ALL_VALID_CODE = ";";
    // ETKZ Werte sind nur für Werte am Teilestamm!
    private static final Set<String> ETKZ_VALUES = new HashSet<>(); // ETKZ Werte für die normale Berechnung
    private static final Set<String> ETKZ_VALUES_ALT_CALC = new HashSet<>(); // ETKZ Werte für die alternative Berechnung
    private static final Map<String, String> ERROR_MAP = new TreeMap<>();

    // ETK Werte sind nur Werte am DIALOG Datensatz!
    public static final Set<String> ETK_VALUES = new HashSet<>(); // ETK Werte für die normale Berechnung

    static {
        ETKZ_VALUES.add("E");
        ETKZ_VALUES.add("F");
        ETKZ_VALUES.add("V");
        ETKZ_VALUES.add("S");  // siehe DAIMLER-8438

        ETKZ_VALUES_ALT_CALC.addAll(ETKZ_VALUES);
        ETKZ_VALUES_ALT_CALC.add(iPartsWireHarnessHelper.WIRE_HARNESS_ETKZ_WITH_ADDITIONAL_CHECK);

        ETK_VALUES.add(iPartsWireHarnessHelper.WIRE_HARNESS_ETKZ_WITH_ADDITIONAL_CHECK);
        ETK_VALUES.add("KB");
        ETK_VALUES.add("N");
        ETK_VALUES.add("NB");
    }

    private final EtkProject project;
    private final EtkDataPartListEntry partListEntry;
    private boolean isWireHarnessRelevantForOldCalcModel; // Kenner, ob es sich um den Sonderfall eines Leitungssatz-BK handelt (DAIMLER-11956) → NUR für das alte Berechnungsmodell relevant!

    private boolean comesFromPictureAndTuValidation; // Kenner, ob der Kontext die Qualitätsprüfung ist

    public static synchronized void clearBadCodes() {
        ERROR_MAP.clear();
    }

    /**
     * Setzt die übergebene Doku-Relevanz an der übergebenen Stücklistenposition
     *
     * @param partListEntry
     * @param docuRelevant
     */
    private static void setDocuRelevant(EtkDataPartListEntry partListEntry, iPartsDocuRelevant docuRelevant) {
        if ((partListEntry != null) && (docuRelevant != null)) {
            String dbValue = SetOfEnumDataType.getSetOfEnumTag(docuRelevant.getDbValue());
            partListEntry.getAttributes().addField(DD_CALCULATED_DOCU_RELEVANT, dbValue, true, DBActionOrigin.FROM_DB);
        } else if (partListEntry != null) {
            partListEntry.getAttributes().addField(DD_CALCULATED_DOCU_RELEVANT, "", true, DBActionOrigin.FROM_DB);
        }
    }

    /**
     * Liefert die berechnete Doku-Relevanz zur übergebenen Stücklistenposition
     *
     * @param partListEntry
     * @return
     */
    private static iPartsDocuRelevant getCalcDocRelevantValueFromPartListEntry(EtkDataPartListEntry partListEntry) {
        List<String> docRelValue = partListEntry.getFieldValueAsSetOfEnum(DD_CALCULATED_DOCU_RELEVANT);
        if (!docRelValue.isEmpty()) {
            return iPartsDocuRelevant.getFromDBValue(docRelValue.get(0));
        }
        return null;
    }

    public iPartsVirtualCalcFieldDocuRel(EtkProject project, EtkDataPartListEntry partListEntry, boolean comesFromPictureAndTuValidation) {
        this.project = project;
        this.partListEntry = partListEntry;
        this.comesFromPictureAndTuValidation = comesFromPictureAndTuValidation;
    }

    public iPartsVirtualCalcFieldDocuRel(EtkProject project, EtkDataPartListEntry partListEntry) {
        this(project, partListEntry, false);
    }

    public EtkProject getProject() {
        return project;
    }

    public EtkDataPartListEntry getPartListEntry() {
        return partListEntry;
    }

    /**
     * Liefert zurück, ob für die aktuelle Baureihe das neue Berechnungsmodell verwendet werden soll.
     *
     * @return
     */
    protected boolean isAlternativeCalculation() {
        iPartsDialogSeries dialogSeries = getDialogSeries();
        return dialogSeries.isAlternativeDocuCalc();
    }

    public boolean isWireHarnessRelevantForOldCalcModel() {
        return isWireHarnessRelevantForOldCalcModel;
    }

    protected String getFieldValue(String tableName, String virtualFieldName) {
        VirtualFieldDefinition virtDef = findField(tableName, virtualFieldName);
        if (virtDef != null) {
            if (partListEntry != null) {
                return partListEntry.getFieldValue(virtDef.getVirtualFieldName());
            }
        }
        return "";
    }

    protected String getFieldValue(String fieldName) {
        if (partListEntry != null) {
            return partListEntry.getPart().getFieldValue(fieldName);
        }
        return "";
    }

    public iPartsDocuRelevant getDocuRelevant() {
        return iPartsDocuRelevant.getFromDBValue(getFieldValue(TABLE_KATALOG, DIALOG_DD_DOCU_RELEVANT));
    }

    private iPartsDocuRelevant getDocuRelevantFromPartListEntry(EtkDataPartListEntry partListEntry) {
        return iPartsDocuRelevant.getFromDBValue(partListEntry.getFieldValue(DIALOG_DD_DOCU_RELEVANT));
    }

    /**
     * Liefert das Ersatzteil-Kennzeichen am Teilestamm
     * Falls wir uns im Kontext der Qualitätsprüfung befinden muss auf eine Q-Sachnummer geprüft werden
     * Bei Q-Sachnummern soll immer das ETKZ der Originalteilenummer verwendet werden
     *
     * @return
     */
    private String getETKZFromPart() {
        if (comesFromPictureAndTuValidation) {
            if (partListEntry != null) {
                if (partListEntry.getPart().getAsId().getMatNr().startsWith("Q")) {
                    String originalMatNr = partListEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.DA_ORIGINAL_MAT_NR);
                    if (StrUtils.isValid(originalMatNr)) {
                        PartId partId = partListEntry.getPart().getAsId();
                        EtkDataPart dataPartOrigMat = EtkDataObjectFactory.createDataPart(getProject(), originalMatNr, partId.getMVer());
                        if (dataPartOrigMat.existsInDB()) { // lädt das Material implizit vollständig
                            return dataPartOrigMat.getFieldValue(FIELD_M_ETKZ);
                        }
                    }
                }
            }
        }
        return getFieldValue(FIELD_M_ETKZ);
    }

    private String getVisETKZFromPart(String etkzValue) {
        return getProject().getVisObject().asText(TABLE_MAT, FIELD_M_ETKZ, etkzValue, getProject().getDBLanguage());
    }

    /**
     * Liefert das Ersatzteil-Kennzeichen an der Stücklistenposition
     *
     * @return
     */
    public String getETKFromPartlistEntry() {
        return getFieldValue(TABLE_KATALOG, DIALOG_DD_ETKZ);
    }

    private String getDDFED() {
        return getFieldValue(TABLE_KATALOG, DIALOG_DD_FED);
    }

    protected boolean isDDFEDequalZB() {
        return getDDFED().equals("ZB");
    }

    protected String getDDHierarchy() {
        return getFieldValue(TABLE_KATALOG, DIALOG_DD_HIERARCHY);
    }

    private iPartsDialogEECategory getEECategory() {
        return iPartsDialogEECategory.getFromDBValue(getFieldValue(FIELD_M_VERKSNR));
    }

    private iPartsDocuRelevant calculateDocuRelevant(boolean specialCalc) {
        return calculateDocuRelevant(specialCalc, null);
    }

    protected iPartsDataAssembly getOwnerAssembly() {
        if (getPartListEntry() instanceof iPartsDataPartListEntry) {
            return ((iPartsDataPartListEntry)getPartListEntry()).getOwnerAssembly();
        }
        return null;
    }

    protected HmMSmNode getHmMSmNode() {
        iPartsDataAssembly ownerAssembly = getOwnerAssembly();
        if ((ownerAssembly != null) && ownerAssembly.isDialogSMConstructionAssembly()) {
            return iPartsVirtualNode.getHmMSmNodeForAssemblyId(ownerAssembly.getAsId(), getProject());
        }
        return null;
    }

    private boolean isModuleHidden() {
        HmMSmNode currentHmMSmNode = getHmMSmNode();
        if (currentHmMSmNode != null) {
            return currentHmMSmNode.isHiddenRecursively();
        }
        return false;
    }

    private boolean isModuleNoCalc() {
        HmMSmNode currentHmMSmNode = getHmMSmNode();
        if (currentHmMSmNode != null) {
            return currentHmMSmNode.isNoCalcRecursively();
        }
        return false;
    }

    private boolean isNodeWithChangeDocuRelOmittedPart() {
        HmMSmNode currentHmMSmNode = getHmMSmNode();
        if (currentHmMSmNode != null) {
            return currentHmMSmNode.isChangeDocuRelOmittedPartRecursively();
        }
        return false;
    }

    private boolean isModuleHiddenOrNoCalc() {
        HmMSmNode currentHmMSmNode = getHmMSmNode();
        if (currentHmMSmNode != null) {
            return currentHmMSmNode.isHiddenRecursively() || currentHmMSmNode.isNoCalcRecursively();
        }
        return false;
    }

    /**
     * Berechnet die Doku-Relevanz abhängig von den Daten des aktuellen Stücklisteneintrags
     *
     * @param specialCalc: true: Alternative Berechnung der zu bearbeitenden Stände - DIALOG
     * @return
     */
    private iPartsDocuRelevant calculateDocuRelevant(boolean specialCalc, List<iPartsVirtualDocuRelStates.DocuRelFilterElement> docuRelList) {
        // Die von Hand gesetzte Doku-Relevanz
        iPartsDocuRelevant result = getDocuRelevant();
        boolean isRealFilter = docuRelList == null;
        // Hier wird unterschieden, ob der Aufruf aus der Filterung heraus kommt oder für den Erklärdialog der Doku-Relevanz
        // bestimmt war
        if (isRealFilter) {
            return calculateDocuRelevantForRealFilter(result, specialCalc);
        } else {
            return calculateDocuRelevantForDescription(result, specialCalc, docuRelList);
        }
    }

    /**
     * Berechnet die Doku-Relevanz samt Erklärungstexte für den Aufruf aus dem Doku-Relevanz Erklärdialog.
     *
     * @param currentResult
     * @param specialCalc
     * @param docuRelList
     * @return
     */
    private iPartsDocuRelevant calculateDocuRelevantForDescription(iPartsDocuRelevant currentResult, boolean specialCalc,
                                                                   List<DocuRelFilterElement> docuRelList) {
        boolean triggerWasFound = containsTriggerElement(docuRelList);
        // Handelt es sich um den normalen oder den alternativen Weg
        createAndAddDocuRelFilterElem(DOCUREL_CALCULATION_MODEL, specialCalc, docuRelList);
        // Check, ob Teileposition in einem Hidden oder NoCalc Modul verwendet wird
        triggerWasFound |= createAndAddDocuRelFilterElemForResult(isModuleHiddenOrNoCalc(), specialCalc, triggerWasFound,
                                                                  DOCUREL_MODULE_HIDDEN_NOCALC,
                                                                  iPartsDocuRelevant.DOCU_RELEVANT_NO, docuRelList);

        // Check, ob Teileposition mit Verwendung in AS Stückliste. Falls ja, dann "dokumentiert" (Status: D)
        triggerWasFound |= createAndAddDocuRelFilterElemForResult(isUsedInAS(), specialCalc, triggerWasFound,
                                                                  DOCUREL_USED_IN_AS, iPartsDocuRelevant.DOCU_DOCUMENTED,
                                                                  docuRelList);
        // Check, ob Teileposition in einem anderen nicht freigegebenen Autorenauftrag dokumentiert wurde. Falls ja, dann "dokumentiert" (Status: (D))
        triggerWasFound |= createAndAddDocuRelFilterElemForResult(isUsedInASInOtherAuthorOrder(), specialCalc,
                                                                  triggerWasFound, DOCUREL_USED_IN_AS_IN_OTHER_AUTHOR_ORDER,
                                                                  iPartsDocuRelevant.DOCU_DOCUMENTED_IN_AUTHOR_ORDER,
                                                                  docuRelList);

        // Wenn die Doku-Relevanz != DOCU_RELEVANT_NOT_SPECIFIED, dann wurde der Wert manuell gesetzt. Diese Werte gewinnen
        // vor der Berechnung des künstlichen Wertes.
        boolean valueSetByAuthor = currentResult != iPartsDocuRelevant.DOCU_RELEVANT_NOT_SPECIFIED;
        iPartsDocuRelevant overallResult;
        if (!valueSetByAuthor) {
            overallResult = calcDocuRelForNonAuthorValue(docuRelList, triggerWasFound, specialCalc);
        } else {
            calcDocuRelForAlreadyExistingAuthorValue(docuRelList, currentResult, specialCalc);
            overallResult = currentResult;

        }
        // Hier folgen nun die Zusatzinformationen zu den PVs, damit die Autoren nachvollziehen können, wieso
        // die Doku-Relevanz beeinflusst wurde
        createAndAddDocuRelFilterElem(DOKU_REL_STATUS_CURRENT_POSV, specialCalc, docuRelList);
        createAndAddDocuRelFilterElem(DOCU_REL_POSV_RESULTS, specialCalc, docuRelList);
        createAndAddDocuRelFilterElem(DOCU_REL_V_POSITION_RESULTS, specialCalc, docuRelList);
        createAndAddDocuRelFilterElem(DOCU_REL_OVERALL_RESULT, overallResult, specialCalc, false,
                                      docuRelList);
        return overallResult;
    }

    /**
     * Berechnung der Doku-Relevanz bei manuell gesetztem Status vom Autor
     *
     * @param docuRelList
     * @param currentResult
     * @param specialCalc
     */
    private void calcDocuRelForAlreadyExistingAuthorValue(List<DocuRelFilterElement> docuRelList, iPartsDocuRelevant currentResult, boolean specialCalc) {
        // für Anzeige alle Filter durchlaufen
        iPartsDocuRelevant newCalculatedValue = checkBaseDocRelevance(specialCalc, docuRelList);
        // Berechnung der Doku-Relevanz auf Basis der Positionsvarianten
        iPartsDocuRelevant resultAfterPositionVariants = getVariantsCheckResultForDescription(newCalculatedValue);
        createAndAddDocuRelFilterElem(DOCU_REL_POSV_RESULTS_CHANGED_ORIGINAL_RESULT, resultAfterPositionVariants,
                                      specialCalc, false, docuRelList);
        // Abschließende Prüfung für Wegfallsachnummern in HM/M/SM Knoten, in denen die Sonderberechnung aktiviert wurde:
        // Check, ob es sich um so einen Fall handelt
        if (isChangeDocuRelForOmittedPart(getPartListEntry(), resultAfterPositionVariants)) {
            createAndAddDocuRelFilterElem(DOCU_REL_CHANGE_DOCU_REL_OMITTED_PART, iPartsDocuRelevant.DOCU_RELEVANT_YES,
                                          specialCalc, false, docuRelList);
        } else {
            createAndAddDocuRelFilterElem(DOCU_REL_CHANGE_DOCU_REL_OMITTED_PART, specialCalc, docuRelList);
        }
        // Erst nachdem es ohne gesetzten Wert durchsimuliert wurde, den manuell gesetzten Wert vom Autor als Ergebnis
        // bzw Grund setzen. Dafür wird der bisherige Trigger gelöscht und die manuelle Eingabe als Trigger gesetzt.
        resetTriggeredElements(docuRelList);
        createAndAddDocuRelFilterElem(DOCUREL_SET_BY_AUTHOR, currentResult, specialCalc, true, docuRelList);
    }

    /**
     * Berechnung der Doku-Relevanz bei nicht gesetztem Status vom Autor
     *
     * @param docuRelList
     * @param triggerWasFound
     * @param specialCalc
     * @return
     */
    private iPartsDocuRelevant calcDocuRelForNonAuthorValue(List<DocuRelFilterElement> docuRelList,
                                                            boolean triggerWasFound, boolean specialCalc) {
        // Wurde schon ein Trigger gesetzt, der nichts mit der Berechnung der Doku-Relevanz zu tun hat (dokumentiert, versteckt, usw),
        // dann wird dieser zwischengespeichert und nach der Berechnung wieder gesetzt
        DocuRelFilterElement currentTrigger = findTriggerElement(docuRelList);
        resetTriggeredElements(docuRelList);
        // Der Autor hat keinen Status manuell gesetzt:
        createAndAddDocuRelFilterElem(DOCUREL_SET_BY_AUTHOR, specialCalc, docuRelList);

        // Doku-Relevanz unter Berücksichtigung aller Positionsvarianten:
        // 1. Die Doku-Relevanz auf Basis aller Einzelprüfungen berechnen
        iPartsDocuRelevant overallResult = checkBaseDocRelevance(specialCalc, docuRelList);
        // 2. Berechnung der Doku-Relevanz auf Basis der Positionsvarianten und des bisherigen Ergebnisses.
        iPartsDocuRelevant resultAfterPositionVariants = getVariantsCheckResultForDescription(overallResult);
        boolean docuRelChanged = overallResult != resultAfterPositionVariants;
        if (docuRelChanged) {
            // Haben die Positionsvarianten die Doku-Relevanz beeinflusst, dann müssen die bisherigen Trigger
            // zurückgesetzt werden (Ergebnis der Einzelprüfungen)
            resetTriggeredElements(docuRelList);
            overallResult = resultAfterPositionVariants;
        }
        // Ergebnis der Doku-Relevanz auf Basis der PVs setzen
        createAndAddDocuRelFilterElem(DOCU_REL_POSV_RESULTS_CHANGED_ORIGINAL_RESULT, resultAfterPositionVariants,
                                      specialCalc, docuRelChanged, docuRelList);

        // Abschließende Prüfung für Wegfallsachnummern in HM/M/SM Knoten, in denen die Sonderberechnung aktiviert wurde:
        // Check, ob es sich um so einen Fall handelt
        docuRelChanged = isChangeDocuRelForOmittedPart(getPartListEntry(), resultAfterPositionVariants);
        if (docuRelChanged) {
            // Die Teilenummer ist eine Wegfallsachnummer mit dem Status "ANR" und für den HMMSM Strukturpfad wurde
            // die Sonderberechnung aktiviert. D.h. die Doku-Relevanz ändert sich dadurch von "ANR" zu "offen".
            // Daher müssen die bisherigen Trigger zurückgesetzt werden
            resetTriggeredElements(docuRelList);
            overallResult = iPartsDocuRelevant.DOCU_RELEVANT_YES;
        }
        // Ergebnis der Doku-Relevanz für Wegfallsachnummern setzen
        createAndAddDocuRelFilterElemForResult(docuRelChanged, specialCalc, triggerWasFound,
                                               DOCU_REL_CHANGE_DOCU_REL_OMITTED_PART,
                                               iPartsDocuRelevant.DOCU_RELEVANT_YES, docuRelList);

        // Die Berechnung ist vorbei. Existierte schon ein Trigger, dann muss dieser gesetzt werden
        if (currentTrigger != null) {
            resetTriggeredElements(docuRelList);
            currentTrigger.setTrigger(true);
            overallResult = currentTrigger.getState();
        }
        return overallResult;
    }

    /**
     * Liefert das Ergebnis des Doku-Relevanz Checks auf Basis von allen Positionsvarianten der aktuellen Position
     *
     * @param calculatedResultForDescription
     * @return
     */
    private iPartsDocuRelevant getVariantsCheckResultForDescription(iPartsDocuRelevant calculatedResultForDescription) {
        // Hier muss beachtet werden, dass an der Position schon das Endergebnis steht und das muss NICHT das
        // Ergebnis der Positionsvariantenprüfung sein (nach den Varianten wird noch der Wegfallsachnr-Check gemacht!).
        // D.h. das aktuelle Endergebnis muss zwischengespeichert werden und an der aktuellen Position muss die
        // neu berechnete Doku-Relevanz gesetzt werden, da bei calcDocRelValueForPositionVariants() die Doku-Relevanzen
        // der Positionsvarianten auch neu berechnet werden. Weil die PV angepasst werden, muss man ihre aktuellen
        // Doku-Relevanzen ebenfalls zwischenspeichern!
        List<EtkDataPartListEntry> positionVariantsWithCurrentEntry = getPositionVariants(false);
        if (positionVariantsWithCurrentEntry == null) {
            return calculatedResultForDescription;
        }
        // 1. Die aktuellen Werte zwischenspeichern (also die, die schon im normalen lauf berechnet wurden und nun angezeigt werden)
        Map<PartListEntryId, iPartsDocuRelevant> currentValues = positionVariantsWithCurrentEntry.stream()
                .filter(entry -> getCalcDocRelevantValueFromPartListEntry(entry) != null)
                .collect(Collectors.toMap(EtkDataPartListEntry::getAsId, iPartsVirtualCalcFieldDocuRel::getCalcDocRelevantValueFromPartListEntry, (o1, o2) -> o1, HashMap::new));
        // 2. An der aktuellen Position den Doku-Rel Wert setzen, der von Grund auf neu berechnet wurde
        setDocuRelevant(getPartListEntry(), calculatedResultForDescription);
        // 3. Jetzt den Check mit den Varianten laufen lassen (Doku-Rel Werte der Varianten werden hier ebenfalls von Grund auf neu berechnet)
        calcDocRelValueForPositionVariants();
        // 4. Jetzt das Ergebnis auf Basis des Variantenchecks speichern
        iPartsDocuRelevant resultAfterPositionVariants = getCalcDocRelevantValueFromPartListEntry(getPartListEntry());
        // 5. Zum Schluss die ursprünglichen Werte wieder setzen
        positionVariantsWithCurrentEntry.forEach(entry -> setDocuRelevant(entry, currentValues.get(entry.getAsId())));
        return resultAfterPositionVariants;
    }

    /**
     * Setzt alle Elemente, die die aktuelle Doku-Relevanz getriggert haben zurück.
     *
     * @param docuRelList
     */
    private void resetTriggeredElements(List<DocuRelFilterElement> docuRelList) {
        if (docuRelList != null) {
            for (DocuRelFilterElement docuRelFilterElement : docuRelList) {
                if (docuRelFilterElement.isTrigger()) {
                    docuRelFilterElement.setTrigger(false);
                }
            }
        }
    }

    /**
     * Berechnet die Doku-Relevanz für die ganz normale Verwendung beim Aufbau der Stückliste
     *
     * @param currentResult
     * @param specialCalc
     * @return
     */
    private iPartsDocuRelevant calculateDocuRelevantForRealFilter(iPartsDocuRelevant currentResult, boolean specialCalc) {
        // Check, ob Teileposition in einem Hidden oder NoCalc Modul verwendet wird
        if (isModuleHiddenOrNoCalc()) {
            return iPartsDocuRelevant.DOCU_RELEVANT_NO;
        }
        // Check, ob Teileposition mit Verwendung in AS Stückliste. Falls ja, dann "dokumentiert" (Status: E)
        if (isUsedInAS()) {
            return iPartsDocuRelevant.DOCU_DOCUMENTED;
        }
        // DAIMLER-14184: Falls die Position keine Verwendung in AS hat (Status: D), dann muss kontrolliert
        // werden, ob die Teileposition schon in einem anderen nicht freigegebenen Autorenauftrag dokumentiert ist
        if (isUsedInASInOtherAuthorOrder()) {
            return iPartsDocuRelevant.DOCU_DOCUMENTED_IN_AUTHOR_ORDER;
        }

        iPartsDocuRelevant tempCalcResult = currentResult;
        // Wenn die Doku-relevanz != DOCU_RELEVANT_NOT_SPECIFIED, dann wurde der Wert manuell gesetzt. Diese Werte gewinnen
        // vor der Berechnung des künstlichen Wertes.
        if (currentResult == iPartsDocuRelevant.DOCU_RELEVANT_NOT_SPECIFIED) {
            tempCalcResult = checkBaseDocRelevance(specialCalc, null);
        }

        return tempCalcResult;
    }

    /**
     * Check, ob die Position in einem anderen Autorenauftrag schon in AS übernommen wurde
     *
     * @return
     */
    private boolean isUsedInASInOtherAuthorOrder() {
        return AOAffiliationForDIALOGEntry.isUsedInOtherAuthorOrders(getFieldValue(TABLE_KATALOG, DD_AUTHOR_ORDER_AFFILIATION));
    }

    /**
     * Abhängig vom Ergebnis der zuvor ausgeführten Prüfung wird der Text der ausgeführten Berechnung erzeugt und gesetzt.
     *
     * @param checkResult
     * @param specialCalc
     * @param triggerWasFound
     * @param state
     * @param docuRelResult
     * @param docuRelList
     * @return
     */
    private boolean createAndAddDocuRelFilterElemForResult(boolean checkResult, boolean specialCalc, boolean triggerWasFound,
                                                           iPartsVirtualDocuRelStates state, iPartsDocuRelevant docuRelResult,
                                                           List<DocuRelFilterElement> docuRelList) {
        if (checkResult) {
            createAndAddDocuRelFilterElem(state, docuRelResult, specialCalc, !triggerWasFound, docuRelList);
            return true;
        } else {
            createAndAddDocuRelFilterElem(state, specialCalc, docuRelList);
            return false;
        }
    }

    /**
     * Berechnet die Doku-Relevanz zur übergebenen Stücklistenposition und den anderen Positionsvarianten zur gemeinsamen
     * DIALOG-Position.
     *
     * @param positionVariants
     */
    private void calcDocRelForAllPositionVariants(List<EtkDataPartListEntry> positionVariants) {
        iPartsOmittedParts omittedParts = iPartsOmittedParts.getInstance(getProject());
        EtkDataPartListEntry currentEntry = getPartListEntry();
        if (currentEntry instanceof iPartsDataPartListEntry) {
            List<EtkDataPartListEntry> tempPositionVariants = new ArrayList<>(positionVariants);
            tempPositionVariants.add(currentEntry);
            if (isAlternativeCalculation()) {
                // Erst das Doku-Relevanz Ergebnis für die einzelnen Positionsvarianten berechnen (bei der aktuellen Position
                // wurde die Doku-Relevanz schon berechnet)
                for (EtkDataPartListEntry positionVariant : positionVariants) {
                    iPartsVirtualCalcFieldDocuRel posV = new iPartsVirtualCalcFieldDocuRel(getProject(), positionVariant);
                    posV.calculateAndSetDocuRelevant();
                }
                calcOmittedPartsOrETKZFlagDocuRelForVariantsAlternate(tempPositionVariants, omittedParts);
            } else {
                Optional<WireHarnessDocuRelData> wireHarnessRelevantPositions = calcSingleDocuRelValuesWithWireHarnessCheckForSpecialCalc(positionVariants);
                calcOmittedPartsOrETKZFlagDocuRelForVariants(tempPositionVariants, omittedParts);
                wireHarnessRelevantPositions.ifPresent(WireHarnessDocuRelData::calcWireHarnessDocuRelevance);
            }
            calcOmittedPartsAndETCodesDocuRelForVariants(tempPositionVariants, omittedParts);
            calcDocuRelForVPositions(tempPositionVariants);
        }
    }

    /**
     * Kalkuliert die Doku-Relevanz für die V-Positionsvarianten nach folgender Logik:
     * <p>
     * DAIMLER-14047: Diese Prüfung ist nur bei V-Teilen (ET-KZ = V) relevant:
     * <p>
     * Gibt es auf der V-Position weitere Positionen mit gleicher POS, PV, AA und Code
     * Dann prüfe, ist zur genannten Position mind. eine weitere Position mit ET-Zählerstand (ETZ ungleich blank) und ET-KZ = E vorhanden
     * Ja, setze den Status der V-Position auf NR
     * Nein, der Status ist weiterhin potenziell offen und die weiteren Prüfungen werden durchlaufen
     *
     * @param tempPositionVariants
     */
    private void calcDocuRelForVPositions(List<EtkDataPartListEntry> tempPositionVariants) {
        if (getDialogSeries().isVPositionCheckAndLinkingActive()) {
            Map<String, List<iPartsVirtualCalcFieldDocuRel>> codeToPartListEntryDocuRels = createVPositionGroups(tempPositionVariants);
            if (!codeToPartListEntryDocuRels.isEmpty()) {
                // Durchlaufe alle Gruppierungen
                codeToPartListEntryDocuRels.values().stream()
                        .filter(list -> list.size() > 1) // Berücksichtige nur Gruppen mit mehr als einer Position (man
                        // benötigt ja mind. eine V und eine E Position)
                        .collect(Collectors.toList()).forEach(list -> {
                            // Prüfe für jede Gruppierung, ob eine "E" Position mit nicht leeren ETZ vorhanden ist
                            boolean hasValidETKZPositionVariant = list.stream()
                                    .anyMatch(docuRelEntry -> docuRelEntry.getETKZFromPart().equals("E")
                                                              && !docuRelEntry.getFieldValue(TABLE_KATALOG, DIALOG_DD_ETZ).isEmpty());
                            if (hasValidETKZPositionVariant) {
                                // Jetzt bei allen vorhandenen V Position, die nicht schon auf NR stehen und nicht im AS sind und nicht von Hand gesetzt wurden ein "NR" setzen
                                list.stream()
                                        .filter(docuRelEntry -> docuRelEntry.getETKZFromPart().equals("V")
                                                                && (getCalcDocRelevantValueFromPartListEntry(docuRelEntry.getPartListEntry()) != iPartsDocuRelevant.DOCU_RELEVANT_NO)
                                                                && !isSetByUserOrAlreadyInAS(docuRelEntry.getPartListEntry()))
                                        .collect(Collectors.toList())
                                        .forEach(docuRelEntry -> setDocuRelevant(docuRelEntry.getPartListEntry(), iPartsDocuRelevant.DOCU_RELEVANT_NO));
                            }
                        });
            }
        }
    }

    /**
     * Gruppiert die Positionsvarianten nach gleichen POSE, POSV, AA und CODE Werten
     *
     * @param tempPositionVariants
     * @return
     */
    private Map<String, List<iPartsVirtualCalcFieldDocuRel>> createVPositionGroups(List<EtkDataPartListEntry> tempPositionVariants) {
        Map<String, List<iPartsVirtualCalcFieldDocuRel>> codeToPartListEntryDocuRels = new HashMap<>();
        // Gruppiere alle Positionen nach POSE, POSV, AA und CODE
        tempPositionVariants.forEach(position -> {
            iPartsVirtualCalcFieldDocuRel docuRel = new iPartsVirtualCalcFieldDocuRel(getProject(), position);
            String groupKey = createGroupKeyForVPositions(docuRel);
            List<iPartsVirtualCalcFieldDocuRel> docuRelEntries = codeToPartListEntryDocuRels.computeIfAbsent(groupKey, k -> new ArrayList<>());
            docuRelEntries.add(docuRel);
        });
        return codeToPartListEntryDocuRels;
    }

    /**
     * Erzeugt den Gruppenschlüssel für die Gruppierung der Positionsvarianten
     *
     * @param docuRel
     * @return
     */
    private String createGroupKeyForVPositions(iPartsVirtualCalcFieldDocuRel docuRel) {
        String posE = docuRel.getFieldValue(TABLE_KATALOG, DIALOG_DD_POSE);
        String posV = docuRel.getFieldValue(TABLE_KATALOG, DIALOG_DD_POSV);
        String aaValue = docuRel.getDDAusfuehrungsart();
        String code = docuRel.getDDCodes();
        return StrUtils.makeDelimitedString("||", posE, posV, aaValue, code);
    }

    /**
     * Kalkuliert die Doku-Relevanz für die übergebenen Positionsvarianten nach folgender Logik:
     * <p>
     * DAIMLER-8332: PVs mit Wegfall-SNR erhalten den Status "offen", falls mindestens eine PVs mit ET-Code zur
     * selben AA den Status "offen", "dokumentiert" oder "in anderem AA dokumentiert" besitzt und KEM-BIS-Datum > KEM-Stichtag ist
     *
     * @param tempPositionVariants
     * @param omittedParts
     */
    private void calcOmittedPartsAndETCodesDocuRelForVariants(List<EtkDataPartListEntry> tempPositionVariants, iPartsOmittedParts omittedParts) {
        // DAIMLER-8332: PVs mit Wegfall-SNR erhalten den Status "offen", falls mindestens eine PVs mit ET-Code zur
        // selben AA den Status offen besitzt und KEM-BIS-Datum > KEM-Stichtag ist
        Set<String> aaFromPositionsWithETCodes = new HashSet<>(); // Set mit allen Ausführungsarten, die aus Positionsvarianten extrahiert wurden, die ET-Code enthielten
        Map<String, List<EtkDataPartListEntry>> positionVariantsWithOmittedPartsForAAs = new HashMap<>(); // Map für alle Positionsvarianten mit Wegfallsachnummern
        // 1. Alle Positionsvarianten durchlaufen
        for (EtkDataPartListEntry positionVariant : tempPositionVariants) {
            // Die aktuell berechnete Doku-Relevanz bestimmen (auch nach vorherigen PV Checks)
            iPartsDocuRelevant docuRelevantPositionVariant = getCalcDocRelevantValueFromPartListEntry(positionVariant);
            iPartsVirtualCalcFieldDocuRel docuRel = new iPartsVirtualCalcFieldDocuRel(getProject(), positionVariant);
            String aaValue = docuRel.getDDAusfuehrungsart();
            // Check, ob eine Wegfallsachnummer vorhanden ist und die berechnete Doku-Relevanz nicht "offen" ist.
            // Wurde der Status manuell gesetzt oder wird die Position schon im AS verwendet, dann darf der Status
            // nicht verändert werden
            if (omittedParts.isOmittedPart(positionVariant)
                && ((docuRelevantPositionVariant != iPartsDocuRelevant.DOCU_RELEVANT_YES))
                && !isSetByUserOrAlreadyInAS(positionVariant)) {
                // KEM bis Datum der Stücklistenposition
                String kemToDate = docuRel.getDDKemToDate();
                // KEM Stichtag, der in DA_SERIES_CODES für die Ausführungsart hinterlegt wurde
                String kemDueDate = iPartsDialogSeries.getInstance(project, new iPartsSeriesId(docuRel.getDDSeries())).getKemDueDateForAA(getDDAusfuehrungsart());
                // KEM bis Datum als Long. Wenn KEM bis leer ist, dann wird das als unendlich betrachtet
                long kemToDateLong = StrUtils.isValid(kemToDate) ? StrUtils.strToLongDef(kemToDate, -1) : Long.MAX_VALUE;
                // KEM Stichtag als Long. Ist das Datum leer oder nicht vorhanden, dann wird es als minus unendlich interpretiert
                long kemDueDateLong = StrUtils.isEmpty(kemDueDate) ? Long.MIN_VALUE : StrUtils.strToLongDef(kemDueDate, -1);
                // Check, KEM bis Datum > KEM Stichtag
                if ((kemToDateLong == Long.MAX_VALUE) || (kemToDateLong > kemDueDateLong)) {
                    List<EtkDataPartListEntry> omittedPartsForAA = positionVariantsWithOmittedPartsForAAs.get(aaValue);
                    if (omittedPartsForAA == null) {
                        omittedPartsForAA = new DwList<>();
                        positionVariantsWithOmittedPartsForAAs.put(aaValue, omittedPartsForAA);
                    }
                    omittedPartsForAA.add(positionVariant);
                }
            }
            // Hier werden alle Positionsvarianten gesammelt, die ET Code enthalten und den Status "offen", "dokumentiert"
            // oder "in anderem AA dokumentiert" besitzen
            if (((docuRelevantPositionVariant == iPartsDocuRelevant.DOCU_RELEVANT_YES) ||
                 (docuRelevantPositionVariant == iPartsDocuRelevant.DOCU_DOCUMENTED) ||
                 (docuRelevantPositionVariant == iPartsDocuRelevant.DOCU_DOCUMENTED_IN_AUTHOR_ORDER))
                && docuRel.hasAccOrAsStatusCodes()) {
                aaFromPositionsWithETCodes.add(aaValue);
            }

        }
        // Falls Positionsvarianten mit Wegfallsachnummer und andere mit ET Code gefunden wurden, überprüfen, ob
        // Doku-Relevanz verändert werden soll
        if (!positionVariantsWithOmittedPartsForAAs.isEmpty() && !aaFromPositionsWithETCodes.isEmpty()) {
            // Durchlaufe alle PVs mit Wegfallsachnummern
            for (Map.Entry<String, List<EtkDataPartListEntry>> singlePVWithOmittedPartForAA : positionVariantsWithOmittedPartsForAAs.entrySet()) {
                // Ausführungsart bestimmen
                String aaValue = singlePVWithOmittedPartForAA.getKey();
                // Check, ob zu AA PVs mit ET Code gefunden wurden
                if (aaFromPositionsWithETCodes.contains(aaValue)) {
                    // Falls ja, hier die Doku-Relevanz der PVs mit Wegfallsachnummer setzen
                    // Sie ist nur dann offen, falls BAD-Code Prüfungen fehlschlagen → DAIMLER-16042
                    for (EtkDataPartListEntry positionVariantWithOmittedPart : singlePVWithOmittedPartForAA.getValue()) {
                        iPartsVirtualCalcFieldDocuRel dummyVirtualCalcFieldDocuRel =
                                new iPartsVirtualCalcFieldDocuRel(project, positionVariantWithOmittedPart);
                        if (!dummyVirtualCalcFieldDocuRel.hasEveryConjunctionValidBadCodes() &&
                            !dummyVirtualCalcFieldDocuRel.hasEveryConjunctionPermanentBadCodes()) {
                            positionVariantWithOmittedPart.setFieldValue(iPartsDataVirtualFieldsDefinition.DD_CALCULATED_DOCU_RELEVANT,
                                                                         iPartsDocuRelevant.DOCU_RELEVANT_YES.getDbValue(), DBActionOrigin.FROM_DB);
                        }
                    }
                }
            }
        }
    }

    /**
     * Kalkuliert die Doku-Relevanz für die übergebenen Positionsvarianten nach folgender Logik (altes Berechnungsmodell):
     * <p>
     * DAIMLER-5257: Als Autor möchte ich, dass in der konstruktiven DIALOG-Stückliste Teilepositionen mit einer
     * Wegfallsachnummer als nicht doku-relevant dargestellt werden, wenn innerhalb einer DIALOG Teileposition
     * keine echte Teileposition mehr übrig ist, die "doku relevant" ist.
     *
     * @param tempPositionVariants
     * @param omittedParts
     */
    private void calcOmittedPartsOrETKZFlagDocuRelForVariants(List<EtkDataPartListEntry> tempPositionVariants, iPartsOmittedParts omittedParts) {
        // Berechne die Doku-Relevanz für alle Positionsvarianten, da sie einzeln hier nicht mehr reinkommen
        for (EtkDataPartListEntry positionVariant : tempPositionVariants) {
            iPartsDocuRelevant docuRelevantCurrentEntry = getCalcDocRelevantValueFromPartListEntry(positionVariant);
            // 1. Überprüfen, ob die aktuelle Position eine Wegfallsachnummer und den Status "offen" hat und nicht
            // vom Autor gesetzt und nicht in AS übernommen wurde
            boolean isOmmitedPartWithNoRealVariants = !isSetByUserOrAlreadyInAS(positionVariant) && omittedParts.isOmittedPart(positionVariant)
                                                      && (docuRelevantCurrentEntry == iPartsDocuRelevant.DOCU_RELEVANT_YES);
            if (isOmmitedPartWithNoRealVariants) {
                // 2. Durchlaufe alle Positionsvarianten zur aktuellen Position und überprüfe, ob eine davon nicht
                // "nicht doku-relevant" ist und keine Wegfallsachnummer hat. Hier ist es nicht wichtig, ob der
                // Status von Hand auf "K" gesetzt wurde (siehe DAIMLER-5257)
                for (EtkDataPartListEntry comparePositionVariant : tempPositionVariants) {
                    if (comparePositionVariant.getAsId().equals(positionVariant.getAsId())) {
                        continue;
                    }
                    iPartsDocuRelevant docuRelevantPositionVariant = getCalcDocRelevantValueFromPartListEntry(comparePositionVariant);
                    boolean isNotDocuRelevantOrOmitted = false;
                    if (docuRelevantPositionVariant != null) {
                        isNotDocuRelevantOrOmitted = (docuRelevantPositionVariant == iPartsDocuRelevant.DOCU_RELEVANT_NO)
                                                     || omittedParts.isOmittedPart(comparePositionVariant);
                    }
                    if (!isNotDocuRelevantOrOmitted) {
                        isOmmitedPartWithNoRealVariants = false;
                        break;
                    }
                }
                // 3. Sind alle Positionsvarianten "nicht doku-relevant" oder haben eine Wegfallsachnummer, dann wird
                // bei der aktuellen Stücklistenposition der Status "K" gesetzt (nicht doku-relevant)
                if (isOmmitedPartWithNoRealVariants) {
                    positionVariant.setFieldValue(iPartsDataVirtualFieldsDefinition.DD_CALCULATED_DOCU_RELEVANT,
                                                  iPartsDocuRelevant.DOCU_RELEVANT_NO.getDbValue(), DBActionOrigin.FROM_DB);
                }
            }
        }

    }

    /**
     * Kalkuliert die Doku-Relevanz für die übergebenen Positionsvarianten nach folgender Logik (neues Berechnungsmodell):
     * <p>
     * DAIMLER-5257: Sollten bei einer DIALOG Position nur Positionsvarianten mit Wegfallsachnummern oder Sachnummern mit
     * ETKZ=K (Teilestamm) als offene Stände übrig bleiben sollen diese als not-docu-relevant gekennzeichnet
     * werden
     *
     * @param tempPositionVariants
     * @param omittedParts
     */
    private void calcOmittedPartsOrETKZFlagDocuRelForVariantsAlternate(List<EtkDataPartListEntry> tempPositionVariants,
                                                                       iPartsOmittedParts omittedParts) {
        List<EtkDataPartListEntry> docuRelEntries = new ArrayList<>();
        // Logik für das neue Berechnungsmodell:
        // "Sollten bei einer DIALOG Position nur Positionsvarianten mit Wegfallsachnummern oder Sachnummern mit
        // ETKZ=K (Teilestamm) als offene Stände übrig bleiben sollen diese als not-docu-relevant gekennzeichnet werden"
        // 1. Alle Positionsvarianten sammeln, die den Status "offen" haben
        for (EtkDataPartListEntry positionVariant : tempPositionVariants) {
            // Wenn der gesetzte Status nicht "NOT_SPECIFIED" ist, dann wurde er von Hand gesetzt, d.h. es
            // war so gewollt. Die anderen Positionsvarianten werden nicht angepasst, da sie dadurch ebenfalls
            // doku-relevant sein könnten.
            // Sonderfall: Wenn der Autor "K" von Hand gesetzt hat, dann ist die Position nicht doku-relevant und
            // soll in die Prüfung auf nicht doku-relevante Positionsvarianten einfließen (siehe DAIMLER-5257)
            if (isSetByUserOrAlreadyInAS(positionVariant) && (getCalcDocRelevantValueFromPartListEntry(positionVariant) != iPartsDocuRelevant.DOCU_RELEVANT_NO)) {
                docuRelEntries.clear();
                break;
            }
            // Hat die Positionsvariante den Status "offen"?
            if (getCalcDocRelevantValueFromPartListEntry(positionVariant) == iPartsDocuRelevant.DOCU_RELEVANT_YES) {
                docuRelEntries.add(positionVariant);
            }
        }
        if (!docuRelEntries.isEmpty()) {
            boolean allOmitted = true;
            // 2. Prüfen, ob alle "offenen" Positionsvarianten entweder eine Wegfallsachnummer oder am
            // Teilestamm ETKZ="K" haben
            for (EtkDataPartListEntry docuRelEntry : docuRelEntries) {
                if (!omittedParts.isOmittedPart(docuRelEntry)
                    && checkETKZValueForPosVariants(docuRelEntry)) {
                    allOmitted = false;
                    break;
                }
            }
            // 3. Falls alle offenen die Kriterien aus Punkt 2 erfüllen, setze bei allen "offenen"
            // Stücklistenpositionen die Doku-relevanz auf "K" (nicht doku-relevant)
            if (allOmitted) {
                for (EtkDataPartListEntry docuRelEntry : docuRelEntries) {
                    docuRelEntry.setFieldValue(iPartsDataVirtualFieldsDefinition.DD_CALCULATED_DOCU_RELEVANT,
                                               iPartsDocuRelevant.DOCU_RELEVANT_NO.getDbValue(), DBActionOrigin.FROM_DB);
                }
            }
        }
    }

    /**
     * Überprüft, ob das aktuelle ET-KZ an der übergebenen Positionsvariante dafür sorgt, dass alle offenen Positionen
     * offen bleiben oder auf "nicht relevant" gesetzt werden.
     * <p>
     * Logik:
     * Es werden alle Positionen auf "NR" gestellt, wenn sie alle
     * - ET-KZ = "K", Teil ist ein Leitungssatz-BK und sonstige-KZ ungleich "LA"
     * oder
     * - ET-KZ = "E" und ETK = "N, K, NB, KB" haben
     *
     * @param docuRelEntry
     * @return
     */
    private boolean checkETKZValueForPosVariants(EtkDataPartListEntry docuRelEntry) {
        EtkDataPart part = docuRelEntry.getPart();
        String etkzValue = part.getFieldValue(FIELD_M_ETKZ);
        // Ist ET-KZ = "K"
        if (etkzValue.equals(iPartsWireHarnessHelper.WIRE_HARNESS_ETKZ_WITH_ADDITIONAL_CHECK)) {
            // Falls ja, werden bei sonstige-KZ = "LA", wenn das Teil ein Leitungssatz-BK ist und es zur BR/AA mind ein
            // Produkt gibt bei dem Connect-Daten sichtbar sind, alle PVs auf "offen" belassen
            return iPartsWireHarnessHelper.hasValidAdditionalWireHarnessFlag(docuRelEntry)
                   && iPartsWireHarness.getInstance(getProject()).isWireHarness(part.getAsId())
                   && iPartsDataAssembly.getValidConnectAASet().contains(docuRelEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_AA));

        }
        // Ist ET-KZ = "E"
        if (etkzValue.equals(iPartsWireHarnessHelper.WIRE_HARNESS_VALID_ETKZ)) {
            // Falls ja, werden bei ETK != "N, K, NB, KB" alle PVs auf "offen" belassen
            String etkValue = docuRelEntry.getFieldValue(DIALOG_DD_ETKZ);
            return !ETK_VALUES.contains(etkValue);
        }
        // ET-KZ weder "K" noch "E" → alle PVs auf "offen" belassen
        return true;
    }

    /**
     * Berechnet die Doku-Relevanz der Einzelprüfungen für jede Positionsvariante und erzeugt optionale
     * Leitungssatz-Informations-Objekte (DAIMLER-11956) - NUR für das alte Berechnungsmodell!
     *
     * @param positionVariants
     * @return
     */
    private Optional<WireHarnessDocuRelData> calcSingleDocuRelValuesWithWireHarnessCheckForSpecialCalc(List<EtkDataPartListEntry> positionVariants) {
        // Nur relevant für das alte Berechnungsmodell
        if (!isAlternativeCalculation()) {
            WireHarnessDocuRelData wireHarnessDocuRelData = new WireHarnessDocuRelData();
            // Erst prüfen, ob die aktuelle Position Leitungssatz-BK-relevant ist
            wireHarnessDocuRelData.addEntryIfWireHarnessRelevant(this);
            for (EtkDataPartListEntry positionVariant : positionVariants) {
                iPartsVirtualCalcFieldDocuRel posV = new iPartsVirtualCalcFieldDocuRel(getProject(), positionVariant);
                posV.calculateAndSetDocuRelevant();
                // Prüfen, ob die Positionsvariante Leitungssatz-BK-relevant ist
                wireHarnessDocuRelData.addEntryIfWireHarnessRelevant(posV);
            }
            // Wurden keine relevanten Leitungssatz-BK Positionen gefunden, können auch keine im Nachgang auf
            // "offen" gesetzt werden → nichts zurückliefern
            if (wireHarnessDocuRelData.hasData()) {
                return Optional.of(wireHarnessDocuRelData);
            }
        } else {
            // Zur Sicherheit mind. das Ergebnis der Einzelprüfungen berechnen lassen
            for (EtkDataPartListEntry positionVariant : positionVariants) {
                iPartsVirtualCalcFieldDocuRel posV = new iPartsVirtualCalcFieldDocuRel(getProject(), positionVariant);
                posV.calculateAndSetDocuRelevant();
            }
        }

        return Optional.empty();
    }

    private boolean isSetByUserOrAlreadyInAS(EtkDataPartListEntry partListEntry) {
        iPartsDocuRelevant docuRel = getCalcDocRelevantValueFromPartListEntry(partListEntry);
        return (iPartsDocuRelevant.isDocumented(docuRel))
               || (getDocuRelevantFromPartListEntry(partListEntry) != iPartsDocuRelevant.DOCU_RELEVANT_NOT_SPECIFIED);
    }

    /**
     * Initiale Berechnung der Doku-Relevanz ohne spezielle Prüfungen (z.b. Positionsvarianten - siehe DAIMLER-5257)
     *
     * @param specialCalc
     * @return
     */
    private iPartsDocuRelevant checkBaseDocRelevance(boolean specialCalc, List<iPartsVirtualDocuRelStates.DocuRelFilterElement> docuRelList) {
        // Hier den Kenner zurücksetzen, da er bei der ET-KZ Einzelprüfung gesetzt wird
        isWireHarnessRelevantForOldCalcModel = false;
        boolean isRealFilter = docuRelList == null;
        // Hier wird unterschieden, ob der Aufruf aus der Filterung heraus kommt oder für den Erklärdialog der Doku-Relevanz
        // bestimmt war
        if (isRealFilter) {
            return checkBaseDocRelevanceForRealFilter(specialCalc).getResult();
        } else {
            return checkBaseDocRelevanceForDescription(specialCalc, docuRelList);
        }
    }

    /**
     * Berechnung der Doku-Relevanz samt Erklärungen für den Erklärdialog auf Basis aller Informationen an einer
     * einzelnen Stücklistenposition (ohne PVs).
     *
     * @param specialCalc
     * @param docuRelList
     * @return
     */
    private iPartsDocuRelevant checkBaseDocRelevanceForDescription(boolean specialCalc, List<DocuRelFilterElement> docuRelList) {
        iPartsDocuRelevant result = iPartsDocuRelevant.DOCU_RELEVANT_YES;
        boolean triggerWasFound = containsTriggerElement(docuRelList);

        triggerWasFound |= createAndAddDocuRelFilterElemForResult(hasEveryConjunctionPermanentBadCodes(), specialCalc, triggerWasFound,
                                                                  DOCU_REL_CONJUNCTIONS_HAS_PERMANENT_BADCODES,
                                                                  iPartsDocuRelevant.DOCU_RELEVANT_NO, docuRelList);

        triggerWasFound |= createAndAddDocuRelFilterElemForResult(hasEveryConjunctionValidBadCodes(), specialCalc, triggerWasFound,
                                                                  DOCU_REL_CONJUNCTIONS_HAS_VALID_BADCODES,
                                                                  iPartsDocuRelevant.DOCU_RELEVANT_NO, docuRelList);

        Set<String> productFactories = getProductFactories();
        // Check, ob ein Produkt mit Baumuster zur AA der Teileposition existiert. Falls nein, dann "nicht Doku-relevant" (Status: K)
        // hier werden bereits für die spätere Prüfung die productfactories aufgesammelt
        triggerWasFound |= createAndAddDocuRelFilterElemForResult(productFactories.isEmpty(), specialCalc, triggerWasFound,
                                                                  DOCU_REL_NO_MODEL_WITH_AA, iPartsDocuRelevant.DOCU_RELEVANT_NO, docuRelList);


        // ET-KZ Prüfung mit optionaler Leitungssatz-BK Prüfung
        iPartsDocuRelevant etkzCheckResult = doETKZCheck(specialCalc);
        // Check, ob der ETKZ Wert am Teilestamm einem der vorgegebenen Werte entspricht. Falls nicht, Dokurelevanz = Nicht relevant (K bzw NR)
        boolean isInvalidETKZValue = etkzCheckResult == iPartsDocuRelevant.DOCU_RELEVANT_NO;
        triggerWasFound |= createAndAddDocuRelFilterElemForResult(isInvalidETKZValue, specialCalc, triggerWasFound,
                                                                  DOCU_REL_ETKZ_FROM_PART, iPartsDocuRelevant.DOCU_RELEVANT_NO, docuRelList);

        if (!specialCalc) {
            // Check, ob es sich um einen gültigen Leitungssatz-BK handelt. Gültig ist er (Status "offen" bzw. "O"), wenn
            // folgende Kriterien erfüllt sind:
            // - ist ein Leitungssatz-BK (Sachnummer in DA_WIRE_HARNESS)
            // - hat ET-KZ = K
            // - wurde durch die ET-KZ Einzelprüfung auf "NR" gesetzt
            // - sonstige-KZ = LA
            boolean isValidWireHarness = etkzCheckResult == iPartsDocuRelevant.DOCU_RELEVANT_YES;
            triggerWasFound |= createAndAddDocuRelFilterElemForResult(isValidWireHarness, specialCalc, triggerWasFound,
                                                                      DOCU_REL_VALID_WIRE_HARNESS, iPartsDocuRelevant.DOCU_RELEVANT_YES, docuRelList);
        }

        // Check, ob eine Position ET-KZ (Teil) = "K" und Code = ";" -> Falls ja, dann "nicht Doku-relevant" (Status: K bzw. NR)
        triggerWasFound |= createAndAddDocuRelFilterElemForResult(doETPartETKZCheck(), specialCalc, triggerWasFound,
                                                                  DOCU_REL_ETKZ_FROM_PART_ET_PART, iPartsDocuRelevant.DOCU_RELEVANT_NO, docuRelList);

        // Check, ob Teil die E/E Kategorie "SW" oder "HW" + Fußnote 400 hat. Falls ja, dann "nicht Doku-relevant" (Status: K)
        boolean resultOfHWSWCheck = doEECategoryCheck();
        triggerWasFound |= createAndAddDocuRelFilterElemForResult(resultOfHWSWCheck, specialCalc, triggerWasFound, DOKU_REL_EE_CATEGORY,
                                                                  iPartsDocuRelevant.DOCU_RELEVANT_NO, docuRelList);

        // Alle Werksdaten mit PEM bis Termin unendlich oder gültig zum Ablaufdatum und AA der Baureihe
        List<iPartsFactoryData.DataForFactory> factoryData = getFactoryData();
        // Check, ob KEM_bis_Termin ≤ Vorgabedatum bei nicht offenen Werkseinsatzdaten. Falls ja, dann "nicht Doku-relevant" (Status: K)
        triggerWasFound |= createAndAddDocuRelFilterElemForResult(isKEMToBeforeSeriesKEMDueDate(factoryData).isNotRelevant(), specialCalc, triggerWasFound,
                                                                  DOKU_REL_SOP_VALUES, iPartsDocuRelevant.DOCU_RELEVANT_NO, docuRelList);

        // Check, ob ffKF (Konstruktive Federführung) = "ZB". Falls ja, dann "Doku-relevant" (Status: offen)
        triggerWasFound |= createAndAddDocuRelFilterElemForResult(isDDFEDequalZB(), specialCalc, triggerWasFound, DOKU_REL_FED,
                                                                  iPartsDocuRelevant.DOCU_RELEVANT_YES, docuRelList);

        // Check, ob die Position eine Ausführungsart hat, bei der für die aktuelle Baureihe kein Werksdatencheck
        // durchgeführt werden soll. Falls ja, dann "Doku-relevant" (Status: offen)
        triggerWasFound |= createAndAddDocuRelFilterElemForResult(hasAAForSeriesWithoutFactoryDataCheck(), specialCalc, triggerWasFound, DOKU_REL_AA_WITHOUT_FACTORY_CHECK,
                                                                  iPartsDocuRelevant.DOCU_RELEVANT_YES, docuRelList);

        // Check, ob Codebedingung mit ET- oder TZ-Code. Falls ja, dann "Doku-relevant" (Status: offen)
        triggerWasFound |= createAndAddDocuRelFilterElemForResult(hasAccOrAsStatusCodes(), specialCalc, triggerWasFound,
                                                                  DOKU_REL_HAS_ACC_OR_AS_CODES, iPartsDocuRelevant.DOCU_RELEVANT_YES, docuRelList);

        // Check, ob ein echter PEM ab Termin vorliegt. Falls nein, dann "noch nicht Doku-relevant" (Status: K*)
        // bei specialCalc: Check, ob ein echter PEM ab Termin und PEM bis Termin vorliegt und PEM ab Termin < PEM bis Termin.
        // Falls nein, dann "noch nicht Doku-relevant" (Status: K*)
        triggerWasFound |= createAndAddDocuRelFilterElemForResult(!calcDocuRelFromFactoryData(specialCalc, factoryData), specialCalc, triggerWasFound,
                                                                  DOKU_REL_FACTORY_DATA, iPartsDocuRelevant.DOCU_RELEVANT_NOT_YET, docuRelList);

        // Check, ob die PEM bis Datumsangaben aller Werksdaten älter als das SOP der Baureihe zur Ausführungsart sind (Status: K*)
        triggerWasFound |= createAndAddDocuRelFilterElemForResult(checkAllPEMToDatesBeforeSOP(factoryData), specialCalc, triggerWasFound,
                                                                  DOKU_REL_PEM_DATES_BEFORE_SOP, iPartsDocuRelevant.DOCU_RELEVANT_NOT_YET, docuRelList);

        // Check, ob Werksdaten vorhanden sind, die in der Zukunft liegen (Status: K*)
        triggerWasFound |= createAndAddDocuRelFilterElemForResult(hasFutureFactoryData(factoryData), specialCalc, triggerWasFound,
                                                                  DOKU_REL_FUTURE_FACTORY_DATA, iPartsDocuRelevant.DOCU_RELEVANT_NOT_YET, docuRelList);

        // Wurde das Flußdiagramm der Prüfungen bis zum Ende durchlaufen, wird der Status per Default auf "offen" (Doku-relevant) gesetzt
        triggerWasFound |= createAndAddDocuRelFilterElemForResult(!triggerWasFound, specialCalc, triggerWasFound, DOCU_REL_WITHOUT_DEFINITE_RESULT,
                                                                  result, docuRelList);

        if (triggerWasFound) {
            DocuRelFilterElement docuRelFilterElement = findTriggerElement(docuRelList);
            if (docuRelFilterElement != null) {
                result = docuRelFilterElement.getState();
            }
        }

        return result;
    }

    /**
     * Berechnung der Doku-Relevanz für die Verwendung beim Aufbau der Stückliste auf Basis aller Informationen an einer
     * einzelnen Stücklistenposition (ohne PVs).
     *
     * @param specialCalc
     * @return
     */
    public iPartsDocuRelBaseResult checkBaseDocRelevanceForRealFilter(boolean specialCalc) {
        // Regeln für BAD-Code Check (DAIMLER-7366):
        // - sind alle Teilkonjunktionen von dauerhaften BAD-Coden betroffen, dann wird die Teilepos ausgeblendet, außer
        // sie ist bereits im Retail verortet
        // - sind alle Teilkonjunktionen von dauerhaften BAD-Coden oder temporären BAD-Coden betroffen, dann ist die Teilepos
        // potenziell nicht doku-relevant
        // - ist mindestens ein Teilkonjunktion weder von dauerhaften BAD-Coden und temporären BAD-Coden betroffen, dann
        // ist die Teilepos potenziell doku-relevant
        if (hasEveryConjunctionPermanentBadCodes()) {
//            return iPartsDocuRelevant.DOCU_RELEVANT_NO;
            return new iPartsDocuRelBaseResult(iPartsDocuRelevant.DOCU_RELEVANT_NO, DOCU_REL_CONJUNCTIONS_HAS_PERMANENT_BADCODES);
        }

        // DAIMLER-8988 Wenn mindestens alle Teilkonjunktionen von BAD-Code getroffen werden, soll sofort der Status "nicht doku-
        // relevant" gesetzt werden und keine weiteren Prüfungen durchlaufen werden.
        if (hasEveryConjunctionValidBadCodes()) {
//            return iPartsDocuRelevant.DOCU_RELEVANT_NO;
            return new iPartsDocuRelBaseResult(iPartsDocuRelevant.DOCU_RELEVANT_NO, DOCU_REL_CONJUNCTIONS_HAS_VALID_BADCODES);
        }

        Set<String> productFactories = getProductFactories();
        // Check, ob ein Produkt mit Baumuster zur AA der Teileposition existiert. Falls nein, dann "nicht Doku-relevant" (Status: K)
        // hier werden bereits für die spätere Prüfung die productfactories aufgesammelt
        if (productFactories.isEmpty()) {
//            return iPartsDocuRelevant.DOCU_RELEVANT_NO;
            return new iPartsDocuRelBaseResult(iPartsDocuRelevant.DOCU_RELEVANT_NO, DOCU_REL_NO_MODEL_WITH_AA);
        }

        // Check, ob der ET-KZ Wert am Teilestamm einem der vorgegebenen Werte entspricht. Falls nicht, Dokurelevanz = Nicht relevant (K bzw NR)
        // und zusätzlich
        // Check, ob es sich um einen gültigen Leitungssatz-BK handelt. Gültig ist er (Status "offen"), wenn folgende Kriterien erfüllt sind:
        // - ist ein Leitungssatz-BK (Sachnummer in DA_WIRE_HARNESS)
        // - hat ET-KZ = K
        // - wurde durch die ET-KZ Einzelprüfung auf "NR" gesetzt
        // - sonstige-KZ = LA
        iPartsDocuRelevant etkzResult = doETKZCheck(specialCalc);
        // Wurde kein Doku-Relevanz Wert zurückgegeben, dann hat keine Prüfung zugeschlagen
        if (etkzResult != null) {
//            return etkzResult;
            String additionalText = TranslationHandler.translate("!!Neues Berechnungsmodell: %1, ETKZ: %2", String.valueOf(specialCalc), getETKZFromPart());
            return new iPartsDocuRelBaseResult(etkzResult, DOCU_REL_ETKZ_FROM_PART, additionalText);
        }

        // Check, ob es sich um eine Position mit ET-KZ (Teil) = "K" handelt und Code = ";" ist. Falls ja,
        // Dokurelevanz = Nicht relevant (K bzw NR)
        if (doETPartETKZCheck()) {
//            return iPartsDocuRelevant.DOCU_RELEVANT_NO;
            return new iPartsDocuRelBaseResult(iPartsDocuRelevant.DOCU_RELEVANT_NO, DOCU_REL_ETKZ_FROM_PART_ET_PART);
        }

        // Check, ob Teil die E/E Kategorie "SW" oder ("HW"+Fußnote 400) hat. Falls ja, dann "nicht Doku-relevant" (Status: K)
        if (doEECategoryCheck()) {
//            return iPartsDocuRelevant.DOCU_RELEVANT_NO;
            return new iPartsDocuRelBaseResult(iPartsDocuRelevant.DOCU_RELEVANT_NO, DOKU_REL_EE_CATEGORY);
        }
        // Alle Werksdaten mit PEM bis Termin unendlich oder gültig zum Ablaufdatum und AA der Baureihe
        List<iPartsFactoryData.DataForFactory> factoryData = getFactoryData();
        // Check, ob KEM_bis_Termin ≤ Vorgabedatum bei nicht offenen Werkseinsatzdaten. Falls ja, dann "nicht Doku-relevant" (Status: K)
        KEMToDueDateResult kemToDueDateResult = isKEMToBeforeSeriesKEMDueDate(factoryData);
        if (kemToDueDateResult.isNotRelevant()) {
//            return iPartsDocuRelevant.DOCU_RELEVANT_NO;
            return new iPartsDocuRelBaseResult(iPartsDocuRelevant.DOCU_RELEVANT_NO, DOKU_REL_SOP_VALUES);
        }

        // Check, ob ffKF (Konstruktive Federführung) = "ZB". Falls ja, dann "Doku-relevant" (Status: offen)
        if (isDDFEDequalZB()) {
//            return iPartsDocuRelevant.DOCU_RELEVANT_YES;
            return new iPartsDocuRelBaseResult(iPartsDocuRelevant.DOCU_RELEVANT_YES, DOKU_REL_FED);
        }

        // Bei speziellen Baureihen soll die Werksdaten Prüfung nicht durchgeführt werden, wenn ffKF != ZB. Stattdessen
        // soll gleich der Status "offen" zurückgegeben werden
        if (hasAAForSeriesWithoutFactoryDataCheck()) {
//            return iPartsDocuRelevant.DOCU_RELEVANT_YES;
            return new iPartsDocuRelBaseResult(iPartsDocuRelevant.DOCU_RELEVANT_YES, DOKU_REL_AA_WITHOUT_FACTORY_CHECK);
        }

        // Check, ob Codebedingung mit ET- oder TZ-Code. Falls ja, dann "Doku-relevant" (Status: offen)
        if (hasAccOrAsStatusCodes()) {
//            return iPartsDocuRelevant.DOCU_RELEVANT_YES;
            return new iPartsDocuRelBaseResult(iPartsDocuRelevant.DOCU_RELEVANT_YES, DOKU_REL_HAS_ACC_OR_AS_CODES);
        }

        // Check, ob ein echter PEM ab Termin vorliegt. Falls nein, dann "noch nicht Doku-relevant" (Status: K*)
        // bei specialCalc: Check, ob ein echter PEM ab Termin und PEM bis Termin vorliegt und PEM ab Termin < PEM bis Termin.
        // Falls nein, dann "noch nicht Doku-relevant" (Status: K*)
        if (!calcDocuRelFromFactoryData(specialCalc, factoryData)) {
//            return iPartsDocuRelevant.DOCU_RELEVANT_NOT_YET;
            return new iPartsDocuRelBaseResult(iPartsDocuRelevant.DOCU_RELEVANT_NOT_YET, DOKU_REL_FACTORY_DATA);
        }

        // Check, ob die PEM bis Datumsangaben aller Werksdaten älter als das SOP der Baureihe zur Ausführungsart sind.
        // Die prüfung wird schon in der KEM bis Prüfung durchgeführt. Falls das der Fall ist und das Ergebnis "true"
        // war, kann hier die Prüfung übersprungen und das Ergebnis direkt verwendet werden
        if (doCheckAllPEMToDatesBeforeSOP(kemToDueDateResult, factoryData)) {
//            return iPartsDocuRelevant.DOCU_RELEVANT_NOT_YET;
            return new iPartsDocuRelBaseResult(iPartsDocuRelevant.DOCU_RELEVANT_NOT_YET, DOKU_REL_SOP_VALUES);
        }

        // Check, ob Werksdaten vorhanden sind, die in der Zukunft liegen. Falls ja, soll "ANR" ausgegeben werden
        if (hasFutureFactoryData(factoryData)) {
//            return iPartsDocuRelevant.DOCU_RELEVANT_NOT_YET;
            return new iPartsDocuRelBaseResult(iPartsDocuRelevant.DOCU_RELEVANT_NOT_YET, DOKU_REL_FUTURE_FACTORY_DATA);
        }
//        return iPartsDocuRelevant.DOCU_RELEVANT_YES;
        return new iPartsDocuRelBaseResult(iPartsDocuRelevant.DOCU_RELEVANT_YES, null);
    }

    /**
     * Prüft, ob die Position den ET-KZ(Teil) Wert "K" hat. Ist das der Fall, wird geprüft, ob der Code ";" ist.
     * Wenn das auch passt, dann wird der Status auf "K" bzw "NR" gesetzt. Aber nur falls sonstige-Kenner nicht "LA" ist,
     * bei einem Leitungssatz-BK handelt
     *
     * @return
     */
    private boolean doETPartETKZCheck() {
        String etkzValue = getETKZFromPart();
        if (etkzValue.equals(iPartsWireHarnessHelper.WIRE_HARNESS_ETKZ_WITH_ADDITIONAL_CHECK) &&
            !iPartsWireHarnessHelper.isWireHarnessPartListEntryWithAdditionalWireHarnessFlag(project, getPartListEntry())) {
            String code = getCode();
            return StrUtils.isValid(code) && code.equals(ALL_VALID_CODE);
        }
        return false;
    }

    /**
     * Check, ob die PEM bis Datumsangaben aller Werksdaten älter als das SOP der Baureihe zur Ausführungsart sind.
     */
    public boolean doCheckAllPEMToDatesBeforeSOP(KEMToDueDateResult kemToDueDateResult, List<iPartsFactoryData.DataForFactory> factoryData) {
        // Wenn es kein Ergebnis einer vorherigen KEM Termin bis Prüfung gab → die PEM Termin bis Prüfung normal durchführen
        if (kemToDueDateResult == null) {
            return checkAllPEMToDatesBeforeSOP(factoryData);
        }
        // Die Prüfung wird schon in der KEM bis Prüfung durchgeführt. Falls das der Fall ist und das Ergebnis "true"
        // war, kann hier die Prüfung übersprungen und das Ergebnis direkt verwendet werden
        boolean checkAlreadyDoneAndPositive = kemToDueDateResult.isPemToDatesBeforeSOPCheckDone()
                                              && kemToDueDateResult.getPemToDatesBeforeCheckResult();
        return checkAlreadyDoneAndPositive || (!kemToDueDateResult.isPemToDatesBeforeSOPCheckDone()
                                               && checkAllPEMToDatesBeforeSOP(factoryData));
    }

    /**
     * Führt die E/E-Kategorie Prüfung durch
     * <p>
     * Logik:
     * Ist die E/E Kategorie HW und hat das Teil die Fußnote 400, soll die Teileposition in der Einzelprüfung "nicht doku-relevant" sein
     * Ist die E/E Kategorie HW und hat das Teil keine Fußnote 400, bleibt diese erstmal offen und durchläuft alle anderen Prüfungen
     * Ist die E/E Kategorie SW soll die Teileposition in der Einzelprüfung "nicht doku-relevant" sein
     *
     * @return
     */
    private boolean doEECategoryCheck() {
        iPartsDialogEECategory eeCategory = getEECategory();
        if (eeCategory.equals(iPartsDialogEECategory.SW_SNR)) {
            return true;
        } else if (eeCategory.equals(iPartsDialogEECategory.HW_SNR)) {
            return entryHasFootNote400();
        }
        return false;
    }

    private boolean entryHasFootNote400() {
        EtkDataPartListEntry entry = getPartListEntry();
        if (entry instanceof iPartsDataPartListEntry) {
            iPartsDataPartListEntry partListEntry = (iPartsDataPartListEntry)entry;
            if (!partListEntry.hasFootNotes()) {
                return false;
            }
            iPartsFootNote foundFootNote = partListEntry.getFootNotes()
                    .stream()
                    .filter(footnote -> footnote.getFootNoteId().getFootNoteId().equals("400"))
                    .findFirst()
                    .orElse(null);
            return foundFootNote != null;
        }
        return false;
    }

    /**
     * Führt die Prüfungen durch, die auf dem ET-KZ Kenner des Teilestamms basieren
     *
     * @param specialCalc
     * @return
     */
    private iPartsDocuRelevant doETKZCheck(boolean specialCalc) {
        String etkzValue = getETKZFromPart();
        // Unterscheidung ETKZ Werte auf Basis des Berechnungsmodells
        if (specialCalc) {
            if (!ETKZ_VALUES_ALT_CALC.contains(etkzValue)) {
                return iPartsDocuRelevant.DOCU_RELEVANT_NO;
            }
        } else {
            if (!ETKZ_VALUES.contains(etkzValue)) {
                // Hier den Kenner setzen, wenn es sich um einen Leitungssatz-BK handelt, der das ET-KZ = K hat und wenn die
                // eigentliche ET-KZ Prüfung zum Endergebnis "NR" geführt hätte
                isWireHarnessRelevantForOldCalcModel = etkzValue.equals(iPartsWireHarnessHelper.WIRE_HARNESS_ETKZ_WITH_ADDITIONAL_CHECK)
                                                       && iPartsWireHarness.getInstance(getProject()).isWireHarness(getFieldValue(FIELD_M_MATNR));

                // Positionen, die die oberen Kriterien erfüllen werden unter Umständen später in der Positionsvariantenprüfung
                // auf offen gesetzt. Positionen, die die oberen Kriterien erfüllen, zusätzlich sonstige-KZ = LA haben und es zur BR/AA mind ein Produkt gibt
                // bei dem Connect-Daten sichtbar sind, führen sofort zum Ergebnis "offen".
                if (isWireHarnessRelevantForOldCalcModel && iPartsWireHarnessHelper.hasValidAdditionalWireHarnessFlag(getPartListEntry())
                    && iPartsDataAssembly.getValidConnectAASet().contains(partListEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_AA))) {
                    return iPartsDocuRelevant.DOCU_RELEVANT_YES;
                }
                // Wenn die "normale" ET-KZ zuschlägt, wird ein "NR" zurückgeliefert (unabhängig von einem möglichen Leitungssatz-BK)
                return iPartsDocuRelevant.DOCU_RELEVANT_NO;
            }
        }
        return null;
    }

    /**
     * Prüft, ob die PEM bis Datumsangaben aller Werksdatensätze älter als das Start of Production Datum der Baureihe
     * zur aktuellen Ausführungsart ist
     *
     * @param factoryData
     * @return
     */
    public boolean checkAllPEMToDatesBeforeSOP(List<iPartsFactoryData.DataForFactory> factoryData) {
        String aaFromPartlistEntry = getDDAusfuehrungsart();
        if (StrUtils.isValid(aaFromPartlistEntry) && !factoryData.isEmpty()) {
            iPartsDialogSeries dialogSeries = getDialogSeries();
            String sopDate = dialogSeries.getStartOfProductionDateForAA(aaFromPartlistEntry);
            if (StrUtils.isValid(sopDate)) {
                sopDate = StrUtils.padStringWithCharsUpToLength(sopDate, '0', 14);
                for (iPartsFactoryData.DataForFactory singleFactoryData : factoryData) {
                    if (singleFactoryData.getDateToWithInfinity() == Long.MAX_VALUE) {
                        return false;
                    }
                    String pemToDate = StrUtils.padStringWithCharsUpToLength(String.valueOf(singleFactoryData.dateTo), '0', 14);
                    if (pemToDate.compareTo(sopDate) >= 0) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Check, ob Werksdaten vorhanden sind, die in der Zukunft liegen.
     *
     * @param factoryData
     * @return
     */
    public boolean hasFutureFactoryData(List<iPartsFactoryData.DataForFactory> factoryData) {
        // Keine vorhanden → weitermachen
        if (factoryData.isEmpty()) {
            return false;
        }
        // Aktuelles Datum 6 Monate in der Zukunft und ohne Millisekunden
        String checkDateTime = getFutureDateForFactoryDataCheck();
        // Alle durchlaufen und prüfen, ob das Datum weiter als 6 Monate in der Zukunft liegt
        for (iPartsFactoryData.DataForFactory singleFactoryData : factoryData) {
            String pemFromDate = String.valueOf(singleFactoryData.dateFrom);
            // Werksdaten bei denen PEM ab unendlich ist, sollen als Zukunftswerksdaten berücksichtigt werden
            if (pemFromDate.equals("0") || pemFromDate.isEmpty()) {
                continue;
            }
            String targetDate = pemFromDate;
            // Längencheck. Eigentlich sind die Datumsangaben 14 Stellen lang oder 0, weil sie beim Setzen schon geprüft werden
            if (pemFromDate.length() < 14) {
                targetDate = StrUtils.padStringWithCharsUpToLength(targetDate, '0', 14);
            } else if (targetDate.length() > 14) {
                targetDate = targetDate.substring(0, 14);
            }
            // Wenn mind. ein Werksdatum kleiner ist als das aktuelle Datum in der Zukunft, dann ist die Doku-Relevanz nicht
            // "ANR" und die nachfolgenden Prüfungen sollen laufen
            if (targetDate.compareTo(checkDateTime) <= 0) {
                return false;
            }
        }
        // Wenn alle Werksdatum durchlaufen wurden und alle "0" (unendlich) oder in der Zukunft liegen, dann ist der
        // Check positiv → Es gibt Werksdaten in der Zukunft
        return true;
    }

    /**
     * Liefert den Schwellenwert für die Zukunftsprüfung auf Basis der Werksdaten
     *
     * @return
     */
    private String getFutureDateForFactoryDataCheck() {
        // Aktuelles Datum 6 Monate in der Zukunft und ohne Millisekunden
        Calendar now = Calendar.getInstance();
        now.add(Calendar.MONTH, 6);
        return SQLStringConvert.calendarToPPDateTimeString(now);
    }

    /**
     * Liefert zurück, ob für die aktuelle Stücklistenposition der nachfolgende Werksdaten-Check durchgeführt werden soll.
     * Falls nicht, erhält die Position automatisch den Status "offen".
     *
     * @return
     */
    public boolean hasAAForSeriesWithoutFactoryDataCheck() {
        Set<String> aaWithoutFactoryDataCheck = getDialogSeries().getAAValuesWithoutFactoryDataCheck();
        if (aaWithoutFactoryDataCheck.isEmpty()) {
            return false;
        }
        return aaWithoutFactoryDataCheck.contains(getDDAusfuehrungsart());
    }

    private void addExtraInfo(iPartsVirtualDocuRelStates.DocuRelFilterElement docuRelElem) {
        StringBuilder str = new StringBuilder();
        boolean isSpecialCalc = docuRelElem.isSpecialCalc();
        switch (docuRelElem.getType()) {
            case DOCUREL_USED_IN_AS:
            case DOCUREL_USED_IN_AS_IN_OTHER_AUTHOR_ORDER:
                break;
            case DOCUREL_MODULE_HIDDEN_NOCALC:
                String msg = "!!sichtbar";
                if (docuRelElem.getState() != null) {
                    boolean isHidden = isModuleHidden();
                    boolean isNoCalc = isModuleNoCalc();
                    if (isHidden) {
                        if (isNoCalc) {
                            msg = "!!ausgeblendet und nicht berechnungsrelevant";
                        } else {
                            msg = "!!ausgeblendet";
                        }
                    } else {
                        msg = "!!nicht berechnungsrelevant";
                    }
                }
                addTo(str, "!!SM-Knoten ist %1", TranslationHandler.translate(msg));
                break;
            case DOCUREL_CALCULATION_MODEL:
                if (isSpecialCalc) {
                    addTo(str, "!!Neu");
                } else {
                    addTo(str, "!!Alt");
                }
                break;
            case DOCUREL_SET_BY_AUTHOR:
                iPartsDocuRelevant docuRel = iPartsDocuRelevant.DOCU_RELEVANT_NOT_SPECIFIED;
                if (docuRelElem.getState() != null) {
                    docuRel = docuRelElem.getState();
                }
                addTo(str, "!!Doku-relevant Wert: \"%1\"", docuRel.getDisplayValue(getProject()));
                break;
            case DOCU_REL_CONJUNCTIONS_HAS_PERMANENT_BADCODES:
                addTo(str, "!!Teilkonjunktionen der Teileposition: \"%1\"", getPartialConjunctions());
                str.append("\n");
                String permanentBadCodes = StrUtils.stringListToString(getPermanentBadCodeFromSeriesAndAA(), " ");
                addTo(str, "!!Permanente BAD-Code (gültig für AA der TP: \"%1\"): \"%2\"", getAAFromVisObject(), permanentBadCodes);
                break;
            case DOCU_REL_CONJUNCTIONS_HAS_VALID_BADCODES:
                // Check, ob die Teileposition nur aktuell gültige Bad-Code enthält. Falls ja, ist die Teileposition "nicht Doku-relevant" (Status: K),
                // Falls nein, ist sie "Doku-relevant" (Status: offen). Aber nur falls sich nicht durch nachfolgende Checks andere Werte ergeben.
                String validBadCodes = StrUtils.stringListToString(getValidBadCodeFromSeriesAndAA(), " ");
                addTo(str, "!!Aktuell gültige BAD-Code (gültig für AA der TP: \"%1\"): \"%2\"", getAAFromVisObject(), validBadCodes);
//                str.append("\n");
                break;
            case DOCU_REL_NO_MODEL_WITH_AA:
                // Check, ob ein Produkt mit Baumuster zur AA der Teileposition existiert. Falls nein, dann "nicht Doku-relevant" (Status: K)
                Set<String> factoriesForProduct = getProductFactories();
                String productFactoriesAsString = StrUtils.stringListToString(factoriesForProduct, ", ");
                addTo(str, "!!Baureihe: \"%1\"", getDDSeries());
                str.append("\n");
                addTo(str, "!!Ausführungsart: \"%1\"", getAAFromVisObject());
                str.append("\n");
                addTo(str, "!!Werke zu Produkt (mind. 1 BM mit gleicher AA): %1", productFactoriesAsString);
                break;
            case DOCU_REL_ETKZ_FROM_PART:
                addTo(str, "!!Ersatzteil-KZ (TEIL): \"%1\"", getVisETKZFromPart(getETKZFromPart()));
                if (!iPartsDataAssembly.getValidConnectAASet().contains(partListEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_AA))) {
                    str.append("\n");
                    addTo(str, "!!Leitungssatz-BK mit keinem Produkt (passend zur Baureihe und Ausführungsart), das Connect-Daten anzeigt");
                }
                break;
            case DOCU_REL_ETKZ_FROM_PART_ET_PART:
                addTo(str, "!!Ersatzteil-KZ (TEIL): \"%1\"", getVisETKZFromPart(getETKZFromPart()));
                str.append("\n");
                addTo(str, "!!Code: \"%1\"", getCode());
                if (iPartsWireHarnessHelper.isWireHarnessPartListEntryWithAdditionalWireHarnessFlag(project, getPartListEntry())) {
                    str.append("\n");
                    addTo(str, "!!sonstige-KZ = \"%1\"", iPartsWireHarnessHelper.WIRE_HARNESS_ADDITIONAL_CHECK_VALID_FLAG);
                }
                break;
            case DOCU_REL_VALID_WIRE_HARNESS:
                addValidWireHarnessInfo(str);
                break;
            case DOKU_REL_EE_CATEGORY:
                String eeCategory = getEECategory().getDBValue();
                if (eeCategory.equals(iPartsDialogEECategory.HW_SNR.getDBValue()) && entryHasFootNote400()) {
                    addTo(str, "!!E/E-Kategorie: \"%1\" und Fußnote 400", eeCategory);
                } else {
                    addTo(str, "!!E/E-Kategorie: \"%1\"", eeCategory);
                }
                break;
            case DOKU_REL_SOP_VALUES:
                factoriesForProduct = getProductFactories();
                productFactoriesAsString = StrUtils.stringListToString(factoriesForProduct, ", ");
                String visKemToDate = getProject().getVisObject().asText(TABLE_KATALOG, DIALOG_DD_SDATB, getDDKemToDate(), getProject().getDBLanguage());
                // Check, ob KEM_bis_Termin ≤ Vorgabedatum bei nicht offenen Werkseinsatzdaten. Falls ja, dann "nicht Doku-relevant" (Status: K)
                addTo(str, "!!Werke zu Produkt (mind. 1 BM mit gleicher AA): %1", productFactoriesAsString);
                str.append("\n");
                addTo(str, "!!Kem bis Termin: \"%1\"", visKemToDate);
                str.append("\n");
                List<iPartsFactoryData.DataForFactory> factoryData = getFactoryData();
                String helper = hasOpenEndedPemToDate(factoryData) ? "!!Ja" : "!!Nein";
                addTo(str, "!!Existieren \"offene\" Werkseinsatzdaten (PEM Datum bis nicht gesetzt): \"%1\"",
                      TranslationHandler.translate(helper));
                str.append("\n");
                helper = checkAllPEMToDatesBeforeSOP(factoryData) ? "!!Ja" : "!!Nein";
                addTo(str, "!!Alle PEM bis Termine < SOP: \"%1\"", TranslationHandler.translate(helper));
                str.append("\n");
                addPEMDatesToBeforeSOPMessage(str);
                str.append("\n");
                String kemDueDate = getDialogSeries().getKemDueDateForAA(getDDAusfuehrungsart());
                kemDueDate = getProject().getVisObject().asText(TABLE_KATALOG, DIALOG_DD_SDATB, kemDueDate, getProject().getDBLanguage());
                addTo(str, "!!Vorgabetermin (12 Monate vor Start of Production - Termin): \"%1\"", kemDueDate);
                break;
            case DOKU_REL_FED:
                // Check, ob ffKF (Konstruktive Federführung) = "ZB". Falls ja, dann "Doku-relevant" (Status: offen)
                String visFed = getProject().getVisObject().asText(TABLE_KATALOG, DIALOG_DD_FED, getDDFED(), getProject().getDBLanguage());
                addTo(str, "!!Federführende KF: \"%1\"", visFed);
                break;
            case DOKU_REL_AA_WITHOUT_FACTORY_CHECK:
                String allAAWithoutFactoryCheck = StrUtils.stringListToString(getDialogSeries().getAAValuesWithoutFactoryDataCheck(), ", ");
                addTo(str, "!!Markierte Ausführungsarten für die Baureihe \"%1\": %2", getDDSeries(), allAAWithoutFactoryCheck);
                str.append("\n");
                addTo(str, "!!Ausführungsart der Position: %1", getDDAusfuehrungsart());
                break;
            case DOKU_REL_HAS_ACC_OR_AS_CODES:
                // Check, ob Codebedingung mit ET- oder TZ-Code. Falls ja, dann "Doku-relevant" (Status: offen)
                String code = getCode();
                String accAsCode = StrUtils.stringListToString(iPartsAccAndAsCodeCache.getInstance(project).getConstStatusCodes(), ", ");
                addTo(str, "!!Code TP: \"%1\"", code);
                str.append("\n");
                addTo(str, "!!ACC-AS-Code \"%1\"", accAsCode);
                break;
            case DOKU_REL_FACTORY_DATA:
                // Check, ob ein echter PEM ab Termin vorliegt. Falls nein, dann "noch nicht Doku-relevant" (Status: K*)
                // bei specialCalc: Check, ob ein echter PEM ab Termin und PEM bis Termin vorliegt und PEM ab Termin < PEM bis Termin.
                String dialogGUID = getDialogGuid();
                factoriesForProduct = getProductFactories();
                productFactoriesAsString = StrUtils.stringListToString(factoriesForProduct, ", ");
                addTo(str, "!!Werkseinsatzdaten-GUID: \"%1\"", getVisGuid(dialogGUID));
                str.append("\n");
                addTo(str, "!!Werke zu Produkt (mind. 1 BM mit gleicher AA): %1", productFactoriesAsString);
                addExpDates(str);
                str.append("\n");
                addTo(str, "!!Werke samt PEM ab Datumsangaben: %1", getPEMDatesAsString(true));
                str.append("\n");
                addTo(str, "!!Werke samt PEM bis Datumsangaben: %1", getPEMDatesAsString(false));
                break;
            case DOKU_REL_STATUS_CURRENT_POSV:
                // Gesamtergebnis
                iPartsWireHarness wireHarnessCache = iPartsWireHarness.getInstance(getProject());
                iPartsOmittedParts omittedParts = iPartsOmittedParts.getInstance(getProject());
                addPosVar(str, this, docuRelElem, omittedParts, wireHarnessCache);
                break;
            case DOCU_REL_POSV_RESULTS:
                omittedParts = iPartsOmittedParts.getInstance(getProject());
                wireHarnessCache = iPartsWireHarness.getInstance(getProject());
                List<EtkDataPartListEntry> positionVariants = getPositionVariants(true);
                if ((positionVariants != null) && !positionVariants.isEmpty()) {
                    for (EtkDataPartListEntry positionVariant : positionVariants) {
                        iPartsVirtualCalcFieldDocuRel docuRelFromPositionVariant = new iPartsVirtualCalcFieldDocuRel(getProject(), positionVariant);
                        addPosVar(str, docuRelFromPositionVariant, docuRelElem, omittedParts, wireHarnessCache);
                        str.append("\n");
                    }
                } else {
                    addTo(str, "!!Keine weiteren Positionsvarianten vorhanden!");
                }
                break;
            case DOCU_REL_V_POSITION_RESULTS:
                String etkzValue = getETKZFromPart();
                if (etkzValue.equals("V")) {
                    iPartsDialogSeries dialogSeries = getDialogSeries();
                    if (dialogSeries.isVPositionCheckAndLinkingActive()) {
                        positionVariants = getPositionVariants(true);
                        if ((positionVariants != null) && !positionVariants.isEmpty()) {
                            Map<String, List<iPartsVirtualCalcFieldDocuRel>> codeToPartListEntryDocuRels = createVPositionGroups(positionVariants);
                            String groupKey = createGroupKeyForVPositions(this);
                            List<iPartsVirtualCalcFieldDocuRel> group = codeToPartListEntryDocuRels.get(groupKey);
                            if ((group != null) && !group.isEmpty()) {
                                for (iPartsVirtualCalcFieldDocuRel positionVariant : group) {
                                    addPosVarForVPosition(str, positionVariant, docuRelElem);
                                    str.append("\n");
                                }
                            } else {
                                addTo(str, "!!Keine Positionsvarianten zur V-Position vorhanden!");
                            }
                        } else {
                            addTo(str, "!!Keine Positionsvarianten vorhanden!");
                        }
                    } else {
                        addTo(str, "!!V-Position Prüfung an Baureihe deaktiviert!");
                    }
                } else {
                    addTo(str, "!!Position ist keine V-Position!");
                }
                break;
            case DOCU_REL_POSV_RESULTS_CHANGED_ORIGINAL_RESULT:
                // Einzelergebnis (ohne Positionsvarianten)
                iPartsDocuRelevant resultWithoutPVs = checkBaseDocRelevance(docuRelElem.isSpecialCalc(), null);
                addTo(str, "!!Ausführungsart: \"%1\"", getAAFromVisObject());
                str.append("\n");
                // Die Infos zum Leitungssatz-BK nur ausgeben, wenn es sich um einen Leitungsatz-BK handelt
                if (!isSpecialCalc && isWireHarnessRelevantForOldCalcModel()) {
                    addWireHarnessInfo(str, " - ");
                    str.append("\n");
                }
                addTo(str, "!!Bisheriges Ergebnis der Doku-Relevanz: \"%1\"", resultWithoutPVs.getDisplayValue(getProject()));
                str.append("\n");
                addTo(str, "!!Ergebnis Doku-Relevanz nach PV Check \"%1\"",
                      docuRelElem.getState().getDisplayValue(getProject()));
                omittedParts = iPartsOmittedParts.getInstance(getProject());
                if (omittedParts.isOmittedPart(getPartListEntry()) &&
                    (hasEveryConjunctionPermanentBadCodes() ||
                     hasEveryConjunctionValidBadCodes())) {
                    str.append("\n");
                    addTo(str, "!!Grund:");
                    str.append("\n");
                    addTo(str, "!!Wegfallsachnummer mit Status \"%1\" durch BAD-Code-Prüfung -> Status bleibt bestehen", "NR");
                }
                if (!iPartsDataAssembly.getValidConnectAASet().contains(partListEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_AA))) {
                    str.append("\n");
                    addTo(str, "!!Leitungssatz-BK mit keinem Produkt (passend zur Baureihe und Ausführungsart), das Connect-Daten anzeigt");
                }
                break;
            case DOCU_REL_CHANGE_DOCU_REL_OMITTED_PART:
                omittedParts = iPartsOmittedParts.getInstance(getProject());
                addTo(str, "!!Sonderberechnung Wegfall-SNR aktiv: %1", getYesNoText(isNodeWithChangeDocuRelOmittedPart()));
                str.append("\n");
                addTo(str, "!!Sonderberechnung aktiviert an Knoten \"%1\"", getHmMSmNodeWithOmittedPartCalcForDescription());
                str.append("\n");
                addTo(str, "!!Teil: \"%1\" - Wegfallsachnummer: %2", getFormattedPartNo(getProject().getDBLanguage()), getYesNoText(omittedParts.isOmittedPart(getPartListEntry())));
                break;
            case DOCU_REL_WITHOUT_DEFINITE_RESULT:
                addTo(str, "!!Standardwert: Doku-Relevanz \"%1\"", iPartsDocuRelevant.DOCU_RELEVANT_YES.getDisplayValue(getProject()));
                break;
            case DOKU_REL_PEM_DATES_BEFORE_SOP:
                // Check, ob die PEM bis Datumsangaben aller Werksdaten älter als das SOP der Baureihe zur Ausführungsart sind (Status: ANR)
                addPEMDatesToBeforeSOPMessage(str);
                break;
            case DOKU_REL_FUTURE_FACTORY_DATA:
                String futureDate = getProject().getVisObject().asText(TABLE_DA_SERIES_SOP, FIELD_DSP_START_OF_PROD, getFutureDateForFactoryDataCheck(), getProject().getDBLanguage());
                addTo(str, "!!Datum in der Zukunft (Schwellenwert): \"%1\"", futureDate);
                str.append("\n");
                addTo(str, "!!Werke samt PEM ab Datumsangaben: %1", getPEMDatesAsString(true));
                break;
        }
        docuRelElem.setExtraInfo(str.toString());
    }

    /**
     * Alle Informationen zu PEM bis Terminen und SOP der Werksdaten und der aktuellen Baureihe
     *
     * @param str
     */
    private void addPEMDatesToBeforeSOPMessage(StringBuilder str) {
        addTo(str, "!!Baureihe: \"%1\"", getDDSeries());
        str.append("\n");
        addTo(str, "!!Ausführungsart: \"%1\"", getAAFromVisObject());
        String seriesSOPForAA = getDialogSeries().getStartOfProductionDateForAA(getDDAusfuehrungsart());
        seriesSOPForAA = getProject().getVisObject().asText(TABLE_DA_SERIES_SOP, FIELD_DSP_START_OF_PROD, seriesSOPForAA, getProject().getDBLanguage());
        str.append("\n");
        addTo(str, "!!SOP der Baureihe: %1", seriesSOPForAA);
        str.append("\n");
        addTo(str, "!!Werke samt PEM bis Datumsangaben: %1", getPEMDatesAsString(false));
    }

    /**
     * Fügt die Auslauftermine zur Baureihe und AA hinzu, sofern welche vorhanden sind
     *
     * @param str
     */
    private void addExpDates(StringBuilder str) {
        Map<String, String> expDates = getDialogSeries().getExpirationDatesForAA(getDDAusfuehrungsart());
        if ((expDates != null) && !expDates.isEmpty()) {
            str.append("\n");
            String factoryWithExpDates = expDates.entrySet()
                    .stream()
                    .map(entry -> entry.getKey() + ": " + getProject().getVisObject().asText(TABLE_DA_SERIES_EXPDATE, FIELD_DSED_EXP_DATE, entry.getValue(), getProject().getDBLanguage()))
                    .collect(Collectors.joining(" - "));
            addTo(str, "!!Auslauftermine für Werke zur AA \"%1\": %2", getAAFromVisObject(), factoryWithExpDates);
        }
    }

    /**
     * Fügt die Leitungssatz-BK Informationen zur aktuellen Position hinzu inkl. sonstige-KZ Information
     *
     * @param str
     */
    private void addValidWireHarnessInfo(StringBuilder str) {
        addWireHarnessInfo(str, "\n");
        if (iPartsWireHarnessHelper.isWireHarnessPartListEntry(getProject(), getPartListEntry())) {
            str.append("\n");
            addTo(str, "!!sonstige-KZ = \"%1\": %2", iPartsWireHarnessHelper.WIRE_HARNESS_ADDITIONAL_CHECK_VALID_FLAG,
                  getYesNoText(getFieldValue(FIELD_M_LAYOUT_FLAG).equals(iPartsWireHarnessHelper.WIRE_HARNESS_ADDITIONAL_CHECK_VALID_FLAG)));
        }
    }

    /**
     * Fügt die Leitungssatz-BK Informationen zur aktuellen Position hinzu
     *
     * @param str
     */
    private void addWireHarnessInfo(StringBuilder str, String delimiter) {
        if (iPartsWireHarnessHelper.isWireHarnessPartListEntry(getProject(), getPartListEntry())) {
            addTo(str, "!!Leitungssatz-BK: Ja");
            str.append(delimiter);
            addTo(str, "!!ET-KZ = \"%1\": %2", iPartsWireHarnessHelper.WIRE_HARNESS_ETKZ_WITH_ADDITIONAL_CHECK,
                  getYesNoText(getETKZFromPart().equals(iPartsWireHarnessHelper.WIRE_HARNESS_ETKZ_WITH_ADDITIONAL_CHECK)));
            str.append(delimiter);
            addTo(str, "!!ET-KZ Prüfung = \"NR\": %1", getYesNoText(isWireHarnessRelevantForOldCalcModel()));
        } else {
            addTo(str, "!!Kein Leitungssatz-BK");
        }
    }

    /**
     * Liefert den nächsten HM/M/SM Knoten an dem der Kenner für die Sonderberechnung gesetzt wurde
     *
     * @return
     */
    private String getHmMSmNodeWithOmittedPartCalcForDescription() {
        HmMSmNode hmMSmNode = getHmMSmNode();
        while ((hmMSmNode != null) && !hmMSmNode.isChangeDocuRelOmittedPart()) {
            hmMSmNode = (HmMSmNode)hmMSmNode.getParent();
        }
        return (hmMSmNode != null) ? hmMSmNode.getId().toString() : "";
    }

    /**
     * Liefert einen String mit den Werken der Werksdaten samt Datumsangaben. Über <code>ísFromDate</code> kann man
     * vorgeben, ob man die PEM ab oder die PEM bis Datumswerte haben möchte.
     *
     * @param isFromDate
     * @return
     */
    private String getPEMDatesAsString(boolean isFromDate) {
        StringBuilder sb = new StringBuilder();
        List<iPartsFactoryData.DataForFactory> factoryData = getFactoryData();
        int counter = 0;
        for (iPartsFactoryData.DataForFactory singleFactoryData : factoryData) {
            String pemDate;
            boolean isInfiniteValue = isFromDate ? (singleFactoryData.dateFrom == 0) : (singleFactoryData.getDateToWithInfinity() == Long.MAX_VALUE);
            if (isInfiniteValue) {
                pemDate = TranslationHandler.translate("!!Unendlich");
            } else {
                String dateValue = isFromDate ? String.valueOf(singleFactoryData.dateFrom) : String.valueOf(singleFactoryData.dateTo);
                String fieldname = isFromDate ? FIELD_DFD_PEMTA : FIELD_DFD_PEMTB;
                pemDate = getProject().getVisObject().asText(TABLE_DA_FACTORY_DATA, fieldname, dateValue, getProject().getDBLanguage());
            }
            String text = singleFactoryData.factoryDataId.getFactory() + " - " + pemDate + "; ";
            sb.append(text);
            counter++;
            if ((counter % 5) == 0) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    private String getAAFromVisObject() {
        return getProject().getVisObject().asText(TABLE_KATALOG, DIALOG_DD_AA, getDDAusfuehrungsart(), getProject().getDBLanguage());
    }


    private void addPosVar(StringBuilder str, iPartsVirtualCalcFieldDocuRel docuRelFromPositionVariant,
                           DocuRelFilterElement docuRelElem, iPartsOmittedParts omittedParts,
                           iPartsWireHarness wireHarnessCache) {
        EtkDataPartListEntry partListEntry = docuRelFromPositionVariant.getPartListEntry();
        String lang = getProject().getDBLanguage();
        str.append(getVisGuid(partListEntry.getAsId().getKLfdnr()));
        str.append(" - ");
        addTo(str, "!!Doku-Rel: \"%1\"", docuRelFromPositionVariant.calculateDocuRelevant(docuRelElem.isSpecialCalc()).getDisplayValue(getProject()));
        str.append(" - ");
        addTo(str, "!!ETKZ (Teil): \"%1\"", getVisETKZFromPart(docuRelFromPositionVariant.getETKZFromPart()));
        str.append(" - ");
        addTo(str, "!!Sonstige-KZ (Teil): \"%1\"", partListEntry.getPart().getFieldValue(iPartsConst.FIELD_M_LAYOUT_FLAG));
        str.append(" - ");
        addTo(str, "!!ETK (DIALOG): \"%1\"", getProject().getVisObject().asText(TABLE_KATALOG, DIALOG_DD_ETKZ, docuRelFromPositionVariant.getETKFromPartlistEntry(), getProject().getDBLanguage()));
        str.append(" - ");
        addTo(str, "!!Teil: \"%1\"", docuRelFromPositionVariant.getFormattedPartNo(lang));
        str.append(" - ");
        addTo(str, "!!Hat ACC oder ET Code: \"%1\"", getYesNoText(docuRelFromPositionVariant.hasAccOrAsStatusCodes()));
        str.append(" - ");
        addTo(str, "!!Wegfallsachnummer: %1", getYesNoText(omittedParts.isOmittedPart(partListEntry)));
        str.append(" - ");
        addTo(str, "!!Leitungssatz-BK: %1", getYesNoText(wireHarnessCache.isWireHarness(partListEntry.getPart().getAsId())));

    }

    /**
     * Schreibt die Werte der Positionsvarianten für den V-Positionen-Check
     *
     * @param str
     * @param docuRelFromPositionVariant
     * @param docuRelElem
     */
    private void addPosVarForVPosition(StringBuilder str, iPartsVirtualCalcFieldDocuRel docuRelFromPositionVariant,
                                       DocuRelFilterElement docuRelElem) {
        EtkDataPartListEntry partListEntry = docuRelFromPositionVariant.getPartListEntry();
        str.append(getVisGuid(partListEntry.getAsId().getKLfdnr()));
        str.append(" - ");
        addTo(str, "!!Doku-Rel: \"%1\"", docuRelFromPositionVariant.calculateDocuRelevant(docuRelElem.isSpecialCalc()).getDisplayValue(getProject()));
        str.append(" - ");
        addTo(str, "!!ETKZ (Teil): \"%1\"", getVisETKZFromPart(docuRelFromPositionVariant.getETKZFromPart()));
        str.append(" - ");
        String posE = docuRelFromPositionVariant.getFieldValue(TABLE_KATALOG, DIALOG_DD_POSE);
        addTo(str, "!!Position: \"%1\"", posE);
        str.append(" - ");
        String posV = docuRelFromPositionVariant.getFieldValue(TABLE_KATALOG, DIALOG_DD_POSV);
        addTo(str, "!!Positionsvariante: \"%1\"", posV);
        str.append(" - ");
        String etzValue = docuRelFromPositionVariant.getFieldValue(TABLE_KATALOG, DIALOG_DD_ETZ);
        addTo(str, "!!ET-Zählerstand: \"%1\"", etzValue);
        str.append(" - ");
        String aaValue = docuRelFromPositionVariant.getAAFromVisObject();
        addTo(str, "!!Ausführungsart: \"%1\"", aaValue);
        str.append(" - ");
        String code = docuRelFromPositionVariant.getDDCodes();
        addTo(str, "!!Code-Gültigkeit: \"%1\"", code);

    }

    private String getFormattedPartNo(String lang) {
        return getProject().getVisObject().asText(TABLE_MAT, FIELD_M_BESTNR, partListEntry.getPart().getAsId().getMatNr(), lang);
    }

    private String getYesNoText(boolean result) {
        if (result) {
            return TranslationHandler.translate("!!Ja");
        } else {
            return TranslationHandler.translate("!!Nein");
        }
    }

    public String getVisGuid(String guid) {
        String lang = getProject().getDBLanguage();
        String visGuid = getProject().getVisObject().asText(TABLE_KATALOG, FIELD_K_SOURCE_GUID, guid, lang);
        if (!visGuid.equals(guid)) {
            iPartsDialogBCTEPrimaryKey bcteKey = iPartsDialogBCTEPrimaryKey.createFromDialogGuid(guid);
            if (bcteKey != null) {
                visGuid += " " + getProject().getVisObject().asText(TABLE_KATALOG, FIELD_K_DATEFROM, bcteKey.sData, lang);
            }
        } else {
            visGuid = guid;
        }
        return visGuid;
    }

    private void addTo(StringBuilder str, String key, String... placeHolderTexts) {
        str.append(TranslationHandler.translate(key, placeHolderTexts));
    }

    private void createAndAddDocuRelFilterElem(iPartsVirtualDocuRelStates type, boolean specialCalc,
                                               List<DocuRelFilterElement> docuRelList) {
        createAndAddDocuRelFilterElem(type, null, specialCalc, false, docuRelList);
    }

    private void createAndAddDocuRelFilterElem(iPartsVirtualDocuRelStates type, iPartsDocuRelevant state, boolean specialCalc,
                                               boolean trigger, List<DocuRelFilterElement> docuRelList) {
        if (docuRelList != null) {
            iPartsVirtualDocuRelStates.DocuRelFilterElement docuRelFilterElement =
                    new iPartsVirtualDocuRelStates.DocuRelFilterElement(type, state, specialCalc);
            docuRelFilterElement.setTrigger(trigger);
            docuRelList.add(docuRelFilterElement);
            addExtraInfo(docuRelFilterElement);
        }
    }

    /**
     * Liefert zurück, ob der KEM bis-Termin der Teileposition ≤ Vorgabedatum (aus Baureihe - Ausführungsart - SOP). Vor
     * dieser Prüfung muss geprüft werden, ob der KEM bis Termin überhaupt gesetzt ist und kein Werkseinsatzdatensatz
     * einen offenen Stand hat (PEM Datum bis nicht gesetzt).
     *
     * @param factoryData
     * @return
     */
    public KEMToDueDateResult isKEMToBeforeSeriesKEMDueDate(List<iPartsFactoryData.DataForFactory> factoryData) {
        KEMToDueDateResult result = new KEMToDueDateResult();
        String kemToDate = getDDKemToDate();
        // Wenn KEM bis der Teileposition leer ist, dann werden die weiteren Prüfungen zur Berechnung der Doku-Relevanz
        // angewandt.
        if (StrUtils.isEmpty(kemToDate)) {
            return result;
        }
        // Existieren "offene" Werkseinsatzdaten (PEM Datum bis nicht gesetzt), dann werden die weiteren Prüfungen zur
        // Berechnung der Doku-Relevanz angewandt.
        if (hasOpenEndedPemToDate(factoryData)) {
            return result;
        }
        // DAIMLER-14311: Wenn alle PEM bis Termin < SOP, dann Position auf "NR" setzen. Ist ein Termin dabei, der jünger
        // ist als der SOP → Prüfung überspringen
        if (!factoryData.isEmpty()) {
            boolean pemsBeforeSOPResult = checkAllPEMToDatesBeforeSOP(factoryData);
            // Ergebnis der Prüfung setzen
            result.setPemToDatesBeforeCheckResult(pemsBeforeSOPResult);
            if (!pemsBeforeSOPResult) {
                return result;
            }
        }

        long kemToDateAsLong = StrUtils.strToLongDef(kemToDate, -1);
        String aaFromPartlistEntry = getDDAusfuehrungsart();
        if ((kemToDateAsLong != -1) && StrUtils.isValid(aaFromPartlistEntry)) {
            iPartsDialogSeries dialogSeries = getDialogSeries();
            if (dialogSeries.hasKEMDueDates()) {
                String kemDueDateFromSeries = dialogSeries.getKemDueDateForAA(aaFromPartlistEntry);
                long kemDueDate = StrUtils.strToLongDef(kemDueDateFromSeries, -1);
                // Teilepositionen mit gesetztem KEM_bis_Termin ≤ Vorgabedatum (aus Baureihe - Ausführungsart) sollen
                // als "NOT_DOCU_RELEVANT" gekennzeichnet werden
                if ((kemDueDate != -1) && (kemToDateAsLong <= kemDueDate)) {
                    result.setIsNotRelevant(true);
                    return result;
                }
            }
        }
        return result;
    }

    /**
     * Liefert zurück, ob ein Werksdatensatz einen offenen PEM bis Termin hat (also gültig ist)
     *
     * @param factoryData
     * @return
     */
    private boolean hasOpenEndedPemToDate(List<iPartsFactoryData.DataForFactory> factoryData) {
        if ((partListEntry instanceof iPartsDataPartListEntry) && (factoryData != null)) {
            // Laut DAIMLER-10492 soll für die Berechnung der Doku-Relevanz nur der Datensatz mit dem höchsten ADAT+Sequenznummer
            // und dem Status "freigegeben" oder "neu" pro Werk herangezogen werden. Das wird beim Erzeugen der
            // Werksdaten (factoryData) berücksichtigt
            for (iPartsFactoryData.DataForFactory dataForFactory : factoryData) {
                if (dataForFactory != null) {
                    if (dataForFactory.getDateToWithInfinity() == Long.MAX_VALUE) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Check, ob mind. eine Teilkonjunktion einen ACC oder AS Code enthält. Es werden nur die Code geprüft, die explizit
     * für die Status-Berechnung angelegt wurden, weil diese nicht einsatzgesteuert sind. D.h für diese ergibt die Berechnung
     * sofort {@link iPartsDocuRelevant#DOCU_RELEVANT_YES} und die Werksdatenprüfung wird nicht mehr durchlaufen.
     *
     * @return
     */
    public boolean hasAccOrAsStatusCodes() {
        String codes = getDDCodes();
        if (DaimlerCodes.isEmptyCodeString(codes)) {
            return false;
        }
        Set<String> accAsCodes = iPartsAccAndAsCodeCache.getInstance(project).getConstStatusCodes();
        try {
            // Klonen der DNF findet in basicCheckCodeFilter() statt
            Disjunction dnf = DaimlerCodes.getDnfCodeOriginal(codes);
            for (Conjunction partialConjunction : dnf) {
                VarParam<Integer> matches = new VarParam<>();
                // Hier müssen die bei der Vereinfachung entfernten Terme drangehängt werden (es können ja ACC oder AS Code
                // entfernt worden sein)
                Disjunction tempDisjunction = new Disjunction(partialConjunction);
                tempDisjunction.addToRemovedTerms(dnf.getRemovedTerms());
                // DAIMLER-6898 TODO Wie bisher nur die positiven Matches zählen oder alle?
                iPartsFilterHelper.basicCheckCodeFilter(tempDisjunction, accAsCodes, null, null, null, matches);
                if (matches.getValue() > 0) {
                    return true;
                }
            }
        } catch (BooleanFunctionSyntaxException e) {
            Logger.getLogger().handleRuntimeException(e);
        }

        return false;
    }

    /**
     * @return {@code true}, wenn jede Teilkonjunktion der Coderegel des Stücklisteneintrags alle Code aus einer dauerhaften
     * BAD-Codeliste als positive Code enthält.
     */
    public boolean hasEveryConjunctionPermanentBadCodes() {
        String partlistEntryCodes = getDDCodes();
        if (DaimlerCodes.isEmptyCodeString(partlistEntryCodes)) {
            return false;
        }
        String seriesNumber = getDDSeries();
        if (StrUtils.isValid(seriesNumber)) {
            Set<String> permanentBadCodesOfSeriesAA = getPermanentBadCodeFromSeriesAndAA();
            return hasEveryConjunctionBadCodes(permanentBadCodesOfSeriesAA, partlistEntryCodes);
        }
        return false;
    }

    /**
     * @return {@code true}, wenn jede Teilkonjunktion der Coderegel des Stücklisteneintrags alle Code aus einer dauerhaften,
     * oder aktuell gültigen (aktuelles Datum ≤ Verfallsdatum der BAD-Coderegel), BAD-Codeliste als positive Code enthält.
     */
    public boolean hasEveryConjunctionValidBadCodes() {
        String partlistEntryCodes = getDDCodes();
        if (DaimlerCodes.isEmptyCodeString(partlistEntryCodes)) {
            return false;
        }
        String seriesNumber = getDDSeries();
        if (StrUtils.isValid(seriesNumber)) {
            Set<String> validBadCodesOfSeriesAA = getValidBadCodeFromSeriesAndAA();
            return hasEveryConjunctionBadCodes(validBadCodesOfSeriesAA, partlistEntryCodes);
        }
        return false;
    }

    /**
     * Überprüft, ob in jeder Teilkonjunktion der Coderegel aus mindestens einem BAD-Code Set alle Bad-Code enthalten sind.
     *
     * @param badCodes
     * @param partlistEntryCodes
     * @return
     */
    private boolean hasEveryConjunctionBadCodes(Set<String> badCodes, String partlistEntryCodes) {
        if (!badCodes.isEmpty()) {
            try {
                // Klonen der DNF findet in basicCheckCodeFilter() statt
                Disjunction dnf = DaimlerCodes.getDnfCodeOriginal(partlistEntryCodes);
                for (Conjunction partialConjunction : dnf) {
                    boolean conjunctionHasBadCodes = false;
                    for (String badCode : badCodes) {
                        Set<String> singleBadCodes = DaimlerCodes.getCodeSet(badCode);
                        int expectedCount = singleBadCodes.size();
                        VarParam<Integer> matches = new VarParam<>();
                        // DAIMLER-7366: Nur positive Matches werden gezählt
                        iPartsFilterHelper.basicCheckCodeFilter(partialConjunction, singleBadCodes, null, null, null, matches);
                        if (matches.getValue() == expectedCount) {
                            conjunctionHasBadCodes = true;
                            break;
                        }
                    }
                    if (!conjunctionHasBadCodes) {
                        return false;
                    }
                }
                return true;
            } catch (BooleanFunctionSyntaxException e) {
                Logger.getLogger().handleRuntimeException(e);
            }
        }
        return false;
    }

    /**
     * Berechnet die Doku-Relevanz in Abhängigkeit der Positionsvarianten
     * <p>
     * siehe DAIMLER-5257
     */
    public void calculateAndSetDocuRelevantWithPositionVariants() {
        EtkDataPartListEntry partListEntry = getPartListEntry();
        if (partListEntry != null) {
            boolean isPartListText = VirtualMaterialType.isPartListTextEntry(partListEntry);
            if (isPartListText) {
                setDocuRelevant(partListEntry, iPartsDocuRelevant.DOCU_RELEVANT_NO);
            } else {
                // Berechne zuerst die Doku-Relevanz für den aktuellen Stücklisteneintrag
                calculateAndSetDocuRelevant();
                if (partListEntry instanceof iPartsDataPartListEntry) {
                    calcDocRelValueForPositionVariants();
                    // Hier die abschließende Prüfung für alle Wegfallsachnummer
                    calcDocuRelForAllOmittedPartsWihtinVariants();
                }
            }
        }
    }

    /**
     * Führt die Prüfung für Wegfallsachnummern durch, sofern die Sonderberechnung aktiv ist
     *
     * @param partListEntry
     */
    private void doOmittedPartCheckForHmMSmStructure(EtkDataPartListEntry partListEntry) {
        iPartsDocuRelevant calculatedDocuRelFromEntry = getCalcDocRelevantValueFromPartListEntry(partListEntry);
        if (isChangeDocuRelForOmittedPart(partListEntry, calculatedDocuRelFromEntry)) {
            setDocuRelevant(partListEntry, iPartsDocuRelevant.DOCU_RELEVANT_YES);
        }
    }

    /**
     * Liefert zurück, ob bei der aktuellen Stücklistenposition die Sonderberechnung für Wegfallsachnummern durchgeführt
     * werden kann
     *
     * @param partListEntry
     * @param calculatedDocuRelFromEntry
     * @return
     */
    private boolean isChangeDocuRelForOmittedPart(EtkDataPartListEntry partListEntry, iPartsDocuRelevant calculatedDocuRelFromEntry) {
        iPartsDocuRelevant docuRelFromDB = getDocuRelevantFromPartListEntry(partListEntry);
        iPartsOmittedParts omittedParts = iPartsOmittedParts.getInstance(getProject());
        return (calculatedDocuRelFromEntry == iPartsDocuRelevant.DOCU_RELEVANT_NOT_YET)     // Der aktuelle Status muss "ANR" sein (Noch nicht Doku-relevant)
               && (docuRelFromDB != iPartsDocuRelevant.DOCU_RELEVANT_NOT_YET)               // Der gesetzte Status vom Autor darf nicht "ANR" sein
               && omittedParts.isOmittedPart(partListEntry.getPart().getAsId().getMatNr())  // Es muss eine Wegfallsachnummer sein
               && isNodeWithChangeDocuRelOmittedPart();                                     // Ein Knoten aus dem HM/M/SM-Strukturpfad muss für die Sonderberechnung markiert sein
    }

    /**
     * Berechnet die Doku-Relevanz auf Basis der Positionsvarianten und der übergebenen Einzel-Doku-Relevanz
     */
    private void calcDocRelValueForPositionVariants() {
        iPartsDocuRelevant result = getDocuRelevant();
        // Die Zusatzprüfungen basierend auf den Positionsvarianten dürfen nur durchgeführt werden, wenn der
        // Doku-Relevanz Wert nicht von Hand gesetzt wurde und die Position noch nicht in der AS Stückliste
        // verwendet wird.

        // Wenn die Doku-relevanz != DOCU_RELEVANT_NOT_SPECIFIED, dann wurde der Wert manuell gesetzt und müsste
        // eigentlich dafür sorgen, dass die Positionsvarianten-Checks nicht durchgeführt werden. Außer für den
        // Sonderfall, dass der Autor die Doku-Relevanz von Hand auf "K" gesetzt hat. Denn für diesen Fall sollen
        // die "offenen" Positionsvarianten mit Wegfallsachnummern ebenfalls auf "K" gesetzt werden (siehe DAIMLER-5257).
        // Wenn der schon gesetzte Doku-Relevanz-Wert "DOCUMENTED" ist, dann wurde die Position schon in AS übernommen.
        iPartsDocuRelevant calculatedDocuRel = getCalcDocRelevantValueFromPartListEntry(getPartListEntry());
        if (((result == iPartsDocuRelevant.DOCU_RELEVANT_NOT_SPECIFIED) || (result == iPartsDocuRelevant.DOCU_RELEVANT_NO))
            && (calculatedDocuRel != null)
            && (!iPartsDocuRelevant.isDocumented(calculatedDocuRel))) {
            // Die endgültige Berechnung der Doku-Relevanz ist abhängig von den Doku-Relevanz-Werten aller
            // Positionsvarianten → Hole alle Positionsvarianten und lass die Doku-Relevanz berechnen.
            List<EtkDataPartListEntry> positionVariants = getPositionVariants(true);
            if (positionVariants != null) {
                // Und nun die endgültige Berechnung der Doku-Relevanz
                calcDocRelForAllPositionVariants(positionVariants);
            }
        }
    }

    /**
     * Führt die Sonderberechnung für Wegfallsachnummern für alle Positionsvarianten durch (sofern möglich)
     */
    private void calcDocuRelForAllOmittedPartsWihtinVariants() {
        if (isNodeWithChangeDocuRelOmittedPart()) {
            List<EtkDataPartListEntry> positionVariants = getPositionVariants(false);
            if (positionVariants != null) {
                for (EtkDataPartListEntry positionVariant : positionVariants) {
                    doOmittedPartCheckForHmMSmStructure(positionVariant);
                }
            }
        }
    }

    /**
     * Liefert alle Positionsvarianten zur aktuellen Stücklistenposition
     *
     * @param removeOwnEntry
     * @return
     */
    private List<EtkDataPartListEntry> getPositionVariants(boolean removeOwnEntry) {
        if (getPartListEntry() instanceof iPartsDataPartListEntry) {
            iPartsDataAssembly assembly = ((iPartsDataPartListEntry)getPartListEntry()).getOwnerAssembly();
            return assembly.getPositionVariants(getPartListEntry(), removeOwnEntry);
        }
        return null;
    }


    /**
     * Berechnet und setzt die Doku-Relevanz für die aktuelle Stücklistenposition
     */
    private void calculateAndSetDocuRelevant() {
        if (getPartListEntry() != null) {
            iPartsDocuRelevant docuRelevant = calculateDocuRelevant(getPartListEntry());
            setDocuRelevant(getPartListEntry(), docuRelevant);
        }
    }

    private iPartsDocuRelevant calculateDocuRelevant(EtkDataPartListEntry partListEntry) {
        return calculateDocuRelevant(partListEntry, null);
    }

    public iPartsDocuRelevant calculateDocuRelevant(EtkDataPartListEntry partListEntry,
                                                    List<iPartsVirtualDocuRelStates.DocuRelFilterElement> docuRelList) {
        if (partListEntry != null) {
            return calculateDocuRelevant(isAlternativeCalculation(), docuRelList);
        }
        return null;
    }

    private String getDDCodes() {
        return getFieldValue(TABLE_KATALOG, DIALOG_DD_CODES);
    }

    private String getDDAusfuehrungsart() {
        return getFieldValue(TABLE_KATALOG, DIALOG_DD_AA);
    }

    public String getCode() {
        String code;
        try {
            // Klonen der DNF ist für getVariableNames() nicht notwendig
            code = getDDCodes();
            if (!code.equals(ALL_VALID_CODE)) {
                code = StrUtils.stringListToString(DaimlerCodes.getDnfCodeFunctionOriginal(code).getVariableNames(), ", ");
            }
        } catch (BooleanFunctionSyntaxException e) {
            code = getDDCodes();
            Logger.getLogger().handleRuntimeException(e);
        }
        return code;
    }

    private String getPartialConjunctions() {
        String partialConjunctions;
        try {
            // Klonen der DNF ist für toDaimlerString() nicht notwendig
            partialConjunctions = DaimlerCodes.getDnfCodeOriginal(getDDCodes()).toEditString();
        } catch (BooleanFunctionSyntaxException e) {
            partialConjunctions = getDDCodes();
            Logger.getLogger().handleRuntimeException(e);
        }
        return partialConjunctions;
    }

    /**
     * Berechnet das Ergebnis abhängig von den Werkseinsatzdaten der Teileposition. <>True</>, wenn die Teileposition
     * Doku-relevant ist, <>False</>, wenn nicht.
     *
     * @return
     */
    public boolean calcDocuRelFromFactoryData(boolean specialCalc, List<iPartsFactoryData.DataForFactory> factoryData) {
        if (partListEntry instanceof iPartsDataPartListEntry) {
            for (iPartsFactoryData.DataForFactory singleFactoryData : factoryData) {
                if (singleFactoryData != null) {
                    if (!specialCalc) {
                        // Check, ob ein echter PEM ab Termin vorliegt.
                        if (singleFactoryData.hasPEMFrom() && (singleFactoryData.dateFrom > 0)) {
                            return true;
                        }
                    } else {
                        // bei specialCalc: Check, ob ein echter PEM ab Termin vorliegt und PEM ab Termin < PEM bis Termin
                        // (falls PEM bis vorhanden inkl. Berücksichtigung von 0 = unendlich bei PEM bis Termin)
                        if (singleFactoryData.hasPEMFrom() && (singleFactoryData.dateFrom > 0)) {
                            if (!singleFactoryData.hasPEMTo() || (singleFactoryData.dateTo == 0)) {
                                return true;
                            } else if (singleFactoryData.dateFrom < singleFactoryData.dateTo) {
                                return true;
                            }
                        }
                    }

                }
            }
        }
        return false;
    }

    /**
     * Liefert die Werksdaten der aktuellen Position in Form einer Liste mit {@link de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsFactoryData.DataForFactory}
     *
     * @return
     */
    public List<iPartsFactoryData.DataForFactory> getFactoryData() {
        Set<String> productFactories = getProductFactories();
        List<iPartsFactoryData.DataForFactory> result = new ArrayList<>();
        if ((partListEntry instanceof iPartsDataPartListEntry) && (productFactories != null)) {
            iPartsFactoryData factoryData = ((iPartsDataPartListEntry)partListEntry).getFactoryDataForConstruction();
            iPartsDialogSeries dialogSeries = getDialogSeries();
            String aaValue = getDDAusfuehrungsart();
            boolean doExpDateCheck = dialogSeries.hasExpirationDatesForAA(aaValue);
            for (String factory : productFactories) {
                // Laut DAIMLER-10492 soll für die Berechnung der Doku-Relevanz nur der Datensatz mit dem höchsten ADAT+Sequenznummer
                // und dem Status "freigegeben" oder "neu" pro Werk herangezogen werden
                iPartsFactoryData.DataForFactory dataForFactory =
                        factoryData.getNewestDataFactoryForFactoryAndStates(factory, iPartsDataReleaseState.NEW,
                                                                            iPartsDataReleaseState.RELEASED);
                if ((dataForFactory != null)) {
                    if (!doExpDateCheck || isValidForExpDate(dataForFactory, aaValue, factory, dialogSeries)) {
                        result.add(dataForFactory);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Check, ob das PEM ab Datum der übergebenen Werksdaten kleiner oder gleich dem Auslauftermin der Baureihe zur AA
     * und dem Werk der Werksdaten ist
     * <p>
     * DAIMLER-11691: "Werkseinsatzdaten deren PEM-Ab-Termin > Auslauftermin sind, werden bei der Berechnung nicht berücksichtigt"
     *
     * @param dataForFactory
     * @param aaValue
     * @param factory
     * @param dialogSeries
     * @return
     */
    private boolean isValidForExpDate(iPartsFactoryData.DataForFactory dataForFactory, String aaValue, String factory, iPartsDialogSeries dialogSeries) {
        // Wenn keine AA vorhanden ist (sollte nicht passieren) oder kein echtes PEM ab Datum existiert, ist der Check nicht möglich → Werksdaten gültig
        if ((aaValue != null) && dataForFactory.hasPEMFrom() && (dataForFactory.dateFrom > 0)) {
            String expDate = dialogSeries.getExpirationDateForFactoryAndAA(aaValue, factory);
            // Ist kein Auslauftermin vorhanden → Werksdaten gültig
            if (StrUtils.isValid(expDate)) {
                long expDateLong = iPartsFactoryData.getFactoryDateFromDateString(expDate, "iPartsVirtualCalcFieldDocuRel.getFactoryData");
                // Ist PEM-Ab-Termin > Auslauftermin → Werksdaten nicht gültig
                return dataForFactory.dateFrom <= expDateLong;
            }
        }
        return true;
    }

    public String getDDSeries() {
        return getFieldValue(TABLE_KATALOG, DIALOG_DD_SERIES_NO);
    }

    private String getDialogGuid() {
        return getFieldValue(TABLE_KATALOG, DIALOG_DD_GUID);
    }


    private Set<String> getPermanentBadCodeFromSeriesAndAA() {
        iPartsDialogSeries series = getDialogSeries();

        Set<iPartsBadCodeId> permanentBadCode = series.getPermanentBadCodeForAA(getDDAusfuehrungsart());
        Set<String> result = new HashSet<>();

        // Sollten permanente Code existieren, dann werden diese genutzt
        for (iPartsBadCodeId badCode : permanentBadCode) {
            result.add(badCode.getCodeId());
        }

        return result;
    }

    private iPartsDialogSeries getDialogSeries() {
        return iPartsDialogSeries.getInstance(getProject(), new iPartsSeriesId(getDDSeries()));
    }

    /**
     * Liefert die validen Bad-Code zu der Baureihe und der Ausführungsart der Teileposition und dem aktuellen Datum.
     * Also dauerhafte oder aktuell gültige (aktuelles Datum ≤ Verfallsdatum der BAD-Coderegel) BAD-Code.
     *
     * @return
     */
    private Set<String> getValidBadCodeFromSeriesAndAA() {
        iPartsDialogSeries series = getDialogSeries();

        Set<iPartsBadCodeId> validBadCode = series.getBadCodesForCurrentDateAndAA(getDDAusfuehrungsart(), ERROR_MAP);
        Set<String> result = new HashSet<>();
        // Aufgelaufene Fehler ausgeben.
        if (!ERROR_MAP.isEmpty()) {
            StringBuilder stringBuilder = new StringBuilder();
            for (Map.Entry<String, String> entry : ERROR_MAP.entrySet()) {
                // Wenn der Eintrag noch nicht ausgegeben wurde, ausgeben
                if (!entry.getValue().isEmpty()) {
                    String value = entry.getValue();
                    stringBuilder.append(value);
                    stringBuilder.append(OsUtils.NEWLINE);
                    // Ungültig machen, sprich: auf schon ausgegeben setzen.
                    entry.setValue("");
                }
            }
            if (stringBuilder.length() > 0) {
                // Ohne den separaten Thread würde es zu einem Deadlock kommen, weil wir uns gerade mitten beim Laden der
                // Stückliste befinden
                Session.get().invokeThreadSafeWithThread(() -> MessageDialog.showError(stringBuilder.toString()));
            }
        }
        // Sollten valide Code existieren, dann werden diese genutzt
        for (iPartsBadCodeId badCode : validBadCode) {
            result.add(badCode.getCodeId());
        }

        return result;
    }

    /**
     * Liefert valide Werke zu allen Produkten, die über die referenzierte Baureihe der Teileposition ermittelt wurden.
     * Die Werke eines Produkts werden nur herangezogen, wenn mind. ein Baumuster des Produkts die gleiche Ausführungsart
     * hat wie die Teileposition.
     *
     * @return
     */
    public Set<String> getProductFactories() {
        return iPartsFilterHelper.getProductFactoriesForReferencedSeriesAndAA(getProject(), new iPartsSeriesId(getDDSeries()), getDDAusfuehrungsart());
    }

    private String getDDKemToDate() {
        return getFieldValue(TABLE_KATALOG, DIALOG_DD_SDATB);
    }

    public boolean isUsedInAS() {
        return getFieldValue(TABLE_KATALOG, iPartsDataVirtualFieldsDefinition.DIALOG_DD_RETAILUSE).equals(RETAIL_ASSIGNED);
    }

    /**
     * Hilfsklasse zum Berechnen der Doku-Relevanz eines Leitungssatz-BK
     */
    private static class WireHarnessDocuRelData {

        private final List<EtkDataPartListEntry> wireHarnessRelevantPositions;
        private boolean hasOpenOrDocumentedPosition; // Kenner, ob innerhalb der DIALOG Position eine offene oder dokumentierte Position vorhanden ist

        public WireHarnessDocuRelData() {
            // Liste mit Positionen bei denen folgende Kriterien erfüllt sind (DAIMLER-11956):
            // - ist ein Leitungssatz-BK (Sachnummer in DA_WIRE_HARNESS)
            // - hat ET-KZ = K
            // - wurde durch die ET-KZ Einzelprüfung auf "NR" gesetzt
            wireHarnessRelevantPositions = new DwList<>();
        }


        /**
         * Fügt die Stücklistenposition des übergebenen {@link iPartsVirtualCalcFieldDocuRel} den potenziell relevanten
         * Leitungssatz-BK hinzu inkl. Prüfung, ob die Position "offen" oder "dokumentiert" ist
         * (DAIMLER-11956)
         *
         * @param posV
         */
        public void addEntryIfWireHarnessRelevant(iPartsVirtualCalcFieldDocuRel posV) {
            EtkDataPartListEntry partListEntry = posV.getPartListEntry();
            // Check, ob die Position relevant ist (ET-KZ = K, ET-KZ Prüfung hat zu "NR" geführt und Sachnummer = Leitungssatz-BK)
            if (posV.isWireHarnessRelevantForOldCalcModel()) {
                wireHarnessRelevantPositions.add(partListEntry);
            } else if (!hasOpenOrDocumentedPosition) {
                // Handelt es sich nicht um einen für die Prüfung relevanten Leitungssatz-BK, muss geprüft werden, ob
                // die aktuelle Position "offen" oder "dokumentiert ist. Sobald eine gefunden wurde, müssen die
                // aufgesammelten Leitungssatz-BK auf "offen" gesetzt werden (DAIMLER-11956)
                iPartsDocuRelevant calculatedDocuRel = getCalcDocRelevantValueFromPartListEntry(partListEntry);
                hasOpenOrDocumentedPosition = iPartsDocuRelevant.isDocumented(calculatedDocuRel) || (calculatedDocuRel == iPartsDocuRelevant.DOCU_RELEVANT_YES);
            }
        }

        /**
         * Liefert zurück, ob es Positionen gibt, die man eventuell auf "offen" setzen müsste
         *
         * @return
         */
        public boolean hasData() {
            return !wireHarnessRelevantPositions.isEmpty();
        }

        /**
         * Setzt die relevanten Leitungssatz-BK Positionen auf "offen" sofern eine andere offene bzw dokumentierte
         * Positionsvariante gefunden wurde (DAIMLER-11956)
         */
        public void calcWireHarnessDocuRelevance() {
            if (hasOpenOrDocumentedPosition) {
                wireHarnessRelevantPositions.forEach(entry -> setDocuRelevant(entry, iPartsDocuRelevant.DOCU_RELEVANT_YES));
            }
        }
    }

    /**
     * Hilfsklasse für das Ergebnis der KEM bis Datum Prüfung einer Position
     */
    public static class KEMToDueDateResult {

        private boolean isNotRelevant; // Ist das Ergebnis der Prüfung "NR"
        private boolean pemToDatesBeforeSOPCheckDone; // Wurde die "alle PEM bis Termine < SOP" Prüfung durchgeführt
        private boolean pemToDatesBeforeCheckResult; // Ergebnis der "alle PEM bis Termine < SOP"

        public boolean isNotRelevant() {
            return isNotRelevant;
        }

        public void setIsNotRelevant(boolean result) {
            this.isNotRelevant = result;
        }

        public boolean isPemToDatesBeforeSOPCheckDone() {
            return pemToDatesBeforeSOPCheckDone;
        }

        public boolean getPemToDatesBeforeCheckResult() {
            return pemToDatesBeforeCheckResult;
        }

        public void setPemToDatesBeforeCheckResult(boolean pemToDatesBeforeCheckResult) {
            // Prüfung durchgeführt
            this.pemToDatesBeforeSOPCheckDone = true;
            this.pemToDatesBeforeCheckResult = pemToDatesBeforeCheckResult;
        }
    }

}