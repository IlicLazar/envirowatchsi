plugins {
    kotlin("jvm") version "2.3.21"
    application
}

application {
    mainClass.set("lexer.MainKt")
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
