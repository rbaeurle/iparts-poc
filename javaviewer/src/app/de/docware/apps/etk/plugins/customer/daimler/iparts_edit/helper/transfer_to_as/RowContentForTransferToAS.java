/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.transfer_to_as;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.config.db.datatypes.enumtypes.SetOfEnumDataType;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsEntrySourceType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsTransferAssignmentValue;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.EditConstructionToRetailHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogBCTEPrimaryKey;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.helper.iPartsSaaPartsListPrimaryKey;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.iPartsConstructionPrimaryKey;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu.KgTuHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu.KgTuId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu.KgTuListItem;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.EditTransferPartlistPredictionGrid;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;

import java.util.*;

/**
 * Diese Klasse enthält sämtliche Metadaten die für das Formular und die eigentliche Übernahme
 * aus der Konstruktion in AS notwendig sind
 */
public class RowContentForTransferToAS {

    protected TransferToASElement transferElement;

    protected iPartsTransferAssignmentValue assignmentValue;
    protected PartListEntryId assignedASpartlistEntry; // pro Übernahme gibt es eine eigene Zeile
    protected iPartsDialogBCTEPrimaryKey bcteKeyForOmittedPartPos;
    protected iPartsDialogBCTEPrimaryKey bcteKeyForPosVar;
    protected iPartsDialogBCTEPrimaryKey bcteKeyForModule;
    protected String aa;
    protected List<KgTuId> alternativeKgTuList;
    protected iPartsProductId fallBackProductId;
    protected boolean isCreated;
    protected boolean transferMark;
    protected boolean isHotspotEdited;

    protected Map<String, KgTuListItem> kgTuMap;

    public RowContentForTransferToAS() {
        assignmentValue = iPartsTransferAssignmentValue.UNKNOWN;
        transferElement = new TransferToASElement();
        assignedASpartlistEntry = null;
        aa = null;
        bcteKeyForPosVar = null;
        bcteKeyForModule = null;
        alternativeKgTuList = null;
        kgTuMap = new HashMap<>();
        isCreated = false;
        transferMark = false;
        isHotspotEdited = false;
    }

    public RowContentForTransferToAS(iPartsConstructionPrimaryKey constPrimaryKey, EtkDataPartListEntry selectedPartlistEntry,
                                     iPartsProduct product) {
        this();
        this.assignmentValue = iPartsTransferAssignmentValue.NOT_ASSIGNED;
        this.transferElement.constructionPrimaryKey = constPrimaryKey;
        this.transferElement.setProduct(product);
        this.transferElement.selectedPartlistEntry = selectedPartlistEntry;

    }

    public static Comparator<RowContentForTransferToAS> createComparatorForFindPosVariant() {
        return new Comparator<RowContentForTransferToAS>() {
            @Override
            public int compare(RowContentForTransferToAS o1, RowContentForTransferToAS o2) {
                return o1.getCompareString().compareTo(o2.getCompareString());
            }
        };
    }

    public static Comparator<RowContentForTransferToAS> createComparatorForBCTEandProduct() {
        return new Comparator<RowContentForTransferToAS>() {
            @Override
            public int compare(RowContentForTransferToAS o1, RowContentForTransferToAS o2) {
                if ((o1.getTransferElement() != null) && (o2.getTransferElement() != null)) {
                    return TransferToASElement.compareByBCTEandProduct(o1.getTransferElement(), o2.getTransferElement());
                }
                return 0;
            }
        };
    }

    public static Comparator<? super RowContentForTransferToAS> createComparatorForSeqNrAndProduct() {
        return new Comparator<RowContentForTransferToAS>() {
            @Override
            public int compare(RowContentForTransferToAS o1, RowContentForTransferToAS o2) {
                return TransferToASElement.compareBySeqNrAndProduct(o1.getTransferElement(), o2.getTransferElement());
            }
        };
    }

    public void copyValues(RowContentForTransferToAS other) {
        this.assignmentValue = other.assignmentValue;
        this.transferElement.copyValues(other.transferElement);
        if (other.assignedASpartlistEntry != null) {
            this.assignedASpartlistEntry = new PartListEntryId(other.assignedASpartlistEntry);
        }
        this.bcteKeyForPosVar = null;
        this.bcteKeyForModule = null;
        this.aa = other.aa;
        this.alternativeKgTuList = other.alternativeKgTuList;
        this.kgTuMap = other.kgTuMap;
        this.isCreated = other.isCreated;
        this.transferMark = other.transferMark;
        this.isHotspotEdited = other.isHotspotEdited;
    }

    public void resetValuesForCreate() {
        this.assignedASpartlistEntry = null;
        this.isCreated = true;
        this.assignmentValue = iPartsTransferAssignmentValue.NOT_ASSIGNED;
        this.alternativeKgTuList = null;
        setKgTuId(new KgTuId());
        setHotspot("");
        this.transferMark = false;
    }

    public DBDataObjectAttributes getAsAttributes(EtkProject project) {
        DBDataObjectAttributes attributes = new DBDataObjectAttributes();
        String product = getProductForAttribute();
        if (StrUtils.isValid(product)) {
            attributes.addField(EditTransferPartlistPredictionGrid.FIELD_PSEUDO_PRODUCT, product, DBActionOrigin.FROM_DB);
            attributes.addField(EditTransferPartlistPredictionGrid.FIELD_PSEUDO_KG, getKgForAttribute(project), DBActionOrigin.FROM_DB);
            attributes.addField(EditTransferPartlistPredictionGrid.FIELD_PSEUDO_TU, getTuForAttribute(project), DBActionOrigin.FROM_DB);
            attributes.addField(EditTransferPartlistPredictionGrid.FIELD_PSEUDO_SA, "", DBActionOrigin.FROM_DB);
        } else {
            // MBS Fall für Übernahme in SA Modul
            attributes.addField(EditTransferPartlistPredictionGrid.FIELD_PSEUDO_PRODUCT, "", DBActionOrigin.FROM_DB);
            attributes.addField(EditTransferPartlistPredictionGrid.FIELD_PSEUDO_KG, "", DBActionOrigin.FROM_DB);
            attributes.addField(EditTransferPartlistPredictionGrid.FIELD_PSEUDO_TU, "", DBActionOrigin.FROM_DB);
            attributes.addField(EditTransferPartlistPredictionGrid.FIELD_PSEUDO_SA, getSaModuleNumberForAttribute(), DBActionOrigin.FROM_DB);
        }
        attributes.addField(EditTransferPartlistPredictionGrid.FIELD_PSEUDO_ASSIGNED, getAssignmentValue().getDisplayValue(project), DBActionOrigin.FROM_DB);

        attributes.addField(EditTransferPartlistPredictionGrid.FIELD_PSEUDO_TRANSFER, "", DBActionOrigin.FROM_DB);
        attributes.getField(EditTransferPartlistPredictionGrid.FIELD_PSEUDO_TRANSFER).setValueAsBoolean(getTransferMark(), DBActionOrigin.FROM_DB);

        attributes.addFields(transferElement.selectedPartlistEntry.getAttributes().cloneMe(DBActionOrigin.FROM_DB), DBActionOrigin.FROM_DB);
        // zusätzlich die Part-Attributes hinzuhängen, da Felder aus MAT konfiguriert werden können
        if (transferElement.selectedPartlistEntry.getPart().getAttributes() != null) {
            attributes.addFields(transferElement.selectedPartlistEntry.getPart().getAttributes().cloneMe(DBActionOrigin.FROM_DB), DBActionOrigin.FROM_DB);
        }
        if (transferElement.getConstPrimaryKey() != null) {
            if (transferElement.getConstPrimaryKey().isDialog()) {
                // DIALOG BCTE-Schlüssel umsetzen
                iPartsDialogBCTEPrimaryKey bctePrimaryKey = transferElement.constructionPrimaryKey.getAsDialogBCTEPrimaryKey();
                attributes.addField(iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_SERIES_NO, bctePrimaryKey.getHmMSmId().getSeries(), DBActionOrigin.FROM_DB);
                attributes.addField(iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_HM, bctePrimaryKey.getHmMSmId().getHm(), DBActionOrigin.FROM_DB);
                attributes.addField(iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_M, bctePrimaryKey.getHmMSmId().getM(), DBActionOrigin.FROM_DB);
                attributes.addField(iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_SM, bctePrimaryKey.getHmMSmId().getSm(), DBActionOrigin.FROM_DB);
                attributes.addField(iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_POSE, bctePrimaryKey.getPosE(), DBActionOrigin.FROM_DB);
                attributes.addField(iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_POSV, bctePrimaryKey.getPosV(), DBActionOrigin.FROM_DB);
                attributes.addField(iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_WW, bctePrimaryKey.getWW(), DBActionOrigin.FROM_DB);
                attributes.addField(iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_ETKZ, bctePrimaryKey.getET(), DBActionOrigin.FROM_DB);
                attributes.addField(iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_AA, bctePrimaryKey.getAA(), DBActionOrigin.FROM_DB);
                attributes.addField(iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_SDATA, bctePrimaryKey.getSData(), DBActionOrigin.FROM_DB);

                attributes.addField(iPartsDataVirtualFieldsDefinition.FIELD_DD_AA_SOE, /*aa*/ SetOfEnumDataType.getSetOfEnumTag(bctePrimaryKey.getAA()), DBActionOrigin.FROM_DB);
            } else if (transferElement.getConstPrimaryKey().isSaaPartsList()) {
                // EDS-Schlüssel umsetzen
                iPartsSaaPartsListPrimaryKey edsPrimaryKey = transferElement.getConstPrimaryKey().getAsSaaPartsListPrimaryKey();
                if (!attributes.fieldExists(iPartsDataVirtualFieldsDefinition.EDS_KEMFROM)) {
                    attributes.addField(iPartsDataVirtualFieldsDefinition.EDS_KEMFROM, edsPrimaryKey.getKemFrom(), DBActionOrigin.FROM_DB);
                }
                if (!attributes.fieldExists(iPartsDataVirtualFieldsDefinition.EDS_REVFROM)) {
                    attributes.addField(iPartsDataVirtualFieldsDefinition.EDS_REVFROM, edsPrimaryKey.getRevFrom(), DBActionOrigin.FROM_DB);
                }
                if (!attributes.fieldExists(iPartsDataVirtualFieldsDefinition.EDS_PARTPOS)) {
                    attributes.addField(iPartsDataVirtualFieldsDefinition.EDS_PARTPOS, edsPrimaryKey.getPos(), DBActionOrigin.FROM_DB);
                }
            }
        }

        attributes.getField(EtkDbConst.FIELD_K_POS).setValueAsString(getHotspotForAttribute(), DBActionOrigin.FROM_DB);
        return attributes;
    }

    public Collection<KgTuListItem> getKGItems() {
        return kgTuMap.values();
    }

    public Collection<KgTuListItem> getTUItems() {
        return getTUItems(transferElement.kgTuId.getKg());
    }

    private Collection<KgTuListItem> getTUItems(String kg) {
        if (kgTuMap != null) {
            KgTuListItem kgTuListItem = kgTuMap.get(kg);
            if (kgTuListItem != null) {
                return kgTuListItem.getChildren();
            }
            return new DwList<>();
        }
        return null;
    }

    public boolean hasKGItems() {
        return !kgTuMap.isEmpty();
    }

    public boolean hasTUItems() {
        Collection<KgTuListItem> tuItems = getTUItems();
        return ((tuItems != null) && !tuItems.isEmpty());
    }

    /**
     * Check ob der KG/TU Knoten valide für die Übernahme ist. Das ist er falls er bereits
     * zum Produkt existiert oder im Rahmen des KG/TU Templates fürs Produkt zulässig ist.
     *
     * @return
     */
    public boolean isKGTUValidForTransfer() {
        return isKGTUValidForTransfer(getKgTuId());
    }

    private boolean isKGTUValidForTransfer(KgTuId kgTuId) {
        if (!kgTuId.isValidId()) {
            return false;
        }
        Set<String> validTUs = new HashSet<>();
        Collection<KgTuListItem> validTUItems = getTUItems(kgTuId.getKg());
        if (validTUItems == null) {
            return false;
        }
        for (KgTuListItem tuItem : validTUItems) {
            validTUs.add(tuItem.getKgTuId().getTu());
        }
        return !validTUs.isEmpty() && validTUs.contains(kgTuId.getTu());
    }

    public String getKgForAttribute(EtkProject project) {
        if (transferElement.kgTuId.isValidId()) {
            if ((transferElement.product != null) && (kgTuMap != null)) {
                KgTuListItem kgTuListItem = kgTuMap.get(transferElement.kgTuId.getKg());
                if (kgTuListItem != null) {
                    return KgTuHelper.buildKgTuComboText(kgTuListItem, project.getViewerLanguage(), project.getDataBaseFallbackLanguages());
                }
            }
            // mit dem Produkt stimmt etwas nicht. Es konnten keine passenden Einträge aus dem KgTu Template
            // ermittelt werden, wahrscheinlich ist keine AS-Produktklasse hinterlegt
            return "";
        }
        return "";
    }

    public String getTuForAttribute(EtkProject project) {
        if (transferElement.kgTuId.isValidId()) {
            if ((transferElement.product != null) && (kgTuMap != null)) {
                boolean hasAlternateKgTus = isAlternateKgTuListValid();
                KgTuListItem kgTuListItem = kgTuMap.get(transferElement.kgTuId.getKg());
                if (kgTuListItem != null) {
                    for (KgTuListItem tuListItem : kgTuListItem.getChildren()) {
                        if (tuListItem.getKgTuId().getTu().equals(transferElement.kgTuId.getTu())) {
                            return KgTuHelper.buildKgTuComboText(tuListItem, project.getViewerLanguage(), project.getDataBaseFallbackLanguages());
                        }
                    }
                }
            }
            // mit dem Produkt stimmt etwas nicht. Es konnten keine passenden Einträge aus dem KgTu Template
            // ermittelt werden, wahrscheinlich ist keine AS-Produktklasse hinterlegt
            return "";
        }
        return "";
    }

    public void correctPredictedKgTuWithAlternatives() {
        if (isAlternateKgTuListValid()) {
            KgTuId correctedKgTu = null;
            for (KgTuId alternativeKgTu : alternativeKgTuList) {
                if (isKGTUValidForTransfer(alternativeKgTu)) {
                    correctedKgTu = alternativeKgTu;
                    break;
                }
            }
            if (correctedKgTu != null) {
                alternativeKgTuList.add(getKgTuId());
                setKgTuId(correctedKgTu);
                alternativeKgTuList.remove(correctedKgTu);
                setAssignmentValue(iPartsTransferAssignmentValue.ASSIGNED_OTHER_PRODUCT);
            }
        }
    }

    public KgTuId getKgTuId() {
        return transferElement.kgTuId;
    }

    public void setKgTuId(KgTuId kgtuId) {
        if (kgtuId == null) {
            this.transferElement.setKgTuId(new KgTuId());
        } else {
            this.transferElement.setKgTuId(kgtuId);
        }
    }

    public void setKgTuId(String kg, String tu) {
        transferElement.setKgTuId(kg, tu);
    }

    public void setKgTuMap(Map<String, KgTuListItem> kgTuMap) {
        this.kgTuMap = kgTuMap;
    }

    public iPartsTransferAssignmentValue getAssignmentValue() {
        return assignmentValue;
    }

    public void setAssignmentValue(iPartsTransferAssignmentValue assignmentValue) {
        this.assignmentValue = assignmentValue;
    }

    public void setAlternativeKgTuList(List<KgTuId> alternativeKgTuList) {
        this.alternativeKgTuList = alternativeKgTuList;
    }

    public List<KgTuId> getAlternativeKgTuList() {
        return alternativeKgTuList;
    }

    public boolean isAlternateKgTuListValid() {
        return (getAlternativeKgTuList() != null) && !getAlternativeKgTuList().isEmpty();
    }

    public iPartsProductId getFallBackProductId() {
        return fallBackProductId;
    }

    public void setFallBackProductId(iPartsProductId fallBackProductId) {
        this.fallBackProductId = fallBackProductId;
    }

    public void setHotspot(String hotspot) {
        this.transferElement.setHotspot(hotspot);
    }

    public String getHotspotForAttribute() {
        return transferElement.hotspot;
    }

    public void setAssignedASpartlistEntry(PartListEntryId assignedASpartlistEntry) {
        this.assignedASpartlistEntry = assignedASpartlistEntry;
    }

    public boolean isEditable() {
        if (getProductId() != null) {
            return (assignedASpartlistEntry == null) && hasKGItems();
        } else if (getSaModuleNumber() != null) {
            return assignedASpartlistEntry == null;
        }
        return false;
    }

    public String getProductForAttribute() {
        if (getProductId() != null) {
            return getProductId().getProductNumber();
        }
        return "";
    }

    public iPartsProductId getProductId() {
        if (transferElement != null) {
            return transferElement.getProductId();
        }
        return null;
    }

    public String getSaModuleNumber() {
        if (transferElement != null) {
            return transferElement.getSaModuleNumber();
        }
        return null;
    }

    public String getSaModuleNumberForAttribute() {
        String saModuleNumber = getSaModuleNumber();
        if (saModuleNumber == null) {
            return "";
        }
        return saModuleNumber;
    }

    public String getSourceGUIDForAttribute() {
        return transferElement.getSourceGUIDForAttribute();
    }

    public iPartsDialogBCTEPrimaryKey getBcteKeyForOmittedPartsPos() {
        if (bcteKeyForOmittedPartPos == null) {
            iPartsDialogBCTEPrimaryKey bctePrimaryKey = transferElement.constructionPrimaryKey.getAsDialogBCTEPrimaryKey();
            if (bctePrimaryKey != null) {
                bcteKeyForOmittedPartPos = new iPartsDialogBCTEPrimaryKey(bctePrimaryKey.seriesNo, bctePrimaryKey.hm, bctePrimaryKey.m, bctePrimaryKey.sm,
                                                                          bctePrimaryKey.posE, "", "", "", bctePrimaryKey.aa, "");
            }
        }
        return bcteKeyForOmittedPartPos;
    }

    public iPartsDialogBCTEPrimaryKey getBcteKeyForPosVar() {
        if (bcteKeyForPosVar == null) {
            iPartsDialogBCTEPrimaryKey bctePrimaryKey = transferElement.constructionPrimaryKey.getAsDialogBCTEPrimaryKey();
            if (bctePrimaryKey != null) {
                bcteKeyForPosVar = new iPartsDialogBCTEPrimaryKey(bctePrimaryKey.seriesNo, bctePrimaryKey.hm, bctePrimaryKey.m, bctePrimaryKey.sm,
                                                                  bctePrimaryKey.posE, bctePrimaryKey.posV, "", bctePrimaryKey.et,
                                                                  bctePrimaryKey.aa, "");
            }
        }
        return bcteKeyForPosVar;
    }

    public iPartsDialogBCTEPrimaryKey getBcteKeyForNewVar() {
        if (bcteKeyForModule == null) {
            iPartsDialogBCTEPrimaryKey bctePrimaryKey = transferElement.constructionPrimaryKey.getAsDialogBCTEPrimaryKey();
            if (bctePrimaryKey != null) {
                bcteKeyForModule = new iPartsDialogBCTEPrimaryKey(bctePrimaryKey.seriesNo, bctePrimaryKey.hm, bctePrimaryKey.m, bctePrimaryKey.sm,
                                                                  bctePrimaryKey.posE, "", "", "",
                                                                  bctePrimaryKey.aa, "");
            }
        }
        return bcteKeyForModule;
    }

    public boolean getTransferMark() {
        return transferMark;
    }

    public void setTransferMark(boolean transferMark) {
        this.transferMark = transferMark;
    }

    private String getCompareString() {
        StringBuilder sb = new StringBuilder();
        if (transferElement.product != null) {
            sb.append(transferElement.product.getAsId().getProductNumber());
        }
        sb.append(iPartsConst.K_SOURCE_CONTEXT_DELIMITER);
        if (transferElement.kgTuId != null) {
            sb.append(EditConstructionToRetailHelper.createSourceContext(iPartsEntrySourceType.DIALOG, transferElement.kgTuId));
        }
        sb.append(iPartsConst.K_SOURCE_CONTEXT_DELIMITER);
        if (transferElement.hotspot != null) {
            sb.append(transferElement.hotspot);
        }
        sb.append(iPartsConst.K_SOURCE_CONTEXT_DELIMITER);
        if (assignmentValue != null) {
            sb.append(assignmentValue.getDbValue());
        }
        return sb.toString();
    }

    public boolean isCreated() {
        return isCreated;
    }

    public boolean isHotspotEdited() {
        return isHotspotEdited;
    }

    public void setHotspotEdited(boolean hotspotEdited) {
        isHotspotEdited = hotspotEdited;
    }

    public boolean isRowContentValidForPasteKgTu(boolean checkAssigned) {
        if (kgTuMap != null) {
            if (!checkAssigned || (assignmentValue != iPartsTransferAssignmentValue.ASSIGNED)) {
                return true;
            }
        }
        return false;
    }

    public EtkDataPartListEntry getSelectedPartlistEntry() {
        return transferElement.selectedPartlistEntry;
    }

    public AssemblyId getAssemblyId() {
        return transferElement.assemblyId;
    }

    public iPartsConstructionPrimaryKey getConstructionPrimaryKey() {
        return transferElement.constructionPrimaryKey;
    }

    public void setConstructionPrimaryKey(iPartsConstructionPrimaryKey primaryKey) {
        transferElement.setConstPrimaryKey(primaryKey);
    }

    public TransferToASElement getTransferElement() {
        return transferElement;
    }
}
