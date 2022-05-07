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

    implementation("cn.hutool:hutool-all:5.7.18")

    val vertxVersion = "4.2.7"
    implementation("io.vertx:vertx-core:$vertxVersion")
    implementation("io.vertx:vertx-config:$vertxVersion")
    implementation("io.vertx:vertx-web:$vertxVersion")
    implementation("io.vertx:vertx-lang-kotlin:$vertxVersion")
    implementation("io.vertx:vertx-lang-kotlin-coroutines:$vertxVersion")

    // xml
    implementation("org.dom4j:dom4j:2.1.3")

    // logger
    // 高版本log4j不显示彩色日志, 需要在虚拟机选项中设置 -Dlog4j.skipJansi=false
    val log4jVersion = "2.17.2"
    implementation("org.apache.logging.log4j:log4j-api:$log4jVersion")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:$log4jVersion")
    implementation("org.apache.logging.log4j:log4j-api:$log4jVersion")
    implementation("org.apache.logging.log4j:log4j-core:$log4jVersion")
    implementation("org.slf4j:slf4j-api:1.7.36")

    // test dependencies
    implementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    implementation("org.junit.jupiter:junit-jupiter-engine:5.8.2")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}