package org.pocketretail.core.deliverylayer.ui;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

import jakarta.servlet.http.HttpServletResponse;

@RestController
public class RedirectController {

    @GetMapping({"/deliverylayer/ui", "/deliverylayer/ui/"})
    public void redirectFromUIRoot(HttpServletResponse response) throws IOException {
        response.sendRedirect("/deliverylayer/ui/home");
    }
}
