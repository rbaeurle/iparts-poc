/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.config.drawing.EtkImageSettings;
import de.docware.apps.etk.base.mechanic.imageview.forms.SingleImageViewerPanel;
import de.docware.apps.etk.base.mechanic.imageview.forms.ThumbnailsImageViewerPanel;
import de.docware.apps.etk.base.mechanic.imageview.model.DefaultImageViewerItem;
import de.docware.apps.etk.base.mechanic.imageview.model.ImageViewerItem;
import de.docware.apps.etk.base.mechanic.imageview.model.ThumbnailsImageViewerConfig;
import de.docware.apps.etk.base.mechanic.imageview.model.UnsupportedImageViewerItem;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.mechanic.drawing.EtkDataImage;
import de.docware.apps.etk.base.project.mechanic.drawing.EtkDataPool;
import de.docware.framework.modules.gui.controls.GuiLabel;
import de.docware.framework.modules.gui.controls.GuiPanel;
import de.docware.framework.modules.gui.controls.GuiTextField;
import de.docware.framework.modules.gui.controls.GuiWindow;
import de.docware.framework.modules.gui.controls.misc.DWDimensionInt;
import de.docware.framework.modules.gui.controls.viewer.*;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.event.EventListenerOptions;
import de.docware.framework.modules.gui.layout.LayoutBorder;
import de.docware.framework.modules.gui.misc.http.server.HttpServerException;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.responsive.components.button.ThumbnailHelper;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Zeigt die übergebenen Zeichnungen inkl. Zeichnungsnummern in einem Fenster an.
 */
public class EditShowImagesInWindow extends GuiWindow {

    private EtkProject project;
    private String title;
    private List<EtkDataImage> dataImageList;
    private List<ImageViewerItem> imageViewerItems;
    private boolean showImageNumber;
    private int showSingleImageIndex;
    private EtkImageSettings imageSettings;
    private GuiLabel singleImageNumberLabel;
    private SingleImageViewerPanel singleImageViewerPanel;
    private ThumbnailsImageViewerPanel thumbnailsImageViewerPanel;
    private GuiTextField cursorControlTextField;

    public EditShowImagesInWindow(String title, int width, int height, List<EtkDataImage> dataImageList, boolean showImageNumber,
                                  int showSingleImageIndex, EtkProject project) {
        super(title, width, height);
        this.title = title;
        this.dataImageList = dataImageList;
        this.showImageNumber = showImageNumber;
        this.showSingleImageIndex = showSingleImageIndex;
        this.project = project;
        setLayout(new LayoutBorder());

        int index = 0;
        imageViewerItems = new ArrayList<>();
        for (EtkDataImage dataImage : dataImageList) {
            imageViewerItems.add(createImageViewerItem(dataImage, index));
            index++;
        }

        setPaddingTop(8);

        if ((showSingleImageIndex < 0) || (showSingleImageIndex >= imageViewerItems.size())) {
            // Padding rechts und unten macht das ThumbnailsImageViewerPanel selbst durch die Abstände zwischen Thumbnails
            setPaddingLeft(8);

            showThumbnailsImageViewerPanel();
        } else {
            // Dummy-Textfeld für die Cursortasten-Steuerung
            cursorControlTextField = new GuiTextField();
            cursorControlTextField.setMinimumHeight(0);
            cursorControlTextField.setMaximumHeight(0);
            cursorControlTextField.setPadding(0);
            cursorControlTextField.setBorderWidth(0);
            cursorControlTextField.addEventListener(new EventListener(Event.KEY_TYPED_EVENT) {
                @Override
                public void fire(Event event) {
                    Object keyCode = event.getParameter(Event.EVENT_PARAMETER_KEY_CODE);
                    if (keyCode.equals(KeyEvent.VK_LEFT)) {
                        if (EditShowImagesInWindow.this.showSingleImageIndex > 0) {
                            EditShowImagesInWindow.this.showSingleImageIndex--;
                            showSingleImage();
                        }
                    } else if (keyCode.equals(KeyEvent.VK_RIGHT)) {
                        if (EditShowImagesInWindow.this.showSingleImageIndex < imageViewerItems.size() - 1) {
                            EditShowImagesInWindow.this.showSingleImageIndex++;
                            showSingleImage();
                        }
                    }
                }
            });
            addChildBorderSouth(cursorControlTextField);

            showSingleImage();
        }
    }

    private void showThumbnailsImageViewerPanel() {
        final ThumbnailsImageViewerConfig thumbnailsConfig = createThumbnailsImageViewerConfig();
        thumbnailsImageViewerPanel = new ThumbnailsImageViewerPanel(thumbnailsConfig, imageViewerItems, false) {
            {
                int imageIndex = 0;
                for (ImageViewerItem imageViewerItem : imageViewerItems) {
                    // Bisherige MouseListener entfernen und nur einen synchronen MouseListener für das Öffnen vom nicht-modalen
                    // Fenster mit dem Einzelbild hinzufügen
                    imageViewerItem.getGui().removeEventListeners(Event.MOUSE_RELEASED_EVENT);
                    imageViewerItem.getGui().removeEventListeners(Event.MOUSE_DOUBLECLICKED_EVENT);
                    if (dataImageList.size() > 1) { // Doppelklick macht nur bei mehr als einem Bild Sinn
                        final int imageIndexFinal = imageIndex;
                        imageViewerItem.getGui().addEventListener(new EventListener(Event.MOUSE_DOUBLECLICKED_EVENT, EventListenerOptions.SYNCHRON_EVENT) {
                            @Override
                            public void fire(Event event) {
                                onImageDoubleClick(imageIndexFinal);
                            }
                        });
                    }
                    imageIndex++;
                }
            }

            @Override
            protected ThumbnailHelper createThumbnailHelper() {
                return new ThumbnailHelper(thumbnailsConfig.getThumbnailMinButtonSize(), thumbnailsConfig.getThumbnailMinButtonSizeOnMobile(),
                                           thumbnailsConfig.getThumbnailMaxButtonSize(), thumbnailsConfig.getThumbnailPadding()) {
                    @Override
                    public DWDimensionInt calculateThumbnailSize(int width, int height, int numItems) {
                        DWDimensionInt thumbnailSize = super.calculateThumbnailSize(width, height, numItems);

                        // Bei nur einem Thumbnail dieses Fenster-füllend anzeigen
                        if (dataImageList.size() == 1) {
                            thumbnailSize.setWidth(width - 2 * thumbnailsConfig.getThumbnailPadding());
                        }

                        return thumbnailSize;
                    }
                };
            }

            @Override
            protected GuiPanel createThumbnail(int imageIndex, int colCount, DWDimensionInt thumbnailSize, int insets) {
                GuiPanel wrapper = super.createThumbnail(imageIndex, colCount, thumbnailSize, insets);

                if (showImageNumber) {
                    GuiLabel imageNumberLabel = createImageNumberLabel(imageIndex);
                    wrapper.addChildBorderNorth(imageNumberLabel);
                }

                return wrapper;
            }

            @Override
            public void onImageClick(int imageIndex) {
                // Nichts tun
            }

            @Override
            public void onImageDoubleClick(int imageIndex) {
                // (Nicht-)modales Fenster mit dem einzelnen Bild anzeigen
                EditShowImagesInWindow singleImageWindow = new EditShowImagesInWindow(title, getWidth() - 20, getHeight() - 20,
                                                                                      dataImageList, showImageNumber, imageIndex,
                                                                                      project);

                // Nicht-modales Einzelbild-Fenster schließen, wenn das Fenster für alle Bilder geschlossen wird
                addEventListener(new EventListener(Event.SUB_WINDOW_CLOSED_EVENT) {
                    @Override
                    public void fire(Event event) {
                        singleImageWindow.setVisible(false);
                    }
                });

                if (isShownModal()) { // Falls das Öffnen des Fensters für alle Bilder modal erfolgt ist
                    singleImageWindow.addEventListener(new EventListener(Event.CLOSING_EVENT) {
                        @Override
                        public void fire(Event event) {
                            singleImageWindow.setVisible(false);
                            singleImageWindow.dispose();
                        }
                    });

                    singleImageWindow.showModal();
                } else {
                    singleImageWindow.showNonModal(NonModalStyle.OPEN_IN_NEW_WINDOW);
                }
            }
        };

        addChildBorderCenter(thumbnailsImageViewerPanel.getGui());
    }

    private void showSingleImage() {
        if (singleImageNumberLabel != null) {
            singleImageNumberLabel.removeFromParent();
            singleImageNumberLabel = null;
        }
        if (singleImageViewerPanel != null) {
            singleImageViewerPanel.getGui().removeFromParent();
        }

        if (showImageNumber) {
            singleImageNumberLabel = createImageNumberLabel(showSingleImageIndex);
            singleImageNumberLabel.setMinimumHeight(singleImageNumberLabel.getPreferredHeight() + singleImageNumberLabel.getPaddingBottom());
            addChildBorderNorth(singleImageNumberLabel);
        }

        int imageCount = imageViewerItems.size();
        setTitle(title + " (" + (showSingleImageIndex + 1) + "/" + imageCount + ")");

        singleImageViewerPanel = new SingleImageViewerPanel(imageViewerItems.get(showSingleImageIndex));
        GuiViewerImageInterface imageInterface = singleImageViewerPanel.getViewer();
        if (imageInterface instanceof GuiViewerImageNavigationInterface) {
            GuiViewerImageNavigationInterface imageNavigationInterface = ((GuiViewerImageNavigationInterface)imageInterface);
            GuiViewerImageNavigation viewerImageNavigation = GuiViewerImageNavigation.create(true, showSingleImageIndex, imageCount);
            imageNavigationInterface.setGuiViewerImageNavigation(viewerImageNavigation);

            imageNavigationInterface.getGuiControl().addEventListener(new EventListener(Event.IMAGE_INDEX_CHANGE_EVENT) {
                @Override
                public void fire(Event event) {
                    Integer newIndex = (Integer)event.getParameter(Event.EVENT_PARAMETER_IMAGE_INDEX);
                    if (newIndex != null) {
                        showSingleImageIndex = newIndex;
                        showSingleImage();
                    }
                }
            });
        }
        addChildBorderCenter(singleImageViewerPanel.getGui());

        cursorControlTextField.requestFocus();
    }

    private GuiLabel createImageNumberLabel(int imageIndex) {
        // Zeichnungsnummer über der Zeichnung anzeigen
        EtkDataImage dataImage = dataImageList.get(imageIndex);
        String imageNumber = dataImage.getImagePoolNo();
        if (!dataImage.getImagePoolVer().isEmpty()) {
            imageNumber += " / " + dataImage.getImagePoolVer();
        }
        GuiLabel imageNumberLabel = new GuiLabel(imageNumber);
        imageNumberLabel.setHorizontalAlignment(GuiLabel.HorizontalAlignment.CENTER);
        imageNumberLabel.setPaddingBottom(8);
        return imageNumberLabel;
    }

    @Override
    public void showNonModal(NonModalStyle nonModalStyle) {
        addEventListener(new EventListener(Event.SUB_WINDOW_CLOSED_EVENT) {
            @Override
            public void fire(Event event) {
                dispose();
            }
        });

        super.showNonModal(nonModalStyle);
    }

    public void dispose() {
        if (thumbnailsImageViewerPanel != null) {
            thumbnailsImageViewerPanel.dispose();
            thumbnailsImageViewerPanel = null;
        }
        if (singleImageViewerPanel != null) {
            singleImageViewerPanel.dispose();
            singleImageViewerPanel = null;
        }
    }

    private ThumbnailsImageViewerConfig createThumbnailsImageViewerConfig() {
        return new ThumbnailsImageViewerConfig() {
            @Override
            public int getThumbnailMinButtonSize() {
                return 350; // Gute Größe für sinnvollen Umbruch bei mehr als zwei Zeichnungen
            }

            @Override
            public int getThumbnailMinButtonSizeOnMobile() {
                return getThumbnailMinButtonSize();
            }

            @Override
            public int getThumbnailMaxButtonSize() {
                return Integer.MAX_VALUE;
            }

            @Override
            public int getThumbnailPadding() {
                return 8;
            }
        };
    }

    // Ab hier Code analog zu AssemblyImageForm (nur auf das Nötigste reduziert und ohne Connector)

    private ImageViewerItem createImageViewerItem(EtkDataImage imageData, int imageIndex) {
        GuiViewerImageInterface imageViewer = createImageViewer(imageData, imageIndex);
        if (imageViewer != null) {
            return new DefaultImageViewerItem(imageViewer);
        } else {
            return new UnsupportedImageViewerItem();
        }
    }

    private GuiViewerImageInterface createImageViewer(EtkDataImage imageData, int imageIndex) {
        if (imageData == null) {
            return null;
        }

        EtkDataPool variant = getImageVariant(imageData);
        if (variant == null) {
            return null;
        }

        GuiViewerImageInterface imageViewer = getViewerByImageType(variant.getImageType(), imageIndex);  // passenden Viewer für Bild bestimmen
        if (imageViewer == null) {
            return null;
        }
        imageViewer.setImageId(imageData.getPoolEntryId());
        imageViewer.setData(variant.getImgBytes(), variant.getImageType(), GuiViewerImageInterface.MAX_NUMBER_OF_PIXELS_UNLIMITED, true);

        if (imageViewer instanceof AbstractImageViewer3D) {
            ((AbstractImageViewer3D)imageViewer).assignSettings(getImageSettings().getImageCommonSettings(),
                                                                getImageSettings().getImageHotspotSettings(),
                                                                getImageSettings().getImageSecuritySettings());
        }

        AbstractGuiViewer abstractGuiViewer = (AbstractGuiViewer)imageViewer;
        abstractGuiViewer.display(); //Jetzt die Darstellung auslösen
        return imageViewer;
    }

    private boolean isValidVariant(EtkDataPool variant) {
        return (variant != null) && (variant.getImageType() != null) &&
               (variant.getImgBytes() != null) && (variant.getImgBytes().length > 0);
    }

    private EtkDataPool getImageVariant(EtkDataImage imageData) {
        EtkDataPool variant = imageData.getBestImageVariant(project.getDBLanguage(), EtkDataImage.IMAGE_USAGE_2D);
        if (isValidVariant(variant)) {
            return variant;
        }
        return null;
    }

    private GuiViewerImageInterface getViewerByImageType(String extension, final int imageIndex) {
        try {
            return GuiViewer.getImageViewerForFilename("dummy." + extension, imageIndex, false, project.isDocumentDecryptionNecessary());
        } catch (HttpServerException e) {
            Logger.getLogger().throwRuntimeException(e);
            return null;
        }
    }

    /**
     * Imagesetting ermitteln, falls noch nicht geladen, tue das
     */
    private EtkImageSettings getImageSettings() {
        if (imageSettings == null) {
            imageSettings = new EtkImageSettings();
            imageSettings.load(project.getConfig());
        }
        return imageSettings;
    }
}