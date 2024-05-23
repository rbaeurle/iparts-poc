/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.helper;

import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.framework.modules.gui.misc.logger.LogChannels;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.output.j2ee.htmlcreator.Styles;
import de.docware.framework.utils.VarParam;
import de.docware.util.StrUtils;
import de.docware.util.java1_1.Java1_1_Utils;
import de.docware.util.svg.SvgUtils;
import org.apache.batik.anim.dom.SVGOMGElement;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.gvt.GraphicsNode;
import org.w3c.dom.*;
import org.w3c.dom.svg.SVGDocument;

import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Helper für die Anreicherung von SVG Zeichnungen mit Kontur (outline) Informationen
 * <p>
 * Die bereitgestellten SVG Zeichnungen (ASPLM Workflow) weisen immer die gleiche Struktur auf
 * <g id="hotspot_<posnr><suffix>>[...]</g>
 * <p>
 * Der Suffix ist optional und bildet die Option ab, mehrere Text-Knoten, Images, Hotspots, .. zu einer Pos-Nr/ID zu haben
 * z.B. hotspot_1
 * hotspot_1_
 * hotspot_1_1
 * <p>
 * Rahmenbedingungen
 * - Suffix zwischen Text-ID und Hotspot-ID kann abweichend sein (z.B. hotspot_60_ vs text_60)
 * - innerhalb einer Hotspot-Gruppe mehrere Images möglich (abweichender Suffix, aber POS-Nr identisch)
 * - innerhalb einer Hotspot-Gruppe mehrere Text Verlinkungen möglich (abweicheder Suffix, aber POS-Nr identisch)
 * - innerhalb einer Zeichnung mehrere Hotsposts zu einer POS-Nr möglich (abweichender Suffix)
 * <p>
 * Eine Normierung der IDs wird aus den obigen Rahmenbedingungen heraus nicht weiter verfolgt.
 * <p>
 * Beim Anhängen der Kontur-Information wird
 * - Filter für Darstellung mit Highlight
 * - Filter für Darstellung ohne Highlight
 * - Style Information für Darstellung mit Highlight
 * in den SVG Root Knoten geschrieben
 * <p>
 * In diesem Root Knoten wird auch das Attribut {ATTRIBUTE_SVG_OUTLINE_GENERATOR} ergänzt,
 * das auch zur Erkennung eines bereits vorherigen Durchlaufs genutzt genutzt wird.
 * Dies gewährleistet, dass die Konturen nur einmalig an das SVG Dokument angehängt werden. *
 * <p>
 * Ausblick
 * Der Javascript-Algorithmus sucht nach Elementen die den korrekten Prefix der ID aufweisen (z.B. text_123) und
 * iteriert über die Ergebnismenge
 */
public class iPartsSvgOutlineHelper {

    private static final String SVG_NAMESPACE_URI = "http://www.w3.org/2000/svg";
    private static final String XLINK_NAMESPACE_URI = "http://www.w3.org/1999/xlink";
    private static final String HOTSPOT_ID_PREFIX = "hotspot_";
    private static final String FILTER_ID_PREFIX = "filter_";
    private static final String FILTERED_GROUP_ID_PREFIX = "filtered_hotspot_";
    private static final String ATTRIBUTE_SVG_OUTLINE_GENERATOR = "svg_outline_generator";
    private static final String ATTRIBUTE_VALUE_SVG_OUTLINE_GENERATOR = "asplm";
    private static final String ATTRIBUTE_SVG_OUTLINE_VERSION = "svg_outline_version";
    private static final String ATTRIBUTE_VALUE_OUTLINE_VERSION = "2";
    private static final String SVG_BASE_PATH_COLOR_ID = "svg-basecolor";

    /**
     * Schreibt die Filter für die Visualisierung der Kontur-Informationen (Outlines) und
     * verlinkt die Filter mit den Konturen
     * <p>
     * Die (Hotspot-)Texte werden explizit nicht von den Filtern verarbeitet
     *
     * @param svgImage
     * @return
     */
    public static iPartsSvgOutlineResult attachOutlineFilter(byte[] svgImage) {
        iPartsSvgOutlineResult svgOutlineResult = new iPartsSvgOutlineResult(svgImage);
        try {
            SVGDocument document = SvgUtils.getSvgDocument(svgImage); // Normales "Document" nicht möglich (obwohl schneller), da BBOX Bestimmung notwendig ist

            svgOutlineResult.setMarkerAttached(isMarkerAttached(document));
            svgOutlineResult.setHasClipPaths(hasClipPaths(document));

            if (svgOutlineResult.isMarkerAttached() || svgOutlineResult.hasClipPaths()) {
                return svgOutlineResult;
            }

            boolean svgOutlineVersion2 = iPartsPlugin.getPluginConfig().getConfigValueAsBoolean(iPartsPlugin.CONFIG_SVG_OUTLINE_VERSION_2);

            replaceFontFamily(document);
            attachAsplmMarker(document, svgOutlineVersion2);
            attachHighlightStyle(document);
            attachDimInactiveStyle(document);
            attachInCartStyle(document);
            attachNoHighlightFilter(document);
            attachHighlightFilter(document);
            attachDimInactiveFilter(document);
            attachInCartFilter(document);
            attachHighlightTextFilter(document);
            attachInCartTextFilter(document);
            NodeList groups = document.getElementsByTagName("g");
            for (int groupIdx = 0; groupIdx < groups.getLength(); groupIdx++) {
                Element groupNode = (Element)groups.item(groupIdx);
                String groupNodeId = groupNode.getAttribute("id");
                if (StrUtils.isEmpty(groupNodeId) || !groupNodeId.startsWith(HOTSPOT_ID_PREFIX)) {
                    continue;
                }

                // Hotspot Gruppe - in dieser Gruppe liegt ein Image (image_<nr><suffix> und ein Text Element (text_<nr><suffix>)
                prepareImageNodes(document, groupNode, svgOutlineVersion2);
            }
            rearrangeHotspotsBySize(document);
            svgOutlineResult.setProcessedData(SvgUtils.getByteArray(document));
        } catch (Exception e) {
            Logger.logExceptionWithoutThrowing(LogChannels.IMAGE_SVG, LogType.ERROR, e);
            svgOutlineResult.setHasException(true);
        }
        return svgOutlineResult;
    }

    /**
     * Check, ob eine SVG Zeichnung die <clipPath> Elemente enthält
     *
     * @param document
     * @return
     */
    private static boolean hasClipPaths(SVGDocument document) {
        NodeList clipPathList = document.getDocumentElement().getElementsByTagName("clipPath");
        if (clipPathList.getLength() > 0) {
            Logger.log(LogChannels.IMAGE_SVG, LogType.ERROR, "Found clipPath elements in SVG file: " + document.getTitle());
            return true;
        }
        return false;
    }

    /**
     * Schreibt den Marker zur Erkennung des Outline-Mechanismus in der zentralen svg.js
     *
     * @param document
     * @param svgOutlineVersion2
     */
    private static void attachAsplmMarker(Document document, boolean svgOutlineVersion2) {
        document.getDocumentElement().setAttribute(ATTRIBUTE_SVG_OUTLINE_GENERATOR, ATTRIBUTE_VALUE_SVG_OUTLINE_GENERATOR);
        if (svgOutlineVersion2) {
            document.getDocumentElement().setAttribute(ATTRIBUTE_SVG_OUTLINE_VERSION, ATTRIBUTE_VALUE_OUTLINE_VERSION);
        }
    }

    public static boolean isMarkerAttached(Document document) {
        String svgOutlineGenerator = document.getDocumentElement().getAttribute(ATTRIBUTE_SVG_OUTLINE_GENERATOR);
        if (!StrUtils.isValid(svgOutlineGenerator)) {
            return false;
        }
        return svgOutlineGenerator.equals(ATTRIBUTE_VALUE_SVG_OUTLINE_GENERATOR);
    }

    /**
     * Ersetzt den globalen Font vom SVG und macht alle in Styles explizit gesetzten Fonts ungültig.
     *
     * @param document
     */
    private static void replaceFontFamily(Document document) {
        String fontName = iPartsPlugin.getPluginConfig().getConfigValueAsString(iPartsPlugin.CONFIG_SVG_FONT_NAME).trim();
        if (!fontName.isEmpty()) {
            document.getDocumentElement().setAttribute(Styles.CSS_STYLE_FONT_FAMILY, fontName);
            NodeList styles = document.getElementsByTagName("style");
            for (int styleIdx = 0; styleIdx < styles.getLength(); styleIdx++) {
                Element styleNode = (Element)styles.item(styleIdx);
                String styleText = styleNode.getTextContent();

                // Das Style-Attribut "font-family" zu entfernen wäre deutlich aufwändiger als dieses einfach in ein ungültiges
                // Style-Attribut umzubenennen
                styleText = styleText.replace(Styles.CSS_STYLE_FONT_FAMILY, "ignored_" + Styles.CSS_STYLE_FONT_FAMILY);

                styleNode.setTextContent(styleText);
            }
        }
    }

    /**
     * Schreibt den NoHighlight Filter
     * Der Filter wird auf Hotspot-Images aktiviert, wenn diese weder selektiert sind oder mouse-hover Effekt haben
     *
     * <filter id="NoHighlight">
     * <feColorMatrix in="SourceGraphic"
     * type="matrix"
     * values="1 0 0 0 0
     * 0 1 0 0 0
     * 0 0 1 0 0
     * 0 0 0 1 0" />
     * </filter>
     *
     * @param document
     */
    private static void attachNoHighlightFilter(Document document) {
        Element filter = document.createElementNS(SVG_NAMESPACE_URI, "filter");
        filter.setAttribute("id", "NoHighlight");
        Element feColorMatrix = document.createElementNS(SVG_NAMESPACE_URI, "feColorMatrix");
        feColorMatrix.setAttribute("in", "SourceGraphic");
        feColorMatrix.setAttribute("type", "matrix");
        feColorMatrix.setAttribute("values",
                                   "1 0 0 0 0 " +
                                   "0 1 0 0 0 " +
                                   "0 0 1 0 0 " +
                                   "0 0 0 1 0 ");
        filter.appendChild(feColorMatrix);
        document.getDocumentElement().insertBefore(filter, document.getDocumentElement().getFirstChild());
        document.getDocumentElement().insertBefore(createNewLineNode(document, 0), document.getDocumentElement().getFirstChild());
    }

    private static String createColorMatrix(Color color) {
        return createColorMatrix(color, Color.white);
    }

    private static String createColorMatrix(Color color, Color baseColor) {
        return ((float)color.getRed() / Math.max(1f, (float)baseColor.getRed())) + " 0 0 0 0 " +
               "0 " + ((float)color.getGreen() / Math.max(1f, (float)baseColor.getGreen())) + " 0 0 0 " +
               "0 0 " + ((float)color.getBlue() / Math.max(1f, (float)baseColor.getBlue())) + " 0 0 " +
               "0 0 0 1 0 ";
    }

    /**
     * Schreibt einen Filter zum Einfärben und Blur von Elementen.
     *
     * <filter id="...">
     * <feColorMatrix in="SourceGraphic" result="colorMe"
     * type="matrix"
     * values="..." />
     * <feOffset in="SourceGraphic" dx="5" dy="5"/>
     * <feGaussianBlur stdDeviation="5" result="blur2" />
     * <feMerge>
     * <feMergeNode in="blur2" />
     * <feMergeNode in="colorMe" />
     * </feMerge>
     * </filter>
     *
     * @param document
     * @param filterId
     * @param colorValue
     */
    private static void attachColorBlurFilter(Document document, String filterId, String colorValue) {
        Element filter = document.createElementNS(SVG_NAMESPACE_URI, "filter");
        filter.setAttribute("id", filterId);
        Element feColorMatrix = document.createElementNS(SVG_NAMESPACE_URI, "feColorMatrix");
        feColorMatrix.setAttribute("in", "SourceGraphic");
        feColorMatrix.setAttribute("type", "matrix");
        feColorMatrix.setAttribute("result", "colorMe");
        feColorMatrix.setAttribute("values", colorValue);
        Element feOffset = document.createElementNS(SVG_NAMESPACE_URI, "feOffset");
        feOffset.setAttribute("in", "SourceGraphic");
        feOffset.setAttribute("dx", "5");
        feOffset.setAttribute("dy", "5");

        Element feGaussianBlur = document.createElementNS(SVG_NAMESPACE_URI, "feGaussianBlur");
        feGaussianBlur.setAttribute("stdDeviation", "5");
        feGaussianBlur.setAttribute("result", "blur2");

        Element feMerge = document.createElementNS(SVG_NAMESPACE_URI, "feMerge");
        Element feMergeNodeBlur = document.createElementNS(SVG_NAMESPACE_URI, "feMergeNode");
        feMergeNodeBlur.setAttribute("in", "blur2");
        Element feMergeNodeColor = document.createElementNS(SVG_NAMESPACE_URI, "feMergeNode");
        feMergeNodeColor.setAttribute("in", "colorMe");
        feMerge.appendChild(feMergeNodeBlur);
        feMerge.appendChild(feMergeNodeColor);

        filter.appendChild(feColorMatrix);
        filter.appendChild(feOffset);
        filter.appendChild(feGaussianBlur);
        filter.appendChild(feMerge);
        document.getDocumentElement().insertBefore(filter, document.getDocumentElement().getFirstChild());
        document.getDocumentElement().insertBefore(createNewLineNode(document, 0), document.getDocumentElement().getFirstChild());
    }

    private static Color getSVGBasePathColor(Document document) {
        // DAIMLER-15238: SVG - Erweiterung Konfiguration basecolor
        // SVG_basePathColor aus dem SVG auslesen falls vorhanden (fill-Attribut vom Style des Elements optional über CSS-Klasse)
        Element svgBasePathColorElement = document.getElementById(SVG_BASE_PATH_COLOR_ID);
        if (svgBasePathColorElement != null) {
            String svgBasePathColorStyleValue = svgBasePathColorElement.getAttribute("style");
            if (StrUtils.isEmpty(svgBasePathColorStyleValue)) {
                // Style in einer CSS-Klasse?
                String svgBasePathColorClass = svgBasePathColorElement.getAttribute("class");
                if (StrUtils.isValid(svgBasePathColorClass)) {
                    NodeList styleElements = document.getElementsByTagName("style");
                    if (styleElements != null) {
                        for (int i = 0; i < styleElements.getLength(); i++) {
                            Node styleElement = styleElements.item(i);
                            String styleContent = styleElement.getTextContent();
                            if (StrUtils.isValid(styleContent)) {
                                int styleIndex = styleContent.indexOf("." + svgBasePathColorClass);
                                if (styleIndex >= 0) {
                                    styleIndex = styleContent.indexOf("{", styleIndex);
                                    if (styleIndex >= 0) {
                                        int endIndex = styleContent.indexOf("}", styleIndex);
                                        if (endIndex >= 0) {
                                            svgBasePathColorStyleValue = styleContent.substring(styleIndex + 1, endIndex).trim();
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (StrUtils.isValid(svgBasePathColorStyleValue)) { // Style-Attribut gefunden
                String svgBasePathColorValue = null;
                String[] styleAttributes = StrUtils.toStringArray(svgBasePathColorStyleValue, ";", false, true);
                for (String styleAttribute : styleAttributes) {
                    int valueIndex = styleAttribute.indexOf(":");
                    if ((valueIndex > 0) && (valueIndex < styleAttribute.length() - 1)) {
                        String styleKey = styleAttribute.substring(0, valueIndex).trim();
                        if (styleKey.equals("fill")) { // fill-Attribut suchen
                            svgBasePathColorValue = styleAttribute.substring(valueIndex + 1).trim();
                            svgBasePathColorValue = StrUtils.removeFirstCharacterIfCharacterIs(svgBasePathColorValue, "'");
                            svgBasePathColorValue = StrUtils.removeLastCharacterIfCharacterIs(svgBasePathColorValue, "'");
                            break;
                        }
                    }
                }

                if (StrUtils.isValid(svgBasePathColorValue)) {
                    Color svgBasePathColor = Java1_1_Utils.colorFromHexString(svgBasePathColorValue);
                    if (svgBasePathColor != null) {
                        return svgBasePathColor;
                    }
                }
            }
        }

        // Fallback auf die Farbe aus dem Admin-Modus
        return iPartsPlugin.clPlugin_iParts_SVG_basePathColor.getColor();
    }

    /**
     * Schreibt den HighlightMe Filter
     * Der Filter wird auf Hotspot-Images aktiviert, wenn diese entweder selektiert sind oder einen mouse-hover Effekt haben
     *
     * @param document
     */
    private static void attachHighlightFilter(Document document) {
        attachColorBlurFilter(document, "HighlightMe", createColorMatrix(iPartsPlugin.clPlugin_iParts_SVG_highlightPathColor.getColor(),
                                                                         getSVGBasePathColor(document)));
    }

    /**
     * Schreibt den DimmedInactive Filter
     * Der Filter wird auf Hotspot-Images aktiviert, wenn diese nicht verlinkt sind
     * <p>
     * Anmerkung: Aktuell funktioniert dieser Filter nur auf Hotspots, die allerdings "ohne Outline" definiert sind (iParts Edit)
     *
     * @param document
     */
    private static void attachDimInactiveFilter(Document document) {
        attachColorBlurFilter(document, "DimmedInactive", "0.8 0 0 0.6 0 " +
                                                          "0 0.8 0 0.6 0 " +
                                                          "0 0 0.8 0.6 0 " +
                                                          "0 0 0 1 0");
    }

    /**
     * Schreibt den InCart Filter (wird nur von externen Programmen gesetzt, wenn der Hotspot im Warenkorb ist)
     *
     * @param document
     */
    private static void attachInCartFilter(Document document) {
        attachColorBlurFilter(document, "InCart", createColorMatrix(iPartsPlugin.clPlugin_iParts_SVG_inCartPathColor.getColor(),
                                                                    getSVGBasePathColor(document)));
    }

    /**
     * Schreibt die Style Klasse für das Einfärben von Hotspot-Texten.
     *
     * @param document
     */
    private static void attachTextColorStyle(Document document, String cssClassName, String colorValue) {
        Element style = document.createElementNS(SVG_NAMESPACE_URI, "style");
        style.appendChild(document.createTextNode("." + cssClassName + "{fill: " + colorValue + ";}"));
        document.getDocumentElement().insertBefore(style, document.getDocumentElement().getFirstChild());
        document.getDocumentElement().insertBefore(createNewLineNode(document, 0), document.getDocumentElement().getFirstChild());
    }

    /**
     * Schreibt die Style Klasse für das Highlighting des Hotspot-Textes (bei Hover / Selektion).
     * <p>
     * Bei Mouseover/Selektion im Browser wird die Style-Klasse den Text-Elementen des Hotspots zugeordnet
     *
     * @param document
     */
    private static void attachHighlightStyle(Document document) {
        attachTextColorStyle(document, "st_high", iPartsPlugin.clPlugin_iParts_SVG_highlightTextColor.getHtml());
    }

    /**
     * Schreibt die Style Klasse für das Dimmen des Hotspot-Textes.
     * <p>
     * Bei nicht vorhandenem Hotspot in der Stückliste wird die Style-Klasse den Text-Elementen des Hotspots zugeordnet
     *
     * @param document
     */
    private static void attachDimInactiveStyle(Document document) {
        attachTextColorStyle(document, "st_dimmed", iPartsPlugin.clPlugin_iParts_SVG_inactiveTextColor.getHtml());
    }

    /**
     * Schreibt die Style Klasse für Hotspot-Texte im Warenkorb.
     * <p>
     * Bei nicht vorhandenem Hotspot in der Stückliste wird die Style-Klasse den Text-Elementen des Hotspots zugeordnet
     *
     * @param document
     */
    private static void attachInCartStyle(Document document) {
        attachTextColorStyle(document, "st_cart", iPartsPlugin.clPlugin_iParts_SVG_inCartTextColor.getHtml());
    }

    /**
     * Schreibt den Filter für das Einfärben vom Hotspot-Text-Hintergrund.
     *
     * <filter x="-0.05" y="0" width="1.1" height="1" id="...">
     * <feFlood flood-color="..."/>
     * <feComposite in="SourceGraphic"/>
     * </filter>
     *
     * @param document
     */
    private static void attachTextBackgroundColorFilter(Document document, String filterId, String colorValue) {
        Element filter = document.createElementNS(SVG_NAMESPACE_URI, "filter");
        filter.setAttribute("x", "-0.05");
        filter.setAttribute("y", "0");
        filter.setAttribute("width", "1.1");
        filter.setAttribute("height", "1");
        filter.setAttribute("id", filterId);
        Element feFlood = document.createElementNS(SVG_NAMESPACE_URI, "feFlood");
        feFlood.setAttribute("flood-color", colorValue);

        Element feComposite = document.createElementNS(SVG_NAMESPACE_URI, "feComposite");
        feComposite.setAttribute("in", "SourceGraphic");

        filter.appendChild(feFlood);
        filter.appendChild(feComposite);
        document.getDocumentElement().insertBefore(filter, document.getDocumentElement().getFirstChild());
        document.getDocumentElement().insertBefore(createNewLineNode(document, 0), document.getDocumentElement().getFirstChild());
    }

    /**
     * Schreibt den HighlightText Filter
     * Der Filter wird auf Hotspot-Texte aktiviert, wenn diese entweder selektiert sind oder einen mouse-hover Effekt haben.
     *
     * @param document
     */
    private static void attachHighlightTextFilter(Document document) {
        attachTextBackgroundColorFilter(document, "HighlightText", iPartsPlugin.clPlugin_iParts_SVG_highlightBackgroundColor.getHtml());
    }

    /**
     * Schreibt den InCartText Filter
     * Der Filter wird auf Hotspot-Texte im Warenkorb aktiviert.
     *
     * @param document
     */
    private static void attachInCartTextFilter(Document document) {
        attachTextBackgroundColorFilter(document, "InCartText", iPartsPlugin.clPlugin_iParts_SVG_inCartBackgroundColor.getHtml());
    }

    /**
     * Erzeugt eine Zwischen-Gruppe für alle nicht-textuellen-Knoten
     * <p>
     * Das neue group Element muss vor den Text-Elementen eingefügt werden - damit die Darstellung des Textes noch gewährleistet ist.
     * Die Reihenfolge spielt eine wichtige Rolle - später hinzugefügte Elemente überlagern ihre Vorgänger (bei gleichem x/y)
     * <p>
     * Die Darstellung der Konturen erfolgt durch Anwendung von Filtern auf die neu eingefügte Gruppe (HiglightMe/NoHighlight)
     *
     * @param document
     * @param groupNode
     * @param svgOutlineVersion2
     */
    private static void prepareImageNodes(Document document, Element groupNode, boolean svgOutlineVersion2) {
        // Wir generieren erst den Use-Filter Eintrag. Die Gruppe für sämtliche nicht-Text-Elemente wird dann VOR diesem Element eingefügt
        Element useFilter = createImageFilterUsage(document, groupNode, svgOutlineVersion2);

        if (svgOutlineVersion2) { // Neues Highlighting mit Pfaden ohne das Erzeugen von FilteredHotspotGroups
            Node nextSibling = groupNode.getNextSibling();
            Node parentNode = groupNode.getParentNode();
            if (parentNode != null) {
                if (nextSibling != null) {
                    parentNode.insertBefore(createNewLineNode(document, 4), nextSibling);
                    parentNode.insertBefore(useFilter, nextSibling);
                } else {
                    parentNode.appendChild(createNewLineNode(document, 4));
                    parentNode.appendChild(useFilter);
                }
            }
        } else { // Altes Highlighting mit Erzeugen von FilteredHotspotGroups
            groupNode.insertBefore(useFilter, groupNode.getFirstChild());
            Element filterGroup = createImageFilterGroup(document, groupNode);
            groupNode.insertBefore(filterGroup, useFilter);

            groupNode.insertBefore(createNewLineNode(document, 4), filterGroup);
            groupNode.insertBefore(createNewLineNode(document, 4), useFilter);
            moveNonTextualElementsToFilterGroup(groupNode, filterGroup);
        }
    }

    private static Element createImageFilterGroup(Document document, Element groupNode) {
        String hotspotKey = getHotspotIdSuffix(groupNode);
        Element imageGroup = document.createElementNS(SVG_NAMESPACE_URI, "g");
        imageGroup.setAttribute("id", FILTERED_GROUP_ID_PREFIX + hotspotKey);
        return imageGroup;
    }

    private static Element createImageFilterUsage(Document document, Element groupNode, boolean svgOutlineVersion2) {
        // Hotspot ID kann mehrfach vorkommen (unterschiedliche Suffixe)
        // Aktueller Suffix nach dem "hotspot_" Prefix wird verwendet für die weitere Verlinkung innerhalb dieser Hotspot Gruppe
        String hotspotKey = getHotspotIdSuffix(groupNode);
        Element useFilter = document.createElementNS(SVG_NAMESPACE_URI, "use");
        useFilter.setAttribute("id", FILTER_ID_PREFIX + hotspotKey);
        useFilter.setAttributeNS(XLINK_NAMESPACE_URI, "xlink:href", "#" + (svgOutlineVersion2 ? groupNode.getAttribute("id")
                                                                                              : FILTERED_GROUP_ID_PREFIX + hotspotKey));
        useFilter.setAttribute("filter", "url(#NoHighlight)");
        return useFilter;
    }

    private static String getHotspotIdSuffix(Element groupNode) {
        // Hotspot ID kann mehrfach vorkommen (unterschiedliche Suffixe)
        // Aktueller Suffix nach dem "hotspot_" Prefix wird verwendet für die weitere Verlinkung innerhalb dieser Hotspot Gruppe
        String hotspotKey = StrUtils.stringAfterCharacter(groupNode.getAttribute("id"), HOTSPOT_ID_PREFIX);
        return hotspotKey;
    }

    /**
     * Hängt alle Knoten aus der Hotspot Gruppe um (hotspot_<id><suffix>), die nicht
     * - {FILTERED_GROUP_ID_PREFIX} (Gruppe, in die alles verschoben werden soll)
     * - {FILTER_ID_PREFIX} (use Element, das für die Visualisierung der Zielgruppe verantwortlich ist)
     * - Text
     * sind.
     * <p>
     * Bei dem Umhängen wird versucht, die vorherige Reihenfolge beizubehalten.
     *
     * @param groupNode
     * @param imageFilterGroup
     */
    private static void moveNonTextualElementsToFilterGroup(Element groupNode, Element imageFilterGroup) {
        NodeList nodeList = groupNode.getChildNodes(); // Nodelist ist aktiv. Bei vorwärts-Iteration sind nicht alle Nodes erreichbar)
        Document document = groupNode.getOwnerDocument();
        imageFilterGroup.appendChild(createNewLineNode(document, 4));
        for (int i = nodeList.getLength() - 1; i >= 0; i--) {
            Node node = nodeList.item(i);
            if (!node.getNodeName().equals("text")) {
                if ((node.getNodeType() == Node.ELEMENT_NODE) &&
                    (((Element)node).getAttribute("id").startsWith(FILTERED_GROUP_ID_PREFIX) ||
                     ((Element)node).getAttribute("id").startsWith(FILTER_ID_PREFIX))) {
                    continue;
                }
                if (node.getNodeType() == Node.TEXT_NODE && ((Text)node).getWholeText().contains("\n")) {
                    continue;
                }
                imageFilterGroup.insertBefore(node, imageFilterGroup.getFirstChild());
                imageFilterGroup.insertBefore(createNewLineNode(document, 4), imageFilterGroup.getFirstChild());
            }
        }
    }

    /**
     * Leerer Textknoten - Neue Zeile im SVG Dokument
     * Verwendet zur besseren visuellen Darstellung der neu eingefügten Elemente
     *
     * @param document
     * @param indentNewLine
     * @return
     */
    private static Text createNewLineNode(Document document, int indentNewLine) {
        String text = "\n" + StrUtils.pad("", indentNewLine);
        return document.createTextNode(text);
    }

    /**
     * Ordnet die Hotspot Gruppen (hotspot_ Prefix) im SVG Dokument anhand ihres Umfangs neu.
     * <p>
     * Im Browser wird der Mouseover Effekt ausgelöst, wenn sich die Maus innerhalb der Bounding Box (BBox) befindet.
     * Die BBoxen können sich überschneiden. Liegt eine kleinere BBox komplett innerhalb einer großen, ist das Highlight
     * der kleineren Bbox nur möglich, wenn diese "zuletzt" im SVG DOM hinzugefügt wurde (Prescedence Regeln).
     * <p>
     * Der einfache Algorithmus sortiert nicht nur sich jeweils überschneidende Flächen sondern alle Hotspot Flächen anhand ihrer Größe.
     * <p>
     * Eine kleinere Hotspot Fläche sollte damit einen Hover Effekt zuverlässiger auslösen können.
     *
     * @param document
     */
    private static void rearrangeHotspotsBySize(SVGDocument document) {
        BridgeContext ctx = new BridgeContext(new UserAgentAdapter());
        try {
            new GVTBuilder().build(ctx, document);
            VarParam<Boolean> foundRootNodeType1 = new VarParam<>(false);
            NodeList nodeList = document.getRootElement().getChildNodes();
            VarParam<Node> nodeDelimiter = new VarParam<>(null);
            List<SVGOMGElement> nodeStream = IntStream.range(0, nodeList.getLength())
                    .mapToObj(nodeList::item)
                    .peek((node) -> {
                        boolean isRootNodeType1 = node instanceof SVGOMGElement && ((SVGOMGElement)node).getAttribute("id").startsWith(HOTSPOT_ID_PREFIX);
                        if (isRootNodeType1) {
                            // Hotspot node ... ignore
                            foundRootNodeType1.setValue(true);
                            return;
                        }
                        if (!(node.getNodeType() == Node.ELEMENT_NODE)) {
                            // Only check for elements (as only group elements can be hotspot nodes)
                            return;
                        }
                        if (!foundRootNodeType1.getValue()) {
                            return; // Node prior to first of type 1 (i.e. no hotspot node)
                        }

                        // Current element is no hotspot node. However it is after a hotspot has been traversed
                        // Keep a hold of the first possible node as "delimiter"
                        // Any resorted hotspot node has to be inserted before this delimiter node
                        if (nodeDelimiter.getValue() == null) {
                            nodeDelimiter.setValue(node);
                        }
                    })
                    .filter(node -> node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().equalsIgnoreCase("g"))
                    .map(node -> (SVGOMGElement)node)
                    .filter(node -> node.getAttribute("id").startsWith(HOTSPOT_ID_PREFIX))
                    .sorted((elem1, elem2) -> {
                        GraphicsNode node1 = new GVTBuilder().build(ctx, elem1);
                        if (node1 != null) {
                            GraphicsNode node2 = new GVTBuilder().build(ctx, elem2);
                            if (node2 != null) {
                                int circumference1 = (int)(2 * (node1.getBounds().getWidth() + node1.getBounds().getHeight()));
                                int circumference2 = (int)(2 * (node2.getBounds().getWidth() + node2.getBounds().getHeight()));
                                return circumference2 - circumference1;
                            }
                        }
                        return 0; // Fallback bei fehlerhaften SVG-Inhalten
                    }).collect(Collectors.toList());
            if (nodeDelimiter.getValue() == null) {
                for (Node node : nodeStream) {
                    node.getParentNode().appendChild(node);
                    node.getParentNode().appendChild(createNewLineNode(document, 0));
                }
            } else {
                for (Node node : nodeStream) {
                    node.getParentNode().insertBefore(node, nodeDelimiter.getValue());
                    node.getParentNode().insertBefore(createNewLineNode(document, 0), nodeDelimiter.getValue());
                }
            }
        } catch (Exception e) {
            Logger.logExceptionWithoutThrowing(LogChannels.IMAGE_SVG, LogType.ERROR, e);
        } finally {
            ctx.dispose();
        }
    }
}
