ALTER TABLE "public"."fr_annotation" ADD COLUMN "structure_id" VARCHAR (16) COLLATE "pg_catalog"."default";
COMMENT ON COLUMN "public"."fr_annotation"."structure_id" IS '结构Id';