package de.docware.apps.etk.plugins.customer.daimler.iparts.forms;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsLanguage;
import de.docware.apps.etk.plugins.customer.daimler.iparts.images.iPartsDefaultImages;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.gui.controls.GuiButton;
import de.docware.framework.modules.gui.controls.GuiComboBoxMode;
import de.docware.framework.modules.gui.controls.GuiPanel;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventCreator;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.layout.LayoutBorder;
import de.docware.framework.modules.gui.layout.LayoutGridBag;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag;
import de.docware.framework.modules.gui.misc.color.Colors;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.responsive.components.combobox.RComboBox;
import de.docware.util.StrUtils;

import java.util.List;

/**
 * Gui-Element zur Spracheingabe für DAIMLER
 */
public class iPartsGuiDaimlerLangSelectionBox extends GuiPanel {

    public static final String TYPE = "ipartsGuiDaimlerLangSelection";

    private static final Colors DEFAULT_BUTTON_BACKGROUND_COLOR_NAME = Colors.clDesignComboBoxEnabledBackground;
    private static final iPartsDefaultImages DEFAULT_SELECTALL_ICON = iPartsDefaultImages.edit_lang_selectall;
    private static final iPartsDefaultImages DEFAULT_DAIMLER_SELECT_ICON = iPartsDefaultImages.edit_lang_daimler_select;
    private static final iPartsDefaultImages DEFAULT_CLEAR_ICON = iPartsDefaultImages.edit_clear;

    protected RComboBox<Language> rComboBox;
    private GuiButton buttonSelectAll;
    private GuiButton buttonSelectDaimler;
    private GuiButton buttonDeselectAll;

    public iPartsGuiDaimlerLangSelectionBox() {
        super(); // Default-Initialisierung des "Panels"
        setType(TYPE); // Überlagern des tatsächlichen Typs
        __internal_initializeChildComponents();
        __internal_setTestNameOnControl();
        enabledButtons();
    }

    public void init(EtkProject project, String initialValue) {
        rComboBox.removeAllItems();
        rComboBox.switchOffEventListeners();
        List<Language> languages = iPartsLanguage.getAvailDaimlerLanguages(project);
        for (Language lang : languages) {
            rComboBox.addItem(lang, getComboText(lang));
        }
        rComboBox.switchOnEventListeners();
        if (StrUtils.isValid(initialValue)) {
            List<Language> selectedLanguages = iPartsLanguage.getLangListFromDBValue(initialValue);
            int[] selectedIndices = convertLangsToIndices(selectedLanguages);
            rComboBox.setSelectedIndices(selectedIndices);
        }
    }

    private int[] convertLangsToIndices(List<Language> Languages) {
        int[] selectedIndices = new int[Languages.size()];
        int lfdNr = 0;
        List<Language> userObjects = rComboBox.getUserObjects();
        for (Language lang : Languages) {
            int index = userObjects.indexOf(lang);
            if (index >= 0) {
                selectedIndices[lfdNr] = index;
                lfdNr++;
            }
        }
        return selectedIndices;
    }

    /**
     * Selektierte Sprachen als DB-String liefern (Komma-separiert)
     *
     * @return
     */
    public String getSelectedLangs() {
        return iPartsLanguage.convertToDBStringLangs(getSelectedLanguages());
    }

    /**
     * Selektierte Sprachen als Liste liefern
     *
     * @return
     */
    public List<Language> getSelectedLanguages() {
        return rComboBox.getSelectedUserObjects();
    }

    private String getComboText(Language lang) {
        return lang.getCode() + " - " + TranslationHandler.translate(lang.getDisplayName());
    }

    private void __internal_initializeChildComponents() {
        setMinimumWidth(270);
        setLayout(new LayoutBorder());
        // Initialisierung der Eingabekomponente
        rComboBox = new RComboBox();
        rComboBox.setMode(GuiComboBoxMode.Mode.CHECKBOX);
        rComboBox.setEditable(false);
        rComboBox.setMaximumRowCount(10);
        ConstraintsBorder comboboxConstraints = new ConstraintsBorder();
        rComboBox.setConstraints(comboboxConstraints);
        addChild(rComboBox);

        GuiPanel panelButtons = new GuiPanel();
        panelButtons.setName("panelButtons");
        panelButtons.setLayout(new LayoutGridBag());

        // Button um alle Einträge zu selektieren
        int gridX = 0;
        buttonSelectAll = createButton("!!Alle Sprachen markieren", DEFAULT_BUTTON_BACKGROUND_COLOR_NAME,
                                       DEFAULT_SELECTALL_ICON, gridX);
        buttonSelectAll.setName("buttonSelectAll");
        buttonSelectAll.addEventListener(new EventListener(Event.ACTION_PERFORMED_EVENT) {
            @Override
            public void fire(Event event) {
                // Alle Sprachen markieren
                int itemCount = rComboBox.getItemCount();
                int[] selectedIndices = new int[itemCount];
                for (int i = 0; i < itemCount; i++) {
                    selectedIndices[i] = i;
                }
                rComboBox.setSelectedIndices(selectedIndices);
            }
        });
        panelButtons.addChild(buttonSelectAll);

        // Button um nur Daimler spezifische Sprachen zu selektieren
        gridX++;
        buttonSelectDaimler = createButton("!!Nur Primär-Sprachen", DEFAULT_BUTTON_BACKGROUND_COLOR_NAME,
                                           DEFAULT_DAIMLER_SELECT_ICON, gridX);
        buttonSelectDaimler.setName("buttonSelectDaimler");
        buttonSelectDaimler.addEventListener(new EventListener(Event.ACTION_PERFORMED_EVENT) {
            @Override
            public void fire(Event event) {
                // nur die DAIMLER-Sprachen markieren
                int[] selectedIndices = convertLangsToIndices(iPartsLanguage.getDaimlerPrimaryLanguages());
                rComboBox.setSelectedIndices(selectedIndices);
            }
        });
        panelButtons.addChild(buttonSelectDaimler);

        // Button um alle Einträge zu deselektieren
        gridX++;
        buttonDeselectAll = createButton("!!Alle Markierungen löschen", DEFAULT_BUTTON_BACKGROUND_COLOR_NAME,
                                         DEFAULT_CLEAR_ICON, gridX);
        buttonDeselectAll.setName("buttonDeselectAll");
        buttonDeselectAll.addEventListener(new EventListener(Event.ACTION_PERFORMED_EVENT) {
            @Override
            public void fire(Event event) {
                // alles deselektieren
                rComboBox.setSelectedIndices(null);
            }
        });
        panelButtons.addChild(buttonDeselectAll);

        ConstraintsBorder panelButtonsConstraints = new ConstraintsBorder();
        panelButtonsConstraints.setPosition(ConstraintsBorder.POSITION_EAST);
        panelButtons.setConstraints(panelButtonsConstraints);
        addChild(panelButtons);

        // OnChange Event des GuiTextfields an die GuiFileChooserTextfield Komponente weitergeben, die es dann intern verwerten und dispatchen kann
        // Es ist notwendig, dass ein OnChange-Event vom GuiFileChooserTextfield geworfen wird (und nicht nur von den Kind-Controls),
        // wenn man manuell das Eingabefeld editiert
        final iPartsGuiDaimlerLangSelectionBox _self = this;
        rComboBox.addEventListener(new EventListener(Event.ON_CHANGE_EVENT) {
            public void fire(Event event) {
                enabledButtons();

                // Die Kind-Komponente aktualisiert ihren eigenen Zustand. Ist sie aktualisiert, holen wir uns den neuen Wert aus dem Kind
                // GuiFileChooserTextfield muss seinen eigenen Zustand nach der Änderung im Kind aktualisieren. Dabei dürfen keine Events ausgelöst werden.
                fireEvent(EventCreator.createOnChangeEvent(_self.getEventHandlerComponent(), _self.getUniqueId())); // Registrierte Listener informieren
            }
        });
    }

    private void enabledButtons() {
        List<Language> selected = rComboBox.getSelectedUserObjects();
        buttonDeselectAll.setEnabled(!selected.isEmpty());
        buttonSelectAll.setEnabled(selected.isEmpty() || (selected.size() != rComboBox.getItemCount()));
        boolean isEnabled = selected.isEmpty();
        if (!isEnabled) {
            List<Language> primLang = iPartsLanguage.getDaimlerPrimaryLanguages();
            if (primLang.size() == selected.size()) {
                for (Language selection : selected) {
                    primLang.remove(selection);
                }
                isEnabled = !primLang.isEmpty();
            } else {
                isEnabled = true;
            }
        }
        buttonSelectDaimler.setEnabled(isEnabled);
    }

    private GuiButton createButton(String tooltip, Colors backgroundColor, iPartsDefaultImages image, int gridX) {
        GuiButton button = new GuiButton();
        button.setScaleForResolution(true);
        button.setMinimumHeight(10);
        button.setMaximumWidth(27);
        button.setMaximumHeight(27);
        if (backgroundColor != null) {
            button.setBackgroundColor(backgroundColor);
        }
        button.setTooltip(tooltip);
        button.setMnemonicEnabled(true);
        if (image != null) {
            button.setIcon(image.getImage());
        }
        ConstraintsGridBag buttonConstraints = new ConstraintsGridBag(gridX, 0, 1, 1, 0.0, 0.0,
                                                                      ConstraintsGridBag.ANCHOR_CENTER, ConstraintsGridBag.FILL_NONE,
                                                                      0, 2, 0, 2);
        button.setConstraints(buttonConstraints);
        return button;
    }
}
