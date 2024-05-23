/*
 * Copyright (c) 2020 Docware GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.SimpleSelectSearchResultGrid;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsRight;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets.iPartsDataReservedPK;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets.iPartsDataReservedPKList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataSa;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataSaList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsSaId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataProductSAs;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataProductSAsList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsSAId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilterHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu.KgTuId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.db.DBDataObjectAttributesList;
import de.docware.framework.modules.db.DBDataObjectList;
import de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonOnPanel;
import de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.output.j2ee.J2EEHandler;
import de.docware.framework.modules.gui.session.Session;
import de.docware.framework.utils.VarParam;
import de.docware.util.StrUtils;
import de.docware.util.misc.id.IdWithType;
import de.docware.util.sql.TableAndFieldName;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Dialog zur Auswahl einer freien SA für die noch kein TU existiert.
 * Angeboten werden alle Einträge aus DA_SA für die es keinen Eintrag in DA_SA_MODULES gibt. Zusätzlich werden alle Einträge
 * ausgefiltert für deren AssemblyId es eine Primärschlüsselreservierung in DA_RESERVED_PK gibt.
 * Der Dialog leitet vom SA-Stammdaten Dialog ab, hat aber sämtliche Edit Funktionen und weiterführende Dialoge deaktiviert.
 */
public class SelectSAForm extends MasterDataFreeSAsForm {

    public static iPartsDataSa showSASelectForCreateModule(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm) {

        SelectSAForm dlg = new SelectSAForm(dataConnector, parentForm, TABLE_DA_SA);
        dlg.setSize(800, 600); // damit der Dialog genauso klein ist wie "TU laden" oder "TU anlegen"

        prepareDisplaySortAndSearchFields(dlg, dataConnector.getProject());

        dlg.setTitle("!!Freie SAs ohne TU");
        dlg.setWindowName("FreeSAsForModuleCreate");

        dlg.enableFilterReservedAssemblys();
        dlg.setShowSasWithoutModules(true);

        ModalResult modalResult = dlg.showModal();
        if (modalResult == ModalResult.OK) {
            return dlg.getSelectedSA();
        }
        return null;
    }

    public static iPartsDataSa showSASelectForBindSaTu(AbstractJavaViewerFormIConnector dataConnector, iPartsProductId productId, KgTuId kgTuId) {
        if (productId == null || kgTuId == null) {
            return null;
        }
        SelectSAForm dlg = new SelectSAForm(dataConnector, null, TABLE_DA_SA);

        prepareDisplaySortAndSearchFields(dlg, dataConnector.getProject());

        dlg.setTitle(TranslationHandler.translate("!!SA-Module, die noch keine Zuordnung haben für Produkt: \"%1\", KG: %2",
                                                  productId.getProductNumber(), kgTuId.getKg()));
        dlg.setWindowName("SaTusForBind");

        dlg.enableFilterProductKGs(productId, kgTuId);
        dlg.setShowSasWithoutModules(false);

        ModalResult modalResult = dlg.showModal();
        if (modalResult == ModalResult.OK) {
            return dlg.getSelectedSA();
        }
        return null;
    }

    private static void prepareDisplaySortAndSearchFields(SelectSAForm dlg, EtkProject project) {
        // Suchfelder definieren
        EtkDisplayFields searchFields = MasterDataFreeSAsForm.getSearchFieldsForSaForm(project);

        // Anzeigefelder definieren
        EtkDisplayFields displayFields = MasterDataFreeSAsForm.getDisplayFieldsForSaForm(project);

        // sicherstellen dass das Feld DS_SA auf wirklich vorhanden ist, damit die spätere Logik funktioniert
        EtkDisplayField saField = displayFields.getFeldByName(TABLE_DA_SA, FIELD_DS_SA);
        if (saField == null) {
            saField = addDisplayField(TABLE_DA_SA, FIELD_DS_SA, false, false, null, project, displayFields);
            saField.setVisible(false);
        }

        // Sortierung
        LinkedHashMap<String, Boolean> sortFields = new LinkedHashMap<>();
        sortFields.put(FIELD_DS_SA, false);
        dlg.setSortFields(sortFields);
        dlg.setDisplayResultFields(displayFields);
        dlg.setSortTableByField(TableAndFieldName.make(TABLE_DA_SA, FIELD_DS_SA));
        dlg.setSearchFields(searchFields);
    }

    private Set<String> reservedAssemblys = null;
    private Set<String> sasForProductKG = null;
    private iPartsProductId productId = null;
    private KgTuId kgTuId = null;
    private boolean showSasWithoutModules;
    private final boolean hasBothCarAndTruckRights = iPartsRight.checkUserHasBothVehicleTypeRightsInSession();
    private final boolean hasNeitherCarNorTruckRights = !iPartsRight.checkCarAndVanInSession() && !iPartsRight.checkTruckAndBusInSession();

    /**
     * Erzeugt eine Instanz von SimpleMasterDataSearchResultGrid.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     *
     * @param dataConnector
     * @param parentForm
     * @param tableName
     */
    public SelectSAForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm, String tableName) {
        super(dataConnector, parentForm, tableName, null);
        setEditAllowed(false);
        setModifyAllowed(false);
        setNewAllowed(false);
        setDeleteAllowed(false);
        hideContextMenuItems();
        showToolbar(false);
        setMaxResults(J2EEHandler.isJ2EE() ? MAX_SELECT_SEARCH_RESULTS_SIZE : -1);
        getButtonPanel().setDialogStyle(GuiButtonPanel.DialogStyle.DIALOG);
        getButtonPanel().setButtonModalResult(GuiButtonOnPanel.ButtonType.OK, ModalResult.OK);
        getButtonPanel().setButtonModalResult(GuiButtonOnPanel.ButtonType.CANCEL, ModalResult.CANCEL);
    }

    private void enableFilterReservedAssemblys() {
        reservedAssemblys = new HashSet<>();
    }

    private void disableFilterReservedAssemblys() {
        reservedAssemblys = null;
    }

    private void enableFilterProductKGs(iPartsProductId productId, KgTuId kgTuId) {
        sasForProductKG = new HashSet<>();
        this.kgTuId = kgTuId;
        this.productId = productId;
    }

    private void disableFilterProductKGs() {
        sasForProductKG = null;
        this.kgTuId = null;
        this.productId = null;
    }

    private void setShowSasWithoutModules(boolean showSasWithoutModules) {
        this.showSasWithoutModules = showSasWithoutModules;
    }

    @Override
    protected synchronized void startSearch() {
        super.startSearch();
        if (reservedAssemblys != null) {
            reservedAssemblys = new HashSet<>();
            // Vor Beginn der Suche alle passenden Primärschlüsselreservierungen abfragen
            iPartsDataReservedPKList reservedPKs = new iPartsDataReservedPKList();
            reservedPKs.searchAndFillWithLike(getProject(), TABLE_DA_RESERVED_PK, null, new String[]{ FIELD_DRP_DO_TYPE, FIELD_DRP_DO_ID },
                                              new String[]{ AssemblyId.TYPE, SA_MODULE_PREFIX + "*" }, DBDataObjectList.LoadType.COMPLETE, false,
                                              DBActionOrigin.FROM_DB);

            for (iPartsDataReservedPK reservedPK : reservedPKs) {
                IdWithType objectId = reservedPK.getAsId().getAsDataObjectId();
                AssemblyId assemblyId = IdWithType.fromStringArrayWithTypeFromClass(AssemblyId.class, objectId.toStringArrayWithoutType());
                reservedAssemblys.add(assemblyId.getKVari());
            }
        }
        if ((sasForProductKG != null) && (productId != null) && (kgTuId != null)) {
            sasForProductKG = new HashSet<>();
            // Vor Beginn der Suche alle bereits zugeordneten SA für dieses Produkt, KG abfragen
            iPartsDataProductSAsList productSAs = new iPartsDataProductSAsList();
            productSAs.searchAndFill(getProject(), TABLE_DA_PRODUCT_SAS,
                                     new String[]{ FIELD_DPS_PRODUCT_NO, FIELD_DPS_KG },
                                     new String[]{ productId.getProductNumber(), kgTuId.getKg() },
                                     DBDataObjectList.LoadType.ONLY_IDS, DBActionOrigin.FROM_DB);
            for (iPartsDataProductSAs productSA : productSAs) {
                sasForProductKG.add(productSA.getAsId().getSaNumber());
            }
        }
    }

    @Override
    protected boolean executeExplicitSearch() {
        if (hasNeitherCarNorTruckRights) { // Ohne eine der beiden Benutzer-Eigenschaften brauchen wir gar nicht weitermachen
            return true;
        }

        EtkDisplayFields selectFields = getSelectFields();

        Map<EtkDisplayField, String> searchFieldsAndValuesForQuery = getSearchFieldsAndValuesForQuery(true, true);
        String[] searchFieldsArray = new String[]{};
        String[] searchValuesArray = new String[]{};
        for (Map.Entry<EtkDisplayField, String> entry : searchFieldsAndValuesForQuery.entrySet()) {
            searchFieldsArray = StrUtils.mergeArrays(searchFieldsArray, entry.getKey().getKey().getFieldName());
            searchValuesArray = StrUtils.mergeArrays(searchValuesArray, entry.getValue());
        }

        iPartsDataSaList saList = new iPartsDataSaList();
        VarParam<Integer> processedRecords = new VarParam<>(0);
        Session session = Session.get();
        EtkProject project = getProject();

        if (showSasWithoutModules) {
            // Left Outer Join von DA_SA auf DA_SA_MODULES mit Abfrage auf DSM_SA_NO is null um genau die Einträge zu finden
            // für die es einen Eintrag in DA_SA aber nicht in DA_SA_MODULES gibt.
            EtkDataObjectList.FoundAttributesCallback callback = new EtkDataObjectList.FoundAttributesCallback() {
                @Override
                public boolean foundAttributes(DBDataObjectAttributes attributes) {
                    if (checkMaxResultsExceeded(processedRecords.getValue())) {
                        return false;
                    }

                    String saModuleNumber = attributes.getFieldValue(FIELD_DSM_SA_NO);
                    if (StrUtils.isEmpty(saModuleNumber)) { // Es gibt noch kein SA-Modul
                        addFoundAttributes(attributes, session, processedRecords);
                    }

                    return false;
                }
            };

            saList.searchSortAndFillWithJoin(project, null,
                                             selectFields, searchFieldsArray, searchValuesArray,
                                             false, new String[]{ FIELD_DS_SA }, false, null, false, true, false, callback, true,
                                             new EtkDataObjectList.JoinData(TABLE_DA_SA_MODULES,
                                                                            new String[]{ FIELD_DS_SA },
                                                                            new String[]{ FIELD_DSM_SA_NO },
                                                                            true, false));

        } else {
            EtkDataObjectList.FoundAttributesCallback callback = new EtkDataObjectList.FoundAttributesCallback() {
                @Override
                public boolean foundAttributes(DBDataObjectAttributes attributes) {
                    if (checkMaxResultsExceeded(processedRecords.getValue())) {
                        return false;
                    }

                    // Sichtbarkeit der freien SA basierend auf den Benutzer-Eigenschaften prüfen mit Abkürzung hasBothCarAndTruckRights
                    if (hasBothCarAndTruckRights || iPartsFilterHelper.isSAVisibleForUserInSession(new iPartsSAId(attributes.getFieldValue(FIELD_DS_SA)),
                                                                                                   project)) {
                        addFoundAttributes(attributes, session, processedRecords);
                    }

                    return false;
                }
            };

            // Inner Join auf DA_SA_MODULES damit nur solche SAs für die es bereits einen TU gibt gefunden werden.
            saList.searchSortAndFillWithJoin(project, null,
                                             selectFields, searchFieldsArray, searchValuesArray,
                                             false, new String[]{ FIELD_DS_SA }, false, null, false, true, false, callback, true,
                                             new EtkDataObjectList.JoinData(TABLE_DA_SA_MODULES,
                                                                            new String[]{ FIELD_DS_SA },
                                                                            new String[]{ FIELD_DSM_SA_NO },
                                                                            false, false));


        }
        return true;
    }

    private void addFoundAttributes(DBDataObjectAttributes attributes, Session session, VarParam<Integer> processedRecords) {
        session.invokeThreadSafe(() -> processedRecords.setValue(processedRecords.getValue() + processResultAttributes(attributes)));
    }

    @Override
    protected boolean doValidateAttributes(DBDataObjectAttributes attributes) {
        // Bei der Auswahl von freien SAs zur Erzeugung neuer SA-TUs oder für neue Verortungen die Quelle NICHT prüfen wie
        // in der Superklasse MasterDataFreeSAsForm
        return true;
    }

    @Override
    protected SimpleSelectSearchResultGrid.GuiTableRowWithAttributes createRow(DBDataObjectAttributes attributes) {
        String saValue = attributes.getFieldValue(FIELD_DS_SA);
        // Filterung nach Primärschlüsselreservierungen
        if ((reservedAssemblys != null) && (reservedAssemblys.contains(SA_MODULE_PREFIX + saValue))) {
            return null;
        }
        // Filter nach bereits zugeordneten SAs zu Produkt, KG
        if ((sasForProductKG != null) && sasForProductKG.contains(saValue)) {
            return null;
        }
        return super.createRow(attributes);
    }

    private iPartsDataSa getSelectedSA() {
        DBDataObjectAttributes selection = getSelection();
        if (selection != null) {
            iPartsSaId saId = new iPartsSaId(selection.getFieldValue(FIELD_DS_SA));
            iPartsDataSa dataSa = new iPartsDataSa(getProject(), saId);
            dataSa.assignAttributes(getProject(), selection, false, DBActionOrigin.FROM_DB);
            return dataSa;
        }
        return null;
    }

    @Override
    protected void onTableDoubleClicked(Event event) {
        closeWithModalResult(ModalResult.OK);
    }

    @Override
    protected void enableButtons() {
        super.enableButtons();
        DBDataObjectAttributesList multiSelection = getMultiSelection();
        boolean enableOk = false;
        if (multiSelection != null && multiSelection.size() == 1) {
            enableOk = true;
        }
        getButtonPanel().getButtonOnPanel(GuiButtonOnPanel.ButtonType.OK).setEnabled(enableOk);
    }
}
