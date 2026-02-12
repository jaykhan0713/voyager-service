plugins {
    java
    jacoco

    `jvm-test-suite`

    //spring
    id("io.spring.dependency-management") version "1.1.7"
    id("org.springframework.boot") version "4.0.0"

    //third party
    id("org.sonarqube") version "6.3.1.5724"
}

group = "com.jay.voyager"
version = "0.0.1-SNAPSHOT"
description = "voyager-service"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

configurations {
    compileOnly {
        // keep annotation processors to compile path, and not packaged on runtime classpath
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web") // embedded tomcat servlet container
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-restclient")
    implementation("org.springframework.boot:spring-boot-starter-opentelemetry")

    //logback
    implementation("net.logstash.logback:logstash-logback-encoder:8.0")

    //micrometer
    implementation("io.micrometer:micrometer-registry-prometheus")

    //OpenAPI
    implementation(platform("org.springdoc:springdoc-openapi-bom:3.0.0"))
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui")

    //project DTOs
    constraints {
        implementation("io.swagger.core.v3:swagger-annotations-jakarta:2.2.38")
    }
    //implementation(project(":openapi-dtos"))
    implementation("com.jay.voyager:voyager-openapi-dtos:0.0.1-SNAPSHOT")

    //Resilience4j
    implementation(platform("io.github.resilience4j:resilience4j-bom:2.3.0"))
    // no boot4 r4j starter yet. Need for autoconfig of source (yaml) properties
    implementation("io.github.resilience4j:resilience4j-spring-boot3")
    implementation("io.github.resilience4j:resilience4j-micrometer")

    //OpenTelemetry
    implementation(platform("io.opentelemetry:opentelemetry-bom:1.50.0"))
    implementation("io.opentelemetry:opentelemetry-exporter-otlp")

    //IDE mapping such as yml configs with javadocs, generates meta-data json at build time.
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

/* This is an application, not a library.
 * Disable the default <service-name>-SNAPSHOT-"plain" jar (classes-only, non-runnable) so that
 * build/libs contains ONLY the Spring Boot executable jar produced by bootJar.
 * Explicility name to app.jar
 */
tasks.jar { enabled = false }
tasks.bootJar {
    enabled = true
    archiveFileName.set("app.jar")
}

// JUnit
tasks.withType<Test> { //test and functionalTest will use this runner
    useJUnitPlatform()
}

//Spring boot
springBoot {
    mainClass.set("com.jay.voyager.Starter")
    buildInfo()
}

//Sonar
sonarqube {
    properties {
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.organization", "jaykhan0713")
        property("sonar.projectKey", "jaykhan0713_voyager-service")

        // coverage settings
        property("sonar.sources", "src/main/java")
        property("sonar.java.binaries", "build/classes/java/main")

        property("sonar.tests", "src/test/java,src/functionalTest/java")
        property("sonar.java.test.binaries", "build/classes/java/test,build/classes/java/functionalTest")

        property("sonar.junit.reportPaths", "build/test-results/test,build/test-results/functionalTest")
        property(
            "sonar.coverage.jacoco.xmlReportPaths",
            "build/reports/jacoco/test/jacocoTestReport.xml"
        )

        //exclusions
        property(
            "sonar.exclusions", "**/api/**/openapi/**" // sonar complains about openAPI style annotations.
        )

        // stops openApi plural annotations from being marked as an issue
        property("sonar.issue.ignore.multicriteria", "openapiS1710") //',' separated value for more rules if needed.
        property("sonar.issue.ignore.multicriteria.openapiS1710.ruleKey", "java:S1710")
        property(
            "sonar.issue.ignore.multicriteria.openapiS1710.resourceKey",
            "src/main/java/com/jay/voyager/api/**"
        )
    }
}

//Jacoco
jacoco {
    toolVersion = "0.8.14"
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport) // run report after tests
}

tasks.jacocoTestReport {
    dependsOn(tasks.test) //when this task is ran, will run test first.
    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }
}

// separate from jacocoTestReport, this is your actual gate. Coverage is per class basis
tasks.jacocoTestCoverageVerification {
    dependsOn(tasks.jacocoTestReport)

    violationRules {
        rule {
            element = "CLASS"

            includes = listOf(
                "com.jay.voyager.app.*",
                "com.jay.voyager.infra.*",
                "com.jay.voyager.web.*",
                "com.jay.voyager.core.context.*"
            )

            excludes = listOf(
                //exclude any smoke test related package path.
                "com.jay.voyager.*.smoke.*",
                "com.jay.voyager.*.ping.*"
            )

            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum = 0.75.toBigDecimal()
            }
        }
    }
}

tasks.named("check") {
    dependsOn("jacocoTestCoverageVerification")
    dependsOn(tasks.named("functionalTest"))
}

/* Functional (smoke) test suite
 -----------------------------
 We use Gradle's `jvm-test-suite` instead of manually creating a source set.
 Important gotchas this setup addresses:

 1) This is an *application*, not a published library.
    - jar is disabled and only bootJar is produced.
    - Because of that, implementation(project()) DOES NOT reliably put
      main classes on the functional test classpath.
    - We must depend directly on sourceSets.main.output.

 2) testImplementation / testRuntimeOnly are NOT resolvable configurations.
    - They cannot be "used" as dependencies.
    - Instead, the functional test configurations must EXTEND them so they
      inherit the same deps (JUnit, Mockito, Spring Boot test support, etc).

 3) Spring Boot context bootstraps the *real application*.
    - Functional tests must see all main classes + runtime deps.
    - Missing this causes ClassNotFound / NoClassDefFound errors in CI.

 This wiring ensures functional tests behave like "run the app and hit it",
 without coupling tests to internal packaging or published artifacts.
 */
testing {
    suites {
        val functionalTest by registering(JvmTestSuite::class) {
            useJUnitJupiter()

            dependencies {
                // Make main application classes visible to functional tests
                implementation(sourceSets.main.get().output)

                // HTTP client for smoke tests (RestClient-based)
                implementation("org.springframework.boot:spring-boot-resttestclient")

                // Mock downstream HTTP services
                implementation(platform("com.squareup.okhttp3:okhttp-bom:5.2.1"))
                implementation("com.squareup.okhttp3:mockwebserver")
            }

            targets.all {
                testTask.configure {
                    // Use smoke profile to limit beans and external integrations
                    systemProperty("spring.profiles.active", "smoke")

                    // Ensure functional tests run after unit tests when running check
                    shouldRunAfter(tasks.test)
                }
            }
        }
    }
}

/* Functional test configurations
 ------------------------------
 These configs inherit from the standard test configs so functional tests
 automatically get:
 - spring-boot-starter-test
 - junit-jupiter
 - mockito
 - test runtime support

 We EXTEND instead of "depending on" because testImplementation/testRuntimeOnly
 are not resolvable by design.
*/
configurations {
    named("functionalTestImplementation") {
        extendsFrom(configurations.testImplementation.get())
    }
    named("functionalTestRuntimeOnly") {
        extendsFrom(configurations.testRuntimeOnly.get())
    }
}