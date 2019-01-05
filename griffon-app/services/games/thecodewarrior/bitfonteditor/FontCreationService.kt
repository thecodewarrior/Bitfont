package games.thecodewarrior.bitfonteditor

import games.thecodewarrior.bitfont.file.BitfontBundle
import games.thecodewarrior.bitfont.file.FontFile
import games.thecodewarrior.bitfont.file.UCDFile
import games.thecodewarrior.bitfonteditor.ucd.UnicodeCharacterDatabase
import griffon.core.artifact.GriffonService
import griffon.metadata.ArtifactProviderFor
import org.codehaus.griffon.runtime.core.artifact.AbstractGriffonService
import java.io.BufferedInputStream
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import javax.inject.Singleton
import java.io.FileOutputStream
import java.nio.channels.Channels
import java.nio.channels.ReadableByteChannel
import java.nio.file.FileSystems
import java.nio.file.Paths
import java.util.zip.ZipOutputStream

@Singleton
@ArtifactProviderFor(GriffonService::class)
class FontCreationService: AbstractGriffonService() {
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

    fun create(): BitfontBundle {
        val bundle = BitfontBundle()
        FileSystems.newFileSystem(ucdPath, null).use { fs ->
            val database = UnicodeCharacterDatabase(fs.getPath("/"))

            bundle.ucd.blocks = database.blocks.map { (range, name) ->
                UCDFile.Block(
                    name.toLowerCase().replace("[^a-zA-Z]".toRegex(), "_"),
                    name,
                    range.start,
                    range.endInclusive
                )
            }.toMutableList()

            bundle.font.blocks = bundle.ucd.blocks.map {
                FontFile.Block(
                    it.filename,
                    it.min,
                    it.max
                )
            }.toMutableList()

            bundle.ucd.codepoints = database.codepoints.map { (codepoint, info) ->
                UCDFile.Codepoint(
                    codepoint,
                    info.name
                )
            }.toMutableList()
        }
        bundle.font.name = "Bitfont test"
        bundle.font.license = "MIT"
        bundle.font.ascender = 6u
        bundle.font.descender = 2u
        bundle.font.capHeight = 5u
        bundle.font.xHeight = 4u
        bundle.font.lineHeight = 9u

        val path = Paths.get("/Users/code/Documents/Bitfont/run/bundle")
        bundle.save(path, false)
        return bundle
    }
}
