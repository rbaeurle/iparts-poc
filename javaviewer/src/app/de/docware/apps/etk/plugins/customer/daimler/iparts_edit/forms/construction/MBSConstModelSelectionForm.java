package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.construction;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.EditControl;
import de.docware.apps.etk.base.forms.common.EtkMessageLogForm;
import de.docware.apps.etk.base.forms.common.components.DataObjectGrid;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsEditConfigConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataModel;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.SessionKeyHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsUserSettingsHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.mbs.iPartsMBSModel;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.iPartsEditPlugin;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.gui.controls.GuiLabel;
import de.docware.framework.modules.gui.controls.GuiSeparator;
import de.docware.framework.modules.gui.controls.calendar.GuiCalendar;
import de.docware.framework.modules.gui.controls.misc.DWOrientation;
import de.docware.framework.modules.gui.controls.table.GuiTableRow;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag;
import de.docware.framework.modules.gui.misc.Constants;
import de.docware.framework.modules.gui.misc.threads.FrameworkRunnable;
import de.docware.framework.modules.gui.misc.threads.FrameworkThread;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.util.StrUtils;
import de.docware.util.date.DateUtils;

import java.util.Calendar;
import java.util.Set;

/**
 * Konkrete Klasse für die Auwahl von MBS Konstruktionsbaumuster
 */
public class MBSConstModelSelectionForm extends AbstractConstModelSelectionForm {

    public static final String CONFIG_KEY_SELECT_MBS_MODEL_DATA = iPartsEditConfigConst.iPARTS_EDIT_MASTER_MODEL_AFTER_SALES_MBS_KEY + iPartsEditConfigConst.REL_EDIT_MASTER_DISPLAYFIELDS;

    private String chosenDateTimeString;

    protected MBSConstModelSelectionForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm, iPartsDataAssembly dataAssembly) {
        super(dataConnector, parentForm, dataAssembly, CONFIG_KEY_SELECT_MBS_MODEL_DATA, CONFIG_KEY_SELECT_MBS_MODEL_DATA);
    }

    @Override
    protected double getWeightXForSearchFieldEditControl() {
        return 0.0;
    }

    @Override
    protected int addAdditionalControlsToSearchArea(EditControl searchFieldControl) {
        GuiSeparator separatorCalendar = new GuiSeparator(DWOrientation.VERTICAL);
        separatorCalendar.setMinimumHeight(searchFieldControl.getEditControl().getControl().getPreferredHeight());
        ConstraintsGridBag gridbagConstraints = new ConstraintsGridBag(2, 0, 1, 1, 0.0, 0.0,
                                                                       ConstraintsGridBag.ANCHOR_WEST, ConstraintsGridBag.FILL_NONE,
                                                                       8, 4, 8, 4);
        separatorCalendar.setConstraints(gridbagConstraints);
        getAdditionalTopPanel().addChild(separatorCalendar);

        GuiLabel label = new GuiLabel("!!Freigabedatum");
        gridbagConstraints = new ConstraintsGridBag(3, 0, 1, 1, 0.0, 0.0,
                                                    ConstraintsGridBag.ANCHOR_WEST, ConstraintsGridBag.FILL_NONE, 8, 4, 8, 4);
        label.setConstraints(gridbagConstraints);
        getAdditionalTopPanel().addChild(label);

        GuiCalendar guiCalendar = new GuiCalendar();
        gridbagConstraints = new ConstraintsGridBag(4, 0, 1, 1, 100.0, 0.0,
                                                    ConstraintsGridBag.ANCHOR_WEST, ConstraintsGridBag.FILL_NONE, 8, 4, 8, 4);
        guiCalendar.setConstraints(gridbagConstraints);

        // In der Session gespeichertes Datum verwenden. Falls keines gesetzt ist,
        // wird das aktuelle zurückgeliefert
        Calendar chosenDateTime = SessionKeyHelper.getMbsConstructionDate();
        guiCalendar.setDate(chosenDateTime);
        // aktuelles Freigabedatum beim Aufruf merken
        chosenDateTimeString = SessionKeyHelper.getMbsConstructionDBDate();

        guiCalendar.addEventListener(new EventListener(Event.ON_CHANGE_EVENT) {
            @Override
            public void fire(Event event) {
                String rawDate = guiCalendar.getDateAsRawString();
                if (DateUtils.isValidDate_yyyyMMdd(rawDate)) {
                    SessionKeyHelper.setMbsConstructionDate(guiCalendar.getDate());
                } else {
                    SessionKeyHelper.setMbsConstructionDateEmpty();
                }
                doFilterModel(event);

                // Den Baugruppen-Baum sowie die MBS-Konstruktions-Stücklisten aktualisieren
                iPartsEditPlugin.assemblyListEditFilterChanged(getConnector());
            }
        });

        guiCalendar.setName("calendarValidityDateMBSModelSelection");
        getAdditionalTopPanel().addChild(guiCalendar);

        return 2;
    }

    @Override
    protected String getTitle() {
        return "!!MBS Konstruktions-Baumuster";
    }

    @Override
    protected String getSubTitle() {
        return "!!Auswahl MBS Baumuster";
    }

    @Override
    public void close() {
        // falls das Freigabe-Datum leer ist auf den Wert beim Aufruf setzen (kann auch heute sein)
        if (StrUtils.isEmpty(SessionKeyHelper.getMbsConstructionDBDate())) {
            SessionKeyHelper.setMbsConstructionDBDate(chosenDateTimeString);
        }
    }

    @Override
    protected Set<String> getSelectedModelSet(boolean isAggregate) {
        return SessionKeyHelper.getMbsSelectedModelSet(isAggregate);
    }

    @Override
    protected boolean isValidModel(iPartsDataModel modelData) {
        return iPartsMBSModel.isValidMBSModel(modelData.getAsId().getModelNumber(), modelData, getProject());
    }

    @Override
    protected void fillGrid(boolean top) {
        // nur das obere Gird updaten
        if (top) {
            super.fillGrid(top);
        }
    }

    @Override
    protected void collectModelFilterValues(DataObjectGrid grid, Set<String> filterValues) {
        int rowCount = grid.getTable().getRowCount();
        if (rowCount > 0) {
            String validationDate = "";
            boolean searchValidationDate = StrUtils.isEmpty(SessionKeyHelper.getMbsConstructionDBDate());
            for (int i = 0; i < rowCount; i++) {
                GuiTableRow row = grid.getTable().getRow(i);
                if (row instanceof DataObjectGrid.GuiTableRowWithObjects) {
                    EtkDataObject dataObject = ((DataObjectGrid.GuiTableRowWithObjects)row).getObjectForTable(TABLE_DA_MODEL);
                    if (dataObject instanceof iPartsDataModel) {
                        if (searchValidationDate) {
                            String currentReleaseDate = getMBSReleaseFrom(dataObject);
                            if (StrUtils.isValid(currentReleaseDate)) {
                                if (StrUtils.isValid(validationDate)) {
                                    if (currentReleaseDate.compareTo(validationDate) < 0) {
                                        validationDate = currentReleaseDate;
                                    }
                                } else {
                                    validationDate = currentReleaseDate;
                                }
                            }
                        }
                        filterValues.add(((iPartsModelId)dataObject.getAsId()).getModelNumber());
                    }
                }
            }
            if (searchValidationDate && !StrUtils.isEmpty(validationDate)) {
                SessionKeyHelper.setMbsConstructionDBDate(validationDate);
            }
        }
    }

    private String getMBSReleaseFrom(EtkDataObject dataObject) {
        DBDataObjectAttributes attributes = dataObject.getAttributes();
        if (attributes != null) {
            DBDataObjectAttribute attribute = attributes.getField(iPartsMBSModel.FIELD_STRUCTURE_MBS_RELEASE_FROM_DATE, false);
            if (attribute != null) {
                return attribute.getAsString();
            }
        }
        return "";
    }

    @Override
    protected void setConstructionModelSetToFilter(Set<String> filterValues) {
        if (!filterValues.isEmpty()) {
            EtkMessageLogForm logForm = new EtkMessageLogForm("!!MBS Konstruktions-Baumuster", "!!Lade MBS Baumuster...", null);
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
                        iPartsMBSModel model = iPartsMBSModel.getInstance(getProject(), modelId);
                        // Lade die Struktur und die Texte
                        model.loadStructureAndTexts(getProject());
                        logForm.getMessageLog().fireProgress(counter, maxSize, "", true, false);
                    }
                }
            });
        }
        // Abhängig vom Typ (Aggregat oder Fahrzeug) Baumuster für den Filter setzen
        SessionKeyHelper.setMbsConstructionModelSetToFilter(filterValues, isAggregateForm());
        if (Constants.DEVELOPMENT && !Constants.DEVELOPMENT_QFTEST) {
            iPartsUserSettingsHelper.setSelectedMBSConstModels(getProject(), SessionKeyHelper.getSelectedMBSModelMap());
        }
    }
}
