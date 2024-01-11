plugins {
    java
    `maven-publish`
    signing
}

repositories {
    mavenCentral()
}

group = "com.dvoraksw.ce"
version = "0.1.0"

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")
    implementation("com.sun.mail:jakarta.mail:2.0.1")
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
    withJavadocJar()
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.dvoraksw.ce"
            artifactId = "common-email"
            version = "0.1.0"
            from(components["java"])
            pom {
                name = "Common Email"
                description = "Simple email sender."
                url = "https://github.com/jandvorak1/common-email"
                licenses {
                    license {
                        name = "The Apache License, Version 2.0"
                        url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
                    }
                }
                developers {
                    developer {
                        id = "dvorak1"
                        name = "Jan Dvorak"
                        email = "jan.dvorak@dvorak-sw.com"
                    }
                }
                scm {
                    connection = "scm:git:git@github.com:jandvorak1/common-email .git"
                    developerConnection = "scm:git:ssh://github.com:jandvorak1/common-email.git"
                    url = "https://github.com/jandvorak1/common-email/tree/master"
                }
            }
        }
    }
    repositories {
        maven {
            val releasesRepoUrl = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            val snapshotsRepoUrl = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
            url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
            name = "ossrh"
            credentials(PasswordCredentials::class)
        }
    }
}

signing {
    sign(publishing.publications["maven"])
}

tasks.jar {
  manifest {
    attributes["Built-By"] = System.getProperty("user.name")
 attributes["Specification-Title"] = project.name
        attributes["Specification-Version"] = "${project.version}"
        attributes["Specification-Vendor"] = "Jan Dvorak"
        attributes["Implementation-Title"] = project.name
        attributes["Implementation-Version"] = "${project.version}"
        attributes["Implementation-Vendor"] = "Jan Dvorak"
    attributes["Created-By"] = "Gradle ${project.gradle.gradleVersion}"
    attributes["Build-Jdk"] = "${System.getProperty("java.version")} (${System.getProperty("java.vendor").replace("N/A", "Arch Linux")} ${System.getProperty("java.vm.version")})"
    attributes["Build-OS"] = "${System.getProperty("os.name")} ${System.getProperty("os.arch")} ${System.getProperty("os.version")}"
  }
  exclude("**/*.xcf")
}

tasks.test {
  useJUnitPlatform()
  jvmArgs("-Xshare:off")
  testLogging {
    events("PASSED", "SKIPPED", "FAILED", "STANDARD_OUT", "STANDARD_ERROR")
  }
}

tasks.compileJava {
  options.encoding = "UTF-8"  
}

tasks.compileTestJava {
  options.encoding = "UTF-8"
}

tasks.javadoc {
    if (JavaVersion.current().isJava9Compatible) {
        (options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
    }
}
