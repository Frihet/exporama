CREATE TABLE fileobject (
	id		SERIAL PRIMARY KEY,
	mime		text,
	contents	bytea
);

CREATE TABLE event (
	id		SERIAL PRIMARY KEY,
	code		text,
	state		char(1),
	name		text,
	administrator	text,
	startdate	timestamp,
	enddate		timestamp,
	theme		integer REFERENCES fileobject,
	logo		integer REFERENCES fileobject,
	badgelogo       integer REFERENCES fileobject
);

CREATE TABLE participant (
	id		SERIAL PRIMARY KEY,
	date		timestamp,
	event_id	integer REFERENCES event
);

CREATE TABLE visit (
	id		SERIAL PRIMARY KEY,
	date		date,
	time		time,
	scanner_id	integer,
	participant_id	integer REFERENCES participant,
	CONSTRAINT c1	UNIQUE(date,participant_id,scanner_id)
);

CREATE TABLE inputfield (
	id		SERIAL PRIMARY KEY,
	title		text,
	type		char(1),
	size		integer,
	optional	boolean,
	badge		integer,
	event_id	integer REFERENCES event
);

CREATE TABLE menuelement (
	id		SERIAL PRIMARY KEY,
	title		text,
	category	char(1),
	inputfield_id	integer REFERENCES inputfield
);

CREATE TABLE participantchoice (
	id		SERIAL PRIMARY KEY,
	data		text,
	inputfield_id	integer REFERENCES inputfield,
	menuelement_id	integer REFERENCES menuelement,
	participant_id	integer REFERENCES participant
);

CREATE TABLE inputtemplate (
	id		SERIAL PRIMARY KEY,
	name		text,
	template	text
);
