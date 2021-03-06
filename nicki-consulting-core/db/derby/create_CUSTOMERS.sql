DROP TABLE APP.CUSTOMERS;

CREATE TABLE APP.CUSTOMERS (
  ID bigint NOT NULL GENERATED ALWAYS AS IDENTITY,
  NAME varchar(1000) NOT NULL,
  PARENT_ID bigint,
  ALIAS varchar(1000),
  STREET varchar(1000),
  ZIP varchar(1000),
  CITY varchar(1000),
  INVOICE_TEMPLATE varchar(1000),
  TIMESHEET_TEMPLATE varchar(1000),
  CONSTRAINT CUSTOMERS_PK PRIMARY KEY (ID)
);