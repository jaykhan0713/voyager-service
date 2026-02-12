rootProject.name = "voyager-service"

val profile = System.getenv("SPRING_PROFILES_ACTIVE")

val useLocalDtos = profile.isNullOrBlank() || profile == "dev" || profile == "smoke"

//For local project- use local
if (useLocalDtos) {
    includeBuild("openapi-dtos")
}