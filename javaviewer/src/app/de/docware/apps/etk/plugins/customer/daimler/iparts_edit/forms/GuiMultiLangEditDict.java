package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.iPartsDataDictTextKind;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.iPartsDataDictTextKindList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTextKindTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.dictionary.DictMultiLangEditForm;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.gui.controls.GuiButton;
import de.docware.framework.modules.gui.controls.GuiMultiLangEdit;
import de.docware.framework.modules.gui.controls.menu.GuiContextMenu;
import de.docware.framework.modules.gui.controls.menu.GuiMenuItem;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.utils.EtkMultiSprache;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

/**
 * Erweitert {@GuiMultiLangEdit} um die Möglichkeit, einen Text im Lexikon zu suchen/neu anzulegen und im Formular zu löschen
 */
public class GuiMultiLangEditDict extends GuiMultiLangEdit {

    public static final String TYPE = "multieditdict";

    private static final EnumSet<DictTextKindTypes> DEFAULT_TEXTKIND_TYPES = EnumSet.allOf(DictTextKindTypes.class);

    private AbstractJavaViewerFormIConnector dataConnector;
    private boolean withPreSearch;
    private EnumSet<DictTextKindTypes> textKindTypes = DEFAULT_TEXTKIND_TYPES;
    private String tableForDictionary;
    private String foreignSourceForCreate;

    public GuiMultiLangEditDict() {
        setType(TYPE);
        this.withPreSearch = true;
    }

    public GuiMultiLangEditDict(EtkMultiSprache multi, Language startLanguage) {
        setValues(multi, startLanguage);
    }

    public GuiMultiLangEditDict(EtkMultiSprache multi) {
        setValues(multi, null);
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        super.setReadOnly(readOnly);
        getButton().setEnabled(!readOnly);
    }

    @Override
    protected void setValues(EtkMultiSprache etkMultiSprache, Language startLang) {
        if (etkMultiSprache != null) {
            // Sprachen mit leeren Texten entfernen
            etkMultiSprache = etkMultiSprache.cloneMe();
            etkMultiSprache.removeLanguagesWithEmptyTexts();
            if (etkMultiSprache.isEmpty()) {
                // Deutsch muss immer zumindest leer übrigbleiben
                etkMultiSprache.setText(Language.DE.getCode(), "");
            }
        }
        super.setValues(etkMultiSprache, startLang);
    }

    public void setSearchTextKindTypes(EnumSet<DictTextKindTypes> searchTextKindTypes) {
        if (searchTextKindTypes != null) {
            this.textKindTypes = searchTextKindTypes;
        } else {
            this.textKindTypes = DEFAULT_TEXTKIND_TYPES;
        }
    }

    public void setTableForDictionary(String tableForDictionary) {
        this.tableForDictionary = tableForDictionary;
    }

    public void setForeignSourceForCreate(String foreignSourceForCreate) {
        this.foreignSourceForCreate = foreignSourceForCreate;
    }

    /**
     * Der {@code dataConnector} wird benötigt für die Suche.
     * Erst wenn dieser gesetzt ist, wird der Button freigeschaltet.
     *
     * @param dataConnector
     */
    public void setDataConnector(AbstractJavaViewerFormIConnector dataConnector) {
        this.dataConnector = dataConnector;
        if (dataConnector != null) {
            modifyDictionaryButton();
            setWithButton(true);
            setReadOnly(true);
        } else {
            setWithButton(false);
        }
    }

    public boolean isWithPreSearch() {
        return withPreSearch;
    }

    public void setWithPreSearch(boolean withPreSearch) {
        this.withPreSearch = withPreSearch;
    }

    @Override
    public void setTextControlEditable(boolean editable) {
        if (dataConnector != null) {
            editable = false;
        }
        super.setTextControlEditable(editable);
    }

    /**
     * Callback für Button => Suche anzeigen
     */
    private void modifyDictionaryButton() {
        GuiButton buttonDictionaryEdit = getButton();
        buttonDictionaryEdit.addEventListener(new EventListener(Event.ACTION_PERFORMED_EVENT) {
            public void fire(Event event) {
                String searchText = "";
                EtkProject project = dataConnector.getProject();
                if (withPreSearch) {
                    searchText = getMultiText(getSelectedLanguage().getCode());
                }
                Collection<iPartsDataDictTextKind> textKindList = iPartsDataDictTextKindList.loadSpecialTextKindListSortedByName(project,
                                                                                                                                 textKindTypes);
                // Textart aus der aktuellen (optionalen) Selektion ermitteln
                DictTextKindTypes initialType = textKindTypes.iterator().next();
                EtkMultiSprache multiLang = DictMultiLangEditForm.showDictMultiLangEditForCreate(dataConnector, dataConnector.getActiveForm(),
                                                                                                 getSelectedLanguage(), searchText, initialType,
                                                                                                 textKindList, true, foreignSourceForCreate, false,
                                                                                                 tableForDictionary);
                if (multiLang != null) {
                    setValues(multiLang, null);
                    getTextControl().fireEvent(new Event(Event.ON_CHANGE_EVENT));
                }
            }
        });

        // den Löschen-Button einhängen
        GuiContextMenu contextMenu = new GuiContextMenu();
        GuiMenuItem menuItem = new GuiMenuItem();
        menuItem.setText("!!Löschen");
        menuItem.addEventListener(new EventListener(Event.MENU_ITEM_EVENT) {
            @Override
            public void fire(Event event) {
                Set<String> sprachen = getMultiLanguage().getSprachen();
                EtkMultiSprache multi = new EtkMultiSprache("", sprachen);

                // hier bleibt leider die TextId stehen (wird in fillAttribByEditControlValue() behoben
                setValues(multi, null);
                getTextControl().fireEvent(new Event(Event.ON_CHANGE_EVENT));
            }
        });
        contextMenu.addChild(menuItem);
        buttonDictionaryEdit.setContextMenu(contextMenu);
        buttonDictionaryEdit.setTooltip("!!Wiederverwendbare Texte suchen");
    }
}
