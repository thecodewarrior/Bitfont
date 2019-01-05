package games.thecodewarrior.bitfont.file

import games.thecodewarrior.bitfont.stream.BinarySerializable
import games.thecodewarrior.bitfont.stream.ReadStream
import games.thecodewarrior.bitfont.stream.WriteStream
import java.io.File
import java.io.FileNotFoundException
import java.util.zip.ZipFile
import java.nio.file.Files.delete
import java.io.IOException
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.nio.file.Files.createDirectories
import java.nio.file.Files.notExists
import java.util.HashMap
import java.io.FileOutputStream
import java.nio.file.Files
import java.util.zip.ZipOutputStream
import kotlin.streams.asSequence

class BitfontBundle {
    var font = FontFile()
    var ucd = UCDFile()
    val blocks = mutableMapOf<String, BlockFile>()

    fun write(path: Path) {
        fun write(obj: BinarySerializable, name: String)
            = WriteStream(Files.newOutputStream(path.resolve(name)), 0u).use { obj.write(it) }
        write(font, "font.bin")
        write(ucd, "ucd.bin")
        Files.createDirectories(path.resolve("blocks"))
        blocks.forEach { (name, block) ->
            write(block, "blocks/$name.bin")
        }
    }

    fun read(path: Path) {
        fun stream(path: Path) = ReadStream(Files.newInputStream(path))
        fun stream(name: String) = stream(path.resolve(name))

        font = stream("font.bin").use { FontFile(it) }
        ucd = stream("ucd.bin").use { UCDFile(it) }
        blocks.clear()
        Files.list(path.resolve("blocks")).asSequence().associateTo(blocks) {
            val block = stream(it).use { BlockFile(it) }
            it.fileName.toString().removeSuffix(".bin") to block
        }
    }

    fun save(path: Path, archive: Boolean) {
        if(archive) {
            // create an empty zip for us to open
            ZipOutputStream(FileOutputStream(path.toFile())).close()
            // edit that zip to contain our files
            FileSystems.newFileSystem(path, null).use { fs ->
                write(fs.getPath("/"))
            }
        } else {
            Files.createDirectories(path)
            write(path)
        }
    }

    fun open(path: Path) {
        if(Files.isDirectory(path)) {
            read(path)
        } else {
            FileSystems.newFileSystem(path, null).use { fs ->
                read(fs.getPath("/"))
            }
        }
    }
}