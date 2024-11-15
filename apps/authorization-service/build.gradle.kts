import java.io.ByteArrayOutputStream
import kotlin.random.Random

plugins {
	java
	id("org.springframework.boot") version "3.3.5"
	id("io.spring.dependency-management") version "1.1.6"

	id("maven-publish")
	kotlin("jvm") version "1.8.21" // adjust the Kotlin version as necessary
	id("com.google.cloud.tools.jib") version "3.4.0"
}

group = "com.infra"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	// Spring
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-validation")

	// DB
	runtimeOnly("org.postgresql:postgresql")
	implementation("org.liquibase:liquibase-core")

	// FIREBASE
	implementation("com.google.firebase:firebase-admin:9.4.1")

	// TEST
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.security:spring-security-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")

	// LIBRARIES
//	implementation("io.jsonwebtoken:jjwt:0.12.6")
	implementation("io.jsonwebtoken:jjwt-api:0.12.6")
	implementation("io.jsonwebtoken:jjwt-impl:0.12.6")
	implementation("io.jsonwebtoken:jjwt-jackson:0.12.6")
	compileOnly("org.projectlombok:lombok:1.18.34")
	annotationProcessor("org.projectlombok:lombok:1.18.34")
	implementation("org.mapstruct:mapstruct:1.5.5.Final")
	annotationProcessor("org.mapstruct:mapstruct-processor:1.5.5.Final")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

val getGitHash = { ->
	val stdout = ByteArrayOutputStream()
	exec {
		commandLine("git", "rev-parse", "--short", "HEAD" )
		standardOutput = stdout
	}
	stdout.toString().trim()
}

fun generateRandomString(length: Int = 7): String {
	val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
	return (1..length)
			.map { allowedChars.random() }
			.joinToString("")
}

jib {
	from {
		image = "openjdk:17-slim"
	}
	to {
		image = "${findProperty("DOCKER_REPOSITORY")}/authorization-service-3"
		tags = setOf(generateRandomString(7))
		auth {
			username = findProperty("DOCKER_USERNAME") as String?
			password = findProperty("DOCKER_PASSWORD") as String?
		}
	}
}