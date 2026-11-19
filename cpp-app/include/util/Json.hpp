#pragma once

#include <string>
#include <string_view>
#include <map>
#include <vector>
#include <sstream>
#include <iomanip>
#include <cctype>

namespace fintrack::util {

class Json {
public:
    static std::string escape(std::string_view s) {
        std::string result;
        result.reserve(s.size() + 16);
        for (char c : s) {
            switch (c) {
                case '\"': result += "\\\""; break;
                case '\\': result += "\\\\"; break;
                case '\b': result += "\\b"; break;
                case '\f': result += "\\f"; break;
                case '\n': result += "\\n"; break;
                case '\r': result += "\\r"; break;
                case '\t': result += "\\t"; break;
                default:
                    if (static_cast<unsigned char>(c) < 0x20) {
                        char buf[8];
                        snprintf(buf, sizeof(buf), "\\u%04x", c);
                        result += buf;
                    } else {
                        result += c;
                    }
            }
        }
        return result;
    }

    static std::string quote(std::string_view s) {
        return "\"" + escape(s) + "\"";
    }

    // Simple flat/top-level JSON object parser (string, number, bool, null)
    static std::map<std::string, std::string> parseObject(const std::string& jsonStr) {
        std::map<std::string, std::string> result;
        size_t i = 0;
        size_t n = jsonStr.size();

        auto skipWhitespace = [&]() {
            while (i < n && std::isspace(static_cast<unsigned char>(jsonStr[i]))) {
                i++;
            }
        };

        skipWhitespace();
        if (i >= n || jsonStr[i] != '{') return result;
        i++; // skip '{'

        while (i < n) {
            skipWhitespace();
            if (i < n && jsonStr[i] == '}') {
                i++;
                break;
            }

            // Key must be string
            if (i >= n || jsonStr[i] != '\"') break;
            i++; // skip opening quote
            std::string key;
            while (i < n && jsonStr[i] != '\"') {
                if (jsonStr[i] == '\\' && i + 1 < n) {
                    i++;
                    char esc = jsonStr[i];
                    if (esc == 'n') key += '\n';
                    else if (esc == 't') key += '\t';
                    else if (esc == 'r') key += '\r';
                    else key += esc;
                } else {
                    key += jsonStr[i];
                }
                i++;
            }
            if (i < n && jsonStr[i] == '\"') i++;

            skipWhitespace();
            if (i >= n || jsonStr[i] != ':') break;
            i++; // skip ':'

            skipWhitespace();
            if (i >= n) break;

            // Value: string or literal
            std::string val;
            if (jsonStr[i] == '\"') {
                i++; // skip quote
                while (i < n && jsonStr[i] != '\"') {
                    if (jsonStr[i] == '\\' && i + 1 < n) {
                        i++;
                        char esc = jsonStr[i];
                        if (esc == 'n') val += '\n';
                        else if (esc == 't') val += '\t';
                        else if (esc == 'r') val += '\r';
                        else val += esc;
                    } else {
                        val += jsonStr[i];
                    }
                    i++;
                }
                if (i < n && jsonStr[i] == '\"') i++;
            } else {
                // Number / boolean / null / nested
                while (i < n && jsonStr[i] != ',' && jsonStr[i] != '}' && !std::isspace(static_cast<unsigned char>(jsonStr[i]))) {
                    val += jsonStr[i];
                    i++;
                }
            }

            result[key] = val;

            skipWhitespace();
            if (i < n && jsonStr[i] == ',') {
                i++;
            }
        }

        return result;
    }
};

} // namespace fintrack::util
