/*
 * alter sku table
 *
 * @author Felipe Alves
 * @date 24/03/2026
 */

DO $$
BEGIN

    -- sku_code
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'ecommerce'
          AND table_name = 'product_sku'
          AND column_name = 'sku_code'
    ) THEN
        ALTER TABLE ecommerce.product_sku ADD sku_code VARCHAR(100) NOT NULL UNIQUE;
        RAISE NOTICE 'Coluna sku_code adicionada';
    ELSE
        RAISE NOTICE 'Coluna sku_code já existe';
    END IF;

    -- active
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'ecommerce'
          AND table_name = 'product_sku'
          AND column_name = 'active'
    ) THEN
        ALTER TABLE ecommerce.product_sku ADD active BOOLEAN NOT NULL default true;
        RAISE NOTICE 'Coluna active adicionada';
    ELSE
        RAISE NOTICE 'Coluna active já existe';
    END IF;

--created_by
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'ecommerce'
        AND table_name = 'product_sku'
        AND column_name = 'created_by'
    ) THEN
        ALTER TABLE ecommerce.product_sku
        ADD COLUMN created_by UUID
        REFERENCES ecommerce.app_user(id);
        RAISE NOTICE 'Coluna created_by adicionada';
    ELSE
        RAISE NOTICE 'Coluna created_by já existe';
    END IF;

    -- created_at
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'ecommerce'
        AND table_name = 'product_sku'
        AND column_name = 'created_at'
    ) THEN
        ALTER TABLE ecommerce.product_sku
        ADD COLUMN created_at TIMESTAMP
        NOT NULL
        DEFAULT CURRENT_TIMESTAMP;
        RAISE NOTICE 'Coluna created_at adicionada';
    ELSE
        RAISE NOTICE 'Coluna created_at já existe';
    END IF;

    -- updated_by
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'ecommerce'
        AND table_name = 'product_sku'
        AND column_name = 'updated_by'
    ) THEN
        ALTER TABLE ecommerce.product_sku
        ADD COLUMN updated_by UUID
        REFERENCES ecommerce.app_user(id);
        RAISE NOTICE 'Coluna updated_by adicionada';
    ELSE
        RAISE NOTICE 'Coluna updated_by já existe';
    END IF;

    --updated_at
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'ecommerce'
        AND table_name = 'product_sku'
        AND column_name = 'updated_at'
    ) THEN
        ALTER TABLE ecommerce.product_sku
        ADD COLUMN updated_at TIMESTAMP
        NOT NULL
        DEFAULT CURRENT_TIMESTAMP;
        RAISE NOTICE 'Coluna updated_at adicionada';
    ELSE
        RAISE NOTICE 'Coluna updated_at já existe';
    END IF;
END
$$;