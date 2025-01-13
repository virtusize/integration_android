import org.gradle.process.ExecOperations
import java.io.File

// Directories for fonts and localization
val sourceFontDir = file("./Fonts")
val sdkFontDir = file("./virtusize/src/main/res/font")
val localizationDir = file("./virtusize/src/main/res")

// Access ExecOperations
interface InjectedExecOps {
    @get:Inject
    val execOperations: ExecOperations
}

// Function to rename font file and its metadata
fun renameFont(
    execOps: ExecOperations,
    fontDir: File,
    font: File,
) {
    val fontName = font.nameWithoutExtension
    val newName = "subset_$fontName"
    val ttxFile = File(fontDir, "$fontName.ttx")

    // Step 1: Extract TTX XML
    execOps.exec {
        commandLine("ttx", font.absolutePath)
    }

    // Step 2: Rename font inside the TTX file
    val updatedTtxContent = ttxFile.readText().replace(fontName, newName)
    ttxFile.writeText(updatedTtxContent)

    // Step 3: Regenerate font
    execOps.exec {
        commandLine("ttx", "-o", "${fontDir.path}/$newName.ttf", ttxFile.absolutePath)
    }

    // Step 4: Cleanup
    ttxFile.delete()
    font.delete()

    println("-- Font renamed: $newName")
}

// Function to subset font
fun generateSubsetFont(
    execOps: ExecOperations,
    font: String,
    language: String,
) {
    val fontPath = File(sourceFontDir, font).absolutePath
    val outputPath = File(sdkFontDir, font).absolutePath
    val localizationFile = File(localizationDir, "values-$language/strings.xml").absolutePath

    // Subset the font using pyftsubset
    execOps.exec {
        commandLine(
            "pyftsubset",
            fontPath,
            "--output-file=$outputPath",
            "--unicodes=U+0020-007E",
            "--text-file=$localizationFile",
        )
    }

    // Rename the subset font
    renameFont(execOps, sdkFontDir, File(sdkFontDir, font))
}

// Gradle task for generating subset fonts
tasks.register("generateSubsetFonts") {
    val injected = project.objects.newInstance<InjectedExecOps>()
    val execOps = injected.execOperations

    // Japanese Regular
    generateSubsetFont(execOps, "noto_sans_jp_regular.ttf", "ja")

    // Japanese Bold
    generateSubsetFont(execOps, "noto_sans_jp_bold.ttf", "ja")

    // Korean Regular
    generateSubsetFont(execOps, "noto_sans_kr_regular.ttf", "ko")

    // Korean Bold
    generateSubsetFont(execOps, "noto_sans_kr_bold.ttf", "ko")
}
