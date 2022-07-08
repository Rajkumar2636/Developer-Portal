package com.fd.fiserv.developerPortal;

import developerAPI.DeveloperAPI;

import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;


@RestController
public class IPGWebShopController {

    @GetMapping("/")
    @CrossOrigin("http://localhost:13002")
    public ResponseEntity<String> loadWebShop(Model model) throws IOException {
        return DeveloperAPI.getAPI();
    }

    @CrossOrigin("http://localhost:13002")
    @RequestMapping(value = "/getTidList", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<String> getResultofGetRequest() throws IOException {
        return DeveloperAPI.getAPI();
    }
}
