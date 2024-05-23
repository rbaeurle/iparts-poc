/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_import.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.timer.AbstractDayOfWeekHandler;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.session.Session;
import de.docware.framework.utils.VarParam;

/**
 * Scheduler für die automatische Vorverdichtung für den CTT-Arbeitsvorrat.
 */
public class CTTWorkbasketPrecalculationScheduler extends AbstractDayOfWeekHandler {

    public CTTWorkbasketPrecalculationScheduler(EtkProject project, Session session) {
        super(project, session, iPartsImportPlugin.LOG_CHANNEL_CTT_PRECALC, "precalculate CTT workbasket");
    }

    @Override
    protected void executeLogic() {
        VarParam<Boolean> cancelVarParam = new VarParam<>(false);
        try {
            WorkbasketPrecalculationHelper.doCalculateSingleWorkbasket(getProject(), getLogChannel(), cancelVarParam,
                                                                       iPartsImportDataOrigin.SAP_CTT,
                                                                       iPartsImportPlugin.getPluginConfig().getConfigValueAsBoolean(iPartsImportPlugin.CONFIG_CALC_SINGLE_CTT_WORKBASKET_DELETE));
        } catch (Exception e) {
            Logger.logExceptionWithoutThrowing(getLogChannel(), LogType.ERROR, e);
        }
        if (cancelVarParam.getValue()) {
            Logger.log(getLogChannel(), LogType.ERROR, "CTT workbasket precalculation cancelled");
        }
    }
}
