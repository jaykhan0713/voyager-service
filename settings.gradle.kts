rootProject.name = "voyager-service"

val profile = System.getenv("SPRING_PROFILES_ACTIVE")

val useLocalDtos = profile.isNullOrBlank() || profile == "dev" || profile == "smoke"

if (useLocalDtos) {
    includeBuild("openapi-dtos")
}