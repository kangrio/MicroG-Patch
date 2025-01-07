group = "app.revanced"

patches {
    about {
        name = "MicroG Patches"
        description = "Patches for support MicroG"
        source = "git@github.com:kangrio/MicroG-Patches.git"
        author = "KangRio"
        contact = "https://github.com/kangrio"
        website = "https://github.com/kangrio"
        license = "GNU General Public License v3.0"
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs = listOf("-Xcontext-receivers")
    }
}

dependencies{
    implementation("com.android.tools.build:apksig:8.5.2")
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/kangrio/MicroG-Patch")
            credentials {
                username = providers.gradleProperty("gpr.user").getOrElse(System.getenv("GITHUB_ACTOR"))
                password = providers.gradleProperty("gpr.key").getOrElse(System.getenv("GITHUB_TOKEN"))
            }
        }
    }
}
