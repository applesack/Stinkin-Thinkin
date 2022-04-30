plugins {
    kotlin("jvm") version "1.6.10"
    java
}

group = "xyz.scootaloo"
version = "0.1"

repositories {
    maven("https://maven.aliyun.com/repository/public/")
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))

    val vertxVersion = "4.2.7"
    implementation("io.vertx:vertx-core:$vertxVersion")
    implementation("io.vertx:vertx-config:$vertxVersion")
    implementation("io.vertx:vertx-web:$vertxVersion")
    implementation("io.vertx:vertx-lang-kotlin:$vertxVersion")
    implementation("io.vertx:vertx-lang-kotlin-coroutines:$vertxVersion")

    // xml
    implementation("org.dom4j:dom4j:2.1.3")

    // test dependencies
    implementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    implementation("org.junit.jupiter:junit-jupiter-engine:5.8.2")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}