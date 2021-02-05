package com.udacity.jwdnd.course1.cloudstorage.services;

import com.udacity.jwdnd.course1.cloudstorage.mappers.FileMapper;
import com.udacity.jwdnd.course1.cloudstorage.mappers.UserMapper;
import com.udacity.jwdnd.course1.cloudstorage.models.File;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Service
public class FileService {
    private final FileMapper fileMapper;
    private final UserMapper userMapper;

    public FileService(FileMapper fileMapper, UserMapper userMapper) {
        this.fileMapper = fileMapper;
        this.userMapper = userMapper;
    }
    public String[] getFileListingByUserId(Integer userId) {
        return fileMapper.getFileListingsByUserId(userId);
    }
    public void addFile(MultipartFile multipartFile, String username) throws IOException {
        InputStream fileString = multipartFile.getInputStream();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        //Reads some number of bytes from the input stream and stores them into the buffer array.
        int numRead;
        byte[] data = new byte[1024];
        while ((numRead = fileString.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, numRead);
        }
        //flush the string
        buffer.flush();
        byte[] fileData = buffer.toByteArray();

        String fileName = multipartFile.getOriginalFilename();
        String contentType = multipartFile.getContentType();
        String fileSize = String.valueOf(multipartFile.getSize());
        Integer userId = userMapper.getUserByUsername(username).getUserId();
        File file = new File(0, fileName, contentType, fileSize, userId, fileData);
        fileMapper.insert(file);
    }

    public File getFileByFileName(String fileName) {
        return fileMapper.getFileByFileName(fileName);
    }

    public void deleteFile(String fileName) {
        fileMapper.deleteFile(fileName);
    }
}
