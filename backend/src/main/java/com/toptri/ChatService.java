package com.toptri;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;

@Service
public class ChatService {

  private final NLP nlp = new NLP();
  private final OfferRanker ranker = new OfferRanker();

  private Firestore db() {
    return FirestoreClient.getFirestore();
  }

  // =========================
  // BUYER: chat endpoint logic
  // =========================
  public ChatRes chat(ChatReq req) throws ExecutionException, InterruptedException {
    String text = (req.text == null) ? "" : req.text.trim();

    NLP.Intent parsed = nlp.parse(text);
    List<String> prios = normalize(req.priorities);

    List<Offer> offers = loadOffers();

    List<Rec> recs;
    if ("BEST_NOW".equals(parsed.intent)) {
      // default priorities for best offer now
      List<String> bestPrios = List.of("cheapest", "fastest", "simple", "sweet");
      recs = ranker.topN(offers, parsed, bestPrios, 1);
    } else {
      recs = ranker.topN(offers, parsed, prios, 3);
    }

    String requestId = saveRequest(text, parsed, recs);

    ChatRes res = new ChatRes();
    res.requestId = requestId;
    res.parsed = parsed;
    res.recs = recs;
    return res;
  }

  // =========================
  // SELLER: save offer to Firestore (called by POST /api/offers)
  // =========================
  public String saveOffer(OfferReq req) throws ExecutionException, InterruptedException {
    Map<String, Object> data = new HashMap<>();
    data.put("active", req.active);
    data.put("category", nz(req.category));
    data.put("item", nz(req.item));
    data.put("vendor", nz(req.vendor));
    data.put("price", req.price);
    data.put("etaMin", req.etaMin);
    data.put("sweet", req.sweet);
    data.put("simple", req.simple);
    data.put("createdAt", FieldValue.serverTimestamp());

    // return doc id (useful for debugging)
    return db().collection("offers").add(data).get().getId();
  }

  // =========================
  // Firestore: load offers
  // =========================
  private List<Offer> loadOffers() throws ExecutionException, InterruptedException {
    ApiFuture<QuerySnapshot> fut = db().collection("offers").get();
    QuerySnapshot snap = fut.get();

    List<Offer> out = new ArrayList<>();
    for (DocumentSnapshot d : snap.getDocuments()) {
      Boolean active = d.getBoolean("active");
      if (active != null && !active) continue;

      Offer o = new Offer();
      o.id = d.getId();
      o.vendor = str(d, "vendor");
      o.item = str(d, "item");
      o.category = str(d, "category");
      o.price = dbl(d, "price");
      o.etaMin = (int) lng(d, "etaMin");
      o.sweet = (int) lng(d, "sweet");
      o.simple = (int) lng(d, "simple");
      o.active = (active == null) || active;
      out.add(o);
    }
    return out;
  }

  // =========================
  // Firestore: save request logs
  // =========================
  private String saveRequest(String text, NLP.Intent parsed, List<Rec> recs)
      throws ExecutionException, InterruptedException {

    Map<String, Object> data = new HashMap<>();
    data.put("text", text);
    data.put("parsed", parsed);
    data.put("createdAt", FieldValue.serverTimestamp());

    DocumentReference ref = db().collection("requests").document();
    ref.set(data).get();

    for (Rec r : recs) {
      Map<String, Object> rd = new HashMap<>();
      rd.put("offerId", r.offerId);
      rd.put("vendor", r.vendor);
      rd.put("title", r.title);
      rd.put("category", r.category);
      rd.put("price", r.price);
      rd.put("etaMin", r.etaMin);
      rd.put("score", r.score);
      rd.put("createdAt", FieldValue.serverTimestamp());
      ref.collection("recommendations").add(rd).get();
    }

    return ref.getId();
  }

  // =========================
  // Helpers
  // =========================
  private List<String> normalize(String[] p) {
    if (p == null || p.length == 0) return List.of("cheapest", "fastest");

    List<String> out = new ArrayList<>();
    for (String s : p) {
      if (s == null) continue;
      String x = s.trim().toLowerCase();
      if (List.of("cheapest", "fastest", "sweet", "simple").contains(x)) out.add(x);
    }
    return out.isEmpty() ? List.of("cheapest", "fastest") : out;
  }

  private String nz(String s) {
    return (s == null) ? "" : s.trim();
  }

  private String str(DocumentSnapshot d, String k) {
    return Optional.ofNullable(d.getString(k)).orElse("");
  }

  private double dbl(DocumentSnapshot d, String k) {
    Double v = d.getDouble(k);
    if (v != null) return v;
    Long l = d.getLong(k);
    return (l == null) ? 0.0 : l.doubleValue();
  }

  private long lng(DocumentSnapshot d, String k) {
    Long v = d.getLong(k);
    return (v == null) ? 0L : v;
  }
  public void saveOffer(OfferReq req) throws Exception {
  Map<String, Object> data = new HashMap<>();
  data.put("active", true);
  data.put("vendor", req.vendor);
  data.put("item", req.item);
  data.put("category", req.category);
  data.put("price", req.price);
  data.put("etaMin", req.etaMin);
  data.put("sweet", req.sweet);
  data.put("simple", req.simple);
  data.put("createdAt", FieldValue.serverTimestamp());

  db().collection("offers").add(data).get();
}

}
