/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.model;

import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.HierarchicalIDWithType;
import de.docware.framework.utils.EtkMultiSprache;

import java.util.Collection;
import java.util.List;

/**
 * Ein abstrakter iParts-Knoten mit den Kindknotenelementen <i>E</i> (abgeleitet von {@link AbstractiPartsNodes} und dem
 * Enum-Typ <i>T</i> zusammen mit seinen Bezeichnungen.
 * L=ListKlasse für die Childs, E=ElementKlasse und T = NodetypKlasse
 * z.B. public class EinPasNode extends AbstractiPartsNode<EinPasNodes, EinPasNode, EinPasType> {
 * L muss Collection von E sein
 */
public abstract class AbstractiPartsNode<L extends AbstractiPartsNodes, E extends AbstractiPartsNode, T> {

    protected T type;
    protected String number;
    protected EtkMultiSprache title;
    protected String pictureName = "";
    protected L children;
    protected AbstractiPartsNode<L, E, T> parent;

    public AbstractiPartsNode(T type, String number, AbstractiPartsNode<L, E, T> parent, L emptyChildren) {
        this.type = type;
        this.number = number;
        this.title = new EtkMultiSprache();
        this.parent = parent;
        children = emptyChildren;

    }

    public T getType() {
        return type;
    }

    public String getNumber() {
        return number;
    }

    public EtkMultiSprache getTitle() {
        return title;
    }

    public void setTitle(String language, String value, String textID) {
        title.setTextId(textID);
        title.setText(language, value);
    }

    public void setTitle(EtkMultiSprache title) {
        if (title != null) {
            this.title = title;
        }
    }

    /**
     * Alle zusätzlichen Daten des Knotens kopieren, aber nicht die 'Key'-Felder
     *
     * @param source
     */
    public void assignAddData(AbstractiPartsNode<L, E, T> source) {
        this.title.assign(source.title);
        this.pictureName = source.pictureName;
    }

    public String getNumberAndTitle(String language, List<String> fallbackLanguages) {
        return getNumber() + " - " + getTitle().getTextByNearestLanguage(language, fallbackLanguages);
    }

    public Collection<E> getChildren() {
        return children.getValues();
    }

    public E getOrCreateChild(T type, String key, E parent) {
        return (E)children.getOrCreate(type, key, parent);
    }

    public E getChild(String key) {
        return (E)children.get(key);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " " + type.toString() + " " + number;
    }

    public String getPictureName() {
        return pictureName;
    }

    public void setPictureName(String pictureName) {
        this.pictureName = pictureName;
    }

    public AbstractiPartsNode<L, E, T> getParent() {
        return parent;
    }

    /**
     * Liefert über den Parent die {@link HierarchicalIDWithType} zu diesem Knoten. Jede Ableitung muss diese natürlich
     * implementieren, da generisch nicht bekannt ist, wie sich diese ID zusammensetzt.
     *
     * @return
     */
    public abstract HierarchicalIDWithType getId();
}
