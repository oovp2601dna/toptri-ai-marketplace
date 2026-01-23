// ===============================
// app.js (FULL) - Final (NO HALF)
// Features:
// 1) Chat UI (OOP)
// 2) BUY button -> alert
// 3) BEST OFFER NOW button (optional)
//    - If you previously searched a category (ex: "nasi padang")
//      then BEST OFFER will automatically send: "best offer now nasi padang"
//    - If no category yet, it sends: "best offer now" (global)
// 4) BEST_NOW intent detected from backend (works even if typed manually)
//    - Shows only 1 item and label "BEST"
// 5) No requestId shown in UI
// ===============================

class UI {
  constructor(log){ this.log = log; }

  add(html){
    const d = document.createElement("div");
    d.className = "msg";
    d.innerHTML = html;
    this.log.prepend(d);
  }

  prios(){
    const p = [];
    if (document.getElementById("c1")?.checked) p.push("cheapest");
    if (document.getElementById("c2")?.checked) p.push("fastest");
    if (document.getElementById("c3")?.checked) p.push("sweet");
    if (document.getElementById("c4")?.checked) p.push("simple");
    return p.length ? p : ["cheapest","fastest"];
  }

  static esc(s){
    return (s||"")
      .replaceAll("&","&amp;")
      .replaceAll("<","&lt;")
      .replaceAll(">","&gt;")
      .replaceAll('"',"&quot;")
      .replaceAll("'","&#039;");
  }

  static money(n){
    return "Rp " + Number(n||0).toLocaleString("id-ID");
  }
}

class Api {
  constructor(base){ this.base = base; }

  async chat(text, priorities){
    const r = await fetch(`${this.base}/api/chat`,{
      method:"POST",
      headers:{ "Content-Type":"application/json" },
      body: JSON.stringify({ text, priorities })
    });
    if (!r.ok) {
      const t = await r.text();
      throw new Error(t || "Failed to fetch");
    }
    return r.json();
  }
}

class App {
  constructor(){
    this.ui  = new UI(document.getElementById("log"));
    this.api = new Api("http://localhost:8080");

    // ⭐ remember last category from backend parse (ex: "nasi padang")
    this.lastCategory = null;

    this.#wire();

    this.ui.add(
      `<span class="badge ok">READY</span> ` +
      `coba: <b>nasi padang</b> / <b>something sweet</b> / <b>nasi padang max 17000</b> ` +
      `/ <b>best offer now</b>  `
    );
  }

  #wire(){
    const form = document.getElementById("form");
    const inp  = document.getElementById("inp");

    // OPTIONAL: kalau kamu punya tombol di HTML:
    // <button type="button" id="bestBtn">Best Offer Now</button>
    const bestBtn = document.getElementById("bestBtn");

    // Send normal chat
    form?.addEventListener("submit", async (e)=>{
      e.preventDefault();
      const text = (inp.value||"").trim();
      if(!text) return;
      inp.value = "";
      await this.#send(text);
    });

    // Send best offer (auto attach lastCategory if exists)
    if (bestBtn){
      bestBtn.addEventListener("click", async ()=>{
        const cat = this.lastCategory ? (" " + this.lastCategory) : "";
        await this.#send("best offer now" + cat);
      });
    }
  }

  async #send(text){
    const pr = this.ui.prios();

    // Show user bubble
    this.ui.add(
      `<span class="badge">YOU</span> ` +
      `<b>${UI.esc(text)}</b> ` +
      `<span class="badge">${UI.esc(pr.join(", "))}</span>`
    );

    try{
      const data = await this.api.chat(text, pr);

      // ⭐ save last category for later best-offer button usage
      if (data.parsed?.category) {
        this.lastCategory = data.parsed.category;
      }

      // detect BEST_NOW intent from backend (works even if typed manually)
      const isBestNow = (data.parsed?.intent === "BEST_NOW");

      const recs = data.recs || [];
      const shown = isBestNow ? recs.slice(0,1) : recs;

      const items = shown.map((r,i)=>`
        <div style="margin-top:10px;padding-top:8px;border-top:1px dashed rgba(255,255,255,.15)">
          <span class="badge ok">${isBestNow ? "BEST" : ("#"+(i+1))}</span>
          <b>${UI.esc(r.title)}</b> — ${UI.esc(r.vendor)}<br>

          <span class="badge">${UI.money(r.price)}</span>
          <span class="badge">ETA ${r.etaMin}m</span>
          <span class="badge">score ${Number(r.score).toFixed(2)}</span>

          <div style="margin-top:8px">
            <button class="buy-btn"
              onclick="buyItem('${UI.esc(r.title)}','${UI.esc(r.vendor)}',${Number(r.price||0)})">
              BUY
            </button>
          </div>
        </div>
      `).join("");

      // Show bot bubble (no requestId)
      this.ui.add(
        `<span class="badge ok">BOT</span>` +
        `<span class="badge">${UI.esc(data.parsed?.intent||"")}</span>` +
        `${data.parsed?.category ? `<span class="badge">${UI.esc(data.parsed.category)}</span>` : ""}` +
        `${data.parsed?.maxPrice ? `<span class="badge">max ${UI.money(data.parsed.maxPrice)}</span>` : ""}` +
        (this.lastCategory ? `<span class="badge">last: ${UI.esc(this.lastCategory)}</span>` : "") +
        `<div style="margin-top:8px">${items || "<span class='badge'>No recs</span>"}</div>`
      );

    }catch(err){
      console.error(err);
      this.ui.add(`<span class="badge">ERR</span> ${UI.esc(err.message || "Failed to fetch")}`);
    }
  }
}

// Start app
new App();

// Global buy function (alert)
function buyItem(title, vendor, price){
  alert(
    "🛒 BUY CONFIRMED\n\n" +
    "Item : " + title + "\n" +
    "Vendor : " + vendor + "\n" +
    "Price : Rp " + Number(price||0).toLocaleString("id-ID")
  );
}
