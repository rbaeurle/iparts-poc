/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.base.db.EtkDbsHelper;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataReleaseState;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogBCTEPrimaryKey;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsFactoryModel;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;
import de.docware.framework.modules.config.db.VirtualFieldsUtils;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.util.StrUtils;
import de.docware.util.Utils;

import java.util.Comparator;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für TABLE_DA_FACTORY_DATA.
 */
public class iPartsDataFactoryData extends EtkDataObject implements iPartsConst, iPartsEtkDataObjectFactoryDataInterface {

    static private final String[] KEYS = new String[]{ FIELD_DFD_GUID, FIELD_DFD_FACTORY, FIELD_DFD_SPKZ, FIELD_DFD_ADAT, FIELD_DFD_DATA_ID, FIELD_DFD_SEQ_NO };

    public static final String AGGREGATE_ELDAS_FOOTNOTE = "eldasFootnote";

    // zur Sortierung nach ADAT und SeqNo; Der neuste Eintrag (höchstes ADAT und SeqNo) ist der an Position 0
    public static Comparator<iPartsDataFactoryData> comparator = new Comparator<iPartsDataFactoryData>() {
        @Override
        public int compare(iPartsDataFactoryData o1, iPartsDataFactoryData o2) {
            String o1AdatString = Utils.toSortString(o1.getAsId().getAdat());
            String o2AdatString = Utils.toSortString(o2.getAsId().getAdat());
            int result = o2AdatString.compareTo(o1AdatString);
            if (result == 0) {
                String o1SeqNoString = Utils.toSortString(o1.getAsId().getSeqNo());
                String o2SeqNoString = Utils.toSortString(o2.getAsId().getSeqNo());
                result = o2SeqNoString.compareTo(o1SeqNoString);
            }
            return result;
        }
    };

    private iPartsDataFactoryData(EtkProject project) {
        super(KEYS);
        tableName = TABLE_DA_FACTORY_DATA;
        if (project != null) {
            init(project);
        }
    }

    public iPartsDataFactoryData(EtkProject project, iPartsFactoryDataId id) {
        this(project);
        setId(id, DBActionOrigin.FROM_DB);
    }

    public iPartsDataFactoryData(EtkProject project, DBDataObjectAttributes attributes) {
        this(project);

        if (attributes != null) {
            setAttributes(attributes, DBActionOrigin.FROM_DB);
        }
    }

    @Override
    public iPartsFactoryDataId createId(String... idValues) {
        return new iPartsFactoryDataId(idValues[0], idValues[1], idValues[2], idValues[3], idValues[4], idValues[5]);
    }

    @Override
    public iPartsFactoryDataId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }

        return (iPartsFactoryDataId)id;
    }

    /**
     * Liefert die DIALOG GUID als Referenz in das Feld {@code DA_DIALOG.DD_GUID} einer DIALOG Konstruktionsstückliste.
     *
     * @return
     */
    public String getGUID() {
        return getFieldValue(FIELD_DFD_GUID);
    }

    /**
     * Aktualisiert die (reduntanten) BCTE-Felder aus der Primärschlüssel GUID
     *
     * @param origin
     */
    public void updateDialogBCTEFields(DBActionOrigin origin) {
        if (getSource() != iPartsImportDataOrigin.ELDAS) {
            iPartsDialogBCTEPrimaryKey bcteKey = getAsId().getBCTEPrimaryKey();
            if (bcteKey == null) {
                return;
            }
            setFieldValue(FIELD_DFD_SERIES_NO, bcteKey.seriesNo, origin);
            setFieldValue(FIELD_DFD_HM, bcteKey.hm, origin);
            setFieldValue(FIELD_DFD_M, bcteKey.m, origin);
            setFieldValue(FIELD_DFD_SM, bcteKey.sm, origin);
            setFieldValue(FIELD_DFD_POSE, bcteKey.posE, origin);
            setFieldValue(FIELD_DFD_POSV, bcteKey.posV, origin);
            setFieldValue(FIELD_DFD_WW, bcteKey.ww, origin);
            setFieldValue(FIELD_DFD_ET, bcteKey.et, origin);
            setFieldValue(FIELD_DFD_AA, bcteKey.aa, origin);
            setFieldValue(FIELD_DFD_SDATA, bcteKey.sData, origin);
        }
    }

    public void setFieldsFromBCTEKEYAndPartListEntry(EtkDataPartListEntry partListEntry) {
        if (partListEntry != null) {
            // Produktgruppe und BCTE-Schlüssel in den neuen Werkseinsatzdaten vom Stücklisteneintrag übernehmen
            setFieldValue(iPartsConst.FIELD_DFD_PRODUCT_GRP, partListEntry.getFieldValue(iPartsConst.FIELD_K_PRODUCT_GRP),
                          DBActionOrigin.FROM_EDIT);
        }
        updateDialogBCTEFields(DBActionOrigin.FROM_EDIT);
    }

    /**
     * Liefert eine neue {@link iPartsFactoryDataId} für nicht-DIALOG Werksdaten auf Basis von bestehenden Werksdaten
     *
     * @param partListEntry
     * @param factoryDataList
     * @return
     */
    public static iPartsFactoryDataId getFactoryDataIDForNonDIALOGFromPartListEntry(EtkDataPartListEntry partListEntry, iPartsDataFactoryDataList factoryDataList) {
        if ((partListEntry != null) && (factoryDataList != null)) {
            String guid = partListEntry.getFieldValue(FIELD_K_SOURCE_GUID);

            int temp = 1;
            if (!factoryDataList.getAsList().isEmpty()) {
                iPartsDataFactoryData highestSeqNr = factoryDataList.get(0);
                for (iPartsDataFactoryData fd : factoryDataList) {
                    if (highestSeqNr.getAsId().getSeqNo().compareTo(fd.getAsId().getSeqNo()) < 0) {
                        highestSeqNr = fd;
                    }
                }
                temp = StrUtils.strToIntDef(highestSeqNr.getAsId().getSeqNo(), 0);
                // Um eins erhöhen, sonst gleiche Nummer, wie der letzte Werksdatensatz
                temp++;
            }

            String newSeq = EtkDbsHelper.formatLfdNr(temp);

            return new iPartsFactoryDataId(guid, newSeq);
        }
        return null;
    }

    public iPartsImportDataOrigin getSource() {
        return iPartsImportDataOrigin.getTypeFromCode(getFieldValue(FIELD_DFD_SOURCE));
    }

    @Override
    public boolean loadVirtualField(String attributeName) {
        if (attributeName.equals(iPartsDataVirtualFieldsDefinition.DFD_FACTORY_SIGNS)) { // Werkskennbuchstaben ermitteln
            // Falls vorhanden den Aggregatetyp über das virtuelle Feld DFD_AGGREGATE_TYPE auslesen
            String aggregateType;
            if (attributeExists(iPartsDataVirtualFieldsDefinition.DFD_AGGREGATE_TYPE)) {
                aggregateType = getFieldValue(iPartsDataVirtualFieldsDefinition.DFD_AGGREGATE_TYPE);
            } else {
                aggregateType = null;
            }

            String factorySignsString = iPartsFactoryModel.getInstance(getEtkProject()).getFactorySignsStringForFactoryNumberAndSeries(getFieldValue(FIELD_DFD_FACTORY),
                                                                                                                                       new iPartsSeriesId(getFieldValue(FIELD_DFD_SERIES_NO)),
                                                                                                                                       aggregateType);
            attributes.addField(iPartsDataVirtualFieldsDefinition.DFD_FACTORY_SIGNS, factorySignsString, true, DBActionOrigin.FROM_DB);
            return false;
        } else if (attributeName.startsWith(VirtualFieldsUtils.VIRTUAL_FIELD_MARKER_PREFIX + "DFD_")) { // Weitere virtuelle DFD-Felder einfach leer anlegen
            attributes.addField(attributeName, "", true, DBActionOrigin.FROM_DB);
            return false;
        }
        return super.loadVirtualField(attributeName);
    }

    @Override
    public iPartsDataFactoryData cloneMe(EtkProject project) {
        iPartsDataFactoryData clone = new iPartsDataFactoryData(project, getAsId());
        clone.assignRecursively(project, this, DBActionOrigin.FROM_DB);
        return clone;
    }

    public boolean isReleased() {
        return (getReleaseState() == iPartsDataReleaseState.RELEASED);
    }

    public iPartsDataReleaseState getReleaseState() {
        return iPartsDataReleaseState.getTypeByDBValue(getFieldValue(FIELD_DFD_STATUS));
    }

    @Override
    public String getDataId() {
        return getAsId().getDataId();
    }

    @Override
    public String getFactory() {
        return getAsId().getFactory();
    }

}
