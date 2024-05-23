package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.util.misc.id.IdWithType;

/**
 * ID für die Tabelle DA_KGTU_TEMPLATE
 */
public class iPartsKgTuTemplateId extends IdWithType {

    public static String TYPE = "DA_iPartsKgTuTemplateId";


    protected enum INDEX {AGGREGEGATE_TYPE, AS_PRODUCT_CLASS, KG, TU}

    /**
     * Der normale Konstruktor
     *
     * @param aggregateType  Baumusterart (hieß früher: Aggregateart)
     * @param asProductClass
     * @param kg
     * @param tu
     */
    public iPartsKgTuTemplateId(String aggregateType, String asProductClass, String kg, String tu) {

        super(TYPE, new String[]{ aggregateType, asProductClass, kg, tu });
    }

    public String getAggregateType() {
        return id[INDEX.AGGREGEGATE_TYPE.ordinal()];
    }

    public String getASProductClass() {
        return id[INDEX.AS_PRODUCT_CLASS.ordinal()];
    }

    public String getKG() {
        return id[INDEX.KG.ordinal()];
    }

    public String getTU() {
        return id[INDEX.TU.ordinal()];
    }

}
