DROP TABLE FORECAST_CUSTOMERS;

CREATE TABLE FORECAST_CUSTOMERS (
  ID bigint NOT NULL AUTO_INCREMENT,
  NAME varchar(100) NOT NULL,
  ALIAS varchar(100),
  STREET varchar(100),
  ZIP varchar(100),
  CITY varchar(100),
  CONSTRAINT FORECAST_CUSTOMERS_PK PRIMARY KEY (ID)
);