/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.hash;

import de.docware.framework.utils.EtkMultiSprache;
import de.docware.framework.utils.HashHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * HashValue Erzeugung über ein {@link EtkMultiSprache} für RSK
 */
public class RSKHash {

    public static String calcHashValue(EtkMultiSprache multiLang) {
        return calcHashValue(multiLang, null);
    }

    public static String calcHashValue(EtkMultiSprache multiLang, String status) {
        StringBuilder strBuilder = new StringBuilder(multiLang.getTextId());
        if ((status != null) && !status.isEmpty()) {
            strBuilder.append("\t" + status);
        }


        // Die Values in eines Stringlist, damit die Sprachen immer in der gleichen Reihenfolge kommen
        // muss nach den Sprachen sortiert werden, deshalb erst in eine Stringliste und dann sortieren

        List<String> values = new ArrayList<String>();
        for (Map.Entry<String, String> languageAndText : multiLang.getLanguagesAndTexts().entrySet()) {
            values.add("\t" + languageAndText.getKey() + "\t" + languageAndText.getValue());
        }

        Collections.sort(values);

        for (String s : values) {
            strBuilder.append(s);
        }

        return HashHelper.buildHashValue(strBuilder.toString());
    }
}
