/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.textfilter;

import de.docware.apps.etk.base.project.EtkProject;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Hilfsklasse für die Ermittlung verschiedener Texte, die ausgefiltert werden sollen
 * Die Patterns werden in der WorkBench konfiguriert und aus der DWK gelesen
 * MAD-Lexikon
 */
public class MADFilterHelper {

    protected List<Pattern> patternList = new ArrayList<Pattern>();

    public void loadPatterns(EtkProject project) {
        patternList.clear();
        //load from Config
        loadFromConfig(project, "");
    }

    /**
     * load Filter-Pattern from DWK
     *
     * @param project
     */
    protected void loadFromConfig(EtkProject project, String key) {
        //aus DWK mit key laden

        if (patternList.isEmpty()) {
            setDefaultFilter();
        }
    }

    protected void setDefaultFilter() {
        //Default Werte einsetzen
        patternList.add(Pattern.compile("^QFT[0-9]{3}[A-Z][0-9]{4}.*"));
        patternList.add(Pattern.compile("^FA [ A-Z][ 0-9]{15}"));
        patternList.add(Pattern.compile("^F B[ A-Z][ 0-9]{15}.*"));
        patternList.add(Pattern.compile("^GA [ 0-9]{16}"));
        patternList.add(Pattern.compile("^G B[ 0-9]{16}"));
        patternList.add(Pattern.compile("^MA [ 0-9]{16}"));
        patternList.add(Pattern.compile("^M B[ 0-9]{16}"));
    }

    public boolean isRestricted(String text) {
        for (Pattern pattern : patternList) {
            Matcher matcher = pattern.matcher(text);
            if (matcher.matches()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Liefert {@code true} zurück, wenn der Text ausschließlich aus ".", Leerzeichen, Tabstopps und Zeilenumbrüchen besteht
     * und in Summe mehr als 2 "." enthalten sind.
     */
    public boolean checkDotText(String text) {
        if (text.isEmpty()) {
            return false;
        }
        int dotCounter = 0;
        for (int lfdNr = 0; lfdNr < text.length(); lfdNr++) {
            switch (text.charAt(lfdNr)) {
                case ' ':
                case '\t':
                case '\n':
                    break;
                case '.':
                    dotCounter++;
                    break;
                default:
                    return false;
            }
        }
        return dotCounter > 1;
    }
}
