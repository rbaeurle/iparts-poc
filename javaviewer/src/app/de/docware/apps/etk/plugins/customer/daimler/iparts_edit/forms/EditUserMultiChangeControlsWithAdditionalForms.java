package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.EditControl;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditFields;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.gui.controls.*;
import de.docware.framework.modules.gui.layout.LayoutBorder;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag;
import de.docware.framework.modules.gui.misc.color.FrameworkConstantColor;
import de.docware.framework.modules.gui.misc.resources.FrameworkConstantImage;
import de.docware.framework.utils.FrameworkUtils;

import java.awt.*;

/**
 * Liefert Methoden, um an die Vereinheitlichen Dialoge noch zusätzliche Forms hinzuzufügen
 */
public class EditUserMultiChangeControlsWithAdditionalForms extends EditUserMultiChangeControls {

    public EditUserMultiChangeControlsWithAdditionalForms(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm, EtkEditFields externalEditFields,
                                                          DBDataObjectAttributes initialAttributes, boolean startPostPostCreateGui, boolean fieldActivatesCheckbox,
                                                          boolean allowEmptyEnumValues, UnifySource unifySource) {
        super(dataConnector, parentForm, externalEditFields, initialAttributes, startPostPostCreateGui, fieldActivatesCheckbox, allowEmptyEnumValues, unifySource);
    }

    protected GuiDockingPanel createDockingControl(String textHide, String textShow) {
        GuiDockingPanel dockingPanel = new GuiDockingPanel();
        dockingPanel.setBackgroundColor(new java.awt.Color(255, 255, 255, 255));
        dockingPanel.setForegroundColor(new java.awt.Color(0, 0, 0, 255));
        dockingPanel.setTextHide(textHide);
        dockingPanel.setTextShow(textShow);
        dockingPanel.setImageHide(new FrameworkConstantImage("imgDesignDockingPanelSouth"));
        dockingPanel.setImageShow(new FrameworkConstantImage("imgDesignDockingPanelNorth"));
        dockingPanel.setDockPosition(de.docware.framework.modules.gui.controls.misc.DWBorderPosition.NORTH);
        dockingPanel.setButtonBackgroundColor(new FrameworkConstantColor("clDesignButtonDisabledBackgroundGradient1"));
        dockingPanel.setButtonForegroundColor(new FrameworkConstantColor("clDesignButtonBorderSelected"));
        dockingPanel.setButtonFill(true);
        dockingPanel.setStartWithArrow(false);
        return dockingPanel;
    }

    protected void addPanelAsSplitPaneElement(GuiPanel panel) {
        Dimension screenSize = FrameworkUtils.getScreenSize();
        int height = (int)(screenSize.getHeight() / 100) * 100;
        height = getCalculatedHeight(height);
        int dividerPosition = getDividerPosition(height);
        dividerPosition += 30; // etwas mehr Platz, damit Divider nicht direkt anliegt
        addChildAsSplitPaneElement(panel, dividerPosition, height);
    }

    protected void addFormDialogWithCheckbox(GuiPanel panel, AbstractGuiControl control, GuiCheckbox checkbox, String labelText) {
        if (control != null) {
            int insetLeft = 36; // 36px für horizontale Ausrichtung mit den Checkboxen vom Vereinheitlichen
            int insetTop = 4;
            int insetBottom = 0;
            panel.addChildGridBag(checkbox, 0, 0, 1, 1, 0, 0, ConstraintsGridBag.ANCHOR_NORTH, ConstraintsGridBag.FILL_NONE, insetTop, insetLeft, insetBottom, 4);
            // Label für die Form
            GuiLabel label = createLabel("dataLabel", labelText);
            panel.addChildGridBag(label, 1, 0, 2, 1, 100, 0, ConstraintsGridBag.ANCHOR_WEST, ConstraintsGridBag.FILL_HORIZONTAL, insetTop, 4, insetBottom, 8);

            GuiPanel helpPanel = new GuiPanel();
            helpPanel.setLayout(new LayoutBorder());
            GuiScrollPane scrollPane = new GuiScrollPane();
            scrollPane.addChild(helpPanel);
            helpPanel.addChildBorderCenter(control);
            panel.addChildGridBag(scrollPane, 0, 1, 3, 1, 100, 100, ConstraintsGridBag.ANCHOR_CENTER, ConstraintsGridBag.FILL_BOTH, 0, 8, insetBottom, 8);
        }
    }

    @Override
    protected void addEditControlChild(EditControl ctrl) {
        // Alle Controls sollen einheitlich breit dargestellt werden, wobei 600 Pixel aufgrund von den zusätzlichen Forms eine
        // gute Ansicht ergibt.
        AbstractGuiControl control = ctrl.getEditControl().getControl();
        control.setMinimumWidth(600);
        super.addEditControlChild(ctrl);
    }

    @Override
    protected ConstraintsGridBag createHeadingConstraints() {
        ConstraintsGridBag headingConstraints = super.createHeadingConstraints();
        // Spart Platz, indem das Label die ersten beiden Spalten überspannt
        headingConstraints.setGridwidth(2);
        headingConstraints.setAnchor(ConstraintsGridBag.ANCHOR_WEST);
        return headingConstraints;
    }
}
