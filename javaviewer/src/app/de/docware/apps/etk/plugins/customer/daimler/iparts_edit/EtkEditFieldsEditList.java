/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit;

import de.docware.util.collections.dwlist.DwList;

import java.util.Iterator;

/**
 * entspricht der Delphi-Klasse TStuecklistenFelderEditListe
 * Alle Ebenen in einer Liste
 */
public class EtkEditFieldsEditList extends DwList<EtkEditFieldsEdit> {

    //private List<EtkEditFieldsEdit> editList = new DwList<EtkEditFieldsEdit>();
    // Falls keine eigene Definition vorliegt, so nehme Einstellung dieser Ebene
    private String standardTyp;

    public EtkEditFieldsEditList() {

    }

    public String getStandardTypName() {
        return standardTyp;
    }

    public void setStandardTypName(String value) {
        standardTyp = value;
    }

    public void assign(EtkEditFieldsEditList source) {
        clear();
        Iterator<EtkEditFieldsEdit> iter = source.listIterator();
        while (iter.hasNext()) {
            EtkEditFieldsEdit actItem = iter.next();
            EtkEditFieldsEdit newItem = new EtkEditFieldsEdit();
            newItem.assign(actItem);
            add(newItem);
        }
        this.standardTyp = source.getStandardTypName();
    }

    public void deleteTyp(String name) {
        for (int lfdNr = 0; lfdNr < size(); lfdNr++) {
            if (get(lfdNr).getName().equals(name)) {
                remove(lfdNr);
                break;
            }
        }
    }

    public EtkEditFieldsEdit getTypByName(String name, boolean withDefaultType) {
        if (withDefaultType) {
            return getStandardTyp();
        }
        for (int lfdNr = 0; lfdNr < size(); lfdNr++) {
            if (get(lfdNr).getName().equals(name)) {
                return get(lfdNr);
            }
        }
        return null;
    }

    public EtkEditFieldsEdit getStandardTyp() {
        return getTypByName(standardTyp, false);
    }

/*
procedure TStuecklistenFelderEditListe.SetItems(index: integer;
  const Value: TStuecklistenFelderEdit);
begin
  inherited Items[index] := Value;
end;
 */
}
