package hello;

import org.springframework.social.connect.ConnectionRepository;
import org.springframework.social.facebook.api.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class HelloController {

    private Facebook facebook;
    private ConnectionRepository connectionRepository;

    public HelloController(Facebook facebook, ConnectionRepository connectionRepository) {
        this.facebook = facebook;
        this.connectionRepository = connectionRepository;
    }

    @GetMapping
    public String helloFacebook(Model model) {
        if (connectionRepository.findPrimaryConnection(Facebook.class) == null) {
            return "redirect:/connect/facebook";
        }

        String [] fields = { "id", "email",  "first_name", "last_name", "name"};
        User userProfile = facebook.fetchObject("me", User.class, fields);

        //User userProfile = facebook.userOperations().getUserProfile(); // raises the exception caused by the "bio" field.

        model.addAttribute("facebookProfile", userProfile);

        String userId = userProfile.getId();
        PagingParameters pagingParameters = new PagingParameters(15,0,null,null);

        try {
            //PagedList<Page> liked_page = facebook.likeOperations().getPagesLiked(pagingParameters);
            //Error deserializing data from Facebook: Can not deserialize instance of int out of START_OBJECT token
            //https://github.com/spring-projects/spring-social-facebook/issues/209
            //dont ask for "likes" (fan count)

            String [] PAGE_FIELDS = { "id", "link", "name"};
            PagedList<Page> liked_page = facebook.fetchConnections(userId, "likes", Page.class, pagingParameters.toMap(), PAGE_FIELDS);

            if(liked_page.isEmpty()) model.addAttribute("message", "You haven't liked any page yet.");
            else{
                model.addAttribute("liked_page", liked_page);
                model.addAttribute("message", "Here are pages you liked:");
            }

        }catch (Exception e){
            System.out.println(e.getMessage());
            model.addAttribute("message", e.getMessage());

        }

        return "hello";
    }

}
