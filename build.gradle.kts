import com.vanniktech.maven.publish.JavaLibrary
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.SourcesJar

plugins {
    `java-library`
    id("com.vanniktech.maven.publish") version "0.37.0"
}

group = project.property("group") as String
version = project.property("version") as String

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

repositories {
    mavenCentral()
}

val jacksonVersion = "2.17.2"

dependencies {
    api("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    api("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")

    compileOnly("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok:1.18.34")
    testCompileOnly("org.projectlombok:lombok:1.18.34")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.34")

    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.assertj:assertj-core:3.26.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

tasks.javadoc {
    // Javadoc runs without annotation processing, so it can't see Lombok-generated getters/setters
    // that some @link references point at. Doclint would otherwise fail the build on those.
    (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
}

mavenPublishing {
    // automaticRelease = false: stages the deployment on Central Portal but requires a manual
    // "Publish" click there before it goes live. Flip to true once the release process is trusted
    // — Central artifacts can never be deleted once published, so the first few releases warrant
    // a manual look.
    publishToMavenCentral(automaticRelease = false)
    signAllPublications()

    coordinates(group.toString(), "xingen-sdk", version.toString())

    configure(
        JavaLibrary(
            javadocJar = JavadocJar.Javadoc(),
            sourcesJar = SourcesJar.Sources(),
        )
    )

    pom {
        name.set("Xingen SDK")
        description.set(
            "Java client SDK for the Xingen e-invoice validation API - submit UBL, CII, ZUGFeRD, " +
                "and SAP IDoc/OData invoices for validation against EN16931, XRechnung, and Peppol."
        )
        inceptionYear.set("2026")
        url.set("https://github.com/nko-technologies/xingen-java-sdk")

        licenses {
            license {
                name.set("MIT License")
                url.set("https://opensource.org/licenses/MIT")
                distribution.set("https://opensource.org/licenses/MIT")
            }
        }

        developers {
            developer {
                id.set("manuelgerstner")
                name.set("Manuel Gerstner")
                email.set("manuel@nko-technologies.com")
            }
        }

        scm {
            url.set("https://github.com/nko-technologies/xingen-java-sdk")
            connection.set("scm:git:git://github.com/nko-technologies/xingen-java-sdk.git")
            developerConnection.set("scm:git:ssh://git@github.com/nko-technologies/xingen-java-sdk.git")
        }
    }
}
