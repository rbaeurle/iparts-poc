/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.application;

import de.docware.apps.etk.base.config.EtkConfig;
import de.docware.apps.etk.viewer.JavaViewerApplication;
import de.docware.framework.modules.config.ConfigBase;
import de.docware.framework.modules.config.ConfigContainer;

public class iPartsJavaViewerApplication extends JavaViewerApplication {

    public iPartsJavaViewerApplication(ConfigBase configBase) {
        super(configBase);
    }

    public static void createInstance(ConfigBase config) {
        new iPartsJavaViewerApplication(config);
    }

    @Override
    protected EtkConfig createEtkConfig(ConfigContainer applicationDwk, ConfigContainer userDwk) {
        return new iPartsEtkConfig(applicationDwk, userDwk);
    }
}
