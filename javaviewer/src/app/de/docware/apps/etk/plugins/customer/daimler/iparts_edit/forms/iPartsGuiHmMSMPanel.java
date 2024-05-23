package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataDialogDataList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.HmMSm;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.HmMSmId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.HmMSmNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.gui.controls.GuiComboBox;
import de.docware.framework.modules.gui.controls.GuiEqualDimensionPanel;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.utils.VarParam;
import de.docware.util.StrUtils;
import de.docware.util.sql.TableAndFieldName;

import java.util.*;

public class iPartsGuiHmMSMPanel extends GuiEqualDimensionPanel {

    public static final String TYPE = "hmmsmedit";

    /**
     * hier wird der Text für die Comboboxen erzeugt
     *
     * @param node
     * @param language
     * @return
     */
    static public String buildHmMSmComboText(HmMSmNode node, String language, Set<String> positivValues) {
        if (node != null) {
            String suffix = "";
            if (positivValues != null) {
                if (!positivValues.contains(node.getNumber())) {
                    suffix = " *";
                }
            }
            return node.getNumber() + " - " + node.getTitle().getText(language) + suffix;
        }
        return "";
    }

    // Defaultwerte
    private String startLanguageDefaultValue = Language.DE.getCode();

    // Spezifische Eigenschaften der Komponente
    private String startLanguage = startLanguageDefaultValue;

    // Weitere benötigte Variablen
    private GuiComboBox<Object> combobox_HM;
    private GuiComboBox<Object> combobox_M;
    private GuiComboBox<Object> combobox_SM;

    private EtkProject project;
    private HmMSm hmMSm = null;
    private HmMSmId startHmMSmId = null;
    private iPartsSeriesId seriesId = null;
    private Map<String, Map<String, Set<String>>> positivHmMSmMap;
    private Set<String> doneHM;

    public iPartsGuiHmMSMPanel() {
        super();
        setType(TYPE);
        initGui();
    }

    /**
     * Hier kann eigener Code stehen der gerufen wird wenn die Instanz
     * erzeugt wurde.
     */
    private void initGui() {
        setMinimumWidth(10);
        setMinimumHeight(20);
        setHorizontal(true);
        combobox_HM = createComboHmMSm(new EventListener(Event.ON_CHANGE_EVENT) {
            @Override
            public void fire(Event event) {
                onChange_CB_HM(event);
            }
        }, "hm");
        this.addChild(combobox_HM);
        clearComboBox(combobox_HM);
        combobox_M = createComboHmMSm(new EventListener(Event.ON_CHANGE_EVENT) {
            @Override
            public void fire(Event event) {
                onChange_CB_M(event);
            }
        }, "m");
        this.addChild(combobox_M);
        clearComboBox(combobox_M);
        combobox_SM = createComboHmMSm(new EventListener(Event.ON_CHANGE_EVENT) {
            @Override
            public void fire(Event event) {
                onChange_CB_SM(event);
            }
        }, "sm");
        this.addChild(combobox_SM);
        clearComboBox(combobox_SM);
    }

    /**
     * Notwendiger Aufruf, bevor das EinPasPanel aktiv wird
     * Wegen EtkProject als eigene Routine
     *
     * @param project
     */
    public void init(EtkProject project, iPartsSeriesId seriesId) {
        if ((seriesId != null) && seriesId.isValidId()) {
            this.project = project;
            hmMSm = HmMSm.getInstance(project, seriesId);
            this.seriesId = seriesId;
            Collection<HmMSmNode> hmNodes = hmMSm.getHMNodeList();
            // besetzen der HG-Combobox + Vorbereitung G-/TU-Combobox
            fillComboBoxItems(combobox_HM, hmNodes, null);
            clearComboBox(combobox_M);
            clearComboBox(combobox_SM);
            positivHmMSmMap = new HashMap<>();
            doneHM = new TreeSet<>();
            setStartHmMSmId(startHmMSmId);
//            buildPositivList(project, "02", null, null);
        }
    }

    private void buildPositivList(EtkProject project, String hm, String m, String sm) {
        final Set<HmMSmId> result = new TreeSet<>();
        iPartsDataDialogDataList list = new iPartsDataDialogDataList();
        list.clear(DBActionOrigin.FROM_DB);
        list.setSearchWithoutActiveChangeSets(true);

        EtkDisplayFields selectFields = new EtkDisplayFields();
        selectFields.addFeld(SimpleMasterDataSearchResultGrid.createSearchField(project, iPartsConst.TABLE_DA_DIALOG, iPartsConst.FIELD_DD_HM, false, false));
        selectFields.addFeld(SimpleMasterDataSearchResultGrid.createSearchField(project, iPartsConst.TABLE_DA_DIALOG, iPartsConst.FIELD_DD_M, false, false));
        selectFields.addFeld(SimpleMasterDataSearchResultGrid.createSearchField(project, iPartsConst.TABLE_DA_DIALOG, iPartsConst.FIELD_DD_SM, false, false));
        String[] whereTableAndFields = new String[]{ TableAndFieldName.make(iPartsConst.TABLE_DA_DIALOG, iPartsConst.FIELD_DD_SERIES_NO) };
        String[] whereValues = new String[]{ seriesId.getSeriesNumber() };
        if (StrUtils.isValid(hm)) {
            whereTableAndFields = mergeArrays(whereTableAndFields, new String[]{ TableAndFieldName.make(iPartsConst.TABLE_DA_DIALOG, iPartsConst.FIELD_DD_HM) });
            whereValues = mergeArrays(whereValues, new String[]{ hm });
            doneHM.add(hm);
        }
        if (StrUtils.isValid(m)) {
            whereTableAndFields = mergeArrays(whereTableAndFields, new String[]{ TableAndFieldName.make(iPartsConst.TABLE_DA_DIALOG, iPartsConst.FIELD_DD_M) });
            whereValues = mergeArrays(whereValues, new String[]{ m });
        }
        if (StrUtils.isValid(sm)) {
            whereTableAndFields = mergeArrays(whereTableAndFields, new String[]{ TableAndFieldName.make(iPartsConst.TABLE_DA_DIALOG, iPartsConst.FIELD_DD_SM) });
            whereValues = mergeArrays(whereValues, new String[]{ sm });
        }
        final VarParam<Integer> counter = new VarParam<>(0);
        list.searchSortAndFillWithJoin(project, null, selectFields, whereTableAndFields, whereValues,
                                       false, null, false,
                                       new EtkDataObjectList.FoundAttributesCallback() {
                                           @Override
                                           public boolean foundAttributes(DBDataObjectAttributes attributes) {
                                               HmMSmId hmMSmId = new HmMSmId(seriesId.getSeriesNumber(),
                                                                             attributes.getFieldValue(iPartsConst.FIELD_DD_HM),
                                                                             attributes.getFieldValue(iPartsConst.FIELD_DD_M),
                                                                             attributes.getFieldValue(iPartsConst.FIELD_DD_SM));
                                               result.add(hmMSmId);
                                               counter.setValue(counter.getValue() + 1);
//                                               Thread.currentThread().interrupt();
                                               return false;
                                           }
                                       });
        if (!result.isEmpty()) {
            for (HmMSmId hmMSmId : result) {
                String currentHM = hmMSmId.getHm();
                if (StrUtils.isValid(currentHM)) {
                    Map<String, Set<String>> hmMap = positivHmMSmMap.get(currentHM);
                    if (hmMap == null) {
                        hmMap = new HashMap<>();
                        positivHmMSmMap.put(currentHM, hmMap);
                    }
                    String currentM = hmMSmId.getM();
                    if (StrUtils.isValid(currentM)) {
                        Set<String> smList = hmMap.get(currentM);
                        if (smList == null) {
                            smList = new TreeSet<>();
                            hmMap.put(currentM, smList);
                        }
                        if (hmMSmId.isSmNode()) {
                            smList.add(hmMSmId.getSm());
                        }
                    }
                }
            }
        }
    }

    private String[] mergeArrays(String[] array1, String[] array2) {
        if (array1 != null) {
            return StrUtils.mergeArrays(array1, array2);
        }
        return array2;
    }


    /**
     * Überprüfungsfunktion, ob EinPasPanel initialisiert ist
     *
     * @return
     */
    public boolean isInit() {
        return hmMSm != null;
    }

    /**
     * Test, ob alle EinPas Felder besetzt sind
     *
     * @return
     */
    public boolean isValid() {
        if (isInit()) {
            return (getSelectedHMNumber() != null) && (getSelectedMNumber() != null) &&
                   (getSelectedSMNumber() != null);
        }
        return false;
    }

    /**
     * Selektierte HG-Nummer holen
     *
     * @return
     */
    public String getHM() {
        if (isValid()) {
            return getSelectedHMNumber();
        }
        return null;
    }

    /**
     * Selektierte G-Nummer holen
     *
     * @return
     */
    public String getM() {
        if (isValid()) {
            return getSelectedMNumber();
        }
        return null;
    }

    /**
     * Selektierte TU-Nummer holen
     *
     * @return
     */
    public String getSM() {
        if (isValid()) {
            return getSelectedSMNumber();
        }
        return null;
    }

    /**
     * Liefert Ergebnis als EinPasId
     *
     * @return
     */
    public HmMSmId getHmMSmId() {
        if (isValid()) {
            return new HmMSmId(seriesId.getSeriesNumber(), getSelectedHMNumber(), getSelectedMNumber(), getSelectedSMNumber());
        }
        return null;
    }

    public void setStartHmMSmId(HmMSmId startHmMSmId) {
        if (startHmMSmId != null) {
            if (isInit()) {
                HmMSmNode node = hmMSm.getHmNode(startHmMSmId.getHm());
                setSelectedIndexByNode(combobox_HM, node);
                node = hmMSm.getMNode(startHmMSmId.getHm(), startHmMSmId.getM());
                setSelectedIndexByNode(combobox_M, node);
                node = hmMSm.getSmNode(startHmMSmId.getHm(), startHmMSmId.getM(), startHmMSmId.getSm());
                setSelectedIndexByNode(combobox_SM, node);
                this.startHmMSmId = null;
            } else {
                this.startHmMSmId = startHmMSmId;
            }
        }
    }

    private void setSelectedIndexByNode(GuiComboBox combobox, HmMSmNode node) {
        if (node != null) {
            int index = -1;
            for (int lfdNr = 0; lfdNr < combobox.getItemCount(); lfdNr++) {
                HmMSmNode actNode = (HmMSmNode)combobox.getUserObject(lfdNr);
                if (actNode.getNumber().equals(node.getNumber())) {
                    index = lfdNr;
                    break;
                }
            }
            if (index != -1) {
                combobox.setSelectedIndex(index);
            }
        }
    }


    /**
     * Callback für HG-Combobox
     * (=> besetzen der G-Combobox)
     *
     * @param event
     */
    private void onChange_CB_HM(Event event) {
        String numberHMNode = getSelectedHMNumber();
        if (numberHMNode != null) {
            if (!doneHM.contains(numberHMNode)) {
                buildPositivList(project, numberHMNode, null, null);
                if (positivHmMSmMap.get(numberHMNode) == null) {
                    combobox_HM.switchOffEventListeners();
                    HmMSmNode selectedNode = getSelectedUserObject(combobox_HM);
                    String text = buildHmMSmComboText(selectedNode, startLanguage, new TreeSet<>());
                    int index = combobox_HM.getSelectedIndex();
                    combobox_HM.addItem(selectedNode, text, null, index);
                    combobox_HM.switchOnEventListeners();
                }
            }
            Collection<HmMSmNode> mNodes = hmMSm.getMNodeList(numberHMNode);
            Map<String, Set<String>> hmMap = positivHmMSmMap.get(numberHMNode);
            fillComboBoxItems(combobox_M, mNodes, hmMap.keySet());
            clearComboBox(combobox_SM);
            fireOnChangeEvent(event);
        }
    }

    /**
     * Callback für G-Combobox
     * (=> besetzen der TU-Combobox)
     *
     * @param event
     */
    private void onChange_CB_M(Event event) {
        String numberHMNode = getSelectedHMNumber();
        String numberMNode = getSelectedMNumber();
        if ((numberHMNode != null) && (numberMNode != null)) {
            Collection<HmMSmNode> smNodes = hmMSm.getSMNodeList(numberHMNode, numberMNode);
            Set<String> mPositiveList = null;
            Map<String, Set<String>> hmMap = positivHmMSmMap.get(numberHMNode);
            if (hmMap != null) {
                mPositiveList = hmMap.get(numberMNode);
                if (mPositiveList == null) {
                    mPositiveList = new TreeSet<>();
                }
            } else {
                mPositiveList = new TreeSet<>();
            }
            fillComboBoxItems(combobox_SM, smNodes, mPositiveList);
            fireOnChangeEvent(event);
        }
    }

    /**
     * Callback für TU-Combobox
     * (i.A. not used)
     *
     * @param event
     */
    private void onChange_CB_SM(Event event) {
        fireOnChangeEvent(event);
    }

    private void fireOnChangeEvent(Event event) {
        for (EventListener listener : eventListeners.getListeners(Event.ON_CHANGE_EVENT)) {
            listener.fire(event);
        }
    }


    /* Hilfsroutinen */

    private HmMSmNode getSelectedUserObject(GuiComboBox combobox) {
        int index = combobox.getSelectedIndex();
        if (index != -1) {
            return (HmMSmNode)combobox.getUserObject(index);
        }
        return null;
    }

    private String getSelectedNumber(GuiComboBox combobox) {
        HmMSmNode selectedNode = getSelectedUserObject(combobox);
        if (selectedNode != null) {
            return selectedNode.getNumber();
        }
        return null;
    }

    private String getSelectedHMNumber() {
        return getSelectedNumber(combobox_HM);
    }

    private String getSelectedMNumber() {
        return getSelectedNumber(combobox_M);
    }

    private String getSelectedSMNumber() {
        return getSelectedNumber(combobox_SM);
    }

    private void fillComboBoxItems(GuiComboBox combobox, Collection<HmMSmNode> nodes, Set<String> positivValues) {
        combobox.switchOffEventListeners();
        combobox.removeAllItems();
        if (nodes != null) {
            Iterator<HmMSmNode> iter = nodes.iterator();
            while (iter.hasNext()) {
                HmMSmNode node = iter.next();
                combobox.addItem(node, buildHmMSmComboText(node, startLanguage, positivValues));
            }
            combobox.setEnabled(true);
        } else {
            combobox.setEnabled(false);
        }
        combobox.setSelectedIndex(-1);
        combobox.switchOnEventListeners();
    }

    private void clearComboBox(GuiComboBox combobox) {
        combobox.removeAllItems();
        combobox.setEnabled(false);
    }

    /**
     * Erzeugung einer Combobox
     *
     * @param eventListener
     * @param nameSuffix
     * @return
     */
    private GuiComboBox<Object> createComboHmMSm(EventListener eventListener, String nameSuffix) {
        GuiComboBox<Object> combobox = new de.docware.framework.modules.gui.controls.GuiComboBox<Object>();
        combobox.setName("combobox" + nameSuffix);
        combobox.__internal_setGenerationDpi(96);
        combobox.registerTranslationHandler(translationHandler);
        combobox.setScaleForResolution(true);
        combobox.setMinimumWidth(0);
        combobox.setMinimumHeight(0);
        combobox.setMaximumWidth(2147483647);
        combobox.setMaximumHeight(2147483647);
        combobox.addEventListener(eventListener);
        de.docware.framework.modules.gui.layout.constraints.ConstraintsAbsolute combobox_Constraints =
                new de.docware.framework.modules.gui.layout.constraints.ConstraintsAbsolute(0, 0, 0, 0, 0);
        combobox.setConstraints(combobox_Constraints);
        return combobox;
    }

}
