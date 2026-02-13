rootProject.name = "voyager-service"

// Default: local dev uses the included build.
// CI sets DISABLE_COMPOSITE_DTO=true to force resolving from CodeArtifact.
val disableComposite = System.getenv("DISABLE_COMPOSITE_DTO")
    ?.equals("true", ignoreCase = true) == true

if (!disableComposite) {
    includeBuild("openapi-dtos")
}