/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.forms;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.mechanic.listview.forms.AssemblyListForm;
import de.docware.apps.etk.base.mechanic.listview.forms.AssemblyListFormIConnector;
import de.docware.apps.etk.base.mechanic.listview.forms.PartListEntryUserObjectForTableRow;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.events.GotoPartWithPartialPathEvent;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.NavigationPath;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsDataChangedEventByEdit;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsEntrySourceType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataModuleEinPAS;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataModuleEinPASList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsNodeType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsVirtualNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.EditModuleHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsPSKHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.iPartsRetailUsageId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu.KgTuId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.controls.table.TableInterface;
import de.docware.framework.modules.gui.controls.table.TableRowInterface;
import de.docware.framework.modules.gui.session.Session;
import de.docware.util.ArrayUtil;
import de.docware.util.misc.observer.ObserverCall;
import de.docware.util.misc.observer.ObserverCallback;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * iParts-spezifische Erweiterung vom {@link AssemblyListForm}.
 */
public class iPartsAssemblyListForm extends AssemblyListForm {

    private boolean ignoreNextDataChanged;

    public iPartsAssemblyListForm(AssemblyListFormIConnector dataConnector, AbstractJavaViewerForm parentForm) {
        super(dataConnector, parentForm);

        // Eventlistener für das iPartsDataChangedEventByEdit, um auf neue/gelöschte Retail-Verwendungen von Konstruktions-
        // Stücklisteneinträgen reagieren zu können
        getProject().addAppEventListener(new ObserverCallback(getCallbackBinder(), iPartsDataChangedEventByEdit.class) {
            @Override
            public void callback(ObserverCall call) {
                if (getConnector().getCurrentAssembly() instanceof iPartsDataAssembly) {
                    iPartsDataChangedEventByEdit<iPartsRetailUsageId> dataChangedEventByEdit = (iPartsDataChangedEventByEdit)call;
                    if (dataChangedEventByEdit.getDataType() == iPartsDataChangedEventByEdit.DataType.RETAIL_USAGE) {
                        if (iPartsVirtualNode.isVirtualId(getConnector().getCurrentAssembly().getAsId())) {
                            iPartsDataAssembly iPartsAssembly = (iPartsDataAssembly)getConnector().getCurrentAssembly();
                            boolean isDialogSMConstructionAssembly = iPartsAssembly.isDialogSMConstructionAssembly();
                            boolean isEdsSAAConstructionAssembly = !isDialogSMConstructionAssembly
                                                                   && iPartsAssembly.isSaaPartsListConstructionAssembly();
                            if (isDialogSMConstructionAssembly || isEdsSAAConstructionAssembly) {
                                // Nach einem iPartsDataChangedEventByEdit mit RETAIL_USAGE kommt immer auch ein DataChangedEvent,
                                // welches in den Konstruktions-Stücklisten aber genau einmal ignoriert werden muss, damit
                                // diese sich nicht komplett neu aufbauen
                                ignoreNextDataChanged = true;

                                // Alle betroffenen Konstruktions-Stücklisteneinträge bestimmen bzgl. Verwendung im Retail
                                Set<PartListEntryId> foundRelevantPartListEntryIds = new HashSet<>();
                                Map<String, EtkDataPartListEntry> sourceGUIDToPLEMap = null;
                                for (iPartsRetailUsageId retailUsageId : dataChangedEventByEdit.getElementIds()) {
                                    iPartsEntrySourceType sourceType = iPartsEntrySourceType.getFromDbValue(retailUsageId.getType());
                                    String sourceGUID = retailUsageId.getGUID();
                                    EtkDataPartListEntry partListEntry = null;
                                    if (isDialogSMConstructionAssembly && (sourceType == iPartsEntrySourceType.DIALOG)) {
                                        if (sourceGUIDToPLEMap == null) {
                                            sourceGUIDToPLEMap = iPartsAssembly.getFieldValueToPartListEntryMap(iPartsDataVirtualFieldsDefinition.DIALOG_DD_GUID);
                                        }
                                        partListEntry = sourceGUIDToPLEMap.get(sourceGUID);
                                    } else if (isEdsSAAConstructionAssembly && (sourceType == iPartsEntrySourceType.EDS)) {
                                        if (sourceGUIDToPLEMap == null) {
                                            sourceGUIDToPLEMap = iPartsAssembly.getFieldValueToPartListEntryMap(iPartsDataVirtualFieldsDefinition.EDS_SAAGUID);
                                        }
                                        partListEntry = sourceGUIDToPLEMap.get(sourceGUID);
                                    }
                                    if (partListEntry != null) {
                                        foundRelevantPartListEntryIds.add(partListEntry.getAsId());
                                    }
                                }

                                // Betroffene Konstruktions-Stücklisteneinträge direkt in der Tabelle aktualisieren
                                if (!foundRelevantPartListEntryIds.isEmpty()) {
                                    Session.invokeThreadSafeInSession(() -> {
                                        TableInterface tableLocal = getPartListTable();
                                        if (tableLocal == null) { // Nur zur Sicherheit...
                                            return;
                                        }

                                        // Alle betroffenen virtuellen Felder und Zeilen in der Tabelle aktualisieren
                                        int foundPLEsCounter = 0;
                                        for (int rowIndex = 0; rowIndex < tableLocal.getRowCount(); rowIndex++) {
                                            TableRowInterface rowInterface = tableLocal.getRow(rowIndex);
                                            if (rowInterface != null) {
                                                PartListEntryUserObjectForTableRow pleUserObjectForTableRow = getPartListEntryUserObject(rowInterface);
                                                EtkDataPartListEntry partListEntry = pleUserObjectForTableRow.getPartListEntry();
                                                if ((partListEntry != null) && foundRelevantPartListEntryIds.contains(partListEntry.getAsId())) {
                                                    foundPLEsCounter++;
                                                    if (isDialogSMConstructionAssembly) {
                                                        partListEntry.getAttributes().deleteField(iPartsDataVirtualFieldsDefinition.DIALOG_DD_RETAILUSE,
                                                                                                  false, DBActionOrigin.FROM_DB);

                                                        // Berechnete virtuelle Felder müssen auch gelöscht werden, damit
                                                        // sie neu berechnet werden, da diese DIALOG_DD_RETAIL_USE verwenden
                                                        partListEntry.getAttributes().deleteField(iPartsDataVirtualFieldsDefinition.DD_CALCULATED_DOCU_RELEVANT,
                                                                                                  false, DBActionOrigin.FROM_DB);
                                                        partListEntry.getAttributes().deleteField(iPartsDataVirtualFieldsDefinition.DD_CALCULATED_AS_RELEVANT,
                                                                                                  false, DBActionOrigin.FROM_DB);
                                                        partListEntry.getAttributes().deleteField(iPartsDataVirtualFieldsDefinition.DD_CALCULATED_BUSINESS_CASE,
                                                                                                  false, DBActionOrigin.FROM_DB);
                                                        partListEntry.getAttributes().deleteField(iPartsDataVirtualFieldsDefinition.DIALOG_DD_WITHOUT_USAGE,
                                                                                                  false, DBActionOrigin.FROM_DB);
                                                    } else if (isEdsSAAConstructionAssembly) {
                                                        partListEntry.getAttributes().deleteField(iPartsDataVirtualFieldsDefinition.EDS_RETAIL_USE,
                                                                                                  false, DBActionOrigin.FROM_DB);
                                                    }

                                                    updateRow(rowIndex, true); // Zeile in der Tabelle aktualisieren

                                                    // Vorzeitigen Abbruch prüfen
                                                    if (foundPLEsCounter == foundRelevantPartListEntryIds.size()) {
                                                        return;
                                                    }
                                                }
                                            }
                                        }
                                    });
                                }
                            }
                        }
                    }
                }
            }
        });
    }

    @Override
    public void modifyConnectorBeforeUpdateData(AbstractJavaViewerForm sender, boolean forceUpdateAll) {
        super.modifyConnectorBeforeUpdateData(sender, forceUpdateAll);
        if (getConnector().isFlagDatabaseLanguageChanged()) {
            // Beim Ändern der DB-Sprache muss bei iParts das aktuelle Modul komplett neu geladen werden, damit sämtliche
            // Manipulationen (z.B. Dummy-Teilenummer bei Leitungssatzbaukästen) korrekt angewandt werden
            getConnector().setCurrentAssembly(EtkDataObjectFactory.createDataAssembly(getProject(), getConnector().getCurrentAssembly().getAsId()));
        }
    }

    @Override
    public void afterUpdateData(AbstractJavaViewerForm sender, boolean forceUpdateAll) {
        super.afterUpdateData(sender, forceUpdateAll);

        // Nächstes DataChangedEvent wieder normal abarbeiten
        ignoreNextDataChanged = false;
    }

    @Override
    protected List<EtkDisplayField> getDisplayFieldsForTableHelper() {
        List<EtkDisplayField> fields = super.getDisplayFieldsForTableHelper();
        // Spalten, die bei PSK Modulen nicht angezeigt werden dürfen, müssen hier entfernt werden
        iPartsPSKHelper.handlePSKDisplayFields(getCurrentAssembly(), fields);
        return fields;
    }

    @Override
    public boolean isReloadAssemblyNeeded(AbstractJavaViewerForm sender) {
        // ignoreNextDataChanged wirkt sich auf die Flags dataChanged und currentAssemblyChanged aus, welches durch den
        // Baugruppen-Baum aufgrund von dataChanged gesetzt wird
        // Ansonsten Logik analog zu super.isReloadAssemblyNeeded()

        // DataChange-Flag nur verarbeiten, wenn der Event nicht von mir selbst kommt
        if (!ignoreNextDataChanged && (sender != this) && getConnector().isFlagDataChanged()) {
            return true;
        }

        if (getConnector().isFlagCurrentAssemblyIdChanged() || (!ignoreNextDataChanged && getConnector().isFlagCurrentAssemblyChanged())
            || getConnector().isAnyLanguageChanged()) {
            return true;
        }

        return false;
    }

    @Override
    protected void gotoChildAssembly(AssemblyId assemblyId, PartListEntryId parentPartListEntry) {
        if (EditModuleHelper.isCarPerspectiveAssembly(getProject(), parentPartListEntry.getOwnerAssemblyId())) {
            // Bei CarPerspective: damit der Sprung in die Aggregate-Produkte klappt
            NavigationPath navPath = calculatePartialPathForCarPerspective(assemblyId);
            // todo Eintrag in die History (Zurück-Button) fehlt noch
            gotoRetail(navPath, assemblyId, null);
        }
        super.gotoChildAssembly(assemblyId, parentPartListEntry);
    }

    private boolean gotoRetail(NavigationPath path, AssemblyId assemblyId, PartListEntryId partListEntryId) {
        String kLfdNr = "";
        if (partListEntryId != null) {
            kLfdNr = partListEntryId.getKLfdnr();
        }
        GotoPartWithPartialPathEvent partWithPartialPathEvent = new GotoPartWithPartialPathEvent(path, assemblyId,
                                                                                                 kLfdNr, false, false,
                                                                                                 this);
        getProject().fireProjectEvent(partWithPartialPathEvent);
        return partWithPartialPathEvent.isFound();
    }

    /**
     * Berechnung eines Teil-NavigationPaths bei Sprung zu Aggregaten, damit der Sprung klappt
     *
     * @param assemblyId
     * @return
     */
    private NavigationPath calculatePartialPathForCarPerspective(AssemblyId assemblyId) {
        NavigationPath resultPath = new NavigationPath();
        // Produkt des Ziels bestimmen
        EtkDataAssembly assembly = EtkDataObjectFactory.createDataAssembly(getProject(), assemblyId);
        iPartsProductId productId = ((iPartsDataAssembly)assembly).getProductIdFromModuleUsage();
        iPartsProduct product = iPartsProduct.getInstance(getProject(), productId);
        if (product.isAggregateProduct(getProject())) {
            // Ziel ist ein Aggregate-Produkt
            // KG/TU bestimmen
            KgTuId kgTuId = calculateKgTuForAssembly(product, assemblyId);
            if (kgTuId != null) {
                // Ziel konnte sauber bestimmt werden => Teil-NavigationPath zusammenbauen
                String productNav = iPartsVirtualNode.getVirtualIdString(product, product.isStructureWithAggregates(), getProject());
                List<iPartsVirtualNode> virtualNodes = iPartsVirtualNode.parseVirtualIds(productNav);
                virtualNodes.add(new iPartsVirtualNode(iPartsNodeType.KGTU, kgTuId));
                resultPath.addAssembly(new AssemblyId(iPartsVirtualNode.getVirtualIdString(ArrayUtil.toArray(virtualNodes)), ""));
            }
        }
        resultPath.addAssembly(assemblyId);
        return resultPath;

        // ALTERNATIVE
//        MechanicUsageCalculator calculator = new MechanicUsageCalculator(getProject(), getConnector().getRootAssemblyId(),
//                                                                         assembly, true, true);
//        MechanicUsageCalculator.CalculatorResult result = null;
//        startPseudoTransactionForActiveChangeSet(true);
//        try {
//            result = calculator.calculate();
//            if ((result != null) && (result.getDirectUsagesOfSearchObject() != null) && !result.getDirectUsagesOfSearchObject().isEmpty()) {
//                AssemblyId searchAssemblyId = result.getDirectUsagesOfSearchObject().get(0).getParentAssemblyId();
//                resultPath = ModuleHierarchyNet.getShortPathToModule(getProject(), getConnector().getRootAssemblyId(), searchAssemblyId, true,
//                                                                     ModuleHierarchyNet.SearchNetType.sntMuch,
//                                                                     false);
//
//            }
//        } finally {
//            stopPseudoTransactionForActiveChangeSet();
//        }
    }

    private KgTuId calculateKgTuForAssembly(iPartsProduct product, AssemblyId assemblyId) {
        KgTuId kgTuId = null;
        // KG/TU bestimmen
        iPartsDataModuleEinPASList moduleEinPASList = iPartsDataModuleEinPASList.loadForProductAndModule(getProject(), product.getAsId(), assemblyId);
        if (!moduleEinPASList.isEmpty()) {
            iPartsDataModuleEinPAS moduleEinPAS = moduleEinPASList.get(0);
            // zur Sicherheit nochmal abfragen
            if (product.getProductStructuringType() == iPartsConst.PRODUCT_STRUCTURING_TYPE.KG_TU) {
                // Wird sind in einem KGTU Modul
                String kg = moduleEinPAS.getFieldValue(iPartsConst.FIELD_DME_SOURCE_KG);
                String tu = moduleEinPAS.getFieldValue(iPartsConst.FIELD_DME_SOURCE_TU);
                kgTuId = new KgTuId(kg, tu);
            }
        }
        return kgTuId;
    }
}
