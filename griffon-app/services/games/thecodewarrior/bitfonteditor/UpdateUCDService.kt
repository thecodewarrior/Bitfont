package games.thecodewarrior.bitfonteditor

import games.thecodewarrior.bitfont.file.UCDFile
import games.thecodewarrior.bitfonteditor.ucd.UnicodeCharacterDatabase
import griffon.core.artifact.GriffonService
import griffon.metadata.ArtifactProviderFor
import org.codehaus.griffon.runtime.core.artifact.AbstractGriffonService
import java.io.FileOutputStream
import java.net.URL
import java.nio.channels.Channels
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import javax.inject.Singleton

@Singleton
@ArtifactProviderFor(GriffonService::class)
class UpdateUCDService: AbstractGriffonService() {
    // only download once per session
    private val ucdPath: Path by lazy {
        val path = Files.createTempDirectory("UnicodeCharacterDatabase")
        val zipPath = path.resolve("UCD.zip")

        val website = URL("http://www.unicode.org/Public/UCD/latest/ucd/UCD.zip")
        val rbc = Channels.newChannel(website.openStream())
        val fos = FileOutputStream(zipPath.toFile())
        fos.channel.transferFrom(rbc, 0, java.lang.Long.MAX_VALUE)

        return@lazy zipPath
    }

    fun updateUCD(file: UCDFile) {
        FileSystems.newFileSystem(ucdPath, null).use { fs ->
            val database = UnicodeCharacterDatabase(fs.getPath("/"))

            file.blocks = database.blocks.map { (range, name) ->
                UCDFile.Block(
                    name.toLowerCase().replace("[^a-zA-Z]".toRegex(), "_"),
                    name,
                    range.start,
                    range.endInclusive
                )
            }.toMutableList()

            file.codepoints = database.codepoints.map { (codepoint, info) ->
                UCDFile.Codepoint(
                    codepoint,
                    info.name
                )
            }.toMutableList()
        }
    }
}