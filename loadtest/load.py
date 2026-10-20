#!/usr/bin/env python3
"""Driver de carga em loop fechado (só stdlib). Dados e credenciais fictícios.
  load.py setup BASE_URL                              cria USERS usuários, cada um com REMINDERS lembretes mensais
  load.py run BASE_URL A|B|C CONCURRENCY DURATION_S [WARMUP_S]
Cenários: A login | B listagem mensal (monthly-bills com simulação) | C criar lembrete + achar transação + pagar (1 iteração = 3 requests)."""
import datetime, http.client, json, random, sys, threading, time, urllib.parse

USERS, REMINDERS, PASSWORD = 64, 30, "loadtest-pass-123"
email = lambda i: "loadtest-u%d@example.com" % i


def call(c, method, path, body=None, token=None):
    h = {"Content-Type": "application/json"}
    if token:
        h["Authorization"] = "Bearer " + token
    c.request(method, path, json.dumps(body) if body is not None else None, h)
    r = c.getresponse()
    return r.status, r.read(), r.getheader("Location")


def new_conn(u):
    return http.client.HTTPConnection(u.hostname, u.port)


def login(c, i):
    s, b, _ = call(c, "POST", "/auth/login", {"email": email(i), "password": PASSWORD})
    assert s == 200, (s, b)
    return json.loads(b)["token"]


def reminder(title, day):
    return {"title": title, "description": "load", "date": day.isoformat(), "recurrence": "MENSAL",
            "method": "PIX", "value": 100.5}


def setup(u):
    c = new_conn(u)
    for i in range(USERS):
        s, b, _ = call(c, "POST", "/users", {"name": "Load %d" % i, "email": email(i), "password": PASSWORD})
        assert s in (200, 201), (s, b)
        tok = login(c, i)
        for k in range(REMINDERS):
            s, b, _ = call(c, "POST", "/payments/reminders", reminder("seed-%d" % k, datetime.date.today() + datetime.timedelta(days=1 + k % 28)), tok)
            assert s == 201, (s, b)
    print("setup: %d users x %d monthly reminders" % (USERS, REMINDERS))


def run(u, sc, conc, dur, warm):
    boot = new_conn(u)
    tokens = [login(boot, i) for i in range(USERS)]
    today = datetime.date.today()
    run_id = "%x" % random.getrandbits(32)  # títulos únicos entre rodadas (409 se repetir)
    lat, errs, lock = [], {}, threading.Lock()
    measure_from = time.monotonic() + warm
    end = measure_from + dur

    def once(conn, wid, n):
        i = random.randrange(USERS) if sc != "C" else wid % USERS
        tok = tokens[i]
        if sc == "A":
            return call(conn, "POST", "/auth/login", {"email": email(i), "password": PASSWORD})[0] == 200
        if sc == "B":
            return call(conn, "GET", "/payments/reminders/monthly-bills?withSimulatedBills=true&year=%d&month=%d" % (today.year, today.month), None, tok)[0] == 200
        s, _, loc = call(conn, "POST", "/payments/reminders", reminder("w%s-%d-%d" % (run_id, wid, n), today + datetime.timedelta(days=1 + n % 28)), tok)
        if s != 201:
            return False
        s, b, _ = call(conn, "GET", "/payments/transactions/list?reminderId=" + loc.rsplit("/", 1)[1], None, tok)
        if s != 200:
            return False
        tx = json.loads(b)["content"][0]["id"]
        return call(conn, "POST", "/payments/transactions/%s/pay" % tx, {}, tok)[0] == 204

    def worker(wid):
        conn, n = new_conn(u), 0
        while time.monotonic() < end:
            t, n = time.monotonic(), n + 1
            try:
                ok = once(conn, wid, n)
            except Exception as e:
                ok = type(e).__name__
                conn = new_conn(u)
            d = time.monotonic() - t
            if t >= measure_from:
                with lock:
                    if ok is True:
                        lat.append(d)
                    else:
                        errs[str(ok)] = errs.get(str(ok), 0) + 1

    ts = [threading.Thread(target=worker, args=(w,)) for w in range(conc)]
    [t.start() for t in ts]; [t.join() for t in ts]
    lat.sort()
    pct = lambda p: lat[min(len(lat) - 1, int(p * len(lat)))] * 1000 if lat else float("nan")
    print(json.dumps({"scenario": sc, "concurrency": conc, "duration_s": dur, "ok": len(lat), "errors": errs,
                      "throughput_per_s": round(len(lat) / dur, 1), "p50_ms": round(pct(.5), 1),
                      "p95_ms": round(pct(.95), 1), "p99_ms": round(pct(.99), 1),
                      "max_ms": round(lat[-1] * 1000, 1) if lat else None}))


if __name__ == "__main__":
    a = sys.argv
    url = urllib.parse.urlparse(a[2])
    if a[1] == "setup":
        setup(url)
    else:
        run(url, a[3], int(a[4]), float(a[5]), float(a[6]) if len(a) > 6 else 5.0)
