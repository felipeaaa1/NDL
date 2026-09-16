CREATE INDEX IF NOT EXISTS idx_product_sku_cursor_active
    ON ecommerce.product_sku (created_at ASC, id DESC)
    WHERE active = true;
