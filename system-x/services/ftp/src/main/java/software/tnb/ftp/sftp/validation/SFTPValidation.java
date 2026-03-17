package software.tnb.ftp.sftp.validation;

import static org.junit.jupiter.api.Assertions.fail;

import software.tnb.common.utils.IOUtils;
import software.tnb.ftp.common.FileTransferValidation;
import software.tnb.ftp.sftp.account.SFTPAccount;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import net.schmizz.sshj.sftp.RemoteResourceInfo;
import net.schmizz.sshj.sftp.SFTPClient;

public class SFTPValidation implements FileTransferValidation {

    private final SFTPClient client;
    private final SFTPAccount account;

    public SFTPValidation(SFTPAccount account, SFTPClient client) {
        this.client = client;
        this.account = account;
        System.out.println("=== SFTPValidation initialized ===");
        System.out.println("  baseDir: '" + account.baseDir() + "'");
        
    }

    @Override
    public void createFile(String fileName, String fileContent) {
        System.out.println("\n--- createFile called ---");
        System.out.println("  fileName: '" + fileName + "'");
        System.out.println("  fileContent length: " + (fileContent != null ? fileContent.length() : 0));
        Path tempFile = createTempFile();
        try {
            IOUtils.writeFile(tempFile, fileContent);
            String remotePath = getRemoteFileName(fileName);
            System.out.println("  Writing to remote path: '" + remotePath + "'");
            client.put(tempFile.toString(), getRemoteFileName(fileName));
            System.out.println("  File created successfully");
        } catch (IOException e) {
            System.err.println("  ERROR creating file: " + e.getMessage());
            e.printStackTrace();
            fail("Validation could not create file in SFTP", e);
        } finally {
            tempFile.toFile().delete();
            System.out.println("  Temp file deleted");
        }
    }

    @Override
    public String downloadFile(String fileName) {
        System.out.println("\n--- downloadFile called ---");
        System.out.println("  fileName: '" + fileName + "'");
        Path tempFile = createTempFile();
        try {
            String remotePath = getRemoteFileName(fileName);
            System.out.println("  Downloading from remote path: '" + remotePath + "'");
            client.get(getRemoteFileName(fileName), tempFile.toString());
            String content = IOUtils.readFile(tempFile);
            System.out.println("  File downloaded successfully, content length: " + content.length());
            return IOUtils.readFile(tempFile);
        } catch (IOException e) {
            System.err.println("  ERROR downloading file: " + e.getMessage());
            e.printStackTrace();
            return fail("Validation could not download file from SFTP", e);
        } finally {
            tempFile.toFile().delete();
        }
    }

    @Override
    public void createDirectory(String dirName) {
        System.out.println("\n========== CREATE DIRECTORY DEBUG ==========");
        System.out.println("  Attempting to create directory: '" + dirName + "'");
        System.out.println("  Base directory from account: '" + account.baseDir() + "'");
        try {
            // Ensure baseDir exists (create if needed)
            if (client.statExistence(account.baseDir()) == null) {
                // Try to create the baseDir, handling nested paths
                String baseDir = account.baseDir();
                String[] parts = baseDir.split("/");
                String currentPath = "";
                for (String part : parts) {
                    if (!part.isEmpty()) {
                        currentPath = currentPath.isEmpty() ? part : currentPath + "/" + part;
                        if (client.statExistence(currentPath) == null) {
                            client.mkdir(currentPath);
                        }
                    }
                }
            }

            client.mkdir(account.baseDir() + "/" + dirName);
        } catch (IOException e) {
            System.err.println("  ERROR Creating directory1: " + e.getMessage());
            e.printStackTrace();
            fail("Validation could not create directory in SFTP", e);
        }
    }

    @Override
    public Map<String, String> downloadAllFiles(String dirName) {
        return listAllFiles(dirName).stream()
            .collect(Collectors.toMap(file -> file, file -> this.downloadFile(String.format("%s/%s", dirName, file))));
    }

    @Override
    public List<String> listAllFiles(String dirName) {
        try {
            return client.ls(account.baseDir() + "/" + dirName).stream()
                .filter(RemoteResourceInfo::isRegularFile)
                .map(RemoteResourceInfo::getName)
                .collect(Collectors.toList());
        } catch (IOException e) {
            System.err.println("  ERROR Creating directory2: " + e.getMessage());
            e.printStackTrace();
            return fail("Validation could not list directory in SFTP", e);
        }
    }

    private String getRemoteFileName(String fileName) {
        // the root dir of remote is owned by root, we create a subdir owned by us during container startup, prepend that path here
        return account.baseDir() + "/" + fileName;
    }

    private Path createTempFile() {
        Path tempFile = null;
        try {
            tempFile = Files.createTempFile(null, null);
        } catch (IOException e) {
            System.err.println("  ERROR Validation could not create temp file: " + e.getMessage());
            e.printStackTrace();
            fail("Validation could not create temp file", e);
        }
        return tempFile;
    }
}
