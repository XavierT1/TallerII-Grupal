plugins {
    id("java")
    application
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