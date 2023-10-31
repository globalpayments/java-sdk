package com.global.api.entities.propay;

import com.global.api.entities.enums.DocumentCategory;
import com.global.api.entities.enums.FileType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.codec.binary.Base64;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@Getter
@Setter
public class DocumentUploadData {

    /** Name the document according to instructions provided to you by ProPay's Risk team */
    private String documentName;

    /** The transaction number of the chargeback you need to dispute */
    private String transactionReference;

    /** The file format of the Document to be uploaded. This property MUST be set if using the Document property directly, but will be set automatically if using the DocumentPath property */
    private FileType docType;

    /** Type of document */
    private static List<String> docTypeList=Arrays.asList("TIF", "TIFF", "BMP", "JPG", "JPEG", "GIF", "PNG", "DOC", "DOCX");

    /**
     * The document data in base64 format.
     * This property can be assigned to directly (the DocType property must also be provided a value) or
     *This property will be set automatically by setting the DocumentPath property
    */
    private String document;

    /**
     * The type of document you've been asked to provide by ProPay's Risk team. Valid values are:
     * Verification, FraudHolds, Underwriting, RetrievalRequest
    */
    private DocumentCategory docCategory;

    @Setter(AccessLevel.NONE)
    private String documentPath;

    public void setDocumentPath(String path) throws Exception {
        documentPath=path;
        if(path!=null){
            String docTypeValue=path.substring(path.lastIndexOf('.') + 1).toUpperCase();
            if (docTypeList.contains(docTypeValue)) {
                this.docType = FileType.valueOf(docTypeValue);
                this.document = Base64.encodeBase64String(Files.readAllBytes(Paths.get(path)));
            }
            else {
                throw new Exception("The document provided is not a valid file type.");
            }
        }
        else {
            throw new Exception("DocumentPath has not been set");
        }
    }


}
