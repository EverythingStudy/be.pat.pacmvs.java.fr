-- postgresql
ALTER TABLE "fr_annotation" ADD COLUMN "contour_type" int2 DEFAULT 0;
COMMENT ON COLUMN "fr_annotation"."contour_type" IS '标注类型：默认 0  1 粗轮廓 2 精细轮廓';
ALTER TABLE "fr_annotation_del" ADD COLUMN "contour_type" int2 DEFAULT 0;
COMMENT ON COLUMN "fr_annotation_del"."contour_type" IS '标注类型：默认 0  1 粗轮廓 2 精细轮廓';
ALTER TABLE "public"."fr_annotation" ADD COLUMN "single_slide_id" int8 DEFAULT 0;
COMMENT ON COLUMN "public"."fr_annotation"."single_slide_id" IS '单切片id';
-- 筛差表
DROP TABLE IF EXISTS "public"."fr_annotation_sd";
CREATE TABLE "public"."fr_annotation_sd" (
  "annotation_id" BIGSERIAL PRIMARY KEY,
  "area" numeric(16,4),
  "perimeter" numeric(16,4),
  "description" varchar(100),
  "tag_id" int8 DEFAULT 0,
  "contour" geometry(GEOMETRY),
  "location_type" varchar(100),
  "annotation_type" varchar(50) DEFAULT 'Draw',
  "create_by" int8,
  "create_time" timestamp(0) DEFAULT CURRENT_TIMESTAMP,
  "update_by" int8,
  "update_time" timestamp(0) DEFAULT CURRENT_TIMESTAMP,
  "slide_id" int8,
  "json_id" varchar(500),
  "single_slide_id" int8
)
;
COMMENT ON COLUMN "public"."fr_annotation_sd"."annotation_id" IS '主键id';
COMMENT ON COLUMN "public"."fr_annotation_sd"."area" IS '面积';
COMMENT ON COLUMN "public"."fr_annotation_sd"."perimeter" IS '周长';
COMMENT ON COLUMN "public"."fr_annotation_sd"."description" IS '轮廓描述';
COMMENT ON COLUMN "public"."fr_annotation_sd"."tag_id" IS '标签id';
COMMENT ON COLUMN "public"."fr_annotation_sd"."contour" IS '轮廓坐标625';
COMMENT ON COLUMN "public"."fr_annotation_sd"."location_type" IS '轮廓类型';
COMMENT ON COLUMN "public"."fr_annotation_sd"."annotation_type" IS '标注类型(AI表示AI算出的标注，Draw表示前端绘制的标注)';
COMMENT ON COLUMN "public"."fr_annotation_sd"."create_by" IS '创建者';
COMMENT ON COLUMN "public"."fr_annotation_sd"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."fr_annotation_sd"."update_by" IS '更新者';
COMMENT ON COLUMN "public"."fr_annotation_sd"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."fr_annotation_sd"."slide_id" IS '切片id';
COMMENT ON COLUMN "public"."fr_annotation_sd"."json_id" IS 'geojson中数据id';
COMMENT ON COLUMN "public"."fr_annotation_sd"."single_slide_id" IS '单切片id';
CREATE INDEX "idx_single_slide_id" ON "public"."fr_annotation_sd" USING btree (
  "single_slide_id"
);
CREATE INDEX "idx_slide_id" ON "public"."fr_annotation_sd" USING btree (
  "slide_id"
);