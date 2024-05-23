/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataReleaseState;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsFactoryModel;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.util.StrUtils;
import de.docware.util.Utils;

import java.util.Comparator;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für TABLE_DA_COLORTABLE_FACTORY.
 * (Werkseinsatzdaten für Farbvariantentabellen bzw. Farbvarianteninhalte)
 */
public class iPartsDataColorTableFactory extends EtkDataObject implements iPartsConst, iPartsEtkDataObjectFactoryDataInterface {

    static private final String[] KEYS = new String[]{ FIELD_DCCF_TABLE_ID, FIELD_DCCF_POS, FIELD_DCCF_FACTORY, FIELD_DCCF_ADAT, FIELD_DCCF_DATA_ID, FIELD_DCCF_SDATA };

    // zur Sortierung nach ADAT; Der neuste Eintrag (höchstes ADAT) ist der an Position 0
    public static Comparator<iPartsDataColorTableFactory> adatComparator = new Comparator<iPartsDataColorTableFactory>() {
        @Override
        public int compare(iPartsDataColorTableFactory o1, iPartsDataColorTableFactory o2) {
            String o1AdatString = Utils.toSortString(o1.getAsId().getAdat());
            String o2AdatString = Utils.toSortString(o2.getAsId().getAdat());
            return o2AdatString.compareTo(o1AdatString);
        }
    };

    public iPartsDataColorTableFactory(EtkProject project, iPartsColorTableFactoryId id) {
        super(KEYS);
        tableName = TABLE_DA_COLORTABLE_FACTORY;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsColorTableFactoryId createId(String... idValues) {
        return new iPartsColorTableFactoryId(idValues[0], idValues[1], idValues[2], idValues[3], idValues[4], idValues[5]);
    }

    @Override
    public iPartsColorTableFactoryId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }

        return (iPartsColorTableFactoryId)id;
    }

    public iPartsImportDataOrigin getSource() {
        return iPartsImportDataOrigin.getTypeFromCode(getFieldValue(FIELD_DCCF_SOURCE));
    }

    public String getFactory() {
        return getFieldValue(FIELD_DCCF_FACTORY);
    }

    public String getPEMTerminAb() {
        return getFieldValue(FIELD_DCCF_PEMTA);
    }

    public String getPEMTerminBis() {
        return getFieldValue(FIELD_DCCF_PEMTB);
    }

    public String getADat() {
        return getFieldValue(FIELD_DCCF_ADAT);
    }

    public String getOriginalSdata() {
        String value = getFieldValue(FIELD_DCCF_ORIGINAL_SDATA);
        if (StrUtils.isEmpty(value)) {
            value = getAsId().getSdata();
        }
        return value;
    }

    @Override
    public boolean loadVirtualField(String attributeName) {
        if (attributeName.equals(iPartsDataVirtualFieldsDefinition.DCCF_FACTORY_SIGNS)) { // Werkskennbuchstaben ermitteln
            // Falls vorhanden die Baureihe über das virtuelle Feld DCCF_SERIES_NUMBER auslesen
            iPartsSeriesId seriesId;
            if (attributeExists(iPartsDataVirtualFieldsDefinition.DCCF_SERIES_NUMBER)) {
                seriesId = new iPartsSeriesId(getFieldValue(iPartsDataVirtualFieldsDefinition.DCCF_SERIES_NUMBER));
            } else {
                seriesId = null;
            }

            // Falls vorhanden den Aggregatetyp über das virtuelle Feld DCCF_AGGREGATE_TYPE auslesen
            String aggregateType;
            if (attributeExists(iPartsDataVirtualFieldsDefinition.DCCF_AGGREGATE_TYPE)) {
                aggregateType = getFieldValue(iPartsDataVirtualFieldsDefinition.DCCF_AGGREGATE_TYPE);
            } else {
                aggregateType = null;
            }

            String factorySignsString = iPartsFactoryModel.getInstance(getEtkProject()).getFactorySignsStringForFactoryNumberAndSeries(getFieldValue(FIELD_DCCF_FACTORY),
                                                                                                                                       seriesId,
                                                                                                                                       aggregateType);
            attributes.addField(iPartsDataVirtualFieldsDefinition.DCCF_FACTORY_SIGNS, factorySignsString, true, DBActionOrigin.FROM_DB);
            return false;
        }
        return super.loadVirtualField(attributeName);
    }

    @Override
    public iPartsDataColorTableFactory cloneMe(EtkProject project) {
        iPartsDataColorTableFactory clone = new iPartsDataColorTableFactory(project, getAsId());
        clone.assignRecursively(project, this, DBActionOrigin.FROM_DB);
        return clone;
    }

    public iPartsDataReleaseState getReleaseState() {
        return iPartsDataReleaseState.getTypeByDBValue(getFieldValue(FIELD_DCCF_STATUS));
    }

    @Override
    public String getDataId() {
        return getAsId().getDataId();
    }

    public boolean isDeleted() {
        return getFieldValueAsBoolean(FIELD_DCCF_IS_DELETED);
    }
}
