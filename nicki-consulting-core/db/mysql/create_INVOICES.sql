DROP TABLE INVOICES;

CREATE TABLE INVOICES (
  ID bigint NOT NULL AUTO_INCREMENT,
  PROJECT_ID bigint NOT NULL,
  INVOICE_NUMBER varchar(1000) NOT NULL,
  START_DATE timestamp,
  END_DATE timestamp,
  INVOICE_DATE timestamp,
  CONSTRAINT INVOICES_PK PRIMARY KEY (ID)
);
