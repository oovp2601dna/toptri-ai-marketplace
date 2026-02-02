package com.toptri;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/seller")
@CrossOrigin // so your frontend can call it
public class SellerController {

  private Firestore db(){ return FirestoreClient.getFirestore(); }

  // Create new offer
  @PostMapping("/offers")
  public Map<String, Object> createOffer(@RequestBody Offer o) throws ExecutionException, InterruptedException {
    Map<String,Object> data = offerToMap(o);
    data.put("active", o.active); // default can be true from UI

    DocumentReference ref = db().collection("offers").document();
    ref.set(data).get();

    return Map.of("id", ref.getId(), "ok", true);
  }

  // Update existing offer (full update)
  @PutMapping("/offers/{id}")
  public Map<String, Object> updateOffer(@PathVariable String id, @RequestBody Offer o)
      throws ExecutionException, InterruptedException {

    Map<String,Object> data = offerToMap(o);
    data.put("active", o.active);

    db().collection("offers").document(id).set(data, SetOptions.merge()).get();
    return Map.of("id", id, "ok", true);
  }

  // Toggle active (soft delete)
  @PatchMapping("/offers/{id}/active")
  public Map<String, Object> setActive(@PathVariable String id, @RequestBody Map<String,Object> body)
      throws ExecutionException, InterruptedException {

    Boolean active = (Boolean) body.get("active");
    if (active == null) active = true;

    db().collection("offers").document(id).update("active", active).get();
    return Map.of("id", id, "active", active, "ok", true);
  }

  // List offers (optional filter by vendor)
  @GetMapping("/offers")
  public List<Map<String,Object>> listOffers(@RequestParam(required = false) String vendor)
      throws ExecutionException, InterruptedException {

    CollectionReference col = db().collection("offers");
    Query q = (vendor == null || vendor.isBlank()) ? col : col.whereEqualTo("vendor", vendor);

    ApiFuture<QuerySnapshot> fut = q.get();
    QuerySnapshot snap = fut.get();

    List<Map<String,Object>> out = new ArrayList<>();
    for (DocumentSnapshot d : snap.getDocuments()){
      Map<String,Object> m = new HashMap<>(d.getData() == null ? Map.of() : d.getData());
      m.put("id", d.getId());
      out.add(m);
    }
    return out;
  }

  private Map<String,Object> offerToMap(Offer o){
    Map<String,Object> data = new HashMap<>();
    data.put("vendor", o.getVendor());
    data.put("item", o.getItem());
    data.put("category", o.getCategory());
    data.put("price", o.getPrice());
    data.put("etaMin", o.getEtaMin());
    data.put("sweet", o.getSweet());
    data.put("simple", o.getSimple());
    return data;

  }
}
