package br.com.condo.manager.arch.service;

import br.com.condo.manager.arch.model.dto.FileMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Service
public class FileDAO {

    protected Logger LOGGER = LoggerFactory.getLogger(FileDAO.class);

    @Value("${app.file.storage.root}")
    private String fileStorageRoot;

    /**
     * <p>Informa o endereço físico em disco do diretório raiz de armazenamento de de arquivos.</p>
     *
     * @return o endereço configurado como diretório raiz de armazenamento de arquivos
     */
    public Path getFileStorageRootPath() {
        return Paths.get(fileStorageRoot);
    }

    /**
     * <p>Armazena um arquivo temporário de upload para um determinado local em disco, renomeando-o para um novo nome,
     * caso informado. Se um novo nome não for fornecido, o nome original é mantido. É obrigatório informar um arquivo e
     * um endereço de diretório de destino - este endereço é validado e, caso ainda não exista, um novo diretório é
     * criado no processo.</p>
     *
     * @param multipartFile instância do arquivo a ser movido para o novo local
     * @param destinationDirectory o novo local para onde o arquivo será movido
     * @param newFileName o novo nome para o arquivo
     * @return instância de {@Link FileMetadata}, com as informações do arquivo recém armazenado
     */
    protected FileMetadata storeMultipartFile(MultipartFile multipartFile, String destinationDirectory, String newFileName) {
        if(multipartFile == null || multipartFile.isEmpty()) throw new IllegalArgumentException("A file must be specified");
        if(destinationDirectory == null || destinationDirectory.trim().isEmpty()) throw new IllegalArgumentException("A destination directory must be specified");
        if(newFileName == null || newFileName.trim().isEmpty()) newFileName = multipartFile.getOriginalFilename();

        try {
            Path directoryPath = Paths.get(destinationDirectory);
            createDirectoryIfNotExists(directoryPath);
            Path filePath = directoryPath.resolve(newFileName);
            multipartFile.transferTo(filePath.toFile());

            return new FileMetadata(multipartFile, filePath, newFileName);
        } catch(Exception e) {
            throw new RuntimeException("There was an error while handling the storage process of file \"" + multipartFile.getOriginalFilename() + "\" to destination: + \"" + destinationDirectory.toString() + "\" " + getSimpleExceptionMessage(e));
        }
    }

    /**
     * <p>Armazena um arquivo temporário de upload para um determinado local em disco. É obrigatório informar um arquivo
     * e um endereço de diretório de destino - este endereço é validado e, caso ainda não exista, um novo diretório é
     * criado no processo.</p>
     *
     * @param multipartFile instância do arquivo a ser movido para o novo local
     * @param destinationDirectory o novo local para onde o arquivo será movido
     * @return instância de {@Link FileMetadata}, com as informações do arquivo recém armazenado
     */
    protected FileMetadata storeMultipartFile(MultipartFile multipartFile, String destinationDirectory) {
        return storeMultipartFile(multipartFile, destinationDirectory, null);
    }

    /**
     * <p>Entrega o arquivo de um determinado caminho. O método espera que o caminho aponte para um arquivo válido, que
     * não seja um arquivo oculto no sistema, e que também não seja um diretório.</p>
     *
     * @param filePath o caminho para o arquivo que será adquirido
     * @return intância representando o arquivo
     */
    protected File getFile(Path filePath) {
        if(filePath == null || filePath.toString().trim().isEmpty()) throw new IllegalArgumentException("A file path must be specified");
        if(!fileExists(filePath)) throw new IllegalArgumentException("The file path must point to a valid file on disk and can not be hidden");
        return filePath.toFile();
    }

    /**
     * <p>Entrega o arquivo de um determinado caminho. O método espera que o caminho aponte para um arquivo válido, que
     * não seja um arquivo oculto no sistema, e que também não seja um diretório.</p>
     *
     * @param filePath o caminho absoluto para o arquivo que será adquirido
     * @return intância representando o arquivo
     *
     * @see #getFile(Path)
     */
    protected java.io.File getFile(String filePath) {
        return getFile(filePath != null ? Paths.get(filePath) : null);
    }

    /**
     * <p>Entrega o array de bytes do arquivo de um determinado caminho. O método adquire o arquivo com o método
     * {@link #getFile(String)}, retornando os bytes do arquivo encontrado.</p>
     *
     * @param filePath o caminho para o arquivo cujos bytes serão adquiridos
     * @return o array de bytes do arquivo
     *
     * @see #getFile(Path)
     */
    protected byte[] getFileByteArray(Path filePath) {
        java.io.File file = getFile(filePath);
        try {
            return Files.readAllBytes(file.toPath());
        } catch (Exception e) {
            throw new RuntimeException("There was an error while reading bytes from file \"" + filePath + "\" " + getSimpleExceptionMessage(e));
        }
    }

    /**
     * <p>Entrega o array de bytes do arquivo de um determinado caminho. O método adquire o arquivo com o método
     * {@link #getFile(String)}, retornando os bytes do arquivo encontrado.</p>
     *
     * @param filePath o caminho absoluto para o arquivo cujos bytes serão adquiridos
     * @return o array de bytes do arquivo
     *
     * @see #getFileByteArray(Path)
     * @see #getFile(Path)
     */
    protected byte[] getFileByteArray(String filePath) {
        return getFileByteArray(filePath != null ? Paths.get(filePath): null);
    }

    /**
     * MÉTODO DE TESTE...
     *
     * @deprecated é só um teste...
     */
    @Deprecated
    protected void unzipTest(MultipartFile file, String destinationDirectory) throws IOException {
        Path directoryPath = Paths.get(destinationDirectory).resolve("temp_" + UUID.randomUUID().toString());
        createDirectoryIfNotExists(directoryPath);
        java.io.File tempFile = new java.io.File(directoryPath.resolve(file.getOriginalFilename()).toString());
        file.transferTo(tempFile);
        unzip(tempFile.toPath(), directoryPath);
    }

    /**
     * <p>Descompacta um arquivo para o diretório de destino. O método espera que o caminho do arquivo zip aponte para
     * um arquivo válido, e que um diretório de destino seja informado: caso o diretório de destino não exista, ele será
     * criado no processo. Por fim, o método descompacta o conteúdo inteiro do arquivo zip dentro do diretório destino.</p>
     *
     * @param zipFilePath o caminho para o arquivo que será descompactado
     * @param destinationDirectoryPath o caminho até o diretório destino da descompactação
     */
    protected void unzip(Path zipFilePath, Path destinationDirectoryPath) {
        if(zipFilePath == null || zipFilePath.toString().trim().isEmpty()) throw new IllegalArgumentException("A zip file path must be specified");
        if(!fileExists(zipFilePath)) throw new IllegalArgumentException("The zip file path must point to a valid file on disk and can not be hidden");
        if (destinationDirectoryPath == null || destinationDirectoryPath.toString().trim().isEmpty()) throw new IllegalArgumentException("A destination directory must be specified");

        java.io.File file = zipFilePath.toFile();
        ZipInputStream zis;
        try {
            zis = new ZipInputStream(new FileInputStream(file));
        } catch (Exception e) {
            throw new RuntimeException("The file \"" + file.getAbsolutePath() + "\" was not found or is not a valid zip file");
        }

        createDirectoryIfNotExists(destinationDirectoryPath);
        try {
            byte[] buffer = new byte[1024];
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                Path entryPath = destinationDirectoryPath.resolve(zipEntry.getName());
                if (zipEntry.isDirectory()) {
                    createDirectoryIfNotExists(entryPath);
                } else {
                    java.io.File newFile = new java.io.File(entryPath.toString());
                    FileOutputStream fos = new FileOutputStream(newFile);
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                    fos.close();
                }
                zipEntry = zis.getNextEntry();
            }
            zis.closeEntry();
            zis.close();
        } catch (Exception e) {
            throw new RuntimeException("There was an error while handling the unzip process of file \"" + file.getAbsolutePath() + "\" to destination: + \"" + destinationDirectoryPath + "\" " + getSimpleExceptionMessage(e));
        }
    }

    /**
     * <p>Descompacta um arquivo para o diretório de destino. O método espera que o caminho do arquivo zip aponte para
     * um arquivo válido, e que um diretório de destino seja informado: caso o diretório de destino não exista, ele será
     * criado no processo. Por fim, o método descompacta o conteúdo inteiro do arquivo zip dentro do diretório destino.</p>
     *
     * @param zipFilePath o caminho absoluto para o arquivo que será descompactado
     * @param destinationDirectoryPath o caminho absoluto até o diretório destino da descompactação
     *
     * @see #unzip(Path, Path)
     */
    protected void unzip(String zipFilePath, String destinationDirectoryPath) {
        unzip(zipFilePath != null ? Paths.get(zipFilePath) : null, destinationDirectoryPath != null ? Paths.get(destinationDirectoryPath) : null);
    }

    /**
     * <p>Compacta o conteúdo alvo indicado e armazena em um arquivo zip no caminho informado, entregando o zip
     * resultante dessa compactação. O método espera que o conteúdo alvo informado aponte para um arquivo ou diretório
     * válido; no caso de ser um diretório, seu conteúdo completo será compactado recursivamente.</p>
     *
     * <p>Caso o caminho para o arquivo zip de destino não seja informado, é criado um zip com o mesmo nome do contúdo
     * alvo da compactação, no mesmo diretório onde o conteúdo alvo se encontra. Ex.: ao compactar o diretório
     * <code>Z:/repo/scorm/scorm_123_456</code>, será criado um zip em <code>Z:/repo/scorm/scorm_123_456.zip</code>.</p>
     *
     * @param targetContentPath o caminho até o conteúdo que será alvo da compactação
     * @param zipFilePath o caminho onde será armazenado o arquivo zip resultante da compactação
     * @return instância representando o arquivo zip resultante da compactação
     *
     * @see #zipDirectory(java.io.File, ZipOutputStream)
     * @see #zipFile(java.io.File, ZipOutputStream)
     */
    protected java.io.File getZipFile(Path targetContentPath, Path zipFilePath) {
        if(targetContentPath == null || targetContentPath.toString().trim().isEmpty()) throw new IllegalArgumentException("A target content path must be specified");
        if(!fileExists(targetContentPath) && !directoryExists(targetContentPath)) throw new IllegalArgumentException("The target content path must point to a valid file or directory on disk and can not be hidden");

        try {
            if(zipFilePath == null || zipFilePath.toString().trim().isEmpty()) {
                zipFilePath = Paths.get(targetContentPath.toString() + ".zip");
            }

            java.io.File zipFile = new java.io.File(zipFilePath.toString());
            FileOutputStream fos = new FileOutputStream(zipFile);
            ZipOutputStream zos = new ZipOutputStream(fos);

            java.io.File contentToZip = targetContentPath.toFile();
            if(contentToZip.isDirectory())
                zipDirectory(contentToZip, zos);
            else
                zipFile(contentToZip, zos);

            zos.close();
            fos.close();

            return zipFile;
        } catch (Exception e) {
            throw new RuntimeException("There was an error while handling the zip process of path \"" + targetContentPath + "\" " + getSimpleExceptionMessage(e));
        }
    }

    /**
     * <p>Compacta o conteúdo alvo indicado e armazena em um arquivo zip no caminho informado, entregando o zip
     * resultante dessa compactação. O método espera que o conteúdo alvo informado aponte para um arquivo ou diretório
     * válido; no caso de ser um diretório, seu conteúdo completo será compactado recursivamente.</p>
     *
     * <p>Caso o caminho para o arquivo zip de destino não seja informado, é criado um zip com o mesmo nome do contúdo
     * alvo da compactação, no mesmo diretório onde o conteúdo alvo se encontra. Ex.: ao compactar o diretório
     * <code>Z:/repo/scorm/scorm_123_456</code>, será criado um zip em <code>Z:/repo/scorm/scorm_123_456.zip</code>.</p>
     *
     * @param targetContentPath o caminho absoluto até o conteúdo que será alvo da compactação
     * @param zipFilePath o caminho absoluto onde será armazenado o arquivo zip resultante da compactação
     * @return instância representando o arquivo zip resultante da compactação
     *
     * @see #getZipFile(Path, Path)
     * @see #zipDirectory(java.io.File, ZipOutputStream)
     * @see #zipFile(java.io.File, ZipOutputStream)
     */
    protected java.io.File getZipFile(String targetContentPath, String zipFilePath) {
        return getZipFile(targetContentPath != null ? Paths.get(targetContentPath) : null, zipFilePath != null ? Paths.get(zipFilePath) : null);
    }

    /**
     * <p>Compacta o conteúdo alvo indicado e entrega o array de bytes do zip resultante dessa compactação, sem nunca
     * armazená-lo em disco. O método espera que o conteúdo alvo informado aponte para um arquivo ou diretório válido;
     * no caso de ser um diretório, seu conteúdo completo será compactado recursivamente.</p>
     *
     * @param targetContentPath o caminho até o conteúdo que será alvo da compactação
     * @return o array de bytes do arquivo zip resultante da compactação
     *
     * @see #zipDirectory(java.io.File, ZipOutputStream)
     * @see #zipFile(java.io.File, ZipOutputStream)
     */
    protected byte[] getZipFileByteArray(Path targetContentPath) {
        if(targetContentPath == null || targetContentPath.toString().trim().isEmpty()) throw new IllegalArgumentException("A target content path must be specified");
        if(!fileExists(targetContentPath) && !directoryExists(targetContentPath)) throw new IllegalArgumentException("The target content path must point to a valid file or directory on disk and can not be hidden");

        try {
            java.io.File fileToZip = targetContentPath.toFile();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ZipOutputStream zos = new ZipOutputStream(baos);

            if(fileToZip.isDirectory())
                zipDirectory(fileToZip, zos);
            else
                zipFile(fileToZip, zos);

            zos.close();
            baos.close();

            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("There was an error while handling the zip process of path \"" + targetContentPath + "\" " + getSimpleExceptionMessage(e));
        }
    }

    /**
     * <p>Compacta o conteúdo alvo indicado e entrega o array de bytes do zip resultante dessa compactação, sem nunca
     * armazená-lo em disco. O método espera que o conteúdo alvo informado aponte para um arquivo ou diretório válido;
     * no caso de ser um diretório, seu conteúdo completo será compactado recursivamente.</p>
     *
     * @param targetContentPath o caminho até o conteúdo que será alvo da compactação
     * @return o array de bytes do arquivo zip resultante da compactação
     *
     * @see #getZipFileByteArray(Path)
     * @see #zipDirectory(java.io.File, ZipOutputStream)
     * @see #zipFile(java.io.File, ZipOutputStream)
     */
    protected byte[] getZipFileByteArray(String targetContentPath) {
        return getZipFileByteArray(targetContentPath != null ? Paths.get(targetContentPath) : null);
    }

    /**
     * <p>Informa se um determinado arquivo existe no disco. Para ser considerado existente, o caminho informado deve
     * apontar para um arquivo válido, não ser um diretório, e nem ser um arquivo oculto do sistema operacional.</p>
     *
     * @param filePath o caminho do arquivo a ser verificado
     * @return <code>true</code> caso o arquivo exista, <code>false</code> do contrário
     */
    protected boolean fileExists(Path filePath) {
        if(filePath == null) return false;
        try {
            return Files.exists(filePath) && !Files.isDirectory(filePath) && !Files.isHidden(filePath);
        } catch (IOException e) {
            throw new RuntimeException("The existence of the file \"" + filePath + "\" could not be determined " + getSimpleExceptionMessage(e));
        }
    }

    /**
     * <p>Informa se um determinado arquivo existe no disco. Para ser considerado existente, o caminho informado deve
     * apontar para um arquivo válido, não ser um diretório, e nem ser um arquivo oculto do sistema operacional.</p>
     *
     * @param filePath o caminho absoluto do arquivo a ser verificado
     * @return <code>true</code> caso o arquivo exista, <code>false</code> do contrário
     */
    protected boolean fileExists(String filePath) {
        return fileExists(filePath != null ? Paths.get(filePath) : null);
    }

    /**
     * <p>Remove um arquivo do disco. O método verifica a existência do arquivo em questão e o remove, lançando uma
     * exceção em caso de problemas.</p>
     *
     * @param filePath caminho para o arquivo que será removido
     */
    protected void deleteFile(Path filePath) {
        if(filePath == null || filePath.toString().trim().isEmpty())
            throw new IllegalArgumentException("A file path must be specified");
        if(!fileExists(filePath))
            throw new IllegalArgumentException("The file path must point to a valid file on disk and can not be hidden");

        try {
            FileSystemUtils.deleteRecursively(filePath);
        } catch (Exception e) {
            throw new RuntimeException("There was an error on deleting the file \"" + filePath + "\" " + getSimpleExceptionMessage(e));
        }
    }

    /**
     * <p>Remove um arquivo do disco. O método verifica a existência do arquivo em questão e o remove, lançando uma
     * exceção em caso de problemas.</p>
     *
     * @param filePath caminho para o arquivo que será removido
     */
    protected void deleteFile(String filePath) {
        deleteFile(filePath != null ? Paths.get(filePath) : null);
    }

    /**
     * <p>Informa se um determinado diretório existe no disco. Para ser considerado existente, o caminho informado deve
     * apontar para um diretório válido, não ser um arquivo, e nem ser um diretório oculto do sistema operacional.</p>
     *
     * @param directoryPath o caminho do diretório a ser verificado
     * @return <code>true</code> caso o diretório exista, <code>false</code> do contrário
     */
    protected boolean directoryExists(Path directoryPath) {
        if(directoryPath == null) return false;
        try {
            return Files.exists(directoryPath) && Files.isDirectory(directoryPath) && !Files.isHidden(directoryPath);
        } catch (IOException e) {
            throw new RuntimeException("The existence of the directory \"" + directoryPath + "\" could not be determined " + getSimpleExceptionMessage(e));
        }
    }

    /**
     * <p>Informa se um determinado diretório existe no disco. Para ser considerado existente, o caminho informado deve
     * apontar para um diretório válido, não ser um arquivo, e nem ser um diretório oculto do sistema operacional.</p>
     *
     * @param directoryPath o caminho absoluto do diretório a ser verificado
     * @return <code>true</code> caso o diretório exista, <code>false</code> do contrário
     */
    protected boolean directoryExists(String directoryPath) {
        return directoryExists(directoryPath != null ? Paths.get(directoryPath) : null);
    }

    /**
     * <p>Cria o diretório no caminho informado, caso ainda não exista. O método vailda a existência de um diretório no
     * caminho especificado, abandonando o processo caso encontre um lá, mesmo que seja um diretório oculto do sistema.
     * Do contrário (caso não exista o diretóri ainda), o método cria toda a estrutura de diretórios no caminho em
     * questão, se necessário, até o diretório final, lançando uma exceção, em caso de problemas.</p>
     *
     * @param directoryPath o caminho do diretório a ser criado
     */
    protected void createDirectoryIfNotExists(Path directoryPath) {
        if(!Files.exists(directoryPath) || !Files.isDirectory(directoryPath)) {
            try {
                Files.createDirectories(directoryPath);
            } catch (IOException e) {
                throw new RuntimeException("The directory \"" + directoryPath + "\" could not be created " + getSimpleExceptionMessage(e));
            }
        }
    }

    /**
     * <p>Informa se um diretório está vazio. O método valida a existência do ditretório e determina se ele está vazio
     * ou não: para se considerado vazio, o diretório não pode ter nenhum arquivo dentro dele - arquivos ocultos contam
     * como arquivos válidos.</p>
     *
     * @param directoryPath o caminho do diretório a ser verificado
     * @return <code>true</code> caso o diretório esteja vazio, <code>false</code> do contrário
     *
     * @see #directoryExists(Path)
     */
    protected boolean directoryIsEmpty(Path directoryPath) {
        if(directoryPath == null || directoryPath.toString().trim().isEmpty())
            throw new IllegalArgumentException("A directory path must be specified");
        if(!directoryExists(directoryPath))
            throw new IllegalArgumentException("The directory path must point to a valid directory on disk and can not be hidden");

        try {
            return Files.list(directoryPath).findAny().isPresent();
        } catch (IOException e) {
            throw new RuntimeException("The presence of any content inside the directory \"" + directoryPath + "\" could not be determined " + getSimpleExceptionMessage(e));
        }
    }

    /**
     * <p>Remove um diretório do disco junto com o seu conteúdo. O método verifica a existência do diretório em questão
     * e o remove, removendo também o seu conteúdo recursivamente, se necessário. O método lança uma exceção em caso de
     * problemas.</p>
     *
     * @param directoryPath caminho para o arquivo que será removido
     */
    protected void deleteDirectory(Path directoryPath) {
        if(directoryPath == null || directoryPath.toString().trim().isEmpty())
            throw new IllegalArgumentException("A directory path must be specified");
        if(!directoryExists(directoryPath))
            throw new IllegalArgumentException("The directory path must point to a valid directory on disk and can not be hidden");

        try {
            FileSystemUtils.deleteRecursively(directoryPath);
        } catch (Exception e) {
            throw new RuntimeException("There was an error on deleting the directory \"" + directoryPath + "\" " + getSimpleExceptionMessage(e));
        }
    }

    /**
     * <p>Remove um diretório do disco junto com o seu conteúdo. O método verifica a existência do diretório em questão
     * e o remove, removendo também o seu conteúdo recursivamente, se necessário. O método lança uma exceção em caso de
     * problemas.</p>
     *
     * @param directoryPath caminho para o arquivo que será removido
     */
    protected void deleteDirectory(String directoryPath) {
        deleteDirectory(directoryPath != null ? Paths.get(directoryPath) : null);
    }

    private void zipDirectory(java.io.File directory, Path pathInsideZip, ZipOutputStream zos) {
        if(directory == null || !directory.exists() || !directory.isDirectory()) throw new IllegalArgumentException("Directory must be specified and should point to a valid directory");
        if(zos == null) throw new IllegalArgumentException("A valid zip output stream must be provided");

        try {
            if (!directory.isHidden()) {
                java.io.File[] children = directory.listFiles();
                for (java.io.File child : children) {
                    Path childPathInsideZip = pathInsideZip != null ? pathInsideZip.resolve(child.getName()) : Paths.get(child.getName());
                    if (child.isDirectory())
                        zipDirectory(child, childPathInsideZip, zos);
                    else
                        zipFile(child, childPathInsideZip, zos);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("There was an error while handling the zip process of directory \"" + directory.getPath() + "\" at zip path \"" + pathInsideZip + "\" " + getSimpleExceptionMessage(e));
        }
    }

    private void zipDirectory(java.io.File directory, ZipOutputStream zos) {
        zipDirectory(directory, null, zos);
    }

    private void zipFile(java.io.File file, Path pathInsideZip, ZipOutputStream zos) {
        if(file == null || !file.exists() || file.isDirectory()) throw new IllegalArgumentException("File must be specified and can not be a directory");
        if(zos == null) throw new IllegalArgumentException("A valid zip output stream must be provided");

        try {
            if (!file.isHidden()) {
                ZipEntry zipEntry = new ZipEntry(pathInsideZip.toString());
                zos.putNextEntry(zipEntry);
                FileInputStream fis = new FileInputStream(file);
                byte[] bytes = new byte[1024];
                int length;
                while ((length = fis.read(bytes)) >= 0) {
                    zos.write(bytes, 0, length);
                }
                fis.close();
            }
        } catch (Exception e) {
            throw new RuntimeException("There was an error while handling the zip process of file \"" + file.getPath() + "\" at zip path \"" + pathInsideZip + "\" " + getSimpleExceptionMessage(e));
        }
    }

    private void zipFile(java.io.File file, ZipOutputStream zos) {
        zipFile(file, Paths.get(file.getName()), zos);
    }

    protected String getSimpleExceptionMessage(Exception e) {
        return "[\"" + e.toString() + "\" at \"" + e.getStackTrace()[0].toString() + "\"]";
    }
}
