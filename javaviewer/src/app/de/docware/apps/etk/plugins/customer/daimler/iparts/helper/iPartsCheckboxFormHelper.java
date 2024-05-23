package de.docware.apps.etk.plugins.customer.daimler.iparts.helper;

import de.docware.framework.modules.gui.controls.GuiCheckbox;
import de.docware.framework.modules.gui.controls.GuiLabel;
import de.docware.framework.modules.gui.controls.GuiSeparator;
import de.docware.framework.modules.gui.controls.misc.DWOrientation;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;

import java.util.List;

public class iPartsCheckboxFormHelper {

    public static void setCheckboxValue(GuiCheckbox checkbox, boolean value) {
        checkbox.switchOffEventListeners();
        checkbox.setSelected(value);
        checkbox.switchOnEventListeners();
    }

    public static void setSelectionForAll(List<GuiCheckbox> checkboxes, boolean selectionForAll) {
        for (GuiCheckbox checkbox : checkboxes) {
            setCheckboxValue(checkbox, selectionForAll);
        }
    }

    public static GuiLabel makeLabel(int gridY, String name, String text, TranslationHandler translationHandler) {
        ConstraintsGridBag constraintsGridBagLabel =
                new ConstraintsGridBag(1, gridY, 1, 1, 0.0, 0.0, ConstraintsGridBag.ANCHOR_WEST, ConstraintsGridBag.FILL_HORIZONTAL, 4, 4, 4, 8);
        GuiLabel label = new GuiLabel();
        label.setName("label_" + name);
        label.__internal_setGenerationDpi(96);
        label.registerTranslationHandler(translationHandler);
        label.setScaleForResolution(true);
        label.setMinimumWidth(10);
        label.setMinimumHeight(10);
        label.setText(text);
        label.setConstraints(constraintsGridBagLabel);
        return label;
    }

    public static GuiCheckbox makeCheckbox(int gridY, String name, TranslationHandler translationHandler) {
        ConstraintsGridBag constraintsGridBagCheckBox =
                new ConstraintsGridBag(0, gridY, 1, 1, 0.0, 0.0, ConstraintsGridBag.ANCHOR_EAST, ConstraintsGridBag.FILL_NONE, 4, 8, 4, 4);
        GuiCheckbox checkbox = new GuiCheckbox();
        checkbox.setName("checkbox_" + name);
        checkbox.__internal_setGenerationDpi(96);
        checkbox.registerTranslationHandler(translationHandler);
        checkbox.setScaleForResolution(true);
        checkbox.setMinimumWidth(10);
        checkbox.setMinimumHeight(10);
        checkbox.setConstraints(constraintsGridBagCheckBox);
        return checkbox;
    }

    public static GuiCheckbox createSelectAllCheckbox(List<GuiCheckbox> checkboxes, int gridY, String name,
                                                      TranslationHandler translationHandler) {
        GuiCheckbox checkboxAll = makeCheckbox(gridY, name, translationHandler);
        checkboxAll.addEventListener(new EventListener(Event.ON_CHANGE_EVENT) {
            @Override
            public void fire(Event event) {
                setSelectionForAll(checkboxes, checkboxAll.isSelected());
            }
        });
        return checkboxAll;
    }

    public static GuiSeparator createSeparator(int gridY) {
        GuiSeparator separator = new GuiSeparator(DWOrientation.HORIZONTAL);
        ConstraintsGridBag constraintsGridBagSeparator =
                new ConstraintsGridBag(0, gridY, 2, 1, 0.0, 0.0, ConstraintsGridBag.ANCHOR_CENTER, ConstraintsGridBag.FILL_HORIZONTAL, 0, 0, 0, 0);
        separator.setConstraints(constraintsGridBagSeparator);
        return separator;
    }
}
