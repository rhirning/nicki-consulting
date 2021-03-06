DROP TABLE PROJECTS;

CREATE TABLE PROJECTS (
  ID bigint NOT NULL AUTO_INCREMENT,
  CUSTOMER_ID bigint NOT NULL,
  NAME varchar(1000) NOT NULL,
  CUSTOMER_REFERENCE varchar(1000) NOT NULL,
  CONTACT varchar(1000) NOT NULL,
  PHONE varchar(1000),
  EMAIL varchar(1000),
  DAYS int,
  RATE float,
  ACTIVE int,
  VACATION int,
  START_DATE date,
  END_DATE date,
  OPEN_DATE date,
  INVOICE_TEMPLATE varchar(1000),
  TIMESHEET_TEMPLATE varchar(1000),
  CUSTOMER_REPORT int,
  CONSTRAINT PROJECTS_PK PRIMARY KEY (ID)
);
