/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary;

import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.author.iPartsDataAuthorOrder;
import de.docware.framework.modules.config.common.Language;
import de.docware.util.collections.dwlist.DwList;

import java.util.List;

/**
 * Container für vorhandene und neu erzeugte dictTransJob-Objekte und Variablen die immer gültig sind
 * (jobId, Datum, userId)
 */
public class TranslationJobContainer {

    public enum JobTypes {
        TRANSLATION("Ueb"),
        CORRECTION("Kor");

        String type;

        JobTypes(String jobType) {
            this.type = jobType;
        }

        public String getType() {
            return type;
        }
    }

    private final boolean isTruckObjectStoreTranslations;
    private String jobId;
    private String userId;
    private JobTypes jobType;
    private Language sourceLang;
    private Language destLang;
    private List<iPartsDataDictMeta> dataDictMetas;
    private iPartsDataDictTextKind textKind;
    private String translationDate; // Übersetzungsdatum - Wird vom Exporter gesetzt
    private String bundleName; // Dateiname - Wird vom Exporter gesetzt
    private String bundleDate; // Dateidatum - Wird vom Exporter gesetzt

    public TranslationJobContainer(Language sourceLang, Language destLang, iPartsDataDictTextKind textKind,
                                   boolean isTruckObjectStoreTranslations) {
        this.sourceLang = sourceLang;
        this.destLang = destLang;
        this.textKind = textKind;
        this.isTruckObjectStoreTranslations = isTruckObjectStoreTranslations;
        // todo: im Moment noch fest
        this.jobType = JobTypes.TRANSLATION;
        init();
    }

    /**
     * eine bereits vorhandene JobListe laden und die jobId, Datum und UserId bestimmen
     */
    private void init() {
        userId = iPartsDataAuthorOrder.getLoginAcronym();
        dataDictMetas = new DwList<>();
        bundleName = "";
        translationDate = "";
        bundleDate = "";
        jobId = "";
    }

    public void addDictMeta(iPartsDataDictMeta dictMeta) {
        dataDictMetas.add(dictMeta);
    }

    public Language getSourceLang() {
        return sourceLang;
    }

    public Language getDestLang() {
        return destLang;
    }

    public List<iPartsDataDictMeta> getDataDictMetas() {
        return dataDictMetas;
    }

    public int getDictMetaCount() {
        return getDataDictMetas().size();
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public void setTranslationDate(String translationDate) {
        this.translationDate = translationDate;
    }

    public String getTranslationDate() {
        return translationDate;
    }

    public String getUserId() {
        return userId;
    }

    public iPartsDataDictTextKind getTextKind() {
        return textKind;
    }

    public JobTypes getJobType() {
        return jobType;
    }

    public String getBundleName() {
        return bundleName;
    }

    public void setBundleName(String bundleName) {
        this.bundleName = bundleName;
    }

    public String getBundleDate() {
        return bundleDate;
    }

    public void setBundleDate(String bundleDate) {
        this.bundleDate = bundleDate;
    }

    public boolean isTruckObjectStoreTranslations() {
        return isTruckObjectStoreTranslations;
    }
}
