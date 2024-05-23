/*
 * Copyright (c) 2020 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.timer.AbstractDayOfWeekHandler;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.iPartsEditPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket.WorkbasketAutoCalculationHelper;
import de.docware.framework.modules.gui.session.Session;

/**
 * Scheduler für die automatische Berechnung aller offenen Arbeitsvorräte
 * Dazu werden in allen 6 Arbeitsvorräten Einträge mit Status Gültigkeitserweiterung, neu oder offen abgefragt und in einzelnen csv Dateien abgelegt
 */
public class iPartsWorkbasketCalcAndExport extends AbstractDayOfWeekHandler {

    public iPartsWorkbasketCalcAndExport(EtkProject project, Session session) {
        super(project, session, iPartsEditPlugin.LOG_CHANNEL_WORKBASKETS_CALC_AND_EXPORT, "workbasket calc and export");
    }

    @Override
    protected void executeLogic() {
        WorkbasketAutoCalculationHelper.calculateAllWorkbaskets(getProject());
    }
}