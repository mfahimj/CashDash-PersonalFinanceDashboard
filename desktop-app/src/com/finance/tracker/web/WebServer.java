package com.finance.tracker.web;

import com.finance.tracker.dao.BudgetDao;
import com.finance.tracker.dao.CategoryDao;
import com.finance.tracker.dao.TransactionDao;
import com.finance.tracker.model.*;
import com.finance.tracker.service.FinanceService;
import com.finance.tracker.service.FinanceService.MonthlyReportItem;
import com.finance.tracker.util.CurrencyFormatter;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;

public class WebServer {
    private final int port;
    private final FinanceService financeService;
    private HttpServer server;

    public WebServer(int port, FinanceService financeService) {
        this.port = port;
        this.financeService = financeService;
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);

        // API Endpoints
        server.createContext("/api/dashboard/summary", new DashboardSummaryHandler());
        server.createContext("/api/transactions", new TransactionsHandler());
        server.createContext("/api/categories", new CategoriesHandler());
        server.createContext("/api/budgets", new BudgetsHandler());
        server.createContext("/api/analytics/monthly", new MonthlyTrendsHandler());
        server.createContext("/api/analytics/categories", new CategoryBreakdownHandler());
        server.createContext("/api/reports/export-csv", new ExportCsvHandler());

        // Web GUI Static Handler
        server.createContext("/", new StaticAppHandler());

        server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
        server.start();
        System.out.println("======================================================================");
        System.out.println("CashDash Web Server running at: http://localhost:" + port);
        System.out.println("REST API available at: http://localhost:" + port + "/api/");
        System.out.println("Open http://localhost:" + port + " in your web browser!");
        System.out.println("======================================================================");
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    private void handleCors(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }

    private void sendJson(HttpExchange exchange, int statusCode, String json) throws IOException {
        handleCors(exchange);
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private String readRequestBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody();
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            byte[] buf = new byte[1024];
            int len;
            while ((len = is.read(buf)) != -1) {
                bos.write(buf, 0, len);
            }
            return bos.toString(StandardCharsets.UTF_8);
        }
    }

    private Map<String, String> parseQueryParams(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null || query.isEmpty()) return map;
        for (String param : query.split("&")) {
            String[] pair = param.split("=");
            try {
                String key = URLDecoder.decode(pair[0], StandardCharsets.UTF_8.name());
                String val = pair.length > 1 ? URLDecoder.decode(pair[1], StandardCharsets.UTF_8.name()) : "";
                map.put(key, val);
            } catch (Exception ignored) {}
        }
        return map;
    }

    // Handlers
    private class DashboardSummaryHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            handleCors(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            Map<String, String> params = parseQueryParams(exchange.getRequestURI().getQuery());
            int m = params.containsKey("month") ? Integer.parseInt(params.get("month")) : LocalDate.now().getMonthValue();
            int y = params.containsKey("year") ? Integer.parseInt(params.get("year")) : LocalDate.now().getYear();

            DashboardSummary s = financeService.getDashboardSummary(m, y);
            StringBuilder sb = new StringBuilder("{");
            sb.append("\"totalBalance\":").append(s.getTotalBalance()).append(",");
            sb.append("\"totalIncome\":").append(s.getTotalIncome()).append(",");
            sb.append("\"totalExpenses\":").append(s.getTotalExpenses()).append(",");
            sb.append("\"totalSavings\":").append(s.getTotalSavings()).append(",");
            sb.append("\"savingsRate\":").append(s.getSavingsRate()).append(",");
            sb.append("\"incomeGrowthPercent\":").append(s.getIncomeGrowthPercent()).append(",");
            sb.append("\"expenseGrowthPercent\":").append(s.getExpenseGrowthPercent()).append(",");
            sb.append("\"budgetLimit\":").append(s.getBudgetLimit()).append(",");
            sb.append("\"budgetSpent\":").append(s.getBudgetSpent()).append(",");
            sb.append("\"budgetRemaining\":").append(s.getBudgetRemaining()).append(",");
            sb.append("\"budgetUsagePercentage\":").append(s.getBudgetUsagePercentage()).append(",");
            sb.append("\"recentTransactions\":[");
            List<Transaction> recent = s.getRecentTransactions();
            for (int i = 0; i < recent.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append(transactionToJson(recent.get(i)));
            }
            sb.append("]}");

            sendJson(exchange, 200, sb.toString());
        }
    }

    private class TransactionsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            handleCors(exchange);
            String method = exchange.getRequestMethod().toUpperCase();
            if ("OPTIONS".equals(method)) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            String path = exchange.getRequestURI().getPath();
            if ("GET".equals(method)) {
                Map<String, String> params = parseQueryParams(exchange.getRequestURI().getQuery());
                String typeStr = params.get("type");
                TransactionType type = (typeStr != null && !typeStr.equalsIgnoreCase("ALL"))
                        ? TransactionType.fromString(typeStr) : null;
                Long catId = params.containsKey("categoryId") ? Long.parseLong(params.get("categoryId")) : null;
                String search = params.get("search");

                List<Transaction> list = financeService.getTransactionDao().findByFilters(type, catId, null, null, null, search);
                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < list.size(); i++) {
                    if (i > 0) sb.append(",");
                    sb.append(transactionToJson(list.get(i)));
                }
                sb.append("]");
                sendJson(exchange, 200, sb.toString());
            } else if ("POST".equals(method)) {
                String body = readRequestBody(exchange);
                Transaction tx = parseTransactionJson(body);
                financeService.getTransactionDao().save(tx);
                sendJson(exchange, 201, transactionToJson(tx));
            } else if ("PUT".equals(method)) {
                String[] parts = path.split("/");
                Long id = Long.parseLong(parts[parts.length - 1]);
                String body = readRequestBody(exchange);
                Transaction tx = parseTransactionJson(body);
                tx.setId(id);
                financeService.getTransactionDao().update(tx);
                sendJson(exchange, 200, transactionToJson(tx));
            } else if ("DELETE".equals(method)) {
                String[] parts = path.split("/");
                Long id = Long.parseLong(parts[parts.length - 1]);
                financeService.getTransactionDao().delete(id);
                sendJson(exchange, 200, "{\"success\":true}");
            }
        }
    }

    private class CategoriesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            handleCors(exchange);
            String method = exchange.getRequestMethod().toUpperCase();
            if ("OPTIONS".equals(method)) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if ("GET".equals(method)) {
                List<Category> list = financeService.getCategoryDao().findAll();
                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < list.size(); i++) {
                    if (i > 0) sb.append(",");
                    Category c = list.get(i);
                    int count = financeService.getTransactionDao().countByCategoryId(c.getId());
                    sb.append("{")
                      .append("\"id\":").append(c.getId()).append(",")
                      .append("\"userId\":").append(c.getUserId()).append(",")
                      .append("\"name\":\"").append(escapeJson(c.getName())).append("\",")
                      .append("\"type\":\"").append(c.getType().name()).append("\",")
                      .append("\"transactionCount\":").append(count)
                      .append("}");
                }
                sb.append("]");
                sendJson(exchange, 200, sb.toString());
            } else if ("POST".equals(method)) {
                String body = readRequestBody(exchange);
                Map<String, String> json = parseSimpleJson(body);
                String name = json.getOrDefault("name", "New Category");
                TransactionType type = TransactionType.fromString(json.getOrDefault("type", "EXPENSE"));
                Category c = new Category(null, 1L, name, type);
                financeService.getCategoryDao().save(c);
                sendJson(exchange, 201, "{\"id\":" + c.getId() + ",\"name\":\"" + escapeJson(c.getName()) + "\",\"type\":\"" + c.getType().name() + "\"}");
            } else if ("DELETE".equals(method)) {
                String path = exchange.getRequestURI().getPath();
                String[] parts = path.split("/");
                Long id = Long.parseLong(parts[parts.length - 1]);
                if (!financeService.canDeleteCategory(id)) {
                    sendJson(exchange, 400, "{\"error\":\"Cannot delete category referenced by transactions\"}");
                    return;
                }
                financeService.getCategoryDao().delete(id);
                sendJson(exchange, 200, "{\"success\":true}");
            }
        }
    }

    private class BudgetsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            handleCors(exchange);
            String method = exchange.getRequestMethod().toUpperCase();
            if ("OPTIONS".equals(method)) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if ("GET".equals(method)) {
                Map<String, String> params = parseQueryParams(exchange.getRequestURI().getQuery());
                int m = params.containsKey("month") ? Integer.parseInt(params.get("month")) : LocalDate.now().getMonthValue();
                int y = params.containsKey("year") ? Integer.parseInt(params.get("year")) : LocalDate.now().getYear();

                List<Budget> list = financeService.getBudgetsWithCalculations(m, y);
                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < list.size(); i++) {
                    if (i > 0) sb.append(",");
                    Budget b = list.get(i);
                    sb.append("{")
                      .append("\"id\":").append(b.getId()).append(",")
                      .append("\"userId\":").append(b.getUserId()).append(",")
                      .append("\"categoryId\":").append(b.getCategoryId() != null ? b.getCategoryId() : "null").append(",")
                      .append("\"categoryName\":\"").append(escapeJson(b.getCategoryName() != null ? b.getCategoryName() : "Overall Budget")).append("\",")
                      .append("\"amount\":").append(b.getAmount()).append(",")
                      .append("\"month\":").append(b.getMonth()).append(",")
                      .append("\"year\":").append(b.getYear()).append(",")
                      .append("\"spent\":").append(b.getSpent()).append(",")
                      .append("\"remaining\":").append(b.getRemaining()).append(",")
                      .append("\"percentage\":").append(b.getPercentage()).append(",")
                      .append("\"status\":\"").append(b.getStatus()).append("\"")
                      .append("}");
                }
                sb.append("]");
                sendJson(exchange, 200, sb.toString());
            } else if ("POST".equals(method)) {
                String body = readRequestBody(exchange);
                Map<String, String> json = parseSimpleJson(body);
                String catStr = json.get("categoryId");
                Long catId = (catStr != null && !catStr.equals("null") && !catStr.isEmpty()) ? Long.parseLong(catStr) : null;
                BigDecimal amt = new BigDecimal(json.getOrDefault("amount", "1000"));
                int m = Integer.parseInt(json.getOrDefault("month", "10"));
                int y = Integer.parseInt(json.getOrDefault("year", "2026"));

                Budget b = new Budget(null, 1L, catId, amt, m, y);
                financeService.getBudgetDao().save(b);
                sendJson(exchange, 201, "{\"id\":" + b.getId() + ",\"amount\":" + b.getAmount() + "}");
            } else if ("DELETE".equals(method)) {
                String path = exchange.getRequestURI().getPath();
                String[] parts = path.split("/");
                Long id = Long.parseLong(parts[parts.length - 1]);
                financeService.getBudgetDao().delete(id);
                sendJson(exchange, 200, "{\"success\":true}");
            }
        }
    }

    private class MonthlyTrendsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            handleCors(exchange);
            List<MonthlyReportItem> items = financeService.getMonthlyTrends(6);
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < items.size(); i++) {
                if (i > 0) sb.append(",");
                MonthlyReportItem it = items.get(i);
                sb.append("{")
                  .append("\"monthLabel\":\"").append(it.getMonthLabel()).append("\",")
                  .append("\"month\":").append(it.getMonth()).append(",")
                  .append("\"year\":").append(it.getYear()).append(",")
                  .append("\"income\":").append(it.getIncome()).append(",")
                  .append("\"expenses\":").append(it.getExpenses()).append(",")
                  .append("\"savings\":").append(it.getSavings())
                  .append("}");
            }
            sb.append("]");
            sendJson(exchange, 200, sb.toString());
        }
    }

    private class CategoryBreakdownHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            handleCors(exchange);
            Map<String, String> params = parseQueryParams(exchange.getRequestURI().getQuery());
            int m = params.containsKey("month") ? Integer.parseInt(params.get("month")) : LocalDate.now().getMonthValue();
            int y = params.containsKey("year") ? Integer.parseInt(params.get("year")) : LocalDate.now().getYear();

            Map<String, BigDecimal> breakdown = financeService.getExpenseBreakdownByCategory(m, y);
            StringBuilder sb = new StringBuilder("{");
            int i = 0;
            for (Map.Entry<String, BigDecimal> entry : breakdown.entrySet()) {
                if (i++ > 0) sb.append(",");
                sb.append("\"").append(escapeJson(entry.getKey())).append("\":").append(entry.getValue());
            }
            sb.append("}");
            sendJson(exchange, 200, sb.toString());
        }
    }

    private class ExportCsvHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            handleCors(exchange);
            List<Transaction> list = financeService.getTransactionDao().findAll();
            StringBuilder sb = new StringBuilder();
            sb.append("Transaction ID,Description,Date,Category,Type,Payment Method,Amount,Notes\n");
            for (Transaction tx : list) {
                sb.append(tx.getId()).append(",")
                  .append("\"").append(tx.getDescription().replace("\"", "\"\"")).append("\",")
                  .append(tx.getTransactionDate()).append(",")
                  .append("\"").append(tx.getCategoryName() != null ? tx.getCategoryName() : "Other").append("\",")
                  .append(tx.getType().name()).append(",")
                  .append("\"").append(tx.getPaymentMethod()).append("\",")
                  .append(tx.getAmount().toPlainString()).append(",")
                  .append("\"").append(tx.getNotes() != null ? tx.getNotes().replace("\"", "\"\"") : "").append("\"\n");
            }
            byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/csv; charset=UTF-8");
            exchange.getResponseHeaders().set("Content-Disposition", "attachment; filename=\"transactions_export_" + LocalDate.now() + ".csv\"");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    // Static Web GUI Handler
    private class StaticAppHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            handleCors(exchange);
            String html = WebGuiTemplate.getHtml();
            byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    private String transactionToJson(Transaction tx) {
        return "{" +
                "\"id\":" + tx.getId() + "," +
                "\"userId\":" + tx.getUserId() + "," +
                "\"description\":\"" + escapeJson(tx.getDescription()) + "\"," +
                "\"amount\":" + tx.getAmount() + "," +
                "\"type\":\"" + tx.getType().name() + "\"," +
                "\"categoryId\":" + tx.getCategoryId() + "," +
                "\"categoryName\":\"" + escapeJson(tx.getCategoryName() != null ? tx.getCategoryName() : "Other") + "\"," +
                "\"transactionDate\":\"" + tx.getTransactionDate() + "\"," +
                "\"paymentMethod\":\"" + escapeJson(tx.getPaymentMethod()) + "\"," +
                "\"notes\":\"" + escapeJson(tx.getNotes() != null ? tx.getNotes() : "") + "\"" +
                "}";
    }

    private Transaction parseTransactionJson(String json) {
        Map<String, String> m = parseSimpleJson(json);
        Transaction tx = new Transaction();
        tx.setDescription(m.getOrDefault("description", "Untitled"));
        tx.setAmount(new BigDecimal(m.getOrDefault("amount", "0")));
        tx.setType(TransactionType.fromString(m.getOrDefault("type", "EXPENSE")));
        tx.setCategoryId(Long.parseLong(m.getOrDefault("categoryId", "1")));
        tx.setTransactionDate(LocalDate.parse(m.getOrDefault("transactionDate", LocalDate.now().toString())));
        tx.setPaymentMethod(m.getOrDefault("paymentMethod", "Bank Transfer"));
        tx.setNotes(m.getOrDefault("notes", ""));
        return tx;
    }

    private Map<String, String> parseSimpleJson(String json) {
        Map<String, String> map = new HashMap<>();
        if (json == null) return map;
        String trimmed = json.trim();
        if (trimmed.startsWith("{")) trimmed = trimmed.substring(1);
        if (trimmed.endsWith("}")) trimmed = trimmed.substring(0, trimmed.length() - 1);

        String[] tokens = trimmed.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
        for (String t : tokens) {
            String[] kv = t.split(":(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
            if (kv.length == 2) {
                String k = kv[0].trim().replace("\"", "");
                String v = kv[1].trim();
                if (v.startsWith("\"") && v.endsWith("\"")) {
                    v = v.substring(1, v.length() - 1);
                }
                map.put(k, v);
            }
        }
        return map;
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "");
    }

    public static void main(String[] args) throws IOException {
        CategoryDao catDao = new CategoryDao();
        TransactionDao txDao = new TransactionDao(catDao);
        BudgetDao bgtDao = new BudgetDao(catDao);
        FinanceService service = new FinanceService(txDao, catDao, bgtDao);

        int port = 8080;
        if (args.length > 0) {
            try { port = Integer.parseInt(args[0]); } catch (Exception ignored) {}
        }
        WebServer ws = new WebServer(port, service);
        ws.start();
    }
}

