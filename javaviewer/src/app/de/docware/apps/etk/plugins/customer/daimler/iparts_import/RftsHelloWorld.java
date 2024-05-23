package de.docware.apps.etk.plugins.customer.daimler.iparts_import;

import de.docware.util.file.DWFile;
import de.docware.util.misc.reflection.ReflectionException;
import de.docware.util.misc.reflection.ReflectionUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.*;


/**
 * Diese Klasse ist eine HelloWorld-Anwendung für DAIMLER-504. Wenn der Code real in den Importer eingebaut ist, kann sie wieder entfernt werden.
 * Der Listener-Mechanismus ist abgeschaut von FileChangeMonitor aus PPSync.
 * In Java 7 gibt es einen Mechanismus zum Überwachen von Verzeichnissen https://docs.oracle.com/javase/tutorial/essential/io/notification.html.
 * Wir müssen aber Java 6 unterstützen.
 * Mit DAIMLER-2160 wurde eine Unterstützung für den Java 7 Mechanismus per Reflection hinzugefügt.
 *
 * Testanleitung
 * - Verzeichniss RFTS_SHARE anlegen
 * - Arbeitsverzeichnis anlegen (je eines pro Instanz)
 * - mehrere Instanzen dieser Klasse starten (simulieren mehrere Clusterknoten); für jede Instanz mittels Programmparameter den
 * Arbeitsverzeichnispfad einstellen
 * - Dateien mit vereinbarter Endung ALLOWED_EXTENSIONS in RFTS_SHARE kopieren
 * - die gewinnende Instanz kopiert die Datei in ihr Arbeitsverzeichnis
 */
public class RftsHelloWorld {

    public static final String RFTS_SHARE = "D:\\Temp\\rftsx\\share"; // das überwachte Share (Clusterweit)
    public static final String RFTS_ARCHIVE_SHARE = "D:\\Temp\\rftsx\\share\\archive"; // Archivverzeichnis (Clusterweit); hier bisher unberücksichtig
    public static final String[] ALLOWED_EXTENSIONS = new String[]{ "xml", "csv" }; // vereinbarte Liste der Fileextensions, die bei Polling berücksichtigt wird

    static String workDir;
    final static UUID uuid = UUID.randomUUID();

    public static void main(String[] args) throws IOException, ReflectionException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, ClassNotFoundException {
        final boolean deamon = false;  // VM will end if only deamon threads are active

        FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                for (String ext : ALLOWED_EXTENSIONS) {
                    if (name.toLowerCase().endsWith("." + ext)) {
                        return true;
                    }
                }
                return false;
            }
        };

        if (args.length != 0) {
            workDir = args[0];
        } else {
            System.out.println("Arbeitsverzeichnis Parameter fehlt");
            return;
        }

        DirectoryChangeMonitor monitor = new DirectoryChangeMonitor(filter, 0, 5000, deamon);
        monitor.addListener(new DirectoryChangeEventListener() {

            @Override
            public void fireEvent(DirectoryChangeEvent event) {
                List<DWFile> files = event.getFiles();

                for (DWFile file : files) {
                    /**
                     * An Hand Dateinamen entscheiden ob Datei zu einem Fileset gehört. Unter Fileset verstehe ich Gruppe
                     * zusammengehörender Dateien, die sinnvollerweise nur von einer Importer Instanz bearbeitet werden können.
                     * Wenn eine Datei aus einem Fileset erkannt wird muss sich im Cluster geeinigt werden wer dieses Fileset
                     * importiert. Wie das technisch gemacht wird ist hier out-of-scope.
                     * Diese Funktion bzw. das hier zu demonstrieren verschieben wir auf später.
                     */

                    System.out.print("Instanz mit workdir '" + workDir + "' tries to get file " + file.getAbsoluteFile() + "...");
                    try {
                        if (moveFile(file, DWFile.get(workDir))) {
                            // verschieben hat geklappt -> wir sind der Bearbeiter
                            System.out.println("Success -> we can now process the file");

                            // um den Zweck einer Datei zu erkennen wird dieser in den Dateinamen kodiert. Eventuell wird auch eine Unterverzeichnisstruktur aufgebaut,
                            // die Dateien nach deren Zweck unterscheidbar macht. Hier muss noch mit ITR eine Vereinbarung getroffen werden.
                        } else {
                            // verschieben hat nicht geklappt -> ein anderer Cluster-Knoten hat die Datei verschoben und ist der Bearbeiter
                            System.out.println("Error -> another node in cluster will process the file");
                        }
                    } catch (IOException e) {
                        System.err.println("Error '" + e.getMessage() + "'");
                    }
                }
            }
        });

        monitor.startMonitoring();

    }


    /**
     * Das Verschieben von Dateien ist gar nicht so trivial wie es zunächst aussieht.
     *
     * Zunächst Klärung des Begriffs "atomar":
     * Eine Aktion ist atomar wenn nach der Aktion die Datei im Zielverzeichnis ist, und sicher im Quellverzeichnis entfernt wurde.
     * Für Java <= 7 kann nie garantiert werden dass das Verschieben atomar verläuft weil immer eine Kopie der Datei erstellt wird,
     * und anschließend die Originaldatei gelöscht wird. Dabei ist letztlich nicht ausgeschlossen dass die Vorgang unvollständig abläuft
     * und am Ende z.B. Dateien auf Quelle und Ziel entstehen.
     * Mit Java >= 7 wird Atomarität zugesichert wenn Quelle und Ziel auf einem Dateisystem liegen. Dabei wird mit native Filesystem-API garbeitet,
     * die natürlich nur Kontrolle über EIN Filesystem haben. Für Verschiebung z.B. zwischen lokalem Dateisystem und Netzwerkshare kann das nicht garantiert werden.
     * Letzteres ist ja henau unsere Anforderung. Daher gehen wir folgenden Weg:
     * - wir benennen die Datei auf dem Share zunächst um. Das sollte unter Java 7 ein atomarer Vorgang sein. Dabei verwenden wir eine String-Konstante
     * die eindeutig für eine iParts-Instanz ist. In diesem Beispiel eine statische GUID - es kann aber auch irgendein anderer Mechanismus sein.
     * - durch das Umbenennen ist die Datei nicht mehr sichtbar für andere Instanzen
     * - nun kann die bearbeitende Instanz die Datei in ihr lokales Arbeitsverzeichnis verschieben. Dies ist zwar nicht-atomar aber kann nicht mehr
     * durch andere Instanze gestört werden.
     *
     * @param file
     * @param destDir
     * @return
     * @throws IOException
     */
    private static boolean moveFile(DWFile file, DWFile destDir) throws IOException {
        String filename = file.getName() + "." + uuid; // innerhalb des Verzeichnisses umbenennen hat hoffentlich die größte Chance für atomar
        String destPath = file.extractDirectory();
        DWFile destFile = DWFile.get(destPath, filename);

        if (file.renameTo(destFile)) {  // todo evtl. nicht atomar; mit java.nio atomar wenn auf gleichem Device; siehe JavaDoc
            return destFile.move(destDir, true);
        } else {
            return false;
        }

    }

}


class DirectoryChangeEvent {

    private DWFile dir;
    private List<DWFile> files;

    public DirectoryChangeEvent(DWFile dir, List<DWFile> files) {
        this.dir = dir;
        this.files = files;
    }

    public DWFile getDir() {
        return dir;
    }

    public List<DWFile> getFiles() {
        return files;
    }

}

interface DirectoryChangeEventListener {

    public void fireEvent(DirectoryChangeEvent event);
}

/**
 * Überwacht eine Liste von Verzeichnissen per Polling; wenn eine Datei in dem Verzeichnis auftaucht, die der definierten
 * Filterregel entspricht, werden die registrierten Listener darüber informiert.
 * Arbeitet mit Polling unter Java < 7 und java.nio.file unter Java >= 7
 *
 * Die Java 7 Funktion wird von Klasse Java7WatchDir implementiert. Der Code wurde dann in Reflection-Aufrufe umgewandelt,
 * so dass die Klasse keine Importe von Java 7 verwendet, und damit (unter Java 6 lauffähig ist).
 * Die nach Refelection gewandelten Aufrufe wurden zur Verdeutlichung jeweils darüber auskommentiert stehen gelassen.
 */
class DirectoryChangeMonitor {

    private Collection listeners;   // of WeakReference(FileChangeEvent)
    //    private Map<WatchKey,Path> keys = null;
    private Map keys = null;
    private FilenameFilter filter;
    private long firstDelayTime, pollingInterval;  // nur für Java < 7
    private boolean deamon;  // nur für Java < 7

//    @SuppressWarnings("unchecked")
//    static <T> WatchEvent<T> cast(WatchEvent<?> event) {
//        return (WatchEvent<T>)event;
//    }

    public DirectoryChangeMonitor(final FilenameFilter filter, long firstDelayTime, long pollingInterval, boolean deamon) {
        this.filter = filter;
        this.firstDelayTime = firstDelayTime;
        this.pollingInterval = pollingInterval;
        this.deamon = deamon;
        listeners = new ArrayList();

    }

    /**
     * todo prüfen ob File in Bearbeitung; typischer UseCase: wird grad durch RFTS/x geschrieben
     * Recherche ergab es dass es offenbar keinen einfachen Weg das zu prüfen, der auf allen Plattformen sicher funktioniert
     * Für uns muss es ja auf Windows und Linux gehen.
     * DWFile.canWriteToFile() ist hier keine Hilfe da dies offenbar im Prinzip das Schreibzuattribut prüft
     */
    private boolean isFileInUse(DWFile file) {
        return false;
    }

    public void startMonitoring() throws ReflectionException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, ClassNotFoundException {
        try {
            new Java7WatchDir(RftsHelloWorld.RFTS_SHARE).handleEvents(filter);
        } catch (IOException e) {
            // todo
            e.printStackTrace();
        }
    }

    /**
     * add a new Listener
     * der Code stammt 1:1 aus FileChangeMonitor aus ppsync; Daher der unklare Zustand WeakReferences oder nicht
     *
     * @param fileListener
     */
    public void addListener(DirectoryChangeEventListener fileListener) {
        // Don't add if its already there
     /*   for (Iterator i = listeners.iterator(); i.hasNext();) {
            WeakReference reference = (WeakReference) i.next();
            FileChangeEvent listener = (FileChangeEvent) reference.get();
            if (listener == fileListener)
                return;
        }

        // Use WeakReference to avoid memory leak if this becomes the
        // sole reference to the object.
        listeners.add(new WeakReference(fileListener));*/
        if (!listeners.contains(fileListener)) {
            listeners.add(fileListener);
        }
    }

    /**
     * remove a Listener
     * der Code stammt 1:1 aus FileChangeMonitor aus ppsync; Daher der unklare Zustand WeakReferences oder nicht
     *
     * @param listener
     */
    public void removeListener(DirectoryChangeEventListener listener) {
        for (Iterator i = listeners.iterator(); i.hasNext(); ) {
            WeakReference reference = (WeakReference)i.next();
            DirectoryChangeEventListener aListener = (DirectoryChangeEventListener)reference.get();
            if (aListener == listener) {
                i.remove();
                break;
            }
        }
    }

    class Java7WatchDir {

        //        private final WatchService watchService;
        private final Object watchService;

        //        private final Map<WatchKey,Path> keys;
        private final Map keys;


        /**
         * Register the given directory with the WatchService
         */
//        private void register(Path dir) throws IOException, ReflectionException {
        private void register(Object dir) throws IOException, ReflectionException, ClassNotFoundException {

//            WatchKey key = dir.register(watcher, ENTRY_CREATE /*, ENTRY_DELETE, ENTRY_MODIFY*/);
            Object create = ReflectionUtils.getStaticField("java.nio.file.StandardWatchEventKinds", "ENTRY_CREATE");
            Object[] watchEventKindArrayArg = (Object[])Array.newInstance(Class.forName("java.nio.file.WatchEvent$Kind"), 1);
            watchEventKindArrayArg[0] = create;
            Object key = ReflectionUtils.doInstanceMethodCall(dir, "register",
                                                              new Class[]{ Class.forName("java.nio.file.WatchService"), ReflectionUtils.getClass("[Ljava.nio.file.WatchEvent$Kind;") },
                                                              new Object[]{ watchService, watchEventKindArrayArg }, true);

            keys.put(key, dir);
        }

        /**
         * Creates a WatchService and registers the given directory
         */
        Java7WatchDir(String dirStr) throws IOException, ReflectionException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, ClassNotFoundException {

//            this.watcher = FileSystems.getDefault().newWatchService();
            Object defaultFS = ReflectionUtils.doStaticMethodCall("java.nio.file.FileSystems", "getDefault");
            Object watchService = ReflectionUtils.doInstanceMethodCall(defaultFS, "newWatchService", true);
            this.watchService = watchService;


            this.keys = new HashMap();

//            Path dir = Paths.get(dirStr);
            Object dir = ReflectionUtils.doStaticMethodCall("java.nio.file.Paths", "get",
                                                            new Class[]{ String.class, String[].class },
                                                            new Object[]{ dirStr, new String[0] });

            register(dir);

        }


        /**
         * Process all events for keys queued to the watchService
         */
        void handleEvents(FilenameFilter filter) throws ReflectionException, ClassNotFoundException {
            for (; ; ) {

                // wait for key to be signalled
//                WatchKey key;
                Object key;

                try {
//                    key = watcher.take();
                    key = ReflectionUtils.doInstanceMethodCall(watchService, "take", true);
                } catch (Exception x) {
                    if (x instanceof InterruptedException) { // todo wieso sagt IDEA dass die Bedingung immer falsch ist?
                        return;
                    } else {
                        throw new ReflectionException(x.getMessage(), x);
                    }
                }

//                Path dir = (Path)keys.get(key);
                Object dir = keys.get(key);
                if (dir == null) {
                    System.err.println("WatchKey not recognized!!");
                    continue;
                }

                Object watchEvents = ReflectionUtils.doInstanceMethodCall(key, "pollEvents", true);
                for (Object event : (List<Object>)watchEvents) {
//                for (WatchEvent<?> event: key.pollEvents()) {

//                    WatchEvent.Kind kind = ((WatchEvent)event).kind();
                    Object kind = ReflectionUtils.doInstanceMethodCall(event, "kind", true);

                    // TBD - provide example of how OVERFLOW event is handled
                    Object overflow = ReflectionUtils.getStaticField("java.nio.file.StandardWatchEventKinds", "OVERFLOW");
                    if (kind == overflow) {
                        continue;
                    }

                    // Context for directory entry event is the file name of entry
//                    WatchEvent<Path> ev = cast((WatchEvent)event);
//                    Object ev = ReflectionUtils.doStaticMethodCall(this.getClass().getCanonicalName(), "cast", java.nio.file.WatchEvent.class, event);

//                    Path name = ((WatchEvent<Path>)ev).context();
                    Object name = ReflectionUtils.doInstanceMethodCall(event, "context", true);

//                    Path child = ((Path)dir).resolve((Path)name);
                    Object child = ReflectionUtils.doInstanceMethodCall(dir, "resolve", Class.forName("java.nio.file.Path"), name, true);

                    String filename = name.toString();
                    DWFile dwDir = DWFile.get(new File(RftsHelloWorld.RFTS_SHARE));
                    if (filter.accept(dwDir, filename)) {
                        // print out event
                        Object kindName = ReflectionUtils.doInstanceMethodCall(kind, "name", true);
                        System.out.format("%s: %s\n", kindName, child);

                        // Notify listeners
                        List<DWFile> dwFiles = new ArrayList<DWFile>();
                        dwFiles.add(DWFile.get(child.toString()));
                        for (Iterator j = listeners.iterator(); j.hasNext(); ) {
                            ((DirectoryChangeEventListener)j.next()).fireEvent(new DirectoryChangeEvent(dwDir, dwFiles));
                        }
                    }

                }

                // reset key and remove from set if directory no longer accessible
//                boolean valid = key.reset();
                boolean valid = (Boolean)ReflectionUtils.doInstanceMethodCall(key, "reset", true);
                if (!valid) {
                    keys.remove(key);

                    // all directories are inaccessible
                    if (keys.isEmpty()) {
                        break;
                    }
                }
            }
        }


    }

}


