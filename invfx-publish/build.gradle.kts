import com.vanniktech.maven.publish.SonatypeHost

plugins {
    id("com.vanniktech.maven.publish") version "0.28.0"
    `maven-publish`
    signing
}
publishing {
    publications {
        fun MavenPublication.setup(target: Project) {
            artifactId = target.name
            from(target.components["java"])
            artifact(target.tasks["sourcesJar"])
            artifact(target.tasks["dokkaJar"])
        }

        create<MavenPublication>("api") {
            setup(projectApi)
        }

        create<MavenPublication>("core") {
            setup(projectCore)
            artifact(coreReobfJar)
        }
    }
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()
    pom {
                name.set("invfx")
                description.set("Kotlin DSL for PaperMC Inventory GUI")
                url.set("https://github.com/monun/${rootProject.name}")

                licenses {
                    license {
                        name.set("GNU General Public License version 3")
                        url.set("https://opensource.org/licenses/GPL-3.0")
                    }
                }

                developers {
                    developer {
                        id.set("monun")
                        name.set("Monun")
                        email.set("monun1010@gmail.com")
                        url.set("https://github.com/monun")
                        roles.addAll("developer")
                        timezone.set("Asia/Seoul")
                    }

                    developer {
                        id.set("icetang0123")
                        name.set("Icetang0123")
                        email.set("1415wwwh@gmail.com")
                        url.set("https://github.com/gooddltmdqls")
                        roles.addAll("developer")
                        timezone.set("Asia/Seoul")
                    }

                    developer {
                        id.set("zeettn")
                        name.set("Zeettn")
                        email.set("devleooh@gmail.com")
                        url.set("https://github.com/zeettn")
                        roles.addAll("developer")
                        timezone.set("Asia/Seoul")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/zeettn/${rootProject.name}.git")
                    developerConnection.set("scm:git:ssh://github.com:zeettn/${rootProject.name}.git")
                    url.set("https://github.com/zeettn/${rootProject.name}")
                }
            }

}



signing {
    isRequired = true
    sign(publishing.publications["api"], publishing.publications["core"])
}

