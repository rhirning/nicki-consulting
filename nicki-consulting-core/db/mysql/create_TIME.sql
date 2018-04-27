DROP TABLE TIME;

CREATE TABLE TIME (
  ID bigint NOT NULL AUTO_INCREMENT,
  MEMBER_ID bigint NOT NULL,
  TEXT varchar(1000) NOT NULL,
  START_TIME timestamp,
  END_TIME timestamp,
  PAUSE int,
  HOURS float not null,
  INVOICE_ID bigint,
  CONSTRAINT TIME_PK PRIMARY KEY (ID)
);