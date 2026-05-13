package ru.warehouse.api.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "suppliers")
public class Supplier {
    @Id @GeneratedValue
    private UUID id;
    @Column(nullable = false)
    private String name;
    private String contact;
    private String phone;
    private String email;

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getContact() { return contact; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public void setName(String name) { this.name = name; }
    public void setContact(String contact) { this.contact = contact; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setEmail(String email) { this.email = email; }
}
