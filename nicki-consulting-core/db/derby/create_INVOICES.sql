DROP TABLE APP.INVOICES;

CREATE TABLE APP.INVOICES (
  ID bigint NOT NULL GENERATED ALWAYS AS IDENTITY,
  PROJECT_ID bigint NOT NULL,
  INVOICE_NUMBER varchar(1000) NOT NULL,
  START_DATE timestamp,
  END_DATE timestamp,
  INVOICE_DATE timestamp,
  CONSTRAINT INVOICES_PK PRIMARY KEY (ID)
);
