/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.search;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.search.model.WildCardSettings;
import de.docware.util.StrUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Hilfsklasse, die tested, ob der Datensatz mit den Suchvalues bei den speziellen Suchoptionen matched.
 * Im Prinzip ist hier das ganze Verhalten mit AndOr usw. gekapselt
 */
public class SearchMatchHelper {


    public static boolean fieldsMatchSearchOptions(Map<String, String> fieldAndValues,
                                                   boolean isAndOr, EtkDisplayFields selectFields, List<String> selectValues,
                                                   EtkDisplayFields andOrWhereFields, List<String> andOrValues,
                                                   WildCardSettings wildCardSettings) {
        // Hier muss nochmal gematcht werden, weil bei Utf8 die Unicodezeichen mit einem * ersetzt werden
        // Filterung wird bei UTF8-Datenbanken wieder auf unterer Ebene erledigt.
        // Der Filter muss leider doch rein, weil z.B. bei Oracle mit ? alle leeren Felder gefunden werden
        // Das ist wegen dem doofen ' ' anstatt null
        if (!isAndOr) {
            return recordMatchesAllWhereFields(fieldAndValues, selectFields, selectValues);
        } else {
            return recordMatchesAllAndOrWhereFields(fieldAndValues, andOrWhereFields, andOrValues, wildCardSettings);
        }
    }


    public static boolean matchStrings(String source, String pattern, boolean sensitive) {
        return StrUtils.matchesSqlLike(pattern, source, sensitive);
    }

    private static boolean valueMatch(EtkDisplayField field, String searchString, List<String> whereValues, int index) {
        boolean result = false;

        //Verodertes Feld -> 1 von n Werten muss passen
        if (field.isSearchExact()) { //Exakte Suche und SetOfEnum-> alle Werte müssen passen

            // Bei der Exakten Suche wird auch auf Groß/KleinSchreibung geachtet
            // deshalb hier nur matchen, wenn wirklich identisch ist
            result = matchStrings(searchString, whereValues.get(index), true);
        } else {
            result = matchStrings(searchString, whereValues.get(index), false);
        }
        return result;
    }


    private static boolean recordMatchesWithSuchOptionen(Map<String, String> fieldAndValues, EtkDisplayFields whereFields,
                                                         List<String> whereValues) {
        boolean result = true;
        int i = 0;
        for (EtkDisplayField whereField : whereFields.getFields()) {
            if (!result) {
                break;
            }
            String value;

            value = fieldAndValues.get(whereField.getKey().getName());

            if (value == null) {
                // Nicht da wird wie leer betrachtet
                value = "";
            }

            result = valueMatch(whereField, value, whereValues, i);

            i++;
        }
        return result;
    }


    private static boolean recordMatchesAllWhereFields(Map<String, String> fieldAndValues, EtkDisplayFields selectFields, List<String> selectValues) {

        EtkDisplayFields whereFields = new EtkDisplayFields();
        List<String> whereValues = new ArrayList<String>();

        for (int i = 0; i < selectFields.size(); i++) {
            if (!selectValues.get(i).isEmpty()) {
                EtkDisplayField field = new EtkDisplayField();
                field.assign(selectFields.getFeld(i));
                whereFields.addFeld(field);
                whereValues.add(selectValues.get(i));
            }
        }

        if (whereFields.size() == 0) {
            return true;
        } else {
            return recordMatchesWithSuchOptionen(fieldAndValues, whereFields, whereValues);
        }
    }


    private static boolean recordMatchesAllAndOrWhereFields(Map<String, String> fieldAndValues, EtkDisplayFields andOrFields, List<String> andOrValues,
                                                            WildCardSettings wildCardSettings) {
        // In den andOrValues stehen Werte, die alle in einem der Felder gefunden werden müsse
        // ist andOrValue ein Wert, dann muss dieser Wert in einem der Felder gefunden werden

        for (String whereValue : andOrValues) {
            if (!whereValue.isEmpty()) {
                String whereValueWithWildCards = null;

                boolean valid = false;
                // Suche in allen Felder nach dem Text, falls in keinem Feld etwas gefunden wurde, dann ist der Datensatz nicht gültig
                for (EtkDisplayField field : andOrFields.getFields()) {
                    EtkDisplayFields whereFields = new EtkDisplayFields();
                    List<String> whereValues = new ArrayList<String>();
                    whereFields.addFeld(field);

                    // Der whereValue ist für alle Suchfelder gleich; je nach Feld gilt aber ein anderes WildCardSetting;
                    // deshalb muss je nach Wert für field.isSearchExact() whereValue oder whereValueWithWildCards verwendet werden
                    if (field.isSearchExact()) {
                        whereValues.add(whereValue);
                    } else {
                        if (whereValueWithWildCards == null) {
                            whereValueWithWildCards = wildCardSettings.makeWildCard(whereValue);
                        }
                        whereValues.add(whereValueWithWildCards);
                    }

                    if (recordMatchesWithSuchOptionen(fieldAndValues, whereFields, whereValues)) {
                        valid = true;
                        break;
                    }
                }
                if (!valid) {
                    // Der aktuelle Suchbegriff ist in diesem Datensatz nicht, wir brauchen nicht weiterzusuchen der Datensatz ist ungültig
                    return false;
                }
            }
        }

        // Entweder war die Suche leer, oder die Testes haben immer etwas passendes gefunden
        return true;
    }
}