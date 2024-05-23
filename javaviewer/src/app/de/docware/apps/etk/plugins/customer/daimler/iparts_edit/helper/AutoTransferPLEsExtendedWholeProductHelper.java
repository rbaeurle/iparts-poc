/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper;

import de.docware.apps.etk.base.mechanic.mainview.connector.MechanicFormConnector;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.base.ExtendedDataTypeLoadType;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets.iPartsChangeSetSource;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsAssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsVirtualNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.ImportExportLogHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsRevisionChangeSet;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.HmMSmId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.db.DBDataObjectAttributesList;
import de.docware.framework.modules.gui.session.Session;
import de.docware.framework.utils.SortType;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class AutoTransferPLEsExtendedWholeProductHelper {

    public static boolean TEST_SINGLE_SUBMODULE_WITH_ROLLBACK = false;  // wenn true: kompletter Durchlauf, jedoch am Ende ein Rollback

    private Session session;
    private EtkProject project;
    private ImportExportLogHelper logHelper;

    public AutoTransferPLEsExtendedWholeProductHelper(Session session, EtkProject project, ImportExportLogHelper logHelper) {
        this.session = session;
        this.project = project;
        this.logHelper = logHelper;
    }

    public boolean transferPLEsExtendedWholeProduct(iPartsProduct product) {
        // hole referenzierte Baureihe aus currentProduct
        // hole alle HmMSm zur Baureihe
        Set<HmMSmId> hmMSmIds = getHmMSmNodesFromDialog(product);

        // Nur für Testzwecke falls AutoTransferPartListEntriesHelper.DO_TEST_WHOLE_PRODUCT = false
        HmMSmId testHmMSmId = new HmMSmId("C213", "08", "04", "04");
        HmMSmId testScndHmMSmId = new HmMSmId("C213", "08", "04", "06");

        iPartsRevisionChangeSet techChangeSet = iPartsRevisionChangeSet.createTempChangeSet(project, iPartsChangeSetSource.PRODUCT);

        // über alle HmMSm-Knoten und damit SubModule
        for (HmMSmId hmMSmId : hmMSmIds) {
            if (session.isActive() && !Thread.currentThread().isInterrupted()) {
                if (!AutoTransferPartListEntriesHelper.DO_TEST_WHOLE_PRODUCT) {
                    // Test nur mit 2 HmMSm-Knoten
                    if (hmMSmId.equals(testHmMSmId) || hmMSmId.equals(testScndHmMSmId)) {
                        // lade Konstr.StüLi (SubModul)
                        if (!handleOneConstPartList(hmMSmId, product, techChangeSet)) {
                            return false;
                        }
                    }
                } else {
                    // lade Konstr.StüLi (SubModul)
                    if (!handleOneConstPartList(hmMSmId, product, techChangeSet)) {
                        return false;
                    }
                }
            } else {
                logHelper.addLogErrorWithTranslation("!!Ausführung im Hintergrund wurde abgebrochen.");
                return false;
            }
        }
        try {
            if (!techChangeSet.isEmpty()) {
                // Produkt-Zeitstempel aktualisieren
                iPartsDataProduct dataProduct = new iPartsDataProduct(project, product.getAsId());
                if (dataProduct.existsInDB()) {
                    dataProduct.refreshModificationTimeStamp();
                    techChangeSet.addDataObject(dataProduct, false, false, false);
                    dataProduct.saveToDB();
                }

                if (!techChangeSet.commit(false, true, true, null, null)) {
                    // Fehlermeldung
                    logHelper.addLogErrorWithTranslation("!!Fehler beim Speichern des technischen ChangeSets");
                    return false;
                }
            }
        } catch (Exception e) {
            // Fehlermeldung
            logHelper.addLogError(e.getMessage());
            return false;
        }
        return true;
    }

    private boolean handleOneConstPartList(HmMSmId hmMSmId, iPartsProduct product, iPartsRevisionChangeSet techChangeSet) {
        logHelper.addLogMsgWithTranslation("!!Bearbeite Submodul %1...", hmMSmId.toString("/"));
        String virtualIdString = iPartsVirtualNode.getVirtualIdString(hmMSmId);
        iPartsAssemblyId assemblyId = new iPartsAssemblyId(virtualIdString, "");
        List<iPartsVirtualNode> nodes = iPartsVirtualNode.parseVirtualIds(virtualIdString);
        EtkDataAssembly assembly = EtkDataObjectFactory.createDataAssembly(project, assemblyId);
        if ((nodes != null) && assembly.existsInDB()) {
            MechanicFormConnector connector = new MechanicFormConnector(null) {
                @Override
                public EtkProject getProject() {
                    return project;
                }
            };
            connector.setCurrentAssembly(assembly);
            AutoTransferPartListEntriesHelper.doBackgroundAutoTransferPartListEntries(connector, assembly, logHelper, product, techChangeSet);
        } else {
            logHelper.addLogWarningWithTranslation("!!Fehler beim Laden der Stückliste für Submodul %1.", hmMSmId.toString("/"));
        }
        return true;
    }

    private Set<HmMSmId> getHmMSmNodesFromDialog(iPartsProduct product) {
        if (product.getReferencedSeries() == null) {
            return Collections.EMPTY_SET;
        }

        String[] fields = new String[]{ iPartsConst.FIELD_DD_SERIES_NO, iPartsConst.FIELD_DD_HM, iPartsConst.FIELD_DD_M, iPartsConst.FIELD_DD_SM };
        String[] whereFields = new String[]{ iPartsConst.FIELD_DD_SERIES_NO };
        String[] whereValues = new String[]{ product.getReferencedSeries().getSeriesNumber() };
        DBDataObjectAttributesList seriesAttributesList = project.getDbLayer().getAttributesList(iPartsConst.TABLE_DA_DIALOG,
                                                                                                 fields,
                                                                                                 whereFields, whereValues,
                                                                                                 null, null,
                                                                                                 ExtendedDataTypeLoadType.NONE, true);
        seriesAttributesList.sort(fields, new SortType[]{ SortType.AUTOMATIC, SortType.AUTOMATIC, SortType.AUTOMATIC, SortType.AUTOMATIC });
        Set<HmMSmId> hmMSmIds = new LinkedHashSet<>(seriesAttributesList.size());
        for (DBDataObjectAttributes seriesAttributes : seriesAttributesList) {
            hmMSmIds.add(new HmMSmId(seriesAttributes.getFieldValue(iPartsConst.FIELD_DD_SERIES_NO),
                                     seriesAttributes.getFieldValue(iPartsConst.FIELD_DD_HM),
                                     seriesAttributes.getFieldValue(iPartsConst.FIELD_DD_M),
                                     seriesAttributes.getFieldValue(iPartsConst.FIELD_DD_SM)));
        }
        return hmMSmIds;
    }
}
