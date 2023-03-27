package bitxon.containerization;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {


    @GetMapping("/hello")
    public Message get() {
        return new Message("Hello");
    }


    public record Message(String message) {
    }
}
