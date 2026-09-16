/*
 * create SKU index
 *
 * @author Felipe Alves
 * @date 24/03/2026
 */

CREATE INDEX IF NOT EXISTS idx_product_sku_cursor_active
    ON ecommerce.product_sku (created_at ASC, id DESC)
    WHERE active = true;