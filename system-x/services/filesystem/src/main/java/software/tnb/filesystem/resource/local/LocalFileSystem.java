package software.tnb.filesystem.resource.local;

import software.tnb.common.deployment.Deployable;
import software.tnb.common.utils.IOUtils;
import software.tnb.filesystem.service.FileSystem;

import org.apache.commons.io.FileUtils;

import com.google.auto.service.AutoService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@AutoService(FileSystem.class)
public class LocalFileSystem extends FileSystem implements Deployable {
    @Override
    public void setAppName(String app) {
    }

    @Override
    public String getFileContent(Path path) {
        return IOUtils.readFile(path);
    }

    @Override
    public boolean createFile(Path directory, String filename, String content) throws IOException {
        Files.write(Path.of(directory.toFile().getAbsolutePath(), filename), content.getBytes());
        return true;
    }

    @Override
    public void copyFile(Path srcPath, Path destPath) throws IOException {
        FileUtils.copyFile(srcPath.toFile(), destPath.toFile());
    }

    @Override
    public Path createDirectory(Path path) throws IOException {
        return Files.createDirectories(path);
    }

    @Override
    public void deleteDirectory(Path directory) throws IOException {
        FileUtils.deleteDirectory(directory.toFile());
    }

    @Override
    public void deleteFile(Path directory, String filename) throws IOException {
        java.nio.file.Files.deleteIfExists(directory.resolve(filename));
    }

    @Override
    public Path getRoot() {
        return Paths.get("target");
    }
}
