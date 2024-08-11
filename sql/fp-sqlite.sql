CREATE TABLE accounts (
  id INTEGER PRIMARY KEY,
  playerUuid varchar(36) NOT NULL,
  name varchar(50) NOT NULL,
  password varchar(200) DEFAULT NULL,
  lastLogin varchar(19) DEFAULT NULL
);

CREATE TABLE account_options (
  id INTEGER PRIMARY KEY,
  accountId INTEGER NOT NULL,
  languageKey varchar(3) NOT NULL,
  buildMode tinyint(1) NOT NULL
);

CREATE TABLE characters (
  id INTEGER PRIMARY KEY,
  accountId INTEGER NOT NULL,
  name varchar(50) NOT NULL,
  locWorld varchar(50) NOT NULL,
  locX double NOT NULL,
  locY double NOT NULL,
  locZ double NOT NULL,
  locYaw float NOT NULL,
  locPitch float NOT NULL,
  active tinyint(1) NOT NULL,
  foodLevel INTEGER NOT NULL,
  characterClassIdentifier varchar(50) NOT NULL
);

CREATE TABLE save_points (
  id INTEGER PRIMARY KEY,
  characterId INTEGER NOT NULL,
  savePointId INTEGER NOT NULL
);

CREATE TABLE friends (
  id INTEGER PRIMARY KEY,
  accountId1 INTEGER NOT NULL,
  accountId2 INTEGER NOT NULL
);

CREATE TABLE friend_requests (
  id INTEGER PRIMARY KEY,
  requestingAccountId INTEGER NOT NULL,
  receivingAccountId INTEGER NOT NULL
);
