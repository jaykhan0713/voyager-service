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

val codeArtifactEndpoint = System.getenv("CODEARTIFACT_MAVEN_ENDPOINT")
val codeArtifactToken = System.getenv("CODEARTIFACT_AUTH_TOKEN")

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            //artifactId = "voyager-openapi-dtos" > this is set by settings.gradle.kts
        }
    }

    // Only add remote repo if env vars are present (CI)
    if (!codeArtifactEndpoint.isNullOrBlank() && !codeArtifactToken.isNullOrBlank()) {
        repositories {
            maven {
                url = uri(codeArtifactEndpoint)
                credentials {
                    username = "aws"
                    password = codeArtifactToken
                }
            }
        }
    }
}

// fail loudly only when publishing is actually invoked
tasks.withType<PublishToMavenRepository>().configureEach {
    doFirst {
        if (codeArtifactEndpoint.isNullOrBlank() || codeArtifactToken.isNullOrBlank()) {
            error("Publishing requires CODEARTIFACT_MAVEN_ENDPOINT and CODEARTIFACT_AUTH_TOKEN")
        }
    }
}

//reproducible builds: normalize timstamps and entry order
tasks.withType<Jar>().configureEach {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}