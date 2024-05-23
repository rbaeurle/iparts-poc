/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.textfilter;

import de.docware.apps.etk.base.project.EtkProject;

import java.util.regex.Pattern;

/**
 * Hilfsklasse für die Ermittlung verschiedener Lang-Texte, die ausgefiltert werden sollen
 * Die Patterns werden in der WorkBench konfiguriert und aus der DWK gelesen
 * MAD-Lexikon
 */
public class MADLangTextFilterHelper extends MADFilterHelper {

    @Override
    public void loadPatterns(EtkProject project) {
        patternList.clear();
        //load from Config
        loadFromConfig(project, "");
    }

    @Override
    protected void setDefaultFilter() {
        //Default Werte einsetzen
        patternList.add(Pattern.compile("^QFT[0-9]{3}[A-Z][0-9]{4}.*"));
        patternList.add(Pattern.compile("^FA [ A-Z][ 0-9]{15}.*"));
        patternList.add(Pattern.compile("^F B[ A-Z][ 0-9]{15}.*"));
        patternList.add(Pattern.compile("^GA [ 0-9]{16}.*"));
        patternList.add(Pattern.compile("^G B[ 0-9]{16}.*"));
        patternList.add(Pattern.compile("^MA [ 0-9]{16}.*"));
        patternList.add(Pattern.compile("^M B[ 0-9]{16}.*"));
    }
}
