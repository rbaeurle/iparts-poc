/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit;

/**
 * entspricht der Delphi Klasse TStuecklistenFelderEdit
 * Stücklistenfelder für die Editfunktion
 */
public class EtkEditFieldsEdit extends EtkEditFields {

    private String name;  // Ebene (meist 0, 1, 2 etc.)

    public EtkEditFieldsEdit() {
        super();
    }

    public String getName() {
        return name;
    }

    public void setName(String value) {
        name = value;
    }

    public void assign(EtkEditFieldsEdit source) {
        super.assign(source);
        this.name = source.getName();
    }

    public void assignEditFields(EtkEditFields source) {
        super.assign(source);
    }
}
