plugins {
    id("org.jetbrains.intellij.platform") version "2.19.0"
}

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

group = providers.gradleProperty("pluginGroup").get()
version = providers.gradleProperty("pluginVersion").get()

dependencies {
    intellijPlatform {
        create(
            providers.gradleProperty("platformType"),
            providers.gradleProperty("platformVersion"),
        )
    }
}

intellijPlatform {
    pluginConfiguration {
        version = providers.gradleProperty("pluginVersion")
        ideaVersion {
            sinceBuild = providers.gradleProperty("pluginSinceBuild")
            // No untilBuild: a theme uses no APIs that can break, so pinning an
            // upper bound would only strand users on new IDE releases.
            untilBuild = provider { null }
        }
    }

    // Credentials come from ~/.gradle/gradle.properties (never from this repo),
    // falling back to the environment variables CI uses. See README > Signing
    // and publishing. Both providers stay empty until you actually run
    // signPlugin or publishPlugin, so ordinary builds need none of this.
    signing {
        certificateChainFile = layout.file(
            providers.gradleProperty("desert.signing.certificateChainFile").map(::expandHome)
        )
        privateKeyFile = layout.file(
            providers.gradleProperty("desert.signing.privateKeyFile").map(::expandHome)
        )
        password = providers.gradleProperty("desert.signing.password")
            .orElse(providers.environmentVariable("PRIVATE_KEY_PASSWORD"))
    }

    publishing {
        token = providers.gradleProperty("desert.publishing.token")
            .orElse(providers.environmentVariable("PUBLISH_TOKEN"))
    }
}

/** Lets gradle.properties paths start with `~`, which Java does not expand. */
fun expandHome(path: String): File =
    File(path.replaceFirst(Regex("^~"), System.getProperty("user.home")))

/**
 * The two themes must stay structurally identical - same editor-scheme keys,
 * same `ui` keys. A key added to one and not the other is how light and dark
 * quietly drift apart. This fails the build instead.
 */
val checkThemeParity = tasks.register("checkThemeParity") {
    group = "verification"
    description = "Checks that the dark and light themes define the same keys."

    val themesDir = layout.projectDirectory.dir("src/main/resources/themes")
    inputs.dir(themesDir)

    doLast {
        val dir = themesDir.asFile
        val optionName = Regex("""<option name="([A-Za-z0-9_]+)"""")

        // Editor schemes: same keys, same order.
        val schemeKeys = listOf("Desert.xml", "DesertLight.xml").associateWith { name ->
            val text = dir.resolve(name).readText()
            require(!text.contains(Regex("""baseAttributes\s*="""))) {
                "$name: baseAttributes does not resolve once a scheme is saved - set values explicitly"
            }
            optionName.findAll(text).map { it.groupValues[1] }.toList()
        }
        val (darkScheme, lightScheme) = schemeKeys.values.toList()
        if (darkScheme != lightScheme) {
            val onlyDark = darkScheme.toSet() - lightScheme.toSet()
            val onlyLight = lightScheme.toSet() - darkScheme.toSet()
            throw GradleException(
                "editor schemes have drifted:\n" +
                    "  only in Desert.xml: ${onlyDark.ifEmpty { "none" }}\n" +
                    "  only in DesertLight.xml: ${onlyLight.ifEmpty { "none" }}"
            )
        }

        // Theme descriptions: same `ui` keys, and every colour reference resolves.
        fun keys(node: Any?, prefix: String = ""): Set<String> = when (node) {
            is Map<*, *> -> node.entries.flatMap { (k, v) ->
                keys(v, "$prefix.$k") + "$prefix.$k"
            }.toSet()
            else -> emptySet()
        }

        val uiKeys = listOf("Desert.theme.json", "DesertLight.theme.json").associateWith { name ->
            @Suppress("UNCHECKED_CAST")
            val theme = groovy.json.JsonSlurper().parse(dir.resolve(name)) as Map<String, Any>
            @Suppress("UNCHECKED_CAST")
            val names = (theme["colors"] as Map<String, Any>).keys
            @Suppress("UNCHECKED_CAST")
            val ui = theme["ui"] as Map<String, Any>

            val unresolved = mutableListOf<String>()
            fun walk(node: Any?, path: String) {
                when (node) {
                    is Map<*, *> -> node.forEach { (k, v) -> walk(v, "$path.$k") }
                    is String -> if (!node.startsWith("#") && node !in names) {
                        unresolved += "$name$path = $node"
                    }
                }
            }
            walk(ui, "")
            if (unresolved.isNotEmpty()) {
                throw GradleException("unresolved colour references:\n  " + unresolved.joinToString("\n  "))
            }
            keys(ui)
        }
        val (darkUi, lightUi) = uiKeys.values.toList()
        if (darkUi != lightUi) {
            throw GradleException(
                "ui blocks have drifted:\n" +
                    "  only in Desert.theme.json: ${(darkUi - lightUi).ifEmpty { "none" }}\n" +
                    "  only in DesertLight.theme.json: ${(lightUi - darkUi).ifEmpty { "none" }}"
            )
        }

        logger.lifecycle("themes in step: ${darkScheme.size} scheme keys, ${darkUi.size} ui keys each")
    }
}

tasks.named("verifyPlugin") { dependsOn(checkThemeParity) }
tasks.named("buildPlugin") { dependsOn(checkThemeParity) }

// verifyPluginSignature reads signPlugin's output. Gradle 9 rejects an
// undeclared dependency between them, so running both in one invocation fails
// unless the ordering is explicit.
tasks.named("verifyPluginSignature") { dependsOn("signPlugin") }
