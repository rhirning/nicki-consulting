DROP TABLE APP.FORECAST_DEALS;

CREATE TABLE APP.FORECAST_DEALS (
  ID bigint NOT NULL GENERATED ALWAYS AS IDENTITY,
  CUSTOMER_ID bigint NOT NULL,
  STAGE_ID bigint NOT null,
  NAME varchar(100) NOT NULL,
  DESCRIPTION varchar(1000),
  CONTACT varchar(100),
  CATEGORY_ID bigint,
  SALES_ID bigint,
  START_DATE date,
  END_DATE date,
  TEAM_SIZE int,
  RATE float,
  PROBABILITY float,
  VALID_FROM timestamp NOT NULL,
  VALID_TO timestamp,
  CONSTRAINT FORECAST_DEALS_PK PRIMARY KEY (ID)
);