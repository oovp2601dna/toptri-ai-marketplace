package com.toptri;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class OfferRanker {

  public List<Rec> topN(List<Offer> offers, NLP.Intent ctx, List<String> prios, int n) {
    List<Offer> pool = new ArrayList<>(offers);

    // kalau BEST_NOW: kita biarin ctx.category / maxPrice tetap boleh,
    // tapi kalau kamu mau BEST_NOW benar-benar global, bisa di-ignore (lihat catatan bawah)
    if (ctx.category != null) {
      pool.removeIf(o -> o.category == null || !o.category.equalsIgnoreCase(ctx.category));
    }
    if (ctx.maxPrice != null) {
      pool.removeIf(o -> o.price > ctx.maxPrice);
    }

    // fallback kalau filter terlalu ketat
    if (pool.isEmpty()) {
      pool = new ArrayList<>(offers);
      if (ctx.maxPrice != null) pool.removeIf(o -> o.price > ctx.maxPrice);
    }

    List<Rec> out = new ArrayList<>();
    for (Offer o : pool) {
      Rec r = new Rec();
      r.offerId = o.id;
      r.vendor = o.vendor;
      r.title = o.item;
      r.category = o.category;
      r.price = o.price;
      r.etaMin = o.etaMin;
      r.score = score(o, ctx, prios);
      out.add(r);
    }

    out.sort(Comparator.comparingDouble(a -> a.score));
    return out.subList(0, Math.min(n, out.size()));
  }

  private double score(Offer o, NLP.Intent ctx, List<String> pr) {
    double priceScore = o.price / 1000.0;
    double etaScore = o.etaMin;
    double sweetScore = (3 - clamp(o.sweet)) * 2.0;
    double simpleScore = (3 - clamp(o.simple)) * 2.0;

    double s = 0;
    for (String p : pr) {
      if ("cheapest".equals(p)) {
        s += priceScore * 3.0;
      } else if ("fastest".equals(p)) {
        s += etaScore * 1.2;
      } else if ("sweet".equals(p)) {
        s += sweetScore * 1.5;
      } else if ("simple".equals(p)) {
        s += simpleScore * 1.5;
      }
    }

    if (ctx.wantsSweet && !pr.contains("sweet")) s += sweetScore * 0.4;
    return s;
  }

  private int clamp(int x) {
    if (x < 0) return 0;
    if (x > 3) return 3;
    return x;
  }
}
