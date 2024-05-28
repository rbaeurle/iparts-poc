import proguard.gradle.ProGuardTask
import java.text.SimpleDateFormat
import java.util.*

plugins {
    war
    id("com.github.hierynomus.license-report") version "0.16.1"
}

val includeInWar by configurations.creating
val ipartsVersion = properties["ipartsVersion"]
val warTemplate = "src/app/de/docware/apps/etk/plugins/customer/daimler/buildfiles/war_template"
java.sourceCompatibility = JavaVersion.VERSION_11
java.targetCompatibility = JavaVersion.VERSION_11

buildscript {
    dependencies {
        classpath("com.guardsquare:proguard-gradle:7.4.2")
    }
}

sourceSets {
    main {
        java {
            setSrcDirs(listOf("src/app"))
            include("de/docware/util/sql/**")
            include("de/docware/framework/modules/db/**")
            include("de/docware/apps/etk/base/**")
            include("de/docware/apps/etk/plugins/customer/daimler/**")
            exclude("de/docware/apps/etk/plugins/customer/daimler/iparts/tests/**")
            exclude("de/docware/apps/etk/plugins/customer/daimler/**/Test*.java")
        }
        resources {
            setSrcDirs(listOf("src/app"))
            include("de/docware/apps/etk/plugins/customer/daimler/**/*.sql")
            include("de/docware/apps/etk/plugins/customer/daimler/**/*.png")
        }
    }
}

repositories {
    mavenCentral()
    maven {
        name = "sisOne"
        url = uri("https://nexus.dev.qcpt.io/repository/sis-one-maven/")
        credentials(PasswordCredentials::class)
    }
    maven {
        name = "JBoss 3rd-party"
        url = uri("https://repository.jboss.org/nexus/content/repositories/thirdparty-releases/")
    }
    maven {
        name = "Gael"
        url = uri("https://repository.gael-systems.com/repository/public/")
    }
}

dependencies {
    // JAVAVIEWER DEPENDENCIES
    compileOnly("javax.servlet:servlet-api:2.5")
    implementation("com.sun.xml.ws:jaxws-ri:2.2.5")
    implementation("commons-io:commons-io:2.11.0")
    implementation("org.apache.commons:commons-lang3:3.12.0")
    implementation("org.apache.commons:commons-collections4:4.4")
    implementation("commons-fileupload:commons-fileupload:1.3.1")
    implementation("commons-codec:commons-codec:1.11")
    implementation("commons-net:commons-net:3.3")
    implementation("org.codehaus.janino:commons-compiler:3.1.10")
    implementation("org.codehaus.janino:commons-compiler-jdk:3.1.10")
    implementation("org.apache.commons:commons-compress:1.18")
    implementation("org.apache.commons:commons-email:1.5")
    implementation("org.apache.commons.imaging:commons-imaging:pre1.0_03")
    implementation("org.apache.ant:ant:1.7.0")
    implementation("org.apache.lucene:lucene-core:2.4.0")
    implementation("org.apache.lucene:lucene-highlighter:2.4.0")
    implementation("org.apache.lucene.store.transform:lucene-transform:0.2")
    implementation("com.owlike:genson:1.3")
    // iText ("@jar" wird benötigt um explizit Version 4.2.0 zu erhalten, ansonsten Relocation)
    implementation("com.lowagie:itext:4.2.0@jar")
    implementation("joda-time:joda-time:2.12.7")
    implementation("org.json:json:20240205")
    implementation("com.jcraft:jsch:0.1.54")
    implementation("com.jcraft:jzlib:1.1.3")
    implementation("org.apache.avalon.framework:avalon-framework-impl:4.3.1")
    implementation("org.apache.xmlgraphics:batik-all:1.17")
    implementation("org.beanshell:bsh-core:2.0b4")
    implementation("com.jniwrapper:jniwrap:3.12")
    implementation("com.jniwrapper.win32:comfyj:2.13")
    implementation("com.jniwrapper.win32:winpack:3.12")
    implementation("cryptix:cryptix:3.2.0")
    implementation("org.apache.httpcomponents.client5:httpclient5:5.2.1")
    implementation("accusoft.ig:imagegear:1.0")
    implementation("org.codehaus.janino:janino:3.1.10")
    implementation("com.onelogin:java-saml:2.9.0")
    implementation("org.apache.jcs:jcs:1.3")
    implementation("org.jdom:jdom2:2.0.6.1")
    implementation("com.jtheory.jdring:jdring:2.0")
    implementation("com.oracle.database.jdbc:ojdbc8:21.9.0.0")
    implementation("au.com.bytecode:opencsv:2.4")
    implementation("com.helger:ph-commons:9.4.5")
    implementation("com.helger:ph-css:6.2.3")
    implementation("ar.com.hjg:pngj:2.1.0")
    implementation("org.apache.poi:poi:5.2.3")
    implementation("org.apache.poi:poi-ooxml:5.2.3")
    implementation("com.sap.mw.jco:sapjco:2.1.9")
    implementation("com.sap:sapjco:3.0.14")
    implementation("net.coobird:thumbnailator:0.4.8")
    implementation("org.thymeleaf:thymeleaf:3.0.11.RELEASE")
    implementation("ach:tiffy:1.0")
    implementation("com.quanos:teamDevLics:1.0")
    implementation("dev.samstevens.totp:totp:1.7.1")
    implementation("org.apache.tomcat:tomcat-jdbc:11.0.0-M16")
    implementation("org.bidib.com.github.markusbernhardt:proxy-vole:1.0.16")
    implementation("com.quanos:h2quanos:1.2.144_DOCWARE_SVN3022_BUILD_24")
    implementation("com.sun.media:jai-codec:1.1.3")
    implementation("com.sun.media:jai_core:1.1.3")

    // JAX-WS
    implementation("com.sun.xml.ws:jaxws-tools:2.3.1")
    implementation("com.sun.xml.ws:jaxws-rt:2.3.5")
    implementation("javax.xml.ws:jaxws-api:2.3.1")

    // GraphicsMagick
    implementation("com.quanos:graphicsmagick:1.3.35")

    // TEST
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testImplementation("org.hamcrest:hamcrest-all:1.3")
    testImplementation("httpunit:httpunit:1.7")
    testImplementation("org.mockito:mockito-core:3.2.4")
    testImplementation("org.mockito:mockito-junit-jupiter:3.2.4")


    // IPARTS PLUGIN DEPENDENCIES
    // intern
    // implementation("com.quanos:javaviewer:7.24.1.3-240229-101112")
    implementation("com.quanos:javaviewer:7.24.1.6-240502-100435")
    // implementation("com.quanos:javaviewer-translations:7.24.1.3-240227-135059")
    implementation("com.quanos:javaviewer-translations:7.24.1.6-240502-100435")
    includeInWar(fileTree("src/app/de/docware/apps/etk/plugins/customer/daimler") { include("**/*translations.jar") })

    // extern
    implementation("org.apache.activemq:activemq-all:5.18.3")
    includeInWar("org.apache.activemq:activemq-all:5.18.3")
    implementation("software.amazon.awssdk:s3:2.21.5")
    implementation("software.amazon.awssdk:s3-transfer-manager:2.21.5")
    implementation("org.flywaydb:flyway-core:9.22.2")
    implementation("org.apache.commons:commons-pool2:2.11.1")
    implementation("redis.clients:jedis:4.4.3")
}

tasks.named<War>("war") {
    archiveBaseName = "iPartsEdit"
    classpath(includeInWar)
    webAppDirectory = file(warTemplate)
    dependsOn(migrationJar)
    dependsOn("versionTxt")
    dependsOn(ipartsWebserviceWar)
    exclude("WEB-INF/web-ws.xml")
    rename {
        it.replace("web-edit.xml", "web.xml")
    }
    from("../framework/de/docware/framework/extern/gm") {
        into("WEB-INF/lib/gm/")
    }
}

val ipartsWebserviceWar by tasks.registering(War::class) {
    archiveBaseName = "iPartsWebservice"
    classpath(includeInWar)
    webAppDirectory = file(warTemplate)
    dependsOn("versionTxt")
    rootSpec.exclude("de/docware/apps/etk/plugins/customer/daimler/iparts_edit/**")
    rootSpec.exclude("de/docware/apps/etk/plugins/customer/daimler/iparts_export/**")
    rootSpec.exclude("de/docware/apps/etk/plugins/customer/daimler/iparts_import/**")
    rootSpec.exclude("schemas")
    rootSpec.exclude("a*.xsd")
    from("../framework/de/docware/framework/extern/gm") {
        into("WEB-INF/lib/gm/")
    }
    exclude("WEB-INF/web-edit.xml")
    rename {
        it.replace("web-ws.xml", "web.xml")
    }
}

tasks.register("versionTxt") {
    doLast {
        file("$warTemplate/version.txt").writeText("""
            javaviewer
            $ipartsVersion
        """.trimIndent())
    }
}

val copyDependencies by tasks.registering(Copy::class) {
    from(configurations.runtimeClasspath)
    into(layout.projectDirectory.dir("extLibs"))
}

val migrationJar by tasks.registering(Jar::class) {
    group = "iparts"
    description = "Assembles a jar archive containing the Flyway migrations."
    archiveBaseName = "migration"
    from(fileTree("src/app/de/docware/apps/etk/plugins/customer/daimler/iparts/database/db/migration/") { include("*.sql") }) {
        into("db/migration/")
    }
    manifest {
        attributes(mapOf("Project-Title" to project.name,
                "Version" to ipartsVersion,
                "Description" to "Jar archive containing the Flyway migrations",
                "Date" to SimpleDateFormat("MM-dd-yyyy_hh-mm").format(Date())))
    }
}

downloadLicenses {
    includeProjectDependencies = true
    dependencyConfiguration = "runtimeClasspath"
}

tasks.compileJava {
    options.encoding = "utf-8"
}

tasks.register("00_generateTranslations") {
    group = "translations"
    description = "Generates new translation properties from the source code."

    dependsOn("01_buildPluginJarFiles")
    dependsOn("02_createPluginProGuardDumps")
    dependsOn("03_createTranslationProperties")

    tasks.findByName("02_createPluginProGuardDumps")?.mustRunAfter("01_buildPluginJarFiles")
    tasks.findByName("03_createTranslationProperties")?.mustRunAfter("02_createPluginProGuardDumps")
}

tasks.register("01_buildPluginJarFiles") {
    group = "translations"
    description = "Generates a jar file for each plugin."

    dependsOn("buildMainPluginJar")
    dependsOn("buildEditPluginJar")
    dependsOn("buildExportPluginJar")
    dependsOn("buildImportPluginJar")
    dependsOn("buildWebservicePluginJar")

    tasks.findByName("buildEditPluginJar")?.mustRunAfter("buildMainPluginJar")
    tasks.findByName("buildExportPluginJar")?.mustRunAfter("buildEditPluginJar")
    tasks.findByName("buildImportPluginJar")?.mustRunAfter("buildExportPluginJar")
    tasks.findByName("buildWebservicePluginJar")?.mustRunAfter("buildImportPluginJar")

}

tasks.register("02_createPluginProGuardDumps") {
    group = "translations"
    description = "Generates a ProGuard dump file for each plugin."

    dependsOn("createMainPluginProGuardDump")
    dependsOn("createEditPluginProGuardDump")
    dependsOn("createExportPluginProGuardDump")
    dependsOn("createImportPluginProGuardDump")
    dependsOn("createWebservicePluginProGuardDump")

    tasks.findByName("createMainPluginProGuardDump")?.mustRunAfter("buildWebservicePluginJar")
    tasks.findByName("createEditPluginProGuardDump")?.mustRunAfter("createMainPluginProGuardDump")
    tasks.findByName("createExportPluginProGuardDump")?.mustRunAfter("createEditPluginProGuardDump")
    tasks.findByName("createImportPluginProGuardDump")?.mustRunAfter("createExportPluginProGuardDump")
    tasks.findByName("createWebservicePluginProGuardDump")?.mustRunAfter("createImportPluginProGuardDump")

}

tasks.register<JavaExec>("03_createTranslationProperties") {
    group = "translations"
    description = "Generates a Properties file from the ProGuard dump for the main plugin."
    classpath = sourceSets["main"].runtimeClasspath
    mainClass = "de.docware.apps.etk.plugins.customer.daimler.translations.TranslationProperties"
}

tasks.register<ProGuardTask>("createMainPluginProGuardDump") {
    group = "translations/proguard"
    description = "Generates a ProGuard Dump for main plugin jar file."

    dontshrink()
    dontoptimize()
    dontobfuscate()
    dontpreverify()
    dontnote("**")
    dontwarn("**")

    injars(layout.buildDirectory.file("translations/jars/iPartsMainPlugin.jar"))
    dump(layout.buildDirectory.dir("translations/proguard/ProGuardDumpMainPlugin.dump_out"));
}

tasks.register<ProGuardTask>("createEditPluginProGuardDump") {
    group = "translations/proguard"
    description = "Generates a ProGuard Dump for edit plugin jar file."

    dontshrink()
    dontoptimize()
    dontobfuscate()
    dontpreverify()
    dontnote("**")
    dontwarn("**")

    injars(layout.buildDirectory.file("translations/jars/iPartsEditPlugin.jar"))
    dump(layout.buildDirectory.dir("translations/proguard/ProGuardDumpEditPlugin.dump_out"));
}

tasks.register<ProGuardTask>("createExportPluginProGuardDump") {
    group = "translations/proguard"
    description = "Generates a ProGuard Dump for export plugin jar file."

    dontshrink()
    dontoptimize()
    dontobfuscate()
    dontpreverify()
    dontnote("**")
    dontwarn("**")

    injars(layout.buildDirectory.file("translations/jars/iPartsExportPlugin.jar"))
    dump(layout.buildDirectory.dir("translations/proguard/ProGuardDumpExportPlugin.dump_out"));
}

tasks.register<ProGuardTask>("createImportPluginProGuardDump") {
    group = "translations/proguard"
    description = "Generates a ProGuard Dump for import plugin jar file."

    dontshrink()
    dontoptimize()
    dontobfuscate()
    dontpreverify()
    dontnote("**")
    dontwarn("**")

    injars(layout.buildDirectory.file("translations/jars/iPartsImportPlugin.jar"))
    dump(layout.buildDirectory.dir("translations/proguard/ProGuardDumpImportPlugin.dump_out"));
}

tasks.register<ProGuardTask>("createWebservicePluginProGuardDump") {
    group = "translations/proguard"
    description = "Generates a ProGuard Dump for webservice plugin jar file."

    dontshrink()
    dontoptimize()
    dontobfuscate()
    dontpreverify()
    dontnote("**")
    dontwarn("**")

    injars(layout.buildDirectory.file("translations/jars/iPartsWebservicePlugin.jar"))
    dump(layout.buildDirectory.dir("translations/proguard/ProGuardDumpWebservicePlugin.dump_out"));
}

tasks.register<Jar>("buildMainPluginJar") {
    group = "translations/jars"
    description = "Generates a war file for the iParts main plugin."
    archiveBaseName = "iPartsMainPlugin"
    from(sourceSets["main"].runtimeClasspath)
    rootSpec.exclude("de/docware/apps/etk/plugins/customer/daimler/iparts_edit/**")
    rootSpec.exclude("de/docware/apps/etk/plugins/customer/daimler/iparts_export/**")
    rootSpec.exclude("de/docware/apps/etk/plugins/customer/daimler/iparts_import/**")
    rootSpec.exclude("de/docware/apps/etk/plugins/customer/daimler/iparts_webservice/**")
    rootSpec.exclude("schemas")
    rootSpec.exclude("a*.xsd")
    destinationDirectory.set(file(layout.buildDirectory.dir("translations/jars")))
}

tasks.register<Jar>("buildEditPluginJar") {
    group = "translations/jars"
    description = "Generates a jar file for the iParts edit plugin."
    archiveBaseName = "iPartsEditPlugin"
    from(sourceSets["main"].runtimeClasspath)
    rootSpec.exclude("de/docware/apps/etk/plugins/customer/daimler/iparts/**")
    rootSpec.exclude("de/docware/apps/etk/plugins/customer/daimler/iparts_export/**")
    rootSpec.exclude("de/docware/apps/etk/plugins/customer/daimler/iparts_import/**")
    rootSpec.exclude("de/docware/apps/etk/plugins/customer/daimler/iparts_webservice/**")
    rootSpec.exclude("schemas")
    rootSpec.exclude("a*.xsd")
    rootSpec.exclude("de/docware/apps/etk/plugins/customer/daimler/buildfiles/**")
    rootSpec.exclude("de/docware/apps/etk/plugins/customer/daimler/documentation/**")
    rootSpec.exclude("de/docware/apps/etk/plugins/customer/daimler/translations/**")
    destinationDirectory.set(file(layout.buildDirectory.dir("translations/jars")))
}

tasks.register<Jar>("buildExportPluginJar") {
    group = "translations/jars"
    description = "Generates a jar file for the iParts export plugin."
    archiveBaseName = "iPartsExportPlugin"
    from(sourceSets["main"].runtimeClasspath)
    rootSpec.exclude("de/docware/apps/etk/plugins/customer/daimler/iparts_edit/**")
    rootSpec.exclude("de/docware/apps/etk/plugins/customer/daimler/iparts/**")
    rootSpec.exclude("de/docware/apps/etk/plugins/customer/daimler/iparts_import/**")
    rootSpec.exclude("de/docware/apps/etk/plugins/customer/daimler/iparts_webservice/**")
    rootSpec.exclude("schemas")
    rootSpec.exclude("a*.xsd")
    rootSpec.exclude("de/docware/apps/etk/plugins/customer/daimler/buildfiles/**")
    rootSpec.exclude("de/docware/apps/etk/plugins/customer/daimler/documentation/**")
    rootSpec.exclude("de/docware/apps/etk/plugins/customer/daimler/translations/**")
    destinationDirectory.set(file(layout.buildDirectory.dir("translations/jars")))
}

tasks.register<Jar>("buildImportPluginJar") {
    group = "translations/jars"
    description = "Generates a jar file for the iParts import plugin."
    archiveBaseName = "iPartsImportPlugin"
    from(sourceSets["main"].runtimeClasspath)
    rootSpec.exclude("de/docware/apps/etk/plugins/customer/daimler/iparts_edit/**")
    rootSpec.exclude("de/docware/apps/etk/plugins/customer/daimler/iparts_export/**")
    rootSpec.exclude("de/docware/apps/etk/plugins/customer/daimler/iparts/**")
    rootSpec.exclude("de/docware/apps/etk/plugins/customer/daimler/iparts_webservice/**")
    rootSpec.exclude("schemas")
    rootSpec.exclude("a*.xsd")
    rootSpec.exclude("de/docware/apps/etk/plugins/customer/daimler/buildfiles/**")
    rootSpec.exclude("de/docware/apps/etk/plugins/customer/daimler/documentation/**")
    rootSpec.exclude("de/docware/apps/etk/plugins/customer/daimler/translations/**")
    destinationDirectory.set(file(layout.buildDirectory.dir("translations/jars")))
}

tasks.register<Jar>("buildWebservicePluginJar") {
    group = "translations/jars"
    description = "Generates a jar file for the iParts webservice plugin."
    archiveBaseName = "iPartsWebservicePlugin"
    from(sourceSets["main"].runtimeClasspath)
    rootSpec.exclude("de/docware/apps/etk/plugins/customer/daimler/iparts_edit/**")
    rootSpec.exclude("de/docware/apps/etk/plugins/customer/daimler/iparts_export/**")
    rootSpec.exclude("de/docware/apps/etk/plugins/customer/daimler/iparts_import/**")
    rootSpec.exclude("de/docware/apps/etk/plugins/customer/daimler/iparts/**")
    rootSpec.exclude("schemas")
    rootSpec.exclude("a*.xsd")
    rootSpec.exclude("de/docware/apps/etk/plugins/customer/daimler/buildfiles/**")
    rootSpec.exclude("de/docware/apps/etk/plugins/customer/daimler/documentation/**")
    rootSpec.exclude("de/docware/apps/etk/plugins/customer/daimler/translations/**")
    destinationDirectory.set(file(layout.buildDirectory.dir("translations/jars")))
}