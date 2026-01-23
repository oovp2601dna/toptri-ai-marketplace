package com.toptri;

import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = {"http://localhost:5500"})
@RestController
@RequestMapping("/api")
public class ChatController {

  private final ChatService svc;
  public ChatController(ChatService svc){ this.svc = svc; }

  @GetMapping("/health")
  public String health(){ return "ok"; }

  @PostMapping("/chat")
  public ChatRes chat(@RequestBody ChatReq req) throws Exception {
    return svc.chat(req);
  }
}
