import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.5.2"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    id("me.champeau.jmh") version "0.6.5"
    kotlin("jvm") version "1.5.20"
    kotlin("plugin.spring") version "1.5.20"
}

group = "com.totango"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_1_8

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.reactivestreams:reactive-streams:1.0.4")
    implementation("org.openjdk.jmh:jmh-core:1.32")
    implementation("org.openjdk.jmh:jmh-generator-annprocess:1.32")
    testImplementation("org.reactivestreams:reactive-streams-tck:1.0.3")
    testImplementation("org.testng:testng:7.4.0")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "1.8"
    }
}

tasks.withType<Test> {
    useTestNG {
        useDefaultListeners = true
    }
}


jmh {
	resultFormat.set("JSON") // Result format type (one of CSV, JSON, NONE, SCSV, TEXT)
	resultsFile.set(project.file("${project.buildDir}/reports/jmh/results.json"))
	duplicateClassesStrategy.set(DuplicatesStrategy.WARN) // Strategy to apply when encountring duplicate
}
