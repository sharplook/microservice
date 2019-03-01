CREATE TABLE "public"."mail" (
                               "id" int4 NOT NULL,
                               "create_time" timestamp(6),
                               "modify_time" timestamp(6),
                               "web_id" int4,
                               "mail" varchar(255) COLLATE "default",
                               "use_for" varchar(255) COLLATE "default",
                               CONSTRAINT "mail_pkey" PRIMARY KEY ("id")
)
  WITH (OIDS=FALSE)
;

ALTER TABLE "public"."mail" OWNER TO "postgres";

COMMENT ON COLUMN "public"."mail"."create_time" IS '创建时间';

COMMENT ON COLUMN "public"."mail"."modify_time" IS '修改时间';

COMMENT ON COLUMN "public"."mail"."web_id" IS '站点id，1表示新浪，2表示QQ，3表示搜狐，4表示火狐';

COMMENT ON COLUMN "public"."mail"."mail" IS '邮箱名';

COMMENT ON COLUMN "public"."mail"."use_for" IS '邮箱用途';