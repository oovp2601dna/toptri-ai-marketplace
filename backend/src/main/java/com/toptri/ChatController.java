package com.toptri;

import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
public class ChatController {

  private final ChatService chatService;

  // ✅ inject ChatService (Spring will auto-wire)
  public ChatController(ChatService chatService) {
    this.chatService = chatService;
  }

  // =========================
  // BUYER: chat
  // =========================
  @PostMapping("/api/chat")
  public ChatRes chat(@RequestBody ChatReq req) throws Exception {
    return chatService.chat(req);
  }

  // =========================
  // SELLER: add offer
  // =========================
  @PostMapping("/api/offers")
  public String createOffer(@RequestBody OfferReq req) throws Exception {
    // return id biar gampang debug
    return chatService.saveOffer(req);
  }

  // (optional) health check
  @GetMapping("/api/health")
  public String health() {
    return "OK";
  }
}
