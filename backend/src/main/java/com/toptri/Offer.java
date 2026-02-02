package com.toptri;

public class Offer {
  public String id, vendor, item, category;
  public double price;
  public int etaMin, sweet, simple;
  public boolean active;

  // Getter biar SellerController bisa pakai getX()
  public String getId() { return id; }
  public String getVendor() { return vendor; }
  public String getItem() { return item; }
  public String getCategory() { return category; }
  public double getPrice() { return price; }
  public int getEtaMin() { return etaMin; }
  public int getSweet() { return sweet; }
  public int getSimple() { return simple; }
  public boolean isActive() { return active; }
}
