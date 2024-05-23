/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.connect.handler;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.bigdata.AbstractSAXPushHandlerImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.bigdata.handler.multihandler.AbstractMappedHandlerWithRecord;
import de.docware.framework.modules.gui.misc.MessageLogType;

/**
 * Abstrakter Handler für den Leitunsgsatz-Importer
 */
public abstract class AbstractWireHarnessHandler extends AbstractMappedHandlerWithRecord implements iPartsConst {

    // XML Tags, die von allen Leitunsgssatz Handler verwendet werden
    protected static final String BENENNUNG = "Benennung";
    protected static final String LEITUNGSSATZ = "Leitungssatz";
    protected static final String DATENSTAND = "Datenstand";
    protected static final String REF = "REF";

    private AbstractSAXPushHandlerImporter importer;

    public AbstractWireHarnessHandler(EtkProject project, String mainXMLTag, String importName,
                                      AbstractSAXPushHandlerImporter importer) {
        super(project, mainXMLTag, importName);
        this.importer = importer;
    }

    public void addMessage(String translationsKey, String... placeHolderTexts) {
        addMsg(MessageLogType.tmlMessage, translationsKey, placeHolderTexts);
    }

    public void addWarning(String translationsKey, String... placeHolderTexts) {
        addMsg(MessageLogType.tmlWarning, translationsKey, placeHolderTexts);
    }

    public void addError(String translationsKey, String... placeHolderTexts) {
        addMsg(MessageLogType.tmlError, translationsKey, placeHolderTexts);
    }

    private void addMsg(MessageLogType msgLogType, String translationsKey, String... placeHolderTexts) {
        if (importer != null) {
            importer.getMessageLog().fireMessage(importer.translateForLog(translationsKey, placeHolderTexts),
                                                 msgLogType, MessageLogOption.TIME_STAMP,
                                                 MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
        }
    }
}
