/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.construction;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.EtkMessageLogForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsEditConfigConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataModel;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.SessionKeyHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsCTTHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsUserSettingsHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.ctt.iPartsCTTModel;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.framework.modules.gui.misc.Constants;
import de.docware.framework.modules.gui.misc.threads.FrameworkRunnable;
import de.docware.framework.modules.gui.misc.threads.FrameworkThread;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;

import java.util.Set;

/**
 * Konkrete Klasse für die Auswahl von CTT Konstruktionsbaumuster
 */
public class CTTConstModelSelectionForm extends AbstractConstModelSelectionForm {

    public static final String CONFIG_KEY_SELECT_CTT_MODEL_DATA = iPartsEditConfigConst.iPARTS_EDIT_MASTER_MODEL_AFTER_SALES_CTT_KEY
                                                                  + iPartsEditConfigConst.REL_EDIT_MASTER_DISPLAYFIELDS;


    protected CTTConstModelSelectionForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                         iPartsDataAssembly dataAssembly) {
        super(dataConnector, parentForm, dataAssembly, CONFIG_KEY_SELECT_CTT_MODEL_DATA, CONFIG_KEY_SELECT_CTT_MODEL_DATA);
    }

    @Override
    protected void setConstructionModelSetToFilter(Set<String> filterValues) {
        if (!filterValues.isEmpty()) {
            EtkMessageLogForm logForm = new EtkMessageLogForm("!!CTT Konstruktions-Baumuster", "!!Lade CTT Baumuster...", null);
            logForm.disableButtons(true);
            logForm.getGui().setSize(600, 250);
            logForm.showModal(new FrameworkRunnable() {
                @Override
                public void run(FrameworkThread thread) {
                    int counter = 0;
                    int maxSize = filterValues.size();
                    for (String modelNo : filterValues) {
                        counter++;
                        logForm.getMessageLog().fireMessage(TranslationHandler.translate("!!Lade Baumuster \"%1\"...", modelNo));
                        iPartsModelId modelId = new iPartsModelId(modelNo);
                        iPartsCTTModel model = iPartsCTTModel.getInstance(getProject(), modelId);
                        // Lade die Struktur
                        model.getCompleteCTTStructure(getProject());
                        logForm.getMessageLog().fireProgress(counter, maxSize, "", true, false);
                    }
                }
            });
        }
        // Abhängig vom Typ (Aggregat oder Fahrzeug) Baumuster für den Filter setzen
        SessionKeyHelper.setCttConstructionModelSetToFilter(filterValues, isAggregateForm());
        if (Constants.DEVELOPMENT && !Constants.DEVELOPMENT_QFTEST) {
            iPartsUserSettingsHelper.setSelectedCTTConstModels(getProject(), SessionKeyHelper.getSelectedCTTModelMap());
        }
    }

    @Override
    protected Set<String> getSelectedModelSet(boolean isAggregate) {
        return SessionKeyHelper.getCTTSelectedModelSet(isAggregate);
    }

    @Override
    protected String getTitle() {
        return "!!CTT Konstruktions-Baumuster";
    }

    @Override
    protected String getSubTitle() {
        return "!!Auswahl CTT Baumuster";
    }

    @Override
    protected boolean isValidModel(iPartsDataModel modelData) {
        return iPartsCTTHelper.isValidModel(getProject(), modelData.getAsId());
    }
}
