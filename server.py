#!/usr/bin/env python3
"""
FinTrack Pro / CashDash - Local Web Server & REST API
Zero external dependencies - runs on Python 3.8+
Shares identical schema, calculations, and data stores with the Java backend.
"""

import sys
import os
import json
import csv
import io
import argparse
from http.server import HTTPServer, BaseHTTPRequestHandler
from urllib.parse import urlparse, parse_qs
from datetime import datetime, date
from decimal import Decimal, ROUND_HALF_UP

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
DATA_TRANSACTIONS = os.path.join(BASE_DIR, "data_transactions.dat")
DATA_CATEGORIES = os.path.join(BASE_DIR, "data_categories.dat")
DATA_BUDGETS = os.path.join(BASE_DIR, "data_budgets.dat")
WEB_INDEX = os.path.join(BASE_DIR, "web", "index.html")

OPENING_BALANCE = Decimal("50000.00")


def to_dec(val):
    if val is None or val == "":
        return Decimal("0.00")
    try:
        return Decimal(str(val))
    except Exception:
        return Decimal("0.00")


class DataStore:
    def __init__(self):
        self.categories = {}      # id -> dict
        self.transactions = {}    # id -> dict
        self.budgets = {}         # id -> dict
        self.load_all()

    def load_all(self):
        self.load_categories()
        self.load_transactions()
        self.load_budgets()

    def load_categories(self):
        self.categories.clear()
        if not os.path.exists(DATA_CATEGORIES):
            return
        with open(DATA_CATEGORIES, "r", encoding="utf-8") as f:
            for line in f:
                line = line.strip()
                if not line:
                    continue
                parts = line.split("||")
                if len(parts) >= 4:
                    cid = int(parts[0])
                    user_id = int(parts[1])
                    name = parts[2]
                    ctype = parts[3].upper()
                    created_at = parts[4] if len(parts) >= 5 else datetime.now().isoformat()
                    self.categories[cid] = {
                        "id": cid,
                        "userId": user_id,
                        "name": name,
                        "type": ctype,
                        "createdAt": created_at,
                    }

    def save_categories(self):
        with open(DATA_CATEGORIES, "w", encoding="utf-8") as f:
            for c in self.categories.values():
                f.write(f"{c['id']}||{c['userId']}||{c['name']}||{c['type']}||{c.get('createdAt', datetime.now().isoformat())}\n")

    def load_transactions(self):
        self.transactions.clear()
        if not os.path.exists(DATA_TRANSACTIONS):
            return
        with open(DATA_TRANSACTIONS, "r", encoding="utf-8") as f:
            for line in f:
                line = line.strip()
                if not line:
                    continue
                parts = line.split("||")
                if len(parts) >= 8:
                    tid = int(parts[0])
                    user_id = int(parts[1])
                    desc = parts[2]
                    amount = to_dec(parts[3])
                    ttype = parts[4].upper()
                    cat_id = int(parts[5])
                    tdate = parts[6]
                    method = parts[7]
                    notes = parts[8] if len(parts) >= 9 else ""
                    created_at = parts[9] if len(parts) >= 10 else datetime.now().isoformat()
                    updated_at = parts[10] if len(parts) >= 11 else datetime.now().isoformat()
                    cat = self.categories.get(cat_id)
                    cat_name = cat["name"] if cat else "Other"
                    self.transactions[tid] = {
                        "id": tid,
                        "userId": user_id,
                        "description": desc,
                        "amount": amount,
                        "type": ttype,
                        "categoryId": cat_id,
                        "categoryName": cat_name,
                        "transactionDate": tdate,
                        "paymentMethod": method,
                        "notes": notes,
                        "createdAt": created_at,
                        "updatedAt": updated_at,
                    }

    def save_transactions(self):
        with open(DATA_TRANSACTIONS, "w", encoding="utf-8") as f:
            for tx in self.transactions.values():
                f.write(f"{tx['id']}||{tx['userId']}||{tx['description']}||{tx['amount']:.2f}||"
                        f"{tx['type']}||{tx['categoryId']}||{tx['transactionDate']}||"
                        f"{tx.get('paymentMethod', 'Other')}||{tx.get('notes', '').replace(chr(10), ' ')}||"
                        f"{tx.get('createdAt', datetime.now().isoformat())}||{tx.get('updatedAt', datetime.now().isoformat())}\n")

    def load_budgets(self):
        self.budgets.clear()
        if not os.path.exists(DATA_BUDGETS):
            return
        with open(DATA_BUDGETS, "r", encoding="utf-8") as f:
            for line in f:
                line = line.strip()
                if not line:
                    continue
                parts = line.split("||")
                if len(parts) >= 6:
                    bid = int(parts[0])
                    user_id = int(parts[1])
                    cat_id_raw = parts[2]
                    cat_id = int(cat_id_raw) if cat_id_raw and cat_id_raw != "0" and cat_id_raw != "null" else None
                    amount = to_dec(parts[3])
                    month = int(parts[4])
                    year = int(parts[5])
                    created_at = parts[6] if len(parts) >= 7 else datetime.now().isoformat()
                    updated_at = parts[7] if len(parts) >= 8 else datetime.now().isoformat()
                    cat = self.categories.get(cat_id) if cat_id else None
                    cat_name = cat["name"] if cat else "Overall Budget"
                    self.budgets[bid] = {
                        "id": bid,
                        "userId": user_id,
                        "categoryId": cat_id,
                        "categoryName": cat_name,
                        "amount": amount,
                        "month": month,
                        "year": year,
                        "createdAt": created_at,
                        "updatedAt": updated_at,
                    }

    def save_budgets(self):
        with open(DATA_BUDGETS, "w", encoding="utf-8") as f:
            for b in self.budgets.values():
                cid_str = str(b["categoryId"]) if b.get("categoryId") else "0"
                f.write(f"{b['id']}||{b['userId']}||{cid_str}||{b['amount']:.2f}||{b['month']}||{b['year']}||"
                        f"{b.get('createdAt', datetime.now().isoformat())}||{b.get('updatedAt', datetime.now().isoformat())}\n")

    def next_tx_id(self):
        return max(self.transactions.keys(), default=0) + 1

    def next_cat_id(self):
        return max(self.categories.keys(), default=100) + 1

    def next_bgt_id(self):
        return max(self.budgets.keys(), default=0) + 1


db = DataStore()


class FinTrackHandler(BaseHTTPRequestHandler):
    def send_cors_headers(self):
        self.send_header("Access-Control-Allow-Origin", "*")
        self.send_header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
        self.send_header("Access-Control-Allow-Headers", "Content-Type, Authorization")

    def do_OPTIONS(self):
        self.send_response(204)
        self.send_cors_headers()
        self.end_headers()

    def reply_json(self, status, data):
        def decimal_default(obj):
            if isinstance(obj, Decimal):
                return float(obj)
            raise TypeError
        body = json.dumps(data, default=decimal_default).encode("utf-8")
        self.send_response(status)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_cors_headers()
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def read_json_body(self):
        length = int(self.headers.get("Content-Length", 0))
        if length == 0:
            return {}
        raw = self.rfile.read(length).decode("utf-8")
        try:
            return json.loads(raw)
        except Exception:
            return {}

    def do_GET(self):
        parsed = urlparse(self.path)
        path = parsed.path.rstrip("/")
        qs = parse_qs(parsed.query)

        # Static Web GUI
        if path == "" or path == "/index.html":
            if not os.path.exists(WEB_INDEX):
                self.send_response(404)
                self.end_headers()
                self.wfile.write(b"web/index.html not found")
                return
            with open(WEB_INDEX, "rb") as f:
                content = f.read()
            self.send_response(200)
            self.send_header("Content-Type", "text/html; charset=utf-8")
            self.send_cors_headers()
            self.send_header("Content-Length", str(len(content)))
            self.end_headers()
            self.wfile.write(content)
            return

        # Dashboard Summary API
        if path == "/api/dashboard/summary":
            now = datetime.now()
            month = int(qs.get("month", [now.month])[0])
            year = int(qs.get("year", [now.year])[0])

            prev_month = 12 if month == 1 else month - 1
            prev_year = year - 1 if month == 1 else year

            cur_inc = Decimal("0.00")
            cur_exp = Decimal("0.00")
            prev_inc = Decimal("0.00")
            prev_exp = Decimal("0.00")

            all_txs = list(db.transactions.values())
            for tx in all_txs:
                try:
                    td = datetime.strptime(tx["transactionDate"], "%Y-%m-%d").date()
                except Exception:
                    continue
                if td.month == month and td.year == year:
                    if tx["type"] == "INCOME":
                        cur_inc += tx["amount"]
                    else:
                        cur_exp += tx["amount"]
                elif td.month == prev_month and td.year == prev_year:
                    if tx["type"] == "INCOME":
                        prev_inc += tx["amount"]
                    else:
                        prev_exp += tx["amount"]

            tot_bal = OPENING_BALANCE + cur_inc - cur_exp
            tot_sav = cur_inc - cur_exp
            sav_rate = float((tot_sav / cur_inc * Decimal(100)).quantize(Decimal("0.01"), rounding=ROUND_HALF_UP)) if cur_inc > Decimal(0) else 0.0

            inc_growth = float(((cur_inc - prev_inc) / prev_inc * Decimal(100)).quantize(Decimal("0.01"), rounding=ROUND_HALF_UP)) if prev_inc > Decimal(0) else 0.0
            exp_growth = float(((cur_exp - prev_exp) / prev_exp * Decimal(100)).quantize(Decimal("0.01"), rounding=ROUND_HALF_UP)) if prev_exp > Decimal(0) else 0.0

            # Budget limit
            bgt_limit = Decimal("0.00")
            overall_bgt = [b for b in db.budgets.values() if b["month"] == month and b["year"] == year and (b["categoryId"] is None or b["categoryId"] == 0)]
            if overall_bgt:
                bgt_limit = overall_bgt[0]["amount"]
            else:
                cat_bgts = [b for b in db.budgets.values() if b["month"] == month and b["year"] == year and b["categoryId"]]
                for b in cat_bgts:
                    bgt_limit += b["amount"]

            bgt_spent = cur_exp
            bgt_remaining = bgt_limit - bgt_spent
            bgt_usage = float((bgt_spent / bgt_limit * Decimal(100)).quantize(Decimal("0.01"), rounding=ROUND_HALF_UP)) if bgt_limit > Decimal(0) else 0.0

            # Recent transactions (latest 7)
            sorted_txs = sorted(all_txs, key=lambda t: (t["transactionDate"], t["id"]), reverse=True)[:7]
            recent = []
            for tx in sorted_txs:
                recent.append({
                    "id": tx["id"],
                    "userId": tx["userId"],
                    "description": tx["description"],
                    "amount": float(tx["amount"]),
                    "type": tx["type"],
                    "categoryId": tx["categoryId"],
                    "categoryName": tx["categoryName"],
                    "transactionDate": tx["transactionDate"],
                    "paymentMethod": tx.get("paymentMethod", "Other"),
                    "notes": tx.get("notes", ""),
                })

            self.reply_json(200, {
                "totalBalance": float(tot_bal),
                "totalIncome": float(cur_inc),
                "totalExpenses": float(cur_exp),
                "totalSavings": float(tot_sav),
                "savingsRate": sav_rate,
                "incomeGrowthPercent": inc_growth,
                "expenseGrowthPercent": exp_growth,
                "budgetLimit": float(bgt_limit),
                "budgetSpent": float(bgt_spent),
                "budgetRemaining": float(bgt_remaining),
                "budgetUsagePercentage": bgt_usage,
                "recentTransactions": recent,
            })
            return

        # Transactions API
        if path == "/api/transactions":
            t_type = qs.get("type", [None])[0]
            cat_id = qs.get("categoryId", [None])[0]
            search = qs.get("search", [None])[0]

            res = list(db.transactions.values())
            if t_type and t_type.upper() != "ALL":
                res = [t for t in res if t["type"] == t_type.upper()]
            if cat_id:
                try:
                    cid_int = int(cat_id)
                    res = [t for t in res if t["categoryId"] == cid_int]
                except Exception:
                    pass
            if search and search.strip():
                s = search.strip().lower()
                res = [t for t in res if s in t["description"].lower() or s in t.get("categoryName", "").lower() or s in t.get("paymentMethod", "").lower() or s in t.get("notes", "").lower()]

            res = sorted(res, key=lambda t: (t["transactionDate"], t["id"]), reverse=True)
            output = []
            for tx in res:
                output.append({
                    "id": tx["id"],
                    "userId": tx["userId"],
                    "description": tx["description"],
                    "amount": float(tx["amount"]),
                    "type": tx["type"],
                    "categoryId": tx["categoryId"],
                    "categoryName": tx.get("categoryName", "Other"),
                    "transactionDate": tx["transactionDate"],
                    "paymentMethod": tx.get("paymentMethod", "Other"),
                    "notes": tx.get("notes", ""),
                })
            self.reply_json(200, output)
            return

        # Categories API
        if path == "/api/categories":
            tx_counts = {}
            for t in db.transactions.values():
                tx_counts[t["categoryId"]] = tx_counts.get(t["categoryId"], 0) + 1

            cats = sorted(db.categories.values(), key=lambda c: c["name"])
            out = []
            for c in cats:
                out.append({
                    "id": c["id"],
                    "userId": c["userId"],
                    "name": c["name"],
                    "type": c["type"],
                    "transactionCount": tx_counts.get(c["id"], 0),
                })
            self.reply_json(200, out)
            return

        # Budgets API
        if path == "/api/budgets":
            now = datetime.now()
            month = int(qs.get("month", [now.month])[0])
            year = int(qs.get("year", [now.year])[0])

            # Calculate expenses by category and overall for this period
            overall_expense = Decimal("0.00")
            cat_expenses = {}
            for tx in db.transactions.values():
                try:
                    td = datetime.strptime(tx["transactionDate"], "%Y-%m-%d").date()
                except Exception:
                    continue
                if td.month == month and td.year == year and tx["type"] == "EXPENSE":
                    overall_expense += tx["amount"]
                    cat_expenses[tx["categoryId"]] = cat_expenses.get(tx["categoryId"], Decimal("0.00")) + tx["amount"]

            bgts = [b for b in db.budgets.values() if b["month"] == month and b["year"] == year]
            out = []
            for b in bgts:
                is_overall = b["categoryId"] is None or b["categoryId"] == 0
                spent = overall_expense if is_overall else cat_expenses.get(b["categoryId"], Decimal("0.00"))
                rem = b["amount"] - spent
                pct = float((spent / b["amount"] * Decimal(100)).quantize(Decimal("0.01"), rounding=ROUND_HALF_UP)) if b["amount"] > Decimal(0) else 0.0
                status = "Exceeded" if pct >= 100.0 else ("Warning" if pct >= 80.0 else "Normal")
                cat_name = b.get("categoryName")
                if not cat_name:
                    cat_name = "Overall Budget" if is_overall else "Category Budget"
                out.append({
                    "id": b["id"],
                    "userId": b["userId"],
                    "categoryId": b["categoryId"],
                    "categoryName": cat_name,
                    "amount": float(b["amount"]),
                    "month": b["month"],
                    "year": b["year"],
                    "spent": float(spent),
                    "remaining": float(rem),
                    "percentage": pct,
                    "status": status,
                })
            self.reply_json(200, out)
            return

        # Monthly Trends Analytics API
        if path == "/api/analytics/monthly":
            count = int(qs.get("months", [6])[0])
            now = datetime.now()
            cur_month = now.month
            cur_year = now.year

            months_list = []
            for i in range(count - 1, -1, -1):
                m = cur_month - i
                y = cur_year
                while m <= 0:
                    m += 12
                    y -= 1
                months_list.append((m, y))

            out = []
            month_names = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"]
            for m, y in months_list:
                inc = Decimal("0.00")
                exp = Decimal("0.00")
                for tx in db.transactions.values():
                    try:
                        td = datetime.strptime(tx["transactionDate"], "%Y-%m-%d").date()
                    except Exception:
                        continue
                    if td.month == m and td.year == y:
                        if tx["type"] == "INCOME":
                            inc += tx["amount"]
                        else:
                            exp += tx["amount"]
                out.append({
                    "monthLabel": f"{month_names[m - 1]} {y}",
                    "month": m,
                    "year": y,
                    "income": float(inc),
                    "expenses": float(exp),
                    "savings": float(inc - exp),
                })
            self.reply_json(200, out)
            return

        # Category Breakdown Analytics API
        if path == "/api/analytics/categories":
            now = datetime.now()
            month = int(qs.get("month", [now.month])[0])
            year = int(qs.get("year", [now.year])[0])

            breakdown = {}
            for tx in db.transactions.values():
                try:
                    td = datetime.strptime(tx["transactionDate"], "%Y-%m-%d").date()
                except Exception:
                    continue
                if td.month == month and td.year == year and tx["type"] == "EXPENSE":
                    cname = tx.get("categoryName") or "Other"
                    breakdown[cname] = breakdown.get(cname, Decimal("0.00")) + tx["amount"]

            out = {k: float(v) for k, v in breakdown.items()}
            self.reply_json(200, out)
            return

        # Export CSV Report
        if path == "/api/reports/export-csv":
            output = io.StringIO()
            writer = csv.writer(output)
            writer.writerow(["Transaction ID", "Description", "Date", "Category", "Type", "Payment Method", "Amount", "Notes"])
            for tx in sorted(db.transactions.values(), key=lambda t: (t["transactionDate"], t["id"]), reverse=True):
                writer.writerow([
                    tx["id"],
                    tx["description"],
                    tx["transactionDate"],
                    tx.get("categoryName", "Other"),
                    tx["type"],
                    tx.get("paymentMethod", "Other"),
                    f"{tx['amount']:.2f}",
                    tx.get("notes", ""),
                ])
            csv_data = output.getvalue().encode("utf-8")
            self.send_response(200)
            self.send_header("Content-Type", "text/csv; charset=utf-8")
            self.send_header("Content-Disposition", f'attachment; filename="transactions_export_{date.today().isoformat()}.csv"')
            self.send_cors_headers()
            self.send_header("Content-Length", str(len(csv_data)))
            self.end_headers()
            self.wfile.write(csv_data)
            return

        # Unknown route
        self.send_response(404)
        self.end_headers()
        self.wfile.write(b"Not Found")

    def do_POST(self):
        parsed = urlparse(self.path)
        path = parsed.path.rstrip("/")
        body = self.read_json_body()

        if path == "/api/transactions":
            tid = db.next_tx_id()
            cid = int(body.get("categoryId", 1))
            cat = db.categories.get(cid)
            cat_name = cat["name"] if cat else "Other"
            tx = {
                "id": tid,
                "userId": 1,
                "description": body.get("description", "Untitled"),
                "amount": to_dec(body.get("amount", 0)),
                "type": body.get("type", "EXPENSE").upper(),
                "categoryId": cid,
                "categoryName": cat_name,
                "transactionDate": body.get("transactionDate", date.today().isoformat()),
                "paymentMethod": body.get("paymentMethod", "Bank Transfer"),
                "notes": body.get("notes", ""),
                "createdAt": datetime.now().isoformat(),
                "updatedAt": datetime.now().isoformat(),
            }
            db.transactions[tid] = tx
            db.save_transactions()
            self.reply_json(201, {
                "id": tx["id"],
                "userId": tx["userId"],
                "description": tx["description"],
                "amount": float(tx["amount"]),
                "type": tx["type"],
                "categoryId": tx["categoryId"],
                "categoryName": tx["categoryName"],
                "transactionDate": tx["transactionDate"],
                "paymentMethod": tx["paymentMethod"],
                "notes": tx["notes"],
            })
            return

        if path == "/api/categories":
            cid = db.next_cat_id()
            name = body.get("name", "New Category")
            ctype = body.get("type", "EXPENSE").upper()
            c = {
                "id": cid,
                "userId": 1,
                "name": name,
                "type": ctype,
                "createdAt": datetime.now().isoformat(),
            }
            db.categories[cid] = c
            db.save_categories()
            self.reply_json(201, {"id": cid, "name": name, "type": ctype})
            return

        if path == "/api/budgets":
            cid_val = body.get("categoryId")
            cid = int(cid_val) if cid_val and str(cid_val) not in ("0", "null", "") else None
            amount = to_dec(body.get("amount", 0))
            now = datetime.now()
            month = int(body.get("month", now.month))
            year = int(body.get("year", now.year))

            # Check if budget already exists for this category/period, update or create
            existing = [b for b in db.budgets.values() if b["categoryId"] == cid and b["month"] == month and b["year"] == year]
            if existing:
                b = existing[0]
                b["amount"] = amount
                b["updatedAt"] = datetime.now().isoformat()
            else:
                bid = db.next_bgt_id()
                cat = db.categories.get(cid) if cid else None
                cat_name = cat["name"] if cat else "Overall Budget"
                b = {
                    "id": bid,
                    "userId": 1,
                    "categoryId": cid,
                    "categoryName": cat_name,
                    "amount": amount,
                    "month": month,
                    "year": year,
                    "createdAt": datetime.now().isoformat(),
                    "updatedAt": datetime.now().isoformat(),
                }
                db.budgets[bid] = b
            db.save_budgets()
            self.reply_json(201, {"id": b["id"], "success": True})
            return

        self.send_response(404)
        self.end_headers()

    def do_PUT(self):
        parsed = urlparse(self.path)
        path = parsed.path.rstrip("/")
        body = self.read_json_body()

        if path.startswith("/api/transactions/"):
            tid = int(path.split("/")[-1])
            if tid in db.transactions:
                tx = db.transactions[tid]
                if "description" in body:
                    tx["description"] = body["description"]
                if "amount" in body:
                    tx["amount"] = to_dec(body["amount"])
                if "type" in body:
                    tx["type"] = body["type"].upper()
                if "categoryId" in body:
                    cid = int(body["categoryId"])
                    tx["categoryId"] = cid
                    cat = db.categories.get(cid)
                    tx["categoryName"] = cat["name"] if cat else "Other"
                if "transactionDate" in body:
                    tx["transactionDate"] = body["transactionDate"]
                if "paymentMethod" in body:
                    tx["paymentMethod"] = body["paymentMethod"]
                if "notes" in body:
                    tx["notes"] = body["notes"]
                tx["updatedAt"] = datetime.now().isoformat()
                db.save_transactions()
                self.reply_json(200, {
                    "id": tx["id"],
                    "description": tx["description"],
                    "amount": float(tx["amount"]),
                    "type": tx["type"],
                    "categoryId": tx["categoryId"],
                    "categoryName": tx["categoryName"],
                })
                return

        self.send_response(404)
        self.end_headers()

    def do_DELETE(self):
        parsed = urlparse(self.path)
        path = parsed.path.rstrip("/")

        if path.startswith("/api/transactions/"):
            tid = int(path.split("/")[-1])
            if tid in db.transactions:
                del db.transactions[tid]
                db.save_transactions()
            self.reply_json(200, {"success": True})
            return

        if path.startswith("/api/categories/"):
            cid = int(path.split("/")[-1])
            in_use = any(t["categoryId"] == cid for t in db.transactions.values())
            if in_use:
                self.reply_json(400, {"error": "Cannot delete category referenced by transactions"})
                return
            if cid in db.categories:
                del db.categories[cid]
                db.save_categories()
            self.reply_json(200, {"success": True})
            return

        if path.startswith("/api/budgets/"):
            bid = int(path.split("/")[-1])
            if bid in db.budgets:
                del db.budgets[bid]
                db.save_budgets()
            self.reply_json(200, {"success": True})
            return

        self.send_response(404)
        self.end_headers()

    def log_message(self, format, *args):
        # Keep console output clean
        sys.stderr.write(f"[{datetime.now().strftime('%H:%M:%S')}] {args[0]} {args[1]}\n")


def run(port=8080):
    server = HTTPServer(("0.0.0.0", port), FinTrackHandler)
    print("=" * 70, flush=True)
    print(f"FinTrack Pro Web Server running at: http://localhost:{port}", flush=True)
    print(f"REST API available at: http://localhost:{port}/api/", flush=True)
    print(f"Open http://localhost:{port} in your web browser!", flush=True)
    print("=" * 70, flush=True)
    try:
        server.serve_forever()
    except KeyboardInterrupt:
        print("\nShutting down server...", flush=True)
        server.server_close()


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="FinTrack Pro Web Server")
    parser.add_argument("--port", type=int, default=8080, help="Port to listen on (default 8080)")
    args = parser.parse_args()
    run(port=args.port)