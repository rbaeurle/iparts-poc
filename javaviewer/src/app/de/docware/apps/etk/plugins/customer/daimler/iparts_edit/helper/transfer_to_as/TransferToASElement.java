/*
 * Copyright (c) 2020 Docware GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.transfer_to_as;

import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.iPartsConstructionPrimaryKey;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu.KgTuId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.framework.utils.EtkMultiSprache;

import java.util.Map;

/**
 * Diese Klasse enthält die Metadaten die zur Übernahme aus der Konstruktion notwendig sind
 */
public class TransferToASElement {

    protected AssemblyId assemblyId;
    protected KgTuId kgTuId;
    protected String hotspot;
    protected iPartsProduct product;
    protected String saModuleNumber; // alternativ zu Produkt bei Übernahme in freie SA
    protected String sourceGUID;
    protected EtkMultiSprache sourceKgTitle;
    protected Map<KgTuId, EtkMultiSprache> sourceTuTitlesMap;
    protected iPartsConstructionPrimaryKey constructionPrimaryKey;
    protected EtkDataPartListEntry selectedPartlistEntry;
    protected Object userObject;
    protected boolean isAutoTransfer;

    public TransferToASElement() {
        this.assemblyId = null;
        this.kgTuId = new KgTuId();
        this.hotspot = "";
        this.product = null;
        this.saModuleNumber = null;
        this.sourceGUID = null;
        this.sourceKgTitle = null;
        this.sourceTuTitlesMap = null;
        this.constructionPrimaryKey = null;
        this.selectedPartlistEntry = null;
        this.userObject = null;
        this.isAutoTransfer = false;
    }

    public TransferToASElement(AssemblyId assemblyId, KgTuId kgTuId, String hotspot, iPartsProduct product,
                               String sourceGUID, EtkMultiSprache sourceKgTitle, Map<KgTuId, EtkMultiSprache> sourceTuTitlesMap,
                               iPartsConstructionPrimaryKey constPrimaryKey, EtkDataPartListEntry selectedPartlistEntry) {
        this.assemblyId = assemblyId;
        this.kgTuId = kgTuId;
        this.hotspot = hotspot;
        this.product = product;
        this.saModuleNumber = null;
        this.sourceGUID = sourceGUID;
        this.sourceKgTitle = sourceKgTitle;
        this.sourceTuTitlesMap = sourceTuTitlesMap;
        this.constructionPrimaryKey = constPrimaryKey;
        this.selectedPartlistEntry = selectedPartlistEntry;
        this.userObject = null;
        this.isAutoTransfer = false;
    }

    public void copyValues(TransferToASElement other) {
        this.assemblyId = other.assemblyId;
        this.kgTuId = other.kgTuId;
        this.hotspot = other.hotspot;
        this.product = other.product;
        this.saModuleNumber = other.saModuleNumber;
        this.constructionPrimaryKey = other.constructionPrimaryKey;
        this.selectedPartlistEntry = other.selectedPartlistEntry;
        this.sourceGUID = other.sourceGUID;
        this.sourceKgTitle = other.sourceKgTitle;
        this.sourceTuTitlesMap = other.sourceTuTitlesMap;
        this.isAutoTransfer = other.isAutoTransfer;
    }

    public iPartsProduct getProduct() {
        return product;
    }

    public iPartsProductId getProductId() {
        if (product != null) {
            return product.getAsId();
        }
        return null;
    }

    public String getSaModuleNumber() {
        return saModuleNumber;
    }

    public KgTuId getKgTuId() {
        return kgTuId;
    }

    public void setAssemblyId(AssemblyId assemblyId) {
        this.assemblyId = assemblyId;
    }

    public void setKgTuId(KgTuId kgTuId) {
        this.kgTuId = kgTuId;
    }

    public void setKgTuId(String kg, String tu) {
        this.kgTuId = new KgTuId(kg, tu);
    }

    public void setHotspot(String hotspot) {
        this.hotspot = hotspot;
    }

    public void setProduct(iPartsProduct product) {
        this.product = product;
    }

    public void setSaModuleNumber(String saModuleNumber) {
        this.saModuleNumber = saModuleNumber;
    }

    public void setSourceGUID(String sourceGUID) {
        this.sourceGUID = sourceGUID;
    }

    public void setConstPrimaryKey(iPartsConstructionPrimaryKey constPrimaryKey) {
        this.constructionPrimaryKey = constPrimaryKey;
    }

    public void setAutoTransfer(boolean autoTransfer) {
        isAutoTransfer = autoTransfer;
    }

    public void setSelectedPartlistEntry(EtkDataPartListEntry selectedPartlistEntry) {
        this.selectedPartlistEntry = selectedPartlistEntry;
    }

    public AssemblyId getAssemblyId() {
        return assemblyId;
    }

    public String getSourceGUIDForAttribute() {
        if (sourceGUID == null) {
            if (constructionPrimaryKey != null) {
                sourceGUID = constructionPrimaryKey.createGUID();
            } else {
                sourceGUID = "";
            }
        }
        return sourceGUID;
    }

    public Object getUserObject() {
        return userObject;
    }

    public void setUserObject(Object userObject) {
        this.userObject = userObject;
    }

    public boolean hasUserObject() {
        return userObject != null;
    }

    public EtkDataPartListEntry getSelectedPartlistEntry() {
        return selectedPartlistEntry;
    }

    public iPartsConstructionPrimaryKey getConstPrimaryKey() {
        return constructionPrimaryKey;
    }

    public String getHotspot() {
        return hotspot;
    }

    public static int compareByBCTEandProduct(TransferToASElement o1, TransferToASElement o2) {
        int result = iPartsConstructionPrimaryKey.compareForTransferList(o1.getConstPrimaryKey(), o2.getConstPrimaryKey(), iPartsConstructionPrimaryKey.Type.DIALOG);
        if (result == 0) {
            result = o1.getProductId().getProductNumber().compareTo(o2.getProductId().getProductNumber());
        }
        return result;

    }

    public static int compareBySeqNrAndProduct(TransferToASElement o1, TransferToASElement o2) {
        int result = 0;
        EtkDataPartListEntry o1PartListEntry = o1.getSelectedPartlistEntry();
        EtkDataPartListEntry o2PartListEntry = o2.getSelectedPartlistEntry();
        if ((o1PartListEntry != null) && (o2PartListEntry != null)) {
            String o1SeqNr = o1PartListEntry.getFieldValue(iPartsConst.FIELD_K_SEQNR);
            String o2SeqNr = o2PartListEntry.getFieldValue(iPartsConst.FIELD_K_SEQNR);
            result = o1SeqNr.compareTo(o2SeqNr);
        }
        if (result == 0) {
            result = o1.getProductId().getProductNumber().compareTo(o2.getProductId().getProductNumber());
        }
        return result;
    }
}
