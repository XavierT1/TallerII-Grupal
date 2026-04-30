import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java")
    application
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("edu.sc.seis.launch4j") version "3.0.5"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.formdev:flatlaf:3.4")
    implementation("com.formdev:flatlaf-extras:3.4")
}

application {
    mainClass.set("com.programacion.Principal")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.shadowJar {
    archiveBaseName.set("TallerII")
    archiveClassifier.set("")
    archiveVersion.set(project.version.toString())
    mergeServiceFiles()
}

launch4j {
    mainClassName.set("com.programacion.Principal")
    outfile.set("TallerII-App.exe")
    errTitle.set("Error de Aplicacion")
    dontWrapJar.set(false)
    downloadUrl.set("https://adoptium.net/")
    // icon = "${projectDir}/src/main/resources/icon.ico" // TODO: Descomentar y cambiar a futuro cuando tengas un icono genérico .ico
}