package com.toptri;

public class NLP {
  public static class Intent {
    public String raw, intent, category, keywords;
    public Integer maxPrice;
    public boolean wantsSweet;
  }

public Intent parse(String text){
  String raw = text == null ? "" : text.trim();
  String t = raw.toLowerCase();

  Intent p = new Intent();
  p.raw = raw;

  // 1) detect BEST_NOW
  boolean isBest = t.contains("best offer now") || t.contains("best now") || t.startsWith("best ");
  p.intent = isBest ? "BEST_NOW" : "SEARCH";

  // 2) wants sweet + max price
  p.wantsSweet = t.contains("sweet") || t.contains("manis");
  p.maxPrice = maxPrice(t);

  // 3) remove best keywords for cleaner category detection
  String clean = t
    .replace("best offer now","")
    .replace("best now","")
    .replace("best","")
    .trim();

  // 4) category detection from clean text
  if (clean.contains("nasi padang") || clean.contains("padang")) p.category = "nasi padang";
  else if (clean.contains("warteg")) p.category = "warteg";
  else if (clean.contains("ayam") || clean.contains("geprek")) p.category = "ayam";
  else if (p.wantsSweet) p.category = "sweet";
  else p.category = null;

  p.keywords = clean;
  return p;
}

  private Integer maxPrice(String t){
    if (!t.matches(".*\\b(max|maks|<=|under)\\s*\\d+\\s*k?\\b.*")) return null;
    var r = t.replaceAll(".*\\b(max|maks|<=|under)\\s*(\\d+)\\s*(k)?\\b.*", "$2,$3");
    String[] p = r.split(",");
    int n = Integer.parseInt(p[0]);
    boolean isK = p.length > 1 && "k".equals(p[1]);
    return isK ? n*1000 : n;
  }
}
