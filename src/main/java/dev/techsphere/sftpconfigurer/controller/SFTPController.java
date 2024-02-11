package dev.techsphere.sftpconfigurer.controller;

import dev.techsphere.sftpconfigurer.model.IntegrationFlowRequest;
import dev.techsphere.sftpconfigurer.model.ServerInfoRequest;
import dev.techsphere.sftpconfigurer.service.SFTPService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/register")
public class SFTPController {

    @Autowired
    private SFTPService service;

    @PostMapping("/server")
    public ResponseEntity<String> registerSessionFactory(@RequestBody ServerInfoRequest server) {
        return new ResponseEntity<>(service.registerSessionFactory(server), HttpStatus.OK);
    }

    @PostMapping("/inbound")
    public ResponseEntity<String> registerInbound(@RequestBody IntegrationFlowRequest request) {
        return new ResponseEntity<>(service.registerInboundFlow(request), HttpStatus.OK);
    }

    @PostMapping("/outbound")
    public ResponseEntity<String> registerOutbound(@RequestBody IntegrationFlowRequest request) {
        return new ResponseEntity<>(service.registerOutboundFlow(request), HttpStatus.OK);
    }
}
