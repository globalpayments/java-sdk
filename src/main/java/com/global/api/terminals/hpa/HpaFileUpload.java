package com.global.api.terminals.hpa;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedList;

import com.global.api.utils.IOUtils;
import com.global.api.utils.StringUtils;
import org.apache.commons.codec.binary.Hex;
import com.global.api.entities.enums.SendFileType;
import com.global.api.entities.exceptions.ApiException;

class HpaFileUpload {
    private String hexData;
    private String fileName;
    private int fileSize;

    String getFileName() {
        return fileName;
    }
    int getFileSize() {
        return fileSize;
    }

    HpaFileUpload(SendFileType imageType, String filePath) throws ApiException {
        //File name
        File file = new File(filePath);
        fileName = file.getName();

        switch(imageType) {
            case Banner: {
                if (!fileName.equalsIgnoreCase("banner.jpg")) {
                    throw new ApiException("The filename must be BANNER.JPG.");
                }
            } break;
            case Logo: {
                if (!fileName.equalsIgnoreCase("idlelogo.jpg")) {
                    throw new ApiException("The filename must be IDLELOGO.JPG.");
                }
            } break;
            default: {
                throw new ApiException("Unknown Send file type");
            }
        }
        
        //File size
        byte[] buffer;
        try(FileInputStream input= new FileInputStream(file);) {
            buffer = new byte[input.available()];
            int bytesRead = input.read(buffer, 0, buffer.length);
        } catch (IOException e) {
            throw new ApiException("Buffer should not be empty.");
        }
        hexData = Hex.encodeHexString(buffer).toUpperCase();
        fileSize = buffer.length;
    }
    
    LinkedList<String> getFileParts(int maxDataLength) {
        if (StringUtils.isNullOrEmpty(hexData)) {
            return null;
        }

        LinkedList<String> fileParts = new LinkedList<String>();
        for (int i = 0; i < hexData.length(); i += maxDataLength) {
            int remainingBytes = hexData.length() - i;
            int endIndex = i + Math.min(maxDataLength, remainingBytes);

            fileParts.add(hexData.substring(i, endIndex));
        }
        return fileParts;
    }
}
