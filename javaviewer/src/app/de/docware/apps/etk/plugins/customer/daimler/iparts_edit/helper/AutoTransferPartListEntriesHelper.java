/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper;

import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.EtkMessageLogForm;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataModuleEinPASList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsTransferAssignmentValue;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsVirtualNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.ImportExportLogHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsDocuRelevant;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsRevisionChangeSet;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu.KgTuForProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu.KgTuId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu.KgTuNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu.KgTuTemplate;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.EditAutoTransferPartListEntriesExtendedForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.EditAutoTransferPartlistEntriesForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.EditTransferPartlistEntriesWithPredictionForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.transfer_to_as.RowContentForTransferToAS;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.transfer_to_as.TransferToASElement;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.misc.threads.FrameworkRunnable;
import de.docware.framework.modules.gui.misc.threads.FrameworkThread;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.utils.VarParam;
import de.docware.util.collections.dwlist.DwList;

import java.util.*;
import java.util.stream.Collectors;

public class AutoTransferPartListEntriesHelper {

    public static boolean DO_TEST_WHOLE_PRODUCT = true;  // wenn false: Bestimmung der KG/TU-Knoten der konstr. TeilePos, Ausgabe in JobLog und Abbruch

    /**
     * Datensätze zur Übernahme ermitteln und übernehmen
     *
     * @param assembly  Baugruppe
     * @param connector Connector
     */
    public static void doAutoTransferPartListEntries(EtkDataAssembly assembly, AbstractJavaViewerFormIConnector connector) {
        doAutoTransferPartListEntries(assembly, connector, false);
    }

    /**
     * Datensätze zur Übernahme ermitteln (erweitert durch KI) und übernehmen
     *
     * @param assembly  Baugruppe
     * @param connector Connector
     */
    public static void doExtendedAutoTransferPartListEntries(EtkDataAssembly assembly, AbstractJavaViewerFormIConnector connector) {
        doAutoTransferPartListEntries(assembly, connector, true);
    }

    public static void doBackgroundAutoTransferPartListEntries(AbstractJavaViewerFormIConnector connector, EtkDataAssembly assembly,
                                                               ImportExportLogHelper logHelper, iPartsProduct masterProduct, iPartsRevisionChangeSet techChangeSet) {
        doAutoTransferPartListEntries(assembly, connector, true, logHelper, masterProduct, techChangeSet);
    }

    private static void doAutoTransferPartListEntries(EtkDataAssembly assembly, AbstractJavaViewerFormIConnector connector, boolean isExtended) {
        doAutoTransferPartListEntries(assembly, connector, isExtended, null, null, null);
    }

    /**
     * Datensätze zur Übernahme ermitteln (mit und ohne KI) und übernehmen
     *
     * @param assembly   Baugruppe
     * @param connector  Connector
     * @param isExtended true: erweitert durch KI
     * @param logHelper
     * @param masterProduct
     * @param techChangeSet
     */
    private static void doAutoTransferPartListEntries(EtkDataAssembly assembly, AbstractJavaViewerFormIConnector connector, boolean isExtended,
                                                      ImportExportLogHelper logHelper, iPartsProduct masterProduct, iPartsRevisionChangeSet techChangeSet) {
        List<iPartsVirtualNode> virtualNodesPath = ((iPartsDataAssembly)assembly).getVirtualNodesPath();
        if (virtualNodesPath != null) {
            EditTransferPartlistEntriesWithPredictionForm form =
                    new EditTransferPartlistEntriesWithPredictionForm(EditTransferPartlistEntriesWithPredictionForm.Mode.DIALOG, connector,
                                                                      assembly, null, virtualNodesPath,
                                                                      EditTransferPartlistEntriesWithPredictionForm.TransferMode.PARTLIST);
            form.setMasterProduct(masterProduct);
            form.setLogHelper(logHelper);
            form.setTechChangeSet(techChangeSet);

            List<TransferToASElement> result = searchTUsForTransfer(assembly, form, isExtended, logHelper);
            if (!DO_TEST_WHOLE_PRODUCT) {
                if (logHelper != null) {
                    for (TransferToASElement elem : result) {
                        logHelper.addLogMsgWithTranslation("!!(%1) %2 %3", elem.getKgTuId().toString("/"), elem.getHotspot(),
                                                           elem.getConstPrimaryKey().toString("/", false));
                    }
                }
                result.clear();
            }
            if (!result.isEmpty()) {
                // Vorhandene Module für die TransferToASElements suchen und danach gruppieren
                Map<String, List<TransferToASElement>> moduleToTransferElementsMap = new TreeMap<>();
                Map<String, TransferToASElement> notExistingModuleMap = new TreeMap<>();
                form.collectByModules(result, moduleToTransferElementsMap, notExistingModuleMap);
                VarParam<Boolean> openModulesInEdit = new VarParam<>(false);
                if (isExtended) {
                    result = EditAutoTransferPartListEntriesExtendedForm.doAutoTransferToASPartlistExtended(connector, connector.getActiveForm(),
                                                                                                            moduleToTransferElementsMap, notExistingModuleMap,
                                                                                                            masterProduct, openModulesInEdit);
                } else {
                    result = EditAutoTransferPartlistEntriesForm.doAutoTransferToASPartlist(connector, connector.getActiveForm(),
                                                                                            moduleToTransferElementsMap, masterProduct, openModulesInEdit);
                }
                if (result != null) {
                    form.doTransfer(result, openModulesInEdit.getValue());
                }
            } else {
                if (logHelper == null) {
                    MessageDialog.show("!!Keine Positionen zur Übernahme gefunden", "!!In AS-Stückliste übernehmen");
                } else {
                    if (DO_TEST_WHOLE_PRODUCT) {
                        logHelper.addLogMsgWithTranslation("!!Keine Positionen zur Übernahme gefunden");
                    }
                }
            }
            form.dispose();
        }
    }

    /**
     * -Offene Teilepositionen aus der ganzen Stückliste ermitteln
     * -Zu diesen Teilepositionen, die Vorschläge zur Übernahme ermitteln
     * -Vorschläge zur Übernahme filtern nach Fällen bei denen eine Teilposition im Retail vorhanden ist
     * und diese die gleichen Werte für BR-HM-M-SM-POS-AA hat wie die zu übernehmende Teileposition
     * -Falls es sich um die erweiterte Übernahme handelt zusätzlich für neue, nicht referenzierte Teilepos
     * mittels KI nach Übernahmepostionen suchen
     * -Das Flag zur automatischen Übernahme setzen
     *
     * @param assembly   Konstruktions-Baugruppe
     * @param form       Das Formular der manuellen Übernahme für Funktionalitäten
     * @param isExtended ist es die erweiterte automatische Übernahme
     * @param logHelper
     * @return gefundene Positionen zur Übernahme
     */
    private static List<TransferToASElement> searchTUsForTransfer(EtkDataAssembly assembly, EditTransferPartlistEntriesWithPredictionForm form,
                                                                  boolean isExtended, ImportExportLogHelper logHelper) {
        List<TransferToASElement> result = new ArrayList<>();
        EtkMessageLogForm progressForm = null;
        boolean withMsgForm = logHelper == null;
        if (withMsgForm) {
            progressForm = new EtkMessageLogForm("!!Retail-Verwendung Suche", "!!Fortschritt", null);
        }
        FrameworkRunnable runnable = createRunnable(progressForm, logHelper, assembly, form, isExtended, result);
        if (withMsgForm) {
            progressForm.setButtonsEnabled(false);
            progressForm.setAutoClose(true);
            progressForm.setMessagesTitle("");
            progressForm.getGui().setSize(600, 250);
            progressForm.showModal(runnable);
        } else {
            runnable.run(null);
        }
        return result;
    }

    private static FrameworkRunnable createRunnable(EtkMessageLogForm progressForm, ImportExportLogHelper logHelper, EtkDataAssembly assembly,
                                                    EditTransferPartlistEntriesWithPredictionForm form,
                                                    boolean isExtended, List<TransferToASElement> result) {
        return new FrameworkRunnable() {
            @Override
            public void run(FrameworkThread thread) {
                fireMessage("!!Ermittle offene Teilepositionen");
                List<EtkDataPartListEntry> filteredSourcePartList = new DwList<>();
                // Kopie der Stückliste erzeugen aus doku-relevanten Positionen oder Positionen mit nicht spezifizierter Doku-Relevanz
                for (EtkDataPartListEntry partListEntry : assembly.getPartListUnfiltered(assembly.getEbene())) {
                    List<String> docuValues = partListEntry.getFieldValueAsSetOfEnum(iPartsDataVirtualFieldsDefinition.DD_CALCULATED_DOCU_RELEVANT);
                    iPartsDocuRelevant docuRelevant = iPartsDocuRelevant.getFromDBValue(docuValues.isEmpty() ? "" : docuValues.get(0));
                    if (docuRelevant == iPartsDocuRelevant.DOCU_RELEVANT_YES) {
                        filteredSourcePartList.add(partListEntry);
                    }
                }

                if (filteredSourcePartList.isEmpty()) {
                    fireMessage("!!Es sind keine Doku-relevanten Stücklisteneinträge oder Einträge mit nicht spezifizierter Doku-Relevanz vorhanden");
                    return;
                }
                fireMessage("!!%1 offene Teileposition/en gefunden", Integer.toString(filteredSourcePartList.size()));
                fireMessage("!!Ermittle Retail-Verwendungen");
                List<EtkDataPartListEntry> alreadyTransferredPartListEntries = EditTransferPartlistEntriesWithPredictionForm.getToASTransferredPartListEntries(assembly,
                                                                                                                                                               filteredSourcePartList);
                // Falls es keine Verwendung im Retail gibt, kann es immer noch eine im KGTU Mapping (KI) geben
                if (alreadyTransferredPartListEntries.isEmpty() && !isExtended) {
                    fireMessage("!!Keine Verwendungen im Retail gefunden");
                    return;
                }

                form.findCompleteRetailUse(filteredSourcePartList, alreadyTransferredPartListEntries);
                fireMessage("!!Ermittle Positionen zur Übernahme");
                Map<String, iPartsDataModuleEinPASList> einPASListMap = new HashMap<>();

                List<RowContentForTransferToAS> alreadyTransferredRows = form.calculateTransferListDIALOG(
                        alreadyTransferredPartListEntries, einPASListMap, null, null);
                List<RowContentForTransferToAS> initialTableRows = form.calculateTransferListDIALOG(filteredSourcePartList, einPASListMap, alreadyTransferredRows, null);

                form.updateKgTuPredictionForOmittedParts(initialTableRows);

                // Für die automatische Übernahme sind nur KGTU-Vorschläge interessant mit iPartsTransferAssignmentValue.ASSIGNED_OTHER_PV ->
                // Hier gibt es bereits Teilepositionen in Retail-Verwendung mit den gleichen Werten für BR-HM-M-SM-POS-AA
                result.addAll(initialTableRows.stream()
                                      .filter(row -> iPartsTransferAssignmentValue.isValidForAutoTransfer(row.getAssignmentValue()))
                                      .map(RowContentForTransferToAS::getTransferElement)
                                      .collect(Collectors.toList()));

                // Alle GUIDs sammeln von Teilepositionen für die es einen Übernahme-Vorschlag gibt
                // Alle Vorschläge sammeln die aus der KI stammen
                // Falls es KI Vorschläge für Teilepositionen ohne Übernahme-Vorschlag gibt, diesen KI Vorschlag noch aufnehmen
                if (isExtended) {
                    fireMessage("!!Suche über KI neue, noch nicht referenzierte Positionen zur Übernahme");
                    Set<String> selectedPartListEntriesWithTransferRowGUID = result.stream().map(TransferToASElement::getSourceGUIDForAttribute).collect(Collectors.toSet());
                    List<TransferToASElement> kiResults = initialTableRows.stream()
                            .filter(row -> row.getAssignmentValue().equals(iPartsTransferAssignmentValue.FROM_KI))
                            .map(RowContentForTransferToAS::getTransferElement).collect(Collectors.toList());

                    for (TransferToASElement kiResult : kiResults) {
                        if (!selectedPartListEntriesWithTransferRowGUID.contains(kiResult.getSourceGUIDForAttribute())) {
                            // Gibt es zum KI Vorschlag ein Template?
                            KgTuId kgTuId = kiResult.getKgTuId();
                            Map<String, KgTuTemplate> kgTuTemplates = KgTuTemplate.getInstance(kiResult.getProductId(), form.getProject());
                            List<KgTuNode> tuNodes = kgTuTemplates.values().stream()
                                    .map(kgTuTemplate -> kgTuTemplate.getTuNode(kgTuId.getKg(), kgTuId.getTu()))
                                    .filter(Objects::nonNull)
                                    .collect(Collectors.toList());
                            if (!tuNodes.isEmpty()) {
                                fireMessage("!!KI-Vorschlag %1 aus KgTuTemplate mit %2 für Produkt %3",
                                            kiResult.getSourceGUIDForAttribute(), kiResult.getKgTuId().toString("/"), kiResult.getProductId().getProductNumber());
                                result.add(kiResult);
                            } else {
                                // DAIMLER-16363: Sollte KI-Vorschlag nicht im Template vorhanden sein, überprüfe ob TU (TUNode) existiert
                                KgTuForProduct kgTuProduct = KgTuForProduct.getInstance(form.getProject(), kiResult.getProductId());
                                if (kgTuProduct.getTuNode(kgTuId.getKg(), kgTuId.getTu()) != null) {
                                    fireMessage("!!KI-Vorschlag %1 aus KgTuProdukt mit %2 für Produkt %3",
                                                kiResult.getSourceGUIDForAttribute(), kiResult.getKgTuId().toString("/"), kiResult.getProductId().getProductNumber());
                                    result.add(kiResult);
                                }
                            }
                        }
                    }
                }

                if (result.isEmpty()) {
                    fireMessage("!!Keine Positionen zur Übernahme gefunden");
                    return;
                }
                // Flag setzen, es handelt sich um die automatische Übernahme
                result.forEach(transferToASElement -> transferToASElement.setAutoTransfer(true));
                String msgText;
                if (result.size() == 1) {
                    msgText = "!!%1 zu übernehmende Position";
                } else {
                    msgText = "!!%1 zu übernehmende Positionen gefunden";
                }
                fireMessage(msgText, Integer.toString(result.size()));
            }

            public void fireMessage(String msgText, String... placeHolderTexts) {
                if (progressForm != null) {
                    progressForm.getMessageLog().fireMessage(TranslationHandler.translate(msgText, placeHolderTexts));
                } else if (logHelper != null) {
                    logHelper.addLogMsgWithTranslation(msgText, placeHolderTexts);
                }
            }
        };
    }
}
