/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu;

import de.docware.apps.etk.base.db.EtkRevisionsHelper;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsRight;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.iPartsDataDictMeta;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.iPartsDataDictMetaList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.iPartsDictMetaId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.iPartsDictTextKindId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataKgTuAfterSales;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataKgTuAfterSalesId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictImportTextIdHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTextKindTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTxtKindIdByMADId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsRevisionChangeSet;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;

import java.util.Collection;
import java.util.TreeSet;


/**
 * Diese Klasse enthält einen {@link KgTuNode} inkl. vorhandener Kinder und die jeweilige Quelle dazu.
 * Aktuell sind die Quellen entweder TEMPLATE {@link KgTuTemplate} oder PRODUCT {@link KgTuForProduct}
 * Dadurch kann eine gemischte Liste, die die Elemente aus beiden Datenquellen enthält, verwendet werden
 */
public class KgTuListItem implements Comparable<KgTuListItem> {

    public enum Source {
        TEMPLATE,
        PRODUCT
    }

    public enum PSK_NATURE {
        PSK_NEW_NODE,
        PSK_CHANGED_TITLE,
        PSK_NONE
    }

    private KgTuNode node;
    private Source source;
    private TreeSet<KgTuListItem> children;
    private KgTuListItem parent;
    private PSK_NATURE pskNature;

    public KgTuListItem(KgTuNode node, Source source, KgTuListItem parent, boolean initChildren) {
        this.node = node;
        this.source = source;
        this.pskNature = PSK_NATURE.PSK_NONE;
        this.parent = parent;
        if (initChildren) {
            this.children = new TreeSet<>();
        } else {
            this.children = null;
        }
    }

    /**
     * @param node         null möglich
     * @param source       Quelle Produkt oder Template
     * @param initChildren
     */
    public KgTuListItem(KgTuNode node, Source source, boolean initChildren) {
        this(node, source, null, initChildren);
    }

    public KgTuId getKgTuId() {
        if (node != null) {
            return node.getId();
        }
        return null;
    }

    public KgTuListItem getParent() {
        return parent;
    }

    public KgTuNode getParentNode() {
        if (parent != null) {
            return parent.node;
        }
        return null;
    }

    public KgTuNode getKgTuNode() {
        return node;
    }

    public Collection<KgTuListItem> getChildren() {
        return children;
    }

    public boolean addChild(KgTuListItem child) {
        if (children == null) {
            children = new TreeSet<>();
        }
        return children.add(child);
    }

    public boolean isSourceTemplate() {
        return (source == Source.TEMPLATE);
    }

    public boolean isSourceProduct() {
        return (source == Source.PRODUCT);
    }

    public void setPskNature(PSK_NATURE pskNature) {
        this.pskNature = pskNature;
    }

    public PSK_NATURE getPskNature() {
        return pskNature;
    }

    @Override
    public int compareTo(KgTuListItem otherItem) {
        if (otherItem != null) {
            // Unterscheidung zwischen KG und TU Knoten
            if (this.node.getId().isKgNode() && otherItem.node.getId().isKgNode()) {
                return this.node.getId().getKg().compareTo(otherItem.node.getId().getKg());
            } else if (this.node.getId().isTuNode() && otherItem.node.getId().isTuNode()) {
                return this.node.getId().getTu().compareTo(otherItem.node.getId().getTu());
            } else {
                // Falls mal KG mit TU verglichen werden sollte auf die IDs zurückfallen
                return this.node.getId().compareTo(otherItem.node.getId());
            }
        }
        throw new NullPointerException();
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof KgTuListItem) {
            KgTuListItem otherItem = (KgTuListItem)other;
            // Unterscheidung zwischen KG und TU Knoten
            if (this.node.getId().isKgNode() && otherItem.node.getId().isKgNode()) {
                return this.node.getId().getKg().equals(otherItem.node.getId().getKg());
            } else if (this.node.getId().isTuNode() && otherItem.node.getId().isTuNode()) {
                return this.node.getId().getTu().equals(otherItem.node.getId().getTu());
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        if (node != null) {
            return node.getId().hashCode();
        }
        return new KgTuId().hashCode();
    }

    public void saveToDB(EtkProject project, String productNr) {
        saveToDB(project, productNr, null);
    }

    /**
     * Speichert diesen KG/TU AS-Knoten mit den dazugehörigen Texten für das angegebene Produkt in der Datenbank.
     *
     * @param project
     * @param productNr
     * @param techChangeSet
     */
    public void saveToDB(EtkProject project, String productNr, iPartsRevisionChangeSet techChangeSet) {
        if (node == null) {
            return;
        }

        KgTuListItem kgNode = null;
        KgTuListItem tuNode = null;
        if (node.getId().isTuNode()) {
            kgNode = getParent();
            tuNode = this;
        } else if (node.getId().isKgNode()) {
            kgNode = this;
            tuNode = null;
        }

        if (kgNode != null) {
            if (kgNode.isSourceTemplate()) {
                setTextAndSaveKgTuNode(new iPartsDataKgTuAfterSalesId(productNr, kgNode.getKgTuId().getKg(), ""), kgNode.node.getTitle(),
                                       project, techChangeSet);
            } else {
                if (kgNode.getPskNature() != PSK_NATURE.PSK_NONE) {
                    checkAndSaveModifiedTextForPSK(new iPartsDataKgTuAfterSalesId(productNr, kgNode.getKgTuId().getKg(), ""), kgNode.node.getTitle(),
                                                   project, techChangeSet);
                }
            }
        }

        if (tuNode != null) {
            if (tuNode.isSourceTemplate()) {
                setTextAndSaveKgTuNode(new iPartsDataKgTuAfterSalesId(productNr, tuNode.getKgTuId().getKg(), tuNode.getKgTuId().getTu()),
                                       tuNode.node.getTitle(), project, techChangeSet);
            } else {
                if (tuNode.getPskNature() != PSK_NATURE.PSK_NONE) {
                    checkAndSaveModifiedTextForPSK(new iPartsDataKgTuAfterSalesId(productNr, tuNode.getKgTuId().getKg(), tuNode.getKgTuId().getTu()),
                                                   tuNode.node.getTitle(), project, techChangeSet);
                }
            }
        }
    }

    /**
     * Setzt im KG/TU AS-Knoten mit der übergebenen {@link iPartsDataKgTuAfterSalesId} den übergebenen mehrsprachigen Text
     * und speichert den Knoten in der Datenbank.
     *
     * @param kgTuAfterSalesId
     * @param text
     * @param project
     */
    public static void setTextAndSaveKgTuNode(iPartsDataKgTuAfterSalesId kgTuAfterSalesId, EtkMultiSprache text, EtkProject project, iPartsRevisionChangeSet techChangeSet) {
        iPartsDataDictMeta dataDictMetaPSK = handlePSKDict(kgTuAfterSalesId, text, project);

        createAndSaveKgTuAfterSales(kgTuAfterSalesId, dataDictMetaPSK, text, project, techChangeSet);
    }

    private static void createAndSaveKgTuAfterSales(iPartsDataKgTuAfterSalesId kgTuAfterSalesId, iPartsDataDictMeta dataDictMetaPSK, EtkMultiSprache text,
                                                    EtkProject project, iPartsRevisionChangeSet techChangeSet) {
        iPartsDataKgTuAfterSales dataKgTuAfterSales = new iPartsDataKgTuAfterSales(project, kgTuAfterSalesId);
        if (!dataKgTuAfterSales.existsInDB()) {
            dataKgTuAfterSales.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
        }
        if (dataDictMetaPSK != null) {
            text.assign(dataDictMetaPSK.getMultiLang());
        }
        dataKgTuAfterSales.setFieldValueAsMultiLanguage(iPartsConst.FIELD_DA_DKM_DESC, text, DBActionOrigin.FROM_EDIT);

        EtkRevisionsHelper revisionsHelper = dataKgTuAfterSales.getRevisionsHelper();
        if ((revisionsHelper != null) && revisionsHelper.isRevisionChangeSetActiveForEdit()) {
            revisionsHelper.addDataObjectToActiveChangeSetForEdit(dataKgTuAfterSales);
            if (dataDictMetaPSK != null) {
                revisionsHelper.addDataObjectToActiveChangeSetForEdit(dataDictMetaPSK);
            }
        } else {
            if (techChangeSet != null) {
                techChangeSet.addDataObject(dataKgTuAfterSales, false, false, false);
            }
            dataKgTuAfterSales.saveToDB();
            if (dataDictMetaPSK != null) {
                if (techChangeSet != null) {
                    techChangeSet.addDataObject(dataDictMetaPSK, false, false, false);
                }
                dataDictMetaPSK.saveToDB();
            }
        }

        // Cache für die AS KG/TU-Struktur vom Produkt löschen
        KgTuForProduct.removeKgTuForProductFromCache(dataKgTuAfterSales.getEtkProject(), dataKgTuAfterSales.getAsId().getProductId());
    }

    /**
     * Neue PSK Knoten/Texte übernehmen
     *
     * @param kgTuAfterSalesId
     * @param text
     * @param project
     */
    private static void checkAndSaveModifiedTextForPSK(iPartsDataKgTuAfterSalesId kgTuAfterSalesId, EtkMultiSprache text, EtkProject project, iPartsRevisionChangeSet techChangeSet) {
        if (isPSKorSpecialCat(project, kgTuAfterSalesId)) {
            // User besitzt PSK-Rechte und Produkt ist PSK
            iPartsDataDictMeta dataDictMetaPSK = handlePSKDict(kgTuAfterSalesId, text, project);
            if (dataDictMetaPSK == null) {
                setTextAndSaveKgTuNode(kgTuAfterSalesId, text, project, techChangeSet);
            } else {
                EtkMultiSprache currentText = dataDictMetaPSK.getMultiLang();
                // nochmals die Texte überprüfen
                if (!currentText.equalText(text)) {
                    EtkMultiSprache multi;
                    if (!dataDictMetaPSK.getAsId().getTextId().equals(text.getTextId())) {
                        multi = new EtkMultiSprache(dataDictMetaPSK.getAsId().getTextId());
                        multi.setLanguagesAndTexts(text.getLanguagesAndTextsModifiable());
                    } else {
                        multi = text;
                    }
                    dataDictMetaPSK.setNewMultiLang(multi);
                }
                createAndSaveKgTuAfterSales(kgTuAfterSalesId, dataDictMetaPSK, text, project, techChangeSet);
            }
        }
    }

    /**
     * Abfrage, ob Produkt und User PSK-fähig sind
     *
     * @param project
     * @param kgTuAfterSalesId
     * @return
     */
    private static boolean isPSKorSpecialCat(EtkProject project, iPartsDataKgTuAfterSalesId kgTuAfterSalesId) {
        iPartsProduct product = iPartsProduct.getInstance(project, kgTuAfterSalesId.getProductId());
        return (iPartsRight.checkPSKInSession() && product.isPSK()) || product.isSpecialCatalog();
    }

    private static iPartsDataDictMeta handlePSKDict(iPartsDataKgTuAfterSalesId kgTuAfterSalesId, EtkMultiSprache text, EtkProject project) {
        if (isPSKorSpecialCat(project, kgTuAfterSalesId)) {
            // User besitzt PSK-Rechte und Produkt ist PSK
            iPartsDictTextKindId textKindId = DictTxtKindIdByMADId.getInstance(project).getTxtKindId(DictTextKindTypes.KG_TU_NAME);
            iPartsDataDictMeta dataDictMetaPSK = findPSKDictData(project, textKindId, text.getTextId());
            if (dataDictMetaPSK == null) {
                // keine PSK DictMeta Daten gefunden => neu anlegen
                dataDictMetaPSK = new iPartsDataDictMeta(project, new iPartsDictMetaId(textKindId.getTextKindId(), DictHelper.buildIPARTSDictTextId(StrUtils.makeGUID())));
                DictImportTextIdHelper.initDataDictMeta(dataDictMetaPSK, "", iPartsImportDataOrigin.PSK.getOrigin(),
                                                        null, "", "");
            }
            EtkMultiSprache multi;
            if (!dataDictMetaPSK.getAsId().getTextId().equals(text.getTextId())) {
                // neues MultiLang mit TextId anlegen und die Texte übernehmen
                multi = new EtkMultiSprache(dataDictMetaPSK.getAsId().getTextId());
                multi.setLanguagesAndTexts(text.getLanguagesAndTextsModifiable());
                // und im Lexikon eintragen
                dataDictMetaPSK.setNewMultiLang(multi);
            }
            return dataDictMetaPSK;
        }
        return null;
    }

    /**
     * Aufgrund der TextArt und der TextId die zugehörigen PSK DictMeta-Daten finden
     *
     * @param project
     * @param textKindId
     * @param textId
     * @return
     */
    public static iPartsDataDictMeta findPSKDictData(EtkProject project, iPartsDictTextKindId textKindId, String textId) {
        // mit DAIMLER-13162: keine neue TextId für PSK erzeugen
        return findDictData(project, textKindId, textId, iPartsImportDataOrigin.UNKNOWN);
    }

    /**
     * Aufgrund der TextArt und der TextId die zugehörigen {@param searchOrigin} DictMeta-Daten finden
     *
     * @param project
     * @param textKindId
     * @param textId
     * @param searchOrigin
     * @return
     */
    public static iPartsDataDictMeta findDictData(EtkProject project, iPartsDictTextKindId textKindId, String textId,
                                                  iPartsImportDataOrigin searchOrigin) {
        iPartsDataDictMeta dataDictMetaPSK = null;
        if (StrUtils.isValid(textId)) {
            iPartsDataDictMetaList dictMetaList = iPartsDataDictMetaList.loadMetaFromTextIdWithChangeSetList(project,
                                                                                                             textKindId.getTextKindId(),
                                                                                                             textId, false);
            if (!dictMetaList.isEmpty()) {
                for (iPartsDataDictMeta dataDictMetaSearch : dictMetaList) {
                    if (searchOrigin != iPartsImportDataOrigin.UNKNOWN) {
                        iPartsImportDataOrigin dataOrigin = iPartsImportDataOrigin.getTypeFromCode(dataDictMetaSearch.getSource());
                        if (dataOrigin == searchOrigin) {
                            dataDictMetaPSK = dataDictMetaSearch;
                            break;
                        }
                    } else {
                        dataDictMetaPSK = dataDictMetaSearch;
                        break;
                    }
                }
            }
        }
        return dataDictMetaPSK;
    }
}