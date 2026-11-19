#pragma once

#include <string>
#include <optional>

namespace fintrack::model {

class User {
private:
    std::optional<int64_t> id;
    std::string name;
    std::string email;
    std::string passwordHash;
    std::string createdAt;

public:
    User() = default;
    User(std::optional<int64_t> id, std::string name, std::string email, std::string createdAt = "")
        : id(id), name(std::move(name)), email(std::move(email)), createdAt(std::move(createdAt)) {}

    std::optional<int64_t> getId() const { return id; }
    void setId(std::optional<int64_t> newId) { id = newId; }

    const std::string& getName() const { return name; }
    void setName(const std::string& newName) { name = newName; }

    const std::string& getEmail() const { return email; }
    void setEmail(const std::string& newEmail) { email = newEmail; }

    const std::string& getPasswordHash() const { return passwordHash; }
    void setPasswordHash(const std::string& hash) { passwordHash = hash; }

    const std::string& getCreatedAt() const { return createdAt; }
    void setCreatedAt(const std::string& time) { createdAt = time; }
};

} // namespace fintrack::model
