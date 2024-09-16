package com.contactapp.contactApplication.controller;


import com.contactapp.contactApplication.Entities.Contact;
import com.contactapp.contactApplication.Entities.User;
import com.contactapp.contactApplication.dao.ContactRepository;
import com.contactapp.contactApplication.dao.UserRepository;
import org.hibernate.boot.jaxb.mapping.marshall.GenerationTimingMarshalling;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
public class SearchController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ContactRepository contactRepository;


    //search Handler
    @GetMapping("/search/{query}")
    public ResponseEntity<?> search(@PathVariable("query") String query, Principal principal){

        System.out.println(query);

        User user= this.userRepository.findByEmail(principal.getName());
        List<Contact> contacts=this.contactRepository.findByNameContainingAndUser(query,user);
        return ResponseEntity.ok(contacts);
    }

}
