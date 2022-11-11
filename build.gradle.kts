import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.testing.logging.TestLogEvent.*
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin ("jvm") version "1.6.10"
  application
  id("com.github.johnrengelman.shadow") version "7.0.0"
  id("com.parmet.buf") version "0.8.2"
}

group = "com.example"
version = "1.0.0-SNAPSHOT"

repositories {
  mavenCentral()
}

val vertxVersion = "4.3.4"
val junitJupiterVersion = "5.7.0"

val mainVerticleName = "com.example.test_vertx_grpc.MainVerticle"
val launcherClassName = "io.vertx.core.Launcher"

val watchForChange = "src/**/*"
val doOnChange = "${projectDir}/gradlew classes"

application {
  mainClass.set(launcherClassName)
}

dependencies {
  implementation(platform("io.vertx:vertx-stack-depchain:$vertxVersion"))
  implementation("io.vertx:vertx-web")
  implementation("io.vertx:vertx-grpc-server")
  implementation("io.vertx:vertx-lang-kotlin")
  implementation("io.grpc:grpc-kotlin-stub:1.3.0")
  implementation("com.google.protobuf:protobuf-kotlin:3.21.9")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
  implementation(kotlin("stdlib-jdk8"))

  //Logging
  implementation("io.github.microutils:kotlin-logging-jvm:3.0.3")
  implementation("ch.qos.logback:logback-classic:1.4.4")

  testImplementation("io.vertx:vertx-junit5")
  testImplementation("org.junit.jupiter:junit-jupiter:$junitJupiterVersion")
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions.jvmTarget = "17"

sourceSets["main"].java { srcDir("$buildDir/bufbuild/generated/java") }
sourceSets["main"].java { srcDir("$buildDir/bufbuild/generated/kotlin") }

tasks.withType<ShadowJar> {
  archiveClassifier.set("fat")
  manifest {
    attributes(mapOf("Main-Verticle" to mainVerticleName))
  }
  mergeServiceFiles()
}

tasks.withType<Test> {
  useJUnitPlatform()
  testLogging {
    events = setOf(PASSED, SKIPPED, FAILED)
  }
}

tasks.withType<JavaExec> {
  args = listOf("run", mainVerticleName, "--redeploy=$watchForChange", "--launcher-class=$launcherClassName", "--on-redeploy=$doOnChange")
}
