package de.docware.apps.etk.plugins.customer.daimler.iparts_export.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.timer.AbstractDayOfWeekHandler;
import de.docware.apps.etk.plugins.customer.daimler.iparts_export.exporter.iPartsSaaDataXMLExporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_export.iPartsExportPlugin;
import de.docware.framework.modules.gui.session.Session;

/**
 * Timer für den SA - SAA Benennungen Exporter
 */
public class iPartsSaaDataExportTimer extends AbstractDayOfWeekHandler {


    public iPartsSaaDataExportTimer(EtkProject project, Session session) {
        super(project, session, iPartsExportPlugin.LOG_CHANNEL_EXPORT, "saa data export");
    }

    @Override
    protected void executeLogic() {
        iPartsSaaDataXMLExporter exporter = new iPartsSaaDataXMLExporter(getProject());
        exporter.exportWithoutMessageLogForm();
    }
}
