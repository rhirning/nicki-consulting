create view APP.TIME_VIEW AS
select e.BUSINESS_CATEGORY
     , e.ACCOUNT_ID
	 , e.MODIFY_TIME
	 , e.COMMAND
	 , e.MODIFIER
	 , c.CATEGORY
	 , c.ATTRIBUTE
	 , c.VALUE
  from APP.TIME t,
  		APP.MEMBERS m,
  		APP.PERSONS pe,
  		APP.PROJECTS pr,
  		APP.CUSTOMERS c
  where c.ID = pr.CUSTOMER_ID
  		and pr.ID = m.PROJECT_ID;
  
       left outer join
       APP.HISTORY_CHANGES c
    on e.ID = c.ENTRY_ID
 order by e.MODIFY_TIME ASC
;



CREATE VIEW APP.TIME_VIEW (
  ID bigint NOT NULL GENERATED ALWAYS AS IDENTITY,
  MEMBER_ID bigint NOT NULL,
  TEXT varchar(1000) NOT NULL,
  START_TIME timestamp,
  END_TIME timestamp,
  PAUSE float,
  HOURS float not null,
  CONSTRAINT TIME_PK PRIMARY KEY (ID)
);