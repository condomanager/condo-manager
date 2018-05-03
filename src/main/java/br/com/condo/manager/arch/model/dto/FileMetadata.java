package br.com.condo.manager.arch.model.dto;

import lombok.Data;
import org.springframework.lang.Nullable;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.Date;

@Data
public class FileMetadata {

    /**
     * <p>Data em que o arquivo foi criado na API</p>
     */
    private Date creationDate;

    /**
     * <p>O caminho em disco onde o arquivo foi armazenado.</p>
     */
    private Path storagePath;

    /**
     * <p>O nome original do arquivo enviado</p>
     */
    private String originalFileName;

    /**
     * <p>O nome final com o qual o arquivo ficou armazenado em disco</p>
     */
    private String finalFileName;

    /**
     * <p>A extensão do arquivo</p>
     */
    private String extension;

    /**
     * <p>O tipo MIME (Multipurpose Internet Mail Extensions) do arquivo</p>
     */
    private String mimeType;

    /**
     * <p>O tamanho do arquivo em bytes</p>
     */
    private Long byteSize;

    public FileMetadata(Path storagePath, String originalFileName, String finalFileName, String mimeType, long byteSize) {
        this.creationDate = new Date();
        this.storagePath = storagePath;
        this.originalFileName = originalFileName;
        this.finalFileName = finalFileName;
        this.extension = getExtensionFromName(originalFileName);
        this.mimeType = mimeType;
        this.byteSize = byteSize;
    }

    public FileMetadata(MultipartFile multipartFile, Path storagePath, String finalFileName) {
        this(storagePath, multipartFile.getOriginalFilename(), finalFileName, multipartFile.getContentType(), multipartFile.getSize());
    }

    @Nullable
    public static String getExtensionFromName(String fileName) {
        if(fileName != null && !fileName.trim().isEmpty() && fileName.contains("."))
            return fileName.substring(fileName.lastIndexOf(".") + 1);
        return null;
    }
}
