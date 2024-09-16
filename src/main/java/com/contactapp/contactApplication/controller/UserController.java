package com.contactapp.contactApplication.controller;


import com.contactapp.contactApplication.Entities.Contact;
import com.contactapp.contactApplication.dao.ContactRepository;
import com.contactapp.contactApplication.helper.Message;
import jakarta.servlet.http.HttpSession;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.ui.Model;
import com.contactapp.contactApplication.Entities.User;
import com.contactapp.contactApplication.dao.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ContactRepository contactRepository;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;


    @ModelAttribute
    public void addCommonData(Model model, Principal principal){
        String userName = principal.getName();
        System.out.println("USERNAME: " + userName);

        User user = userRepository.findByEmail(userName);

        System.out.println("USER: " + user);
        model.addAttribute("user", user);

    }

    @RequestMapping("/index")
    public String dashboard(Model model, Principal principal) {

        model.addAttribute("title","User Dashboard");
       return "user_dashboard";
    }

    //open add form handler
    @GetMapping("/add-contact")
    public String openAddContactForm(Model model){
        model.addAttribute("title", "Add Contact");
        model.addAttribute("contact", new Contact());
        return "add_contact_form";
    }

    @PostMapping("/process-contact")
    public String processContact(@ModelAttribute Contact contact, @RequestParam("imageFile") MultipartFile file, Principal principal, HttpSession session) {
        try {
            String name = principal.getName();
            User user = this.userRepository.findByEmail(name);

            // Processing and uploading file
            if (file.isEmpty()) {
                System.out.println("File is empty");
                contact.setImage("background.avif");
            } else {
                // Save the file to folder and update the name to contact
                contact.setImage(file.getOriginalFilename());
                File saveFile = new ClassPathResource("static/img").getFile();
                Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Image is uploaded");
            }

            contact.setUser(user);
            user.getContacts().add(contact);
            this.userRepository.save(user);
            System.out.println("Data: " + contact);
            System.out.println("Added to database");

            // Success message
            session.setAttribute("message", new Message("Your contact is added! Add more..", "success"));
        } catch (IOException e) {
            System.out.println("File upload failed: " + e.getMessage());
            session.setAttribute("message", new Message("File upload failed. Try again.", "danger"));
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            session.setAttribute("message", new Message("Something went wrong. Try again.", "danger"));
        }
        return "add_contact_form";
    }

    @GetMapping("/show_contacts/{page}")
    public String showContacts(@PathVariable("page") Integer page, Model m,Principal principal){
    m.addAttribute("title", "Show User Contacts");

           String userName= principal.getName();
        User user=this.userRepository.findByEmail(userName);

        Pageable pageable=PageRequest.of(page,8);
        Page<Contact> contacts=this.contactRepository.findContactsByUser(user.getId(),pageable);
          m.addAttribute("contacts", contacts);
          m.addAttribute("currentPage",page);
          m.addAttribute("totalPages",contacts.getTotalPages());
        return "show_contacts";
    }

    @RequestMapping("/{cId}/contact")
    public String showContactDetail(@PathVariable("cId") Integer cId, Model model,Principal principal){
        System.out.println("CID "+cId);
        Optional<Contact> contactOptional= this.contactRepository.findById(cId);
        Contact contact = contactOptional.get();
        String userName=principal.getName();
        User user=this.userRepository.findByEmail(userName);
        if(user.getId()== contact.getUser().getId()){
            model.addAttribute("contact", contact);
        }

        return "contact_detail";
    }

    //delete contact
    @GetMapping("/delete/{cid}")
    public String deleteContact(@PathVariable("cid") Integer cId,Model model ,Principal principal,HttpSession session){

        Optional<Contact> contactOptional=this.contactRepository.findById(cId);
        Contact contact = contactOptional.get();
        System.out.println("Contact "+contact.getcId());
        String userName=principal.getName();
        User user=this.userRepository.findByEmail(userName);
        if(user.getId()== contact.getUser().getId()){
            contact.setUser(null);
            this.contactRepository.delete(contact);
        }

        session.setAttribute("message",new Message("Contact deleted successfully..", "success"));
        return "redirect:/user/show_contacts/0";
    }

    //update contact
    @PostMapping("/update-contact/{cid}")
    public String UpdateForm(@PathVariable("cid") Integer cId,Model m){
        m.addAttribute("title","Update Contact");
        Optional<Contact> contactOptional=this.contactRepository.findById(cId);
        Contact contact = contactOptional.get();
        m.addAttribute("contact", contact);
        return "update_form";
    }

    @RequestMapping(value="/process-update", method=RequestMethod.POST)
    public String updateHandler(@ModelAttribute Contact contact,
                                @RequestParam("imageFile") MultipartFile file,
                                Model m,
                                HttpSession session,
                                Principal principal){
        try{
            // Fetch old contact details
            Contact oldContactDetail = this.contactRepository.findById(contact.getcId()).get();

            // If a new file is uploaded (file is not empty)
            if(!file.isEmpty()){
                // Delete the old photo (optional: if you want to remove it from the server)

                // Save the new photo
                File saveFile = new ClassPathResource("static/img").getFile();
                Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                contact.setImage(file.getOriginalFilename()); // Set new image filename
            } else {
                // If no new file is uploaded, keep the old image
                contact.setImage(oldContactDetail.getImage());
            }

            // Set the user who owns the contact
            String userName = principal.getName();
            User user = this.userRepository.findByEmail(userName);
            contact.setUser(user);

            // Save the updated contact details
            this.contactRepository.save(contact);

            // Success message
            session.setAttribute("message", new Message("Your contact is updated..", "success"));

        } catch(Exception e){
            e.printStackTrace();
            session.setAttribute("message", new Message("Something went wrong while updating the contact", "danger"));
        }

        System.out.println("Contact Name: " + contact.getName());
        System.out.println("Contact Id: " + contact.getcId());

        // Return the updated contact detail page
        return "contact_detail";
    }

    //your profile handler
    @GetMapping("/profile")
    public String Profile(Model model,Principal principal){
        String userName=principal.getName();
        User user=this.userRepository.findByEmail(userName);
        System.out.println("User Image URL: " + user.getImagUrl());
        model.addAttribute("title", "profile Page");
        return "profile";
    }


    //open Settings handler
    @GetMapping("/settings")
    public String OpenSettings(){
        return "settings";
    }


    @PostMapping("/change-password")
    public String changePassword(@RequestParam("oldPassword") String oldPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 Principal principal, HttpSession session) {
        String userName = principal.getName();
        User currentUser = this.userRepository.findByEmail(userName);

        if (this.bCryptPasswordEncoder.matches(oldPassword, currentUser.getPassword())) {
            // Change password
            currentUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
            this.userRepository.save(currentUser);
            session.setAttribute("message", new Message("Your password is changed successfully.", "success"));
        } else {
            session.setAttribute("message", new Message("Wrong old password!", "danger"));
            return "redirect:/user/settings"; // Redirect back to settings if the old password is wrong
        }

        // Optional: You can clear the message here as well, but usually, it's better to clear it after it's displayed on the settings page
        return "redirect:/user/settings"; // Redirect back to settings after successful change
    }


}
