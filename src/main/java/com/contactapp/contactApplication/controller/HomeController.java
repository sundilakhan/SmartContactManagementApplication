package com.contactapp.contactApplication.controller;

import com.contactapp.contactApplication.Entities.User;
import com.contactapp.contactApplication.dao.UserRepository;
import com.contactapp.contactApplication.helper.Message;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class HomeController {

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @RequestMapping("/")
    public String home(Model model) {
        model.addAttribute("title", "Home - Smart Contact Manager");
        return "home";
    }

    @RequestMapping("/about")
    public String about(Model model) {
        model.addAttribute("title", "About - Smart Contact Manager");
        model.addAttribute("user", new User());
        return "about";
    }

    @RequestMapping("/signup")
    public String signup(Model model) {
        model.addAttribute("title", "SignUp - Smart Contact Manager");
        model.addAttribute("user", new User()); // Add this line to create a new User object and pass it to the model
        return "signup";
    }

        @RequestMapping("/user_dashboard")
        public String userDashboard(Model model) {
            model.addAttribute("title", "User Dashboard - Smart Contact Manager");
            return "user_dashboard"; // This should map to a Thymeleaf template named user-dashboard.html
        }


    // Handler for registering user
    @RequestMapping(value = "/do_register", method = RequestMethod.POST)
    public String registerUser(@Valid
                               @ModelAttribute("user") User user,BindingResult result1,
                               @RequestParam(value = "agreement", defaultValue = "false") boolean agreement,
                               Model model,
                               RedirectAttributes redirectAttributes) {

        try {
            if (!agreement) {
                System.out.println("You have not agreed to the terms and condition");
                throw new Exception("You have not agreed to terms and conditions");
            }

            if(result1.hasErrors()){
                System.out.println("Error" + result1.toString());
                model.addAttribute("user,user");
                return "signup";
            }
            user.setRole("ROLE_USER");
            user.setEnabled(true);
            user.setImagUrl("default.png");
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            System.out.println("Agreement " + agreement);
            System.out.println("User " + user);
            User result = this.userRepository.save(user);

            redirectAttributes.addFlashAttribute("message", new Message("Successfully Registered!!", "alert-success"));

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("user", user); // Preserve user input
            redirectAttributes.addFlashAttribute("message", new Message("Something went wrong!! " + e.getMessage(), "alert-danger"));
            return "redirect:/signup"; // Redirect in case of an error to show the flash message
        }

        return "redirect:/signup"; // Redirect to the signup page to avoid form resubmission
    }

    //handler for custom login
    @RequestMapping("/login")
    public String CustomLogin(Model model){
        model.addAttribute("title", "Login- Smart Contact Manager");
        return "login";
    }

}