package org.pocketretail.core.deliverylayer.ui;

import org.pocketretail.core.deliverylayer.database.entity.Client;
import org.pocketretail.core.deliverylayer.ui.handler.ClientsUIHandler;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Objects;

@Controller
@RequestMapping(path = "/deliverylayer/ui")
public class UIController {

    //TODO: Remove controller and add Webflux Router function
    private final ClientsUIHandler clientUIHandler;

    public UIController(ClientsUIHandler clientUIHandler) {
        this.clientUIHandler = clientUIHandler;
    }


    @GetMapping(path = "/home")
    public String home(Model model) {
        OAuth2User user = ((OAuth2User) SecurityContextHolder.getContext().getAuthentication()
                                                             .getPrincipal());
        model.addAllAttributes(user.getAttributes());
        return "home";
    }

    @GetMapping(path = "/clients")
    public String clients(Model model) {
        OAuth2User user = ((OAuth2User) SecurityContextHolder.getContext().getAuthentication()
                                                             .getPrincipal());
        model.addAllAttributes(user.getAttributes());
        model.addAttribute("clients", clientUIHandler.getAllClients());
        return "clients/clients";
    }

    @GetMapping(path = "/clients/client/{clientId}")
    public String client(Model model, @PathVariable String clientId) {
        OAuth2User user = ((OAuth2User) SecurityContextHolder.getContext().getAuthentication()
                                                             .getPrincipal());
        model.addAllAttributes(user.getAttributes());
        model.addAttribute("client", clientUIHandler.getClient(clientId));
        model.addAttribute("clientRequests", clientUIHandler.getClientRequests(
                (Client) Objects.requireNonNull(model.getAttribute("client"))));
        return "clients/client";
    }

}
