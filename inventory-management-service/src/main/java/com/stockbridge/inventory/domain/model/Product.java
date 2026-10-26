package com.stockbridge.inventory.domain.model;

import java.math.BigDecimal;

/**
 * Entidad de dominio. NO tiene anotaciones de JPA ni de Spring:
 * la entidad de base de datos es ProductEntity (capa de infraestructura).
 */
public class Product {

    private Long id;
    private String code;
    private String name;
    private String description;
    private String category;
    private BigDecimal price;
    private int stock;
    private boolean active;
    private Long version;

    public Product(Long id, String code, String name, String description, String category,
                   BigDecimal price, int stock, boolean active, Long version) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.description = description;
        this.category = category;
        this.price = price;
        this.stock = stock;
        this.active = active;
        this.version = version;
    }

    /** Fábrica para productos nuevos: valida las reglas y deja el producto activo. */
    public static Product create(String code, String name, String description, String category,
                                 BigDecimal price, int stock) {
        // TODO: precio > 0 y stock >= 0, si no lanzar InvalidDomainDataException
        throw new UnsupportedOperationException("TODO");
    }

    public void updateInfo(String name, String description, String category, BigDecimal price) {
        // TODO: validar precio > 0 y actualizar campos
        throw new UnsupportedOperationException("TODO");
    }

    /** Descuenta unidades. Las existencias nunca pueden quedar negativas. */
    public void decreaseStock(int quantity) {
        // TODO: quantity > 0, y si stock < quantity lanzar InsufficientStockException
        throw new UnsupportedOperationException("TODO");
    }

    public void increaseStock(int quantity) {
        // TODO: quantity > 0
        throw new UnsupportedOperationException("TODO");
    }

    public boolean hasEnoughStock(int quantity) {
        return stock >= quantity;
    }

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    public Long getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public BigDecimal getPrice() { return price; }
    public int getStock() { return stock; }
    public boolean isActive() { return active; }
    public Long getVersion() { return version; }
}
