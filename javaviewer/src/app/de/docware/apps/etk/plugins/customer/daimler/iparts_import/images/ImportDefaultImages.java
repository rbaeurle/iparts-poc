/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.images;

import de.docware.apps.etk.base.misc.images.DefaultImages;
import de.docware.framework.modules.gui.design.DesignCategory;
import de.docware.framework.modules.gui.design.DesignImage;
import de.docware.framework.modules.gui.misc.resources.FrameworkImage;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * iParts-Import Images
 */
public class ImportDefaultImages extends DefaultImages {

    private static Map<String, DefaultImages> importDefaultImages = new HashMap<String, DefaultImages>();

    public static final ImportDefaultImages importToolbarButton = new ImportDefaultImages("ImportToolbarButton", "img_import.png");
    public static final ImportDefaultImages importStoppedToolbarButton = new ImportDefaultImages("ImportStoppedToolbarButton", "img_import_stopped.png");


    private ImportDefaultImages(String name, String filename) {
        super(DesignCategory.PLUGIN, name, filename, true, ImportDefaultImages.importDefaultImages, null, false, "ohne Beschreibung");
    }

    /**
     * Liste aller Bilder in dieser Klasse.
     *
     * @return
     */
    public static Collection<? extends DesignImage> getImages() {
        return Collections.unmodifiableCollection(ImportDefaultImages.importDefaultImages.values());
    }

    public static FrameworkImage getByFilename(String filename) {
        return getByFilename(filename, ImportDefaultImages.importDefaultImages);
    }
}
