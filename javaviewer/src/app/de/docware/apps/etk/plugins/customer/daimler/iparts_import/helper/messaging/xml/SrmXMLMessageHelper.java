/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_import.helper.messaging.xml;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.MQChannelType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.primus.iPartsXMLSRMDataset;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.helper.AbstractXMLPartImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.srm.SrmPartImporter;
import de.docware.framework.modules.config.defaultconfig.UniversalConfigOption;

/**
 * Helfer für den SRM XML Teilestammimporter
 */
public class SrmXMLMessageHelper extends AbstractPartXMLMessageHelper {

    public SrmXMLMessageHelper() {
        super(iPartsXMLSRMDataset.TYPE);
    }

    @Override
    protected AbstractXMLPartImporter getImporter(EtkProject project) {
        return new SrmPartImporter(project);
    }

    @Override
    protected String getChildThreadName() {
        return "MQ SRM messages collect thread";
    }

    @Override
    protected UniversalConfigOption getCollectTimeConfig() {
        return iPartsImportPlugin.CONFIG_MQ_SRM_COLLECT_TIME;
    }

    @Override
    protected MQChannelType getMQChannelName() {
        return iPartsImportPlugin.MQ_CHANNEL_TYPE_SRM_IMPORT;
    }
}
