/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_import.helper.messaging.xml;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.MQChannelType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.primus.iPartsXMLPrimusDataset;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.helper.AbstractXMLPartImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.primus.PrimusPartImporter;
import de.docware.framework.modules.config.defaultconfig.UniversalConfigOption;

/**
 * Helfer für den PRIMUS XML Teilestammimporter
 */
public class PrimusXMLMessageHelper extends AbstractPartXMLMessageHelper {

    public PrimusXMLMessageHelper() {
        super(iPartsXMLPrimusDataset.TYPE);
    }

    @Override
    protected AbstractXMLPartImporter getImporter(EtkProject project) {
        return new PrimusPartImporter(project);
    }

    @Override
    protected String getChildThreadName() {
        return "MQ PRIMUS messages collect thread";
    }

    @Override
    protected UniversalConfigOption getCollectTimeConfig() {
        return iPartsImportPlugin.CONFIG_MQ_PRIMUS_COLLECT_TIME;
    }

    @Override
    protected MQChannelType getMQChannelName() {
        return iPartsImportPlugin.MQ_CHANNEL_TYPE_PRIMUS_IMPORT;
    }
}
