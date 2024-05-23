/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.iPartsStructure;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.iPartsStructureId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.iPartsStructureNode;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.GuiImage;
import de.docware.framework.modules.gui.controls.GuiLabel;
import de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonOnPanel;
import de.docware.framework.modules.gui.controls.tree.GuiTree;
import de.docware.framework.modules.gui.controls.tree.GuiTreeNode;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventCreator;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.event.EventListeners;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.session.Session;
import de.docware.util.collections.dwlist.DwList;

import java.util.List;

/**
 * Dialog zur Auswahl eines Strukturknotens zum Einhängen von einem Produkt.
 */
public class EditStructureTreeSelectDialog extends AbstractJavaViewerForm {

    private class TreeNodeWithId extends GuiTreeNode {

        private iPartsStructureId structureId;
        private boolean isValidNodeForProduct = false;

        public TreeNodeWithId() {
            super();
        }

        public TreeNodeWithId(iPartsStructureNode structureNode) {
            this(new GuiLabel(structureNode.getTitle().getText(getProject().getViewerLanguage())));
            setStructureId(structureNode.getId());
        }

        public TreeNodeWithId(AbstractGuiControl displayObject) {
            super(displayObject);
        }

        public TreeNodeWithId(AbstractGuiControl displayObject, GuiImage image) {
            super(displayObject, image);
        }

        public TreeNodeWithId(AbstractGuiControl displayObject, List<GuiImage> images) {
            super(displayObject, images);
        }

        public TreeNodeWithId(AbstractGuiControl displayObject, GuiImage... images) {
            super(displayObject, images);
        }

        public iPartsStructureId getStructureId() {
            return structureId;
        }

        public void setStructureId(iPartsStructureId structureId) {
            this.structureId = structureId;
        }

        public boolean isValidNodeForProduct() {
            return isValidNodeForProduct;
        }

        public void setValidNodeForProduct(boolean isValidNodeForProduct) {
            this.isValidNodeForProduct = isValidNodeForProduct;
        }
    }

    // Die Instanz die die onChangeEvents Aktionen abarbeitet
    protected EventListeners eventOnChangeListeners;

    /**
     * Erzeugt eine Instanz von EditStructureTreeSelectDialog.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     */
    public EditStructureTreeSelectDialog(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm) {
        super(dataConnector, parentForm);
        $$internalCreateGui$$(null);
        this.eventOnChangeListeners = new EventListeners();
        postCreateGui();
    }

    /**
     * Hier kann eigener Code stehen der gerufen wird wenn die Instanz
     * erzeugt wurde.
     */
    private void postCreateGui() {
        mainWindow.secondTitlePanel.setVisible(false);
        fillTree();
        treeSelectionChange(null);
    }

    @Override
    public AbstractGuiControl getGui() {
        return mainWindow.panelMain;
    }

    public ModalResult showModal() {
        ModalResult modalResult = mainWindow.showModal();
        super.close();
        return modalResult;
    }

    @Override
    public void close() {
        mainWindow.setVisible(false);
        super.close();
    }

    public void setTitle(String title) {
        mainWindow.title.setTitle(title);
    }

    public void setSecondTitle(String title) {
        mainWindow.secondTitleLabel.setText(title);
        mainWindow.secondTitlePanel.setVisible(true);
    }

    public boolean isSecondTitleVisible() {
        return mainWindow.secondTitlePanel.isVisible();
    }

    public iPartsStructureId getSelectedStructureId() {
        TreeNodeWithId selectedNode = (TreeNodeWithId)getTree().getSelectedNode();
        if ((selectedNode != null) && selectedNode.isValidNodeForProduct()) {
            return selectedNode.getStructureId();
        }
        return null;
    }

    public void setSelectedStructure(iPartsStructureId id) {
        List<String> selectionPath = findPath(id);
        if (!selectionPath.isEmpty()) {
            getTree().setSelectionPath(selectionPath);
            treeSelectionChange(null);
        }
    }

    public void setEnabled(boolean enabled) {
        getTree().setEnabled(enabled);
    }

    public boolean isEnabled() {
        return getTree().isEnabled();
    }

    /**
     * Füge den übergebenen Eventlistener hinzu
     */
    public void addEventListener(EventListener eventListener) {
        if (eventListener.getType().equals(Event.ON_CHANGE_EVENT)) {
            eventOnChangeListeners.addEventListener(eventListener);
        }
    }

    /**
     * Entferne den übergebenen Eventlistener
     */
    public void removeEventListener(EventListener eventListener) {
        if (eventListener.getType().equals(Event.ON_CHANGE_EVENT)) {
            eventOnChangeListeners.removeEventListener(eventListener);
        }
    }

    protected GuiTree getTree() {
        return mainWindow.tree;
    }

    protected void setComment(String msg) {
        mainWindow.labelComment.setText(msg);
    }

    private void fireChangeEvents() {
        // Eigenen Kind-Thread starten, weil der aufrufende (Warte-)Thread ansonsten zwischendrin abgebrochen werden könnte,
        // was z.B. bei IO-Aktionen Exceptions verursacht
        Session.startChildThreadInSession(thread -> {
            if (eventOnChangeListeners.isActive()) {
                final List<EventListener> listeners = eventOnChangeListeners.getListeners(Event.ON_CHANGE_EVENT);
                if (!listeners.isEmpty()) {
                    Session.invokeThreadSafeInSession(() -> {
                        Event onChangeEvent = EventCreator.createOnChangeEvent(mainWindow.tree, mainWindow.tree.getUniqueId());
                        for (EventListener listener : listeners) {
                            listener.fire(onChangeEvent);
                        }
                    });
                }
            }
        });
    }

    private void fillTree() {
        iPartsStructure structure = iPartsStructure.getInstance(getProject());
        iPartsStructureNode rootStructureNode = structure.getRootNode();
        String nodeTitle = rootStructureNode.getTitle().getText(getProject().getViewerLanguage());
        if ((nodeTitle == null) || nodeTitle.isEmpty()) {
            nodeTitle = getProject().getConfig().getCatalogRootNodeText();
        }
        TreeNodeWithId rootNode = new TreeNodeWithId(new GuiLabel(nodeTitle));
        rootNode.setStructureId(rootStructureNode.getId());
        getTree().addRootNode(rootNode);
        addSubChildren(rootStructureNode, rootNode);
    }

    private void addSubChildren(iPartsStructureNode structureParentNode, TreeNodeWithId parentNode) {
        for (iPartsStructureNode structureNode : structureParentNode.getChildren()) {
            if (!structureNode.isConstructionNode()) {
                TreeNodeWithId node = new TreeNodeWithId(structureNode);
                if (!structureNode.isMissingNode()) {
                    node.setValidNodeForProduct(structureNode.getChildren().isEmpty());
                    parentNode.addChild(node);
                    addSubChildren(structureNode, node);
                }
            }
        }
    }

    public List<String> findPath(iPartsStructureId id) {
        List<String> selectionPath = new DwList<String>();
        if ((id != null) && id.isValidId()) {
            List<GuiTreeNode> rootNodes = getTree().getRootNodes();
            TreeNodeWithId rootNode = (TreeNodeWithId)rootNodes.get(0);
            GuiTreeNode findNode = searchInAllNodes(rootNode, id);
            while (findNode != null) {
                selectionPath.add(0, ((GuiLabel)findNode.getDisplayObject()).getText());
                findNode = findNode.getParentNode();
            }
        }
        return selectionPath;
    }

    private GuiTreeNode searchInAllNodes(GuiTreeNode node, iPartsStructureId id) {
        if (((TreeNodeWithId)node).getStructureId().equals(id)) {
            return node;
        }
        if (node.getChildren() != null) {
            for (GuiTreeNode childNode : node.getChildren()) {
                GuiTreeNode foundNode = searchInAllNodes(childNode, id);
                if (foundNode != null) {
                    return foundNode;
                }
            }
        }
        return null;
    }

    private void treeSelectionChange(Event event) {
        boolean enabled = false;
        TreeNodeWithId selectedNode = (TreeNodeWithId)getTree().getSelectedNode();
        if (selectedNode != null) {
            if (isValidNode(selectedNode)) {
                setComment(TranslationHandler.translate("!!Knoten \"%1\" ausgewählt", ((GuiLabel)selectedNode.getDisplayObject()).getText()));
                enabled = true;
            } else {
                setComment(TranslationHandler.translate("!!Kein gültiger Knoten ausgewählt"));
            }
        } else {
            setComment(TranslationHandler.translate("!!Kein Knoten ausgewählt"));
        }
        enableButtons(enabled);
        fireChangeEvents();
    }

    private void enableButtons(boolean enabled) {
        mainWindow.buttonpanel.setButtonEnabled(GuiButtonOnPanel.ButtonType.OK, enabled);
    }

    private boolean isValidNode(TreeNodeWithId node) {
        return node.isValidNodeForProduct();
    }

    //<editor-fold defaultstate="collapsed" desc="Framework Gui Code">
    /* START: DOCWARE GUI CODE, DO NOT MODIFY THIS COMMENT AND/OR CODE BELOW THIS LINE */
    protected void $$internalCreateGui$$(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
        mainWindow = new MainWindowClass(translationHandler);
        mainWindow.__internal_setGenerationDpi(96);
    }

    @SuppressWarnings({ "FieldCanBeLocal" })
    protected MainWindowClass mainWindow;

    private class MainWindowClass extends de.docware.framework.modules.gui.controls.GuiWindow {

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTitle title;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelMain;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel secondTitlePanel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel secondTitleLabel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiSeparator separator;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiScrollPane scrollpaneTree;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.tree.GuiTree tree;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelComment;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel buttonpanel;

        private MainWindowClass(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
            super();
            this.registerTranslationHandler(translationHandler);
            this.setScaleForResolution(true);
            this.setVisible(false);
            de.docware.framework.modules.gui.layout.LayoutBorder mainWindowLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            this.setLayout(mainWindowLayout);
            title = new de.docware.framework.modules.gui.controls.GuiTitle();
            title.setName("title");
            title.__internal_setGenerationDpi(96);
            title.registerTranslationHandler(translationHandler);
            title.setScaleForResolution(true);
            title.setMinimumWidth(10);
            title.setMinimumHeight(50);
            title.setTitle("...");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder titleConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            titleConstraints.setPosition("north");
            title.setConstraints(titleConstraints);
            this.addChild(title);
            panelMain = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelMain.setName("panelMain");
            panelMain.__internal_setGenerationDpi(96);
            panelMain.registerTranslationHandler(translationHandler);
            panelMain.setScaleForResolution(true);
            panelMain.setMinimumWidth(10);
            panelMain.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutBorder panelMainLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelMain.setLayout(panelMainLayout);
            secondTitlePanel = new de.docware.framework.modules.gui.controls.GuiPanel();
            secondTitlePanel.setName("secondTitlePanel");
            secondTitlePanel.__internal_setGenerationDpi(96);
            secondTitlePanel.registerTranslationHandler(translationHandler);
            secondTitlePanel.setScaleForResolution(true);
            secondTitlePanel.setMinimumWidth(10);
            secondTitlePanel.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutBorder secondTitlePanelLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            secondTitlePanel.setLayout(secondTitlePanelLayout);
            secondTitleLabel = new de.docware.framework.modules.gui.controls.GuiLabel();
            secondTitleLabel.setName("secondTitleLabel");
            secondTitleLabel.__internal_setGenerationDpi(96);
            secondTitleLabel.registerTranslationHandler(translationHandler);
            secondTitleLabel.setScaleForResolution(true);
            secondTitleLabel.setMinimumWidth(10);
            secondTitleLabel.setMinimumHeight(10);
            secondTitleLabel.setBackgroundColor(new de.docware.framework.modules.gui.misc.color.FrameworkConstantColor("clHighlightText"));
            secondTitleLabel.setFontSize(12);
            secondTitleLabel.setFontStyle(1);
            secondTitleLabel.setPaddingTop(8);
            secondTitleLabel.setPaddingLeft(8);
            secondTitleLabel.setPaddingRight(8);
            secondTitleLabel.setPaddingBottom(8);
            secondTitleLabel.setText("!!Verortung");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder secondTitleLabelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            secondTitleLabelConstraints.setPosition("north");
            secondTitleLabel.setConstraints(secondTitleLabelConstraints);
            secondTitlePanel.addChild(secondTitleLabel);
            separator = new de.docware.framework.modules.gui.controls.GuiSeparator();
            separator.setName("separator");
            separator.__internal_setGenerationDpi(96);
            separator.registerTranslationHandler(translationHandler);
            separator.setScaleForResolution(true);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder separatorConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            separatorConstraints.setPosition("south");
            separator.setConstraints(separatorConstraints);
            secondTitlePanel.addChild(separator);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder secondTitlePanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            secondTitlePanelConstraints.setPosition("north");
            secondTitlePanel.setConstraints(secondTitlePanelConstraints);
            panelMain.addChild(secondTitlePanel);
            scrollpaneTree = new de.docware.framework.modules.gui.controls.GuiScrollPane();
            scrollpaneTree.setName("scrollpaneTree");
            scrollpaneTree.__internal_setGenerationDpi(96);
            scrollpaneTree.registerTranslationHandler(translationHandler);
            scrollpaneTree.setScaleForResolution(true);
            scrollpaneTree.setMinimumWidth(10);
            scrollpaneTree.setMinimumHeight(10);
            tree = new de.docware.framework.modules.gui.controls.tree.GuiTree();
            tree.setName("tree");
            tree.__internal_setGenerationDpi(96);
            tree.registerTranslationHandler(translationHandler);
            tree.setScaleForResolution(true);
            tree.setMinimumWidth(10);
            tree.setMinimumHeight(10);
            tree.setPaddingTop(4);
            tree.setPaddingLeft(4);
            tree.setPaddingRight(4);
            tree.addEventListener(new de.docware.framework.modules.gui.event.EventListener("treeSelectionEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    treeSelectionChange(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder treeConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            tree.setConstraints(treeConstraints);
            scrollpaneTree.addChild(tree);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder scrollpaneTreeConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            scrollpaneTree.setConstraints(scrollpaneTreeConstraints);
            panelMain.addChild(scrollpaneTree);
            labelComment = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelComment.setName("labelComment");
            labelComment.__internal_setGenerationDpi(96);
            labelComment.registerTranslationHandler(translationHandler);
            labelComment.setScaleForResolution(true);
            labelComment.setMinimumWidth(10);
            labelComment.setMinimumHeight(10);
            labelComment.setPaddingTop(8);
            labelComment.setPaddingLeft(8);
            labelComment.setPaddingRight(8);
            labelComment.setPaddingBottom(8);
            labelComment.setText("!!Nichts ausgewählt");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder labelCommentConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            labelCommentConstraints.setPosition("south");
            labelComment.setConstraints(labelCommentConstraints);
            panelMain.addChild(labelComment);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelMainConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelMain.setConstraints(panelMainConstraints);
            this.addChild(panelMain);
            buttonpanel = new de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel();
            buttonpanel.setName("buttonpanel");
            buttonpanel.__internal_setGenerationDpi(96);
            buttonpanel.registerTranslationHandler(translationHandler);
            buttonpanel.setScaleForResolution(true);
            buttonpanel.setMinimumWidth(10);
            buttonpanel.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder buttonpanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            buttonpanelConstraints.setPosition("south");
            buttonpanel.setConstraints(buttonpanelConstraints);
            this.addChild(buttonpanel);
        }

    }
    /* END: DOCWARE GUI CODE, DO NOT MODIFY THIS COMMENT AND/OR CODE ABOVE THIS LINE */
    //</editor-fold>
}