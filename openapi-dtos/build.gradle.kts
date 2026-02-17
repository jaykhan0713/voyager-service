plugins {
    `java-library`
    `maven-publish`
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

group = "com.jay.voyager"
version = "0.0.1-SNAPSHOT" //Source of truth for this lib's version. Bump when contract changes
description = "Open API DTOs to be published"

repositories {
    mavenCentral()
}

dependencies {
    api("io.swagger.core.v3:swagger-annotations-jakarta:2.2.38")
}

fun propOrEnv(propName: String, envName: String): String? {
    val p = providers.gradleProperty(propName).orNull
    if (!p.isNullOrBlank()) return p
    val e = System.getenv(envName)
    return if (e.isNullOrBlank()) null else e
}

val codeartifactEndpoint = propOrEnv("codeartifactEndpoint", "CODEARTIFACT_ENDPOINT")
val codeartifactAuthToken = propOrEnv("codeartifactAuthToken", "CODEARTIFACT_AUTH_TOKEN")

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifactId = "voyager-openapi-dtos"
        }
    }

    // Only add remote repo if env vars are present (CI)
    if (!codeartifactEndpoint.isNullOrBlank() && !codeartifactAuthToken.isNullOrBlank()) {
        repositories {
            maven {
                url = uri(codeartifactEndpoint)
                credentials {
                    username = "aws"
                    password = codeartifactAuthToken
                }
            }
        }
    }
}

// fail loudly only when publishing is actually invoked
tasks.withType<PublishToMavenRepository>().configureEach {
    doFirst {
        if (codeartifactEndpoint.isNullOrBlank() || codeartifactAuthToken.isNullOrBlank()) {
            error("Publishing requires CODEARTIFACT_ENDPOINT and CODEARTIFACT_AUTH_TOKEN")
        }
    }
}

//reproducible builds: normalize timstamps and entry order
tasks.withType<Jar>().configureEach {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}