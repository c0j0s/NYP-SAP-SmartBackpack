create column table "NYPFYPJ03"."DEVICE"( "DEVICE_ID" BIGINT not null,
	 "LAST_ONLINE" TIMESTAMP null,
	 "SENSOR_HUMIDITY" VARCHAR (1) null default 'Y',
	 "SENSOR_TEMPERATURE" VARCHAR (1) null default 'Y',
	 "SENSOR_BUTTON" VARCHAR (1) null default 'Y',
	 "SENSOR_AIR_QUALITY" VARCHAR (1) null default 'Y',
	 "ACTUATOR_BUZZER" VARCHAR (1) null default 'Y',
	 "ACTUATOR_LED" VARCHAR (1) null default 'Y',
	 "ACTUALTOR_DISPLAY" VARCHAR (1) null,
	 "DEVICE_SN" VARCHAR (6) not null,
	 "MANUFACTURED_ON" TIMESTAMP null,
	 primary key ("DEVICE_ID") ) 
;
comment on column "NYPFYPJ03"."DEVICE"."DEVICE_ID" is 'database device id' 
;
comment on column "NYPFYPJ03"."DEVICE"."DEVICE_SN" is '6 char serial number'