/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_export.helper;

import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.AbstractS3ObjectStoreHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_export.iPartsExportPlugin;
import de.docware.framework.modules.gui.misc.logger.LogChannels;
import de.docware.util.security.PasswordString;

/**
 * Hilfsklasse für den S3 Object Store für die nach Baumustern gefilterten Stücklisten sowie FastSearch.
 */
public class iPartsModelPartsListsFastSearchS3Helper extends AbstractS3ObjectStoreHelper {

    public iPartsModelPartsListsFastSearchS3Helper(LogChannels logChannel, String usage) {
        super(logChannel, usage);
    }

    @Override
    protected String getBucketName() {
        return iPartsExportPlugin.getPluginConfig().getConfigValueAsString(iPartsExportPlugin.CONFIG_MODEL_DATA_FAST_SEARCH_S3_BUCKET_NAME);
    }

    @Override
    protected String getAccessKey() {
        return iPartsExportPlugin.getPluginConfig().getConfigValueAsString(iPartsExportPlugin.CONFIG_MODEL_DATA_FAST_SEARCH_S3_ACCESS_KEY);
    }

    @Override
    protected PasswordString getSecretAccessKey() {
        return iPartsExportPlugin.getPluginConfig().getConfigValueAsPassword(iPartsExportPlugin.CONFIG_MODEL_DATA_FAST_SEARCH_S3_SECRET_ACCESS_KEY);
    }
}