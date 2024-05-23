/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.helper;

import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.gui.misc.logger.LogChannels;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.util.StrUtils;
import de.docware.util.file.DWFile;
import de.docware.util.security.PasswordString;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.transfer.s3.S3TransferManager;
import software.amazon.awssdk.transfer.s3.model.*;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Hilfsklasse für den Zugriff auf einen S3 Object Store
 */
public abstract class AbstractS3ObjectStoreHelper implements iPartsConst {

    private static final Region EU_REGION = Region.EU_CENTRAL_1; // Region für AWS damit die Daten nicht im nicht-EU Raum landen

    private LogChannels logChannel;
    private String usage;
    private S3AsyncClient s3AsyncClient;
    private volatile boolean connected;

    public AbstractS3ObjectStoreHelper(LogChannels logChannel, String usage) {
        this.logChannel = logChannel;
        this.usage = usage;
        init();
    }

    protected LogChannels getLogChannel() {
        return logChannel;
    }

    protected String getUsage() {
        return usage;
    }

    protected int getMaxFileCount() {
        return -1;
    }

    protected abstract String getBucketName();

    protected abstract String getAccessKey();

    protected abstract PasswordString getSecretAccessKey();

    /**
     * Initialisiert die Verbindung bzw. den Client zum Abfragen der Bucket Daten
     *
     * @return
     */
    protected synchronized boolean init() {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(getAccessKey(), getSecretAccessKey().decrypt());
        s3AsyncClient = S3AsyncClient.crtBuilder().maxConcurrency(100)
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .region(EU_REGION)
                .build();
        connected = false;

        return checkConnection();
    }

    /**
     * Überprüft, ob eine Verbindung zum Object Store aufgebaut werden kann
     *
     * @return
     */
    protected boolean checkConnection() {
        if (s3AsyncClient == null) {
            return false;
        }
        try {
            // Check, ob der Bucket gefunden werden kann
            ListObjectsV2Request request = ListObjectsV2Request.builder().bucket(getBucketName()).maxKeys(0).build();
            CompletableFuture<ListObjectsV2Response> listObjectsFuture = s3AsyncClient.listObjectsV2(request);
            ListObjectsV2Response response = listObjectsFuture.get();
            if (response == null) {
                Logger.log(logChannel, LogType.ERROR, "Could not find bucket for " + usage);
                connected = false;
                return false;
            }
        } catch (S3Exception | ExecutionException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            Logger.log(logChannel, LogType.ERROR, "Could not connect to bucket for " + usage + ": " + e.getMessage());
            Logger.logExceptionWithoutThrowing(logChannel, LogType.ERROR, e);
            connected = false;
            return false;
        }
        connected = true;
        return true;
    }

    /**
     * Überprüft, ob die Verbindnugsdaten zum S3 Bucket korrekt sind.
     *
     * @return
     */
    public boolean isConnectionValid() {
        if (connected) {
            return true;
        } else {
            return checkConnection() || init();
        }
    }

    /**
     * Lädt die Dateien aus dem übergebenen Object Store Verzeichnis in das übergebene, lokale Verzeichnis herunter
     *
     * @param downloadDir
     * @param objectStoreDirectory
     * @param validExtensions
     * @throws RuntimeException Bei Fehlern
     */
    public void downloadData(DWFile downloadDir, String objectStoreDirectory, Set<String> validExtensions) {
        try (S3TransferManager transferManager = S3TransferManager.builder()
                .s3Client(s3AsyncClient)
                .build()) {
            int maxFileCountForImport = getMaxFileCount();
            DownloadDirectoryRequest downloadRequest = DownloadDirectoryRequest.builder()
                    .bucket(getBucketName())
                    .destination(Paths.get(downloadDir.getAbsolutePath()))
                    .filter(s3Object -> {
                        String completeKey = s3Object.key();
                        return validExtensions.contains(StrUtils.stringAfterAndIncludingLastCharacter(completeKey, '.').toLowerCase());
                    })
                    .listObjectsV2RequestTransformer(request -> {
                        request.prefix(objectStoreDirectory);
                        if (maxFileCountForImport > 0) {
                            request.maxKeys(maxFileCountForImport); // Hier die maximal Anzahl Dateien herunterladen und importieren. Rest wird erst beim nächsten Aufruf heruntergeladen
                        }
                    })
                    .build();
            DirectoryDownload fileDownload = transferManager.downloadDirectory(downloadRequest);

            // Warte bis der Download durch ist
            fileDownload.completionFuture().join();
        }
    }

    /**
     * Überprüft, ob in dem Object Store Verzeichnis Dateien vorhanden sind
     *
     * @param objectStoreDirectory
     * @return
     */
    public boolean checkIfFilesAvailable(String objectStoreDirectory) {
        // Wir fragen die Informationen zu maximal zwei Objekten an. Je nachdem, wie das "Verzeichnis" angelegt wurde,
        // wird es ebenfalls mitgeliefert. Daher wird geprüft, ob mind. eine Datei dabei ist, die nicht mit "/" endet
        CompletableFuture<ListObjectsResponse> listObjectsFuture = s3AsyncClient.listObjects(ListObjectsRequest.builder()
                                                                                                     .bucket(getBucketName())
                                                                                                     .prefix(objectStoreDirectory)
                                                                                                     .maxKeys(2)
                                                                                                     .build());
        ListObjectsResponse listObjectsResponse = listObjectsFuture.join();
        return listObjectsResponse.contents()
                .stream()
                .anyMatch(object -> !object.key().endsWith("/"));
    }

    /**
     * Löscht die übergebenen Dateien aus dem Object Store
     *
     * @param dirAndFilesToBeDeleted Dateinamen inkl. Object Store Verzeichnis
     * @param
     */
    public boolean deleteFilesInObjectStore(Set<String> dirAndFilesToBeDeleted) {
        if (dirAndFilesToBeDeleted.isEmpty()) {
            return false;
        }
        // Jede Datei hat im object store einen eindeutigen Schlüssel. Mit Hilfe dieser Schlüssel löschen wir die
        // Daten im object store
        ArrayList<ObjectIdentifier> deleteObjects = new ArrayList<>();
        for (String filename : dirAndFilesToBeDeleted) {
            deleteObjects.add(ObjectIdentifier.builder()
                                      .key(filename)
                                      .build());
        }

        DeleteObjectsRequest deleteObjectsRequest = DeleteObjectsRequest.builder()
                .bucket(getBucketName())
                .delete(Delete.builder()
                                .objects(deleteObjects)
                                .build())
                .build();
        if (!isConnectionValid()) {
            Logger.log(logChannel, LogType.ERROR, usage + ": Could not reinit connection to object store to delete files!");
            return false;
        }
        try {
            CompletableFuture<DeleteObjectsResponse> deleteObjectsFuture = s3AsyncClient.deleteObjects(deleteObjectsRequest);
            DeleteObjectsResponse response = deleteObjectsFuture.join();
            String deletedObjects = response.deleted()
                    .stream()
                    .map(DeletedObject::key)
                    .collect(Collectors.joining(","));
            Logger.log(logChannel, LogType.DEBUG, usage + ": Successfully deleted the following objects: " + deletedObjects);
            return true;
        } catch (SdkClientException e) {
            Logger.logExceptionWithoutThrowing(logChannel, LogType.ERROR, e);
            connected = false;
            return false;
        }
    }

    /**
     * Überträgt den übergebenen Inhalt in den Object Store
     *
     * @param objectStoreDirAndFileName Dateiname inkl. Object Store Verzeichnis
     * @param data
     * @return
     */
    public boolean uploadData(String objectStoreDirAndFileName, byte[] data) {
        if (!isConnectionValid()) {
            Logger.log(logChannel, LogType.ERROR, usage + ": Could not reinit connection to object store to upload data!");
            return false;
        }

        try {
            CompletableFuture<PutObjectResponse> completionFuture = s3AsyncClient.putObject(PutObjectRequest.builder()
                                                                                                    .bucket(getBucketName())
                                                                                                    .key(objectStoreDirAndFileName)
                                                                                                    .build(),
                                                                                            AsyncRequestBody.fromBytes(data));

            // Warte bis der Upload durch ist
            completionFuture.join();
            return !completionFuture.isCompletedExceptionally();
        } catch (Exception e) {
            Logger.logExceptionWithoutThrowing(logChannel, LogType.ERROR, e);
            connected = false;
            return false;
        }
    }

    /**
     * Überträgt die übergebene Datei in den Object Store
     *
     * @param objectStoreDirAndFileName Dateiname inkl. Object Store Verzeichnis
     * @param fileToUpload
     * @return
     */
    public boolean uploadFile(String objectStoreDirAndFileName, DWFile fileToUpload) {
        if (!isConnectionValid()) {
            Logger.log(logChannel, LogType.ERROR, usage + ": Could not reinit connection to object store to upload file!");
            return false;
        }

        try {
            CompletableFuture<PutObjectResponse> completionFuture = s3AsyncClient.putObject(PutObjectRequest.builder()
                                                                                                    .bucket(getBucketName())
                                                                                                    .key(objectStoreDirAndFileName)
                                                                                                    .build(),
                                                                                            AsyncRequestBody.fromFile(fileToUpload));

            // Warte bis der Upload durch ist
            completionFuture.join();
            return !completionFuture.isCompletedExceptionally();
        } catch (Exception e) {
            Logger.logExceptionWithoutThrowing(logChannel, LogType.ERROR, e);
            connected = false;
            return false;
        }
    }

    /**
     * Überträgt den gesamten Inhalt vom übergebenen Verzeichnis rekursiv in den Object Store
     *
     * @param objectStoreDirectory
     * @param dirToUpload
     * @return
     */
    public boolean uploadDir(String objectStoreDirectory, DWFile dirToUpload) {
        if (!isConnectionValid()) {
            Logger.log(logChannel, LogType.ERROR, usage + ": Could not reinit connection to object store to upload directory!");
            return false;
        }

        try (S3TransferManager transferManager = S3TransferManager.builder()
                .s3Client(s3AsyncClient)
                .build()) {
            UploadDirectoryRequest dirUploadRequest = UploadDirectoryRequest.builder()
                    .bucket(getBucketName())
                    .s3Prefix(objectStoreDirectory)
                    .source(Paths.get(dirToUpload.getAbsolutePath()))
                    .build();
            DirectoryUpload dirUpload = transferManager.uploadDirectory(dirUploadRequest);

            // Warte bis der Upload durch ist
            CompletedDirectoryUpload completedDirUpload = dirUpload.completionFuture().join();
            return completedDirUpload.failedTransfers().isEmpty();
        } catch (Exception e) {
            Logger.logExceptionWithoutThrowing(logChannel, LogType.ERROR, e);
            connected = false;
            return false;
        }
    }
}