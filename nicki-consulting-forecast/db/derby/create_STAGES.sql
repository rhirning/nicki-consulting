DROP TABLE APP.FORECAST_STAGES;

CREATE TABLE APP.FORECAST_STAGES (
  ID bigint NOT NULL GENERATED ALWAYS AS IDENTITY,
  NAME varchar(1000),
  MIN_PROB int,
  MAX_PROB int,
  POSITION int,
  CONSTRAINT FORECAST_STAGES_PK PRIMARY KEY (ID)
);