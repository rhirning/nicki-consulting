DROP TABLE FORECAST_CATEGORIES;

CREATE TABLE FORECAST_CATEGORIES (
  ID bigint NOT NULL AUTO_INCREMENT,
  NAME varchar(100) NOT NULL,
  CONSTRAINT FORECAST_CATEGORIES_PK PRIMARY KEY (ID)
);