package com.stockbridge.inventory.domain.model;

import java.math.BigDecimal;

import com.stockbridge.inventory.domain.exception.InsufficientStockException;
import com.stockbridge.inventory.domain.exception.InvalidDomainDataException;

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
        validatePrice(price);
        if(stock < 0){
            throw new InvalidDomainDataException("El stock no puede ser negativo");
        }
        return new Product(null,code,name,description,category,price,stock,true,null);
    }

    public void updateInfo(String name, String description, String category, BigDecimal price) {
        validatePrice(price);
        this.name = name;
        this.description = description;
        this.category = category;
        this.price = price;
    }

    /** Descuenta unidades. Las existencias nunca pueden quedar negativas. */
    public void decreaseStock(int quantity) {
        validatePositiveQuantity(quantity);
        if (!hasEnoughStock(quantity)){
            throw new InsufficientStockException(
                    "Stock insuficiente para " + code + ": disponible " + stock + ", solicitado"
            + quantity);
        }
        this.stock -= quantity;
    }

    public void increaseStock(int quantity) {
        validatePositiveQuantity(quantity);
        this.stock += quantity;
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

    private static void validatePrice(BigDecimal price){
        if(price == null || price.compareTo(BigDecimal.ZERO) <= 0){
            throw new InvalidDomainDataException("El precio debe ser mayor que cero");
        }
    }

    private static void validatePositiveQuantity(int quantity){
        if (quantity <= 0){
            throw new InvalidDomainDataException("La cantidad debe ser mayor que cero");
        }
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
