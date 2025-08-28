-- postgresql
ALTER TABLE "fr_annotation" ADD COLUMN "contour_type" int2 DEFAULT 0;
COMMENT ON COLUMN "fr_annotation"."contour_type" IS '标注类型：默认 0  1 粗轮廓 2 精细轮廓';
ALTER TABLE "fr_annotation_del" ADD COLUMN "contour_type" int2 DEFAULT 0;
COMMENT ON COLUMN "fr_annotation_del"."contour_type" IS '标注类型：默认 0  1 粗轮廓 2 精细轮廓';
ALTER TABLE "public"."fr_annotation" ADD COLUMN "single_slide_id" int8 DEFAULT 0;
COMMENT ON COLUMN "public"."fr_annotation"."single_slide_id" IS '单切片id';