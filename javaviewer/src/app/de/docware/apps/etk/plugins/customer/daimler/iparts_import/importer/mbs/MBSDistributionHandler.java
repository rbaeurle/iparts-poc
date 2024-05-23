/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.mbs;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.mbs.structure.AbstractMBSStructureHandler;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Handler für Structure-Daten in den SAP-MBS XML Elementen. Er verarbeitet die Daten nicht selber sondern gibt sie an seine
 * Sub-Handler weiter. Alle Sub-Handler hören auf das gleiche XML-Element (<structure>).
 */
public class MBSDistributionHandler extends AbstractMBSDataHandler {

    private Set<AbstractMBSStructureHandler> registeredSubHandlers;

    public MBSDistributionHandler(EtkProject project, MBSDataImporter importer, String triggerElement) {
        super(project, triggerElement, importer, null, null);
        registeredSubHandlers = new HashSet<>();
    }

    @Override
    protected void handleCurrentRecord() {
        registeredSubHandlers.forEach(handler -> {
            handler.setCurrentRecord(getCurrentRecord());
            handler.handleCurrentRecord();
        });
    }

    public void registerSubHandler(AbstractMBSStructureHandler subHandler) {
        registeredSubHandlers.add(subHandler);
    }

    @Override
    protected void initMapping(Map<String, String> mapping) {

    }

    @Override
    public String getHandlerName() {
        StringBuilder builder = new StringBuilder();
        registeredSubHandlers.forEach(handler -> {
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append(TranslationHandler.translate(handler.getHandlerName()));
        });
        return builder.toString();
    }

    @Override
    public void onPreImportTask() {
        registeredSubHandlers.forEach(handler -> {
            handler.onPreImportTask();
        });
    }

    @Override
    public void onPostImportTask() {
        registeredSubHandlers.forEach(handler -> {
            handler.onPostImportTask();
        });
    }

}
