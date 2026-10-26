package com.stockbridge.inventory.domain.model;

public class User {

    private Long id;
    private String fullName;
    private String email;
    private String password; // siempre el hash, nunca la contraseña en texto plano
    private Role role;
    private boolean active;

    public User(Long id, String fullName, String email, String password, Role role, boolean active) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.role = role;
        this.active = active;
    }

    /** Registro público: todo usuario nuevo nace como EMPLEADO. */
    public static User register(String fullName, String email, String encodedPassword) {
        return new User(null, fullName, email, encodedPassword, Role.EMPLEADO, true);
    }

    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public Role getRole() { return role; }
    public boolean isActive() { return active; }
}
