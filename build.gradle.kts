plugins {
    kotlin("jvm") version "1.9.24"
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.xerial:sqlite-jdbc:3.46.1.0")

    testImplementation(kotlin("test"))
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher") // <--- Добавьте это
}

application {
    mainClass.set("MainKt")
}


tasks.test {
    useJUnitPlatform()
}

tasks.run {
    standardInput = System.`in`
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "MainKt"
    }
}

tasks.register<JavaExec>("runGui") {
    group = "application"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("monopoly.ui.gui.GuiMainKt")
}
