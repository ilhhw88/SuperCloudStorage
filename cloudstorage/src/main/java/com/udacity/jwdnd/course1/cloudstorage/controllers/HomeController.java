package com.udacity.jwdnd.course1.cloudstorage.controllers;

import com.udacity.jwdnd.course1.cloudstorage.models.CredentialForm;
import com.udacity.jwdnd.course1.cloudstorage.models.FileForm;
import com.udacity.jwdnd.course1.cloudstorage.models.NoteForm;
import com.udacity.jwdnd.course1.cloudstorage.models.User;
import com.udacity.jwdnd.course1.cloudstorage.services.*;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Controller
@RequestMapping("/home")
public class HomeController {
    private final FileService fileService;
    private final UserService userService;
    private final NoteService noteService;
    private final CredentialService credentialService;
    private final EncryptionService encryptionService;

    public HomeController(FileService fileService,
                          UserService userService,
                          NoteService noteService,
                          CredentialService credentialService,
                          EncryptionService encryptionService) {
        this.fileService = fileService;
        this.userService = userService;
        this.noteService = noteService;
        this.credentialService = credentialService;
        this.encryptionService = encryptionService;
    }

    @GetMapping
    public String getHomePage(
            Authentication authentication,
            @ModelAttribute("newFile") FileForm newFile,
            @ModelAttribute("newNote") NoteForm newNote,
            @ModelAttribute("newCredential") CredentialForm newCredential,
            Model model
    ) {
        Integer userId = getUserId(authentication);
        model.addAttribute("files", this.fileService.getFileListingByUserId(userId));
        model.addAttribute("note", this.noteService.getNoteListingByUserId(userId));
        model.addAttribute("credentials", this.credentialService.getCredentialListingByUserId(userId));
        model.addAttribute("encryptionService", encryptionService);

        return "home";
    }

    private Integer getUserId(Authentication authentication) {
        String username = authentication.getName();
        User user = userService.getUserByUsername(username);
        return user.getUserId();
    }

    @PostMapping
    public String newFile(
            Authentication authentication,
            @ModelAttribute("newFile") FileForm newFile,
            @ModelAttribute("newNote") NoteForm newNote,
            @ModelAttribute("newCredential") CredentialForm newCredential,
            Model model
    ) throws IOException {
        String userName = authentication.getName();
        User user = userService.getUserByUsername(userName);
        Integer userId = user.getUserId();
        String[] fileListings = fileService.getFileListingByUserId(userId);
        //multipartFile get file from inputStream
        MultipartFile multipartFile = newFile.getFile();
        String fileName = multipartFile.getOriginalFilename();
        boolean fileExists = false;
        for (String fileListing : fileListings) {
            if (fileListing.equals(fileName)) {
                fileExists = true;
                break;
            }
        }
        if (!fileExists) {
            fileService.addFile(multipartFile, userName);
            model.addAttribute("result", "success");
        } else {
            model.addAttribute("result", "error");
            model.addAttribute("message", "file has already been created");
        }
        model.addAttribute("files", fileService.getFileListingByUserId(userId));

        return "result";
    }

    //download file
    @GetMapping(
            value = "/get-file/{fileName}",
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE //tell browser return binary stream data
    )
    public @ResponseBody
    byte[] getFile(@PathVariable String fileName) {
        return fileService.getFileByFileName(fileName).getFileData();
    }

    @GetMapping(value = "/delete-file/{fileName}")
    public String deleteFile(
            Authentication authentication,
            @PathVariable String fileName,
            @ModelAttribute("newFile") FileForm newFile,
            @ModelAttribute("newNote") NoteForm newNote,
            @ModelAttribute("newCredential") CredentialForm newCredential,
            Model model) {
        fileService.deleteFile(fileName);
        Integer userId = getUserId(authentication);
        model.addAttribute("files", fileService.getFileListingByUserId(userId));
        model.addAttribute("result", "success");

        return "result";
    }
}
