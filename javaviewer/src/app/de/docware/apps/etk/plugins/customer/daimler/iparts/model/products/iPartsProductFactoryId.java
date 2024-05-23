package de.docware.apps.etk.plugins.customer.daimler.iparts.model.products;

import de.docware.util.misc.id.IdWithType;

/**
 * ID für die Zuordnung Produkt (=Katalog) zu Werken im After Sales = ID für ein Produkt das in einem Werk gefertigt wurde.
 * Das gleiche Produkt kann in verschiedenen Werken hergestellt worden sein.
 * ==> Die Relation Produkt zu Werk(en) = 1:n
 */

public class iPartsProductFactoryId extends IdWithType {

    public static final String TYPE = "DA_iPartsProductFactoryId";

    private enum INDEX {PRODUCT_NO, FACTORY_NO}

    /**
     * Normaler Konstruktor mit den Parametern "Produktnummer" und "Werksnummer"
     *
     * @param productNumber Katalog/Produkt-Nummer
     * @param factoryNumber Nummer der Fertigung
     */
    public iPartsProductFactoryId(String productNumber, String factoryNumber) {
        super(TYPE, new String[]{ productNumber, factoryNumber });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsProductFactoryId() {
        this("", "");
    }

    public String getProductNumber() {
        return id[INDEX.PRODUCT_NO.ordinal()];
    }

    public String getCatalogNumber() {
        return getProductNumber();
    }

    public String getFactoryNumber() {
        return id[INDEX.FACTORY_NO.ordinal()];
    }

    @Override
    public String toString() {
        return "(" + getProductNumber() + ", " + getFactoryNumber() + ") iPartsProductFactoryId";
    }
}
